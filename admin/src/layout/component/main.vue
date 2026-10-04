<template>
  <el-main class="layout-main">
    <el-scrollbar
      class="layout-scrollbar"
      ref="layoutScrollbarRef"
      :style="{ minHeight: `calc(100vh - ${headerHeight})` }"
    >
      <LayoutParentView />
      <Footers v-if="getThemeConfig.isFooter && !isFullScreen" />
    </el-scrollbar>
  </el-main>
</template>
<script>
// 顶栏总高度：导航层 56 + 页面层 48 = 104（方案 B 双层顶栏）
// 与 layout/component/header.vue 的 setHeaderHeight() 必须保持一致
const HEADER_TOTAL_HEIGHT = 104;

import LayoutParentView from '@/layout/routerView/parent.vue';
import Footers from '@/layout/footer/index.vue';
import Links from '@/layout/routerView/link.vue';
import Iframes from '@/layout/routerView/iframes.vue';
export default {
  name: 'layoutMain',
  components: { LayoutParentView, Footers, Links, Iframes },
  data() {
    return {
      headerHeight: '',
      currentRouteMeta: {},
      isShowLink: false,
    };
  },
  computed: {
    // 获取布局配置信息
    getThemeConfig() {
      return this.$store.state.themeConfig.themeConfig;
    },
    isFullScreen() {
      return this.$route.meta.fullScreen;
    },
  },
  mounted() {
    this.initHeaderHeight();
    this.initCurrentRouteMeta(this.$route.meta);
  },
  methods: {
    // 初始化当前路由 meta 信息
    initCurrentRouteMeta(meta) {
      this.isShowLink = false;
      this.currentRouteMeta = meta;
      setTimeout(() => {
        this.isShowLink = true;
      }, 100);
    },
    // 设置 main 的高度
    // ⚠️ 2026-10-04：这里必须是「顶栏总高度」，否则每个页面底部都会多出一截虚假滚动。
    // 顶栏已拆双层（导航层 56 + 页面层 48 = 104），与 layout/component/header.vue 的
    // setHeaderHeight() 必须联动修改。原来写死 50px 是单层顶栏时代的遗留值。
    initHeaderHeight() {
      if (this.isFullScreen) return (this.headerHeight = '0px');
      // 顶栏双层 104px；顶部标签栏（tagsView）已删除
      return (this.headerHeight = `${HEADER_TOTAL_HEIGHT}px`);
    },
    // 子组件触发更新
    onGetCurrentRouteMeta() {
      this.initCurrentRouteMeta(this.$route.meta);
    },
  },
  watch: {
    // 监听 vuex 数据变化
    '$store.state.themeConfig.themeConfig': {
      handler(val) {
        if (this.isFullScreen) {
          this.headerHeight = '0px';
          return;
        }
        // 同 initHeaderHeight：顶栏双层 104px，不再用旧的 50px
        this.headerHeight = `${HEADER_TOTAL_HEIGHT}px`;
        if (val.isFixedHeaderChange !== val.isFixedHeader) {
          if (!this.$refs.layoutScrollbarRef) return false;
          this.$refs.layoutScrollbarRef.update();
        }
      },
      deep: true,
    },
    // 监听路由的变化
    $route: {
      handler(to) {
        this.initCurrentRouteMeta(to.meta);
        this.initHeaderHeight();
        this.$refs.layoutScrollbarRef.wrap.scrollTop = 0;
      },
      deep: true,
    },
  },
};
</script>
