package com.qxkj.admin.task.bargain;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.StoreBargainService;
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
@Component("BargainStopChangeTask")
public class BargainStopChangeTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(BargainStopChangeTask.class);

    @Autowired
    private StoreBargainService storeBargainService;

    /**
     * 每天0点执行
     */
    public void bargainStopChange() {
        // cron : 0 0 0 */1 * ?
        logger.info("---BargainStopChangeTask------bargain stop status change task: Execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            storeBargainService.stopAfterChange();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("BargainStopChangeTask" + " | msg : " + e.getMessage());
        }
    }

}
