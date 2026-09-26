package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.product.ProductCommissionConfig;
import com.qxkj.common.model.product.StoreProduct;
import com.qxkj.common.model.system.SystemTeamLevel;
import com.qxkj.common.model.system.SystemTeamLevelConfig;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.ProductCommissionUtil;
import com.qxkj.common.vo.StoreOrderInfoOldVo;
import com.qxkj.service.service.StoreOrderInfoService;
import com.qxkj.service.service.StoreProductService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.SystemTeamLevelConfigService;
import com.qxkj.service.service.SystemTeamLevelService;
import com.qxkj.service.service.TeamBrokerageService;
import com.qxkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 团队奖分润：沿推荐链向上按团队极差比例递扣分配，平级时按各等级平级奖配置补发
 */
@Service
public class TeamBrokerageServiceImpl implements TeamBrokerageService {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemTeamLevelConfigService systemTeamLevelConfigService;

    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreProductService storeProductService;

    @Override
    public List<UserBrokerageRecord> assignTeamBrokerage(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return CollUtil.newArrayList();
        }
        String status = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_STATUS);
        if (StrUtil.isBlank(status) || "0".equals(status)) {
            return CollUtil.newArrayList();
        }
        User buyer = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(buyer)) {
            return CollUtil.newArrayList();
        }
        Integer spreadUid = buyer.getSpreadUid();
        if (ObjectUtil.isNull(spreadUid) || spreadUid <= 0 || spreadUid.equals(storeOrder.getUid())) {
            return CollUtil.newArrayList();
        }

        String frozenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);
        int frozenDays = Integer.parseInt(Optional.ofNullable(frozenTime).orElse("0"));

        // 1) 先沿推荐链向上收集有团队等级的上级（同一订单各商品共用同一条链）
        List<TeamChainNode> chain = buildChain(spreadUid);
        if (chain.isEmpty()) {
            return CollUtil.newArrayList();
        }
        List<StoreOrderInfoOldVo> orderInfoVoList = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        if (CollUtil.isEmpty(orderInfoVoList)) {
            return CollUtil.newArrayList();
        }

        // 2) 逐商品沿链做极差分配：
        //    商品级「比例」覆盖 = 替代该等级的全局团队极差比例，仍参与极差差额累计（不重复发放）；
        //    商品级「金额」    = 该商品该等级的固定佣金，不参与差额累计；商品开关 false = 该商品不参与团队奖。
        Map<Integer, BigDecimal> diffByUid = new LinkedHashMap<>();
        Map<Integer, BigDecimal> peerByUid = new LinkedHashMap<>();
        Map<Integer, Set<String>> diffRateHint = new LinkedHashMap<>();
        for (StoreOrderInfoOldVo orderInfoVo : orderInfoVoList) {
            if (ObjectUtil.isNull(orderInfoVo.getInfo())) {
                continue;
            }
            Integer productId = ObjectUtil.defaultIfNull(orderInfoVo.getProductId(), orderInfoVo.getInfo().getProductId());
            StoreProduct product = ObjectUtil.isNotNull(productId) ? storeProductService.getById(productId) : null;
            ProductCommissionConfig.Team teamCfg = ProductCommissionUtil.parse(
                    ObjectUtil.isNotNull(product) ? product.getCommissionConfig() : null).getTeam();
            if (Boolean.FALSE.equals(teamCfg.getEnabled())) {
                continue;
            }
            BigDecimal unitPrice = ProductCommissionUtil.brokerageUnitPrice(orderInfoVo.getInfo());
            if (ObjectUtil.isNull(unitPrice) || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            int payNum = Math.max(ObjectUtil.defaultIfNull(orderInfoVo.getInfo().getPayNum(), 1), 1);

            BigDecimal accumulatedRate = BigDecimal.ZERO;
            for (TeamChainNode node : chain) {
                BigDecimal myRate = new BigDecimal(ObjectUtil.defaultIfNull(node.config.getTeamBrokerageRate(), 0));
                BigDecimal[] diffOverride = ProductCommissionUtil.teamLevelAmountRate(teamCfg, node.teamLevelId, true);
                if (diffOverride[0] != null) {
                    // 固定金额极差奖：不参与差额累计，直接作为该等级在本商品的固定佣金
                    BigDecimal amount = diffOverride[0].multiply(new BigDecimal(payNum)).setScale(2, RoundingMode.DOWN);
                    if (amount.signum() > 0) {
                        diffByUid.merge(node.uid, amount, BigDecimal::add);
                        diffRateHint.computeIfAbsent(node.uid, k -> new LinkedHashSet<>()).add("固定" + strip(diffOverride[0]) + "元");
                    }
                    continue;
                }
                if (diffOverride[1] != null) {
                    // 比例覆盖：替换该等级比例，继续走差额算法
                    myRate = diffOverride[1];
                }
                int compare = myRate.compareTo(accumulatedRate);
                if (compare > 0) {
                    BigDecimal diffRate = myRate.subtract(accumulatedRate);
                    BigDecimal brokerage = commissionByRate(unitPrice, payNum, diffRate);
                    if (brokerage.signum() > 0) {
                        diffByUid.merge(node.uid, brokerage, BigDecimal::add);
                        diffRateHint.computeIfAbsent(node.uid, k -> new LinkedHashSet<>()).add(strip(diffRate));
                    }
                    accumulatedRate = myRate;
                } else if (compare == 0) {
                    BigDecimal[] peerOverride = ProductCommissionUtil.teamLevelAmountRate(teamCfg, node.teamLevelId, false);
                    BigDecimal peerAmount = ProductCommissionUtil.calcOverride(
                            peerOverride[0], peerOverride[1], unitPrice, payNum);
                    if (peerAmount == null) {
                        BigDecimal peerRate = new BigDecimal(ObjectUtil.defaultIfNull(node.config.getPeerAwardRate(), 0));
                        peerAmount = commissionByRate(unitPrice, payNum, peerRate);
                    }
                    if (peerAmount.signum() > 0) {
                        peerByUid.merge(node.uid, peerAmount, BigDecimal::add);
                    }
                }
            }
        }

        // 3) 汇总生成佣金记录（同类型的多个商品合并为一笔）
        List<UserBrokerageRecord> recordList = new ArrayList<>();
        Map<Integer, TeamChainNode> nodeByUid = new LinkedHashMap<>();
        for (TeamChainNode node : chain) {
            nodeByUid.putIfAbsent(node.uid, node);
        }
        for (Map.Entry<Integer, BigDecimal> entry : diffByUid.entrySet()) {
            if (entry.getValue().signum() <= 0) {
                continue;
            }
            TeamChainNode node = nodeByUid.get(entry.getKey());
            String rateText = StrUtil.join("/", diffRateHint.getOrDefault(entry.getKey(), new LinkedHashSet<>()));
            recordList.add(buildRecord(node.uid, entry.getValue(), frozenDays,
                    BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_DIFF,
                    BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF,
                    StrUtil.format("获得团队极差奖，团等级【{}】极差{}%，分佣{}",
                            node.teamLevelName, rateText, entry.getValue())));
        }
        for (Map.Entry<Integer, BigDecimal> entry : peerByUid.entrySet()) {
            if (entry.getValue().signum() <= 0) {
                continue;
            }
            TeamChainNode node = nodeByUid.get(entry.getKey());
            recordList.add(buildRecord(node.uid, entry.getValue(), frozenDays,
                    BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_PEER,
                    BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_PEER,
                    StrUtil.format("获得团队平级奖，团等级【{}】分佣{}", node.teamLevelName, entry.getValue())));
        }
        return recordList;
    }

    /** 沿推荐链向上收集有效节点（有团队等级且已配置团队奖），受团队奖追溯层数限制 */
    private List<TeamChainNode> buildChain(Integer spreadUid) {
        List<TeamChainNode> chain = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Integer currentUid = spreadUid;
        int depth = 0;
        int maxDepth = getMaxDepth();
        while (ObjectUtil.isNotNull(currentUid) && currentUid > 0) {
            if (!visited.add(currentUid)) {
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                break;
            }
            User upline = userService.getById(currentUid);
            if (ObjectUtil.isNull(upline)) {
                break;
            }
            Integer teamLevelId = ObjectUtil.defaultIfNull(upline.getTeamLevel(), 0);
            if (teamLevelId > 0) {
                SystemTeamLevelConfig config = systemTeamLevelConfigService.getByTeamLevelId(teamLevelId);
                if (ObjectUtil.isNotNull(config)) {
                    SystemTeamLevel teamLevel = systemTeamLevelService.getById(teamLevelId);
                    chain.add(new TeamChainNode(upline.getUid(), teamLevelId, config,
                            ObjectUtil.isNotNull(teamLevel) ? teamLevel.getName() : ""));
                }
            }
            currentUid = upline.getSpreadUid();
        }
        return chain;
    }

    /** 按比例（百分数）计算单行佣金，与全局团队奖算法保持一致 */
    private BigDecimal commissionByRate(BigDecimal unitPrice, int payNum, BigDecimal ratePercent) {
        if (ObjectUtil.isNull(unitPrice) || ObjectUtil.isNull(ratePercent) || ratePercent.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rateDecimal = ratePercent.divide(new BigDecimal("100"), 4, RoundingMode.DOWN);
        BigDecimal line = unitPrice.multiply(rateDecimal).setScale(2, RoundingMode.DOWN);
        if (line.signum() > 0 && payNum > 1) {
            line = line.multiply(new BigDecimal(payNum));
        }
        return line;
    }

    private String strip(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    /** 推荐链节点：领奖上级 + 其团队等级与团队奖配置 */
    private static class TeamChainNode {
        private final Integer uid;
        private final Integer teamLevelId;
        private final SystemTeamLevelConfig config;
        private final String teamLevelName;

        private TeamChainNode(Integer uid, Integer teamLevelId, SystemTeamLevelConfig config, String teamLevelName) {
            this.uid = uid;
            this.teamLevelId = teamLevelId;
            this.config = config;
            this.teamLevelName = teamLevelName;
        }
    }

    private UserBrokerageRecord buildRecord(Integer uid, BigDecimal brokerage, int frozenDays,
                                            String title, Integer brokerageLevel, String mark) {
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(uid);
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setPrice(brokerage);
        record.setMark(mark);
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
        record.setFrozenTime(frozenDays);
        record.setCreateTime(QianxuDateUtil.nowDateTime());
        record.setBrokerageLevel(brokerageLevel);
        return record;
    }

    private int getMaxDepth() {
        String maxDepthStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_MAX_DEPTH);
        try {
            int v = Integer.parseInt(ObjectUtil.defaultIfNull(maxDepthStr, "0"));
            return v <= 0 ? 0 : Math.min(v, 200);
        } catch (Exception e) {
            return 0;
        }
    }
}
