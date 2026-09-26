package com.qxkj.admin.controller;


import com.qxkj.common.model.system.SystemUserLevel;
import com.qxkj.common.request.SystemUserLevelRequest;
import com.qxkj.common.request.SystemUserLevelUpdateShowRequest;
import com.qxkj.common.response.SystemUserLevelInfoResponse;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemUserLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@RestController
@RequestMapping("api/admin/system/user/level")
@Api(tags = "设置 -- 会员等级")
public class SystemUserLevelController {

    @Autowired
    private SystemUserLevelService systemUserLevelService;

    /**
     * 等级列表
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:list')")
    @ApiOperation(value = "等级列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<SystemUserLevel>> getList() {
        return CommonResult.success(systemUserLevelService.getList());
    }

    /**
     * 获取全部会员等级（不分页）
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:list')")
    @ApiOperation(value = "全部等级列表（不分页）")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public CommonResult<List<SystemUserLevel>> getAllList() {
        return CommonResult.success(systemUserLevelService.getAllList());
    }

    /**
     * 等级详情
     * @param id 等级id
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:list')")
    @ApiOperation(value = "等级详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemUserLevelInfoResponse> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(systemUserLevelService.getInfo(id));
    }

    /**
     * 新增等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:save')")
    @ApiOperation(value = "新增等级", notes = "upgradeType决定升级条件：1=experience消费金额，2=upgradeValue订单数，3=两者同时满足；" +
            "consumptionTriggerType/orderCountTriggerType决定统计时机：1=已付款，2=交易完成；giveIntegral为每单固定赠送积分；" +
            "brokerage可配置自购/一级/二级返佣比例(%)。")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增会员等级")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<Object> save(@RequestBody @Validated SystemUserLevelRequest request) {
        if (systemUserLevelService.create(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除等级
     * @param id 等级id
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:delete')")
    @ApiOperation(value = "删除等级")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除会员等级")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<Object> delete(@PathVariable(value = "id") Integer id) {
        if (systemUserLevelService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 更新等级
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:update')")
    @ApiOperation(value = "更新等级", notes = "upgradeType决定升级条件：1=experience消费金额，2=upgradeValue订单数，3=两者同时满足；" +
            "consumptionTriggerType/orderCountTriggerType决定统计时机：1=已付款，2=交易完成；giveIntegral为每单固定赠送积分；" +
            "brokerage可配置自购/一级/二级返佣比例(%)。")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新会员等级")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Object> update(@PathVariable(value = "id") Integer id,
                                       @RequestBody @Validated SystemUserLevelRequest request) {
        if (systemUserLevelService.update(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 使用/禁用
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:use')")
    @ApiOperation(value = "使用/禁用")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "启用/禁用会员等级")
    @RequestMapping(value = "/use", method = RequestMethod.POST)
    public CommonResult<Object> use(@RequestBody @Validated SystemUserLevelUpdateShowRequest request) {
        if (systemUserLevelService.updateShow(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}



