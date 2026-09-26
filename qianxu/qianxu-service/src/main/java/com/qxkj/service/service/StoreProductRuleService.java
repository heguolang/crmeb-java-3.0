package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.product.StoreProductRule;
import com.qxkj.common.request.StoreProductRuleRequest;
import com.qxkj.common.request.StoreProductRuleSearchRequest;

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
public interface StoreProductRuleService extends IService<StoreProductRule> {

    /**
     * 商品规格列表
     * @param request 查询参数
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<StoreProductRule> getList(StoreProductRuleSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增商品规格
     * @param storeProductRuleRequest 规格参数
     * @return 新增结果
     */
    boolean save(StoreProductRuleRequest storeProductRuleRequest);

    /**
     * 修改规格
     * @param storeProductRuleRequest 规格参数
     * @return Boolean
     */
    Boolean updateRule(StoreProductRuleRequest storeProductRuleRequest);
}
