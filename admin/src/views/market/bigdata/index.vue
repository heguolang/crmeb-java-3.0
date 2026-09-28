<template>
  <div class="bigdata-viewport">
    <div ref="stage" class="bigdata-stage" :style="stageStyle">
      <!-- 背景：紫色星云 + 光斑 + 网格 -->
      <div class="bd-bg">
        <i v-for="n in 14" :key="'bk' + n" class="bd-bokeh" :class="'bd-bokeh-' + n"></i>
        <div class="bd-bg-grid"></div>
      </div>

      <!-- 顶栏 -->
      <header class="bd-header">
        <div class="bd-hd-left">
          <div class="bd-hd-date">{{ dateText }}</div>
          <div class="bd-hd-clock">{{ clockText }}</div>
        </div>
        <div class="bd-hd-title">
          <span class="bd-hd-wing"></span>
          <h1>市 场 大 数 据 中 心</h1>
          <span class="bd-hd-wing flip"></span>
        </div>
        <div class="bd-hd-right">
          <span class="bd-hd-chip"><i class="dot"></i>数据实时同步</span>
          <a class="bd-hd-fs" @click="toggleFull"><i class="el-icon-full-screen"></i> 全屏</a>
        </div>
      </header>

      <!-- 主体两栏 -->
      <section class="bd-body">
        <!-- 左栏 2/3 -->
        <div class="bd-left">
          <!-- 今日交易额 -->
          <div class="bd-panel bd-panel-trade">
            <div class="bd-corner tl"></div><div class="bd-corner tr"></div>
            <div class="bd-corner bl"></div><div class="bd-corner br"></div>
            <div class="bd-trade-main">
              <div class="bd-trade-label">
                今日交易额
                <span class="bd-trade-time">{{ dateText }} {{ clockText }}</span>
              </div>
              <div class="bd-trade-num">¥ {{ todaySalesText }}</div>
            </div>
            <div class="bd-trade-sub">
              <div class="bd-trade-item">
                <div class="bd-ti-label">昨日交易额</div>
                <div class="bd-ti-num">{{ yesterdaySalesText }}</div>
              </div>
              <div class="bd-trade-item">
                <div class="bd-ti-label">昨日交易笔数</div>
                <div class="bd-ti-num">{{ yesterdayOrderNum }}</div>
              </div>
            </div>
          </div>

          <!-- 运营健康度（三仪表盘） -->
          <div class="bd-panel bd-panel-gauge">
            <div class="bd-corner tl"></div><div class="bd-corner tr"></div>
            <div class="bd-corner bl"></div><div class="bd-corner br"></div>
            <div class="bd-gauge-head">
              <i class="bd-shield el-icon-circle-check"></i>
              <div>
                <div class="bd-gauge-title">本月经营健康度</div>
                <div class="bd-gauge-sub">全量订单 {{ statusAll }} 单 · 实时计算</div>
              </div>
            </div>
            <div class="bd-gauges">
              <div class="bd-gauge" v-for="g in gauges" :key="g.key">
                <div class="bd-gauge-name">{{ g.name }}</div>
                <div class="bd-gauge-wrap"><div class="bd-gauge-echart" :ref="'gauge_' + g.key"></div></div>
              </div>
            </div>
          </div>

          <!-- 实时分享数据（中国地图） -->
          <div class="bd-panel bd-panel-map">
            <div class="bd-corner tl"></div><div class="bd-corner tr"></div>
            <div class="bd-corner bl"></div><div class="bd-corner br"></div>
            <div class="bd-panel-cap">实时分享数据</div>
            <div class="bd-map-wrap">
              <div class="bd-map-echart" ref="mapChart"></div>
              <div class="bd-map-side">
                <div class="bd-ms-item" v-for="(s, i) in mapStats" :key="i">
                  <div class="bd-ms-num" :style="{ color: s.color }">{{ s.value }}</div>
                  <div class="bd-ms-label">{{ s.label }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右栏 1/3 -->
        <div class="bd-right">
          <!-- 最近下单 -->
          <div class="bd-panel bd-panel-order">
            <div class="bd-corner tl"></div><div class="bd-corner tr"></div>
            <div class="bd-corner bl"></div><div class="bd-corner br"></div>
            <div class="bd-panel-cap">最近下单</div>
            <div class="bd-order-list">
              <div class="bd-order" v-for="(o, i) in orders" :key="o.orderId + '_' + i">
                <div class="bd-order-user">
                  <span class="bd-avatar">{{ (o.nickname || '客')[0] }}</span>
                  <div class="bd-ou-info">
                    <div class="bd-ou-name">{{ maskName(o.nickname || o.realName) }}</div>
                    <div class="bd-ou-meta">{{ o.payTypeStr || '线上支付' }}</div>
                  </div>
                  <div class="bd-ou-time">{{ shortTime(o.createTime) }}</div>
                </div>
                <div class="bd-order-card">
                  <div class="bd-oc-row">
                    <span class="bd-oc-name">会员订单</span>
                    <span class="bd-oc-price">价格: <b>¥{{ o.payPrice }}</b></span>
                    <span class="bd-oc-num">数量: {{ o.totalNum || 1 }}</span>
                    <span class="bd-oc-postage">运费: {{ Number(o.payPostage || 0) > 0 ? '¥' + o.payPostage : '免运费' }}</span>
                  </div>
                  <div class="bd-oc-row sub">
                    <span class="bd-oc-orderid">订单编号: {{ o.orderId }}</span>
                    <span class="bd-oc-tag" :class="o.paid ? 'ok' : 'wait'">{{ o.paid ? '常规发货' : '待支付' }}</span>
                  </div>
                </div>
              </div>
              <div v-if="!orders.length" class="bd-empty">暂无订单数据</div>
            </div>
          </div>

          <!-- 优秀团队 -->
          <div class="bd-panel bd-panel-team">
            <div class="bd-corner tl"></div><div class="bd-corner tr"></div>
            <div class="bd-corner bl"></div><div class="bd-corner br"></div>
            <div class="bd-panel-cap">优秀团队</div>
            <div class="bd-team-list">
              <div class="bd-team" v-for="(t, i) in teams" :key="'t' + i">
                <img v-if="t.image" :src="t.image" class="bd-team-img" />
                <span v-else class="bd-team-img ph">{{ (t.productName || '团')[0] }}</span>
                <div class="bd-team-info">
                  <div class="bd-team-name">{{ t.productName || t.activityName || '拼购团' }}</div>
                  <div class="bd-team-meta">
                    <span>{{ t.joinNum || 0 }}人</span>
                    <span v-if="t.totalNum">成团 {{ t.totalNum }} 单</span>
                  </div>
                </div>
                <div class="bd-team-rank" :class="'rk' + (i + 1)">{{ i + 1 }}</div>
              </div>
              <div v-if="!teams.length" class="bd-empty">暂无团队数据</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts';
import screenfull from 'screenfull';
import chinaJson from 'echarts/map/json/china.json';
import { viewModelApi } from '@/api/dashboard';
import { tradeDataApi, userTotalData } from '@/api/statistic';
import { orderListApi, orderStatusNumApi } from '@/api/order';
import { pingouTeamListApi } from '@/api/pingou';

// 主要城市坐标（地图光点）
const MAP_POINTS = [
  { name: '贵阳', value: [106.63, 26.65] },
  { name: '广州', value: [113.23, 23.16] },
  { name: '深圳', value: [114.07, 22.62] },
  { name: '杭州', value: [120.19, 30.26] },
  { name: '上海', value: [121.47, 31.23] },
  { name: '北京', value: [116.4, 39.9] },
  { name: '成都', value: [104.06, 30.67] },
  { name: '重庆', value: [106.54, 29.59] },
  { name: '武汉', value: [114.3, 30.6] },
  { name: '西安', value: [108.94, 34.34] },
  { name: '长沙', value: [112.98, 28.19] },
  { name: '昆明', value: [102.71, 25.04] },
];

export default {
  name: 'MarketBigData',
  data() {
    return {
      stageStyle: '',
      now: new Date(),
      timer: null,
      refreshTimer: null,
      gaugeCharts: [],
      mapChartObj: null,
      // 交易
      todaySales: 0,
      yesterdaySales: 0,
      yesterdayOrderNum: 0,
      todayOrderNum: 0,
      monthOrderSales: 0,
      monthOrderNum: 0,
      // 用户
      userNum: 0,
      newUserNum: 0,
      agentCount: 0,
      // 订单状态
      statusAll: 0,
      unPaid: 0,
      notShipped: 0,
      refundCnt: 0,
      // 列表
      orders: [],
      teams: [],
      gauges: [
        { key: 'pay', name: '支付完成率', value: 0 },
        { key: 'ship', name: '发货完成率', value: 0 },
        { key: 'refund', name: '退款率', value: 0 },
      ],
      mapPoints: [],
    };
  },
  computed: {
    dateText() {
      const d = this.now;
      const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
      return `${d.getFullYear()}年${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日 星期${week}`;
    },
    clockText() {
      const d = this.now;
      return [d.getHours(), d.getMinutes(), d.getSeconds()].map((v) => String(v).padStart(2, '0')).join(':');
    },
    todaySalesText() {
      return this.formatMoney(this.todaySales);
    },
    yesterdaySalesText() {
      return this.formatMoney(this.yesterdaySales);
    },
    mapStats() {
      return [
        { label: '会员总数', value: this.userNum, color: '#c9b6ff' },
        { label: '今日新增', value: this.newUserNum, color: '#7ef0c8' },
        { label: '本月订单', value: this.monthOrderNum, color: '#ffd479' },
        { label: '本月销售额', value: '¥' + this.formatMoney(this.monthOrderSales), color: '#8fc7ff' },
      ];
    },
  },
  mounted() {
    this.fitStage();
    window.addEventListener('resize', this.fitStage);
    this.timer = setInterval(() => (this.now = new Date()), 1000);
    this.$nextTick(() => {
      this.initGauges();
      this.initMap();
      this.loadData();
    });
    this.refreshTimer = setInterval(this.loadData, 60 * 1000);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.fitStage);
    clearInterval(this.timer);
    clearInterval(this.refreshTimer);
    this.gaugeCharts.forEach((c) => c && c.dispose());
    if (this.mapChartObj) this.mapChartObj.dispose();
  },
  methods: {
    // 1920×1080 设计稿等比缩放，适配任意 LED 分辨率
    fitStage() {
      const el = this.$refs.stage;
      if (!el) return;
      const w = window.innerWidth;
      const h = window.innerHeight;
      const scale = Math.min(w / 1920, h / 1080);
      this.stageStyle = `width:1920px;height:1080px;transform:scale(${scale});`;
    },
    toggleFull() {
      if (screenfull.isEnabled) screenfull.toggle();
    },
    formatMoney(v) {
      return Number(v || 0)
        .toFixed(2)
        .replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    maskName(n) {
      const s = String(n || '');
      if (!s) return '匿名会员';
      if (s.length <= 2) return s;
      return s[0] + '***' + s[s.length - 1];
    },
    shortTime(t) {
      return String(t || '').slice(5, 16);
    },
    pct(a, b) {
      if (!b || b <= 0) return 0;
      return Math.max(0, Math.min(100, Math.round((a / b) * 1000) / 10));
    },
    loadData() {
      viewModelApi()
        .then((res) => {
          this.todaySales = res.sales || 0;
          this.yesterdaySales = res.yesterdaySales || 0;
          this.newUserNum = res.newUserNum || 0;
        })
        .catch(() => {});
      tradeDataApi()
        .then((res) => {
          this.todayOrderNum = res.todayOrderNum || 0;
          this.yesterdayOrderNum = res.yesterdayOrderNum || 0;
          this.monthOrderSales = res.monthOrderSales || 0;
          this.monthOrderNum = res.monthOrderNum || 0;
        })
        .catch(() => {});
      userTotalData()
        .then((res) => {
          this.userNum = res.userNum || 0;
        })
        .catch(() => {});
      orderStatusNumApi({ type: 2 })
        .then((res) => {
          const all = res.all || 0;
          this.statusAll = all;
          this.unPaid = res.unPaid || 0;
          this.notShipped = res.notShipped || 0;
          this.refundCnt = (res.refunding || 0) + (res.refunded || 0);
          const values = {
            pay: this.pct(all - this.unPaid, all),
            ship: this.pct(all - this.notShipped, all),
            refund: this.pct(this.refundCnt, all),
          };
          this.gauges.forEach((g) => {
            g.value = values[g.key];
            this.updateGauge(g.key, g.value);
          });
        })
        .catch(() => {});
      orderListApi({ page: 1, limit: 6, status: 'all', type: 2 })
        .then((res) => {
          this.orders = (res && res.list) || [];
        })
        .catch(() => {});
      pingouTeamListApi({ page: 1, limit: 6 })
        .then((res) => {
          const list = (res && res.list) || [];
          this.teams = list.sort((a, b) => (b.joinNum || 0) - (a.joinNum || 0)).slice(0, 6);
        })
        .catch(() => {});
    },
    // ===== 仪表盘 =====
    makeGaugeOption(value, color) {
      return {
        series: [
          {
            type: 'gauge',
            startAngle: 210,
            endAngle: -30,
            min: 0,
            max: 100,
            radius: '95%',
            center: ['50%', '58%'],
            axisLine: {
              lineStyle: {
                width: 12,
                color: [
                  [0.4, '#3ddc97'],
                  [0.7, '#f7b500'],
                  [1, '#ff5c5c'],
                ],
              },
            },
            pointer: {
              length: '58%',
              width: 4,
              itemStyle: { color: '#e8dcff' },
            },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { show: false },
            detail: {
              formatter: '{value}%',
              color: color || '#e8dcff',
              fontSize: 18,
              offsetCenter: [0, '62%'],
            },
            data: [{ value: value || 0 }],
          },
        ],
      };
    },
    initGauges() {
      this.gauges.forEach((g) => {
        const el = this.$refs['gauge_' + g.key];
        const dom = Array.isArray(el) ? el[0] : el;
        if (!dom) return;
        const chart = echarts.init(dom);
        chart.setOption(this.makeGaugeOption(0));
        this.gaugeCharts.push(chart);
      });
    },
    updateGauge(key, value) {
      const idx = this.gauges.findIndex((g) => g.key === key);
      const chart = this.gaugeCharts[idx];
      if (chart) chart.setOption({ series: [{ data: [{ value }] }] });
    },
    // ===== 中国地图 =====
    initMap() {
      const dom = this.$refs.mapChart;
      if (!dom) return;
      echarts.registerMap('qx_china', chinaJson);
      const chart = echarts.init(dom);
      this.mapChartObj = chart;
      const pts = MAP_POINTS.map((p, i) => ({ ...p, value: [p.value[0], p.value[1], 20 - i] }));
      chart.setOption({
        geo: {
          map: 'qx_china',
          roam: false,
          zoom: 1.12,
          top: '8%',
          bottom: '4%',
          label: { show: false },
          itemStyle: {
            areaColor: 'rgba(66,34,126,.55)',
            borderColor: 'rgba(154,120,255,.85)',
            borderWidth: 1,
            shadowColor: 'rgba(124,92,255,.45)',
            shadowBlur: 14,
          },
          emphasis: { disabled: true },
        },
        series: [
          {
            type: 'effectScatter',
            coordinateSystem: 'geo',
            symbolSize: 7,
            rippleEffect: { brushType: 'stroke', scale: 4.2, period: 4 },
            showEffectOn: 'render',
            itemStyle: { color: '#7ef0c8', shadowBlur: 10, shadowColor: '#7ef0c8' },
            data: pts,
          },
          {
            type: 'lines',
            coordinateSystem: 'geo',
            zlevel: 2,
            effect: { show: true, period: 5, trailLength: 0.55, symbol: 'arrow', symbolSize: 5, color: '#b18cff' },
            lineStyle: { color: '#7c5cff', width: 1, opacity: 0.28, curveness: 0.28 },
            // 各点飞向贵阳（运营中心）
            data: pts.slice(1).map((p) => ({ coords: [p.value.slice(0, 2), MAP_POINTS[0].value] })),
          },
        ],
      });
    },
  },
};
</script>

<style scoped lang="scss">
.bigdata-viewport {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: #0d0524;
  overflow: hidden;
}
.bigdata-stage {
  position: absolute;
  top: 50%;
  left: 50%;
  transform-origin: center center;
  margin-left: -960px;
  margin-top: -540px;
  display: flex;
  flex-direction: column;
  background: radial-gradient(1200px 700px at 18% 8%, #2c1358 0%, rgba(44, 19, 88, 0) 60%),
    radial-gradient(1000px 620px at 85% 90%, #241040 0%, rgba(36, 16, 64, 0) 55%),
    linear-gradient(160deg, #1b0b3b 0%, #140829 55%, #190b33 100%);
  color: #fff;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 背景光斑与网格 */
.bd-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.bd-bg-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(154, 120, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(154, 120, 255, 0.05) 1px, transparent 1px);
  background-size: 64px 64px;
}
.bd-bokeh {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(177, 140, 255, 0.32) 0%, rgba(177, 140, 255, 0) 70%);
  animation: bdFloat 9s ease-in-out infinite alternate;
}
@for $i from 1 through 14 {
  .bd-bokeh-#{$i} {
    width: #{40 + ($i * 13) % 120}px;
    height: #{40 + ($i * 13) % 120}px;
    left: #{($i * 137) % 1850}px;
    top: #{($i * 211) % 1000}px;
    animation-delay: #{$i * 0.6}s;
  }
}
@keyframes bdFloat {
  from {
    transform: translateY(0) scale(1);
    opacity: 0.55;
  }
  to {
    transform: translateY(-36px) scale(1.18);
    opacity: 0.95;
  }
}

/* 顶栏 */
.bd-header {
  position: relative;
  height: 88px;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(180deg, rgba(64, 30, 130, 0.55), rgba(64, 30, 130, 0));
  border-bottom: 1px solid rgba(154, 120, 255, 0.35);
  z-index: 2;
}
.bd-hd-left {
  min-width: 280px;
}
.bd-hd-date {
  font-size: 15px;
  color: #b7a3e6;
  letter-spacing: 1px;
}
.bd-hd-clock {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #e8dcff;
  text-shadow: 0 0 14px rgba(177, 140, 255, 0.8);
}
.bd-hd-title {
  display: flex;
  align-items: center;
  gap: 22px;
  h1 {
    margin: 0;
    font-size: 40px;
    font-weight: 800;
    letter-spacing: 10px;
    background: linear-gradient(180deg, #ffffff 30%, #c9b0ff 75%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 26px rgba(124, 92, 255, 0.55);
  }
}
.bd-hd-wing {
  width: 190px;
  height: 12px;
  background: linear-gradient(90deg, rgba(177, 140, 255, 0), #b18cff);
  clip-path: polygon(0 45%, 100% 0, 100% 100%, 0 55%);
  &.flip {
    transform: scaleX(-1);
  }
}
.bd-hd-right {
  min-width: 280px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
}
.bd-hd-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  font-size: 14px;
  color: #cfeaff;
  border: 1px solid rgba(126, 240, 200, 0.5);
  border-radius: 999px;
  background: rgba(61, 220, 151, 0.1);
  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #3ddc97;
    box-shadow: 0 0 8px #3ddc97;
    animation: bdBlink 1.6s infinite;
  }
}
@keyframes bdBlink {
  50% {
    opacity: 0.25;
  }
}
.bd-hd-fs {
  padding: 7px 14px;
  font-size: 14px;
  color: #e8dcff;
  border: 1px solid rgba(177, 140, 255, 0.55);
  border-radius: 999px;
  cursor: pointer;
  background: rgba(124, 92, 255, 0.14);
  &:hover {
    background: rgba(124, 92, 255, 0.34);
  }
}

/* 主体 */
.bd-body {
  position: relative;
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px 20px 20px;
  z-index: 1;
  min-height: 0;
}
.bd-left {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.bd-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

/* 面板通用 */
.bd-panel {
  position: relative;
  background: linear-gradient(180deg, rgba(58, 30, 116, 0.5), rgba(38, 18, 78, 0.62));
  border: 1px solid rgba(154, 120, 255, 0.4);
  border-radius: 10px;
  box-shadow: 0 0 22px rgba(94, 58, 196, 0.28) inset;
}
.bd-corner {
  position: absolute;
  width: 14px;
  height: 14px;
  border-color: #b18cff;
  border-style: solid;
  border-width: 0;
  &.tl { top: -1px; left: -1px; border-top-width: 2px; border-left-width: 2px; border-top-left-radius: 8px; }
  &.tr { top: -1px; right: -1px; border-top-width: 2px; border-right-width: 2px; border-top-right-radius: 8px; }
  &.bl { bottom: -1px; left: -1px; border-bottom-width: 2px; border-left-width: 2px; border-bottom-left-radius: 8px; }
  &.br { bottom: -1px; right: -1px; border-bottom-width: 2px; border-right-width: 2px; border-bottom-right-radius: 8px; }
}
.bd-panel-cap {
  padding: 12px 18px 0;
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #d9c9ff;
}

/* 今日交易额 */
.bd-panel-trade {
  height: 190px;
  display: flex;
  align-items: center;
  padding: 0 34px;
  gap: 60px;
  background: linear-gradient(120deg, rgba(96, 50, 190, 0.5), rgba(58, 30, 116, 0.55));
}
.bd-trade-label {
  font-size: 18px;
  color: #cbb8f5;
  .bd-trade-time {
    margin-left: 16px;
    font-size: 13px;
    color: #9c86d4;
    letter-spacing: 1px;
  }
}
.bd-trade-num {
  margin-top: 6px;
  font-size: 74px;
  font-weight: 800;
  font-family: 'DIN Alternate', 'Consolas', monospace;
  letter-spacing: 2px;
  background: linear-gradient(180deg, #ffffff 35%, #d9c9ff 80%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 30px rgba(177, 140, 255, 0.45);
}
.bd-trade-sub {
  display: flex;
  gap: 70px;
  margin-left: auto;
  padding-right: 20px;
}
.bd-ti-label {
  font-size: 16px;
  color: #b7a3e6;
}
.bd-ti-num {
  margin-top: 8px;
  font-size: 40px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Consolas', monospace;
  color: #fff;
  text-shadow: 0 0 16px rgba(177, 140, 255, 0.5);
}

/* 健康度仪表盘 */
.bd-panel-gauge {
  height: 235px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
}
.bd-gauge-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-left: 22px;
  min-width: 220px;
  .bd-shield {
    font-size: 52px;
    color: #7ef0c8;
    text-shadow: 0 0 18px rgba(61, 220, 151, 0.7);
  }
  .bd-gauge-title {
    font-size: 20px;
    font-weight: 700;
    color: #d9c9ff;
    letter-spacing: 2px;
  }
  .bd-gauge-sub {
    margin-top: 6px;
    font-size: 13px;
    color: #9c86d4;
  }
}
.bd-gauges {
  flex: 1;
  display: flex;
}
.bd-gauge {
  flex: 1;
  text-align: center;
}
.bd-gauge-name {
  font-size: 15px;
  color: #b7a3e6;
}
.bd-gauge-wrap {
  height: 160px;
}
.bd-gauge-echart {
  width: 100%;
  height: 100%;
}

/* 地图 */
.bd-panel-map {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.bd-map-wrap {
  flex: 1;
  display: flex;
  min-height: 0;
}
.bd-map-echart {
  flex: 1;
  height: 100%;
  min-width: 0;
}
.bd-map-side {
  width: 210px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 18px;
  padding-right: 22px;
}
.bd-ms-item {
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(96, 50, 190, 0.22);
  border: 1px solid rgba(154, 120, 255, 0.35);
}
.bd-ms-num {
  font-size: 28px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Consolas', monospace;
}
.bd-ms-label {
  margin-top: 4px;
  font-size: 13px;
  color: #b7a3e6;
}

/* 最近下单 */
.bd-panel-order {
  flex: 1.35;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.bd-order-list {
  flex: 1;
  overflow: hidden;
  padding: 8px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bd-order {
  padding: 10px 12px;
  border-radius: 8px;
  background: rgba(24, 10, 52, 0.55);
  border: 1px solid rgba(154, 120, 255, 0.28);
  animation: bdSlideIn 0.6s ease both;
}
@keyframes bdSlideIn {
  from {
    opacity: 0;
    transform: translateX(26px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
.bd-order-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bd-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  color: #241040;
  background: linear-gradient(135deg, #c9b0ff, #8f6fff);
  flex-shrink: 0;
}
.bd-ou-info {
  flex: 1;
  min-width: 0;
}
.bd-ou-name {
  font-size: 15px;
  font-weight: 600;
  color: #efe6ff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bd-ou-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #9c86d4;
}
.bd-ou-time {
  font-size: 12px;
  color: #9c86d4;
}
.bd-order-card {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  background: rgba(96, 50, 190, 0.24);
  border: 1px solid rgba(154, 120, 255, 0.25);
}
.bd-oc-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #cbb8f5;
  b {
    color: #ffd479;
    font-family: 'DIN Alternate', 'Consolas', monospace;
  }
  &.sub {
    margin-top: 6px;
    justify-content: space-between;
  }
}
.bd-oc-name {
  color: #fff;
  font-weight: 600;
}
.bd-oc-orderid {
  font-size: 12px;
  color: #9c86d4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}
.bd-oc-tag {
  flex-shrink: 0;
  padding: 2px 10px;
  font-size: 12px;
  border-radius: 999px;
  &.ok {
    color: #7ef0c8;
    border: 1px solid rgba(61, 220, 151, 0.55);
    background: rgba(61, 220, 151, 0.12);
  }
  &.wait {
    color: #ffd479;
    border: 1px solid rgba(247, 181, 0, 0.55);
    background: rgba(247, 181, 0, 0.12);
  }
}

/* 优秀团队 */
.bd-panel-team {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.bd-team-list {
  flex: 1;
  overflow: hidden;
  padding: 8px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.bd-team {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(24, 10, 52, 0.55);
  border: 1px solid rgba(154, 120, 255, 0.28);
}
.bd-team-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(177, 140, 255, 0.6);
  flex-shrink: 0;
  &.ph {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 700;
    color: #fff;
    background: linear-gradient(135deg, #8f6fff, #5e3ac4);
  }
}
.bd-team-info {
  flex: 1;
  min-width: 0;
}
.bd-team-name {
  font-size: 15px;
  font-weight: 600;
  color: #efe6ff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bd-team-meta {
  margin-top: 2px;
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #9c86d4;
}
.bd-team-rank {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #241040;
  background: #c9b0ff;
  &.rk1 { background: linear-gradient(135deg, #ffe08a, #f7b500); }
  &.rk2 { background: linear-gradient(135deg, #e8e8ef, #b9bcd0); }
  &.rk3 { background: linear-gradient(135deg, #ffc79c, #e89a5f); }
}

.bd-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c6bb0;
  font-size: 14px;
}
</style>
