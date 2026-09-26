package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.express.ShippingTemplates;
import com.qxkj.common.request.ShippingTemplatesRequest;
import com.qxkj.common.request.ShippingTemplatesSearchRequest;
import com.qxkj.common.response.ShippingTemplatesInfoResponse;

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
public interface ShippingTemplatesService extends IService<ShippingTemplates> {

    List<ShippingTemplates> getList(ShippingTemplatesSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增运费模板
     * @param request 请求参数
     * @return 新增结果
     */
    Boolean create(ShippingTemplatesRequest request);

    Boolean update(Integer id, ShippingTemplatesRequest request);

    /**
     * 删除模板
     * @param id 模板id
     * @return Boolean
     */
    Boolean remove(Integer id);

    /**
     * 获取模板信息
     * @param id 模板id
     * @return ShippingTemplates
     */
    ShippingTemplatesInfoResponse getInfo(Integer id);
}
