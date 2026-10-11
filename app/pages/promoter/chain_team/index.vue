<template>
	<view :data-theme="theme" class="ct-page">
		<!-- 顶部切换 -->
		<view class="top-tabs">
			<view class="tt" :class="{ on: tab === 'team' }" @click="switchTab('team')">我的团队</view>
			<view class="tt" :class="{ on: tab === 'help' }" @click="switchTab('help')">帮扶基金</view>
		</view>

		<!-- ================= 团队 ================= -->
		<template v-if="tab === 'team'">
			<view class="stat">
				<view class="s">
					<view class="n">{{ info.teamSize || 0 }}</view>
					<view class="l">团队总人数</view>
				</view>
				<view class="s-div"></view>
				<view class="s">
					<view class="n">{{ info.directCount || 0 }}</view>
					<view class="l">直推人数</view>
				</view>
				<view class="s-div"></view>
				<view class="s">
					<view class="n">{{ info.teamBossCount || 0 }}</view>
					<view class="l">已升{{ bossName }}</view>
				</view>
			</view>

			<!-- 留人座位 -->
			<view class="card" v-if="seats.length">
				<view class="card-t">
					我的直推位<text class="more">走人留人</text>
				</view>
				<view class="seat" v-for="(s, i) in seats" :key="s.uid">
					<view class="sn">{{ i + 1 }}</view>
					<view class="sb">
						<view class="s1">
							{{ s.nickname || ('会员ID ' + s.uid) }}
							<text class="tag-keep">留人座位</text>
						</view>
						<view class="s2">
							{{ s.identityName }} · 团队 {{ s.teamOrders || 0 }} 单
						</view>
					</view>
					<view class="sv" :class="s.qualified ? 'ok' : ''">
						{{ s.qualified ? '达标' : (s.upgraded ? '待冲单' : '待升' + bossName) }}
					</view>
				</view>
				<view class="seat" v-for="(d, k) in extraDirects" :key="'d' + k">
					<view class="sn gray">{{ seats.length + k + 1 }}</view>
					<view class="sb">
						<view class="s1">{{ d.nickname || ('会员ID ' + d.uid) }}</view>
						<view class="s2">{{ d.identityName }} · 随本人独立发展</view>
					</view>
					<view class="sv">—</view>
				</view>
				<view class="tip">
					「留人座位」= 本人晋升{{ bossName }}前已产生的直推，仍留在原上级名下继续贡献见点奖；
					第 {{ info.bossNeedCount || 2 }} 名及以后的直推随本人独立发展。
				</view>
			</view>

			<!-- 团队树 -->
			<view class="card">
				<view class="card-t">
					团队结构
					<text class="more" @click="toggleAll">{{ expandAll ? '收起' : '展开全部' }}</text>
				</view>
				<view v-if="!team.children || !team.children.length" class="empty">暂无下级成员</view>
				<view class="tree" v-else>
					<view class="tn" v-for="(n1, i) in team.children" :key="'a' + i">
						<view class="line" @click="toggle(n1)">
							<text class="arw" v-if="hasChild(n1)">{{ isOpen(n1) ? '▾' : '▸' }}</text>
							<text class="arw" v-else></text>
							<view class="av">{{ initial(n1.nickname) }}</view>
							<text class="nm">{{ n1.nickname || ('会员ID ' + n1.uid) }}</text>
							<text class="id">ID {{ n1.uid }}</text>
							<text class="keep" v-if="seatUidSet[n1.uid]">留人</text>
							<text class="lv" :class="lvCls(n1.identity)">{{ n1.identityName }}</text>
						</view>
						<template v-if="isOpen(n1)">
							<view class="tn l2" v-for="(n2, j) in (n1.children || [])" :key="'b' + j">
								<view class="line" @click="toggle(n2)">
									<text class="arw" v-if="hasChild(n2)">{{ isOpen(n2) ? '▾' : '▸' }}</text>
									<text class="arw" v-else></text>
									<view class="av">{{ initial(n2.nickname) }}</view>
									<text class="nm">{{ n2.nickname || ('会员ID ' + n2.uid) }}</text>
									<text class="id">ID {{ n2.uid }}</text>
									<text class="lv" :class="lvCls(n2.identity)">{{ n2.identityName }}</text>
								</view>
								<template v-if="isOpen(n2)">
									<view class="tn l3" v-for="(n3, k) in (n2.children || [])" :key="'c' + k">
										<view class="line">
											<text class="arw"></text>
											<view class="av sm">{{ initial(n3.nickname) }}</view>
											<text class="nm">{{ n3.nickname || ('会员ID ' + n3.uid) }}</text>
											<text class="id">ID {{ n3.uid }}</text>
											<text class="lv" :class="lvCls(n3.identity)">{{ n3.identityName }}</text>
										</view>
									</view>
								</template>
							</view>
						</template>
					</view>
				</view>
			</view>
		</template>

		<!-- ================= 帮扶基金 ================= -->
		<template v-else>
			<view class="help-hero">
				<view class="hh-l">帮扶基金冻结中</view>
				<view class="hh-n">¥{{ money(info.helpFundAmount) }}</view>
				<view class="hh-s">
					{{ info.helpUnlocked ? '已解锁，资金已转入可提现佣金' : '三项条件全部达成后自动解冻，无需申请' }}
				</view>
			</view>

			<view class="card">
				<view class="card-t">
					解锁进度<text class="more">{{ info.helpUnlockedSeatCount || 0 }}/{{ info.helpNeedSeatCount || 2 }}</text>
				</view>

				<view class="hrow">
					<view class="hi" :class="seatsAllBoss ? 'done' : 'wait'">{{ seatsAllBoss ? '✓' : '1' }}</view>
					<view class="hb">
						<view class="h1">留人座位均晋升{{ bossName }}</view>
						<view class="h2">{{ seatsDesc }}</view>
					</view>
					<view class="hv" :class="seatsAllBoss ? 'ok' : ''">{{ bossCount }}/{{ seats.length || (info.helpNeedSeatCount || 2) }}</view>
				</view>

				<view class="hrow">
					<view class="hi" :class="seatsOrdersOk ? 'done' : 'wait'">{{ seatsOrdersOk ? '✓' : '2' }}</view>
					<view class="hb">
						<view class="h1">各自团队有效订单数 ≥ {{ info.helpUnlockNeedOrders || 10 }}</view>
						<view class="h2">{{ seatsOrdersDesc }}</view>
					</view>
					<view class="hv" :class="seatsOrdersOk ? 'ok' : ''">{{ seatsOrdersOk ? '达标' : '未达标' }}</view>
				</view>

				<view class="hrow">
					<view class="hi" :class="selfOk ? 'done' : 'wait'">{{ selfOk ? '✓' : '3' }}</view>
					<view class="hb">
						<view class="h1">本人团队有效订单数 ≥ {{ info.helpUnlockNeedTeamOrders || 20 }}</view>
						<view class="h2">当前 {{ info.selfTeamOrders || 0 }} 单</view>
					</view>
					<view class="hv" :class="selfOk ? 'ok' : ''">{{ info.selfTeamOrders || 0 }}/{{ info.helpUnlockNeedTeamOrders || 20 }}</view>
				</view>

				<view class="done-box" v-if="info.helpUnlocked">
					三项条件全部满足，帮扶基金已自动解冻并入可提现佣金。
				</view>
			</view>

			<!-- 冻结明细 -->
			<view class="card" v-if="funds.length">
				<view class="card-t">冻结明细</view>
				<view class="li" v-for="(f, i) in funds" :key="i">
					<view class="dot">{{ f.rewardType === 'peer_help' ? '平级' : '见点' }}</view>
					<view class="body">
						<view class="t1">{{ f.rewardType === 'peer_help' ? '平级奖冻结' : '见点奖冻结' }}</view>
						<view class="t2">{{ f.mark || ('订单 ' + (f.orderId || '-')) }}</view>
					</view>
					<view class="right">
						<view class="amt frozen">¥{{ money(f.amount) }}</view>
						<view class="tm">{{ shortTime(f.createTime) }}</view>
					</view>
				</view>
			</view>

			<view class="card" v-if="!info.helpUnlocked">
				<view class="card-t">怎么解冻</view>
				<view class="how">
					① 本人晋升{{ bossName }}后，系统会记录升{{ bossName }}前的前 {{ info.helpNeedSeatCount || 2 }} 名直推为「留人座位」<br>
					② 留人座位全部晋升{{ bossName }}<br>
					③ 每个留人座位的团队有效订单 ≥ {{ info.helpUnlockNeedOrders || 10 }} 单，同时本人团队有效订单 ≥ {{ info.helpUnlockNeedTeamOrders || 20 }} 单<br>
					④ 达成后系统自动解冻，转入可提现佣金，无需手动申请
				</view>
			</view>
		</template>

		<view class="foot">奖励为推广佣金／服务奖励，基于真实商品交易产生，不构成投资建议。</view>
	</view>
