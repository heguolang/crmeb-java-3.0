package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.product.StoreProductDescription;
import com.qxkj.service.dao.StoreProductDescriptionDao;
import com.qxkj.service.service.StoreProductDescriptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
@Service
public class StoreProductDescriptionServiceImpl extends ServiceImpl<StoreProductDescriptionDao, StoreProductDescription> implements StoreProductDescriptionService {

    @Resource
    private StoreProductDescriptionDao dao;

    /**
     * 根据商品id和type删除对应描述
     * @param productId 商品id
     * @param type      类型
     */
    @Override
    public void deleteByProductId(int productId,int type) {
        LambdaQueryWrapper<StoreProductDescription> lmq = Wrappers.lambdaQuery();
        lmq.eq(StoreProductDescription::getProductId, productId).eq(StoreProductDescription::getType,type);
        dao.delete(lmq);
    }

    /**
     * 获取详情
     * @param productId 商品id
     * @param type 商品类型
     * @return StoreProductDescription
     */
    @Override
    public StoreProductDescription getByProductIdAndType(Integer productId, Integer type) {
        LambdaQueryWrapper<StoreProductDescription> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductDescription::getProductId, productId);
        lqw.eq(StoreProductDescription::getType,type);
        return dao.selectOne(lqw);
    }
}

