<template>
	<view class="page">
		<view class="card identity">
			<view class="title">当前身份：{{ info.identityName || '游客' }}</view>
			<view class="progress">直推进度 {{ info.effectiveDirectCount || 0 }}/{{ info.bossNeedCount || 2 }}</view>
			<view class="tip" v-if="(info.identity || 0) < 2">距离老板还差 {{ info.bossRemainCount || 0 }} 人</view>
			<view class="money-row">
				<view class="item"><text>今日</text><text class="n">{{ info.todayAmount || 0 }}</text></view>
				<view class="item"><text>累计</text><text class="n">{{ info.totalAmount || 0 }}</text></view>
				<view class="item"><text>冻结</text><text class="n">{{ info.freezeAmount || 0 }}</text></view>
			</view>
		</view>

		<view class="card">
			<view class="title-row">
				<view class="title">我的团队</view>
				<text class="link" @click="toggleExpand">{{ expandAll ? '收起' : '展开全部' }}</text>
			</view>
			<view v-if="!team.children || !team.children.length" class="empty">暂无下级</view>
			<view v-for="(node, idx) in team.children" :key="'n'+idx" class="team-node">
				<view class="line" @click="toggleNode(node)">
					<text class="arrow">{{ node._open || expandAll ? '▼' : '▶' }}</text>
					<text>{{ node.nickname || ('UID'+node.uid) }}</text>
					<text class="tag">{{ node.identityName }}</text>
					<text class="tag help" v-if="node.isHelp">帮扶</text>
				</view>
				<view v-if="node._open || expandAll">
					<view v-for="(c2, j) in (node.children || [])" :key="'c'+j" class="team-node child">
						<view class="line" @click="toggleNode(c2)">
							<text class="arrow" v-if="c2.children && c2.children.length">{{ c2._open || expandAll ? '▼' : '▶' }}</text>
							<text>{{ c2.nickname || ('UID'+c2.uid) }}</text>
							<text class="tag">{{ c2.identityName }}</text>
						</view>
						<view v-if="(c2._open || expandAll) && c2.children && c2.children.length">
							<view v-for="(c3, k) in c2.children" :key="'d'+k" class="team-node child2">
								<text>{{ c3.nickname || ('UID'+c3.uid) }}</text>
								<text class="tag">{{ c3.identityName }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="card">
			<view class="title">收益明细</view>
			<view class="tabs">
				<text :class="['tab', type===''?'on':'']" @click="switchType('')">全部</text>
				<text :class="['tab', type==='direct'?'on':'']" @click="switchType('direct')">直推</text>
				<text :class="['tab', type==='see'?'on':'']" @click="switchType('see')">见单</text>
				<text :class="['tab', type==='peer'?'on':'']" @click="switchType('peer')">平级</text>
				<text :class="['tab', type==='help'?'on':'']" @click="switchType('help')">帮扶</text>
			</view>
			<view v-for="(item, i) in list" :key="i" class="row">
				<view>
					<view>{{ item.rewardType }} · {{ item.amount }}</view>
					<view class="sub">{{ item.mark || item.orderId }}</view>
				</view>
				<view class="sub">{{ item.createTime }}</view>
			</view>
			<view class="empty" v-if="!list.length">暂无记录</view>
		</view>

		<view class="card" v-if="inviteUrl">
			<view class="title">邀请链接</view>
			<view class="sub wrap">{{ inviteUrl }}</view>
			<view class="copy" @click="copyInvite">复制邀请链接</view>
		</view>

		<navigator url="/pages/promoter/chain_notify/index" class="btn ghost">消息通知{{ unread ? '('+unread+')' : '' }}</navigator>
		<navigator url="/pages/promoter/user_spread_code/index" class="btn">去生成海报</navigator>
		<navigator url="/pages/promoter/chain_rules/index" class="btn ghost">规则说明</navigator>
	</view>
</template>

<script>
	import { chainMyIdentity, chainMyTeam, chainCommissionList, chainPromotePoster, chainNotifyUnread } from '@/api/chain.js';
	export default {
		data() {
			return {
				info: {},
				team: { children: [] },
				list: [],
				type: '',
				page: 1,
				expandAll: false,
				inviteUrl: '',
				unread: 0
			};
		},
		onShow() {
			this.loadAll();
		},
		methods: {
			loadAll() {
				chainMyIdentity().then(res => { this.info = res.data || {}; });
				chainMyTeam(3).then(res => { this.team = res.data || { children: [] }; });
				chainPromotePoster().then(res => {
					this.inviteUrl = (res.data && res.data.inviteUrl) || '';
				}).catch(() => {});
				chainNotifyUnread().then(res => {
					this.unread = (res.data && res.data.count) || 0;
				}).catch(() => {});
				this.loadList();
			},
			toggleExpand() {
				this.expandAll = !this.expandAll;
			},
			toggleNode(node) {
				this.$set(node, '_open', !node._open);
			},
			switchType(t) {
				this.type = t;
				this.page = 1;
				this.loadList();
			},
			loadList() {
				chainCommissionList({ type: this.type, page: this.page, limit: 20 }).then(res => {
					this.list = (res.data && res.data.list) || [];
				});
			},
			copyInvite() {
				if (!this.inviteUrl) return;
				// #ifdef H5
				uni.setClipboardData({ data: this.inviteUrl });
				// #endif
				// #ifndef H5
				uni.setClipboardData({ data: this.inviteUrl });
				// #endif
			}
		}
	};
</script>

<style scoped>
	.page { padding: 24rpx; background: #f5f5f5; min-height: 100vh; }
	.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
	.title { font-size: 32rpx; font-weight: 600; margin-bottom: 12rpx; }
	.title-row { display: flex; justify-content: space-between; align-items: center; }
	.link { color: #e74c3c; font-size: 24rpx; }
	.progress, .tip { color: #666; font-size: 26rpx; margin-bottom: 8rpx; }
	.money-row { display: flex; margin-top: 16rpx; }
	.money-row .item { flex: 1; text-align: center; color: #666; font-size: 24rpx; }
	.money-row .n { display: block; color: #222; font-size: 32rpx; margin-top: 6rpx; }
	.team-node { padding: 10rpx 0; border-bottom: 1px solid #f0f0f0; font-size: 26rpx; }
	.team-node.child { padding-left: 30rpx; color: #666; }
	.team-node.child2 { padding-left: 60rpx; color: #999; padding: 8rpx 0 8rpx 60rpx; }
	.arrow { margin-right: 8rpx; font-size: 20rpx; color: #999; }
	.tag { margin-left: 12rpx; font-size: 22rpx; color: #e67e22; }
	.tag.help { color: #27ae60; }
	.tabs { display: flex; flex-wrap: wrap; margin-bottom: 12rpx; }
	.tab { margin-right: 16rpx; margin-bottom: 8rpx; padding: 6rpx 16rpx; background: #f2f2f2; border-radius: 20rpx; font-size: 24rpx; }
	.tab.on { background: #e74c3c; color: #fff; }
	.row { display: flex; justify-content: space-between; padding: 14rpx 0; border-bottom: 1px solid #f5f5f5; font-size: 26rpx; }
	.sub { color: #999; font-size: 22rpx; margin-top: 4rpx; }
	.sub.wrap { word-break: break-all; }
	.copy { margin-top: 12rpx; color: #e74c3c; font-size: 26rpx; }
	.empty { color: #999; text-align: center; padding: 20rpx 0; }
	.btn { display: block; text-align: center; background: #e74c3c; color: #fff; height: 80rpx; line-height: 80rpx; border-radius: 40rpx; margin-top: 12rpx; }
	.btn.ghost { background: #fff; color: #e74c3c; border: 1px solid #e74c3c; }
</style>
