package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.BargainFrontRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.BargainRecordResponse;
import com.qxkj.common.response.BargainUserInfoResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.bargain.StoreBargainUser;
import com.qxkj.common.request.StoreBargainUserSearchRequest;
import com.qxkj.common.response.StoreBargainUserResponse;

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
public interface StoreBargainUserService extends IService<StoreBargainUser> {

    PageInfo<StoreBargainUserResponse> getList(StoreBargainUserSearchRequest request);

    /**
     * 获取砍价商品参与用户列表
     * @param bargainId 砍价商品Id
     * @return List<StoreBargainUser>
     */
    List<StoreBargainUser> getListByBargainId(Integer bargainId);

    /**
     * 获取用户砍价活动列表
     * @param bargainId 砍价商品编号
     * @param uid       参与用户uid
     * @return StoreBargainUser
     */
    List<StoreBargainUser> getListByBargainIdAndUid(Integer bargainId, Integer uid);

    /**
     * 砍价商品用户根据实体查询
     * @param bargainUser 砍价活动
     * @return List<StoreBargainUser>
     */
    List<StoreBargainUser> getByEntity(StoreBargainUser bargainUser);

    /**
     * 获取砍价成功列表Header
     */
    List<StoreBargainUser> getHeaderList();

    /**
     * 获取用户砍价信息
     * @param bargainFrontRequest 请求参数
     * @return BargainUserInfoResponse
     */
    BargainUserInfoResponse getBargainUserInfo(BargainFrontRequest bargainFrontRequest);

    /**
     * 砍价记录
     * @return PageInfo<BargainRecordResponse>
     */
    PageInfo<BargainRecordResponse> getRecordList(PageParamRequest pageParamRequest);
}
