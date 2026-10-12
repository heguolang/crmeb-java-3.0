<template>
  <div class="bigdata-hub">
    <!-- 深空多层底 -->
    <div class="hub-bg-grid"></div>
    <svg class="hub-bg-links" preserveAspectRatio="none" viewBox="0 0 1440 900">
      <defs>
        <linearGradient id="hublg" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stop-color="#00e5ff" stop-opacity="0" />
          <stop offset="0.5" stop-color="#2f7bff" />
          <stop offset="1" stop-color="#00e5ff" stop-opacity="0" />
        </linearGradient>
      </defs>
      <path class="flow" d="M0 180 C360 120 720 260 1440 160" stroke="url(#hublg)" />
      <path class="flow b" d="M0 620 C420 720 900 540 1440 660" stroke="url(#hublg)" />
      <path class="flow" d="M120 0 C240 300 60 600 260 900" stroke="url(#hublg)" opacity="0.28" />
      <path class="flow b" d="M1200 0 C1080 320 1360 620 1160 900" stroke="url(#hublg)" opacity="0.28" />
    </svg>

    <!-- 页头 -->
    <div class="hub-hd">
      <div class="hub-hd-left">
        <div class="hub-eyebrow"><span class="bar"></span>DATA COMMAND CENTER</div>
        <h2>大数据中心</h2>
        <p>运营数据 / 市场数据统一入口。点击卡片即可预览大屏实时效果，全屏投屏挂墙即用。</p>
      </div>
      <div class="hub-hd-right">
        <span class="hub-pill"><i class="dot"></i>数据源在线</span>
        <span class="hub-pill hub-clock">{{ clock }}</span>
      </div>
    </div>

    <!-- 大屏卡片 -->
    <div class="hub-cards">
      <div
        v-for="s in screens"
        :key="s.key"
        class="hub-card"
        @click="openScreen(s)"
      >
        <span class="hub-badge">{{ s.screen }}</span>

        <!-- 仿真大屏预览（按真实布局绘制 + 扫描/滚动/旋转动效） -->
        <div class="hub-thumb" :class="'thumb-' + s.key">
          <div class="prev-grid"></div>
          <div class="prev-scan"></div>
          <div class="prev-hd">
            <span class="wing"></span>
            <span class="prev-title">{{ titles[s.key] }}</span>
            <span class="wing flip"></span>
          </div>

          <!-- 运营大数据：左 KPI/趋势 + 中订单流 + 右环形/柱状，底部模块条 -->
          <div v-if="s.key === 'operation'" class="prev-body">
            <div class="tb-ticker"></div>
            <div class="thumb-main">
              <div class="tb-col">
                <div class="tb-kpi4"><i></i><i></i><i></i><i></i></div>
                <div class="tb-panel trend"></div>
              </div>
              <div class="tb-col main">
                <div class="tb-rows"><i></i><i></i><i></i><i></i><i></i></div>
              </div>
              <div class="tb-col">
                <div class="tb-ring"><span></span></div>
                <div class="tb-panel bars"><i></i><i></i><i></i><i></i><i></i></div>
              </div>
            </div>
            <div class="tb-foot"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div>
          </div>

          <!-- 市场大数据：左柱状 + 右数据块，底部模块条 -->
          <div v-else class="prev-body">
            <div class="thumb-main">
              <div class="tb-col wide">
                <div class="tb-panel bars tall"><i></i><i></i><i></i><i></i><i></i><i></i></div>
              </div>
              <div class="tb-col stat-col">
                <div class="tb-stat">
                  <span class="tb-stat-label">今日交易额</span>
                  <span class="tb-stat-bar w2"></span>
                </div>
                <div class="tb-stat">
                  <span class="tb-stat-label">优秀团队</span>
                  <span class="tb-stat-bar w1"></span>
                </div>
              </div>
            </div>
            <div class="tb-foot"><i></i><i></i><i></i><i></i><i></i></div>
          </div>

          <!-- 悬浮：打开大屏 -->
          <div class="thumb-hover">
            <span class="open-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="#03121f" stroke-width="2"><path d="M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5" /></svg>
              打开大屏
            </span>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="hub-meta">
          <div class="hub-name">
            <span class="nm">{{ s.name }}</span>
            <span class="hub-live"><i></i>LIVE</span>
          </div>
          <p class="hub-desc">{{ s.desc }}</p>
          <div class="hub-facts">
            <span v-for="(f, i) in s.facts" :key="i" class="hub-fact">
              <i :class="f.icon"></i>{{ f.text }}
            </span>
          </div>
          <div class="hub-foot">
            <button class="btn-open" @click.stop="openScreen(s)">打开大屏</button>
            <span class="hub-foot-tip">{{ s.tip }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 展示名称设置（玻璃面板，可收起） -->
    <div class="hub-setting">
      <div class="hs-header" @click="settingCollapsed = !settingCollapsed">
        <div class="hs-title-wrap">
          <span class="hs-ic">
            <svg viewBox="0 0 24 24" fill="none" stroke="#00e5ff" stroke-width="2"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-2.7 1.1V21a2 2 0 0 1-4 0v-.1A1.6 1.6 0 0 0 7 19.4a1.6 1.6 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.6 1.6 0 0 0-1.1-2.7H1a2 2 0 0 1 0-4h.1A1.6 1.6 0 0 0 2.6 7a1.6 1.6 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1A1.6 1.6 0 0 0 7 2.6h.1A1.6 1.6 0 0 0 9 1.1V1a2 2 0 0 1 4 0v.1A1.6 1.6 0 0 0 15 2.6" /></svg>
          </span>
          <span class="hs-title">大屏展示名称设置</span>
          <span class="hs-sub">设置大屏顶部展示标题，留空保存即恢复默认</span>
        </div>
        <span class="hs-toggle">
          {{ settingCollapsed ? '展开' : '收起' }}
          <i :class="settingCollapsed ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"></i>
        </span>
      </div>

      <div v-show="!settingCollapsed" class="hs-body">
        <div class="hs-form">
          <div class="hs-field">
            <span class="hs-label">运营大屏标题</span>
            <div class="hs-inp">
              <input v-model="form.operation" :placeholder="'默认：' + defaults.operation" maxlength="30" />
              <span class="hs-cnt">{{ (form.operation || '').length }}/30</span>
            </div>
          </div>
          <div class="hs-field">
            <span class="hs-label">市场大屏标题</span>
            <div class="hs-inp">
              <input v-model="form.market" :placeholder="'默认：' + defaults.market" maxlength="30" />
              <span class="hs-cnt">{{ (form.market || '').length }}/30</span>
            </div>
          </div>
        </div>
        <div class="hs-actions">
          <button class="btn-primary" :disabled="saving" @click="save">保 存</button>
          <button class="btn-ghost" @click="reset">恢复默认</button>
          <span class="hs-tip">保存后大屏刷新页面即可看到新标题。</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getBigdataScreenSettingApi, saveBigdataScreenSettingApi } from '@/api/dashboard';
