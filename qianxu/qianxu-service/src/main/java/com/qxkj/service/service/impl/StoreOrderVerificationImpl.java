package com.qxkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.PayConstants;
import com.qxkj.common.constants.TaskConstants;
import com.qxkj.common.constants.WeChatConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.combination.StorePink;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.system.SystemAdmin;
import com.qxkj.common.request.StoreOrderStaticsticsRequest;
import com.qxkj.common.response.StoreOrderVerificationConfirmResponse;
import com.qxkj.common.response.StoreStaffDetail;
import com.qxkj.common.response.StoreStaffTopDetail;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.utils.SecurityUtil;
import com.qxkj.common.vo.DateLimitUtilVo;
import com.qxkj.common.vo.LoginUserVo;
import com.qxkj.service.dao.StoreOrderDao;
import com.qxkj.service.delete.OrderUtils;
import com.qxkj.service.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
public class StoreOrderVerificationImpl implements StoreOrderVerification {

    @Resource
    private StoreOrderDao dao;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private WechatOrderShippingService wechatOrderShippingService;
    @Autowired
    private StorePinkService storePinkService;
    @Autowired
    private com.qxkj.service.service.MerchantStoreService merchantStoreService;
    /**
     * 获取订单核销数据
     */
    @Override
    public StoreStaffTopDetail getOrderVerificationData() {
        StoreStaffTopDetail storeStaffTopDetail = new StoreStaffTopDetail();
        //订单支付没有退款 数量-
        LambdaQueryWrapper<StoreOrder> lqwOrderCount = Wrappers.lambdaQuery();
        lqwOrderCount.eq(StoreOrder::getIsDel,false).eq(StoreOrder::getPaid,true).eq(StoreOrder::getRefundStatus,0);
        storeStaffTopDetail.setOrderCount(dao.selectCount(lqwOrderCount));
        //订单支付没有退款 支付总金额
        LambdaQueryWrapper<StoreOrder> lqwSumPrice = Wrappers.lambdaQuery();
        lqwSumPrice.eq(StoreOrder::getIsDel,false).eq(StoreOrder::getPaid,true).eq(StoreOrder::getRefundStatus,0);
        List<StoreOrder> storeOrdersSumPrice = dao.selectList(lqwSumPrice);
        Double sumPrice = storeOrdersSumPrice.stream().mapToDouble(e->e.getPayPrice().doubleValue()).sum();
        storeStaffTopDetail.setSumPrice(BigDecimal.valueOf(sumPrice).setScale(2,BigDecimal.ROUND_HALF_UP));
        //订单待支付 数量
        LambdaQueryWrapper<StoreOrder> lqwUnPaidCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(lqwUnPaidCount,0);
        storeStaffTopDetail.setUnpaidCount(dao.selectCount(lqwUnPaidCount));
        //订单待发货 数量
        LambdaQueryWrapper<StoreOrder> lqwUnShippedCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(lqwUnShippedCount,1);
        storeStaffTopDetail.setUnshippedCount(dao.selectCount(lqwUnShippedCount));
        //订单待收货 数量
        LambdaQueryWrapper<StoreOrder> lqwReceivedCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(lqwReceivedCount,2);
        storeStaffTopDetail.setReceivedCount(dao.selectCount(lqwReceivedCount));
        // 订单待核销数量
        LambdaQueryWrapper<StoreOrder> verificationCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(verificationCount,3);
        storeStaffTopDetail.setVerificationCount(dao.selectCount(verificationCount));
        //订单已完成 数量
        LambdaQueryWrapper<StoreOrder> lqwCompleteCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(lqwCompleteCount,4);
        storeStaffTopDetail.setCompleteCount(dao.selectCount(lqwCompleteCount));
        //订单退款 数量
        LambdaQueryWrapper<StoreOrder> lqwRefundCount = Wrappers.lambdaQuery();
        orderUtils.statusApiByWhere(lqwRefundCount,-3);
        storeStaffTopDetail.setRefundCount(dao.selectCount(lqwRefundCount));

        // 获取今日，昨日，本月，订单金额
        String dayStart = QianxuDateUtil.nowDateTime(Constants.DATE_FORMAT_START);
        String dayEnd = QianxuDateUtil.nowDateTime(Constants.DATE_FORMAT_END);
        String yesterdayStart = QianxuDateUtil.addDay(dayStart,-1,Constants.DATE_FORMAT_START);
        String yesterdayEnd = QianxuDateUtil.addDay(dayEnd,-1,Constants.DATE_FORMAT_END);
        String monthStart = QianxuDateUtil.nowDateTime(Constants.DATE_FORMAT_MONTH_START);
        String monthEnd = QianxuDateUtil.getMonthEndDay();

        // 今日订单数量
        LambdaQueryWrapper<StoreOrder> lqwTodayCount = Wrappers.lambdaQuery();
        lqwTodayCount.eq(StoreOrder::getIsDel,false).between(StoreOrder::getPayTime,dayStart,dayEnd)
                .eq(StoreOrder::getPaid,1).eq(StoreOrder::getRefundStatus,0);
        List<StoreOrder> storeOrdersTodayCount = dao.selectList(lqwTodayCount);
        if(null == storeOrdersTodayCount) storeOrdersTodayCount = new ArrayList<>();
        storeStaffTopDetail.setTodayCount(storeOrdersTodayCount.size());

        // 今日成交额
        double todayPrice = storeOrdersTodayCount.stream().mapToDouble(e->e.getPayPrice().doubleValue()).sum();
        storeStaffTopDetail.setTodayPrice(BigDecimal.valueOf(todayPrice).setScale(2,BigDecimal.ROUND_HALF_UP));

        // 昨日订单数
        LambdaQueryWrapper<StoreOrder> lqwPro = Wrappers.lambdaQuery();
        lqwPro.eq(StoreOrder::getIsDel,false).between(StoreOrder::getCreateTime,yesterdayStart,yesterdayEnd)
                .eq(StoreOrder::getPaid, true).eq(StoreOrder::getRefundStatus,0);
        List<StoreOrder> storeOrdersPro = dao.selectList(lqwPro);
        if(null == storeOrdersPro) storeOrdersPro = new ArrayList<>();
        storeStaffTopDetail.setProCount(storeOrdersPro.size());

        //  昨日成交额
        double proPrice = storeOrdersPro.stream().mapToDouble(e->e.getPayPrice().doubleValue()).sum();
        storeStaffTopDetail.setProPrice(BigDecimal.valueOf(proPrice).setScale(2,BigDecimal.ROUND_HALF_UP));

        // 本月成交订单数量
        LambdaQueryWrapper<StoreOrder> lqwMonth = Wrappers.lambdaQuery();
        lqwMonth.eq(StoreOrder::getIsDel,false).between(StoreOrder::getPayTime,monthStart, monthEnd)
                .eq(StoreOrder::getPaid,true).eq(StoreOrder::getRefundStatus,0);
        List<StoreOrder> storeOrdersMonth = dao.selectList(lqwMonth);
        if(null == storeOrdersMonth) storeOrdersMonth = new ArrayList<>();
        storeStaffTopDetail.setMonthCount(storeOrdersMonth.size());

        // 本月成交额
        double monthTotalPrice = storeOrdersMonth.stream().mapToDouble(e -> e.getPayPrice().doubleValue()).sum();
        storeStaffTopDetail.setMonthPrice(BigDecimal.valueOf(monthTotalPrice).setScale(2,BigDecimal.ROUND_HALF_UP));

        return storeStaffTopDetail;
    }

