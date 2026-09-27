package com.qxkj.admin.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PingouTeamSearchRequest;
import com.qxkj.common.response.PingouTeamListResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.PingouService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台 —— 拼购团记录
 */
@Slf4j
@RestController
@RequestMapping("api/admin/pingou/team")
@Api(tags = "后台——拼购——团记录")
public class PingouTeamController {

    @Autowired
    private PingouService pingouService;

    /**
     * 团记录分页列表
     */
    @PreAuthorize("hasAuthority('admin:pingou:team:list')")
    @ApiOperation(value = "拼购团记录分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PingouTeamListResponse>> getList(@Validated PingouTeamSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<PingouTeamListResponse> commonPage = CommonPage.restPage(pingouService.getAdminTeamList(request, pageParamRequest));
        return CommonResult.success(commonPage);
    }

    /**
     * 团详情（成员与中奖情况）
     */
    @PreAuthorize("hasAuthority('admin:pingou:team:list')")
    @ApiOperation(value = "拼购团详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<List<PingouTeamListResponse>> detail(@PathVariable(value = "id") Integer id) {
        return CommonResult.success(pingouService.getTeamDetail(id));
    }
}
