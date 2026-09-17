<template>
  <view class="verify-page">
    <view class="input-card">
      <view class="input-title">输入订单核销码</view>
      <input v-model="vCode" class="v-input" type="text" placeholder="请输入用户出示的核销码" maxlength="16" />
      <button class="btn-primary" :disabled="!vCode || loading" @click="onPreview">查询订单</button>
    </view>

    <view v-if="order" class="order-card">
      <view class="o-line"><text class="o-label">订单号</text><text>{{ order.orderId }}</text></view>
      <view class="o-line"><text class="o-label">客户</text><text>{{ order.realName }} {{ order.userPhone }}</text></view>
      <view class="o-line"><text class="o-label">商品</text><text class="o-pro">{{ productSummary }}</text></view>
      <view class="o-line"><text class="o-label">支付金额</text><text class="o-price">¥{{ order.payPrice }}</text></view>
      <button class="btn-success" :loading="loading" @click="onConfirm">确认核销</button>
    </view>
  </view>
</template>

<script>
	import { previewVerifyOrder, confirmVerifyOrder } from '@/api/merchantStore.js';
	export default {
		data() {
			return {
				vCode: '',
				order: null,
				loading: false
			};
		},
		computed: {
			productSummary() {
				const infos = this.order.storeOrderInfoVos || [];
				return infos.map(e => (e.info ? e.info.productName + 'x' + e.info.payNum : '')).filter(Boolean).join('；');
			}
		},
		methods: {
			onPreview() {
				if (!this.vCode) return;
				this.loading = true;
				previewVerifyOrder(this.vCode).then(res => {
					this.loading = false;
					this.order = res.data;
				}).catch(() => {
					this.loading = false;
					this.order = null;
				});
			},
			onConfirm() {
				const that = this;
				uni.showModal({
					title: '确认核销',
					content: '核销后订单不可恢复，确定核销该订单吗？',
					success(m) {
						if (!m.confirm) return;
						that.loading = true;
						confirmVerifyOrder(that.vCode).then(() => {
							that.loading = false;
							uni.showToast({ title: '核销成功', icon: 'success' });
							that.order = null;
							that.vCode = '';
						}).catch(() => { that.loading = false; });
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.verify-page { padding: 24rpx; }
.input-card, .order-card {
  background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 24rpx;
}
.input-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 20rpx; }
.v-input {
  background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 30rpx;
  margin-bottom: 24rpx;
}
.btn-primary { background: #1890ff; color: #fff; border-radius: 40rpx; font-size: 28rpx; }
.btn-success { background: #52c41a; color: #fff; border-radius: 40rpx; font-size: 28rpx; margin-top: 24rpx; }
.o-line { display: flex; font-size: 26rpx; color: #333; margin-bottom: 16rpx; }
.o-label { color: #999; width: 140rpx; flex-shrink: 0; }
.o-pro { flex: 1; }
.o-price { color: #ff4d4f; font-weight: bold; }
</style>
