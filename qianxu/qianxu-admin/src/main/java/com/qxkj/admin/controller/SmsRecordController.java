package com.qxkj.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.service.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("api/admin/sms")
@Api(tags = "短信服务")
public class SmsRecordController {

    @Autowired
    private SmsService smsService;

    /**
     * 修改签名
     */
    @PreAuthorize("hasAuthority('admin:sms:modify:sign')")
    @ApiOperation(value = "修改签名")
    @RequestMapping(value = "/modify/sign", method = RequestMethod.POST)
    public CommonResult<JSONObject> modifySign(@RequestBody @Validated SmsModifySignRequest request) {
        if (smsService.modifySign(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 短信模板
     */
    @PreAuthorize("hasAuthority('admin:sms:temps')")
    @ApiOperation(value = "短信模板")
    @RequestMapping(value = "/temps", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> temps(@ModelAttribute PageParamRequest pageParamRequest) {
        MyRecord myRecord = smsService.temps(pageParamRequest);
        return CommonResult.success(myRecord);
    }

    /**
     * 申请短信模板
     */
    @PreAuthorize("hasAuthority('admin:sms:temp:apply')")
    @ApiOperation(value = "申请短信模板")
    @RequestMapping(value = "/temp/apply", method = RequestMethod.POST)
    public CommonResult<JSONObject> applyTempMessage(@RequestBody @Validated SmsApplyTempRequest request) {
        if (smsService.applyTempMessage(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 模板申请记录
     */
    @PreAuthorize("hasAuthority('admin:sms:applys')")
    @ApiOperation(value = "模板申请记录")
    @RequestMapping(value = "/applys", method = RequestMethod.POST)
    @ApiImplicitParam(name="type", value="type (1=验证码 2=通知 3=推广)")
    public CommonResult<Map<String, Object>> applys(@RequestParam(name = "type", required = false) Integer type, @ModelAttribute PageParamRequest pageParamRequest) {
        return CommonResult.success(smsService.applys(type, pageParamRequest));
    }

}


