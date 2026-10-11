<template>
	<view :data-theme="theme" class="cc-page">
		<!-- 功能未开启 -->
		<view class="closed-box" v-if="!enabled">
			<text class="iconfont icon-huangguan4 closed-icon"></text>
			<view class="closed-text">链动功能暂未开启</view>
		</view>

		<template v-else>
			<!-- ========== 身份卡 ========== -->
			<view class="hero" :class="{ 'hero-plain': !isAgent }">
				<view class="hero-inner">
					<view class="lvl">
						<text class="crown" v-if="isBoss">♛</text>
						{{ info.identityName || '未激活' }}
					</view>
					<view class="who">会员ID <text class="strong">{{ uid || '-' }}</text> · 昵称 <text class="strong">{{ nickname || '-' }}</text></view>
					<view class="big">
						¥{{ money(info.totalAmount) }}
						<text class="unit">累计推广佣金</text>
					</view>
					<view class="hr"></view>
					<view class="stats">
						<view class="st">
							<view class="n">{{ info.teamSize || 0 }}</view>
							<view class="l">团队人数</view>
						</view>
						<view class="st-div"></view>
						<view class="st">
							<view class="n">{{ info.effectiveDirectCount || 0 }}</view>
							<view class="l">直推有效</view>
						</view>
						<view class="st-div"></view>
						<view class="st">
							<view class="n">{{ info.teamBossCount || 0 }}</view>
							<view class="l">已升{{ bossName }}</view>
						</view>
					</view>

					<!-- 未升老板：直推进度 -->
					<view class="prog" v-if="!isBoss">
						<view class="pt">
							<text>直推进度</text>
							<text class="pv">{{ info.effectiveDirectCount || 0 }} / {{ info.bossNeedCount || 2 }}</text>
						</view>
						<view class="bar"><i :style="{ width: bossPercent + '%' }"></i></view>
						<view class="hint" v-if="(info.bossRemainCount || 0) > 0">
							再直推 <text class="hl">{{ info.bossRemainCount }}</text> 名有效{{ agentName }}即可晋升{{ bossName }}，解锁见点奖 + 平级奖
						</view>
						<view class="hint" v-else>直推条件已满，结算后自动晋升{{ bossName }}</view>
					</view>

					<!-- 已是老板：帮扶基金进度 -->
					<view class="prog" v-else>
						<view class="pt">
							<text>帮扶基金</text>
							<text class="pv">{{ info.helpUnlocked ? '已解锁' : '冻结中 ¥' + money(info.helpFundAmount) }}</text>
						</view>
						<view class="bar"><i :style="{ width: seatPercent + '%' }"></i></view>
						<view class="hint" v-if="info.helpUnlocked">
							留人的 {{ info.helpNeedSeatCount || 2 }} 名{{ agentName }}均已晋升{{ bossName }}，条件达标自动解冻
						</view>
						<view class="hint" v-else>
							留人座位达标 {{ info.helpUnlockedSeatCount || 0 }}/{{ info.helpNeedSeatCount || 2 }} · 每人团队 ≥ {{ info.helpUnlockNeedOrders }} 单 · 本人团队 ≥ {{ info.helpUnlockNeedTeamOrders }} 单
						</view>
					</view>
				</view>
			</view>

			<!-- ========== 收益总览 ========== -->
			<view class="money">
				<view class="m" @click="goCommission('')">
					<view class="n up">¥{{ money(info.todayAmount) }}</view>
					<view class="l">今日</view>
				</view>
				<view class="m-div"></view>
				<view class="m" @click="goCommission('')">
					<view class="n">¥{{ money(info.withdrawable) }}</view>
					<view class="l">可提现</view>
				</view>
				<view class="m-div"></view>
				<view class="m" @click="goCommission('')">
					<view class="n frozen">¥{{ money(info.freezeAmount) }}</view>
					<view class="l">冻结中</view>
				</view>
			</view>

			<!-- ========== 四宫格 ========== -->
			<view class="grid4">
				<view class="g" @click="goCommission('')">
					<view class="ic b1"><text class="iconfont icon-mingxi"></text></view>
					<view class="t">收益明细</view>
				</view>
				<view class="g" @click="goTeam()">
					<view class="ic b2"><text class="iconfont icon-tuandui"></text></view>
					<view class="t">我的团队</view>
				</view>
				<view class="g" @click="goTeam('help')">
					<view class="ic b3"><text class="iconfont icon-jifen"></text></view>
					<view class="t">帮扶基金</view>
				</view>
				<view class="g" @click="goPoster()">
					<view class="ic b4"><text class="iconfont icon-haibao"></text></view>
					<view class="t">推广海报</view>
				</view>
			</view>

			<!-- ========== 最近收益 ========== -->
			<view class="card">
				<view class="card-t" @click="goCommission('')">
					最近收益<text class="more">全部 ›</text>
				</view>
				<view class="li" v-for="(item, i) in recent" :key="i">
					<view class="dot" :class="typeCls(item.rewardType)">{{ typeShort(item.rewardType) }}</view>
					<view class="body">
						<view class="t1">{{ typeName(item.rewardType) }}</view>
						<view class="t2">{{ item.mark || ('订单 ' + (item.orderId || '-')) }}</view>
					</view>
					<view class="right">
						<view class="amt" :class="amountCls(item)">{{ amountText(item) }}</view>
						<view class="tm">{{ shortTime(item.createTime) }}</view>
					</view>
				</view>
				<view class="empty" v-if="!recent.length">暂无收益记录</view>
			</view>

			<!-- ========== 未激活引导 ========== -->
			<view class="guide" v-if="!isAgent">
				<view class="guide-t">还没有链动身份</view>
				<view class="guide-d">
					购买链动礼包<text v-if="threshold > 0">（实付 ≥ ¥{{ money(threshold) }}）</text>后自动成为{{ agentName }}，
					才能查看团队与收益
				</view>
				<view class="btn" @click="goGift()">去选购礼包</view>
			</view>

			<!-- ========== 底部操作 ========== -->
			<view class="btn-row">
				<view class="btn gold" @click="goPoster()">生成推广海报</view>
				<view class="btn ghost" @click="goRules()">规则说明</view>
			</view>

			<!-- ========== 通知入口 ========== -->
			<view class="notice" @click="goNotify()">
				<text class="iconfont icon-xiaoxi"></text>
				<text class="nt">消息通知</text>
				<text class="badge" v-if="unread > 0">{{ unread > 99 ? '99+' : unread }}</text>
				<text class="go">›</text>
			</view>

			<view class="foot">奖励为推广佣金／服务奖励，基于真实商品交易产生，不构成投资建议。</view>
		</template>
	</view>
