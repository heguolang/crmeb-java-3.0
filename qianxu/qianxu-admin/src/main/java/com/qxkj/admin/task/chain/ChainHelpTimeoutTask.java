package com.qxkj.admin.task.chain;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 链动售后保护期到期结算（需在定时任务中配置 bean: ChainHelpTimeoutTask，方法 releaseTimeout）。
 * 支付时若配置了售后保护期，订单只标记为链动礼包单；保护期满后由本任务补偿结算（升身份 + 分润 + 帮扶推进）。
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
            int n = chainService.processAfterSaleOrders();
            logger.info("ChainHelpTimeoutTask afterSaleSettled={}", n);
        } catch (Exception e) {
            logger.error("ChainHelpTimeoutTask error: {}", e.getMessage(), e);
        }
    }
}
