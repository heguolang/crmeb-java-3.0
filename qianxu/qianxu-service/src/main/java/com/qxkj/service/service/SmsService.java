package com.qxkj.service.service;

import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsChannelConfigRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.vo.MyRecord;

import java.math.BigDecimal;

/**
 * 短信服务（阿里云）
 */
public interface SmsService {

    /**
     * 修改签名（本地配置写入）
     */
    Boolean modifySign(SmsModifySignRequest request);

    /**
     * 短信模板列表（本地 eb_sms_template）
     */
    MyRecord temps(PageParamRequest pageParamRequest);

    /**
     * 申请模板消息（已切换阿里云，控制台申请）
     */
    Boolean applyTempMessage(SmsApplyTempRequest request);

    /**
     * 模板申请记录
     */
    MyRecord applys(Integer type, PageParamRequest pageParamRequest);

    /**
     * 发送公共验证码
     */
    Boolean sendCommonCode(String phone);

    /**
     * 发送支付成功短信
     *
     * @param templateCode 阿里云模板 CODE
     */
    Boolean sendPaySuccess(String phone, String orderNo, BigDecimal payPrice, String templateCode);

    /**
     * 发送管理员下单短信提醒
     */
    Boolean sendCreateOrderNotice(String phone, String orderNo, String realName, String templateCode);

    /**
     * 发送订单支付成功管理员提醒短信
     */
    Boolean sendOrderPaySuccessNotice(String phone, String orderNo, String realName, String templateCode);

    /**
     * 发送用户退款管理员提醒短信
     */
    Boolean sendOrderRefundApplyNotice(String phone, String orderNo, String realName, String templateCode);

    /**
     * 发送用户确认收货管理员提醒短信
     */
    Boolean sendOrderReceiptNotice(String phone, String orderNo, String realName, String templateCode);

    /**
     * 发送订单改价提醒短信
     */
    Boolean sendOrderEditPriceNotice(String phone, String orderNo, BigDecimal price, String templateCode);

    /**
     * 发送订单发货提醒短信
     */
    Boolean sendOrderDeliverNotice(String phone, String nickName, String storeName, String orderNo, String templateCode);

    /**
     * 按自由内容发送短信（滨礼通道，多号码逗号分割）
     *
     * @return 批次 taskId
     */
    String sendContent(String phones, String content);

    /**
     * 查询滨礼账户余额
     */
    MyRecord balance();

    /**
     * 读取短信通道配置（账号、密码掩码、mock 开关、通道选择）
     */
    MyRecord getChannelConfig();

    /**
     * 保存短信通道配置（账号、接口密码、mock 开关、通道选择）
     */
    Boolean saveChannelConfig(SmsChannelConfigRequest request);
}
