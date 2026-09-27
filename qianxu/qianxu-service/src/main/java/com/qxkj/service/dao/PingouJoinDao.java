package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.pingou.PingouJoin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拼购参与明细 Dao
 */
@Mapper
public interface PingouJoinDao extends BaseMapper<PingouJoin> {
}
