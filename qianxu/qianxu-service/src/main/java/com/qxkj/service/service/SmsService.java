package com.qxkj.service.service;

import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.common.request.PageParamRequest;

import java.math.BigDecimal;

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
public interface SmsService {

    /**
     * 修改签名
     */
    Boolean modifySign(SmsModifySignRequest request);

    /**
     * 短信模板
     */
    MyRecord temps(PageParamRequest pageParamRequest);

    /**
     * 申请模板消息
     */
    Boolean applyTempMessage(SmsApplyTempRequest request);

    /**
     * 模板申请记录
     *
     * @param type (1=验证码 2=通知 3=推广)
     */
    MyRecord applys(Integer type, PageParamRequest pageParamRequest);

    /**
     * 发送公共验证码
     *
     * @param phone 手机号
     */
    Boolean sendCommonCode(String phone);

    /**
     * 发送支付成功短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param payPrice 支付金额
     * @param msgTempId 短信模板id
     * @return Boolean
     */
    Boolean sendPaySuccess(String phone, String orderNo, BigDecimal payPrice, Integer msgTempId);

    /**
     * 发送管理员下单短信提醒短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param realName 管理员名称
     * @param msgTempId 短信模板id
     */
    Boolean sendCreateOrderNotice(String phone, String orderNo, String realName, Integer msgTempId);

    /**
     * 发送订单支付成功管理员提醒短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param realName 管理员名称
     * @param msgTempId 短信模板id
     */
    Boolean sendOrderPaySuccessNotice(String phone, String orderNo, String realName, Integer msgTempId);

    /**
     * 发送用户退款管理员提醒短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param realName 管理员名称
     * @param msgTempId 短信模板id
     */
    Boolean sendOrderRefundApplyNotice(String phone, String orderNo, String realName, Integer msgTempId);

    /**
     * 发送用户确认收货管理员提醒短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param realName 管理员名称
     * @param msgTempId 短信模板id
     */
    Boolean sendOrderReceiptNotice(String phone, String orderNo, String realName, Integer msgTempId);

    /**
     * 发送订单改价提醒短信
     * @param phone 手机号
     * @param orderNo 订单编号
     * @param price 修改后的支付金额
     * @param msgTempId 短信模板id
     */
    Boolean sendOrderEditPriceNotice(String phone, String orderNo, BigDecimal price, Integer msgTempId);

    /**
     * 发送订单发货提醒短信
     * @param phone 手机号
     * @param nickName 用户昵称
     * @param storeName 商品名称
     * @param orderNo 订单编号
     * @param msgTempId 短信模板id
     */
    Boolean sendOrderDeliverNotice(String phone, String nickName, String storeName, String orderNo, Integer msgTempId);
}
