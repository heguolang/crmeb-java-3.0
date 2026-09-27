// +----------------------------------------------------------------------
// | 拼购活动 API
// +----------------------------------------------------------------------
import request from '@/utils/request.js';

/**
 * 拼购活动列表（首页/专区展示）
 */
export function getPinGouList(data) {
  return request.get('pingou/list', data, { noAuth: true });
}

/**
 * 拼购活动详情（含当前团进度、累计开团次数、最近开奖团）
 */
export function getPinGouDetail(id) {
  return request.get('pingou/detail/' + id, {}, { noAuth: true });
}

/**
 * 活动团列表（历史期数，含成员与中奖情况）
 */
export function getPinGouTeams(activityId, data) {
  return request.get('pingou/teams/' + activityId, data, { noAuth: true });
}

/**
 * 我的拼购参与记录
 */
export function getPinGouMy(data) {
  return request.get('pingou/my', data);
}
