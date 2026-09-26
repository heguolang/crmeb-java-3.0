package com.qxkj.admin.controller;


import com.qxkj.common.model.user.User;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.*;
import com.qxkj.common.response.TopDetail;
import com.qxkj.common.response.UserResponse;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserService;
import com.qxkj.service.service.UserTeamLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/user")
@Api(tags = "会员管理")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserTeamLevelService userTeamLevelService;

    /**
     * 分页显示用户表
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:user:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserResponse>> getList(@Validated UserSearchRequest request) {
        CommonPage<UserResponse> userCommonPage = CommonPage.restPage(userService.getList(request));
        return CommonResult.success(userCommonPage);
    }

    /**
     * 修改用户表
     * @param id integer id
     * @param userRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:user:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员信息")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated UserUpdateRequest userRequest) {
        userRequest.setUid(id);
        if (userService.updateUser(userRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改用户手机号
     * @param id 用户uid
     * @param phone 手机号
     */
    @PreAuthorize("hasAuthority('admin:user:update:phone')")
    @ApiOperation(value = "修改用户手机号")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员手机号")
    @RequestMapping(value = "/update/phone", method = RequestMethod.GET)
    public CommonResult<String> updatePhone(@RequestParam(name = "id") Integer id, @RequestParam(name = "phone") String phone) {
        if (userService.updateUserPhone(id, phone)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改会员登录密码
     */
    @PreAuthorize("hasAuthority('admin:user:update:password')")
    @ApiOperation(value = "修改会员登录密码")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员登录密码")
    @RequestMapping(value = "/update/password", method = RequestMethod.POST)
    public CommonResult<String> updatePassword(@Validated @RequestBody UserUpdatePasswordRequest request) {
        if (userService.updateUserPassword(request.getUid(), request.getPassword())) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    /**
     * 用户详情
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:user:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<User> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(userService.getInfoByUid(id));
    }

    /**
     * 根据参数类型查询会员对应的信息
     * @param userId Integer 会员id
     * @param type int 类型 0=消费记录，1=积分明细，2=签到记录，3=持有优惠券，4=余额变动，5=好友关系
     * @param pageParamRequest PageParamRequest 分页
     */
    @PreAuthorize("hasAuthority('admin:user:infobycondition')")
    @ApiOperation(value="会员详情")
    @RequestMapping(value = "/infobycondition", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId",example = "1", required = true),
            @ApiImplicitParam(name = "type", value="0=消费记录，1=积分明细，2=签到记录，3=持有优惠券，4=余额变动，5=好友关系", example = "0"
                    , required = true)
    })
    public CommonResult<CommonPage<T>> infoByCondition(@RequestParam(name = "userId") @Valid Integer userId,
                                                       @RequestParam(name = "type") @Valid @Max(5) @Min(0) int type,
                                                       @ModelAttribute PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage((List<T>) userService.getInfoByCondition(userId,type,pageParamRequest)));
    }

    /**
     * 会员详情页Top数据
     */
    @PreAuthorize("hasAuthority('admin:user:topdetail')")
    @ApiOperation(value = "会员详情页Top数据")
    @RequestMapping(value = "topdetail", method = RequestMethod.GET)
    public CommonResult<TopDetail> topDetail (@RequestParam @Valid Integer userId) {
        return CommonResult.success(userService.getTopDetail(userId));
    }

    /**
     * 操作积分
     */
    @PreAuthorize("hasAuthority('admin:user:operate:founds')")
    @ApiOperation(value = "账户充减")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "操作会员账户充减")
    @RequestMapping(value = "/operate/founds", method = RequestMethod.GET)
    public CommonResult<Object> founds(@Validated UserOperateIntegralMoneyRequest request) {
        if (userService.updateIntegralMoney(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改佣金
     */
    @PreAuthorize("hasAuthority('admin:user:operate:founds')")
    @ApiOperation(value = "修改佣金")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员佣金账户")
    @RequestMapping(value = "/operate/brokerage", method = RequestMethod.GET)
    public CommonResult<Object> brokerage(@Validated UserOperateBrokerageRequest request) {
        if (userService.updateBrokerage(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 会员分组
     * @param id String id
     * @param groupId Integer 分组Id
     */
    @PreAuthorize("hasAuthority('admin:user:group')")
    @ApiOperation(value = "分组")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员分组")
    @RequestMapping(value = "/group", method = RequestMethod.POST)
    public CommonResult<String> group(@RequestParam String id, @RequestParam String groupId) {
        if (userService.group(id, groupId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 会员标签
     * @param id String id
     * @param tagId Integer 标签id
     */
    @PreAuthorize("hasAuthority('admin:user:tag')")
    @ApiOperation(value = "标签")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员标签")
    @RequestMapping(value = "/tag", method = RequestMethod.POST)
    public CommonResult<String> tag(@RequestParam String id, @RequestParam String tagId) {
        if (userService.tag(id, tagId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改上级推广人
     */
    @PreAuthorize("hasAuthority('admin:user:update:spread')")
    @ApiOperation(value = "修改上级推广人")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员上级推广人")
    @RequestMapping(value = "/update/spread", method = RequestMethod.POST)
    public CommonResult<String> editSpread(@Validated @RequestBody UserUpdateSpreadRequest request) {
        if (userService.editSpread(request)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    /**
     * 更新用户会员等级
     */
    @PreAuthorize("hasAuthority('admin:user:update:level')")
    @ApiOperation(value = "更新用户会员等级")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员等级")
    @RequestMapping(value = "/update/level", method = RequestMethod.POST)
    public CommonResult<Object> updateUserLevel(@Validated @RequestBody UpdateUserLevelRequest request) {
        if (userService.updateUserLevel(request)) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");
    }

    /**
     * 更新用户团队等级
     */
    @PreAuthorize("hasAuthority('admin:user:update:level')")
    @ApiOperation(value = "更新用户团队等级")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改会员团队等级")
    @RequestMapping(value = "/update/team/level", method = RequestMethod.POST)
    public CommonResult<Object> updateUserTeamLevel(@Validated @RequestBody UpdateUserTeamLevelRequest request) {
        if (userTeamLevelService.adminUpdateTeamLevel(request.getUid(), request.getTeamLevelId())) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");
    }
}



