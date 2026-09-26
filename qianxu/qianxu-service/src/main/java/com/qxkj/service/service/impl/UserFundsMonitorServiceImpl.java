package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.vo.UserFundsMonitor;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.request.BrokerageRecordRequest;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.service.dao.UserFundsMonitorDao;
import com.qxkj.service.service.UserBrokerageRecordService;
import com.qxkj.service.service.UserFundsMonitorService;
import com.qxkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
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
public class UserFundsMonitorServiceImpl extends ServiceImpl<UserFundsMonitorDao, UserFundsMonitor> implements UserFundsMonitorService {

    @Resource
    private UserFundsMonitorDao dao;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private UserService userService;

    /**
     * 佣金记录
     * @param request 筛选条件
     * @return PageInfo
     */
    @Override
    public PageInfo<UserBrokerageRecord> getBrokerageRecord(BrokerageRecordRequest request) {
        PageInfo<UserBrokerageRecord> pageInfo = userBrokerageRecordService.getAdminList(request);
        List<UserBrokerageRecord> list = pageInfo.getList();
        if (CollUtil.isEmpty(list)) {
            pageInfo.setList(list);
            return pageInfo;
        }
        List<Integer> uidList = list.stream().map(e -> e.getUid()).distinct().collect(Collectors.toList());
        HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
        list.forEach(e -> {
            if (e.getLinkType().equals(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_WITHDRAW)
                    && e.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE)
                    && e.getType().equals(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_SUB)) {
                e.setTitle("提现成功");
            }
            String name = "-";
            if(ObjectUtil.isNotNull(userMap.get(e.getUid()))){
                name = userMap.get(e.getUid()).getNickname();
            }
            e.setUserName(name);
        });
        pageInfo.setList(list);
        return pageInfo;
    }


}

