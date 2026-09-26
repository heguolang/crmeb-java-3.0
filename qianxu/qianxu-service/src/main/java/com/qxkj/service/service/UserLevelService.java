package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.system.SystemUserLevel;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserLevel;

import java.math.BigDecimal;
import java.util.List;

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
public interface UserLevelService extends IService<UserLevel> {

    /**
     * 用户等级列表
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserLevel> getList(PageParamRequest pageParamRequest);

    /**
     * 经验升级
     * @param user 用户
     * @return Boolean
     */
    Boolean upLevel(User user);

    /**
     * 经验降级
     * @param user 用户
     * @return Boolean
     */
    Boolean downLevel(User user);

    /**
     * 删除（通过系统等级id）
     * @param levelId 系统等级id
     * @return Boolean
     */
    Boolean deleteByLevelId(Integer levelId);

    /**
     * 根据用户当前数据匹配应达到的会员等级
     * @param user 用户
     * @return 匹配的等级配置
     */
    SystemUserLevel resolveMatchedLevel(User user);

    /**
     * 获取用户当前等级每单赠送积分
     * @param user 用户
     * @return 赠送积分数
     */
    Integer getGiveIntegral(User user);

    /**
     * 获取用户完成本单支付后等级每单赠送积分
     * @param user 用户
     * @param payAmount 本单支付金额
     * @return 赠送积分数
     */
    Integer getProjectedGiveIntegral(User user, BigDecimal payAmount);

    /**
     * 订单交易完成时按配置累计消费金额/订单数并触发升级
     * @param storeOrder 订单
     * @return Boolean
     */
    Boolean processLevelOnOrderComplete(StoreOrder storeOrder);

}
