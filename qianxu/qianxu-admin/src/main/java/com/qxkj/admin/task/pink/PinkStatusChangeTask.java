package com.qxkj.admin.task.pink;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.StorePinkService;
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
@Component("PinkStatusChangeTask")
public class PinkStatusChangeTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(PinkStatusChangeTask.class);

    @Autowired
    private StorePinkService storePinkService;

    /**
     * 每分钟执行一次
     */
    public void pinkStatusChage() {
        // cron : 0 */1 * * * ?
        logger.info("---PinkStatusChange------bargain stop status change task: Execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            storePinkService.detectionStatus();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("PinkStatusChange" + " | msg : " + e.getMessage());
        }
    }
}
