<template>
  <view class="order-page">
    <!-- 渐变头 -->
    <view class="head-card">
      <view class="badge">{{ tabType === 'audit' ? '订单审核' : '订货管理' }}</view>
      <view class="head-title">{{ tabType === 'audit' ? '下级订单审核' : '我的订货订单' }}</view>
      <view class="head-sub">{{ tabType === 'audit' ? '审核通过后订单流转总部云仓扣库存' : '下级订货 · 审核结算 · 物流跟踪一站管理' }}</view>
    </view>

    <!-- 状态筛选 -->
    <view class="tabs-wrap">
      <view class="tabs">
        <view v-for="t in tabs" :key="t.value" class="tab-item" :class="{ active: status === t.value }" @click="switchTab(t.value)">
          {{ t.label }}
        </view>
      </view>
    </view>

    <!-- 我的订货订单 -->
    <view v-if="tabType === 'mine'" class="card-list">
      <view v-for="o in list" :key="o.id" class="order-card" @click="toggleDetail(o)">
        <view class="row-1">
          <text class="order-no">{{ o.orderNo }}</text>
          <view class="pill-group">
            <text class="tp-pill" :class="o.stockType === 2 ? 'is-virtual' : 'is-physical'">{{ stockTypeText(o.stockType) }}</text>
            <text v-if="o.orderType === 2" class="tp-pill is-pickup">提货</text>
            <text v-if="o.orderType === 3" class="tp-pill is-exchange">换货</text>
            <text v-if="o.exchanged === 1" class="st-pill ex-pill">已换货</text>
            <text class="st-pill" :class="'st' + o.status">{{ statusText(o.status) }}</text>
          </view>
        </view>
        <view class="row-time">
          <text class="rt-item">下单 {{ shortTime(o.createTime) }}</text>
          <text v-if="o.status === 10 && o.waitDurationText" class="rt-item rt-wait">{{ o.waitDurationText }}</text>
        </view>
        <view v-for="p in o.productList" :key="p.id" class="row-p">
          <image :src="p.image" class="p-img" mode="aspectFill" />
          <view class="p-info">
            <view class="p-name">{{ p.productName }}</view>
            <view class="p-num">¥{{ p.price }} <text class="p-x">× {{ p.num }}</text></view>
          </view>
          <view class="p-sum">¥{{ p.totalPrice }}</view>
        </view>
        <view class="row-total">
          共 {{ o.totalNum }} 件 · 合计 <text class="total-price">¥{{ o.totalPrice }}</text>
        </view>
        <view v-if="o.status === -1" class="reject-box">驳回原因：{{ o.rejectReason }}</view>
        <view v-if="o.status === 10" class="reject-box wait-box">
          下单时上级暂无库存，正在等待上级补货；上级补货后订单将自动进入正常订货流程，超时未补货将自动匹配给有货的更高级上级
        </view>
        <view v-if="o.exchanged === 1" class="reject-box">
          该订单已换货（换货单号 {{ o.exchangeNo }}），不可重复申请换货
        </view>
        <view v-if="o.expressNum" class="express-box">
          <text class="ex-tag">快递</text>{{ o.expressName }} {{ o.expressNum }}
        </view>
        <view class="row-op" v-if="o.status === 1">
          <button class="op-btn danger" size="mini" @click.stop="cancelOrder(o)">取消订单</button>
          <button class="op-btn primary" size="mini" @click.stop="payOrder(o)">去支付</button>
        </view>
        <view class="row-op" v-if="o.status === 3">
          <button class="op-btn primary" size="mini" @click.stop="receive(o)">确认收货</button>
        </view>
        <view class="row-op" v-if="o.status === 4 && o.exchanged !== 1">
          <button class="op-btn ghost" size="mini" @click.stop="applyExchange(o)">申请换货</button>
        </view>
      </view>
    </view>

    <!-- 待我审核 -->
    <view v-if="tabType === 'audit'" class="card-list">
      <!-- 下级提交的换货申请：换货单在独立表里，必须单独带出来，否则上级永远看不到 -->
      <view v-for="e in exList" :key="'ex' + e.id" class="order-card ex-card">
        <view class="row-1">
          <text class="order-no">{{ e.exchangeNo }}</text>
          <view class="pill-group">
            <text class="tp-pill is-exchange">换货</text>
            <text class="st-pill st0">待我审核</text>
          </view>
        </view>
        <view class="audit-user">
          <view class="au-avatar">{{ (e.nickname || '下').slice(0, 1) }}</view>
          <view class="au-info">
            <text class="au-name">{{ e.nickname }}</text>
            <text class="au-level">下级实体换货申请</text>
          </view>
          <view class="au-tip">等待您审核</view>
        </view>
        <view class="row-p">
          <image :src="e.productImage" class="p-img" mode="aspectFill" />
          <view class="p-info">
            <view class="p-name">{{ e.productName }}</view>
            <view class="p-num">换货数量 × {{ e.num }} <text class="p-x">原单 {{ e.orderNo }}</text></view>
          </view>
        </view>
        <view class="ex-target">
          换入：{{ e.targetProductName }}
          <text v-if="Number(e.diffPrice) > 0" class="diff-amt">需补差价 ¥{{ e.diffPrice }}</text>
          <text v-else class="same-amt">无需补差价</text>
        </view>
        <view class="ex-reason">换货原因：{{ e.reason }}</view>
        <view class="row-op">
          <button class="op-btn danger" size="mini" @click="auditEx(e, -1)">驳回</button>
          <button class="op-btn primary" size="mini" @click="auditEx(e, 1)">通过</button>
        </view>
      </view>

      <view v-for="o in list" :key="o.id" class="order-card">
        <view class="row-1">
          <text class="order-no">{{ o.orderNo }}</text>
          <text class="st-pill" :class="o.status === 10 ? 'stwait' : 'st0'">{{ auditStatusText(o.status) }}</text>
        </view>
        <view class="audit-user">
          <view class="au-avatar">{{ (o.nickname || '下') }}</view>
          <view class="au-info">
            <text class="au-name">{{ o.nickname }}</text>
            <text class="au-level">{{ o.levelName }}</text>
          </view>
          <view class="au-tip">{{ o.status === 10 ? '待补货后审核' : '等待您审核' }}</view>
        </view>
        <view v-for="p in o.productList" :key="p.id" class="row-p">
          <image :src="p.image" class="p-img" mode="aspectFill" />
          <view class="p-info">
            <view class="p-name">{{ p.productName }}</view>
            <view class="p-num">我的拿价 ¥{{ p.parentPrice }} <text class="p-x">× {{ p.num }}</text></view>
          </view>
          <view class="p-sum">¥{{ (p.parentPrice * p.num).toFixed(2) }}</view>
        </view>
        <view class="row-total">合计 <text class="total-price">¥{{ o.totalPrice }}</text></view>
        <view v-if="o.status === 10" class="reject-box wait-box">
          该订单下单时您的库存不足，请尽快补货；补货后即可审核通过。超时未补货，系统将自动向上匹配有货的上级。
          <text v-if="o.waitDurationText">（{{ o.waitDurationText }}）</text>
        </view>
        <view class="row-op" v-if="o.status === 0 || o.status === 10">
          <button class="op-btn danger" size="mini" @click="audit(o, -1)">驳回</button>
          <button class="op-btn primary" size="mini" @click="audit(o, 1)">通过</button>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <view v-if="showEmpty" class="empty-box">
      <view class="empty-icon">
        <view class="e-box">
          <view class="e-lid"></view>
          <view class="e-face"><text class="e-check">✓</text></view>
        </view>
      </view>
      <view class="empty-title">暂无订单</view>
      <view class="empty-sub">当前筛选条件下没有订单记录</view>
    </view>

    <view class="bottom-tip">— 订货订单 —</view>
  </view>
