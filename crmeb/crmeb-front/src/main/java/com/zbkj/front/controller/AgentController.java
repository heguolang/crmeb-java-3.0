package com.zbkj.front.controller;

import com.zbkj.common.model.agent.AgentReward;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AgentApplyRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.AgentService;
import com.zbkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 区域代理控制器（会员端）
 */
@Slf4j
@RestController
@RequestMapping("api/front/agent")
@Api(tags = "会员端 -- 区域代理")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "申请成为区域代理")
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public CommonResult<Boolean> apply(@RequestBody @Validated AgentApplyRequest request) {
        return CommonResult.success(agentService.apply(userService.getUserIdException(), request));
    }

    @ApiOperation(value = "我的代理中心信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> info() {
        return CommonResult.success(agentService.getMyAgentInfo(userService.getUserIdException()));
    }

    @ApiOperation(value = "我的奖励明细")
    @RequestMapping(value = "/reward/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<AgentReward>> rewardList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(agentService.getMyRewardList(userService.getUserIdException(), pageParamRequest));
    }
}
