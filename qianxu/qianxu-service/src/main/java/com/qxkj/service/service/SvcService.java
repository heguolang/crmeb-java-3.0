package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;

import java.util.HashMap;
import java.util.List;

/**
 * 企业服务（软件开发服务：合同签约 + 项目进度 + 客户案例 + 服务设置）
 */
public interface SvcService extends IService<SvcProject> {

    // ==================== 后台 ====================

    /** 后台-合同分页列表 */
    CommonPage<SvcContract> adminContractList(Integer uid, String keywords, Integer status, PageParamRequest page);

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
