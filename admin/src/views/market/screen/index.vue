<template>
  <div class="qx-stage">
    <!-- 1920×1080 设计稿等比缩放容器 -->
    <div class="qx-scaler" :style="{ transform: 'scale(' + scale + ')' }">
      <div class="qx-screen">
        <!-- 背景层 -->
        <div class="qx-bg-grid"></div>
        <div class="qx-glow g1"></div>
        <div class="qx-glow g2"></div>
        <div class="qx-glow g3"></div>

        <!-- ==================== 顶栏 ==================== -->
        <header class="qx-header">
          <div class="qx-brand">
            <div class="qx-logo">QX</div>
            <div class="qx-brand-text">
              <h1>{{ screenTitles.operation }}</h1>
              <p>QIANXU OPERATION COMMAND CENTER</p>
            </div>
          </div>

          <div class="qx-wing">
            <span class="wg-line"></span>
            <span class="wg-dot"></span>
            <span class="wg-diamond"></span>
            <span class="wg-dot"></span>
            <span class="wg-line"></span>
          </div>

          <div class="qx-header-right">
            <span class="qx-live"><i></i>实时同步中</span>
            <span class="qx-clock-wrap">
              <span class="qx-clock">{{ nowTime }}</span>
              <span class="qx-date">{{ nowDate }}</span>
            </span>
            <a class="qx-fullscreen" :title="isFull ? '退出全屏' : '全屏显示'" @click="toggleFull">
              <i :class="isFull ? 'el-icon-close' : 'el-icon-full-screen'"></i>
            </a>
          </div>
        </header>

        <!-- ==================== 跑马灯 ==================== -->
        <div class="qx-ticker">
          <div class="qx-ticker-track">
            <span
              v-for="(t, i) in tickerItems"
              :key="'a' + i"
              class="qx-ticker-item"
              :class="'tone-' + t.tone"
            >
              {{ t.label }}<b>{{ t.value }}</b>
            </span>
            <span
              v-for="(t, i) in tickerItems"
              :key="'b' + i"
              class="qx-ticker-item"
              :class="'tone-' + t.tone"
            >
              {{ t.label }}<b>{{ t.value }}</b>
            </span>
          </div>
        </div>

        <!-- ==================== 主体三栏 ==================== -->
        <section class="qx-main">
          <!-- ---------- 左栏 ---------- -->
          <div class="qx-col qx-col-l">
            <div class="qx-hero" :class="{ 'is-focus': focusKey === 'kpi' }">
              <div class="qx-hero-label">今日销售额 <span class="qx-tag">今日</span></div>
              <div class="qx-hero-num">￥{{ heroSales.value }}</div>
              <div class="qx-hero-foot">
                <span v-if="heroSales.ratio" :class="heroSales.ratioClass">{{ heroSales.ratio }}</span>
                <span class="qx-hero-dim">{{ heroSales.foot }}</span>
              </div>
            </div>
            <div class="qx-kpi-grid">
              <div
                v-for="(item, i) in kpiList"
                :key="i"
                class="qx-kpi"
                :class="{ 'is-focus': focusKey === 'kpi', 'is-alarm': item.alarm }"
              >
                <div class="qx-kpi-label">
                  <span>{{ item.label }}</span>
                  <span class="qx-tag">{{ item.tag }}</span>
                </div>
                <div class="qx-kpi-num" :style="{ color: item.color || '#eaf6ff' }">
                  <small v-if="item.prefix">{{ item.prefix }}</small>{{ item.value }}
                </div>
                <div class="qx-kpi-foot">
                  <span v-if="item.ratio" :class="item.ratioClass">{{ item.ratio }}</span>
                  <span class="qx-kpi-foot-dim">{{ item.foot }}</span>
                </div>
              </div>
            </div>

            <div class="qx-panel qx-panel-fill" :class="{ 'is-focus': focusKey === 'trend' }">
              <div class="qx-panel-head">
                <span class="qx-panel-title">销售趋势</span>
                <span class="qx-panel-sub">近 30 天</span>
              </div>
              <div ref="trendChart" class="qx-chart qx-chart-trend"></div>
            </div>
          </div>

          <!-- ---------- 中栏 ---------- -->
          <div class="qx-col qx-col-c">
            <div class="qx-panel qx-panel-fill" :class="{ 'is-focus': focusKey === 'orders' }">
              <div class="qx-panel-head">
                <span class="qx-panel-title">实时订单流</span>
                <span class="qx-panel-sub"><i class="qx-dot-live"></i>每 10 秒刷新 · 滚动轮播</span>
              </div>
              <div class="qx-order-head">
                <span class="qo-h-no">序号</span>
                <span class="qo-h-user">会员</span>
                <span class="qo-h-goods">商品</span>
                <span class="qo-h-right">金额 / 状态 / 时间</span>
              </div>
              <transition-group name="qx-row" tag="div" class="qx-order-list">
                <div
                  v-for="(o, i) in visibleOrders"
                  :key="o.orderId"
                  class="qx-order-row"
                  :class="{ 'is-new': o.isNew }"
                >
                  <div class="qo-top">
                    <span class="qo-no">{{ pad(i + 1) }}</span>
                    <span class="qo-user">{{ o.realName || o.nickname || '匿名用户' }}</span>
                    <span class="qo-right">
                      <span class="qo-amt">￥{{ o.payPrice }}</span>
                      <span class="qo-status" :style="statusStyle(o.statusKey)">{{ o.statusText }}</span>
                      <span class="qo-time">{{ o.timeShort }}</span>
                    </span>
                  </div>
                  <div class="qo-goods">{{ o.goodsName }}</div>
                </div>
              </transition-group>
              <div class="qx-empty" v-if="!visibleOrders.length">暂无订单数据</div>
            </div>

            <!-- 待办 + 完成情况 -->
            <div class="qx-todo" :class="{ 'is-focus': focusKey === 'todo' }">
              <div
                v-for="(t, i) in todoList"
                :key="i"
                class="qx-todo-card"
                :class="{ 'is-alarm': t.alarm }"
                :style="{ '--tc': t.color }"
              >
                <span class="qx-todo-label">{{ t.label }}</span>
                <span class="qx-todo-num">{{ t.value }}<small>{{ t.unit }}</small></span>
              </div>
              <div class="qx-progress">
                <div class="qx-progress-top">
                  <span>累计已完成订单</span>
                  <b>{{ int(orderStatus.complete || 0) }} 单</b>
                </div>
                <div class="qx-progress-rail">
                  <div class="qx-progress-fill" :style="{ width: completeRate + '%' }"></div>
                  <div class="qx-progress-head" :style="{ left: completeRate + '%' }"></div>
                </div>
                <div class="qx-progress-bottom">
                  <span>订单完成率</span>
                  <b>{{ completeRate }}%</b>
                </div>
              </div>
            </div>
          </div>

          <!-- ---------- 右栏 ---------- -->
          <div class="qx-col qx-col-r">
            <div
              class="qx-panel qx-panel-chart qx-panel-channel"
              :class="{ 'is-focus': focusKey === 'channel' }"
            >
              <div class="qx-panel-head">
                <span class="qx-panel-title">用户渠道分布</span>
                <span class="qx-panel-sub">累计 {{ int(userTotal.userNum || 0) }} 人</span>
              </div>
              <div class="qx-channel-wrap">
                <div ref="channelChart" class="qx-chart qx-chart-channel"></div>
                <div class="qx-radar-sweep"></div>
              </div>
            </div>

            <div class="qx-panel qx-panel-fill" :class="{ 'is-focus': focusKey === 'status' }">
              <div class="qx-panel-head">
                <span class="qx-panel-title">订单状态分布</span>
                <span class="qx-panel-sub">全部订单</span>
              </div>
              <div ref="statusChart" class="qx-chart qx-chart-status"></div>
            </div>
          </div>
        </section>

        <!-- ==================== 页脚业务板块 ==================== -->
        <footer class="qx-footer">
          <div
            v-for="(item, i) in moduleList"
            :key="i"
            class="qx-biz-card"
            :class="{ 'is-lit': bizLitIndex === i, 'is-focus': focusKey === 'biz' }"
            :style="{ '--bc': item.color }"
          >
            <span class="qx-biz-label">{{ item.label }}</span>
            <span class="qx-biz-num">{{ item.value }}<small v-if="item.unit">{{ item.unit }}</small></span>
            <span class="qx-biz-bar"></span>
          </div>
        </footer>
      </div>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts';
