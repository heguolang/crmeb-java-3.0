package com.qxkj.admin.controller;

import com.qxkj.common.model.chain.ChainAuditLog;
import com.qxkj.common.model.chain.ChainCommission;
import com.qxkj.common.model.chain.ChainDividendRecord;
import com.qxkj.common.model.chain.ChainRewardRule;
import com.qxkj.common.model.chain.ChainRiskLog;
import com.qxkj.common.model.chain.ChainSlideLog;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.ChainRelationUpdateRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.ChainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController("AdminChainController")
@RequestMapping("api/admin/chain")
@Api(tags = "2+1链动管理")
public class ChainController {

    @Autowired
    private ChainService chainService;

    @PreAuthorize("hasAuthority('admin:chain:config')")
    @ApiOperation("获取配置")
    @GetMapping("/config")
    public CommonResult<Map<String, Object>> getConfig() {
        return CommonResult.success(chainService.getConfig());
    }

    @PreAuthorize("hasAuthority('admin:chain:config')")
    @ApiOperation("保存配置")
    @PutMapping("/config")
    public CommonResult<String> saveConfig(@RequestBody Map<String, String> config) {
        chainService.saveConfig(config);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:chain:config')")
    @ApiOperation("奖励规则列表")
    @GetMapping("/rules")
    public CommonResult<List<ChainRewardRule>> rules() {
        return CommonResult.success(chainService.listRules());
    }

    @PreAuthorize("hasAuthority('admin:chain:config')")
    @ApiOperation("保存奖励规则")
    @PutMapping("/rules")
    public CommonResult<String> saveRules(@RequestBody List<ChainRewardRule> rules) {
        chainService.saveRules(rules);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:chain:users')")
    @ApiOperation("链动用户列表")
    @GetMapping("/users")
    public CommonResult<CommonPage<Map<String, Object>>> users(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "identity", required = false) Integer identity,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.adminUserPage(keywords, identity, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:users')")
    @ApiOperation("调整关系")
    @PostMapping("/relation-update")
    public CommonResult<String> relationUpdate(@RequestBody @Validated ChainRelationUpdateRequest request) {
        chainService.adminUpdateRelation(request, "admin");
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:chain:slide')")
    @ApiOperation("滑落日志")
    @GetMapping("/slide-logs")
    public CommonResult<CommonPage<ChainSlideLog>> slideLogs(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.slideLogPage(pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:commission')")
    @ApiOperation("佣金明细")
    @GetMapping("/commission")
    public CommonResult<CommonPage<ChainCommission>> commission(
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "type", required = false) String type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.adminCommissionPage(orderId, type, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:config')")
    @ApiOperation("数据看板")
    @GetMapping("/dashboard")
    public CommonResult<Map<String, Object>> dashboard() {
        return CommonResult.success(chainService.dashboard());
    }

    @PreAuthorize("hasAuthority('admin:chain:commission')")
    @ApiOperation("导出佣金Excel")
    @GetMapping("/commission/export")
    public CommonResult<Map<String, String>> commissionExport(
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "type", required = false) String type) {
        String fileName = chainService.exportCommission(orderId, type);
        Map<String, String> map = new HashMap<>();
        map.put("fileName", fileName);
        return CommonResult.success(map);
    }

    @PreAuthorize("hasAuthority('admin:chain:users')")
    @ApiOperation("关系审计日志")
    @GetMapping("/audit-logs")
    public CommonResult<CommonPage<ChainAuditLog>> auditLogs(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.auditLogPage(pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:users')")
    @ApiOperation("用户链动详情")
    @GetMapping("/user-detail")
    public CommonResult<Map<String, Object>> userDetail(@RequestParam Integer uid) {
        return CommonResult.success(chainService.userChainDetail(uid));
    }

    @PreAuthorize("hasAuthority('admin:chain:risk')")
    @ApiOperation("风控日志")
    @GetMapping("/risk-logs")
    public CommonResult<CommonPage<ChainRiskLog>> riskLogs(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.riskLogPage(pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:dividend')")
    @ApiOperation("团队分红记录")
    @GetMapping("/dividend")
    public CommonResult<CommonPage<ChainDividendRecord>> dividend(
            @RequestParam(value = "period", required = false) String period,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(chainService.dividendPage(period, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:chain:dividend')")
    @ApiOperation("手动执行团队分红")
    @PostMapping("/dividend/run")
    public CommonResult<Map<String, Object>> dividendRun(@RequestParam(value = "period", required = false) String period) {
        int n = chainService.runDividend(period);
        Map<String, Object> map = new HashMap<>();
        map.put("issued", n);
        return CommonResult.success(map);
    }
}
