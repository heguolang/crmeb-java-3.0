package com.qxkj.admin.controller;

import com.qxkj.common.model.recruit.RecruitApply;
import com.qxkj.common.model.recruit.RecruitJob;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.RecruitRequests;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.RecruitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * 招聘系统控制器（后台管理）—— 大市场 → 招聘系统
 */
@Slf4j
@RestController
@RequestMapping("api/admin/recruit")
@Api(tags = "招聘系统 -- 后台管理")
public class RecruitAdminController {

    @Autowired
    private RecruitService recruitService;

    // ==================== 职位管理 ====================

    @PreAuthorize("hasAuthority('admin:recruit:job:list')")
    @ApiOperation(value = "职位分页列表")
    @RequestMapping(value = "/job/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<RecruitJob>> jobList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(recruitService.adminJobList(keywords, category, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:list')")
    @ApiOperation(value = "职位详情")
    @RequestMapping(value = "/job/info", method = RequestMethod.GET)
    public CommonResult<RecruitJob> jobInfo(@RequestParam Integer id) {
        return CommonResult.success(recruitService.adminJobInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:save')")
    @ApiOperation(value = "保存职位（新增/修改），返回职位 ID")
    @RequestMapping(value = "/job/save", method = RequestMethod.POST)
    public CommonResult<String> saveJob(@RequestBody @Validated RecruitRequests.JobSaveRequest request) {
        Integer id = recruitService.saveJob(request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:status')")
    @ApiOperation(value = "职位上下架")
    @RequestMapping(value = "/job/status", method = RequestMethod.POST)
    public CommonResult<String> updateJobStatus(@RequestParam Integer id, @RequestParam Integer status) {
        if (recruitService.updateJobStatus(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:delete')")
    @ApiOperation(value = "删除职位")
    @RequestMapping(value = "/job/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteJob(@RequestParam Integer id) {
        if (recruitService.deleteJob(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:list')")
    @ApiOperation(value = "职位统计：在招 / 关闭 / 累计投递 / 待联系 / 本月投递")
    @RequestMapping(value = "/job/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> jobStats() {
        return CommonResult.success(recruitService.jobStats());
    }

    // ==================== 应聘管理 ====================

    @PreAuthorize("hasAuthority('admin:recruit:apply:list')")
    @ApiOperation(value = "应聘登记分页列表")
    @RequestMapping(value = "/apply/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<RecruitApply>> applyList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "jobId", required = false) Integer jobId,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(recruitService.adminApplyList(keywords, jobId, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:recruit:apply:list')")
    @ApiOperation(value = "应聘详情")
    @RequestMapping(value = "/apply/info", method = RequestMethod.GET)
    public CommonResult<RecruitApply> applyInfo(@RequestParam Integer id) {
        return CommonResult.success(recruitService.adminApplyInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:recruit:apply:save')")
    @ApiOperation(value = "处理应聘：改状态 + 面试时间 + 备注（写跟进日志）")
    @RequestMapping(value = "/apply/handle", method = RequestMethod.POST)
    public CommonResult<String> handleApply(@RequestBody @Validated RecruitRequests.ApplyHandleRequest request) {
        if (recruitService.handleApply(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:apply:delete')")
    @ApiOperation(value = "删除应聘记录")
    @RequestMapping(value = "/apply/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteApply(@RequestParam Integer id) {
        if (recruitService.deleteApply(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:apply:list')")
    @ApiOperation(value = "应聘统计：各状态数量")
    @RequestMapping(value = "/apply/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> applyStats() {
        return CommonResult.success(recruitService.applyStats());
    }

    // ==================== 招聘设置 ====================

    @PreAuthorize("hasAuthority('admin:recruit:setting:list')")
    @ApiOperation(value = "招聘设置读取")
    @RequestMapping(value = "/setting/get", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> getSetting() {
        return CommonResult.success(recruitService.getSetting());
    }

    @PreAuthorize("hasAuthority('admin:recruit:setting:save')")
    @ApiOperation(value = "招聘设置保存")
    @RequestMapping(value = "/setting/save", method = RequestMethod.POST)
    public CommonResult<String> saveSetting(@RequestBody HashMap<String, Object> settingMap) {
        if (recruitService.saveSetting(settingMap)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:recruit:job:list')")
    @ApiOperation(value = "在招职位下拉（应聘登记/筛选用）")
    @RequestMapping(value = "/job/enabled", method = RequestMethod.GET)
    public CommonResult<List<RecruitJob>> jobEnabled() {
        PageParamRequest page = new PageParamRequest();
        page.setPage(1);
        page.setLimit(200);
        return CommonResult.success(recruitService.adminJobList(null, null, RecruitJob.STATUS_ON, page).getList());
    }
}
