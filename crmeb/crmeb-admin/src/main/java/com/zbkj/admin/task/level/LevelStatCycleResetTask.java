package com.zbkj.admin.task.level;

import com.zbkj.service.dao.UserDistributorLevelStatDao;
import com.zbkj.service.dao.UserTeamLevelStatDao;
import com.zbkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 等级统计周期清零任务
 *
 * <p>默认关闭：只有把配置项 team_level_cycle_reset / distributor_level_cycle_reset 置为 "1" 才生效。
 * 每月 1 号 02:00 运行一次，把统计金额归零，重新累计下一周期业绩。
 * 注意：清零只影响统计数值，不自动降低用户已获得的等级（与「只升不降」策略一致）。
 */
@Slf4j
@Component("LevelStatCycleResetTask")
public class LevelStatCycleResetTask {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UserTeamLevelStatDao userTeamLevelStatDao;

    @Autowired
    private UserDistributorLevelStatDao userDistributorLevelStatDao;

    /**
     * 周期清零，cron：每月 1 号 02:00
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void reset() {
        try {
            if ("1".equals(systemConfigService.getValueByKey("team_level_cycle_reset"))) {
                int rows = userTeamLevelStatDao.resetAllAmount();
                log.info("团队等级统计周期清零完成，影响行数：{}", rows);
            }
            if ("1".equals(systemConfigService.getValueByKey("distributor_level_cycle_reset"))) {
                int rows = userDistributorLevelStatDao.resetAllAmount();
                log.info("分销商等级统计周期清零完成，影响行数：{}", rows);
            }
        } catch (Exception e) {
            log.error("等级统计周期清零失败", e);
        }
    }
}
