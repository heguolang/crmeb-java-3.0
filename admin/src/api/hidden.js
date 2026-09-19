// +----------------------------------------------------------------------
// | 系统运维隐藏面板 API
// +----------------------------------------------------------------------
import request from '@/utils/request';

/**
 * 面板总览（数据行数 + 开关状态）
 */
export function hiddenInfo() {
  return request({
    url: '/admin/hidden/info',
    method: 'GET',
  });
}

/**
 * 一键清除
 * @param type loginLog|operateLog|user|money|balance|integral|all
 * @param confirm 确认串，必须为 DELETE
 */
export function hiddenClear(type, confirm) {
  return request({
    url: `/admin/hidden/clear/${type}`,
    method: 'POST',
    params: { confirm },
  });
}

/**
 * 设置模块开关
 */
export function hiddenSetSwitch(key, value) {
  return request({
    url: '/admin/hidden/switch/set',
    method: 'POST',
    params: { key, value },
  });
}