import { SCREEN_TITLES_DEFAULT, mergeScreenTitles } from '../components/screenNames';

export default {
  name: 'MarketScreenHub',
  data() {
    return {
      saving: false,
      settingCollapsed: true,
      clock: '',
      clockTimer: null,
      defaults: { ...SCREEN_TITLES_DEFAULT },
      titles: { ...SCREEN_TITLES_DEFAULT },
      form: {
        operation: SCREEN_TITLES_DEFAULT.operation,
        market: SCREEN_TITLES_DEFAULT.market,
      },
      screens: [
        {
          key: 'operation',
          name: '运营大数据',
          screen: '指挥中心',
          desc: '交易 / 会员 / 分销核心指标总览，跑马灯 + 实时订单流 + 趋势环形，适合公司大屏、会议室投屏。',
          facts: [
            { icon: 'el-icon-full-screen', text: '新标签页全屏打开' },
            { icon: 'el-icon-monitor', text: '适合会议室投屏' },
            { icon: 'el-icon-data-line', text: '数据实时刷新' },
          ],
          tip: '新窗口全屏 · 支持投屏',
          path: '/market/screen/operation',
        },
        {
          key: 'market',
          name: '市场大数据',
          screen: '市场中心',
          desc: '今日交易、全国会员分布、最近下单与优秀团队榜，柱状 + 地图 + 榜单，适合市场部门挂屏激励。',
          facts: [
            { icon: 'el-icon-full-screen', text: '新标签页全屏打开' },
            { icon: 'el-icon-monitor', text: '适合市场部门挂屏' },
            { icon: 'el-icon-trophy', text: '优秀团队榜' },
          ],
          tip: '新窗口全屏 · 支持投屏',
          path: '/market/screen/market',
        },
      ],
    };
  },
  mounted() {
    this.load();
    this.tickClock();
    this.clockTimer = setInterval(this.tickClock, 1000);
  },
  beforeDestroy() {
    if (this.clockTimer) clearInterval(this.clockTimer);
  },
  methods: {
    tickClock() {
      const d = new Date();
      const p = (n) => (n < 10 ? '0' + n : n);
      this.clock = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
    },
    load() {
      getBigdataScreenSettingApi()
        .then((res) => {
          const t = mergeScreenTitles(res);
          this.titles = t;
          this.form.operation = t.operation;
          this.form.market = t.market;
        })
        .catch(() => {});
    },
    // 新标签页打开大屏（大屏页面 fixed 全视口铺满，独立标签即全屏投屏）
    openScreen(s) {
      const href = this.$router.resolve(s.path).href;
      window.open(href, '_blank');
    },
    save() {
      this.saving = true;
      saveBigdataScreenSettingApi(JSON.stringify(this.form))
        .then(() => {
          this.titles = mergeScreenTitles(JSON.stringify(this.form));
          this.$message.success('保存成功，大屏刷新后生效');
        })
        .finally(() => {
          this.saving = false;
        });
    },
    reset() {
      this.form.operation = '';
      this.form.market = '';
      this.save();
    },
  },
};
</script>

