package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.vo.ShopAuditBrandRequestVo;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.wechat.video.PayComponentShopBrand;

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
public interface PayComponentShopBrandService extends IService<PayComponentShopBrand> {

    /**
     * 上传品牌
     * @param request 上传品牌请求参数
     * @return 审核单id
     */
    String auditBrand(ShopAuditBrandRequestVo request);

    /**
     * 获取品牌详情（根据审核单号）
     * @param auditId 审核单号
     * @return 品牌详情
     */
    PayComponentShopBrand getByAuditId(String auditId);

    /**
     * 获取品牌列表
     * @param pageParamRequest 分页参数
     * @param status 审核状态, 0：审核中，1：审核成功，9：审核拒绝
     * @return 品牌列表
     */
    PageInfo<PayComponentShopBrand> findList(PageParamRequest pageParamRequest, Integer status);

    /**
     * 获取品牌列表（可用）
     * @return 品牌列表
     */
    List<PayComponentShopBrand> getUsableList();
}
