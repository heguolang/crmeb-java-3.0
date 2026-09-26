package com.qxkj.admin.controller;

import com.qxkj.common.model.wechat.video.PayComponentShopBrand;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.ShopUploadImgRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.BaseResultResponseVo;
import com.qxkj.common.vo.RegisterCheckResponseVo;
import com.qxkj.common.vo.ShopAuditBrandRequestVo;
import com.qxkj.common.vo.WechatVideoUploadImageResponseVo;
import com.qxkj.service.service.PayComponentShopService;
import com.qxkj.service.service.WechatVideoBeforeService;
import com.qxkj.service.service.WechatVideoShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("api/admin/pay/component/shop")
@Api(tags = "自定义交易组件—商家及接入前") //配合swagger使用
public class PayComponentShopController {

    @Autowired
    private PayComponentShopService shopService;

    @Autowired
    private WechatVideoShopService wechatVideoShopService;

    @Autowired
    private WechatVideoBeforeService wechatVideoBeforeService;

    /**
     * 小程序接入申请
     * @return 申请结果
     */
    @PreAuthorize("hasAuthority('admin:pay:component:shop:register')")
    @ApiOperation(value = "小程序接入申请")
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public CommonResult<BaseResultResponseVo> shopRegisterApply() {
        return CommonResult.success(wechatVideoShopService.shopRegisterApply());
    }

    /**
     * 小程序接入状态检查
     * @return 接入状态
     */
    @PreAuthorize("hasAuthority('admin:pay:component:shop:register:check')")
    @ApiOperation(value = "获取小程序接入状态")
    @RequestMapping(value = "/register/check", method = RequestMethod.GET)
    public CommonResult<RegisterCheckResponseVo> shopRegisterCheck() {
        return CommonResult.success(wechatVideoShopService.shopRegisterCheck());
    }

    // 获取类目详情在其他业务中实现
    @PreAuthorize("hasAuthority('admin:pay:component:shop:img:upload')")
    @ApiOperation(value = "上传图片，只用于品牌和类目申请")
    @RequestMapping(value = "/img/upload", method = RequestMethod.POST)
    public CommonResult<WechatVideoUploadImageResponseVo> shopImgUpload(@RequestBody ShopUploadImgRequest request) {
        return CommonResult.success(wechatVideoBeforeService.shopImgUpload(request));
    }

    @PreAuthorize("hasAuthority('admin:pay:component:shop:brand:audit')")
    @ApiOperation(value = "上传品牌信息")
    @RequestMapping(value = "/brand/audit", method = RequestMethod.POST)
    public CommonResult<Object> shopAuditBrand(@RequestBody @Validated ShopAuditBrandRequestVo request) {
        return CommonResult.success(shopService.auditBrand(request));
    }

    @PreAuthorize("hasAuthority('admin:pay:component:shop:brand:list')")
    @ApiOperation(value = "品牌列表")
    @RequestMapping(value = "/brand/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PayComponentShopBrand>> shopBrandList(@Validated PageParamRequest pageParamRequest, @RequestParam(value = "status", required = false) Integer status) {
        return CommonResult.success(CommonPage.restPage(shopService.brandList(pageParamRequest, status)));
    }

    @PreAuthorize("hasAuthority('admin:pay:component:shop:brand:usable:list')")
    @ApiOperation(value = "品牌列表(可用)")
    @RequestMapping(value = "/brand/usable/list", method = RequestMethod.GET)
    public CommonResult<List<PayComponentShopBrand>> shopUsableBrandList() {
        return CommonResult.success(shopService.usableBrandList());
    }
}
