<template>
	<view class="pg-detail" v-if="info">
		<!-- 活动头图 -->
		<image class="pg-banner" :src="info.image" mode="aspectFill" />
		<view class="pg-main">
			<view class="pg-name">{{ info.name }}</view>
			<view class="pg-goods" @click="goGoods">
				<text class="pg-goods-label">拼购商品</text>
				<text class="pg-goods-name">{{ info.productName }}</text>
				<text class="el-icon-arrow pg-goods-arrow">›</text>
			</view>

			<!-- 当前团进度 -->
			<view class="pg-progress-card">
				<view class="pg-progress-head">
					<text class="pg-progress-title">当前团（第 {{ info.currentTeamNo || info.teamCount + 1 }} 期）</text>
					<text class="pg-progress-num">{{ info.currentJoinNum || 0 }}/{{ info.totalNum }} 人</text>
				</view>
				<view class="pg-bar">
					<view class="pg-bar-inner" :style="{ width: progressPercent + '%' }"></view>
				</view>
				<view class="pg-progress-tip">
					还差 <text class="pg-strong">{{ remainNum }}</text> 人成团，满{{ info.totalNum }}人立即开奖
				</view>
				<view class="pg-avatars" v-if="recentTeam && recentTeam.joins && recentTeam.joins.length">
					<view class="pg-avatar" v-for="j in recentTeam.joins.slice(0, 10)" :key="j.id" :class="{ win: j.isWin === 1 }">
						<image v-if="j.avatar" :src="j.avatar" mode="aspectFill" />
						<text v-else class="pg-avatar-txt">{{ (j.nickname || '?').slice(0, 1) }}</text>
					</view>
				</view>
			</view>

			<!-- 玩法规则 -->
			<view class="pg-rule-card">
				<view class="pg-rule-title">玩法规则</view>
				<view class="pg-rule-item">
					<view class="pg-rule-badge">1</view>
					<view class="pg-rule-txt">购买拼购商品即可参与本期活动，支持余额、微信、支付宝支付</view>
				</view>
				<view class="pg-rule-item">
					<view class="pg-rule-badge">2</view>
					<view class="pg-rule-txt">
						满 {{ info.totalNum }} 人成团，系统随机抽取 <text class="pg-strong">{{ info.winNum }}</text> 人
						<text class="pg-red">中奖</text>，中奖者获得商品，订单正常发货
					</view>
				</view>
				<view class="pg-rule-item">
					<view class="pg-rule-badge">3</view>
					<view class="pg-rule-txt">
						未中奖的 {{ info.totalNum - info.winNum }} 人：购买本金<text class="pg-red">全额退回余额账户</text>
						<template v-if="info.refundSubsidy > 0">，另获得 <text class="pg-red">{{ info.refundSubsidy }} 元</text> 佣金补贴</template>
					</view>
				</view>
				<view class="pg-rule-item">
					<view class="pg-rule-badge">4</view>
					<view class="pg-rule-txt">
						分享奖励：被推荐人参与拼购，推荐人按直推人数获得佣金奖励
						<text v-for="(t, i) in shareTiers" :key="i" class="pg-red">
							{{ i > 0 ? '；' : '' }}直推 {{ t.minDirectNum }} 人及以上享 {{ t.rewardType === 2 ? t.rewardValue + ' 元' : t.rewardValue + '%' }}
						</text>
					</view>
				</view>
				<view class="pg-rule-item" v-if="info.limitNum > 0">
					<view class="pg-rule-badge">5</view>
					<view class="pg-rule-txt">每人限参与 {{ info.limitNum }} 次</view>
				</view>
			</view>

			<!-- 累计开团 -->
			<view class="pg-stat">该活动已开团 <text class="pg-strong">{{ info.teamCount }}</text> 期</view>

			<!-- 历史期数 -->
			<view class="pg-rule-card" v-if="history.length">
				<view class="pg-rule-title">往期开奖</view>
				<view class="pg-history" v-for="t in history" :key="t.id" @click="toggleTeam(t)">
					<view class="pg-history-head">
						<text>第 {{ t.teamNo }} 期</text>
						<text class="pg-history-win">中{{ t.winNum }}人</text>
						<text class="pg-history-time">{{ t.winTime }}</text>
					</view>
					<view class="pg-history-members" v-if="t.joins && t.joins.length">
						<view class="pg-avatar" v-for="j in t.joins" :key="j.id" :class="{ win: j.isWin === 1 }">
							<image v-if="j.avatar" :src="j.avatar" mode="aspectFill" />
							<text v-else class="pg-avatar-txt">{{ (j.nickname || '?').slice(0, 1) }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部按钮 -->
		<view class="pg-footer">
			<view class="pg-footer-price">￥{{ info.productPrice }}</view>
			<view class="pg-footer-btn" @click="goGoods">立即参团</view>
		</view>
	</view>
</template>

<script>
import { getPinGouDetail, getPinGouTeams } from '@/api/pingou.js';
import { mapGetters } from 'vuex';

export default {
	data() {
		return {
			id: 0,
			info: null,
			history: []
		};
	},
	computed: {
		...mapGetters(['uid']),
		progressPercent() {
			if (!this.info || !this.info.totalNum) return 0;
			return Math.min(100, Math.round(((this.info.currentJoinNum || 0) / this.info.totalNum) * 100));
		},
		remainNum() {
			if (!this.info) return 0;
			return Math.max(0, this.info.totalNum - (this.info.currentJoinNum || 0));
		},
		recentTeam() {
			if (this.info && this.info.recentTeams && this.info.recentTeams.length) {
				return this.info.recentTeams[0];
			}
			return null;
		},
		shareTiers() {
			if (!this.info) return [];
			let tiers = [];
			if (this.info.shareTiers) {
				try { tiers = JSON.parse(this.info.shareTiers) || []; } catch (e) { tiers = []; }
			}
			tiers.sort((a, b) => (a.minDirectNum || 0) - (b.minDirectNum || 0));
			return tiers;
		}
	},
	onLoad(options) {
		this.id = options.id || 0;
		if (options.spread) this.$Cache.set('spread', options.spread);
		this.getDetail();
	},
	onShareAppMessage() {
		return {
			title: this.info ? this.info.name : '拼购活动',
			path: '/pages/pingou/detail/index?id=' + this.id + '&spread=' + this.uid,
			imageUrl: this.info ? this.info.image : ''
		};
	},
	methods: {
		getDetail() {
			getPinGouDetail(this.id).then((res) => {
				this.info = res.data;
				this.getHistory();
			});
		},
		getHistory() {
			getPinGouTeams(this.id, { page: 1, limit: 5 }).then((res) => {
				this.history = (res.data.list || []).filter((t) => t.status === 1);
			});
		},
		toggleTeam(t) {
			t._open = !t._open;
		},
		goGoods() {
			// 走正常商品购买链路（余额/微信/支付宝），支付成功自动参团开奖
			uni.navigateTo({
				url: '/pages/goods/goods_details/index?id=' + this.info.productId + '&spread=' + this.uid
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.pg-detail {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: 140rpx;
}
.pg-banner {
	width: 100%;
	height: 380rpx;
	display: block;
}
.pg-main {
	padding: 24rpx;
}
.pg-name {
	font-size: 36rpx;
	font-weight: bold;
	color: #282828;
}
.pg-goods {
	margin-top: 16rpx;
	background: #fff;
	border-radius: 12rpx;
	padding: 20rpx 24rpx;
	display: flex;
	align-items: center;
	.pg-goods-label {
		font-size: 24rpx;
		color: #fff;
		background: #1f5fd6;
		border-radius: 6rpx;
		padding: 4rpx 12rpx;
		margin-right: 16rpx;
		flex-shrink: 0;
	}
	.pg-goods-name {
		flex: 1;
		font-size: 28rpx;
		color: #282828;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.pg-goods-arrow {
		color: #c0c4cc;
		font-size: 32rpx;
	}
}
.pg-progress-card {
	margin-top: 24rpx;
	background: #fff;
	border-radius: 12rpx;
	padding: 24rpx;
}
.pg-progress-head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	.pg-progress-title {
		font-size: 28rpx;
		font-weight: bold;
		color: #282828;
	}
	.pg-progress-num {
		font-size: 26rpx;
		color: #1f5fd6;
		font-weight: bold;
	}
}
.pg-bar {
	margin-top: 20rpx;
	height: 16rpx;
	background: #eef1f7;
	border-radius: 999rpx;
	overflow: hidden;
	.pg-bar-inner {
		height: 100%;
		background: linear-gradient(155deg, #16337c, #1f5fd6 48%, #3a8df2);
		border-radius: 999rpx;
		transition: width 0.4s;
	}
}
.pg-progress-tip {
	margin-top: 16rpx;
	font-size: 24rpx;
	color: #666;
}
.pg-strong {
	color: #e93323;
	font-weight: bold;
}
.pg-red {
	color: #e93323;
}
.pg-avatars {
	margin-top: 20rpx;
	display: flex;
	flex-wrap: wrap;
}
.pg-avatar {
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	overflow: hidden;
	margin-right: 10rpx;
	background: #eef1f7;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid transparent;
	image {
		width: 100%;
		height: 100%;
	}
	.pg-avatar-txt {
		font-size: 20rpx;
		color: #999;
	}
	&.win {
		border-color: #e93323;
	}
}
.pg-rule-card {
	margin-top: 24rpx;
	background: #fff;
	border-radius: 12rpx;
	padding: 24rpx;
}
.pg-rule-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #282828;
	margin-bottom: 20rpx;
}
.pg-rule-item {
	display: flex;
	margin-bottom: 18rpx;
	.pg-rule-badge {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: linear-gradient(155deg, #16337c, #3a8df2);
		color: #fff;
		font-size: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 16rpx;
		flex-shrink: 0;
		margin-top: 2rpx;
	}
	.pg-rule-txt {
		flex: 1;
		font-size: 26rpx;
		color: #555;
		line-height: 40rpx;
	}
}
.pg-stat {
	margin-top: 24rpx;
	text-align: center;
	font-size: 24rpx;
	color: #999;
}
.pg-history {
	border-top: 1rpx solid #f0f2f5;
	padding: 20rpx 0;
	.pg-history-head {
		display: flex;
		justify-content: space-between;
		font-size: 24rpx;
		color: #555;
		.pg-history-win {
			color: #e93323;
		}
		.pg-history-time {
			color: #999;
		}
	}
	.pg-history-members {
		margin-top: 14rpx;
		display: flex;
		flex-wrap: wrap;
	}
}
.pg-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
	.pg-footer-price {
		font-size: 40rpx;
		font-weight: bold;
		color: #e93323;
	}
	.pg-footer-btn {
		background: linear-gradient(155deg, #16337c, #1f5fd6 48%, #3a8df2);
		color: #fff;
		font-size: 30rpx;
		font-weight: bold;
		padding: 20rpx 90rpx;
		border-radius: 999rpx;
	}
}
</style>
