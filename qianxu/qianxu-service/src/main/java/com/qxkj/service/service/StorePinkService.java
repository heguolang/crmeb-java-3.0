package com.qxkj.service.service;

import com.qxkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.combination.StorePink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.StorePinkSearchRequest;
import com.qxkj.common.response.StorePinkAdminHeaderResponse;
import com.qxkj.common.response.StorePinkAdminListResponse;
import com.qxkj.common.response.StorePinkDetailResponse;
import com.qxkj.common.vo.MyRecord;

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
public interface StorePinkService extends IService<StorePink> {

    /**
     * 获取拼团列表
     * @param request
     * @return
     */
    PageInfo<StorePinkAdminListResponse> getList(StorePinkSearchRequest request);

    /**
     * 获取拼团列表Cid
     * @param cid 拼团商品id
     * @return
     */
    List<StorePink> getListByCid(Integer cid);

    /**
     * 实体查询
     * @param storePink
     * @return
     */
    List<StorePink> getByEntity(StorePink storePink);

    /**
     * PC拼团详情列表
     * @param pinkId 团长pinkId
     * @return
     */
    List<StorePinkDetailResponse> getAdminList(Integer pinkId);

    /**
     * 查询拼团列表
     * @param cid
     * @param kid
     */
    List<StorePink> getListByCidAndKid(Integer cid, Integer kid);

    /**
     * 根据团长拼团id获取拼团人数
     * @param pinkId
     * @return
     */
    Integer getCountByKid(Integer pinkId);

    /**
     * 检查状态，更新数据
     */
    void detectionStatus();

    /**
     * 拼团成功
     * @param kid
     * @return
     */
    boolean pinkSuccess(Integer kid);

    /**
     * 根据订单编号获取
     * @param orderId
     * @return
     */
    StorePink getByOrderId(String orderId);

    /**
     * 获取最后3个拼团信息（不同用户）
     * @return List
     */
    List<StorePink> findSizePink(Integer size);

    /**
     * 获取拼团参与总人数
     * @return Integer
     */
    Integer getTotalPeople();

    /**
     *  获取拼团记录的表头数量
     *
     * 状态：1进行中，2已完成，3未完成
     * @param request request
     * @return StorePinkAdminHeaderResponse
     */
    StorePinkAdminHeaderResponse getListHeaderCount(StorePinkSearchRequest request);
}
