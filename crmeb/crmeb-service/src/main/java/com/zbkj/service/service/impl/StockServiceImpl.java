package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.stock.StockAdjustLog;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockChangeLog;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockLadder;
import com.zbkj.common.model.stock.StockLog;
import com.zbkj.common.model.stock.StockPrice;
import com.zbkj.common.model.stock.StockPriceSku;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.stock.StockProductRel;
import com.zbkj.common.model.stock.StockVirtualStock;
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

    @Autowired
    private com.zbkj.service.dao.StockPriceSkuDao stockPriceSkuDao;

    @Autowired
    private com.zbkj.service.dao.StoreProductAttrValueDao storeProductAttrValueDao;

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
    private com.zbkj.service.dao.StockVirtualStockDao stockVirtualStockDao;

    @Resource
    private com.zbkj.service.dao.StockExchangeDao stockExchangeDao;

    @Resource
    private com.zbkj.service.dao.StockAdjustLogDao stockAdjustLogDao;

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
    public CommonPage<StockAgent> getAdminAgentList(String keywords, Integer uid, Integer levelId, Integer status, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<StockAgent> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockAgent::getIsDel, 0);
        // 按订货商UID精确查询
        if (uid != null && uid > 0) {
            lqw.eq(StockAgent::getUid, uid);
        }
        if (levelId != null && levelId > 0) {
            lqw.eq(StockAgent::getLevelId, levelId);
        }
        if (status != null) {
            lqw.eq(StockAgent::getStatus, status);
        }
        // 关键词按用户昵称/手机号/UID模糊（先查用户）
        if (keywords != null && !keywords.trim().isEmpty()) {
            String kw = keywords.trim();
            List<User> users = userService.lambdaQuery()
                    .and(w -> w.like(User::getNickname, kw).or().like(User::getPhone, kw))
                    .list();
            if (users.isEmpty()) {
                // 关键词为纯数字时按UID精确匹配订货商
                if (kw.matches("\\d+")) {
                    lqw.eq(StockAgent::getUid, Long.parseLong(kw));
                } else {
                    return new CommonPage<>();
                }
            } else {
                List<Integer> uids = new ArrayList<>();
                for (User u : users) {
                    uids.add(u.getUid());
                }
                lqw.in(StockAgent::getUid, uids);
                if (kw.matches("\\d+")) {
                    lqw.or().eq(StockAgent::getUid, Long.parseLong(kw));
                }
            }
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
    public StockProductRel getProductRel(Integer productId) {
        return stockProductRelDao.selectOne(new LambdaQueryWrapper<StockProductRel>()
                .eq(StockProductRel::getProductId, productId).last(" limit 1"));
    }

    @Override
    public void saveProductStockType(Integer productId, Boolean supportVirtual, Boolean supportPhysical) {
        StockProductRel rel = getProductRel(productId);
        if (rel == null) {
            throw new CrmebException("该商品尚未加入订货模块");
        }
        rel.setSupportVirtual(supportVirtual == null ? Boolean.TRUE : supportVirtual);
        rel.setSupportPhysical(supportPhysical == null ? Boolean.TRUE : supportPhysical);
        stockProductRelDao.updateById(rel);
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
        // 规格级拿货价（skuKey 非空时写入 eb_stock_price_sku）
        if (request.getSkuKey() != null && !request.getSkuKey().trim().isEmpty()) {
            String sku = request.getSkuKey().trim();
            return transactionTemplate.execute(status -> {
                for (StockRequests.PriceItem item : request.getPrices()) {
                    StockPriceSku exist = stockPriceSkuDao.selectOne(new LambdaQueryWrapper<StockPriceSku>()
                            .eq(StockPriceSku::getProductId, request.getProductId())
                            .eq(StockPriceSku::getSkuKey, sku)
                            .eq(StockPriceSku::getLevelId, item.getLevelId())
                            .last(" limit 1"));
                    if (item.getPrice() == null) {
                        if (exist != null) {
                            stockPriceSkuDao.deleteById(exist.getId());
                        }
                    } else if (exist != null) {
                        exist.setPrice(item.getPrice());
                        stockPriceSkuDao.updateById(exist);
                    } else {
                        StockPriceSku sps = new StockPriceSku();
                        sps.setProductId(request.getProductId());
                        sps.setSkuKey(sku);
                        sps.setLevelId(item.getLevelId());
                        sps.setPrice(item.getPrice());
                        stockPriceSkuDao.insert(sps);
                    }
                }
                return true;
            });
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
        fillLogs(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /** 库存日志补充：商品名称 + 关联会员信息（由关联单号反查订货单/换货单的会员） */
    private void fillLogs(List<StockLog> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        HashMap<Integer, String> productNameMap = new HashMap<>();
        for (StockLog log : list) {
            if (log.getProductId() == null) {
                continue;
            }
            if (!productNameMap.containsKey(log.getProductId())) {
                StoreProduct p = storeProductService.getById(log.getProductId());
                productNameMap.put(log.getProductId(), p == null ? ("商品" + log.getProductId()) : p.getStoreName());
            }
        }
        // 关联单号 -> 会员（订货单号 / 换货单号）
        List<String> linkNos = new ArrayList<>();
        for (StockLog log : list) {
            if (log.getLinkNo() != null && !log.getLinkNo().trim().isEmpty()) {
                linkNos.add(log.getLinkNo().trim());
            }
        }
        HashMap<String, Integer> linkUidMap = new HashMap<>();
        if (!linkNos.isEmpty()) {
            for (com.zbkj.common.model.stock.StockOrder o : stockOrderDao.selectList(
                    new LambdaQueryWrapper<com.zbkj.common.model.stock.StockOrder>()
                            .in(com.zbkj.common.model.stock.StockOrder::getOrderNo, linkNos))) {
                linkUidMap.put(o.getOrderNo(), o.getUid());
            }
            for (com.zbkj.common.model.stock.StockExchange e : stockExchangeDao.selectList(
                    new LambdaQueryWrapper<com.zbkj.common.model.stock.StockExchange>()
                            .in(com.zbkj.common.model.stock.StockExchange::getExchangeNo, linkNos))) {
                linkUidMap.put(e.getExchangeNo(), e.getUid());
            }
        }
        HashMap<Integer, User> userMap = new HashMap<>();
        for (Integer uid : linkUidMap.values()) {
            if (uid != null && !userMap.containsKey(uid)) {
                User u = userService.getById(uid);
                if (u != null) {
                    userMap.put(uid, u);
                }
            }
        }
        for (StockLog log : list) {
            log.setProductName(productNameMap.get(log.getProductId()));
            if (log.getLinkNo() == null || log.getLinkNo().trim().isEmpty()) {
                continue;
            }
            Integer uid = linkUidMap.get(log.getLinkNo().trim());
            if (uid == null) {
                continue;
            }
            User u = userMap.get(uid);
            log.setUid(uid);
            log.setNickName(u == null ? "" : u.getNickname());
            log.setPhone(u == null ? "" : u.getPhone());
        }
    }

    @Override
    public List<HashMap<String, Object>> getAgentTeam(Integer agentId, Integer uid) {
        List<HashMap<String, Object>> out = new ArrayList<>();
        StockAgent root = agentId != null && agentId > 0 ? getAgentById(agentId)
                : (uid != null && uid > 0 ? getAgentByUid(uid) : null);
        if (root == null) {
            return out;
        }
        // 按层展开（广度优先），depth=1 为直接下级
        List<StockAgent> current = new ArrayList<>();
        current.add(root);
        int depth = 0;
        Set<Integer> visited = new HashSet<>();
        visited.add(root.getId());
        while (!current.isEmpty() && depth < 20) {
            depth++;
            List<Integer> ids = new ArrayList<>();
            for (StockAgent a : current) {
                ids.add(a.getId());
            }
            List<StockAgent> children = stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                    .in(StockAgent::getParentId, ids)
                    .eq(StockAgent::getIsDel, 0));
            List<StockAgent> next = new ArrayList<>();
            for (StockAgent c : children) {
                if (visited.contains(c.getId())) {
                    continue;
                }
                visited.add(c.getId());
                fillAgent(c);
                HashMap<String, Object> row = new HashMap<>();
                row.put("agentId", c.getId());
                row.put("uid", c.getUid());
                row.put("nickname", c.getNickname());
                row.put("phone", c.getPhone());
                row.put("levelName", c.getLevelName());
                row.put("status", c.getStatus());
                row.put("depth", depth);
                row.put("parentId", c.getParentId());
                out.add(row);
                next.add(c);
            }
            current = next;
        }
        return out;
    }

    @Override
    public void adjustAgentVirtualStock(StockRequests.StockAgentAdjustRequest request) {
        StockAgent agent = resolveAgent(request.getAgentId(), request.getUid());
        String sku = request.getSkuKey() == null ? "" : request.getSkuKey().trim();
        if (request.getNum() == null || request.getNum() == 0) {
            throw new CrmebException("调整数量不能为0");
        }
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (product == null || product.getIsDel()) {
            throw new CrmebException("商品不存在");
        }
        transactionTemplate.executeWithoutResult(status -> {
            StockVirtualStock vs = stockVirtualStockDao.selectOne(new LambdaQueryWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getUid, agent.getUid())
                    .eq(StockVirtualStock::getProductId, request.getProductId())
                    .eq(StockVirtualStock::getSkuKey, sku)
                    .eq(StockVirtualStock::getIsDel, 0)
                    .last(" limit 1"));
            if (vs == null) {
                if (request.getNum() < 0) {
                    throw new CrmebException("该订货商无此虚拟库存记录，无法扣减");
                }
                StockVirtualStock n = new StockVirtualStock();
                n.setUid(agent.getUid());
                n.setProductId(request.getProductId());
                n.setProductName(product.getStoreName());
                n.setImage(product.getImage());
                n.setSkuKey(sku);
                n.setNum(request.getNum());
                n.setRemainNum(request.getNum());
                n.setSourceOrderNo("ADJUST" + System.currentTimeMillis());
                n.setParentAgentId(agent.getParentId() == null ? 0 : agent.getParentId());
                n.setIsDel(0);
                stockVirtualStockDao.insert(n);
            } else {
                int remain = (vs.getRemainNum() == null ? 0 : vs.getRemainNum()) + request.getNum();
                int total = (vs.getNum() == null ? 0 : vs.getNum()) + request.getNum();
                if (remain < 0 || total < 0) {
                    throw new CrmebException("扣减数量超出该订货商虚拟库存（当前可提：" + vs.getRemainNum() + "）");
                }
                stockVirtualStockDao.update(null, new LambdaUpdateWrapper<StockVirtualStock>()
                        .eq(StockVirtualStock::getId, vs.getId())
                        .set(StockVirtualStock::getNum, total)
                        .set(StockVirtualStock::getRemainNum, remain));
            }
            writeAgentAdjustLog(agent, request, 2);
        });
    }

    @Override
    public void adjustAgentPhysicalStock(StockRequests.StockAgentAdjustRequest request) {
        StockAgent agent = resolveAgent(request.getAgentId(), request.getUid());
        if (request.getNum() == null || request.getNum() == 0) {
            throw new CrmebException("调整数量不能为0");
        }
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (product == null || product.getIsDel()) {
            throw new CrmebException("商品不存在");
        }
        transactionTemplate.executeWithoutResult(status -> {
            // 实体库存调整同步反映到云仓库存（增加=总部补货入库，减少=总部回收出库）
            String linkNo = "ADJ" + System.currentTimeMillis();
            if (request.getNum() > 0) {
                addStockBySku(request.getProductId(), request.getSkuKey(), request.getNum(), 5, linkNo,
                        "后台调整订货商实体库存（增加）");
            } else {
                deductStockBySku(request.getProductId(), request.getSkuKey(), -request.getNum(), 5, linkNo,
                        "后台调整订货商实体库存（扣减）");
            }
            writeAgentAdjustLog(agent, request, 1);
        });
    }

    /** 写订货商库存调整记录（实体调整参与实体库存推导，虚拟调整仅留痕） */
    private void writeAgentAdjustLog(StockAgent agent, StockRequests.StockAgentAdjustRequest request, int stockType) {
        StockAdjustLog log = new StockAdjustLog();
        log.setAgentId(agent.getId());
        log.setUid(agent.getUid());
        log.setProductId(request.getProductId());
        log.setSkuKey(request.getSkuKey() == null ? "" : request.getSkuKey().trim());
        log.setStockType(stockType);
        log.setNum(request.getNum());
        log.setMark(request.getMark() == null ? "" : request.getMark());
        log.setIsDel(0);
        stockAdjustLogDao.insert(log);
    }

    private StockAgent resolveAgent(Integer agentId, Integer uid) {
        StockAgent agent = null;
        if (agentId != null && agentId > 0) {
            agent = getAgentById(agentId);
        } else if (uid != null && uid > 0) {
            agent = getAgentByUid(uid);
        }
        if (agent == null) {
            throw new CrmebException("订货商不存在");
        }
        return agent;
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
        // 会员端展示：订货商级别与上级UID（0/空=总部）
        map.put("uid", agent.getUid());
        map.put("levelName", agent.getLevelName());
        Integer parentUid = 0;
        if (agent.getParentId() != null && agent.getParentId() > 0) {
            StockAgent parent = stockAgentDao.selectById(agent.getParentId());
            if (parent != null) {
                parentUid = parent.getUid() == null ? 0 : parent.getUid();
            }
        }
        map.put("parentUid", parentUid);
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
    public BigDecimal getProductPrice(StockAgent agent, Integer productId, String skuKey) {
        if (skuKey == null || skuKey.trim().isEmpty()) {
            return getProductPrice(agent, productId);
        }
        String sku = skuKey.trim();
        StoreProduct product = storeProductService.getById(productId);
        if (product == null || product.getIsDel()) {
            throw new CrmebException("商品不存在");
        }
        StoreProductAttrValue av = storeProductAttrValueDao.selectOne(new LambdaQueryWrapper<StoreProductAttrValue>()
                .eq(StoreProductAttrValue::getProductId, productId)
                .eq(StoreProductAttrValue::getSuk, sku)
                .eq(StoreProductAttrValue::getIsDel, false)
                .last(" limit 1"));
        if (av == null) {
            throw new CrmebException("商品规格不存在或已删除");
        }
        if (agent == null) {
            return av.getPrice() == null ? product.getPrice() : av.getPrice();
        }
        // 1) 规格级专用价
        StockPriceSku sps = stockPriceSkuDao.selectOne(new LambdaQueryWrapper<StockPriceSku>()
                .eq(StockPriceSku::getProductId, productId)
                .eq(StockPriceSku::getSkuKey, sku)
                .eq(StockPriceSku::getLevelId, agent.getLevelId())
                .last(" limit 1"));
        if (sps != null && sps.getPrice() != null) {
            return sps.getPrice();
        }
        // 2) 商品级专用价
        StockPrice sp = stockPriceDao.selectOne(new LambdaQueryWrapper<StockPrice>()
                .eq(StockPrice::getProductId, productId).eq(StockPrice::getLevelId, agent.getLevelId()));
        if (sp != null) {
            return sp.getPrice();
        }
        // 3) 层级折扣 × 规格零售价
        BigDecimal base = av.getPrice() == null ? product.getPrice() : av.getPrice();
        StockLevel level = stockLevelDao.selectById(agent.getLevelId());
        if (level != null && level.getDiscount() != null && level.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            return base.multiply(level.getDiscount()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        // 4) 规格零售价
        return base;
    }

    @Override
    public List<java.util.HashMap<String, Object>> getProductSkuList(Integer productId) {
        List<java.util.HashMap<String, Object>> rows = new ArrayList<>();
        for (StoreProductAttrValue av : storeProductAttrValueDao.selectList(
                new LambdaQueryWrapper<StoreProductAttrValue>()
                        .eq(StoreProductAttrValue::getProductId, productId)
                        .eq(StoreProductAttrValue::getIsDel, false)
                        .orderByAsc(StoreProductAttrValue::getId))) {
            java.util.HashMap<String, Object> row = new java.util.HashMap<>();
            row.put("skuKey", av.getSuk());
            row.put("attrValue", av.getAttrValue());
            row.put("price", av.getPrice());
            row.put("stock", av.getStock());
            row.put("image", av.getImage());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<java.util.HashMap<String, Object>> getPriceSkuList(Integer productId, String skuKey) {
        List<java.util.HashMap<String, Object>> rows = new ArrayList<>();
        if (skuKey == null || skuKey.trim().isEmpty()) {
            return rows;
        }
        for (StockPriceSku sps : stockPriceSkuDao.selectList(new LambdaQueryWrapper<StockPriceSku>()
                .eq(StockPriceSku::getProductId, productId)
                .eq(StockPriceSku::getSkuKey, skuKey.trim()))) {
            java.util.HashMap<String, Object> row = new java.util.HashMap<>();
            row.put("levelId", sps.getLevelId());
            row.put("price", sps.getPrice());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public int getSkuStock(Integer productId, String skuKey) {
        if (skuKey == null || skuKey.trim().isEmpty()) {
            StoreProduct p = storeProductService.getById(productId);
            return p == null || p.getStock() == null ? 0 : p.getStock();
        }
        StoreProductAttrValue av = storeProductAttrValueDao.selectOne(new LambdaQueryWrapper<StoreProductAttrValue>()
                .eq(StoreProductAttrValue::getProductId, productId)
                .eq(StoreProductAttrValue::getSuk, skuKey.trim())
                .eq(StoreProductAttrValue::getIsDel, false)
                .last(" limit 1"));
        return av == null || av.getStock() == null ? 0 : av.getStock();
    }

    @Override
    public void deductStockBySku(Integer productId, String skuKey, Integer num, Integer type, String linkNo, String mark) {
        if (skuKey == null || skuKey.trim().isEmpty()) {
            deductStock(productId, num, type, linkNo, mark);
            return;
        }
        String sku = skuKey.trim();
        int updated = storeProductAttrValueDao.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StoreProductAttrValue>()
                .eq(StoreProductAttrValue::getProductId, productId)
                .eq(StoreProductAttrValue::getSuk, sku)
                .eq(StoreProductAttrValue::getIsDel, false)
                .ge(StoreProductAttrValue::getStock, num)
                .setSql("stock = stock - (" + num + ")")) ;
        if (updated == 0) {
            throw new CrmebException("该规格云仓库存不足，当前库存：" + getSkuStock(productId, sku));
        }
        // 商品总库存同步扣减（失败记录日志，不回滚规格扣减由外层事务兜底）
        deductStock(productId, num, type, linkNo, mark);
    }

    @Override
    public void addStockBySku(Integer productId, String skuKey, Integer num, Integer type, String linkNo, String mark) {
        if (skuKey == null || skuKey.trim().isEmpty()) {
            addStock(productId, num, type, linkNo, mark);
            return;
        }
        storeProductAttrValueDao.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StoreProductAttrValue>()
                .eq(StoreProductAttrValue::getProductId, productId)
                .eq(StoreProductAttrValue::getSuk, skuKey.trim())
                .eq(StoreProductAttrValue::getIsDel, false)
                .setSql("stock = stock + (" + num + ")"));
        addStock(productId, num, type, linkNo, mark);
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
                        .eq(com.zbkj.common.model.stock.StockOrder::getPayStatus, 1)
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
            a.setAvatar(u == null ? "" : u.getAvatar());
            a.setParentName(parentNameMap.get(a.getParentId()) == null ? "总部" : parentNameMap.get(a.getParentId()));
        }
    }

    private void fillAgent(StockAgent agent) {
        List<StockAgent> one = new ArrayList<>();
        one.add(agent);
        fillAgents(one);
    }
}
