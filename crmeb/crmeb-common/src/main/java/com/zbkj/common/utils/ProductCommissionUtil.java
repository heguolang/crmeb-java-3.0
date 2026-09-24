package com.zbkj.common.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.zbkj.common.model.agent.Agent;
import com.zbkj.common.model.product.ProductCommissionConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 商品级佣金解析工具：
 * null=取全局；0=该商品无此项；其它=用商品配置。
 */
public final class ProductCommissionUtil {

    private ProductCommissionUtil() {
    }

    public static ProductCommissionConfig parse(String json) {
        if (StrUtil.isBlank(json)) {
            return new ProductCommissionConfig();
        }
        try {
            ProductCommissionConfig cfg = JSON.parseObject(json, ProductCommissionConfig.class);
            return cfg == null ? new ProductCommissionConfig() : cfg;
        } catch (Exception e) {
            return new ProductCommissionConfig();
        }
    }

    public static String toJson(ProductCommissionConfig cfg) {
        if (cfg == null) {
            return null;
        }
        return JSON.toJSONString(cfg);
    }

    /**
     * 是否配置了「覆盖值」（含显式 0）。未配置返回 false → 走全局。
     */
    public static boolean hasOverride(BigDecimal amount, BigDecimal rate) {
        return amount != null || rate != null;
    }

