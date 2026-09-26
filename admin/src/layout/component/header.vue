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
    // 顶栏菜单行高已提高到 56px，这里同步调整，否则菜单底部会被裁切
    // 顶部标签栏（tagsView）已删除，固定高度
    setHeaderHeight() {
      return '56px';
    },
  },
};
</script>
