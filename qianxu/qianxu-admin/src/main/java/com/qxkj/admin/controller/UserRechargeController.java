package com.qxkj.admin.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserRechargeSearchRequest;
import com.qxkj.common.response.UserRechargeResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserRechargeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;


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
@RequestMapping("api/admin/user/topUpLog")
@Api(tags = "财务 -- 充值")
public class UserRechargeController {

    @Autowired
    private UserRechargeService userRechargeService;

    /**
     * 分页显示用户充值表
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:recharge:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserRechargeResponse>>  getList(@Validated UserRechargeSearchRequest request){
        CommonPage<UserRechargeResponse> userRechargeCommonPage = CommonPage.restPage(userRechargeService.getList(request));
        return CommonResult.success(userRechargeCommonPage);
    }

    /**
     * 充值总金额
     */
    @PreAuthorize("hasAuthority('admin:recharge:balance')")
    @ApiOperation(value = "提现总金额")
    @RequestMapping(value = "/balance", method = RequestMethod.POST)
    public CommonResult<HashMap<String, BigDecimal>> balance(){
        return CommonResult.success(userRechargeService.getBalanceList());
    }
}



