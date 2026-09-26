package com.qxkj.front.controller;

import com.qxkj.common.model.merchant.MerchantStoreVerifyRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.response.StoreOrderVerificationConfirmResponse;
import com.qxkj.service.service.MerchantStoreService;
import com.qxkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * 门店系统控制器（会员端）
 * 负责人进入门店中心查看/核销；普通用户查询附近门店。
 */
@Slf4j
@RestController
@RequestMapping("api/front/merchantStore")
@Api(tags = "门店系统 -- 会员端")
public class MerchantStoreController {

    @Autowired
    private MerchantStoreService merchantStoreService;

    @Autowired
    private UserService userService;

    private Integer currentUid() {
        return userService.getUserIdException();
    }

    @ApiOperation(value = "我的门店中心（负责人身份+数据概览）")
    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> my() {
        return CommonResult.success(merchantStoreService.myStore(currentUid()));
    }

    @ApiOperation(value = "附近/可服务门店列表")
    @RequestMapping(value = "/nearby", method = RequestMethod.GET)
    public CommonResult<List<com.qxkj.common.response.MerchantStoreNearVo>> nearby(
            @RequestParam(value = "latitude", required = false) String latitude,
            @RequestParam(value = "longitude", required = false) String longitude,
            @RequestParam(value = "productId", required = false) Integer productId) {
        return CommonResult.success(merchantStoreService.nearby(latitude, longitude, productId));
    }

    @ApiOperation(value = "核销码预览待核销订单")
    @RequestMapping(value = "/verify/preview", method = RequestMethod.GET)
    public CommonResult<StoreOrderVerificationConfirmResponse> verifyPreview(@RequestParam String vCode) {
        return CommonResult.success(merchantStoreService.previewVerifyOrder(vCode, currentUid()));
    }

    @ApiOperation(value = "核销订单")
    @RequestMapping(value = "/verify/confirm", method = RequestMethod.POST)
    public CommonResult<Boolean> verifyConfirm(@RequestParam String vCode) {
        return CommonResult.success(merchantStoreService.verifyOrderByCode(vCode, currentUid()));
    }

    @ApiOperation(value = "我的门店核销记录")
    @RequestMapping(value = "/verify/records", method = RequestMethod.GET)
    public CommonResult<CommonPage<MerchantStoreVerifyRecord>> verifyRecords(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(merchantStoreService.myVerifyRecords(currentUid(), pageParamRequest));
    }
}
