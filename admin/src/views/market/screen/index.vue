<template>
  <div ref="screenWrap" class="qx-screen">
    <!-- 背景网格 -->
    <div class="qx-bg-grid"></div>

    <!-- 顶栏 -->
    <header class="qx-header">
      <div class="qx-brand">
        <div class="qx-logo">QX</div>
        <div class="qx-brand-text">
          <h1>黔序数据指挥中心</h1>
          <p>QIANXU DATA COMMAND CENTER</p>
        </div>
      </div>
      <div class="qx-ticker">
        <span>本月销售额 <b>￥{{ trade.monthOrderSales || '0.00' }}</b></span>
        <span>本月订单 <b>{{ trade.monthOrderNum || 0 }}</b> 单</span>
        <span>累计消费额 <b>￥{{ userTotal.consumptionAmount || '0.00' }}</b></span>
        <span>累计会员 <b>{{ userTotal.userNum || 0 }}</b> 人</span>
      </div>
      <div class="qx-header-right">
        <span class="qx-live"><i></i>实时同步</span>
        <span class="qx-clock">{{ nowText }}</span>
        <a class="qx-fullscreen" :title="isFull ? '退出全屏' : '全屏显示'" @click="toggleFull">
          <i :class="isFull ? 'el-icon-close' : 'el-icon-full-screen'"></i>
        </a>
      </div>
    </header>

    <!-- KPI 指标行 -->
    <section class="qx-kpi-row">
      <div class="qx-kpi" v-for="(item, i) in kpiList" :key="i">
        <div class="qx-kpi-label">
          {{ item.label }}
          <span class="qx-tag">{{ item.tag }}</span>
        </div>
        <div class="qx-kpi-num" :style="{ color: item.color || '#eaf6ff' }">
          <small v-if="item.prefix">{{ item.prefix }}</small>{{ item.value }}<small v-if="item.unit">{{ item.unit }}</small>
        </div>
        <div class="qx-kpi-foot">
          <span v-if="item.ratio" :class="item.ratioClass">{{ item.ratio }}</span>
          <span>{{ item.foot }}</span>
        </div>
      </div>
    </section>

    <!-- 主体三栏 -->
    <section class="qx-main">
      <!-- 左栏 -->
      <div class="qx-col">
        <div class="qx-panel qx-panel-chart">
          <div class="qx-panel-head">
            <span class="qx-panel-title">销售趋势</span>
            <span class="qx-panel-sub">近 30 天</span>
          </div>
          <div ref="trendChart" class="qx-chart qx-chart-trend"></div>
        </div>
        <div class="qx-panel">
          <div class="qx-panel-head">
            <span class="qx-panel-title">核心经营指标</span>
            <span class="qx-panel-sub">实时</span>
          </div>
          <div class="qx-biz-grid">
            <div class="qx-biz-item" v-for="(item, i) in bizList" :key="i">
              <div class="qx-biz-label">{{ item.label }}</div>
              <div class="qx-biz-num" :style="{ color: item.color }">{{ item.value }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中栏 -->
      <div class="qx-col">
        <div class="qx-panel qx-panel-chart">
          <div class="qx-panel-head">
            <span class="qx-panel-title">实时订单流</span>
            <span class="qx-panel-sub"><i class="qx-dot-live"></i>每 10 秒刷新</span>
          </div>
          <div class="qx-order-stream">
            <transition-group name="qx-fade">
              <div class="qx-order-row" v-for="(o, i) in orderStream" :key="o.orderId" :class="{ 'is-new': o.isNew }">
                <span class="qx-o-no">{{ i + 1 < 10 ? '0' + (i + 1) : i + 1 }}</span>
                <span class="qx-o-user">{{ o.realName || o.nickname || '匿名用户' }}</span>
                <span class="qx-o-goods">{{ o.goodsName }}</span>
                <span class="qx-o-amt">￥{{ o.payPrice }}</span>
                <span class="qx-o-status" :style="statusStyle(o.statusKey)">{{ o.statusText }}</span>
                <span class="qx-o-time">{{ o.timeShort }}</span>
              </div>
            </transition-group>
            <div class="qx-empty" v-if="!orderStream.length">暂无订单数据</div>
          </div>
        </div>
        <div class="qx-panel">
          <div class="qx-panel-head">
            <span class="qx-panel-title">业务板块</span>
            <span class="qx-panel-sub">代理 / 拼购 / 分红</span>
          </div>
          <div class="qx-biz-grid qx-biz-grid-6">
            <div class="qx-biz-item" v-for="(item, i) in moduleList" :key="i">
              <div class="qx-biz-label">{{ item.label }}</div>
              <div class="qx-biz-num" :style="{ color: item.color }">{{ item.value }}<small v-if="item.unit">{{ item.unit }}</small></div>
            </div>
          </div>
          <div class="qx-dividend-tip" v-if="dividend.periodNo">
            最近一期分红 <b>{{ dividend.periodNo }}</b> 共发放 <b>￥{{ dividend.totalPaid }}</b>，覆盖 <b>{{ dividend.memberCount }}</b> 人
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="qx-col">
        <div class="qx-panel qx-panel-chart">
          <div class="qx-panel-head">
            <span class="qx-panel-title">用户渠道分布</span>
            <span class="qx-panel-sub">累计 {{ userTotal.userNum || 0 }} 人</span>
          </div>
          <div ref="channelChart" class="qx-chart qx-chart-channel"></div>
        </div>
        <div class="qx-panel qx-panel-chart">
          <div class="qx-panel-head">
            <span class="qx-panel-title">订单状态分布</span>
            <span class="qx-panel-sub">全部订单</span>
          </div>
          <div ref="statusChart" class="qx-chart qx-chart-status"></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import echarts from 'echarts';
import screenfull from 'screenfull';
import { viewModelApi, chartOrder30Api, businessData } from '@/api/dashboard';
import { tradeDataApi, userTotalData, userChannelData } from '@/api/statistic';
import { orderListApi, orderStatusNumApi } from '@/api/order';
import { stockAgentListApi } from '@/api/stock';
import { agentListApi } from '@/api/daili';
import { pingouTeamListApi, pingouActivityListApi } from '@/api/pingou';
import { dividendRecordListApi } from '@/api/dividend';

const CYAN = '#00e5ff';
const BLUE = '#2f7bff';
const GOLD = '#ffc84a';
const PURPLE = '#a06bff';
const GREEN = '#2fe0a2';
const RED = '#ff5c6c';

const STATUS_MAP = {
  notShipped: { text: '待发货', color: GOLD },
  toBeWrittenOff: { text: '待核销', color: CYAN },
  complete: { text: '已完成', color: GREEN },
  unPaid: { text: '待付款', color: '#8fa7c9' },
  refunding: { text: '退款中', color: RED },
  refunded: { text: '已退款', color: '#7f8ea3' },
  spike: { text: '秒杀', color: PURPLE },
  bargain: { text: '砍价', color: PURPLE },
  deleted: { text: '已删除', color: '#64748b' },
};

export default {
  name: 'MarketDataScreen',
  data() {
    return {
      loading: false,
      isFull: false,
      nowText: '',
      kpi: {},
      trade: {},
      biz: {},
      userTotal: {},
      userSex: [],
      orderStatus: {},
      orderStream: [],
      agentCount: 0,
      agentAudit: 0,
      stockAgentCount: 0,
      pingouTeamCount: 0,
      pingouActivityCount: 0,
      dividend: {},
      channelData: [],
      trendData: { price: {}, quality: {} },
      charts: {},
      timers: [],
    };
  },
  computed: {
    // KPI 指标卡
    kpiList() {
      const k = this.kpi || {};
      const b = this.biz || {};
      return [
        {
          label: '今日销售额',
          tag: '今日',
          prefix: '￥',
          value: k.sales || '0.00',
          ratio: this.ratioText(k.sales, k.yesterdaySales),
          ratioClass: this.ratioClass(k.sales, k.yesterdaySales),
          foot: `昨日 ￥${k.yesterdaySales || '0.00'}`,
        },
        {
          label: '支付订单量',
          tag: '今日',
          value: k.orderNum || 0,
          unit: ' 单',
          ratio: this.ratioText(k.orderNum, k.yesterdayOrderNum),
          ratioClass: this.ratioClass(k.orderNum, k.yesterdayOrderNum),
          foot: `昨日 ${k.yesterdayOrderNum || 0} 单`,
        },
        {
          label: '新增用户',
          tag: '今日',
          value: k.newUserNum || 0,
          unit: ' 人',
          ratio: this.ratioText(k.newUserNum, k.yesterdayNewUserNum),
          ratioClass: this.ratioClass(k.newUserNum, k.yesterdayNewUserNum),
          foot: `昨日 ${k.yesterdayNewUserNum || 0} 人`,
        },
        {
          label: '待发货订单',
          tag: '待办',
          value: b.notShippingOrderNum || 0,
          unit: ' 单',
          color: GOLD,
          foot: '需尽快处理',
        },
        {
          label: '库存预警',
          tag: '告警',
          value: b.vigilanceInventoryNum || 0,
          unit: ' 件',
          color: (b.vigilanceInventoryNum || 0) > 0 ? RED : GREEN,
          foot: '低于安全库存',
        },
        {
          label: '累计会员',
          tag: '总量',
          value: this.userTotal.userNum || 0,
          unit: ' 人',
          color: CYAN,
          foot: `访客 ${k.pageviews || 0} · 充值 ${this.userTotal.rechargePeopleNum || 0} 人`,
        },
      ];
    },
    // 核心经营指标
    bizList() {
      const b = this.biz || {};
      return [
        { label: '待发货', value: b.notShippingOrderNum || 0, color: GOLD },
        { label: '退款中', value: b.refundingOrderNum || 0, color: RED },
        { label: '待核销', value: b.notWriteOffOrderNum || 0, color: CYAN },
        { label: '库存预警', value: b.vigilanceInventoryNum || 0, color: RED },
        { label: '上架商品', value: b.onSaleProductNum || 0, color: '#eaf6ff' },
        { label: '仓库商品', value: b.notSaleProductNum || 0, color: '#eaf6ff' },
        { label: '待审核', value: b.notAuditNum || 0, color: GOLD },
        { label: '账户充值', value: `￥${b.totalRechargeAmount || '0.00'}`, color: GREEN },
      ];
    },
    // 业务板块
    moduleList() {
      return [
        { label: '库存代理商', value: this.stockAgentCount, unit: ' 个', color: CYAN },
        { label: '区域代理', value: this.agentCount, unit: ' 个', color: BLUE },
        { label: '代理待审核', value: this.agentAudit, unit: ' 个', color: GOLD },
        { label: '拼购活动', value: this.pingouActivityCount, unit: ' 场', color: PURPLE },
        { label: '拼购团', value: this.pingouTeamCount, unit: ' 个', color: GREEN },
        {
          label: '上期分红基数',
          value: `￥${this.dividend.baseAmount || '0.00'}`,
          color: GOLD,
        },
      ];
    },
  },
  mounted() {
    this.tickClock();
    this.timers.push(setInterval(this.tickClock, 1000));
    this.loadAll();
    // 实时订单流：10 秒
    this.timers.push(setInterval(this.loadOrderStream, 10000));
    // 统计类数据：30 秒
    this.timers.push(setInterval(this.loadStat, 30000));
    window.addEventListener('resize', this.resizeCharts);
    document.addEventListener('fullscreenchange', this.onFullChange);
  },
  beforeDestroy() {
    this.timers.forEach((t) => clearInterval(t));
    window.removeEventListener('resize', this.resizeCharts);
    document.removeEventListener('fullscreenchange', this.onFullChange);
    Object.keys(this.charts).forEach((k) => {
      if (this.charts[k]) this.charts[k].dispose();
    });
    this.charts = {};
  },
  methods: {
    /* ---------------- 时钟 ---------------- */
    tickClock() {
      const d = new Date();
      const p = (n) => (n < 10 ? '0' + n : n);
      const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
      this.nowText = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} 星期${week} ${p(
        d.getHours()
      )}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
    },

    /* ---------------- 全屏 ---------------- */
    toggleFull() {
      if (!screenfull.isEnabled) return this.$message.warning('当前浏览器不支持全屏');
      const el = document.documentElement;
      screenfull.toggle(el);
    },
    onFullChange() {
      this.isFull = !!(screenfull.isFullscreen || document.fullscreenElement);
      setTimeout(this.resizeCharts, 300);
    },
    resizeCharts() {
      Object.keys(this.charts).forEach((k) => {
        if (this.charts[k]) this.charts[k].resize();
      });
    },

    /* ---------------- 数值工具 ---------------- */
    ratioText(cur, pre) {
      const c = Number(cur || 0);
      const p = Number(pre || 0);
      if (!p) return c ? '较昨日 新增' : '';
      const r = ((c - p) / p) * 100;
      if (!r) return '较昨日 持平';
      return `较昨日 ${r > 0 ? '▲' : '▼'} ${Math.abs(r).toFixed(1)}%`;
    },
    // 国内惯例：涨红跌绿
    ratioClass(cur, pre) {
      const c = Number(cur || 0);
      const p = Number(pre || 0);
      if (!p) return '';
      if (c > p) return 'is-up';
      if (c < p) return 'is-down';
      return '';
    },
    statusStyle(key) {
      const s = STATUS_MAP[key] || { color: '#8fa7c9' };
      return {
        color: s.color,
        borderColor: s.color + '66',
        background: s.color + '14',
      };
    },

    /* ---------------- 数据加载 ---------------- */
    loadAll() {
      this.loadStat();
      this.loadOrderStream();
      this.loadTrend();
    },
    loadStat() {
      // 首页概览
      viewModelApi()
        .then((res) => {
          this.kpi = res || {};
        })
        .catch(() => {});
      // 经营数据
      businessData()
        .then((res) => {
          this.biz = res || {};
        })
        .catch(() => {});
      // 交易数据
      tradeDataApi()
        .then((res) => {
          this.trade = res || {};
        })
        .catch(() => {});
      // 会员总量
      userTotalData()
        .then((res) => {
          this.userTotal = res || {};
        })
        .catch(() => {});
      // 渠道
      userChannelData()
        .then((res) => {
          const list = res || [];
          const map = { h5: 'H5', ios: 'APP', routine: '小程序', wechat: '公众号' };
          this.channelData = list.map((i) => ({
            name: map[i.channel] || i.channel || '其他',
            value: Number(i.num || 0),
          }));
          this.renderChannelChart();
        })
        .catch(() => {});
      // 订单状态
      orderStatusNumApi({ type: 2 })
        .then((res) => {
          const d = res || {};
          this.orderStatus = d;
          this.renderStatusChart();
        })
        .catch(() => {});
      // 库存代理商
      stockAgentListApi({ page: 1, limit: 1 })
        .then((res) => {
          this.stockAgentCount = (res && res.total) || 0;
        })
        .catch(() => {});
      // 区域代理（总数 / 待审核）
      agentListApi({ page: 1, limit: 1 })
        .then((res) => {
          this.agentCount = (res && res.total) || 0;
        })
        .catch(() => {});
      agentListApi({ page: 1, limit: 1, status: 0 })
        .then((res) => {
          this.agentAudit = (res && res.total) || 0;
        })
        .catch(() => {});
      // 拼购
      pingouActivityListApi({ page: 1, limit: 1 })
        .then((res) => {
          this.pingouActivityCount = (res && res.total) || 0;
        })
        .catch(() => {});
      pingouTeamListApi({ page: 1, limit: 1 })
        .then((res) => {
          this.pingouTeamCount = (res && res.total) || 0;
        })
        .catch(() => {});
      // 分红最近一期
      dividendRecordListApi({ page: 1, limit: 1 })
        .then((res) => {
          const list = (res && res.list) || [];
          this.dividend = list[0] || {};
        })
        .catch(() => {});
    },
    loadOrderStream() {
      orderListApi({ page: 1, limit: 9, status: 'all', type: 2 })
        .then((res) => {
          const list = (res && res.list) || [];
          const oldIds = this.orderStream.map((i) => i.orderId);
          this.orderStream = list.map((o) => {
            const s = STATUS_MAP[(o.statusStr && o.statusStr.key) || ''] || {};
            const goods = (o.productList || []).map((p) => p.productName || p.storeName || '').join('、');
            return {
              orderId: o.orderId,
              realName: o.realName,
              nickname: o.nickname,
              goodsName: goods || '商品订单',
              payPrice: o.payPrice || '0.00',
              statusKey: (o.statusStr && o.statusStr.key) || '',
              statusText: (o.statusStr && o.statusStr.value) || '未知',
              timeShort: (o.createTime || '').slice(11, 19),
              isNew: oldIds.length > 0 && !oldIds.includes(o.orderId),
            };
          });
          setTimeout(() => {
            this.orderStream = this.orderStream.map((i) => ({ ...i, isNew: false }));
          }, 3000);
        })
        .catch(() => {});
    },
    loadTrend() {
      chartOrder30Api()
        .then((res) => {
          this.trendData = res || { price: {}, quality: {} };
          this.renderTrendChart();
        })
        .catch(() => {
          this.renderTrendChart();
        });
    },

    /* ---------------- 图表 ---------------- */
    initChart(ref, key) {
      if (!this.$refs[ref]) return null;
      if (!this.charts[key]) {
        this.charts[key] = echarts.init(this.$refs[ref]);
      }
      return this.charts[key];
    },
    renderTrendChart() {
      const chart = this.initChart('trendChart', 'trend');
      if (!chart) return;
      const price = this.trendData.price || {};
      const quality = this.trendData.quality || {};
      const xData = Object.keys(price);
      const priceData = xData.map((k) => Number(price[k] || 0));
      const qualityData = xData.map((k) => Number(quality[k] || 0));
      chart.setOption({
        grid: { top: 42, left: 8, right: 8, bottom: 4, containLabel: true },
        legend: {
          data: ['销售额', '订单量'],
          right: 10,
          top: 6,
          itemWidth: 14,
          itemHeight: 3,
          textStyle: { color: '#7fa3d8', fontSize: 11 },
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(6,18,40,.92)',
          borderColor: 'rgba(0,229,255,.35)',
          textStyle: { color: '#d9e8ff', fontSize: 12 },
        },
        xAxis: {
          type: 'category',
          data: xData,
          boundaryGap: true,
          axisLine: { lineStyle: { color: 'rgba(0,196,255,.25)' } },
          axisTick: { show: false },
          axisLabel: { color: '#5d7dad', fontSize: 10, interval: Math.floor(xData.length / 8) },
        },
        yAxis: [
          {
            type: 'value',
            name: '销售额',
            nameTextStyle: { color: '#5d7dad', fontSize: 10 },
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { lineStyle: { color: 'rgba(0,196,255,.10)' } },
            axisLabel: { color: '#5d7dad', fontSize: 10 },
          },
          {
            type: 'value',
            name: '订单量',
            nameTextStyle: { color: '#5d7dad', fontSize: 10 },
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { color: '#5d7dad', fontSize: 10 },
          },
        ],
        series: [
          {
            name: '订单量',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 5,
            itemStyle: {
              borderRadius: [3, 3, 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255,200,74,.95)' },
                { offset: 1, color: 'rgba(255,200,74,.15)' },
              ]),
            },
            data: qualityData,
          },
          {
            name: '销售额',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 5,
            showSymbol: false,
            lineStyle: { width: 2, color: CYAN, shadowColor: 'rgba(0,229,255,.8)', shadowBlur: 10 },
            itemStyle: { color: CYAN },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0,229,255,.35)' },
                { offset: 1, color: 'rgba(0,229,255,0)' },
              ]),
            },
            data: priceData,
          },
        ],
      });
      this.charts.trend.resize();
    },
    renderChannelChart() {
      const chart = this.initChart('channelChart', 'channel');
      if (!chart) return;
      const data = this.channelData.length ? this.channelData : [{ name: '暂无数据', value: 0 }];
      const colors = [CYAN, BLUE, GOLD, PURPLE, '#64748b'];
      const total = data.reduce((s, i) => s + i.value, 0);
      chart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(6,18,40,.92)',
          borderColor: 'rgba(0,229,255,.35)',
          textStyle: { color: '#d9e8ff', fontSize: 12 },
          formatter: '{b}：{c} 人（{d}%）',
        },
        legend: {
          orient: 'vertical',
          right: 8,
          top: 'center',
          itemWidth: 9,
          itemHeight: 9,
          icon: 'rect',
          itemGap: 12,
          textStyle: { color: '#8fa7c9', fontSize: 11 },
        },
        color: colors,
        series: [
          {
            type: 'pie',
            radius: ['52%', '74%'],
            center: ['34%', '50%'],
            avoidLabelOverlap: false,
            label: { show: false },
            labelLine: { show: false },
            itemStyle: { borderColor: 'rgba(6,18,40,.9)', borderWidth: 2 },
            data,
          },
        ],
        graphic: [
          {
            type: 'text',
            left: '27%',
            top: '40%',
            style: {
              text: String(total),
              fill: '#eaf6ff',
              font: 'bold 22px Consolas, monospace',
              textAlign: 'center',
            },
          },
          {
            type: 'text',
            left: '27.8%',
            top: '56%',
            style: { text: '累计会员', fill: '#7fa3d8', font: '11px "Microsoft YaHei"' },
          },
        ],
      });
      this.charts.channel.resize();
    },
    renderStatusChart() {
      const chart = this.initChart('statusChart', 'status');
      if (!chart) return;
      const d = this.orderStatus || {};
      const conf = [
        { key: 'unPaid', label: '待付款', color: '#8fa7c9' },
        { key: 'notShipped', label: '待发货', color: GOLD },
        { key: 'toBeWrittenOff', label: '待核销', color: CYAN },
        { key: 'complete', label: '已完成', color: GREEN },
        { key: 'refunding', label: '退款中', color: RED },
      ];
      const cats = conf.map((i) => i.label);
      const vals = conf.map((i) => Number(d[i.key] || 0));
      chart.setOption({
        grid: { top: 12, left: 8, right: 34, bottom: 4, containLabel: true },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(6,18,40,.92)',
          borderColor: 'rgba(0,229,255,.35)',
          textStyle: { color: '#d9e8ff', fontSize: 12 },
        },
        xAxis: {
          type: 'value',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: 'rgba(0,196,255,.10)' } },
          axisLabel: { color: '#5d7dad', fontSize: 10 },
        },
        yAxis: {
          type: 'category',
          data: cats,
          inverse: true,
          axisLine: { lineStyle: { color: 'rgba(0,196,255,.25)' } },
          axisTick: { show: false },
          axisLabel: { color: '#8fa7c9', fontSize: 11 },
        },
        series: [
          {
            type: 'bar',
            barWidth: 10,
            label: { show: true, position: 'right', color: '#d9e8ff', fontSize: 11, fontFamily: 'Consolas' },
            itemStyle: {
              borderRadius: [0, 5, 5, 0],
              color: (p) => conf[p.dataIndex].color,
            },
            data: vals,
          },
        ],
      });
      this.charts.status.resize();
    },
  },
};
</script>

