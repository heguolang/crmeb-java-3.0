package com.qxkj.front.controller;

import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.combination.StoreCombination;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.StorePinkRequest;
import com.qxkj.common.response.*;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreCombinationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("api/front/combination")
@Api(tags = "拼团商品")
public class CombinationController {

    @Autowired
    private StoreCombinationService storeCombinationService;

    /**
     * 拼团首页
     */
    @ApiOperation(value = "拼团首页数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<CombinationIndexResponse> index() {
        return CommonResult.success(storeCombinationService.getIndexInfo());
    }

    /**
     * 拼团商品列表header
     */
    @ApiOperation(value = "拼团商品列表header")
    @RequestMapping(value = "/header", method = RequestMethod.GET)
    public CommonResult<CombinationHeaderResponse> header() {
        return CommonResult.success(storeCombinationService.getHeader());
    }

    /**
     * 拼团商品列表
     */
    @ApiOperation(value = "拼团商品列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCombinationH5Response>> list(@ModelAttribute PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(storeCombinationService.getH5List(pageParamRequest)));
    }

    /**
     * 拼团商品详情
     */
    @ApiOperation(value = "拼团商品详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<CombinationDetailResponse> detail(@PathVariable(value = "id") Integer id) {
        CombinationDetailResponse h5Detail = storeCombinationService.getH5Detail(id);
        return CommonResult.success(h5Detail);
    }

    /**
     * 去拼团
     * @param pinkId 拼团团长单id
     */
    @ApiOperation(value = "去拼团")
    @RequestMapping(value = "/pink/{pinkId}", method = RequestMethod.GET)
    public CommonResult<GoPinkResponse> goPink(@PathVariable(value = "pinkId") Integer pinkId) {
        GoPinkResponse goPinkResponse = storeCombinationService.goPink(pinkId);
        return CommonResult.success(goPinkResponse);
    }

    /**
     * 更多拼团
     */
    @ApiOperation(value = "更多拼团")
    @RequestMapping(value = "/more", method = RequestMethod.GET)
    public CommonResult<PageInfo<StoreCombination>> getMore(@RequestParam Integer comId, @Validated PageParamRequest pageParamRequest) {
        PageInfo<StoreCombination> more = storeCombinationService.getMore(pageParamRequest, comId);
        return CommonResult.success(more);
    }

    /**
     * 取消拼团
     */
    @ApiOperation(value = "取消拼团")
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public CommonResult<Object> remove(@RequestBody @Validated StorePinkRequest storePinkRequest) {
        if (storeCombinationService.removePink(storePinkRequest)) {
            return CommonResult.success("取消成功");
        } else {
            return CommonResult.failed("取消失败");
        }
    }

}
