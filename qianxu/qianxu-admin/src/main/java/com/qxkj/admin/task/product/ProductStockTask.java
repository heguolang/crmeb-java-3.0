package com.qxkj.admin.task.product;

import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.StoreBargainService;
import com.qxkj.service.service.StoreCombinationService;
import com.qxkj.service.service.StoreProductService;
import com.qxkj.service.service.StoreSeckillService;
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

@Component("ProductStockTask")
public class ProductStockTask {
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ProductStockTask.class);

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreSeckillService storeSeckillService;

    @Autowired
    private StoreBargainService storeBargainService;

    @Autowired
    private StoreCombinationService storeCombinationService;

    /**
     * 1分钟同步一次数据
     */
    public void stockOperation() {
        // cron : 0 */1 * * * ?
        logger.info("---OrderTakeByUser task------produce Data with fixed rate task: Execution Time - {}", QianxuDateUtil.nowDateTime());
        try {
            storeProductService.consumeProductStock(); // 商品本身库存任务
            storeSeckillService.consumeProductStock(); // 秒杀本身库存任务
            storeBargainService.consumeProductStock(); // 砍价本身库存任务
            storeCombinationService.consumeProductStock(); // 拼团本身库存任务
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("OrderTakeByUser.task" + " | msg : " + e.getMessage());
        }
    }

}
