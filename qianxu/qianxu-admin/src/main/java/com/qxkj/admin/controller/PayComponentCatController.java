package com.qxkj.admin.controller;

import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.CatItem;
import com.qxkj.service.service.PayComponentCatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
@RequestMapping("api/admin/pay/component/cat")
@Api(tags = "自定义交易组件—组件类目") //配合swagger使用
public class PayComponentCatController {

    @Autowired
    private PayComponentCatService payComponentCatService;

    /**
     * 获取类目列表
     */
    @PreAuthorize("hasAuthority('admin:pay:component:cat:list')")
    @ApiOperation(value = "获取类目列表")
    @RequestMapping(value = "/get/list", method = RequestMethod.GET)
    public CommonResult<List<CatItem>> getList() {
        return CommonResult.success(payComponentCatService.getList());
    }

    /**
     * 获取类目(测试用，前端不调用)
     */
    @ApiOperation(value = "获取类目(测试用，前端不调用)")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public CommonResult<Object> sendUserCode() {
        payComponentCatService.autoUpdate();
        return CommonResult.success();
    }
}



