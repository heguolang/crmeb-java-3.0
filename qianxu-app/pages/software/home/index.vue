<template>
  <view class="svc-home">
    <!-- 顶部品牌头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="status-pad"></view>
      <view class="brand-row">
        <view class="brand-badge">黔</view>
        <view class="brand-info">
          <view class="brand-name">{{ config.companyName || '黔序科技' }}</view>
          <view class="brand-sub">软件开发 · 小程序 · App</view>
        </view>
        <view class="brand-call" @click="openContact">?</view>
      </view>
      <view class="search-bar" @click="openContact">
        <text class="search-ico">⌕</text>
        <text class="search-ph">搜索小程序开发 / App开发 / 系统定制</text>
      </view>
    </view>

    <view class="page-body">
      <!-- 主推服务 banner -->
      <view class="hero-card">
        <view class="hero-deco h1"></view>
        <view class="hero-deco h2"></view>
        <view class="hero-tag">主营服务</view>
        <view class="hero-title">小程序 / App 定制开发</view>
        <view class="hero-sub">源码交付 · 一对一项目经理</view>
        <view class="hero-btn" @click="openContact">免费获取方案</view>
      </view>

      <!-- 服务宫格 -->
      <view class="grid-card">
        <view v-for="(g, i) in services" :key="i" class="g-item" hover-class="g-press" :hover-stay-time="80" @click="openContact">
          <image :src="g.icon" class="g-icon" mode="aspectFit" />
          <text class="g-label">{{ g.name }}</text>
        </view>
      </view>

      <!-- 服务流程 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">服务流程</text>
          <text class="sec-link" @click="openContact">了解详情</text>
        </view>
        <view class="flow-row">
          <view v-for="(f, i) in flows" :key="i" class="flow-item">
            <view class="flow-num">{{ i + 1 }}</view>
            <view class="flow-name">{{ f.name }}</view>
            <view class="flow-desc">{{ f.desc }}</view>
          </view>
        </view>
      </view>

      <!-- 客户案例 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">客户案例</text>
          <text class="sec-link" @click="goCase">全部案例</text>
        </view>
        <view class="case-row">
          <view v-for="(c, i) in cases" :key="i" class="case-item" @click="goCase">
            <view class="case-img-wrap">
              <image v-if="c.image" :src="c.image" class="case-img" mode="aspectFill" />
              <view v-else class="case-img case-ph">{{ (c.category || '案例').slice(0, 2) }}</view>
            </view>
            <view class="case-tag">{{ c.category || '小程序' }}</view>
            <view class="case-title">{{ c.title }}</view>
            <view class="case-desc">{{ c.description }}</view>
          </view>
          <view v-if="!cases.length" class="case-empty">案例整理中，欢迎咨询</view>
        </view>
      </view>

      <!-- 为什么选择我们 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">为什么选择我们</text>
        </view>
        <view class="adv-list">
          <view v-for="(a, i) in advantages" :key="i" class="adv-item">
            <view class="adv-ico">{{ a.ico }}</view>
            <view class="adv-body">
              <view class="adv-name">{{ a.name }}</view>
              <view class="adv-desc">{{ a.desc }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- CTA -->
      <view class="cta-card" @click="openContact">
        <view class="cta-main">💬 免费获取开发方案</view>
        <view class="cta-sub">{{ config.contactTime || '10分钟内响应，提供初步报价与周期参考' }}</view>
      </view>

      <view style="height: 140rpx"></view>
    </view>

    <!-- 联系弹窗 -->
    <view v-if="contactShow" class="mask" @click="contactShow = false">
      <view class="contact-card" @click.stop>
        <view class="ct-title">免费获取开发方案</view>
        <view class="ct-sub">{{ config.contactTime || '10分钟内响应，提供初步报价与周期参考' }}</view>
        <view v-if="config.contactPhone" class="ct-row" @click="copy(config.contactPhone)">
          <text class="ct-label">📞 电话</text>
          <text class="ct-value">{{ config.contactPhone }}</text>
          <text class="ct-copy">复制</text>
        </view>
        <view v-if="config.contactWechat" class="ct-row" @click="copy(config.contactWechat)">
          <text class="ct-label">💬 微信</text>
          <text class="ct-value">{{ config.contactWechat }}</text>
          <text class="ct-copy">复制</text>
        </view>
        <view v-if="!config.contactPhone && !config.contactWechat" class="ct-empty">联系方式配置中，请稍后再试</view>
        <view class="ct-close" @click="contactShow = false">关闭</view>
      </view>
    </view>

    <svc-footer v-if="navMode" active="home" />
  </view>
</template>

<script>
import { getSvcConfig, getSvcCases } from '@/api/svc.js';
import svcFooter from '@/components/svcFooter/index.vue';

export default {
  components: { svcFooter },
  data() {
    return {
      config: {},
      navMode: true,
      cases: [],
      contactShow: false,
      services: [
        { name: '软件开发', icon: '/static/software/icon_code.png' },
        { name: '小程序开发', icon: '/static/software/icon_miniapp.png' },
        { name: 'App 开发', icon: '/static/software/icon_app.png' },
        { name: 'UI 设计', icon: '/static/software/icon_ui.png' },
        { name: '系统对接', icon: '/static/software/icon_api.png' },
        { name: '运维托管', icon: '/static/software/icon_ops.png' },
        { name: '源码交付', icon: '/static/software/icon_source.png' },
        { name: '方案咨询', icon: '/static/software/icon_consult.png' }
      ],
      flows: [
        { name: '需求沟通', desc: '1对1梳理需求' },
        { name: '方案报价', desc: '3天出方案' },
        { name: '开发交付', desc: '分阶段验收' },
        { name: '上线运维', desc: '7×24响应' }
      ],
      advantages: [
        { ico: '📄', name: '原生源码交付', desc: '前端 100% 以内交付，支持二次开发' },
        { ico: '🕐', name: '7×24 运维响应', desc: '上线后持续保障，故障 30 分钟内响应' },
        { ico: '👤', name: '一对一项目经理', desc: '进度全程透明，分阶段验收确认' }
      ]
    };
  },
  onLoad() {
    this.loadConfig();
    this.loadCases();
  },
  methods: {
    loadConfig() {
      getSvcConfig()
        .then((res) => {
          this.config = res.data || {};
          this.navMode = this.config.navMode !== false;
        })
        .catch(() => {});
    },
    loadCases() {
      getSvcCases(2)
        .then((res) => {
          this.cases = (res.data && (Array.isArray(res.data) ? res.data : res.data.list)) || [];
        })
        .catch(() => {});
    },
    goCase() {
      uni.navigateTo({ url: '/pages/software/case/index' });
    },
    openContact() {
      this.contactShow = true;
    },
    copy(text) {
      uni.setClipboardData({
        data: String(text),
        success: () => {
          uni.hideToast();
          uni.showToast({ title: '已复制', icon: 'success' });
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-home {
  min-height: 100vh;
  background: #f4f6fb;
}
.status-pad {
  height: var(--status-bar-height, 0px);
}
/* 顶部 */
.top-wrap {
  position: relative;
  padding: 20rpx 30rpx 40rpx;
  background: linear-gradient(135deg, #0d4fc4 0%, #1d6bf3 60%, #3d8bfd 100%);
  overflow: hidden;
}
.top-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.top-deco.d1 { width: 320rpx; height: 320rpx; right: -100rpx; top: -120rpx; }
.top-deco.d2 { width: 200rpx; height: 200rpx; left: -60rpx; bottom: -80rpx; }
.brand-row { display: flex; align-items: center; position: relative; }
.brand-badge {
  width: 76rpx; height: 76rpx; border-radius: 20rpx;
  background: #fff; color: #1d6bf3;
  font-size: 38rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.brand-info { margin-left: 20rpx; flex: 1; }
.brand-name { color: #fff; font-size: 34rpx; font-weight: 700; }
.brand-sub { color: rgba(255, 255, 255, 0.75); font-size: 22rpx; margin-top: 4rpx; }
.brand-call {
  width: 56rpx; height: 56rpx; border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.6);
  color: #fff; font-size: 32rpx;
  display: flex; align-items: center; justify-content: center;
}
.search-bar {
  margin-top: 28rpx; position: relative;
  height: 76rpx; border-radius: 38rpx;
  background: rgba(255, 255, 255, 0.16);
  border: 1rpx solid rgba(255, 255, 255, 0.25);
  display: flex; align-items: center; padding: 0 30rpx;
}
.search-ico { color: rgba(255, 255, 255, 0.8); font-size: 32rpx; margin-right: 12rpx; }
.search-ph { color: rgba(255, 255, 255, 0.65); font-size: 26rpx; }

.page-body { padding: 24rpx 30rpx 0; }

/* 主推 banner */
.hero-card {
  position: relative; overflow: hidden;
  border-radius: 24rpx; padding: 36rpx 34rpx;
  background: linear-gradient(120deg, #0d4fc4 0%, #1d6bf3 55%, #4b95ff 100%);
  color: #fff;
}
.hero-deco {
  position: absolute; border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}
.hero-deco.h1 { width: 260rpx; height: 260rpx; right: -60rpx; top: -80rpx; }
.hero-deco.h2 { width: 160rpx; height: 160rpx; right: 120rpx; bottom: -60rpx; background: rgba(255, 255, 255, 0.07); }
.hero-tag {
  display: inline-block; font-size: 20rpx;
  background: rgba(255, 255, 255, 0.2); border-radius: 20rpx;
  padding: 4rpx 18rpx; position: relative;
}
.hero-title { font-size: 44rpx; font-weight: 700; margin-top: 16rpx; position: relative; }
.hero-sub { font-size: 24rpx; opacity: 0.85; margin-top: 10rpx; position: relative; }
.hero-btn {
  display: inline-block; margin-top: 26rpx; position: relative;
  background: #fff; color: #1d6bf3;
  font-size: 26rpx; font-weight: 600;
  border-radius: 34rpx; padding: 14rpx 34rpx;
}

/* 服务宫格 */
.grid-card {
  margin-top: 24rpx; background: #fff; border-radius: 24rpx;
  padding: 34rpx 10rpx 10rpx;
  display: flex; flex-wrap: wrap;
}
.g-item {
  width: 25%; display: flex; flex-direction: column; align-items: center;
  margin-bottom: 30rpx;
}
.g-item.g-press { opacity: 0.7; }
.g-icon { width: 72rpx; height: 72rpx; }
.g-label { font-size: 24rpx; color: #333; margin-top: 12rpx; }

/* 通用卡片 */
.section-card {
  margin-top: 24rpx; background: #fff; border-radius: 24rpx; padding: 30rpx;
}
.sec-head { display: flex; align-items: center; justify-content: space-between; }
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }
.sec-link { font-size: 24rpx; color: #9aa4b2; }

/* 服务流程 */
.flow-row { display: flex; margin-top: 30rpx; }
.flow-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.flow-num {
  width: 56rpx; height: 56rpx; border-radius: 50%;
  background: #1d6bf3; color: #fff; font-size: 28rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.flow-name { font-size: 26rpx; font-weight: 600; color: #333; margin-top: 14rpx; }
.flow-desc { font-size: 20rpx; color: #9aa4b2; margin-top: 6rpx; }

/* 客户案例 */
.case-row { display: flex; flex-wrap: wrap; margin-top: 26rpx; }
.case-item {
  width: calc(50% - 12rpx); background: #f7f9fc; border-radius: 18rpx;
  padding: 18rpx; margin-right: 24rpx; margin-bottom: 20rpx;
}
.case-item:nth-child(2n) { margin-right: 0; }
.case-img-wrap { border-radius: 14rpx; overflow: hidden; }
.case-img { width: 100%; height: 200rpx; display: block; }
.case-ph {
  background: linear-gradient(135deg, #dce9ff, #f0f5ff);
  color: #1d6bf3; font-size: 40rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.case-tag {
  display: inline-block; margin-top: 14rpx;
  font-size: 18rpx; color: #1d6bf3;
  background: rgba(29, 107, 243, 0.1); border-radius: 16rpx;
  padding: 2rpx 14rpx;
}
.case-title { font-size: 26rpx; font-weight: 600; color: #222; margin-top: 10rpx; }
.case-desc { font-size: 20rpx; color: #9aa4b2; margin-top: 6rpx; }
.case-empty { width: 100%; text-align: center; color: #9aa4b2; font-size: 24rpx; padding: 30rpx 0; }

/* 优势 */
.adv-list { margin-top: 10rpx; }
.adv-item { display: flex; align-items: flex-start; padding: 20rpx 0; }
.adv-ico {
  width: 64rpx; height: 64rpx; border-radius: 16rpx;
  background: #eef4ff; display: flex; align-items: center; justify-content: center;
  font-size: 32rpx;
}
.adv-body { margin-left: 20rpx; flex: 1; }
.adv-name { font-size: 28rpx; font-weight: 600; color: #222; }
.adv-desc { font-size: 22rpx; color: #9aa4b2; margin-top: 6rpx; line-height: 1.5; }

/* CTA */
.cta-card {
  margin-top: 24rpx; border-radius: 24rpx; text-align: center;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  padding: 34rpx 30rpx; color: #fff;
}
.cta-main { font-size: 32rpx; font-weight: 700; }
.cta-sub { font-size: 22rpx; opacity: 0.8; margin-top: 10rpx; }

/* 联系弹窗 */
.mask {
  position: fixed; left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.55); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
}
.contact-card {
  width: 600rpx; background: #fff; border-radius: 28rpx; padding: 44rpx 36rpx;
}
.ct-title { font-size: 34rpx; font-weight: 700; color: #222; text-align: center; }
.ct-sub { font-size: 22rpx; color: #9aa4b2; text-align: center; margin-top: 10rpx; }
.ct-row {
  display: flex; align-items: center;
  background: #f5f8ff; border-radius: 16rpx;
  padding: 24rpx; margin-top: 20rpx;
}
.ct-label { font-size: 26rpx; color: #333; width: 130rpx; }
.ct-value { flex: 1; font-size: 28rpx; font-weight: 600; color: #1d6bf3; }
.ct-copy { font-size: 22rpx; color: #1d6bf3; }
.ct-empty { text-align: center; color: #9aa4b2; font-size: 24rpx; padding: 30rpx 0 10rpx; }
.ct-close {
  margin-top: 30rpx; text-align: center;
  border-top: 1rpx solid #f0f2f7; padding-top: 24rpx;
  color: #9aa4b2; font-size: 26rpx;
}
</style>
