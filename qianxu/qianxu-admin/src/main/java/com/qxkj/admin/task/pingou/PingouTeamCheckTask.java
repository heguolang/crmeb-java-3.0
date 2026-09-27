package com.qxkj.admin.task.pingou;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.PingouService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 拼购团开奖兜底任务：
 * 每分钟扫描满员未开奖的团执行开奖，并补发未发放的未中奖奖励。
 */
@Component("PingouTeamCheckTask")
public class PingouTeamCheckTask {

    private static final Logger logger = LoggerFactory.getLogger(PingouTeamCheckTask.class);

    @Autowired
    private PingouService pingouService;

    /**
     * 每分钟执行一次
     */
    public void pingouTeamCheck() {
        logger.info("---PingouTeamCheckTask------execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            pingouService.checkAndDrawTeams();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("PingouTeamCheckTask | msg : " + e.getMessage());
        }
    }
}
