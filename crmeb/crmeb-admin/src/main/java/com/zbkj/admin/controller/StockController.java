package com.zbkj.admin.controller;

import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockExchange;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockLog;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockReward;
import com.zbkj.common.model.stock.StockWithdraw;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockAgentRequest;
import com.zbkj.common.request.StockRequests;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StockOrderService;
import com.zbkj.service.service.StockRewardService;
import com.zbkj.service.service.StockService;
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

import java.util.HashMap;
import java.util.List;

/**
 * 订货系统控制器（后台）
 */
@Slf4j
@RestController
@RequestMapping("api/admin/stock")
@Api(tags = "订货系统 -- 后台管理")
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockOrderService stockOrderService;

    @Autowired
    private StockRewardService stockRewardService;

    // ==================== 层级 ====================

    @PreAuthorize("hasAuthority('admin:stock:agent:list')")
    @ApiOperation(value = "层级列表")
    @RequestMapping(value = "/level/list", method = RequestMethod.GET)
    public CommonResult<List<StockLevel>> levelList() {
        return CommonResult.success(stockService.getLevelList());
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:save')")
    @ApiOperation(value = "保存层级")
    @RequestMapping(value = "/level/save", method = RequestMethod.POST)
    public CommonResult<String> saveLevel(@RequestBody StockLevel level) {
        if (stockService.saveLevel(level)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:delete')")
    @ApiOperation(value = "删除层级")
    @RequestMapping(value = "/level/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteLevel(@RequestParam Integer id) {
        if (stockService.deleteLevel(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 订货代理 ====================

    @PreAuthorize("hasAuthority('admin:stock:agent:list')")
    @ApiOperation(value = "订货代理列表")
    @RequestMapping(value = "/agent/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockAgent>> agentList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "levelId", required = false) Integer levelId,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getAdminAgentList(keywords, uid, levelId, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:list')")
    @ApiOperation(value = "订货商下级团队（伞下全部，含层级深度）")
    @RequestMapping(value = "/agent/team", method = RequestMethod.GET)
    public CommonResult<List<HashMap<String, Object>>> agentTeam(
            @RequestParam(value = "agentId", required = false) Integer agentId,
            @RequestParam(value = "uid", required = false) Integer uid) {
        return CommonResult.success(stockService.getAgentTeam(agentId, uid));
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:update')")
    @ApiOperation(value = "调整订货商虚拟库存")
    @RequestMapping(value = "/agent/virtual/adjust", method = RequestMethod.POST)
    public CommonResult<String> adjustVirtualStock(@RequestBody @Validated StockRequests.StockAgentAdjustRequest request) {
        stockService.adjustAgentVirtualStock(request);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:update')")
    @ApiOperation(value = "调整订货商实体库存")
    @RequestMapping(value = "/agent/physical/adjust", method = RequestMethod.POST)
    public CommonResult<String> adjustPhysicalStock(@RequestBody @Validated StockRequests.StockAgentAdjustRequest request) {
        stockService.adjustAgentPhysicalStock(request);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:save')")
    @ApiOperation(value = "新增代理")
    @RequestMapping(value = "/agent/save", method = RequestMethod.POST)
    public CommonResult<String> saveAgent(@RequestBody @Validated StockAgentRequest request) {
        if (stockService.saveAgent(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:update')")
    @ApiOperation(value = "修改代理")
    @RequestMapping(value = "/agent/update", method = RequestMethod.POST)
    public CommonResult<String> updateAgent(@RequestBody @Validated StockAgentRequest request) {
        if (stockService.saveAgent(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:update')")
    @ApiOperation(value = "启用/禁用代理")
    @RequestMapping(value = "/agent/status", method = RequestMethod.POST)
    public CommonResult<String> agentStatus(@RequestParam Integer id, @RequestParam Integer status) {
        if (stockService.changeAgentStatus(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:delete')")
    @ApiOperation(value = "删除代理")
    @RequestMapping(value = "/agent/delete", method = RequestMethod.POST)
    public CommonResult<String> deleteAgent(@RequestParam Integer id) {
        if (stockService.deleteAgent(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:agent:list')")
    @ApiOperation(value = "订货商变更记录列表")
    @RequestMapping(value = "/changelog/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<com.zbkj.common.model.stock.StockChangeLog>> changeLogList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "type", required = false) Integer type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getChangeLogList(uid, type, pageParamRequest));
    }

    // ==================== 商品与库存 ====================

    @PreAuthorize("hasAuthority('admin:stock:product:list')")
    @ApiOperation(value = "商品列表（含库存与各层拿货价，仅已加入订货的商品）")
    @RequestMapping(value = "/product/list", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> productList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getProductList(keywords, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:product:list')")
    @ApiOperation(value = "可添加商品列表（未加入订货的商品）")
    @RequestMapping(value = "/product/selectList", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> productSelectList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getSelectableProductList(keywords, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "批量添加商品到订货模块")
    @RequestMapping(value = "/product/add", method = RequestMethod.POST)
    public CommonResult<String> addProducts(@RequestBody List<Integer> productIds) {
        if (stockService.addProducts(productIds)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "从订货模块移除商品")
    @RequestMapping(value = "/product/remove", method = RequestMethod.POST)
    public CommonResult<String> removeProduct(@RequestParam Integer productId) {
        if (stockService.removeStockProduct(productId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "保存商品层级拿货价（带 skuKey 时为规格级价）")
    @RequestMapping(value = "/price/save", method = RequestMethod.POST)
    public CommonResult<String> savePrice(@RequestBody @Validated StockRequests.StockPriceSetRequest request) {
        if (stockService.savePrice(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "商品规格列表（含零售价与云仓规格库存）")
    @RequestMapping(value = "/product/skulist", method = RequestMethod.GET)
    public CommonResult<java.util.List<java.util.HashMap<String, Object>>> productSkuList(@RequestParam Integer productId) {
        return CommonResult.success(stockService.getProductSkuList(productId));
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "某规格的层级拿货价（回显）")
    @RequestMapping(value = "/product/skuPrice", method = RequestMethod.GET)
    public CommonResult<java.util.List<java.util.HashMap<String, Object>>> priceSkuList(
            @RequestParam Integer productId,
            @RequestParam String skuKey) {
        return CommonResult.success(stockService.getPriceSkuList(productId, skuKey));
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "商品是否支持虚拟/实体库存（读取）")
    @RequestMapping(value = "/product/stockType", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> productStockType(@RequestParam Integer productId) {
        HashMap<String, Object> map = new HashMap<>();
        com.zbkj.common.model.stock.StockProductRel rel = stockService.getProductRel(productId);
        map.put("supportVirtual", rel == null || rel.getSupportVirtual() == null || rel.getSupportVirtual());
        map.put("supportPhysical", rel == null || rel.getSupportPhysical() == null || rel.getSupportPhysical());
        return CommonResult.success(map);
    }

    @PreAuthorize("hasAuthority('admin:stock:price:save')")
    @ApiOperation(value = "保存商品是否支持虚拟/实体库存")
    @RequestMapping(value = "/product/stockType/save", method = RequestMethod.POST)
    public CommonResult<String> saveProductStockType(@RequestBody HashMap<String, Object> params) {
        Integer productId = params.get("productId") == null ? null : Integer.valueOf(String.valueOf(params.get("productId")));
        if (productId == null) {
            return CommonResult.failed("商品不能为空");
        }
        Boolean supportVirtual = params.get("supportVirtual") == null ? Boolean.TRUE
                : Boolean.valueOf(String.valueOf(params.get("supportVirtual")));
        Boolean supportPhysical = params.get("supportPhysical") == null ? Boolean.TRUE
                : Boolean.valueOf(String.valueOf(params.get("supportPhysical")));
        stockService.saveProductStockType(productId, supportVirtual, supportPhysical);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:stock:log:adjust')")
    @ApiOperation(value = "手动调整库存")
    @RequestMapping(value = "/stock/adjust", method = RequestMethod.POST)
    public CommonResult<String> adjustStock(@RequestBody @Validated StockRequests.StockAdjustRequest request) {
        if (stockService.adjustStock(request, null)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:product:list')")
    @ApiOperation(value = "库存变动日志")
    @RequestMapping(value = "/log/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockLog>> logList(
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "type", required = false) Integer type,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getLogList(productId, type, pageParamRequest));
    }

    // ==================== 订货订单 ====================

    @PreAuthorize("hasAuthority('admin:stock:order:list')")
    @ApiOperation(value = "订货订单列表")
    @RequestMapping(value = "/order/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockOrder>> orderList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "payStatus", required = false) Integer payStatus,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getAdminOrderList(uid, orderNo, status, payStatus, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:order:audit')")
    @ApiOperation(value = "总部介入审核订单（仅待上级审核状态）")
    @RequestMapping(value = "/order/audit", method = RequestMethod.POST)
    public CommonResult<String> auditOrderByAdmin(@RequestParam Integer id,
                                                  @RequestBody @Validated StockRequests.StockAuditRequest request) {
        if (stockOrderService.auditOrderByAdmin(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:order:audit')")
    @ApiOperation(value = "跳过匹配上级（对等待匹配状态订单立即沿上级链向上找有库存的上级）")
    @RequestMapping(value = "/order/skipMatch", method = RequestMethod.POST)
    public CommonResult<String> skipMatchUpOrder(@RequestParam Integer id) {
        if (stockOrderService.skipMatchUpOrder(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:order:list')")
    @ApiOperation(value = "等待匹配上级的订单列表")
    @RequestMapping(value = "/order/waitMatch", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockOrder>> waitMatchOrderList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getWaitMatchOrderList(pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:order:list')")
    @ApiOperation(value = "订货商库存修改记录（溯源）")
    @RequestMapping(value = "/adjustLog/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<HashMap<String, Object>>> adjustLogList(
            @RequestParam(value = "agentId", required = false) Integer agentId,
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "stockType", required = false) Integer stockType,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockService.getAdjustLogList(agentId, uid, stockType, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:order:list')")
    @ApiOperation(value = "某会员的当前库存情况（实体 + 虚拟）")
    @RequestMapping(value = "/agent/stock", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> agentStock(@RequestParam Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("physical", stockOrderService.getMyPhysicalStock(uid));
        map.put("virtual", stockOrderService.getMyVirtualStock(uid));
        return CommonResult.success(map);
    }

    @PreAuthorize("hasAuthority('admin:stock:order:pay')")
    @ApiOperation(value = "确认收款")
    @RequestMapping(value = "/order/pay", method = RequestMethod.POST)
    public CommonResult<String> confirmPay(@RequestParam Integer id) {
        if (stockOrderService.confirmPay(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:order:send')")
    @ApiOperation(value = "订单发货")
    @RequestMapping(value = "/order/send", method = RequestMethod.POST)
    public CommonResult<String> sendOrder(@RequestParam Integer id, @RequestBody @Validated StockRequests.StockSendRequest request) {
        if (stockOrderService.sendOrder(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:order:send')")
    @ApiOperation(value = "标记订单完成")
    @RequestMapping(value = "/order/finish", method = RequestMethod.POST)
    public CommonResult<String> finishOrder(@RequestParam Integer id) {
        if (stockOrderService.finishOrder(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 换货管理 ====================

    @PreAuthorize("hasAuthority('admin:stock:exchange:list')")
    @ApiOperation(value = "换货单列表")
    @RequestMapping(value = "/exchange/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockExchange>> exchangeList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockOrderService.getAdminExchangeList(status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:audit')")
    @ApiOperation(value = "总部审核换货单")
    @RequestMapping(value = "/exchange/audit", method = RequestMethod.POST)
    public CommonResult<String> auditExchange(@RequestParam Integer id,
                                              @RequestBody @Validated StockRequests.StockAuditRequest request) {
        if (stockOrderService.auditExchangeByHq(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:back')")
    @ApiOperation(value = "确认旧品入库")
    @RequestMapping(value = "/exchange/back", method = RequestMethod.POST)
    public CommonResult<String> confirmExchangeBack(@RequestParam Integer id) {
        if (stockOrderService.confirmExchangeBack(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:send')")
    @ApiOperation(value = "发新品")
    @RequestMapping(value = "/exchange/send", method = RequestMethod.POST)
    public CommonResult<String> sendExchangeNew(@RequestParam Integer id,
                                                @RequestBody @Validated StockRequests.StockSendRequest request) {
        if (stockOrderService.sendExchangeNew(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:list')")
    @ApiOperation(value = "换货设置列表（按商品）")
    @RequestMapping(value = "/exchange/config", method = RequestMethod.GET)
    public CommonResult<java.util.List<com.zbkj.common.model.stock.StockExchangeConfig>> exchangeConfigList(
            @RequestParam Integer productId) {
        return CommonResult.success(stockOrderService.getExchangeConfigList(productId));
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:list')")
    @ApiOperation(value = "保存换货设置（商品级或规格级）")
    @RequestMapping(value = "/exchange/config/save", method = RequestMethod.POST)
    public CommonResult<String> saveExchangeConfig(@RequestBody @Validated StockRequests.StockExchangeConfigRequest request) {
        stockOrderService.saveExchangeConfig(request);
        return CommonResult.success();
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:list')")
    @ApiOperation(value = "换货可选目标清单")
    @RequestMapping(value = "/exchange/targets", method = RequestMethod.GET)
    public CommonResult<java.util.List<com.zbkj.common.model.stock.StockExchangeTarget>> exchangeTargets(
            @RequestParam Integer productId,
            @RequestParam(value = "skuKey", required = false) String skuKey) {
        return CommonResult.success(stockOrderService.getExchangeTargetList(productId, skuKey));
    }

    @PreAuthorize("hasAuthority('admin:stock:exchange:list')")
    @ApiOperation(value = "保存换货可选目标清单（覆盖式）")
    @RequestMapping(value = "/exchange/targets/save", method = RequestMethod.POST)
    public CommonResult<String> saveExchangeTargets(@RequestBody @Validated StockRequests.StockExchangeTargetSaveRequest request) {
        stockOrderService.saveExchangeTargets(request);
        return CommonResult.success();
    }

    // ==================== 奖金与提现 ====================

    @PreAuthorize("hasAuthority('admin:stock:reward:list')")
    @ApiOperation(value = "奖金明细列表")
    @RequestMapping(value = "/reward/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockReward>> rewardList(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getAdminRewardList(uid, type, orderNo, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:withdraw:list')")
    @ApiOperation(value = "提现列表")
    @RequestMapping(value = "/withdraw/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StockWithdraw>> withdrawList(
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getAdminWithdrawList(status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:withdraw:audit')")
    @ApiOperation(value = "提现审核")
    @RequestMapping(value = "/withdraw/audit", method = RequestMethod.POST)
    public CommonResult<String> auditWithdraw(@RequestParam Integer id,
                                              @RequestBody @Validated StockRequests.StockWithdrawAuditRequest request) {
        if (stockRewardService.auditWithdraw(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    // ==================== 奖励规则 ====================

    @Autowired
    private com.zbkj.service.service.SystemConfigService systemConfigService;

    @PreAuthorize("hasAuthority('admin:stock:setting:list')")
    @ApiOperation(value = "奖励规则设置")
    @RequestMapping(value = "/setting/get", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> getSetting() {
        HashMap<String, Object> map = new HashMap<>();
        String[] keys = {"stock_order_audit", "stock_diff_reward_status", "stock_exchange_diff",
                "stock_ladder_status", "stock_ladder_cycle", "stock_peer_status",
                "stock_peer_rate", "stock_peer_generations",
                "stock_parent_deliver", "stock_up_search_hours",
                "stock_virtual_audit", "stock_wait_pay_hours",
                "stock_exchange_single", "stock_exchange_diff_parent_rate",
                "stock_exchange_diff_wechat", "stock_exchange_hq_audit", "stock_exchange_return_address"};
        for (String key : keys) {
            map.put(key, systemConfigService.getValueByKey(key));
        }
        map.put("ladders", stockService.getLadderList());
        return CommonResult.success(map);
    }

    @PreAuthorize("hasAuthority('admin:stock:setting:save')")
    @ApiOperation(value = "保存奖励规则设置")
    @RequestMapping(value = "/setting/save", method = RequestMethod.POST)
    public CommonResult<String> saveSetting(@RequestBody HashMap<String, Object> settingMap) {
        try {
            String[] keys = {"stock_order_audit", "stock_diff_reward_status", "stock_exchange_diff",
                    "stock_ladder_status", "stock_ladder_cycle", "stock_peer_status",
                    "stock_peer_rate", "stock_peer_generations",
                    "stock_parent_deliver", "stock_up_search_hours",
                    "stock_virtual_audit", "stock_wait_pay_hours",
                    "stock_exchange_single", "stock_exchange_diff_parent_rate",
                    "stock_exchange_diff_wechat", "stock_exchange_hq_audit", "stock_exchange_return_address"};
            for (String key : keys) {
                if (settingMap.containsKey(key)) {
                    Object v = settingMap.get(key);
                    systemConfigService.updateOrSaveValueByName(key, v == null ? "" : String.valueOf(v));
                }
            }
            Object laddersObj = settingMap.get("ladders");
            if (laddersObj != null) {
                // Jackson 反序列化 HashMap 时 List 元素是 LinkedHashMap，需显式转 StockLadder
                List<com.zbkj.common.model.stock.StockLadder> ladders =
                        com.alibaba.fastjson.JSON.parseArray(
                                com.alibaba.fastjson.JSON.toJSONString(laddersObj),
                                com.zbkj.common.model.stock.StockLadder.class);
                if (!stockService.saveLadders(ladders)) {
                    return CommonResult.failed("阶梯保存失败");
                }
            }
            return CommonResult.success();
        } catch (Exception e) {
            log.error("保存订货奖励规则失败", e);
            return CommonResult.failed("保存失败：" + e.getMessage());
        }
    }

    // ==================== 报表 ====================

    @PreAuthorize("hasAuthority('admin:stock:report:list')")
    @ApiOperation(value = "数据报表")
    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> report(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam(value = "dateLimit", required = false) String dateLimit,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(stockRewardService.getReport(uid, dateLimit, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:stock:setting:save')")
    @ApiOperation(value = "阶梯业绩奖励结算（月度/季度/年度）")
    @RequestMapping(value = "/reward/monthlySettle", method = RequestMethod.POST)
    public CommonResult<String> monthlySettle(@RequestBody @Validated StockRequests.StockMonthlySettleRequest request) {
        if (stockRewardService.settleLadderReward(request.getType(), request.getMonth())) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
