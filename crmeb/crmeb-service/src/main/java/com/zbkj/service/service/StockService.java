package com.zbkj.service.service;

import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockLog;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.StockAgentCreateRequest;
import com.zbkj.common.request.StockAgentRequest;
import com.zbkj.common.request.StockRequests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订货系统-基础服务（层级/代理树/拿货价/云仓库存）
 */
public interface StockService {

    // ==================== 后台 ====================

    /** 层级列表 */
    List<StockLevel> getLevelList();

    /** 保存层级（新增或修改） */
    Boolean saveLevel(StockLevel level);

    /** 删除层级 */
    Boolean deleteLevel(Integer id);

    /** 代理分页列表（联用户/层级/上级昵称） */
    CommonPage<StockAgent> getAdminAgentList(String keywords, Integer levelId, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 后台新增/修改代理 */
    Boolean saveAgent(StockAgentRequest request);

    /** 删除代理 */
    Boolean deleteAgent(Integer id);

    /** 启用/禁用代理 */
    Boolean changeAgentStatus(Integer id, Integer status);

    /** 商品列表（含库存与各层级拿货价，仅已加入订货的商品） */
    HashMap<String, Object> getProductList(String keywords, com.zbkj.common.request.PageParamRequest page);

    /** 已加入订货模块的商品关联列表 */
    java.util.List<com.zbkj.common.model.stock.StockProductRel> getStockProductRelList();

    /** 可添加商品列表（尚未加入订货的商品，供选择添加） */
    HashMap<String, Object> getSelectableProductList(String keywords, com.zbkj.common.request.PageParamRequest page);

    /** 批量添加商品到订货模块 */
    Boolean addProducts(java.util.List<Integer> productIds);

    /** 从订货模块移除商品 */
    Boolean removeStockProduct(Integer productId);

    /** 保存商品层级拿货价 */
    Boolean savePrice(StockRequests.StockPriceSetRequest request);

    /** 手动调整库存（记日志） */
    Boolean adjustStock(StockRequests.StockAdjustRequest request, Integer adminId);

    /** 库存变动日志 */
    CommonPage<StockLog> getLogList(Integer productId, Integer type, com.zbkj.common.request.PageParamRequest page);

    // ==================== 会员端 ====================

    /** 我的代理身份（含层级名、上级昵称） */
    HashMap<String, Object> getMyAgentInfo(Integer uid);

    /** 新增下级代理（按手机号绑定已注册用户） */
    Boolean createSubAgent(Integer uid, StockAgentCreateRequest request);

    /** 我的下级代理列表 */
    List<StockAgent> getSubAgentList(Integer uid);

    /** 可选层级列表（代理端创建下级时：只能选比自己低的层级） */
    List<StockLevel> getLevelListForAgent(Integer uid);

    // ==================== 公共 ====================

    /** 根据UID取订货代理身份（含层级） */
    StockAgent getAgentByUid(Integer uid);

    /** 根据代理ID取代理 */
    StockAgent getAgentById(Integer agentId);

    /** 某代理对某商品的拿货价（价格表优先 -> 层级默认折扣 -> 零售价） */
    java.math.BigDecimal getProductPrice(StockAgent agent, Integer productId);

    /** 扣减库存（乐观锁 stock>=num），失败抛出 CrmebException */
    void deductStock(Integer productId, Integer num, Integer type, String linkNo, String mark);

    /** 回补库存 */
    void addStock(Integer productId, Integer num, Integer type, String linkNo, String mark);

    /** 收集某代理的全部下级代理ID（含间接，BFS） */
    List<Integer> collectSubAgentIds(Integer agentId);

    /** 某代理的直接下级代理列表 */
    List<StockAgent> getDirectChildren(Integer agentId);

    /** 级差阶梯列表 */
    List<com.zbkj.common.model.stock.StockLadder> getLadderList();

    /** 保存级差阶梯（全量覆盖） */
    Boolean saveLadders(List<com.zbkj.common.model.stock.StockLadder> ladders);

    /** 代理ID -> uid 映射 */
    Map<Integer, Integer> getAgentUidMap(List<Integer> agentIds);

    /**
     * 校验并自动升级订货商层级
     * 依据 eb_stock_level 上的四项条件（自购消费 / 直推订单业绩 / 团队伞下业绩 / 购买指定产品）
     * 与条件组合方式（0=或 1=与）判断是否可升到更高层级，可升则更新并返回 true
     */
    Boolean checkAndUpgrade(Integer uid);

    /** 订货商变更记录列表（联查昵称/手机号） */
    CommonPage<com.zbkj.common.model.stock.StockChangeLog> getChangeLogList(
            Integer uid, Integer type, com.zbkj.common.request.PageParamRequest page);

    /** 写入订货商变更记录 */
    void logChange(Integer agentId, Integer uid, Integer type, String oldValue, String newValue, String mark);
}
