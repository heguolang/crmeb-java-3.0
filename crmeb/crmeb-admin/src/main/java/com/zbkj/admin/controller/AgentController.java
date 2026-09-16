package com.zbkj.admin.controller;

import com.zbkj.common.model.agent.Agent;
import com.zbkj.common.model.agent.AgentReward;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AgentAdminRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.AgentService;
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

import java.util.HashMap;

/**
 * 区域代理控制器（后台）
 */
@Slf4j
@RestController
@RequestMapping("api/admin/agent")
@Api(tags = "代理 -- 区域代理管理")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PreAuthorize("hasAuthority('admin:agent:list')")
    @ApiOperation(value = "代理列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<Agent>> getList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(agentService.getAdminList(keywords, level, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:agent:save')")
    @ApiOperation(value = "添加代理（后台直接设置）")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated AgentAdminRequest request) {
        if (agentService.saveAdminAgent(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:agent:update')")
    @ApiOperation(value = "修改代理")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated AgentAdminRequest request) {
        if (agentService.updateAdminAgent(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:agent:audit')")
    @ApiOperation(value = "审核代理")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public CommonResult<String> audit(@RequestParam Integer id, @RequestParam Integer status) {
        if (agentService.auditAgent(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:agent:delete')")
    @ApiOperation(value = "删除代理")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<String> delete(@RequestParam Integer id) {
        if (agentService.deleteAgent(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:agent:reward:list')")
    @ApiOperation(value = "代理奖励明细列表")
    @RequestMapping(value = "/reward/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<AgentReward>> rewardList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(agentService.getRewardList(uid, orderId, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:agent:setting:list')")
    @ApiOperation(value = "获取代理设置")
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> getSetting() {
        return CommonResult.success(agentService.getSetting());
    }

    @PreAuthorize("hasAuthority('admin:agent:setting:save')")
    @ApiOperation(value = "保存代理设置")
    @RequestMapping(value = "/setting/save", method = RequestMethod.POST)
    public CommonResult<String> saveSetting(@RequestBody HashMap<String, Object> settingMap) {
        if (agentService.updateSetting(settingMap)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
