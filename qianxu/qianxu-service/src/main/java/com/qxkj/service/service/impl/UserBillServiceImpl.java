package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.constants.UserConstants;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.response.UserBillResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.request.FundsMonitorRequest;
import com.qxkj.common.request.FundsMonitorSearchRequest;
import com.qxkj.common.response.MonitorResponse;
import com.qxkj.common.request.StoreOrderRefundRequest;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBill;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.DateLimitUtilVo;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.service.dao.StoreOrderDao;
import com.qxkj.service.dao.UserBillDao;
import com.qxkj.service.service.UserBillService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Service
public class UserBillServiceImpl extends ServiceImpl<UserBillDao, UserBill> implements UserBillService {

    @Resource
    private UserBillDao dao;

    @Resource
    private StoreOrderDao storeOrderDao;

    /**
    * 列表
    * @param request 请求参数
    * @param pageParamRequest 分页类参数
    * @return List<UserBill>
    */
    @Override
    public List<UserBill> getList(FundsMonitorSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        QueryWrapper<UserBill> queryWrapper = new QueryWrapper<>();
        getMonthSql(request, queryWrapper);

        //排序
        if (request.getSort() == null) {
            queryWrapper.orderByDesc("create_time");
        }else{
            if (request.getSort().equals("asc")) {
                queryWrapper.orderByAsc("number");
            }else{
                queryWrapper.orderByDesc("number");
            }
        }

        // 查询类型
        if (StringUtils.isNotBlank(request.getCategory())) {
            queryWrapper.eq("category", request.getCategory());
        }
        if (ObjectUtil.isNotNull(request.getPm())) {
            queryWrapper.eq("pm", request.getPm());
        }

        return dao.selectList(queryWrapper);
    }

    private void getMonthSql(FundsMonitorSearchRequest request, QueryWrapper<UserBill> queryWrapper) {
        queryWrapper.gt("status", 0); // -1无效
        if (!StringUtils.isBlank(request.getKeywords())) {
            queryWrapper.and(i -> i.
                    or().eq("id", request.getKeywords()).   //用户账单id
                    or().eq("uid", request.getKeywords()). //用户uid
                    or().eq("link_id", request.getKeywords()). //关联id
                    or().like("title", request.getKeywords()) //账单标题
            );
        }

        //时间范围
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            //判断时间
            int compareDateResult = QianxuDateUtil.compareDate(dateLimit.getEndTime(), dateLimit.getStartTime(), Constants.DATE_FORMAT);
            if (compareDateResult == -1) {
                throw new QianxuException("开始时间不能大于结束时间！");
            }

            queryWrapper.between("create_time", dateLimit.getStartTime(), dateLimit.getEndTime());

            //资金范围
            if (request.getMax() != null && request.getMin() != null) {
                //判断时间
                if (request.getMax().compareTo(request.getMin()) < 0) {
                    throw new QianxuException("最大金额不能小于最小金额！");
                }
                queryWrapper.between("number", request.getMin(), request.getMax());
            }
        }


        //关联id
        if (StringUtils.isNotBlank(request.getLinkId())) {
            if (request.getLinkId().equals("gt")) {
                queryWrapper.ne("link_id", 0);
            }else{
                queryWrapper.eq("link_id", request.getLinkId());
            }
        }

        //用户id集合
        if (null != request.getUserIdList() && request.getUserIdList().size() > 0) {
            queryWrapper.in("uid", request.getUserIdList());
        } else if (ObjectUtil.isNotNull(request.getUid())) {
            queryWrapper.eq("uid", request.getUid());
        }



        if (StringUtils.isNotBlank(request.getCategory())) {
            queryWrapper.eq("category", request.getCategory());
        }

