<template>
  <view class="bonus-page">
    <view class="balance-card">
      <view class="bc-label">累计奖励（已并入佣金余额）</view>
      <view class="bc-num">¥{{ bonus.totalReward || 0 }}</view>
      <view class="bc-row">
        <text>佣金余额 ¥{{ bonus.commission || 0 }}</text>
      </view>
      <button class="wd-btn" @click="goWithdraw">申请提现</button>
    </view>
    <view class="type-row">
      <view class="type-item"><text class="ti-num">¥{{ bonus.diffReward || 0 }}</text><text class="ti-label">差价奖励</text></view>
      <view class="type-item"><text class="ti-num">¥{{ bonus.ladderReward || 0 }}</text><text class="ti-label">阶梯奖励</text></view>
      <view class="type-item"><text class="ti-num">¥{{ bonus.peerReward || 0 }}</text><text class="ti-label">平级奖励</text></view>
    </view>

    <view class="list-card">
      <view class="tabs">
        <view class="tab-item active">奖励明细</view>
      </view>
      <view v-for="r in rewards" :key="r.id" class="item-card">
        <view class="item-line">
          <text class="item-title">{{ typeName(r.type) }}</text>
          <text class="item-money">+¥{{ r.rewardPrice }}</text>
        </view>
        <view class="item-sub">{{ r.mark }}</view>
        <view class="item-sub grey">{{ r.createTime }}</view>
      </view>
      <view v-if="!rewards.length" class="empty">暂无奖励明细</view>
    </view>
  </view>
</template>

<script>
	import { getMyStockBonus, getMyStockRewards } from '@/api/stock.js';
	export default {
		data() {
			return {
				bonus: {},
				rewards: []
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			typeName(t) {
				return { 1: '差价奖励', 2: '阶梯奖励', 3: '平级奖励' }[t] || '奖励';
			},
			load() {
				getMyStockBonus().then(res => { this.bonus = res.data || {}; });
				getMyStockRewards({ page: 1, limit: 30 }).then(res => { this.rewards = res.data.list || []; });
			},
			goWithdraw() {
				// 跳转系统统一的佣金提现页面，走佣金提现逻辑
				uni.navigateTo({ url: '/pages/users/user_cash/index' });
			}
		}
	};
</script>

<style lang="scss" scoped>
.bonus-page { min-height: 100vh; background: #f5f6f8; padding: 24rpx; padding-bottom: 60rpx; }
.balance-card { background: linear-gradient(135deg, #e6b33c, #f0cd6e); border-radius: 20rpx; padding: 40rpx 36rpx; color: #fff; }
.bc-label { font-size: 26rpx; opacity: .9; }
.bc-num { font-size: 64rpx; font-weight: 700; margin: 12rpx 0; }
.bc-row { display: flex; gap: 40rpx; font-size: 24rpx; opacity: .9; }
.wd-btn { margin-top: 26rpx; background: #fff; color: #c98f1f; border-radius: 40rpx; font-size: 28rpx; }
.type-row { display: flex; gap: 16rpx; margin: 20rpx 0; }
.type-item { flex: 1; background: #fff; border-radius: 14rpx; padding: 24rpx 0; text-align: center; }
.ti-num { display: block; font-size: 30rpx; font-weight: 600; color: #333; }
.ti-label { font-size: 22rpx; color: #999; margin-top: 6rpx; }
.list-card { background: #fff; border-radius: 14rpx; overflow: hidden; }
.tabs { display: flex; border-bottom: 1rpx solid #f2f3f5; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 27rpx; color: #666; }
.tab-item.active { color: #e6b33c; font-weight: 600; border-bottom: 4rpx solid #e6b33c; }
.item-card { padding: 24rpx 30rpx; border-bottom: 1rpx solid #f2f3f5; }
.item-line { display: flex; justify-content: space-between; }
.item-title { font-size: 27rpx; color: #333; }
.item-money { font-size: 28rpx; color: #e93323; font-weight: 600; }
.item-sub { font-size: 23rpx; color: #666; margin-top: 8rpx; }
.grey { color: #b0b6bf; }
.empty { text-align: center; color: #999; padding: 80rpx 0; font-size: 26rpx; background: #fff; }
</style>
