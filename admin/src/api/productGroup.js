import request from '@/utils/request';

/** 商品分组分页列表 */
export function productGroupListApi(params) {
  return request({
    url: '/admin/store/product/group/list',
    method: 'get',
    params,
  });
}

/** 启用分组简表 */
export function productGroupSimpleListApi() {
  return request({
    url: '/admin/store/product/group/simple/list',
    method: 'get',
  });
}

/** 商品分组详情 */
export function productGroupInfoApi(params) {
  return request({
    url: '/admin/store/product/group/info',
    method: 'get',
    params,
  });
}

/** 新增商品分组 */
export function productGroupSaveApi(data) {
  return request({
    url: '/admin/store/product/group/save',
    method: 'post',
    data,
  });
}

/** 修改商品分组 */
export function productGroupUpdateApi(params, data) {
  return request({
    url: '/admin/store/product/group/update',
    method: 'post',
    params,
    data,
  });
}

/** 删除商品分组 */
export function productGroupDeleteApi(params) {
  return request({
    url: '/admin/store/product/group/delete',
    method: 'get',
    params,
  });
}

/** 更新状态 */
export function productGroupStatusApi(params) {
  return request({
    url: '/admin/store/product/group/status',
    method: 'post',
    params,
  });
}

/** 获取分组的装修页ID（不存在则后端懒创建），返回 eb_theme.id */
export function productGroupThemeApi(id) {
  return request({
    url: `/admin/store/product/group/theme/${id}`,
    method: 'post',
  });
}

/** 获取全局配置 */
export function productGroupConfigApi() {
  return request({
    url: '/admin/store/product/group/config',
    method: 'get',
  });
}

/** 保存全局配置 */
export function productGroupConfigSaveApi(data) {
  return request({
    url: '/admin/store/product/group/config',
    method: 'post',
    data,
  });
}

/** 批量将商品移出分组（groupIds 为空表示移除全部分组） */
export function productGroupBatchUnbindApi(data) {
  return request({
    url: '/admin/store/product/group/batch/unbind',
    method: 'post',
    data,
  });
}

/** 批量将商品加入分组 */
export function productGroupBatchBindApi(data) {
  return request({
    url: '/admin/store/product/group/batch/bind',
    method: 'post',
    data,
  });
}
