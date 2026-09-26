package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.ComponentProductSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PayComponentProductAddRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.wechat.video.PayComponentDraftProduct;

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
public interface PayComponentDraftProductService extends IService<PayComponentDraftProduct> {

    /**
     * 添加商品
     * @param addRequest 商品请求参数
     * @return Boolean
     */
    Boolean add(PayComponentProductAddRequest addRequest);

    /**
     * 根据商品id获取草稿商品
     * @param proId 商品id
     * @return PayComponentDraftProduct
     */
    PayComponentDraftProduct getByProId(Integer proId);

    /**
     * 通过商品id删除草稿
     * @param proId 商品id
     */
    Boolean deleteByProId(Integer proId);

    /**
     * 管理端草稿商品列表
     * @param request 搜索参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<PayComponentDraftProduct> getAdminList(ComponentProductSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 商品详情
     * @param id 商品id
     * @return
     */
    PayComponentDraftProduct getInfo(Integer id);
}
