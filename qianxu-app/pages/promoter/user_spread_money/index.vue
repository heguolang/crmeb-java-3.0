<template>
	<view :data-theme="theme">
		<view class='commission-details'>
			<view class='promoterHeader'>
				<view class='headerCon acea-row row-between-wrapper'>
					<view>
						<view class='name'>{{name}}</view>
						<view class='money' v-if="recordType == 4">￥<text class='num'>{{extractCount}}</text></view>
						<view class='money' v-else>￥<text class='num'>{{commissionCount}}</text></view>
					</view>
					<view class='iconfont icon-jinbi1'></view>
				</view>
			</view>
			<view class='sign-record' v-if="recordType == 4">
				<block v-for="(item,index) in recordList" :key="index" v-if="recordList.length>0">
					<view class='list pad30'>
						<view class='item'>
							<view class='data'>{{item.date}}</view>
							<view class='listn borRadius14'>
								<block v-for="(child,indexn) in item.list" :key="indexn">
									<view class='itemn acea-row row-between-wrapper'>
										<view>
											<view class='name line1'>{{child.status | statusFilter}}</view>
											<view>{{child.createTime}}</view>
										</view>
										<view class='num font_color' v-if="child.status == -1">+{{child.extractPrice}}
										</view>
										<view class='num' v-else>-{{child.extractPrice}}</view>
									</view>
								</block>
							</view>
						</view>
					</view>
				</block>
				<view v-if="recordList.length == 0">
					<emptyPage title='暂无提现记录~'></emptyPage>
				</view>
			</view>
			<view class='sign-record' v-else>
				<block v-for="(item,index) in recordList" :key="index" v-if="recordList.length>0">
					<view class='list pad30'>
						<view class='item'>
							<view class='data'>{{item.date}}</view>
							<view class='listn borRadius14'>
								<block v-for="(child,indexn) in item.list" :key="indexn">
									<view class='itemn acea-row row-between-wrapper'>
										<view class="record-main">
											<view class='name line1 acea-row row-middle'>
												<text
													v-if="getBrokerageLevelLabel(child.brokerageLevel)"
													class="brokerage-tag"
													:class="getBrokerageLevelClass(child.brokerageLevel)"
												>{{ getBrokerageLevelLabel(child.brokerageLevel) }}</text>
												<text class="record-title">{{ formatBrokerageDisplayText(child.title) }}</text>
											</view>
											<view class="record-mark" v-if="recordType == 5 && child.mark">{{ formatBrokerageDisplayText(child.mark) }}</view>
											<view class="record-time">
												{{ child.updateTime }}
												<text v-if="recordType == 5 && getStatusLabel(child.status)" class="status-text"> · {{ getStatusLabel(child.status) }}</text>
											</view>
										</view>
										<view class='num font_color' v-if="child.type == 1">+{{child.price}}
										</view>
										<view class='num' v-else>-{{child.price}}</view>
									</view>
								</block>
							</view>
						</view>
					</view>
				</block>
				<view v-if="recordList.length == 0">
					<emptyPage :title="recordType == 5 ? '暂无社群奖记录~' : '暂无佣金记录~'"></emptyPage>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		getCommissionInfo,
		getRecordApi,
		getTeamCommissionInfo,
		getTeamCommissionTotal,
	} from '@/api/user.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from "vuex";
	import emptyPage from '@/components/emptyPage.vue'
	import {setThemeColor} from '@/utils/setTheme.js'
	import { getBrokerageLevelLabel, getBrokerageLevelClass, formatBrokerageDisplayText } from '@/utils/brokerage.js'
	const app = getApp();
	export default {
		components: {
			emptyPage
		},
		filters: {
			statusFilter(status) {
				const statusMap = {
					'-1': '未通过',
					'0': '审核中',
					'1': '已提现'
				}
				return statusMap[status]
			}
		},
		data() {
			return {
				name: '',
				type: 0,
				page: 1,
				limit: 10,
				recordList: [],
				recordType: 0,
				statuss: false,
				extractCount: 0,
				theme:app.globalData.theme,
				commissionCount:0,
				bgColor:'#e93323'
			};
		},
		computed: mapGetters(['isLogin']),
		onLoad(options) {
			if (this.isLogin) {
				this.type = options.type;
				this.extractCount = options.extractCount;
				this.commissionCount = options.commissionCount || 0;
			} else {
				toLogin();
			}
			let that = this;
			that.bgColor = setThemeColor();
			uni.setNavigationBarColor({
				frontColor: '#ffffff',
				backgroundColor:that.bgColor,
			});
		},
		onShow: function() {
			let type = this.type;
			this.page = 1;
			this.statuss = false;
			this.recordList = [];
			if (type == 1) {
				uni.setNavigationBarTitle({
					title: "提现记录"
				});
				this.name = '提现总额';
				this.recordType = 4;
				this.getList();
			} else if (type == 2) {
				uni.setNavigationBarTitle({
					title: "佣金记录"
				});
				this.name = '佣金明细';
				this.recordType = 3;
				this.getRecordList();
			} else if (type == 3) {
				uni.setNavigationBarTitle({
					title: "社群奖记录"
				});
				this.name = '社群奖累计';
				this.recordType = 5;
				this.loadTeamTotal();
				this.getTeamRecordList();
			} else {
				uni.showToast({
					title: '参数错误',
					icon: 'none',
					duration: 1000,
					mask: true,
					success: function(res) {
						setTimeout(function() {
							// #ifndef H5
							uni.navigateBack({
								delta: 1,
							});
							// #endif
							// #ifdef H5
							history.back();
							// #endif

						}, 1200)
					},
				});
			}

		},
		methods: {
			getBrokerageLevelLabel,
			getBrokerageLevelClass,
			formatBrokerageDisplayText,
			getStatusLabel(status) {
				const map = {
					1: '待入账',
					2: '冻结中',
					3: '已到账',
					4: '已失效'
				};
				return map[status] || '';
			},
			loadTeamTotal() {
				getTeamCommissionTotal().then(res => {
					this.commissionCount = (res.data && res.data.count) != null ? res.data.count : 0;
				}).catch(() => {});
			},
			getList: function() {
				let that = this;
				let recordList = that.recordList;
				let recordListNew = [];
				if (that.statuss == true) return;
				getRecordApi({
					page: that.page,
					limit: that.limit
				}).then(res => {
					let len = res.data.list ? res.data.list.length : 0;
					let recordListData = res.data.list || [];
					recordListNew = recordList.concat(recordListData);
					that.statuss = that.limit > len;
					that.page = that.page + 1;
					that.$set(that, 'recordList', recordListNew);
				});
			},
			getRecordList: function() {
				let that = this;
				let page = that.page;
				let limit = that.limit;
				let statuss = that.statuss;
				let recordList = that.recordList;
				let recordListNew = [];
				if (statuss == true) return;
				getCommissionInfo({
					page: page,
					limit: limit
				}).then(res => {
					if (res.data.list) {
						let len = res.data.list ? res.data.list.length : 0;
						let recordListData = res.data.list || [];
						recordListNew = recordList.concat(recordListData);
						that.statuss = limit > len;
						that.page = page + 1;
						that.$set(that, 'recordList', recordListNew);
					}
				});
			},
			getTeamRecordList: function() {
				let that = this;
				if (that.statuss == true) return;
				getTeamCommissionInfo({
					page: that.page,
					limit: that.limit
				}).then(res => {
					if (res.data && res.data.list) {
						let len = res.data.list.length;
						that.recordList = that.recordList.concat(res.data.list);
						that.statuss = that.limit > len;
						that.page = that.page + 1;
					} else {
						that.statuss = true;
					}
				});
			}
		},
		onReachBottom: function() {
			if (this.recordType == 5) {
				this.getTeamRecordList();
			} else if (this.recordType == 3) {
				this.getRecordList();
			} else if (this.recordType == 4) {
				this.getList();
			}
		}
	}
</script>

<style scoped lang="scss">
	.commission-details .promoterHeader .headerCon .money {
		font-size: 36rpx;
	}
	.promoterHeader{
		@include main_bg_color(theme);
	}
	.commission-details .promoterHeader .headerCon .money .num {
		font-family: 'Guildford Pro';
	}
	.font_color{
		color: #E93323 !important;
	}
	.record-main {
		flex: 1;
		min-width: 0;
		padding-right: 20rpx;
	}
	.name {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
	}
	.record-title {
		flex: 1;
		min-width: 0;
	}
	.record-mark {
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #999;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.record-time {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #999;
	}
	.status-text {
		color: #666;
	}
	.brokerage-tag {
		display: inline-block;
		margin-right: 12rpx;
		padding: 2rpx 12rpx;
		border-radius: 6rpx;
		font-size: 20rpx;
		line-height: 28rpx;
		color: #fff;
		flex-shrink: 0;
	}
	.tag-self { background: #909399; }
	.tag-one { background: #409eff; }
	.tag-two { background: #67c23a; }
	.tag-team-diff { background: #e6a23c; }
	.tag-team-peer { background: #f56c6c; }
</style>
