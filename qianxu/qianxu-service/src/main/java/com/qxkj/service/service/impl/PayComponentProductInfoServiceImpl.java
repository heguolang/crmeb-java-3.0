package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.wechat.video.PayComponentProductInfo;
import com.qxkj.service.dao.PayComponentProductInfoDao;
import com.qxkj.service.service.PayComponentProductInfoService;
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
public class PayComponentProductInfoServiceImpl extends ServiceImpl<PayComponentProductInfoDao, PayComponentProductInfo> implements PayComponentProductInfoService {

    @Resource
    private PayComponentProductInfoDao dao;

    /**
     * 获取商品详情
     * @param proId 商品id
     * @return PayComponentProductInfo
     */
    @Override
    public PayComponentProductInfo getByProId(Integer proId) {
        LambdaQueryWrapper<PayComponentProductInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(PayComponentProductInfo::getProductId, proId);
        lqw.eq(PayComponentProductInfo::getIsDel, false);
        return dao.selectOne(lqw);
    }

    /**
     * 删除通过商品id
     * @param proId 商品id
     * @return Boolean
     */
    @Override
    public Boolean deleteByProId(Integer proId) {
        LambdaUpdateWrapper<PayComponentProductInfo> luw = Wrappers.lambdaUpdate();
        luw.set(PayComponentProductInfo::getIsDel, true);
        luw.eq(PayComponentProductInfo::getProductId, proId);
        luw.eq(PayComponentProductInfo::getIsDel, false);
        return update(luw);
    }
}

