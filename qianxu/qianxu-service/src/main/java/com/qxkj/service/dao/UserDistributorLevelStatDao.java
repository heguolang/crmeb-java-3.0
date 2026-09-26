package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.user.UserDistributorLevelStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销商等级统计 Mapper 接口
 */
public interface UserDistributorLevelStatDao extends BaseMapper<UserDistributorLevelStat> {

    /**
     * 原子累加统计字段（并发安全），下限 0
     *
     * @param column 列名，仅允许白名单值
     * @param delta  增量，可为负
     */
    @Update("UPDATE eb_user_distributor_level_stat SET ${column} = GREATEST(${column} + #{delta}, 0) WHERE uid = #{uid}")
    int incrColumn(@Param("uid") Integer uid, @Param("column") String column, @Param("delta") BigDecimal delta);

    /**
     * 直推会员人数：直接推荐的下级数量
     */
    @Select("SELECT COUNT(*) FROM eb_user WHERE spread_uid = #{uid}")
    int countDirectUsers(@Param("uid") Integer uid);

    /**
     * 团队会员人数：整条推荐链的所有下级数量（递归 CTE，depth 上限 50）
     */
    @Select("WITH RECURSIVE team_chain(uid, depth) AS (" +
            " SELECT uid, 1 FROM eb_user WHERE spread_uid = #{uid}" +
            " UNION ALL" +
            " SELECT u.uid, tc.depth + 1 FROM eb_user u INNER JOIN team_chain tc ON u.spread_uid = tc.uid WHERE tc.depth < 50" +
            ") SELECT COUNT(*) FROM team_chain")
    int countTeamUsers(@Param("uid") Integer uid);

    /**
     * 直推中达到指定分销商等级的人数（eb_user.distributor_level_id = eb_distributor_level.id）
     */
    @Select("SELECT COUNT(*) FROM eb_user WHERE spread_uid = #{uid} AND distributor_level_id = #{levelId}")
    int countDirectLevelUsers(@Param("uid") Integer uid, @Param("levelId") Integer levelId);

    /**
     * 本人已支付订单中包含的指定商品种数（任一命中即 > 0）
     * 注意：订单明细表是 eb_store_order_info（不是 eb_store_order_product）
     */
    @Select("<script>SELECT COUNT(DISTINCT op.product_id) FROM eb_store_order o" +
            " INNER JOIN eb_store_order_info op ON op.order_id = o.id" +
            " WHERE o.uid = #{uid} AND o.paid = 1 AND o.is_del = 0 AND o.refund_status != 2" +
            " AND op.product_id IN <foreach collection='productIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach></script>")
    int countSelfPaidProductOrders(@Param("uid") Integer uid, @Param("productIds") List<Integer> productIds);

    /**
     * 累计充值额（已支付），实时聚合，不落表
     */
    @Select("SELECT IFNULL(SUM(price), 0) FROM eb_user_recharge WHERE uid = #{uid} AND paid = 1")
    BigDecimal sumRechargeAmount(@Param("uid") Integer uid);

    /**
     * 全量重算：本人已支付且未退款订单总额
     */
    @Select("SELECT IFNULL(SUM(pay_price), 0) FROM eb_store_order " +
            "WHERE uid = #{uid} AND paid = 1 AND is_del = 0 AND refund_status <> 2")
    BigDecimal sumSelfOrderAmount(@Param("uid") Integer uid);

    /**
     * 全量重算：一级下级已支付且未退款订单总额
     */
    @Select("SELECT IFNULL(SUM(o.pay_price), 0) FROM eb_store_order o " +
            "INNER JOIN eb_user u ON u.uid = o.uid " +
            "WHERE u.spread_uid = #{uid} AND o.paid = 1 AND o.is_del = 0 AND o.refund_status <> 2")
    BigDecimal sumDirectOrderAmount(@Param("uid") Integer uid);

    /**
     * 全量重算：一级下级中已分配会员等级(level>0)的订单总额
     */
    @Select("SELECT IFNULL(SUM(o.pay_price), 0) FROM eb_store_order o " +
            "INNER JOIN eb_user u ON u.uid = o.uid " +
            "WHERE u.spread_uid = #{uid} AND u.level > 0 AND o.paid = 1 AND o.is_del = 0 AND o.refund_status <> 2")
    BigDecimal sumDirectUserOrderAmount(@Param("uid") Integer uid);

    /**
     * 周期清零：把所有统计金额归零（由配置项控制是否启用，默认不启用）
     */
    @Update("UPDATE eb_user_distributor_level_stat SET total_consume_amount = 0, direct_consume_amount = 0, " +
            "direct_user_consume_amount = 0, team_product_amount = 0")
    int resetAllAmount();

    /**
     * 全量重算：团队（整条推荐链下级）已支付且未退款订单总额
     */
    @Select("WITH RECURSIVE team_chain(uid, depth) AS (" +
            " SELECT uid, 1 FROM eb_user WHERE spread_uid = #{uid}" +
            " UNION ALL" +
            " SELECT u.uid, tc.depth + 1 FROM eb_user u INNER JOIN team_chain tc ON u.spread_uid = tc.uid WHERE tc.depth < 50" +
            ") SELECT IFNULL(SUM(o.pay_price), 0) FROM eb_store_order o " +
            "INNER JOIN team_chain tc ON o.uid = tc.uid " +
            "WHERE o.paid = 1 AND o.is_del = 0 AND o.refund_status <> 2")
    BigDecimal sumTeamOrderAmount(@Param("uid") Integer uid);
}
