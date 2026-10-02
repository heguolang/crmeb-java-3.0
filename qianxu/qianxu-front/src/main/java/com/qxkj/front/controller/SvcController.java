package com.qxkj.front.controller;

import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcDemand;
import com.qxkj.common.model.svc.SvcInvoice;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SvcService;
import com.qxkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业服务控制器（会员端）—— 软件开发服务：合同签约 + 项目进度
 */
@Slf4j
@RestController
@RequestMapping("api/front/svc")
@Api(tags = "企业服务 -- 会员端")
public class SvcController {

    @Autowired
    private SvcService svcService;

    @Autowired
    private UserService userService;

    private Integer currentUid() {
        return userService.getUserIdException();
    }

    // ==================== 免登录 ====================

    @ApiOperation(value = "服务配置（免登录：模式开关 + 公司名 + 联系方式）")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> config() {
        return CommonResult.success(svcService.getConfig());
    }

    @ApiOperation(value = "客户案例列表（免登录）")
    @RequestMapping(value = "/case/list", method = RequestMethod.GET)
    public CommonResult<List<SvcCase>> caseList(@RequestParam(value = "limit", required = false) Integer limit) {
        return CommonResult.success(svcService.frontCaseList(limit));
    }

    // ==================== 合同签约 ====================

    @ApiOperation(value = "我的合同分页列表")
    @RequestMapping(value = "/contract/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcContract>> myContractList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.myContractList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "合同详情")
    @RequestMapping(value = "/contract/info", method = RequestMethod.GET)
    public CommonResult<SvcContract> contractInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.myContractDetail(currentUid(), id));
    }

    @ApiOperation(value = "发送签约短信验证码")
    @RequestMapping(value = "/contract/sign/code", method = RequestMethod.POST)
    public CommonResult<String> sendSignCode(@RequestBody Map<String, String> params) {
        String phone = params == null ? null : params.get("phone");
        if (svcService.sendSignCode(phone)) {
            return CommonResult.success("发送成功");
        }
        return CommonResult.failed("发送失败");
    }

    // ---------- 以下为「客户在线签署」遗留接口 ----------
    // 2026-10-02 第八批重构后已停用：合同在线下签署，线上只由后台选状态，客户在 H5 只读。
    // 接口保留仅为兼容可能存在的旧 H5 产物，后续版本可整体移除。

    // ==================== 服务需求 ====================

    @ApiOperation(value = "提交服务需求")
    @RequestMapping(value = "/demand/submit", method = RequestMethod.POST)
    public CommonResult<String> submitDemand(@RequestBody @Validated SvcRequests.DemandSubmitRequest request) {
        Integer id = svcService.submitDemand(currentUid(), request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed("提交失败，请稍后重试");
    }

    @ApiOperation(value = "我的需求分页列表")
    @RequestMapping(value = "/demand/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcDemand>> myDemandList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.myDemandList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "需求详情（含报价与跟进进度）")
    @RequestMapping(value = "/demand/info", method = RequestMethod.GET)
    public CommonResult<SvcDemand> demandInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.myDemandDetail(currentUid(), id));
    }

    @ApiOperation(value = "撤销需求（仅「待受理」可撤销）")
    @RequestMapping(value = "/demand/cancel", method = RequestMethod.POST)
    public CommonResult<String> cancelDemand(@RequestParam Integer id) {
        if (svcService.cancelDemand(currentUid(), id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("撤销失败");
    }

    @ApiOperation(value = "同意报价（需求进入「已成单」，等待生成合同）")
    @RequestMapping(value = "/demand/accept", method = RequestMethod.POST)
    public CommonResult<String> acceptDemandQuote(@RequestParam Integer id) {
        if (svcService.acceptDemandQuote(currentUid(), id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("操作失败");
    }

    // ==================== 发票管理 ====================

    @ApiOperation(value = "提交开票申请")
    @RequestMapping(value = "/invoice/submit", method = RequestMethod.POST)
    public CommonResult<String> submitInvoice(@RequestBody @Validated SvcRequests.InvoiceSubmitRequest request) {
        Integer id = svcService.submitInvoice(currentUid(), request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed("提交失败，请稍后重试");
    }

    @ApiOperation(value = "我的开票申请分页列表")
    @RequestMapping(value = "/invoice/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcInvoice>> myInvoiceList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.myInvoiceList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "开票申请详情（含状态进度与成品发票）")
    @RequestMapping(value = "/invoice/info", method = RequestMethod.GET)
    public CommonResult<SvcInvoice> invoiceInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.myInvoiceDetail(currentUid(), id));
    }

    @ApiOperation(value = "撤销开票申请（仅「待开票」可撤销）")
    @RequestMapping(value = "/invoice/cancel", method = RequestMethod.POST)
    public CommonResult<String> cancelInvoice(@RequestParam Integer id) {
        if (svcService.cancelInvoice(currentUid(), id)) {
            return CommonResult.success();
        }
        return CommonResult.failed("撤销失败");
    }

    // ==================== 项目进度 ====================

    @ApiOperation(value = "签约合同（手写签名 + 电子签章 + 短信验证）：把签署要素按后台设定的位置合成进合同正文，产出可下载的成品合同")
    @RequestMapping(value = "/contract/sign", method = RequestMethod.POST)
    public CommonResult<String> signContract(@RequestBody @Validated SvcRequests.ContractSignRequest request) {
        // 短信验证开关（后台可关）
        if ("1".equals(svcService.getSetting().get("svc_sign_sms"))) {
            svcService.checkSignCode(request.getPhone(), request.getCode());
        }
        if (svcService.signContract(currentUid(), request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 项目进度 ====================

    @ApiOperation(value = "我的项目分页列表")
    @RequestMapping(value = "/project/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcProject>> myProjectList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.myProjectList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "项目详情（含进度记录，会员只读）")
    @RequestMapping(value = "/project/info", method = RequestMethod.GET)
    public CommonResult<SvcProject> projectInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.myProjectDetail(currentUid(), id));
    }

    // ==================== 会员中心 ====================

    @ApiOperation(value = "会员中心统计：进行中项目 / 待验收 / 账户余额")
    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> stats() {
        return CommonResult.success(svcService.myStats(currentUid()));
    }

    @ApiOperation(value = "最新动态（最近项目进度记录）")
    @RequestMapping(value = "/dynamic", method = RequestMethod.GET)
    public CommonResult<List<SvcProjectProgress>> dynamic(
            @RequestParam(value = "limit", required = false, defaultValue = "5") Integer limit) {
        return CommonResult.success(svcService.myDynamic(currentUid(), limit));
    }
}
