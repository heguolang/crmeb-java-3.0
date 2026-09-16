<template>
  <view class="stock-home">
    <view class="head-card">
      <view v-if="isAgent" class="agent-info">
        <view class="info-line">
          <text class="level-tag">{{ agent.levelName }}</text>
          <text class="nickname">{{ agent.nickname }}</text>
        </view>
        <view class="info-sub">上级：{{ agent.parentId > 0 ? agent.parentName : '总部' }}</view>
        <view class="info-sub">订货拿货专价已生效，团队订货奖励自动结算</view>
      </view>
      <view v-else class="agent-none">
        <text>您还不是订货代理</text>
        <text class="tip">请联系上级代理或总部开通订货权限</text>
      </view>
    </view>

    <view v-if="isAgent" class="grid">
      <view class="grid-item" @click="nav('/pages/users/stock/goods')">
        <view class="gi-icon gi-blue">商城</view>
        <view class="gi-label">商品中心</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/order-list')">
        <view class="gi-icon gi-orange">订单</view>
        <view class="gi-label">订货订单</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/order-list?tab=audit')">
        <view class="gi-icon gi-red">审核</view>
        <view class="gi-label">订单审核</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/exchange')">
        <view class="gi-icon gi-purple">换货</view>
        <view class="gi-label">换货管理</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/team')">
        <view class="gi-icon gi-cyan">团队</view>
        <view class="gi-label">我的团队</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/performance')">
        <view class="gi-icon gi-green">业绩</view>
        <view class="gi-label">业绩中心</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/bonus')">
        <view class="gi-icon gi-gold">奖金</view>
        <view class="gi-label">奖金中心</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/stock/notice')">
        <view class="gi-icon gi-grey">消息</view>
        <view class="gi-label">消息通知</view>
      </view>
    </view>

    <emptyPage v-if="loaded && !isAgent" :title="'暂无订货权限'"></emptyPage>
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
.stock-home { min-height: 100vh; background: #f5f6f8; padding: 24rpx; }
.head-card {
	background: linear-gradient(135deg, #2b6fe3, #4a9df8);
	border-radius: 20rpx; color: #fff; padding: 40rpx 36rpx;
	.agent-none { display: flex; flex-direction: column; .tip { font-size: 24rpx; opacity: .8; margin-top: 10rpx; } }
	.info-line { display: flex; align-items: center; }
	.level-tag { background: rgba(255,255,255,.25); border-radius: 8rpx; padding: 4rpx 16rpx; font-size: 24rpx; margin-right: 16rpx; }
	.nickname { font-size: 34rpx; font-weight: 600; }
	.info-sub { font-size: 24rpx; opacity: .85; margin-top: 12rpx; }
}
.grid { display: flex; flex-wrap: wrap; background: #fff; border-radius: 20rpx; margin-top: 24rpx; padding: 20rpx 0; }
.grid-item { width: 25%; display: flex; flex-direction: column; align-items: center; padding: 26rpx 0; }
.gi-icon {
	width: 88rpx; height: 88rpx; border-radius: 24rpx; display: flex; align-items: center;
	justify-content: center; color: #fff; font-size: 28rpx; font-weight: 600;
}
.gi-blue { background: #4a9df8; } .gi-orange { background: #ff9a3c; } .gi-red { background: #f56c6c; }
.gi-purple { background: #9b6ff5; } .gi-cyan { background: #38c2c2; } .gi-green { background: #5cc45c; }
.gi-gold { background: #e6b33c; } .gi-grey { background: #98a2b0; }
.gi-label { margin-top: 14rpx; font-size: 24rpx; color: #333; }
</style>
