package com.qxkj.admin.controller;

import com.qxkj.common.response.ShippingTemplatesFreeResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.ShippingTemplatesFreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("api/admin/express/shipping/free")
@Api(tags = "设置 -- 物流 -- 免费")
public class ShippingTemplatesFreeController {

    @Autowired
    private ShippingTemplatesFreeService shippingTemplatesFreeService;

    /**
     * 根据模板id查询数据
     * @param tempId Integer 模板id
     */
    @PreAuthorize("hasAuthority('admin:shipping:templates:free:list')")
    @ApiOperation(value = "根据模板id查询数据")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<ShippingTemplatesFreeResponse>> getList(@RequestParam Integer tempId){
        return CommonResult.success(shippingTemplatesFreeService.getListGroup(tempId));
    }

}
