import request from '@/utils/request';

/** 分销商等级 -- 列表 */
export function distributorLevelListApi() {
  return request({
    url: '/admin/distributor/level/list',
    method: 'get',
  });
}

/** 分销商等级 -- 详情 */
export function distributorLevelInfoApi(id) {
  return request({
    url: `/admin/distributor/level/info/${id}`,
    method: 'get',
  });
}

/** 分销商等级 -- 新增 */
export function distributorLevelSaveApi(data) {
  return request({
    url: '/admin/distributor/level/save',
    method: 'post',
    data,
  });
}

/** 分销商等级 -- 更新 */
export function distributorLevelUpdateApi(id, data) {
  return request({
    url: `/admin/distributor/level/update/${id}`,
    method: 'post',
    data,
  });
}

/** 分销商等级 -- 删除 */
export function distributorLevelDeleteApi(id) {
  return request({
    url: `/admin/distributor/level/delete/${id}`,
    method: 'post',
  });
}

/** 分销商等级 -- 启用/隐藏 */
export function distributorLevelUseApi(id, isShow) {
  return request({
    url: `/admin/distributor/level/use/${id}/${isShow}`,
    method: 'post',
  });
}
