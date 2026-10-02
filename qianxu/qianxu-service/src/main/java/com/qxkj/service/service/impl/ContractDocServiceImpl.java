package com.qxkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.UploadUtil;
import com.qxkj.service.service.ContractDocService;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 合同文档服务实现。
 *
 * 三个能力：
 *   ① ensurePdf  —— 正文统一成 PDF（图片包页 / Office 走 LibreOffice / PDF 原样）
 *   ② renderPagePng —— 把某页渲染成 PNG，给后台当「用印位置设置」的底图
 *   ③ stamp —— 按百分比坐标把公章/签字/日期叠加进 PDF，输出成品
 *
 * 坐标约定（与前端一致）：x/y/w/h 都是相对页面宽高的百分比，x/y 以**页面左上角**为原点。
 * PDF 的坐标系原点在左下角，所以换算成 PDF 坐标时要做 y 翻转。
 */
@Service
public class ContractDocServiceImpl implements ContractDocService {

    private static final Logger logger = LoggerFactory.getLogger(ContractDocServiceImpl.class);

    /** 本地图片根目录下的业务关键字（与上传保持一致，nginx 只放行这个目录） */
    private static final String IMAGE_KEYWORD = UploadConstants.UPLOAD_FILE_KEYWORD;

    /** 生成物统一落在 qianxuimage/public/contract/yyyy/MM/dd/ 下 */
    private static final String SUB_DIR = "public/contract/";

    /** 中文字体（用于把日期渲染成图片，避免 PDFBox 嵌 12MB 字库） */
    private static final String FONT_RES = "fonts/WenQuanZhengHei.ttf";

    /** LibreOffice 转换必须串行：混跑机内存紧张，且 soffice 多实例会抢 profile 锁 */
    private static final Object SOFFICE_LOCK = new Object();
    private static final long SOFFICE_TIMEOUT_SECONDS = 90;

    private static final List<String> IMAGE_EXTS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final List<String> OFFICE_EXTS = Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx", "wps", "et");

    private static volatile Font CJK_FONT;

    @Autowired
    private QianxuConfig qianxuConfig;

    private final Gson gson = new Gson();

    // ============================================================
    // ① 正文统一成 PDF
    // ============================================================

    @Override
    public String ensurePdf(String srcUrl, String srcName) {
        if (StrUtil.isBlank(srcUrl)) {
            return "";
        }
        String ext = extOf(StrUtil.isNotBlank(srcName) ? srcName : srcUrl);
        if ("pdf".equals(ext)) {
            return srcUrl;
        }
        File src = fileOf(srcUrl);
        if (!src.exists()) {
            throw new QianxuException("合同正文文件不存在，请重新上传");
        }
        if (IMAGE_EXTS.contains(ext)) {
            return imageToPdf(src, ext);
        }
        if (OFFICE_EXTS.contains(ext)) {
            return officeToPdf(src);
        }
        throw new QianxuException("暂不支持该格式作为合同正文：" + ext
                + "（支持 PDF / 图片 / Word / Excel），请先另存为 PDF 再上传");
    }

