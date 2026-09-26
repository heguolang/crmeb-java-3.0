<template>
  <div class="layout-navbars-container">
    <BreadcrumbIndex />
  </div>
</template>

<script>
import BreadcrumbIndex from '@/layout/navBars/breadcrumb/index.vue';
export default {
  name: 'layoutNavBars',
  components: { BreadcrumbIndex },
  data() {
    return {};
  },
};
</script>

<style scoped lang="scss">
.layout-navbars-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}
</style>
