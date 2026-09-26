package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentProductSku;

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
public interface PayComponentProductSkuService extends IService<PayComponentProductSku> {

    /**
     * 通过商品id删除数据
     * @param proId 商品id
     * @return Boolean
     */
    Boolean deleteByProId(Integer proId);

    /**
     * 通过商品id获取列表
     * @param proId 商品id
     * @return List
     */
    List<PayComponentProductSku> getListByProId(Integer proId);

    /**
     * 通过商品id，规格属性id获取对象
     * @param proId 商品id
     * @param attrValueId 规格属性id
     * @return PayComponentProductSku
     */
    PayComponentProductSku getByProIdAndAttrValueId(Integer proId, Integer attrValueId);

    /**
     * 添加/扣减库存
     * @param skuId skuId
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @return Boolean
     */
    Boolean operationStock(Integer skuId, Integer num, String operationType, Integer version);
}
