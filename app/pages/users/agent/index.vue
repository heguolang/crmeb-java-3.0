<template>
	<view :data-theme="theme">
		<view class="agent-page">
			<!-- 功能关闭 -->
			<view class="closed-box borRadius14" v-if="info.funcStatus === '0'">
				<text class="iconfont icon-huangguan4 closed-icon"></text>
				<view class="closed-text">代理功能暂未开启</view>
			</view>

			<template v-else>
				<!-- 统计卡片（已是代理才显示） -->
				<view class="stat-card borRadius14" v-if="info.isAgent">
					<view class="stat-title">代理收益（元）</view>
					<view class="stat-row acea-row">
						<view class="stat-item">
							<view class="num">{{ info.totalReward || 0 }}</view>
							<view class="label">累计已入账</view>
						</view>
						<view class="stat-item">
							<view class="num">{{ info.waitReward || 0 }}</view>
							<view class="label">待入账</view>
						</view>
						<view class="stat-item">
							<view class="num">{{ info.rewardCount || 0 }}</view>
							<view class="label">奖励笔数</view>
						</view>
					</view>
					<view class="tips">奖励自动进入佣金，可按佣金提现规则提现</view>
				</view>

				<!-- 我的代理区域 -->
				<view class="region-card borRadius14" v-if="info.agentList && info.agentList.length">
					<view class="card-title">我的代理区域</view>
					<view class="region-item acea-row row-between-wrapper" v-for="(item, index) in info.agentList" :key="index">
						<view>
							<view class="region-name line1">{{ item.regionName }}</view>
							<view class="region-sub">{{ levelLabel(item.level) }}<text v-if="item.ratio > 0"> · 奖励比例 {{ item.ratio }}%</text></view>
						</view>
						<el-tag :type="statusType(item.status)"><text class="tag-text">{{ statusLabel(item.status) }}</text></el-tag>
					</view>
				</view>

				<!-- 审核中提示 -->
				<view class="pending-box borRadius14" v-if="hasPending">
					<text class="iconfont icon-jiazai pending-icon"></text>
					<view class="pending-text">您的代理申请正在审核中，请耐心等待</view>
				</view>

				<!-- 申请表单（无有效代理时显示） -->
				<view class="apply-card borRadius14" v-if="canApply">
					<view class="card-title">申请成为区域代理</view>
					<view class="apply-desc">锁定对应区域所有自然订单收益，订单收货地址归属您的代理区域时，自动按比例获得奖励</view>
					<view class="apply-form">
						<view class="form-item acea-row row-between-wrapper">
							<text class="form-label">申请级别</text>
							<picker :range="levelNames" :value="levelIndex" @change="onLevelChange">
								<view class="picker-value">{{ levelNames[levelIndex] }}<text class="iconfont icon-xiangyou"></text></view>
							</picker>
						</view>
						<view class="form-item acea-row row-between-wrapper">
							<text class="form-label">选择区域</text>
							<picker mode="region" @change="onRegionChange">
								<view class="picker-value" :class="{ placeholder: !regionText }">{{ regionText || '请选择省市区' }}<text class="iconfont icon-xiangyou"></text></view>
							</picker>
						</view>
						<view class="form-item acea-row">
							<text class="form-label">申请说明</text>
							<input class="form-input" v-model="applyMark" placeholder="选填" placeholder-class="placeholder-input" />
						</view>
					</view>
					<button class="apply-btn" @click="onApply">提交申请</button>
					<view class="apply-note">提交后由平台审核，审核通过并由平台设置奖励比例后生效</view>
				</view>

				<!-- 奖励明细（已是代理或有奖励记录时显示） -->
				<view class="reward-card borRadius14" v-if="info.isAgent || rewardList.length">
					<view class="card-title">区域奖励明细</view>
					<view class="reward-item acea-row row-between-wrapper" v-for="(item, index) in rewardList" :key="index">
						<view class="reward-left">
							<view class="reward-order line1">订单 {{ item.orderId }}</view>
							<view class="reward-sub">{{ item.createTime }} · {{ item.regionName }} · {{ item.ratio }}%</view>
						</view>
						<view class="reward-right">
							<view class="reward-num">+{{ item.rewardPrice }}</view>
							<view class="reward-status" :class="'st' + item.status">{{ statusTextLabel(item.status) }}</view>
						</view>
					</view>
					<view class="loadingicon acea-row row-center-wrapper" v-if="rewardList.length">
						<text class='loading iconfont icon-jiazai' :hidden='loading == false'></text>{{ loadTitle }}
					</view>
					<view v-if="!rewardList.length && !loading">
						<emptyPage title="暂无奖励记录"></emptyPage>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script>
	import { getAgentInfo, agentApply, getAgentRewardList } from '@/api/user.js';
	import { toLogin } from '@/libs/login.js';
	import { mapGetters } from 'vuex';
	import emptyPage from '@/components/emptyPage.vue';
	let app = getApp();
	export default {
		components: { emptyPage },
		data() {
			return {
				theme: app.globalData.theme,
				info: {
					funcStatus: '1',
					applyStatus: '1',
					isAgent: false,
					agentList: [],
					totalReward: 0,
					waitReward: 0,
					rewardCount: 0,
				},
				levelNames: ['省级代理', '市级代理', '区级代理'],
				levelIndex: 0,
				regionText: '',
				regionArr: [],
				applyMark: '',
				rewardList: [],
				loadTitle: '加载更多',
				loading: false,
				loadend: false,
				page: 1,
				limit: 10,
			};
		},
		computed: {
			...mapGetters(['isLogin']),
			hasPending() {
				return (this.info.agentList || []).some((a) => a.status === 0);
			},
			canApply() {
				if (this.info.funcStatus !== '1') return false;
				if (this.info.isAgent || this.hasPending) return false;
				return this.info.applyStatus === '1';
			},
		},
		onShow() {
			if (this.isLogin) {
				this.loadInfo();
				this.loadReward(true);
			} else {
				toLogin();
			}
		},
		onReachBottom() {
			this.loadReward(false);
		},
		methods: {
			levelLabel(level) {
				const map = { 1: '省级', 2: '市级', 3: '区级' };
				return map[level] || '-';
			},
			statusLabel(status) {
				const map = { 0: '审核中', 1: '生效中', 2: '已拒绝' };
				return map[status] || '-';
			},
			statusType(status) {
				const map = { 0: 'warning', 1: 'success', 2: 'danger' };
				return map[status] || 'info';
			},
			statusTextLabel(status) {
				const map = { 1: '待入账', 2: '已入账', 3: '已失效' };
				return map[status] || '-';
			},
			loadInfo() {
				getAgentInfo()
					.then((res) => {
						if (res.data) this.info = res.data;
					})
					.catch(() => {});
			},
			loadReward(reset) {
				if (reset) {
					this.page = 1;
					this.loadend = false;
					this.rewardList = [];
				}
				if (this.loadend || this.loading) return;
				this.loading = true;
				this.loadTitle = '';
				getAgentRewardList({ page: this.page, limit: this.limit })
					.then((res) => {
						const list = (res.data && res.data.list) || [];
						this.rewardList = this.rewardList.concat(list);
						this.loadend = (res.data && res.data.totalPage) ? res.data.totalPage <= this.page : true;
						this.page += 1;
						this.loading = false;
						this.loadTitle = '加载更多';
					})
					.catch(() => {
						this.loading = false;
						this.loadTitle = '加载更多';
					});
			},
			onLevelChange(e) {
				this.levelIndex = Number(e.detail.value) || 0;
			},
			onRegionChange(e) {
				this.regionArr = e.detail.value || [];
				this.regionText = this.regionArr.filter(Boolean).join(' / ');
			},
			onApply() {
				const level = this.levelIndex + 1;
				if (!this.regionArr.length) return this.$util.Tips({ title: '请选择区域' });
				if (level >= 2 && !this.regionArr[1]) return this.$util.Tips({ title: '请选择城市' });
				if (level >= 3 && !this.regionArr[2]) return this.$util.Tips({ title: '请选择区/县' });
				const data = {
					level,
					province: this.regionArr[0] || '',
					city: level >= 2 ? this.regionArr[1] || '' : '',
					district: level >= 3 ? this.regionArr[2] || '' : '',
					applyMark: this.applyMark,
				};
				agentApply(data)
					.then(() => {
						this.$util.Tips({ title: '申请已提交，等待审核', icon: 'success' });
						this.loadInfo();
					})
					.catch((err) => {
						this.$util.Tips({ title: (err && err.msg) || '提交失败' });
					});
			},
		},
	};
