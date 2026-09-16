<template>
  <view class="order-page">
    <view class="tabs">
      <view v-for="t in tabs" :key="t.value" class="tab-item" :class="{ active: status === t.value }" @click="switchTab(t.value)">
        {{ t.label }}
      </view>
    </view>

    <view v-if="tabType === 'mine'">
      <view v-for="o in list" :key="o.id" class="order-card" @click="toggleDetail(o)">
        <view class="row-1">
          <text class="order-no">{{ o.orderNo }}</text>
          <text class="order-status" :class="'st' + o.status">{{ statusText(o.status) }}</text>
        </view>
        <view v-for="p in o.productList" :key="p.id" class="row-p">
          <image :src="p.image" class="p-img" mode="aspectFill" />
          <view class="p-info">
            <view class="p-name">{{ p.productName }}</view>
            <view class="p-num">¥{{ p.price }} × {{ p.num }}</view>
          </view>
          <view class="p-sum">¥{{ p.totalPrice }}</view>
        </view>
        <view class="row-total">
          共 {{ o.totalNum }} 件 · 合计 <text class="total-price">¥{{ o.totalPrice }}</text>
        </view>
        <view v-if="o.status === -1" class="reject">驳回原因：{{ o.rejectReason }}</view>
        <view v-if="o.expressNum" class="express">快递：{{ o.expressName }} {{ o.expressNum }}</view>
        <view class="row-op" v-if="o.status === 3">
          <button class="op-btn primary" size="mini" @click.stop="receive(o)">确认收货</button>
        </view>
        <view class="row-op" v-if="o.status === 4">
          <button class="op-btn" size="mini" @click.stop="applyExchange(o)">申请换货</button>
        </view>
      </view>
    </view>

    <view v-if="tabType === 'audit'">
      <view v-for="o in list" :key="o.id" class="order-card">
        <view class="row-1">
          <text class="order-no">{{ o.orderNo }}</text>
          <text class="order-status" :class="'st' + o.status">{{ statusText(o.status) }}</text>
        </view>
        <view class="audit-user">下级：{{ o.nickname }}（{{ o.levelName }}）</view>
        <view v-for="p in o.productList" :key="p.id" class="row-p">
          <image :src="p.image" class="p-img" mode="aspectFill" />
          <view class="p-info">
            <view class="p-name">{{ p.productName }}</view>
            <view class="p-num">我的拿价 ¥{{ p.parentPrice }} × {{ p.num }}</view>
          </view>
          <view class="p-sum">¥{{ (p.parentPrice * p.num).toFixed(2) }}</view>
        </view>
        <view class="row-total">合计 <text class="total-price">¥{{ o.totalPrice }}</text></view>
        <view class="row-op" v-if="o.status === 0">
          <button class="op-btn primary" size="mini" @click="audit(o, 1)">通过</button>
          <button class="op-btn danger" size="mini" @click="audit(o, -1)">驳回</button>
        </view>
      </view>
    </view>

    <view v-if="!list.length && loaded" class="empty">暂无订单</view>
  </view>
</template>

