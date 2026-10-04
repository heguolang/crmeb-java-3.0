<!--
  首页 KPI 卡 · 2026-10-04 第二次重构（清爽风 8 卡）

  改造前：4 张卡（销售额/访问量/订单量/新增用户），全是「今昨」对比，没有任何累计业绩。
  改造后：上排核心业绩（可切周期）+ 下排经营质量（累计口径），共 8 张卡。

  数据源仍是 viewModelApi（/admin/statistics/home/index）——
  后端 HomeRateResponse 本次补了 18 个业绩字段，原有 8 个字段后端一行未改，这里也一个没删。

  设计口径（对齐设计稿「控制台-清爽风五套配色」）：
    · 浅底 + 1px 极细边框 + 几乎无投影，科技感靠秩序感而非发光
    · 数字等宽 + tabular-nums，位数变化不跳动
    · 涨红跌绿（中国习惯），语义色不随主题变
    · 金额自动换算「万 / 亿」，避免长数字撑破卡片

  ⚠ 诚实性约束：每张卡的字段都必须后端真的返回了，不做「看起来合理」的假造。
    后端没给月度退款额，所以月周期里就不出现「本月实付净额」——
    实付净额只在累计周期出现（netGmv 是累计口径）。
-->
<template>
  <div class="divBox kpi-box">
    <!-- 区块头 + 周期切换（只影响上排） -->
    <div class="kpi-head">
      <div class="kpi-head-l">
        <span class="kpi-sec-title">核心业绩</span>
        <span class="kpi-sec-sub">仅统计商城已付款订单（paid=1）</span>
      </div>
      <div class="kpi-tabs">
        <button
          v-for="p in periods"
          :key="p.key"
          type="button"
          class="kpi-tab"
          :class="{ on: p.key === period }"
          @click="period = p.key"
        >
          {{ p.label }}
        </button>
      </div>
    </div>

    <!-- 上排：核心业绩 4 张 -->
    <div class="kpi-row">
      <div v-for="c in coreCards" :key="c.key" class="kpi-card">
        <span class="kpi-accent" :class="'ac-' + c.tone"></span>
        <div class="kpi-top">
          <span class="kpi-label">{{ c.label }}</span>
          <span class="kpi-tag" :class="'tg-' + c.tone">{{ c.tag }}</span>
        </div>
        <div class="kpi-value" :title="c.raw">
          <em v-if="c.cur" class="kpi-cur">{{ c.cur }}</em>{{ c.value
          }}<i v-if="c.unit" class="kpi-unit">{{ c.unit }}</i>
        </div>
        <div class="kpi-cmp">
          <span class="cmp-l">{{ c.foot }}</span>
          <b v-if="c.trend !== null" :class="c.trend >= 0 ? 'up' : 'down'">
            {{ c.trend >= 0 ? '↑' : '↓' }}{{ Math.abs(c.trend) }}%
          </b>
          <span v-else class="cmp-na">无环比基数</span>
        </div>
      </div>
    </div>

    <!-- 下排：经营质量 4 张（累计口径，与上方周期切换无关） -->
    <div class="kpi-row">
      <div v-for="c in qualityCards" :key="c.key" class="kpi-card kpi-flat">
        <div class="kpi-top">
          <span class="kpi-label">{{ c.label }}</span>
        </div>
        <div class="kpi-value" :title="c.raw">
          <em v-if="c.cur" class="kpi-cur">{{ c.cur }}</em>{{ c.value
          }}<i v-if="c.unit" class="kpi-unit">{{ c.unit }}</i>
        </div>
        <div class="kpi-cmp">
          <span class="cmp-l">{{ c.foot }}</span>
          <b v-if="c.good !== null" :class="c.good ? 'up' : 'down'">{{ c.flag }}</b>
          <span v-else class="cmp-na">{{ c.flag }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { viewModelApi } from '@/api/dashboard';

const WAN = 10000;
const YI = 100000000;

/* ---------------- 展示层工具函数（模块级，避免每次渲染重建） ---------------- */

/** 安全转数字，null/''/非数字一律给 0 */
function num(v) {
  if (v === null || v === undefined || v === '') return 0;
  const n = Number(v);
  return isNaN(n) ? 0 : n;
}

/**
 * 金额自动换算单位：< 1 万保留元；< 1 亿显示万；否则显示亿。
 * 卡片宽度有限，1234567.89 直接铺上去会撑破，必须换算。
 * 返回 { v: 显示值, u: 单位, raw: 原值 }
 */
function money(v) {
  const n = num(v);
  const sign = n < 0 ? '-' : '';
  const a = Math.abs(n);
  if (a >= YI) return { v: sign + (a / YI).toFixed(2), u: '亿元', raw: String(n) };
  if (a >= WAN) return { v: sign + (a / WAN).toFixed(2), u: '万元', raw: String(n) };
  return { v: sign + a.toFixed(2), u: '元', raw: String(n) };
}

/** 整数带千分位 */
function int(v) {
  return num(v).toLocaleString('zh-CN');
}

/** 占比文案，分母为 0 时返回 0% */
function pct(part, all) {
  if (!all) return '0%';
  return ((part / all) * 100).toFixed(2) + '%';
}

/**
 * 后端算好的百分比（null / 非数字 → null）。
 * ⚠ 不能用 num()：num(null) 会返回 0，那样「上月为 0 无法算环比」就会被显示成 "0%"，
 *    看起来像业绩持平，实际是没数据 —— 比不显示更误导。
 */
function pctNum(v) {
  if (v === null || v === undefined || v === '') return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

/**
 * 组装一张卡。
 * @param moneyV 传 true 表示 value 用 money() 换算，否则按整数展示
 * @param ratio  环比百分比（已乘 100）；null / undefined / 非数字 → 显示「无环比基数」
 */
function card(o) {
  const m = o.moneyV ? money(o.value) : null;
  return {
    key: o.key,
    label: o.label,
    tone: o.tone,
    tag: o.tag,
    cur: o.moneyV ? '¥' : '',
    value: m ? m.v : int(o.value),
    unit: m ? m.u : o.unit || '',
    raw: m ? m.raw : String(num(o.value)),
    foot: o.foot,
    trend: typeof o.ratio === 'number' && !isNaN(o.ratio) ? Math.round(o.ratio * 10) / 10 : null,
    footLabel: o.footLabel,
  };
}

export default {
  name: 'BaseInfo',
  data() {
    return {
      viewData: {},
      // period 只影响上排四张卡的取值与环比口径；下排经营质量固定累计口径。
      //   today = 今日 vs 昨日（沿用老接口的今昨口径，后端原有字段）
      //   month = 本月 vs 上月同期（后端 monthGmvRatio / monthOrderRatio）
      //   year  = 本年 vs 累计参照（后端只给了本年 GMV，无去年同期，环比一律 null）
      //   total = 累计（无环比对象）
      period: 'month',
      periods: [
        { key: 'month', label: '本月' },
        { key: 'today', label: '今日' },
        { key: 'year', label: '本年' },
        { key: 'total', label: '累计' },
      ],
    };
  },
  computed: {
    /** 上排 4 张：按当前周期取对应字段，每个周期只列后端真的返回了的值 */
    coreCards() {
      const d = this.viewData || {};
      const g = (k) => num(d[k]);
      switch (this.period) {
        case 'today':
          return [
            card({ key: 'tSales', label: '今日销售额', tone: 'pri', tag: '今日', moneyV: true, value: d.sales, foot: '昨日 ¥' + money(d.yesterdaySales).v + ' ' + money(d.yesterdaySales).u, ratio: this.growth(d.sales, d.yesterdaySales) }),
            card({ key: 'tOrder', label: '今日订单量', tone: 'cyan', tag: '今日', value: d.orderNum, unit: '笔', foot: '昨日 ' + int(d.yesterdayOrderNum) + ' 笔', ratio: this.growth(d.orderNum, d.yesterdayOrderNum) }),
            card({ key: 'tUser', label: '今日新增用户', tone: 'violet', tag: '今日', value: d.newUserNum, unit: '人', foot: '昨日 ' + int(d.yesterdayNewUserNum) + ' 人', ratio: this.growth(d.newUserNum, d.yesterdayNewUserNum) }),
            card({ key: 'tPv', label: '今日访问量', tone: 'gray', tag: '今日', value: d.pageviews, unit: '次', foot: '昨日 ' + int(d.yesterdayPageviews) + ' 次', ratio: this.growth(d.pageviews, d.yesterdayPageviews) }),
          ];
        case 'year':
          return [
            card({ key: 'yGmv', label: '本年 GMV', tone: 'pri', tag: '本年', moneyV: true, value: d.yearGmv, foot: '1 月 1 日至今累计', ratio: null }),
            card({ key: 'yAvg', label: '本年客单价', tone: 'cyan', tag: '本年', moneyV: true, value: this.div(d.yearGmv, g('totalOrderNum')), foot: '本年 GMV / 累计订单量', ratio: null }),
            card({ key: 'yPaid', label: '累计支付用户', tone: 'violet', tag: '累计', value: d.paidUserCount, unit: '人', foot: '已付款订单去重 uid', ratio: null }),
            card({ key: 'yOrder', label: '累计订单量', tone: 'gray', tag: '累计', value: d.totalOrderNum, unit: '笔', foot: '已付款订单总数', ratio: null }),
          ];
        case 'total':
          return [
            card({ key: 'cGmv', label: '累计 GMV', tone: 'pri', tag: '累计', moneyV: true, value: d.totalGmv, foot: '开业至今已付款订单实付金额', ratio: null }),
            card({ key: 'cNet', label: '累计实付净额', tone: 'cyan', tag: '累计', moneyV: true, value: d.netGmv, foot: 'GMV 减退款金额 ' + money(d.refundAmount).v + money(d.refundAmount).u, ratio: null }),
            card({ key: 'cOrder', label: '累计订单量', tone: 'violet', tag: '累计', value: d.totalOrderNum, unit: '笔', foot: '已付款订单总数', ratio: null }),
            card({ key: 'cAvg', label: '累计客单价', tone: 'gray', tag: '累计', moneyV: true, value: d.avgOrderPrice, foot: '累计 GMV / 累计订单量', ratio: null }),
          ];
        case 'month':
        default:
          return [
            card({ key: 'mGmv', label: '本月 GMV', tone: 'pri', tag: '本月', moneyV: true, value: d.monthGmv, foot: '上月同期 ¥' + money(d.lastMonthGmv).v + money(d.lastMonthGmv).u, ratio: pctNum(d.monthGmvRatio) }),
            card({ key: 'mOrder', label: '本月订单量', tone: 'cyan', tag: '本月', value: d.monthOrderNum, unit: '笔', foot: '上月同期 ' + int(d.lastMonthOrderNum) + ' 笔', ratio: pctNum(d.monthOrderRatio) }),
            card({ key: 'mAvg', label: '本月客单价', tone: 'violet', tag: '本月', moneyV: true, value: this.div(d.monthGmv, g('monthOrderNum')), foot: '本月 GMV / 本月订单量', ratio: null }),
            card({ key: 'mYear', label: '本年 GMV', tone: 'gray', tag: '本年', moneyV: true, value: d.yearGmv, foot: '1 月 1 日至今累计', ratio: null }),
          ];
      }
    },
    /** 下排 4 张：经营质量。后端只给累计口径，所以不参与周期切换。 */
    qualityCards() {
      const d = this.viewData || {};
      const total = num(d.totalOrderNum);
      const gmv = num(d.totalGmv);
      const alter = num(d.alterPriceCount);
      const rate = num(d.refundRate);
      const mDis = money(d.discountAmount);
      const mRef = money(d.refundAmount);
      const mNet = money(d.netGmv);
      return [
        {
          key: 'discount',
          label: '优惠让利',
          cur: '¥',
          value: mDis.v,
          unit: mDis.u,
          raw: mDis.raw,
          foot: gmv > 0 ? '占 GMV ' + pct(num(d.discountAmount), gmv) : '暂无已付款订单',
          flag: '累计让利',
          good: null,
        },
        {
          key: 'refundRate',
          label: '退款率',
          value: rate.toFixed(2),
          unit: '%',
          raw: String(rate),
          foot: int(d.refundCount) + ' 笔退款 / ' + total + ' 笔订单',
          flag: total > 0 ? (rate <= 5 ? '正常' : '偏高') : '暂无订单',
          good: total > 0 ? rate <= 5 : null,
        },
        {
          key: 'alter',
          label: '改价单',
          value: alter,
          unit: '笔',
          raw: String(alter),
          foot: total > 0 ? '占订单 ' + pct(alter, total) + '，需人工复核' : '人工改价订单',
          flag: total > 0 ? (alter / total <= 0.02 ? '正常' : '偏高') : '暂无订单',
          good: total > 0 ? alter / total <= 0.02 : null,
        },
        {
          key: 'refundAmt',
          label: '退款金额',
          cur: '¥',
          value: mRef.v,
          unit: mRef.u,
          raw: mRef.raw,
          foot: '实付净额 ¥' + mNet.v + mNet.u,
          flag: '已从 GMV 扣除',
          good: null,
        },
      ];
    },
  },
  methods: {
    statisticsOrder() {
      viewModelApi().then((res) => {
        this.viewData = res || {};
      });
    },
    /** 环比百分比：保留一位小数；基数缺失或为 0 时返回 null（前端显示「无环比基数」，不显示误导性的 0%） */
    growth(cur, pre) {
      const c = num(cur);
      const p = num(pre);
      if (!p) return null;
      return ((c - p) / p) * 100;
    },
    /** 除法防零：分母为 0 给 0 */
    div(a, b) {
      const x = num(a);
      const y = num(b);
      return y === 0 ? 0 : x / y;
    },
  },
  mounted() {
    this.statisticsOrder();
  },
};
</script>

<style scoped lang="scss">
.kpi-box {
  padding-bottom: 0;
}

/* ---------- 区块头 ---------- */
.kpi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}
.kpi-head-l {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.kpi-sec-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--prev-color-text-title);
  letter-spacing: 0.2px;
}
.kpi-sec-sub {
  font-size: 12px;
  color: var(--prev-color-text-secondary);
}
.kpi-tabs {
  display: flex;
  gap: 2px;
  padding: 2px;
  background: #f4f6fa;
  border: 1px solid var(--prev-border-color-lighter);
  border-radius: 8px;
}
.kpi-tab {
  border: 0;
  background: transparent;
  font-size: 12px;
  color: var(--prev-color-text-secondary);
  padding: 3px 12px;
  border-radius: 6px;
  cursor: pointer;
  line-height: 1.6;
  font-family: inherit;
  transition: all 0.16s ease;
}
.kpi-tab:hover {
  color: var(--prev-color-primary);
}
.kpi-tab.on {
  background: #fff;
  color: var(--prev-color-primary);
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.06);
}

