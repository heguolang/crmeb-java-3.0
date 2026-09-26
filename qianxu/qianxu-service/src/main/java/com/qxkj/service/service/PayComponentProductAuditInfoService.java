package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentProductAuditInfo;

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
public interface PayComponentProductAuditInfoService extends IService<PayComponentProductAuditInfo> {

    /**
     * 获取最后一条商品审核信息
     * @param productId 商品id
     * @param auditId 审核单id
     * @return PayComponentProductAuditInfo
     */
    PayComponentProductAuditInfo getByProductIdAndAuditId(Integer productId, String auditId);
}
