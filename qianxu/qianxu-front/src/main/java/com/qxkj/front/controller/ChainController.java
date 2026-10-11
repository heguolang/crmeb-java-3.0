package com.qxkj.front.controller;

import com.qxkj.common.model.chain.ChainCommission;
import com.qxkj.common.model.chain.ChainNotifyLog;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.ChainBindParentRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.ChainIdentityResponse;
import com.qxkj.common.response.ChainTeamNodeResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.ChainService;
import com.qxkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/front/chain")
@Api(tags = "2+1链动")
public class ChainController {

    @Autowired
    private ChainService chainService;
    @Autowired
    private UserService userService;

    @ApiOperation("我的身份")
    @GetMapping("/my-identity")
    public CommonResult<ChainIdentityResponse> myIdentity() {
        return CommonResult.success(chainService.myIdentity(userService.getUserIdException()));
    }

    @ApiOperation("我的团队树")
    @GetMapping("/my-team")
    public CommonResult<ChainTeamNodeResponse> myTeam(@RequestParam(value = "depth", required = false, defaultValue = "3") Integer depth) {
        return CommonResult.success(chainService.myTeam(userService.getUserIdException(), depth));
    }

    @ApiOperation("佣金明细")
    @GetMapping("/commission-list")
    public CommonResult<CommonPage<ChainCommission>> commissionList(
            @RequestParam(value = "type", required = false) String type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.commissionPage(userService.getUserIdException(), type, pageParamRequest));
    }

    @ApiOperation("佣金汇总")
    @GetMapping("/commission-summary")
    public CommonResult<Map<String, Object>> commissionSummary() {
        return CommonResult.success(chainService.commissionSummary(userService.getUserIdException()));
    }

    @ApiOperation("绑定上级")
    @PostMapping("/bind-parent")
    public CommonResult<String> bindParent(@RequestBody @Validated ChainBindParentRequest request) {
        if (chainService.bindParent(userService.getUserIdException(), request.getInviteUid())) {
            return CommonResult.success();
        }
        return CommonResult.failed("绑定失败");
    }

    @ApiOperation("推广海报（含链动邀请链接）")
    @GetMapping("/promote-poster")
    public CommonResult<Map<String, Object>> promotePoster() {
        return CommonResult.success(chainService.promotePoster(userService.getUserIdException()));
    }

    @ApiOperation("链动规则说明")
    @GetMapping("/rules")
    public CommonResult<Map<String, Object>> rules() {
        Map<String, Object> map = new HashMap<>(chainService.rulesMeta());
        map.put("content", chainService.rulesContent());
        return CommonResult.success(map);
    }

    @ApiOperation("站内通知列表")
    @GetMapping("/notify-list")
    public CommonResult<CommonPage<ChainNotifyLog>> notifyList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.notifyPage(userService.getUserIdException(), pageParamRequest));
    }

    @ApiOperation("未读通知数")
    @GetMapping("/notify-unread")
    public CommonResult<Map<String, Object>> notifyUnread() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", chainService.unreadNotifyCount(userService.getUserIdException()));
        return CommonResult.success(map);
    }

    @ApiOperation("通知已读")
    @PostMapping("/notify-read")
    public CommonResult<String> notifyRead(@RequestParam Integer id) {
        chainService.markNotifyRead(userService.getUserIdException(), id);
        return CommonResult.success();
    }
}
