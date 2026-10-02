<template>
  <view class="svc-project">
    <view class="status-pad"></view>
    <!-- 状态筛选 -->
    <view class="tab-row">
      <view v-for="(t, i) in tabs" :key="i" class="tab-item" :class="{ active: status === t.value }" @click="onTab(t.value)">
        {{ t.name }}
      </view>
    </view>

    <view class="page-body">
      <view v-if="list.length" class="proj-list">
        <view v-for="(p, i) in list" :key="i" class="proj-card" @click="navDetail(p.id)">
          <view class="proj-head">
            <text class="proj-name">{{ p.name }}</text>
            <view class="proj-status" :class="'st-' + p.status">{{ p.statusText }}</view>
          </view>
          <view class="proj-meta">
            <text v-if="p.type">{{ p.type }}</text>
            <text v-if="p.cycle">{{ p.cycle }}</text>
          </view>
          <view class="proj-bar">
            <view class="proj-bar-inner" :style="{ width: (p.percent || 0) + '%' }"></view>
          </view>
          <view class="proj-foot">
            <text>{{ p.stage || '—' }} · 进度 {{ p.percent || 0 }}%</text>
            <text v-if="p.manager">项目经理 {{ p.manager }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-tip">暂无项目</view>
      <view style="height: 140rpx"></view>
    </view>
    <svc-footer v-if="navMode" active="project" />
  </view>
</template>

<script>
import { getSvcProjects, getSvcConfig } from '@/api/svc.js';
import svcFooter from '@/components/svcFooter/index.vue';

export default {
  components: { svcFooter },
  data() {
    return {
      navMode: true,
      status: undefined,
      list: [],
      tabs: [
        { name: '全部', value: undefined },
        { name: '进行中', value: 2 },
        { name: '待验收', value: 3 },
        { name: '已完成', value: 4 }
      ]
    };
  },
  onLoad(options) {
    if (options && options.status) {
      this.status = Number(options.status);
    }
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
    onTab(v) {
      this.status = v;
      this.loadList();
    },
    loadList() {
      const data = { page: 1, limit: 50 };
      if (this.status !== undefined) data.status = this.status;
      getSvcProjects(data)
        .then((res) => {
          this.list = (res.data && res.data.list) || [];
        })
        .catch(() => {});
    },
    navDetail(id) {
      uni.navigateTo({ url: '/pages/software/project/detail?id=' + id });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-project { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.tab-row {
  display: flex; background: #fff; padding: 20rpx 30rpx 0;
}
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
.proj-card {
  background: #fff; border-radius: 22rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.proj-head { display: flex; align-items: center; justify-content: space-between; }
.proj-name { font-size: 32rpx; font-weight: 700; color: #222; }
.proj-status { font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff; }
.proj-status.st-1 { background: #f59e0b; }
.proj-status.st-2 { background: #1d6bf3; }
.proj-status.st-3 { background: #f59e0b; }
.proj-status.st-4 { background: #16a34a; }
.proj-status.st--1 { background: #9aa4b2; }
.proj-meta { display: flex; margin-top: 12rpx; font-size: 22rpx; color: #9aa4b2; }
.proj-meta text { margin-right: 24rpx; }
.proj-bar {
  margin-top: 22rpx; height: 14rpx; border-radius: 8rpx;
  background: #edf1f7; overflow: hidden;
}
.proj-bar-inner { height: 100%; border-radius: 8rpx; background: linear-gradient(90deg, #1d6bf3, #4b95ff); }
.proj-foot {
  display: flex; justify-content: space-between;
  margin-top: 16rpx; font-size: 22rpx; color: #9aa4b2;
}
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; padding: 100rpx 0; }
</style>