<style lang="scss" scoped>
$cyan: #00e5ff;
$text: #d9e8ff;
$dim: #7fa3d8;
$faint: #5d7dad;
$panel-border: rgba(0, 196, 255, 0.28);

.qx-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3000;
  overflow-y: auto;
  padding: 14px 16px 18px;
  background: radial-gradient(1200px 600px at 18% -10%, rgba(47, 123, 255, 0.2), transparent 60%),
    radial-gradient(900px 500px at 92% 0%, rgba(0, 229, 255, 0.12), transparent 55%),
    linear-gradient(180deg, #071026 0%, #060d1f 100%);
  color: $text;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  overflow: hidden;

  .qx-bg-grid {
    position: absolute;
    inset: 0;
    pointer-events: none;
    background-image: linear-gradient(rgba(0, 196, 255, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 196, 255, 0.05) 1px, transparent 1px);
    background-size: 44px 44px;
  }
}

/* ---------------- 顶栏 ---------------- */
.qx-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 62px;
  padding: 0 20px;
  margin-bottom: 14px;
  border: 1px solid $panel-border;
  border-radius: 10px;
  background: linear-gradient(90deg, rgba(10, 26, 56, 0.92), rgba(13, 36, 78, 0.78) 50%, rgba(10, 26, 56, 0.92));
  box-shadow: 0 0 22px rgba(0, 196, 255, 0.1), inset 0 0 30px rgba(0, 196, 255, 0.04);
}
.qx-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  .qx-logo {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 15px;
    color: #04122b;
    background: linear-gradient(135deg, #2f7bff, #00e5ff);
    box-shadow: 0 0 14px rgba(0, 229, 255, 0.55);
  }
  h1 {
    font-size: 19px;
    letter-spacing: 2px;
    font-weight: 600;
    margin: 0;
    background: linear-gradient(90deg, #eaf6ff, #8fd4ff);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
  p {
    margin: 2px 0 0;
    font-size: 10px;
    letter-spacing: 3px;
    color: #4a6a9c;
  }
}
.qx-ticker {
  display: flex;
  gap: 26px;
  font-size: 12px;
  color: $dim;
  span b {
    color: $cyan;
    font-family: Consolas, monospace;
    font-size: 14px;
    margin-left: 4px;
  }
}
.qx-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: $dim;

  .qx-live i {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #2fe0a2;
    box-shadow: 0 0 8px #2fe0a2;
    margin-right: 6px;
    animation: qxPulse 1.6s infinite;
  }
  .qx-clock {
    font-family: Consolas, monospace;
    font-size: 14px;
    color: $cyan;
  }
  .qx-fullscreen {
    cursor: pointer;
    color: $cyan;
    font-size: 16px;
    &:hover {
      text-shadow: 0 0 10px rgba(0, 229, 255, 0.8);
    }
  }
}
@keyframes qxPulse {
  50% {
    opacity: 0.3;
  }
}

/* ---------------- KPI ---------------- */
.qx-kpi-row {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}
.qx-kpi {
  position: relative;
  padding: 14px 16px 12px;
  border: 1px solid $panel-border;
  border-radius: 10px;
  background: rgba(13, 30, 64, 0.72);
  box-shadow: 0 0 18px rgba(0, 196, 255, 0.1), inset 0 0 24px rgba(0, 196, 255, 0.03);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 2px;
    background: linear-gradient(90deg, transparent, $cyan, transparent);
    opacity: 0.55;
  }
}
.qx-kpi-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: $dim;
}
.qx-tag {
  font-size: 10px;
  color: $cyan;
  border: 1px solid rgba(0, 229, 255, 0.4);
  border-radius: 3px;
  padding: 1px 5px;
}
.qx-kpi-num {
  font-family: Consolas, 'DIN Alternate', monospace;
  font-size: 26px;
  font-weight: 700;
  margin: 6px 0 4px;
  color: #eaf6ff;
  text-shadow: 0 0 12px rgba(0, 229, 255, 0.3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  small {
    font-size: 13px;
    font-weight: 400;
    color: $dim;
    margin: 0 2px;
  }
}
.qx-kpi-foot {
  font-size: 11px;
  color: #4a6a9c;
  display: flex;
  gap: 10px;
  white-space: nowrap;
  overflow: hidden;
  .is-up {
    color: #ff5c6c;
  }
  .is-down {
    color: #2fe0a2;
  }
}

/* ---------------- 主体 ---------------- */
.qx-main {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.22fr 1.12fr 1fr;
  gap: 12px;
  align-items: stretch;
}
.qx-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}
.qx-panel {
  position: relative;
  padding: 12px 14px 14px;
  border: 1px solid $panel-border;
  border-radius: 10px;
  background: rgba(13, 30, 64, 0.72);
  box-shadow: 0 0 18px rgba(0, 196, 255, 0.1), inset 0 0 24px rgba(0, 196, 255, 0.03);

  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 12px;
    height: 12px;
    border: 0 solid $cyan;
    opacity: 0.85;
  }
  &::before {
    top: -1px;
    left: -1px;
    border-top-width: 2px;
    border-left-width: 2px;
    border-top-left-radius: 10px;
  }
  &::after {
    bottom: -1px;
    right: -1px;
    border-bottom-width: 2px;
    border-right-width: 2px;
    border-bottom-right-radius: 10px;
  }
}
.qx-panel-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.qx-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.qx-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #eaf6ff;

  &::before {
    content: '';
    width: 4px;
    height: 14px;
    border-radius: 2px;
    background: linear-gradient(180deg, $cyan, #2f7bff);
    box-shadow: 0 0 8px rgba(0, 229, 255, 0.6);
  }
}
.qx-panel-sub {
  font-size: 11px;
  color: #4a6a9c;
}
.qx-dot-live {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #2fe0a2;
  box-shadow: 0 0 8px #2fe0a2;
  margin-right: 5px;
  animation: qxPulse 1.6s infinite;
}
.qx-chart {
  width: 100%;
  flex: 1;
}
.qx-chart-trend {
  min-height: 210px;
}
.qx-chart-channel {
  min-height: 168px;
}
.qx-chart-status {
  min-height: 150px;
}

