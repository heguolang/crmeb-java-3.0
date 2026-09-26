package com.qxkj.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.model.finance.UserExtract;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserExtractRequest;
import com.qxkj.common.request.UserExtractSearchRequest;
import com.qxkj.common.response.BalanceResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserExtractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;


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
@RequestMapping("api/admin/finance/apply")
@Api(tags = "财务 -- 提现申请")
public class UserExtractController {

    @Autowired
    private UserExtractService userExtractService;

    @Autowired
    private SystemConfigService systemConfigService;

    private static final String[] SETTING_KEYS = new String[]{
            // 佣金提现
            SysConfigConstants.CONFIG_EXTRACT_SWITCH,
            SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE,
            SysConfigConstants.CONFIG_EXTRACT_MULTIPLE,
            SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE,
            SysConfigConstants.CONFIG_EXTRACT_FEE,
            SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS,
            SysConfigConstants.CONFIG_EXTRACT_TIME_START,
            SysConfigConstants.CONFIG_EXTRACT_TIME_END,
            // 余额提现
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_SWITCH,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_MIN_PRICE,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_MULTIPLE,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE_TYPE,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_WEEKDAYS,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_START,
            SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_END,
            // 支持银行
            SysConfigConstants.CONFIG_EXTRACT_BANK
    };

    /**
     * 分页显示用户提现表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:finance:apply:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserExtract>> getList(@Validated UserExtractSearchRequest request, @Validated PageParamRequest pageParamRequest){
        CommonPage<UserExtract> userExtractCommonPage = CommonPage.restPage(userExtractService.getList(request, pageParamRequest));
        return CommonResult.success(userExtractCommonPage);
    }

    /**
     * 修改用户提现表
     * @param id integer id
     * @param userExtractRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:finance:apply:update')")
    @ApiOperation(value = "修改")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @Validated UserExtractRequest userExtractRequest){
        if (userExtractService.updateExtract(id, userExtractRequest)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 提现统计
     * @Param dateLimit 时间限制 today,yesterday,lately7,lately30,month,year,/yyyy-MM-dd hh:mm:ss,yyyy-MM-dd hh:mm:ss/
     */
    @PreAuthorize("hasAuthority('admin:finance:apply:balance')")
    @ApiOperation(value = "提现统计")
    @RequestMapping(value = "/balance", method = RequestMethod.POST)
    public CommonResult<BalanceResponse> balance(@RequestParam(value = "dateLimit", required = false,defaultValue = "")
                    String dateLimit){
        return CommonResult.success(userExtractService.getBalance(dateLimit));
    }

    /**
     * 提现审核
     * @param id    提现id
     * @param status    审核状态 -1 未通过 0 审核中 1 已提现
     * @param backMessage   驳回原因
     * @return 审核结果
     */
    @PreAuthorize("hasAuthority('admin:finance:apply:apply')")
    @ApiOperation(value = "提现申请审核")
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public CommonResult<String> updateStatus(@RequestParam(value = "id") Integer id,
                                             @RequestParam(value = "status",defaultValue = "审核状态 -1 未通过 0 审核中 1 已提现") Integer status,
                                             @RequestParam(value = "backMessage",defaultValue = "驳回原因", required = false) String backMessage){
        if(userExtractService.updateStatus(id, status, backMessage)){
            return CommonResult.success();
        }else{
            return CommonResult.failed();
        }
    }

    /**
     * 提现设置获取
     */
    @PreAuthorize("hasAuthority('admin:finance:extract:setting:get')")
    @ApiOperation(value = "提现设置获取")
    @RequestMapping(value = "/setting/get", method = RequestMethod.GET)
    public CommonResult<Map<String, String>> getSetting() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : SETTING_KEYS) {
            String val = systemConfigService.getValueByKey(key);
            map.put(key, val == null ? "" : val);
        }
        // 佣金提现默认值
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_SWITCH, "1");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE, "1");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_MULTIPLE, "0");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE, "ratio");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_FEE, "0");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS, "1,2,3,4,5,6,7");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_TIME_START, "0");
        putDefault(map, SysConfigConstants.CONFIG_EXTRACT_TIME_END, "24");
        // 余额提现默认值
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_SWITCH, "0");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_MIN_PRICE, "1");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_MULTIPLE, "0");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE_TYPE, "ratio");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE, "0");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_WEEKDAYS, "1,2,3,4,5,6,7");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_START, "0");
        putDefault(map, SysConfigConstants.CONFIG_BALANCE_EXTRACT_TIME_END, "24");
        // 支持银行默认值
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_BANK))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_BANK,
                    "中国工商银行\n中国建设银行\n中国农业银行\n中国银行\n交通银行\n招商银行\n中国邮政储蓄银行\n中信银行\n中国光大银行\n兴业银行\n浦发银行\n民生银行");
        } else {
            map.put(SysConfigConstants.CONFIG_EXTRACT_BANK,
                    map.get(SysConfigConstants.CONFIG_EXTRACT_BANK).replace("\\n", "\n"));
        }
        return CommonResult.success(map);
    }

    /**
     * 提现设置保存
     */
    @PreAuthorize("hasAuthority('admin:finance:extract:setting:save')")
    @ApiOperation(value = "提现设置保存")
    @RequestMapping(value = "/setting/save", method = RequestMethod.POST)
    public CommonResult<String> saveSetting(@RequestBody Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return CommonResult.failed("参数不能为空");
        }
        for (String key : SETTING_KEYS) {
            if (body.containsKey(key)) {
                Object v = body.get(key);
                systemConfigService.updateOrSaveValueByName(key, v == null ? "" : String.valueOf(v));
            }
        }
        return CommonResult.success();
    }

    private void putDefault(Map<String, String> map, String key, String defaultVal) {
        if (StrUtil.isBlank(map.get(key))) {
            map.put(key, defaultVal);
        }
    }
}
