<template>
	<view :data-theme="theme">
		<view class='my-promotion'>
			<!-- 佣金主卡：渐变由主题色派生（亮端 = 主题色提亮），换主题自动适配 -->
			<view class="commission-card" :style="headerStyle">
				<view class="card-top">
					<text class="card-label">当前佣金（元）</text>
					<navigator :url="'/pages/promoter/user_spread_money/index?type=1&extractCount='+spreadInfo.extractCount" hover-class="none" class='record'>
						<text>提现记录</text>
						<text class='iconfont icon-xiangyou'></text>
					</navigator>
				</view>
				<view class='amount-row'>
					<text class='yen'>¥</text>
					<text class='num'>{{ commissionText }}</text>
				</view>
				<view class='amount-tip'>可提现佣金余额，明细可在「佣金明细」查看</view>
				<view class='level-box'>
					<view class='level-item'>
						<text class='label'>分销级别</text>
						<text class='val'>{{spreadInfo.distributorLevelName || '未获得'}}</text>
					</view>
					<view class='level-line'></view>
					<view class='level-item'>
						<text class='label'>团队奖级别</text>
						<text class='val'>{{spreadInfo.teamLevelName || '未获得'}}</text>
					</view>
				</view>
				<view class='stat-row'>
					<view class='stat-item'>
						<view class='label'>昨日收益（元）</view>
						<view class='money'>{{ lastDayText }}</view>
					</view>
					<view class='stat-item'>
						<view class='label'>累计已提（元）</view>
						<view class='money'>{{ extractText }}</view>
					</view>
				</view>
				<!-- #ifdef APP-PLUS || H5 -->
				<navigator url="/pages/users/user_cash/index" hover-class="none" class='cash-btn' :style="{color: bgColor}">立即提现</navigator>
				<!-- #endif -->
				<!-- #ifdef MP -->
				<view @click="openSubscribe('/pages/users/user_cash/index')" class='cash-btn' :style="{color: bgColor}">立即提现</view>
				<!-- #endif -->
			</view>
			<!-- 零收益引导条：仅佣金为 0 时显示，点击直达推广名片 -->
			<navigator v-if="showGuide" url="/pages/promoter/user_spread_code/index" hover-class="none" class='guide-bar'>
				<text class='guide-icon'>💡</text>
				<text class='guide-text'>还没有收益？分享推广名片邀请好友，下单即得佣金</text>
				<text class='guide-link' :style="{color: bgColor}">去推广</text>
			</navigator>
			<!-- 推广工具：6 个入口与跳转路径保持不变 -->
			<view class='tool-section'>
				<view class='section-title'>推广工具</view>
				<view class='tool-grid'>
					<navigator url='/pages/promoter/user_spread_code/index' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-erweima' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>推广名片</view>
						<view class='item-desc'>专属二维码海报，分享邀新</view>
					</navigator>
					<navigator url='/pages/promoter/promoter-list/index' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-tongji' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>推广人统计</view>
						<view class='item-desc'>团队人数与推广等级一览</view>
					</navigator>
					<navigator :url="'/pages/promoter/user_spread_money/index?type=2&commissionCount='+spreadInfo.commissionCount" hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-qiandai' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>佣金明细</view>
						<view class='item-desc'>每笔佣金来源与到账记录</view>
					</navigator>
					<navigator url='/pages/promoter/user_spread_money/index?type=3' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-tuandui' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>社群奖记录</view>
						<view class='item-desc'>团队奖励的发放明细</view>
					</navigator>
					<navigator url='/pages/promoter/promoter-order/index' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-dingdan' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>推广人订单</view>
						<view class='item-desc'>下级订单状态跟进</view>
					</navigator>
					<navigator url='/pages/promoter/my_referrer/index' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-geren' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>我的推荐人</view>
						<view class='item-desc'>我的上级推荐关系</view>
					</navigator>
					<navigator url='/pages/promoter/chain_center/index' hover-class="none" class='tool-item'>
						<view class='icon-wrap' :style="{backgroundColor: tintBg}">
							<text class='iconfont icon-tuandui' :style="{color: bgColor}"></text>
						</view>
						<view class='item-title'>链动中心</view>
						<view class='item-desc'>2+1 链动收益与团队</view>
					</navigator>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { getSpreadInfo } from '@/api/user.js';
	import {toLogin} from '@/libs/login.js';
	import {mapGetters} from "vuex";
	import { setThemeColor } from '@/utils/setTheme.js'
	import { guardModule } from '@/libs/moduleSwitch.js'
	const app = getApp();
	export default {
		data() {
			return {
				spreadInfo: {},
				loaded: false,
				theme:app.globalData.theme,
				bgColor:'#e93323'
			};
		},
		computed: {
			...mapGetters(['isLogin']),
			/* 主题色派生：亮端提亮 22%，图标底 8% 透明度（换主题自动适配） */
			headerStyle() {
				return {
					background: 'linear-gradient(160deg, ' + this.lightenHex(this.bgColor, 0.22) + ' 0%, ' + this.bgColor + ' 100%)'
				};
			},
			tintBg() {
				return this.hexToRgba(this.bgColor, 0.08);
			},
			commissionText() {
				return (Number(this.spreadInfo.commissionCount) || 0).toFixed(2);
			},
			lastDayText() {
				return (Number(this.spreadInfo.lastDayCount) || 0).toFixed(2);
			},
			extractText() {
				return (Number(this.spreadInfo.extractCount) || 0).toFixed(2);
			},
			showGuide() {
				return this.loaded && (Number(this.spreadInfo.commissionCount) || 0) === 0;
			}
		},
		watch: {
			isLogin: {
				handler: function(newV, oldV) {
					if (newV) {
						this.getSpreadInfo();
					}
				},
				deep: true
			}
		},
		onShow() {
			let that = this;
			guardModule('spread');
			that.bgColor = setThemeColor();
			uni.setNavigationBarColor({
				frontColor: '#ffffff',
				backgroundColor:that.bgColor,
			});

			if (this.isLogin) {
				this.getSpreadInfo();
			} else {
				toLogin();
			}
		},
		methods: {
			openSubscribe: function(page) {
				uni.navigateTo({
					url: page,
				});
			},
			/**
			 * hex 提亮：与白色按 ratio 混合（ratio 0~1）
			 */
			lightenHex: function(hex, ratio) {
				let c = this.hexToRgb(hex);
				if (!c) return hex;
				let r = Math.round(c.r + (255 - c.r) * ratio);
				let g = Math.round(c.g + (255 - c.g) * ratio);
				let b = Math.round(c.b + (255 - c.b) * ratio);
				return '#' + [r, g, b].map(function(v) { return ('0' + v.toString(16)).slice(-2); }).join('');
			},
			/**
			 * hex 转 rgba 字符串
			 */
			hexToRgba: function(hex, alpha) {
				let c = this.hexToRgb(hex);
				if (!c) return hex;
				return 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',' + alpha + ')';
			},
			hexToRgb: function(hex) {
				if (!hex || typeof hex !== 'string') return null;
				let h = hex.replace('#', '');
				if (h.length === 3) h = h.split('').map(function(s) { return s + s; }).join('');
				if (h.length !== 6) return null;
				return {
					r: parseInt(h.slice(0, 2), 16),
					g: parseInt(h.slice(2, 4), 16),
					b: parseInt(h.slice(4, 6), 16)
				};
			},
			/**
			 * 获取个人用户信息
			 */
			getSpreadInfo: function() {
				let that = this;
				getSpreadInfo().then(res => {
					that.$set(that,'spreadInfo',res.data);
					that.loaded = true;
				});
			}
		}
	}
