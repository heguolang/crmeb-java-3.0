package com.qxkj.front.controller;

import com.qxkj.common.model.pingou.PingouActivity;
import com.qxkj.common.model.pingou.PingouJoin;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.PingouFrontDetailResponse;
import com.qxkj.common.response.PingouTeamListResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.PingouService;
import com.qxkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



/**
 * 会员端 —— 拼购活动
 */
@Slf4j
@RestController
@RequestMapping("api/front/pingou")
@Api(tags = "会员端——拼购活动")
public class PingouController {

    @Autowired
    private PingouService pingouService;

    @Autowired
    private UserService userService;

    /**
     * 拼购活动列表（首页/专区展示）
     */
    @ApiOperation(value = "拼购活动列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<PingouActivity>> list(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(pingouService.getFrontList(pageParamRequest)));
    }

    /**
     * 拼购活动详情（含当前团进度、累计开团次数、最近开奖团成员与中奖情况）
     */
    @ApiOperation(value = "拼购活动详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<PingouFrontDetailResponse> detail(@PathVariable(value = "id") Integer id) {
        return CommonResult.success(pingouService.getFrontDetail(id));
    }

    /**
     * 活动团列表（历史期数）
     */
    @ApiOperation(value = "拼购活动团列表")
    @RequestMapping(value = "/teams/{activityId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<PingouTeamListResponse>> teams(@PathVariable(value = "activityId") Integer activityId, @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(pingouService.getFrontTeamList(activityId, pageParamRequest)));
    }

    /**
     * 我的拼购参与记录（含是否中奖、奖励发放情况）
     */
    @ApiOperation(value = "我的拼购参与记录")
    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public CommonResult<CommonPage<PingouJoin>> my(@Validated PageParamRequest pageParamRequest) {
        Integer userId = userService.getUserIdException();
        return CommonResult.success(CommonPage.restPage(pingouService.getMyJoins(userId, pageParamRequest)));
    }
}
