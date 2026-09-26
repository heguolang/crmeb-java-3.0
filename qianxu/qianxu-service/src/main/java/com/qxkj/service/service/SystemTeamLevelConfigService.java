package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemTeamLevelConfig;
import com.qxkj.common.request.SystemTeamLevelConfigRequest;
import com.qxkj.common.request.TeamBrokerageManageRequest;

import java.util.List;
import java.util.Map;

/**
 * 团队等级配置 Service
 */
public interface SystemTeamLevelConfigService extends IService<SystemTeamLevelConfig> {

    List<SystemTeamLevelConfig> getList();

    SystemTeamLevelConfig getByTeamLevelId(Integer teamLevelId);

    Map<Integer, SystemTeamLevelConfig> mapByTeamLevelIds(List<Integer> teamLevelIds);

    Boolean saveOrUpdateByTeamLevelId(Integer teamLevelId, SystemTeamLevelConfigRequest request);

    Boolean deleteByTeamLevelId(Integer teamLevelId);

    TeamBrokerageManageRequest getManageInfo();

    Boolean setManageInfo(TeamBrokerageManageRequest request);
}
