package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.model.user.User;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.dao.SvcCaseDao;
import com.qxkj.service.dao.SvcContractDao;
import com.qxkj.service.dao.SvcProjectDao;
import com.qxkj.service.dao.SvcProjectProgressDao;
import com.qxkj.service.dao.UserDao;
import com.qxkj.service.service.ContractDocService;
import com.qxkj.service.service.SmsService;
import com.qxkj.service.service.SvcService;
import com.qxkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业服务实现（合同签约 + 项目进度 + 客户案例 + 服务设置）
 */
@Slf4j
@Service
public class SvcServiceImpl extends ServiceImpl<SvcProjectDao, SvcProject> implements SvcService {

    @Resource
    private SvcContractDao svcContractDao;

    @Resource
    private SvcProjectDao svcProjectDao;

    @Resource
    private SvcProjectProgressDao svcProjectProgressDao;

    @Resource
    private SvcCaseDao svcCaseDao;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ContractDocService contractDocService;

    // ==================== 私有辅助 ====================

    /** 会员在合同中的角色 */
    private static final String ROLE_CUST = "cust";
    private static final String ROLE_OUR = "our";

    /** 乙方（黔序）签约方默认值的配置键 */
    private static final String CFG_OUR_UID = "svc_our_uid";
    private static final String CFG_OUR_COMPANY = "svc_our_company";
    private static final String CFG_OUR_CODE = "svc_our_code";
    private static final String CFG_OUR_LEGAL = "svc_our_legal";
    private static final String CFG_OUR_PHONE = "svc_our_phone";
    private static final String CFG_OUR_ADDR = "svc_our_addr";

    private String makeContractNo() {
        // 合同编号：HT + yyyyMMdd + 6 位随机数
        String date = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        return "HT" + date + cn.hutool.core.util.RandomUtil.randomNumbers(6);
    }

    /** 当前登录者在合同中的角色：命中 uid=甲方，命中 our_uid=乙方，都不命中返回空串 */
    private String roleOf(SvcContract c, Integer uid) {
        if (c == null || uid == null || uid <= 0) {
            return "";
        }
        if (uid.equals(c.getUid())) {
            return ROLE_CUST;
        }
        if (c.getOurUid() != null && c.getOurUid() > 0 && uid.equals(c.getOurUid())) {
            return ROLE_OUR;
        }
        return "";
    }

    /** 甲方是否已签署（签署时间 + 手写签名都在才算） */
    private static boolean custSigned(SvcContract c) {
        return c != null && c.getSignTime() != null && !blankToEmpty(c.getSignHand()).isEmpty();
    }

    /** 乙方是否已签署 */
    private static boolean ourSigned(SvcContract c) {
        return c != null && c.getOurSignedAt() != null && !blankToEmpty(c.getOurSign()).isEmpty();
    }

    /** 双方齐备且正式合同已生成 */
    private static boolean finished(SvcContract c) {
        return c != null && !blankToEmpty(c.getSignedFile()).isEmpty();
    }

    /**
     * 补齐合同的「人」的信息：甲方/乙方会员昵称手机 + 双方签署进度 + 当前登录者角色。
     * 列表与详情共用；uid 为空时（后台列表）只填昵称手机与进度，角色留空。
     */
    private void fillContract(List<SvcContract> list, Integer uid) {
        if (list == null || list.isEmpty()) return;
        for (SvcContract c : list) {
            fillContract(c, uid);
        }
    }

    private void fillContract(SvcContract c, Integer uid) {
        if (c == null) return;
        User cust = c.getUid() == null ? null : userDao.selectById(c.getUid());
        if (cust != null) {
            c.setNickname(cust.getNickname());
            c.setPhone(cust.getPhone());
        }
        if (c.getOurUid() != null && c.getOurUid() > 0) {
            User our = userDao.selectById(c.getOurUid());
            if (our != null) {
                c.setOurNickname(our.getNickname());
                c.setOurPhoneNo(our.getPhone());
            }
        }
        c.setCustSigned(custSigned(c));
        c.setOurSigned(ourSigned(c));
        c.setFinished(finished(c));
        c.setMyRole(roleOf(c, uid));
    }

