<template>
  <view class="stock-home">
    <template v-if="isAgent">
      <!-- 代理信息头 -->
      <view class="head-card">
        <view class="badge">{{ agent.levelName || '订货代理' }}</view>
        <view class="nickname">{{ agent.nickname }}</view>
        <view class="head-divider"></view>
        <view class="meta-row">
          <text class="meta-label">上级</text>
          <text class="meta-value">{{ agent.parentId > 0 ? agent.parentName : '总部' }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">权益</text>
          <text class="meta-value">订货拿货专价已生效，团队订货奖励自动结算</text>
        </view>
      </view>

      <!-- 交易管理 -->
      <view class="section-title">交易管理</view>
      <view class="group-card">
        <view class="g-item" @click="nav('/pages/users/stock/goods')">
          <view class="g-icon ic-blue">商城</view>
          <view class="g-label">商品中心</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/order-list')">
          <view class="g-icon ic-orange">订单</view>
          <view class="g-label">订货订单</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/order-list?tab=audit')">
          <view class="g-icon ic-red">审核</view>
          <view class="g-label">订单审核</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/exchange')">
          <view class="g-icon ic-purple">换货</view>
          <view class="g-label">换货管理</view>
        </view>
      </view>

      <!-- 团队与收益 -->
      <view class="section-title">团队与收益</view>
      <view class="group-card">
        <view class="g-item" @click="nav('/pages/users/stock/team')">
          <view class="g-icon ic-cyan">团队</view>
          <view class="g-label">我的团队</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/performance')">
          <view class="g-icon ic-green">业绩</view>
          <view class="g-label">业绩中心</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/bonus')">
          <view class="g-icon ic-gold">奖金</view>
          <view class="g-label">奖金中心</view>
        </view>
        <view class="g-item" @click="nav('/pages/users/stock/notice')">
          <view class="g-icon ic-grey">消息</view>
          <view class="g-label">消息通知</view>
        </view>
      </view>

      <view class="bottom-tip">— 订货中心 —</view>
    </template>

    <emptyPage v-if="loaded && !isAgent" :title="'您还不是订货代理，请联系上级代理或总部开通订货权限'"></emptyPage>
  </view>
</template>

<script>
	import { getStockAgentInfo } from '@/api/stock.js';
	import emptyPage from '@/components/emptyPage.vue';
	export default {
		components: { emptyPage },
		data() {
			return {
				loaded: false,
				isAgent: false,
				agent: {}
			};
		},
		onShow() {
			this.loadInfo();
		},
		methods: {
			loadInfo() {
				getStockAgentInfo().then(res => {
					this.isAgent = res.data.isAgent;
					this.agent = res.data.agent || {};
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			nav(url) {
				uni.navigateTo({ url });
			}
		}
	};
</script>

<style lang="scss" scoped>
.stock-home {
  min-height: 100vh;
  padding: 24rpx 24rpx 60rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}

/* ---------- 代理信息头 ---------- */
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #2b6fe3 0%, #4a9df8 60%, #6bb2ff 100%);
  border-radius: 24rpx;
  padding: 36rpx 32rpx 32rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(43, 111, 227, 0.28);

  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
  }
  &::before {
    width: 220rpx;
    height: 220rpx;
    right: -70rpx;
    top: -90rpx;
  }
  &::after {
    width: 140rpx;
    height: 140rpx;
    right: 90rpx;
    bottom: -70rpx;
  }
}
.badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.nickname {
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}
.head-divider {
  margin: 26rpx 0 20rpx;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.28);
}
.meta-row {
  display: flex;
  align-items: flex-start;
  margin-top: 12rpx;
  position: relative;
  z-index: 1;
}
.meta-label {
  flex-shrink: 0;
  font-size: 24rpx;
  opacity: 0.75;
  width: 76rpx;
}
.meta-value {
  font-size: 24rpx;
  line-height: 34rpx;
  opacity: 0.98;
}

/* ---------- 分组 ---------- */
.section-title {
  margin: 36rpx 8rpx 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}
.group-card {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  border-radius: 24rpx;
  padding: 16rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.g-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 0 26rpx;
}
.g-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 600;
}
.ic-blue   { background: #ecf3ff; color: #2b6fe3; }
.ic-orange { background: #fff1e8; color: #ff7a45; }
.ic-red    { background: #ffecec; color: #f56c6c; }
.ic-purple { background: #f3edff; color: #9b6ff5; }
.ic-cyan   { background: #e6f8f8; color: #23a8a8; }
.ic-green  { background: #e9f9ec; color: #21a84f; }
.ic-gold   { background: #fdf5e2; color: #c99a25; }
.ic-grey   { background: #f0f2f6; color: #7b8698; }
.g-label {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #303133;
}

.bottom-tip {
  margin-top: 60rpx;
  text-align: center;
  font-size: 22rpx;
  color: #c3cad6;
  letter-spacing: 4rpx;
}
</style>
