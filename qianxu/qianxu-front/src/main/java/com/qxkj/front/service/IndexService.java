package com.qxkj.front.service;

import com.qxkj.common.model.system.SystemConfig;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.CopyrightConfigInfoResponse;
import com.qxkj.common.response.IndexInfoResponse;
import com.qxkj.common.response.IndexProductResponse;
import com.qxkj.common.response.pagelayout.PageLayoutBottomNavigationResponse;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.common.vo.SplashAdConfigVo;

import java.util.HashMap;
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
public interface IndexService{

    /**
     * 首页信息
     * @return IndexInfoResponse
     */
    IndexInfoResponse getIndexInfo();

    /**
     * 热门搜索
     * @return List
     */
    List<HashMap<String, Object>> hotKeywords();

    /**
     * 分享配置信息
     */
    HashMap<String, String> getShareConfig();

    /**
     * 获取首页商品列表
     * @param type 类型 【1 精品推荐 2 热门榜单 3首发新品 4促销单品】
     * @param pageParamRequest 分页参数
     * @return List
     */
    CommonPage<IndexProductResponse> findIndexProductList(Integer type, PageParamRequest pageParamRequest);

    /**
     * 获取颜色配置
     * @return SystemConfig
     */
    SystemConfig getColorConfig();

    /**
     * 获取版本信息
     * @return MyRecord
     */
    MyRecord getVersion();

    /**
     * 获取全局本地图片域名
     * @return String
     */
    String getImageDomain();

    /**
     * 获取公司版权图片
     */
    CopyrightConfigInfoResponse getCopyrightInfo();

    /**
     * 获取底部导航信息
     */
    PageLayoutBottomNavigationResponse getBottomNavigationInfo();

    /**
     * 获取开屏广告信息
     */
    SplashAdConfigVo getSplashAdInfo();

}
