package com.zbkj.admin.task.stock;

import com.zbkj.service.service.StockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 订货单定时任务
 * 每 10 分钟：
 *   1. 直接上级已补货的等待匹配(状态10)订单：立即释放回正常订货流程（待上级审核/待发货/完成）
 *   2. 等待匹配(状态10)且超时的订单：自动向上匹配有货上级（兜底懒触发）
 *   3. 待付款(状态1)超时订单：自动取消(-2)，时长取配置 stock_wait_pay_hours（默认24小时）
 */
@Slf4j
@Component
public class StockUpSearchTask {

    @Resource
    private StockOrderService stockOrderService;

    @Scheduled(initialDelay = 60_000L, fixedDelay = 600_000L)
    public void run() {
        try {
            // 先释放"上级已补货"的订单（优先原地恢复，避免被超时逻辑误向上改挂）
            stockOrderService.releaseRestockedWaitMatchOrders();
        } catch (Exception e) {
            log.error("订货单补货后释放匹配订单定时任务异常", e);
        }
        try {
            stockOrderService.processExpiredUpSearchOrders();
        } catch (Exception e) {
            log.error("订货单等待匹配定时任务异常", e);
        }
        try {
            stockOrderService.cancelExpiredUnpaidOrders();
        } catch (Exception e) {
            log.error("订货单待付款超时取消定时任务异常", e);
        }
    }
}
