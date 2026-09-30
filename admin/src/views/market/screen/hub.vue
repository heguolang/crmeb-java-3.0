<template>
  <div class="bigdata-hub">
    <!-- 页头 -->
    <div class="hub-hd">
      <div class="hub-hd-left">
        <h2>大数据中心</h2>
        <p>运营数据 / 市场数据统一入口，点击卡片即可预览并全屏打开大屏</p>
      </div>
      <span class="hub-count">共 {{ screens.length }} 块大屏</span>
    </div>

    <!-- 两个大屏卡片 -->
    <div class="hub-cards">
      <div
        v-for="s in screens"
        :key="s.key"
        class="hub-card"
        @click="openScreen(s)"
      >
        <!-- 缩略图（按真实大屏布局绘制的示意图） -->
        <div class="hub-thumb" :class="'thumb-' + s.key">
          <div class="thumb-hd">
            <span class="thumb-wing"></span>
            <span class="thumb-title">{{ titles[s.key] }}</span>
            <span class="thumb-wing flip"></span>
          </div>

          <!-- 运营大数据：跑马灯 + 左 KPI/趋势 + 中订单流 + 右环形/状态，底部 6 模块条 -->
          <div v-if="s.key === 'operation'" class="thumb-body">
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
            <div class="tb-foot"><i></i><i></i><i></i><i></i><i></i><i></i></div>
          </div>

          <!-- 市场大数据：左柱状面板 + 右数据块，底部模块条 -->
          <div v-else class="thumb-body">
            <div class="thumb-main">
              <div class="tb-col wide">
                <div class="tb-panel bars tall"><i></i><i></i><i></i><i></i><i></i></div>
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
            <div class="tb-foot"><i></i><i></i><i></i><i></i></div>
          </div>

          <!-- 悬浮提示层：右下角「打开大屏」 -->
          <div class="thumb-hover">
            <span class="thumb-hover-btn">
              <i class="el-icon-full-screen"></i>
              打开大屏
            </span>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="hub-meta">
          <div class="hub-name">
            <span class="nm">{{ s.name }}</span>
            <span class="hub-tag">{{ s.screen }}</span>
          </div>
          <p class="hub-desc">{{ s.desc }}</p>
          <div class="hub-facts">
            <span v-for="(f, i) in s.facts" :key="i" class="hub-fact">
              <i :class="f.icon"></i>{{ f.text }}
            </span>
          </div>
          <div class="hub-foot">
            <el-button
              size="small"
              type="primary"
              icon="el-icon-full-screen"
              @click.stop="openScreen(s)"
            >
              打开大屏
            </el-button>
            <span class="hub-foot-tip">{{ s.tip }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 展示名称设置（可收起） -->
    <div class="hub-setting">
      <div class="hs-header" @click="settingCollapsed = !settingCollapsed">
        <div class="hs-title-wrap">
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
            <el-input
              v-model="form.operation"
              placeholder="默认：黔序数据指挥中心"
              maxlength="30"
              show-word-limit
            />
          </div>
          <div class="hs-field">
            <span class="hs-label">市场大屏标题</span>
            <el-input
              v-model="form.market"
              placeholder="默认：市 场 大 数 据 中 心"
              maxlength="30"
              show-word-limit
            />
          </div>
        </div>
        <div class="hs-actions">
          <el-button type="primary" size="small" :loading="saving" @click="save">保 存</el-button>
          <el-button size="small" @click="reset">恢复默认</el-button>
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
      // 设置区默认收起为一行，弱于大屏入口区
      settingCollapsed: true,
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
          desc: '交易 / 会员 / 分销核心指标总览，适合公司大屏、会议室投屏',
          facts: [
            { icon: 'el-icon-full-screen', text: '新标签页全屏打开' },
            { icon: 'el-icon-monitor', text: '适合会议室投屏' },
          ],
          tip: '支持投屏观看',
          path: '/market/screen/operation',
        },
        {
          key: 'market',
          name: '市场大数据',
          screen: '市场中心',
          desc: '今日交易、实时分享、下单动态与优秀团队榜，适合市场部门挂屏',
          facts: [
            { icon: 'el-icon-full-screen', text: '新标签页全屏打开' },
            { icon: 'el-icon-monitor', text: '适合市场部门挂屏' },
          ],
          tip: '支持投屏观看',
          path: '/market/screen/market',
        },
      ],
    };
  },
  mounted() {
    this.load();
  },
  methods: {
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
  padding: 20px;
  background: #f4f6fa;
  min-height: calc(100vh - 100px);
  box-sizing: border-box;
}

/* ---------- 页头 ---------- */
.hub-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.hub-hd h2 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}
.hub-hd p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}
.hub-count {
  flex: none;
  padding: 6px 14px;
  border-radius: 16px;
  background: #eef3ff;
  font-size: 12px;
  color: #2b6fe3;
}

