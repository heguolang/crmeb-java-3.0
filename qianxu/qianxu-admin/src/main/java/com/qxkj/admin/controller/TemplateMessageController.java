package com.qxkj.admin.controller;


import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.TemplateMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("api/admin/wechat/template")
@Api(tags = "微信 -- 消息模版") //配合swagger使用
public class TemplateMessageController {

    @Autowired
    private TemplateMessageService templateMessageService;

    /**
     * 公众号模板消息同步
     */
    @PreAuthorize("hasAuthority('admin:wechat:whcbqhn:sync')")
    @ApiOperation(value = "公众号模板消息同步")
    @RequestMapping(value = "/whcbqhn/sync", method = RequestMethod.POST)
    public CommonResult<String> whcbqhnSync() {
        if (templateMessageService.whcbqhnSync()) {
            return CommonResult.success("公众号模板消息同步成功");
        }
        return CommonResult.failed("公众号模板消息同步失败");
    }

    /**
     * 小程序订阅消息同步
     */
    @PreAuthorize("hasAuthority('admin:wechat:routine:sync')")
    @ApiOperation(value = "小程序订阅消息同步")
    @RequestMapping(value = "/routine/sync", method = RequestMethod.POST)
    public CommonResult<String> routineSync() {
        if (templateMessageService.routineSync()) {
            return CommonResult.success("小程序订阅消息同步成功");
        }
        return CommonResult.failed("小程序订阅消息同步失败");
    }
}



