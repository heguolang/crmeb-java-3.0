package com.qxkj.admin.controller;

import com.qxkj.admin.service.WeChatMiniCodeDownloadService;
import com.qxkj.common.result.CommonResult;
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
@RequestMapping("api/admin/wechat/code")
@Api(tags = "微信开放平台 -- 微信小程序源码下载")
public class WeChatMiniCodeDownloadController {


    @Autowired
    WeChatMiniCodeDownloadService weChatMiniCodeDownloadService;

    @PreAuthorize("hasAuthority('admin:wechat:code:download')")
    @ApiOperation(value = "小程序源码下载")
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public CommonResult<String>  getList() {
        String miniCodeSourcePath = weChatMiniCodeDownloadService.WeChatMiniCodeDownload();
        return CommonResult.success(miniCodeSourcePath);
    }
}
