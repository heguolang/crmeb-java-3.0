package com.qxkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.response.pagelayout.PageLayoutBottomNavigationResponse;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.common.vo.SplashAdConfigVo;

import java.util.HashMap;

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
public interface PageLayoutService {

    /**
     * 页面首页
     * @return 首页信息
     */
    HashMap<String, Object> index();

    /**
     * 首页保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean save(JSONObject jsonObject);

    /**
     * 页面首页banner保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean indexBannerSave(JSONObject jsonObject);

    /**
     * 页面首页menu保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean indexMenuSave(JSONObject jsonObject);

    /**
     * 页面首页新闻保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean indexNewsSave(JSONObject jsonObject);

    /**
     * 页面用户中心banner保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean userBannerSave(JSONObject jsonObject);

    /**
     * 页面用户中心导航保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean userMenuSave(JSONObject jsonObject);

    /**
     * 页面用户中心商品table保存
     * @param jsonObject 数据
     * @return Boolean
     */
    Boolean indexTableSave(JSONObject jsonObject);

    /**
     * 获取分类页配置
     * @return MyRecord
     */
    MyRecord getCategoryConfig();

    /**
     * 分类页配置保存
     * @param jsonObject 配置数据
     * @return Boolean
     */
    Boolean categoryConfigSave(JSONObject jsonObject);

    /**
     * 获取页面底部导航信息
     */
    PageLayoutBottomNavigationResponse getBottomNavigation();

    /**
     * 页面底部导航信息保存
     * @return 保存结果
     */
    Boolean bottomNavigationSave(JSONObject jsonObject);

    /**
     * 获取开屏广告配置
     */
    SplashAdConfigVo getSplashAdConfig();

    /**
     * 编辑开屏广告配置
     */
    Boolean splashAdConfigSave(SplashAdConfigVo configVo);
}
