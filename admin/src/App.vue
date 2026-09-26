<template>
  <div id="app">
    <router-view v-if="isRouterAlive" />
    <!--
      说明：原这里常驻挂载 <Setings />（主题设置抽屉）。
      它会通过 documentElement.style.setProperty 把 themeStyle 的变量写成内联样式，
      内联样式优先级高于 theme/app.scss 里的 :root 定义，导致"固定主题"失效。
      主题已固定，故整个组件移除，主题变量统一由 src/theme/app.scss 的 :root 决定。
    -->
  </div>
</template>

<script>
// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
import { Local } from '@/utils/storage.js';

export default {
  name: 'App',

  provide() {
    return {
      reload: this.reload,
    };
  },
  data() {
    return {
      isRouterAlive: true,
    };
  },
  watch: {
    // 监听路由 控制侧边栏显示 标记当前顶栏菜单（如需要）
    $route(to, from) {
      const onRoutes = to.meta.activeMenu ? to.meta.activeMenu : to.meta.path;
      this.$store.commit('menu/setActivePath', onRoutes);
      if (to.name == 'crud_crud') {
        this.$store.state.user.oneLvRoutes.map((e) => {
          if (e.path === to.path) {
            to.meta.title = e.title;
          }
        });
      }
      //优惠券、秒杀活动
      if (['creatProduct', 'CreatCoupon', 'CreatSeckill', 'CreatTag', 'border', 'articleCreat'].includes(to.name)) {
        let route = to.matched[1].path.split(':')[0];
        this.$store.state.user.oneLvRoutes.map((e) => {
          if (route.indexOf(e.path) != -1) {
            to.meta.title = `${e.title} ${to.params.id ? 'ID:' + to.params.id : ''}`;
          }
        });
      }
      //个人中心、修改密码
      if (['MaintainUser', 'MaintainUpdate'].includes(to.name)) {
        this.bus.$emit('oneCatName', '控制台');
      }
    },
  },
  mounted() {
    this.getLayoutThemeConfig();
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
    // 获取缓存中的布局配置
    // 说明：后台主题样式已按需求固定（不允许用户自定义），因此这里不再读取本地的个性化配置，
    // 一律使用 src/theme/app.scss 的 :root 默认值；
    // 同时清理历史遗留的自定义缓存，避免旧配置继续生效导致界面不一致。
    getLayoutThemeConfig() {
      Local.remove('JavaPlatThemeConfigPrev');
      Local.remove('JavaPlatThemeConfigStyle');
    },
  },
  destroyed() {
    this.bus.$off('openSetingsDrawer');
  },
};
</script>
