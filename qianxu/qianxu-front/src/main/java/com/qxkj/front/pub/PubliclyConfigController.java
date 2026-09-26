package com.qxkj.front.pub;

import com.qxkj.common.constants.CopyrightConstants;
import com.qxkj.common.response.CopyRightResponse;
import com.qxkj.common.response.PayConfigResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@Slf4j
@RestController
@RequestMapping("api/public/config")
@Api(tags = "公开设置控制器")
public class PubliclyConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @ApiOperation(value = "获取版权")
    @RequestMapping(value = "/get/copyright", method = RequestMethod.GET)
    public CommonResult<CopyRightResponse> getCopyright() {
        CopyRightResponse copyRightResponse = new CopyRightResponse();
        copyRightResponse.setCopyrightIcpNumber(systemConfigService.getValueByKey(CopyrightConstants.COPYRIGHT_ICP_NUMBER));
        copyRightResponse.setCopyrightIcpNumberUrl(systemConfigService.getValueByKey(CopyrightConstants.COPYRIGHT_ICP_NUMBER_URL));
        copyRightResponse.setCopyrightInternetRecord(systemConfigService.getValueByKey(CopyrightConstants.COPYRIGHT_INTERNET_RECORD));
        copyRightResponse.setCopyrightInternetRecordUrl(systemConfigService.getValueByKey(CopyrightConstants.COPYRIGHT_INTERNET_RECORD_URL));
        return CommonResult.success(copyRightResponse);
    }

    @ApiOperation(value = "获取移动端域名")
    @RequestMapping(value = "/get/front/domain", method = RequestMethod.GET)
    public CommonResult<String> getFrontDomain() {
        return CommonResult.success(systemConfigService.getFrontDomain());
    }

    @ApiOperation(value = "获取平台当前的素材地址")
    @RequestMapping(value = "/get/admin/mediadomain", method = RequestMethod.GET)
    public CommonResult<String> getMediaDomain() {
        return CommonResult.success(systemConfigService.getMediaDomain());
    }


}
