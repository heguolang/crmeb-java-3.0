package com.qxkj.front.controller;

import com.qxkj.common.constants.Constants;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserRechargeRequest;
import com.qxkj.common.response.OrderPayResultResponse;
import com.qxkj.common.response.UserRechargeBillRecordResponse;
import com.qxkj.common.response.UserRechargeFrontResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.front.service.UserCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
@RestController("UserRechargeController")
@RequestMapping("api/front/recharge")
@Api(tags = "用户 -- 充值")
public class UserRechargeController {
    @Autowired
    private UserCenterService userCenterService;

    /**
     * 充值额度选择
     */
    @ApiOperation(value = "充值额度选择")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<UserRechargeFrontResponse> getRechargeConfig() {
        return CommonResult.success(userCenterService.getRechargeConfig());
    }

    /**
     * 小程序充值
     */
    @ApiOperation(value = "小程序充值")
    @RequestMapping(value = "/routine", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> routineRecharge(HttpServletRequest httpServletRequest, @RequestBody @Validated UserRechargeRequest request) {
        request.setFromType(Constants.PAY_TYPE_WE_CHAT_FROM_PROGRAM);
        request.setClientIp(QianxuUtil.getClientIp(httpServletRequest));
        OrderPayResultResponse recharge = userCenterService.recharge(request);
        Map<String, Object> map = new HashMap<>();
        map.put("data", recharge);
        map.put("type", request.getFromType());
        return CommonResult.success(map);
    }

    /**
     * 公众号充值
     */
    @ApiOperation(value = "公众号充值")
    @RequestMapping(value = "/wechat", method = RequestMethod.POST)
    public CommonResult<OrderPayResultResponse> weChatRecharge(HttpServletRequest httpServletRequest, @RequestBody @Validated UserRechargeRequest request) {
        request.setClientIp(QianxuUtil.getClientIp(httpServletRequest));
        return CommonResult.success(userCenterService.recharge(request));
    }

    /**
     * App充值
     */
    @ApiOperation(value = "App充值")
    @RequestMapping(value = "/wechat/app", method = RequestMethod.POST)
    public CommonResult<OrderPayResultResponse> weChatAppRecharge(HttpServletRequest httpServletRequest, @RequestBody @Validated UserRechargeRequest request) {
        request.setClientIp(QianxuUtil.getClientIp(httpServletRequest));
        return CommonResult.success(userCenterService.recharge(request));
    }

    /**
     * 支付宝充值
     */
    @ApiOperation(value = "支付宝充值")
    @RequestMapping(value = "/alipay", method = RequestMethod.POST)
    public CommonResult<OrderPayResultResponse> aliPayRecharge(HttpServletRequest httpServletRequest, @RequestBody @Validated UserRechargeRequest request) {
        request.setClientIp(QianxuUtil.getClientIp(httpServletRequest));
        return CommonResult.success(userCenterService.aliPayRecharge(request));
    }

    /**
     * 佣金转入余额
     */
    @ApiOperation(value = "佣金转入余额")
    @RequestMapping(value = "/transferIn", method = RequestMethod.POST)
    public CommonResult<Boolean> transferIn(@RequestParam(name = "price") BigDecimal price) {
        return CommonResult.success(userCenterService.transferIn(price));
    }

    /**
     * 用户账单记录
     */
    @ApiOperation(value = "用户账单记录")
    @RequestMapping(value = "/bill/record", method = RequestMethod.GET)
    @ApiImplicitParam(name = "type", value = "记录类型：all-全部，expenditure-支出，income-收入", required = true)
    public CommonResult<CommonPage<UserRechargeBillRecordResponse>> billRecord(@RequestParam(name = "type") String type, @ModelAttribute PageParamRequest pageRequest) {
        return CommonResult.success(userCenterService.nowMoneyBillRecord(type, pageRequest));
    }
}



