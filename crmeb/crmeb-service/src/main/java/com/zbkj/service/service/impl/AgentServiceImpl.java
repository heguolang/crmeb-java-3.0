package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.agent.Agent;
import com.zbkj.common.model.agent.AgentChangeLog;
import com.zbkj.common.model.agent.AgentReward;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.product.ProductCommissionConfig;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AgentAdminRequest;
import com.zbkj.common.request.AgentApplyRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.ProductCommissionUtil;
import com.zbkj.common.vo.StoreOrderInfoOldVo;
import com.zbkj.service.dao.AgentChangeLogDao;
import com.zbkj.service.dao.AgentDao;
import com.zbkj.service.dao.AgentRewardDao;
import com.zbkj.service.service.AgentService;
import com.zbkj.service.service.StoreOrderInfoService;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserBrokerageRecordService;
import com.zbkj.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域代理服务实现
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Autowired
    private AgentDao agentDao;

    @Autowired
    private AgentRewardDao agentRewardDao;

    @Autowired
    private AgentChangeLogDao agentChangeLogDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreProductService storeProductService;

    // ==================== 后台 ====================

    @Override
    public CommonPage<Agent> getAdminList(String keywords, Integer level, Integer status, PageParamRequest pageParamRequest) {
        Page<Agent> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getIsDel, 0);
        if (ObjectUtil.isNotNull(level) && level > 0) {
            wrapper.eq(Agent::getLevel, level);
        }
        if (ObjectUtil.isNotNull(status)) {
            wrapper.eq(Agent::getStatus, status);
        }
        if (StrUtil.isNotBlank(keywords)) {
            // 关键词：UID 或 区域名称模糊
            if (keywords.matches("^\\d+$")) {
                wrapper.and(w -> w.eq(Agent::getUid, Integer.valueOf(keywords))
                        .or().like(Agent::getRegionName, keywords));
            } else {
                wrapper.like(Agent::getRegionName, keywords);
            }
        }
        wrapper.orderByDesc(Agent::getId);
        List<Agent> list = agentDao.selectList(wrapper);
        fillUserInfo(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveAdminAgent(AgentAdminRequest request) {
        validateRegion(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict());
        User user = userService.getById(request.getUid());
        if (ObjectUtil.isNull(user)) {
            throw new CrmebException("代理用户不存在");
        }
        // 区域唯一校验
        checkRegionConflict(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict(), null);

        Agent agent = new Agent();
        agent.setUid(request.getUid());
        agent.setLevel(request.getLevel());
        agent.setProvince(request.getProvince());
        agent.setCity(StrUtil.nullToEmpty(request.getCity()));
        agent.setDistrict(StrUtil.nullToEmpty(request.getDistrict()));
        agent.setRegionName(buildRegionName(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        agent.setMatchKey(buildMatchKey(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        agent.setRatio(request.getRatio());
        // 后台直接设置默认已通过（也可选待审核）
        agent.setStatus(ObjectUtil.defaultIfNull(request.getStatus(), Agent.STATUS_PASS));
        agent.setApplyMark(StrUtil.nullToEmpty(request.getApplyMark()));
        if (agent.getStatus().equals(Agent.STATUS_PASS)) {
            agent.setCheckTime(new Date());
        }
        agent.setIsDel(0);
        boolean result = agentDao.insert(agent) > 0;
        if (result) {
            logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_ADD, "",
                    agentDesc(agent), "后台新增代理");
        }
        return result;
    }

    @Override
    public Boolean updateAdminAgent(AgentAdminRequest request) {
        if (ObjectUtil.isNull(request.getId())) {
            throw new CrmebException("缺少代理ID");
        }
        Agent agent = agentDao.selectById(request.getId());
        if (ObjectUtil.isNull(agent) || agent.getIsDel().equals(1)) {
            throw new CrmebException("代理不存在");
        }
        validateRegion(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict());
        checkRegionConflict(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict(), request.getId());

        // 记录变更前快照
        String oldDesc = agentDesc(agent);
        String oldRatio = ratioText(agent.getRatio());
        Integer oldStatus = agent.getStatus();

        agent.setUid(request.getUid());
        agent.setLevel(request.getLevel());
        agent.setProvince(request.getProvince());
        agent.setCity(StrUtil.nullToEmpty(request.getCity()));
        agent.setDistrict(StrUtil.nullToEmpty(request.getDistrict()));
        agent.setRegionName(buildRegionName(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        agent.setMatchKey(buildMatchKey(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        agent.setRatio(request.getRatio());
        if (ObjectUtil.isNotNull(request.getStatus())) {
            agent.setStatus(request.getStatus());
            if (request.getStatus().equals(Agent.STATUS_PASS)) {
                agent.setCheckTime(new Date());
            }
        }
        agent.setApplyMark(StrUtil.nullToEmpty(request.getApplyMark()));
        boolean updated = agentDao.updateById(agent) > 0;
        if (updated) {
            String newDesc = agentDesc(agent);
            if (!newDesc.equals(oldDesc)) {
                logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_LEVEL, oldDesc, newDesc, "后台修改代理级别/区域");
            }
            String newRatio = ratioText(agent.getRatio());
            if (!newRatio.equals(oldRatio)) {
                logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_RATIO, oldRatio, newRatio, "后台修改分成比例");
            }
            if (ObjectUtil.isNotNull(request.getStatus()) && !request.getStatus().equals(oldStatus)) {
                logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_STATUS,
                        statusName(oldStatus), statusName(agent.getStatus()), "后台修改代理状态");
            }
        }
        return updated;
    }

    @Override
    public Boolean auditAgent(Integer id, Integer status) {
        if (!Agent.STATUS_PASS.equals(status) && !Agent.STATUS_FAIL.equals(status)) {
            throw new CrmebException("审核状态不正确");
        }
        Agent agent = agentDao.selectById(id);
        if (ObjectUtil.isNull(agent) || agent.getIsDel().equals(1)) {
            throw new CrmebException("代理不存在");
        }
        if (Agent.STATUS_PASS.equals(status)) {
            checkRegionConflict(agent.getLevel(), agent.getProvince(), agent.getCity(), agent.getDistrict(), id);
        }
        Integer oldStatus = agent.getStatus();
        agent.setStatus(status);
        agent.setCheckTime(new Date());
        boolean result = agentDao.updateById(agent) > 0;
        if (result && !status.equals(oldStatus)) {
            logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_STATUS,
                    statusName(oldStatus), statusName(status), "后台审核代理");
        }
        return result;
    }

    @Override
    public Boolean deleteAgent(Integer id) {
        Agent agent = agentDao.selectById(id);
        if (ObjectUtil.isNull(agent) || agent.getIsDel().equals(1)) {
            throw new CrmebException("代理不存在");
        }
        agent.setIsDel(1);
        boolean result = agentDao.updateById(agent) > 0;
        if (result) {
            logChange(agent.getId(), agent.getUid(), AgentChangeLog.TYPE_DELETE,
                    agentDesc(agent), "", "后台删除代理");
        }
        return result;
    }

    @Override
    public CommonPage<AgentChangeLog> getChangeLogList(Integer uid, Integer type, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<AgentChangeLog> lqw = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(uid) && uid > 0) {
            lqw.eq(AgentChangeLog::getUid, uid);
        }
        if (ObjectUtil.isNotNull(type) && type > 0) {
            lqw.eq(AgentChangeLog::getType, type);
        }
        lqw.orderByDesc(AgentChangeLog::getId);
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<AgentChangeLog> list = agentChangeLogDao.selectList(lqw);
        if (CollUtil.isNotEmpty(list)) {
            List<Integer> uidList = list.stream().map(AgentChangeLog::getUid).distinct().collect(Collectors.toList());
            HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
            list.forEach(e -> {
                User user = userMap.get(e.getUid());
                e.setNickname(ObjectUtil.isNotNull(user) ? user.getNickname() : "-");
                e.setPhone(ObjectUtil.isNotNull(user) ? StrUtil.blankToDefault(user.getPhone(), user.getAccount()) : "-");
            });
        }
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public HashMap<String, Object> getSetting() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_FUNC_STATUS,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_FUNC_STATUS), "1"));
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_STATUS,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_STATUS), "1"));
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_CREDIT_TIMING,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_CREDIT_TIMING), "1"));
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_REGIONS, defaultApplyRegions());
        // 各级别默认奖励比例（%）：代理申请「通过」弹窗的默认值，客户可改后保存
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_PROVINCE,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_PROVINCE), "5"));
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_CITY,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_CITY), "3"));
        map.put(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_DISTRICT,
                ObjectUtil.defaultIfNull(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_DISTRICT), "2"));
        return map;
    }

    /** 会员端可申请的代理区域级别，默认全部（1省级 2市级 3区级） */
    private String defaultApplyRegions() {
        String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_REGIONS);
        if (StrUtil.isBlank(value)) {
            return "1,2,3";
        }
        return value;
    }

    @Override
    public Boolean updateSetting(HashMap<String, Object> settingMap) {
        if (ObjectUtil.isNull(settingMap) || settingMap.isEmpty()) {
            throw new CrmebException("设置内容不能为空");
        }
        for (Map.Entry<String, Object> entry : settingMap.entrySet()) {
            String key = entry.getKey();
            if (!SysConfigConstants.CONFIG_KEY_AGENT_FUNC_STATUS.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_APPLY_STATUS.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_CREDIT_TIMING.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_APPLY_REGIONS.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_PROVINCE.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_CITY.equals(key)
                    && !SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_DISTRICT.equals(key)) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue().toString();
            // 默认奖励比例：只保留数字（0~100），空值跳过不覆盖
            if (SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_PROVINCE.equals(key)
                    || SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_CITY.equals(key)
                    || SysConfigConstants.CONFIG_KEY_AGENT_DEFAULT_RATIO_DISTRICT.equals(key)) {
                if (StrUtil.isBlank(value)) {
                    continue;
                }
                try {
                    java.math.BigDecimal ratio = new java.math.BigDecimal(value.trim());
                    if (ratio.compareTo(java.math.BigDecimal.ZERO) < 0
                            || ratio.compareTo(new java.math.BigDecimal("100")) > 0) {
                        throw new CrmebException("默认奖励比例须在 0~100 之间");
                    }
                    value = ratio.stripTrailingZeros().toPlainString();
                } catch (NumberFormatException e) {
                    throw new CrmebException("默认奖励比例必须是数字");
                }
            }
            if (SysConfigConstants.CONFIG_KEY_AGENT_APPLY_REGIONS.equals(key)) {
                // 只允许 1/2/3 的组合，空则清空（表示不开放任何申请）
                StringBuilder sb = new StringBuilder();
                for (String part : value.replace("，", ",").split(",")) {
                    String p = part.trim();
                    if ("1".equals(p) || "2".equals(p) || "3".equals(p)) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(p);
                    }
                }
                value = sb.toString();
            }
            systemConfigService.updateOrSaveValueByName(key, value);
        }
        return Boolean.TRUE;
    }

    @Override
    public CommonPage<AgentReward> getRewardList(Integer uid, String orderId, Integer status, PageParamRequest pageParamRequest) {
        Page<AgentReward> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<AgentReward> wrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(uid) && uid > 0) {
            wrapper.eq(AgentReward::getUid, uid);
        }
        if (StrUtil.isNotBlank(orderId)) {
            wrapper.eq(AgentReward::getOrderId, orderId);
        }
        if (ObjectUtil.isNotNull(status) && status > 0) {
            wrapper.eq(AgentReward::getStatus, status);
        }
        wrapper.orderByDesc(AgentReward::getId);
        List<AgentReward> list = agentRewardDao.selectList(wrapper);
        fillRewardUserInfo(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    // ==================== 会员端 ====================

    @Override
    public Boolean apply(Integer uid, AgentApplyRequest request) {
        String applyStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_STATUS);
        if (!"1".equals(applyStatus)) {
            throw new CrmebException("当前未开放代理申请");
        }
        // 校验申请的代理区域级别是否在后台配置的开放范围内
        String allowed = defaultApplyRegions();
        if (StrUtil.isBlank(allowed) || !("," + allowed + ",").contains("," + request.getLevel() + ",")) {
            throw new CrmebException("当前未开放该区域级别的代理申请");
        }
        // 是否已有待审核/已通过的代理
        Integer exists = agentDao.selectCount(new LambdaQueryWrapper<Agent>()
                .eq(Agent::getUid, uid)
                .eq(Agent::getIsDel, 0)
                .in(Agent::getStatus, Agent.STATUS_WAIT_AUDIT, Agent.STATUS_PASS));
        if (exists > 0) {
            throw new CrmebException("您已申请或已是区域代理");
        }
        validateRegion(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict());
        checkRegionConflict(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict(), null);

        Agent agent = new Agent();
        agent.setUid(uid);
        agent.setLevel(request.getLevel());
        agent.setProvince(request.getProvince());
        agent.setCity(StrUtil.nullToEmpty(request.getCity()));
        agent.setDistrict(StrUtil.nullToEmpty(request.getDistrict()));
        agent.setRegionName(buildRegionName(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        agent.setMatchKey(buildMatchKey(request.getLevel(), request.getProvince(), request.getCity(), request.getDistrict()));
        // 申请时代理比例为0，审核通过后由后台设置
        agent.setRatio(BigDecimal.ZERO);
        agent.setStatus(Agent.STATUS_WAIT_AUDIT);
        agent.setApplyMark(StrUtil.nullToEmpty(request.getApplyMark()));
        agent.setIsDel(0);
        return agentDao.insert(agent) > 0;
    }

    @Override
    public HashMap<String, Object> getMyAgentInfo(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getUid, uid).eq(Agent::getIsDel, 0).orderByDesc(Agent::getId);
        List<Agent> agentList = agentDao.selectList(wrapper);

        // 汇总统计（按用户维度）
        BigDecimal totalReward = agentRewardDao.selectList(new LambdaQueryWrapper<AgentReward>()
                .eq(AgentReward::getUid, uid)
                .eq(AgentReward::getStatus, AgentReward.STATUS_CREDITED))
                .stream().map(AgentReward::getRewardPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal waitReward = agentRewardDao.selectList(new LambdaQueryWrapper<AgentReward>()
                .eq(AgentReward::getUid, uid)
                .eq(AgentReward::getStatus, AgentReward.STATUS_WAIT))
                .stream().map(AgentReward::getRewardPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer rewardCount = agentRewardDao.selectCount(new LambdaQueryWrapper<AgentReward>()
                .eq(AgentReward::getUid, uid));

        map.put("agentList", agentList);
        map.put("isAgent", agentList.stream().anyMatch(a -> Agent.STATUS_PASS.equals(a.getStatus())));
        map.put("applyStatus", ObjectUtil.defaultIfNull(
                systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_APPLY_STATUS), "1"));
        map.put("funcStatus", ObjectUtil.defaultIfNull(
                systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_FUNC_STATUS), "1"));
        map.put("applyRegions", defaultApplyRegions());
        map.put("totalReward", totalReward);
        map.put("waitReward", waitReward);
        map.put("rewardCount", rewardCount);
        return map;
    }

    @Override
    public CommonPage<AgentReward> getMyRewardList(Integer uid, PageParamRequest pageParamRequest) {
        Page<AgentReward> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<AgentReward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentReward::getUid, uid).orderByDesc(AgentReward::getId);
        List<AgentReward> list = agentRewardDao.selectList(wrapper);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    // ==================== 结算 ====================

    @Override
    public List<UserBrokerageRecord> assignAgentBrokerage(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return new ArrayList<>();
        }
        String funcStatus = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_AGENT_FUNC_STATUS);
        if (StrUtil.isBlank(funcStatus) || "0".equals(funcStatus)) {
            return new ArrayList<>();
        }
        String address = storeOrder.getUserAddress();
        if (StrUtil.isBlank(address)) {
            return new ArrayList<>();
        }
        // 该订单已生成过奖励则不重复发放
        Integer exists = agentRewardDao.selectCount(new LambdaQueryWrapper<AgentReward>()
                .eq(AgentReward::getOrderId, storeOrder.getOrderId()));
        if (exists > 0) {
            return new ArrayList<>();
        }
        List<Agent> activeAgents = agentDao.selectList(new LambdaQueryWrapper<Agent>()
                .eq(Agent::getStatus, Agent.STATUS_PASS)
                .eq(Agent::getIsDel, 0));
        if (CollUtil.isEmpty(activeAgents)) {
            return new ArrayList<>();
        }
        List<Agent> matchedList = matchAgents(address, activeAgents);
        String frozenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);
        int frozenDays = Integer.parseInt(StrUtil.blankToDefault(frozenTime, "0"));

        ArrayList<UserBrokerageRecord> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(matchedList)) {
            Map<Integer, BigDecimal> rewardByAgentId = calcGeoRewards(storeOrder, matchedList);
            Map<Integer, Agent> agentMap = matchedList.stream()
                    .collect(Collectors.toMap(Agent::getId, a -> a, (a, b) -> a));
            for (Map.Entry<Integer, BigDecimal> entry : rewardByAgentId.entrySet()) {
                BigDecimal reward = entry.getValue();
                if (reward == null || reward.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Agent matched = agentMap.get(entry.getKey());
                if (matched == null) {
                    continue;
                }
                list.add(buildAgentRecord(storeOrder, matched, reward, frozenDays,
                        BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_AGENT,
                        BrokerageRecordConstants.BROKERAGE_LEVEL_AGENT,
                        StrUtil.format("订单【{}】实付{}元，收货地址命中代理区域【{}】，区域代理奖励{}",
                                storeOrder.getOrderId(), storeOrder.getPayPrice(), matched.getRegionName(), reward)));
            }
        }
        // 区域代理只有「按区域的提成奖励」一项（无平级/越级推荐奖）
        return list;
    }

    /**
     * 区域代理地理分润：支持商品级覆盖；syncMode 时上级比例含下级份额（极差）；
     * superiorClaim 控制无下级时上级是否领取下级份额。
     */
    private Map<Integer, BigDecimal> calcGeoRewards(StoreOrder storeOrder, List<Agent> matchedList) {
        Map<Integer, Agent> byLevel = new HashMap<>();
        for (Agent a : matchedList) {
            if (a.getLevel() != null) {
                byLevel.put(a.getLevel(), a);
            }
        }
        List<StoreOrderInfoOldVo> lines = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        Map<Integer, BigDecimal> rewardByAgentId = new HashMap<>();
        boolean anySync = false;
        if (CollUtil.isNotEmpty(lines)) {
            for (StoreOrderInfoOldVo line : lines) {
                Integer productId = line.getProductId();
                if (productId == null && line.getInfo() != null) {
                    productId = line.getInfo().getProductId();
                }
                StoreProduct product = ObjectUtil.isNotNull(productId) ? storeProductService.getById(productId) : null;
                ProductCommissionConfig.Agent agentCfg = ProductCommissionUtil.parse(
                        product != null ? product.getCommissionConfig() : null).getAgent();
                if (Boolean.TRUE.equals(agentCfg.getSyncMode())) {
                    anySync = true;
                    break;
                }
            }
        }
        if (!anySync) {
            for (Agent matched : matchedList) {
                BigDecimal reward = calcAgentRewardWithProductOverride(storeOrder, matched);
                if (reward.compareTo(BigDecimal.ZERO) > 0) {
                    rewardByAgentId.merge(matched.getId(), reward, BigDecimal::add);
                }
            }
            return rewardByAgentId;
        }
        // 同总设置模式：按行累计 raw 后极差分配
        if (CollUtil.isEmpty(lines)) {
            for (Agent matched : matchedList) {
                BigDecimal reward = calcAgentRewardWithProductOverride(storeOrder, matched);
                if (reward.compareTo(BigDecimal.ZERO) > 0) {
                    rewardByAgentId.merge(matched.getId(), reward, BigDecimal::add);
                }
            }
            return rewardByAgentId;
        }
        BigDecimal sumPayP = BigDecimal.ZERO;
        BigDecimal sumPayC = BigDecimal.ZERO;
        BigDecimal sumPayD = BigDecimal.ZERO;
        for (StoreOrderInfoOldVo line : lines) {
            if (ObjectUtil.isNull(line.getInfo())) {
                continue;
            }
            Integer productId = ObjectUtil.defaultIfNull(line.getProductId(), line.getInfo().getProductId());
            StoreProduct product = ObjectUtil.isNotNull(productId) ? storeProductService.getById(productId) : null;
            ProductCommissionConfig.Agent agentCfg = ProductCommissionUtil.parse(
                    product != null ? product.getCommissionConfig() : null).getAgent();
            if (!ProductCommissionUtil.resolveEnabled(agentCfg.getEnabled(), true)) {
                continue;
            }
            boolean sync = Boolean.TRUE.equals(agentCfg.getSyncMode());
            boolean claim = Boolean.TRUE.equals(agentCfg.getSuperiorClaim());
            BigDecimal unitPrice = ObjectUtil.isNotNull(line.getInfo().getVipPrice())
                    ? line.getInfo().getVipPrice() : line.getInfo().getPrice();
            int payNum = ObjectUtil.defaultIfNull(line.getInfo().getPayNum(), 1);
            BigDecimal rawP = calcLevelRaw(agentCfg, Agent.LEVEL_PROVINCE, byLevel.get(Agent.LEVEL_PROVINCE), unitPrice, payNum);
            BigDecimal rawC = calcLevelRaw(agentCfg, Agent.LEVEL_CITY, byLevel.get(Agent.LEVEL_CITY), unitPrice, payNum);
            BigDecimal rawD = calcLevelRaw(agentCfg, Agent.LEVEL_DISTRICT, byLevel.get(Agent.LEVEL_DISTRICT), unitPrice, payNum);
            boolean hasP = byLevel.containsKey(Agent.LEVEL_PROVINCE);
            boolean hasC = byLevel.containsKey(Agent.LEVEL_CITY);
            boolean hasD = byLevel.containsKey(Agent.LEVEL_DISTRICT);
            if (!sync) {
                if (hasP) {
                    sumPayP = sumPayP.add(rawP);
                }
                if (hasC) {
                    sumPayC = sumPayC.add(rawC);
                }
                if (hasD) {
                    sumPayD = sumPayD.add(rawD);
                }
                continue;
            }
            BigDecimal payD = hasD ? rawD : BigDecimal.ZERO;
            BigDecimal lowerForC = hasD ? rawD : (claim ? BigDecimal.ZERO : rawD);
            BigDecimal payC = hasC ? rawC.subtract(lowerForC).max(BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal lowerForP;
            if (hasC) {
                lowerForP = rawC;
            } else if (claim) {
                lowerForP = hasD ? rawD : BigDecimal.ZERO;
            } else {
                lowerForP = rawC;
            }
            BigDecimal payP = hasP ? rawP.subtract(lowerForP).max(BigDecimal.ZERO) : BigDecimal.ZERO;
            sumPayP = sumPayP.add(payP);
            sumPayC = sumPayC.add(payC);
            sumPayD = sumPayD.add(payD);
        }
        putLevelReward(rewardByAgentId, byLevel.get(Agent.LEVEL_PROVINCE), sumPayP);
        putLevelReward(rewardByAgentId, byLevel.get(Agent.LEVEL_CITY), sumPayC);
        putLevelReward(rewardByAgentId, byLevel.get(Agent.LEVEL_DISTRICT), sumPayD);
        return rewardByAgentId;
    }

    private void putLevelReward(Map<Integer, BigDecimal> map, Agent agent, BigDecimal reward) {
        if (agent == null || reward == null || reward.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        map.merge(agent.getId(), reward, BigDecimal::add);
    }

    private BigDecimal calcLevelRaw(ProductCommissionConfig.Agent agentCfg, Integer level, Agent matched,
                                    BigDecimal unitPrice, int payNum) {
        BigDecimal[] ar = ProductCommissionUtil.agentAmountRate(agentCfg, level);
        BigDecimal override = ProductCommissionUtil.calcOverride(ar[0], ar[1], unitPrice, payNum);
        if (override != null) {
            return override;
        }
        BigDecimal ratio = matched != null ? matched.getRatio() : null;
        if (ObjectUtil.isNull(ratio) || ratio.compareTo(BigDecimal.ZERO) <= 0 || ObjectUtil.isNull(unitPrice)) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(ratio)
                .divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
                .multiply(new BigDecimal(Math.max(payNum, 1)));
    }

    private UserBrokerageRecord buildAgentRecord(StoreOrder storeOrder, Agent matched, BigDecimal reward,
                                                 int frozenDays, String title, Integer brokerageLevel, String mark) {
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(matched.getUid());
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setPrice(reward);
        record.setMark(mark);
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
        record.setFrozenTime(frozenDays);
        record.setCreateTime(CrmebDateUtil.nowDateTime());
        record.setBrokerageLevel(brokerageLevel);

        AgentReward rewardRow = new AgentReward();
        rewardRow.setAgentId(matched.getId());
        rewardRow.setUid(matched.getUid());
        rewardRow.setOrderId(storeOrder.getOrderId());
        rewardRow.setOrderPayPrice(storeOrder.getPayPrice());
        rewardRow.setRatio(ObjectUtil.defaultIfNull(matched.getRatio(), BigDecimal.ZERO));
        rewardRow.setRewardPrice(reward);
        rewardRow.setRegionName(matched.getRegionName());
        rewardRow.setRecordId(0);
        rewardRow.setStatus(AgentReward.STATUS_WAIT);
        rewardRow.setCreateTime(new Date());
        agentRewardDao.insert(rewardRow);
        return record;
    }

    /**
     * 区域代理奖励：支持商品级覆盖（空=代理比例，0=该商品无此项）。独立模式（非 syncMode）使用。
     */
    private BigDecimal calcAgentRewardWithProductOverride(StoreOrder storeOrder, Agent matched) {
        List<StoreOrderInfoOldVo> lines = storeOrderInfoService.getOrderListByOrderId(storeOrder.getId());
        BigDecimal fallbackRatio = matched.getRatio();
        boolean needLineCalc = false;
        if (CollUtil.isNotEmpty(lines)) {
            for (StoreOrderInfoOldVo line : lines) {
                Integer productId = line.getProductId();
                if (productId == null && line.getInfo() != null) {
                    productId = line.getInfo().getProductId();
                }
                StoreProduct product = ObjectUtil.isNotNull(productId) ? storeProductService.getById(productId) : null;
                ProductCommissionConfig.Agent agentCfg = ProductCommissionUtil.parse(
                        product != null ? product.getCommissionConfig() : null).getAgent();
                BigDecimal[] ar = ProductCommissionUtil.agentAmountRate(agentCfg, matched.getLevel());
                if (Boolean.FALSE.equals(agentCfg.getEnabled())
                        || ProductCommissionUtil.hasOverride(ar[0], ar[1])
                        || Boolean.TRUE.equals(agentCfg.getSyncMode())) {
                    needLineCalc = true;
                    break;
                }
            }
        }
        if (!needLineCalc) {
            if (ObjectUtil.isNull(fallbackRatio) || fallbackRatio.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("区域代理【{}】奖励比例未设置，跳过订单{}", matched.getRegionName(), storeOrder.getOrderId());
                return BigDecimal.ZERO;
            }
            return storeOrder.getPayPrice()
                    .multiply(fallbackRatio)
                    .divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (StoreOrderInfoOldVo line : lines) {
            if (ObjectUtil.isNull(line.getInfo())) {
                continue;
            }
            Integer productId = ObjectUtil.defaultIfNull(line.getProductId(), line.getInfo().getProductId());
            StoreProduct product = ObjectUtil.isNotNull(productId) ? storeProductService.getById(productId) : null;
            ProductCommissionConfig.Agent agentCfg = ProductCommissionUtil.parse(
                    product != null ? product.getCommissionConfig() : null).getAgent();
            if (!ProductCommissionUtil.resolveEnabled(agentCfg.getEnabled(), true)) {
                continue;
            }
            BigDecimal unitPrice = ObjectUtil.isNotNull(line.getInfo().getVipPrice())
                    ? line.getInfo().getVipPrice() : line.getInfo().getPrice();
            int payNum = ObjectUtil.defaultIfNull(line.getInfo().getPayNum(), 1);
            BigDecimal[] ar = ProductCommissionUtil.agentAmountRate(agentCfg, matched.getLevel());
            BigDecimal override = ProductCommissionUtil.calcOverride(ar[0], ar[1], unitPrice, payNum);
            if (override != null) {
                total = total.add(override);
            } else if (ObjectUtil.isNotNull(fallbackRatio) && fallbackRatio.compareTo(BigDecimal.ZERO) > 0
                    && ObjectUtil.isNotNull(unitPrice)) {
                total = total.add(unitPrice.multiply(fallbackRatio)
                        .divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
                        .multiply(new BigDecimal(payNum)));
            }
        }
        return total;
    }

    @Override
    public Boolean syncRewardStatus(String orderId) {
        if (StrUtil.isBlank(orderId)) {
            return Boolean.FALSE;
        }
        List<AgentReward> rewardList = agentRewardDao.selectList(new LambdaQueryWrapper<AgentReward>()
                .eq(AgentReward::getOrderId, orderId)
                .eq(AgentReward::getStatus, AgentReward.STATUS_WAIT));
        if (CollUtil.isEmpty(rewardList)) {
            return Boolean.TRUE;
        }
        List<UserBrokerageRecord> recordList = userBrokerageRecordService
                .findListByLinkIdAndLinkType(orderId, BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER)
                .stream()
                .filter(r -> BrokerageRecordConstants.BROKERAGE_LEVEL_AGENT.equals(r.getBrokerageLevel())
                        || BrokerageRecordConstants.BROKERAGE_LEVEL_AGENT_PEER.equals(r.getBrokerageLevel())
                        || BrokerageRecordConstants.BROKERAGE_LEVEL_AGENT_LEAP.equals(r.getBrokerageLevel()))
                .collect(Collectors.toList());
        Date now = new Date();
        for (AgentReward reward : rewardList) {
            // 同一订单可能产生多条代理奖励（省/市/区逐级分润），且各级可能是同一个人；
            // 必须按 uid + 金额一一配对（配对即从池中移除），否则同人多条奖励会重复关联到同一条记录
            UserBrokerageRecord matched = recordList.stream()
                    .filter(r -> r.getUid().equals(reward.getUid()))
                    .filter(r -> r.getPrice() != null && r.getPrice().compareTo(reward.getRewardPrice()) == 0)
                    .findFirst().orElse(null);
            if (ObjectUtil.isNull(matched)) {
                continue;
            }
            recordList.remove(matched);
            if (BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE.equals(matched.getStatus())) {
                reward.setStatus(AgentReward.STATUS_CREDITED);
                reward.setCreditTime(now);
                agentRewardDao.updateById(reward);
            } else if (BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION.equals(matched.getStatus())) {
                reward.setStatus(AgentReward.STATUS_INVALID);
                agentRewardDao.updateById(reward);
            }
        }
        return Boolean.TRUE;
    }

    // ==================== 私有方法 ====================

    /**
     * 按收货地址逐级匹配区域代理：省/市/区各级各取一个得分最高的代理（可全部命中、全部分润）。
     * 单个代理的命中规则与原 matchAgent 一致：地址需包含该代理自身的区域关键词（matchKey）。
     */
    private List<Agent> matchAgents(String address, List<Agent> agents) {
        Agent bestProvince = null;
        Agent bestCity = null;
        Agent bestDistrict = null;
        int scoreProvince = 0;
        int scoreCity = 0;
        int scoreDistrict = 0;
        int lenProvince = 0;
        int lenCity = 0;
        int lenDistrict = 0;
        for (Agent agent : agents) {
            int score = 0;
            if (StrUtil.isNotBlank(agent.getProvince()) && address.contains(agent.getProvince())) {
                score += 4;
            }
            if (StrUtil.isNotBlank(agent.getCity()) && address.contains(agent.getCity())) {
                score += 2;
            }
            if (StrUtil.isNotBlank(agent.getDistrict()) && address.contains(agent.getDistrict())) {
                score += 1;
            }
            // 至少命中自身级别的关键词
            String selfKey = agent.getMatchKey();
            if (score == 0 || StrUtil.isBlank(selfKey) || !address.contains(selfKey)) {
                continue;
            }
            int len = selfKey.length();
            Integer level = agent.getLevel();
            if (Agent.LEVEL_PROVINCE.equals(level)) {
                if (score > scoreProvince || (score == scoreProvince && len > lenProvince)) {
                    bestProvince = agent;
                    scoreProvince = score;
                    lenProvince = len;
                }
            } else if (Agent.LEVEL_CITY.equals(level)) {
                if (score > scoreCity || (score == scoreCity && len > lenCity)) {
                    bestCity = agent;
                    scoreCity = score;
                    lenCity = len;
                }
            } else if (Agent.LEVEL_DISTRICT.equals(level)) {
                if (score > scoreDistrict || (score == scoreDistrict && len > lenDistrict)) {
                    bestDistrict = agent;
                    scoreDistrict = score;
                    lenDistrict = len;
                }
            }
        }
        List<Agent> result = new ArrayList<>();
        // 返回顺序：省 → 市 → 区（从大到小），便于日志与明细阅读
        if (bestProvince != null) {
            result.add(bestProvince);
        }
        if (bestCity != null) {
            result.add(bestCity);
        }
        if (bestDistrict != null) {
            result.add(bestDistrict);
        }
        return result;
    }

    private void validateRegion(Integer level, String province, String city, String district) {
        if (ObjectUtil.isNull(province) || province.trim().isEmpty()) {
            throw new CrmebException("请选择省份");
        }
        if (Agent.LEVEL_CITY.equals(level) && (StrUtil.isBlank(city))) {
            throw new CrmebException("市级代理必须选择城市");
        }
        if (Agent.LEVEL_DISTRICT.equals(level) && (StrUtil.isBlank(city) || StrUtil.isBlank(district))) {
            throw new CrmebException("区级代理必须选择城市和区/县");
        }
    }

    /**
     * 同级别同区域唯一代理校验
     */
    private void checkRegionConflict(Integer level, String province, String city, String district, Integer excludeId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getLevel, level)
                .eq(Agent::getProvince, province)
                .eq(Agent::getCity, StrUtil.nullToEmpty(city))
                .eq(Agent::getDistrict, StrUtil.nullToEmpty(district))
                .eq(Agent::getIsDel, 0)
                .eq(Agent::getStatus, Agent.STATUS_PASS);
        if (ObjectUtil.isNotNull(excludeId)) {
            wrapper.ne(Agent::getId, excludeId);
        }
        Integer count = agentDao.selectCount(wrapper);
        if (count > 0) {
            throw new CrmebException("该区域已存在代理，每个区域仅可设置一名代理");
        }
    }

    private String buildRegionName(Integer level, String province, String city, String district) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(province)) {
            sb.append(province);
        }
        if (level >= Agent.LEVEL_CITY && StrUtil.isNotBlank(city)) {
            sb.append(city);
        }
        if (level >= Agent.LEVEL_DISTRICT && StrUtil.isNotBlank(district)) {
            sb.append(district);
        }
        return sb.toString();
    }

    private String buildMatchKey(Integer level, String province, String city, String district) {
        if (Agent.LEVEL_PROVINCE.equals(level)) {
            return province;
        }
        if (Agent.LEVEL_CITY.equals(level)) {
            return city;
        }
        return district;
    }

    // ==================== 代理商变更记录 ====================

    /**
     * 写入变更记录：失败不影响主流程
     */
    private void logChange(Integer agentId, Integer uid, Integer type, String oldValue, String newValue, String mark) {
        try {
            AgentChangeLog log = new AgentChangeLog();
            log.setAgentId(agentId);
            log.setUid(uid);
            log.setType(type);
            log.setOldValue(StrUtil.nullToEmpty(oldValue));
            log.setNewValue(StrUtil.nullToEmpty(newValue));
            log.setMark(StrUtil.nullToEmpty(mark));
            log.setCreateTime(new Date());
            agentChangeLogDao.insert(log);
        } catch (Exception e) {
            logger.error("写入代理商变更记录失败，agentId={}, type={}, error={}", agentId, type, e.getMessage());
        }
    }

    /** 代理级别+区域描述 */
    private String agentDesc(Agent agent) {
        return StrUtil.format("{}【{}】", levelName(agent.getLevel()), StrUtil.nullToEmpty(agent.getRegionName()));
    }

    private String levelName(Integer level) {
        if (Agent.LEVEL_PROVINCE.equals(level)) {
            return "省级代理";
        }
        if (Agent.LEVEL_CITY.equals(level)) {
            return "市级代理";
        }
        if (Agent.LEVEL_DISTRICT.equals(level)) {
            return "区级代理";
        }
        return "未知级别";
    }

    private String statusName(Integer status) {
        if (Agent.STATUS_WAIT_AUDIT.equals(status)) {
            return "待审核";
        }
        if (Agent.STATUS_PASS.equals(status)) {
            return "已通过";
        }
        if (Agent.STATUS_FAIL.equals(status)) {
            return "已拒绝";
        }
        return "未知状态";
    }

    private String ratioText(BigDecimal ratio) {
        return ObjectUtil.isNull(ratio) ? "0" : ratio.stripTrailingZeros().toPlainString();
    }

    private void fillUserInfo(List<Agent> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Integer> uidList = list.stream().map(Agent::getUid).distinct().collect(Collectors.toList());
        HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
        list.forEach(e -> {
            User user = userMap.get(e.getUid());
            if (ObjectUtil.isNotNull(user)) {
                e.setNickname(user.getNickname());
                e.setAccount(StrUtil.blankToDefault(user.getPhone(), user.getAccount()));
            } else {
                e.setNickname("-");
                e.setAccount("-");
            }
        });
    }

    private void fillRewardUserInfo(List<AgentReward> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Integer> uidList = list.stream().map(AgentReward::getUid).distinct().collect(Collectors.toList());
        HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
        list.forEach(e -> {
            User user = userMap.get(e.getUid());
            e.setNickname(ObjectUtil.isNotNull(user) ? user.getNickname() : "-");
        });
    }
}