/* ---------- 卡片栅格 ---------- */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
@media (max-width: 1280px) {
  .kpi-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 700px) {
  .kpi-row {
    grid-template-columns: minmax(0, 1fr);
  }
}

/* ---------- 单卡 ---------- */
.kpi-card {
  position: relative;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--prev-border-color-light);
  border-radius: 10px;
  /* 清爽风：几乎不投影，只留一层几乎看不见的浮起感 */
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  padding: 14px 16px 13px;
  transition: box-shadow 0.18s ease, border-color 0.18s ease;
}
.kpi-card:hover {
  border-color: #d6dce8;
  box-shadow: 0 2px 8px rgba(16, 24, 40, 0.07);
}
/* 左侧 2px 竖条色码，比顶部渐变描边克制 */
.kpi-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
}
.ac-pri {
  background: var(--prev-color-primary);
}
.ac-cyan {
  background: #0e7490;
}
.ac-violet {
  background: #5b5bd6;
}
.ac-gray {
  background: #94a3b8;
}
/* 下排质量卡不加竖条，视觉重心留给上排业绩 */
.kpi-flat {
  padding-left: 16px;
}

.kpi-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  min-height: 18px;
}
.kpi-label {
  font-size: 13px;
  color: var(--prev-color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.kpi-tag {
  margin-left: auto;
  font-size: 11px;
  line-height: 1.5;
  padding: 1px 7px;
  border-radius: 4px;
  font-weight: 500;
  flex: 0 0 auto;
}
.tg-pri {
  background: var(--prev-color-primary-light-8);
  color: var(--prev-color-primary);
}
.tg-cyan {
  background: #ecfeff;
  color: #0e7490;
}
.tg-violet {
  background: #eef2ff;
  color: #4f46e5;
}
.tg-gray {
  background: #f1f5f9;
  color: #64748b;
}

/* ---------- 数值：等宽 + tabular-nums，位数变化不跳动 ---------- */
.kpi-value {
  font-size: 25px;
  font-weight: 650;
  letter-spacing: -0.3px;
  line-height: 1.15;
  color: var(--prev-color-text-title);
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.kpi-cur {
  font-size: 15px;
  font-style: normal;
  font-weight: 500;
  color: var(--prev-color-text-secondary);
  margin-right: 1px;
}
.kpi-unit {
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  color: var(--prev-color-text-secondary);
  margin-left: 4px;
  font-family: inherit;
}

/* ---------- 脚注 ---------- */
.kpi-cmp {
  margin-top: 9px;
  padding-top: 8px;
  border-top: 1px solid #f1f4f8;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--prev-color-text-secondary);
  font-variant-numeric: tabular-nums;
  min-height: 18px;
}
.cmp-l {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cmp-na {
  margin-left: auto;
  font-size: 11px;
  color: #b6bfcd;
  flex: 0 0 auto;
  padding-left: 8px;
}
.kpi-cmp b {
  margin-left: auto;
  font-weight: 600;
  flex: 0 0 auto;
  padding-left: 8px;
}
/* 涨红跌绿（中国市场习惯），语义色不随主题变 */
.kpi-cmp b.up {
  color: var(--prev-color-up);
}
.kpi-cmp b.down {
  color: var(--prev-color-down);
}
</style>
