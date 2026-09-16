package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.zbkj.common.model.user.UserTeamLevel;
import com.zbkj.common.response.UserTeamLevelRecordResponse;
import com.zbkj.common.response.UserTeamLevelUserResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户团队等级记录表 Mapper 接口
 */
public interface UserTeamLevelDao extends BaseMapper<UserTeamLevel> {
    /**
     * 团队关联用户分页
     */
    Page<UserTeamLevelUserResponse> getTeamUserPage(@Param("keywords") String keywords,
                                                    @Param("teamLevelId") Integer teamLevelId);

    /**
     * 团队等级变更记录分页
     */
    Page<UserTeamLevelRecordResponse> getTeamRecordPage(@Param("keywords") String keywords,
                                                        @Param("teamLevelId") Integer teamLevelId,
                                                        @Param("status") Integer status);

    /**
     * 统计团队（整条推荐链所有下级）中当前会员等级等于目标等级的人数。
     * 递归 CTE 沿 spread_uid 下钻，depth 上限 50 防御异常循环链。
     */
    @Select("WITH RECURSIVE team_chain(uid, depth) AS (" +
            " SELECT uid, 1 FROM eb_user WHERE spread_uid = #{uid}" +
            " UNION ALL" +
            " SELECT u.uid, tc.depth + 1 FROM eb_user u INNER JOIN team_chain tc ON u.spread_uid = tc.uid WHERE tc.depth < 50" +
            ") SELECT COUNT(*) FROM team_chain tc INNER JOIN eb_user u ON u.uid = tc.uid WHERE u.level = #{levelId}")
    int countTeamLevelUsers(@Param("uid") Integer uid, @Param("levelId") Integer levelId);
}

