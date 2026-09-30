<template>
  <div class="bigdata-hub">
    <!-- 页头 -->
    <div class="hub-hd">
      <div class="hub-hd-left">
        <h2>大数据中心</h2>
        <p>运营大数据 / 市场大数据统一入口，点击卡片按钮在新标签页全屏打开，适合投屏观看</p>
      </div>
    </div>

    <!-- 两个大屏卡片 -->
    <div class="hub-cards">
      <div v-for="s in screens" :key="s.key" class="hub-card">
        <!-- 缩略图（按真实大屏布局绘制的示意图） -->
        <div class="hub-thumb" :class="'thumb-' + s.key">
          <div class="thumb-hd">
            <span class="thumb-wing"></span>
            <span class="thumb-title">{{ titles[s.key] }}</span>
            <span class="thumb-wing flip"></span>
          </div>

          <!-- 运营大数据：中间地图 + 左右面板 -->
          <div v-if="s.key === 'operation'" class="thumb-body">
            <div class="tb-col">
              <div class="tb-panel"><i></i><i></i><i></i></div>
              <div class="tb-panel rect"></div>
            </div>
            <div class="tb-col main">
              <div class="tb-map">
                <span class="tb-map-core"></span>
                <span class="tb-map-dot d1"></span>
                <span class="tb-map-dot d2"></span>
                <span class="tb-map-dot d3"></span>
              </div>
              <div class="tb-kpi"><i></i><i></i><i></i><i></i></div>
            </div>
            <div class="tb-col">
              <div class="tb-panel rect"></div>
              <div class="tb-panel bars"><i></i><i></i><i></i><i></i></div>
            </div>
          </div>

          <!-- 市场大数据：左三块 + 右两块 -->
          <div v-else class="thumb-body">
            <div class="tb-col wide">
              <div class="tb-panel wide rect"></div>
              <div class="tb-panel rect"></div>
              <div class="tb-panel bars"><i></i><i></i><i></i><i></i><i></i></div>
            </div>
            <div class="tb-col">
              <div class="tb-panel tall"></div>
              <div class="tb-panel rect"></div>
            </div>
          </div>

          <div class="thumb-mask">
            <el-button size="small" type="primary" icon="el-icon-full-screen" @click="openScreen(s)">
              打开大屏
            </el-button>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="hub-meta">
          <div class="hub-name">
            <span class="dot"></span>
            <span class="nm">{{ s.name }}</span>
            <el-tag size="mini" type="info" effect="plain">{{ s.screen }}</el-tag>
          </div>
          <p class="hub-desc">{{ s.desc }}</p>
          <div class="hub-actions">
            <el-button size="small" type="primary" icon="el-icon-full-screen" @click="openScreen(s)">
              打开大屏
            </el-button>
            <span class="hub-actions-tip">新标签页打开，全屏投屏</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 展示名称设置 -->
    <el-card class="hub-setting" shadow="never">
      <div slot="header" class="hs-header">
        <span>大屏展示名称设置</span>
        <span class="hs-sub">设置数据中心大屏展示的大标题名称，留空保存即恢复默认</span>
      </div>
      <el-form label-width="150px" class="hs-form" @submit.native.prevent>
        <el-form-item label="运营大数据标题">
          <el-input
            v-model="form.operation"
            placeholder="默认：黔序数据指挥中心"
            maxlength="30"
            show-word-limit
            class="hs-input"
          />
        </el-form-item>
        <el-form-item label="市场大数据标题">
          <el-input
            v-model="form.market"
            placeholder="默认：市 场 大 数 据 中 心"
            maxlength="30"
            show-word-limit
            class="hs-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保 存</el-button>
          <el-button @click="reset">恢复默认</el-button>
          <span class="hs-tip">保存后大屏刷新页面即可看到新标题。</span>
        </el-form-item>
      </el-form>
    </el-card>
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
          path: '/market/screen/operation',
        },
        {
          key: 'market',
          name: '市场大数据',
          screen: '市场中心',
          desc: '今日交易、实时分享、下单动态与优秀团队榜，适合市场部门挂屏',
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
  padding: 16px;
  background: #f4f6fb;
  min-height: calc(100vh - 100px);
  box-sizing: border-box;
}
.hub-hd {
  margin-bottom: 14px;
}
.hub-hd h2 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 600;
  color: #1f2a3d;
}
.hub-hd p {
  margin: 0;
  font-size: 12px;
  color: #8a94a6;
}

/* ---------- 卡片 ---------- */
.hub-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.hub-card {
  flex: 1 1 420px;
  max-width: 620px;
  background: #fff;
  border: 1px solid #e8ecf3;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(31, 79, 158, 0.06);
  transition: box-shadow 0.2s, transform 0.2s;
}
.hub-card:hover {
  box-shadow: 0 6px 20px rgba(31, 79, 158, 0.14);
  transform: translateY(-2px);
}

