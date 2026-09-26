package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.UserCouponReceiveRequest;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.coupon.StoreCouponUser;
import com.qxkj.common.request.StoreCouponUserRequest;
import com.qxkj.common.request.StoreCouponUserSearchRequest;
import com.qxkj.common.response.StoreCouponUserOrder;
import com.qxkj.common.response.StoreCouponUserResponse;

import java.util.HashMap;
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
public interface StoreCouponUserService extends IService<StoreCouponUser> {

    /**
     * 优惠券发放记录
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<StoreCouponUserResponse> getList(StoreCouponUserSearchRequest request);

    /**
     * PC领取优惠券
     * @param storeCouponUserRequest 优惠券参数
     * @return Boolean
     */
    Boolean receive(StoreCouponUserRequest storeCouponUserRequest);

    HashMap<Integer, StoreCouponUser> getMapByUserId(Integer userId);

    /**
     * 当前订单可用优惠券
     * @param preOrderNo 预下单订单号
     * @return 可用优惠券集合
     */
    List<StoreCouponUserOrder> getListByPreOrderNo(String preOrderNo);

    /**
     * 优惠券过期定时任务
     */
    void overdueTask();

    /**
     * 用户领取优惠券
     */
    Boolean receiveCoupon(UserCouponReceiveRequest request);

    /**
     * 支付成功赠送处理
     * @param couponId 优惠券编号
     * @param uid  用户uid
     * @return MyRecord
     */
    MyRecord paySuccessGiveAway(Integer couponId, Integer uid);

    /**
     * 根据uid获取列表
     * @param uid uid
     * @param pageParamRequest 分页参数
     * @return List<StoreCouponUser>
     */
    List<StoreCouponUser> findListByUid(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 获取可用优惠券数量
     * @param uid 用户uid
     */
    Integer getUseCount(Integer uid);

    /**
     * 我的优惠券列表
     * @param type 类型，usable-可用，unusable-不可用
     * @param pageParamRequest 分页参数
     * @return CommonPage<StoreCouponUserResponse>
     */
    CommonPage<StoreCouponUserResponse> getMyCouponList(String type, PageParamRequest pageParamRequest);
}