</template>

<script>
	import { guardModule } from '@/libs/moduleSwitch.js';
	import { toLogin } from '@/libs/login.js';
	import { mapGetters } from 'vuex';
	import { chainMyIdentity, chainMyTeam, chainCommissionList } from '@/api/chain.js';
	let app = getApp();
	export default {
		data() {
			return {
				theme: app.globalData.theme,
				tab: 'team',
				info: {},
				team: { children: [] },
				funds: [],
				expandAll: false,
				agentName: '代理',
				bossName: '老板'
			};
		},
		computed: {
			...mapGetters(['isLogin']),
			seats() {
				return this.info.keptSeats || [];
			},
			bossCount() {
				return this.seats.filter((s) => s.upgraded).length;
			},
			seatsAllBoss() {
				const need = this.info.helpNeedSeatCount || 2;
				return this.seats.length >= need && this.bossCount >= need;
			},
			seatsOrdersOk() {
				const need = this.info.helpNeedSeatCount || 2;
				const n = Number(this.info.helpUnlockNeedOrders || 10);
				return this.seats.length >= need && this.seats.every((s) => Number(s.teamOrders || 0) >= n);
			},
			selfOk() {
				return Number(this.info.selfTeamOrders || 0) >= Number(this.info.helpUnlockNeedTeamOrders || 20);
			},
			seatsDesc() {
				if (!this.seats.length) return '暂无留人座位';
				return this.seats.map((s) => (s.nickname || ('ID ' + s.uid)) + (s.upgraded ? '✓' : '✗')).join(' · ');
			},
			seatsOrdersDesc() {
				if (!this.seats.length) return '-';
				return this.seats.map((s) => (s.nickname || ('ID ' + s.uid)) + ' ' + (s.teamOrders || 0) + ' 单').join(' · ');
			},
			extraDirects() {
				// 超出留人座位数的直推（随本人独立发展）
				const seatUids = this.seats.map((s) => s.uid);
				return (this.team.children || []).filter((c) => seatUids.indexOf(c.uid) === -1);
			},
			seatUidSet() {
				const m = {};
				this.seats.forEach((s) => { m[s.uid] = true; });
				return m;
			}
		},
		onLoad(opt) {
			guardModule('chain');
			this.tab = (opt && opt.tab) === 'help' ? 'help' : 'team';
		},
		onShow() {
			if (!this.isLogin) {
				toLogin();
				return;
			}
			this.loadInfo();
			this.loadTeam();
			this.loadFunds();
		},
		methods: {
			loadInfo() {
				chainMyIdentity().then((res) => {
					this.info = res.data || {};
				}).catch(() => {});
			},
			loadTeam() {
				chainMyTeam(3).then((res) => {
					this.team = res.data || { children: [] };
				}).catch(() => {});
			},
			loadFunds() {
				chainCommissionList({ type: 'help', page: 1, limit: 20 }).then((res) => {
					this.funds = (res.data && res.data.list) || [];
				}).catch(() => {});
			},
			switchTab(t) {
				if (this.tab === t) return;
				this.tab = t;
			},
			toggleAll() {
				this.expandAll = !this.expandAll;
			},
			hasChild(n) {
				return n && n.children && n.children.length;
			},
			isOpen(n) {
				return this.expandAll || (n && n._open);
			},
			toggle(n) {
				if (!this.hasChild(n)) return;
				this.$set(n, '_open', !n._open);
			},
			initial(name) {
				const s = String(name || '会');
				return s.slice(0, 1);
			},
			lvCls(id) {
				if (Number(id || 0) >= 2) return 'lv-boss';
				if (Number(id || 0) >= 1) return 'lv-agent';
				return 'lv-none';
			},
			money(v) {
				return Number(v || 0).toFixed(2);
			},
			shortTime(t) {
				if (!t) return '';
				return String(t).replace('T', ' ').slice(0, 16);
			}
		}
	};
