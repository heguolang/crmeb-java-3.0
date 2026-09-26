package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.ExperienceRecordConstants;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.model.user.UserExperienceRecord;
import com.qxkj.service.dao.UserExperienceRecordDao;
import com.qxkj.service.service.UserExperienceRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class UserExperienceRecordServiceImpl extends ServiceImpl<UserExperienceRecordDao, UserExperienceRecord> implements UserExperienceRecordService {

    @Resource
    private UserExperienceRecordDao dao;

    /**
     * 获取用户经验列表（移动端）
     * @param userId 用户id
     * @param pageParamRequest 分页参数
     * @return List
     */
    @Override
    public List<UserExperienceRecord> getH5List(Integer userId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserExperienceRecord> lqw = Wrappers.lambdaQuery();
        lqw.select(UserExperienceRecord::getId, UserExperienceRecord::getTitle, UserExperienceRecord::getType, UserExperienceRecord::getExperience, UserExperienceRecord::getCreateTime);
        lqw.eq(UserExperienceRecord::getUid, userId);
        lqw.orderByDesc(UserExperienceRecord::getId);
        return dao.selectList(lqw);
    }

    /**
     * 通过订单编号获取记录
     * @param orderNo 订单编号
     * @param uid uid
     * @return UserExperienceRecord
     */
    @Override
    public UserExperienceRecord getByOrderNoAndUid(String orderNo, Integer uid) {
        return getByOrderNoAndUidAndLinkType(orderNo, uid, ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER);
    }

    @Override
    public UserExperienceRecord getByOrderNoAndUidAndLinkType(String orderNo, Integer uid, String linkType) {
        LambdaQueryWrapper<UserExperienceRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserExperienceRecord::getLinkId, orderNo);
        lqw.eq(UserExperienceRecord::getLinkType, linkType);
        lqw.eq(UserExperienceRecord::getUid, uid);
        return dao.selectOne(lqw);
    }

    @Override
    public Integer countCompleteOrderByUid(Integer uid) {
        LambdaQueryWrapper<UserExperienceRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserExperienceRecord::getUid, uid);
        lqw.eq(UserExperienceRecord::getLinkType, ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER_COUNT);
        lqw.eq(UserExperienceRecord::getType, ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_ADD);
        return dao.selectCount(lqw);
    }

    @Override
    public Integer sumPaidConsumptionByUid(Integer uid) {
        return sumOrderConsumptionByTitle(uid, ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_ORDER);
    }

    @Override
    public Integer sumCompleteConsumptionByUid(Integer uid) {
        return sumOrderConsumptionByTitle(uid, ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_ORDER_COMPLETE);
    }

    private Integer sumOrderConsumptionByTitle(Integer uid, String title) {
        LambdaQueryWrapper<UserExperienceRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserExperienceRecord::getUid, uid);
        lqw.eq(UserExperienceRecord::getLinkType, ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER);
        lqw.eq(UserExperienceRecord::getType, ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_ADD);
        lqw.eq(UserExperienceRecord::getTitle, title);
        List<UserExperienceRecord> recordList = dao.selectList(lqw);
        return recordList.stream()
                .mapToInt(record -> ObjectUtil.defaultIfNull(record.getExperience(), 0))
                .sum();
    }
}