/* ---------- 缩略图 ---------- */
.hub-thumb {
  position: relative;
  height: 200px;
  padding: 12px 14px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.thumb-operation {
  background: radial-gradient(120% 100% at 50% 0%, #1b3f7d 0%, #0d2350 55%, #061229 100%);
}
.thumb-market {
  background: radial-gradient(120% 100% at 50% 0%, #123a72 0%, #0a2350 55%, #041020 100%);
}
.thumb-hd {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}
.thumb-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #cfe4ff;
  text-shadow: 0 0 12px rgba(74, 157, 248, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.thumb-wing {
  width: 26px;
  height: 2px;
  background: linear-gradient(90deg, rgba(74, 157, 248, 0), #4a9df8);
}
.thumb-wing.flip {
  background: linear-gradient(270deg, rgba(74, 157, 248, 0), #4a9df8);
}
.thumb-body {
  flex: 1;
  display: flex;
  gap: 8px;
  min-height: 0;
}
.tb-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.tb-col.wide {
  flex: 1.15;
}
.tb-col.main {
  flex: 1.6;
}
.tb-panel {
  flex: 1;
  border: 1px solid rgba(74, 157, 248, 0.32);
  border-radius: 4px;
  background: linear-gradient(180deg, rgba(74, 157, 248, 0.16), rgba(74, 157, 248, 0.04));
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  padding: 4px;
  min-height: 0;
}
.tb-panel.tall {
  flex: 1.6;
}
.tb-panel i {
  display: block;
  width: 74%;
  height: 3px;
  border-radius: 2px;
  background: rgba(140, 200, 255, 0.55);
}
.tb-panel.rect {
  background: linear-gradient(180deg, rgba(74, 157, 248, 0.28), rgba(74, 157, 248, 0.06));
}
.tb-panel.bars {
  flex-direction: row;
  align-items: flex-end;
  gap: 3px;
}
.tb-panel.bars i {
  width: 10%;
  background: linear-gradient(180deg, #7fc0ff, rgba(74, 157, 248, 0.25));
}
.tb-panel.bars i:nth-child(1) {
  height: 40%;
}
.tb-panel.bars i:nth-child(2) {
  height: 72%;
}
.tb-panel.bars i:nth-child(3) {
  height: 55%;
}
.tb-panel.bars i:nth-child(4) {
  height: 88%;
}
.tb-panel.bars i:nth-child(5) {
  height: 62%;
}
.tb-map {
  position: relative;
  flex: 2.2;
  border: 1px solid rgba(74, 157, 248, 0.4);
  border-radius: 6px;
  background: radial-gradient(80% 70% at 50% 55%, rgba(74, 157, 248, 0.35), rgba(74, 157, 248, 0.05) 70%);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}
.tb-map-core {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: radial-gradient(circle, #a9d3ff 0%, rgba(74, 157, 248, 0.15) 70%, transparent 72%);
  box-shadow: 0 0 18px rgba(74, 157, 248, 0.8);
}
.tb-map-dot {
  position: absolute;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #8fd0ff;
  box-shadow: 0 0 8px #4a9df8;
}
.tb-map-dot.d1 {
  top: 26%;
  left: 26%;
}
.tb-map-dot.d2 {
  bottom: 24%;
  right: 30%;
}
.tb-map-dot.d3 {
  top: 46%;
  right: 18%;
}
.tb-kpi {
  flex: 1;
  display: flex;
  gap: 6px;
}
.tb-kpi i {
  flex: 1;
  border-radius: 4px;
  border: 1px solid rgba(74, 157, 248, 0.3);
  background: rgba(74, 157, 248, 0.14);
}
.thumb-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(6, 18, 41, 0.55);
  opacity: 0;
  transition: opacity 0.2s;
}
.hub-thumb:hover .thumb-mask {
  opacity: 1;
}

/* ---------- 卡片信息 ---------- */
.hub-meta {
  padding: 14px 16px 16px;
}
.hub-name {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hub-name .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2b6fe3;
}
.hub-name .nm {
  font-size: 15px;
  font-weight: 600;
  color: #1f2a3d;
}
.hub-desc {
  margin: 8px 0 12px;
  font-size: 12px;
  line-height: 1.7;
  color: #8a94a6;
  min-height: 34px;
}
.hub-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hub-actions-tip {
  font-size: 12px;
  color: #b6bece;
}

/* ---------- 名称设置 ---------- */
.hub-setting {
  margin-top: 16px;
  border: 1px solid #e8ecf3;
  border-radius: 10px;
}
.hs-header {
  font-size: 15px;
  font-weight: 600;
  color: #1f2a3d;
}
.hs-sub {
  margin-left: 12px;
  font-size: 12px;
  font-weight: 400;
  color: #8a94a6;
}
.hs-input {
  max-width: 420px;
}
.hs-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #8a94a6;
}
</style>
