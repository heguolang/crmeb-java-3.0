<template>
	<view class="pingou-page">
		<!-- 顶部标题 -->
		<view class="pg-header">
			<view class="pg-header-title">拼购专区</view>
			<view class="pg-header-sub">满团开奖 · 未中奖退本金还送佣金</view>
		</view>

		<!-- 活动列表 -->
		<view class="pg-list">
			<view class="pg-card" v-for="item in list" :key="item.id" @click="goDetail(item)">
				<image class="pg-card-img" :src="item.image" mode="aspectFill" />
				<view class="pg-card-body">
					<view class="pg-card-name">{{ item.name }}</view>
					<view class="pg-card-goods">{{ item.productName }}</view>
					<view class="pg-card-rule">
						<text>{{ item.totalNum }}人成团</text>
						<text class="pg-rule-gap">·</text>
						<text>随机抽{{ item.winNum }}人中奖</text>
					</view>
					<view class="pg-card-subsidy" v-if="item.refundSubsidy > 0">未中奖退本金 + 补贴{{ item.refundSubsidy }}元佣金</view>
					<view class="pg-card-bottom">
						<view class="pg-card-price">￥{{ item.productPrice }}</view>
						<view class="pg-btn">立即参团</view>
					</view>
				</view>
			</view>
			<view class="pg-empty" v-if="!loading && !list.length">暂无拼购活动，敬请期待</view>
		</view>
	</view>
</template>

<script>
import { getPinGouList } from '@/api/pingou.js';

export default {
	data() {
		return {
			loading: false,
			list: [],
			page: 1,
			limit: 20,
			finished: false
		};
	},
	onLoad() {
		this.getList();
	},
	onReachBottom() {
		if (!this.finished) this.getList();
	},
	methods: {
		getList() {
			if (this.loading) return;
			this.loading = true;
			getPinGouList({ page: this.page, limit: this.limit })
				.then((res) => {
					const records = res.data.list || [];
					this.list = this.list.concat(records);
					this.finished = this.list.length >= (res.data.total || 0);
					this.page++;
				})
				.finally(() => {
					this.loading = false;
				});
		},
		goDetail(item) {
			uni.navigateTo({
				url: '/pages/pingou/detail/index?id=' + item.id
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.pingou-page {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: 30rpx;
}
.pg-header {
	background: linear-gradient(155deg, #16337c, #1f5fd6 48%, #3a8df2);
	padding: 40rpx 30rpx 60rpx;
	color: #fff;
	.pg-header-title {
		font-size: 40rpx;
		font-weight: bold;
	}
	.pg-header-sub {
		margin-top: 8rpx;
		font-size: 24rpx;
		opacity: 0.85;
	}
}
.pg-list {
	padding: 0 24rpx;
	margin-top: -34rpx;
}
.pg-card {
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 24rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(22, 51, 124, 0.06);
	.pg-card-img {
		width: 100%;
		height: 280rpx;
		display: block;
	}
	.pg-card-body {
		padding: 24rpx;
	}
	.pg-card-name {
		font-size: 32rpx;
		font-weight: bold;
		color: #282828;
	}
	.pg-card-goods {
		margin-top: 8rpx;
		font-size: 26rpx;
		color: #666;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.pg-card-rule {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #1f5fd6;
		background: rgba(31, 95, 214, 0.08);
		display: inline-block;
		padding: 6rpx 16rpx;
		border-radius: 8rpx;
		.pg-rule-gap {
			margin: 0 10rpx;
		}
	}
	.pg-card-subsidy {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #b8860b;
	}
	.pg-card-bottom {
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.pg-card-price {
		font-size: 36rpx;
		font-weight: bold;
		color: #e93323;
	}
	.pg-btn {
		background: linear-gradient(155deg, #16337c, #1f5fd6 48%, #3a8df2);
		color: #fff;
		font-size: 26rpx;
		padding: 14rpx 40rpx;
		border-radius: 999rpx;
	}
}
.pg-empty {
	text-align: center;
	color: #999;
	font-size: 26rpx;
	padding: 120rpx 0;
}
</style>
