package com.zbkj.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.model.finance.UserExtract;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserExtractRequest;
import com.zbkj.common.request.UserExtractSearchRequest;
import com.zbkj.common.response.BalanceResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserExtractService;
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
 * 用户提现表 前端控制器
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
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
        String[] keys = new String[]{
                SysConfigConstants.CONFIG_EXTRACT_SWITCH,
                SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE,
                SysConfigConstants.CONFIG_EXTRACT_MULTIPLE,
                SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE,
                SysConfigConstants.CONFIG_EXTRACT_FEE,
                SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS,
                SysConfigConstants.CONFIG_EXTRACT_TIME_START,
                SysConfigConstants.CONFIG_EXTRACT_TIME_END,
                SysConfigConstants.CONFIG_EXTRACT_BANK
        };
        for (String key : keys) {
            String val = systemConfigService.getValueByKey(key);
            map.put(key, val == null ? "" : val);
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_SWITCH))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_SWITCH, "1");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_MULTIPLE))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_MULTIPLE, "0");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE, "ratio");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_FEE))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_FEE, "0");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS, "1,2,3,4,5,6,7");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_TIME_START))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_TIME_START, "0");
        }
        if (StrUtil.isBlank(map.get(SysConfigConstants.CONFIG_EXTRACT_TIME_END))) {
            map.put(SysConfigConstants.CONFIG_EXTRACT_TIME_END, "24");
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
        String[] keys = new String[]{
                SysConfigConstants.CONFIG_EXTRACT_SWITCH,
                SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE,
                SysConfigConstants.CONFIG_EXTRACT_MULTIPLE,
                SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE,
                SysConfigConstants.CONFIG_EXTRACT_FEE,
                SysConfigConstants.CONFIG_EXTRACT_WEEKDAYS,
                SysConfigConstants.CONFIG_EXTRACT_TIME_START,
                SysConfigConstants.CONFIG_EXTRACT_TIME_END,
                SysConfigConstants.CONFIG_EXTRACT_BANK
        };
        for (String key : keys) {
            if (body.containsKey(key)) {
                Object v = body.get(key);
                systemConfigService.updateOrSaveValueByName(key, v == null ? "" : String.valueOf(v));
            }
        }
        return CommonResult.success();
    }
}
