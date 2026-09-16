<template>
  <view class="perf-page">
    <view class="range-bar">
      <picker :range="rangeNames" @change="onRangeChange">
        <view class="range-picker">{{ rangeNames[rangeIndex] }} ▾</view>
      </picker>
    </view>
    <view class="stat-grid">
      <view class="stat-card">
        <view class="sc-num">¥{{ perf.selfPerformance || 0 }}</view>
        <view class="sc-label">个人业绩（{{ perf.selfOrderCount || 0 }}单）</view>
      </view>
      <view class="stat-card">
        <view class="sc-num">¥{{ perf.teamPerformance || 0 }}</view>
        <view class="sc-label">团队业绩（{{ perf.teamOrderCount || 0 }}单）</view>
      </view>
    </view>
    <view class="stat-grid">
      <view class="stat-card wide">
        <view class="sc-num">{{ perf.subAgentCount || 0 }}</view>
        <view class="sc-label">团队代理人数（含间接）</view>
      </view>
    </view>
    <view class="note">个人业绩 = 我的已完成订货单金额；团队业绩 = 名下全部下级的已完成订货单金额。</view>
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
.perf-page { min-height: 100vh; background: #f5f6f8; padding: 24rpx; }
.range-bar { margin-bottom: 20rpx; }
.range-picker { display: inline-block; background: #fff; border-radius: 30rpx; padding: 10rpx 30rpx; font-size: 26rpx; color: #333; }
.stat-grid { display: flex; gap: 20rpx; margin-bottom: 20rpx; }
.stat-card { flex: 1; background: #fff; border-radius: 16rpx; padding: 36rpx 30rpx; }
.stat-card.wide { flex: 2; }
.sc-num { font-size: 40rpx; font-weight: 600; color: #2b6fe3; }
.sc-label { font-size: 24rpx; color: #999; margin-top: 10rpx; }
.note { font-size: 22rpx; color: #b0b6bf; line-height: 1.6; padding: 10rpx; }
</style>
