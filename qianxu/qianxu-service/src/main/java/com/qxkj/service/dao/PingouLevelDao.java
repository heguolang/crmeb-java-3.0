package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.pingou.PingouLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拼购等级设置 Dao
 */
@Mapper
public interface PingouLevelDao extends BaseMapper<PingouLevel> {
}
