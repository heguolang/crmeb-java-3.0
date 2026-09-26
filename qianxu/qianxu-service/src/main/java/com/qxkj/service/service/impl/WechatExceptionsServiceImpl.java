package com.qxkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.wechat.WechatExceptions;
import com.qxkj.service.dao.WechatExceptionsDao;
import com.qxkj.service.service.WechatExceptionsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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
public class WechatExceptionsServiceImpl extends ServiceImpl<WechatExceptionsDao, WechatExceptions> implements WechatExceptionsService {

    @Resource
    private WechatExceptionsDao dao;


    /**
     * 自动删除日志
     * 只保留十天的日志
     */
    @Override
    public void autoDeleteLog() {
        String beforeDate = DateUtil.offsetDay(new Date(), -9).toString("yyyy-MM-dd");
        UpdateWrapper<WechatExceptions> wrapper = Wrappers.update();
        wrapper.lt("create_time", beforeDate);
        dao.delete(wrapper);
    }
}

