package com.qxkj.admin.task.chain;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 链动团队分红定时任务（每月1日）
 * bean: ChainDividendTask，方法: runMonthly
 */
@Component("ChainDividendTask")
public class ChainDividendTask {

    private static final Logger logger = LoggerFactory.getLogger(ChainDividendTask.class);

    @Autowired
    private ChainService chainService;

    public void runMonthly() {
        logger.info("---ChainDividendTask--- {}", QianxuDateUtil.nowDateTime());
        try {
            int n = chainService.runDividend(null);
            logger.info("ChainDividendTask issued={}", n);
        } catch (Exception e) {
            logger.error("ChainDividendTask error: {}", e.getMessage(), e);
        }
    }
}
