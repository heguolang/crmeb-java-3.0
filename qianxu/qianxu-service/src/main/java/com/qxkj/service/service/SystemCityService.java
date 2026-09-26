package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemCity;
import com.qxkj.common.request.SystemCityRequest;
import com.qxkj.common.request.SystemCitySearchRequest;
import com.qxkj.common.vo.SystemCityTreeVo;

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
public interface SystemCityService extends IService<SystemCity> {

    /**
     * 分页城市列表
     * @param request 搜索条件
     */
    Object getList(SystemCitySearchRequest request);

    /**
     * 修改状态
     * @param id 城市id
     * @param status 状态
     */
    Boolean updateStatus(Integer id, Boolean status);

    /**
     * 修改城市
     * @param id 城市id
     * @param request 修改参数
     */
    Boolean update(Integer id, SystemCityRequest request);

    /**
     * 获取城市树
     */
    List<SystemCityTreeVo> getListTree();

    /**
     * 获取所有城市cityId
     * @return List<Integer>
     */
    List<Integer> getCityIdList();

    /**
     * 获取城市
     * @param cityId 城市id
     * @return SystemCity
     */
    SystemCity getCityByCityId(Integer cityId);

    /**
     * 根据城市名称获取城市详细数据
     * @param cityName 城市名称
     * @return 城市数据
     */
    SystemCity getCityByCityName(String cityName);

    /**
     * 根据区名获取数据
     */
    SystemCity getByAreaNameAndPid(String areaName, Integer pid);

    /**
     * 通过区域id获取
     * @param areaId 区域Id
     * @return SystemCity
     */
    SystemCity getByAreaId(Integer areaId);
}
