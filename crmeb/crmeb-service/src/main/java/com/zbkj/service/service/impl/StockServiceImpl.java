package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockChangeLog;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockLadder;
import com.zbkj.common.model.stock.StockLog;
import com.zbkj.common.model.stock.StockPrice;
import com.zbkj.common.model.stock.StockProductRel;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockAgentCreateRequest;
import com.zbkj.common.request.StockAgentRequest;
import com.zbkj.common.request.StockRequests;
import com.zbkj.service.dao.StockAgentDao;
import com.zbkj.service.dao.StockLevelDao;
import com.zbkj.service.dao.StockLogDao;
import com.zbkj.service.dao.StockPriceDao;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 订货系统-基础服务实现
 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockAgentDao stockAgentDao;

    @Autowired
    private StockLevelDao stockLevelDao;

    @Autowired
    private StockPriceDao stockPriceDao;

    @Autowired
    private StockLogDao stockLogDao;

    @Autowired
    private com.zbkj.service.dao.StockLadderDao stockLadderDao;

    @Resource
    private UserService userService;

    @Resource
    private StoreProductService storeProductService;

    @Resource
    private com.zbkj.service.dao.StockOrderDao stockOrderDao;

    @Resource
    private com.zbkj.service.dao.StockOrderProductDao stockOrderProductDao;

    @Resource
    private com.zbkj.service.dao.StockChangeLogDao stockChangeLogDao;

    @Resource
    private com.zbkj.service.dao.StockProductRelDao stockProductRelDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ==================== 层级 ====================

    @Override
    public List<StockLevel> getLevelList() {
        return stockLevelDao.selectList(new LambdaQueryWrapper<StockLevel>()
                .eq(StockLevel::getIsDel, 0).orderByAsc(StockLevel::getSort));
    }

    @Override
    public Boolean saveLevel(StockLevel level) {
        if (level.getId() != null && level.getId() > 0) {
            return stockLevelDao.updateById(level) > 0;
        }
        level.setIsDel(0);
        return stockLevelDao.insert(level) > 0;
    }

    @Override
    public Boolean deleteLevel(Integer id) {
        Integer used = stockAgentDao.selectCount(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getLevelId, id).eq(StockAgent::getIsDel, 0));
        if (used != null && used > 0) {
            throw new CrmebException("该层级下存在订货代理，无法删除");
        }
        return stockLevelDao.deleteById(id) > 0;
    }

    // ==================== 后台代理管理 ====================

    @Override
    public CommonPage<StockAgent> getAdminAgentList(String keywords, Integer levelId, Integer status, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StockAgent> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockAgent::getIsDel, 0);
        if (levelId != null && levelId > 0) {
            lqw.eq(StockAgent::getLevelId, levelId);
        }
        if (status != null) {
            lqw.eq(StockAgent::getStatus, status);
        }
        // 关键词按用户昵称/手机号模糊（先查用户）
        if (keywords != null && !keywords.trim().isEmpty()) {
            List<User> users = userService.lambdaQuery()
                    .and(w -> w.like(User::getNickname, keywords.trim()).or().like(User::getPhone, keywords.trim()))
                    .list();
            if (users.isEmpty()) {
                return new CommonPage<>();
            }
            List<Integer> uids = new ArrayList<>();
            for (User u : users) {
                uids.add(u.getUid());
            }
            lqw.in(StockAgent::getUid, uids);
        }
        lqw.orderByDesc(StockAgent::getId);
        // ★ startPage 必须紧邻目标查询（上面的用户查询会吃掉分页参数）
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<StockAgent> list = stockAgentDao.selectList(lqw);
        fillAgents(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean saveAgent(StockAgentRequest request) {
        // 用户校验
        User user = userService.getById(request.getUid());
        if (user == null) {
            throw new CrmebException("用户不存在");
        }
        // 层级校验
        StockLevel level = stockLevelDao.selectById(request.getLevelId());
        if (level == null || level.getIsDel() == 1) {
            throw new CrmebException("层级不存在");
        }
        // 上级校验：上级层级必须更高（sort 更小）
        if (request.getParentId() != null && request.getParentId() > 0) {
            StockAgent parent = stockAgentDao.selectById(request.getParentId());
            if (parent == null || parent.getIsDel() == 1) {
                throw new CrmebException("上级代理不存在");
            }
            StockLevel parentLevel = stockLevelDao.selectById(parent.getLevelId());
            // 下级层级不得【高于】上级；同级允许（平推，用于产生平级奖励）
            if (parentLevel == null || parentLevel.getSort() > level.getSort()) {
                throw new CrmebException("下级代理层级不得高于上级代理");
            }
        }
        if (request.getId() != null && request.getId() > 0) {
            StockAgent exist = stockAgentDao.selectById(request.getId());
            if (exist == null || exist.getIsDel() == 1) {
                throw new CrmebException("代理不存在");
            }
            Integer oldLevelId = exist.getLevelId();
            Integer oldParentId = exist.getParentId();
            // 修改层级时校验不能高于其上级
            if (request.getParentId() != null && !request.getParentId().equals(exist.getParentId())) {
                exist.setParentId(request.getParentId());
            }
            exist.setLevelId(request.getLevelId());
            exist.setStatus(request.getStatus() == null ? exist.getStatus() : request.getStatus());
            exist.setMark(request.getMark() == null ? exist.getMark() : request.getMark());
            // 变更上级/层级时做合法性校验（避免成环）
            if (exist.getParentId() != null && exist.getParentId() > 0) {
                Set<Integer> subIds = new HashSet<>(collectSubAgentIds(exist.getId()));
                if (subIds.contains(exist.getParentId())) {
                    throw new CrmebException("上级不能是自己团队的下级代理");
                }
            }
            boolean ok = stockAgentDao.updateById(exist) > 0;
            if (ok) {
                if (!oldLevelId.equals(exist.getLevelId())) {
                    logChange(exist.getId(), exist.getUid(), StockChangeLog.TYPE_LEVEL,
                            levelName(oldLevelId), levelName(exist.getLevelId()), "后台修改层级");
                }
                if (!oldParentId.equals(exist.getParentId())) {
                    logChange(exist.getId(), exist.getUid(), StockChangeLog.TYPE_PARENT,
                            parentName(oldParentId), parentName(exist.getParentId()), "后台修改上级");
                }
            }
            return ok;
        }
        // 新增：同一用户只能有一个订货代理身份
        Integer count = stockAgentDao.selectCount(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getUid, request.getUid()).eq(StockAgent::getIsDel, 0));
        if (count != null && count > 0) {
            throw new CrmebException("该用户已是订货代理");
        }
        StockAgent agent = new StockAgent();
        agent.setUid(request.getUid());
        agent.setLevelId(request.getLevelId());
        agent.setParentId(request.getParentId() == null ? 0 : request.getParentId());
        agent.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        agent.setMark(request.getMark() == null ? "" : request.getMark());
        agent.setIsDel(0);
        boolean ok = stockAgentDao.insert(agent) > 0;
        if (ok) {
            logChange(agent.getId(), agent.getUid(), StockChangeLog.TYPE_ADD,
                    null, levelName(agent.getLevelId()), "后台新增订货商");
        }
        return ok;
    }

    @Override
    public Boolean deleteAgent(Integer id) {
        Integer subCount = stockAgentDao.selectCount(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getParentId, id).eq(StockAgent::getIsDel, 0));
        if (subCount != null && subCount > 0) {
            throw new CrmebException("该代理存在下级，请先处理下级代理");
        }
        StockAgent exist = stockAgentDao.selectById(id);
        StockAgent agent = new StockAgent();
        agent.setId(id);
        agent.setIsDel(1);
        boolean ok = stockAgentDao.updateById(agent) > 0;
        if (ok && exist != null) {
            logChange(exist.getId(), exist.getUid(), StockChangeLog.TYPE_DELETE,
                    levelName(exist.getLevelId()), null, "后台删除订货商");
        }
        return ok;
    }

    @Override
    public Boolean changeAgentStatus(Integer id, Integer status) {
        LambdaUpdateWrapper<StockAgent> luw = new LambdaUpdateWrapper<>();
        luw.eq(StockAgent::getId, id).set(StockAgent::getStatus, status);
        boolean ok = stockAgentDao.update(null, luw) > 0;
        if (ok) {
            StockAgent agent = stockAgentDao.selectById(id);
            if (agent != null) {
                logChange(id, agent.getUid(), StockChangeLog.TYPE_STATUS,
                        agent.getStatus() != null && agent.getStatus() == 1 ? "禁用" : "启用",
                        status != null && status == 1 ? "启用" : "禁用",
                        "后台" + (status != null && status == 1 ? "启用" : "禁用") + "订货商");
            }
        }
        return ok;
    }

    // ==================== 商品与拿货价 ====================

    @Override
    public HashMap<String, Object> getProductList(String keywords, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StoreProduct> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StoreProduct::getIsDel, 0).eq(StoreProduct::getIsShow, true);
        // 仅显示已加入订货模块的商品（eb_stock_product_rel 加入制）
        List<Integer> relIds = getStockProductIds();
        if (relIds.isEmpty()) {
            HashMap<String, Object> empty = new HashMap<>();
            empty.put("list", new ArrayList<>());
            empty.put("total", 0);
            empty.put("levels", getLevelList());
            return empty;
        }
        lqw.in(StoreProduct::getId, relIds);
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(StoreProduct::getStoreName, keywords.trim());
        }
        lqw.orderByDesc(StoreProduct::getId);
        List<StockLevel> levels = getLevelList();
        List<HashMap<String, Object>> rows = new ArrayList<>();
        // ★ PageHelper.startPage 必须紧邻目标查询（中间不能插入其它查询，否则分页会被它吃掉）
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<StoreProduct> productList = storeProductService.list(lqw);
        PageInfo<StoreProduct> productPage = new PageInfo<>(productList);
        for (StoreProduct p : productList) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("storeName", p.getStoreName());
            row.put("image", p.getImage());
            row.put("price", p.getPrice());
            row.put("stock", p.getStock());
            // 各层级拿货价
            List<StockPrice> prices = stockPriceDao.selectList(new LambdaQueryWrapper<StockPrice>()
                    .eq(StockPrice::getProductId, p.getId()));
            HashMap<Integer, BigDecimal> priceMap = new HashMap<>();
            for (StockPrice sp : prices) {
                priceMap.put(sp.getLevelId(), sp.getPrice());
            }
            List<HashMap<String, Object>> levelPrices = new ArrayList<>();
            for (StockLevel lv : levels) {
                HashMap<String, Object> lp = new HashMap<>();
                lp.put("levelId", lv.getId());
                lp.put("levelName", lv.getName());
                lp.put("discount", lv.getDiscount());
                lp.put("price", priceMap.get(lv.getId()));
                levelPrices.add(lp);
            }
            row.put("levelPrices", levelPrices);
            rows.add(row);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", rows);
        map.put("total", productPage.getTotal());
        map.put("levels", levels);
        return map;
    }

    /** 已加入订货模块的商品ID集合 */
    private List<Integer> getStockProductIds() {
        List<Integer> ids = new ArrayList<>();
        for (StockProductRel rel : getStockProductRelList()) {
            ids.add(rel.getProductId());
        }
        return ids;
    }

    @Override
    public List<StockProductRel> getStockProductRelList() {
        return stockProductRelDao.selectList(null);
    }

    @Override
    public HashMap<String, Object> getSelectableProductList(String keywords, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StoreProduct> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StoreProduct::getIsDel, 0).eq(StoreProduct::getIsShow, true);
        List<Integer> relIds = getStockProductIds();
        if (!relIds.isEmpty()) {
            lqw.notIn(StoreProduct::getId, relIds);
        }
        if (keywords != null && !keywords.trim().isEmpty()) {
            lqw.like(StoreProduct::getStoreName, keywords.trim());
        }
        lqw.orderByDesc(StoreProduct::getId);
        // ★ PageHelper.startPage 必须紧邻目标查询
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<StoreProduct> productList = storeProductService.list(lqw);
        PageInfo<StoreProduct> productPage = new PageInfo<>(productList);
        List<HashMap<String, Object>> rows = new ArrayList<>();
        for (StoreProduct p : productList) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("storeName", p.getStoreName());
            row.put("image", p.getImage());
            row.put("price", p.getPrice());
            row.put("stock", p.getStock());
            rows.add(row);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", rows);
        map.put("total", productPage.getTotal());
        return map;
    }

    @Override
    public Boolean addProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new CrmebException("请选择要添加的商品");
        }
        return transactionTemplate.execute(status -> {
            List<Integer> existIds = getStockProductIds();
            Date now = new Date();
            for (Integer pid : productIds) {
                StoreProduct product = storeProductService.getById(pid);
                if (product == null || product.getIsDel()) {
                    throw new CrmebException("商品不存在（ID:" + pid + "）");
                }
                if (existIds.contains(pid)) {
                    continue;
                }
                StockProductRel rel = new StockProductRel();
                rel.setProductId(pid);
                rel.setCreateTime(now);
                stockProductRelDao.insert(rel);
            }
            return true;
        });
    }

    @Override
    public Boolean removeStockProduct(Integer productId) {
        return stockProductRelDao.delete(new LambdaQueryWrapper<StockProductRel>()
                .eq(StockProductRel::getProductId, productId)) >= 0;
    }

    // ==================== 订货商变更记录 ====================

    @Override
    public CommonPage<StockChangeLog> getChangeLogList(Integer uid, Integer type, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StockChangeLog> lqw = new LambdaQueryWrapper<>();
        if (uid != null && uid > 0) {
            lqw.eq(StockChangeLog::getUid, uid);
        }
        if (type != null && type > 0) {
            lqw.eq(StockChangeLog::getType, type);
        }
        lqw.orderByDesc(StockChangeLog::getId);
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<StockChangeLog> list = stockChangeLogDao.selectList(lqw);
        if (!list.isEmpty()) {
            List<Integer> uids = new ArrayList<>();
            for (StockChangeLog log : list) {
                uids.add(log.getUid());
            }
            Map<Integer, User> userMap = new HashMap<>();
            for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
                userMap.put(u.getUid(), u);
            }
            for (StockChangeLog log : list) {
                User u = userMap.get(log.getUid());
                log.setNickname(u == null ? "" : u.getNickname());
                log.setPhone(u == null ? "" : u.getPhone());
            }
        }
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public void logChange(Integer agentId, Integer uid, Integer type, String oldValue, String newValue, String mark) {
        try {
            StockChangeLog log = new StockChangeLog();
            log.setAgentId(agentId);
            log.setUid(uid);
            log.setType(type);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setMark(mark);
            stockChangeLogDao.insert(log);
        } catch (Exception e) {
            // 变更记录写入失败不影响主流程
        }
    }

    /** 层级名称（异常/删除时兜底显示原ID） */
    private String levelName(Integer levelId) {
        if (levelId == null) {
            return "";
        }
        StockLevel lv = stockLevelDao.selectById(levelId);
        return lv == null ? ("层级ID:" + levelId) : lv.getName();
    }

    /** 上级显示名（0=总部） */
    private String parentName(Integer parentId) {
        if (parentId == null || parentId <= 0) {
            return "总部";
        }
        StockAgent parent = stockAgentDao.selectById(parentId);
        if (parent == null) {
            return "总部";
        }
        User u = userService.getById(parent.getUid());
        return u == null ? ("代理ID:" + parent.getId()) : u.getNickname();
    }

    @Override
    public Boolean savePrice(StockRequests.StockPriceSetRequest request) {
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (product == null || product.getIsDel()) {
            throw new CrmebException("商品不存在");
        }
        return transactionTemplate.execute(status -> {
            for (StockRequests.PriceItem item : request.getPrices()) {
                LambdaQueryWrapper<StockPrice> lqw = new LambdaQueryWrapper<>();
                lqw.eq(StockPrice::getProductId, request.getProductId()).eq(StockPrice::getLevelId, item.getLevelId());
                StockPrice exist = stockPriceDao.selectOne(lqw);
                if (item.getPrice() == null) {
                    // 清除专用价
                    if (exist != null) {
                        stockPriceDao.deleteById(exist.getId());
                    }
                } else if (exist != null) {
                    exist.setPrice(item.getPrice());
                    stockPriceDao.updateById(exist);
                } else {
                    StockPrice sp = new StockPrice();
                    sp.setProductId(request.getProductId());
                    sp.setLevelId(item.getLevelId());
                    sp.setPrice(item.getPrice());
                    stockPriceDao.insert(sp);
                }
            }
            return true;
        });
    }

    @Override
    public Boolean adjustStock(StockRequests.StockAdjustRequest request, Integer adminId) {
        if (request.getChangeNum() == 0) {
            throw new CrmebException("变动数量不能为0");
        }
        return transactionTemplate.execute(status -> {
            StoreProduct product = storeProductService.getById(request.getProductId());
            if (product == null || product.getIsDel()) {
                throw new CrmebException("商品不存在");
            }
            int after = product.getStock() + request.getChangeNum();
            if (after < 0) {
                throw new CrmebException("库存不足，当前库存：" + product.getStock());
            }
            LambdaUpdateWrapper<StoreProduct> luw = new LambdaUpdateWrapper<>();
            luw.eq(StoreProduct::getId, product.getId())
                    .setSql("stock = stock + (" + request.getChangeNum() + ")");
            storeProductService.update(luw);
            StockLog log = new StockLog();
            log.setProductId(product.getId());
            log.setType(StockLog.TYPE_MANUAL);
            log.setChangeNum(request.getChangeNum());
            log.setBeforeStock(product.getStock());
            log.setAfterStock(after);
            log.setLinkNo("");
            log.setMark(request.getMark() == null ? "后台手动调整" : request.getMark());
            stockLogDao.insert(log);
            return true;
        });
    }

    @Override
    public CommonPage<StockLog> getLogList(Integer productId, Integer type, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<StockLog> lqw = new LambdaQueryWrapper<>();
        if (productId != null && productId > 0) {
            lqw.eq(StockLog::getProductId, productId);
        }
        if (type != null && type > 0) {
            lqw.eq(StockLog::getType, type);
        }
        lqw.orderByDesc(StockLog::getId);
        List<StockLog> list = stockLogDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> getMyAgentInfo(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        StockAgent agent = getAgentByUid(uid);
        if (agent == null) {
            map.put("isAgent", false);
            return map;
        }
        fillAgent(agent);
        map.put("isAgent", true);
        map.put("agent", agent);
        return map;
    }

    @Override
    public Boolean createSubAgent(Integer uid, StockAgentCreateRequest request) {
        StockAgent parent = getAgentByUid(uid);
        if (parent == null || parent.getStatus() == 0) {
            throw new CrmebException("您还不是订货代理或已被禁用");
        }
        StockLevel parentLevel = stockLevelDao.selectById(parent.getLevelId());
        StockLevel level = stockLevelDao.selectById(request.getLevelId());
        if (level == null || level.getIsDel() == 1) {
            throw new CrmebException("层级不存在");
        }
        // 下级层级不得【高于】自己；同级允许（平推同级代理，用于产生平级奖励）
        if (parentLevel == null || level.getSort() < parentLevel.getSort()) {
            throw new CrmebException("下级代理层级不得高于自己（同级平推或更低均可）");
        }
        // 按手机号找已注册用户
        User user = userService.lambdaQuery().eq(User::getPhone, request.getPhone().trim()).one();
        if (user == null) {
            throw new CrmebException("该手机号尚未注册，请让对方先注册会员");
        }
        Integer count = stockAgentDao.selectCount(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getUid, user.getUid()).eq(StockAgent::getIsDel, 0));
        if (count != null && count > 0) {
            throw new CrmebException("该用户已是订货代理");
        }
        StockAgent agent = new StockAgent();
        agent.setUid(user.getUid());
        agent.setLevelId(request.getLevelId());
        agent.setParentId(parent.getId());
        agent.setStatus(1);
        agent.setMark(request.getMark() == null ? "" : request.getMark());
        agent.setIsDel(0);
        return stockAgentDao.insert(agent) > 0;
    }

    @Override
    public List<StockAgent> getSubAgentList(Integer uid) {
        StockAgent agent = getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        List<StockAgent> list = stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getParentId, agent.getId()).eq(StockAgent::getIsDel, 0)
                .orderByDesc(StockAgent::getId));
        fillAgents(list);
        return list;
    }

    @Override
    public List<StockLevel> getLevelListForAgent(Integer uid) {
        StockAgent agent = getAgentByUid(uid);
        List<StockLevel> all = getLevelList();
        if (agent == null) {
            return all;
        }
        StockLevel mine = stockLevelDao.selectById(agent.getLevelId());
        List<StockLevel> result = new ArrayList<>();
        for (StockLevel lv : all) {
            // 允许选同级（平推）或更低层级，不允许更高
            if (mine == null || lv.getSort() >= mine.getSort()) {
                result.add(lv);
            }
        }
        return result;
    }

    // ==================== 公共 ====================

    @Override
    public StockAgent getAgentByUid(Integer uid) {
        List<StockAgent> list = stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getUid, uid).eq(StockAgent::getIsDel, 0)
                .orderByDesc(StockAgent::getId).last(" limit 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public StockAgent getAgentById(Integer agentId) {
        StockAgent agent = stockAgentDao.selectById(agentId);
        if (agent != null && agent.getIsDel() == 0) {
            return agent;
        }
        return null;
    }

    @Override
    public BigDecimal getProductPrice(StockAgent agent, Integer productId) {
        StoreProduct product = storeProductService.getById(productId);
        if (product == null || product.getIsDel()) {
            throw new CrmebException("商品不存在");
        }
        if (agent == null) {
            return product.getPrice();
        }
        // 1) 专用价格表优先
        StockPrice sp = stockPriceDao.selectOne(new LambdaQueryWrapper<StockPrice>()
                .eq(StockPrice::getProductId, productId).eq(StockPrice::getLevelId, agent.getLevelId()));
        if (sp != null) {
            return sp.getPrice();
        }
        // 2) 层级默认折扣
        StockLevel level = stockLevelDao.selectById(agent.getLevelId());
        if (level != null && level.getDiscount() != null && level.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            return product.getPrice().multiply(level.getDiscount())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        // 3) 零售价
        return product.getPrice();
    }

    @Override
    public void deductStock(Integer productId, Integer num, Integer type, String linkNo, String mark) {
        StoreProduct product = storeProductService.getById(productId);
        if (product == null) {
            throw new CrmebException("商品不存在");
        }
        int updated = storeProductService.update(new LambdaUpdateWrapper<StoreProduct>()
                .eq(StoreProduct::getId, productId)
                .ge(StoreProduct::getStock, num)
                .setSql("stock = stock - (" + num + ")")) ? 1 : 0;
        if (updated == 0) {
            throw new CrmebException("商品【" + product.getStoreName() + "】云仓库存不足，当前库存：" + product.getStock());
        }
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setType(type);
        log.setChangeNum(-num);
        log.setBeforeStock(product.getStock());
        log.setAfterStock(product.getStock() - num);
        log.setLinkNo(linkNo == null ? "" : linkNo);
        log.setMark(mark == null ? "" : mark);
        stockLogDao.insert(log);
    }

    @Override
    public void addStock(Integer productId, Integer num, Integer type, String linkNo, String mark) {
        StoreProduct product = storeProductService.getById(productId);
        if (product == null) {
            throw new CrmebException("商品不存在");
        }
        storeProductService.update(new LambdaUpdateWrapper<StoreProduct>()
                .eq(StoreProduct::getId, productId)
                .setSql("stock = stock + (" + num + ")"));
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setType(type);
        log.setChangeNum(num);
        log.setBeforeStock(product.getStock());
        log.setAfterStock(product.getStock() + num);
        log.setLinkNo(linkNo == null ? "" : linkNo);
        log.setMark(mark == null ? "" : mark);
        stockLogDao.insert(log);
    }

    @Override
    public List<Integer> collectSubAgentIds(Integer agentId) {
        List<Integer> result = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(agentId);
        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            List<StockAgent> children = stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                    .eq(StockAgent::getParentId, current).eq(StockAgent::getIsDel, 0));
            for (StockAgent child : children) {
                result.add(child.getId());
                queue.add(child.getId());
            }
        }
        return result;
    }

    @Override
    public List<StockAgent> getDirectChildren(Integer agentId) {
        return stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                .eq(StockAgent::getParentId, agentId).eq(StockAgent::getIsDel, 0));
    }

    @Override
    public List<StockLadder> getLadderList() {
        return stockLadderDao.selectList(new LambdaQueryWrapper<StockLadder>().orderByAsc(StockLadder::getSort));
    }

    @Override
    public Boolean saveLadders(List<StockLadder> ladders) {
        return transactionTemplate.execute(status -> {
            stockLadderDao.delete(null);
            int sort = 1;
            for (StockLadder ladder : ladders) {
                ladder.setId(null);
                ladder.setSort(sort++);
                stockLadderDao.insert(ladder);
            }
            return true;
        });
    }

    // ==================== 自动升级 ====================

    @Override
    public Boolean checkAndUpgrade(Integer uid) {
        StockAgent agent = getAgentByUid(uid);
        if (agent == null) {
            return false;
        }
        List<StockLevel> levels = getLevelList(); // sort 升序：数值越小层级越高
        int curIdx = -1;
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getId().equals(agent.getLevelId())) {
                curIdx = i;
                break;
            }
        }
        if (curIdx < 0) {
            return false;
        }

        BigDecimal selfBuy = sumPaidOrderAmount(java.util.Collections.singletonList(agent.getId()));
        List<Integer> directIds = new ArrayList<>();
        for (StockAgent child : getDirectChildren(agent.getId())) {
            directIds.add(child.getId());
        }
        BigDecimal directAmount = sumPaidOrderAmount(directIds);
        BigDecimal teamAmount = sumPaidOrderAmount(collectSubAgentIds(agent.getId()));
        Set<Integer> boughtProductIds = getBoughtProductIds(agent.getId());

        // 从高往低找第一个满足条件的更高层级
        for (int i = curIdx - 1; i >= 0; i--) {
            StockLevel level = levels.get(i);
            if (matchUpgradeCondition(level, selfBuy, directAmount, teamAmount, boughtProductIds)) {
                StockAgent update = new StockAgent();
                update.setId(agent.getId());
                update.setLevelId(level.getId());
                stockAgentDao.updateById(update);
                logChange(agent.getId(), agent.getUid(), StockChangeLog.TYPE_LEVEL,
                        levelName(agent.getLevelId()), levelName(level.getId()), "满足升级条件自动升级");
                return true;
            }
        }
        return false;
    }

    /** 判断某层级条件是否满足 */
    private boolean matchUpgradeCondition(StockLevel level, BigDecimal selfBuy, BigDecimal direct,
                                          BigDecimal team, Set<Integer> boughtProductIds) {
        List<Boolean> results = new ArrayList<>();
        if (Boolean.TRUE.equals(level.getCondSelfBuy())) {
            results.add(selfBuy.compareTo(nz(level.getSelfBuyAmount())) >= 0);
        }
        if (Boolean.TRUE.equals(level.getCondDirect())) {
            results.add(direct.compareTo(nz(level.getDirectOrderAmount())) >= 0);
        }
        if (Boolean.TRUE.equals(level.getCondTeam())) {
            results.add(team.compareTo(nz(level.getTeamAmount())) >= 0);
        }
        if (Boolean.TRUE.equals(level.getCondProduct()) && level.getUpgradeProductIds() != null
                && !level.getUpgradeProductIds().trim().isEmpty()) {
            boolean hit = false;
            for (String pid : level.getUpgradeProductIds().split(",")) {
                if (pid == null || pid.trim().isEmpty()) {
                    continue;
                }
                try {
                    if (boughtProductIds.contains(Integer.valueOf(pid.trim()))) {
                        hit = true;
                        break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            results.add(hit);
        }
        if (results.isEmpty()) {
            return false;
        }
        if (Integer.valueOf(1).equals(level.getConditionLogic())) { // 与：全部满足
            for (Boolean b : results) {
                if (!Boolean.TRUE.equals(b)) {
                    return false;
                }
            }
            return true;
        }
        for (Boolean b : results) { // 或：任一满足
            if (Boolean.TRUE.equals(b)) {
                return true;
            }
        }
        return false;
    }

    /** 统计一批代理已付款订单总额 */
    private BigDecimal sumPaidOrderAmount(List<Integer> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<com.zbkj.common.model.stock.StockOrder> orders = stockOrderDao.selectList(
                new LambdaQueryWrapper<com.zbkj.common.model.stock.StockOrder>()
                        .in(com.zbkj.common.model.stock.StockOrder::getAgentId, agentIds)
                        .eq(com.zbkj.common.model.stock.StockOrder::getPayStatus, 1)
                        .eq(com.zbkj.common.model.stock.StockOrder::getIsDel, 0));
        BigDecimal total = BigDecimal.ZERO;
        for (com.zbkj.common.model.stock.StockOrder order : orders) {
            total = total.add(order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice());
        }
        return total;
    }

    /** 某代理已购买过的商品ID集合 */
    private Set<Integer> getBoughtProductIds(Integer agentId) {
        Set<Integer> ids = new HashSet<>();
        List<com.zbkj.common.model.stock.StockOrder> orders = stockOrderDao.selectList(
                new LambdaQueryWrapper<com.zbkj.common.model.stock.StockOrder>()
                        .eq(com.zbkj.common.model.stock.StockOrder::getAgentId, agentId)
                        .eq(com.zbkj.common.model.stock.StockOrder::getIsDel, 0));
        if (orders.isEmpty()) {
            return ids;
        }
        List<Integer> orderIds = new ArrayList<>();
        for (com.zbkj.common.model.stock.StockOrder o : orders) {
            orderIds.add(o.getId());
        }
        List<com.zbkj.common.model.stock.StockOrderProduct> products = stockOrderProductDao.selectList(
                new LambdaQueryWrapper<com.zbkj.common.model.stock.StockOrderProduct>()
                        .in(com.zbkj.common.model.stock.StockOrderProduct::getOrderId, orderIds));
        for (com.zbkj.common.model.stock.StockOrderProduct p : products) {
            ids.add(p.getProductId());
        }
        return ids;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Override
    public Map<Integer, Integer> getAgentUidMap(List<Integer> agentIds) {
        Map<Integer, Integer> map = new HashMap<>();
        if (agentIds == null || agentIds.isEmpty()) {
            return map;
        }
        List<StockAgent> agents = stockAgentDao.selectBatchIds(agentIds);
        for (StockAgent a : agents) {
            map.put(a.getId(), a.getUid());
        }
        return map;
    }

    // ==================== 内部 ====================

    private void fillAgents(List<StockAgent> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Integer, StockLevel> levelMap = new HashMap<>();
        for (StockLevel lv : getLevelList()) {
            levelMap.put(lv.getId(), lv);
        }
        List<Integer> uids = new ArrayList<>();
        List<Integer> parentIds = new ArrayList<>();
        for (StockAgent a : list) {
            uids.add(a.getUid());
            if (a.getParentId() != null && a.getParentId() > 0) {
                parentIds.add(a.getParentId());
            }
        }
        Map<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
            userMap.put(u.getUid(), u);
        }
        Map<Integer, String> parentNameMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            for (StockAgent p : stockAgentDao.selectBatchIds(parentIds)) {
                User pu = userService.getById(p.getUid());
                parentNameMap.put(p.getId(), pu == null ? "" : pu.getNickname());
            }
        }
        for (StockAgent a : list) {
            StockLevel lv = levelMap.get(a.getLevelId());
            a.setLevelName(lv == null ? "" : lv.getName());
            a.setLevelSort(lv == null ? 0 : lv.getSort());
            User u = userMap.get(a.getUid());
            a.setNickname(u == null ? "" : u.getNickname());
            a.setPhone(u == null ? "" : u.getPhone());
            a.setParentName(parentNameMap.get(a.getParentId()) == null ? "总部" : parentNameMap.get(a.getParentId()));
        }
    }

    private void fillAgent(StockAgent agent) {
        List<StockAgent> one = new ArrayList<>();
        one.add(agent);
        fillAgents(one);
    }
}
