package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.constants.DateConstants;
import com.qxkj.common.constants.UserConstants;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.AdminIntegralSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.IntegralRecordConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.response.UserIntegralRecordResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.DateLimitUtilVo;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserIntegralRecord;
import com.qxkj.service.dao.UserIntegralRecordDao;
import com.qxkj.service.service.UserIntegralRecordService;
import com.qxkj.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
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
public class UserIntegralRecordServiceImpl extends ServiceImpl<UserIntegralRecordDao, UserIntegralRecord> implements UserIntegralRecordService {

    private static final Logger logger = LoggerFactory.getLogger(UserIntegralRecordServiceImpl.class);

    @Resource
    private UserIntegralRecordDao dao;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 根据订单编号、uid获取记录列表
     * @param orderNo 订单编号
     * @param uid 用户uid
     * @return 记录列表
     */
    @Override
    public List<UserIntegralRecord> findListByOrderIdAndUid(String orderNo, Integer uid) {
        LambdaQueryWrapper<UserIntegralRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserIntegralRecord::getUid, uid);
        lqw.eq(UserIntegralRecord::getLinkId, orderNo);
        lqw.in(UserIntegralRecord::getStatus, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
        List<UserIntegralRecord> recordList = dao.selectList(lqw);
        if (CollUtil.isEmpty(recordList)) {
            return recordList;
        }
        for (int i = 0; i < recordList.size();) {
            UserIntegralRecord record = recordList.get(i);
            if (record.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD)) {
                if (record.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE)) {
                    recordList.remove(i);
                    continue;
                }
            }
            i++;
        }
        return recordList;
    }

    /**
     * 积分解冻
     */
    @Override
    public void integralThaw() {
        // 查询需要解冻的积分
        List<UserIntegralRecord> thawList = findThawList();
        if (CollUtil.isEmpty(thawList)) {
            return;
        }
        for (UserIntegralRecord record : thawList) {
            // 查询对应的用户
            User user = userService.getById(record.getUid());
            if (ObjectUtil.isNull(user)) {
                continue ;
            }
            record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
            // 计算积分余额
            Integer balance = user.getIntegral() + record.getIntegral();
            record.setBalance(balance);
            record.setUpdateTime(cn.hutool.core.date.DateUtil.date());

            // 解冻
            Boolean execute = transactionTemplate.execute(e -> {
                updateById(record);
                userService.operationIntegral(record.getUid(), record.getIntegral(), user.getIntegral(), "add");
                return Boolean.TRUE;
            });
            if (!execute) {
                logger.error(StrUtil.format("积分解冻处理—解冻出错，记录id = {}", record.getId()));
            }
        }
    }

    /**
     * PC后台列表
     * @param request 搜索条件
     * @return 记录列表
     */
    @Override
    public PageInfo<UserIntegralRecordResponse> findAdminList(AdminIntegralSearchRequest request) {
        Page<UserIntegralRecordResponse> page = PageHelper.startPage(request.getPage(), request.getLimit());
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
        List<UserIntegralRecordResponse> responseList = dao.findRecordPageList(map);
        //LambdaQueryWrapper<UserIntegralRecord> lqw = Wrappers.lambdaQuery();
        //lqw.select(UserIntegralRecord::getId, UserIntegralRecord::getTitle, UserIntegralRecord::getBalance, UserIntegralRecord::getIntegral,
        //        UserIntegralRecord::getMark, UserIntegralRecord::getUid, UserIntegralRecord::getUpdateTime);
        //lqw.eq(UserIntegralRecord::getStatus, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
        //if (ObjectUtil.isNotNull(request.getUid())) {
        //    lqw.eq(UserIntegralRecord::getUid, request.getUid());
        //}
        //if (StrUtil.isNotBlank(request.getKeywords())) {
        //    List<Integer> idList = userService.findIdListLikeName(request.getKeywords());
        //    if (CollUtil.isNotEmpty(idList)) {
        //        lqw.in(UserIntegralRecord::getUid, idList);
        //    } else {
        //        return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        //    }
        //}
        ////时间范围
        //if (StrUtil.isNotBlank(request.getDateLimit())) {
        //    DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
        //    //判断时间
        //    int compareDateResult = QianxuDateUtil.compareDate(dateLimit.getEndTime(), dateLimit.getStartTime(), Constants.DATE_FORMAT);
        //    if (compareDateResult == -1) {
        //        throw new QianxuException("开始时间不能大于结束时间！");
        //    }
        //
        //    lqw.between(UserIntegralRecord::getUpdateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
        //}
        //lqw.orderByDesc(UserIntegralRecord::getUpdateTime);
        //List<UserIntegralRecord> list = dao.selectList(lqw);
        //if (CollUtil.isEmpty(list)) {
        //    return CommonPage.copyPageInfo(page, CollUtil.newArrayList());
        //}
        //List<UserIntegralRecordResponse> responseList = list.stream().map(i -> {
        //    UserIntegralRecordResponse response = new UserIntegralRecordResponse();
        //    BeanUtils.copyProperties(i, response);
        //    // 获取用户昵称
        //    User user = userService.getById(i.getUid());
        //    if (ObjectUtil.isNotNull(user)) {
        //        response.setNickName(user.getNickname());
        //    } else {
        //        response.setNickName("");
        //    }
        //    return response;
        //}).collect(Collectors.toList());
        return CommonPage.copyPageInfo(page, responseList);
    }

    /**
     * 根据类型条件计算积分总数
     * @param uid 用户uid
     * @param type 类型：1-增加，2-扣减
     * @param date 日期
     * @param linkTypeList 关联类型
     * @return 积分总数
     */
    @Override
    public Integer getSumIntegral(Integer uid, Integer type, String date, List<String> linkTypeList) {
        QueryWrapper<UserIntegralRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(integral) as integral");
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("type", type);
        if (CollUtil.isNotEmpty(linkTypeList)) {
            queryWrapper.in("link_type", linkTypeList);
        }
        queryWrapper.eq("status", IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
        if (StrUtil.isNotBlank(date)) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(date);
            queryWrapper.between("update_time", dateLimit.getStartTime(), dateLimit.getEndTime());
        }
        UserIntegralRecord integralRecord = dao.selectOne(queryWrapper);
        if (ObjectUtil.isNull(integralRecord) || ObjectUtil.isNull(integralRecord.getIntegral())) {
            return 0;
        }
        return integralRecord.getIntegral();
    }

    /**
     * H5用户积分列表
     * @param uid 用户uid
     * @param pageParamRequest 分页参数
     * @return 记录列表
     */
    @Override
    public List<UserIntegralRecord> findUserIntegralRecordList(Integer uid, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserIntegralRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserIntegralRecord::getUid, uid);
        lqw.eq(UserIntegralRecord::getStatus, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
        lqw.orderByDesc(UserIntegralRecord::getUpdateTime);
        return dao.selectList(lqw);
    }

    /**
     * 获取用户冻结的积分
     * @param uid 用户uid
     * @return 积分数量
     */
    @Override
    public Integer getFrozenIntegralByUid(Integer uid) {
        QueryWrapper<UserIntegralRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(integral) as integral");
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("type", IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
        queryWrapper.eq("link_type", IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_ORDER);
        queryWrapper.eq("status", IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN);
        UserIntegralRecord integralRecord = dao.selectOne(queryWrapper);
        if (ObjectUtil.isNull(integralRecord) || ObjectUtil.isNull(integralRecord.getIntegral())) {
            return 0;
        }
        return integralRecord.getIntegral();
    }

    /**
     * 获取需要解冻的记录列表
     * @return 记录列表
     */
    private List<UserIntegralRecord> findThawList() {
        LambdaQueryWrapper<UserIntegralRecord> lqw = Wrappers.lambdaQuery();
        lqw.le(UserIntegralRecord::getThawTime, System.currentTimeMillis());
        lqw.eq(UserIntegralRecord::getLinkType, IntegralRecordConstants.INTEGRAL_RECORD_LINK_TYPE_ORDER);
        lqw.eq(UserIntegralRecord::getType, IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
        lqw.eq(UserIntegralRecord::getStatus, IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN);
        return dao.selectList(lqw);
    }
}

