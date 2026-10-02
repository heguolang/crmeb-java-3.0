// +----------------------------------------------------------------------
// | 模块开关守卫（配合后台隐藏面板的功能开关）
// +----------------------------------------------------------------------
import { HTTP_REQUEST_URL, HEADER } from '@/config/app';

let cache = { ts: 0, data: null };

/** 拉取模块开关状态（60s 缓存） */
export function getModuleSwitches() {
  return new Promise((resolve) => {
    if (cache.data && Date.now() - cache.ts < 60000) {
      return resolve(cache.data);
    }
    uni.request({
      url: HTTP_REQUEST_URL + '/api/front/hidden/switches',
      method: 'GET',
      header: HEADER,
      success: (res) => {
        const d = (res.data && res.data.data) || null;
        cache = { ts: Date.now(), data: d };
        resolve(d);
      },
      fail: () => resolve(null),
    });
  });
}

/**
 * 模块守卫：对应开关关闭时提示并退回上一页
 * @param {string} key teamReward|stock|store|daili|spread
 * @returns {Promise<boolean>} true=可用
 */
export function guardModule(key) {
  return getModuleSwitches().then((s) => {
    if (s && s[key] === false) {
      uni.showToast({ title: '功能未开放', icon: 'none' });
      setTimeout(() => {
        uni.navigateBack({
          fail: () => {
            uni.switchTab({ url: '/pages/index/index' });
          },
        });
      }, 800);
      return false;
    }
    return true;
  });
}
