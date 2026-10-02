<template>
	<view :data-theme="theme">
		<view class="agent-page">
			<!-- 功能关闭 -->
			<view class="closed-box" v-if="info.funcStatus === '0'">
				<text class="iconfont icon-huangguan4 closed-icon"></text>
				<view class="closed-text">代理功能暂未开启</view>
			</view>

			<template v-else>
				<!-- 代理身份 + 收益头卡（已是代理才显示） -->
				<view class="head-card" v-if="info.isAgent">
					<view class="badge">区域代理</view>
					<view class="earn-label">代理收益（元）</view>
					<view class="earn-num">{{ info.totalReward || 0 }}</view>
					<view class="head-divider"></view>
					<view class="h-stat-row">
						<view class="h-stat">
							<view class="h-num">{{ info.waitReward || 0 }}</view>
							<view class="h-label">待入账</view>
						</view>
						<view class="h-stat-divider"></view>
						<view class="h-stat">
							<view class="h-num">{{ info.rewardCount || 0 }}</view>
							<view class="h-label">奖励笔数</view>
						</view>
					</view>
					<view class="head-tips">奖励自动进入佣金，可按佣金提现规则提现</view>
				</view>

				<!-- 我的代理区域 -->
				<view class="region-card" v-if="info.agentList && info.agentList.length">
					<view class="card-title"><view class="ct-dot"></view>我的代理区域</view>
					<view class="region-item" v-for="(item, index) in info.agentList" :key="index">
						<view class="region-left">
							<view class="region-name line1">{{ item.regionName }}</view>
							<view class="region-sub">{{ levelLabel(item.level) }}<text v-if="item.ratio > 0"> · 奖励比例 {{ item.ratio }}%</text></view>
						</view>
						<text class="st-pill" :class="'s' + item.status">{{ statusLabel(item.status) }}</text>
					</view>
				</view>

				<!-- 审核中提示 -->
				<view class="pending-box" v-if="hasPending">
					<text class="iconfont icon-jiazai pending-icon"></text>
					<view class="pending-text">您的代理申请正在审核中，请耐心等待</view>
				</view>

				<!-- 申请表单（无有效代理时显示） -->
				<view class="apply-card" v-if="canApply">
					<view class="card-title"><view class="ct-dot"></view>申请成为区域代理</view>
					<view class="apply-steps">
						<view class="as-item">
							<view class="as-no">1</view>
							<view class="as-text">选择级别区域</view>
						</view>
						<view class="as-arrow"></view>
						<view class="as-item">
							<view class="as-no">2</view>
							<view class="as-text">提交平台审核</view>
						</view>
						<view class="as-arrow"></view>
						<view class="as-item">
							<view class="as-no">3</view>
							<view class="as-text">奖励比例生效</view>
						</view>
					</view>
					<view class="apply-desc">锁定对应区域所有自然订单收益，订单收货地址归属您的代理区域时，自动按比例获得奖励</view>
					<view class="form-box">
						<view class="f-row">
							<text class="f-label">申请级别</text>
							<picker :range="levelNames" :value="levelIndex" @change="onLevelChange">
								<view class="f-value">{{ levelNames[levelIndex] }}<text class="iconfont icon-xiangyou"></text></view>
							</picker>
						</view>
						<view class="f-row">
							<text class="f-label">选择区域</text>
							<picker mode="multiSelector" :value="multiIndex" :range="multiArray" @change="onRegionChange" @columnchange="onRegionColumnChange">
								<view class="f-value" :class="{ placeholder: !regionText }">{{ regionText || regionPlaceholder }}<text class="iconfont icon-xiangyou"></text></view>
							</picker>
						</view>
						<view class="f-row">
							<text class="f-label">申请说明</text>
							<input class="f-input" v-model="applyMark" placeholder="选填" placeholder-class="placeholder-input" />
						</view>
					</view>
					<button class="apply-btn" @click="onApply">提交申请</button>
					<view class="apply-note">提交后由平台审核，审核通过并由平台设置奖励比例后生效</view>
				</view>

				<!-- 奖励明细（已是代理或有奖励记录时显示） -->
				<view class="reward-card" v-if="info.isAgent || rewardList.length">
					<view class="card-title"><view class="ct-dot"></view>区域奖励明细</view>
					<view class="reward-item" v-for="(item, index) in rewardList" :key="index">
						<view class="reward-left">
							<view class="reward-order line1">订单 {{ item.orderId }}</view>
							<view class="reward-sub">{{ item.createTime }} · {{ item.regionName }} · {{ item.ratio }}%</view>
						</view>
						<view class="reward-right">
							<view class="reward-num">+{{ item.rewardPrice }}</view>
							<view class="reward-status" :class="'st' + item.status">{{ statusTextLabel(item.status) }}</view>
						</view>
					</view>
					<view class="loadingicon" v-if="rewardList.length">
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
	import { guardModule } from '@/libs/moduleSwitch.js';
	import { getCityList } from '@/utils';
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
				levelIndex: 0,
				applyRegions: '1,2,3',
				regionText: '',
				regionArr: [],
				district: [],
				multiArray: [[], [], []],
				multiIndex: [0, 0, 0],
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
			// 后台配置的会员端可申请区域级别（1省级 2市级 3区级）
			levelOptions() {
				const all = [
					{ level: 1, name: '省级代理' },
					{ level: 2, name: '市级代理' },
					{ level: 3, name: '区级代理' },
				];
				const allow = String(this.applyRegions || '')
					.split(',')
					.map((s) => Number(String(s).trim()))
					.filter((n) => n === 1 || n === 2 || n === 3);
				if (!allow.length) return [];
				return all.filter((o) => allow.indexOf(o.level) > -1);
			},
			levelNames() {
				return this.levelOptions.map((o) => o.name);
			},
			currentLevel() {
				const o = this.levelOptions[this.levelIndex];
				return o ? o.level : 1;
			},
			regionPlaceholder() {
				const map = { 1: '请选择省份', 2: '请选择省市', 3: '请选择省市区' };
				return map[this.currentLevel] || '请选择区域';
			},
			canApply() {
				if (this.info.funcStatus !== '1') return false;
				if (this.info.isAgent || this.hasPending) return false;
				if (!this.levelOptions.length) return false;
				return this.info.applyStatus === '1';
			},
		},
		onLoad() {
			guardModule('daili');
			this.loadCityList();
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
			statusTextLabel(status) {
				const map = { 1: '待入账', 2: '已入账', 3: '已失效' };
				return map[status] || '-';
			},
			loadInfo() {
				getAgentInfo()
					.then((res) => {
						if (res.data) this.info = res.data;
						// 后台配置的可申请区域级别
						if (res.data && res.data.applyRegions !== undefined && res.data.applyRegions !== null) {
							this.applyRegions = String(res.data.applyRegions);
						}
						this.levelIndex = 0;
						this.resetRegion();
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
				this.resetRegion();
			},
			// 切换级别时重置区域选择，并按级别联动出对应列数（省级只到省、市级到市、区级到区县）
			resetRegion() {
				this.regionArr = [];
				this.regionText = '';
				this.buildMultiArray(this.currentLevel);
			},
			// 加载省市区数据（与地址管理同源，优先走本地缓存）
			loadCityList() {
				const cached = this.$Cache && this.$Cache.getItem('cityList');
				if (cached && cached.length) {
					this.district = cached;
					this.buildMultiArray(this.currentLevel);
					return;
				}
				uni.showLoading({ title: '数据加载中...' });
				getCityList()
					.then((res) => {
						this.district = res || [];
						this.buildMultiArray(this.currentLevel);
						uni.hideLoading();
					})
					.catch(() => {
						uni.hideLoading();
					});
			},
			// 依据 district 构建三级联动列数据
			buildMultiArray(level) {
				if (!this.district || !this.district.length) return;
				const lv = Number(level) || 1;
				const province = this.district.map((item) => item.name);
				const cityChildren = (this.district[0] && this.district[0].child) || [];
				const city = cityChildren.map((item) => item.name);
				const areaChildren = (cityChildren[0] && cityChildren[0].child) || [];
				const area = areaChildren.map((item) => item.name);
				if (lv === 1) {
					this.multiArray = [province];
					this.multiIndex = [0];
				} else if (lv === 2) {
					this.multiArray = [province, city];
					this.multiIndex = [0, 0];
				} else {
					this.multiArray = [province, city, area];
					this.multiIndex = [0, 0, 0];
				}
			},
			// 滚动某一列时联动刷新后续列
			onRegionColumnChange(e) {
				const column = e.detail.column;
				const value = e.detail.value;
				const multiArray = this.multiArray.slice();
				const multiIndex = this.multiIndex.slice();
				multiIndex[column] = value;
				if (column === 0) {
					const cities = (this.district[value] && this.district[value].child) || [];
					if (multiArray.length > 1) multiArray[1] = cities.map((item) => item.name);
					if (multiArray.length > 2) {
						const areas = (cities[0] && cities[0].child) || [];
						multiArray[2] = areas.map((item) => item.name);
					}
					if (multiIndex.length > 1) multiIndex[1] = 0;
					if (multiIndex.length > 2) multiIndex[2] = 0;
				} else if (column === 1) {
					const province = this.district[multiIndex[0]] || {};
					const cities = province.child || [];
					if (multiArray.length > 2) {
						const areas = (cities[value] && cities[value].child) || [];
						multiArray[2] = areas.map((item) => item.name);
						multiIndex[2] = 0;
					}
				}
				this.multiArray = multiArray;
				this.multiIndex = multiIndex;
			},
			// 确认选择：取名称（不是索引），供提交使用
			onRegionChange(e) {
				const value = (e.detail && e.detail.value) || [];
				this.multiIndex = value;
				this.regionArr = value.map((v, i) => (this.multiArray[i] || [])[v] || '');
				this.regionText = this.regionArr.filter(Boolean).join(' / ');
			},
			onApply() {
				const opt = this.levelOptions[this.levelIndex];
				if (!opt) return this.$util.Tips({ title: '当前未开放该区域级别的申请' });
				const level = opt.level;
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
		min-height: 100vh;
		padding: 24rpx 24rpx 60rpx;
		background: #f5f6fa;
		box-sizing: border-box;
	}

	/* ---------- 功能关闭 ---------- */
	.closed-box {
		background: #fff;
		border-radius: 24rpx;
		padding: 90rpx 30rpx;
		text-align: center;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
	}
	.closed-icon {
		font-size: 70rpx;
		color: #d9dee8;
	}
	.closed-text {
		margin-top: 20rpx;
		color: #8a94a6;
		font-size: 28rpx;
	}

	/* ---------- 代理身份 + 收益头卡 ---------- */
	.head-card {
		position: relative;
		overflow: hidden;
		background: linear-gradient(135deg, #6a3df2 0%, #8d63ff 55%, #a98bff 100%);
		border-radius: 24rpx;
		padding: 36rpx 32rpx 32rpx;
		color: #fff;
		box-shadow: 0 10rpx 30rpx rgba(106, 61, 242, 0.28);

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
		position: relative;
		z-index: 1;
	}
	.earn-label {
		margin-top: 22rpx;
		font-size: 24rpx;
		opacity: 0.8;
		position: relative;
		z-index: 1;
	}
	.earn-num {
		margin-top: 6rpx;
		font-size: 64rpx;
		font-weight: 700;
		line-height: 76rpx;
		position: relative;
		z-index: 1;
	}
	.head-divider {
		margin: 26rpx 0 22rpx;
		height: 1rpx;
		background: rgba(255, 255, 255, 0.28);
		position: relative;
		z-index: 1;
	}
	.h-stat-row {
		display: flex;
		align-items: center;
		position: relative;
		z-index: 1;
	}
	.h-stat {
		flex: 1;
		text-align: center;
	}
	.h-num {
		font-size: 36rpx;
		font-weight: 700;
	}
	.h-label {
		margin-top: 6rpx;
		font-size: 22rpx;
		opacity: 0.8;
	}
	.h-stat-divider {
		width: 1rpx;
		height: 56rpx;
		background: rgba(255, 255, 255, 0.28);
	}
	.head-tips {
		margin-top: 24rpx;
		font-size: 20rpx;
		opacity: 0.65;
		position: relative;
		z-index: 1;
	}

	/* ---------- 通用卡片 ---------- */
	.region-card,
	.reward-card,
	.apply-card,
	.pending-box {
		background: #fff;
		border-radius: 24rpx;
		padding: 30rpx 28rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
	}
	.card-title {
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-weight: 600;
		color: #303133;
		margin-bottom: 22rpx;
	}
	.ct-dot {
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: #6a3df2;
		margin-right: 12rpx;
	}

	/* ---------- 我的代理区域 ---------- */
	.region-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 22rpx 0;
		border-bottom: 1rpx solid #f0f2f6;
		&:last-child {
			border-bottom: none;
			padding-bottom: 4rpx;
		}
		.region-left {
			flex: 1;
			overflow: hidden;
			margin-right: 20rpx;
		}
		.region-name {
			font-size: 28rpx;
			font-weight: 600;
			color: #303133;
		}
		.region-sub {
			margin-top: 8rpx;
			font-size: 22rpx;
			color: #98a2b3;
		}
	}
	.st-pill {
		flex-shrink: 0;
		font-size: 22rpx;
		border-radius: 999rpx;
		padding: 6rpx 18rpx;
		&.s0 {
			color: #c99a25;
			background: #fdf5e2;
		}
		&.s1 {
			color: #12b76a;
			background: #e9f9ec;
		}
		&.s2 {
			color: #98a2b3;
			background: #f0f2f6;
		}
	}

	/* ---------- 审核中 ---------- */
	.pending-box {
		text-align: center;
		padding: 44rpx 30rpx;
		background: #fdf5e2;
	}
	.pending-icon {
		font-size: 56rpx;
		color: #c99a25;
	}
	.pending-text {
		margin-top: 14rpx;
		color: #8a6d1d;
		font-size: 26rpx;
	}

	/* ---------- 申请表单 ---------- */
	.apply-desc {
		font-size: 24rpx;
		color: #98a2b3;
		line-height: 1.7;
		margin-bottom: 22rpx;
	}
	.apply-steps {
		display: flex;
		align-items: flex-start;
		background: #f7f4ff;
		border-radius: 16rpx;
		padding: 24rpx 12rpx 20rpx;
		margin-bottom: 24rpx;
	}
	.as-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
	}
	.as-no {
		width: 44rpx;
		height: 44rpx;
		line-height: 44rpx;
		text-align: center;
		border-radius: 50%;
		background: #6a3df2;
		color: #fff;
		font-size: 22rpx;
		font-weight: 700;
	}
	.as-text {
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #606266;
		text-align: center;
		line-height: 32rpx;
	}
	.as-arrow {
		flex-shrink: 0;
		width: 32rpx;
		height: 32rpx;
		margin-top: 6rpx;
		border-top: 4rpx solid #d5c8ff;
		border-right: 4rpx solid #d5c8ff;
		transform: rotate(45deg);
	}
	.form-box {
		background: #f8f9fc;
		border-radius: 16rpx;
		padding: 6rpx 24rpx;
	}
	.f-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 28rpx 0;
		border-bottom: 1rpx solid #eef0f4;
		&:last-child {
			border-bottom: none;
		}
		.f-label {
			width: 150rpx;
			flex-shrink: 0;
			font-size: 26rpx;
			color: #8a94a6;
		}
		.f-value {
			flex: 1;
			text-align: right;
			font-size: 28rpx;
			color: #303133;
			font-weight: 500;
			.iconfont {
				margin-left: 8rpx;
				font-size: 22rpx;
				color: #c3cad6;
			}
		}
		.placeholder {
			color: #b8c0cc;
			font-weight: 400;
		}
		.f-input {
			flex: 1;
			text-align: right;
			font-size: 28rpx;
			color: #303133;
		}
	}
	.placeholder-input {
		color: #b8c0cc;
	}
	.apply-btn {
		margin-top: 36rpx;
		height: 84rpx;
		line-height: 84rpx;
		background: linear-gradient(135deg, #e93323, #ff6a4d);
		color: #fff;
		font-size: 30rpx;
		font-weight: 600;
		letter-spacing: 2rpx;
		border-radius: 16rpx;
		box-shadow: 0 8rpx 18rpx rgba(233, 51, 35, 0.28);
		&::after {
			border: none;
		}
	}
	.apply-note {
		margin-top: 18rpx;
		text-align: center;
		font-size: 20rpx;
		color: #b8c0cc;
	}

	/* ---------- 奖励明细 ---------- */
	.reward-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 22rpx 0;
		border-bottom: 1rpx solid #f0f2f6;
		&:last-child {
			border-bottom: none;
			padding-bottom: 4rpx;
		}
		.reward-left {
			flex: 1;
			overflow: hidden;
			margin-right: 20rpx;
			.reward-order {
				font-size: 26rpx;
				font-weight: 600;
				color: #303133;
			}
			.reward-sub {
				margin-top: 8rpx;
				font-size: 22rpx;
				color: #98a2b3;
			}
		}
		.reward-right {
			flex-shrink: 0;
			text-align: right;
			.reward-num {
				font-size: 32rpx;
				font-weight: 700;
				color: #ff5a2c;
			}
			.reward-status {
				margin-top: 6rpx;
				font-size: 20rpx;
				color: #b8c0cc;
				&.st2 {
					color: #12b76a;
				}
				&.st3 {
					color: #c3cad6;
				}
			}
		}
	}
	.loadingicon {
		font-size: 24rpx;
		color: #98a2b3;
		padding: 16rpx 0 4rpx;
		text-align: center;
	}
</style>
