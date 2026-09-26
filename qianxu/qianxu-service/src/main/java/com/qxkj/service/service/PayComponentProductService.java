package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.ComponentProductSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PayComponentProductAddRequest;
import com.qxkj.common.response.PayComponentProductResponse;
import com.qxkj.common.response.ProductDetailResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.wechat.video.PayComponentProduct;

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
public interface PayComponentProductService extends IService<PayComponentProduct> {

    /**
     * 添加商品
     * @param addRequest 商品请求参数
     * @return Boolean
     */
//    Boolean add(PayComponentProductAddRequest addRequest);

    /**
     * 删除商品
     * @param proId 商品id
     * @return Boolean
     */
    Boolean delete(Integer proId);

    /**
     * 更新商品
     * @param addRequest 商品请求参数
     * @return Boolean
     */
    Boolean update(PayComponentProductAddRequest addRequest);

    /**
     * 上架商品
     * @param proId 商品id
     * @return Boolean
     */
    Boolean listing(Integer proId);

    /**
     * 下架商品
     * @param proId 商品id
     * @return Boolean
     */
    Boolean delisting(Integer proId);

    /**
     * 获取H5商品详情（为兼容原格式，组装原来的数据格式）
     * @param id 商品id
     * @return ProductDetailResponse
     */
    ProductDetailResponse getH5Detail(Integer id);

    /**
     * 获取管理端商品列表
     * @param request 搜索参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<PayComponentProduct> getAdminList(ComponentProductSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 添加/扣减库存
     * @param productId 商品id
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @return Boolean
     */
    Boolean operationStock(Integer productId, Integer num, String operationType);

    /**
     * 获取商品详情
     * @param id 商品id
     * @return PayComponentProductResponse
     */
    PayComponentProductResponse getInfo(Integer id);
}
