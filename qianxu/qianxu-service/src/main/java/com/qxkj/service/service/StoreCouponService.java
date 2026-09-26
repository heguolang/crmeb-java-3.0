package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SearchAndPageRequest;
import com.qxkj.common.model.coupon.StoreCoupon;
import com.qxkj.common.request.StoreCouponRequest;
import com.qxkj.common.request.StoreCouponSearchRequest;
import com.qxkj.common.response.StoreCouponFrontResponse;
import com.qxkj.common.response.StoreCouponInfoResponse;

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
public interface StoreCouponService extends IService<StoreCoupon> {

    List<StoreCoupon> getList(StoreCouponSearchRequest request, PageParamRequest pageParamRequest);

    boolean create(StoreCouponRequest request);

    StoreCoupon getInfoException(Integer id);

    StoreCouponInfoResponse info(Integer id);

    /**
     * 根据优惠券id获取
     * @param ids 优惠券id集合
     * @return 优惠券列表
     */
    List<StoreCoupon> getByIds(List<Integer> ids);

    /**
     * 扣减数量
     * @param id 优惠券id
     * @param num 数量
     * @param isLimited 是否限量
     */
    Boolean deduction(Integer id, Integer num, Boolean isLimited);

    /**
     * 获取用户注册赠送新人券
     * @return 优惠券列表
     */
    List<StoreCoupon> findRegisterList();

    /**
     * 发送优惠券列表
     * @param searchAndPageRequest 搜索分页参数
     * @return 优惠券列表
     */
    List<StoreCoupon> getSendList(SearchAndPageRequest searchAndPageRequest);

    /**
     * 删除优惠券
     * @param id 优惠券id
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 移动端优惠券列表
     *
     * @param type             类型，1-通用，2-商品，3-品类
     * @param productId        产品id，搜索产品指定优惠券
     * @param pageParamRequest 分页参数
     * @return PageInfo<StoreCouponFrontResponse>
     */
    PageInfo<StoreCouponFrontResponse> getH5List(Integer type, Integer productId, PageParamRequest pageParamRequest);

    /**
     * 移动端主题优惠券列表
     *
     * @param type             类型，1-通用，2-商品，3-品类
     * @param pageParamRequest 分页参数
     * @return PageInfo<StoreCoupon>
     */
    PageInfo<StoreCoupon> getThemeList(Integer type, PageParamRequest pageParamRequest);

    /**
     * 修改优惠券状态
     * @param id 优惠券id
     * @param status 状态
     */
    Boolean updateStatus(Integer id, Boolean status);
}