</template>

<script>
	import { getMyStockOrders, getAuditOrders, auditStockOrder, receiveStockOrder, payStockOrder, cancelStockOrder, getExchangeAuditList, auditStockExchange } from '@/api/stock.js';
	export default {
		data() {
			return {
				tabType: 'mine',
				status: null,
				list: [],
				exList: [],
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
		computed: {
			// 待我审核：订单和换货单是两个来源，都空才算空
			showEmpty() {
				if (!this.loaded) return false;
				if (this.tabType === 'audit') return !this.list.length && !this.exList.length;
				return !this.list.length;
			}
		},
		onLoad(opt) {
			if (opt.tab === 'audit') {
				this.tabType = 'audit';
				this.status = 'audit';
			} else if (opt.tab === 'waitPay') {
				this.status = 1;
			}
			this.load();
		},
		methods: {
			statusText(s) {
				return { 0: '待上级审核', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', '-1': '已驳回', 10: '匹配上级中', '-2': '已取消' }[s] || s;
			},
			// 待我审核 tab：状态10 表示下单时我方无库存，需补货后才能审核通过
			auditStatusText(s) {
				if (s === 10) return '待我补货后审核';
				return this.statusText(s);
			},
			// 库存类型：1=实体库存 2=虚拟库存
			stockTypeText(t) {
				return t === 2 ? '虚拟库存' : '实体库存';
			},
			shortTime(t) {
				if (!t) return '—';
				return String(t).substring(0, 16).replace('T', ' ');
			},
			payOrder(o) {
				uni.showActionSheet({
					itemList: ['余额支付', '微信支付'],
					success: (r) => {
						if (r.tapIndex === 0) {
							payStockOrder({ orderNo: o.orderNo, payType: 'yue' }).then(() => {
								uni.showToast({ title: '支付成功', icon: 'success' });
								this.load();
							});
						} else {
							payStockOrder({ orderNo: o.orderNo, payType: 'weixin', payChannel: 'routine' }).then(res => {
								const js = (res.data && res.data.jsConfig) || {};
								uni.requestPayment({
									provider: 'wxpay',
									appId: js.appId,
									nonceStr: js.nonceStr,
									package: js.packages,
									signType: js.signType || 'MD5',
									timeStamp: js.timeStamp,
									paySign: js.paySign,
									success: () => { uni.showToast({ title: '支付成功', icon: 'success' }); this.load(); },
									fail: () => uni.showToast({ title: '支付未完成', icon: 'none' })
								});
							});
						}
					}
				});
			},
			cancelOrder(o) {
				uni.showModal({
					title: '取消订单',
					content: '确认取消该待付款订单？',
					success: (m) => {
						if (!m.confirm) return;
						cancelStockOrder(o.id).then(() => {
							uni.showToast({ title: '已取消', icon: 'success' });
							this.load();
						});
					}
				});
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
				if (this.tabType === 'audit') this.loadExList();
			},
			// 待我审核的换货单（下级提交的实体换货）
			loadExList() {
				getExchangeAuditList({ page: 1, limit: 30 }).then(res => {
					this.exList = res.data.list || [];
				}).catch(() => { this.exList = []; });
			},
			// 上级审核换货单：1=通过（流转总部审核） -1=驳回（需填原因）
			auditEx(e, result) {
				if (result === -1) {
					uni.showModal({
						title: '驳回换货',
						editable: true,
						placeholderText: '请填写驳回原因',
						success: (m) => {
							if (!m.confirm) return;
							auditStockExchange(e.id, { status: -1, reason: m.content || '' }).then(() => {
								uni.showToast({ title: '已驳回', icon: 'success' });
								this.load();
							}).catch(err => {
								uni.showToast({ title: err || '操作失败', icon: 'none' });
							});
						}
					});
				} else {
					uni.showModal({
						title: '通过换货',
						content: '通过后该换货单流转总部审核，再由总部安排旧品退回与新品发出。',
						success: (m) => {
							if (!m.confirm) return;
							auditStockExchange(e.id, { status: 1 }).then(() => {
								uni.showToast({ title: '已通过', icon: 'success' });
								this.load();
							}).catch(err => {
								uni.showToast({ title: err || '操作失败', icon: 'none' });
							});
						}
					});
				}
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
						content: '该订单已付款。通过后扣云仓库存进入待发货，请确认库存充足。',
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
.order-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 40rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}

/* ---------- 渐变头 ---------- */
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #2b6fe3 0%, #4a9df8 60%, #6bb2ff 100%);
  border-radius: 24rpx;
  padding: 34rpx 32rpx 30rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(43, 111, 227, 0.28);

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
  position: relative;
  z-index: 1;
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.head-title {
  position: relative;
  z-index: 1;
  margin-top: 16rpx;
  font-size: 38rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}
.head-sub {
  position: relative;
  z-index: 1;
  margin-top: 10rpx;
  font-size: 23rpx;
  opacity: 0.8;
}

/* ---------- 状态筛选（胶囊 tab） ---------- */
.tabs-wrap {
  position: sticky;
  top: 0;
  z-index: 9;
  margin: 24rpx -24rpx 0;
  padding: 16rpx 20rpx;
  background: rgba(245, 246, 250, 0.96);
}
.tabs {
  display: flex;
  background: #fff;
  border-radius: 999rpx;
  padding: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(31, 45, 61, 0.06);
  overflow-x: auto;
  white-space: nowrap;

  &::-webkit-scrollbar { display: none; }
}
.tab-item {
  flex-shrink: 0;
  padding: 12rpx 26rpx;
  margin-right: 4rpx;
  font-size: 25rpx;
  color: #606266;
  border-radius: 999rpx;
  transition: all 0.2s;

  &:last-child { margin-right: 0; }
  &.active {
    background: linear-gradient(135deg, #2b6fe3, #4a9df8);
    color: #fff;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(43, 111, 227, 0.32);
  }
}

/* ---------- 订单卡 ---------- */
.card-list { margin-top: 8rpx; }
.order-card {
  background: #fff;
  border-radius: 20rpx;
  margin-top: 20rpx;
  padding: 26rpx 26rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.row-1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid #f0f2f6;
}
.order-no {
  font-size: 26rpx;
  color: #303133;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}
.st-pill {
  flex-shrink: 0;
  font-size: 22rpx;
  line-height: 1;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-weight: 500;

  &.st0 { background: #fff4e5; color: #f08c2e; }
  &.st1 { background: #fff4e5; color: #f08c2e; }
  &.st2 { background: #ecf3ff; color: #2b6fe3; }
  &.st3 { background: #ecf3ff; color: #2b6fe3; }
  &.st4 { background: #e9f9ec; color: #21a84f; }
  &.st-1 { background: #ffecec; color: #f56c6c; }
  &.st10 { background: #f3ecff; color: #7c4dd4; }
  &.st-2 { background: #f2f3f5; color: #909399; }
  /* 待我审核 tab：下单时我方无库存，需补货后才能审核通过（醒目提示待处理） */
  &.stwait { background: #fff0e8; color: #e8652f; }
}

/* 已换货标记 */
.ex-pill {
  margin-right: 12rpx;
  background: #e6f4ff;
  color: #1677ff;
}

/* 单号右侧标签组 */
.pill-group {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.pill-group .tp-pill {
  margin-right: 10rpx;
}
/* 库存/订单类型标签 */
.tp-pill {
  flex-shrink: 0;
  font-size: 21rpx;
  line-height: 1;
  padding: 8rpx 14rpx;
  border-radius: 8rpx;
  font-weight: 500;
  border: 1rpx solid transparent;

  &.is-physical { background: #eef4ff; color: #2b6fe3; border-color: #d6e4ff; }
  &.is-virtual { background: #f0fff4; color: #21a84f; border-color: #cdeddb; }
  &.is-pickup { background: #f3ecff; color: #7c4dd4; border-color: #e2d6ff; }
  &.is-exchange { background: #fff4e5; color: #f08c2e; border-color: #ffe0b8; }
}

/* 下级信息（审核 tab） */
.audit-user {
  display: flex;
  align-items: center;
  background: #f8f9fc;
  border-radius: 14rpx;
  padding: 14rpx 18rpx;
  margin-top: 18rpx;
}
.au-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2b6fe3, #6bb2ff);
  color: #fff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.au-info {
  margin-left: 14rpx;
  display: flex;
  flex-direction: column;
}
.au-name { font-size: 24rpx; color: #303133; font-weight: 600; }
.au-level { font-size: 20rpx; color: #909399; margin-top: 2rpx; }
.au-tip {
  margin-left: auto;
  font-size: 21rpx;
  color: #f08c2e;
  background: #fff4e5;
  border-radius: 999rpx;
  padding: 6rpx 14rpx;
  flex-shrink: 0;
}

/* ---------- 换货审核卡（待我审核 tab） ---------- */
.ex-card { border-left: 6rpx solid #ffb54d; }
.ex-target {
  margin-top: 16rpx;
  background: #fff8ee;
  border-radius: 12rpx;
  padding: 14rpx 18rpx;
  font-size: 23rpx;
  color: #b8781f;
  line-height: 34rpx;
}
.diff-amt { color: #e93323; font-weight: 700; margin-left: 6rpx; }
.same-amt { color: #21a84f; margin-left: 6rpx; }
.ex-reason {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #909399;
  line-height: 34rpx;
}

/* 商品行 */
.row-p {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
}
.p-img {
  width: 104rpx;
  height: 104rpx;
  border-radius: 14rpx;
  flex-shrink: 0;
  background: #f5f6fa;
}
.p-info {
  flex: 1;
  margin: 0 18rpx;
  overflow: hidden;
}
.p-name {
  font-size: 26rpx;
  color: #303133;
  line-height: 36rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.p-num { font-size: 22rpx; color: #909399; margin-top: 8rpx; }
.p-x { color: #b8bfc9; }
.p-sum { font-size: 26rpx; color: #303133; font-weight: 600; flex-shrink: 0; }

/* 合计 */
.row-total {
  text-align: right;
  font-size: 24rpx;
  color: #606266;
  margin-top: 20rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f0f2f6;
}
.total-price {
  color: #e93323;
  font-size: 32rpx;
  font-weight: 700;
  margin-left: 4rpx;
}

/* 驳回 / 快递提示条 */
.reject-box {
  margin-top: 16rpx;
  background: #fef0f0;
  border-radius: 12rpx;
  padding: 14rpx 18rpx;
  font-size: 23rpx;
  color: #f56c6c;
  line-height: 34rpx;
}
/* 匹配上级中提示条 */
.wait-box {
  background: #f6f0ff;
  color: #7c4dd4;
}

/* 下单时间 / 等待时长 */
.row-time {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.rt-item {
  font-size: 22rpx;
  color: #909399;
  margin-right: 20rpx;
}
.rt-wait {
  color: #7c4dd4;
  background: #f3ecff;
  border-radius: 999rpx;
  padding: 4rpx 14rpx;
  margin-right: 0;
}
.express-box {
  margin-top: 16rpx;
  background: #ecf3ff;
  border-radius: 12rpx;
  padding: 14rpx 18rpx;
  font-size: 23rpx;
  color: #2b6fe3;
  display: flex;
  align-items: center;
}
.ex-tag {
  flex-shrink: 0;
  font-size: 20rpx;
  color: #fff;
  background: #2b6fe3;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-right: 12rpx;
}

/* 操作按钮 */
.row-op {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20rpx;
  gap: 18rpx;
}
.op-btn {
  margin: 0;
  padding: 0 34rpx;
  height: 60rpx;
  line-height: 58rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  background: #f2f3f5;
  color: #606266;
  border: none;

  &::after { border: none; }
  &.primary {
    background: linear-gradient(135deg, #2b6fe3, #4a9df8);
    color: #fff;
    box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.28);
  }
  &.danger {
    background: #fff;
    color: #f56c6c;
    border: 1rpx solid #fbc4c4;
  }
  &.ghost {
    background: #fff;
    color: #2b6fe3;
    border: 1rpx solid #c6dcfa;
  }
}

/* ---------- 空态 ---------- */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 110rpx 0 40rpx;
}
.empty-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 8rpx 30rpx rgba(31, 45, 61, 0.07);
  display: flex;
  align-items: center;
  justify-content: center;
}
.e-box { position: relative; width: 72rpx; height: 56rpx; }
.e-lid {
  position: absolute;
  top: -14rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 88rpx;
  height: 18rpx;
  border-radius: 8rpx;
  background: #dfe6f0;
}
.e-face {
  width: 72rpx;
  height: 56rpx;
  border-radius: 10rpx;
  background: #eef2f8;
  display: flex;
  align-items: center;
  justify-content: center;
}
.e-check { font-size: 30rpx; color: #b9c4d4; font-weight: 700; }
.empty-title {
  margin-top: 30rpx;
  font-size: 28rpx;
  color: #606266;
  font-weight: 600;
}
.empty-sub {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #b0b8c4;
}

.bottom-tip {
  margin-top: 50rpx;
  text-align: center;
  font-size: 22rpx;
  color: #c3cad6;
  letter-spacing: 4rpx;
}
</style>