import screenfull from 'screenfull';
import { viewModelApi, chartOrder30Api, businessData, getBigdataScreenSettingApi } from '@/api/dashboard';
import { tradeDataApi, userTotalData, userChannelData } from '@/api/statistic';
import { orderListApi, orderStatusNumApi } from '@/api/order';
import { stockAgentListApi } from '@/api/stock';
import { agentListApi } from '@/api/daili';
import { pingouTeamListApi, pingouActivityListApi } from '@/api/pingou';
import { dividendRecordListApi } from '@/api/dividend';
import { mergeScreenTitles } from '../components/screenNames';

const CYAN = '#00e5ff';
const BLUE = '#2f7bff';
const GOLD = '#ffc84a';
const PURPLE = '#a06bff';
const GREEN = '#2fe0a2';
const RED = '#ff5c6c';

// LED 大屏设计尺寸，实际按屏幕等比缩放
const DESIGN_W = 1920;
const DESIGN_H = 1080;

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

const ORDER_SHOW = 6;

export default {
  name: 'MarketDataScreen',
  data() {
    return {
      loading: false,
      // 大屏大标题（页头齿轮可改）
      screenTitles: mergeScreenTitles(''),
      isFull: false,
      scale: 1,
      nowTime: '',
      nowDate: '',
      kpi: {},
      trade: {},
      biz: {},
      userTotal: {},
      orderStatus: {},
      orderStream: [],
      streamOffset: 0,
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
      // 动效相关
      numShown: { sales: 0, orderNum: 0, newUserNum: 0, memberNum: 0 },
      focusKey: '',
      bizLitIndex: -1,
    };
  },
  computed: {
    /* 顶部跑马灯 */
    tickerItems() {
      const t = this.trade || {};
      const u = this.userTotal || {};
      const b = this.biz || {};
      const k = this.kpi || {};
      return [
        { label: '本月销售额', value: '￥' + this.money(t.monthOrderSales), tone: 'cyan' },
        { label: '本月订单', value: this.int(t.monthOrderNum) + ' 单', tone: 'dim' },
        { label: '累计消费额', value: '￥' + this.money(u.consumptionAmount), tone: 'gold' },
        { label: '累计会员', value: this.int(u.userNum) + ' 人', tone: 'dim' },
        { label: '上架商品', value: this.int(b.onSaleProductNum) + ' 件', tone: 'dim' },
        { label: '库存预警', value: this.int(b.vigilanceInventoryNum) + ' 件', tone: Number(b.vigilanceInventoryNum || 0) > 0 ? 'red' : 'dim' },
        { label: '账户充值', value: '￥' + this.money(b.totalRechargeAmount), tone: 'green' },
        { label: '待审核', value: this.int(b.notAuditNum) + ' 条', tone: 'dim' },
        { label: '页面访问', value: this.int(k.pageviews) + ' 次', tone: 'dim' },
      ];
    },
    /* 今日销售额 hero 锚点（左栏顶部大字） */
    heroSales() {
      const k = this.kpi || {};
      return {
        value: this.money(this.numShown.sales),
        ratio: this.ratioText(k.sales, k.yesterdaySales),
        ratioClass: this.ratioClass(k.sales, k.yesterdaySales),
        foot: `昨日 ￥${this.money(k.yesterdaySales)}`,
      };
    },
    /* KPI 指标卡（2 张：支付订单 / 新增用户） */
    kpiList() {
      const k = this.kpi || {};
      return [
        {
          label: '支付订单量',
          tag: '今日',
          value: this.int(this.numShown.orderNum),
          ratio: this.ratioText(k.orderNum, k.yesterdayOrderNum),
          ratioClass: this.ratioClass(k.orderNum, k.yesterdayOrderNum),
          foot: `昨日 ${this.int(k.yesterdayOrderNum)} 单`,
        },
        {
          label: '新增用户',
          tag: '今日',
          value: this.int(this.numShown.newUserNum),
          ratio: this.ratioText(k.newUserNum, k.yesterdayNewUserNum),
          ratioClass: this.ratioClass(k.newUserNum, k.yesterdayNewUserNum),
          foot: `昨日 ${this.int(k.yesterdayNewUserNum)} 人`,
        },
      ];
    },
    /* 待办三色卡 */
    todoList() {
      const b = this.biz || {};
      return [
        { label: '待发货', value: this.int(b.notShippingOrderNum), unit: ' 单', color: GOLD },
        { label: '待核销', value: this.int(b.notWriteOffOrderNum), unit: ' 单', color: CYAN },
        {
          label: '退款中',
          value: this.int(b.refundingOrderNum),
          unit: ' 单',
          color: RED,
          alarm: Number(b.refundingOrderNum || 0) > 0,
        },
      ];
    },
    /* 订单完成率 */
    completeRate() {
      const d = this.orderStatus || {};
      const keys = ['unPaid', 'notShipped', 'toBeWrittenOff', 'complete', 'refunding'];
      const total = keys.reduce((s, k) => s + Number(d[k] || 0), 0);
      if (!total) return 0;
      return Math.min(100, Math.round((Number(d.complete || 0) / total) * 1000) / 10);
    },
    /* 业务板块（页脚 6 卡） */
    moduleList() {
      return [
        { label: '库存代理商', value: this.int(this.stockAgentCount), unit: ' 家', color: CYAN },
        { label: '区域代理', value: this.int(this.agentCount), unit: ' 家', color: BLUE },
        { label: '代理待审核', value: this.int(this.agentAudit), unit: ' 家', color: GOLD },
        { label: '拼购活动', value: this.int(this.pingouActivityCount), unit: ' 场', color: PURPLE },
        { label: '拼购团', value: this.int(this.pingouTeamCount), unit: ' 个', color: GREEN },
        {
          label: '上期分红基数',
          value: '￥' + this.money(this.dividend.baseAmount),
          color: GOLD,
        },
      ];
    },
    /* 订单流可视窗口（滚动轮播） */
    visibleOrders() {
      const list = this.orderStream || [];
      if (list.length <= ORDER_SHOW) return list;
      const span = list.length - ORDER_SHOW + 1;
      const off = this.streamOffset % span;
      return list.slice(off, off + ORDER_SHOW);
    },
  },
  watch: {
    kpi: {
      handler() {
        this.rollAll();
      },
      deep: true,
    },
    userTotal: {
      handler() {
        this.rollAll();
      },
      deep: true,
    },
  },
  created() {
    this.numRaf = {};
  },
  mounted() {
    this.fitScreen();
    this.tickClock();
    this.timers.push(setInterval(this.tickClock, 1000));
    this.loadAll();
    this.loadScreenTitles();
    // 实时订单流：10 秒刷新
    this.timers.push(setInterval(this.loadOrderStream, 10000));
    // 订单流滚动轮播：4 秒上移一条
    this.timers.push(
      setInterval(() => {
        this.streamOffset += 1;
      }, 4000)
    );
    // 统计类数据：30 秒
    this.timers.push(setInterval(this.loadStat, 30000));
    // 面板焦点轮播：依次点亮，引导视线
    this.startFocusLoop();
    // 页脚板块依次点亮：慢速一轮，弱引导不抢戏
    this.timers.push(
      setInterval(() => {
        this.bizLitIndex = (this.bizLitIndex + 1) % this.moduleList.length;
      }, 2600)
    );
    window.addEventListener('resize', this.onResize);
    document.addEventListener('fullscreenchange', this.onFullChange);
  },
  beforeDestroy() {
    this.timers.forEach((t) => clearInterval(t));
    Object.keys(this.numRaf || {}).forEach((k) => cancelAnimationFrame(this.numRaf[k]));
    window.removeEventListener('resize', this.onResize);
    document.removeEventListener('fullscreenchange', this.onFullChange);
    Object.keys(this.charts).forEach((k) => {
      if (this.charts[k]) this.charts[k].dispose();
    });
    this.charts = {};
  },
  methods: {
    /* ---------------- 大屏等比缩放 ---------------- */
    fitScreen() {
      const w = window.innerWidth || DESIGN_W;
      const h = window.innerHeight || DESIGN_H;
      this.scale = Math.min(w / DESIGN_W, h / DESIGN_H);
    },
    onResize() {
      this.fitScreen();
      this.$nextTick(() => this.resizeCharts());
    },

    /* ---------------- 面板焦点轮播 ---------------- */
    startFocusLoop() {
      const keys = ['kpi', 'trend', 'orders', 'todo', 'channel', 'status', 'biz'];
      let i = 0;
      this.focusKey = keys[0];
      this.timers.push(
        setInterval(() => {
          i = (i + 1) % keys.length;
          this.focusKey = keys[i];
        }, 5000)
      );
    },

    /* ---------------- 大屏大标题设置 ---------------- */
    loadScreenTitles() {
      getBigdataScreenSettingApi()
        .then((res) => {
          this.screenTitles = mergeScreenTitles(res);
        })
        .catch(() => {});
    },

    /* ---------------- 时钟 ---------------- */
    tickClock() {
      const d = new Date();
      const p = (n) => (n < 10 ? '0' + n : n);
      const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
      this.nowTime = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
      this.nowDate = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} 星期${week}`;
    },

    /* ---------------- 全屏 ---------------- */
    toggleFull() {
      if (!screenfull.isEnabled) return this.$message.warning('当前浏览器不支持全屏');
      screenfull.toggle(document.documentElement);
    },
    onFullChange() {
      this.isFull = !!(screenfull.isFullscreen || document.fullscreenElement);
      setTimeout(this.onResize, 300);
    },
    resizeCharts() {
      Object.keys(this.charts).forEach((k) => {
        if (this.charts[k]) this.charts[k].resize();
      });
    },

    /* ---------------- 数值工具 ---------------- */
    money(v) {
      const n = Number(v || 0);
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    int(v) {
      return Number(v || 0).toLocaleString('zh-CN');
    },
    pad(n) {
      return n < 10 ? '0' + n : String(n);
    },
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
    /* 数字滚动（countUp 缓动） */
    rollAll() {
      const k = this.kpi || {};
      this.rollTo('sales', k.sales);
      this.rollTo('orderNum', k.orderNum);
      this.rollTo('newUserNum', k.newUserNum);
      this.rollTo('memberNum', (this.userTotal || {}).userNum);
    },
    rollTo(key, target) {
      const to = Number(target || 0);
      const from = Number(this.numShown[key] || 0);
      if (from === to) return;
      if (this.numRaf[key]) cancelAnimationFrame(this.numRaf[key]);
      const start = performance.now();
      const dur = 1000;
      const tick = (now) => {
        const p = Math.min(1, (now - start) / dur);
        const e = 1 - Math.pow(1 - p, 3);
        this.$set(this.numShown, key, from + (to - from) * e);
        if (p < 1) {
          this.numRaf[key] = requestAnimationFrame(tick);
        } else {
          this.$set(this.numShown, key, to);
          this.numRaf[key] = null;
        }
      };
      this.numRaf[key] = requestAnimationFrame(tick);
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
          const map = { h5: 'H5 商城', ios: 'APP', routine: '小程序', wechat: '公众号' };
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
          this.orderStatus = res || {};
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
            const goods = (o.productList || [])
              .map((p) => p.productName || p.storeName || '')
              .filter(Boolean)
              .join('、');
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
        grid: { top: 58, left: 10, right: 14, bottom: 8, containLabel: true },
        legend: {
          data: ['销售额', '订单量'],
          right: 14,
          top: 10,
          itemWidth: 22,
          itemHeight: 4,
          itemGap: 22,
          textStyle: { color: '#8fa7c9', fontSize: 18, fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif' },
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(6,18,40,.94)',
          borderColor: 'rgba(0,229,255,.4)',
          borderWidth: 2,
          textStyle: { color: '#d9e8ff', fontSize: 16 },
        },
        xAxis: {
          type: 'category',
          data: xData,
          boundaryGap: true,
          axisLine: { lineStyle: { color: 'rgba(0,196,255,.35)', width: 2 } },
          axisTick: { show: false },
          axisLabel: {
            color: '#7fa3d8',
            fontSize: 16,
            interval: Math.max(0, Math.floor(xData.length / 6) - 1),
          },
        },
        yAxis: [
          {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { lineStyle: { color: 'rgba(0,196,255,.14)' } },
            axisLabel: { color: '#7fa3d8', fontSize: 16 },
          },
          {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { color: '#7fa3d8', fontSize: 16 },
          },
        ],
        series: [
          {
            name: '订单量',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 9,
            itemStyle: {
              borderRadius: [4, 4, 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255,200,74,.95)' },
                { offset: 1, color: 'rgba(255,200,74,.15)' },
              ]),
            },
            animationDelay: (idx) => idx * 40,
            data: qualityData,
          },
          {
            name: '销售额',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            showSymbol: false,
            lineStyle: { width: 4, color: CYAN, shadowColor: 'rgba(0,229,255,.85)', shadowBlur: 16 },
            itemStyle: { color: CYAN, borderWidth: 2 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0,229,255,.4)' },
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
          backgroundColor: 'rgba(6,18,40,.94)',
          borderColor: 'rgba(0,229,255,.4)',
          borderWidth: 2,
          textStyle: { color: '#d9e8ff', fontSize: 16 },
          formatter: '{b}：{c} 人（{d}%）',
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          itemWidth: 16,
          itemHeight: 16,
          icon: 'rect',
          itemGap: 20,
          textStyle: { color: '#c6dcff', fontSize: 18, fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif' },
          formatter: (name) => {
            const hit = data.find((d) => d.name === name);
            if (!hit || !total) return name;
            return `${name}  ${Math.round((hit.value / total) * 100)}%`;
          },
        },
        color: colors,
        series: [
          {
            type: 'pie',
            radius: ['54%', '76%'],
            center: ['32%', '50%'],
            avoidLabelOverlap: false,
            label: { show: false },
            labelLine: { show: false },
            itemStyle: { borderColor: 'rgba(6,18,40,.92)', borderWidth: 3 },
            animationType: 'scale',
            animationEasing: 'elasticOut',
            animationDuration: 1200,
            data,
          },
        ],
        graphic: [
          {
            type: 'text',
            left: '24%',
            top: '38%',
            style: {
              text: total.toLocaleString('zh-CN'),
              fill: '#eaf6ff',
              font: 'bold 34px DIN Alternate, Consolas, monospace',
              textAlign: 'center',
            },
          },
          {
            type: 'text',
            left: '26.4%',
            top: '57%',
            style: {
              text: '累计会员',
              fill: '#7fa3d8',
              font: '17px PingFang SC, Microsoft YaHei, sans-serif',
            },
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
        grid: { top: 14, left: 12, right: 76, bottom: 10, containLabel: true },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(6,18,40,.94)',
          borderColor: 'rgba(0,229,255,.4)',
          borderWidth: 2,
          textStyle: { color: '#d9e8ff', fontSize: 16 },
        },
        xAxis: {
          type: 'value',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: 'rgba(0,196,255,.14)' } },
          axisLabel: { color: '#7fa3d8', fontSize: 16 },
        },
        yAxis: {
          type: 'category',
          data: cats,
          inverse: true,
          axisLine: { lineStyle: { color: 'rgba(0,196,255,.35)', width: 2 } },
          axisTick: { show: false },
          axisLabel: { color: '#c6dcff', fontSize: 18, fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif' },
        },
        series: [
          {
            type: 'bar',
            barWidth: 24,
            showBackground: true,
            backgroundStyle: { color: 'rgba(20,44,88,.55)', borderRadius: 12 },
            label: {
              show: true,
              position: 'right',
              color: '#eaf6ff',
              fontSize: 18,
              fontFamily: 'DIN Alternate, Consolas, monospace',
            },
            itemStyle: {
              borderRadius: [0, 12, 12, 0],
              color: (p) => conf[p.dataIndex].color,
              shadowBlur: 12,
              shadowColor: 'rgba(0,229,255,.25)',
            },
            animationDelay: (idx) => idx * 90,
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
$blue: #2f7bff;
$gold: #ffc84a;
$purple: #a06bff;
$green: #2fe0a2;
$red: #ff5c6c;
$text: #eaf6ff;
$dim: #8fa7c9;
$faint: #4a6a9c;
$panel-bg: rgba(11, 27, 58, 0.78);
$panel-border: rgba(0, 196, 255, 0.3);
$font: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
$num-font: 'DIN Alternate', 'Bebas Neue', Consolas, 'Courier New', monospace;

/* ==================== 舞台（等比缩放） ==================== */
.qx-stage {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #03060f;
}
.qx-scaler {
  flex: none;
  width: 1920px;
  height: 1080px;
  transform-origin: center center;
}
.qx-screen {
  position: relative;
  width: 1920px;
  height: 1080px;
  padding: 0 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  color: $text;
  font-family: $font;
  overflow: hidden;
  background: radial-gradient(1100px 520px at 12% -8%, rgba(47, 123, 255, 0.22), transparent 62%),
    radial-gradient(900px 460px at 88% -6%, rgba(0, 229, 255, 0.14), transparent 58%),
    linear-gradient(180deg, #061024 0%, #040a18 100%);
  /* 防烧屏微移：5 分钟一轮，每次 1–2px */
  animation: qxDrift 300s ease-in-out infinite;
}
@keyframes qxDrift {
  0%,
  100% {
    transform: translate(0, 0);
  }
  20% {
    transform: translate(2px, 1px);
  }
  40% {
    transform: translate(-1px, 2px);
  }
  60% {
    transform: translate(-2px, -1px);
  }
  80% {
    transform: translate(1px, -2px);
  }
}
.qx-bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(0, 196, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 196, 255, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
}
.qx-glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(4px);
}
.qx-glow.g1 {
  left: -240px;
  top: -340px;
  width: 1180px;
  height: 900px;
  background: radial-gradient(circle, rgba(47, 123, 255, 0.34), transparent 70%);
  animation: qxBreath 9s ease-in-out infinite;
}
.qx-glow.g2 {
  right: -260px;
  top: -380px;
  width: 1100px;
  height: 860px;
  background: radial-gradient(circle, rgba(0, 229, 255, 0.24), transparent 70%);
  animation: qxBreath 11s ease-in-out infinite reverse;
}
.qx-glow.g3 {
  left: 460px;
  bottom: -400px;
  width: 1050px;
  height: 800px;
  background: radial-gradient(circle, rgba(160, 107, 255, 0.18), transparent 70%);
  animation: qxBreath 13s ease-in-out infinite;
}
@keyframes qxBreath {
  50% {
    opacity: 0.55;
  }
}

/* ==================== 顶栏 ==================== */
.qx-header {
  position: relative;
  z-index: 2;
  flex: none;
  height: 98px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.qx-brand {
  display: flex;
  align-items: center;
  gap: 18px;
  .qx-logo {
    width: 56px;
    height: 56px;
    border-radius: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: $num-font;
    font-weight: 700;
    font-size: 23px;
    color: #031024;
    background: linear-gradient(135deg, #00e5ff, #2f7bff);
    box-shadow: 0 0 22px rgba(0, 229, 255, 0.55);
    animation: qxLogoPulse 3s ease-in-out infinite;
  }
  h1 {
    margin: 0;
    font-size: 38px;
    font-weight: 700;
    letter-spacing: 6px;
    background: linear-gradient(90deg, #eaf6ff 20%, #7fe3ff 90%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    text-shadow: 0 0 30px rgba(0, 229, 255, 0.25);
  }
  p {
    margin: 8px 0 0;
    font-size: 13px;
    letter-spacing: 4px;
    color: $faint;
  }
}
@keyframes qxLogoPulse {
  0%,
  100% {
    box-shadow: 0 0 18px rgba(0, 229, 255, 0.45);
  }
  50% {
    box-shadow: 0 0 30px rgba(0, 229, 255, 0.8);
  }
}
/* 装饰翼 */
.qx-wing {
  position: absolute;
  left: 50%;
  bottom: 6px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  width: 520px;
  .wg-line {
    flex: 1;
    height: 2px;
    background: linear-gradient(90deg, transparent, #2e5aa8);
    &:last-child {
      background: linear-gradient(270deg, transparent, #2e5aa8);
    }
  }
  .wg-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: $cyan;
    box-shadow: 0 0 12px $cyan;
    animation: qxPulse 2s ease-in-out infinite;
  }
  .wg-diamond {
    width: 15px;
    height: 15px;
    background: $cyan;
    transform: rotate(45deg);
    box-shadow: 0 0 16px rgba(0, 229, 255, 0.85);
    animation: qxDiamond 3s ease-in-out infinite;
  }
}
@keyframes qxDiamond {
  0%,
  100% {
    transform: rotate(45deg) scale(1);
    opacity: 0.85;
  }
  50% {
    transform: rotate(225deg) scale(1.18);
    opacity: 1;
  }
}
.qx-header-right {
  display: flex;
  align-items: center;
  gap: 30px;
  z-index: 2;
}
.qx-live {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 9px 18px;
  border-radius: 99px;
  border: 2px solid rgba(47, 224, 162, 0.45);
  background: rgba(47, 224, 162, 0.12);
  font-size: 17px;
  font-weight: 500;
  color: $green;
  i {
    width: 11px;
    height: 11px;
    border-radius: 50%;
    background: $green;
    box-shadow: 0 0 12px $green;
    animation: qxRipple 2s ease-out infinite;
  }
}
@keyframes qxRipple {
  0% {
    box-shadow: 0 0 0 0 rgba(47, 224, 162, 0.7);
  }
  100% {
    box-shadow: 0 0 0 14px rgba(47, 224, 162, 0);
  }
}
.qx-clock-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
  .qx-clock {
    font-family: $num-font;
    font-size: 34px;
    font-weight: 600;
    color: $cyan;
    letter-spacing: 2px;
    text-shadow: 0 0 18px rgba(0, 229, 255, 0.55);
  }
  .qx-date {
    font-size: 15px;
    color: #5d7dad;
    letter-spacing: 1px;
  }
}
.qx-fullscreen {
  cursor: pointer;
  font-size: 22px;
  color: $cyan;
  opacity: 0.35;
  transition: opacity 0.2s, text-shadow 0.2s;
  &:hover {
    opacity: 1;
    text-shadow: 0 0 14px rgba(0, 229, 255, 0.9);
  }
}

/* ==================== 跑马灯 ==================== */
.qx-ticker {
  position: relative;
  z-index: 2;
  flex: none;
  height: 52px;
  margin-bottom: 0;
  overflow: hidden;
  border: 2px solid rgba(0, 229, 255, 0.22);
  border-radius: 8px;
  background: rgba(5, 15, 35, 0.88);
  display: flex;
  align-items: center;
  box-shadow: inset 0 0 34px rgba(0, 229, 255, 0.05);
}
.qx-ticker-track {
  display: flex;
  align-items: center;
  width: max-content;
  animation: qxMarquee 38s linear infinite;
}
@keyframes qxMarquee {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}
.qx-ticker-item {
  flex: none;
  padding: 0 44px;
  font-size: 22px;
  color: $dim;
  white-space: nowrap;
  position: relative;
  &::after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    width: 2px;
    height: 16px;
    margin-top: -8px;
    background: rgba(0, 229, 255, 0.28);
  }
  b {
    margin-left: 12px;
    font-family: $num-font;
    font-size: 26px;
    font-weight: 700;
    color: $cyan;
    text-shadow: 0 0 14px rgba(0, 229, 255, 0.35);
  }
  &.tone-gold b {
    color: $gold;
    text-shadow: 0 0 14px rgba(255, 200, 74, 0.4);
  }
  &.tone-green b {
    color: $green;
    text-shadow: 0 0 14px rgba(47, 224, 162, 0.4);
  }
  &.tone-red b {
    color: $red;
    text-shadow: 0 0 14px rgba(255, 92, 108, 0.45);
  }
}

/* ==================== 主体三栏 ==================== */
.qx-main {
  position: relative;
  z-index: 2;
  flex: none;
  height: 770px;
  display: flex;
  gap: 20px;
  padding: 20px 0;
  box-sizing: border-box;
}
.qx-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}
.qx-col-l {
  flex: none;
  width: 430px;
}
.qx-col-c {
  flex: 1;
  min-width: 0;
}
.qx-col-r {
  flex: none;
  width: 450px;
}

/* ---------- 面板通用 ---------- */
.qx-panel {
  position: relative;
  overflow: hidden;
  padding: 16px 20px 18px;
  border: 2px solid $panel-border;
  border-radius: 14px;
  background: $panel-bg;
  box-shadow: 0 0 24px rgba(0, 196, 255, 0.09), inset 0 0 40px rgba(0, 196, 255, 0.035);
  transition: border-color 0.6s ease, box-shadow 0.6s ease;
  display: flex;
  flex-direction: column;
  /* 边框追光：顶部高光扫过 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 34%;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.95), transparent);
    animation: qxSheen 4.5s linear infinite;
    pointer-events: none;
  }
}
@keyframes qxSheen {
  from {
    transform: translateX(-110%);
  }
  to {
    transform: translateX(400%);
  }
}
.qx-panel.is-focus,
.qx-kpi.is-focus,
.qx-todo.is-focus,
.qx-biz-card.is-focus {
  border-color: rgba(0, 229, 255, 0.8);
  box-shadow: 0 0 34px rgba(0, 229, 255, 0.3), inset 0 0 46px rgba(0, 229, 255, 0.08);
}
.qx-panel-fill {
  flex: 1;
  min-height: 0;
}
.qx-panel-head {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.qx-panel-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
  color: $text;
  &::before {
    content: '';
    width: 6px;
    height: 28px;
    border-radius: 3px;
    background: linear-gradient(180deg, $cyan, $blue);
    box-shadow: 0 0 12px rgba(0, 229, 255, 0.7);
  }
}
.qx-panel-sub {
  font-size: 18px;
  color: $faint;
}
.qx-dot-live {
  display: inline-block;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: $green;
  box-shadow: 0 0 10px $green;
  margin-right: 8px;
  animation: qxRipple 2s ease-out infinite;
}
@keyframes qxPulse {
  50% {
    opacity: 0.3;
  }
}
.qx-chart {
  width: 100%;
  flex: 1;
  min-height: 0;
}
.qx-chart-trend {
  min-height: 0;
}
.qx-chart-channel {
  min-height: 0;
}
.qx-chart-status {
  min-height: 0;
}

/* ---------- 今日销售额 hero 锚点 ---------- */
.qx-hero {
  position: relative;
  overflow: hidden;
  flex: none;
  padding: 18px 22px 16px;
  border: 2px solid $panel-border;
  border-radius: 14px;
  background: linear-gradient(120deg, rgba(0, 120, 220, 0.3), rgba(11, 27, 58, 0.85));
  box-shadow: 0 0 24px rgba(0, 196, 255, 0.1), inset 0 0 40px rgba(0, 196, 255, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  transition: border-color 0.6s ease, box-shadow 0.6s ease;
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 40%;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.95), transparent);
    animation: qxSheen 4.5s linear infinite;
  }
  &.is-focus {
    border-color: rgba(0, 229, 255, 0.8);
    box-shadow: 0 0 34px rgba(0, 229, 255, 0.3), inset 0 0 46px rgba(0, 229, 255, 0.08);
  }
}
.qx-hero-label {
  font-size: 19px;
  color: $dim;
  display: flex;
  align-items: center;
  gap: 10px;
}
.qx-hero-num {
  font-family: $num-font;
  font-size: 54px;
  font-weight: 800;
  line-height: 1.05;
  background: linear-gradient(180deg, #ffffff 35%, #8fd0ff 90%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  white-space: nowrap;
  text-shadow: 0 0 26px rgba(0, 229, 255, 0.3);
}
.qx-hero-foot {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 16px;
  .is-up {
    color: $red;
    font-weight: 600;
  }
  .is-down {
    color: $green;
    font-weight: 600;
  }
}
.qx-hero-dim {
  color: $faint;
}

/* ---------- KPI 卡（2 张） ---------- */
.qx-kpi-grid {
  flex: none;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.qx-kpi {
  position: relative;
  overflow: hidden;
  padding: 16px 18px 14px;
  height: 168px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 2px solid $panel-border;
  border-radius: 14px;
  background: $panel-bg;
  box-shadow: 0 0 22px rgba(0, 196, 255, 0.09), inset 0 0 34px rgba(0, 196, 255, 0.035);
  transition: border-color 0.6s ease, box-shadow 0.6s ease;
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 40%;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.95), transparent);
    animation: qxSheen 4.5s linear infinite;
  }
  &.is-alarm::after {
    content: '';
    position: absolute;
    right: 12px;
    top: 12px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: $red;
    box-shadow: 0 0 12px $red;
    animation: qxRipple 2.5s ease-out infinite;
  }
}
.qx-kpi-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 19px;
  color: $dim;
  white-space: nowrap;
}
.qx-tag {
  flex: none;
  font-size: 15px;
  color: $cyan;
  border: 2px solid rgba(0, 229, 255, 0.42);
  border-radius: 4px;
  padding: 1px 8px;
}
.qx-kpi-num {
  font-family: $num-font;
  font-size: 38px;
  font-weight: 700;
  line-height: 1.05;
  color: $text;
  white-space: nowrap;
  text-shadow: 0 0 18px rgba(0, 229, 255, 0.3);
  small {
    font-size: 20px;
    font-weight: 500;
    margin-right: 2px;
  }
}
.qx-kpi-foot {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  .is-up {
    color: $red;
    font-weight: 600;
  }
  .is-down {
    color: $green;
    font-weight: 600;
  }
  .qx-kpi-foot-dim {
    color: $faint;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

/* ---------- 实时订单流 ---------- */
.qx-order-head {
  flex: none;
  display: flex;
  align-items: center;
  height: 42px;
  padding: 0 18px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: rgba(20, 44, 88, 0.5);
  font-size: 17px;
  color: $faint;
  letter-spacing: 1px;
  .qo-h-no {
    width: 56px;
  }
  .qo-h-user {
    width: 210px;
  }
  .qo-h-goods {
    flex: 1;
  }
  .qo-h-right {
    width: 400px;
    text-align: right;
  }
}
.qx-order-list {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}
.qx-order-row {
  flex: none;
  padding: 8px 18px;
  border: 2px solid rgba(0, 196, 255, 0.16);
  border-radius: 12px;
  background: linear-gradient(90deg, rgba(20, 44, 88, 0.5), rgba(20, 44, 88, 0.28));
  &.is-new {
    animation: qxFlash 2.6s ease-out;
  }
  .qo-top {
    display: flex;
    align-items: center;
    height: 28px;
  }
  .qo-no {
    width: 56px;
    font-family: $num-font;
    font-size: 20px;
    font-weight: 700;
    color: $faint;
  }
  .qo-user {
    width: 210px;
    font-size: 22px;
    font-weight: 600;
    color: $text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    padding-right: 12px;
  }
  .qo-right {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 26px;
    .qo-amt {
      min-width: 150px;
      text-align: right;
      font-family: $num-font;
      font-size: 26px;
      font-weight: 700;
      color: $gold;
      text-shadow: 0 0 14px rgba(255, 200, 74, 0.32);
    }
    .qo-status {
      width: 92px;
      text-align: center;
      font-size: 18px;
      font-weight: 600;
      padding: 2px 0;
      border: 2px solid;
      border-radius: 5px;
    }
    .qo-time {
      width: 110px;
      text-align: right;
      font-family: $num-font;
      font-size: 20px;
      color: $faint;
    }
  }
  .qo-goods {
    margin-top: 4px;
    font-size: 20px;
    line-height: 26px;
    color: #9db6d8;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
@keyframes qxFlash {
  0% {
    background: rgba(0, 229, 255, 0.3);
    border-color: rgba(0, 229, 255, 0.9);
    transform: translateX(-6px);
  }
  100% {
    background: linear-gradient(90deg, rgba(20, 44, 88, 0.5), rgba(20, 44, 88, 0.28));
    transform: translateX(0);
  }
}
/* 订单行滚动轮播过渡 */
.qx-row-enter-active {
  transition: all 0.5s ease;
}
.qx-row-leave-active {
  transition: all 0.4s ease;
  position: absolute;
  left: 0;
  right: 0;
}
.qx-row-enter {
  opacity: 0;
  transform: translateY(18px);
}
.qx-row-leave-to {
  opacity: 0;
  transform: translateY(-18px);
}
.qx-row-move {
  transition: transform 0.5s ease;
}
.qx-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $faint;
  font-size: 20px;
}

/* ---------- 待办 + 完成情况 ---------- */
.qx-todo {
  flex: none;
  height: 118px;
  display: flex;
  gap: 16px;
  padding: 14px 18px;
  box-sizing: border-box;
  border: 2px solid $panel-border;
  border-radius: 14px;
  background: $panel-bg;
  box-shadow: 0 0 22px rgba(0, 196, 255, 0.09), inset 0 0 34px rgba(0, 196, 255, 0.035);
  transition: border-color 0.6s ease, box-shadow 0.6s ease;
}
.qx-todo-card {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 0 18px;
  border: 2px solid var(--tc);
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.22);
  position: relative;
  overflow: hidden;
  &.is-alarm {
    animation: qxAlarm 2.5s ease-out infinite;
  }
  .qx-todo-label {
    font-size: 18px;
    color: var(--tc);
    opacity: 0.9;
    white-space: nowrap;
  }
  .qx-todo-num {
    font-family: $num-font;
    font-size: 32px;
    font-weight: 700;
    color: var(--tc);
    white-space: nowrap;
    text-shadow: 0 0 16px rgba(0, 229, 255, 0.22);
    small {
      font-size: 17px;
      font-weight: 500;
      margin-left: 4px;
      opacity: 0.85;
    }
  }
}
@keyframes qxAlarm {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 92, 108, 0.45);
  }
  100% {
    box-shadow: 0 0 0 12px rgba(255, 92, 108, 0);
  }
}
.qx-progress {
  flex: 1.5;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 0 18px;
  border-radius: 12px;
  border: 2px solid rgba(0, 196, 255, 0.18);
  background: rgba(0, 0, 0, 0.22);
  .qx-progress-top {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    font-size: 18px;
    color: $dim;
    b {
      font-family: $num-font;
      font-size: 24px;
      color: $text;
    }
  }
  .qx-progress-rail {
    position: relative;
    height: 12px;
    border-radius: 6px;
    background: rgba(20, 44, 88, 0.85);
    overflow: visible;
  }
  .qx-progress-fill {
    height: 100%;
    border-radius: 6px;
    background: linear-gradient(90deg, $cyan, $blue);
    box-shadow: 0 0 16px rgba(0, 229, 255, 0.6);
    transition: width 1.2s cubic-bezier(0.22, 1, 0.36, 1);
  }
  .qx-progress-head {
    position: absolute;
    top: 50%;
    width: 18px;
    height: 18px;
    margin: -9px 0 0 -9px;
    border-radius: 50%;
    background: $cyan;
    box-shadow: 0 0 20px rgba(0, 229, 255, 0.95);
    transition: left 1.2s cubic-bezier(0.22, 1, 0.36, 1);
  }
  .qx-progress-bottom {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    font-size: 17px;
    color: $faint;
    b {
      font-family: $num-font;
      font-size: 22px;
      color: $cyan;
    }
  }
}

/* ---------- 渠道环形图 + 雷达扫掠 ---------- */
.qx-panel-channel {
  flex: none;
  height: 390px;
}
.qx-channel-wrap {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
}
.qx-radar-sweep {
  position: absolute;
  left: 32%;
  top: 50%;
  width: 310px;
  height: 310px;
  margin: -155px 0 0 -155px;
  border-radius: 50%;
  pointer-events: none;
  background: conic-gradient(from 0deg, rgba(0, 229, 255, 0.42) 0deg, rgba(0, 229, 255, 0.06) 26deg, transparent 60deg, transparent 360deg);
  -webkit-mask: radial-gradient(circle, transparent 53%, #000 56%, #000 75%, transparent 78%);
  mask: radial-gradient(circle, transparent 53%, #000 56%, #000 75%, transparent 78%);
  animation: qxSweep 4s linear infinite;
  mix-blend-mode: screen;
}
@keyframes qxSweep {
  to {
    transform: rotate(360deg);
  }
}

/* ==================== 页脚业务板块 ==================== */
.qx-footer {
  position: relative;
  z-index: 2;
  flex: none;
  height: 160px;
  display: flex;
  gap: 20px;
}
.qx-biz-card {
  position: relative;
  overflow: hidden;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 0 22px;
  border: 2px solid $panel-border;
  border-radius: 14px;
  background: $panel-bg;
  box-shadow: 0 0 22px rgba(0, 196, 255, 0.09), inset 0 0 34px rgba(0, 196, 255, 0.035);
  transition: border-color 0.5s ease, box-shadow 0.5s ease, background 0.5s ease;
  .qx-biz-label {
    font-size: 22px;
    color: $dim;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .qx-biz-num {
    font-family: $num-font;
    font-size: 38px;
    font-weight: 700;
    color: var(--bc);
    white-space: nowrap;
    text-shadow: 0 0 18px rgba(0, 229, 255, 0.25);
    small {
      font-size: 20px;
      font-weight: 500;
      margin-left: 6px;
      opacity: 0.9;
    }
  }
  .qx-biz-bar {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 3px;
    background: var(--bc);
    opacity: 0.25;
    transform: scaleX(0);
    transform-origin: left center;
    transition: transform 0.6s ease, opacity 0.6s ease;
  }
  /* 板块追光：依次点亮（收敛版：弱高亮、不抢主体） */
  &.is-lit {
    border-color: var(--bc);
    background: rgba(11, 27, 58, 0.9);
    box-shadow: 0 0 18px rgba(0, 229, 255, 0.12);
    .qx-biz-bar {
      transform: scaleX(1);
      opacity: 0.7;
    }
  }
}
</style>