<style scoped>
.bigdata-hub {
  position: relative;
  padding: 26px 30px 60px;
  min-height: calc(100vh - 100px);
  box-sizing: border-box;
  overflow: hidden;
  color: #e8f1ff;
  background:
    radial-gradient(1200px 620px at 14% -10%, rgba(47, 123, 255, 0.22), transparent 60%),
    radial-gradient(1000px 520px at 90% -6%, rgba(0, 229, 255, 0.14), transparent 56%),
    radial-gradient(900px 700px at 60% 120%, rgba(120, 90, 220, 0.12), transparent 60%),
    linear-gradient(180deg, #08152e 0%, #04070f 100%);
}
.hub-bg-grid {
  position: absolute;
  inset: -2px;
  z-index: 0;
  pointer-events: none;
  opacity: 0.5;
  background-image: linear-gradient(rgba(0, 196, 255, 0.055) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 196, 255, 0.055) 1px, transparent 1px);
  background-size: 46px 46px;
  -webkit-mask-image: radial-gradient(120% 90% at 50% 0%, #000 30%, transparent 78%);
  mask-image: radial-gradient(120% 90% at 50% 0%, #000 30%, transparent 78%);
}
.hub-bg-links {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.hub-bg-links path {
  fill: none;
  stroke-width: 1.4;
  opacity: 0.5;
}
.flow {
  stroke-dasharray: 5 16;
  animation: flow 5.5s linear infinite;
}
.flow.b {
  animation-duration: 7.5s;
}
@keyframes flow {
  to {
    stroke-dashoffset: -420;
  }
}

/* ---------- 页头 ---------- */
.hub-hd {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 26px;
}
.hub-hd-left {
  min-width: 0;
}
.hub-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  letter-spacing: 3px;
  color: #00e5ff;
  text-transform: uppercase;
  margin-bottom: 10px;
}
.hub-eyebrow .bar {
  width: 26px;
  height: 1px;
  background: linear-gradient(90deg, #00e5ff, transparent);
}
.hub-hd h2 {
  margin: 0 0 10px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(120deg, #ffffff 10%, #9fd0ff 60%, #4a9df8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(47, 123, 255, 0.25);
}
.hub-hd p {
  margin: 0;
  font-size: 13.5px;
  color: #8fa6c8;
  max-width: 560px;
  line-height: 1.7;
}
.hub-hd-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: none;
}
.hub-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 12px;
  background: rgba(14, 30, 60, 0.55);
  border: 1px solid rgba(96, 158, 255, 0.24);
  font-size: 12.5px;
  color: #8fa6c8;
  backdrop-filter: blur(8px);
}
.hub-pill .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2fe0a2;
  box-shadow: 0 0 10px #2fe0a2;
  animation: pulse 1.8s infinite;
}
.hub-clock {
  font-family: 'DIN Alternate', 'Bahnschrift', Consolas, monospace;
  letter-spacing: 1px;
  color: #e8f1ff;
  font-size: 15px;
}
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.35;
  }
}