/* 实时订单流 */
.qx-order-stream {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.qx-order-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 10px;
  border: 1px solid rgba(0, 196, 255, 0.1);
  border-radius: 6px;
  background: rgba(20, 44, 88, 0.45);
  font-size: 12px;
  &.is-new {
    animation: qxFlash 2.6s ease-out;
  }
  .qx-o-no {
    font-family: Consolas, monospace;
    font-size: 11px;
    color: #4a6a9c;
    width: 18px;
  }
  .qx-o-user {
    color: $text;
    width: 84px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .qx-o-goods {
    flex: 1;
    min-width: 0;
    color: #8fa7c9;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .qx-o-amt {
    font-family: Consolas, monospace;
    color: #ffc84a;
    font-weight: 600;
    width: 78px;
    text-align: right;
  }
  .qx-o-status {
    font-size: 10px;
    padding: 1px 7px;
    border-radius: 3px;
    border: 1px solid;
    width: 52px;
    text-align: center;
  }
  .qx-o-time {
    font-family: Consolas, monospace;
    font-size: 11px;
    color: #4a6a9c;
    width: 62px;
    text-align: right;
  }
}
@keyframes qxFlash {
  0% {
    background: rgba(0, 229, 255, 0.28);
    border-color: rgba(0, 229, 255, 0.8);
  }
  100% {
    background: rgba(20, 44, 88, 0.45);
  }
}
.qx-fade-enter-active {
  transition: all 0.4s ease;
}
.qx-fade-enter {
  opacity: 0;
  transform: translateY(-8px);
}
.qx-empty {
  text-align: center;
  color: #4a6a9c;
  font-size: 12px;
  padding: 30px 0;
}

/* 指标/板块网格 */
.qx-biz-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
.qx-biz-grid-6 {
  grid-template-columns: repeat(3, 1fr);
}
.qx-biz-item {
  padding: 10px 12px;
  border: 1px solid rgba(0, 196, 255, 0.1);
  border-radius: 8px;
  background: rgba(20, 44, 88, 0.4);
}
.qx-biz-label {
  font-size: 11px;
  color: $dim;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.qx-biz-num {
  font-family: Consolas, 'DIN Alternate', monospace;
  font-size: 20px;
  font-weight: 700;
  color: #eaf6ff;
  white-space: nowrap;
  small {
    font-size: 11px;
    color: $dim;
    font-weight: 400;
  }
}
.qx-dividend-tip {
  margin-top: 10px;
  padding: 7px 10px;
  border-radius: 6px;
  border: 1px dashed rgba(255, 200, 74, 0.35);
  background: rgba(255, 200, 74, 0.06);
  font-size: 11px;
  color: #b9965a;
  b {
    color: #ffc84a;
    font-family: Consolas, monospace;
  }
}

/* 窄屏兜底 */
@media screen and (max-width: 1400px) {
  .qx-kpi-row {
    grid-template-columns: repeat(3, 1fr);
  }
  .qx-main {
    grid-template-columns: 1fr;
  }
  .qx-ticker {
    display: none;
  }
}
</style>
