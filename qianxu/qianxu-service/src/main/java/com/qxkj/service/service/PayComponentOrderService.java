package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentOrder;
import com.qxkj.common.vo.ShopOrderAddVo;

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
public interface PayComponentOrderService extends IService<PayComponentOrder> {

    /**
     * 创建组件订单
     * @param shopOrderAddVo 创建订单参数
     * @return ticket
     */
    String create(ShopOrderAddVo shopOrderAddVo);

    /**
     * 通过订单号获取订单
     * @param orderNo 订单编号
     * @return PayComponentOrder
     */
    PayComponentOrder getByOrderNo(String orderNo);

    /**
     * 创建售后
     * @param orderNo 订单编号
     */
    void createAfterSale(String orderNo);
}