    /**
     * 按「金额优先，否则比例」计算佣金。
     * @param unitPrice 单价（用于比例）
     * @param payNum 数量
     * @return null 表示未覆盖（应走全局）；ZERO 表示显式关闭
     */
    public static BigDecimal calcOverride(BigDecimal amount, BigDecimal rate, BigDecimal unitPrice, int payNum) {
        if (!hasOverride(amount, rate)) {
            return null;
        }
        int num = Math.max(payNum, 1);
        if (amount != null) {
            return amount.multiply(new BigDecimal(num)).setScale(2, RoundingMode.DOWN);
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = ObjectUtil.defaultIfNull(unitPrice, BigDecimal.ZERO);
        return price.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
                .multiply(new BigDecimal(num));
    }

    /**
     * 仅解析比例覆盖：null=未覆盖；其它（含0）=商品比例。
     */
    public static BigDecimal resolveRateOrNull(BigDecimal rate) {
        return rate;
    }

    /**
     * 开关：null/true=跟随全局；false=该商品关闭该项。
     */
    public static boolean resolveEnabled(Boolean productEnabled, boolean globalEnabled) {
        if (Boolean.FALSE.equals(productEnabled)) {
            return false;
        }
        return globalEnabled;
    }

    /** 按代理级别取商品配置的金额/比例 */
    public static BigDecimal[] agentAmountRate(ProductCommissionConfig.Agent agentCfg, Integer level) {
        if (agentCfg == null || level == null) {
            return new BigDecimal[]{null, null};
        }
        if (Agent.LEVEL_PROVINCE.equals(level)) {
            return new BigDecimal[]{agentCfg.getProvinceAmount(), agentCfg.getProvinceRate()};
        }
        if (Agent.LEVEL_CITY.equals(level)) {
            return new BigDecimal[]{agentCfg.getCityAmount(), agentCfg.getCityRate()};
        }
        if (Agent.LEVEL_DISTRICT.equals(level)) {
            return new BigDecimal[]{agentCfg.getDistrictAmount(), agentCfg.getDistrictRate()};
        }
        return new BigDecimal[]{null, null};
    }

    public static BigDecimal[] distributorAmountRate(ProductCommissionConfig.Distributor d, int brokerageLevel) {
        if (d == null) {
            return new BigDecimal[]{null, null};
        }
        if (brokerageLevel == 1) {
            return new BigDecimal[]{d.getDirectAmount(), d.getDirectRate()};
        }
        if (brokerageLevel == 2) {
            return new BigDecimal[]{d.getIndirectAmount(), d.getIndirectRate()};
        }
        return new BigDecimal[]{null, null};
    }

    /**
     * 按分销商等级 + 返佣层级取商品配置的金额/比例（等级配置优先于一刀切）。
     * @param brokerageLevel 0=自购 1=一级(直属) 2=二级(间接)
     */
    public static BigDecimal[] distributorLevelAmountRate(ProductCommissionConfig.Distributor d,
                                                          Integer levelId, int brokerageLevel) {
        ProductCommissionConfig.DistributorLevel lv = findDistributorLevel(
                d == null ? null : d.getLevels(), levelId);
        if (lv == null) {
            return new BigDecimal[]{null, null};
        }
        if (brokerageLevel == 1) {
            return new BigDecimal[]{lv.getOneAmount(), lv.getOneRate()};
        }
        if (brokerageLevel == 2) {
            return new BigDecimal[]{lv.getTwoAmount(), lv.getTwoRate()};
        }
        return new BigDecimal[]{lv.getSelfAmount(), lv.getSelfRate()};
    }

    /**
     * 按团队等级取商品配置的团队奖金额/比例。
     * @param diff true=极差奖 false=平级奖
     */
    public static BigDecimal[] teamLevelAmountRate(ProductCommissionConfig.Team t,
                                                   Integer levelId, boolean diff) {
        ProductCommissionConfig.TeamLevel lv = findTeamLevel(t == null ? null : t.getLevels(), levelId);
        if (lv == null) {
            return new BigDecimal[]{null, null};
        }
        return diff
                ? new BigDecimal[]{lv.getDiffAmount(), lv.getDiffRate()}
                : new BigDecimal[]{lv.getPeerAmount(), lv.getPeerRate()};
    }

    /** 按 levelId 在分销商等级覆盖列表中查找 */
    private static ProductCommissionConfig.DistributorLevel findDistributorLevel(
            List<ProductCommissionConfig.DistributorLevel> levels, Integer levelId) {
        if (levels == null || levels.isEmpty() || levelId == null || levelId <= 0) {
            return null;
        }
        for (ProductCommissionConfig.DistributorLevel lv : levels) {
            if (lv != null && levelId.equals(lv.getLevelId())) {
                return lv;
            }
        }
        return null;
    }

    /** 按 levelId 在团队等级覆盖列表中查找 */
    private static ProductCommissionConfig.TeamLevel findTeamLevel(
            List<ProductCommissionConfig.TeamLevel> levels, Integer levelId) {
        if (levels == null || levels.isEmpty() || levelId == null || levelId <= 0) {
            return null;
        }
        for (ProductCommissionConfig.TeamLevel lv : levels) {
            if (lv != null && levelId.equals(lv.getLevelId())) {
                return lv;
            }
        }
        return null;
    }

    /**
     * 是否对门店服务费做了商品级配置（开关非跟随，或填写了金额/比例）。
     */
    public static boolean isStoreFeeConfigured(ProductCommissionConfig.FeeItem item) {
        if (item == null) {
            return false;
        }
        if (item.getEnabled() != null) {
            return true;
        }
        return hasOverride(item.getAmount(), item.getRate());
    }

    /**
     * 解析商品级门店服务费。
     * <ul>
     *   <li>enabled=false → 0</li>
     *   <li>填写了金额/比例 → 按金额优先计算（与佣金 calcOverride 一致）</li>
     *   <li>其余（跟随门店默认）→ storeDefaultFee</li>
     * </ul>
     *
     * @param unitPrice 单价（比例计费用）；可为 null
     * @param payNum    数量
     */
    public static BigDecimal resolveStoreServiceFee(ProductCommissionConfig.FeeItem item,
                                                    BigDecimal storeDefaultFee,
                                                    BigDecimal unitPrice,
                                                    int payNum) {
        BigDecimal storeFee = ObjectUtil.defaultIfNull(storeDefaultFee, BigDecimal.ZERO);
        if (item == null) {
            return storeFee;
        }
        if (Boolean.FALSE.equals(item.getEnabled())) {
            return BigDecimal.ZERO;
        }
        if (hasOverride(item.getAmount(), item.getRate())) {
            BigDecimal override = calcOverride(item.getAmount(), item.getRate(), unitPrice, payNum);
            return override == null ? BigDecimal.ZERO : override;
        }
        return storeFee;
    }

    /**
     * 分佣用单价：若商品配置「积分抵扣金额不参与分佣」，则从单价中扣减本行积分抵扣均摊金额。
     */
    public static BigDecimal brokerageUnitPrice(com.zbkj.common.vo.OrderInfoDetailVo info) {
        if (info == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal unit = ObjectUtil.defaultIfNull(info.getVipPrice(), info.getPrice());
        if (unit == null) {
            return BigDecimal.ZERO;
        }
        if (!Boolean.FALSE.equals(info.getIsIntegralDeductBrokerage())) {
            return unit;
        }
        BigDecimal lineDeduct = ObjectUtil.defaultIfNull(info.getLineDeductionPrice(), BigDecimal.ZERO);
        if (lineDeduct.compareTo(BigDecimal.ZERO) <= 0) {
            return unit;
        }
        int payNum = Math.max(ObjectUtil.defaultIfNull(info.getPayNum(), 1), 1);
        BigDecimal perUnit = lineDeduct.divide(new BigDecimal(payNum), 2, RoundingMode.HALF_UP);
        BigDecimal effective = unit.subtract(perUnit);
        return effective.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : effective;
    }
}
