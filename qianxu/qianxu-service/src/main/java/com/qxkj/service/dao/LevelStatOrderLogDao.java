package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.user.LevelStatOrderLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 等级统计幂等流水 Mapper 接口
 */
public interface LevelStatOrderLogDao extends BaseMapper<LevelStatOrderLog> {

    /**
     * 抢占式插入：返回 1 表示首次处理（可继续），返回 0 表示已处理过（应跳过）。
     * 依赖唯一键 uk_order_module_scene。
     */
    @Insert("INSERT IGNORE INTO eb_level_stat_order_log(order_no, module, scene) VALUES (#{orderNo}, #{module}, #{scene})")
    int insertIgnore(@Param("orderNo") String orderNo, @Param("module") String module, @Param("scene") String scene);
}
