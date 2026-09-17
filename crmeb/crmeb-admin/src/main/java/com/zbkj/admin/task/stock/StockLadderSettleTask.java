package com.zbkj.admin.task.stock;

import com.zbkj.common.model.stock.StockLadder;
import com.zbkj.service.service.StockRewardService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 阶梯业绩奖励定时结算任务
 * 每月 1 号凌晨 1 点触发，按「阶梯业绩结算周期」配置自动结算上一周期：
 *   1=月度：每月 1 号结算上个自然月
 *   2=季度：每季度首月（1/4/7/10 月）1 号结算上个季度
 *   3=年度：每年 1 月 1 号结算上个年度
 */
@Slf4j
@Component
public class StockLadderSettleTask {

    @Resource
    private StockRewardService stockRewardService;

    @Resource
    private StockService stockService;

    @Resource
    private SystemConfigService systemConfigService;

    /** 每月 1 号 01:00:00 触发 */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void settle() {
        try {
            if (!"1".equals(systemConfigService.getValueByKey("stock_ladder_status"))) {
                log.info("阶梯业绩奖励未开启，跳过定时结算");
                return;
            }
            String cycleStr = systemConfigService.getValueByKey("stock_ladder_cycle");
            int cycle;
            try {
                cycle = cycleStr == null || cycleStr.trim().isEmpty() ? 1 : Integer.parseInt(cycleStr.trim());
            } catch (NumberFormatException e) {
                cycle = 1;
            }
            List<StockLadder> ladders = stockService.getLadderList();
            if (ladders == null || ladders.isEmpty()) {
                log.info("未配置阶梯，跳过定时结算");
                return;
            }
            LocalDate now = LocalDate.now();
            String month;
            int type;
            if (cycle == 2) {
                // 季度：仅 1/4/7/10 月执行，结算上个季度（取上季度内任一月份）
                int m = now.getMonthValue();
                if (m != 1 && m != 4 && m != 7 && m != 10) {
                    return;
                }
                LocalDate prevQuarterMid = now.minusMonths(2); // 1月->去年11月(上季度内)，4月->2月，依此类推
                month = format(prevQuarterMid);
                type = 2;
            } else if (cycle == 3) {
                // 年度：仅 1 月执行，结算上个年度
                if (now.getMonthValue() != 1) {
                    return;
                }
                month = (now.getYear() - 1) + "-06";
                type = 3;
            } else {
                // 月度：结算上个自然月
                month = format(now.minusMonths(1));
                type = 1;
            }
            log.info("阶梯业绩奖励定时结算开始 type={} month={}", type, month);
            Boolean ok = stockRewardService.settleLadderReward(type, month);
            log.info("阶梯业绩奖励定时结算完成 result={}", ok);
        } catch (Exception e) {
            log.error("阶梯业绩奖励定时结算异常", e);
        }
    }

    private String format(LocalDate d) {
        return DateTimeFormatter.ofPattern("yyyy-MM").format(d);
    }
}
