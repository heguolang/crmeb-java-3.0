package com.qxkj.admin.task.dividend;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.DividendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 全球分红周期结算检查任务（每分钟）
 * 命中周期结算时间点（日 00:01 / 周一 00:01 / 1 号 01:00）时结算上一周期
 */
@Component("DividendCheckTask")
public class DividendCheckTask {

    private static final Logger logger = LoggerFactory.getLogger(DividendCheckTask.class);

    @Autowired
    private DividendService dividendService;

    public void dividendCheck() {
        logger.info("---DividendCheckTask------execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            dividendService.dividendCheck();
        } catch (Exception e) {
            logger.error("DividendCheckTask | msg : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
