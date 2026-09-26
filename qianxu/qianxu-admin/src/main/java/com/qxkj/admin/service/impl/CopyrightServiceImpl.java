package com.qxkj.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qxkj.admin.copyright.CopyrightInfoResponse;
import com.qxkj.admin.copyright.CopyrightUpdateInfoRequest;
import com.qxkj.admin.service.CopyrightService;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.service.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service
public class CopyrightServiceImpl implements CopyrightService {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取公司版权信息（仅本地配置，无授权校验/外网请求）
     */
    @Override
    public CopyrightInfoResponse getInfo() {
        CopyrightInfoResponse response = new CopyrightInfoResponse();
        response.setCompanyName(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COPYRIGHT_COMPANY_INFO));
        response.setCompanyImage(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COPYRIGHT_COMPANY_IMAGE));
        return response;
    }

    /**
     * 编辑公司版权信息
     */
    @Override
    @Transactional
    public Boolean updateCompanyInfo(CopyrightUpdateInfoRequest request) {
        Boolean update = systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_COPYRIGHT_COMPANY_INFO, request.getCompanyName());
        String path = StrUtil.isNotBlank(request.getCompanyImage()) ? request.getCompanyImage() : "";
        Boolean update1 = systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_COPYRIGHT_COMPANY_IMAGE, path);
        return update && update1;
    }

    /**
     * 获取商户公司名称
     */
    @Override
    public String getCompanyInfo() {
        return systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COPYRIGHT_COMPANY_INFO);
    }
}
