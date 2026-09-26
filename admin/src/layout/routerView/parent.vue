<template>
  <!-- 顶部标签栏（tagsView）已删除，固定使用 h101 高度 -->
  <div class="h101">
    <keep-alive :include="keepAliveNameList">
      <router-view :key="refreshRouterViewKey" />
    </keep-alive>
  </div>
</template>

<script>
export default {
  name: 'parent',
  data() {
    return {
      refreshRouterViewKey: null,
      keepAliveNameList: [],
      keepAliveNameNewList: [],
    };
  },
  computed: {
    // 设置主界面切换动画
    setTransitionName() {
      return this.$store.state.themeConfig.themeConfig.animation;
    },
  },
  created() {
    this.keepAliveNameList = this.getKeepAliveNames();
    this.bus.$on('onTagsViewRefreshRouterView', (path) => {
      if (this.$route.path !== path) return false;
      this.keepAliveNameList = this.getKeepAliveNames().filter((name) => this.$route.name !== name);
      this.refreshRouterViewKey = this.$route.path;
      this.$nextTick(() => {
        this.refreshRouterViewKey = null;
        this.keepAliveNameList = this.getKeepAliveNames();
      });
    });
  },

  methods: {
    // 获取路由缓存列表（name），默认路由全部缓存
    getKeepAliveNames() {
      return this.$store.state.menu.keepAliveNames;
    },
  },
};
</script>