/* ---------- 卡片 ---------- */
.hub-cards {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(440px, 1fr));
  gap: 24px;
}
.hub-card {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(180deg, rgba(24, 48, 92, 0.66), rgba(14, 30, 60, 0.55));
  border: 1px solid rgba(96, 158, 255, 0.24);
  box-shadow: 0 18px 50px rgba(3, 10, 25, 0.55), inset 0 0 40px rgba(0, 120, 255, 0.05);
  backdrop-filter: blur(10px);
  transition: transform 0.28s cubic-bezier(0.2, 0.7, 0.2, 1), box-shadow 0.28s, border-color 0.28s;
}
.hub-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  z-index: 6;
  background: linear-gradient(90deg, transparent, #00e5ff, #2f7bff, transparent);
  opacity: 0;
  transition: opacity 0.28s;
}
.hub-card:hover {
  transform: translateY(-6px);
  border-color: rgba(120, 180, 255, 0.5);
  box-shadow: 0 30px 70px rgba(3, 10, 25, 0.7), 0 0 40px rgba(0, 180, 255, 0.16),
    inset 0 0 40px rgba(0, 120, 255, 0.08);
}
.hub-card:hover::before {
  opacity: 0.95;
}
.hub-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 7;
  font-size: 11px;
  letter-spacing: 1px;
  padding: 4px 10px;
  border-radius: 20px;
  color: #bfe0ff;
  background: rgba(10, 26, 58, 0.7);
  border: 1px solid rgba(96, 158, 255, 0.24);
}

