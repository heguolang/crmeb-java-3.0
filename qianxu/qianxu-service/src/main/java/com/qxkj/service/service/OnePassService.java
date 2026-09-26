package com.qxkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.request.*;
import com.qxkj.common.request.onepass.OnePassLoginRequest;
import com.qxkj.common.request.onepass.OnePassShipmentCancelOrderRequest;
import com.qxkj.common.request.onepass.OnePassShipmentCreateOrderRequest;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.common.vo.OnePassLogisticsQueryVo;

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
public interface OnePassService {


    /**
     * 用户登录
     * @param request 登录参数
     */
    Boolean login(OnePassLoginRequest request);

    /**
     * 保存一号通应用信息
     * @param request 一号通服务中申请的应用信息
     * @return 保存结果
     */
    Boolean saveOnePassApplicationInfo(OnePassLoginRequest request);

    /**
     * 获取一号通应用信息
     * @return 一号通应用信息
     */
    OnePassLoginRequest getOnePassApplicationInfo();

    /**
     *  商家寄件
     * @param request 寄件请求对象
     * @return 寄件返回数据
     */
    JSONObject shipmentCreateOrder(OnePassShipmentCreateOrderRequest request);


    /**
     * 取消商家寄件
     * @param request 取消商家寄件请求对象
     * @return 取消寄件返回对象
     */
    JSONObject shipmentCancelOrder(OnePassShipmentCancelOrderRequest request);

    /**
     * 获取商家寄件所需的快递公司列表
     * @return 商家寄件功能对应的快递公司列表
     */
    JSONObject shipmentComs();

    /**
     * 商家寄件功能对应的回调
     * @return 回调数据
     */
    Boolean shipmentCallBackMethod(String type, String data);


    /**
     * 一号通用户信息
     */
    JSONObject info();
    /**
     * 服务开通
     * @param request 服务开通参数
     */
    Boolean serviceOpen(ServiceOpenRequest request);

    /**
     * 复制平台商品
     * @param url 商品链接
     */
    JSONObject copyGoods(String url);

    /**
     * 电子面单
     */
    MyRecord expressDump(MyRecord record);

    /**
     * 物流追踪
     * @param expressNo 快递单号
     * @param com   快递公司简写
     * @return OnePassLogisticsQueryVo
     */
    OnePassLogisticsQueryVo exprQuery(String expressNo, String com, String phone);

    /**
     * 校验一号通账号是否配置
     */
    Boolean checkAccount();
}
