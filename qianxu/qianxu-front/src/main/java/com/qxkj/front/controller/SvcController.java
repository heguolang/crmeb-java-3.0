package com.qxkj.front.controller;

import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
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

    @ApiOperation(value = "签约合同（负责人手写签名 + 短信验证，签约后自动生成项目）")
    @RequestMapping(value = "/contract/sign", method = RequestMethod.POST)
    public CommonResult<String> signContract(@RequestBody @Validated SvcRequests.ContractSignRequest request) {
        // 短信验证开关（后台可关）
        if ("1".equals(svcService.getSetting().get("svc_sign_sms"))) {
            svcService.checkSignCode(request.getPhone(), request.getCode());
        }
        if (svcService.signContract(currentUid(), request.getId(), request.getSignName(),
                request.getPhone(), request.getSignImage())) {
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
