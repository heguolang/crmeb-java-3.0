package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcDemand;
import com.qxkj.common.model.svc.SvcInvoice;
import com.qxkj.common.model.svc.SvcPlatform;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;

import java.util.HashMap;
import java.util.List;

/**
 * 企业服务（软件开发服务：服务需求 + 合同签约 + 项目进度 + 客户案例 + 服务设置）
 *
 * 签约口径（2026-10-02 定案）：合同在线下签署，线上只做「登记 → 选状态 → 归档」，
 * 不提供双方在线签署与客户确认；客户在会员端只读。
 */
public interface SvcService extends IService<SvcProject> {

    // ==================== 后台：服务需求 ====================

    /** 后台-服务需求分页列表 */
    CommonPage<SvcDemand> adminDemandList(Integer uid, String keywords, String type, Integer status, PageParamRequest page);

    /** 后台-服务需求详情（含跟进记录与附件） */
    SvcDemand adminDemandInfo(Integer id);

    /** 后台-保存服务需求（新增/修改），返回需求 ID */
    Integer saveDemand(SvcRequests.DemandSaveRequest request);

    /** 后台-删除服务需求 */
    Boolean deleteDemand(Integer id);

    /** 后台-需求报价 / 指派跟进人（金额非空时视为发布报价） */
    Boolean quoteDemand(SvcRequests.DemandQuoteRequest request);

    /** 后台-追加一条跟进记录 */
    Boolean saveDemandFollow(SvcRequests.DemandFollowRequest request);

    /** 后台-关闭需求（不再推进，记录保留；不出现在待办统计里） */
    Boolean closeDemand(Integer id, String reason);

    /** 后台-需求转合同：把需求的客户与报价带进合同，登记成功后回写 demand.contract_id */
    Integer demandToContract(Integer demandId, SvcRequests.ContractSaveRequest request);

    /** 后台-服务需求统计（待受理 / 跟进中 / 已报价 / 本期已签约） */
    HashMap<String, Object> adminDemandStats();

    // ==================== 后台：签约渠道平台 ====================

    /** 后台-签约渠道平台分页列表 */
    CommonPage<SvcPlatform> adminPlatformList(String keywords, PageParamRequest page);

    /** 后台-启用的签约渠道平台（登记合同「电子合同」下拉用；含「纸质合同」「电子合同（其他）」两个固定项由前端补） */
    List<SvcPlatform> enabledPlatforms();

    /** 后台-保存签约渠道平台（新增/修改） */
    Boolean savePlatform(SvcRequests.PlatformSaveRequest request);

    /** 后台-删除签约渠道平台 */
    Boolean deletePlatform(Integer id);

    // ==================== 后台：发票管理 ====================

    /** 后台-发票申请分页列表 */
    CommonPage<SvcInvoice> adminInvoiceList(Integer uid, String keywords, Integer status, Integer type, PageParamRequest page);

    /** 后台-发票申请详情（含状态日志） */
    SvcInvoice adminInvoiceInfo(Integer id);

    /** 后台-保存 / 开票 / 变更发票状态：填了成品件与状态即完成开票，写状态日志 */
    Integer saveInvoice(SvcRequests.InvoiceSaveRequest request);

    /** 后台-删除发票申请 */
    Boolean deleteInvoice(Integer id);

    /** 后台-发票统计（待开票 / 开票中 / 已开票 / 本月开票金额） */
    HashMap<String, Object> adminInvoiceStats();

    // ==================== 后台：合同 ====================

    /** 后台-合同分页列表 */
    CommonPage<SvcContract> adminContractList(Integer uid, String keywords, Integer status, String signChannel, PageParamRequest page);

    /** 后台-合同详情（不做归属校验） */
    SvcContract adminContractInfo(Integer id);

    /** 后台-保存合同（新增/修改），返回合同 ID（失败返回 null） */
    Integer saveContract(SvcRequests.ContractSaveRequest request);

    /** 后台-删除合同 */
    Boolean deleteContract(Integer id);

    /** 后台-把合同正文（annex 第一份）转换成 PDF 并记录页数，返回 {srcPdf, srcPdfName, pageCount} */
    HashMap<String, Object> prepareContractPdf(Integer id);

    /** 后台-保存合同用印位置布局（sign_layout JSON） */
    Boolean saveContractLayout(Integer id, String signLayout);

    /** 后台-把合同正文某页渲染成 PNG，供「用印位置设置」当底图 */
    byte[] renderContractPage(Integer id, int pageNo, int maxWidth);

    /** 后台-读取乙方（黔序）签约方默认值，新建合同时自动带出 */
    HashMap<String, Object> ourDefault();

