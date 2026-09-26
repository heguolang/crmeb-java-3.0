package com.qxkj.service.service;


import com.qxkj.common.vo.DeliveryCompanyVo;
import com.qxkj.common.vo.DeliverySendVo;
import com.qxkj.common.vo.ShopOrderCommonVo;

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
public interface WechatVideoDeliveryService {

    /**
     * 获取快递公司列表
     * @return List<DeliveryCompanyVo>
     */
    List<DeliveryCompanyVo> shopDeliveryGetCompanyList();

    /**
     * 订单发货
     * @return Boolean
     */
    Boolean shopDeliverySend(DeliverySendVo deliverySendVo);

    /**
     * 订单确认收货
     * 把订单状态从30（待收货）流转到100（完成）
     * @return Boolean
     */
    Boolean shopDeliveryRecieve(ShopOrderCommonVo shopOrderCommonVo);
}
