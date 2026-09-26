package com.qxkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.express.Express;
import com.qxkj.common.request.ExpressSearchRequest;
import com.qxkj.common.request.ExpressUpdateRequest;
import com.qxkj.common.request.ExpressUpdateShowRequest;

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
public interface ExpressService extends IService<Express> {

    /**
    * 列表
    * @param request 搜索条件
    * @param pageParamRequest 分页类参数
    * @return List<Express>
    */
    List<Express> getList(ExpressSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 编辑
     */
    Boolean updateExpress(ExpressUpdateRequest expressRequest);

    /**
     * 修改显示状态
     */
    Boolean updateExpressShow(ExpressUpdateShowRequest expressRequest);

    /**
     * 同步快递公司
     */
    Boolean syncExpress();

    /**
     * 查询全部快递公司
     * @param type 类型：normal-普通，elec-电子面单
     */
    List<Express> findAll(String type);

    /**
     * 查询快递公司面单模板
     * @param com 快递公司编号
     */
    JSONObject template(String com);

    /**
     * 获取电子面单模版
     * @return
     */
    JSONObject templateFor(String com, String type, String is_shipment, String page, String limit);

    /**
     * 查询快递公司
     * @param code 快递公司编号
     * @return Express
     */
    Express getByCode(String code);

    /**
     * 通过物流公司名称获取
     * @param name 物流公司名称
     */
    Express getByName(String name);

    /**
     * 获取快递公司详情
     * @param id 快递公司id
     */
    Express getInfo(Integer id);
}