    private void fillProject(List<SvcProject> list, boolean withProgress) {
        if (list == null || list.isEmpty()) return;
        for (SvcProject p : list) {
            p.setStatusText(statusText(p.getStatus()));
            User user = userDao.selectById(p.getUid());
            if (user != null) {
                p.setNickname(user.getNickname());
                p.setPhone(user.getPhone());
            }
            if (withProgress) {
                p.setProgressList(progressListOf(p.getId()));
            }
        }
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "需求确认";
            case 2: return "开发中";
            case 3: return "待验收";
            case 4: return "已完成";
            case -1: return "已终止";
            default: return "";
        }
    }

    // ==================== 后台：合同 ====================

    @Override
    public CommonPage<SvcContract> adminContractList(Integer uid, String keywords, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcContract::getUid, uid);
        if (status != null) lqw.eq(SvcContract::getStatus, status);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.and(w -> w.like(SvcContract::getTitle, keywords.trim())
                    .or().like(SvcContract::getContractNo, keywords.trim()));
        }
        lqw.orderByDesc(SvcContract::getId);
        List<SvcContract> list = svcContractDao.selectList(lqw);
        fillContract(list, null);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcContract adminContractInfo(Integer id) {
        SvcContract contract = mustContract(id);
        fillContract(contract, null);
        return contract;
    }

    @Override
    public Integer saveContract(SvcRequests.ContractSaveRequest request) {
        String annex = request.getAnnex() == null ? "" : request.getAnnex().trim();
        if (request.getId() != null && request.getId() > 0) {
            SvcContract exist = svcContractDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("合同不存在");
            }
            boolean annexChanged = !annex.equals(exist.getAnnex() == null ? "" : exist.getAnnex().trim());
            exist.setUid(request.getUid());
            exist.setTitle(request.getTitle());
            exist.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
            if (request.getPartyType() != null) exist.setPartyType(request.getPartyType());
            exist.setPartyName(request.getPartyName() == null ? "" : request.getPartyName());
            exist.setAnnex(annex);
            exist.setAnnexName(request.getAnnexName() == null ? "" : request.getAnnexName());
            exist.setSignPage(request.getSignPage() == null ? 0 : request.getSignPage());
            exist.setSignPos(request.getSignPos() == null ? "center" : request.getSignPos());
            exist.setSealPos(request.getSealPos() == null ? "center" : request.getSealPos());
            exist.setContent(request.getContent() == null ? "" : request.getContent());
            exist.setRemark(request.getRemark() == null ? "" : request.getRemark());
            fillSignParties(exist, request);
            if (annexChanged) {
                // 正文换了 → 已转换的 PDF、已设定的用印位置、双方已签的件全部作废，需重新转、重新定位、重新签
                exist.setSrcPdf("");
                exist.setSrcPdfName("");
                exist.setPageCount(0);
                exist.setSignLayout("");
                exist.setOurStampedFile("");
                exist.setOurStampedName("");
                exist.setCustStampedFile("");
                exist.setCustStampedName("");
                exist.setSignedFile("");
                exist.setSignedFileName("");
                exist.setFileHash("");
                exist.setSignName("");
                exist.setSignPhone("");
                exist.setSignHand("");
                exist.setSignTime(null);
                exist.setCustSeal("");
                exist.setOurSigner("");
                exist.setOurSign("");
                exist.setOurSignedAt(null);
                exist.setStatus(SvcContract.STATUS_WAIT_SIGN);
            }
            svcContractDao.updateById(exist);
            return exist.getId();
        }
        SvcContract contract = new SvcContract();
        contract.setContractNo(makeContractNo());
        contract.setUid(request.getUid());
        contract.setTitle(request.getTitle());
        contract.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
        contract.setPartyType(request.getPartyType() == null ? 2 : request.getPartyType());
        contract.setPartyName(request.getPartyName() == null ? "" : request.getPartyName());
        contract.setAnnex(annex);
        contract.setAnnexName(request.getAnnexName() == null ? "" : request.getAnnexName());
        contract.setSignPage(request.getSignPage() == null ? 0 : request.getSignPage());
        contract.setSignPos(request.getSignPos() == null ? "center" : request.getSignPos());
        contract.setSealPos(request.getSealPos() == null ? "center" : request.getSealPos());
        contract.setContent(request.getContent() == null ? "" : request.getContent());
        contract.setRemark(request.getRemark() == null ? "" : request.getRemark());
        contract.setSrcPdf("");
        contract.setSrcPdfName("");
        contract.setPageCount(0);
        contract.setSignLayout("");
        contract.setOurStampedFile("");
        contract.setOurStampedName("");
        contract.setCustStampedFile("");
        contract.setCustStampedName("");
        contract.setSignedFile("");
        contract.setSignedFileName("");
        contract.setFileHash("");
        fillSignParties(contract, request);
        contract.setStatus(SvcContract.STATUS_WAIT_SIGN);
        contract.setIsDel(0);
        svcContractDao.insert(contract);
        return contract.getId();
    }

    /**
     * 甲乙双方签约方信息，update / insert 两支共用。
     * 公章不在这里预设：甲方的圆形章由服务端在签署时按主体名称生成，乙方同理（也支持后台上传覆盖）。
     */
    private void fillSignParties(SvcContract target, SvcRequests.ContractSaveRequest request) {
        // 乙方（黔序）
        target.setOurUid(request.getOurUid() == null ? 0 : request.getOurUid());
        target.setOurCompany(blankToEmpty(request.getOurCompany()));
        target.setOurCode(blankToEmpty(request.getOurCode()));
        target.setOurLegal(blankToEmpty(request.getOurLegal()));
        target.setOurPhone(blankToEmpty(request.getOurPhone()));
        target.setOurAddr(blankToEmpty(request.getOurAddr()));
        target.setOurSeal(blankToEmpty(request.getOurSeal()));
        // 甲方（客户）
        target.setCustCode(blankToEmpty(request.getCustCode()));
        target.setCustLegal(blankToEmpty(request.getCustLegal()));
        target.setCustPhone(blankToEmpty(request.getCustPhone()));
        target.setCustAddr(blankToEmpty(request.getCustAddr()));
        target.setCustSeal(blankToEmpty(request.getCustSeal()));
    }

    @Override
    public Boolean deleteContract(Integer id) {
        SvcContract exist = svcContractDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("合同不存在");
        }
        exist.setIsDel(1);
        return svcContractDao.updateById(exist) > 0;
    }

    // ==================== 后台：合同用印位置 + 双方签章 ====================

    @Override
    public HashMap<String, Object> prepareContractPdf(Integer id) {
        SvcContract contract = mustContract(id);
        ensureContractPdf(contract);
        fillContract(contract, null);
        return contractMeta(contract);
    }

    @Override
    public Boolean saveContractLayout(Integer id, String signLayout) {
        SvcContract contract = mustContract(id);
        contract.setSignLayout(signLayout == null ? "" : signLayout.trim());
        return svcContractDao.updateById(contract) > 0;
    }

    @Override
    public byte[] renderContractPage(Integer id, int pageNo, int maxWidth) {
        SvcContract contract = mustContract(id);
        String pdfUrl = blankToEmpty(contract.getSrcPdf());
        if (pdfUrl.isEmpty()) {
            pdfUrl = ensureContractPdf(contract);
        }
        return contractDocService.renderPagePng(pdfUrl, pageNo, maxWidth);
    }

    // ---------- 乙方（黔序）签约方默认值：后台填一次、自动带出、可随时改 ----------
    // 存 eb_system_config（svc_our_*），走 SystemConfigService 自动建行并刷新 Redis，会员端读到的一致。

    @Override
    public HashMap<String, Object> ourDefault() {
        HashMap<String, Object> map = new HashMap<>();
        Integer ourUid = intOf(systemConfigService.getValueByKey(CFG_OUR_UID));
        map.put("ourUid", ourUid == null ? 0 : ourUid);
        map.put("ourCompany", blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_COMPANY)));
        map.put("ourCode", blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_CODE)));
        map.put("ourLegal", blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_LEGAL)));
        map.put("ourPhone", blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_PHONE)));
        map.put("ourAddr", blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_ADDR)));
        User our = (ourUid != null && ourUid > 0) ? userDao.selectById(ourUid) : null;
        map.put("ourNickname", our == null ? "" : blankToEmpty(our.getNickname()));
        map.put("ourPhoneNo", our == null ? "" : blankToEmpty(our.getPhone()));
        return map;
    }

    @Override
    public Boolean saveOurDefault(SvcRequests.ContractOurDefaultRequest request) {
        systemConfigService.updateOrSaveValueByName(CFG_OUR_UID,
                request.getOurUid() == null ? "0" : String.valueOf(request.getOurUid()));
        systemConfigService.updateOrSaveValueByName(CFG_OUR_COMPANY, blankToEmpty(request.getOurCompany()));
        systemConfigService.updateOrSaveValueByName(CFG_OUR_CODE, blankToEmpty(request.getOurCode()));
        systemConfigService.updateOrSaveValueByName(CFG_OUR_LEGAL, blankToEmpty(request.getOurLegal()));
        systemConfigService.updateOrSaveValueByName(CFG_OUR_PHONE, blankToEmpty(request.getOurPhone()));
        systemConfigService.updateOrSaveValueByName(CFG_OUR_ADDR, blankToEmpty(request.getOurAddr()));
        return true;
    }

    /** 合同必须存在且未删除 */
    private SvcContract mustContract(Integer id) {
        SvcContract contract = id == null ? null : svcContractDao.selectById(id);
        if (contract == null || contract.getIsDel() > 0) {
            throw new RuntimeException("合同不存在");
        }
        return contract;
    }

    /**
     * 确保合同正文有可用的 PDF：没有 srcPdf 时取 annex 第一份文件转换，并把结果落库。
     * doc/docx/xls 由 ContractDocService 走服务器上的 LibreOffice 转换。
     */
    private String ensureContractPdf(SvcContract contract) {
        String url = blankToEmpty(contract.getSrcPdf());
        String name = blankToEmpty(contract.getSrcPdfName());
        if (url.isEmpty()) {
            List<String> urls = csvList(contract.getAnnex());
            List<String> names = csvList(contract.getAnnexName());
            if (urls.isEmpty()) {
                throw new RuntimeException("请先上传合同正文文件（PDF / Word / Excel / 图片）");
            }
            url = urls.get(0);
            name = names.isEmpty() ? "" : names.get(0);
        }
        String pdfUrl = contractDocService.ensurePdf(url, name);
        int pages = contractDocService.pageCount(pdfUrl);
        contract.setSrcPdf(pdfUrl);
        contract.setSrcPdfName(name);
        contract.setPageCount(pages);
        svcContractDao.updateById(contract);
        log.info("合同 {} 正文就绪：{}（{} 页）", contract.getId(), pdfUrl, pages);
        return pdfUrl;
    }

    /** 合同「用印相关」的元信息（后台定位编辑器 / 合同详情用） */
    private HashMap<String, Object> contractMeta(SvcContract c) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("srcPdf", blankToEmpty(c.getSrcPdf()));
        map.put("srcPdfName", blankToEmpty(c.getSrcPdfName()));
        map.put("pageCount", c.getPageCount() == null ? 0 : c.getPageCount());
        map.put("signLayout", blankToEmpty(c.getSignLayout()));
        // 甲方（客户）
        map.put("partyType", c.getPartyType());
        map.put("partyName", blankToEmpty(c.getPartyName()));
        map.put("nickname", blankToEmpty(c.getNickname()));
        map.put("custSeal", blankToEmpty(c.getCustSeal()));
        map.put("signName", blankToEmpty(c.getSignName()));
        map.put("signHand", blankToEmpty(c.getSignHand()));
        map.put("signTime", c.getSignTime());
        map.put("custStampedFile", blankToEmpty(c.getCustStampedFile()));
        map.put("custStampedName", blankToEmpty(c.getCustStampedName()));
        // 乙方（黔序）
        map.put("ourUid", c.getOurUid() == null ? 0 : c.getOurUid());
        map.put("ourCompany", blankToEmpty(c.getOurCompany()));
        map.put("ourNickname", blankToEmpty(c.getOurNickname()));
        map.put("ourSeal", blankToEmpty(c.getOurSeal()));
        map.put("ourSign", blankToEmpty(c.getOurSign()));
        map.put("ourSigner", blankToEmpty(c.getOurSigner()));
        map.put("ourSignedAt", c.getOurSignedAt());
        map.put("ourStampedFile", blankToEmpty(c.getOurStampedFile()));
        map.put("ourStampedName", blankToEmpty(c.getOurStampedName()));
        // 双方进度 + 成品
        map.put("custSigned", custSigned(c));
        map.put("ourSigned", ourSigned(c));
        map.put("finished", finished(c));
        map.put("signedFile", blankToEmpty(c.getSignedFile()));
        map.put("signedFileName", blankToEmpty(c.getSignedFileName()));
        map.put("fileHash", blankToEmpty(c.getFileHash()));
        return map;
    }

    private static String blankToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /** 字符串转 int，非法或空返回 0 */
    private static Integer intOf(String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** 逗号分隔 → 列表（去空、去空白） */
    private static List<String> csvList(String s) {
        List<String> list = new ArrayList<>();
        if (s == null || s.trim().isEmpty()) {
            return list;
        }
        for (String it : s.split(",")) {
            String v = it.trim();
            if (!v.isEmpty()) {
                list.add(v);
            }
        }
        return list;
    }

    // ==================== 后台：项目 ====================

    @Override
    public CommonPage<SvcProject> adminProjectList(Integer uid, String keywords, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProject::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcProject::getUid, uid);
        if (status != null) lqw.eq(SvcProject::getStatus, status);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.and(w -> w.like(SvcProject::getName, keywords.trim())
                    .or().like(SvcProject::getManager, keywords.trim()));
        }
        lqw.orderByDesc(SvcProject::getSort).orderByDesc(SvcProject::getId);
        List<SvcProject> list = svcProjectDao.selectList(lqw);
        fillProject(list, false);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveProject(SvcRequests.ProjectSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcProject exist = svcProjectDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("项目不存在");
            }
            exist.setUid(request.getUid());
            exist.setName(request.getName());
            exist.setType(request.getType() == null ? "" : request.getType());
            if (request.getStatus() != null) exist.setStatus(request.getStatus());
            exist.setStage(request.getStage() == null ? "" : request.getStage());
            exist.setManager(request.getManager() == null ? "" : request.getManager());
            exist.setCycle(request.getCycle() == null ? "" : request.getCycle());
            exist.setSort(request.getSort() == null ? 0 : request.getSort());
            return svcProjectDao.updateById(exist) > 0;
        }
        SvcProject project = new SvcProject();
        project.setUid(request.getUid());
        project.setName(request.getName());
        project.setType(request.getType() == null ? "" : request.getType());
        project.setStatus(request.getStatus() == null ? SvcProject.STATUS_DEMAND : request.getStatus());
        project.setPercent(0);
        project.setStage(request.getStage() == null ? "" : request.getStage());
        project.setManager(request.getManager() == null ? "" : request.getManager());
        project.setCycle(request.getCycle() == null ? "" : request.getCycle());
        project.setSort(request.getSort() == null ? 0 : request.getSort());
        project.setIsDel(0);
        return svcProjectDao.insert(project) > 0;
    }

    @Override
    public Boolean deleteProject(Integer id) {
        SvcProject exist = svcProjectDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("项目不存在");
        }
        exist.setIsDel(1);
        return svcProjectDao.updateById(exist) > 0;
    }

    @Override
    public List<SvcProjectProgress> progressListOf(Integer projectId) {
        LambdaQueryWrapper<SvcProjectProgress> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProjectProgress::getProjectId, projectId);
        lqw.orderByDesc(SvcProjectProgress::getId);
        return svcProjectProgressDao.selectList(lqw);
    }

    @Override
    public Boolean saveProgress(SvcRequests.ProgressSetRequest request) {
        SvcProject project = svcProjectDao.selectById(request.getProjectId());
        if (project == null || project.getIsDel() > 0) {
            throw new RuntimeException("项目不存在");
        }
        int percent = request.getPercent();
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        final int finalPercent = percent;
        final SvcProject finalProject = project;

        return transactionTemplate.execute(status -> {
            SvcProjectProgress record = new SvcProjectProgress();
            record.setProjectId(finalProject.getId());
            record.setUid(finalProject.getUid());
            record.setStage(request.getStage());
            record.setPercent(finalPercent);
            record.setRemark(request.getRemark() == null ? "" : request.getRemark());
            svcProjectProgressDao.insert(record);

            finalProject.setPercent(finalPercent);
            finalProject.setStage(request.getStage());
            if (request.getSyncStatus() != null && request.getSyncStatus() == 1) {
                finalProject.setStatus(finalPercent >= 100 ? SvcProject.STATUS_WAIT_ACCEPT : SvcProject.STATUS_DEVELOPING);
            }
            svcProjectDao.updateById(finalProject);
            return true;
        });
    }

    // ==================== 后台：客户案例 ====================

    @Override
    public CommonPage<SvcCase> adminCaseList(String keywords, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcCase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcCase::getIsDel, 0);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(SvcCase::getTitle, keywords.trim());
        }
        lqw.orderByDesc(SvcCase::getSort).orderByDesc(SvcCase::getId);
        List<SvcCase> list = svcCaseDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveCase(SvcRequests.CaseSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcCase exist = svcCaseDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() > 0) {
                throw new RuntimeException("案例不存在");
            }
            exist.setTitle(request.getTitle());
            exist.setCategory(request.getCategory() == null ? "" : request.getCategory());
            exist.setDescription(request.getDescription() == null ? "" : request.getDescription());
            exist.setImage(request.getImage() == null ? "" : request.getImage());
            exist.setSort(request.getSort() == null ? 0 : request.getSort());
            if (request.getIsShow() != null) exist.setIsShow(request.getIsShow());
            return svcCaseDao.updateById(exist) > 0;
        }
        SvcCase svcCase = new SvcCase();
        svcCase.setTitle(request.getTitle());
        svcCase.setCategory(request.getCategory() == null ? "" : request.getCategory());
        svcCase.setDescription(request.getDescription() == null ? "" : request.getDescription());
        svcCase.setImage(request.getImage() == null ? "" : request.getImage());
        svcCase.setSort(request.getSort() == null ? 0 : request.getSort());
        svcCase.setIsShow(request.getIsShow() == null ? 1 : request.getIsShow());
        svcCase.setIsDel(0);
        return svcCaseDao.insert(svcCase) > 0;
    }

    @Override
    public Boolean deleteCase(Integer id) {
        SvcCase exist = svcCaseDao.selectById(id);
        if (exist == null || exist.getIsDel() > 0) {
            throw new RuntimeException("案例不存在");
        }
        exist.setIsDel(1);
        return svcCaseDao.updateById(exist) > 0;
    }

    // ==================== 后台：服务设置 ====================

    private static final String[] SETTING_KEYS = {
            "svc_home_mode", "svc_user_mode", "svc_nav_mode", "svc_sign_sms",
            "svc_company_name", "svc_contact_phone", "svc_contact_wechat", "svc_contact_time"
    };

    @Override
    public HashMap<String, Object> getSetting() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : SETTING_KEYS) {
            map.put(key, systemConfigService.getValueByKey(key));
        }
        return map;
    }

    @Override
    public Boolean saveSetting(HashMap<String, Object> settingMap) {
        for (String key : SETTING_KEYS) {
            if (settingMap.containsKey(key)) {
                Object v = settingMap.get(key);
                systemConfigService.updateOrSaveValueByName(key, v == null ? "" : String.valueOf(v));
            }
        }
        return true;
    }

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> getConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("homeMode", "1".equals(systemConfigService.getValueByKey("svc_home_mode")));
        map.put("userMode", "1".equals(systemConfigService.getValueByKey("svc_user_mode")));
        map.put("navMode", "1".equals(systemConfigService.getValueByKey("svc_nav_mode")));
        map.put("companyName", systemConfigService.getValueByKey("svc_company_name"));
        map.put("contactPhone", systemConfigService.getValueByKey("svc_contact_phone"));
        map.put("contactWechat", systemConfigService.getValueByKey("svc_contact_wechat"));
        map.put("contactTime", systemConfigService.getValueByKey("svc_contact_time"));
        map.put("signSms", "1".equals(systemConfigService.getValueByKey("svc_sign_sms")));
        return map;
    }

    @Override
    public List<SvcCase> frontCaseList(Integer limit) {
        LambdaQueryWrapper<SvcCase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcCase::getIsDel, 0);
        lqw.eq(SvcCase::getIsShow, 1);
        lqw.orderByDesc(SvcCase::getSort).orderByDesc(SvcCase::getId);
        if (limit != null && limit > 0) {
            lqw.last(" limit " + limit);
        }
        return svcCaseDao.selectList(lqw);
    }

    @Override
    public CommonPage<SvcContract> myContractList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        // 我可能是甲方（uid），也可能是乙方（our_uid）——两种都列出来，并带角色标记
        if (uid != null && uid > 0) {
            lqw.and(w -> w.eq(SvcContract::getUid, uid).or().eq(SvcContract::getOurUid, uid));
        }
        if (status != null) lqw.eq(SvcContract::getStatus, status);
        lqw.orderByDesc(SvcContract::getId);
        List<SvcContract> list = svcContractDao.selectList(lqw);
        fillContract(list, uid);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcContract myContractDetail(Integer uid, Integer id) {
        SvcContract contract = id == null ? null : svcContractDao.selectById(id);
        if (contract == null || contract.getIsDel() > 0 || roleOf(contract, uid).isEmpty()) {
            throw new RuntimeException("合同不存在");
        }
        fillContract(contract, uid);
        return contract;
    }

    @Override
    public Boolean signContract(final Integer uid, final SvcRequests.ContractSignRequest request) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> doSign(uid, request)));
    }

    /**
     * 签署（事务内执行，重新读一次合同，尽量缩小双方并发的窗口）。
     *
     * 规则（2026-10-02 用户定案）：
     *   · 甲方（uid）与乙方（our_uid）各自登录 H5 签一次，谁先谁后都行；
     *   · 双方都只提交「手写签名」——公章一律由服务端按签约方名称现场生成圆形章（后台不需预设）；
     *   · 每人签完先出一份「本方单方签署版」供本人下载查看；
     *   · 双方齐备时，从原稿 PDF **一次性**合成双方公章 + 双方签字 + 双日期 → 正式合同（status=1）。
     */
    private boolean doSign(Integer uid, SvcRequests.ContractSignRequest request) {
        SvcContract contract = request.getId() == null ? null : svcContractDao.selectById(request.getId());
        if (contract == null || contract.getIsDel() > 0) {
            throw new RuntimeException("合同不存在");
        }
        String role = roleOf(contract, uid);
        if (role.isEmpty()) {
            throw new RuntimeException("合同不存在");
        }
        if (SvcContract.STATUS_SIGNED.equals(contract.getStatus()) && finished(contract)) {
            throw new RuntimeException("该合同双方均已签署完成");
        }
        boolean isCust = ROLE_CUST.equals(role);
        if (isCust ? custSigned(contract) : ourSigned(contract)) {
            throw new RuntimeException(isCust ? "您已签署过该合同" : "乙方已签署过该合同");
        }
        String signHand = blankToEmpty(request.getSignImage());
        if (signHand.isEmpty()) {
            throw new RuntimeException(isCust ? "请甲方手写签名" : "请乙方手写签名");
        }
        String signer = blankToEmpty(request.getSignName());
        if (signer.isEmpty()) {
            signer = isCust ? blankToEmpty(contract.getCustLegal()) : blankToEmpty(contract.getOurLegal());
        }

        // 公章：服务端按签约方名称生成圆形章（甲方为个人主体时不盖章）
        String sealUrl = isCust ? custSealOf(contract) : ourSealOf(contract);

        // 正文底稿（原稿）
        String srcPdf = blankToEmpty(contract.getSrcPdf());
        if (srcPdf.isEmpty()) {
            srcPdf = blankToEmpty(ensureContractPdf(contract));
        }
        // 日期不用后台摆 —— 布局里缺日期位时，自动补在对应签字/公章位下方
        String layout = ensureDateLayout(contract.getSignLayout());
        Date now = new Date();
        String dateText = cn.hutool.core.date.DateUtil.format(now, "yyyy年MM月dd日");

        // ① 记下本方的签署信息
        if (isCust) {
            contract.setSignName(signer);
            contract.setSignPhone(blankToEmpty(request.getPhone()));
            contract.setSignHand(signHand);
            contract.setSignTime(now);
            contract.setCustSeal(sealUrl);
        } else {
            contract.setOurSigner(signer);
            contract.setOurSign(signHand);
            contract.setOurSignedAt(now);
            if (!sealUrl.isEmpty()) {
                contract.setOurSeal(sealUrl);
            }
        }

        // ② 本方单方签署版（原稿 + 本方要素），供签署人立刻下载查看
        if (!layout.isEmpty() && !srcPdf.isEmpty()) {
            Map<String, String> imgs = new HashMap<>();
            Map<String, String> texts = new HashMap<>();
            if (isCust) {
                if (!sealUrl.isEmpty()) imgs.put("custSeal", sealUrl);
                imgs.put("custSign", signHand);
                texts.put("custDate", dateText);
            } else {
                if (!sealUrl.isEmpty()) imgs.put("ourSeal", sealUrl);
                imgs.put("ourSign", signHand);
                texts.put("ourDate", dateText);
            }
            ContractDocService.StampedFile one = contractDocService.stamp(srcPdf, layout, imgs, texts);
            if (isCust) {
                contract.setCustStampedFile(one.url);
                contract.setCustStampedName(one.name);
            } else {
                contract.setOurStampedFile(one.url);
                contract.setOurStampedName(one.name);
            }
        }

        // ③ 双方齐备 → 自动转正式合同：从原稿一次性合成双方公章 + 签字 + 日期
        if (custSigned(contract) && ourSigned(contract)) {
            contract.setStatus(SvcContract.STATUS_SIGNED);
            contract.setSignSheet("");
            if (!layout.isEmpty() && !srcPdf.isEmpty()) {
                Map<String, String> imgs = new HashMap<>();
                Map<String, String> texts = new HashMap<>();
                if (!blankToEmpty(contract.getCustSeal()).isEmpty()) {
                    imgs.put("custSeal", contract.getCustSeal().trim());
                }
                imgs.put("custSign", contract.getSignHand().trim());
                texts.put("custDate", cn.hutool.core.date.DateUtil.format(contract.getSignTime(), "yyyy年MM月dd日"));
                if (!blankToEmpty(contract.getOurSeal()).isEmpty()) {
                    imgs.put("ourSeal", contract.getOurSeal().trim());
                }
                imgs.put("ourSign", contract.getOurSign().trim());
                texts.put("ourDate", cn.hutool.core.date.DateUtil.format(contract.getOurSignedAt(), "yyyy年MM月dd日"));

                ContractDocService.StampedFile fin = contractDocService.stamp(srcPdf, layout, imgs, texts);
                contract.setSignedFile(fin.url);
                contract.setSignedFileName(fin.name);
                contract.setFileHash(fin.hash);
            }
            attachProject(contract);
            log.info("合同 {} 双方签署齐备，正式合同已生成：{}", contract.getId(), contract.getSignedFile());
        }
        return svcContractDao.updateById(contract) > 0;
    }

    /**
     * 用印布局里缺日期位时，自动补一个：放在对应「签字（无则公章）」位的正下方。
     * 后台摆位置只摆公章/签字，日期由签署时自动带上（2026-10-02 定案）。
     */
    private String ensureDateLayout(String layoutJson) {
        String lay = blankToEmpty(layoutJson);
        if (lay.isEmpty()) {
            return lay;
        }
        try {
            List<Map<String, Object>> items = new Gson().fromJson(lay,
                    new TypeToken<List<Map<String, Object>>>() { }.getType());
            if (items == null) {
                return lay;
            }
            boolean hasCustDate = false;
            boolean hasOurDate = false;
            Map<String, Object> custAnchor = null;
            Map<String, Object> ourAnchor = null;
            for (Map<String, Object> it : items) {
                String type = it.get("type") == null ? "" : String.valueOf(it.get("type"));
                if ("custDate".equals(type)) {
                    hasCustDate = true;
                } else if ("ourDate".equals(type)) {
                    hasOurDate = true;
                } else if ("custSign".equals(type) || ("custSeal".equals(type) && custAnchor == null)) {
                    custAnchor = it;
                } else if ("ourSign".equals(type) || ("ourSeal".equals(type) && ourAnchor == null)) {
                    ourAnchor = it;
                }
            }
            boolean changed = false;
            if (!hasCustDate && custAnchor != null) {
                items.add(dateItemOf("custDate", custAnchor));
                changed = true;
            }
            if (!hasOurDate && ourAnchor != null) {
                items.add(dateItemOf("ourDate", ourAnchor));
                changed = true;
            }
            return changed ? new Gson().toJson(items) : lay;
        } catch (Exception e) {
            log.warn("解析用印布局失败，按原布局签署：{}", e.getMessage());
            return lay;
        }
    }

    /** 依据锚点（签字/公章位）生成一个日期位：同页、正下方、宽 20% 高 3.6% */
    private static Map<String, Object> dateItemOf(String type, Map<String, Object> anchor) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", type);
        m.put("page", anchor.get("page") == null ? 1 : anchor.get("page"));
        m.put("x", round2Of(numOf(anchor.get("x"), 58d) - 2d));
        m.put("y", round2Of(Math.min(95, numOf(anchor.get("y"), 66d) + numOf(anchor.get("h"), 7d) + 1.5d)));
        m.put("w", 20d);
        m.put("h", 3.6d);
        m.put("tilt", 0d);
        return m;
    }

    private static double numOf(Object o, double def) {
        return o instanceof Number ? ((Number) o).doubleValue() : def;
    }

    private static double round2Of(double v) {
        return Math.round(v * 100) / 100d;
    }

    /** 甲方公章：企业主体才盖；按合同的甲方主体名称（空则取会员昵称）生成圆形章 */    private String custSealOf(SvcContract c) {
        if (!SvcContract.PARTY_COMPANY.equals(c.getPartyType())) {
            return "";
        }
        // 后台若已预设过甲方章，沿用（兼容老数据）
        String preset = blankToEmpty(c.getCustSeal());
        if (!preset.isEmpty()) {
            return preset;
        }
        String name = blankToEmpty(c.getPartyName());
        if (name.isEmpty() && c.getUid() != null) {
            User u = userDao.selectById(c.getUid());
            name = u == null ? "" : blankToEmpty(u.getNickname());
        }
        if (name.isEmpty()) {
            throw new RuntimeException("合同缺少甲方名称，无法生成电子公章，请先在后台补全签约主体名称");
        }
        return contractDocService.makeRoundSeal(name);
    }

    /** 乙方公章：后台若上传过自定义章则沿用，否则按乙方名称现场生成圆形章 */
    private String ourSealOf(SvcContract c) {
        String preset = blankToEmpty(c.getOurSeal());
        if (!preset.isEmpty()) {
            return preset;
        }
        String name = blankToEmpty(c.getOurCompany());
        if (name.isEmpty() && c.getOurUid() != null && c.getOurUid() > 0) {
            User u = userDao.selectById(c.getOurUid());
            name = u == null ? "" : blankToEmpty(u.getNickname());
        }
        if (name.isEmpty()) {
            name = blankToEmpty(systemConfigService.getValueByKey(CFG_OUR_COMPANY));
        }
        if (name.isEmpty()) {
            throw new RuntimeException("合同缺少乙方名称，无法生成电子公章，请先在后台补全乙方信息");
        }
        return contractDocService.makeRoundSeal(name);
    }

    /** 双方签署完成 → 自动建立对应项目，会员端「我的项目」可见 */
    private void attachProject(SvcContract contract) {
        Integer pid = contract.getProjectId();
        if (pid != null && pid > 0) {
            SvcProject exist = svcProjectDao.selectById(pid);
            if (exist != null && exist.getIsDel() != null && exist.getIsDel() == 0) {
                return;
            }
        }
        SvcProject project = new SvcProject();
        project.setUid(contract.getUid());
        project.setName(contract.getTitle());
        project.setType("");
        project.setStatus(SvcProject.STATUS_DEMAND);
        project.setPercent(0);
        project.setStage("需求确认");
        project.setIsDel(0);
        svcProjectDao.insert(project);
        contract.setProjectId(project.getId());
    }

    @Override
    public Boolean sendSignCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码错误");
        }
        return smsService.sendCommonCode(phone);
    }

    @Override
    public void checkSignCode(String phone, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("请输入短信验证码");
        }
        Object validateCode = redisUtil.get(SmsConstants.SMS_VALIDATE_PHONE + phone);
        if (validateCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        if (!validateCode.toString().equals(code.trim())) {
            throw new RuntimeException("验证码错误");
        }
        redisUtil.delete(SmsConstants.SMS_VALIDATE_PHONE + phone);
    }

    @Override
    public CommonPage<SvcProject> myProjectList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcProject> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcProject::getIsDel, 0);
        lqw.eq(SvcProject::getUid, uid);
        if (status != null) lqw.eq(SvcProject::getStatus, status);
        lqw.orderByDesc(SvcProject::getSort).orderByDesc(SvcProject::getId);
        List<SvcProject> list = svcProjectDao.selectList(lqw);
        fillProject(list, false);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcProject myProjectDetail(Integer uid, Integer id) {
        SvcProject project = svcProjectDao.selectById(id);
        if (project == null || project.getIsDel() > 0 || !project.getUid().equals(uid)) {
            throw new RuntimeException("项目不存在");
        }
        fillProject(new ArrayList<SvcProject>() {{ add(project); }}, true);
        return project;
    }

    @Override
    public HashMap<String, Object> myStats(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<SvcProject> doing = new LambdaQueryWrapper<>();
        doing.eq(SvcProject::getIsDel, 0);
        doing.eq(SvcProject::getUid, uid);
        doing.in(SvcProject::getStatus, SvcProject.STATUS_DEMAND, SvcProject.STATUS_DEVELOPING);
        map.put("doingCount", svcProjectDao.selectCount(doing));

        LambdaQueryWrapper<SvcProject> waitAccept = new LambdaQueryWrapper<>();
        waitAccept.eq(SvcProject::getIsDel, 0);
        waitAccept.eq(SvcProject::getUid, uid);
        waitAccept.eq(SvcProject::getStatus, SvcProject.STATUS_WAIT_ACCEPT);
        map.put("waitAcceptCount", svcProjectDao.selectCount(waitAccept));

        User user = userDao.selectById(uid);
        map.put("balance", user == null || user.getNowMoney() == null ? BigDecimal.ZERO : user.getNowMoney());
        return map;
    }

    @Override
    public List<SvcProjectProgress> myDynamic(Integer uid, Integer limit) {
        // 最新动态：我的项目最近进度记录
        LambdaQueryWrapper<SvcProject> plqw = new LambdaQueryWrapper<>();
        plqw.eq(SvcProject::getIsDel, 0);
        plqw.eq(SvcProject::getUid, uid);
        plqw.select(SvcProject::getId);
        List<SvcProject> projects = svcProjectDao.selectList(plqw);
        if (projects.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = new ArrayList<>();
        for (SvcProject p : projects) {
            ids.add(p.getId());
        }
        LambdaQueryWrapper<SvcProjectProgress> lqw = new LambdaQueryWrapper<>();
        lqw.in(SvcProjectProgress::getProjectId, ids);
        lqw.orderByDesc(SvcProjectProgress::getId);
        if (limit != null && limit > 0) {
            lqw.last(" limit " + limit);
        }
        return svcProjectProgressDao.selectList(lqw);
    }
}
