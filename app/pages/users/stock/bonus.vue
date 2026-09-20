<template>
  <view class="bonus-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">奖金中心</view>
      <view class="page-sub">奖励自动结算，可并入佣金申请提现</view>
    </view>

    <view class="page-body">
      <!-- 余额卡：与「订货中心」头部同族的深蓝渐变 -->
      <view class="balance-card">
        <view class="bc-deco d1"></view>
        <view class="bc-deco d2"></view>
        <view class="bc-beam"></view>
        <view class="bc-label"><text class="bc-dot"></text>累计奖励（已并入佣金余额）</view>
        <view class="bc-num">
          <text class="bc-cny">¥</text>{{ bonus.totalReward || 0 }}
        </view>
        <view class="bc-row">
          <text>佣金余额 ¥{{ bonus.commission || 0 }}</text>
        </view>
        <button class="wd-btn" @click="goWithdraw">申请提现</button>
      </view>

      <!-- 奖励构成 -->
      <view class="type-card">
        <view class="type-item">
          <text class="ti-num">¥{{ bonus.diffReward || 0 }}</text>
          <text class="ti-label">差价奖励</text>
        </view>
        <view class="ti-divider"></view>
        <view class="type-item">
          <text class="ti-num">¥{{ bonus.ladderReward || 0 }}</text>
          <text class="ti-label">阶梯奖励</text>
        </view>
        <view class="ti-divider"></view>
        <view class="type-item">
          <text class="ti-num">¥{{ bonus.peerReward || 0 }}</text>
          <text class="ti-label">平级奖励</text>
        </view>
      </view>

      <!-- 奖励明细 -->
      <view class="section-title">
        <view class="st-bar"></view>
        <text class="st-text">奖励明细</text>
        <view class="st-line"></view>
      </view>

      <view v-if="rewards.length" class="list-card">
        <view v-for="r in rewards" :key="r.id" class="item-card">
          <view class="ai-dot" :class="'t' + r.type"></view>
          <view class="item-main">
            <view class="item-line">
              <text class="item-title">{{ typeName(r.type) }}</text>
              <text class="item-money">+¥{{ r.rewardPrice }}</text>
            </view>
            <view class="item-sub">{{ r.mark }}</view>
            <view class="item-sub grey">{{ r.createTime }}</view>
          </view>
        </view>
      </view>

      <view v-if="!rewards.length" class="empty-card">
        <view class="empty-ico">奖</view>
        <view class="empty-txt">暂无奖励明细</view>
        <view class="empty-sub">团队订货产生差价 / 阶梯 / 平级奖励后自动入账</view>
      </view>
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
				return { 1: '差价奖励', 2: '阶梯奖励', 3: '平级奖励', 4: '货款成本' }[t] || '奖励';
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
.bonus-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

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

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -56rpx;
}

/* ---------- 余额卡：与「订货中心」头部同族的深蓝渐变 ---------- */
.balance-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(150deg, #16337c 0%, #1f5fd6 52%, #3a8df2 100%);
  border-radius: 24rpx;
  padding: 34rpx 34rpx 32rpx;
  color: #fff;
  box-shadow: 0 12rpx 32rpx rgba(22, 51, 124, 0.28);
  .bc-deco { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.08); }
  .d1 { width: 200rpx; height: 200rpx; right: -60rpx; top: -80rpx; background: rgba(255,255,255,0.12); }
  .d2 { width: 110rpx; height: 110rpx; right: 90rpx; bottom: -50rpx; }
}
/* 斜光带：与订货中心首页头部同一手法，保证两块蓝是一套 */
.bc-beam {
  position: absolute;
  top: -70rpx;
  right: -30rpx;
  width: 200rpx;
  height: 420rpx;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.10) 50%, rgba(255,255,255,0) 100%);
  transform: rotate(22deg);
}
.bc-label {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.88);
}
/* 金色小点：保留"奖金"识别色，只做点缀不再当主色 */
.bc-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #f6cd60, #e0a213);
  margin-right: 10rpx;
  flex-shrink: 0;
}
.bc-num {
  font-size: 68rpx;
  font-weight: 700;
  margin: 14rpx 0 10rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.10);
  position: relative;
  z-index: 1;
}
.bc-cny { font-size: 36rpx; font-weight: 600; margin-right: 6rpx; }
.bc-row { font-size: 24rpx; opacity: 0.92; position: relative; z-index: 1; }
.wd-btn {
  position: relative;
  z-index: 1;
  margin-top: 26rpx;
  background: #fff;
  color: #1f5fd6;
  border-radius: 999rpx;
  font-size: 27rpx;
  font-weight: 600;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 44rpx;
  display: inline-block;
  box-shadow: 0 8rpx 18rpx rgba(10, 31, 78, 0.20);
  &::after { border: none; }
}

/* ---------- 奖励构成 ---------- */
.type-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 30rpx 16rpx;
  margin-top: 20rpx;
  display: flex;
  align-items: stretch;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.type-item { flex: 1; text-align: center; }
.ti-num { display: block; font-size: 32rpx; font-weight: 700; color: #26324b; }
.ti-label { font-size: 22rpx; color: #909399; margin-top: 8rpx; }
.ti-divider { width: 1rpx; background: #eef1f6; }

/* ---------- 奖励明细 ---------- */
.section-title { display: flex; align-items: center; margin: 30rpx 6rpx 20rpx; }
.st-bar {
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4a9df8, #2b6fe3);
  margin-right: 14rpx;
}
.st-text { font-size: 30rpx; font-weight: 700; color: #26324b; }
.st-line { flex: 1; height: 1rpx; margin-left: 20rpx; background: linear-gradient(90deg, #e3e9f4, rgba(227, 233, 244, 0)); }

.list-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.item-card {
  display: flex;
  align-items: flex-start;
  padding: 26rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
  &:last-child { border-bottom: none; }
}
/* 类型色点 */
.ai-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-top: 12rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
  &.t1 { background: #f56c6c; box-shadow: 0 0 0 6rpx rgba(245, 108, 108, 0.14); }
  &.t2 { background: #e6b33c; box-shadow: 0 0 0 6rpx rgba(230, 179, 60, 0.16); }
  &.t3 { background: #2b6fe3; box-shadow: 0 0 0 6rpx rgba(43, 111, 227, 0.14); }
  &.t4 { background: #909399; box-shadow: 0 0 0 6rpx rgba(144, 147, 153, 0.14); }
}
.item-main { flex: 1; overflow: hidden; }
.item-line { display: flex; justify-content: space-between; align-items: center; }
.item-title { font-size: 27rpx; color: #26324b; font-weight: 600; }
.item-money { font-size: 30rpx; color: #e93323; font-weight: 700; }
.item-sub { font-size: 23rpx; color: #606266; margin-top: 8rpx; }
.grey { color: #a4adc0; }

/* ---------- 空态白卡 ---------- */
.empty-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 90rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.empty-ico {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: #eaf2ff;
  color: #4a9df8;
  border: 2rpx solid #dbe7fb;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-txt { margin-top: 26rpx; font-size: 28rpx; color: #3d4a5f; font-weight: 600; }
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #a4adc0; }
</style>
