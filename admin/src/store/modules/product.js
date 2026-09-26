// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import { themeProductCategory } from '@/api/theme';

const state = {
  adminProductClassify: localStorage.getItem('adminProductClassify')
    ? JSON.parse(localStorage.getItem('adminProductClassify'))
    : [] /** 平台商品分类 **/,
};

const mutations = {
  SET_AdminProductClassify: (state, adminProductClassify) => {
    state.adminProductClassify = adminProductClassify;
    localStorage.setItem('adminProductClassify', JSON.stringify(changeNodes(adminProductClassify)));
    if (!adminProductClassify.length) localStorage.removeItem('adminProductClassify');
  },
};

const actions = {
  /** 平台商品分类 **/
  getAdminProductClassify({ commit, dispatch }) {
    return new Promise((resolve, reject) => {
      themeProductCategory({ status: -1 })
        .then(async (res) => {
          const list = res.data || [];
          commit('SET_AdminProductClassify', changeNodes(list));
          resolve(list);
        })
        .catch((error) => {
          reject(error);
        });
    });
  },
};

/** tree去除 childList=[] 的结构**/
const changeNodes = function (data) {
  if (data.length > 0) {
    for (var i = 0; i < data.length; i++) {
      if (data[i].isShow === false) {
        data[i].disabled = true;
      }
      if (!data[i].child || data[i].child.length < 1) {
        data[i].child = undefined;
      } else {
        changeNodes(data[i].child);
      }
    }
  }
  return data;
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  changeNodes,
};
