import request from '@/utils/request';

/** 团队等级 -- 列表 */
export function teamLevelListApi() {
  return request({
    url: '/admin/system/team/level/list',
    method: 'get',
  });
}

/** 团队等级 -- 全部列表 */
export function teamLevelAllApi() {
  return request({
    url: '/admin/system/team/level/all',
    method: 'get',
  });
}

/** 团队等级 -- 详情 */
export function teamLevelInfoApi(params) {
  return request({
    url: '/admin/system/team/level/info',
    method: 'get',
    params,
  });
}

/** 团队等级 -- 新增 */
export function teamLevelSaveApi(data) {
  return request({
    url: '/admin/system/team/level/save',
    method: 'post',
    data,
  });
}

/** 团队等级 -- 更新 */
export function teamLevelUpdateApi(id, data) {
  return request({
    url: `/admin/system/team/level/update/${id}`,
    method: 'post',
    data,
  });
}

/** 团队等级 -- 删除 */
export function teamLevelDeleteApi(id) {
  return request({
    url: `/admin/system/team/level/delete/${id}`,
    method: 'post',
  });
}

/** 团队等级 -- 启用/禁用 */
export function teamLevelUseApi(data) {
  return request({
    url: '/admin/system/team/level/use',
    method: 'post',
    data,
  });
}

/** 团队奖全局配置 -- 获取 */
export function teamBrokerageManageGetApi() {
  return request({
    url: '/admin/system/team/level/config/manage/get',
    method: 'get',
  });
}

/** 团队奖全局配置 -- 保存 */
export function teamBrokerageManageSetApi(data) {
  return request({
    url: '/admin/system/team/level/config/manage/set',
    method: 'post',
    data,
  });
}

/** 团队等级配置 -- 列表 */
export function teamLevelConfigListApi() {
  return request({
    url: '/admin/system/team/level/config/list',
    method: 'get',
  });
}

/** 团队等级配置 -- 详情 */
export function teamLevelConfigInfoApi(teamLevelId) {
  return request({
    url: '/admin/system/team/level/config/info',
    method: 'get',
    params: { teamLevelId },
  });
}

/** 团队等级配置 -- 保存 */
export function teamLevelConfigSaveApi(data) {
  return request({
    url: '/admin/system/team/level/config/save',
    method: 'post',
    data,
  });
}

/** 团队等级配置 -- 删除 */
export function teamLevelConfigDeleteApi(teamLevelId) {
  return request({
    url: `/admin/system/team/level/config/delete/${teamLevelId}`,
    method: 'post',
  });
}

/** 团队关联用户 -- 列表 */
export function teamLevelUserListApi(params) {
  return request({
    url: '/admin/system/team/level/user/list',
    method: 'get',
    params,
  });
}

/** 团队变更记录 -- 列表 */
export function teamLevelRecordListApi(params) {
  return request({
    url: '/admin/system/team/level/record/list',
    method: 'get',
    params,
  });
}

/** 团队奖资金记录 -- 列表 */
export function teamBrokerageRecordListApi(params) {
  return request({
    url: '/admin/system/team/level/brokerage/record',
    method: 'get',
    params,
  });
}
