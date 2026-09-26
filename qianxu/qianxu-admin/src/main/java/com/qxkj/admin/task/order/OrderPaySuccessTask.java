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
@Component("OrderPaySuccessTask")
public class OrderPaySuccessTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(OrderPaySuccessTask.class);

    @Autowired
    private OrderTaskService orderTaskService;

    /**
     * 1分钟同步一次数据
     */
    public void orderPayAfter() {
        // cron : 0 */1 * * * ?
        logger.info("---OrderPaySuccessTask task------produce Data with fixed rate task: Execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            orderTaskService.orderPaySuccessAfter();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("OrderPaySuccessTask.task" + " | msg : " + e.getMessage());
        }
    }

}
