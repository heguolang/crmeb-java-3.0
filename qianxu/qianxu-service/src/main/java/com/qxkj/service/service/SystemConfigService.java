package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemConfig;
import com.qxkj.common.request.SaveConfigRequest;
import com.qxkj.common.request.SystemFormCheckRequest;
import com.qxkj.common.response.AdminSiteLogoResponse;
import com.qxkj.common.vo.ExpressSheetVo;
import com.qxkj.common.vo.MyRecord;

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
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 通过key数组获取Record对象
     * @param keyList key列表
     * @return MyRecord
     */
    MyRecord getValuesByKeyList(List<String> keyList);

    /**
     * 根据menu name 获取 value
     * @param key menu name
     * @return String
     */
    String getValueByKey(String key);

    /**
     * 保存或更新配置数据
     * @param name 菜单名称
     * @param value 菜单值
     * @return Boolean
     */
    Boolean updateOrSaveValueByName(String name, String value);

    /**
     * 根据 name 获取 value 找不到抛异常
     * @param key menu name
     * @return String
     */
    String getValueByKeyException(String key);

    /**
     * 整体保存表单数据
     * @param systemFormCheckRequest SystemFormCheckRequest 数据保存
     * @return Boolean
     */
    Boolean saveForm(SystemFormCheckRequest systemFormCheckRequest);

    /**
     * 根据formId查询数据
     * @param formId Integer id
     * @return HashMap<String, String>
     */
    HashMap<String, String> info(Integer formId);

    /**
     * 获取面单默认配置信息
     * @return ExpressSheetVo
     */
    ExpressSheetVo getDeliveryInfo();

    /**
     * 获取文件存储类型
     */
    SystemConfig getFileUploadType();

    /**
     * 获取管理端logo
     *
     * @return AdminSiteLogoResponse
     */
    AdminSiteLogoResponse getSiteLogo();

    /**
     * 获取腾讯地图key
     */
    SystemConfig getTxMapKey();

    /**
     * 获取移动端首页列表样式
     */
    SystemConfig getHomePageSaleListStyle();

    /**
     * 获取小程序下载地址
     */
    SystemConfig getMiniDownloadUrl();

    /**
     * 保存移动端首页列表样式
     */
    Boolean saveHomePageSaleListStyle(SaveConfigRequest request);

    /**
     * 清除config缓存
     */
    Boolean clearCache();

    /**
     * 获取授权地址
     */
    SystemConfig getAuthHost();

    /**
     * 获取主题色
     */
    SystemConfig getChangeColor();

    /**
     * 保存主题色
     */
    Boolean saveChangeColor(SaveConfigRequest request);

    /**
     * 获取各种文字协议
     * @return String
     */
    String getAgreementByKey(String agreementName);

    /**
     * 获取移动端域名
     * @return 移动端域名
     */
    String getFrontDomain();

    /**
     * 获取素材域名
     *
     * @return 素材域名
     */
    String getMediaDomain();

}
