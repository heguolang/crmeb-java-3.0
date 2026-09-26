package com.qxkj.admin.controller;

import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.BrokerageRecordRequest;
import com.qxkj.common.request.FundsMonitorRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.MonitorResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserBillService;
import com.qxkj.service.service.UserFundsMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("api/admin/finance/founds/monitor")
@Api(tags = "财务 -- 资金监控")
public class FundsMonitorController {

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private UserFundsMonitorService userFundsMonitorService;

    /**
     * 分页显示资金监控
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:finance:monitor:list')")
    @ApiOperation(value = "资金监控（余额/佣金/全部三类）")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MonitorResponse>> getList(@Validated FundsMonitorRequest request){
        // 账户类型路由：integral 走积分表，brokerage_price 走佣金记录 union，now_money 走 bill，all 走三表 union
        CommonPage<MonitorResponse> page;
        if ("integral".equals(request.getCategory())) {
            page = CommonPage.restPage(userBillService.fundMonitoringIntegral(request));
        } else if ("brokerage_price".equals(request.getCategory())) {
            page = CommonPage.restPage(userBillService.fundMonitoringBrokerage(request));
        } else if ("now_money".equals(request.getCategory())) {
            page = CommonPage.restPage(userBillService.fundMonitoring(request));
        } else {
            page = CommonPage.restPage(userBillService.fundMonitoringAll(request));
        }
        return CommonResult.success(page);
    }

    /**
     * 佣金记录
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:finance:monitor:brokerage:record')")
    @ApiOperation(value = "佣金记录")
    @RequestMapping(value = "/brokerage/record", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserBrokerageRecord>> brokerageRecord(@Validated BrokerageRecordRequest request){
        return CommonResult.success(CommonPage.restPage(userFundsMonitorService.getBrokerageRecord(request)));
    }
}



