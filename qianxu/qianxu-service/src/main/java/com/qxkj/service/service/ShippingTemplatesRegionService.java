package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.express.ShippingTemplatesRegion;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.ShippingTemplatesRegionRequest;
import com.qxkj.common.response.ShippingTemplatesRegionResponse;

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
public interface ShippingTemplatesRegionService extends IService<ShippingTemplatesRegion> {

    Boolean saveAll(List<ShippingTemplatesRegionRequest> shippingTemplatesRegionRequestList, Integer type, Integer id);

    List<ShippingTemplatesRegionResponse> getListGroup(Integer tempId);

    Boolean deleteByTempId(Integer tempId);

    /**
     * 删除
     * @param tempId 运费模板id
     * @return Boolean
     */
    Boolean delete(Integer tempId);

    /**
     * 根据模板编号、城市ID查询
     * @param tempId 模板编号
     * @param cityId 城市ID
     * @return 运费模板
     */
    ShippingTemplatesRegion getByTempIdAndCityId(Integer tempId, Integer cityId);

    /**
     * 获取运费模板区域列表
     * @param tempId 模板标号
     * @return List
     */
    List<ShippingTemplatesRegion> findListById(Integer tempId);
}
