package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.vo.ShopBrandVo;
import com.qxkj.common.model.wechat.video.PayComponentBrand;
import com.qxkj.service.dao.PayComponentBrandDao;
import com.qxkj.service.service.PayComponentBrandService;
import com.qxkj.service.service.WechatVideoSpuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
public class PayComponentBrandServiceImpl extends ServiceImpl<PayComponentBrandDao, PayComponentBrand> implements PayComponentBrandService {

    private final Logger logger = LoggerFactory.getLogger(PayComponentBrandServiceImpl.class);

    @Resource
    private PayComponentBrandDao dao;

    @Autowired
    private WechatVideoSpuService wechatVideoSpuService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 更新数据
     */
    @Override
    public void updateData() {
        List<ShopBrandVo> shopBrandList = wechatVideoSpuService.getShopBrandList();
        if (CollUtil.isEmpty(shopBrandList)) {
            logger.info("微信未返回品牌信息");
            return ;
        }
        List<PayComponentBrand> brandList = shopBrandList.stream().map(e -> {
            PayComponentBrand brand = new PayComponentBrand();
            BeanUtils.copyProperties(e, brand);
            return brand;
        }).collect(Collectors.toList());

        Boolean execute = transactionTemplate.execute(e -> {
            saveBatch(brandList);
            return Boolean.TRUE;
        });
        if (!execute) {
            throw new QianxuException("更新自定义交易品牌列表,操作数据库时出错");
        }
        logger.info(StrUtil.format("自动更新自定义交易品牌列表成功，时间{}", DateUtil.now()));
    }
}

