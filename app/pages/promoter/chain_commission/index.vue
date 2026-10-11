<template>
	<view :data-theme="theme" class="cm-page">
		<!-- 汇总 -->
		<view class="sum">
			<view class="s">
				<view class="l">累计收益</view>
				<view class="n">¥{{ money(totals.total) }}</view>
			</view>
			<view class="s-div"></view>
			<view class="s">
				<view class="l">待解冻（帮扶基金）</view>
				<view class="n gold">¥{{ money(totals.helpFund) }}</view>
			</view>
		</view>

		<!-- 类型筛选 -->
		<view class="tabs">
			<view class="tb" :class="{ on: type === '' }" @click="switchType('')">全部</view>
			<view class="tb" :class="{ on: type === 'direct' }" @click="switchType('direct')">直推奖</view>
			<view class="tb" :class="{ on: type === 'see' }" @click="switchType('see')">见点奖</view>
			<view class="tb" :class="{ on: type === 'peer' }" @click="switchType('peer')">平级奖</view>
			<view class="tb" :class="{ on: type === 'help' }" @click="switchType('help')">帮扶基金</view>
		</view>

		<!-- 列表 -->
		<view class="card" v-if="list.length">
			<view class="li" v-for="(item, i) in list" :key="i">
				<view class="dot" :class="typeCls(item.rewardType)">{{ typeShort(item.rewardType) }}</view>
				<view class="body">
					<view class="t1">
						{{ typeName(item.rewardType) }}
						<text class="st" v-if="frozen(item)">冻结中</text>
					</view>
					<view class="t2">{{ item.mark || ('订单 ' + (item.orderId || '-')) }}</view>
				</view>
				<view class="right">
					<view class="amt" :class="frozen(item) ? 'frozen' : 'up'">{{ amountText(item) }}</view>
					<view class="tm">{{ shortTime(item.createTime) }}</view>
				</view>
			</view>
		</view>

		<view class="empty" v-if="!list.length">暂无记录</view>
		<view class="more" v-if="loadend && list.length">— 没有更多了 —</view>
		<view class="more" v-else-if="loading">加载中…</view>

		<view class="foot">
			帮扶基金为见点奖／平级奖按比例冻结的部分，解锁后自动转入可提现佣金。
		</view>
	</view>
</template>

<script>
	import { guardModule } from '@/libs/moduleSwitch.js';
	import { toLogin } from '@/libs/login.js';
	import { mapGetters } from 'vuex';
	import { chainCommissionList, chainCommissionSummary } from '@/api/chain.js';
	let app = getApp();
	export default {
		data() {
			return {
				theme: app.globalData.theme,
				type: '',
				list: [],
				totals: { total: 0, helpFund: 0 },
				page: 1,
				limit: 20,
				loading: false,
				loadend: false
			};
		},
		computed: {
			...mapGetters(['isLogin'])
		},
		onLoad(opt) {
			guardModule('chain');
			this.type = (opt && opt.type) || '';
		},
		onShow() {
			if (!this.isLogin) {
				toLogin();
				return;
			}
			this.loadSummary();
			this.loadList(true);
		},
		onReachBottom() {
			this.loadList(false);
		},
		methods: {
			loadSummary() {
				chainCommissionSummary().then((res) => {
					const d = res.data || {};
					this.totals = {
						total: d.total || 0,
						helpFund: d.helpFund || 0
					};
				}).catch(() => {});
			},
			switchType(t) {
				if (this.type === t) return;
				this.type = t;
				this.list = [];
				this.page = 1;
				this.loadend = false;
				this.loadList(true);
			},
			loadList(reset) {
				if (reset) {
					this.page = 1;
					this.loadend = false;
				}
				if (this.loadend || this.loading) return;
				this.loading = true;
				chainCommissionList({ type: this.type, page: this.page, limit: this.limit }).then((res) => {
					const rows = (res.data && res.data.list) || [];
					this.list = reset ? rows : this.list.concat(rows);
					if (rows.length < this.limit) this.loadend = true;
					else this.page += 1;
					this.loading = false;
				}).catch(() => {
					this.loadend = true;
					this.loading = false;
				});
			},
			money(v) {
				return Number(v || 0).toFixed(2);
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
			frozen(item) {
				return Number(item.status || 0) === 5;
			},
			amountText(item) {
				const n = Number(item.amount || 0);
				return (n >= 0 ? '+' : '') + n.toFixed(2);
			},
			shortTime(t) {
				if (!t) return '';
				return String(t).replace('T', ' ').slice(0, 16);
			}
		}
	};
</script>

<style lang="scss" scoped>
	.cm-page {
		min-height: 100vh;
		padding: 24rpx 24rpx 60rpx;
		background: #f5f6fa;
		box-sizing: border-box;
	}

	.sum {
		display: flex;
		background: #fff;
		border-radius: 28rpx;
		padding: 28rpx 0;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.s { flex: 1; text-align: center; }
	.s .l { font-size: 23rpx; color: #8a94a6; }
	.s .n { font-size: 38rpx; font-weight: 700; margin-top: 8rpx; color: #111827; }
	.s .n.gold { color: #b45309; }
	.s-div { width: 2rpx; background: #f1f3f7; }

	.tabs {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
		margin-bottom: 24rpx;
	}
	.tb {
		font-size: 24rpx;
		padding: 10rpx 26rpx;
		border-radius: 999rpx;
		background: #f1f3f7;
		color: #6b7280;
	}
	.tb.on {
		background: linear-gradient(135deg, #1f5fd6, #3a8df2);
		color: #fff;
		font-weight: 500;
	}

	.card {
		background: #fff;
		border-radius: 28rpx;
		padding: 8rpx 28rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.li {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 26rpx 0;
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
	.st {
		margin-left: 10rpx;
		font-size: 20rpx;
		padding: 2rpx 12rpx;
		border-radius: 999rpx;
		background: #fff7e6;
		color: #b45309;
	}
	.li .body .t2 {
		font-size: 23rpx;
		color: #9ca3af;
		margin-top: 6rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.li .right { text-align: right; flex: 0 0 auto; }
	.amt { font-size: 30rpx; font-weight: 600; }
	.amt.up { color: #e93323; }
	.amt.frozen { color: #b45309; }
	.tm { font-size: 22rpx; color: #c3c8d2; margin-top: 6rpx; }

	.empty { color: #9ca3af; text-align: center; padding: 120rpx 0; font-size: 27rpx; }
	.more { text-align: center; font-size: 23rpx; color: #c3c8d2; padding: 26rpx 0; }
	.foot {
		margin-top: 24rpx;
		font-size: 22rpx;
		color: #a8b0bd;
		line-height: 1.7;
		text-align: center;
	}
</style>