</template>

<script>
	import { guardModule } from '@/libs/moduleSwitch.js';
	import { toLogin } from '@/libs/login.js';
	import { mapGetters } from 'vuex';
	import { chainMyIdentity, chainCommissionList, chainNotifyUnread, chainRules } from '@/api/chain.js';
	let app = getApp();
	export default {
		data() {
			return {
				theme: app.globalData.theme,
				enabled: true,
				uid: 0,
				nickname: '',
				info: {},
				recent: [],
				unread: 0,
				threshold: 0,
				agentName: '代理',
				bossName: '老板'
			};
		},
		computed: {
			...mapGetters(['isLogin', 'uid']),
			isAgent() {
				return (this.info.identity || 0) >= 1;
			},
			isBoss() {
				return (this.info.identity || 0) >= 2;
			},
			bossPercent() {
				const need = this.info.bossNeedCount || 2;
				const got = this.info.effectiveDirectCount || 0;
				return Math.min(100, Math.round((got / need) * 100));
			},
			seatPercent() {
				if (this.info.helpUnlocked) return 100;
				const need = this.info.helpNeedSeatCount || 2;
				const got = this.info.helpUnlockedSeatCount || 0;
				return Math.min(100, Math.round((got / need) * 100));
			}
		},
		onLoad() {
			guardModule('chain');
		},
		onShow() {
			if (!this.isLogin) {
				toLogin();
				return;
			}
			this.loadAll();
		},
		methods: {
			loadAll() {
				chainMyIdentity().then((res) => {
					this.info = res.data || {};
					this.nickname = this.info.nickname || '';
				}).catch(() => {});
				chainCommissionList({ page: 1, limit: 3 }).then((res) => {
					this.recent = (res.data && res.data.list) || [];
				}).catch(() => {});
				chainNotifyUnread().then((res) => {
					this.unread = (res.data && res.data.count) || 0;
				}).catch(() => {});
				chainRules().then((res) => {
					const d = (res.data && res.data) || {};
					this.agentName = d.agentName || '代理';
					this.bossName = d.bossName || '老板';
					this.threshold = Number(d.activeThreshold || 0);
				}).catch(() => {});
			},
			money(v) {
				const n = Number(v || 0);
				return n.toFixed(2);
			},
			typeName(t) {
				const map = {
					direct: '直推奖',
					see: '见点奖',
					peer: '平级奖',
					see_help: '见点奖 · 帮扶冻结',
					peer_help: '平级奖 · 帮扶冻结',
					help: '帮扶基金解冻',
					dividend: '团队分红'
				};
				return map[t] || t || '-';
			},
			typeShort(t) {
				const map = {
					direct: '直推',
					see: '见点',
					peer: '平级',
					see_help: '冻结',
					peer_help: '冻结',
					help: '解冻',
					dividend: '分红'
				};
				return map[t] || '收益';
			},
			typeCls(t) {
				return {
					'c-blue': t === 'direct' || t === 'peer' || t === 'dividend',
					'c-green': t === 'see' || t === 'help',
					'c-gold': t === 'see_help' || t === 'peer_help'
				};
			},
			amountCls(item) {
				return this.isFrozen(item) ? 'frozen' : 'up';
			},
			amountText(item) {
				const n = Number(item.amount || 0);
				return (n >= 0 ? '+' : '') + n.toFixed(2);
			},
			isFrozen(item) {
				// 链动流水状态 5 = 帮扶基金冻结中
				return Number(item.status || 0) === 5;
			},
			shortTime(t) {
				if (!t) return '';
				return String(t).replace('T', ' ').slice(0, 16);
			},
			goCommission(type) {
				uni.navigateTo({ url: '/pages/promoter/chain_commission/index?type=' + (type || '') });
			},
			goTeam(tab) {
				uni.navigateTo({ url: '/pages/promoter/chain_team/index' + (tab ? '?tab=' + tab : '') });
			},
			goRules() {
				uni.navigateTo({ url: '/pages/promoter/chain_rules/index' });
			},
			goNotify() {
				uni.navigateTo({ url: '/pages/promoter/chain_notify/index' });
			},
			goPoster() {
				uni.navigateTo({ url: '/pages/promoter/user_spread_code/index' });
			},
			goGift() {
				uni.switchTab({ url: '/pages/index/index' });
			}
		}
	};
