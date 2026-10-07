package com.qxkj.service.service;

import com.qxkj.common.model.recruit.RecruitApply;
import com.qxkj.common.model.recruit.RecruitJob;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.RecruitRequests;

import java.util.HashMap;
import java.util.List;

/**
 * 招聘系统服务（后台发布职位 + 会员端投递 + 后台跟进）
 */
public interface RecruitService {

    // ==================== 后台：职位管理 ====================

    /** 职位分页列表（后台） */
    CommonPage<RecruitJob> adminJobList(String keywords, String category, Integer status, PageParamRequest page);

    /** 职位详情（后台） */
    RecruitJob adminJobInfo(Integer id);

    /** 保存职位（新增/修改），返回职位 ID */
    Integer saveJob(RecruitRequests.JobSaveRequest request);

    /** 职位上下架 */
    Boolean updateJobStatus(Integer id, Integer status);

    /** 删除职位（软删） */
    Boolean deleteJob(Integer id);

    /** 职位统计：在招职位数 / 关闭数 / 累计投递 / 本月投递 */
    HashMap<String, Object> jobStats();

    // ==================== 后台：应聘管理 ====================

    /** 应聘分页列表（后台） */
    CommonPage<RecruitApply> adminApplyList(String keywords, Integer jobId, Integer status, PageParamRequest page);

    /** 应聘详情（后台） */
    RecruitApply adminApplyInfo(Integer id);

    /** 处理应聘：改状态 + 备注 + 面试时间 */
    Boolean handleApply(RecruitRequests.ApplyHandleRequest request);

    /** 删除应聘记录（软删） */
    Boolean deleteApply(Integer id);

    /** 应聘统计：各状态数量 */
    HashMap<String, Object> applyStats();

    // ==================== 后台：招聘设置 ====================

    /** 招聘设置读取 */
    HashMap<String, Object> getSetting();

    /** 招聘设置保存 */
    Boolean saveSetting(HashMap<String, Object> settingMap);

    // ==================== 会员端 ====================

    /** 招聘配置（免登录：开关 + 公司名 + banner + 联系方式） */
    HashMap<String, Object> getConfig();

    /** 在招职位分页列表（免登录） */
    CommonPage<RecruitJob> frontJobList(String keywords, String category, String city, PageParamRequest page);

    /** 职位详情（免登录，累计浏览量） */
    RecruitJob frontJobInfo(Integer id);

    /** 职位类别列表（免登录，带每个类别的在招数量） */
    List<HashMap<String, Object>> frontCategoryList();

    /** 投递应聘（可游客，uid=0） */
    Integer submitApply(Integer uid, RecruitRequests.ApplySubmitRequest request);

    /** 我的投递（需登录） */
    CommonPage<RecruitApply> myApplyList(Integer uid, PageParamRequest page);

    /** 我的投递详情 */
    RecruitApply myApplyInfo(Integer uid, Integer id);

    /** 发送投递验证码 */
    Boolean sendApplyCode(String phone);

    /** 校验投递验证码 */
    void checkApplyCode(String phone, String code);
}