<script>
	import { getMyStockOrders, getAuditOrders, auditStockOrder, receiveStockOrder } from '@/api/stock.js';
	export default {
		data() {
			return {
				tabType: 'mine',
				status: null,
				list: [],
				loaded: false,
				tabs: [
					{ value: null, label: '全部' },
					{ value: 0, label: '待审核' },
					{ value: 1, label: '待付款' },
					{ value: 2, label: '待发货' },
					{ value: 3, label: '待收货' },
					{ value: 4, label: '已完成' },
					{ value: 'audit', label: '待我审核' }
				]
			};
		},
		onLoad(opt) {
			if (opt.tab === 'audit') {
				this.tabType = 'audit';
				this.status = 'audit';
			}
			this.load();
		},
		methods: {
			statusText(s) {
				return { 0: '待上级审核', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', '-1': '已驳回' }[s] || s;
			},
			switchTab(v) {
				this.status = v;
				if (v === 'audit') this.tabType = 'audit'; else this.tabType = 'mine';
				this.load();
			},
			load() {
				this.loaded = false;
				const params = { page: 1, limit: 30 };
				if (this.tabType === 'mine' && this.status !== null && this.status !== 'audit') params.status = this.status;
				const api = this.tabType === 'audit' ? getAuditOrders : getMyStockOrders;
				if (this.tabType === 'audit') params.status = 0;
				api(params).then(res => {
					this.list = res.data.list || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			toggleDetail() {},
			receive(o) {
				uni.showModal({
					title: '确认收货',
					content: '确认已收到该批订货？完成后将自动结算奖励。',
					success: (m) => {
						if (!m.confirm) return;
						receiveStockOrder(o.id).then(() => {
							uni.showToast({ title: '已确认收货', icon: 'success' });
							this.load();
						});
					}
				});
			},
			audit(o, result) {
				if (result === -1) {
					uni.showModal({
						title: '驳回订单',
						editable: true,
						placeholderText: '请填写驳回原因',
						success: (m) => {
							if (!m.confirm) return;
							auditStockOrder(o.id, { status: -1, reason: m.content || '' }).then(() => {
								uni.showToast({ title: '已驳回', icon: 'success' });
								this.load();
							});
						}
					});
				} else {
					uni.showModal({
						title: '通过审核',
						content: '通过后订单流转总部云仓扣库存，请确认库存充足。',
						success: (m) => {
							if (!m.confirm) return;
							auditStockOrder(o.id, { status: 1 }).then(() => {
								uni.showToast({ title: '已通过', icon: 'success' });
								this.load();
							});
						}
					});
				}
			},
			applyExchange(o) {
				uni.navigateTo({ url: '/pages/users/stock/exchange?orderId=' + o.id });
			}
		}
	};
</script>

<style lang="scss" scoped>
.order-page { min-height: 100vh; background: #f5f6f8; padding-bottom: 40rpx; }
.tabs { display: flex; background: #fff; padding: 0 10rpx; position: sticky; top: 0; z-index: 9; overflow-x: auto; }
.tab-item { padding: 24rpx 20rpx; font-size: 26rpx; color: #666; white-space: nowrap; }
.tab-item.active { color: #2b6fe3; font-weight: 600; border-bottom: 4rpx solid #2b6fe3; }
.order-card { background: #fff; border-radius: 16rpx; margin: 20rpx 24rpx; padding: 24rpx; }
.row-1 { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.order-no { font-size: 26rpx; color: #333; font-weight: 600; }
.order-status { font-size: 24rpx; }
.st0 { color: #ff9a3c; } .st1 { color: #ff9a3c; } .st2 { color: #2b6fe3; } .st3 { color: #2b6fe3; }
.st4 { color: #5cc45c; } .st-1 { color: #f56c6c; }
.audit-user { font-size: 24rpx; color: #666; margin-bottom: 12rpx; }
.row-p { display: flex; align-items: center; margin-bottom: 14rpx; }
.p-img { width: 80rpx; height: 80rpx; border-radius: 8rpx; flex-shrink: 0; }
.p-info { flex: 1; margin: 0 16rpx; overflow: hidden; }
.p-name { font-size: 26rpx; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.p-num { font-size: 22rpx; color: #999; margin-top: 4rpx; }
.p-sum { font-size: 26rpx; color: #333; }
.row-total { text-align: right; font-size: 24rpx; color: #666; margin-top: 8rpx; }
.total-price { color: #e93323; font-size: 30rpx; font-weight: 600; }
.reject { color: #f56c6c; font-size: 24rpx; margin-top: 8rpx; }
.express { color: #2b6fe3; font-size: 24rpx; margin-top: 8rpx; }
.row-op { display: flex; justify-content: flex-end; margin-top: 16rpx; gap: 16rpx; }
.op-btn { border-radius: 30rpx; font-size: 24rpx; background: #f2f3f5; color: #666; }
.op-btn.primary { background: #2b6fe3; color: #fff; }
.op-btn.danger { background: #f56c6c; color: #fff; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
