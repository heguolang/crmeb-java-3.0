package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.log.AdminLoginLog;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.AdminLoginLogSearchRequest;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.vo.DateLimitUtilVo;
import com.qxkj.service.dao.AdminLoginLogDao;
import com.qxkj.service.service.AdminLoginLogService;
import org.apache.commons.lang3.StringUtils;
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
public class AdminLoginLogServiceImpl extends ServiceImpl<AdminLoginLogDao, AdminLoginLog> implements AdminLoginLogService {

    @Resource
    private AdminLoginLogDao dao;

    /**
     * 添加登录日志
     * @param loginLog 日志信息
     */
    @Override
    public void addLog(AdminLoginLog loginLog) {
        if (loginLog.getAdminId() == null) {
            loginLog.setAdminId(0);
        }
        if (loginLog.getStatus() == null) {
            loginLog.setStatus(0);
        }
        if (StringUtils.isBlank(loginLog.getAdminAccount())) {
            loginLog.setAdminAccount("");
        }
        save(loginLog);
    }

    /**
     * 分页列表
     * @param request 搜索条件
     * @return PageInfo
     */
    @Override
    public PageInfo<AdminLoginLog> getPageList(AdminLoginLogSearchRequest request) {
        Page<AdminLoginLog> logPage = PageHelper.startPage(request.getPage(), request.getLimit());

        LambdaQueryWrapper<AdminLoginLog> lqw = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.getAdminAccount())) {
            lqw.like(AdminLoginLog::getAdminAccount, request.getAdminAccount().trim());
        }
        if (request.getStatus() != null) {
            lqw.eq(AdminLoginLog::getStatus, request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getIp())) {
            lqw.like(AdminLoginLog::getIp, request.getIp().trim());
        }
        if (StringUtils.isNotBlank(request.getDateLimit())) {
            DateLimitUtilVo dateLimit = QianxuDateUtil.getDateLimit(request.getDateLimit());
            if (StringUtils.isNotBlank(dateLimit.getStartTime()) && StringUtils.isNotBlank(dateLimit.getEndTime())) {
                lqw.between(AdminLoginLog::getCreateTime, dateLimit.getStartTime(), dateLimit.getEndTime());
            }
        }
        lqw.orderByDesc(AdminLoginLog::getId);
        List<AdminLoginLog> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(logPage, list);
    }

    /**
     * 删除日志
     * @param id 日志id
     * @return Boolean
     */
    @Override
    public Boolean deleteById(Integer id) {
        return dao.deleteById(id) > 0;
    }

    /**
     * 清空全部登录日志
     * @return Boolean
     */
    @Override
    public Boolean clearAll() {
        LambdaQueryWrapper<AdminLoginLog> lqw = Wrappers.lambdaQuery();
        lqw.gt(AdminLoginLog::getId, 0);
        return dao.delete(lqw) >= 0;
    }
}
