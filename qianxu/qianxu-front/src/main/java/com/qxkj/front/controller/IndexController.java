package com.qxkj.front.controller;


import com.qxkj.common.model.system.SystemConfig;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.CopyrightConfigInfoResponse;
import com.qxkj.common.response.IndexInfoResponse;
import com.qxkj.common.response.IndexProductResponse;
import com.qxkj.common.response.pagelayout.PageLayoutBottomNavigationResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.SplashAdConfigVo;
import com.qxkj.front.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
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
@RestController("IndexController")
@RequestMapping("api/front")
@Api(tags = "首页")
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 首页数据
     */
    @ApiOperation(value = "首页数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<IndexInfoResponse> getIndexInfo() {
        return CommonResult.success(indexService.getIndexInfo());
    }

    /**
     * 首页商品列表
     */
    @ApiOperation(value = "首页商品列表")
    @RequestMapping(value = "/index/product/{type}", method = RequestMethod.GET)
    @ApiImplicitParam(name = "type", value = "类型 【1 精品推荐 2 热门榜单 3首发新品 4促销单品】", dataType = "int", required = true)
    public CommonResult<CommonPage<IndexProductResponse>> getProductList(@PathVariable(value = "type") Integer type, PageParamRequest pageParamRequest) {

        return CommonResult.success(indexService.findIndexProductList(type, pageParamRequest));
    }

    /**
     * 热门搜索
     */
    @ApiOperation(value = "热门搜索")
    @RequestMapping(value = "/search/keyword", method = RequestMethod.GET)
    public CommonResult<List<HashMap<String, Object>>> hotKeywords() {
        return CommonResult.success(indexService.hotKeywords());
    }

    /**
     * 分享配置
     */
    @ApiOperation(value = "分享配置")
    @RequestMapping(value = "/share", method = RequestMethod.GET)
    public CommonResult<HashMap<String, String>> share() {
        return CommonResult.success(indexService.getShareConfig());
    }

    /**
     * 颜色配置
     */
    @ApiOperation(value = "颜色配置")
    @RequestMapping(value = "/index/color/config", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getColorConfig() {
        return CommonResult.success(indexService.getColorConfig());
    }

    /**
     * 版本信息
     */
    @ApiOperation(value = "获取版本信息")
    @RequestMapping(value = "/index/get/version", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getVersion() {
        return CommonResult.success(indexService.getVersion());
    }

    /**
     * 全局本地图片域名
     */
    @ApiOperation(value = "全局本地图片域名")
    @RequestMapping(value = "/image/domain", method = RequestMethod.GET)
    public CommonResult<String> getImageDomain() {
        return CommonResult.success(indexService.getImageDomain());
    }

    @ApiOperation(value = "获取版权信息")
    @RequestMapping(value = "/copyright/info", method = RequestMethod.GET)
    public CommonResult<CopyrightConfigInfoResponse> getCopyrightInfo() {
        return CommonResult.success(indexService.getCopyrightInfo());
    }

    @ApiOperation(value = "获取底部导航信息")
    @RequestMapping(value = "/get/bottom/navigation", method = RequestMethod.GET)
    public CommonResult<PageLayoutBottomNavigationResponse> getBottomNavigation() {
        return CommonResult.success(indexService.getBottomNavigationInfo());
    }

    @ApiOperation(value = "获取开屏广告信息")
    @RequestMapping(value = "/splash/ad/info", method = RequestMethod.GET)
    public CommonResult<SplashAdConfigVo> getSplashAdInfo() {
        return CommonResult.success(indexService.getSplashAdInfo());
    }
}



