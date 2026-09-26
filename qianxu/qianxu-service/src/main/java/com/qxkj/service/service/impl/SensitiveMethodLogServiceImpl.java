package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.log.SensitiveMethodLog;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.service.dao.SensitiveMethodLogDao;
import com.qxkj.service.service.SensitiveMethodLogService;
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
public class SensitiveMethodLogServiceImpl extends ServiceImpl<SensitiveMethodLogDao, SensitiveMethodLog> implements SensitiveMethodLogService {

    @Resource
    private SensitiveMethodLogDao dao;

    /**
     * 添加敏感记录
     * @param methodLog 记录信息
     */
    @Override
    public void addLog(SensitiveMethodLog methodLog) {
        save(methodLog);
    }

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<SensitiveMethodLog> getPageList(PageParamRequest pageParamRequest) {
        Page<SensitiveMethodLog> logPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        LambdaQueryWrapper<SensitiveMethodLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(SensitiveMethodLog::getId);
        List<SensitiveMethodLog> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(logPage, list);
    }

    /**
     * 清空全部操作日志
     * @return Boolean
     */
    @Override
    public Boolean clearAll() {
        LambdaQueryWrapper<SensitiveMethodLog> lqw = Wrappers.lambdaQuery();
        lqw.gt(SensitiveMethodLog::getId, 0);
        return dao.delete(lqw) >= 0;
    }
}

