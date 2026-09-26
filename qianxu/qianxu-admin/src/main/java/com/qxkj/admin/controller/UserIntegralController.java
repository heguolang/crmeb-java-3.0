package com.qxkj.admin.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.AdminIntegralSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.UserIntegralRecordResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserIntegralRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/user/integral")
@Api(tags = "用户积分管理")
public class UserIntegralController {

    @Autowired
    private UserIntegralRecordService integralRecordService;

    /**
     * 积分分页列表
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:user:integral:list')")
    @ApiOperation(value = "积分分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserIntegralRecordResponse>> getList(AdminIntegralSearchRequest request) {
        CommonPage<UserIntegralRecordResponse> restPage = CommonPage.restPage(integralRecordService.findAdminList(request));
        return CommonResult.success(restPage);
    }


}