</script>

<style lang="scss" scoped>
	.ct-page {
		min-height: 100vh;
		padding: 24rpx 24rpx 60rpx;
		background: #f5f6fa;
		box-sizing: border-box;
	}

	.top-tabs {
		display: flex;
		background: #fff;
		border-radius: 999rpx;
		padding: 6rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.tt {
		flex: 1;
		text-align: center;
		font-size: 27rpx;
		color: #6b7280;
		padding: 16rpx 0;
		border-radius: 999rpx;
	}
	.tt.on {
		background: linear-gradient(135deg, #1f5fd6, #3a8df2);
		color: #fff;
		font-weight: 600;
	}

	/* ---------- 统计 ---------- */
	.stat {
		display: flex;
		background: #fff;
		border-radius: 28rpx;
		padding: 28rpx 0;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
	}
	.s { flex: 1; text-align: center; }
	.s .n { font-size: 38rpx; font-weight: 700; color: #111827; }
	.s .l { font-size: 23rpx; color: #8a94a6; margin-top: 6rpx; }
	.s-div { width: 2rpx; background: #f1f3f7; }

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
		margin-bottom: 16rpx;
		&::before {
			content: '';
			width: 8rpx;
			height: 28rpx;
			border-radius: 4rpx;
			background: linear-gradient(180deg, #1f5fd6, #3a8df2);
		}
		.more { margin-left: auto; font-size: 24rpx; color: #9ca3af; font-weight: 400; }
	}

	/* ---------- 留人座位 ---------- */
	.seat {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 22rpx 0;
		border-bottom: 2rpx solid #f4f5f8;
		&:last-of-type { border-bottom: 0; }
	}
	.sn {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		background: #fff7e6;
		color: #b45309;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex: 0 0 auto;
	}
	.sn.gray { background: #eef3ff; color: #1f5fd6; }
	.sb { flex: 1; min-width: 0; }
	.s1 { font-size: 27rpx; font-weight: 500; }
	.s2 { font-size: 23rpx; color: #9ca3af; margin-top: 4rpx; }
	.tag-keep {
		margin-left: 10rpx;
		font-size: 20rpx;
		padding: 2rpx 12rpx;
		border-radius: 999rpx;
		background: #fff7e6;
		color: #b45309;
	}
	.sv { font-size: 25rpx; color: #9ca3af; flex: 0 0 auto; }
	.sv.ok { color: #047857; font-weight: 600; }
	.tip {
		margin-top: 16rpx;
		padding: 18rpx 20rpx;
		border-radius: 18rpx;
		background: #fafbfd;
		font-size: 22rpx;
		color: #8a94a6;
		line-height: 1.75;
	}

	/* ---------- 团队树 ---------- */
	.tn { font-size: 26rpx; }
	.line {
		display: flex;
		align-items: center;
		gap: 12rpx;
		padding: 18rpx 0;
		border-bottom: 2rpx solid #f8f9fb;
	}
	.tn.l2 .line { padding-left: 34rpx; }
	.tn.l3 .line { padding-left: 68rpx; }
	.arw { width: 18rpx; font-size: 20rpx; color: #9ca3af; flex: 0 0 auto; }
	.av {
		width: 50rpx;
		height: 50rpx;
		border-radius: 50%;
		background: #eef3ff;
		color: #1f5fd6;
		font-size: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex: 0 0 auto;
	}
	.av.sm { width: 42rpx; height: 42rpx; font-size: 21rpx; }
	.nm { font-size: 27rpx; }
	.id { font-size: 21rpx; color: #b6bcc7; }
	.keep {
		font-size: 20rpx;
		padding: 2rpx 12rpx;
		border-radius: 999rpx;
		background: #fff7e6;
		color: #b45309;
	}
	.lv {
		margin-left: auto;
		font-size: 21rpx;
		padding: 2rpx 14rpx;
		border-radius: 999rpx;
		flex: 0 0 auto;
	}
	.lv-boss { background: #eef3ff; color: #1f5fd6; }
	.lv-agent { background: #ecfdf5; color: #047857; }
	.lv-none { background: #f3f4f6; color: #6b7280; }

	/* ---------- 帮扶基金 ---------- */
	.help-hero {
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
			width: 340rpx;
			height: 340rpx;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.11);
			right: -120rpx;
			top: -150rpx;
		}
	}
	.hh-l { font-size: 25rpx; opacity: 0.85; position: relative; z-index: 1; }
	.hh-n {
		font-size: 64rpx;
		font-weight: 700;
		margin-top: 8rpx;
		position: relative;
		z-index: 1;
	}
	.hh-s {
		margin-top: 16rpx;
		font-size: 23rpx;
		opacity: 0.86;
		line-height: 1.6;
		position: relative;
		z-index: 1;
	}

	.hrow {
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 24rpx 0;
		border-bottom: 2rpx solid #f4f5f8;
		&:last-of-type { border-bottom: 0; }
	}
	.hi {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 600;
		flex: 0 0 auto;
	}
	.hi.done { background: #ecfdf5; color: #047857; }
	.hi.wait { background: #f3f4f6; color: #9ca3af; }
	.hb { flex: 1; min-width: 0; }
	.hb .h1 { font-size: 27rpx; }
	.hb .h2 { font-size: 22rpx; color: #9ca3af; margin-top: 4rpx; }
	.hv { font-size: 24rpx; color: #6b7280; flex: 0 0 auto; }
	.hv.ok { color: #047857; font-weight: 600; }

	.done-box {
		margin-top: 20rpx;
		padding: 20rpx 22rpx;
		border-radius: 20rpx;
		background: #ecfdf5;
		color: #047857;
		font-size: 24rpx;
		line-height: 1.7;
	}
	.how {
		font-size: 24rpx;
		color: #4b5563;
		line-height: 2;
	}

	/* ---------- 冻结明细 ---------- */
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
		background: #fff7e6;
		color: #b45309;
		font-size: 22rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex: 0 0 auto;
	}
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
	.amt.frozen { color: #b45309; font-size: 28rpx; font-weight: 600; }
	.tm { font-size: 22rpx; color: #c3c8d2; margin-top: 4rpx; }

	.empty { color: #9ca3af; text-align: center; padding: 60rpx 0; font-size: 26rpx; }
	.foot {
		margin-top: 24rpx;
		font-size: 22rpx;
		color: #a8b0bd;
		line-height: 1.7;
		text-align: center;
	}
</style>
