package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.product.StoreProductCoupon;

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
public interface StoreProductCouponService extends IService<StoreProductCoupon> {
    /**
     * 根据产品id删除 优惠券关联信息
     * @param productId 产品id
     */
    boolean deleteByProductId(Integer productId);

    /**
     * 根据商品id获取已关联优惠券信息
     * @param productId 商品id
     * @return 已关联优惠券
     */
    List<StoreProductCoupon> getListByProductId(Integer productId);
}
