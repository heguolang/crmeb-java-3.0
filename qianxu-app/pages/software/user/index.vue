<template>
  <view class="svc-user">
    <!-- 头部 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="status-pad"></view>
      <view class="nav-row">
        <text class="nav-title">会员中心</text>
        <text class="nav-gear" @click="nav('/pages/users/profile_setup/index')">⚙</text>
      </view>
      <view class="user-row" @click="nav('/pages/users/user_info/index')">
        <view class="avatar">{{ avatarText }}</view>
        <view class="user-info">
          <view class="user-name">{{ displayName }}</view>
          <view class="user-badges">
            <view v-if="signed" class="badge badge-sign">签约客户</view>
            <view class="badge badge-id">ID {{ uid || '—' }}</view>
          </view>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="page-body">
      <!-- 统计（骑在蓝色渐变上） -->
      <view class="stats-card">
        <view class="stat-item" @click="navProject()">
          <view class="stat-num">{{ stats.doingCount || 0 }}</view>
          <view class="stat-label">进行中项目</view>
        </view>
        <view class="stat-item" @click="navProject(3)">
          <view class="stat-num">{{ stats.waitAcceptCount || 0 }}</view>
          <view class="stat-label">待验收</view>
        </view>
        <view class="stat-item" @click="nav('/pages/software/contract/index')">
          <view class="stat-num">{{ stats.contractCount || 0 }}</view>
          <view class="stat-label">我的合同</view>
        </view>
        <view class="stat-item" @click="nav('/pages/users/user_money/index')">
          <view class="stat-num sm">¥{{ fmtMoney(stats.balance) }}</view>
          <view class="stat-label">账户余额</view>
        </view>
      </view>

      <!-- 我的项目 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">我的项目</text>
          <text class="sec-link" @click="navProject()">全部项目</text>
        </view>
        <view v-if="projects.length" class="proj-list">
          <view v-for="(p, i) in projects" :key="i" class="proj-item" @click="navProjectDetail(p.id)">
            <view class="proj-head">
              <text class="proj-name">{{ p.name }}</text>
              <view class="proj-status" :class="'st-' + p.status">{{ p.statusText }}</view>
            </view>
            <view class="proj-bar">
              <view class="proj-bar-inner" :style="{ width: (p.percent || 0) + '%' }"></view>
            </view>
            <view class="proj-meta">
              <text>{{ p.stage || '—' }} · 进度 {{ p.percent || 0 }}%</text>
              <text v-if="p.manager">项目经理 {{ p.manager }}</text>
            </view>
          </view>
        </view>
        <view v-else class="empty-tip">暂无项目，签约后自动同步到这里</view>
      </view>

      <!-- 功能宫格 -->
      <view class="grid-card">
        <view v-for="(g, i) in menus" :key="i" class="g-item" hover-class="g-press" :hover-stay-time="80" @click="onMenu(g)">
          <image :src="g.icon" class="g-icon" mode="aspectFit" />
          <text class="g-label">{{ g.name }}</text>
        </view>
      </view>

      <!-- 最新动态 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">最新动态</text>
        </view>
        <view v-if="dynamic.length" class="dyn-list">
          <view v-for="(d, i) in dynamic" :key="i" class="dyn-item">
            <view class="dyn-dot"></view>
            <view class="dyn-body">
              <view class="dyn-text">{{ d.stage }} · 进度 {{ d.percent }}%</view>
              <view class="dyn-time">{{ fmtTime(d.createTime) }}<template v-if="d.remark"> · {{ d.remark }}</template></view>
            </view>
          </view>
        </view>
        <view v-else class="empty-tip">暂无动态</view>
      </view>

      <view style="height: 140rpx"></view>
    </view>

    <svc-footer v-if="navMode" active="user" />
  </view>
</template>

<script>
import { getSvcConfig, getSvcStats, getSvcProjects, getSvcDynamic } from '@/api/svc.js';
import { mapGetters } from 'vuex';
import svcFooter from '@/components/svcFooter/index.vue';