</script>

<style lang="scss" scoped>
	.cc-page {
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
	.closed-icon { font-size: 70rpx; color: #d9dee8; }
	.closed-text { margin-top: 20rpx; color: #8a94a6; font-size: 28rpx; }

	/* ---------- 身份卡 ---------- */
	.hero {
		position: relative;
		overflow: hidden;
		border-radius: 36rpx;
		padding: 32rpx 32rpx 28rpx;
		color: #fff;
		margin-bottom: 24rpx;
		background: linear-gradient(135deg, #16337c 0%, #1f5fd6 52%, #3a8df2 100%);
		box-shadow: 0 20rpx 52rpx rgba(31, 95, 214, 0.3);

		&::before, &::after {
			content: '';
			position: absolute;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.11);
		}
		&::before { width: 380rpx; height: 380rpx; right: -128rpx; top: -160rpx; }
		&::after { width: 240rpx; height: 240rpx; right: 152rpx; bottom: -124rpx; }
	}
	.hero-plain {
		background: linear-gradient(135deg, #4b5563 0%, #6b7280 100%);
		box-shadow: 0 20rpx 52rpx rgba(75, 85, 99, 0.25);
	}
	.hero-inner { position: relative; z-index: 1; }
	.lvl {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		background: rgba(255, 255, 255, 0.22);
		border: 2rpx solid rgba(255, 255, 255, 0.42);
		border-radius: 999rpx;
		padding: 4rpx 24rpx;
		font-size: 23rpx;
		letter-spacing: 2rpx;
	}
	.crown { color: #f6d99d; }
	.who {
		margin-top: 22rpx;
		font-size: 25rpx;
		opacity: 0.86;
		.strong { font-weight: 600; }
	}
	.big {
		margin-top: 6rpx;
		font-size: 62rpx;
		font-weight: 700;
		letter-spacing: 1rpx;
		line-height: 1.25;
		.unit { font-size: 24rpx; font-weight: 400; opacity: 0.82; margin-left: 10rpx; }
	}
	.hr { height: 2rpx; background: rgba(255, 255, 255, 0.26); margin: 26rpx 0 22rpx; }
	.stats { display: flex; }
	.st { flex: 1; text-align: center; }
	.st .n { font-size: 34rpx; font-weight: 600; }
	.st .l { font-size: 22rpx; opacity: 0.78; margin-top: 4rpx; }
	.st-div { width: 2rpx; background: rgba(255, 255, 255, 0.24); }

	.prog { margin-top: 26rpx; }
	.pt {
		display: flex;
		justify-content: space-between;
		font-size: 24rpx;
		opacity: 0.9;
		margin-bottom: 10rpx;
	}
	.pv { color: #f6d99d; font-weight: 600; }
	.bar {
		height: 12rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.25);
		overflow: hidden;
		i {
			display: block;
			height: 100%;
			border-radius: 999rpx;
			background: linear-gradient(90deg, #f6d99d, #ffe9b8);
			transition: width 0.3s;
		}
	}
	.hint {
		margin-top: 14rpx;
		font-size: 22rpx;
		opacity: 0.9;
		line-height: 1.6;
		.hl { font-weight: 700; color: #f6d99d; }
	}

	/* ---------- 收益总览 ---------- */
	.money {
		display: flex;
		background: #fff;
		border-radius: 28rpx;
		padding: 26rpx 0;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.m { flex: 1; text-align: center; }
	.m .n { font-size: 34rpx; font-weight: 700; color: #111827; }
	.m .n.up { color: #e93323; }
	.m .n.frozen { color: #b45309; }
	.m .l { font-size: 23rpx; color: #8a94a6; margin-top: 6rpx; }
	.m-div { width: 2rpx; background: #f1f3f7; }

	/* ---------- 四宫格 ---------- */
	.grid4 {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 16rpx;
		margin-bottom: 24rpx;
	}
	.g {
		background: #fff;
		border-radius: 26rpx;
		padding: 24rpx 8rpx 20rpx;
		text-align: center;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.g .ic {
		width: 76rpx;
		height: 76rpx;
		margin: 0 auto 14rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		.iconfont { font-size: 36rpx; }
	}
	.ic.b1 { background: #eef3ff; .iconfont { color: #1f5fd6; } }
	.ic.b2 { background: #ecfdf5; .iconfont { color: #047857; } }
	.ic.b3 { background: #fff7e6; .iconfont { color: #b45309; } }
	.ic.b4 { background: #fdf2f8; .iconfont { color: #be185d; } }
	.g .t { font-size: 24rpx; color: #374151; }

	/* ---------- 卡片 ---------- */
	.card {
		background: #fff;
		border-radius: 28rpx;
		padding: 28rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.card-t {
		display: flex;
		align-items: center;
		gap: 14rpx;
		font-size: 29rpx;
		font-weight: 600;
		margin-bottom: 12rpx;
		&::before {
			content: '';
			width: 8rpx;
			height: 28rpx;
			border-radius: 4rpx;
			background: linear-gradient(180deg, #1f5fd6, #3a8df2);
		}
		.more { margin-left: auto; font-size: 24rpx; color: #9ca3af; font-weight: 400; }
	}

	/* ---------- 列表 ---------- */
	.li {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 22rpx 0;
		border-bottom: 2rpx solid #f4f5f8;
		&:last-child { border-bottom: 0; }
	}
	.dot {
		width: 60rpx;
		height: 60rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-weight: 600;
		flex: 0 0 auto;
	}
	.dot.c-blue { background: #eef3ff; color: #1f5fd6; }
	.dot.c-green { background: #ecfdf5; color: #047857; }
	.dot.c-gold { background: #fff7e6; color: #b45309; }
	.li .body { flex: 1; min-width: 0; }
	.li .body .t1 { font-size: 27rpx; font-weight: 500; }
	.li .body .t2 {
		font-size: 23rpx;
		color: #9ca3af;
		margin-top: 4rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.li .right { text-align: right; flex: 0 0 auto; }
	.amt { font-size: 28rpx; font-weight: 600; }
	.amt.up { color: #e93323; }
	.amt.frozen { color: #b45309; }
	.tm { font-size: 22rpx; color: #c3c8d2; margin-top: 4rpx; }
	.empty { color: #9ca3af; text-align: center; padding: 30rpx 0; font-size: 26rpx; }

	/* ---------- 未激活引导 ---------- */
	.guide {
		background: #fffdf5;
		border: 2rpx dashed #f0dbb0;
		border-radius: 28rpx;
		padding: 30rpx;
		margin-bottom: 24rpx;
	}
	.guide-t { font-size: 29rpx; font-weight: 600; margin-bottom: 12rpx; }
	.guide-d { font-size: 25rpx; color: #8a6d3b; line-height: 1.75; }

	/* ---------- 按钮 ---------- */
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
		margin-top: 22rpx;
		box-shadow: 0 12rpx 32rpx rgba(31, 95, 214, 0.28);
	}
	.btn.ghost {
		background: #fff;
		color: #1f5fd6;
		border: 2rpx solid #bcd2ff;
		box-shadow: none;
	}
	.btn.gold {
		background: linear-gradient(135deg, #d9a441, #f0c96a);
		color: #4a3208;
		box-shadow: 0 12rpx 32rpx rgba(217, 164, 65, 0.3);
	}
	.btn-row { display: flex; gap: 20rpx; }
	.btn-row .btn { flex: 1; }

	/* ---------- 通知 ---------- */
	.notice {
		display: flex;
		align-items: center;
		gap: 14rpx;
		background: #fff;
		border-radius: 28rpx;
		padding: 28rpx;
		margin-top: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
		.iconfont { font-size: 34rpx; color: #1f5fd6; }
		.nt { font-size: 28rpx; color: #374151; }
		.badge {
			min-width: 34rpx;
			height: 34rpx;
			line-height: 34rpx;
			padding: 0 10rpx;
			border-radius: 999rpx;
			background: #e93323;
			color: #fff;
			font-size: 21rpx;
			text-align: center;
		}
		.go { margin-left: auto; font-size: 32rpx; color: #c3c8d2; }
	}

	.foot {
		margin-top: 30rpx;
		font-size: 22rpx;
		color: #a8b0bd;
		line-height: 1.7;
		text-align: center;
	}
</style>
