package com.qxkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.PayConstants;
import com.qxkj.common.constants.TaskConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.merchant.MerchantStoreVerifyRecord;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.product.ProductCommissionConfig;
import com.qxkj.common.model.product.StoreProduct;
import com.qxkj.common.model.system.SystemStore;
import com.qxkj.common.model.user.User;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.MerchantStoreRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.MerchantStoreNearVo;
import com.qxkj.common.response.StoreOrderVerificationConfirmResponse;
import com.qxkj.common.utils.ProductCommissionUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.vo.OrderInfoDetailVo;
import com.qxkj.common.vo.StoreOrderInfoOldVo;
import com.qxkj.service.dao.MerchantStoreVerifyRecordDao;
import com.qxkj.service.dao.StoreOrderDao;
import com.qxkj.service.dao.SystemStoreDao;
import com.qxkj.service.service.MerchantStoreService;
import com.qxkj.service.service.StoreOrderInfoService;
import com.qxkj.service.service.StoreProductService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserService;
import com.qxkj.service.service.WechatOrderShippingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门店系统 Service 实现
 * 门店基于提货点（eb_system_store）扩展：负责人绑定、履约服务开关、
 * 配送半径、三类服务费，以及门店核销记录。
 */
@Service
public class MerchantStoreServiceImpl implements MerchantStoreService {

    @Autowired
    private SystemStoreDao systemStoreDao;

    @Autowired
    private MerchantStoreVerifyRecordDao verifyRecordDao;

    @Autowired
    private StoreOrderDao storeOrderDao;

    @Autowired
    private UserService userService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private WechatOrderShippingService wechatOrderShippingService;

    // ==================== 后台 ====================

