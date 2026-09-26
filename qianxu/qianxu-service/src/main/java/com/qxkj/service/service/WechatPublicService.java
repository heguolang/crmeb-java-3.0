package com.qxkj.service.service;

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
public interface WechatPublicService {

    /**
     * 获取公众号自定义菜单
     */
    Object getCustomizeMenus();

    /**
     * 保存自定义菜单
     * @param data 菜单json
     * @return Boolean
     */
    Boolean createMenus(String data);

    /**
     * 删除自定义菜单
     * @return Boolean
     */
    Boolean deleteMenus();
}