    /**
     * 核销月详情
     * @return 月详情
     */
    @Override
    public List<StoreStaffDetail> getOrderVerificationDetail(StoreOrderStaticsticsRequest request) {
        request.setPage((request.getPage() - 1) * request.getLimit());
        DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
        request.setStartTime(dateLimit.getStartTime());
        request.setEndTime(dateLimit.getEndTime());
        return dao.getOrderVerificationDetail(request);
    }


    /**
     * 根据核销码核销订单(相当于收货)
     *
     * @param vCode 核销码
     * @return 核销结果
     */
    @Override
    public boolean verificationOrderByCode(String vCode) {
        StoreOrderVerificationConfirmResponse existOrder = getVerificationOrderByCode(vCode);
        // 判断当前用户是否有权限核销
        LoginUserVo loginUserVo = SecurityUtil.getLoginUserVo();
        SystemAdmin currentAdmin = loginUserVo.getUser();
        // 添加核销人员后执行核销操作
        StoreOrder storeOrder = new StoreOrder();
        BeanUtils.copyProperties(existOrder,storeOrder);
        if (ObjectUtil.isNotNull(storeOrder.getCombinationId()) && storeOrder.getCombinationId() > 0) {
            StorePink storePink = storePinkService.getById(storeOrder.getPinkId());
            if (storePink.getStatus() != 2) throw new QianxuException("当前订单正在拼团中不能核销！");
        }
        storeOrder.setStatus(Constants.ORDER_STATUS_INT_BARGAIN);
        storeOrder.setClerkId(currentAdmin.getId());
        storeOrder.setUpdateTime(DateUtil.date());
        boolean saveStatus = dao.updateById(storeOrder) > 0;

        // 小程序订阅消息发送
        if(saveStatus){
            //后续操作放入redis
            redisUtil.lPush(TaskConstants.ORDER_TASK_REDIS_KEY_AFTER_TAKE_BY_USER, storeOrder.getId());
            // 小程序发货管理
            if (storeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT) && storeOrder.getIsChannel().equals(1)) {
                String shippingSwitch = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
                if (StrUtil.isNotBlank(shippingSwitch) && shippingSwitch.equals("1")) {
                    wechatOrderShippingService.uploadVerifyShippingInfo(storeOrder.getOrderId());
                }
            }
            // 门店核销记录（后台核销）
            merchantStoreService.writeVerifyRecord(storeOrder, null,
                    StrUtil.blankToDefault(currentAdmin.getRealName(), currentAdmin.getAccount()),
                    com.qxkj.common.model.merchant.MerchantStoreVerifyRecord.SOURCE_ADMIN);
        }
        return saveStatus;
    }

    /**
     * 根据核销码查询待核销订单
     *
     * @param vCode 核销码
     * @return 待核销订单详情
     */
    @Override
    public StoreOrderVerificationConfirmResponse getVerificationOrderByCode(String vCode) {
        StoreOrderVerificationConfirmResponse response = new StoreOrderVerificationConfirmResponse();
        StoreOrder storeOrderPram = new StoreOrder().setVerifyCode(vCode).setPaid(true).setRefundStatus(0);
        StoreOrder existOrder = storeOrderService.getByEntityOne(storeOrderPram);
        if(null == existOrder) throw new QianxuException(Constants.RESULT_VERIFICATION_ORDER_NOT_FUND.replace("${vCode}",vCode));
        if(existOrder.getStatus() > 0) throw new QianxuException(Constants.RESULT_VERIFICATION_ORDER_VED.replace("${vCode}",vCode));
        BeanUtils.copyProperties(existOrder, response);
        response.setStoreOrderInfoVos(storeOrderInfoService.getOrderListByOrderId(existOrder.getId()));
        return response;
    }
}
