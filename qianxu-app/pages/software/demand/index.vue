<template>
  <view class="svc-dm">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">我的需求</text>
    </view>

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
      <view v-if="list.length" class="dm-list">
        <view v-for="(d, i) in list" :key="i" class="dm-card">
          <view class="dm-head">
            <text class="dm-title">{{ d.title }}</text>
            <view class="dm-status" :class="'st-' + d.status">{{ d.statusText || '—' }}</view>
          </view>
          <view class="dm-no">{{ d.demandNo }}<template v-if="d.type"> · {{ d.type }}</template></view>

          <!-- 已报价：展示报价 -->
          <view v-if="d.status === 2 || (d.status === 3 && Number(d.amount) > 0)" class="quote-box">
            <view class="qb-row">
              <text class="qb-k">报价金额</text>
              <text class="qb-v">¥{{ fmtMoney(d.amount) }}</text>
            </view>
            <view v-if="d.quoteDesc" class="qb-desc">{{ d.quoteDesc }}</view>
          </view>
          <view v-else class="dm-info">
            <view class="kv-row">
              <text class="kv-k">预算</text>
              <text class="kv-v">{{ d.budget || '待沟通' }}</text>
            </view>
            <view class="kv-row">
              <text class="kv-k">期望完成</text>
              <text class="kv-v">{{ d.hopeDate || '待沟通' }}</text>
            </view>
          </view>

          <view class="dm-meta">
            <text>提交 {{ fmtTime(d.createTime) }}</text>
            <text>{{ d.follower ? '顾问 ' + d.follower : '顾问待指派' }}</text>
          </view>

          <view v-if="d.contractNo" class="dm-contract" @click="navContract(d.contractId)">
            <text>已转合同 {{ d.contractNo }}</text>
            <text class="dc-go">查看 ›</text>
          </view>

          <view class="dm-ops">
            <view class="op-btn line" @click="navDetail(d.id)">查看详情</view>
            <view v-if="d.status === 2" class="op-btn pri" @click="onAccept(d)">同意报价</view>
            <view v-else-if="d.status === 0" class="op-btn line gray" @click="onCancel(d)">撤销</view>
            <view v-else-if="d.status === 3" class="op-btn line" @click="navDetail(d.id)">查看进度</view>
          </view>
        </view>
      </view>

      <view v-else class="empty-wrap">
        <view class="empty-tip">还没有提交过需求</view>
        <view class="empty-sub">把想做的系统告诉我们，顾问会给你报价与周期</view>
      </view>
      <view style="height: 160rpx"></view>
    </view>

    <view class="fab" @click="navSubmit">＋ 提交需求</view>
  </view>
</template>

<script>
import { getSvcDemands, cancelSvcDemand, acceptSvcDemandQuote } from '@/api/svc.js';

