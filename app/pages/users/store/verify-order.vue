<template>
  <view class="verify-page">
    <!-- 渐变头 -->
    <view class="head">
      <view class="head-title">门店核销</view>
      <view class="head-sub">输入用户出示的核销码，查询并完成订单核销</view>
    </view>

    <!-- 输入卡（上浮叠在头部） -->
    <view class="input-card">
      <view class="input-label">订单核销码</view>
      <view class="input-box" :class="{ focus: inputFocus }">
        <input
          v-model="vCode"
          class="v-input"
          type="text"
          placeholder="请输入用户出示的核销码"
          placeholder-class="v-ph"
          maxlength="16"
          :focus="inputFocus"
          @confirm="onPreview"
          @focus="inputFocus = true"
          @blur="inputFocus = false"
        />
        <text v-if="vCode" class="clear-btn" @click="clearCode">×</text>
      </view>
      <button class="btn-query" :class="{ disabled: !vCode || loading }" :disabled="!vCode || loading" @click="onPreview">
        {{ loading ? '查询中…' : '查询订单' }}
      </button>
    </view>

    <!-- 订单信息 -->
    <view v-if="order" class="order-card">
      <view class="oc-head">
        <view class="oc-dot"></view>
        <text class="oc-title">订单信息</text>
        <text class="oc-tag">待核销</text>
      </view>
      <view class="oc-divider"></view>
      <view class="o-line">
        <text class="o-label">订单号</text>
        <text class="o-value">{{ order.orderId }}</text>
      </view>
      <view class="o-line">
        <text class="o-label">客户</text>
        <text class="o-value">{{ order.realName }} {{ order.userPhone }}</text>
      </view>
      <view class="o-line">
        <text class="o-label">商品</text>
        <text class="o-value o-pro">{{ productSummary }}</text>
      </view>
      <view class="o-line">
        <text class="o-label">支付金额</text>
        <text class="o-price">¥{{ order.payPrice }}</text>
      </view>
      <button class="btn-confirm" :loading="loading" @click="onConfirm">确认核销</button>
      <view class="confirm-tip">核销后订单不可恢复，请与客户当面确认商品</view>
    </view>

    <!-- 空态引导 -->
    <view v-else class="empty-guide">
      <view class="eg-box">
        <view class="eg-icon"><view class="eg-ring"></view><view class="eg-handle"></view></view>
        <text class="eg-text">输入核销码后，订单信息将显示在这里</text>
      </view>
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
				loading: false,
				inputFocus: false
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
			clearCode() {
				this.vCode = '';
				this.order = null;
				this.inputFocus = true;
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
.verify-page {
  min-height: 100vh;
  padding-bottom: 60rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}

/* ---------- 渐变头 ---------- */
.head {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #ff7a45 0%, #ff9a62 55%, #ffb36b 100%);
  padding: 44rpx 36rpx 96rpx;

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
    width: 130rpx;
    height: 130rpx;
    right: 100rpx;
    bottom: -60rpx;
  }
}
.head-title {
  color: #fff;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}
.head-sub {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.85);
  font-size: 24rpx;
}

/* ---------- 输入卡（上浮） ---------- */
.input-card {
  position: relative;
  z-index: 2;
  margin: -64rpx 24rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(31, 45, 61, 0.08);
}
.input-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}
.input-box {
  display: flex;
  align-items: center;
  margin-top: 22rpx;
  background: #f5f6fa;
  border: 2rpx solid transparent;
  border-radius: 16rpx;
  padding: 0 24rpx;
  transition: border-color 0.2s;

  &.focus {
    border-color: #ff9a62;
    background: #fff;
  }
}
.v-input {
  flex: 1;
  height: 88rpx;
  font-size: 30rpx;
  color: #303133;
}
.v-ph { color: #b8c0cc; }
.clear-btn {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  line-height: 40rpx;
  text-align: center;
  border-radius: 50%;
  background: #dfe3ea;
  color: #fff;
  font-size: 30rpx;
}
.btn-query {
  margin-top: 26rpx;
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #ff7a45, #ffb36b);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  box-shadow: 0 8rpx 18rpx rgba(255, 122, 69, 0.3);

  &.disabled {
    background: #ffd6bd;
    box-shadow: none;
  }
  &::after { border: none; }
}

/* ---------- 订单信息卡 ---------- */
.order-card {
  margin: 24rpx 24rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.oc-head {
  display: flex;
  align-items: center;
}
.oc-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #ff7a45;
  margin-right: 12rpx;
}
.oc-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}
.oc-tag {
  margin-left: auto;
  font-size: 22rpx;
  color: #ff7a45;
  background: #fff1e8;
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
}
.oc-divider {
  height: 1rpx;
  background: #f0f2f6;
  margin: 24rpx 0;
}
.o-line {
  display: flex;
  font-size: 26rpx;
  margin-bottom: 18rpx;
}
.o-label {
  flex-shrink: 0;
  width: 130rpx;
  color: #98a2b3;
}
.o-value {
  color: #303133;
  word-break: break-all;
}
.o-pro { flex: 1; }
.o-price {
  margin-left: auto;
  color: #ff5a2c;
  font-size: 30rpx;
  font-weight: 700;
}
.btn-confirm {
  margin-top: 30rpx;
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #16b877, #3dd598);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
  box-shadow: 0 8rpx 18rpx rgba(22, 184, 119, 0.28);

  &::after { border: none; }
}
.confirm-tip {
  margin-top: 16rpx;
  text-align: center;
  font-size: 22rpx;
  color: #b8c0cc;
}

/* ---------- 空态引导 ---------- */
.empty-guide {
  margin: 24rpx 24rpx 0;
}
.eg-box {
  border: 2rpx dashed #d9dee8;
  border-radius: 24rpx;
  padding: 70rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.eg-icon {
  position: relative;
  width: 80rpx;
  height: 80rpx;
}
.eg-ring {
  position: absolute;
  left: 0;
  top: 0;
  width: 52rpx;
  height: 52rpx;
  border: 6rpx solid #d9dee8;
  border-radius: 50%;
}
.eg-handle {
  position: absolute;
  right: 4rpx;
  bottom: 4rpx;
  width: 24rpx;
  height: 6rpx;
  background: #d9dee8;
  border-radius: 4rpx;
  transform: rotate(45deg);
}
.eg-text {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #b8c0cc;
}
</style>
