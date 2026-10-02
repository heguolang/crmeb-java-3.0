package com.qxkj.front.controller;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SmsRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 滨礼短信推送回调（状态推送 / 上行推送）
 *
 * 前台接口无鉴权（CloseSecurityConfig 全放行），短信平台按文档要求收到后返回 HTTP 200 即可。
 */
@Slf4j
@RestController
@RequestMapping("api/front/sms/push")
@Api(tags = "短信平台推送回调")
public class SmsPushController {

    @Autowired
    private SmsRecordService smsRecordService;

    /**
     * 8、状态推送（文字短信/语音）
     */
    @ApiOperation(value = "短信状态推送回执")
    @PostMapping("/report")
    public CommonResult<JSONObject> statusReport(@RequestBody JSONObject body) {
        log.info("[滨礼短信-状态推送] {}", body);
        try {
            smsRecordService.handleStatusPush(
                    body.getString("taskId"),
                    body.getString("mobile"),
                    body.getString("status"),
                    body.getString("times"));
        } catch (Exception e) {
            // 回调处理失败不影响返回 200，避免平台重推
            log.error("[滨礼短信-状态推送] 处理失败", e);
        }
        return CommonResult.success();
    }

    /**
     * 9、上行推送（文字短信）
     */
    @ApiOperation(value = "短信上行推送")
    @PostMapping("/mo")
    public CommonResult<JSONObject> moReport(@RequestBody JSONObject body) {
        log.info("[滨礼短信-上行推送] taskId={}, mobile={}, content={}, times={}",
                body.getString("taskId"), body.getString("mobile"),
                body.getString("content"), body.getString("times"));
        return CommonResult.success();
    }
}
