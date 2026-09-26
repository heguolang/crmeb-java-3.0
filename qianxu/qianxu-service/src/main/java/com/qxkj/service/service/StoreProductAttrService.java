package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.product.StoreProductAttr;

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
public interface StoreProductAttrService extends IService<StoreProductAttr> {

    /**
     * 根据基本属性查询商品属性详情
     * @param storeProductAttr 商品属性
     * @return 查询商品属性集合
     */
    List<StoreProductAttr> getByEntity(StoreProductAttr storeProductAttr);

    /**
     * 根据id删除商品
     * @param productId 待删除商品id
     * @param type 类型区分是是否添加营销
     */
    void removeByProductId(Integer productId,int type);

    /**
     * 删除商品规格
     * @param productId 商品id
     * @param type 商品类型
     * @return Boolean
     */
    Boolean deleteByProductIdAndType(Integer productId, Integer type);

    /**
     * 获取商品规格列表
     * @param productId 商品id
     * @param type 商品类型
     * @return List
     */
    List<StoreProductAttr> getListByProductIdAndType(Integer productId, Integer type);
}
