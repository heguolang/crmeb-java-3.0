<template>
  <view class="svc-detail">
    <view class="status-pad"></view>
    <view class="page-body" v-if="project">
      <!-- 项目概要 -->
      <view class="head-card">
        <view class="head-row">
          <text class="head-name">{{ project.name }}</text>
          <view class="head-status" :class="'st-' + project.status">{{ project.statusText }}</view>
        </view>
        <view class="head-meta">
          <view v-if="project.type" class="hm-item"><text class="hm-label">类型</text>{{ project.type }}</view>
          <view v-if="project.cycle" class="hm-item"><text class="hm-label">周期</text>{{ project.cycle }}</view>
          <view v-if="project.manager" class="hm-item"><text class="hm-label">项目经理</text>{{ project.manager }}</view>
        </view>
        <view class="head-progress">
          <view class="hp-bar">
            <view class="hp-inner" :style="{ width: (project.percent || 0) + '%' }"></view>
          </view>
          <view class="hp-text">
            <text>{{ project.stage || '—' }}</text>
            <text class="hp-percent">{{ project.percent || 0 }}%</text>
          </view>
        </view>
      </view>

      <!-- 进度记录 -->
      <view class="section-card">
        <view class="sec-title">项目进度</view>
        <view v-if="records.length" class="tl-list">
          <view v-for="(r, i) in records" :key="i" class="tl-item">
            <view class="tl-left">
              <view class="tl-dot" :class="{ last: i === records.length - 1 }"></view>
              <view v-if="i !== records.length - 1" class="tl-line"></view>
            </view>
            <view class="tl-body">
              <view class="tl-head">
                <text class="tl-stage">{{ r.stage }}</text>
                <text class="tl-percent">{{ r.percent }}%</text>
              </view>
              <view v-if="r.remark" class="tl-remark">{{ r.remark }}</view>
              <view class="tl-time">{{ fmtTime(r.createTime) }}</view>
            </view>
          </view>
        </view>
        <view v-else class="empty-tip">暂无进度记录</view>
      </view>

      <view style="height: 60rpx"></view>
    </view>
  </view>
</template>

<script>
import { getSvcProjectInfo } from '@/api/svc.js';

export default {
  data() {
    return { project: null, records: [] };
  },
  onLoad(options) {
    if (options && options.id) {
      this.loadDetail(options.id);
    }
  },
  methods: {
    loadDetail(id) {
      getSvcProjectInfo(id)
        .then((res) => {
          this.project = res.data || {};
          this.records = this.project.progressList || [];
        })
        .catch((e) => {
          uni.showToast({ title: typeof e === 'string' ? e : '加载失败', icon: 'none' });
        });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 16) : '';
    }
  }
};
</script>

<style scoped lang="scss">
.svc-detail { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.page-body { padding: 30rpx; }
.head-card {
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  border-radius: 24rpx; padding: 34rpx 30rpx; color: #fff;
}
.head-row { display: flex; align-items: center; justify-content: space-between; }
.head-name { font-size: 36rpx; font-weight: 700; }
.head-status {
  font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx;
  background: rgba(255, 255, 255, 0.22); color: #fff;
}
.head-meta { display: flex; flex-wrap: wrap; margin-top: 22rpx; }
.hm-item { font-size: 24rpx; margin-right: 36rpx; opacity: 0.9; margin-bottom: 8rpx; }
.hm-label { opacity: 0.7; margin-right: 8rpx; }
.head-progress { margin-top: 20rpx; }
.hp-bar { height: 16rpx; border-radius: 10rpx; background: rgba(255, 255, 255, 0.25); overflow: hidden; }
.hp-inner { height: 100%; border-radius: 10rpx; background: #fff; }
.hp-text { display: flex; justify-content: space-between; margin-top: 14rpx; font-size: 24rpx; }
.hp-percent { font-weight: 700; }

.section-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-top: 24rpx;
}
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }

.tl-list { margin-top: 26rpx; }
.tl-item { display: flex; }
.tl-left { display: flex; flex-direction: column; align-items: center; width: 30rpx; }
.tl-dot {
  width: 18rpx; height: 18rpx; border-radius: 50%;
  background: #1d6bf3; margin-top: 8rpx; flex-shrink: 0;
}
.tl-dot.last { background: #1d6bf3; box-shadow: 0 0 0 8rpx rgba(29, 107, 243, 0.15); }
.tl-line { flex: 1; width: 2rpx; background: #e6ebf3; margin: 6rpx 0; }
.tl-body { flex: 1; margin-left: 20rpx; padding-bottom: 36rpx; }
.tl-head { display: flex; align-items: center; justify-content: space-between; }
.tl-stage { font-size: 28rpx; font-weight: 600; color: #222; }
.tl-percent { font-size: 26rpx; font-weight: 700; color: #1d6bf3; }
.tl-remark { font-size: 24rpx; color: #666; margin-top: 10rpx; line-height: 1.6; }
.tl-time { font-size: 22rpx; color: #9aa4b2; margin-top: 10rpx; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; padding: 60rpx 0 20rpx; }
</style>
