package com.qxkj.service.dao;

import com.qxkj.common.model.finance.UserRecharge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.response.UserRechargeResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
public interface UserRechargeDao extends BaseMapper<UserRecharge> {

    /**
     * 根据类型获取该类型充值总金额
     * @param type  充值类型
     * @return      该类型充值总金额
     */
    BigDecimal getSumByType(String type);

    /**
     * 获取退款总金额
     * @return 退款总金额
     */
    BigDecimal getSumByRefund();

    List<UserRechargeResponse> getAdminPage(Map<String, Object> map);
}
