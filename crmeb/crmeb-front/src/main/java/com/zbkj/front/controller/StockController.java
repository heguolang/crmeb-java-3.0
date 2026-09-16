package com.zbkj.front.controller;

import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockExchange;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockNotice;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockReward;
import com.zbkj.common.model.stock.StockWithdraw;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockAgentCreateRequest;
import com.zbkj.common.request.StockOrderAddRequest;
import com.zbkj.common.request.StockRequests;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.StockOrderService;
import com.zbkj.service.service.StockRewardService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.UserService;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 订货系统控制器（会员端）
 */
@Slf4j
@RestController
@RequestMapping("api/front/stock")
@Api(tags = "订货系统 -- 会员端")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockOrderService stockOrderService;

    @Autowired
    private StockRewardService stockRewardService;

    @Autowired
    private UserService userService;

    @Autowired
    private StoreProductService storeProductService;

    private Integer currentUid() {
        return userService.getUserIdException();
    }

    // ==================== 代理身份 ====================

    @ApiOperation(value = "我的代理身份")
    @RequestMapping(value = "/agent/info", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> myAgentInfo() {
        return CommonResult.success(stockService.getMyAgentInfo(currentUid()));
    }

    @ApiOperation(value = "新增下级代理")
    @RequestMapping(value = "/agent/createSub", method = RequestMethod.POST)
    public CommonResult<String> createSubAgent(@RequestBody @Validated StockAgentCreateRequest request) {
        if (stockService.createSubAgent(currentUid(), request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "我的下级代理列表")
    @RequestMapping(value = "/agent/subList", method = RequestMethod.GET)
    public CommonResult<List<StockAgent>> subAgentList() {
        return CommonResult.success(stockService.getSubAgentList(currentUid()));
    }

    @ApiOperation(value = "可选择的层级列表")
    @RequestMapping(value = "/agent/levels", method = RequestMethod.GET)
    public CommonResult<List<StockLevel>> levelListForAgent() {
        return CommonResult.success(stockService.getLevelListForAgent(currentUid()));
    }

    // ==================== 商品中心 ====================

    @ApiOperation(value = "订货商品中心（我的拿货价+云仓库存）")
    @RequestMapping(value = "/product/list", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> productList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @Validated PageParamRequest pageParamRequest) {
        Integer uid = currentUid();
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() == 0) {
            throw new com.zbkj.common.exception.CrmebException("您还不是订货代理或已被禁用");
        }
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StoreProduct> lqw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        lqw.eq(StoreProduct::getIsDel, false).eq(StoreProduct::getIsShow, true);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(StoreProduct::getStoreName, keywords.trim());
        }
        lqw.orderByDesc(StoreProduct::getId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<StoreProduct> page =
                storeProductService.page(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                                pageParamRequest.getPage(), pageParamRequest.getLimit()), lqw);
        List<StoreProduct> products = page.getRecords();
        List<HashMap<String, Object>> rows = new ArrayList<>();
        for (StoreProduct p : products) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("storeName", p.getStoreName());
            row.put("image", p.getImage());
            row.put("price", p.getPrice());
            row.put("stock", p.getStock());
            BigDecimal myPrice = stockService.getProductPrice(agent, p.getId());
            row.put("myPrice", myPrice);
            rows.add(row);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", rows);
        map.put("total", page.getTotal());
        map.put("isAgent", true);
        return CommonResult.success(map);
    }

    // ==================== 订货订单 ====================

    @ApiOperation(value = "提交订货单")
    @RequestMapping(value = "/order/create", method = RequestMethod.POST)
    public CommonResult<HashMap<String, Object>> createOrder(@RequestBody @Validated StockOrderAddRequest request) {
        return CommonResult.success(stockOrderService.createOrder(currentUid(), request));
    }

    @ApiOperation(value = "我的订货单列表")
    @RequestMapping(value = "/order/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockOrder>> myOrderList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getMyOrderList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "订单详情")
    @RequestMapping(value = "/order/detail", method = RequestMethod.GET)
    public CommonResult<StockOrder> orderDetail(@RequestParam Integer id) {
        return CommonResult.success(stockOrderService.getOrderDetail(currentUid(), id));
    }

    @ApiOperation(value = "待我审核的订单列表")
    @RequestMapping(value = "/order/auditList", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockOrder>> auditOrderList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getAuditOrderList(currentUid(), status, pageParamRequest));
    }

    @ApiOperation(value = "上级审核订单")
    @RequestMapping(value = "/order/audit", method = RequestMethod.POST)
    public CommonResult<String> auditOrder(@RequestParam Integer id,
                                           @RequestBody @Validated StockRequests.StockAuditRequest request) {
        if (stockOrderService.auditOrder(currentUid(), id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "确认收货")
    @RequestMapping(value = "/order/receive", method = RequestMethod.POST)
    public CommonResult<String> receiveOrder(@RequestParam Integer id) {
        if (stockOrderService.receiveOrder(currentUid(), id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 换货 ====================

    @ApiOperation(value = "提交换货申请")
    @RequestMapping(value = "/exchange/apply", method = RequestMethod.POST)
    public CommonResult<String> applyExchange(@RequestBody @Validated StockRequests.StockExchangeApplyRequest request) {
        if (stockOrderService.applyExchange(currentUid(), request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "我的换货单列表")
    @RequestMapping(value = "/exchange/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockExchange>> myExchangeList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getMyExchangeList(currentUid(), pageParamRequest));
    }

    @ApiOperation(value = "填写旧品退回快递")
    @RequestMapping(value = "/exchange/backExpress", method = RequestMethod.POST)
    public CommonResult<String> fillBackExpress(@RequestParam Integer id,
                                                @RequestBody @Validated StockRequests.StockExchangeBackRequest request) {
        if (stockOrderService.fillBackExpress(currentUid(), id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "上级审核换货单")
    @RequestMapping(value = "/exchange/audit", method = RequestMethod.POST)
    public CommonResult<String> auditExchange(@RequestParam Integer id,
                                              @RequestBody @Validated StockRequests.StockAuditRequest request) {
        if (stockOrderService.auditExchangeByParent(currentUid(), id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 业绩/奖金/提现/消息 ====================

    @ApiOperation(value = "我的业绩")
    @RequestMapping(value = "/performance", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> myPerformance(
            @RequestParam(value = "dateLimit", required = false) String dateLimit) {
        return CommonResult.success(stockRewardService.getMyPerformance(currentUid(), dateLimit));
    }

    @ApiOperation(value = "我的奖金中心")
    @RequestMapping(value = "/bonus", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> myBonus() {
        return CommonResult.success(stockRewardService.getMyBonus(currentUid()));
    }

    @ApiOperation(value = "我的奖金明细")
    @RequestMapping(value = "/reward/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockReward>> myRewardList(
            @RequestParam(value = "type", required = false) Integer type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getMyRewardList(currentUid(), type, pageParamRequest));
    }

    @ApiOperation(value = "申请提现")
    @RequestMapping(value = "/withdraw/apply", method = RequestMethod.POST)
    public CommonResult<String> applyWithdraw(@RequestBody HashMap<String, Object> params) {
        BigDecimal price = params.get("price") == null ? null : new BigDecimal(String.valueOf(params.get("price")));
        String mark = params.get("mark") == null ? "" : String.valueOf(params.get("mark"));
        if (stockRewardService.applyWithdraw(currentUid(), price, mark)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "我的提现记录")
    @RequestMapping(value = "/withdraw/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockWithdraw>> myWithdrawList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getMyWithdrawList(currentUid(), pageParamRequest));
    }

    @ApiOperation(value = "我的消息列表")
    @RequestMapping(value = "/notice/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockNotice>> myNoticeList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getMyNoticeList(currentUid(), pageParamRequest));
    }

    @ApiOperation(value = "未读消息数")
    @RequestMapping(value = "/notice/unreadCount", method = RequestMethod.GET)
    public CommonResult<Long> unreadCount() {
        return CommonResult.success(stockRewardService.unreadNoticeCount(currentUid()));
    }

    @ApiOperation(value = "标记消息已读")
    @RequestMapping(value = "/notice/read", method = RequestMethod.POST)
    public CommonResult<String> readNotice(@RequestParam Integer id) {
        if (stockRewardService.readNotice(currentUid(), id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
