package com.qxkj.admin.controller;

import com.qxkj.common.model.svc.SvcCase;
import com.qxkj.common.model.svc.SvcContract;
import com.qxkj.common.model.svc.SvcDemand;
import com.qxkj.common.model.svc.SvcInvoice;
import com.qxkj.common.model.svc.SvcPlatform;
import com.qxkj.common.model.svc.SvcProject;
import com.qxkj.common.model.svc.SvcProjectProgress;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SvcRequests;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SvcService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * 企业服务控制器（后台管理）—— 大市场 → 企业管理
 */
@Slf4j
@RestController
@RequestMapping("api/admin/svc")
@Api(tags = "企业服务 -- 后台管理")
public class SvcAdminController {

    @Autowired
    private SvcService svcService;

    // ==================== 服务需求 ====================

    @PreAuthorize("hasAuthority('admin:svc:demand:list')")
    @ApiOperation(value = "服务需求分页列表")
    @RequestMapping(value = "/demand/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcDemand>> demandList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminDemandList(uid, keywords, type, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:list')")
    @ApiOperation(value = "服务需求详情（含跟进记录与附件）")
    @RequestMapping(value = "/demand/info", method = RequestMethod.GET)
    public CommonResult<SvcDemand> demandInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.adminDemandInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:save')")
    @ApiOperation(value = "保存服务需求（新增/修改），返回需求 ID")
    @RequestMapping(value = "/demand/save", method = RequestMethod.POST)
    public CommonResult<String> saveDemand(@RequestBody @Validated SvcRequests.DemandSaveRequest request) {
        Integer id = svcService.saveDemand(request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:delete')")
    @ApiOperation(value = "删除服务需求")
    @RequestMapping(value = "/demand/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteDemand(@RequestParam Integer id) {
        if (svcService.deleteDemand(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:quote')")
    @ApiOperation(value = "需求报价 / 指派跟进人（金额非空即视为发布报价，客户在 H5「我的需求」可见）")
    @RequestMapping(value = "/demand/quote", method = RequestMethod.POST)
    public CommonResult<String> quoteDemand(@RequestBody @Validated SvcRequests.DemandQuoteRequest request) {
        if (svcService.quoteDemand(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:save')")
    @ApiOperation(value = "追加一条跟进记录")
    @RequestMapping(value = "/demand/follow", method = RequestMethod.POST)
    public CommonResult<String> demandFollow(@RequestBody @Validated SvcRequests.DemandFollowRequest request) {
        if (svcService.saveDemandFollow(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:save')")
    @ApiOperation(value = "关闭需求（不再推进，记录保留）")
    @RequestMapping(value = "/demand/close", method = RequestMethod.POST)
    public CommonResult<String> closeDemand(@RequestBody @Validated SvcRequests.DemandCloseRequest request) {
        if (svcService.closeDemand(request.getId(), request.getReason())) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:contract')")
    @ApiOperation(value = "需求转合同：带出需求里的客户与企业信息，登记成功后回写需求")
    @RequestMapping(value = "/demand/to-contract", method = RequestMethod.POST)
    public CommonResult<String> demandToContract(@RequestParam Integer demandId,
                                                 @RequestBody @Validated SvcRequests.ContractSaveRequest request) {
        Integer contractId = svcService.demandToContract(demandId, request);
        if (contractId != null) {
            return CommonResult.success(String.valueOf(contractId));
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:demand:list')")
    @ApiOperation(value = "服务需求统计（待受理 / 跟进中 / 已报价 / 已成单）")
    @RequestMapping(value = "/demand/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> demandStats() {
        return CommonResult.success(svcService.adminDemandStats());
    }

    // ==================== 签约渠道平台 ====================

    @PreAuthorize("hasAuthority('admin:svc:platform:list')")
    @ApiOperation(value = "签约渠道平台分页列表")
    @RequestMapping(value = "/platform/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcPlatform>> platformList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminPlatformList(keywords, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:platform:list')")
    @ApiOperation(value = "启用的签约渠道平台（登记合同下拉用）")
    @RequestMapping(value = "/platform/enabled", method = RequestMethod.GET)
    public CommonResult<List<SvcPlatform>> enabledPlatforms() {
        return CommonResult.success(svcService.enabledPlatforms());
    }

    @PreAuthorize("hasAuthority('admin:svc:platform:save')")
    @ApiOperation(value = "保存签约渠道平台（新增/修改）")
    @RequestMapping(value = "/platform/save", method = RequestMethod.POST)
    public CommonResult<String> savePlatform(@RequestBody @Validated SvcRequests.PlatformSaveRequest request) {
        if (svcService.savePlatform(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:platform:delete')")
    @ApiOperation(value = "删除签约渠道平台")
    @RequestMapping(value = "/platform/delete", method = RequestMethod.POST)
    public CommonResult<String> deletePlatform(@RequestParam Integer id) {
        if (svcService.deletePlatform(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 发票管理 ====================

    @PreAuthorize("hasAuthority('admin:svc:invoice:list')")
    @ApiOperation(value = "发票申请分页列表")
    @RequestMapping(value = "/invoice/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcInvoice>> invoiceList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "type", required = false) Integer type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminInvoiceList(uid, keywords, status, type, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:invoice:list')")
    @ApiOperation(value = "发票申请详情（含状态日志）")
    @RequestMapping(value = "/invoice/info", method = RequestMethod.GET)
    public CommonResult<SvcInvoice> invoiceInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.adminInvoiceInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:svc:invoice:save')")
    @ApiOperation(value = "保存 / 开票 / 变更发票状态（上传成品发票文件 + 选状态即完成开票），返回申请 ID")
    @RequestMapping(value = "/invoice/save", method = RequestMethod.POST)
    public CommonResult<String> saveInvoice(@RequestBody @Validated SvcRequests.InvoiceSaveRequest request) {
        Integer id = svcService.saveInvoice(request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:invoice:delete')")
    @ApiOperation(value = "删除发票申请")
    @RequestMapping(value = "/invoice/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteInvoice(@RequestParam Integer id) {
        if (svcService.deleteInvoice(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:invoice:list')")
    @ApiOperation(value = "发票统计（待开票 / 开票中 / 已开票 + 本月开票金额）")
    @RequestMapping(value = "/invoice/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> invoiceStats() {
        return CommonResult.success(svcService.adminInvoiceStats());
    }

    // ==================== 合同管理 ====================

    @PreAuthorize("hasAuthority('admin:svc:contract:list')")
    @ApiOperation(value = "合同分页列表")
    @RequestMapping(value = "/contract/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcContract>> contractList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "signChannel", required = false) String signChannel,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminContractList(uid, keywords, status, signChannel, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:list')")
    @ApiOperation(value = "合同详情（含附件）")
    @RequestMapping(value = "/contract/info", method = RequestMethod.GET)
    public CommonResult<SvcContract> contractInfo(@RequestParam Integer id) {
        return CommonResult.success(svcService.adminContractInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:save')")
    @ApiOperation(value = "保存合同（新增/修改），返回合同 ID")
    @RequestMapping(value = "/contract/save", method = RequestMethod.POST)
    public CommonResult<String> saveContract(@RequestBody @Validated SvcRequests.ContractSaveRequest request) {
        Integer id = svcService.saveContract(request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:delete')")
    @ApiOperation(value = "删除合同")
    @RequestMapping(value = "/contract/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteContract(@RequestParam Integer id) {
        if (svcService.deleteContract(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:save')")
    @ApiOperation(value = "准备合同正文：doc/docx/xls/图片 自动转 PDF，返回页数与用印元信息")
    @RequestMapping(value = "/contract/prepare", method = RequestMethod.POST)
    public CommonResult<HashMap<String, Object>> prepareContractPdf(@RequestParam Integer id) {
        return CommonResult.success(svcService.prepareContractPdf(id));
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:save')")
    @ApiOperation(value = "保存合同用印位置（百分比坐标布局）")
    @RequestMapping(value = "/contract/layout/save", method = RequestMethod.POST)
    public CommonResult<String> saveContractLayout(@RequestBody @Validated SvcRequests.ContractLayoutRequest request) {
        if (svcService.saveContractLayout(request.getId(), request.getSignLayout())) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:list')")
    @ApiOperation(value = "渲染合同正文某页为 PNG（用印位置设置的底图）")
    @RequestMapping(value = "/contract/page", method = RequestMethod.GET)
    public void contractPage(@RequestParam Integer id,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "maxWidth", required = false) Integer maxWidth,
                             HttpServletResponse response) throws IOException {
        byte[] png = svcService.renderContractPage(id, page, maxWidth == null || maxWidth <= 0 ? 1240 : maxWidth);
        response.reset();
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setContentLength(png.length);
        response.getOutputStream().write(png);
        response.getOutputStream().flush();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:list')")
    @ApiOperation(value = "读取乙方（黔序）签约方默认值（新建合同时自动带出）")
    @RequestMapping(value = "/contract/our-default/get", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> ourDefault() {
        return CommonResult.success(svcService.ourDefault());
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:save')")
    @ApiOperation(value = "保存乙方（黔序）签约方默认值：会员账号 + 名称/信用代码/法人/电话/地址")
    @RequestMapping(value = "/contract/our-default/save", method = RequestMethod.POST)
    public CommonResult<String> saveOurDefault(@RequestBody @Validated SvcRequests.ContractOurDefaultRequest request) {
        if (svcService.saveOurDefault(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:status')")
    @ApiOperation(value = "变更签约状态（合同线下签署，线上只选状态；写状态日志，置为「已生效」时自动建项目）")
    @RequestMapping(value = "/contract/status/save", method = RequestMethod.POST)
    public CommonResult<String> changeContractStatus(@RequestBody @Validated SvcRequests.ContractStatusRequest request) {
        if (svcService.changeContractStatus(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:contract:list')")
    @ApiOperation(value = "合同统计（待签署 / 已签署 / 已生效 数量 + 本月签约总额）")
    @RequestMapping(value = "/contract/stats", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> contractStats() {
        return CommonResult.success(svcService.adminContractStats());
    }

    // ---------- 以下为「双方在线签署 / 用印位置」遗留接口 ----------
    // 2026-10-02 第八批重构后已停用：合同改为线下签署、线上只选状态，后台与 H5 均不再调用。
    // 接口保留仅为兼容可能存在的旧前端产物，后续版本可整体移除。

    // ==================== 项目进度 ====================

    @PreAuthorize("hasAuthority('admin:svc:project:list')")
    @ApiOperation(value = "项目分页列表")
    @RequestMapping(value = "/project/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcProject>> projectList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminProjectList(uid, keywords, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:project:list')")
    @ApiOperation(value = "项目详情（含进度记录）")
    @RequestMapping(value = "/project/info", method = RequestMethod.GET)
    public CommonResult<SvcProject> projectInfo(@RequestParam Integer id) {
        SvcProject project = svcService.getById(id);
        if (project == null || (project.getIsDel() != null && project.getIsDel() > 0)) {
            throw new RuntimeException("项目不存在");
        }
        project.setProgressList(svcService.progressListOf(id));
        return CommonResult.success(project);
    }

    @PreAuthorize("hasAuthority('admin:svc:project:save')")
    @ApiOperation(value = "保存项目（新增/修改）")
    @RequestMapping(value = "/project/save", method = RequestMethod.POST)
    public CommonResult<String> saveProject(@RequestBody @Validated SvcRequests.ProjectSaveRequest request) {
        if (svcService.saveProject(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:project:progress')")
    @ApiOperation(value = "设置项目进度（后台设置，会员端只读查看）")
    @RequestMapping(value = "/project/progress/save", method = RequestMethod.POST)
    public CommonResult<String> saveProgress(@RequestBody @Validated SvcRequests.ProgressSetRequest request) {
        if (svcService.saveProgress(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:project:list')")
    @ApiOperation(value = "项目进度记录列表")
    @RequestMapping(value = "/project/progress/list", method = RequestMethod.GET)
    public CommonResult<List<SvcProjectProgress>> progressList(@RequestParam Integer projectId) {
        return CommonResult.success(svcService.progressListOf(projectId));
    }

    @PreAuthorize("hasAuthority('admin:svc:project:delete')")
    @ApiOperation(value = "删除项目")
    @RequestMapping(value = "/project/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteProject(@RequestParam Integer id) {
        if (svcService.deleteProject(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 客户案例 ====================

    @PreAuthorize("hasAuthority('admin:svc:case:list')")
    @ApiOperation(value = "客户案例分页列表")
    @RequestMapping(value = "/case/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SvcCase>> caseList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(svcService.adminCaseList(keywords, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:svc:case:save')")
    @ApiOperation(value = "保存客户案例（新增/修改）")
    @RequestMapping(value = "/case/save", method = RequestMethod.POST)
    public CommonResult<String> saveCase(@RequestBody @Validated SvcRequests.CaseSaveRequest request) {
        if (svcService.saveCase(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:svc:case:delete')")
    @ApiOperation(value = "删除客户案例")
    @RequestMapping(value = "/case/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteCase(@RequestParam Integer id) {
        if (svcService.deleteCase(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 服务设置 ====================

    @PreAuthorize("hasAuthority('admin:svc:setting:list')")
    @ApiOperation(value = "服务设置读取")
    @RequestMapping(value = "/setting/get", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> getSetting() {
        return CommonResult.success(svcService.getSetting());
    }

    @PreAuthorize("hasAuthority('admin:svc:setting:save')")
    @ApiOperation(value = "服务设置保存")
    @RequestMapping(value = "/setting/save", method = RequestMethod.POST)
    public CommonResult<String> saveSetting(@RequestBody HashMap<String, Object> settingMap) {
        if (svcService.saveSetting(settingMap)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
