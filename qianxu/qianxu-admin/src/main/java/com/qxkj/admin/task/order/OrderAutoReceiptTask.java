package com.qxkj.admin.task.order;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.OrderTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component("OrderAutoReceiptTask")
public class OrderAutoReceiptTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAutoReceiptTask.class);

    @Autowired
    private OrderTaskService orderTaskService;

    public void autoTakeDelivery() {
        // cron : 0 0 0 */1 * ?
        LOGGER.info("---OrderAutoReceiptTask task------produce Data with fixed rate task: Execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            orderTaskService.autoTakeDelivery();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("OrderAutoReceiptTask.exception" + " | msg : " + e.getMessage());
        }
    }

}
