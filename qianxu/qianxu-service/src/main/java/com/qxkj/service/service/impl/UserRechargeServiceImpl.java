package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.constants.DateConstants;
import com.qxkj.common.constants.PayConstants;
import com.qxkj.common.constants.UserConstants;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.model.finance.UserRecharge;
import com.qxkj.common.request.UserRechargeSearchRequest;
import com.qxkj.common.response.UserRechargeResponse;
import com.qxkj.common.model.user.User;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.DateLimitUtilVo;
import com.qxkj.service.dao.UserRechargeDao;
import com.qxkj.service.service.UserRechargeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class UserRechargeServiceImpl extends ServiceImpl<UserRechargeDao, UserRecharge> implements UserRechargeService {

    @Resource
    private UserRechargeDao dao;


    /**
    * 列表
    * @param request 请求参数
    * @return List<UserRecharge>
    */
    @Override
    public PageInfo<UserRechargeResponse> getList(UserRechargeSearchRequest request) {
        //Page<UserRecharge> userRechargesList = PageHelper.startPage(request.getPage(), request.getLimit());
        //
        //DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
        ////带 UserExtract 类的多条件查询
        //LambdaQueryWrapper<UserRecharge> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //if (ObjectUtil.isNotNull(request.getUid()) && request.getUid() > 0) {
        //    lambdaQueryWrapper.eq(UserRecharge::getUid, request.getUid());
        //}
        //if (StrUtil.isNotBlank(request.getKeywords())) {
        //    lambdaQueryWrapper.like(UserRecharge::getOrderId, request.getKeywords()); //订单号
        //}
        ////是否充值
        //lambdaQueryWrapper.eq(UserRecharge::getPaid, true);
        //
        ////时间范围
        //if (StrUtil.isNotBlank(dateLimit.getStartTime()) && StrUtil.isNotBlank(dateLimit.getEndTime())) {
        //    //判断时间
        //    int compareDateResult = QianxuDateUtil.compareDate(dateLimit.getEndTime(), dateLimit.getStartTime(), Constants.DATE_FORMAT);
        //    if(compareDateResult == -1){
        //        throw new QianxuException("开始时间不能大于结束时间！");
        //    }
        //
        //    lambdaQueryWrapper.between(UserRecharge::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        //}
        //lambdaQueryWrapper.orderByDesc(UserRecharge::getId);
        //List<UserRecharge> userRecharges = dao.selectList(lambdaQueryWrapper);
        Map<String, Object> map = CollUtil.newHashMap();
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
            //判断时间
            int compareDateResult = QianxuDateUtil.compareDate(dateLimit.getEndTime(), dateLimit.getStartTime(), DateConstants.DATE_FORMAT);
            if (compareDateResult == -1) {
                throw new QianxuException(CommonResultCode.VALIDATE_FAILED, "开始时间不能大于结束时间！");
            }
            if (StrUtil.isNotBlank(dateLimit.getStartTime())) {
                map.put("startTime", dateLimit.getStartTime());
                map.put("endTime", dateLimit.getEndTime());
            }
        }
        if (StrUtil.isNotBlank(request.getKeywords())) {
            String orderNo = URLUtil.decode(request.getKeywords());
            map.put("orderNo", orderNo);
        }

        Page<UserRechargeResponse> page = PageHelper.startPage(request.getPage(), request.getLimit());
        List<UserRechargeResponse> userRechargesList = dao.getAdminPage(map);
        return CommonPage.copyPageInfo(page, userRechargesList);
        //if (CollUtil.isEmpty(userRecharges)) {
        //    return CommonPage.copyPageInfo(userRechargesList, CollUtil.newArrayList());
        //}
        //
        //List<Integer> userIds = userRecharges.stream().map(UserRecharge::getUid).collect(Collectors.toList());
        //HashMap<Integer, User> userHashMap = userService.getMapListInUid(userIds);
        //List<UserRechargeResponse> responseList = userRecharges.stream().map(e -> {
        //    User user = userHashMap.get(e.getUid());
        //    UserRechargeResponse r = new UserRechargeResponse();
        //    BeanUtils.copyProperties(e, r);
        //    if (null != user) {
        //        r.setAvatar(user.getAvatar());
        //        r.setNickname(user.getNickname());
        //    }
        //    return r;
        //}).collect(Collectors.toList());
        //return CommonPage.copyPageInfo(userRechargesList, responseList);
    }

    /**
     * 充值总金额
     * @return HashMap<String, BigDecimal>
     */
    @Override
    public HashMap<String, BigDecimal> getBalanceList() {
        HashMap<String, BigDecimal> map = new HashMap<>();

        BigDecimal routine = dao.getSumByType("routine");
        if(null == routine) routine = BigDecimal.ZERO;
        map.put("routine", routine); //小程序充值

//        BigDecimal weChat = dao.getSumByType("weixin");
        BigDecimal weChat = dao.getSumByType("public");
        if(null == weChat) weChat = BigDecimal.ZERO;
        map.put("weChat", weChat); //公众号充值

        BigDecimal total = dao.getSumByType("");
        if(null == total) total = BigDecimal.ZERO;
        map.put("total", total); //总金额

        BigDecimal refund = dao.getSumByRefund();
        if(null == refund) refund = BigDecimal.ZERO;
        map.put("refund", refund);

        map.put("other", total.subtract(routine).subtract(weChat)); //其他金额

        return map;
    }

    /**
     * 根据对象查询订单
     * @author Mr.Zhang
     * @since 2020-05-11
     * @return UserRecharge
     */
    @Override
    public UserRecharge getInfoByEntity(UserRecharge userRecharge) {
        LambdaQueryWrapper<UserRecharge> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.setEntity(userRecharge);
        return dao.selectOne(lambdaQueryWrapper);
    }

    /**
     * 根据日期获取充值订单数量
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    @Override
    public Integer getRechargeOrderNumByDate(String date) {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        wrapper.select("id");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        return dao.selectCount(wrapper);
    }

    /**
     * 根据日期获取充值订单金额
     * @param date 日期，yyyy-MM-dd格式
     * @return BigDecimal
     */
    @Override
    public BigDecimal getRechargeOrderAmountByDate(String date) {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        wrapper.select("IFNULL(sum(price), 0) as price");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        return dao.selectOne(wrapper).getPrice();
    }

    /**
     * 获取总人数
     * @return Integer
     */
    @Override
    public Integer getTotalPeople() {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        //wrapper.select("id");
        wrapper.select(" ANY_VALUE(id) AS id ");
        wrapper.eq("paid", 1);
        wrapper.groupBy("uid");
        List<UserRecharge> list = dao.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        return list.size();
    }

    /**
     * 获取总金额
     * @return BigDecimal
     */
    @Override
    public BigDecimal getTotalPrice() {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        wrapper.select("IFNULL(sum(price), 0) as price");
        wrapper.eq("paid", 1);
        return dao.selectOne(wrapper).getPrice();
    }

    /**
     * 根据时间获取充值用户数量
     * @param date 日期
     * @return Integer
     */
    @Override
    public Integer getRechargeUserNumByDate(String date) {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        //wrapper.select("id");
        wrapper.select(" ANY_VALUE(id) AS id ");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        wrapper.groupBy("uid");
        List<UserRecharge> list = dao.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        return list.size();
    }

    /**
     * 根据时间获取充值用户数量
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    @Override
    public Integer getRechargeUserNumByPeriod(String startDate, String endDate) {
        QueryWrapper<UserRecharge> wrapper = Wrappers.query();
        //wrapper.select("id");
        wrapper.select(" ANY_VALUE(id) AS id ");
        wrapper.eq("paid", 1);
        wrapper.apply("date_format(create_time, '%Y-%m-%d') between {0} and {1}", startDate, endDate);
        wrapper.groupBy("uid");
        List<UserRecharge> list = dao.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        return list.size();
    }

    /**
     * 获取待上传微信发货管理订单
     */
    @Override
    public List<UserRecharge> findAwaitUploadWechatList() {
        DateTime date = cn.hutool.core.date.DateUtil.date();
        DateTime offsetMinute = cn.hutool.core.date.DateUtil.offsetMinute(date, -10);
        LambdaQueryWrapper<UserRecharge> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserRecharge::getPaid, 1);
        lqw.eq(UserRecharge::getIsWechatShipping, 0);
        lqw.eq(UserRecharge::getRechargeType, PayConstants.PAY_CHANNEL_WE_CHAT_PROGRAM);
        lqw.le(UserRecharge::getPayTime, offsetMinute);
        return dao.selectList(lqw);
    }

    /**
     * 获取订单
     * @param outTradeNo 商户系统内部的订单号
     */
    @Override
    public UserRecharge getByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<UserRecharge> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserRecharge::getOutTradeNo, outTradeNo);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }
}

