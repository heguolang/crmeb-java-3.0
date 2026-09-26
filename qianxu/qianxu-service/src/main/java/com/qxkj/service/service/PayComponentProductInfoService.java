package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentProductInfo;

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
public interface PayComponentProductInfoService extends IService<PayComponentProductInfo> {

    /**
     * 获取商品详情
     * @param proId 商品id
     * @return PayComponentProductInfo
     */
    PayComponentProductInfo getByProId(Integer proId);

    /**
     * 删除通过商品id
     * @param proId 商品id
     * @return Boolean
     */
    Boolean deleteByProId(Integer proId);
}