export default {
  data() {
    return {
      status: undefined,
      list: [],
      tabs: [
        { name: '全部', value: undefined },
        { name: '待受理', value: 0 },
        { name: '已报价', value: 2 },
        { name: '已成单', value: 3 }
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
      getSvcDemands(data)
        .then((res) => {
          this.list = (res.data && res.data.list) || [];
        })
        .catch(() => {});
    },
    fmtMoney(v) {
      const n = Number(v) || 0;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 16) : '';
    },
    navDetail(id) {
      uni.navigateTo({ url: '/pages/software/demand/detail?id=' + id });
    },
    navSubmit() {
      uni.navigateTo({ url: '/pages/software/demand/submit' });
    },
    navContract(id) {
      if (!id) return;
      uni.navigateTo({ url: '/pages/software/contract/detail?id=' + id, fail: () => {} });
    },
    onCancel(d) {
      uni.showModal({
        title: '撤销需求',
        content: `确认撤销「${d.title}」？撤销后不可恢复。`,
        success: (r) => {
          if (!r.confirm) return;
          cancelSvcDemand(d.id)
            .then(() => {
              uni.showToast({ title: '已撤销', icon: 'success' });
              this.loadList();
            })
            .catch((e) => uni.showToast({ title: typeof e === 'string' ? e : '撤销失败', icon: 'none' }));
        }
      });
    },
    onAccept(d) {
      uni.showModal({
        title: '同意报价',
        content: `报价 ¥${this.fmtMoney(d.amount)}，同意后顾问会为您生成合同。`,
        success: (r) => {
          if (!r.confirm) return;
          acceptSvcDemandQuote(d.id)
            .then(() => {
              uni.showToast({ title: '已同意，等待生成合同', icon: 'none' });
              this.loadList();
            })
            .catch((e) => uni.showToast({ title: typeof e === 'string' ? e : '操作失败', icon: 'none' }));
        }
      });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-dm { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.tab-scroll { background: #fff; white-space: nowrap; }
.tab-row { display: inline-flex; padding: 0 20rpx; }
.tab-item { font-size: 28rpx; color: #666; padding: 20rpx 26rpx; position: relative; }
.tab-item.active { color: #1d6bf3; font-weight: 700; }
.tab-item.active::after {
  content: ''; position: absolute; left: 24rpx; right: 24rpx; bottom: 0;
  height: 6rpx; border-radius: 4rpx; background: #1d6bf3;
}

.page-body { padding: 24rpx 30rpx 0; }

.dm-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.dm-head { display: flex; align-items: flex-start; justify-content: space-between; }
.dm-title { font-size: 30rpx; font-weight: 700; color: #222; flex: 1; margin-right: 16rpx; line-height: 1.4; }
.dm-status { font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff; flex-shrink: 0; }
.dm-status.st-0 { background: #f59e0b; }
.dm-status.st-1 { background: #1d6bf3; }
.dm-status.st-2 { background: #8e6ad8; }
.dm-status.st-3 { background: #16a34a; }
.dm-status.st-4 { background: #9aa4b2; }
.dm-no { font-size: 22rpx; color: #9aa4b2; margin-top: 12rpx; }

.dm-info { margin-top: 16rpx; }
.kv-row { display: flex; margin-top: 10rpx; }
.kv-k { width: 140rpx; flex-shrink: 0; font-size: 24rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 24rpx; color: #333; }

.quote-box {
  margin-top: 16rpx; background: #f4f8ff; border-radius: 16rpx; padding: 18rpx 20rpx;
  border-left: 6rpx solid #1d6bf3;
}
.qb-row { display: flex; align-items: baseline; justify-content: space-between; }
.qb-k { font-size: 24rpx; color: #6b7785; }
.qb-v { font-size: 34rpx; font-weight: 700; color: #1d6bf3; }
.qb-desc { font-size: 23rpx; color: #6b7785; margin-top: 10rpx; line-height: 1.6; }

.dm-meta {
  display: flex; justify-content: space-between;
  margin-top: 18rpx; font-size: 22rpx; color: #9aa4b2;
}

.dm-contract {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 16rpx; background: #f0faf3; border-radius: 12rpx; padding: 14rpx 18rpx;
  font-size: 23rpx; color: #1f8a4c;
}
.dc-go { color: #1f8a4c; }

.dm-ops { display: flex; justify-content: flex-end; margin-top: 20rpx; }
.op-btn {
  font-size: 25rpx; padding: 12rpx 30rpx; border-radius: 32rpx; margin-left: 16rpx;
  &.line { border: 1rpx solid #d6dde8; color: #5a6675; background: #fff; }
  &.line.gray { color: #9aa4b2; }
  &.pri { background: linear-gradient(135deg, #0d4fc4, #1d6bf3); color: #fff; }
}

.empty-wrap { padding: 160rpx 0 0; }
.empty-tip { text-align: center; color: #6b7785; font-size: 28rpx; }
.empty-sub { text-align: center; color: #9aa4b2; font-size: 24rpx; margin-top: 14rpx; line-height: 1.7; }

.fab {
  position: fixed; right: 40rpx; bottom: 80rpx;
  height: 88rpx; padding: 0 40rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 28rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12rpx 28rpx rgba(29, 107, 243, 0.35);
  z-index: 20;
}
</style>
