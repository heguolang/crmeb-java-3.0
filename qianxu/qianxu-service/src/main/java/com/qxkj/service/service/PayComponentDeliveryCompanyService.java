package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentDeliveryCompany;

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
public interface PayComponentDeliveryCompanyService extends IService<PayComponentDeliveryCompany> {

    /**
     * 更新物流公司数据
     */
    void updateData();

    /**
     * 获取组件物流公司列表
     * @return List
     */
    List<PayComponentDeliveryCompany> getList();

    /**
     * 通过快递公司ID获取
     * @param deliveryId 快递公司ID
     * @return PayComponentDeliveryCompany
     */
    PayComponentDeliveryCompany getByDeliveryId(String deliveryId);
}
