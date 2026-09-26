package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemTeamLevel;
import com.qxkj.common.request.SystemTeamLevelRequest;
import com.qxkj.common.request.SystemTeamLevelUpdateShowRequest;
import com.qxkj.common.response.SystemTeamLevelInfoResponse;

import java.util.List;

/**
 * 团队等级 Service
 */
public interface SystemTeamLevelService extends IService<SystemTeamLevel> {

    List<SystemTeamLevel> getList();

    List<SystemTeamLevel> getAllList();

    SystemTeamLevelInfoResponse getInfo(Integer id);

    Boolean create(SystemTeamLevelRequest request);

    Boolean update(Integer id, SystemTeamLevelRequest request);

    Boolean delete(Integer id);

    Boolean updateShow(SystemTeamLevelUpdateShowRequest request);

    List<SystemTeamLevel> getUsableList();
}
