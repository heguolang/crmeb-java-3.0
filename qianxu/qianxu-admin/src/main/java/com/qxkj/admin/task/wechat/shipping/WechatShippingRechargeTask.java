package com.qxkj.admin.task.wechat.shipping;

import com.qxkj.service.service.WechatOrderShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 充值订单微信小程序发货管理定时任务
 *
 * @author Han
 * @version 1.0.0
 * @Date 2024/1/6
 */
@Component("WechatShippingRechargeTask")
public class WechatShippingRechargeTask {

    @Autowired
    private WechatOrderShippingService wechatOrderShippingService;

    /**
     * 每1小时执行一次
     * 处理超过支付10分钟的充值订单
     *
     */
    public void uploadRechargeOrderShipping() {
        // cron : 0 0 */1 * * ?
        wechatOrderShippingService.batchUploadRechargeOrderShipping();
    }

}
