package com.qxkj.service.service.impl;

import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.service.service.AsyncService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserBrokerageRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
@Service
public class AsyncServiceImpl implements AsyncService {

    private final Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);

    @Lazy
    @Autowired
    private SystemConfigService systemConfigService;
    @Lazy
    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;



    /**
     * 佣金冻结
     *
     * @param orderId 订单ID
     * @param nodeType 节点类型
     */
    @Async
    @Override
    public void brokerageFreezeByNode(String orderId, String nodeType) {
        String node = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_SHARE_NODE);
        String freezeDay = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);
        if (node.equals(nodeType)) {
            logger.info("佣金冻结节点触发");
            userBrokerageRecordService.brokerageFrozen(orderId, Integer.parseInt(freezeDay));
        }
    }
}
