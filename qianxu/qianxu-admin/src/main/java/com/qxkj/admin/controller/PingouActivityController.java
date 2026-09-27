package com.qxkj.admin.controller;

import com.qxkj.common.model.pingou.PingouActivity;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PingouActivityRequest;
import com.qxkj.common.request.PingouSearchRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.PingouService;
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

/**
 * 后台 —— 拼购活动管理
 */
@Slf4j
@RestController
@RequestMapping("api/admin/pingou/activity")
@Api(tags = "后台——拼购——活动管理")
public class PingouActivityController {

    @Autowired
    private PingouService pingouService;

    /**
     * 活动分页列表
     */
    @PreAuthorize("hasAuthority('admin:pingou:activity:list')")
    @ApiOperation(value = "拼购活动分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PingouActivity>> getList(@Validated PingouSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<PingouActivity> commonPage = CommonPage.restPage(pingouService.getAdminList(request, pageParamRequest));
        return CommonResult.success(commonPage);
    }

    /**
     * 新增活动
     */
    @PreAuthorize("hasAuthority('admin:pingou:activity:save')")
    @ApiOperation(value = "新增拼购活动")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated PingouActivityRequest request) {
        if (pingouService.saveActivity(request)) {
            return CommonResult.success("新增拼购活动成功");
        }
        return CommonResult.failed("新增拼购活动失败");
    }

    /**
     * 修改活动
     */
    @PreAuthorize("hasAuthority('admin:pingou:activity:update')")
    @ApiOperation(value = "修改拼购活动")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated PingouActivityRequest request) {
        if (pingouService.updateActivity(request)) {
            return CommonResult.success("修改拼购活动成功");
        }
        return CommonResult.failed("修改拼购活动失败");
    }

    /**
     * 删除活动
     */
    @PreAuthorize("hasAuthority('admin:pingou:activity:delete')")
    @ApiOperation(value = "删除拼购活动")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (pingouService.deleteActivity(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 启用/禁用活动
     */
    @PreAuthorize("hasAuthority('admin:pingou:activity:status')")
    @ApiOperation(value = "修改拼购活动状态")
    @RequestMapping(value = "/update/status", method = RequestMethod.POST)
    public CommonResult<Object> updateStatus(@RequestParam(value = "id") Integer id, @RequestParam @Validated boolean status) {
        if (pingouService.updateActivityStatus(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
