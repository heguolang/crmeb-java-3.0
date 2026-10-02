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
import com.qxkj.common.model.svc.SvcDemand;
import com.qxkj.common.model.svc.SvcInvoice;
import com.qxkj.common.model.svc.SvcPlatform;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.model.user.User;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.dao.SvcCaseDao;
import com.qxkj.service.dao.SvcContractDao;
import com.qxkj.service.dao.SvcDemandDao;
import com.qxkj.service.dao.SvcInvoiceDao;
import com.qxkj.service.dao.SvcPlatformDao;
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
    private SvcDemandDao svcDemandDao;

    @Resource
    private SvcPlatformDao svcPlatformDao;

    @Resource
    private SvcInvoiceDao svcInvoiceDao;

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

    private String makeDemandNo() {
        // 需求编号：SRV + yyyyMMdd + 4 位随机数
        String date = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        return "SRV" + date + cn.hutool.core.util.RandomUtil.randomNumbers(4);
    }

    private String makeInvoiceNo() {
        // 开票申请单号：IV + yyyyMMdd + 6 位随机数
        String date = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        return "IV" + date + cn.hutool.core.util.RandomUtil.randomNumbers(6);
    }

    private static String nowStr() {
        return cn.hutool.core.date.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
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
        c.setStatusText(SvcContract.statusName(c.getStatus()));
        c.setChannelText(SvcContract.channelName(c.getSignChannel()));
    }

    /**
     * 详情专用：在列表字段之外，再补「状态日志 / 关联项目 / 关联需求编号」。
     * 列表接口不调这个方法，避免 N+1 查询。
     */
    private void fillContractDetail(SvcContract c, Integer uid) {
        if (c == null) return;
        fillContract(c, uid);
        c.setStatusLogList(parseStatusLog(c.getStatusLog()));
        if (c.getProjectId() != null && c.getProjectId() > 0) {
            SvcProject p = svcProjectDao.selectById(c.getProjectId());
            if (p != null && p.getIsDel() != null && p.getIsDel() == 0) {
                p.setStatusText(statusText(p.getStatus()));
                c.setProject(p);
                c.setProjectName(blankToEmpty(p.getName()));
            }
        }
        if (c.getDemandId() != null && c.getDemandId() > 0) {
            SvcDemand d = svcDemandDao.selectById(c.getDemandId());
            if (d != null && d.getIsDel() != null && d.getIsDel() == 0) {
                c.setDemandNo(blankToEmpty(d.getDemandNo()));
            }
        }
    }

    /** 状态日志 JSON → 结构化列表（解析失败返回空列表，不影响主流程） */
    private List<SvcContract.StatusLogItem> parseStatusLog(String json) {
        List<SvcContract.StatusLogItem> list = new ArrayList<>();
        String s = blankToEmpty(json);
        if (s.isEmpty()) return list;
        try {
            List<SvcContract.StatusLogItem> items = new Gson().fromJson(s,
                    new TypeToken<List<SvcContract.StatusLogItem>>() { }.getType());
            return items == null ? list : items;
        } catch (Exception e) {
            log.warn("解析合同状态日志失败：{}", e.getMessage());
            return list;
        }
    }

    /**
     * 追加一条合同状态变更日志（最新的一条放最前，方便前端直接渲染时间线）。
     */
    private String appendStatusLog(String oldJson, Integer status, String operator, String remark) {
        List<SvcContract.StatusLogItem> list = parseStatusLog(oldJson);
        SvcContract.StatusLogItem item = new SvcContract.StatusLogItem();
        item.setTime(nowStr());
        item.setStatus(status);
        item.setStatusName(SvcContract.statusName(status));
        item.setOperator(blankToEmpty(operator));
        item.setRemark(blankToEmpty(remark));
        list.add(0, item);
        return new Gson().toJson(list);
    }

    /** 需求单：补状态名、会员昵称手机、转出合同编号、跟进记录、附件清单 */
    private void fillDemand(List<SvcDemand> list) {
        if (list == null || list.isEmpty()) return;
        for (SvcDemand d : list) {
            fillDemand(d, false);
        }
    }

    private void fillDemand(SvcDemand d, boolean detail) {
        if (d == null) return;
        d.setStatusText(SvcDemand.statusName(d.getStatus()));
        if (d.getUid() != null && d.getUid() > 0) {
            User u = userDao.selectById(d.getUid());
            if (u != null) {
                d.setNickname(blankToEmpty(u.getNickname()));
                d.setPhone(blankToEmpty(u.getPhone()));
            }
        }
        if (d.getContractId() != null && d.getContractId() > 0) {
            SvcContract c = svcContractDao.selectById(d.getContractId());
            if (c != null && c.getIsDel() != null && c.getIsDel() == 0) {
                d.setContractNo(blankToEmpty(c.getContractNo()));
            }
        }
        if (detail) {
            d.setFollowList(parseFollowLog(d.getFollowLog()));
            d.setAttachList(csvList(d.getAttach()));
            d.setAttachNameList(csvList(d.getAttachName()));
        }
    }

    /** 跟进记录 JSON → 结构化列表 */
    private List<SvcDemand.FollowItem> parseFollowLog(String json) {
        List<SvcDemand.FollowItem> list = new ArrayList<>();
        String s = blankToEmpty(json);
        if (s.isEmpty()) return list;
        try {
            List<SvcDemand.FollowItem> items = new Gson().fromJson(s,
                    new TypeToken<List<SvcDemand.FollowItem>>() { }.getType());
            return items == null ? list : items;
        } catch (Exception e) {
            log.warn("解析需求跟进记录失败：{}", e.getMessage());
            return list;
        }
    }

    /** 追加一条跟进记录（最新在前） */
    private String appendFollowLog(String oldJson, String content, String operator) {
        List<SvcDemand.FollowItem> list = parseFollowLog(oldJson);
        SvcDemand.FollowItem item = new SvcDemand.FollowItem();
        item.setTime(nowStr());
        item.setContent(blankToEmpty(content));
        item.setOperator(blankToEmpty(operator));
        list.add(0, item);
        return new Gson().toJson(list);
    }

    /** 需求必须存在且未删除 */
    private SvcDemand mustDemand(Integer id) {
        SvcDemand d = id == null ? null : svcDemandDao.selectById(id);
        if (d == null || (d.getIsDel() != null && d.getIsDel() > 0)) {
            throw new RuntimeException("需求不存在");
        }
        return d;
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
    public CommonPage<SvcContract> adminContractList(Integer uid, String keywords, Integer status, String signChannel, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcContract::getUid, uid);
        if (status != null) lqw.eq(SvcContract::getStatus, status);
        if (signChannel != null && !signChannel.trim().isEmpty()) {
            lqw.eq(SvcContract::getSignChannel, signChannel.trim());
        }
        if (keywords != null && !keywords.trim().isEmpty()) {
            String kw = keywords.trim();
            lqw.and(w -> w.like(SvcContract::getTitle, kw)
                    .or().like(SvcContract::getContractNo, kw)
                    .or().like(SvcContract::getPartyName, kw));
        }
        lqw.orderByDesc(SvcContract::getId);
        List<SvcContract> list = svcContractDao.selectList(lqw);
        fillContract(list, null);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcContract adminContractInfo(Integer id) {
        SvcContract contract = mustContract(id);
        fillContractDetail(contract, null);
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
            fillChannel(exist, request);
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
            // 签约状态：登记弹窗可直选；日常改状态走「变更签约状态」，此处只在真的变了时补一条日志
            if (request.getStatus() != null && !request.getStatus().equals(exist.getStatus())) {
                exist.setStatus(request.getStatus());
                exist.setStatusLog(appendStatusLog(exist.getStatusLog(), request.getStatus(),
                        blankToEmpty(request.getRemark()), "登记/编辑合同"));
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
        fillChannel(contract, request);
        // 登记时即可指定签约状态（默认「待签署」），并写入第一条状态日志
        int initStatus = request.getStatus() == null ? SvcContract.STATUS_WAIT_SIGN : request.getStatus();
        contract.setStatus(initStatus);
        contract.setStatusLog(appendStatusLog(null, initStatus, blankToEmpty(request.getRemark()), "登记合同"));
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

    /**
     * 签约渠道字段（纸质 / 电子 + 成品件），update / insert 两支共用。
     *
     * 纸质：签署方式、打印份数、快递公司、寄出与回寄单号、寄出与签收日期；
     * 电子：平台名称、平台合同编号/存证编号、平台查看链接、发起日期、存证报告。
     * 两类字段一次只填一类，但列都存在，切换渠道不会丢已填内容（前端按渠道显隐）。
     */
    private void fillChannel(SvcContract target, SvcRequests.ContractSaveRequest request) {
        // 渠道相关字段全部按字符串写。⚠️ sent_date / back_date / start_date 在库里是 varchar，
        // 不要改成 date 列：空值统一写 ''，而 MySQL 严格模式下 date 列写 '' 会直接 500
        // （Incorrect date value），且 MyBatis-Plus updateById 忽略 null，会导致日期填过就清不掉。
        String channel = blankToEmpty(request.getSignChannel());
        target.setSignChannel(SvcContract.CHANNEL_ELECTRONIC.equals(channel)
                ? SvcContract.CHANNEL_ELECTRONIC : SvcContract.CHANNEL_PAPER);
        target.setSignType(blankToEmpty(request.getSignType()));
        target.setPrintCount(blankToEmpty(request.getPrintCount()));
        target.setExpressCo(blankToEmpty(request.getExpressCo()));
        target.setExpressNo(blankToEmpty(request.getExpressNo()));
        target.setExpressNoBack(blankToEmpty(request.getExpressNoBack()));
        target.setSentDate(blankToEmpty(request.getSentDate()));
        target.setBackDate(blankToEmpty(request.getBackDate()));
        target.setSignPlatform(blankToEmpty(request.getSignPlatform()));
        target.setPlatformNo(blankToEmpty(request.getPlatformNo()));
        target.setPlatformUrl(blankToEmpty(request.getPlatformUrl()));
        target.setStartDate(blankToEmpty(request.getStartDate()));
        target.setVoucherFile(blankToEmpty(request.getVoucherFile()));
        target.setVoucherName(blankToEmpty(request.getVoucherName()));
        target.setServiceTerm(blankToEmpty(request.getServiceTerm()));
        if (request.getDemandId() != null && request.getDemandId() > 0) {
            target.setDemandId(request.getDemandId());
        }
        // 成品件（双方签署完成的纸质扫描件 / 平台导出件）
        if (request.getSignedFile() != null) {
            target.setSignedFile(blankToEmpty(request.getSignedFile()));
            target.setSignedFileName(blankToEmpty(request.getSignedFileName()));
        }
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

    // ==================== 后台：合同签约状态（线上唯一要做的「留痕」） ====================

    /**
     * 变更合同签约状态（2026-10-02 定案：合同线下签署，线上只由管理员选状态）。
     *
     * 规则：
     *   · 状态只由后台选择，客户不参与确认；每次变更追加一条日志（时间 + 状态 + 经办人 + 备注）；
     *   · 从任意状态切到「已生效」时，自动建/挂项目，客户在 H5「我的项目」里就能看到进度；
     *   · 切到「已终止」时不动项目，只标记合同不再推进。
     */
    @Override
    public Boolean changeContractStatus(final SvcRequests.ContractStatusRequest request) {
        Integer newStatus = request.getStatus();
        if (newStatus == null || SvcContract.statusName(newStatus).isEmpty()) {
            throw new RuntimeException("请选择有效的签约状态");
        }
        return transactionTemplate.execute(status -> {
            SvcContract contract = mustContract(request.getId());
            Integer oldStatus = contract.getStatus();
            if (newStatus.equals(oldStatus)) {
                throw new RuntimeException("当前已是「" + SvcContract.statusName(newStatus) + "」，无需变更");
            }
            contract.setStatus(newStatus);
            contract.setStatusLog(appendStatusLog(contract.getStatusLog(), newStatus,
                    blankToEmpty(request.getOperator()), blankToEmpty(request.getRemark())));
            // 生效即触发建项目：合同签完自动建项目这条链不断
            if (SvcContract.STATUS_EFFECTIVE.equals(newStatus)) {
                attachProject(contract);
            }
            log.info("合同 {} 签约状态 {} → {}，经办人 {}",
                    contract.getId(), SvcContract.statusName(oldStatus),
                    SvcContract.statusName(newStatus), blankToEmpty(request.getOperator()));
            return svcContractDao.updateById(contract) > 0;
        });
    }

    @Override
    public HashMap<String, Object> adminContractStats() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("waitSignCount", countContractByStatus(SvcContract.STATUS_WAIT_SIGN));
        map.put("signedCount", countContractByStatus(SvcContract.STATUS_SIGNED));
        map.put("effectiveCount", countContractByStatus(SvcContract.STATUS_EFFECTIVE));
        // 本月签约总额：已签署 + 已生效（不含已终止与草稿）
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        lqw.in(SvcContract::getStatus, SvcContract.STATUS_SIGNED, SvcContract.STATUS_EFFECTIVE);
        lqw.ge(SvcContract::getUpdateTime, cn.hutool.core.date.DateUtil.beginOfMonth(new Date()));
        List<SvcContract> list = svcContractDao.selectList(lqw);
        BigDecimal total = BigDecimal.ZERO;
        for (SvcContract c : list) {
            total = total.add(c.getAmount() == null ? BigDecimal.ZERO : c.getAmount());
        }
        map.put("monthAmount", total);
        return map;
    }

    private Integer countContractByStatus(Integer status) {
        LambdaQueryWrapper<SvcContract> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcContract::getIsDel, 0);
        lqw.eq(SvcContract::getStatus, status);
        return svcContractDao.selectCount(lqw);
    }

    // ==================== 后台：服务需求 ====================

    @Override
    public CommonPage<SvcDemand> adminDemandList(Integer uid, String keywords, String type, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcDemand> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcDemand::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcDemand::getUid, uid);
        if (type != null && !type.trim().isEmpty()) lqw.eq(SvcDemand::getType, type.trim());
        if (status != null) lqw.eq(SvcDemand::getStatus, status);
        if (keywords != null && !keywords.trim().isEmpty()) {
            String kw = keywords.trim();
            lqw.and(w -> w.like(SvcDemand::getDemandNo, kw)
                    .or().like(SvcDemand::getTitle, kw)
                    .or().like(SvcDemand::getCompany, kw)
                    .or().like(SvcDemand::getContactName, kw)
                    .or().like(SvcDemand::getContactPhone, kw));
        }
        // 待办优先：待受理永远排最前；同状态下新的在前
        lqw.orderByAsc(SvcDemand::getStatus).orderByDesc(SvcDemand::getId);
        List<SvcDemand> list = svcDemandDao.selectList(lqw);
        fillDemand(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcDemand adminDemandInfo(Integer id) {
        SvcDemand demand = mustDemand(id);
        fillDemand(demand, true);
        return demand;
    }

    @Override
    public Integer saveDemand(SvcRequests.DemandSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcDemand exist = mustDemand(request.getId());
            if (request.getUid() != null) exist.setUid(request.getUid());
            exist.setType(blankToEmpty(request.getType()));
            exist.setTitle(blankToEmpty(request.getTitle()));
            exist.setContent(blankToEmpty(request.getContent()));
            exist.setBudget(blankToEmpty(request.getBudget()));
            exist.setHopeDate(blankToEmpty(request.getHopeDate()));
            exist.setAttach(blankToEmpty(request.getAttach()));
            exist.setAttachName(blankToEmpty(request.getAttachName()));
            exist.setContactName(blankToEmpty(request.getContactName()));
            exist.setContactPhone(blankToEmpty(request.getContactPhone()));
            exist.setCompany(blankToEmpty(request.getCompany()));
            if (request.getStatus() != null) exist.setStatus(request.getStatus());
            exist.setFollowerUid(request.getFollowerUid() == null ? 0 : request.getFollowerUid());
            exist.setFollower(blankToEmpty(request.getFollower()));
            svcDemandDao.updateById(exist);
            return exist.getId();
        }
        SvcDemand demand = new SvcDemand();
        demand.setDemandNo(makeDemandNo());
        demand.setUid(request.getUid() == null ? 0 : request.getUid());
        demand.setType(blankToEmpty(request.getType()));
        demand.setTitle(blankToEmpty(request.getTitle()));
        demand.setContent(blankToEmpty(request.getContent()));
        demand.setBudget(blankToEmpty(request.getBudget()));
        demand.setHopeDate(blankToEmpty(request.getHopeDate()));
        demand.setAttach(blankToEmpty(request.getAttach()));
        demand.setAttachName(blankToEmpty(request.getAttachName()));
        demand.setContactName(blankToEmpty(request.getContactName()));
        demand.setContactPhone(blankToEmpty(request.getContactPhone()));
        demand.setCompany(blankToEmpty(request.getCompany()));
        demand.setStatus(request.getStatus() == null ? SvcDemand.STATUS_WAIT : request.getStatus());
        demand.setFollowerUid(request.getFollowerUid() == null ? 0 : request.getFollowerUid());
        demand.setFollower(blankToEmpty(request.getFollower()));
        demand.setAmount(BigDecimal.ZERO);
        demand.setFollowLog("[]");
        demand.setIsDel(0);
        svcDemandDao.insert(demand);
        return demand.getId();
    }

    @Override
    public Boolean deleteDemand(Integer id) {
        SvcDemand exist = mustDemand(id);
        exist.setIsDel(1);
        return svcDemandDao.updateById(exist) > 0;
    }

    @Override
    public Boolean quoteDemand(SvcRequests.DemandQuoteRequest request) {
        SvcDemand demand = mustDemand(request.getId());
        boolean publish = request.getPublish() == null || request.getPublish() == 1;
        if (publish && request.getAmount() == null) {
            throw new RuntimeException("请填写报价金额");
        }
        if (request.getAmount() != null) {
            demand.setAmount(request.getAmount());
        }
        demand.setQuoteDesc(blankToEmpty(request.getQuoteDesc()));
        demand.setQuoteFile(blankToEmpty(request.getQuoteFile()));
        demand.setQuoteName(blankToEmpty(request.getQuoteName()));
        if (request.getFollowerUid() != null) {
            demand.setFollowerUid(request.getFollowerUid());
        }
        if (request.getFollower() != null) {
            demand.setFollower(blankToEmpty(request.getFollower()));
        }
        if (publish) {
            demand.setQuoteTime(new Date());
            demand.setStatus(SvcDemand.STATUS_QUOTED);
            demand.setFollowLog(appendFollowLog(demand.getFollowLog(),
                    "已发布报价 ¥" + (demand.getAmount() == null ? "0" : demand.getAmount().toPlainString())
                            + "，等待客户确认", blankToEmpty(request.getFollower())));
        } else {
            demand.setStatus(SvcDemand.STATUS_FOLLOWING);
            demand.setFollowLog(appendFollowLog(demand.getFollowLog(),
                    "已指派跟进人：" + blankToEmpty(request.getFollower()), "系统"));
        }
        return svcDemandDao.updateById(demand) > 0;
    }

    @Override
    public Boolean saveDemandFollow(SvcRequests.DemandFollowRequest request) {
        SvcDemand demand = mustDemand(request.getId());
        demand.setFollowLog(appendFollowLog(demand.getFollowLog(),
                blankToEmpty(request.getContent()), blankToEmpty(request.getOperator())));
        // 客户提交后还没人管 → 有人跟进即进入「跟进中」
        if (SvcDemand.STATUS_WAIT.equals(demand.getStatus())) {
            demand.setStatus(SvcDemand.STATUS_FOLLOWING);
        }
        return svcDemandDao.updateById(demand) > 0;
    }

    @Override
    public Boolean closeDemand(Integer id, String reason) {
        SvcDemand demand = mustDemand(id);
        demand.setStatus(SvcDemand.STATUS_CLOSED);
        demand.setFollowLog(appendFollowLog(demand.getFollowLog(),
                "已关闭" + (blankToEmpty(reason).isEmpty() ? "" : "：" + blankToEmpty(reason)), "系统"));
        return svcDemandDao.updateById(demand) > 0;
    }

    /**
     * 需求转合同：需求里的客户/企业/金额带进合同，登记成功后回写 demand.contract_id 并把需求置为「已成单」。
     */
    @Override
    public Integer demandToContract(final Integer demandId, final SvcRequests.ContractSaveRequest request) {
        final SvcDemand demand = mustDemand(demandId);
        if (demand.getContractId() != null && demand.getContractId() > 0) {
            SvcContract exist = svcContractDao.selectById(demand.getContractId());
            if (exist != null && exist.getIsDel() != null && exist.getIsDel() == 0) {
                throw new RuntimeException("该需求已转为合同：" + blankToEmpty(exist.getContractNo()));
            }
        }
        return transactionTemplate.execute(status -> {
            // 需求侧信息兜底：合同没填的，用需求里已登记的补上
            if (request.getUid() == null || request.getUid() <= 0) {
                request.setUid(demand.getUid());
            }
            if (request.getUid() == null || request.getUid() <= 0) {
                // 需求本身没关联会员（代客录入时没填 UID），合同必须挂到会员上否则会员端看不到
                throw new RuntimeException("该需求未关联会员，请先在「代客录入/修改需求」里补填会员UID，再转合同");
            }
            if (blankToEmpty(request.getTitle()).isEmpty()) {
                request.setTitle(demand.getTitle());
            }
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                request.setAmount(demand.getAmount() == null ? BigDecimal.ZERO : demand.getAmount());
            }
            if (request.getPartyName() == null || request.getPartyName().isEmpty()) {
                request.setPartyName(blankToEmpty(demand.getCompany()).isEmpty()
                        ? blankToEmpty(demand.getContactName()) : blankToEmpty(demand.getCompany()));
            }
            if (request.getCustPhone() == null || request.getCustPhone().isEmpty()) {
                request.setCustPhone(blankToEmpty(demand.getContactPhone()));
            }
            request.setDemandId(demand.getId());
            Integer contractId = saveContract(request);
            if (contractId == null) {
                throw new RuntimeException("转合同失败");
            }
            demand.setContractId(contractId);
            demand.setStatus(SvcDemand.STATUS_DEAL);
            demand.setFollowLog(appendFollowLog(demand.getFollowLog(), "已转为合同，等待线下签署", "系统"));
            svcDemandDao.updateById(demand);
            return contractId;
        });
    }

    @Override
    public HashMap<String, Object> adminDemandStats() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("waitCount", countDemandByStatus(SvcDemand.STATUS_WAIT));
        map.put("followingCount", countDemandByStatus(SvcDemand.STATUS_FOLLOWING));
        map.put("quotedCount", countDemandByStatus(SvcDemand.STATUS_QUOTED));
        map.put("dealCount", countDemandByStatus(SvcDemand.STATUS_DEAL));
        return map;
    }

    private Integer countDemandByStatus(Integer status) {
        LambdaQueryWrapper<SvcDemand> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcDemand::getIsDel, 0);
        lqw.eq(SvcDemand::getStatus, status);
        return svcDemandDao.selectCount(lqw);
    }

    // ==================== 后台：签约渠道平台 ====================

    @Override
    public CommonPage<SvcPlatform> adminPlatformList(String keywords, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcPlatform> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcPlatform::getIsDel, 0);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(SvcPlatform::getName, keywords.trim());
        }
        lqw.orderByDesc(SvcPlatform::getSort).orderByDesc(SvcPlatform::getId);
        List<SvcPlatform> list = svcPlatformDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public List<SvcPlatform> enabledPlatforms() {
        LambdaQueryWrapper<SvcPlatform> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcPlatform::getIsDel, 0);
        lqw.eq(SvcPlatform::getStatus, 1);
        lqw.orderByDesc(SvcPlatform::getSort).orderByDesc(SvcPlatform::getId);
        return svcPlatformDao.selectList(lqw);
    }

    @Override
    public Boolean savePlatform(SvcRequests.PlatformSaveRequest request) {
        if (request.getId() != null && request.getId() > 0) {
            SvcPlatform exist = mustPlatform(request.getId());
            exist.setName(blankToEmpty(request.getName()));
            exist.setKind(blankToEmpty(request.getKind()));
            exist.setDescription(blankToEmpty(request.getDescription()));
            if (request.getStatus() != null) exist.setStatus(request.getStatus());
            exist.setSort(request.getSort() == null ? 0 : request.getSort());
            return svcPlatformDao.updateById(exist) > 0;
        }
        SvcPlatform platform = new SvcPlatform();
        platform.setName(blankToEmpty(request.getName()));
        platform.setKind(blankToEmpty(request.getKind()));
        platform.setDescription(blankToEmpty(request.getDescription()));
        platform.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        platform.setSort(request.getSort() == null ? 0 : request.getSort());
        platform.setIsDel(0);
        return svcPlatformDao.insert(platform) > 0;
    }

    @Override
    public Boolean deletePlatform(Integer id) {
        SvcPlatform exist = mustPlatform(id);
        exist.setIsDel(1);
        return svcPlatformDao.updateById(exist) > 0;
    }

    private SvcPlatform mustPlatform(Integer id) {
        SvcPlatform p = id == null ? null : svcPlatformDao.selectById(id);
        if (p == null || (p.getIsDel() != null && p.getIsDel() > 0)) {
            throw new RuntimeException("平台不存在");
        }
        return p;
    }

    // ==================== 后台：发票管理 ====================

    @Override
    public CommonPage<SvcInvoice> adminInvoiceList(Integer uid, String keywords, Integer status, Integer type, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcInvoice> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcInvoice::getIsDel, 0);
        if (uid != null && uid > 0) lqw.eq(SvcInvoice::getUid, uid);
        if (status != null) lqw.eq(SvcInvoice::getStatus, status);
        if (type != null) lqw.eq(SvcInvoice::getType, type);
        if (keywords != null && !keywords.trim().isEmpty()) {
            String kw = keywords.trim();
            lqw.and(w -> w.like(SvcInvoice::getInvoiceNo, kw)
                    .or().like(SvcInvoice::getTitle, kw)
                    .or().like(SvcInvoice::getTaxNo, kw)
                    .or().like(SvcInvoice::getContractNo, kw));
        }
        // 待办优先：待开票永远排最前
        lqw.orderByAsc(SvcInvoice::getStatus).orderByDesc(SvcInvoice::getId);
        List<SvcInvoice> list = svcInvoiceDao.selectList(lqw);
        fillInvoice(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcInvoice adminInvoiceInfo(Integer id) {
        SvcInvoice invoice = mustInvoice(id);
        fillInvoice(invoice, true);
        return invoice;
    }

    /**
     * 后台保存 / 开票 / 变更状态。
     *
     * 一次调用同时承担三件事，是因为设计稿里这就是「一张表单 + 一个状态下拉」：
     *   · 补全或修正抬头信息（客户填错时后台可改）；
     *   · 上传成品发票文件（PDF/图片）；
     *   · 变更状态：填了成品件并选「已开票」即完成开票，选「已驳回」需填原因。
     */
    @Override
    public Integer saveInvoice(SvcRequests.InvoiceSaveRequest request) {
        SvcInvoice invoice;
        if (request.getId() != null && request.getId() > 0) {
            invoice = mustInvoice(request.getId());
        } else {
            invoice = new SvcInvoice();
            invoice.setInvoiceNo(makeInvoiceNo());
            invoice.setUid(request.getUid() == null ? 0 : request.getUid());
            invoice.setStatus(SvcInvoice.STATUS_WAIT);
            invoice.setLog("[]");
            invoice.setIsDel(0);
        }
        Integer oldStatus = invoice.getStatus();
        if (request.getUid() != null && request.getUid() > 0) invoice.setUid(request.getUid());
        if (request.getContractId() != null) invoice.setContractId(request.getContractId());
        if (request.getType() != null) invoice.setType(request.getType());
        if (request.getTitleType() != null) invoice.setTitleType(request.getTitleType());
        invoice.setTitle(blankToEmpty(request.getTitle()));
        invoice.setTaxNo(blankToEmpty(request.getTaxNo()));
        invoice.setBank(blankToEmpty(request.getBank()));
        invoice.setBankAccount(blankToEmpty(request.getBankAccount()));
        invoice.setAddr(blankToEmpty(request.getAddr()));
        invoice.setPhone(blankToEmpty(request.getPhone()));
        if (request.getAmount() != null) invoice.setAmount(request.getAmount());
        invoice.setContent(blankToEmpty(request.getContent()));
        invoice.setReceiver(blankToEmpty(request.getReceiver()));
        invoice.setReceiverPhone(blankToEmpty(request.getReceiverPhone()));
        invoice.setReceiverAddr(blankToEmpty(request.getReceiverAddr()));
        invoice.setEmail(blankToEmpty(request.getEmail()));
        invoice.setRemark(blankToEmpty(request.getRemark()));
        // 合同：填了合同ID就同步冗余编号，便于列表直接展示
        if (invoice.getContractId() != null && invoice.getContractId() > 0) {
            SvcContract c = svcContractDao.selectById(invoice.getContractId());
            if (c != null && c.getIsDel() != null && c.getIsDel() == 0) {
                invoice.setContractNo(blankToEmpty(c.getContractNo()));
            }
        }
        // 成品件与邮寄信息
        if (request.getFileUrl() != null) {
            invoice.setFileUrl(blankToEmpty(request.getFileUrl()));
            invoice.setFileName(blankToEmpty(request.getFileName()));
        }
        invoice.setExpressCo(blankToEmpty(request.getExpressCo()));
        invoice.setExpressNo(blankToEmpty(request.getExpressNo()));
        invoice.setSentDate(blankToEmpty(request.getSentDate()));
        invoice.setRejectReason(blankToEmpty(request.getRejectReason()));
        if (request.getOperator() != null) {
            invoice.setOperator(blankToEmpty(request.getOperator()));
        }

        // 状态流转
        if (request.getStatus() != null) {
            Integer newStatus = request.getStatus();
            if (!newStatus.equals(oldStatus)) {
                if (!SvcInvoice.statusName(newStatus).isEmpty()) {
                    if (SvcInvoice.STATUS_REJECT.equals(newStatus) && blankToEmpty(request.getRejectReason()).isEmpty()) {
                        throw new RuntimeException("驳回时请填写驳回原因");
                    }
                    if (SvcInvoice.STATUS_ISSUED.equals(newStatus) && blankToEmpty(invoice.getFileUrl()).isEmpty()) {
                        throw new RuntimeException("请先上传成品发票文件，再置为「已开票」");
                    }
                    invoice.setStatus(newStatus);
                    if (SvcInvoice.STATUS_ISSUED.equals(newStatus) && invoice.getIssueTime() == null) {
                        invoice.setIssueTime(new Date());
                    }
                    invoice.setLog(appendInvoiceLog(invoice.getLog(), newStatus,
                            blankToEmpty(request.getOperator()),
                            blankToEmpty(request.getLogRemark()).isEmpty()
                                    ? SvcInvoice.statusName(newStatus) : blankToEmpty(request.getLogRemark())));
                }
            }
        }

        if (invoice.getId() == null) {
            svcInvoiceDao.insert(invoice);
        } else {
            svcInvoiceDao.updateById(invoice);
        }
        log.info("发票申请 {} 已保存，状态 {}", invoice.getInvoiceNo(), SvcInvoice.statusName(invoice.getStatus()));
        return invoice.getId();
    }

    @Override
    public Boolean deleteInvoice(Integer id) {
        SvcInvoice exist = mustInvoice(id);
        exist.setIsDel(1);
        return svcInvoiceDao.updateById(exist) > 0;
    }

    @Override
    public HashMap<String, Object> adminInvoiceStats() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("waitCount", countInvoiceByStatus(SvcInvoice.STATUS_WAIT));
        map.put("issuingCount", countInvoiceByStatus(SvcInvoice.STATUS_ISSUING));
        map.put("issuedCount", countInvoiceByStatus(SvcInvoice.STATUS_ISSUED));
        // 本月开票金额
        LambdaQueryWrapper<SvcInvoice> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcInvoice::getIsDel, 0);
        lqw.eq(SvcInvoice::getStatus, SvcInvoice.STATUS_ISSUED);
        lqw.ge(SvcInvoice::getUpdateTime, cn.hutool.core.date.DateUtil.beginOfMonth(new Date()));
        List<SvcInvoice> list = svcInvoiceDao.selectList(lqw);
        BigDecimal total = BigDecimal.ZERO;
        for (SvcInvoice inv : list) {
            total = total.add(inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount());
        }
        map.put("monthAmount", total);
        return map;
    }

    private Integer countInvoiceByStatus(Integer status) {
        LambdaQueryWrapper<SvcInvoice> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcInvoice::getIsDel, 0);
        lqw.eq(SvcInvoice::getStatus, status);
        return svcInvoiceDao.selectCount(lqw);
    }

    // ==================== 会员端：发票管理 ====================

    @Override
    public Integer submitInvoice(Integer uid, SvcRequests.InvoiceSubmitRequest request) {
        SvcInvoice invoice = new SvcInvoice();
        invoice.setInvoiceNo(makeInvoiceNo());
        invoice.setUid(uid);
        invoice.setContractId(request.getContractId() == null || request.getContractId() <= 0
                ? null : request.getContractId());
        if (invoice.getContractId() != null) {
            SvcContract c = svcContractDao.selectById(invoice.getContractId());
            if (c != null && c.getIsDel() != null && c.getIsDel() == 0 && uid.equals(c.getUid())) {
                invoice.setContractNo(blankToEmpty(c.getContractNo()));
            } else {
                invoice.setContractId(null);
            }
        }
        invoice.setType(request.getType() == null ? SvcInvoice.TYPE_NORMAL : request.getType());
        invoice.setTitleType(request.getTitleType() == null ? SvcInvoice.TITLE_COMPANY : request.getTitleType());
        invoice.setTitle(blankToEmpty(request.getTitle()));
        invoice.setTaxNo(blankToEmpty(request.getTaxNo()));
        invoice.setBank(blankToEmpty(request.getBank()));
        invoice.setBankAccount(blankToEmpty(request.getBankAccount()));
        invoice.setAddr(blankToEmpty(request.getAddr()));
        invoice.setPhone(blankToEmpty(request.getPhone()));
        invoice.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
        invoice.setContent(blankToEmpty(request.getContent()));
        invoice.setReceiver(blankToEmpty(request.getReceiver()));
        invoice.setReceiverPhone(blankToEmpty(request.getReceiverPhone()));
        invoice.setReceiverAddr(blankToEmpty(request.getReceiverAddr()));
        invoice.setEmail(blankToEmpty(request.getEmail()));
        invoice.setRemark(blankToEmpty(request.getRemark()));
        invoice.setStatus(SvcInvoice.STATUS_WAIT);
        invoice.setFileUrl("");
        invoice.setFileName("");
        invoice.setExpressCo("");
        invoice.setExpressNo("");
        invoice.setRejectReason("");
        invoice.setOperator("");
        invoice.setLog(appendInvoiceLog(null, SvcInvoice.STATUS_WAIT, "客户", "提交开票申请"));
        invoice.setIsDel(0);
        svcInvoiceDao.insert(invoice);
        log.info("会员 {} 提交开票申请 {}，金额 {}", uid, invoice.getInvoiceNo(), invoice.getAmount());
        return invoice.getId();
    }

    @Override
    public CommonPage<SvcInvoice> myInvoiceList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcInvoice> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcInvoice::getIsDel, 0);
        lqw.eq(SvcInvoice::getUid, uid);
        if (status != null) lqw.eq(SvcInvoice::getStatus, status);
        lqw.orderByDesc(SvcInvoice::getId);
        List<SvcInvoice> list = svcInvoiceDao.selectList(lqw);
        fillInvoice(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcInvoice myInvoiceDetail(Integer uid, Integer id) {
        SvcInvoice invoice = mustInvoice(id);
        if (invoice.getUid() == null || !invoice.getUid().equals(uid)) {
            throw new RuntimeException("开票申请不存在");
        }
        fillInvoice(invoice, true);
        return invoice;
    }

    @Override
    public Boolean cancelInvoice(Integer uid, Integer id) {
        SvcInvoice invoice = mustInvoice(id);
        if (invoice.getUid() == null || !invoice.getUid().equals(uid)) {
            throw new RuntimeException("开票申请不存在");
        }
        if (!SvcInvoice.STATUS_WAIT.equals(invoice.getStatus())) {
            throw new RuntimeException("发票已在处理中，无法撤销，请联系您的服务顾问");
        }
        invoice.setStatus(SvcInvoice.STATUS_REJECT);
        invoice.setRejectReason("客户主动撤销");
        invoice.setLog(appendInvoiceLog(invoice.getLog(), SvcInvoice.STATUS_REJECT, "客户", "客户撤销了开票申请"));
        return svcInvoiceDao.updateById(invoice) > 0;
    }

    // ---------- 发票私有辅助 ----------

    private SvcInvoice mustInvoice(Integer id) {
        SvcInvoice inv = id == null ? null : svcInvoiceDao.selectById(id);
        if (inv == null || (inv.getIsDel() != null && inv.getIsDel() > 0)) {
            throw new RuntimeException("开票申请不存在");
        }
        return inv;
    }

    private void fillInvoice(List<SvcInvoice> list) {
        if (list == null || list.isEmpty()) return;
        for (SvcInvoice inv : list) {
            fillInvoice(inv, false);
        }
    }

    private void fillInvoice(SvcInvoice inv, boolean detail) {
        if (inv == null) return;
        inv.setStatusText(SvcInvoice.statusName(inv.getStatus()));
        inv.setTypeText(SvcInvoice.typeName(inv.getType()));
        inv.setTitleTypeText(SvcInvoice.titleTypeName(inv.getTitleType()));
        if (inv.getUid() != null && inv.getUid() > 0) {
            User u = userDao.selectById(inv.getUid());
            if (u != null) {
                inv.setNickname(blankToEmpty(u.getNickname()));
                inv.setPhoneNo(blankToEmpty(u.getPhone()));
            }
        }
        if (detail) {
            inv.setLogList(parseInvoiceLog(inv.getLog()));
        }
    }

    private List<SvcInvoice.LogItem> parseInvoiceLog(String json) {
        List<SvcInvoice.LogItem> list = new ArrayList<>();
        String s = blankToEmpty(json);
        if (s.isEmpty()) return list;
        try {
            List<SvcInvoice.LogItem> items = new Gson().fromJson(s,
                    new TypeToken<List<SvcInvoice.LogItem>>() { }.getType());
            return items == null ? list : items;
        } catch (Exception e) {
            log.warn("解析发票状态日志失败：{}", e.getMessage());
            return list;
        }
    }

    private String appendInvoiceLog(String oldJson, Integer status, String operator, String remark) {
        List<SvcInvoice.LogItem> list = parseInvoiceLog(oldJson);
        SvcInvoice.LogItem item = new SvcInvoice.LogItem();
        item.setTime(nowStr());
        item.setStatus(status);
        item.setStatusName(SvcInvoice.statusName(status));
        item.setOperator(blankToEmpty(operator));
        item.setRemark(blankToEmpty(remark));
        list.add(0, item);
        return new Gson().toJson(list);
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

    // ==================== 会员端：服务需求（客户只提交、只查看，不参与确认） ====================

    @Override
    public Integer submitDemand(Integer uid, SvcRequests.DemandSubmitRequest request) {
        SvcDemand demand = new SvcDemand();
        demand.setDemandNo(makeDemandNo());
        demand.setUid(uid);
        demand.setType(blankToEmpty(request.getType()));
        demand.setTitle(blankToEmpty(request.getTitle()));
        demand.setContent(blankToEmpty(request.getContent()));
        demand.setBudget(blankToEmpty(request.getBudget()));
        demand.setHopeDate(blankToEmpty(request.getHopeDate()));
        demand.setAttach(blankToEmpty(request.getAttach()));
        demand.setAttachName(blankToEmpty(request.getAttachName()));
        demand.setContactName(blankToEmpty(request.getContactName()));
        demand.setContactPhone(blankToEmpty(request.getContactPhone()));
        demand.setCompany(blankToEmpty(request.getCompany()));
        demand.setStatus(SvcDemand.STATUS_WAIT);
        demand.setFollowerUid(0);
        demand.setFollower("");
        demand.setAmount(BigDecimal.ZERO);
        // 首条跟进记录：让会员端「处理进度」从提交那一刻就有时间线
        demand.setFollowLog(appendFollowLog(null, "客户提交了需求，等待顾问受理", "客户"));
        demand.setIsDel(0);
        svcDemandDao.insert(demand);
        log.info("会员 {} 提交服务需求 {}：{}", uid, demand.getDemandNo(), demand.getTitle());
        return demand.getId();
    }

    @Override
    public CommonPage<SvcDemand> myDemandList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<SvcDemand> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SvcDemand::getIsDel, 0);
        lqw.eq(SvcDemand::getUid, uid);
        if (status != null) lqw.eq(SvcDemand::getStatus, status);
        lqw.orderByDesc(SvcDemand::getId);
        List<SvcDemand> list = svcDemandDao.selectList(lqw);
        fillDemand(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SvcDemand myDemandDetail(Integer uid, Integer id) {
        SvcDemand demand = mustDemand(id);
        if (demand.getUid() == null || !demand.getUid().equals(uid)) {
            throw new RuntimeException("需求不存在");
        }
        fillDemand(demand, true);
        return demand;
    }

    @Override
    public Boolean cancelDemand(Integer uid, Integer id) {
        SvcDemand demand = mustDemand(id);
        if (demand.getUid() == null || !demand.getUid().equals(uid)) {
            throw new RuntimeException("需求不存在");
        }
        if (!SvcDemand.STATUS_WAIT.equals(demand.getStatus())) {
            throw new RuntimeException("需求已被受理，无法撤销，请联系您的服务顾问");
        }
        demand.setStatus(SvcDemand.STATUS_CLOSED);
        demand.setFollowLog(appendFollowLog(demand.getFollowLog(), "客户撤销了该需求", "客户"));
        return svcDemandDao.updateById(demand) > 0;
    }

    @Override
    public Boolean acceptDemandQuote(Integer uid, Integer id) {
        SvcDemand demand = mustDemand(id);
        if (demand.getUid() == null || !demand.getUid().equals(uid)) {
            throw new RuntimeException("需求不存在");
        }
        if (!SvcDemand.STATUS_QUOTED.equals(demand.getStatus())) {
            throw new RuntimeException("当前没有待确认的报价");
        }
        demand.setStatus(SvcDemand.STATUS_DEAL);
        demand.setFollowLog(appendFollowLog(demand.getFollowLog(), "客户已同意报价，等待生成合同", "客户"));
        return svcDemandDao.updateById(demand) > 0;
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
        fillContractDetail(contract, uid);
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

        // 我的合同数（甲方或乙方都算）
        LambdaQueryWrapper<SvcContract> contractQw = new LambdaQueryWrapper<>();
        contractQw.eq(SvcContract::getIsDel, 0);
        contractQw.and(w -> w.eq(SvcContract::getUid, uid).or().eq(SvcContract::getOurUid, uid));
        map.put("contractCount", svcContractDao.selectCount(contractQw));

        // 我的需求数（未关闭的）
        LambdaQueryWrapper<SvcDemand> demandQw = new LambdaQueryWrapper<>();
        demandQw.eq(SvcDemand::getIsDel, 0);
        demandQw.eq(SvcDemand::getUid, uid);
        demandQw.ne(SvcDemand::getStatus, SvcDemand.STATUS_CLOSED);
        map.put("demandCount", svcDemandDao.selectCount(demandQw));

        // 我的开票申请数（未撤销的）
        LambdaQueryWrapper<SvcInvoice> invoiceQw = new LambdaQueryWrapper<>();
        invoiceQw.eq(SvcInvoice::getIsDel, 0);
        invoiceQw.eq(SvcInvoice::getUid, uid);
        map.put("invoiceCount", svcInvoiceDao.selectCount(invoiceQw));

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
