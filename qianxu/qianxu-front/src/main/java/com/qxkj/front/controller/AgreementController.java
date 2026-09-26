package com.qxkj.front.controller;

import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.constants.UserLevelConstants;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("api/front/agreement")
@Api(tags = "协议控制器")
public class AgreementController {

    @Autowired
    private SystemConfigService systemConfigService;

    @ApiOperation(value = "关于我们协议 详情")
    @RequestMapping(value = "/aboutusinfo", method = RequestMethod.GET)
    public CommonResult<String> aboutusAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.ABOUTUS_AGREEMENT));
    }

    @ApiOperation(value = "平台资质证明 详情")
    @RequestMapping(value = "/intelligentinfo", method = RequestMethod.GET)
    public CommonResult<String> intelligentAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.PLATFROM_INTELLIGENT_AGREEMENT));
    }


    @ApiOperation(value = "用户注册协议 详情")
    @RequestMapping(value = "/userinfo", method = RequestMethod.GET)
    public CommonResult<String> userAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.USER_REGISTER_AGREEMENT));
    }

    @ApiOperation(value = "用户隐私协议 详情")
    @RequestMapping(value = "/userprivacyinfo", method = RequestMethod.GET)
    public CommonResult<String> userPrivacyAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.USER_PRIVACY_AGREEMENT));
    }

    @ApiOperation(value = "用户注销协议 详情")
    @RequestMapping(value = "/useraccountcancelinfo", method = RequestMethod.GET)
    public CommonResult<String> userAccountCancelAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.USER_CANCEL_AGREEMENT));
    }

    @ApiOperation(value = "商户入驻协议 详情")
    @RequestMapping(value = "/merincomminginfo", method = RequestMethod.GET)
    public CommonResult<String> merincommingAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.MERCHANT_SETTLEMENT_AGREEMENT));
    }

    @ApiOperation(value = "用户注销声明 详情")
    @RequestMapping(value = "/useraccountcancelnoticeinfo", method = RequestMethod.GET)
    public CommonResult<String> userAccountCancelNoticeAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.USER_CANCEL_NOTICE_AGREEMENT));
    }

    @ApiOperation(value = "用户等级规则说明")
    @RequestMapping(value = "/user/level/rule", method = RequestMethod.GET)
    public String userLevelRule() {
        return systemConfigService.getValueByKey(UserLevelConstants.SYSTEM_USER_LEVEL_RULE);
    }

    @ApiOperation(value = "优惠券规则 详情")
    @RequestMapping(value = "/coupon/agreement/info", method = RequestMethod.GET)
    public CommonResult<String> couponAgreementInfo() {
        return CommonResult.success(systemConfigService.getAgreementByKey(SysConfigConstants.COUPON_AGREEMENT));
    }

}
