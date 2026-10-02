<template>
  <view class="svc-contract">
    <view class="status-pad"></view>
    <!-- 状态筛选 -->
    <view class="tab-row">
      <view v-for="(t, i) in tabs" :key="i" class="tab-item" :class="{ active: status === t.value }" @click="onTab(t.value)">
        {{ t.name }}
      </view>
    </view>

    <view class="page-body">
      <view v-if="list.length" class="ct-list">
        <view v-for="(c, i) in list" :key="i" class="ct-card" @click="navDetail(c.id)">
          <view class="ct-head">
            <text class="ct-title">{{ c.title }}</text>
            <view class="ct-status" :class="'st-' + c.status">{{ statusText(c.status) }}</view>
          </view>
          <view class="ct-no">编号 {{ c.contractNo }}</view>
          <view class="ct-prog">
            <text class="prog-chip" :class="c.custSigned ? 'is-ok' : 'is-wait'">甲方{{ c.custSigned ? '✓' : '…' }}</text>
            <text class="prog-chip" :class="c.ourSigned ? 'is-ok' : 'is-wait'">乙方{{ c.ourSigned ? '✓' : '…' }}</text>
            <text v-if="c.finished" class="prog-chip is-done">正式合同</text>
            <text v-if="c.myRole" class="prog-chip is-mine">{{ c.myRole === 'cust' ? '我是甲方' : '我是乙方' }}</text>
          </view>
          <view class="ct-foot">
            <text class="ct-amount">¥{{ c.amount }}</text>
            <text class="ct-time">{{ fmtTime(c.signTime || c.createTime) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-tip">暂无合同</view>
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
        { name: '待签约', value: 0 },
        { name: '已签约', value: 1 },
        { name: '已驳回', value: 2 }
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
    statusText(s) {
      return { 0: '待签约', 1: '已签约', 2: '已驳回' }[s] || '—';
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 16) : '';
    },
    navDetail(id) {
      uni.navigateTo({ url: '/pages/software/contract/detail?id=' + id });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-contract { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.tab-row { display: flex; background: #fff; padding: 20rpx 30rpx 0; }
.tab-item {
  flex: 1; text-align: center; font-size: 28rpx; color: #666;
  padding-bottom: 20rpx; position: relative;
}
.tab-item.active { color: #1d6bf3; font-weight: 700; }
.tab-item.active::after {
  content: ''; position: absolute; left: 30%; right: 30%; bottom: 0;
  height: 6rpx; border-radius: 4rpx; background: #1d6bf3;
}
.page-body { padding: 30rpx; }
.ct-card {
  background: #fff; border-radius: 22rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.ct-head { display: flex; align-items: center; justify-content: space-between; }
.ct-title { font-size: 30rpx; font-weight: 700; color: #222; }
.ct-status { font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff; }
.ct-status.st-0 { background: #f59e0b; }
.ct-status.st-1 { background: #16a34a; }
.ct-status.st-2 { background: #ef4444; }
.ct-no { font-size: 22rpx; color: #9aa4b2; margin-top: 12rpx; }
.ct-prog { display: flex; align-items: center; margin-top: 14rpx; }
.prog-chip {
  font-size: 20rpx; line-height: 34rpx; padding: 0 14rpx;
  border-radius: 18rpx; border: 1rpx solid; margin-right: 12rpx;
}
.prog-chip.is-ok { color: #1f8a4c; background: #eefaf1; border-color: #bfe6cd; }
.prog-chip.is-wait { color: #b8781f; background: #fff7ec; border-color: #f3d19e; }
.prog-chip.is-done { color: #1d6bf3; background: #ecf5ff; border-color: #d9ecff; }
.prog-chip.is-mine { color: #5a6675; background: #f2f4f8; border-color: #dfe5ee; margin-right: 0; }
.ct-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 18rpx; }
.ct-amount { font-size: 32rpx; font-weight: 700; color: #1d6bf3; }
.ct-time { font-size: 22rpx; color: #9aa4b2; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; padding: 100rpx 0; }
</style>
