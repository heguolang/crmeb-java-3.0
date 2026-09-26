package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.wechat.video.PayComponentOrderProduct;
import com.qxkj.service.dao.PayComponentOrderProductDao;
import com.qxkj.service.service.PayComponentOrderProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@Service
public class PayComponentOrderProductServiceImpl extends ServiceImpl<PayComponentOrderProductDao, PayComponentOrderProduct> implements PayComponentOrderProductService {

    @Resource
    private PayComponentOrderProductDao dao;

    /**
     * 获取订单商品列表
     * @param orderNo 订单编号
     * @return List
     */
    @Override
    public List<PayComponentOrderProduct> getListByOrderNo(String orderNo) {
        LambdaQueryWrapper<PayComponentOrderProduct> lqw = Wrappers.lambdaQuery();
        lqw.eq(PayComponentOrderProduct::getOrderNo, orderNo);
        return dao.selectList(lqw);
    }
}

