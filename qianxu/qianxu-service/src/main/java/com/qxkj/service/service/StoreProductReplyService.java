package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.product.StoreProductReply;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.StoreProductReplyAddRequest;
import com.qxkj.common.request.StoreProductReplyCommentRequest;
import com.qxkj.common.request.StoreProductReplySearchRequest;
import com.qxkj.common.response.ProductDetailReplyResponse;
import com.qxkj.common.response.ProductReplyResponse;
import com.qxkj.common.response.StoreProductReplyResponse;
import com.qxkj.common.vo.MyRecord;

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
public interface StoreProductReplyService extends IService<StoreProductReply> {

    /**
     * 商品评论列表
     * @param request 请求参数
     * @return PageInfo
     */
    PageInfo<StoreProductReplyResponse> getList(StoreProductReplySearchRequest request);

    /**
     * 创建订单商品评价
     * @param request 请求参数
     * @return Boolean
     */
    Boolean create(StoreProductReplyAddRequest request);

    /**
     * 添加虚拟评论
     * @param request 评论参数
     * @return 评论结果
     */
    boolean virtualCreate(StoreProductReplyAddRequest request);

    /**
     * 查询是否已经回复
     * @param unique skuId
     * @param orderId 订单id
     * @return Boolean
     */
    Boolean isReply(String unique, Integer orderId);

    /**
     * H5商品评论统计
     * @param productId 商品编号
     * @return MyRecord
     */
    MyRecord getH5Count(Integer productId);

    /**
     * H5商品详情评论信息
     * @param proId 商品编号
     * @return ProductDetailReplyResponse
     */
    ProductDetailReplyResponse getH5ProductReply(Integer proId);

    /**
     * 移动端商品评论列表
     * @param proId 商品编号
     * @param type 评价等级|0=全部,1=好评,2=中评,3=差评
     * @param pageParamRequest 分页参数
     * @return PageInfo<ProductReplyResponse>
     */
    PageInfo<ProductReplyResponse> getH5List(Integer proId, Integer type, PageParamRequest pageParamRequest);

    /**
     * 删除评论
     * @param id 评论id
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 商品评论回复
     * @param request 回复参数
     */
    Boolean comment(StoreProductReplyCommentRequest request);

    /**
     * 获取统计数据（好评、中评、差评）
     */
    Integer getCountByScore(Integer productId, String type);

}
