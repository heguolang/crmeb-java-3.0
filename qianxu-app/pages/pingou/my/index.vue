<template>
	<view class="pg-my">
		<view class="pg-my-head">
			<view class="pg-my-title">我的拼购</view>
		</view>
		<view class="pg-my-list">
			<view class="pg-my-card" v-for="item in list" :key="item.id">
				<view class="pg-my-row">
					<view class="pg-my-name">{{ item.activityName || '拼购活动' }}</view>
					<view class="pg-my-status" :class="statusClass(item)">{{ statusText(item) }}</view>
				</view>
				<view class="pg-my-line">订单号：{{ item.orderNo }}</view>
				<view class="pg-my-line">参与金额：￥{{ item.payPrice }}　参与时间：{{ item.createTime }}</view>
				<view class="pg-my-line" v-if="item.isWin === 0 && item.rewardStatus === 1">
					<text class="pg-red">已退本金 ￥{{ item.refundPrice }} 至余额账户</text>
					<text class="pg-red" v-if="item.subsidyPrice > 0">，补贴佣金 ￥{{ item.subsidyPrice }} 已到账</text>
				</view>
			</view>
			<view class="pg-empty" v-if="!loading && !list.length">暂无参与记录</view>
		</view>
	</view>
</template>

<script>
import { getPinGouMy } from '@/api/pingou.js';

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
	onShow() {
		this.page = 1;
		this.list = [];
		this.finished = false;
		this.getList();
	},
	onReachBottom() {
		if (!this.finished) this.getList();
	},
	methods: {
		getList() {
			if (this.loading) return;
			this.loading = true;
			getPinGouMy({ page: this.page, limit: this.limit })
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
		statusText(item) {
			if (item.isWin === 1) return '已中奖';
			if (item.rewardStatus === 1) return '未中奖已退款';
			return '参与中';
		},
		statusClass(item) {
			if (item.isWin === 1) return 'win';
			if (item.rewardStatus === 1) return 'refund';
			return 'waiting';
		}
	}
};
</script>

<style lang="scss" scoped>
.pg-my {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: 30rpx;
}
.pg-my-head {
	background: linear-gradient(155deg, #16337c, #1f5fd6 48%, #3a8df2);
	padding: 40rpx 30rpx 50rpx;
	color: #fff;
	.pg-my-title {
		font-size: 40rpx;
		font-weight: bold;
	}
}
.pg-my-list {
	padding: 0 24rpx;
	margin-top: -24rpx;
}
.pg-my-card {
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	padding: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(22, 51, 124, 0.06);
}
.pg-my-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	.pg-my-name {
		font-size: 30rpx;
		font-weight: bold;
		color: #282828;
	}
	.pg-my-status {
		font-size: 24rpx;
		&.win {
			color: #e93323;
		}
		&.refund {
			color: #67c23a;
		}
		&.waiting {
			color: #e6a23c;
		}
	}
}
.pg-my-line {
	margin-top: 12rpx;
	font-size: 24rpx;
	color: #666;
}
.pg-red {
	color: #e93323;
}
.pg-empty {
	text-align: center;
	color: #999;
	font-size: 26rpx;
	padding: 120rpx 0;
}
</style>