/* ---------- 仿真预览 ---------- */
.hub-thumb {
  position: relative;
  height: 236px;
  padding: 16px 18px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-bottom: 1px solid rgba(90, 140, 220, 0.18);
  background: radial-gradient(600px 260px at 50% -30%, rgba(47, 123, 255, 0.28), transparent 60%),
    linear-gradient(180deg, #061024, #03060f);
}
.prev-grid {
  position: absolute;
  inset: 0;
  opacity: 0.5;
  background-image: linear-gradient(rgba(0, 196, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 196, 255, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
}
.prev-scan {
  position: absolute;
  left: 0;
  right: 0;
  height: 70%;
  top: -70%;
  z-index: 3;
  background: linear-gradient(180deg, transparent, rgba(0, 229, 255, 0.1), transparent);
  animation: scan 4.5s ease-in-out infinite;
}
@keyframes scan {
  0% {
    top: -70%;
  }
  55%,
  100% {
    top: 110%;
  }
}
.prev-hd {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
  flex: none;
}
.prev-title {
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 3px;
  color: #a9d4ff;
  text-shadow: 0 0 14px rgba(74, 157, 248, 0.8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.wing {
  width: 52px;
  height: 1px;
  flex: none;
  background: linear-gradient(90deg, transparent, #2e5aa8);
}
.wing.flip {
  background: linear-gradient(270deg, transparent, #2e5aa8);
}
.prev-body {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
}
.tb-ticker {
  flex: none;
  height: 6px;
  border-radius: 3px;
  margin-bottom: 2px;
  background: linear-gradient(90deg, rgba(0, 229, 255, 0.55), rgba(0, 229, 255, 0.08));
}
.thumb-main {
  flex: 1;
  display: flex;
  gap: 10px;
  min-height: 0;
}
.tb-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}
.tb-col.main {
  flex: 1.7;
}
.tb-col.wide {
  flex: 1.6;
}
.tb-col.stat-col {
  flex: 1.1;
}
.tb-panel {
  flex: 1;
  border: 1px solid rgba(74, 157, 248, 0.28);
  border-radius: 6px;
  background: linear-gradient(180deg, rgba(9, 20, 44, 0.72), rgba(9, 20, 44, 0.52));
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  padding: 5px;
  min-height: 0;
}
.tb-panel.bars {
  flex-direction: row;
  align-items: flex-end;
  gap: 4px;
}
.tb-panel.bars i {
  width: 12%;
  border-radius: 2px 2px 0 0;
  background: linear-gradient(180deg, #7fc0ff, rgba(74, 157, 248, 0.3));
  transform-origin: bottom;
  animation: grow 2.4s ease-in-out infinite;
}
.tb-panel.bars i:nth-child(1) {
  height: 36%;
  animation-delay: 0s;
}
.tb-panel.bars i:nth-child(2) {
  height: 60%;
  animation-delay: 0.2s;
}
.tb-panel.bars i:nth-child(3) {
  height: 46%;
  animation-delay: 0.4s;
}
.tb-panel.bars i:nth-child(4) {
  height: 82%;
  animation-delay: 0.6s;
}
.tb-panel.bars i:nth-child(5) {
  height: 98%;
  animation-delay: 0.8s;
}
.tb-panel.bars i:nth-child(6) {
  height: 70%;
  animation-delay: 1s;
}
@keyframes grow {
  0%,
  100% {
    transform: scaleY(0.7);
    opacity: 0.6;
  }
  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}
.tb-kpi4 {
  flex: none;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 5px;
  min-height: 58px;
}
.tb-kpi4 i {
  border-radius: 4px;
  border: 1px solid rgba(74, 157, 248, 0.3);
  background: linear-gradient(180deg, rgba(74, 157, 248, 0.3), rgba(9, 20, 44, 0.5));
}
.tb-panel.trend {
  position: relative;
  overflow: hidden;
}
.tb-panel.trend::after {
  content: '';
  position: absolute;
  inset: 26% 12% 18% 12%;
  background: linear-gradient(180deg, rgba(0, 229, 255, 0.55), rgba(0, 229, 255, 0.05));
  clip-path: polygon(0 72%, 18% 44%, 36% 58%, 54% 24%, 72% 38%, 100% 4%, 100% 100%, 0 100%);
}
.tb-rows {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
}
.tb-rows i {
  flex: 1;
  position: relative;
  border-radius: 4px;
  border: 1px solid rgba(74, 157, 248, 0.26);
  background: linear-gradient(90deg, rgba(74, 157, 248, 0.46), rgba(9, 20, 44, 0.3) 68%);
  overflow: hidden;
}
.tb-rows i::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 22%;
  background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.5), transparent);
  transform: translateX(-120%);
  animation: slide 2.8s linear infinite;
}
.tb-rows i:nth-child(2)::after {
  animation-delay: 0.6s;
}
.tb-rows i:nth-child(3)::after {
  animation-delay: 1.2s;
}
.tb-rows i:nth-child(4)::after {
  animation-delay: 1.8s;
}
@keyframes slide {
  0% {
    transform: translateX(-120%);
  }
  70%,
  100% {
    transform: translateX(560%);
  }
}
.tb-ring {
  flex: 1.3;
  border: 1px solid rgba(74, 157, 248, 0.28);
  border-radius: 6px;
  background: linear-gradient(180deg, rgba(9, 20, 44, 0.72), rgba(9, 20, 44, 0.52));
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}
.tb-ring span {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 6px solid rgba(74, 157, 248, 0.28);
  border-top-color: #8fd0ff;
  border-right-color: #7fc0ff;
  box-shadow: 0 0 14px rgba(74, 157, 248, 0.55);
  animation: spin 3.5s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.tb-stat {
  flex: 1;
  border: 1px solid rgba(74, 157, 248, 0.28);
  border-radius: 6px;
  background: linear-gradient(180deg, rgba(9, 20, 44, 0.72), rgba(9, 20, 44, 0.52));
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 0 10px;
  min-height: 0;
}
.tb-stat-label {
  font-size: 10px;
  letter-spacing: 0.5px;
  color: #7eb6ff;
  white-space: nowrap;
  overflow: hidden;
}
.tb-stat-bar {
  display: block;
  height: 8px;
  border-radius: 3px;
  background: linear-gradient(90deg, #4a9df8, rgba(74, 157, 248, 0.25));
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.5);
}
.tb-stat-bar.w1 {
  width: 58%;
}
.tb-stat-bar.w2 {
  width: 82%;
}
.tb-foot {
  flex: none;
  display: flex;
  gap: 10px;
  height: 30px;
}
.tb-foot i {
  flex: 1;
  border-radius: 6px;
  border: 1px solid rgba(74, 157, 248, 0.24);
  background: rgba(9, 20, 44, 0.6);
}

/* 悬浮遮罩 */
.thumb-hover {
  position: absolute;
  inset: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(3, 8, 20, 0.15), rgba(3, 8, 20, 0.6));
  opacity: 0;
  transition: opacity 0.28s;
  backdrop-filter: blur(1px);
}
.hub-thumb:hover .thumb-hover {
  opacity: 1;
}
.open-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #03121f;
  background: linear-gradient(135deg, #00e5ff, #2f7bff);
  box-shadow: 0 0 26px rgba(0, 229, 255, 0.6);
  transform: translateY(6px);
  transition: transform 0.28s;
}
.open-btn svg {
  width: 18px;
  height: 18px;
}
.hub-thumb:hover .open-btn {
  transform: translateY(0);
}

/* ---------- 卡片信息 ---------- */
.hub-meta {
  padding: 18px 20px 20px;
}
.hub-name {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hub-name .nm {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}
.hub-live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #2fe0a2;
}
.hub-live i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #2fe0a2;
  box-shadow: 0 0 8px #2fe0a2;
  animation: pulse 1.6s infinite;
}
.hub-desc {
  margin: 10px 0 14px;
  font-size: 13px;
  line-height: 1.75;
  color: #8fa6c8;
  min-height: 36px;
}
.hub-facts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(90, 140, 220, 0.18);
}
.hub-fact {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: #7f97b8;
  padding: 5px 11px;
  border-radius: 8px;
  background: rgba(30, 60, 110, 0.28);
  border: 1px solid rgba(90, 140, 220, 0.18);
}
.hub-fact i {
  color: #00e5ff;
}
.hub-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.btn-open {
  display: inline-flex;
  align-items: center;
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #03121f;
  background: linear-gradient(135deg, #00e5ff, #2f7bff);
  box-shadow: 0 6px 18px rgba(0, 160, 255, 0.28);
  border: none;
  cursor: pointer;
}
.hub-foot-tip {
  font-size: 12px;
  color: #5c718f;
}

/* ---------- 名称设置 ---------- */
.hub-setting {
  position: relative;
  z-index: 2;
  margin-top: 26px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(14, 30, 60, 0.55), rgba(10, 22, 44, 0.5));
  border: 1px solid rgba(96, 158, 255, 0.24);
  backdrop-filter: blur(10px);
}
.hs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 22px;
  cursor: pointer;
}
.hs-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.hs-ic {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.22), rgba(47, 123, 255, 0.22));
  border: 1px solid rgba(96, 158, 255, 0.24);
}
.hs-ic svg {
  width: 16px;
  height: 16px;
}
.hs-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}
.hs-sub {
  font-size: 12px;
  color: #5c718f;
}
.hs-toggle {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: #00e5ff;
}
.hs-body {
  padding: 20px 22px 22px;
  border-top: 1px solid rgba(90, 140, 220, 0.18);
}
.hs-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 18px;
  margin-bottom: 18px;
}
.hs-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.hs-label {
  font-size: 12px;
  color: #8fa6c8;
}
.hs-inp {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  height: 42px;
  border-radius: 10px;
  background: rgba(6, 16, 34, 0.8);
  border: 1px solid rgba(96, 158, 255, 0.24);
}
.hs-inp:focus-within {
  border-color: #00e5ff;
  box-shadow: 0 0 0 3px rgba(0, 229, 255, 0.12);
}
.hs-inp input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #e8f1ff;
  font-size: 13.5px;
}
.hs-cnt {
  font-size: 11px;
  color: #5c718f;
}
.hs-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.btn-primary {
  padding: 9px 22px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #03121f;
  background: linear-gradient(135deg, #00e5ff, #2f7bff);
  border: none;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(0, 160, 255, 0.28);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-ghost {
  padding: 9px 20px;
  border-radius: 10px;
  font-size: 13px;
  color: #8fa6c8;
  background: transparent;
  border: 1px solid rgba(96, 158, 255, 0.24);
  cursor: pointer;
}
.hs-tip {
  font-size: 12px;
  color: #5c718f;
}
</style>