        if (StringUtils.isNotBlank(request.getType())) {
            queryWrapper.eq("type", request.getType());
        }
    }

    /**
     * 新增/消耗  总金额
     * @param pm Integer 0 = 支出 1 = 获得
     * @param userId Integer 用户uid
     * @param category String 类型
     * @param date String 时间范围
     * @param type String 小类型
     * @return UserBill
     */
    @Override
    public BigDecimal getSumBigDecimal(Integer pm, Integer userId, String category, String date, String type) {
        QueryWrapper<UserBill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category).
                eq("status", 1);
        queryWrapper.ne("type", Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
        if (ObjectUtil.isNotNull(userId)) {
            queryWrapper.eq("uid", userId);
        }
        if (null != pm) {
            queryWrapper.eq("pm", pm);
        }
        if (null != type) {
            queryWrapper.eq("type", type);
        }
        if (null != date) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(date);
            queryWrapper.between("create_time", dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        List<UserBill> userBills = dao.selectList(queryWrapper);
        if (CollUtil.isEmpty(userBills)) {
            return BigDecimal.ZERO;
        }
        return userBills.stream().map(UserBill::getNumber).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, BigDecimal.ROUND_DOWN);
    }

    /**
     * 保存退款日志
     * @return boolean
     */
    @Override
    public Boolean saveRefundBill(StoreOrderRefundRequest request, User user) {
        UserBill userBill = new UserBill();
        userBill.setTitle("商品退款");
        userBill.setUid(user.getUid());
        userBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        userBill.setType(Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
        userBill.setNumber(request.getAmount());
        // 关联真实订单号（兼容历史把主键 id 写入 link_id 的数据）
        String linkId = request.getOrderId() == null ? "0" : String.valueOf(request.getOrderId());
        if (request.getOrderId() != null) {
            StoreOrder storeOrder = storeOrderDao.selectById(request.getOrderId());
            if (storeOrder != null && StrUtil.isNotBlank(storeOrder.getOrderId())) {
                linkId = storeOrder.getOrderId();
            }
        }
        userBill.setLinkId(linkId);
        userBill.setBalance(user.getNowMoney().add(request.getAmount()));
        userBill.setMark("订单退款到余额" + request.getAmount() + "元");
        userBill.setPm(1);
        return save(userBill);
    }

    /**
     * 资金监控
     * @param request 查询参数
     * @return PageInfo
     */
    @Override
    public PageInfo<MonitorResponse> fundMonitoring(FundsMonitorRequest request) {
        Page<UserBill> billPage = PageHelper.startPage(request.getPage(), request.getLimit());
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(request.getContent())) {
            ValidateFormUtil.validatorUserCommonSearch(request);
            String keywords = URLUtil.decode(request.getContent());
            switch (request.getSearchType()) {
                case UserConstants.USER_SEARCH_TYPE_ALL:
                    map.put("keywords", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_UID:
                    map.put("uid", Integer.valueOf(request.getContent()));
                    break;
                case UserConstants.USER_SEARCH_TYPE_NICKNAME:
                    map.put("nickname", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_PHONE:
                    map.put("phone", request.getContent());
                    break;
            }
        }
        //时间范围
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            map.put("startTime", dateLimit.getStartTime());
            map.put("endTime", dateLimit.getEndTime());
        }
        // 明细类型筛选（余额表：按 titleList 匹配，覆盖普通商城 + 订货体系全部实际标题）
        if (StrUtil.isNotBlank(request.getTitle())) {
            List<String> titleList = new ArrayList<>();
            switch (request.getTitle()) {
                case "recharge":
                    titleList.add("充值支付");
                    break;
                case "admin":
                    titleList.add("后台操作");
                    break;
                case "productRefund":
                    titleList.add("商品退款");
                    titleList.add("订货退款");
                    titleList.add("换货差价退款");
                    break;
                case "payProduct":
                    titleList.add("购买商品");
                    break;
                case "transferIn":
                    titleList.add("佣金转余额");
                    break;
                case "exchange":
                    titleList.add("换货差价");
                    break;
            }
            if (!titleList.isEmpty()) {
                map.put("titleList", titleList);
            }
        }
        // 关联单号筛选
        if (StrUtil.isNotBlank(request.getLinkId())) {
            map.put("linkId", request.getLinkId());
        }
        // 账户类型筛选（仅 now_money / brokerage_price 走本表；integral 走 fundMonitoringIntegral）
        if (StrUtil.isNotBlank(request.getCategory())) {
            String category = request.getCategory();
            if ("now_money".equals(category) || "brokerage_price".equals(category)) {
                map.put("category", category);
            }
            // integral 时不应走到这里，controller 会路由到 fundMonitoringIntegral
        }
        List<UserBillResponse> userBillResponses = dao.fundMonitoring(map);
        if (CollUtil.isEmpty(userBillResponses)) {
            return CommonPage.copyPageInfo(billPage, CollUtil.newArrayList());
        }
        List<MonitorResponse> responseList = userBillResponses.stream().map(e -> {
            MonitorResponse monitorResponse = new MonitorResponse();
            BeanUtils.copyProperties(e, monitorResponse);
            monitorResponse.setSourceTable("bill");
            return monitorResponse;
        }).collect(Collectors.toList());
        fillRealOrderNo(responseList);
        return CommonPage.copyPageInfo(billPage, responseList);
    }

    @Override
    public PageInfo<MonitorResponse> fundMonitoringAll(FundsMonitorRequest request) {
        Page<UserBill> billPage = PageHelper.startPage(request.getPage(), request.getLimit());
        Map<String, Object> map = buildMonitorQueryMap(request);
        List<UserBillResponse> userBillResponses = dao.fundMonitoringAll(map);
        if (CollUtil.isEmpty(userBillResponses)) {
            return CommonPage.copyPageInfo(billPage, CollUtil.newArrayList());
        }
        List<MonitorResponse> responseList = userBillResponses.stream().map(e -> {
            MonitorResponse monitorResponse = new MonitorResponse();
            BeanUtils.copyProperties(e, monitorResponse);
            // sourceTable 由 SQL 返回（bill / integral / brokerage），勿覆盖
            return monitorResponse;
        }).collect(Collectors.toList());
        fillRealOrderNo(responseList);
        return CommonPage.copyPageInfo(billPage, responseList);
    }

    /**
     * 组装资金监控公共查询条件（关键词/时间/uid/单号/标题映射）
     * @param request 查询参数
     * @return Map
     */
    private Map<String, Object> buildMonitorQueryMap(FundsMonitorRequest request) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(request.getContent())) {
            ValidateFormUtil.validatorUserCommonSearch(request);
            String keywords = URLUtil.decode(request.getContent());
            switch (request.getSearchType()) {
                case UserConstants.USER_SEARCH_TYPE_ALL:
                    map.put("keywords", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_UID:
                    map.put("uid", Integer.valueOf(request.getContent()));
                    break;
                case UserConstants.USER_SEARCH_TYPE_NICKNAME:
                    map.put("nickname", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_PHONE:
                    map.put("phone", request.getContent());
                    break;
            }
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            map.put("startTime", dateLimit.getStartTime());
            map.put("endTime", dateLimit.getEndTime());
        }
        if (StrUtil.isNotBlank(request.getLinkId())) {
            map.put("linkId", request.getLinkId());
        }
        // 项目类型 → 标题匹配：优先精确 titleList，其次模糊 titleLikeList
        if (StrUtil.isNotBlank(request.getTitle())) {
            switch (request.getTitle()) {
                case "recharge":
                    map.put("titleList", CollUtil.newArrayList("充值支付", "余额充值"));
                    break;
                case "admin":
                    map.put("titleLikeList", CollUtil.newArrayList("后台"));
                    break;
                case "productRefund":
                    map.put("titleLikeList", CollUtil.newArrayList("退款"));
                    break;
                case "payProduct":
                    map.put("titleList", CollUtil.newArrayList("购买商品"));
                    break;
                case "transferIn":
                    map.put("titleList", CollUtil.newArrayList("佣金转余额"));
                    break;
                case "exchange":
                    map.put("titleLikeList", CollUtil.newArrayList("换货"));
                    break;
                case "stock":
                    map.put("titleLikeList", CollUtil.newArrayList("订货", "代理奖励"));
                    break;
                case "order":
                    map.put("titleList", CollUtil.newArrayList(
                            "获得推广佣金", "获得自购返佣", "获得团队极差奖",
                            "获得团队平级奖", "获得区域代理奖励"));
                    break;
                case "withdraw":
                    map.put("titleList", CollUtil.newArrayList("提现申请", "提现申请拒绝"));
                    break;
                case "yue":
                    map.put("titleList", CollUtil.newArrayList("佣金转余额"));
                    break;
                case "sign":
                    map.put("titleLikeList", CollUtil.newArrayList("签到"));
                    break;
                case "reward":
                    map.put("titleLikeList", CollUtil.newArrayList("奖励", "赠送"));
                    break;
                case "deduct":
                    map.put("titleLikeList", CollUtil.newArrayList("抵扣", "消费", "扣除"));
                    break;
            }
        }
        return map;
    }

    @Override
    public PageInfo<MonitorResponse> fundMonitoringIntegral(FundsMonitorRequest request) {
        Page<UserBill> billPage = PageHelper.startPage(request.getPage(), request.getLimit());
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(request.getContent())) {
            ValidateFormUtil.validatorUserCommonSearch(request);
            String keywords = URLUtil.decode(request.getContent());
            switch (request.getSearchType()) {
                case UserConstants.USER_SEARCH_TYPE_ALL:
                    map.put("keywords", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_UID:
                    map.put("uid", Integer.valueOf(request.getContent()));
                    break;
                case UserConstants.USER_SEARCH_TYPE_NICKNAME:
                    map.put("nickname", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_PHONE:
                    map.put("phone", request.getContent());
                    break;
            }
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            map.put("startTime", dateLimit.getStartTime());
            map.put("endTime", dateLimit.getEndTime());
        }
        // 关联单号筛选
        if (StrUtil.isNotBlank(request.getLinkId())) {
            map.put("linkId", request.getLinkId());
        }
        if (StrUtil.isNotBlank(request.getTitle())) {
            switch (request.getTitle()) {
                case "admin":
                    map.put("titleLikeList", CollUtil.newArrayList("后台"));
                    break;
                case "sign":
                    map.put("titleLikeList", CollUtil.newArrayList("签到"));
                    break;
                case "order":
                    map.put("titleLikeList", CollUtil.newArrayList("下单", "购买", "订单", "付款"));
                    break;
                case "reward":
                    map.put("titleLikeList", CollUtil.newArrayList("奖励", "赠送"));
                    break;
                case "deduct":
                    map.put("titleLikeList", CollUtil.newArrayList("抵扣", "消费", "扣除"));
                    break;
            }
        }
        List<UserBillResponse> userBillResponses = dao.fundMonitoringIntegral(map);
        if (CollUtil.isEmpty(userBillResponses)) {
            return CommonPage.copyPageInfo(billPage, CollUtil.newArrayList());
        }
        List<MonitorResponse> responseList = userBillResponses.stream().map(e -> {
            MonitorResponse monitorResponse = new MonitorResponse();
            BeanUtils.copyProperties(e, monitorResponse);
            monitorResponse.setSourceTable("integral");
            return monitorResponse;
        }).collect(Collectors.toList());
        fillRealOrderNo(responseList);
        return CommonPage.copyPageInfo(billPage, responseList);
    }

    @Override
    public PageInfo<MonitorResponse> fundMonitoringBrokerage(FundsMonitorRequest request) {
        Page<UserBill> billPage = PageHelper.startPage(request.getPage(), request.getLimit());
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(request.getContent())) {
            ValidateFormUtil.validatorUserCommonSearch(request);
            String keywords = URLUtil.decode(request.getContent());
            switch (request.getSearchType()) {
                case UserConstants.USER_SEARCH_TYPE_ALL:
                    map.put("keywords", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_UID:
                    map.put("uid", Integer.valueOf(request.getContent()));
                    break;
                case UserConstants.USER_SEARCH_TYPE_NICKNAME:
                    map.put("nickname", keywords);
                    break;
                case UserConstants.USER_SEARCH_TYPE_PHONE:
                    map.put("phone", request.getContent());
                    break;
            }
        }
        if (StrUtil.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            map.put("startTime", dateLimit.getStartTime());
            map.put("endTime", dateLimit.getEndTime());
        }
        // 关联单号筛选
        if (StrUtil.isNotBlank(request.getLinkId())) {
            map.put("linkId", request.getLinkId());
        }
        // 项目类型映射为 title 枚举（union 两表统一按 title 匹配）
        if (StrUtil.isNotBlank(request.getTitle())) {
            switch (request.getTitle()) {
                case "admin":
                    map.put("titleList", CollUtil.newArrayList("后台操作", "后台修改佣金"));
                    break;
                case "order":
                    // 分销/代理体系佣金（标题为固定常量，精确匹配避免与"后台修改佣金"串味）
                    map.put("titleList", CollUtil.newArrayList(
                            "获得推广佣金", "获得自购返佣", "获得团队极差奖",
                            "获得团队平级奖", "获得区域代理奖励"));
                    break;
                case "withdraw":
                    map.put("titleList", CollUtil.newArrayList("提现申请", "提现申请拒绝"));
                    break;
                case "yue":
                    map.put("titleList", CollUtil.newArrayList("佣金转余额"));
                    break;
                case "stock":
                    // 订货商/代理体系奖金（如"订货奖金"），标题由 StockRewardServiceImpl 写入
                    map.put("titleLikeList", CollUtil.newArrayList("订货", "代理奖励"));
                    break;
            }
        }
        List<UserBillResponse> userBillResponses = dao.fundMonitoringBrokerage(map);
        if (CollUtil.isEmpty(userBillResponses)) {
            return CommonPage.copyPageInfo(billPage, CollUtil.newArrayList());
        }
        List<MonitorResponse> responseList = userBillResponses.stream().map(e -> {
            MonitorResponse monitorResponse = new MonitorResponse();
            BeanUtils.copyProperties(e, monitorResponse);
            monitorResponse.setSourceTable("brokerage");
            return monitorResponse;
        }).collect(Collectors.toList());
        fillRealOrderNo(responseList);
        return CommonPage.copyPageInfo(billPage, responseList);
    }

    /**
     * 资金监控：若 linkId 是订单表主键数字，则替换为真实订单号 order_id。
     * 已是订单号 / 其它业务单号的保持不变。
     */
    private void fillRealOrderNo(List<MonitorResponse> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Integer> orderPkIds = new HashSet<>();
        for (MonitorResponse item : list) {
            if (item == null || StrUtil.isBlank(item.getLinkId()) || "0".equals(item.getLinkId())) {
                continue;
            }
            if (item.getLinkId().matches("^\\d+$")) {
                try {
                    orderPkIds.add(Integer.valueOf(item.getLinkId()));
                } catch (NumberFormatException ignore) {
                    // ignore
                }
            }
        }
        if (orderPkIds.isEmpty()) {
            return;
        }
        List<StoreOrder> orders = storeOrderDao.selectBatchIds(orderPkIds);
        if (CollUtil.isEmpty(orders)) {
            return;
        }
        Map<Integer, String> idToOrderNo = orders.stream()
                .filter(o -> o != null && o.getId() != null && StrUtil.isNotBlank(o.getOrderId()))
                .collect(Collectors.toMap(StoreOrder::getId, StoreOrder::getOrderId, (a, b) -> a));
        for (MonitorResponse item : list) {
            if (item == null || StrUtil.isBlank(item.getLinkId()) || !item.getLinkId().matches("^\\d+$")) {
                continue;
            }
            try {
                String orderNo = idToOrderNo.get(Integer.valueOf(item.getLinkId()));
                if (StrUtil.isNotBlank(orderNo)) {
                    item.setLinkId(orderNo);
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }
    }

    /**
     * 用户账单记录（现金）
     * @param uid 用户uid
     * @param type 记录类型：all-全部，expenditure-支出，income-收入
     * @return PageInfo
     */
    @Override
    public PageInfo<UserBill> nowMoneyBillRecord(Integer uid, String type, PageParamRequest pageRequest) {
        Page<UserBill> billPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
        LambdaQueryWrapper<UserBill> lqw = Wrappers.lambdaQuery();
        lqw.select(UserBill::getTitle, UserBill::getNumber, UserBill::getBalance, UserBill::getMark, UserBill::getCreateTime, UserBill::getPm);
        lqw.eq(UserBill::getUid, uid);
        lqw.eq(UserBill::getCategory, Constants.USER_BILL_CATEGORY_MONEY);
        switch (type) {
            case "all":
                break;
            case "expenditure":
                lqw.eq(UserBill::getPm, 0);
                break;
            case "income":
                lqw.eq(UserBill::getPm, 1);
                lqw.ne(UserBill::getType, Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
                break;
        }
        lqw.eq(UserBill::getStatus, 1);
        lqw.orderByDesc(UserBill::getId);
        List<UserBill> billList = dao.selectList(lqw);
        return CommonPage.copyPageInfo(billPage, billList);
    }

}

