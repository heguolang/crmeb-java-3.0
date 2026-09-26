package com.qxkj.admin.controller;

import com.qxkj.common.model.product.StoreProductReply;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.StoreProductReplyAddRequest;
import com.qxkj.common.request.StoreProductReplyCommentRequest;
import com.qxkj.common.request.StoreProductReplySearchRequest;
import com.qxkj.common.response.StoreProductReplyResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreProductReplyService;
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
@RequestMapping("api/admin/store/product/reply")
@Api(tags = "商品 -- 评论") //配合swagger使用
public class StoreProductReplyController {

    @Autowired
    private StoreProductReplyService storeProductReplyService;

    /**
     * 分页显示评论表
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:product:reply:list')")
    @ApiOperation(value = "分页列表") //配合swagger使用
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreProductReplyResponse>> getList(@Validated StoreProductReplySearchRequest request) {
        CommonPage<StoreProductReplyResponse> storeProductReplyCommonPage =
                CommonPage.restPage(storeProductReplyService.getList(request));
        return CommonResult.success(storeProductReplyCommonPage);
    }

    /**
     * 新增评论表
     * @param request 新增参数
     */
    @PreAuthorize("hasAuthority('admin:product:reply:save')")
    @ApiOperation(value = "新增")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated StoreProductReplyAddRequest request) {
        if (storeProductReplyService.virtualCreate(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 删除评论表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:product:reply:delete')")
    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public CommonResult<String> delete(@PathVariable Integer id) {
        if (storeProductReplyService.delete(id)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 查询评论表信息
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:product:reply:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<StoreProductReply> info(@PathVariable Integer id) {
        StoreProductReply storeProductReply = storeProductReplyService.getById(id);
        return CommonResult.success(storeProductReply);
   }

    /**
     * 回复商品评论
     * @param request  StoreProductReplyCommentRequest 回复参数
     */
    @PreAuthorize("hasAuthority('admin:product:reply:comment')")
    @ApiOperation(value = "回复")
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public CommonResult<String> comment(@RequestBody StoreProductReplyCommentRequest request) {
       if (storeProductReplyService.comment(request)) {
           return CommonResult.success();
       }
       return CommonResult.failed();
    }

}



