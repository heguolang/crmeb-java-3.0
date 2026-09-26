package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.system.SystemStore;
import com.qxkj.common.request.StoreNearRequest;
import com.qxkj.common.request.SystemStoreRequest;
import com.qxkj.common.response.StoreNearResponse;

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
public interface SystemStoreService extends IService<SystemStore> {

    /**
     * 门店自提分页列表
     * @param keywords 搜索条件
     * @param status 状态
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<SystemStore> getList(String keywords, Integer status, PageParamRequest pageParamRequest);

    /**
     * 根据基本参数获取
     * @param systemStore 基本参数
     * @return 门店自提结果
     */
    SystemStore getByCondition(SystemStore systemStore);

    /**
     * 修改门店显示状态
     * @param id integer id
     * @param status 状态
     */
    Boolean updateStatus(Integer id, Boolean status);

    /**
     * 删除门店自提
     * @param id Integer
     */
    Boolean delete(Integer id);

    /**
     * 表头数量
     * @param keywords 搜索条件:门店名称、门店电话
     * @return HashMap<String, Integer>
     */
    HashMap<String, Integer> getCount(String keywords);

    HashMap<Integer, SystemStore> getMapInId(List<Integer> storeIdList);

    StoreNearResponse getNearList(StoreNearRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增门店自提
     * @param request SystemStoreRequest 新增参数
     */
    Boolean create(SystemStoreRequest request);

    /**
     * 修改门店自提
     * @param id integer id
     * @param request 修改参数
     */
    Boolean update(Integer id, SystemStoreRequest request);

    /**
     * 彻底删除
     * @param id 提货点编号
     */
    Boolean completeLyDelete(Integer id);

    /**
     * 提货点恢复
     * @param id 提货点编号
     */
    Boolean recovery(Integer id);

    /**
     * 门店自提详情
     * @param id Integer
     * @return SystemStore
     */
    SystemStore getInfo(Integer id);
}
