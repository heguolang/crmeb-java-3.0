// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import defaultSettings from '@/settings';
import Cache from '@/plugins/cache';
const { showSettings, tagsView, fixedHeader, sidebarLogo, topNav, sideTheme, navIcon } = defaultSettings;
const storageSetting = Cache.local.has('layout-setting') ? Cache.local.getJSON('layout-setting') : '';
const state = {
  theme: storageSetting.theme || '#409EFF',
  sideTheme: storageSetting.sideTheme || sideTheme,
  topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
  showSettings: showSettings,
  tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
  fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
  sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
  navIcon: storageSetting.navIcon === undefined ? navIcon : storageSetting.navIcon,
  frontDomain: localStorage.getItem('frontDomain') || '', //移动端域名
  mediaDomain: localStorage.getItem('mediaDomain') || '', //图片域名
  mobileTheme: localStorage.getItem('mobileTheme') || '',
};
const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    if (state.hasOwnProperty(key)) {
      state[key] = value;
    }
  },
  SET_FrontDomain(state, frontDomain) {
    state.frontDomain = frontDomain;
    localStorage.setItem('frontDomain', frontDomain);
    if (!frontDomain) localStorage.removeItem('frontDomain');
  },
  // 移动端主题色
  SET_mobileThemeColor(state, mobileTheme) {
    state.mobileTheme = mobileTheme;
    localStorage.setItem('mobileTheme', mobileTheme);
    if (!mobileTheme) localStorage.removeItem('mobileTheme');
  },
  SET_mediaDomain(state, mediaDomain) {
    state.mediaDomain = mediaDomain;
    localStorage.setItem('mediaDomain', mediaDomain);
    if (!mediaDomain) localStorage.removeItem('mediaDomain');
  },
};

const actions = {
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
};
