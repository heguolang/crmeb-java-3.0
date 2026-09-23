package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.user.UserTeamLevelStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 用户团队等级统计 Mapper 接口
 */
public interface UserTeamLevelStatDao extends BaseMapper<UserTeamLevelStat> {

    /**
     * 原子累加统计字段，避免「先查后写」在并发下丢失更新。
     * 结果下限为 0，负数（退款回退超扣）自动归零。
     *
     * @param column 列名，仅允许 StatColumn 白名单中的值
     * @param delta  增量，可为负
     */
    @Update("UPDATE eb_user_team_level_stat SET ${column} = GREATEST(${column} + #{delta}, 0) WHERE uid = #{uid}")
    int incrColumn(@Param("uid") Integer uid, @Param("column") String column, @Param("delta") BigDecimal delta);

    /**
     * 周期清零：把所有统计金额归零（由配置项控制是否启用，默认不启用）
     */
    @Update("UPDATE eb_user_team_level_stat SET self_paid_amount = 0, self_complete_amount = 0, " +
            "team_paid_amount = 0, team_complete_amount = 0, direct_paid_amount = 0, direct_complete_amount = 0")
    int resetAllAmount();
}
