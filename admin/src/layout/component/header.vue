<template>
  <el-header class="layout-header" :height="setHeaderHeight">
    <NavBarsIndex />
  </el-header>
</template>

<script>
import NavBarsIndex from '@/layout/navBars/index.vue';
export default {
  name: 'layoutHeader',
  components: { NavBarsIndex },
  data() {
    return {};
  },
  computed: {
    // 设置顶部 header 的具体高度
    // 2026-10-04 方案 B：顶栏拆为双层 —— 导航层 56px + 页面层 48px = 104px。
    // 这里必须同步，否则第二层页面层会被裁切。
    // 顶部标签栏（tagsView）已删除，固定高度
    setHeaderHeight() {
      return '104px';
    },
  },
};
</script>
