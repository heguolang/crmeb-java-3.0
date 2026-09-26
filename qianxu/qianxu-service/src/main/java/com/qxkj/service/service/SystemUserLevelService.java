package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemUserLevel;
import com.qxkj.common.request.SystemUserLevelRequest;
import com.qxkj.common.request.SystemUserLevelUpdateShowRequest;
import com.qxkj.common.response.SystemUserLevelInfoResponse;

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
public interface SystemUserLevelService extends IService<SystemUserLevel> {

    /**
     * 获取等级列表
     */
    List<SystemUserLevel> getList();

    /**
     * 获取全部等级（不分页）
     */
    List<SystemUserLevel> getAllList();

    /**
     * 获取等级详情
     * @param id 等级id
     */
    SystemUserLevelInfoResponse getInfo(Integer id);

    /**
     * 系统等级新增
     * @param request request
     * @return Boolean
     */
    Boolean create(SystemUserLevelRequest request);

    /**
     * 系统等级更新
     * @param id    等级id
     * @param request   等级数据
     * @return Boolean
     */
    Boolean update(Integer id, SystemUserLevelRequest request);

    SystemUserLevel getByLevelId(Integer levelId);

    /**
     * 获取系统等级列表（移动端）
     */
    List<SystemUserLevel> getH5LevelList();

    /**
     * 删除系统等级
     * @param id 等级id
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 使用/禁用
     * @param request request
     */
    Boolean updateShow(SystemUserLevelUpdateShowRequest request);

    /**
     * 获取可用等级列表
     * @return List
     */
    List<SystemUserLevel> getUsableList();

    /**
     * 是否存在「已付款」统计消费金额的启用等级
     */
    Boolean hasConsumptionTriggerOnPaid();

    /**
     * 是否存在「交易完成」统计消费金额的启用等级
     */
    Boolean hasConsumptionTriggerOnComplete();

    /**
     * 是否存在「已付款」统计订单数的启用等级
     */
    Boolean hasOrderCountTriggerOnPaid();

    /**
     * 是否存在「交易完成」统计订单数的启用等级
     */
    Boolean hasOrderCountTriggerOnComplete();

}
