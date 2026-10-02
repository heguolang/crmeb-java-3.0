<template>
  <view class="svc-case">
    <view class="status-pad"></view>
    <view class="page-body">
      <view v-if="list.length" class="case-grid">
        <view v-for="(c, i) in list" :key="i" class="case-item">
          <view class="case-img-wrap">
            <image v-if="c.image" :src="c.image" class="case-img" mode="aspectFill" />
            <view v-else class="case-img case-ph">{{ (c.category || '案例').slice(0, 2) }}</view>
          </view>
          <view class="case-body">
            <view class="case-tag">{{ c.category || '小程序' }}</view>
            <view class="case-title">{{ c.title }}</view>
            <view class="case-desc">{{ c.description }}</view>
          </view>
        </view>
      </view>
      <view v-else class="empty-tip">案例整理中，欢迎咨询</view>
      <view style="height: 140rpx"></view>
    </view>
    <svc-footer v-if="navMode" active="case" />
  </view>
</template>

<script>
import { getSvcCases, getSvcConfig } from '@/api/svc.js';
import svcFooter from '@/components/svcFooter/index.vue';

export default {
  components: { svcFooter },
  data() {
    return { list: [], navMode: true };
  },
  onLoad() {
    getSvcConfig()
      .then((res) => {
        this.navMode = res.data && res.data.navMode !== false;
      })
      .catch(() => {});
  },
  onShow() {
    this.loadList();
  },
  methods: {
    loadList() {
      getSvcCases(50)
        .then((res) => {
          this.list = res.data || [];
        })
        .catch(() => {});
    }
  }
};
</script>

<style scoped lang="scss">
.svc-case { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.page-body { padding: 30rpx; }
.case-grid { display: flex; flex-wrap: wrap; justify-content: space-between; }
.case-item {
  width: calc(50% - 12rpx); background: #fff; border-radius: 20rpx;
  overflow: hidden; margin-bottom: 24rpx;
}
.case-img-wrap { border-radius: 14rpx; overflow: hidden; }
.case-img { width: 100%; height: 240rpx; display: block; }
.case-ph {
  background: linear-gradient(135deg, #dce9ff, #f0f5ff);
  color: #1d6bf3; font-size: 44rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.case-body { padding: 20rpx; }
.case-tag {
  display: inline-block; font-size: 18rpx; color: #1d6bf3;
  background: rgba(29, 107, 243, 0.1); border-radius: 16rpx; padding: 2rpx 14rpx;
}
.case-title { font-size: 28rpx; font-weight: 600; color: #222; margin-top: 12rpx; }
.case-desc { font-size: 22rpx; color: #9aa4b2; margin-top: 8rpx; line-height: 1.5; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; padding: 100rpx 0; }
</style>
