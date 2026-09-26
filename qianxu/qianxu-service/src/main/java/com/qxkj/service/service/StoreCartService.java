package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.CartNumRequest;
import com.qxkj.common.request.CartRequest;
import com.qxkj.common.request.CartResetRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.CartInfoResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.cat.StoreCart;

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
public interface StoreCartService extends IService<StoreCart> {

    /**
     * 根据有效标识符获取出数据
     * @param pageParamRequest 分页参数
     * @param isValid 是否失效
     * @return 购物车列表
     */
    PageInfo<CartInfoResponse> getList(PageParamRequest pageParamRequest, boolean isValid);

    /**
     * 获取当前购物车数量
     * @param request 请求参数
     * @return 数量
     */
    Map<String, Integer> getUserCount(CartNumRequest request);

    /**
     * 新增购物车数据
     * @param storeCartRequest 新增购物车参数
     * @return 新增结果
     */
    String saveCate(CartRequest storeCartRequest);

    /**
     * 删除购物车
     * @param ids 待删除id
     * @return 返回删除状态
     */
    Boolean deleteCartByIds(List<Long> ids);


    /**
     * 检测商品是否有效 更新购物车商品状态
     * @param productId 商品id
     * @return 跟新结果
     */
    Boolean productStatusNotEnable(Integer productId);

    /**
     * 购物车重选提交
     * @param resetRequest 重选数据
     * @return 提交结果
     */
    Boolean resetCart(CartResetRequest resetRequest);

    /**
     * 对应sku购物车生效
     * @param skuIdList skuIdList
     */
    Boolean productStatusNoEnable(List<Integer> skuIdList);

    /**
     * 删除商品对应的购物车
     * @param productId 商品id
     */
    Boolean productDelete(Integer productId);

    /**
     * 通过id和uid获取购物车信息
     * @param id 购物车id
     * @param uid 用户uid
     * @return StoreCart
     */
    StoreCart getByIdAndUid(Long id, Integer uid);

    /**
     * 获取购物车商品数量（不区分规格）
     * @param uid 用户uid
     * @param proId 商品id
     */
    Integer getProductNumByUidAndProductId(Integer uid, Integer proId);

    /**
     * 修改购物车商品数量
     * @param id 购物车id
     * @param number 数量
     */
    Boolean updateCartNum(Integer id, Integer number);

    /**
     * 通过用户id删除
     * @param uid 用户ID
     */
    Boolean deleteByUid(Integer uid);
}
