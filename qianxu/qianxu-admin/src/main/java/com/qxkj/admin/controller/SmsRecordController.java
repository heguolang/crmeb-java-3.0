package com.qxkj.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsChannelConfigRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.request.SmsTestSendRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.service.service.SmsRecordService;
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

    @Autowired
    private SmsRecordService smsRecordService;

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

    // ---------------------------------------------------------------- 滨礼短信

    /**
     * 发送记录分页列表
     */
    @PreAuthorize("hasAuthority('admin:sms:record:list')")
    @ApiOperation(value = "短信发送记录列表")
    @RequestMapping(value = "/record/list", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> recordList(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keywords", required = false) String keywords,
            @ModelAttribute PageParamRequest pageParamRequest) {
        return CommonResult.success(smsRecordService.getList(phone, status, keywords, pageParamRequest));
    }

    /**
     * 从滨礼拉取状态报告，更新本地记录回执状态
     */
    @PreAuthorize("hasAuthority('admin:sms:record:sync')")
    @ApiOperation(value = "同步短信状态回执")
    @RequestMapping(value = "/record/sync", method = RequestMethod.POST)
    public CommonResult<Integer> syncRecordStatus() {
        return CommonResult.success(smsRecordService.syncStatusFromBinli());
    }

    /**
     * 滨礼账户余额
     */
    @PreAuthorize("hasAuthority('admin:sms:balance')")
    @ApiOperation(value = "滨礼账户余额")
    @RequestMapping(value = "/balance", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> balance() {
        return CommonResult.success(smsService.balance());
    }

    /**
     * 测试发送（自由内容，走当前通道）
     */
    @PreAuthorize("hasAuthority('admin:sms:send:test')")
    @ApiOperation(value = "短信测试发送")
    @RequestMapping(value = "/send/test", method = RequestMethod.POST)
    public CommonResult<JSONObject> testSend(@RequestBody @Validated SmsTestSendRequest request) {
        ValidateFormUtil.isPhoneException(request.getPhone());
        String taskId = smsService.sendContent(request.getPhone(), request.getContent());
        JSONObject data = new JSONObject();
        data.put("taskId", taskId);
        return CommonResult.success(data);
    }

    /**
     * 读取短信通道配置（账号/密码掩码/mock/通道）
     */
    @PreAuthorize("hasAuthority('admin:sms:channel:get')")
    @ApiOperation(value = "读取短信通道配置")
    @RequestMapping(value = "/channel/config", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> channelConfig() {
        return CommonResult.success(smsService.getChannelConfig());
    }

    /**
     * 保存短信通道配置（账号/接口密码/mock/通道）
     */
    @PreAuthorize("hasAuthority('admin:sms:channel:save')")
    @ApiOperation(value = "保存短信通道配置")
    @RequestMapping(value = "/channel/save", method = RequestMethod.POST)
    public CommonResult<JSONObject> saveChannelConfig(@RequestBody @Validated SmsChannelConfigRequest request) {
        if (smsService.saveChannelConfig(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

}


