package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.pingou.PingouLevel;
import com.qxkj.service.dao.PingouLevelDao;
import com.qxkj.service.service.PingouLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 拼购等级设置（全局默认分享奖励阶梯）服务实现
 */
@Service
public class PingouLevelServiceImpl extends ServiceImpl<PingouLevelDao, PingouLevel> implements PingouLevelService {

    @Autowired
    private PingouLevelDao dao;

    @Override
    public List<PingouLevel> getLevelList() {
        return dao.selectList(new LambdaQueryWrapper<PingouLevel>()
                .eq(PingouLevel::getIsDel, false)
                .orderByDesc(PingouLevel::getSort)
                .orderByAsc(PingouLevel::getMinDirectNum));
    }

    @Override
    public boolean saveLevel(PingouLevel level) {
        checkLevel(level);
        level.setId(null);
        level.setIsDel(false);
        return dao.insert(level) > 0;
    }

    @Override
    public boolean updateLevel(PingouLevel level) {
        if (level.getId() == null) {
            throw new QianxuException("缺少等级ID");
        }
        checkLevel(level);
        level.setIsDel(null);
        return dao.updateById(level) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLevel(Integer id) {
        PingouLevel level = dao.selectById(id);
        if (level == null || Boolean.TRUE.equals(level.getIsDel())) {
            throw new QianxuException("等级不存在或已删除");
        }
        level.setIsDel(true);
        return dao.updateById(level) > 0;
    }

    private void checkLevel(PingouLevel level) {
        if (level.getName() == null || level.getName().trim().isEmpty()) {
            throw new QianxuException("请填写等级名称");
        }
        if (level.getMinDirectNum() == null || level.getMinDirectNum() < 1) {
            throw new QianxuException("直推人数下限必须大于等于1");
        }
        if (level.getRewardRate() == null || level.getRewardRate().compareTo(java.math.BigDecimal.ZERO) < 0
                || level.getRewardRate().compareTo(new java.math.BigDecimal("100")) > 0) {
            throw new QianxuException("分享奖励比例必须在 0~100 之间");
        }
    }
}