/* ---------- 卡片 ---------- */
.hub-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.hub-card {
  flex: 1 1 420px;
  max-width: 640px;
  background: #fff;
  border: 1px solid #e5e9f2;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(15, 31, 77, 0.06);
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
}
.hub-card:hover {
  box-shadow: 0 10px 26px rgba(15, 31, 77, 0.16);
  transform: translateY(-2px);
}

/* ---------- 缩略图 ---------- */
.hub-thumb {
  position: relative;
  height: 220px;
  padding: 14px 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.thumb-operation {
  background: linear-gradient(145deg, #0a1a3f 0%, #12295e 48%, #1e4a8f 100%);
}
.thumb-market {
  background: linear-gradient(145deg, #0a1a3f 0%, #13305f 48%, #21539b 100%);
}
.thumb-hd {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
  flex: none;
}
.thumb-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #8fc1ff;
  text-shadow: 0 0 12px rgba(74, 157, 248, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.thumb-wing {
  width: 42px;
  height: 1px;
  flex: none;
  background: linear-gradient(90deg, rgba(46, 90, 168, 0), #2e5aa8);
}
.thumb-wing.flip {
  background: linear-gradient(270deg, rgba(46, 90, 168, 0), #2e5aa8);
}
.thumb-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
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
.tb-panel i {
  display: block;
  width: 78%;
  height: 4px;
  border-radius: 2px;
  background: rgba(46, 90, 168, 0.9);
}
.tb-panel.rect {
  background: linear-gradient(180deg, rgba(74, 157, 248, 0.2), rgba(9, 20, 44, 0.6));
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
}
.tb-panel.bars i:nth-child(1) {
  height: 34%;
  opacity: 0.45;
}
.tb-panel.bars i:nth-child(2) {
  height: 58%;
  opacity: 0.6;
}
.tb-panel.bars i:nth-child(3) {
  height: 44%;
  opacity: 0.75;
}
.tb-panel.bars i:nth-child(4) {
  height: 78%;
  opacity: 0.9;
}
.tb-panel.bars i:nth-child(5) {
  height: 96%;
}
/* 跑马灯细条 */
.tb-ticker {
  flex: none;
  height: 6px;
  border-radius: 3px;
  margin-bottom: 2px;
  background: linear-gradient(90deg, rgba(0, 229, 255, 0.55), rgba(0, 229, 255, 0.08));
}
/* 左栏 KPI 2×2 */
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
/* 趋势：面积图意象 */
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
/* 中栏实时订单流：多行条 */
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
}
.tb-rows i::after {
  content: '';
  position: absolute;
  right: 9%;
  top: 50%;
  width: 20%;
  height: 3px;
  margin-top: -1.5px;
  border-radius: 2px;
  background: rgba(255, 200, 74, 0.8);
}
/* 右栏环形图 */
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
}
/* 右侧数据块（市场大屏） */
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
}
.tb-stat-bar.w1 {
  width: 58%;
}
.tb-stat-bar.w2 {
  width: 82%;
}
/* 底部模块条 */
.tb-foot {
  flex: none;
  display: flex;
  gap: 10px;
  height: 34px;
}
.tb-foot i {
  flex: 1;
  border-radius: 6px;
  border: 1px solid rgba(74, 157, 248, 0.24);
  background: rgba(9, 20, 44, 0.6);
}

/* 悬浮提示层 */
.thumb-hover {
  position: absolute;
  right: 14px;
  bottom: 14px;
  opacity: 0;
  transform: translateY(4px);
  transition: opacity 0.2s, transform 0.2s;
  pointer-events: none;
}
.hub-thumb:hover .thumb-hover {
  opacity: 1;
  transform: translateY(0);
}
.thumb-hover-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  background: rgba(43, 111, 227, 0.95);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
  font-size: 12px;
  color: #fff;
}

/* ---------- 卡片信息 ---------- */
.hub-meta {
  padding: 16px 18px 18px;
}
.hub-name {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hub-name .nm {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}
.hub-tag {
  padding: 2px 10px;
  border-radius: 10px;
  background: #e8f1ff;
  font-size: 11px;
  color: #2b6fe3;
}
.hub-desc {
  margin: 10px 0 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
  min-height: 34px;
}
.hub-facts {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eef1f6;
  margin-bottom: 14px;
}
.hub-fact {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #94a3b8;
}
.hub-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hub-foot-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* ---------- 名称设置（可收起） ---------- */
.hub-setting {
  margin-top: 20px;
  background: #fff;
  border: 1px solid #e5e9f2;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(15, 31, 77, 0.04);
}
.hs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
}
.hs-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.hs-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}
.hs-sub {
  font-size: 12px;
  color: #94a3b8;
}
.hs-toggle {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #2b6fe3;
}
.hs-body {
  padding: 0 20px 20px;
}
.hs-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}
.hs-field {
  flex: 1 1 320px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.hs-label {
  font-size: 12px;
  color: #4b5563;
}
.hs-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hs-tip {
  font-size: 12px;
  color: #94a3b8;
}
</style>
