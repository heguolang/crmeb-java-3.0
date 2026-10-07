package com.qxkj.front.controller;

import com.qxkj.common.model.recruit.RecruitApply;
import com.qxkj.common.model.recruit.RecruitJob;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.RecruitRequests;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.RecruitService;
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
 * 招聘系统控制器（会员端）—— 职位浏览 + 留资投递
 *
 * 职位浏览 / 投递均免登录（游客也能留电话），「我的投递」需登录。
 */
@Slf4j
@RestController
@RequestMapping("api/front/recruit")
@Api(tags = "招聘系统 -- 会员端")
public class RecruitController {

    @Autowired
    private RecruitService recruitService;

    @Autowired
    private UserService userService;

    /** 当前登录会员，未登录返回 0（游客投递） */
    private Integer currentUid() {
        Integer uid = userService.getUserId();
        return uid == null ? 0 : uid;
    }

    // ==================== 免登录 ====================

    @ApiOperation(value = "招聘配置（免登录：开关 + 公司名 + banner + 联系方式）")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> config() {
        return CommonResult.success(recruitService.getConfig());
    }

    @ApiOperation(value = "在招职位分页列表（免登录）")
    @RequestMapping(value = "/job/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<RecruitJob>> jobList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "city", required = false) String city,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(recruitService.frontJobList(keywords, category, city, pageParamRequest));
    }

    @ApiOperation(value = "职位详情（免登录，累计浏览量）")
    @RequestMapping(value = "/job/info", method = RequestMethod.GET)
    public CommonResult<RecruitJob> jobInfo(@RequestParam Integer id) {
        return CommonResult.success(recruitService.frontJobInfo(id));
    }

    @ApiOperation(value = "职位类别列表（免登录，含每个类别在招数量）")
    @RequestMapping(value = "/category/list", method = RequestMethod.GET)
    public CommonResult<List<HashMap<String, Object>>> categoryList() {
        return CommonResult.success(recruitService.frontCategoryList());
    }

    @ApiOperation(value = "投递应聘（免登录，登录用户自动关联会员）")
    @RequestMapping(value = "/apply/submit", method = RequestMethod.POST)
    public CommonResult<String> submitApply(@RequestBody @Validated RecruitRequests.ApplySubmitRequest request) {
        // 短信验证开关（后台可关）
        if ("1".equals(recruitService.getSetting().get("rec_sms_verify"))) {
            recruitService.checkApplyCode(request.getPhone(), request.getCode());
        }
        Integer id = recruitService.submitApply(currentUid(), request);
        if (id != null) {
            return CommonResult.success(String.valueOf(id));
        }
        return CommonResult.failed("提交失败，请稍后重试");
    }

    @ApiOperation(value = "发送投递验证码（免登录）")
    @RequestMapping(value = "/apply/code", method = RequestMethod.POST)
    public CommonResult<String> sendApplyCode(@RequestBody Map<String, String> params) {
        String phone = params == null ? null : params.get("phone");
        if (recruitService.sendApplyCode(phone)) {
            return CommonResult.success("发送成功");
        }
        return CommonResult.failed("发送失败");
    }

    // ==================== 需登录 ====================

    @ApiOperation(value = "我的投递分页列表")
    @RequestMapping(value = "/apply/mine", method = RequestMethod.GET)
    public CommonResult<CommonPage<RecruitApply>> myApplyList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(recruitService.myApplyList(userService.getUserIdException(), pageParamRequest));
    }

    @ApiOperation(value = "我的投递详情")
    @RequestMapping(value = "/apply/info", method = RequestMethod.GET)
    public CommonResult<RecruitApply> myApplyInfo(@RequestParam Integer id) {
        return CommonResult.success(recruitService.myApplyInfo(userService.getUserIdException(), id));
    }
}
