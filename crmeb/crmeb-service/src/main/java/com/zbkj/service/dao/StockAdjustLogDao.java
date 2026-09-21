package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.stock.StockAdjustLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 订货系统-订货商库存调整记录 Dao
 */
public interface StockAdjustLogDao extends BaseMapper<StockAdjustLog> {

    /**
     * 订货商库存变动台账（统一视图，分页交给 PageHelper）：
     * 「后台手工调整流水」(eb_stock_adjust_log) UNION「订单推导出的实体库存变动」。
     *
     * 背景：实体库存是【推导式】的 —— 自购已付款实体单 − 供应给下级 − 线下销售 + 后台调整 + 换货净额，
     * 这些订单类变动从来不落 eb_stock_adjust_log，导致会员端「库存记录」看不到采购入库、供货出库等留痕。
     * 本查询把它们拼成同一张台账，过滤口径与 StockOrderServiceImpl 的实体库存推导保持一致
     * （physicalPurchaseWrapper / exchangeStockDeltaMap / exchangePendingDeltaMap），
     * 从而「台账合计」永远等于「实体可供应量」。
     *
     * 只列出实体库存(stockType=1)相关来源；虚拟库存的转卖已在 eb_stock_adjust_log 里留痕。
     *
     * @param agentId   订货商ID
     * @param stockType 1=实体 2=虚拟 null/0=全部（按行自身类型过滤）
     */
    @Select("<script>"
            + "SELECT t.* FROM ("
            + "  SELECT a.id AS id, a.agent_id AS agentId, a.uid AS uid, a.link_uid AS linkUid,"
            + "         a.product_id AS productId, a.sku_key AS skuKey, a.stock_type AS stockType,"
            + "         a.num AS num, a.mark AS mark, a.create_time AS createTime, NULL AS orderNo"
            + "    FROM eb_stock_adjust_log a"
            + "   WHERE a.is_del = 0 AND a.agent_id = #{agentId}"
            + "  UNION ALL"
            + "  SELECT NULL, o.agent_id, o.uid, 0, p.product_id, IFNULL(p.sku_key, ''), 1,"
            + "         p.num, CONCAT('采购入库（订单 ', o.order_no, '）'), o.create_time, o.order_no"
            + "    FROM eb_stock_order o JOIN eb_stock_order_product p ON p.order_id = o.id"
            + "   WHERE o.is_del = 0 AND o.pay_status = 1 AND o.order_type = 1"
            + "     AND (o.stock_type = 1 OR o.stock_type IS NULL) AND o.status NOT IN (-1, -2)"
            + "     AND o.agent_id = #{agentId}"
            + "  UNION ALL"
            + "  SELECT NULL, o.parent_agent_id, o.uid, o.uid, p.product_id, IFNULL(p.sku_key, ''), 1,"
            + "         -p.num, CONCAT('供货出库（订单 ', o.order_no, '）'), o.create_time, o.order_no"
            + "    FROM eb_stock_order o JOIN eb_stock_order_product p ON p.order_id = o.id"
            + "   WHERE o.is_del = 0 AND o.pay_status = 1 AND o.order_type = 1"
            + "     AND (o.stock_type = 1 OR o.stock_type IS NULL) AND o.status NOT IN (-1, -2)"
            + "     AND o.parent_agent_id = #{agentId}"
            + "  UNION ALL"
            + "  SELECT NULL, s.agent_id, s.uid, 0, s.product_id, s.sku_key, 1, -s.num,"
            + "         CONCAT('线下销售出库', IF(s.mark IS NULL OR s.mark = '', '', CONCAT('（', s.mark, '）'))),"
            + "         s.create_time, NULL"
            + "    FROM eb_stock_offline_sale s"
            + "   WHERE s.is_del = 0 AND s.agent_id = #{agentId}"
            + "  UNION ALL"
            + "  SELECT NULL, e.agent_id, e.uid, 0, e.product_id, IFNULL(e.sku_key, ''), 1, -e.num,"
            + "         CONCAT('换货占用（换货单 ', e.exchange_no, '，待审核/退回/发货）'),"
            + "         e.create_time, e.exchange_no"
            + "    FROM eb_stock_exchange e"
            + "   WHERE e.is_del = 0 AND e.status IN (0, 1, 2, 3) AND e.num > 0 AND e.agent_id = #{agentId}"
            + "     AND (e.exchange_type IS NULL OR e.exchange_type != 1)"
            + "  UNION ALL"
            + "  SELECT NULL, e.agent_id, e.uid, 0, e.product_id, IFNULL(e.sku_key, ''), 1, -e.num,"
            + "         CONCAT('换货换出（换货单 ', e.exchange_no, '）'),"
            + "         IFNULL(e.finish_time, e.create_time), e.exchange_no"
            + "    FROM eb_stock_exchange e"
            + "   WHERE e.is_del = 0 AND e.status = 4 AND e.num > 0 AND e.agent_id = #{agentId}"
            + "     AND (e.exchange_type IS NULL OR e.exchange_type != 1)"
            + "  UNION ALL"
            + "  SELECT NULL, e.agent_id, e.uid, 0, e.target_product_id, IFNULL(e.target_sku_key, ''), 1, e.num,"
            + "         CONCAT('换货换入（换货单 ', e.exchange_no, '）'),"
            + "         IFNULL(e.finish_time, e.create_time), e.exchange_no"
            + "    FROM eb_stock_exchange e"
            + "   WHERE e.is_del = 0 AND e.status = 4 AND e.target_product_id > 0 AND e.num > 0"
            + "     AND e.agent_id = #{agentId}"
            + "     AND (e.target_stock_type IS NULL OR e.target_stock_type = 1)"
            + "  UNION ALL"
            + "  SELECT NULL, e.parent_agent_id, e.uid, e.uid, e.target_product_id, IFNULL(e.target_sku_key, ''), 1, -e.num,"
            + "         CONCAT('换货发出新品（下级换货单 ', e.exchange_no, '）'),"
            + "         IFNULL(e.send_time, IFNULL(e.finish_time, e.create_time)), e.exchange_no"
            + "    FROM eb_stock_exchange e"
            + "   WHERE e.is_del = 0 AND e.status = 4 AND e.parent_agent_id = #{agentId}"
            + "     AND e.target_product_id > 0 AND e.num > 0"
            + "     AND (e.target_stock_type IS NULL OR e.target_stock_type = 1)"
            + ") t"
            + "<if test='stockType != null and stockType > 0'> WHERE t.stockType = #{stockType} </if>"
            + " ORDER BY t.createTime DESC, t.stockType ASC, t.productId ASC"
            + "</script>")
    List<HashMap<String, Object>> selectAgentLedger(@Param("agentId") Integer agentId,
                                                     @Param("stockType") Integer stockType);
}
