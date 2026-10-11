package com.qxkj.admin.task.chain;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 链动帮扶超时释放（需在定时任务中配置 bean: ChainHelpTimeoutTask，方法 releaseTimeout）
 * 建议 cron: 0 30 2 * * ?
 */
@Component("ChainHelpTimeoutTask")
public class ChainHelpTimeoutTask {

    private static final Logger logger = LoggerFactory.getLogger(ChainHelpTimeoutTask.class);

    @Autowired
    private ChainService chainService;

    public void releaseTimeout() {
        logger.info("---ChainHelpTimeoutTask--- {}", QianxuDateUtil.nowDateTime());
        try {
            int n = chainService.releaseTimeoutHelps();
            logger.info("ChainHelpTimeoutTask released={}", n);
        } catch (Exception e) {
            logger.error("ChainHelpTimeoutTask error: {}", e.getMessage(), e);
        }
    }
}
