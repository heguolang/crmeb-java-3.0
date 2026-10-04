<!--
  首页 KPI 卡 · 方案 B「极光科技」（2026-10-04 重构）

  改造前：四张 el-card 平铺，只有「标题 + 数字 + 昨日数据」，没有层级、没有涨跌。
  改造后：渐变图标块 + 顶部渐变描边 + 28px 等宽数字 + 环比涨跌（涨红跌绿·中国习惯）。

  数据源不变（仍走 viewModelApi），只是把四个字段收进数组统一渲染。
-->
<template>
  <div class="divBox" style="padding-bottom: 0">
    <div class="kpi-row">
      <div v-for="(c, i) in cards" :key="c.key" class="kpi-card" :class="'kpi-' + (i + 1)">
        <div class="kpi-top">
          <div class="kpi-icon">
            <svg v-if="c.icon === 'trend'" viewBox="0 0 24 24">
              <path d="M3 17l6-6 4 4 8-8" />
              <path d="M17 7h4v4" />
            </svg>
            <svg v-else-if="c.icon === 'eye'" viewBox="0 0 24 24">
              <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            <svg v-else-if="c.icon === 'order'" viewBox="0 0 24 24">
              <path d="M6 3h9l5 5v13H6z" />
              <path d="M15 3v5h5" />
              <path d="M9 13h7" />
              <path d="M9 17h5" />
            </svg>
            <svg v-else viewBox="0 0 24 24">
              <circle cx="10" cy="8" r="3.6" />
              <path d="M3.5 20c0-3.4 3-6 6.5-6 1.2 0 2.3.2 3.3.7" />
              <path d="M18 14v6" />
              <path d="M15 17h6" />
            </svg>
          </div>
          <span class="kpi-label">{{ c.label }}</span>
          <span class="kpi-tag">今日</span>
        </div>
        <div class="kpi-value">{{ formatNum(c.value) }}</div>
        <div class="kpi-cmp">
          <span class="cmp-l">昨日 {{ formatNum(c.yesterday) }}</span>
          <b :class="c.trend >= 0 ? 'up' : 'down'">
            {{ c.trend >= 0 ? '↑' : '↓' }}{{ Math.abs(c.trend) }}%
          </b>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { viewModelApi } from '@/api/dashboard';
export default {
  name: 'BaseInfo',
  data() {
    return {
      viewData: {},
    };
  },
  computed: {
    cards() {
      const d = this.viewData || {};
      const raw = [
        { key: 'sales', label: '销售额', value: d.sales, yesterday: d.yesterdaySales, icon: 'trend' },
        { key: 'pageviews', label: '用户访问量', value: d.pageviews, yesterday: d.yesterdayPageviews, icon: 'eye' },
        { key: 'orderNum', label: '订单量', value: d.orderNum, yesterday: d.yesterdayOrderNum, icon: 'order' },
        { key: 'newUserNum', label: '新增用户', value: d.newUserNum, yesterday: d.yesterdayNewUserNum, icon: 'user' },
      ];
      return raw.map((item) => Object.assign({}, item, { trend: this.calcTrend(item.value, item.yesterday) }));
    },
  },
  methods: {
    statisticsOrder() {
      viewModelApi().then((res) => {
        this.viewData = res || {};
      });
    },
    // 环比百分比：保留一位小数；昨日为 0 时按 0 处理（避免出现 Infinity）
    calcTrend(cur, pre) {
      const c = Number(cur) || 0;
      const p = Number(pre) || 0;
      if (!p) return 0;
      return Math.round(((c - p) / p) * 1000) / 10;
    },
    // 千分位；非数字（接口可能返回 '--' 之类）原样返回，不做破坏性处理
    formatNum(v) {
      if (v === null || v === undefined || v === '') return '-';
      const n = Number(v);
      if (isNaN(n)) return v;
      return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 });
    },
  },
  mounted() {
    this.statisticsOrder();
  },
};
</script>

<style scoped lang="scss">
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
@media (max-width: 1200px) {
  .kpi-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.kpi-card {
  position: relative;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--prev-border-color-light);
  border-radius: var(--prev-radius-card);
  box-shadow: var(--prev-shadow-card);
  padding: 14px 16px;
  transition: box-shadow 0.2s ease;
}
.kpi-card:hover {
  box-shadow: var(--prev-shadow-card-hover);
}
/* 顶部渐变描边（方案 B 标识） */
.kpi-card::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 2px;
  opacity: 0.9;
}
.kpi-1::before {
  background: linear-gradient(90deg, #1f5eff, #4f8cff);
}
.kpi-2::before {
  background: linear-gradient(90deg, #22d3ee, #4f8cff);
}
.kpi-3::before {
  background: linear-gradient(90deg, #6fa8ff, #bfd4ff);
}
.kpi-4::before {
  background: linear-gradient(90deg, #12b76a, #7fe0b4);
}

.kpi-top {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 9px;
}
.kpi-icon {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  background: var(--prev-color-primary-gradient);
  box-shadow: 0 3px 9px rgba(31, 94, 255, 0.32);
}
.kpi-2 .kpi-icon {
  background: linear-gradient(135deg, #22d3ee, #4f8cff);
  box-shadow: 0 3px 9px rgba(34, 211, 238, 0.34);
}
.kpi-3 .kpi-icon {
  background: linear-gradient(135deg, #6fa8ff, #bfd4ff);
  box-shadow: 0 3px 9px rgba(111, 168, 255, 0.3);
}
.kpi-4 .kpi-icon {
  background: linear-gradient(135deg, #12b76a, #7fe0b4);
  box-shadow: 0 3px 9px rgba(18, 183, 106, 0.3);
}
.kpi-icon svg {
  width: 17px;
  height: 17px;
  stroke: #fff;
  fill: none;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
.kpi-label {
  font-size: 13px;
  color: var(--prev-color-text-secondary);
}
.kpi-tag {
  margin-left: auto;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: var(--prev-radius-pill);
  background: var(--prev-color-primary-light-8);
  color: var(--prev-color-primary);
  font-weight: 500;
}
.kpi-value {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.4px;
  line-height: 1.2;
  color: var(--prev-color-text-title);
  font-variant-numeric: tabular-nums;
}
.kpi-cmp {
  margin-top: 7px;
  padding-top: 8px;
  border-top: 1px dashed var(--prev-border-color-lighter);
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--prev-color-text-secondary);
  font-variant-numeric: tabular-nums;
}
.cmp-l {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 涨红跌绿（中国市场习惯） */
.kpi-cmp b {
  margin-left: auto;
  font-weight: 600;
}
.kpi-cmp b.up {
  color: var(--prev-color-up);
}
.kpi-cmp b.down {
  color: var(--prev-color-down);
}
</style>
