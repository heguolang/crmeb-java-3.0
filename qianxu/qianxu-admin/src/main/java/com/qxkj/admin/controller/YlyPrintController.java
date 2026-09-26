package com.qxkj.admin.controller;


import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.YlyPrintService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("api/admin/yly")
@Api(tags = "易联云 打印订单小票") //配合swagger使用
public class YlyPrintController {


    @Autowired
    private YlyPrintService ylyPrintService;

    @PreAuthorize("hasAuthority('admin:yly:print')")
    @ApiOperation(value = "打印小票")
    @RequestMapping(value = "/print/{ordid}", method = RequestMethod.GET)
    public CommonResult<String> updateStatus(@PathVariable  String ordid) {
        ylyPrintService.YlyPrint(ordid,false);
        return CommonResult.success();
    }
}