</script>

<style lang="scss" scoped>
	.agent-page {
		padding: 24rpx 24rpx 40rpx;
	}
	.closed-box,
	.pending-box {
		background: #ffffff;
		padding: 60rpx 30rpx;
		text-align: center;
		margin-bottom: 24rpx;
	}
	.closed-icon,
	.pending-icon {
		font-size: 60rpx;
		color: #e93323;
	}
	.closed-text,
	.pending-text {
		margin-top: 16rpx;
		color: #666;
		font-size: 28rpx;
	}
	.stat-card {
		background: linear-gradient(135deg, #2f2e38 0%, #454350 100%);
		color: #fff;
		padding: 34rpx 30rpx;
		margin-bottom: 24rpx;
		.stat-title {
			font-size: 26rpx;
			opacity: 0.8;
		}
		.stat-row {
			margin-top: 24rpx;
			.stat-item {
				flex: 1;
				text-align: center;
				.num {
					font-size: 40rpx;
					font-weight: 600;
				}
				.label {
					margin-top: 6rpx;
					font-size: 22rpx;
					opacity: 0.8;
				}
			}
		}
		.tips {
			margin-top: 24rpx;
			font-size: 20rpx;
			opacity: 0.6;
		}
	}
	.region-card,
	.reward-card,
	.apply-card {
		background: #ffffff;
		padding: 28rpx 30rpx;
		margin-bottom: 24rpx;
	}
	.card-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #282828;
		margin-bottom: 20rpx;
	}
	.region-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		&:last-child {
			border-bottom: none;
		}
		.region-name {
			font-size: 28rpx;
			color: #282828;
		}
		.region-sub {
			margin-top: 6rpx;
			font-size: 22rpx;
			color: #999;
		}
	}
	.tag-text {
		font-size: 22rpx;
	}
	.apply-desc {
		font-size: 24rpx;
		color: #999;
		line-height: 1.6;
		margin-bottom: 24rpx;
	}
	.form-item {
		padding: 22rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		.form-label {
			width: 150rpx;
			font-size: 28rpx;
			color: #282828;
		}
		.picker-value {
			flex: 1;
			text-align: right;
			font-size: 28rpx;
			color: #282828;
			.iconfont {
				margin-left: 8rpx;
				font-size: 22rpx;
				color: #bbb;
			}
		}
		.placeholder {
			color: #bbb;
		}
		.form-input {
			flex: 1;
			text-align: right;
			font-size: 28rpx;
		}
	}
	.placeholder-input {
		color: #bbb;
	}
	.apply-btn {
		margin-top: 34rpx;
		height: 84rpx;
		line-height: 84rpx;
		background: #e93323;
		color: #fff;
		font-size: 30rpx;
		border-radius: 42rpx;
	}
	.apply-note {
		margin-top: 18rpx;
		text-align: center;
		font-size: 20rpx;
		color: #bbb;
	}
	.reward-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		&:last-child {
			border-bottom: none;
		}
		.reward-left {
			flex: 1;
			overflow: hidden;
			margin-right: 20rpx;
			.reward-order {
				font-size: 26rpx;
				color: #282828;
			}
			.reward-sub {
				margin-top: 6rpx;
				font-size: 22rpx;
				color: #999;
			}
		}
		.reward-right {
			text-align: right;
			.reward-num {
				font-size: 30rpx;
				font-weight: 600;
				color: #e93323;
			}
			.reward-status {
				margin-top: 6rpx;
				font-size: 20rpx;
				color: #999;
				&.st2 {
					color: #2dbd6e;
				}
				&.st3 {
					color: #bbb;
				}
			}
		}
	}
	.loadingicon {
		font-size: 24rpx;
		color: #999;
		padding: 16rpx 0;
	}
</style>