    /** 后台-保存乙方（黔序）签约方默认值（our_uid + 5 项信息），存 eb_system_config */
    Boolean saveOurDefault(SvcRequests.ContractOurDefaultRequest request);

    /** 后台-变更合同签约状态：写一条状态日志；置为「已生效」时自动建项目 */
    Boolean changeContractStatus(SvcRequests.ContractStatusRequest request);

    /** 后台-合同统计：各状态数量 + 本月签约总额 */
    HashMap<String, Object> adminContractStats();

    /** 后台-项目分页列表 */
    CommonPage<SvcProject> adminProjectList(Integer uid, String keywords, Integer status, PageParamRequest page);

    /** 后台-保存项目（新增/修改） */
    Boolean saveProject(SvcRequests.ProjectSaveRequest request);

    /** 后台-删除项目 */
    Boolean deleteProject(Integer id);

    /** 后台-项目进度记录列表 */
    List<SvcProjectProgress> progressListOf(Integer projectId);

    /** 后台-设置项目进度（追加进度记录 + 同步项目进度/阶段） */
    Boolean saveProgress(SvcRequests.ProgressSetRequest request);

    /** 后台-客户案例分页列表 */
    CommonPage<SvcCase> adminCaseList(String keywords, PageParamRequest page);

    /** 后台-保存客户案例 */
    Boolean saveCase(SvcRequests.CaseSaveRequest request);

    /** 后台-删除客户案例 */
    Boolean deleteCase(Integer id);

    /** 后台-服务设置读取 */
    HashMap<String, Object> getSetting();

    /** 后台-服务设置保存 */
    Boolean saveSetting(HashMap<String, Object> settingMap);

    // ==================== 会员端 ====================

    /** 前台-服务配置（免登录：模式开关 + 公司名 + 联系方式 + 签约短信开关） */
    HashMap<String, Object> getConfig();

    /** 前台-客户案例列表（免登录） */
    List<SvcCase> frontCaseList(Integer limit);

    // ==================== 会员端：服务需求 ====================

    /** 会员端-提交服务需求，返回需求 ID */
    Integer submitDemand(Integer uid, SvcRequests.DemandSubmitRequest request);

    /** 会员端-我的需求分页列表 */
    CommonPage<SvcDemand> myDemandList(Integer uid, Integer status, PageParamRequest page);

    /** 会员端-需求详情（校验归属） */
    SvcDemand myDemandDetail(Integer uid, Integer id);

    /** 会员端-撤销需求（仅「待受理」状态允许） */
    Boolean cancelDemand(Integer uid, Integer id);

    /** 会员端-同意报价（需求 → 已成单，等后台转合同） */
    Boolean acceptDemandQuote(Integer uid, Integer id);

    // ==================== 会员端：发票管理 ====================

    /** 会员端-提交开票申请，返回申请 ID */
    Integer submitInvoice(Integer uid, SvcRequests.InvoiceSubmitRequest request);

    /** 会员端-我的开票申请分页列表 */
    CommonPage<SvcInvoice> myInvoiceList(Integer uid, Integer status, PageParamRequest page);

    /** 会员端-开票申请详情（校验归属） */
    SvcInvoice myInvoiceDetail(Integer uid, Integer id);

    /** 会员端-撤销申请（仅「待开票」且未开票时允许） */
    Boolean cancelInvoice(Integer uid, Integer id);

    /** 会员端-我的合同分页列表（uid 命中 uid=甲方 或 our_uid=乙方，带角色标记） */
    CommonPage<SvcContract> myContractList(Integer uid, Integer status, PageParamRequest page);

    /** 会员端-合同详情（甲方或乙方均可访问，带角色标记与双方签署进度） */
    SvcContract myContractDetail(Integer uid, Integer id);

    /** 会员端-签署合同（按登录账号自动判定甲/乙方；双方齐备时自动合成正式合同） */
    Boolean signContract(Integer uid, SvcRequests.ContractSignRequest request);

    /** 会员端-发送签约短信验证码 */
    Boolean sendSignCode(String phone);

    /** 会员端-校验签约短信验证码（通过后删除验证码） */
    void checkSignCode(String phone, String code);

    /** 会员端-我的项目分页列表 */
    CommonPage<SvcProject> myProjectList(Integer uid, Integer status, PageParamRequest page);

    /** 会员端-项目详情（含进度记录，校验归属） */
    SvcProject myProjectDetail(Integer uid, Integer id);

    /** 会员端-会员中心统计：进行中项目 / 待验收 / 账户余额 */
    HashMap<String, Object> myStats(Integer uid);

    /** 会员端-最新动态（取最近进度记录） */
    List<SvcProjectProgress> myDynamic(Integer uid, Integer limit);
}