</script>

<style scoped lang="scss">
	.my-promotion {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding: 20rpx 24rpx 50rpx 24rpx;
		box-sizing: border-box;
	}

	/* ===== 佣金主卡 ===== */
	.my-promotion .commission-card {
		border-radius: 32rpx;
		padding: 36rpx 32rpx;
		color: #fff;
		box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
	}
	.my-promotion .commission-card .card-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.my-promotion .commission-card .card-label {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.85);
	}
	.my-promotion .commission-card .record {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
	}
	.my-promotion .commission-card .record .iconfont {
		font-size: 24rpx;
		margin-left: 4rpx;
	}
	.my-promotion .commission-card .amount-row {
		display: flex;
		align-items: baseline;
		margin-top: 20rpx;
	}
	.my-promotion .commission-card .amount-row .yen {
		font-size: 44rpx;
		font-weight: bold;
	}
	.my-promotion .commission-card .amount-row .num {
		font-size: 88rpx;
		font-weight: bold;
		line-height: 1.1;
		margin-left: 8rpx;
	}
	.my-promotion .commission-card .amount-tip {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.7);
		margin-top: 8rpx;
	}
	.my-promotion .commission-card .level-box {
		display: flex;
		align-items: center;
		margin-top: 28rpx;
		height: 68rpx;
		border-radius: 34rpx;
		background-color: rgba(255, 255, 255, 0.16);
	}
	.my-promotion .commission-card .level-box .level-item {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.8);
	}
	.my-promotion .commission-card .level-box .level-item .val {
		font-size: 24rpx;
		font-weight: bold;
		color: #fff;
		margin-left: 10rpx;
		max-width: 200rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.my-promotion .commission-card .level-box .level-line {
		width: 2rpx;
		height: 28rpx;
		background-color: rgba(255, 255, 255, 0.35);
	}
	.my-promotion .commission-card .stat-row {
		display: flex;
		justify-content: space-between;
		margin-top: 20rpx;
	}
	.my-promotion .commission-card .stat-row .stat-item {
		width: 48.4%;
		background-color: rgba(255, 255, 255, 0.14);
		border-radius: 20rpx;
		padding: 20rpx;
		box-sizing: border-box;
	}
	.my-promotion .commission-card .stat-row .stat-item .label {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.8);
	}
	.my-promotion .commission-card .stat-row .stat-item .money {
		font-size: 36rpx;
		font-weight: bold;
		margin-top: 8rpx;
	}
	.my-promotion .commission-card .cash-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 28rpx;
		height: 88rpx;
		background-color: #fff;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-weight: 500;
		box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.12);
	}

	/* ===== 零收益引导条 ===== */
	.my-promotion .guide-bar {
		display: flex;
		align-items: center;
		margin-top: 20rpx;
		padding: 20rpx 24rpx;
		background-color: #FFF7E8;
		border-radius: 24rpx;
	}
	.my-promotion .guide-bar .guide-icon {
		font-size: 28rpx;
		margin-right: 12rpx;
	}
	.my-promotion .guide-bar .guide-text {
		flex: 1;
		font-size: 24rpx;
		color: #9A6312;
		line-height: 34rpx;
	}
	.my-promotion .guide-bar .guide-link {
		font-size: 24rpx;
		font-weight: bold;
		margin-left: 12rpx;
	}

	/* ===== 推广工具入口 ===== */
	.my-promotion .tool-section {
		margin-top: 28rpx;
	}
	.my-promotion .tool-section .section-title {
		font-size: 28rpx;
		font-weight: bold;
		color: #333;
		margin: 0 8rpx 20rpx 8rpx;
	}
	.my-promotion .tool-section .tool-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}
	.my-promotion .tool-section .tool-item {
		width: 341rpx;
		background-color: #fff;
		border-radius: 24rpx;
		padding: 28rpx;
		box-sizing: border-box;
		margin-bottom: 20rpx;
	}
	.my-promotion .tool-section .tool-item:active {
		background-color: #fafafa;
		transform: scale(0.98);
	}
	.my-promotion .tool-section .tool-item .icon-wrap {
		width: 72rpx;
		height: 72rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.my-promotion .tool-section .tool-item .icon-wrap .iconfont {
		font-size: 40rpx;
	}
	.my-promotion .tool-section .tool-item .item-title {
		margin-top: 16rpx;
		font-size: 28rpx;
		color: #333;
	}
	.my-promotion .tool-section .tool-item .item-desc {
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #999;
		line-height: 32rpx;
	}
</style>
