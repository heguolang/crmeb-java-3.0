package com.qxkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.request.YlyPrintRequest;
import com.qxkj.common.request.YlyPrintRequestGoods;
import com.qxkj.common.vo.StoreOrderInfoOldVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.qxkj.service.service.StoreOrderInfoService;
import com.qxkj.service.service.StoreOrderService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.YlyPrintService;
import com.qxkj.service.util.YlyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class YlyPrintServiceImpl implements YlyPrintService {
    private static final Logger logger = LoggerFactory.getLogger(YlyPrintServiceImpl.class);
    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private YlyUtil ylyUtil;
    /**
     * 易联云打印商品信息
     *
     * @param orderId 订单id
     * @param isAuto 是否自动打印
     */
    @Override
    public void YlyPrint(String orderId,boolean isAuto) {
        if(ylyUtil.checkYlyPrintStatus()){
            throw new QianxuException("易联云 未开启打印");
        }
        // 判断是否开启自动打印
        if(isAuto && ylyUtil.checkYlyPrintAfterPaySuccess()){
            return;
        }
        StoreOrder exitOrder = storeOrderService.getByOderId(orderId);
        if(ObjectUtil.isNull(exitOrder)){
            throw new QianxuException("易联云 打印时未找到 订单信息");
        }
        if(!exitOrder.getPaid()){
            throw new QianxuException("易联云 打印时出错， 订单未支付");
        }
        List<StoreOrderInfoOldVo> exitOrderInfo = storeOrderInfoService.getOrderListByOrderId(exitOrder.getId());
        List<YlyPrintRequestGoods> goods = new ArrayList<>();
        for (StoreOrderInfoOldVo storeOrderInfo : exitOrderInfo) {
            goods.add(new YlyPrintRequestGoods(storeOrderInfo.getInfo().getProductName()
                    ,storeOrderInfo.getInfo().getPrice().toString(),
                    storeOrderInfo.getInfo().getPayNum()+"",
                    storeOrderInfo.getInfo().getPrice().multiply(BigDecimal.valueOf(storeOrderInfo.getInfo().getPayNum())).setScale(2, RoundingMode.HALF_UP).toString()));
        }

        YlyPrintRequest ylyPrintRequest = new YlyPrintRequest();
        ylyPrintRequest.setBusinessName(systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME));
        ylyPrintRequest.setOrderNo(exitOrder.getOrderId());
        ylyPrintRequest.setDate(DateUtil.format(exitOrder.getPayTime(), Constants.DATE_FORMAT));
        ylyPrintRequest.setName(exitOrder.getRealName());
        ylyPrintRequest.setPhone(exitOrder.getUserPhone());
        ylyPrintRequest.setAddress(exitOrder.getUserAddress());
        ylyPrintRequest.setNote(exitOrder.getMark());

        ylyPrintRequest.setGoods(goods);
        ylyPrintRequest.setAmount(exitOrder.getProTotalPrice().toString());
        ylyPrintRequest.setDiscount(exitOrder.getDeductionPrice().toString());
        ylyPrintRequest.setPostal(exitOrder.getPayPostage().toString());
        ylyPrintRequest.setDeduction(exitOrder.getCouponPrice().toString());
        ylyPrintRequest.setPayMoney(exitOrder.getPayPrice().toString());

        try {
            ylyUtil.ylyPrint(ylyPrintRequest);
            logger.info("易联云打印小票成功" + JSONObject.toJSONString(ylyPrintRequest));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("易联云打印小票失败 " + e.getMessage());
        }
    }


}
