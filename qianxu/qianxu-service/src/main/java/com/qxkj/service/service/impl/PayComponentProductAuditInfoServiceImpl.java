package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.wechat.video.PayComponentProductAuditInfo;
import com.qxkj.service.dao.PayComponentProductAuditInfoDao;
import com.qxkj.service.service.PayComponentProductAuditInfoService;
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
public class PayComponentProductAuditInfoServiceImpl extends ServiceImpl<PayComponentProductAuditInfoDao, PayComponentProductAuditInfo> implements PayComponentProductAuditInfoService {

    @Resource
    private PayComponentProductAuditInfoDao dao;

    /**
     * 获取最后一条商品审核信息
     * @param productId 商品id
     * @param auditId 审核单id
     * @return PayComponentProductAuditInfo
     */
    @Override
    public PayComponentProductAuditInfo getByProductIdAndAuditId(Integer productId, String auditId) {
        LambdaQueryWrapper<PayComponentProductAuditInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(PayComponentProductAuditInfo::getProductId, productId);
        lqw.eq(PayComponentProductAuditInfo::getAuditId, auditId);
        return dao.selectOne(lqw);
    }
}

