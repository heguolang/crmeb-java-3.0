package com.qxkj.service.service;

import java.util.Map;

/**
 * 合同文档服务：把合同正文统一成 PDF，并支持「在 PDF 指定坐标盖公章/签字/日期」。
 *
 * 背景（2026-10-02 第六批）：原来的做法是另生成一张「独立签署页 PNG」挂在合同旁边，
 * 客户下载下来合同正文上什么都没有。改为法大大式的做法：
 *   ① 后台在合同正文 PDF 上拖拽设置用印位置（百分比坐标，存 sign_layout）
 *   ② 签署时由服务端把公章图 / 签字图 / 日期文字按坐标叠加进 PDF，输出一份成品 PDF
 *
 * 转换（doc/docx/xls → PDF）走服务器上的 LibreOffice headless；
 * 叠加与渲染走 Apache PDFBox。日期文字用 Java2D 渲染成透明 PNG 再贴图，
 * 避免 PDFBox 嵌中文字体（会把 12MB 的字库塞进每份 PDF）。
 */
public interface ContractDocService {

    /** 用印结果 */
    class StampedFile {
        /** 相对 URL（qianxuimage/...） */
        public String url;
        /** 文件名 */
        public String name;
        /** sha256 十六进制 */
        public String hash;
    }

    /**
     * 确保合同正文有可用的 PDF：
     * PDF 原样返回；图片包成 PDF；doc/docx/xls 走 LibreOffice 转换。
     *
     * @param srcUrl  合同正文原始文件相对 URL（qianxuimage/...）
     * @param srcName 原始文件名（用于判断后缀与命名）
     * @return 转换后的 PDF 相对 URL（若本来就是 PDF 则原样返回 srcUrl）
     */
    String ensurePdf(String srcUrl, String srcName);

    /** 读取 PDF 页数；失败返回 0 */
    int pageCount(String pdfUrl);

    /**
     * 按签约方名称生成一颗「圆形标准公章」PNG（服务端 Java2D 绘制），返回相对 URL。
     *
     * 用途：甲方（客户）的公章不再由后台预设、也不需要客户在 H5 上传 —— 签署时服务端
     * 直接按合同里的甲方主体名称画一颗圆章盖上去；乙方同理。
     *
     * @param companyName 签约方名称（企业全称）
     * @return 生成的 PNG 相对 URL（qianxuimage/public/contract/...）
     */
    String makeRoundSeal(String companyName);

    /**
     * 把 PDF 的某一页渲染成 PNG，供后台「用印位置设置」当底图。
     *
     * @param pdfUrl   正文 PDF 相对 URL
     * @param pageNo   页码，从 1 开始
     * @param maxWidth 输出图片的最大宽度（像素）
     * @return PNG 字节
     */
    byte[] renderPagePng(String pdfUrl, int pageNo, int maxWidth);

    /**
     * 按用印布局把公章 / 签字 / 日期叠加到 PDF 上，生成新的成品 PDF。
     *
     * @param srcPdfUrl  源 PDF 相对 URL
     * @param layoutJson 用印布局 JSON 数组，元素形如
     *                   {"type":"ourSeal","page":3,"x":56,"y":72,"w":13,"h":13,"tilt":-4}
     *                   type 取值：ourSeal / ourSign / ourDate（乙方黔序）
     *                            · custSeal / custSign / custDate（甲方客户）
     * @param images     图片相对 URL，key 为 ourSeal / ourSign / custSeal / custSign；
     *                   缺省或空则**跳过**该元素（用印分两次进行：先乙方，客户签时再补甲方）
     * @param texts      日期文字，key 为 ourDate / custDate；缺省或空则跳过对应的日期元素
     * @return 成品文件信息（url / name / hash）
     */
    StampedFile stamp(String srcPdfUrl, String layoutJson, Map<String, String> images, Map<String, String> texts);
}
