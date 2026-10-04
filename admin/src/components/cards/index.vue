<!--
  通用统计卡（多列表页表头在用）· 方案 B「极光科技」（2026-10-04 视觉改造）

  改造点（API 与用法零变更，仍收 cardLists: [{name, count, color, class, icon}]）：
  1. 卡片：直角白块 → 12px 圆角 + 1px 描边 + 双层投影 + 悬停上浮
  2. 图标块：50px 直角 → 48px 圆角 12px + 同色描边环
  3. 数字：28px/600 → 26px/700 等宽数字（tabular-nums）
  4. 调色板并轨到全局令牌色（蓝/紫/橙/绿/青/粉）
  另：删除从未被引用的 .one1~.six1 死样式（原先与 .one~.six 重复）。
-->
<template>
  <el-row align="middle" :gutter="14" class="ivu-mt">
    <el-col
      :xl="6"
      :lg="6"
      :md="12"
      :sm="12"
      :xs="24"
      class="ivu-mb mb14"
      v-for="(item, index) in cardLists"
      :key="index"
    >
      <div class="card_box">
        <div class="card_box_cir" :class="item.class">
          <span class="iconfont" :class="item.icon" :style="{ color: item.color }" v-if="item.icon"></span>
          <i class="el-icon-edit" style="color: #fff" v-else></i>
        </div>
        <div class="card_box_txt">
          <span class="sp2" v-text="item.name"></span>
          <span class="sp1" v-text="item.count || 0"></span>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: 'index',
  props: {
    cardLists: Array,
  },
};
</script>

<style scoped lang="scss">
.one {
  background: rgba(31, 94, 255, 0.1);
  box-shadow: inset 0 0 0 1px rgba(31, 94, 255, 0.16);
}
.two {
  background: rgba(124, 58, 237, 0.1);
  box-shadow: inset 0 0 0 1px rgba(124, 58, 237, 0.16);
}
.three {
  background: rgba(245, 158, 11, 0.12);
  box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.18);
}
.four {
  background: rgba(18, 183, 106, 0.1);
  box-shadow: inset 0 0 0 1px rgba(18, 183, 106, 0.16);
}
.five {
  background: rgba(34, 211, 238, 0.12);
  box-shadow: inset 0 0 0 1px rgba(34, 211, 238, 0.2);
}
.six {
  background: rgba(219, 39, 119, 0.1);
  box-shadow: inset 0 0 0 1px rgba(219, 39, 119, 0.16);
}
.card_box {
  width: 100%;
  height: 110px;
  display: flex;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
  border: 1px solid var(--prev-border-color-light, #edf0f5);
  border-radius: var(--prev-radius-card, 12px);
  background: #fff;
  box-shadow: var(--prev-shadow-card, 0 1px 2px rgba(16, 24, 40, 0.04), 0 4px 14px rgba(16, 24, 40, 0.05));
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  &:hover {
    box-shadow: var(
      --prev-shadow-card-hover,
      0 4px 10px rgba(16, 24, 40, 0.06),
      0 12px 28px rgba(16, 24, 40, 0.09)
    );
    transform: translateY(-1px);
  }
  .card_box_cir {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    overflow: hidden;
    margin-right: 18px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 0 0 auto;
  }
  .card_box_txt {
    min-width: 0;
    .sp1 {
      display: block;
      color: var(--prev-color-text-title, #0f172a);
      font-size: 26px;
      line-height: 1.25;
      padding-top: 4px;
      font-weight: 700;
      letter-spacing: -0.4px;
      font-variant-numeric: tabular-nums;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .sp2 {
      display: block;
      color: var(--prev-color-text-secondary, #94a3b8);
      font-size: 13px;
      font-weight: 400;
    }
  }
}
.iconfont {
  font-size: 23px;
}
</style>