    /** 图片：一页 PDF，页面尺寸按图片像素 1:1（pt），图片铺满整页 */
    private String imageToPdf(File src, String ext) {
        Output out = newOutput("pdf");
        try (PDDocument doc = new PDDocument()) {
            PDImageXObject img = PDImageXObject.createFromFileByContent(src, doc);
            float w = Math.max(1f, img.getWidth());
            float h = Math.max(1f, img.getHeight());
            PDPage page = new PDPage(new PDRectangle(w, h));
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(img, 0, 0, w, h);
            }
            doc.save(out.file);
        } catch (Exception e) {
            out.file.delete();
            logger.error("图片转 PDF 失败：{}", e.getMessage(), e);
            throw new QianxuException("图片转 PDF 失败：" + e.getMessage());
        }
        return out.relativeUrl;
    }

    /** Office 文档：LibreOffice headless 转 PDF */
    private String officeToPdf(File src) {
        String bin = sofficeBin();
        if (bin == null) {
            throw new QianxuException("服务器未安装 LibreOffice，无法自动转换 Word/Excel，请把合同另存为 PDF 后重新上传");
        }
        synchronized (SOFFICE_LOCK) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qifu-office-" + System.nanoTime());
            if (!tmpDir.mkdirs() && !tmpDir.isDirectory()) {
                throw new QianxuException("创建转换临时目录失败");
            }
            try {
                // 复制到临时目录：源文件名可能含中文/特殊字符，直接传路径容易被 soffice 解析出错
                File local = new File(tmpDir, "src." + extOf(src.getName()));
                Files.copy(src.toPath(), local.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                ProcessBuilder pb = new ProcessBuilder(
                        bin, "--headless", "--norestore", "--nolockcheck", "--nodefault",
                        "-env:UserInstallation=file://" + new File(tmpDir, "profile").getAbsolutePath(),
                        "--convert-to", "pdf", "--outdir", tmpDir.getAbsolutePath(), local.getAbsolutePath());
                pb.directory(tmpDir);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                // 必须把输出读干净，否则 soffice 可能因管道写满而阻塞
                try (InputStream is = p.getInputStream()) {
                    byte[] buf = new byte[4096];
                    while (is.read(buf) > 0) {
                        // 丢弃；只为防止缓冲区写满
                    }
                }
                if (!p.waitFor(SOFFICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                    throw new QianxuException("Word/Excel 转 PDF 超时（" + SOFFICE_TIMEOUT_SECONDS + "秒），请把合同另存为 PDF 后重新上传");
                }
                File produced = new File(tmpDir, "src.pdf");
                if (!produced.exists() || produced.length() == 0) {
                    throw new QianxuException("Word/Excel 转 PDF 失败，请把合同另存为 PDF 后重新上传");
                }
                Output out = newOutput("pdf");
                Files.copy(produced.toPath(), out.file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return out.relativeUrl;
            } catch (QianxuException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Office 转 PDF 失败：{}", e.getMessage(), e);
                throw new QianxuException("Word/Excel 转 PDF 失败，请把合同另存为 PDF 后重新上传");
            } finally {
                deleteQuietly(tmpDir);
            }
        }
    }

    /** 找 soffice 可执行文件；环境变量 SOFFICE_BIN 可覆盖（本地开发指到自定义路径） */
    private String sofficeBin() {
        List<String> candidates = new ArrayList<>();
        String env = System.getenv("SOFFICE_BIN");
        if (StrUtil.isNotBlank(env)) {
            candidates.add(env);
        }
        candidates.add("/usr/bin/soffice");
        candidates.add("/usr/local/bin/soffice");
        candidates.add("/opt/libreoffice/program/soffice");
        candidates.add("/Applications/LibreOffice.app/Contents/MacOS/soffice");
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile() && f.canExecute()) {
                return c;
            }
        }
        return null;
    }

    // ============================================================
    // ② 渲染页面为 PNG（后台定位底图）
    // ============================================================

    @Override
    public int pageCount(String pdfUrl) {
        if (StrUtil.isBlank(pdfUrl)) {
            return 0;
        }
        try (PDDocument doc = PDDocument.load(fileOf(pdfUrl))) {
            return doc.getNumberOfPages();
        } catch (Exception e) {
            logger.error("读取 PDF 页数失败：{}", e.getMessage());
            return 0;
        }
    }

    @Override
    public byte[] renderPagePng(String pdfUrl, int pageNo, int maxWidth) {
        if (StrUtil.isBlank(pdfUrl)) {
            throw new QianxuException("合同正文尚未转换为 PDF");
        }
        File src = fileOf(pdfUrl);
        if (!src.exists()) {
            throw new QianxuException("合同正文文件不存在");
        }
        try (PDDocument doc = PDDocument.load(src)) {
            int total = doc.getNumberOfPages();
            int idx = Math.max(1, Math.min(pageNo, total)) - 1;
            PDPage page = doc.getPage(idx);
            float ptWidth = page.getMediaBox().getWidth();
            float scale = Math.max(0.2f, Math.min(6f, (maxWidth > 0 ? maxWidth : 1200) / ptWidth));
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImage(idx, scale, ImageType.RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            logger.error("渲染 PDF 页面失败：{}", e.getMessage(), e);
            throw new QianxuException("渲染合同页面失败：" + e.getMessage());
        }
    }

    // ============================================================
    // ③ 按坐标叠加用印
    // ============================================================

    @Override
    public StampedFile stamp(String srcPdfUrl, String layoutJson, Map<String, String> images, Map<String, String> texts) {
        if (StrUtil.isBlank(srcPdfUrl)) {
            throw new QianxuException("合同正文尚未转换为 PDF，无法用印");
        }
        File src = fileOf(srcPdfUrl);
        if (!src.exists()) {
            throw new QianxuException("合同正文文件不存在，无法用印");
        }
        List<LayoutItem> items = parseLayout(layoutJson);
        if (items.isEmpty()) {
            throw new QianxuException("请先在后台设置用印位置");
        }
        Map<String, String> imgs = images == null ? new HashMap<>() : images;
        Map<String, String> txs = texts == null ? new HashMap<>() : texts;

        Output out = newOutput("pdf");
        try (PDDocument doc = PDDocument.load(src)) {
            int total = doc.getNumberOfPages();
            for (LayoutItem it : items) {
                if (it.page < 1 || it.page > total) {
                    continue;
                }
                String type = normalizeType(it.type);
                boolean isDate = type.endsWith("Date");
                String url = isDate ? null : imgs.get(type);
                if (!isDate && StrUtil.isBlank(url)) {
                    continue;
                }
                String text = isDate ? txs.get(type) : null;
                if (isDate && StrUtil.isBlank(text)) {
                    continue;
                }
                PDPage page = doc.getPage(it.page - 1);
                PDRectangle box = page.getMediaBox();
                float pw = box.getWidth();
                float ph = box.getHeight();

                float w = pw * it.w / 100f;
                float h = ph * it.h / 100f;
                if (w <= 0 || h <= 0) {
                    continue;
                }
                float x = pw * it.x / 100f;
                // 百分比 y 以页面顶部为原点 → 换成 PDF 的左下角原点
                float y = ph - ph * it.y / 100f - h;

                PDImageXObject image;
                File datePng = null;
                try {
                    if (isDate) {
                        datePng = renderDatePng(text, w, h);
                        image = PDImageXObject.createFromFileByContent(datePng, doc);
                    } else {
                        File f = fileOf(url);
                        if (!f.exists()) {
                            logger.warn("用印图片不存在，跳过：{}", url);
                            continue;
                        }
                        image = PDImageXObject.createFromFileByContent(f, doc);
                    }

                try (PDPageContentStream cs = new PDPageContentStream(doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    if (Math.abs(it.tilt) > 0.01f) {
                        // 绕框中心旋转：盖印手不会端得笔直，轻微倾斜更像真章。
                        // ⚠ 两个坑（2026-10-02 各踩一次）：
                        //   ① Matrix.getRotateInstance(theta, tx, ty) 是「绕原点旋转 + 平移」，
                        //      不是「绕 (tx,ty) 旋转」——直接用会把章甩到页面外；
                        //   ② PDPageContentStream.transform() 对 CTM 是**后乘**，多次 transform
                        //      的调用顺序与直觉相反，T(-C)/R/T(C) 这种写法照样错。
                        //   → 直接把「p' = R·(p - C) + C」展开成**单个** PDF 矩阵，无拼接、无歧义。
                        float rad = (float) Math.toRadians(it.tilt);
                        float cos = (float) Math.cos(rad);
                        float sin = (float) Math.sin(rad);
                        float cx = x + w / 2f;
                        float cy = y + h / 2f;
                        cs.saveGraphicsState();
                        cs.transform(new Matrix(cos, sin, -sin, cos,
                                cx - (cos * cx - sin * cy),
                                cy - (sin * cx + cos * cy)));
                        cs.drawImage(image, x, y, w, h);
                        cs.restoreGraphicsState();
                    } else {
                        cs.drawImage(image, x, y, w, h);
                    }
                }
                } finally {
                    if (datePng != null) {
                        datePng.delete();
                    }
                }
            }
            doc.save(out.file);
        } catch (QianxuException e) {
            out.file.delete();
            throw e;
        } catch (Exception e) {
            out.file.delete();
            logger.error("合同用印失败：{}", e.getMessage(), e);
            throw new QianxuException("合同用印失败：" + e.getMessage());
        }

        StampedFile r = new StampedFile();
        r.url = out.relativeUrl;
        r.name = out.fileName;
        r.hash = sha256(out.file);
        return r;
    }

    /** 元素类型归一：老数据里的 "date" 视为乙方的日期位 */
    private static String normalizeType(String type) {
        if (StrUtil.isBlank(type)) {
            return "";
        }
        String t = type.trim();
        return "date".equals(t) ? "ourDate" : t;
    }


    /** 把日期文字渲染成一张「与目标框同宽高比」的透明 PNG，贴进 PDF 后比例不失真 */
    private File renderDatePng(String text, float targetW, float targetH) throws Exception {
        int ph = 160;
        int pw = Math.max(24, Math.round(ph * (targetW / targetH)));
        BufferedImage img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(20, 20, 20));
            float fs = Math.min(ph * 0.8f, (pw * 0.94f) / Math.max(text.length(), 1));
            Font f = cjkFont().deriveFont(Font.PLAIN, Math.max(8f, fs));
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.drawString(text, (pw - tw) / 2f, (ph - fm.getHeight()) / 2f + fm.getAscent());
        } finally {
            g.dispose();
        }
        File tmp = File.createTempFile("qifu-date-", ".png");
        ImageIO.write(img, "png", tmp);
        return tmp;
    }

    /** 中文字体：走项目内自带的文泉正黑（TTF），不依赖服务器字体配置 */
    private static Font cjkFont() {
        Font f = CJK_FONT;
        if (f != null) {
            return f;
        }
        synchronized (ContractDocServiceImpl.class) {
            if (CJK_FONT != null) {
                return CJK_FONT;
            }
            try (InputStream is = ContractDocServiceImpl.class.getClassLoader().getResourceAsStream(FONT_RES)) {
                if (is == null) {
                    throw new QianxuException("缺少中文字体资源 " + FONT_RES + "，无法在 PDF 上生成日期");
                }
                CJK_FONT = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (QianxuException e) {
                throw e;
            } catch (Exception e) {
                throw new QianxuException("加载中文字体失败：" + e.getMessage());
            }
            return CJK_FONT;
        }
    }

    // ============================================================
    // 工具
    // ============================================================

    /** 布局项 */
    private static class LayoutItem {
        String type;
        int page = 1;
        float x;
        float y;
        float w;
        float h;
        float tilt;
    }

    private List<LayoutItem> parseLayout(String layoutJson) {
        if (StrUtil.isBlank(layoutJson)) {
            return new ArrayList<>();
        }
        try {
            List<LayoutItem> list = gson.fromJson(layoutJson, new TypeToken<List<LayoutItem>>() {
            }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logger.error("解析用印布局失败：{}", e.getMessage());
            throw new QianxuException("用印位置数据有误，请重新在后台设置");
        }
    }

    /** 相对 URL → 本地文件 */
    private File fileOf(String relUrl) {
        String root = qianxuConfig.getImagePath().trim();
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        String u = relUrl.trim();
        int i = u.indexOf(IMAGE_KEYWORD + "/");
        if (i >= 0) {
            u = u.substring(i);
        }
        // 去掉可能带的查询串
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        return new File(FilenameUtils.separatorsToSystem(root + u));
    }

    /** 生成一个正式输出文件（含相对 URL 与文件名） */
    private Output newOutput(String ext) {
        String root = qianxuConfig.getImagePath().trim();
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        String webDir = IMAGE_KEYWORD + "/" + SUB_DIR + QianxuDateUtil.nowDate("yyyy/MM/dd") + "/";
        String fileName = UploadUtil.fileName(ext);
        try {
            File f = UploadUtil.createFile(FilenameUtils.separatorsToSystem(root + webDir + fileName));
            Output o = new Output();
            o.file = f;
            o.fileName = fileName;
            o.relativeUrl = webDir + fileName;
            return o;
        } catch (Exception e) {
            throw new QianxuException("生成合同文件失败：" + e.getMessage());
        }
    }

    private static class Output {
        File file;
        String fileName;
        String relativeUrl;
    }

    private static String extOf(String name) {
        String s = StrUtil.isBlank(name) ? "" : name.trim();
        int q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        String ext = FilenameUtils.getExtension(s);
        return ext == null ? "" : ext.toLowerCase();
    }

    private static String sha256(File f) {
        try (InputStream is = Files.newInputStream(f.toPath())) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (Exception e) {
            logger.error("计算文件摘要失败：{}", e.getMessage());
            return "";
        }
    }

    private static void deleteQuietly(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File c : children) {
                deleteQuietly(c);
            }
        }
        if (!dir.delete()) {
            logger.warn("临时文件删除失败：{}", dir.getAbsolutePath());
        }
    }
}
