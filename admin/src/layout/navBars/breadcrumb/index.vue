<template>
  <!-- 2026-10-04 方案 B：顶栏拆双层，解决「56px 单行塞 Logo+面包屑+菜单+用户区」的拥挤问题 -->
  <div class="layout-navbars-breadcrumb-index">
    <!-- 第一层·导航层 56px：深空渐变底，承载品牌与横向菜单 -->
    <div class="nav-row">
      <Logo v-if="setIsShowLogo" />
      <Horizontal :menuList="menuList" v-if="isLayoutTransverse" class="nav-menu" />
      <transverseAside v-if="isLayoutClassic" />
      <User />
    </div>
    <!-- 第二层·页面层 48px：白底，承载面包屑与页面级通用操作 -->
    <div class="page-row">
      <Breadcrumb />
      <div class="page-actions">
        <div class="pa-btn" title="刷新当前页" @click="onRefresh">
          <i class="el-icon-refresh"></i><span>刷新</span>
        </div>
        <div class="pa-btn" title="全屏显示" @click="onToggleFullscreen">
          <i class="el-icon-full-screen"></i><span>{{ isFullscreen ? '退出全屏' : '全屏' }}</span>
        </div>
      </div>
    </div>
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
      isFullscreen: false,
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
    document.addEventListener('fullscreenchange', this.onFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.onFullscreenChange);
  },
  beforeDestroy() {
    this.bus.$off('routesListChange');
    document.removeEventListener('fullscreenchange', this.onFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.onFullscreenChange);
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
    // 刷新当前路由（复用 tagsView 的刷新逻辑，避免直接 reload 丢失状态）
    onRefresh() {
      this.bus.$emit('onCurrentContextmenuClick', Object.assign({}, this.$route, { contextMenuClickId: 0, meta: { isRefresh: true } }));
    },
    // 全屏切换
    onToggleFullscreen() {
      const el = document.documentElement;
      if (!document.fullscreenElement && !document.webkitFullscreenElement) {
        (el.requestFullscreen || el.webkitRequestFullscreen || function () {}).call(el);
        this.isFullscreen = true;
      } else {
        (document.exitFullscreen || document.webkitExitFullscreen || function () {}).call(document);
        this.isFullscreen = false;
      }
    },
    onFullscreenChange() {
      this.isFullscreen = !!(document.fullscreenElement || document.webkitFullscreenElement);
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
/* 方案 B「极光科技」双层顶栏：导航层 56 + 页面层 48 = 104 */
.layout-navbars-breadcrumb-index {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 104px;
  flex: 0 0 auto;
}

/* ---------- 第一层：导航层 ---------- */
.nav-row {
  display: flex;
  align-items: center;
  height: 56px;
  flex: 0 0 auto;
  padding-left: 15px;
  padding-right: 15px;
  background: var(--prev-bg-topBar);
  box-shadow: 0 6px 22px rgba(15, 44, 107, 0.3);
  position: relative;
  z-index: 2;
}
/* 底边一道青色流光（方案 B 的科技感来源之一） */
.nav-row::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(34, 211, 238, 0.85), rgba(255, 255, 255, 0.3), transparent);
}
/* 横向菜单撑开剩余空间，把用户区顶到最右 */
.nav-row ::v-deep .el-menu-horizontal-warp {
  flex: 1 1 auto;
  min-width: 0;
}

/* ---------- 第二层：页面层 ---------- */
.page-row {
  display: flex;
  align-items: center;
  height: 48px;
  flex: 0 0 auto;
  padding-left: 18px;
  padding-right: 18px;
  background: #fff;
  border-bottom: 1px solid var(--prev-border-color-light);
}
.page-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
}
.pa-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 11px;
  border-radius: var(--prev-radius-ctl);
  font-size: 13px;
  color: var(--prev-color-text-primary);
  cursor: pointer;
  transition: all 0.18s ease;
  user-select: none;
}
.pa-btn i {
  font-size: 14px;
}
.pa-btn:hover {
  color: var(--prev-color-primary);
  background: rgba(31, 94, 255, 0.07);
  box-shadow: inset 0 0 0 1px rgba(31, 94, 255, 0.16);
}
</style>
