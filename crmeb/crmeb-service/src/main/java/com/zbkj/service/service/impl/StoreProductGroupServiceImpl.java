package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.agent.Agent;
import com.zbkj.common.model.product.StoreProductGroup;
import com.zbkj.common.model.product.StoreProductGroupRel;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.theme.Theme;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductGroupRequest;
import com.zbkj.common.request.StoreProductGroupSearchRequest;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.dao.AgentDao;
import com.zbkj.service.dao.StoreProductGroupDao;
import com.zbkj.service.dao.StoreProductGroupRelDao;
import com.zbkj.service.dao.ThemeDao;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.StoreProductGroupService;
import com.zbkj.service.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品分组服务实现
 */
@Service
public class StoreProductGroupServiceImpl extends ServiceImpl<StoreProductGroupDao, StoreProductGroup>
        implements StoreProductGroupService {

    public static final String CONFIG_OTHER_VISIBLE = "product_group_other_visible";
    public static final String CONFIG_DENY_TIP_ENABLE = "product_group_deny_tip_enable";
    public static final String CONFIG_DENY_TIP = "product_group_deny_tip";
    private static final String DEFAULT_DENY_TIP = "您暂无权限查看该商品";
    /** 分组装修页标题前缀，便于在「装修 → 专题页面」里与普通微页面区分 */
    private static final String THEME_TITLE_PREFIX = "商品分组-";

    @Resource
    private StoreProductGroupDao dao;

    @Resource
    private StoreProductGroupRelDao relDao;

    @Resource
    private ThemeDao themeDao;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private AgentDao agentDao;

    @Resource
    private StockService stockService;

    @Override
    public CommonPage<StoreProductGroup> getAdminList(StoreProductGroupSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<StoreProductGroup> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductGroup::getIsDel, false);
        if (StrUtil.isNotBlank(request.getName())) {
            lqw.like(StoreProductGroup::getName, request.getName());
        }
        if (ObjectUtil.isNotNull(request.getStatus())) {
            lqw.eq(StoreProductGroup::getStatus, request.getStatus());
        }
        lqw.orderByDesc(StoreProductGroup::getSort).orderByDesc(StoreProductGroup::getId);
        List<StoreProductGroup> list = dao.selectList(lqw);
        if (CollUtil.isNotEmpty(list)) {
            for (StoreProductGroup group : list) {
                Integer count = relDao.selectCount(Wrappers.<StoreProductGroupRel>lambdaQuery()
                        .eq(StoreProductGroupRel::getGroupId, group.getId()));
                group.setProductCount(count == null ? 0 : count);
            }
        }
        return CommonPage.restPage(list);
    }

    @Override
    public StoreProductGroup getInfo(Integer id) {
        StoreProductGroup group = getValidGroup(id);
        List<StoreProductGroupRel> rels = relDao.selectList(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .eq(StoreProductGroupRel::getGroupId, id));
        group.setProductIds(rels.stream().map(StoreProductGroupRel::getProductId).collect(Collectors.toList()));
        group.setProductCount(group.getProductIds().size());
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(StoreProductGroupRequest request) {
        validatePermissionType(request.getPermissionType());
        StoreProductGroup group = new StoreProductGroup();
        fillEntity(group, request);
        group.setIsDel(false);
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        if (!save(group)) {
            throw new CrmebException("创建商品分组失败");
        }
        saveRels(group.getId(), request.getProductIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(Integer id, StoreProductGroupRequest request) {
        validatePermissionType(request.getPermissionType());
        StoreProductGroup group = getValidGroup(id);
        fillEntity(group, request);
        group.setUpdateTime(new Date());
        if (!updateById(group)) {
            throw new CrmebException("更新商品分组失败");
        }
        relDao.delete(Wrappers.<StoreProductGroupRel>lambdaQuery().eq(StoreProductGroupRel::getGroupId, id));
        saveRels(id, request.getProductIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGroup(Integer id) {
        StoreProductGroup group = getValidGroup(id);
        group.setIsDel(true);
        group.setUpdateTime(new Date());
        updateById(group);
        relDao.delete(Wrappers.<StoreProductGroupRel>lambdaQuery().eq(StoreProductGroupRel::getGroupId, id));
        return true;
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        StoreProductGroup group = getValidGroup(id);
        group.setStatus(ObjectUtil.defaultIfNull(status, true));
        group.setUpdateTime(new Date());
        return updateById(group);
    }

    @Override
    public HashMap<String, Object> getConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("otherVisible", "1".equals(systemConfigService.getValueByKey(CONFIG_OTHER_VISIBLE)));
        map.put("denyTipEnable", "1".equals(systemConfigService.getValueByKey(CONFIG_DENY_TIP_ENABLE)));
        String tip = systemConfigService.getValueByKey(CONFIG_DENY_TIP);
        map.put("denyTip", StrUtil.isBlank(tip) ? DEFAULT_DENY_TIP : tip);
        return map;
    }

    @Override
    public Boolean updateConfig(HashMap<String, Object> config) {
        if (config == null) {
            return true;
        }
        if (config.containsKey("otherVisible")) {
            systemConfigService.updateOrSaveValueByName(CONFIG_OTHER_VISIBLE, boolToSwitch(config.get("otherVisible")));
        }
        if (config.containsKey("denyTipEnable")) {
            systemConfigService.updateOrSaveValueByName(CONFIG_DENY_TIP_ENABLE, boolToSwitch(config.get("denyTipEnable")));
        }
        if (config.containsKey("denyTip")) {
            Object tip = config.get("denyTip");
            systemConfigService.updateOrSaveValueByName(CONFIG_DENY_TIP, tip == null ? DEFAULT_DENY_TIP : String.valueOf(tip));
        }
        return true;
    }

    @Override
    public List<StoreProductGroup> getEnabledSimpleList() {
        LambdaQueryWrapper<StoreProductGroup> lqw = Wrappers.lambdaQuery();
        lqw.select(StoreProductGroup::getId, StoreProductGroup::getName, StoreProductGroup::getSort);
        lqw.eq(StoreProductGroup::getIsDel, false).eq(StoreProductGroup::getStatus, true);
        lqw.orderByDesc(StoreProductGroup::getSort).orderByDesc(StoreProductGroup::getId);
        return dao.selectList(lqw);
    }

    @Override
    public List<Integer> getProductIdsByGroupIds(List<Integer> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        List<StoreProductGroupRel> rels = relDao.selectList(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .in(StoreProductGroupRel::getGroupId, groupIds));
        return rels.stream().map(StoreProductGroupRel::getProductId).distinct().collect(Collectors.toList());
    }

    @Override
    public Set<Integer> getHiddenProductIds(User user) {
        // 其它人能看到列表时，不在列表层隐藏
        if (isOtherVisible()) {
            return Collections.emptySet();
        }
        return getUnauthorizedProductIds(user);
    }

    @Override
    public boolean canViewProduct(User user, Integer productId) {
        if (productId == null) {
            return true;
        }
        if (!isProductInAnyEnabledGroup(productId)) {
            return true;
        }
        if (matchAnyGroup(user, productId)) {
            return true;
        }
        // 无权限：other_visible=1 时列表可见，但详情仍不可进
        return false;
    }

    @Override
    public boolean canBuyProduct(User user, Integer productId) {
        if (productId == null) {
            return true;
        }
        if (!isProductInAnyEnabledGroup(productId)) {
            return true;
        }
        return matchAnyGroup(user, productId);
    }

    @Override
    public String getDenyTip() {
        if (!"1".equals(systemConfigService.getValueByKey(CONFIG_DENY_TIP_ENABLE))) {
            return DEFAULT_DENY_TIP;
        }
        String tip = systemConfigService.getValueByKey(CONFIG_DENY_TIP);
        return StrUtil.isBlank(tip) ? DEFAULT_DENY_TIP : tip;
    }

    @Override
    public void assertPurchaseAllowed(User user, Integer productId, Integer buyNum) {
        if (!canBuyProduct(user, productId)) {
            throw new CrmebException(getDenyTip());
        }
        List<StoreProductGroup> matched = listMatchedGroups(user, productId);
        if (CollUtil.isEmpty(matched)) {
            return;
        }
        int minBuy = 1;
        boolean limitOne = false;
        for (StoreProductGroup g : matched) {
            if (g.getMinBuy() != null && g.getMinBuy() > minBuy) {
                minBuy = g.getMinBuy();
            }
            if (Boolean.TRUE.equals(g.getLimitOne())) {
                limitOne = true;
            }
        }
        int num = buyNum == null ? 1 : buyNum;
        if (num < minBuy) {
            throw new CrmebException("该商品起卖数为" + minBuy);
        }
        if (limitOne && num > 1) {
            throw new CrmebException("该商品限购一件");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindProductGroups(Integer productId, List<Integer> groupIds) {
        if (productId == null || productId <= 0) {
            return true;
        }
        relDao.delete(Wrappers.<StoreProductGroupRel>lambdaQuery().eq(StoreProductGroupRel::getProductId, productId));
        if (CollUtil.isEmpty(groupIds)) {
            return true;
        }
        Date now = new Date();
        Set<Integer> uniq = new HashSet<>(groupIds);
        for (Integer groupId : uniq) {
            if (groupId == null || groupId <= 0) {
                continue;
            }
            StoreProductGroup group = getById(groupId);
            if (group == null || Boolean.TRUE.equals(group.getIsDel())) {
                continue;
            }
            StoreProductGroupRel rel = new StoreProductGroupRel();
            rel.setGroupId(groupId);
            rel.setProductId(productId);
            rel.setCreateTime(now);
            relDao.insert(rel);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchBindProducts(List<Integer> productIds, List<Integer> groupIds) {
        if (CollUtil.isEmpty(productIds) || CollUtil.isEmpty(groupIds)) {
            throw new CrmebException("请选择商品和分组");
        }
        Set<Integer> groupUniq = new HashSet<>(groupIds);
        Set<Integer> productUniq = new HashSet<>(productIds);
        Date now = new Date();
        for (Integer groupId : groupUniq) {
            if (groupId == null || groupId <= 0) {
                continue;
            }
            StoreProductGroup group = getById(groupId);
            if (group == null || Boolean.TRUE.equals(group.getIsDel())) {
                continue;
            }
            for (Integer productId : productUniq) {
                if (productId == null || productId <= 0) {
                    continue;
                }
                Integer exists = relDao.selectCount(Wrappers.<StoreProductGroupRel>lambdaQuery()
                        .eq(StoreProductGroupRel::getGroupId, groupId)
                        .eq(StoreProductGroupRel::getProductId, productId));
                if (exists != null && exists > 0) {
                    continue;
                }
                StoreProductGroupRel rel = new StoreProductGroupRel();
                rel.setGroupId(groupId);
                rel.setProductId(productId);
                rel.setCreateTime(now);
                relDao.insert(rel);
            }
        }
        return true;
    }

    @Override
    public List<Integer> getGroupIdsByProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            return Collections.emptyList();
        }
        List<StoreProductGroupRel> rels = relDao.selectList(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .eq(StoreProductGroupRel::getProductId, productId));
        if (CollUtil.isEmpty(rels)) {
            return Collections.emptyList();
        }
        return rels.stream().map(StoreProductGroupRel::getGroupId).distinct().collect(Collectors.toList());
    }

    // -------------------- helpers --------------------

    private Set<Integer> getUnauthorizedProductIds(User user) {
        List<StoreProductGroup> groups = listEnabledGroups();
        if (CollUtil.isEmpty(groups)) {
            return Collections.emptySet();
        }
        List<Integer> groupIds = groups.stream().map(StoreProductGroup::getId).collect(Collectors.toList());
        List<StoreProductGroupRel> allRels = relDao.selectList(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .in(StoreProductGroupRel::getGroupId, groupIds));
        if (CollUtil.isEmpty(allRels)) {
            return Collections.emptySet();
        }
        Set<Integer> groupedProductIds = allRels.stream().map(StoreProductGroupRel::getProductId).collect(Collectors.toSet());
        Set<Integer> allowed = new HashSet<>();
        HashMap<Integer, List<Integer>> productGroupMap = new HashMap<>();
        for (StoreProductGroupRel rel : allRels) {
            productGroupMap.computeIfAbsent(rel.getProductId(), k -> new ArrayList<>()).add(rel.getGroupId());
        }
        HashMap<Integer, StoreProductGroup> groupMap = new HashMap<>();
        for (StoreProductGroup g : groups) {
            groupMap.put(g.getId(), g);
        }
        for (Integer productId : groupedProductIds) {
            List<Integer> gids = productGroupMap.get(productId);
            if (CollUtil.isEmpty(gids)) {
                continue;
            }
            for (Integer gid : gids) {
                StoreProductGroup g = groupMap.get(gid);
                if (g != null && matchGroup(user, g)) {
                    allowed.add(productId);
                    break;
                }
            }
        }
        Set<Integer> hidden = new HashSet<>(groupedProductIds);
        hidden.removeAll(allowed);
        return hidden;
    }

    private boolean isProductInAnyEnabledGroup(Integer productId) {
        List<StoreProductGroup> groups = listEnabledGroups();
        if (CollUtil.isEmpty(groups)) {
            return false;
        }
        List<Integer> groupIds = groups.stream().map(StoreProductGroup::getId).collect(Collectors.toList());
        Integer count = relDao.selectCount(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .eq(StoreProductGroupRel::getProductId, productId)
                .in(StoreProductGroupRel::getGroupId, groupIds));
        return count != null && count > 0;
    }

    private boolean matchAnyGroup(User user, Integer productId) {
        return CollUtil.isNotEmpty(listMatchedGroups(user, productId));
    }

    private List<StoreProductGroup> listMatchedGroups(User user, Integer productId) {
        List<StoreProductGroup> groups = listEnabledGroups();
        if (CollUtil.isEmpty(groups)) {
            return Collections.emptyList();
        }
        List<Integer> groupIds = groups.stream().map(StoreProductGroup::getId).collect(Collectors.toList());
        List<StoreProductGroupRel> rels = relDao.selectList(Wrappers.<StoreProductGroupRel>lambdaQuery()
                .eq(StoreProductGroupRel::getProductId, productId)
                .in(StoreProductGroupRel::getGroupId, groupIds));
        if (CollUtil.isEmpty(rels)) {
            return Collections.emptyList();
        }
        Set<Integer> hitIds = rels.stream().map(StoreProductGroupRel::getGroupId).collect(Collectors.toSet());
        List<StoreProductGroup> matched = new ArrayList<>();
        for (StoreProductGroup g : groups) {
            if (hitIds.contains(g.getId()) && matchGroup(user, g)) {
                matched.add(g);
            }
        }
        return matched;
    }

    private boolean matchGroup(User user, StoreProductGroup group) {
        if (!matchRole(user, group.getPermissionType())) {
            return false;
        }
        if (!matchUserGroup(user, group.getUserGroupIds())) {
            return false;
        }
        if (!matchUserLevel(user, group)) {
            return false;
        }
        return true;
    }

    private boolean matchRole(User user, String permissionType) {
        String type = StrUtil.blankToDefault(permissionType, StoreProductGroup.PERM_ALL);
        if (StoreProductGroup.PERM_ALL.equals(type)) {
            return true;
        }
        if (user == null) {
            return false;
        }
        if (StoreProductGroup.PERM_PROMOTER.equals(type)) {
            return Boolean.TRUE.equals(user.getIsPromoter());
        }
        if (StoreProductGroup.PERM_AGENT.equals(type)) {
            return isPassAgent(user.getUid());
        }
        if (StoreProductGroup.PERM_STOCK_AGENT.equals(type)) {
            return isEnabledStockAgent(user.getUid());
        }
        if (StoreProductGroup.PERM_TEAM.equals(type)) {
            return hasTeamLevel(user);
        }
        return true;
    }

    private boolean matchUserGroup(User user, String userGroupIds) {
        if (StrUtil.isBlank(userGroupIds)) {
            return true;
        }
        if (user == null || StrUtil.isBlank(user.getGroupId())) {
            return false;
        }
        List<Integer> required = CrmebUtil.stringToArray(userGroupIds);
        List<Integer> owned = CrmebUtil.stringToArray(user.getGroupId());
        if (CollUtil.isEmpty(required) || CollUtil.isEmpty(owned)) {
            return false;
        }
        for (Integer id : required) {
            if (owned.contains(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 等级门槛校验：levelOnly=true 时按权限类型取对应等级源比对
     * all         -> 会员等级 eb_user.level（eb_system_user_level.id）
     * promoter    -> 分销商等级 eb_user.distributor_level_id（eb_distributor_level.id）
     * agent       -> 区域代理等级 eb_agent.level（固定枚举 1=省代 2=市代 3=区代）
     * stock_agent -> 订货商等级 eb_stock_agent.level_id（eb_stock_level.id）
     * team        -> 社群团队等级 eb_user.team_level（eb_system_team_level.id）
     * 对应等级未配置时视为不限（与改造前行为一致）
     */
    private boolean matchUserLevel(User user, StoreProductGroup group) {
        if (!Boolean.TRUE.equals(group.getLevelOnly())) {
            return true;
        }
        String type = StrUtil.blankToDefault(group.getPermissionType(), StoreProductGroup.PERM_ALL);
        if (StoreProductGroup.PERM_PROMOTER.equals(type)) {
            if (StrUtil.isBlank(group.getDistributorLevelIds())) {
                return true;
            }
            if (user == null) {
                return false;
            }
            return containsLevel(group.getDistributorLevelIds(), user.getDistributorLevelId());
        }
        // 仅区域代理：等级为固定枚举 1=省代 2=市代 3=区代（eb_agent.level），不再读 eb_stock_level
        if (StoreProductGroup.PERM_AGENT.equals(type)) {
            if (StrUtil.isBlank(group.getAgentLevelIds())) {
                return true;
            }
            if (user == null) {
                return false;
            }
            return containsLevel(group.getAgentLevelIds(), getPassAgentLevel(user.getUid()));
        }
        // 仅订货商：等级来源 eb_stock_level
        if (StoreProductGroup.PERM_STOCK_AGENT.equals(type)) {
            if (StrUtil.isBlank(group.getStockLevelIds())) {
                return true;
            }
            if (user == null) {
                return false;
            }
            return containsLevel(group.getStockLevelIds(), getEnabledStockAgentLevel(user.getUid()));
        }
        // 仅社群团队：等级来源 eb_system_team_level.id（eb_user.team_level）
        if (StoreProductGroup.PERM_TEAM.equals(type)) {
            if (StrUtil.isBlank(group.getTeamLevelIds())) {
                return true;
            }
            if (user == null) {
                return false;
            }
            return containsLevel(group.getTeamLevelIds(), user.getTeamLevel());
        }
        // 全部会员：按会员等级
        if (StrUtil.isBlank(group.getUserLevelIds())) {
            return true;
        }
        if (user == null) {
            return false;
        }
        return containsLevel(group.getUserLevelIds(), user.getLevel());
    }

    private boolean containsLevel(String ids, Integer levelId) {
        if (StrUtil.isBlank(ids)) {
            return true;
        }
        if (levelId == null) {
            return false;
        }
        return CrmebUtil.stringToArray(ids).contains(levelId);
    }

    private Integer getPassAgentLevel(Integer uid) {
        if (uid == null) {
            return null;
        }
        Agent agent = agentDao.selectOne(Wrappers.<Agent>lambdaQuery()
                .eq(Agent::getUid, uid)
                .eq(Agent::getIsDel, 0)
                .eq(Agent::getStatus, Agent.STATUS_PASS)
                .last("limit 1"));
        return agent == null ? null : agent.getLevel();
    }

    private Integer getEnabledStockAgentLevel(Integer uid) {
        if (uid == null) {
            return null;
        }
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || StockAgent.STATUS_ENABLED != agent.getStatus()) {
            return null;
        }
        return agent.getLevelId();
    }

    /** 是否已获得社群团队等级（eb_user.team_level = eb_system_team_level.id，0=未获得） */
    private boolean hasTeamLevel(User user) {
        return user != null && user.getTeamLevel() != null && user.getTeamLevel() > 0;
    }

    private boolean isPassAgent(Integer uid) {
        if (uid == null) {
            return false;
        }
        Integer count = agentDao.selectCount(Wrappers.<Agent>lambdaQuery()
                .eq(Agent::getUid, uid)
                .eq(Agent::getIsDel, 0)
                .eq(Agent::getStatus, Agent.STATUS_PASS));
        return count != null && count > 0;
    }

    private boolean isEnabledStockAgent(Integer uid) {
        if (uid == null) {
            return false;
        }
        StockAgent agent = stockService.getAgentByUid(uid);
        return agent != null && StockAgent.STATUS_ENABLED == agent.getStatus();
    }

    private List<StoreProductGroup> listEnabledGroups() {
        return dao.selectList(Wrappers.<StoreProductGroup>lambdaQuery()
                .eq(StoreProductGroup::getIsDel, false)
                .eq(StoreProductGroup::getStatus, true));
    }

    private boolean isOtherVisible() {
        return "1".equals(systemConfigService.getValueByKey(CONFIG_OTHER_VISIBLE));
    }

    private StoreProductGroup getValidGroup(Integer id) {
        StoreProductGroup group = getById(id);
        if (group == null || Boolean.TRUE.equals(group.getIsDel())) {
            throw new CrmebException("商品分组不存在");
        }
        return group;
    }

    @Override
    public StoreProductGroup getEnabledById(Integer id) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            return null;
        }
        StoreProductGroup group = getById(id);
        if (group == null || Boolean.TRUE.equals(group.getIsDel()) || !Boolean.TRUE.equals(group.getStatus())) {
            return null;
        }
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer ensureTheme(Integer groupId) {
        StoreProductGroup group = getValidGroup(groupId);
        Integer bound = group.getThemeId();
        if (ObjectUtil.isNotNull(bound) && bound > 0) {
            Theme exist = themeDao.selectById(bound);
            // Theme.isDel 是 Integer（0/1），不是 Boolean，别用 Boolean.TRUE.equals
            if (ObjectUtil.isNotNull(exist) && !Integer.valueOf(1).equals(exist.getIsDel())) {
                return bound;
            }
        }
        // 懒创建装修页（自建微页面，内容落在 home_data）
        int now = CrmebDateUtil.getNowTime();
        Theme theme = new Theme()
                .setVersion("")
                .setTitle(THEME_TITLE_PREFIX + group.getName())
                .setInfo("")
                .setType(0)
                .setPageType("micro")
                .setIsUse(0)
                .setIsDel(0)
                .setAddTime(now)
                .setUpTime(now);
        if (themeDao.insert(theme) <= 0 || ObjectUtil.isNull(theme.getId())) {
            throw new CrmebException("创建分组装修页失败");
        }
        Integer themeId = theme.getId();
        // CAS 回写：只在 theme_id 仍为 0 时写入，避免并发下互相覆盖
        boolean updated = update(Wrappers.<StoreProductGroup>lambdaUpdate()
                .eq(StoreProductGroup::getId, groupId)
                .eq(StoreProductGroup::getThemeId, 0)
                .set(StoreProductGroup::getThemeId, themeId)
                .set(StoreProductGroup::getUpdateTime, new Date()));
        if (!updated) {
            // 已被并发请求抢先绑定，丢弃自己刚建的记录，回读对方的
            themeDao.deleteById(themeId);
            StoreProductGroup latest = getById(groupId);
            if (ObjectUtil.isNotNull(latest) && ObjectUtil.isNotNull(latest.getThemeId()) && latest.getThemeId() > 0) {
                return latest.getThemeId();
            }
        }
        return themeId;
    }

    private void validatePermissionType(String type) {
        if (!StoreProductGroup.PERM_ALL.equals(type)
                && !StoreProductGroup.PERM_PROMOTER.equals(type)
                && !StoreProductGroup.PERM_AGENT.equals(type)
                && !StoreProductGroup.PERM_STOCK_AGENT.equals(type)
                && !StoreProductGroup.PERM_TEAM.equals(type)) {
            throw new CrmebException("权限类型不正确");
        }
    }

    private void fillEntity(StoreProductGroup group, StoreProductGroupRequest request) {
        group.setName(request.getName().trim());
        group.setPermissionType(request.getPermissionType());
        group.setUserGroupIds(joinIds(request.getUserGroupIds()));
        group.setUserLevelIds(joinIds(request.getUserLevelIds()));
        group.setDistributorLevelIds(joinIds(request.getDistributorLevelIds()));
        group.setAgentLevelIds(joinIds(request.getAgentLevelIds()));
        group.setStockLevelIds(joinIds(request.getStockLevelIds()));
        group.setTeamLevelIds(joinIds(request.getTeamLevelIds()));
        group.setLevelOnly(ObjectUtil.defaultIfNull(request.getLevelOnly(), false));
        group.setMinBuy(ObjectUtil.defaultIfNull(request.getMinBuy(), 1));
        if (group.getMinBuy() < 1) {
            group.setMinBuy(1);
        }
        group.setLimitOne(ObjectUtil.defaultIfNull(request.getLimitOne(), false));
        group.setLayout(StrUtil.blankToDefault(request.getLayout(), "double"));
        group.setStyle(ObjectUtil.defaultIfNull(request.getStyle(), 1));
        group.setBadge(StrUtil.nullToDefault(request.getBadge(), ""));
        group.setTitleMulti(ObjectUtil.defaultIfNull(request.getTitleMulti(), false));
        group.setSort(ObjectUtil.defaultIfNull(request.getSort(), 0));
        group.setStatus(ObjectUtil.defaultIfNull(request.getStatus(), true));
    }

    private void saveRels(Integer groupId, List<Integer> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return;
        }
        Date now = new Date();
        Set<Integer> uniq = new HashSet<>(productIds);
        for (Integer productId : uniq) {
            if (productId == null || productId <= 0) {
                continue;
            }
            StoreProductGroupRel rel = new StoreProductGroupRel();
            rel.setGroupId(groupId);
            rel.setProductId(productId);
            rel.setCreateTime(now);
            relDao.insert(rel);
        }
    }

    private String joinIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return "";
        }
        return ids.stream().filter(ObjectUtil::isNotNull).map(String::valueOf).distinct().collect(Collectors.joining(","));
    }

    private String boolToSwitch(Object val) {
        if (val instanceof Boolean) {
            return Boolean.TRUE.equals(val) ? "1" : "0";
        }
        String s = String.valueOf(val);
        return "1".equals(s) || "true".equalsIgnoreCase(s) ? "1" : "0";
    }
}
