package com.qxkj.service.service;

import com.qxkj.common.model.order.StoreOrder;

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
public interface AsyncService {


    /**
     * 佣金冻结
     *
     * @param orderId 订单ID
     * @param nodeType 节点类型
     */
    void brokerageFreezeByNode(String orderId, String nodeType);
}
