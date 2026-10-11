<template>
	<view :data-theme="theme" class="cr-page">
		<!-- 头部 -->
		<view class="head">
			<view class="h-t">{{ agentName }} / {{ bossName }} 晋升机制</view>
			<view class="h-s">基于真实商品交易的推广佣金／服务奖励</view>
		</view>

		<view class="card">
			<view class="card-t">一、身份与升级</view>
			<view class="p">
				· 购买链动礼包<text v-if="meta.activeThreshold > 0">（实付 ≥ ¥{{ money(meta.activeThreshold) }}）</text>
				<text v-if="meta.afterSaleDays > 0">并通过售后保护期（{{ meta.afterSaleDays }} 天）</text> → 成为<text class="b">{{ agentName }}</text><br>
				· 直推满 <text class="b">{{ meta.directCapacity || 2 }}</text> 名有效{{ agentName }} → 自动晋升<text class="b">{{ bossName }}</text><br>
				· {{ bossName }}身份不可降级
			</view>
		</view>

		<view class="card" v-if="meta.leaveTeamEnabled">
			<view class="card-t">二、走人留人</view>
			<view class="p">
				· 晋升{{ bossName }}后，本人<text class="b">脱离</text>原上级团队、独立发展<br>
				· 升级前的直推中，前 <text class="b">{{ meta.directCapacity || 2 }}</text> 名继续留在原上级名下<br>
				· 第 <text class="b">{{ (meta.directCapacity || 2) + 1 }}</text> 名及以后的直推随本人走
			</view>
			<view class="note">升级前后均可到「我的团队」页查看留人座位与团队结构。</view>
		</view>

		<view class="card">
			<view class="card-t">三、奖励规则</view>
			<view class="p">
				· <text class="b">直推奖</text>：直推会员购买礼包<text v-if="meta.directAmount > 0">，固定 ¥{{ money(meta.directAmount) }}／单</text><text v-else>，按后台配置比例</text>（1 级）<br>
				· <text class="b">见点奖</text>：团队有效订单实付 × <text class="b">{{ pct(meta.seeRatio) }}</text>（仅直属上级{{ bossName }}，1 级）<br>
				· <text class="b">平级奖</text>：下级{{ bossName }}的见点奖 × <text class="b">{{ pct(meta.peerRatio) }}</text>（仅上级{{ bossName }}，1 级）<br>
				· 单笔订单链动分润合计不超过实付的 <text class="b">{{ pct(meta.maxBrokerageRatio) }}</text>
			</view>
		</view>

		<view class="card">
			<view class="card-t">四、帮扶基金</view>
			<view class="p">
				· 每笔见点奖 / 平级奖的 <text class="b">{{ pct(meta.helpFreezeRatio) }}</text> 进入帮扶基金冻结<br>
				· 解冻条件：留人座位均晋升{{ bossName }}，且各自团队有效订单 ≥ <text class="b">{{ meta.helpUnlockN || 10 }}</text>、本人团队有效订单 ≥ <text class="b">{{ meta.helpUnlockM || 20 }}</text><br>
				· 全部满足后<text class="b">自动解冻</text>，转入可提现佣金，无需申请
			</view>
		</view>

		<view class="card" v-if="content">
			<view class="card-t">五、完整说明</view>
			<view class="pre">{{ content }}</view>
		</view>

		<view class="card plain">
			<view class="cmplx-t">合规声明</view>
			<view class="cmplx">
				本机制奖励为「推广佣金 / 服务奖励」，均基于真实商品交易产生，不构成任何投资建议，不承诺收益。
				平台禁止以任何形式宣传「投资」「躺赚」「稳赚」等表述。
			</view>
		</view>

		<view class="btn" @click="goCenter">返回链动中心</view>
	</view>
</template>

<script>
	import { guardModule } from '@/libs/moduleSwitch.js';
	import { chainRules } from '@/api/chain.js';
	let app = getApp();
	export default {
		data() {
			return {
				theme: app.globalData.theme,
				meta: {},
				content: '',
				agentName: '代理',
				bossName: '老板'
			};
		},
		onLoad() {
			guardModule('chain');
			this.loadRules();
		},
		methods: {
			loadRules() {
				chainRules().then((res) => {
					const d = (res.data && res.data) || {};
					this.meta = d;
					this.content = d.content || '';
					this.agentName = d.agentName || '代理';
					this.bossName = d.bossName || '老板';
				}).catch(() => {});
			},
			money(v) {
				return Number(v || 0).toFixed(2);
			},
			pct(v) {
				const n = Number(v || 0);
				if (!n) return '—';
				return n.toFixed(n % 1 === 0 ? 0 : 2) + '%';
			},
			goCenter() {
				uni.navigateBack({
					fail: () => {
						uni.redirectTo({ url: '/pages/promoter/chain_center/index' });
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
	.cr-page {
		min-height: 100vh;
		padding: 24rpx 24rpx 60rpx;
		background: #f5f6fa;
		box-sizing: border-box;
	}

	.head {
		position: relative;
		overflow: hidden;
		border-radius: 36rpx;
		padding: 34rpx 32rpx;
		margin-bottom: 24rpx;
		color: #fff;
		background: linear-gradient(135deg, #16337c 0%, #1f5fd6 52%, #3a8df2 100%);
		box-shadow: 0 20rpx 52rpx rgba(31, 95, 214, 0.3);
		&::before {
			content: '';
			position: absolute;
			width: 320rpx;
			height: 320rpx;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.11);
			right: -110rpx;
			top: -140rpx;
		}
	}
	.h-t { font-size: 34rpx; font-weight: 700; position: relative; z-index: 1; }
	.h-s { margin-top: 12rpx; font-size: 23rpx; opacity: 0.86; position: relative; z-index: 1; }

	.card {
		background: #fff;
		border-radius: 28rpx;
		padding: 28rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.card.plain { background: #fafbfd; box-shadow: none; }
	.card-t {
		display: flex;
		align-items: center;
		gap: 14rpx;
		font-size: 29rpx;
		font-weight: 600;
		margin-bottom: 16rpx;
		&::before {
			content: '';
			width: 8rpx;
			height: 28rpx;
			border-radius: 4rpx;
			background: linear-gradient(180deg, #1f5fd6, #3a8df2);
		}
	}
	.p {
		font-size: 25rpx;
		color: #4b5563;
		line-height: 1.95;
		.b { font-weight: 700; color: #16337c; }
	}
	.note {
		margin-top: 16rpx;
		padding: 16rpx 20rpx;
		border-radius: 18rpx;
		background: #fafbfd;
		font-size: 22rpx;
		color: #8a94a6;
		line-height: 1.7;
	}
	.pre {
		font-size: 24rpx;
		color: #4b5563;
		line-height: 1.95;
		white-space: pre-wrap;
		word-break: break-all;
	}
	.cmplx-t { font-size: 27rpx; font-weight: 600; margin-bottom: 12rpx; color: #374151; }
	.cmplx { font-size: 23rpx; color: #8a94a6; line-height: 1.85; }

	.btn {
		display: block;
		text-align: center;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		font-size: 29rpx;
		font-weight: 600;
		background: linear-gradient(135deg, #1f5fd6, #3a8df2);
		color: #fff;
		margin-top: 30rpx;
		box-shadow: 0 12rpx 32rpx rgba(31, 95, 214, 0.28);
	}
</style>
