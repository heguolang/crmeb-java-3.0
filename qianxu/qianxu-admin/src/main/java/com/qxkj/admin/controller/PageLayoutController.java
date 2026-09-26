package com.qxkj.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.response.pagelayout.PageLayoutBottomNavigationResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.SplashAdConfigVo;
import com.qxkj.service.service.PageLayoutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
@RequestMapping("api/admin/page/layout")
@Api(tags = "页面布局管理")
public class PageLayoutController {

    @Autowired
    private PageLayoutService pageLayoutService;

    /**
     * 页面首页
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index')")
    @ApiOperation(value = "页面首页")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> index() {
        return CommonResult.success(pageLayoutService.index());
    }

    /**
     * 页面首页保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:save')")
    @ApiOperation(value = "页面首页保存(不建议调用)")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<Object> save(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.save(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面首页banner保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index:banner:save')")
    @ApiOperation(value = "页面首页banner保存")
    @RequestMapping(value = "/index/banner/save", method = RequestMethod.POST)
    public CommonResult<Object> indexBannerSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexBannerSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面首页menu保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index:menu:save')")
    @ApiOperation(value = "页面首页menu保存")
    @RequestMapping(value = "/index/menu/save", method = RequestMethod.POST)
    public CommonResult<Object> indexMenuSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexMenuSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面首页新闻保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index:news:save')")
    @ApiOperation(value = "页面首页新闻保存")
    @RequestMapping(value = "/index/news/save", method = RequestMethod.POST)
    public CommonResult<Object> indexNewsSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexNewsSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面用户中心banner保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index:banner:save')")
    @ApiOperation(value = "页面用户中心banner保存")
    @RequestMapping(value = "/user/banner/save", method = RequestMethod.POST)
    public CommonResult<Object> userBannerSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.userBannerSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面用户中心导航保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:user:menu:save')")
    @ApiOperation(value = "页面用户中心导航保存")
    @RequestMapping(value = "/user/menu/save", method = RequestMethod.POST)
    public CommonResult<Object> userMenuSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.userMenuSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 页面用户中心商品table保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:index:table:save')")
    @ApiOperation(value = "页面用户中心商品table保存")
    @RequestMapping(value = "/index/table/save", method = RequestMethod.POST)
    public CommonResult<Object> indexTableSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.indexTableSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 分类页配置
     */
    @PreAuthorize("hasAuthority('admin:page:layout:category:config')")
    @ApiOperation(value = "获取分类页配置")
    @RequestMapping(value = "/category/config", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> categoryConfig() {
        return CommonResult.success(pageLayoutService.getCategoryConfig());
    }

    /**
     * 分类页配置保存
     */
    @PreAuthorize("hasAuthority('admin:page:layout:category:config:save')")
    @ApiOperation(value = "分类页配置保存")
    @RequestMapping(value = "/category/config/save", method = RequestMethod.POST)
    public CommonResult<Object> categoryConfigSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.categoryConfigSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:page:layout:bottom:navigation')")
    @ApiOperation(value = "页面底部导航")
    @RequestMapping(value = "/bottom/navigation/get", method = RequestMethod.GET)
    public CommonResult<PageLayoutBottomNavigationResponse> getBottomNavigation() {
        return CommonResult.success(pageLayoutService.getBottomNavigation());
    }

    @PreAuthorize("hasAuthority('admin:page:layout:bottom:navigation:save')")
    @ApiOperation(value = "底部导航保存")
    @RequestMapping(value = "/bottom/navigation/save", method = RequestMethod.POST)
    public CommonResult<Object> bottomNavigationSave(@RequestBody JSONObject jsonObject) {
        if (pageLayoutService.bottomNavigationSave(jsonObject)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:page:layout:splash:ad:get')")
    @ApiOperation(value = "获取开屏广告配置")
    @RequestMapping(value = "/splash/ad/get", method = RequestMethod.GET)
    public CommonResult<SplashAdConfigVo> getSplashAdConfig() {
        return CommonResult.success(pageLayoutService.getSplashAdConfig());
    }

    @PreAuthorize("hasAuthority('admin:page:layout:splash:ad:save')")
    @ApiOperation(value = "编辑开屏广告配置")
    @RequestMapping(value = "/splash/ad/save", method = RequestMethod.POST)
    public CommonResult<Object> splashAdConfigSave(@RequestBody @Validated SplashAdConfigVo configVo) {
        if (pageLayoutService.splashAdConfigSave(configVo)) {
            return CommonResult.success("编辑成功");
        }
        return CommonResult.failed("编辑失败");

    }
}
