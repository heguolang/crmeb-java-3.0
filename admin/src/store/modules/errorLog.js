// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

const state = {
  logs: [],
};

const mutations = {
  ADD_ERROR_LOG: (state, log) => {
    state.logs.push(log);
  },
  CLEAR_ERROR_LOG: (state) => {
    state.logs.splice(0);
  },
};

const actions = {
  addErrorLog({ commit }, log) {
    commit('ADD_ERROR_LOG', log);
  },
  clearErrorLog({ commit }) {
    commit('CLEAR_ERROR_LOG');
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
};
