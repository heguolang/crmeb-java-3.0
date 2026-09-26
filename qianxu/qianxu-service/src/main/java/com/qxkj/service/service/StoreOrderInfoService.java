package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.order.StoreOrderInfo;
import com.qxkj.common.vo.StoreOrderInfoOldVo;
import com.qxkj.common.vo.StoreOrderInfoVo;

import java.math.BigDecimal;
import java.util.HashMap;
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
public interface StoreOrderInfoService extends IService<StoreOrderInfo> {

    HashMap<Integer, List<StoreOrderInfoOldVo>> getMapInId(List<Integer> orderIdList);

    List<StoreOrderInfoOldVo> getOrderListByOrderId(Integer orderId);

    /**
     * 批量添加订单详情
     * @param storeOrderInfos 订单详情集合
     * @return 保存结果
     */
    boolean saveOrderInfos(List<StoreOrderInfo> storeOrderInfos);

    /**
     * 通过订单编号和规格号查询
     * @param uni 规格号
     * @param orderId 订单编号
     * @return StoreOrderInfo
     */
    StoreOrderInfo getByUniAndOrderId(String uni, Integer orderId);

    /**
     * 获取订单详情vo列表
     * @param orderId 订单id
     * @return List<StoreOrderInfoVo>
     */
    List<StoreOrderInfoVo> getVoListByOrderId(Integer orderId);

    /**
     * 获取订单详情-订单编号
     * @param orderNo 订单编号
     * @return List
     */
    List<StoreOrderInfo> getListByOrderNo(String orderNo);

    /**
     * 根据时间、商品id获取销售件数
     * @param date 时间，格式'yyyy-MM-dd'
     * @param proId 商品id
     * @return Integer
     */
    Integer getSalesNumByDateAndProductId(String date, Integer proId);

    /**
     * 根据时间、商品id获取销售额
     * @param date 时间，格式'yyyy-MM-dd'
     * @param proId 商品id
     * @return BigDecimal
     */
    BigDecimal getSalesByDateAndProductId(String date, Integer proId);

    /**
     * 根据订单id、商品id、商品唯一id更新回复状态
     *
     * @param orderId  订单id
     * @param productId  商品id
     * @param unique  商品唯一id
     * @return
     */
    void updateReply(Integer orderId, Integer productId, String unique);
}
