<template>
  <view class="svc-ct">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">我的合同</text>
    </view>

    <!-- 状态筛选 -->
    <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
      <view class="tab-row">
        <view
          v-for="(t, i) in tabs"
          :key="i"
          class="tab-item"
          :class="{ active: status === t.value }"
          @click="onTab(t.value)"
        >{{ t.name }}</view>
      </view>
    </scroll-view>

    <view class="page-body">
      <view v-if="list.length" class="ct-list">
        <view v-for="(c, i) in list" :key="i" class="ct-card" @click="navDetail(c.id)">
          <view class="ct-head">
            <text class="ct-title">{{ c.title }}</text>
            <view class="ct-status" :class="'st-' + c.status">{{ c.statusText || '—' }}</view>
          </view>

          <view class="kv-row">
            <text class="kv-k">合同编号</text>
            <text class="kv-v mono">{{ c.contractNo }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">签约渠道</text>
            <text class="kv-v">{{ channelText(c) }}</text>
          </view>
          <view v-if="c.signChannel === 'electronic' && c.platformNo" class="kv-row">
            <text class="kv-k">存证编号</text>
            <text class="kv-v mono">{{ c.platformNo }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">合同金额</text>
            <text class="kv-v strong">¥{{ fmtMoney(c.amount) }}</text>
          </view>

          <view v-if="tipOf(c)" class="ct-tip">{{ tipOf(c) }}</view>

          <view v-if="c.signedFile" class="ct-file" @click.stop="openFile(c.signedFile)">
            <view class="cf-ic">PDF</view>
            <view class="cf-body">
              <view class="cf-name">{{ c.signedFileName || '双方签署完成的成品件' }}</view>
              <view class="cf-sub">点击查看 / 下载</view>
            </view>
            <text class="cf-go">下载</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-wrap">
        <view class="empty-tip">还没有合同</view>
        <view class="empty-sub">签署后会出现在这里，状态由服务顾问更新</view>
      </view>
      <view style="height: 60rpx"></view>
    </view>
  </view>
</template>

<script>
import { getSvcContracts } from '@/api/svc.js';

export default {
  data() {
    return {
      status: undefined,
      list: [],
      tabs: [
        { name: '全部', value: undefined },
        { name: '待签署', value: 1 },
        { name: '已签署', value: 2 },
        { name: '已生效', value: 3 },
        { name: '已终止', value: -1 }
      ]
    };
  },
  onShow() {
    this.loadList();
  },
  methods: {
    onTab(v) {
      this.status = v;
      this.loadList();
    },
    loadList() {
      const data = { page: 1, limit: 50 };
      if (this.status !== undefined) data.status = this.status;
      getSvcContracts(data)
        .then((res) => {
          this.list = (res.data && res.data.list) || [];
        })
        .catch(() => {});
    },
    channelText(c) {
      const base = c.channelText || '纸质合同';
      if (c.signChannel === 'electronic' && c.signPlatform) return base + ' · ' + c.signPlatform;
      if (c.signChannel !== 'electronic' && c.signType) return base + ' · ' + c.signType;
      return base;
    },
    tipOf(c) {
      if (c.status === 1 && c.signChannel !== 'electronic' && c.expressNo) {
        return `合同已寄出（${c.expressCo || '快递'} ${c.expressNo}），签署完成后由顾问登记状态。`;
      }
      if (c.status === 1) return '合同待签署，顾问会把进度同步到这里。';
      if (c.status === -1) return '该合同已终止，记录仅作留档。';
      return '';
    },
    fmtMoney(v) {
      const n = Number(v) || 0;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fullUrl(u) {
      const s = String(u || '');
      if (!s) return '';
      if (/^https?:\/\//.test(s)) return s;
      return s.indexOf('/') === 0 ? s : '/' + s;
    },
    openFile(url) {
      const full = this.fullUrl(url);
      if (!full) return;
      // #ifdef H5
      window.open(full, '_blank');
      // #endif
      // #ifndef H5
      uni.showLoading({ title: '打开中...' });
      uni.downloadFile({
        url: full,
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200) {
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              fail: () => uni.showToast({ title: '该文件暂不支持预览', icon: 'none' })
            });
          } else {
            uni.showToast({ title: '文件下载失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '文件下载失败', icon: 'none' });
        }
      });
      // #endif
    },
    navDetail(id) {
      uni.navigateTo({ url: '/pages/software/contract/detail?id=' + id });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-ct { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.tab-scroll { background: #fff; white-space: nowrap; }
.tab-row { display: inline-flex; padding: 0 20rpx; }
.tab-item {
  font-size: 28rpx; color: #666; padding: 20rpx 26rpx;
  position: relative;
}
.tab-item.active { color: #1d6bf3; font-weight: 700; }
.tab-item.active::after {
  content: ''; position: absolute; left: 24rpx; right: 24rpx; bottom: 0;
  height: 6rpx; border-radius: 4rpx; background: #1d6bf3;
}

.page-body { padding: 24rpx 30rpx 0; }

.ct-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.ct-head { display: flex; align-items: flex-start; justify-content: space-between; }
.ct-title { font-size: 30rpx; font-weight: 700; color: #222; flex: 1; margin-right: 16rpx; line-height: 1.4; }
.ct-status {
  font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx;
  color: #fff; flex-shrink: 0;
}
.ct-status.st-0 { background: #9aa4b2; }
.ct-status.st-1 { background: #f59e0b; }
.ct-status.st-2 { background: #1d6bf3; }
.ct-status.st-3 { background: #16a34a; }
.ct-status.st--1 { background: #9aa4b2; }

.kv-row { display: flex; align-items: flex-start; margin-top: 14rpx; }
.kv-k { width: 140rpx; flex-shrink: 0; font-size: 24rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 26rpx; color: #333; word-break: break-all; }
.kv-v.strong { font-weight: 700; color: #1d6bf3; }
.mono { font-family: Consolas, Monaco, monospace; }

.ct-tip {
  margin-top: 16rpx; font-size: 22rpx; color: #a06b16;
  background: #fdf6ec; border-radius: 12rpx; padding: 14rpx 16rpx; line-height: 1.6;
}

.ct-file {
  display: flex; align-items: center; margin-top: 20rpx;
  background: #f7f9fc; border-radius: 16rpx; padding: 16rpx 18rpx;
}
.cf-ic {
  width: 64rpx; height: 64rpx; border-radius: 14rpx; background: #eef3fd;
  color: #1d6bf3; font-size: 18rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.cf-body { flex: 1; margin-left: 16rpx; min-width: 0; }
.cf-name { font-size: 24rpx; color: #333; }
.cf-sub { font-size: 20rpx; color: #9aa4b2; margin-top: 4rpx; }
.cf-go { font-size: 24rpx; color: #1d6bf3; flex-shrink: 0; }

.empty-wrap { padding: 160rpx 0 0; }
.empty-tip { text-align: center; color: #6b7785; font-size: 28rpx; }
.empty-sub { text-align: center; color: #9aa4b2; font-size: 24rpx; margin-top: 14rpx; }
</style>