export default {
  components: { svcFooter },
  data() {
    return {
      config: {},
      navMode: true,
      stats: {},
      projects: [],
      dynamic: [],
      menus: [
        { name: '我的需求', icon: '/static/software/svc_require.png', type: 'nav', url: '/pages/software/demand/index' },
        { name: '工单进度', icon: '/static/software/svc_ticket.png', type: 'nav', url: '/pages/software/project/index' },
        { name: '合同管理', icon: '/static/software/svc_contract.png', type: 'nav', url: '/pages/software/contract/index' },
        { name: '发票管理', icon: '/static/software/svc_invoice.png', type: 'nav', url: '/pages/software/invoice/index' },
        { name: '资料设置', icon: '/static/software/svc_profile.png', type: 'nav', url: '/pages/users/user_info/index' },
        { name: '客服中心', icon: '/static/software/svc_service.png', type: 'nav', url: '/pages/users/customer_service/index' }
      ]
    };
  },
  computed: {
    ...mapGetters(['isLogin', 'userInfo']),
    uid() {
      return this.userInfo ? this.userInfo.uid : '';
    },
    displayName() {
      return (this.config.companyName || '黔序科技') + (this.userInfo && this.userInfo.nickname ? ' · ' + this.userInfo.nickname : '');
    },
    avatarText() {
      return (this.config.companyName || '黔').slice(0, 1);
    },
    signed() {
      return this.$store.state.app.token && this.stats.hasSigned !== false;
    }
  },
  onShow() {
    if (!this.$store.getters.isLogin) {
      uni.navigateTo({ url: '/pages/users/login/index' });
      return;
    }
    this.loadAll();
  },
  onLoad() {
    getSvcConfig()
      .then((res) => {
        this.config = res.data || {};
        this.navMode = this.config.navMode !== false;
        // 后台关闭「软件开发会员中心」后，访问该页自动回归商城会员中心
        if (!this.config.userMode) {
          uni.switchTab({ url: '/pages/user/index' });
        }
      })
      .catch(() => {});
  },
  methods: {
    loadAll() {
      getSvcStats()
        .then((res) => {
          this.stats = res.data || {};
        })
        .catch(() => {});
      getSvcProjects({ page: 1, limit: 2 })
        .then((res) => {
          this.projects = (res.data && res.data.list) || [];
        })
        .catch(() => {});
      getSvcDynamic(5)
        .then((res) => {
          this.dynamic = res.data || [];
        })
        .catch(() => {});
    },
    nav(url) {
      uni.navigateTo({ url, fail: () => {} });
    },
    navProject(status) {
      uni.navigateTo({ url: '/pages/software/project/index' + (status ? '?status=' + status : '') });
    },
    navProjectDetail(id) {
      uni.navigateTo({ url: '/pages/software/project/detail?id=' + id });
    },
    onMenu(g) {
      if (g.url) {
        uni.navigateTo({ url: g.url, fail: () => uni.showToast({ title: '页面暂未开放', icon: 'none' }) });
      } else {
        uni.showToast({ title: '敬请期待', icon: 'none' });
      }
    },
    fmtMoney(v) {
      const n = Number(v) || 0;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fmtTime(t) {
      if (!t) return '';
      const s = String(t).replace('T', ' ').slice(0, 16);
      return s;
    }
  }
};
</script>

<style scoped lang="scss">
.svc-user {
  min-height: 100vh;
  background: #f4f6fb;
}
.status-pad { height: var(--status-bar-height, 0px); }
.top-wrap {
  position: relative;
  padding: 16rpx 30rpx 90rpx;
  background: linear-gradient(150deg, #0d4fc4 0%, #1d6bf3 70%);
  overflow: hidden;
}.top-deco {
  position: absolute; border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.top-deco.d1 { width: 300rpx; height: 300rpx; right: -80rpx; top: -100rpx; }
.nav-row {
  display: flex; align-items: center; justify-content: center; position: relative;
}
.nav-title { color: #fff; font-size: 34rpx; font-weight: 700; }
.nav-gear {
  position: absolute; right: 0; color: rgba(255, 255, 255, 0.85); font-size: 40rpx;
}
.user-row { display: flex; align-items: center; margin-top: 40rpx; position: relative; }
.avatar {
  width: 96rpx; height: 96rpx; border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  color: #fff; font-size: 44rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.user-info { margin-left: 24rpx; flex: 1; }
.user-name { color: #fff; font-size: 34rpx; font-weight: 700; }
.user-badges { display: flex; align-items: center; margin-top: 12rpx; }
.badge {
  font-size: 20rpx; border-radius: 18rpx; padding: 4rpx 18rpx; margin-right: 14rpx;
}
.badge-sign { background: rgba(255, 255, 255, 0.22); color: #fff; }
.badge-id { background: rgba(255, 255, 255, 0.14); color: rgba(255, 255, 255, 0.85); }
.arrow { color: rgba(255, 255, 255, 0.8); font-size: 44rpx; }

.stats-card {
  position: relative;
  margin-top: -66rpx;
  background: #fff; border-radius: 24rpx;
  display: flex; padding: 30rpx 0;
  box-shadow: 0 10rpx 30rpx rgba(13, 79, 196, 0.10);
  z-index: 2;
}
.stat-item { flex: 1; text-align: center; }
.stat-num { font-size: 36rpx; font-weight: 700; color: #222; }
.stat-num.sm { font-size: 30rpx; }
.stat-label { font-size: 22rpx; color: #9aa4b2; margin-top: 8rpx; }

.page-body { padding: 24rpx 30rpx 0; }

.section-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.sec-head { display: flex; align-items: center; justify-content: space-between; }
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }
.sec-link { font-size: 24rpx; color: #9aa4b2; }

.proj-list { margin-top: 10rpx; }
.proj-item { padding: 22rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.proj-item:last-child { border-bottom: none; padding-bottom: 6rpx; }
.proj-head { display: flex; align-items: center; justify-content: space-between; }
.proj-name { font-size: 30rpx; font-weight: 600; color: #222; }
.proj-status {
  font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff;
}
.proj-status.st-1 { background: #f59e0b; }
.proj-status.st-2 { background: #1d6bf3; }
.proj-status.st-3 { background: #f59e0b; }
.proj-status.st-4 { background: #16a34a; }
.proj-status.st--1 { background: #9aa4b2; }
.proj-bar {
  margin-top: 18rpx; height: 14rpx; border-radius: 8rpx;
  background: #edf1f7; overflow: hidden;
}
.proj-bar-inner { height: 100%; border-radius: 8rpx; background: linear-gradient(90deg, #1d6bf3, #4b95ff); }
.proj-meta {
  display: flex; justify-content: space-between;
  margin-top: 14rpx; font-size: 22rpx; color: #9aa4b2;
}
.empty-tip { text-align: center; color: #9aa4b2; font-size: 24rpx; padding: 30rpx 0 10rpx; }

.grid-card {
  background: #fff; border-radius: 24rpx; padding: 36rpx 10rpx 6rpx;
  display: flex; flex-wrap: wrap; margin-bottom: 24rpx;
}
.g-item {
  width: 33.33%; display: flex; flex-direction: column; align-items: center;
  margin-bottom: 30rpx;
}
.g-item.g-press { opacity: 0.7; }
.g-icon { width: 72rpx; height: 72rpx; }
.g-label { font-size: 24rpx; color: #333; margin-top: 12rpx; }

.dyn-list { margin-top: 10rpx; }
.dyn-item { display: flex; padding: 16rpx 0; }
.dyn-dot {
  width: 16rpx; height: 16rpx; border-radius: 50%;
  background: #1d6bf3; margin-top: 12rpx; flex-shrink: 0;
}
.dyn-item:last-child .dyn-dot { background: #cbd5e1; }
.dyn-body { margin-left: 18rpx; flex: 1; }
.dyn-text { font-size: 26rpx; color: #333; font-weight: 500; }
.dyn-time { font-size: 22rpx; color: #9aa4b2; margin-top: 6rpx; }
</style>
