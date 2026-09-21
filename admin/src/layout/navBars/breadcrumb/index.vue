<template>
  <div class="layout-navbars-breadcrumb-index">
    <Logo v-if="setIsShowLogo" />
    <Breadcrumb />
    <Horizontal :menuList="menuList" v-if="isLayoutTransverse" />
    <transverseAside v-if="isLayoutClassic" />
    <User />
  </div>
</template>

<script>
import Breadcrumb from '@/layout/navBars/breadcrumb/breadcrumb.vue';
import User from '@/layout/navBars/breadcrumb/user.vue';
import Logo from '@/layout/logo/index.vue';
import Horizontal from '@/layout/navMenu/horizontal.vue';
import transverseAside from '@/layout/component/transverseAside.vue';
export default {
  name: 'layoutNavBars',
  components: { Breadcrumb, User, Logo, Horizontal, transverseAside },
  data() {
    return {
      menuList: [],
    };
  },
  computed: {
    // 设置 logo 是否显示
    setIsShowLogo() {
      let { isShowLogo, layout } = this.$store.state.themeConfig.themeConfig;
      return (isShowLogo && layout === 'classic') || (isShowLogo && layout === 'transverse');
    },
    // 设置是否显示横向菜单
    isLayoutTransverse() {
      let { layout, isClassicSplitMenu } = this.$store.state.themeConfig.themeConfig;
      return layout === 'transverse' || (isClassicSplitMenu && layout === 'classic');
    },
    isLayoutClassic() {
      let { layout } = this.$store.state.themeConfig.themeConfig;
      return layout === 'classic';
    },
  },
  mounted() {
    this.setFilterRoutes();
    this.bus.$on('routesListChange', () => {
      this.setFilterRoutes();
    });
    // 兜底：Menu 拉取原本挂在 Asides / ColumnsAside 两个组件上，
    // 而横向布局（transverse / classic）不渲染它们，导致新环境
    // （无本地菜单缓存、或非从登录页进入）时顶部菜单为空。这里补一次拉取。
    if (this.isLayoutTransverse && this.menuList.length === 0) {
      this.$store
        .dispatch('user/getMenus')
        .then(() => this.setFilterRoutes())
        .catch(() => {});
    }
  },
  beforeDestroy() {
    this.bus.$off('routesListChange');
  },
  methods: {
    // 设置路由的过滤
    setFilterRoutes() {
      this.menuList = this.filterRoutesFun(this.$store.state.user.menuList);
    },
    // 设置路由的过滤递归函数
    filterRoutesFun(arr) {
      return arr
        .filter((item) => item.path)
        .map((item) => {
          item = Object.assign({}, item);
          if (item.children.length) item.children = this.filterRoutesFun(item.children);
          return item;
        });
    },
  },
  watch: {
    // 监听 vuex 数据变化
    '$store.state': {
      handler(val) {
        if (val.user.menuListlength === this.menuList.length) return false;
        this.setFilterRoutes();
      },
      deep: true,
    },
  },
};
</script>

<style scoped lang="scss">
.layout-navbars-breadcrumb-index {
  height: 56px;
  display: flex;
  align-items: center;
  background: var(--prev-bg-topBar);
  /* 深色顶栏下浅色分隔线会突兀，改用轻微暗色描边 */
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
  padding-left: 15px;
}
</style>