    @Override
    public CommonPage<SystemStore> adminPage(String keywords, Integer status, PageParamRequest pageRequest) {
        LambdaQueryWrapper<SystemStore> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SystemStore::getIsDel, false);
        if (StrUtil.isNotBlank(keywords)) {
            lqw.and(w -> w.like(SystemStore::getName, keywords.trim())
                    .or().like(SystemStore::getAddress, keywords.trim())
                    .or().like(SystemStore::getPhone, keywords.trim()));
        }
        if (status != null) {
            lqw.eq(SystemStore::getIsShow, status == 1);
        }
        lqw.orderByDesc(SystemStore::getId);
        PageHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
        List<SystemStore> list = systemStoreDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public SystemStore detail(Integer id) {
        SystemStore store = systemStoreDao.selectById(id);
        if (store == null || store.getIsDel()) {
            throw new QianxuException("门店不存在");
        }
        return store;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveStore(MerchantStoreRequest request) {
        checkNameUnique(request.getName(), null);
        SystemStore store = new SystemStore();
        BeanUtils.copyProperties(request, store);
        if (request.getLeaderUid() != null && request.getLeaderUid() > 0) {
            User leader = validateLeader(request.getLeaderUid());
            store.setLeaderName(leader.getNickname());
        } else {
            store.setLeaderUid(0);
            store.setLeaderName("");
        }
        store.setIsDel(false);
        return systemStoreDao.insert(store) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStore(MerchantStoreRequest request) {
        if (request.getId() == null || request.getId() <= 0) {
            throw new QianxuException("门店ID不能为空");
        }
        SystemStore exist = detail(request.getId());
        checkNameUnique(request.getName(), request.getId());
        SystemStore store = new SystemStore();
        BeanUtils.copyProperties(request, store);
        if (request.getLeaderUid() != null && request.getLeaderUid() > 0) {
            User leader = validateLeader(request.getLeaderUid());
            store.setLeaderName(leader.getNickname());
        } else {
            store.setLeaderUid(0);
            store.setLeaderName("");
        }
        store.setId(exist.getId());
        store.setIsDel(null);
        store.setUpdateTime(DateUtil.date());
        return systemStoreDao.updateById(store) > 0;
    }

    @Override
    public Boolean updateShow(Integer id, Boolean isShow) {
        SystemStore store = detail(id);
        SystemStore update = new SystemStore();
        update.setId(store.getId());
        update.setIsShow(isShow);
        update.setUpdateTime(DateUtil.date());
        return systemStoreDao.updateById(update) > 0;
    }

    @Override
    public Boolean deleteStore(Integer id) {
        SystemStore store = detail(id);
        SystemStore update = new SystemStore();
        update.setId(store.getId());
        update.setIsDel(true);
        update.setUpdateTime(DateUtil.date());
        return systemStoreDao.updateById(update) > 0;
    }

    @Override
    public CommonPage<MerchantStoreVerifyRecord> verifyRecords(Integer storeId, String orderNo, PageParamRequest pageRequest) {
        LambdaQueryWrapper<MerchantStoreVerifyRecord> lqw = new LambdaQueryWrapper<>();
        if (storeId != null && storeId > 0) {
            lqw.eq(MerchantStoreVerifyRecord::getStoreId, storeId);
        }
        if (StrUtil.isNotBlank(orderNo)) {
            lqw.like(MerchantStoreVerifyRecord::getOrderNo, orderNo.trim());
        }
        lqw.orderByDesc(MerchantStoreVerifyRecord::getId);
        PageHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
        List<MerchantStoreVerifyRecord> list = verifyRecordDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    // ==================== 用户端 ====================

    @Override
    public HashMap<String, Object> myStore(Integer uid) {
        HashMap<String, Object> result = new HashMap<>();
        SystemStore store = getStoreByLeader(uid);
        result.put("isLeader", store != null);
        if (store == null) {
            return result;
        }
        result.put("store", store);

        // 履约数据概览
        LambdaQueryWrapper<StoreOrder> pending = Wrappers.lambdaQuery();
        pending.eq(StoreOrder::getStoreId, store.getId())
                .eq(StoreOrder::getPaid, true).eq(StoreOrder::getRefundStatus, 0)
                .eq(StoreOrder::getStatus, Constants.ORDER_STATUS_INT_PAID)
                .eq(StoreOrder::getIsDel, false);
        int pendingCount = storeOrderDao.selectCount(pending);

        LambdaQueryWrapper<StoreOrder> total = Wrappers.lambdaQuery();
        total.eq(StoreOrder::getStoreId, store.getId())
                .eq(StoreOrder::getPaid, true).eq(StoreOrder::getRefundStatus, 0)
                .eq(StoreOrder::getIsDel, false);
        int orderCount = storeOrderDao.selectCount(total);

        LambdaQueryWrapper<MerchantStoreVerifyRecord> recordLqw = Wrappers.lambdaQuery();
        recordLqw.eq(MerchantStoreVerifyRecord::getStoreId, store.getId());
        int verifyCount = verifyRecordDao.selectCount(recordLqw);

        LambdaQueryWrapper<MerchantStoreVerifyRecord> feeLqw = Wrappers.lambdaQuery();
        feeLqw.eq(MerchantStoreVerifyRecord::getStoreId, store.getId());
        double feeSum = verifyRecordDao.selectList(feeLqw).stream()
                .mapToDouble(e -> e.getServiceFee() == null ? 0 : e.getServiceFee().doubleValue()).sum();

        result.put("pendingVerifyCount", pendingCount);
        result.put("orderCount", orderCount);
        result.put("verifyCount", verifyCount);
        result.put("serviceFeeSum", BigDecimal.valueOf(feeSum).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    @Override
    public List<MerchantStoreNearVo> nearby(String latitude, String longitude, Integer productId) {
        LambdaQueryWrapper<SystemStore> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SystemStore::getIsDel, false).eq(SystemStore::getIsShow, true);
        List<SystemStore> stores = systemStoreDao.selectList(lqw);
        if (stores.isEmpty()) {
            return new ArrayList<>();
        }

        // 产品门店服务权限过滤：产品开启门店服务时，按产品支持的自提/配送能力过滤
        Boolean needPickup = null;
        Boolean needDelivery = null;
        StoreProduct product = null;
        ProductCommissionConfig.Store storeFeeCfg = null;
        if (productId != null && productId > 0) {
            product = storeProductService.getById(productId);
            if (product != null && Boolean.TRUE.equals(product.getIsStore())) {
                needPickup = Boolean.TRUE.equals(product.getStoreSelfPickup());
                needDelivery = Boolean.TRUE.equals(product.getStoreDelivery());
            }
            if (product != null) {
                ProductCommissionConfig cfg = ProductCommissionUtil.parse(product.getCommissionConfig());
                storeFeeCfg = cfg == null ? null : cfg.getStore();
            }
        }

        double userLat = parseDouble(latitude);
        double userLng = parseDouble(longitude);
        List<MerchantStoreNearVo> result = new ArrayList<>();
        for (SystemStore store : stores) {
            MerchantStoreNearVo vo = new MerchantStoreNearVo();
            BeanUtils.copyProperties(store, vo);
            applyProductStoreFees(vo, store, storeFeeCfg, product);
            double distance = -1;
            if (userLat > 0 && userLng > 0 && StrUtil.isNotBlank(store.getLatitude()) && StrUtil.isNotBlank(store.getLongitude())) {
                distance = distanceKm(userLat, userLng, parseDouble(store.getLatitude()), parseDouble(store.getLongitude()));
                vo.setDistanceKm(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
            }
            boolean canPickup = Boolean.TRUE.equals(store.getSelfPickup())
                    && (needPickup == null || needPickup);
            boolean canDelivery = Boolean.TRUE.equals(store.getDelivery())
                    && (needDelivery == null || needDelivery)
                    && distance >= 0 && store.getDeliveryRadius() != null
                    && distance <= store.getDeliveryRadius().doubleValue();
            vo.setCanPickup(canPickup);
            vo.setCanDelivery(canDelivery);
            // 至少提供一种门店服务才返回
            if (canPickup || canDelivery) {
                result.add(vo);
            }
        }
        result.sort((a, b) -> {
            double da = a.getDistanceKm() == null ? Double.MAX_VALUE : a.getDistanceKm().doubleValue();
            double db = b.getDistanceKm() == null ? Double.MAX_VALUE : b.getDistanceKm().doubleValue();
            return Double.compare(da, db);
        });
        return result;
    }

    @Override
    public StoreOrderVerificationConfirmResponse previewVerifyOrder(String vCode, Integer uid) {
        SystemStore store = requireLeaderStore(uid);
        StoreOrder order = getVerifiableOrder(vCode);
        checkOrderBelongsToStore(order, store);
        StoreOrderVerificationConfirmResponse response = new StoreOrderVerificationConfirmResponse();
        BeanUtils.copyProperties(order, response);
        response.setStoreOrderInfoVos(storeOrderInfoService.getOrderListByOrderId(order.getId()));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyOrderByCode(String vCode, Integer uid) {
        SystemStore store = requireLeaderStore(uid);
        User leader = userService.getById(uid);
        StoreOrder order = getVerifiableOrder(vCode);
        checkOrderBelongsToStore(order, store);

        order.setStatus(Constants.ORDER_STATUS_INT_BARGAIN);
        order.setUpdateTime(DateUtil.date());
        boolean update = storeOrderDao.updateById(order) > 0;
        if (update) {
            // 后续任务（评价/积分等）放入redis
            redisUtil.lPush(TaskConstants.ORDER_TASK_REDIS_KEY_AFTER_TAKE_BY_USER, order.getId());
            // 小程序发货管理
            if (PayConstants.PAY_TYPE_WE_CHAT.equals(order.getPayType()) && Integer.valueOf(1).equals(order.getIsChannel())) {
                String shippingSwitch = systemConfigService.getValueByKey(
                        com.qxkj.common.constants.WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
                if (StrUtil.isNotBlank(shippingSwitch) && shippingSwitch.equals("1")) {
                    wechatOrderShippingService.uploadVerifyShippingInfo(order.getOrderId());
                }
            }
            writeVerifyRecord(order, uid, leader == null ? "" : leader.getNickname(), MerchantStoreVerifyRecord.SOURCE_LEADER);
        }
        return update;
    }

    @Override
    public CommonPage<MerchantStoreVerifyRecord> myVerifyRecords(Integer uid, PageParamRequest pageRequest) {
        SystemStore store = requireLeaderStore(uid);
        LambdaQueryWrapper<MerchantStoreVerifyRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantStoreVerifyRecord::getStoreId, store.getId());
        lqw.orderByDesc(MerchantStoreVerifyRecord::getId);
        PageHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
        List<MerchantStoreVerifyRecord> list = verifyRecordDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    // ==================== 公共 ====================

    @Override
    public void writeVerifyRecord(StoreOrder order, Integer verifyUid, String verifyName, int source) {
        MerchantStoreVerifyRecord record = new MerchantStoreVerifyRecord();
        record.setStoreId(order.getStoreId() == null ? 0 : order.getStoreId());
        SystemStore store = null;
        if (record.getStoreId() > 0) {
            store = systemStoreDao.selectById(record.getStoreId());
            record.setStoreName(store == null ? "" : store.getName());
        }
        record.setServiceFee(resolveOrderVerifyFee(order, store));
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderId());
        record.setVerifyCode(order.getVerifyCode());
        record.setProductInfo(buildProductSummary(order.getId()));
        record.setVerifyType(MerchantStoreVerifyRecord.TYPE_CODE);
        record.setPayPrice(order.getPayPrice());
        record.setOrderStatus(order.getStatus());
        record.setVerifyUid(verifyUid);
        record.setVerifyName(verifyName == null ? "" : verifyName);
        record.setVerifySource(source);
        record.setCreateTime(DateUtil.date());
        verifyRecordDao.insert(record);
    }

    // ==================== 私有方法 ====================

    private User validateLeader(Integer uid) {
        User user = userService.getById(uid);
        if (user == null || !Boolean.TRUE.equals(user.getStatus())) {
            throw new QianxuException("门店负责人用户不存在或已禁用");
        }
        return user;
    }

    private void checkNameUnique(String name, Integer excludeId) {
        LambdaQueryWrapper<SystemStore> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SystemStore::getName, name).eq(SystemStore::getIsDel, false);
        if (excludeId != null) {
            lqw.ne(SystemStore::getId, excludeId);
        }
        if (systemStoreDao.selectCount(lqw) > 0) {
            throw new QianxuException("门店名称已存在");
        }
    }

    private SystemStore getStoreByLeader(Integer uid) {
        LambdaQueryWrapper<SystemStore> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SystemStore::getLeaderUid, uid).eq(SystemStore::getIsDel, false);
        lqw.last(" limit 1 ");
        return systemStoreDao.selectOne(lqw);
    }

    private SystemStore requireLeaderStore(Integer uid) {
        SystemStore store = getStoreByLeader(uid);
        if (store == null) {
            throw new QianxuException("您不是门店负责人，无权操作");
        }
        if (!Boolean.TRUE.equals(store.getIsShow())) {
            throw new QianxuException("门店已禁用，请联系管理员");
        }
        return store;
    }

    private StoreOrder getVerifiableOrder(String vCode) {
        if (StrUtil.isBlank(vCode)) {
            throw new QianxuException("核销码不能为空");
        }
        LambdaQueryWrapper<StoreOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StoreOrder::getVerifyCode, vCode.trim())
                .eq(StoreOrder::getPaid, true)
                .eq(StoreOrder::getRefundStatus, 0)
                .eq(StoreOrder::getIsDel, false)
                .last(" limit 1 ");
        StoreOrder order = storeOrderDao.selectOne(lqw);
        if (order == null) {
            throw new QianxuException("核销码 " + vCode + " 对应的订单不存在或不可核销");
        }
        if (order.getStatus() > 0) {
            throw new QianxuException("订单已核销，请勿重复核销");
        }
        return order;
    }

    private void checkOrderBelongsToStore(StoreOrder order, SystemStore store) {
        if (order.getStoreId() == null || !order.getStoreId().equals(store.getId())) {
            throw new QianxuException("该订单不属于您的门店，无法核销");
        }
    }

    private String buildProductSummary(Integer orderId) {
        List<StoreOrderInfoOldVo> infos = storeOrderInfoService.getOrderListByOrderId(orderId);
        if (infos == null || infos.isEmpty()) {
            return "";
        }
        return infos.stream().map(e -> {
            OrderInfoDetailVo info = e.getInfo();
            if (info == null) {
                return "";
            }
            return StrUtil.nullToEmpty(info.getProductName()) + "x" + (info.getPayNum() == null ? 0 : info.getPayNum());
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining("；"));
    }

    /**
     * 附近门店列表：按商品佣金配置覆盖三类服务费（留空则跟随门店默认）。
     */
    private void applyProductStoreFees(MerchantStoreNearVo vo, SystemStore store,
                                       ProductCommissionConfig.Store storeFeeCfg, StoreProduct product) {
        if (storeFeeCfg == null || store == null) {
            return;
        }
        BigDecimal unitPrice = product == null ? BigDecimal.ZERO : product.getPrice();
        vo.setPickupFee(ProductCommissionUtil.resolveStoreServiceFee(
                storeFeeCfg.getPickup(), store.getPickupFee(), unitPrice, 1));
        vo.setVerifyFee(ProductCommissionUtil.resolveStoreServiceFee(
                storeFeeCfg.getVerify(), store.getVerifyFee(), unitPrice, 1));
        vo.setDeliveryFee(ProductCommissionUtil.resolveStoreServiceFee(
                storeFeeCfg.getDelivery(), store.getDeliveryFee(), unitPrice, 1));
    }

    /**
     * 核销服务费：若订单商品配置了核销服务费覆盖，则按行汇总；否则取门店默认核销服务费（一次）。
     */
    private BigDecimal resolveOrderVerifyFee(StoreOrder order, SystemStore store) {
        BigDecimal storeFee = store == null || store.getVerifyFee() == null ? BigDecimal.ZERO : store.getVerifyFee();
        if (order == null || order.getId() == null) {
            return storeFee;
        }
        List<StoreOrderInfoOldVo> infos = storeOrderInfoService.getOrderListByOrderId(order.getId());
        if (infos == null || infos.isEmpty()) {
            return storeFee;
        }
        BigDecimal sum = BigDecimal.ZERO;
        boolean anyConfigured = false;
        for (StoreOrderInfoOldVo line : infos) {
            OrderInfoDetailVo detail = line == null ? null : line.getInfo();
            if (detail == null || detail.getProductId() == null) {
                continue;
            }
            StoreProduct product = storeProductService.getById(detail.getProductId());
            if (product == null) {
                continue;
            }
            ProductCommissionConfig cfg = ProductCommissionUtil.parse(product.getCommissionConfig());
            ProductCommissionConfig.FeeItem feeItem = cfg.getStore() == null ? null : cfg.getStore().getVerify();
            if (!ProductCommissionUtil.isStoreFeeConfigured(feeItem)) {
                continue;
            }
            anyConfigured = true;
            BigDecimal unitPrice = detail.getVipPrice() != null ? detail.getVipPrice() : detail.getPrice();
            int payNum = detail.getPayNum() == null ? 1 : detail.getPayNum();
            sum = sum.add(ProductCommissionUtil.resolveStoreServiceFee(feeItem, storeFee, unitPrice, payNum));
        }
        return anyConfigured ? sum : storeFee;
    }

    private double parseDouble(String v) {
        try {
            return StrUtil.isBlank(v) ? 0 : Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Haversine 公式计算两点球面距离（公里）
     */
    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371.0;
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
