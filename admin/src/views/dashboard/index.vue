<template>
  <div>
    <!--问候条（方案 B 新增）-->
    <hello @refresh="onRefreshAll" />
    <!--头部：KPI 四卡-->
    <base-info ref="baseInfo" v-if="checkPermi(['admin:statistics:home:index'])" />
    <!--小方块：快捷入口 + 经营数据-->
    <grid-menu class="mb14" />
    <!-- 用户概览 -->
    <user-overview></user-overview>
    <!--订单统计-->
    <visit-chart ref="visitChart" />
    <!--用户-->
    <user-chart ref="userChart" class="mb20" v-if="checkPermi(['admin:statistics:home:chart:user'])" />
  </div>
</template>

<script>
import hello from './components/hello';
import baseInfo from './components/baseInfo';
import gridMenu from './components/gridMenu';
import visitChart from './components/visitChart';
import userChart from './components/userChart';
import userOverview from './components/userOverview';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'Dashboard',
  components: { hello, baseInfo, gridMenu, visitChart, userChart, userOverview },
  methods: {
    checkPermi,
    // 问候条的「刷新数据」：依次刷新各图表组件，不整页 reload（避免丢失路由状态）。
    // 各组件刷新方法名不一致，逐个兜底尝试，取不到就跳过，不抛错。
    onRefreshAll() {
      ['baseInfo', 'visitChart', 'userChart'].forEach((key) => {
        const ref = this.$refs[key];
        if (!ref) return;
        const fn = ref.statisticsOrder || ref.getList || ref.init || ref.getData;
        if (typeof fn === 'function') fn.call(ref);
      });
    },
  },
};
</script>
