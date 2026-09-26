package com.qxkj.admin.controller;

import com.qxkj.common.model.user.UserTag;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserTagRequest;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
@RequestMapping("api/admin/user/tag")
@Api(tags = "会员 -- 标签") //配合swagger使用
public class UserTagController {

    @Autowired
    private UserTagService userTagService;

    /**
     * 分页显示用户分标签
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:user:tag:list')")
    @ApiOperation(value = "分页列表") //配合swagger使用
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserTag>> getList(@Validated PageParamRequest pageParamRequest) {
        CommonPage<UserTag> userTagCommonPage = CommonPage.restPage(userTagService.getList(pageParamRequest));
        return CommonResult.success(userTagCommonPage);
    }

    /**
     * 新增用户分标签
     * @param userTagRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:user:tag:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增用户标签")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated UserTagRequest userTagRequest) {
        if (userTagService.create(userTagRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除用户分标签
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:user:tag:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除用户标签")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (userTagService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改用户标签
     * @param id integer id
     * @param userTagRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:user:tag:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改用户标签")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated UserTagRequest userTagRequest) {
        if (userTagService.updateTag(id, userTagRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 查询用户标签
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:user:tag:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<UserTag> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(userTagService.getById(id));
   }
}



