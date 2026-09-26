package com.qxkj.admin.service;

import com.qxkj.admin.copyright.CopyrightInfoResponse;
import com.qxkj.admin.copyright.CopyrightUpdateInfoRequest;

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
public interface CopyrightService {

    /**
     * 获取公司名称与图片
     */
    CopyrightInfoResponse getInfo();

    /**
     * 编辑公司名称与图片
     */
    Boolean updateCompanyInfo(CopyrightUpdateInfoRequest request);

    /**
     * 获取公司名称
     */
    String getCompanyInfo();
}
