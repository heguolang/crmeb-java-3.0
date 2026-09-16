<template>
  <view class="bonus-page">
    <view class="balance-card">
      <view class="bc-label">可提现奖金</view>
      <view class="bc-num">¥{{ bonus.balance || 0 }}</view>
      <view class="bc-row">
        <text>累计奖励 ¥{{ bonus.totalReward || 0 }}</text>
        <text>提现中/已提 ¥{{ bonus.lockedWithdraw || 0 }}</text>
      </view>
      <button class="wd-btn" @click="showWd = true">申请提现</button>
    </view>
    <view class="type-row">
      <view class="type-item"><text class="ti-num">¥{{ bonus.diffReward || 0 }}</text><text class="ti-label">差价奖励</text></view>
      <view class="type-item"><text class="ti-num">¥{{ bonus.ladderReward || 0 }}</text><text class="ti-label">级差奖励</text></view>
      <view class="type-item"><text class="ti-num">¥{{ bonus.peerReward || 0 }}</text><text class="ti-label">平级奖励</text></view>
    </view>

    <view class="tabs">
      <view class="tab-item" :class="{ active: tab === 'reward' }" @click="tab = 'reward'; loadList()">奖励明细</view>
      <view class="tab-item" :class="{ active: tab === 'withdraw' }" @click="tab = 'withdraw'; loadList()">提现记录</view>
    </view>

    <view v-if="tab === 'reward'">
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

    <view v-if="tab === 'withdraw'">
      <view v-for="w in withdraws" :key="w.id" class="item-card">
        <view class="item-line">
          <text class="item-title">{{ w.withdrawNo }}</text>
          <text class="item-money">-¥{{ w.price }}</text>
        </view>
        <view class="item-sub">{{ wdStatus(w.status) }}<template v-if="w.auditMark"> · {{ w.auditMark }}</template></view>
        <view class="item-sub grey">{{ w.createTime }}</view>
      </view>
      <view v-if="!withdraws.length" class="empty">暂无提现记录</view>
    </view>

    <view v-if="showWd" class="mask" @click="showWd = false">
      <view class="modal" @click.stop>
        <view class="modal-title">申请提现</view>
        <view class="modal-sub">可提现 ¥{{ bonus.balance || 0 }}，总部审核通过后打款</view>
        <input v-model="wdPrice" type="digit" class="wd-input" placeholder="提现金额" />
        <input v-model="wdMark" class="wd-input" placeholder="收款方式（如：微信/银行卡号）" />
        <button class="submit-btn" @click="submitWd">提交申请</button>
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyStockBonus, getMyStockRewards, getMyStockWithdraws, applyStockWithdraw } from '@/api/stock.js';
	export default {
		data() {
			return {
				bonus: {},
				tab: 'reward',
				rewards: [],
				withdraws: [],
				showWd: false,
				wdPrice: '',
				wdMark: ''
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			typeName(t) {
				return { 1: '差价奖励', 2: '级差奖励', 3: '平级奖励' }[t] || '奖励';
			},
			wdStatus(s) {
				return { 0: '待总部审核', 1: '已打款', '-1': '已驳回' }[s] || s;
			},
			load() {
				getMyStockBonus().then(res => { this.bonus = res.data || {}; });
				this.loadList();
			},
			loadList() {
				if (this.tab === 'reward') {
					getMyStockRewards({ page: 1, limit: 30 }).then(res => { this.rewards = res.data.list || []; });
				} else {
					getMyStockWithdraws({ page: 1, limit: 30 }).then(res => { this.withdraws = res.data.list || []; });
				}
			},
			submitWd() {
				if (!this.wdPrice || Number(this.wdPrice) <= 0) return this.$util.Tips({ title: '请填写提现金额' });
				applyStockWithdraw({ price: Number(this.wdPrice), mark: this.wdMark }).then(() => {
					this.$util.Tips({ title: '申请已提交' });
					this.showWd = false;
					this.wdPrice = '';
					this.load();
				});
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
.tabs { display: flex; background: #fff; border-radius: 14rpx 14rpx 0 0; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 27rpx; color: #666; }
.tab-item.active { color: #e6b33c; font-weight: 600; border-bottom: 4rpx solid #e6b33c; }
.item-card { background: #fff; padding: 24rpx 30rpx; border-bottom: 1rpx solid #f2f3f5; }
.item-line { display: flex; justify-content: space-between; }
.item-title { font-size: 27rpx; color: #333; }
.item-money { font-size: 28rpx; color: #e93323; font-weight: 600; }
.item-sub { font-size: 23rpx; color: #666; margin-top: 8rpx; }
.grey { color: #b0b6bf; }
.empty { text-align: center; color: #999; padding: 80rpx 0; font-size: 26rpx; background: #fff; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 99; }
.modal { width: 600rpx; background: #fff; border-radius: 20rpx; padding: 40rpx 34rpx; }
.modal-title { font-size: 32rpx; font-weight: 600; color: #333; text-align: center; }
.modal-sub { font-size: 24rpx; color: #999; text-align: center; margin: 12rpx 0 24rpx; }
.wd-input { background: #f5f6f8; border-radius: 10rpx; height: 76rpx; padding: 0 20rpx; font-size: 27rpx; margin-bottom: 18rpx; }
.submit-btn { background: #e6b33c; color: #fff; border-radius: 40rpx; font-size: 28rpx; margin-top: 8rpx; }
</style>
