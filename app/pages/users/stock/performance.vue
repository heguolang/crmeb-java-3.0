<template>
  <view class="perf-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">业绩中心</view>
      <view class="page-sub">个人与团队订货业绩实时汇总</view>
      <picker :range="rangeNames" @change="onRangeChange">
        <view class="range-pill">
          <text class="rp-label">统计范围</text>
          <text class="rp-value">{{ rangeNames[rangeIndex] }}</text>
          <text class="rp-arrow">▾</text>
        </view>
      </picker>
    </view>

    <view class="page-body">
      <!-- 统计卡 -->
      <view class="stat-card">
        <view class="stat-item">
          <view class="stat-num">¥{{ perf.selfPerformance || 0 }}</view>
          <view class="stat-label">个人业绩</view>
          <view class="stat-sub">{{ perf.selfOrderCount || 0 }} 单</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-num">¥{{ perf.teamPerformance || 0 }}</view>
          <view class="stat-label">团队业绩</view>
          <view class="stat-sub">{{ perf.teamOrderCount || 0 }} 单</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-num">{{ perf.subAgentCount || 0 }}</view>
          <view class="stat-label">团队代理</view>
          <view class="stat-sub">含间接</view>
        </view>
      </view>

      <!-- 口径说明 -->
      <view class="note-bar">
        <view class="note-ico">i</view>
        <view class="note-txt">个人业绩 = 我的已完成订货单金额；团队业绩 = 名下全部下级的已完成订货单金额。</view>
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyStockPerformance } from '@/api/stock.js';
	export default {
		data() {
			return {
				perf: {},
				rangeIndex: 0,
				rangeNames: ['全部', '本月', '上月', '近30天']
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			onRangeChange(e) {
				this.rangeIndex = Number(e.detail.value);
				this.load();
			},
			load() {
				let dateLimit = '';
				if (this.rangeIndex === 1) dateLimit = this.monthStr(0);
				else if (this.rangeIndex === 2) dateLimit = this.monthStr(-1);
				getMyStockPerformance({ dateLimit }).then(res => { this.perf = res.data || {}; });
			},
			monthStr(offset) {
				const d = new Date();
				let y = d.getFullYear(), m = d.getMonth() + 1 + offset;
				if (m <= 0) { m += 12; y -= 1; }
				return y + '-' + (m < 10 ? '0' + m : m);
			}
		}
	};
</script>

<style lang="scss" scoped>
.perf-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 86rpx;
  background: linear-gradient(160deg, #2b6fe3 0%, #4a9df8 70%, #6dadf9 100%);
  overflow: hidden;
  .top-deco { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.10); }
  .d1 { width: 240rpx; height: 240rpx; right: -70rpx; top: -100rpx; background: rgba(255,255,255,0.14); }
  .d2 { width: 130rpx; height: 130rpx; left: -50rpx; bottom: -30rpx; }
}
.page-title {
  position: relative;
  z-index: 1;
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.12);
}
.page-sub {
  position: relative;
  z-index: 1;
  margin-top: 12rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1rpx;
}
/* 统计范围胶囊：嵌在渐变头内 */
.range-pill {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  margin-top: 24rpx;
  background: rgba(255, 255, 255, 0.20);
  border: 1rpx solid rgba(255, 255, 255, 0.40);
  border-radius: 999rpx;
  padding: 10rpx 26rpx;
}
.rp-label { font-size: 22rpx; color: rgba(255, 255, 255, 0.75); margin-right: 14rpx; }
.rp-value { font-size: 25rpx; color: #fff; font-weight: 600; }
.rp-arrow { font-size: 22rpx; color: rgba(255, 255, 255, 0.8); margin-left: 10rpx; }

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -56rpx;
}

/* ---------- 统计卡 ---------- */
.stat-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 36rpx 16rpx;
  display: flex;
  align-items: stretch;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.stat-item { flex: 1; text-align: center; padding: 0 8rpx; }
.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #2b6fe3;
  line-height: 1.15;
  word-break: break-all;
}
.stat-label { margin-top: 12rpx; font-size: 24rpx; color: #3d4a5f; font-weight: 600; }
.stat-sub { margin-top: 6rpx; font-size: 21rpx; color: #a4adc0; }
.stat-divider { width: 1rpx; background: #eef1f6; }

/* ---------- 口径说明 ---------- */
.note-bar {
  display: flex;
  align-items: flex-start;
  margin-top: 20rpx;
  background: #eef5ff;
  border-radius: 16rpx;
  padding: 18rpx 22rpx;
}
.note-ico {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  background: #2b6fe3;
  color: #fff;
  font-size: 22rpx;
  font-style: italic;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 14rpx;
}
.note-txt { flex: 1; font-size: 22rpx; color: #3d6db5; line-height: 34rpx; }
</style>
