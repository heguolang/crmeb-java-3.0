package com.qxkj.service.service;


import com.qxkj.common.vo.ShopAftersaleAddVo;
import com.qxkj.common.vo.ShopAftersaleUpdateVo;
import com.qxkj.common.vo.ShopAftersaleVo;
import com.qxkj.common.vo.ShopOrderCommonVo;

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
public interface WechatVideoAftersaleService {

    /**
     * 创建售后
     * @return Boolean
     */
    Boolean shopAftersaleAdd(ShopAftersaleAddVo shopAftersaleAddVo);

    /**
     * 获取售后
     * @return ShopAftersaleVo
     */
    ShopAftersaleVo shopAftersaleGet(ShopOrderCommonVo shopOrderCommonVo);

    /**
     * 更新售后
     * @return Boolean
     */
    Boolean shopAftersaleUpdate(ShopAftersaleUpdateVo shopAftersaleUpdateVo);
}
