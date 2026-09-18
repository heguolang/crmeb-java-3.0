<template>
  <view class="store-home">
    <template v-if="isLeader">
      <!-- 门店信息头 -->
      <view class="head-card">
        <view class="badge">门店负责人</view>
        <view class="store-name">{{ store.name }}</view>
        <view class="head-divider"></view>
        <view class="meta-row">
          <text class="meta-label">地址</text>
          <text class="meta-value">{{ store.address }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">电话</text>
          <text class="meta-value">{{ store.phone }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">营业</text>
          <text class="meta-value">{{ store.dayTime || '-' }}</text>
        </view>
        <view class="svc-row">
          <text class="svc-pill" :class="{ dim: !store.selfPickup }">到店自提{{ store.selfPickup ? '已开通' : '未开通' }}</text>
          <text class="svc-pill" :class="{ dim: !store.delivery }">上门配送{{ store.delivery ? ' ' + store.deliveryRadius + 'km' : '未开通' }}</text>
        </view>
      </view>

      <!-- 经营数据 -->
      <view class="stat-card">
        <view class="stat-col">
          <view class="stat-num num-orange">{{ pendingVerifyCount }}</view>
          <view class="stat-label">待核销订单</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-col">
          <view class="stat-num num-green">¥{{ serviceFeeSum }}</view>
          <view class="stat-label">累计服务费</view>
        </view>
      </view>

      <!-- 功能入口 -->
      <view class="section-title">门店经营</view>
      <view class="action-main" @click="nav('/pages/users/store/verify-order')">
        <view class="am-icon">
          <text class="am-icon-text">核销</text>
        </view>
        <view class="am-body">
          <view class="am-title">门店核销</view>
          <view class="am-sub">扫描用户核销码，完成订单核销</view>
        </view>
        <view class="arrow"></view>
      </view>
      <view class="action-sub" @click="nav('/pages/users/store/verify')">
        <view class="as-icon">
          <text class="as-icon-text">录</text>
        </view>
        <view class="am-body">
          <view class="am-title">核销记录</view>
          <view class="am-sub">查看历史核销明细与服务费</view>
        </view>
        <view class="arrow grey"></view>
      </view>

      <view class="bottom-tip">— 门店中心 —</view>
    </template>

    <emptyPage v-if="loaded && !isLeader" :title="'您还不是门店负责人，请联系管理员在后台门店管理中绑定'"></emptyPage>
  </view>
</template>

<script>
	import { getMyStoreInfo } from '@/api/merchantStore.js';
	import emptyPage from '@/components/emptyPage.vue';
	export default {
		components: { emptyPage },
		data() {
			return {
				loaded: false,
				isLeader: false,
				store: {},
				pendingVerifyCount: 0,
				serviceFeeSum: '0.00'
			};
		},
		onLoad() {
			this.loadData();
		},
		methods: {
			loadData() {
				uni.showLoading({ title: '加载中' });
				getMyStoreInfo().then(res => {
					uni.hideLoading();
					this.loaded = true;
					const data = res.data || {};
					this.isLeader = !!data.isLeader;
					this.store = data.store || {};
					this.pendingVerifyCount = data.pendingVerifyCount || 0;
					this.serviceFeeSum = data.serviceFeeSum || '0.00';
				}).catch(() => {
					uni.hideLoading();
					this.loaded = true;
				});
			},
			nav(url) {
				uni.navigateTo({ url });
			}
		}
	};
</script>

<style lang="scss" scoped>
.store-home {
  min-height: 100vh;
  padding: 24rpx 24rpx 60rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}

/* ---------- 门店信息头 ---------- */
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #ff7a45 0%, #ff9a62 55%, #ffb36b 100%);
  border-radius: 24rpx;
  padding: 36rpx 32rpx 32rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(255, 122, 69, 0.28);

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
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.store-name {
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}
.head-divider {
  margin: 26rpx 0 20rpx;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.28);
}
.meta-row {
  display: flex;
  align-items: flex-start;
  margin-top: 12rpx;
  position: relative;
  z-index: 1;
}
.meta-label {
  flex-shrink: 0;
  font-size: 24rpx;
  opacity: 0.75;
  width: 76rpx;
}
.meta-value {
  font-size: 24rpx;
  line-height: 34rpx;
  opacity: 0.98;
}
.svc-row {
  margin-top: 20rpx;
  position: relative;
  z-index: 1;
}
.svc-pill {
  display: inline-block;
  background: rgba(255, 255, 255, 0.24);
  border-radius: 999rpx;
  padding: 6rpx 20rpx;
  font-size: 22rpx;
  margin-right: 14rpx;
}
.svc-pill.dim {
  background: rgba(0, 0, 0, 0.14);
  opacity: 0.65;
}

/* ---------- 经营数据 ---------- */
.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  margin-top: 24rpx;
  padding: 34rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.stat-col {
  flex: 1;
  text-align: center;
}
.stat-divider {
  width: 1rpx;
  height: 64rpx;
  background: #eef0f4;
}
.stat-num {
  font-size: 44rpx;
  font-weight: 700;
  line-height: 52rpx;
}
.num-orange { color: #ff7a45; }
.num-green { color: #12b76a; }
.stat-label {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #8a94a6;
}

/* ---------- 功能入口 ---------- */
.section-title {
  margin: 36rpx 8rpx 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
}
.action-main {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 8rpx;
    background: linear-gradient(180deg, #ff7a45, #ffb36b);
  }
}
.am-icon {
  flex-shrink: 0;
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ff7a45, #ffb36b);
  box-shadow: 0 8rpx 18rpx rgba(255, 122, 69, 0.32);
  display: flex;
  align-items: center;
  justify-content: center;
}
.am-icon-text {
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}
.as-icon {
  flex-shrink: 0;
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #4d8dff, #6fb0ff);
  box-shadow: 0 8rpx 18rpx rgba(77, 141, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
}
.as-icon-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
}
.am-body {
  flex: 1;
  min-width: 0;
  margin-left: 26rpx;
}
.am-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}
.am-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #98a2b3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow {
  flex-shrink: 0;
  width: 18rpx;
  height: 18rpx;
  border-top: 4rpx solid #c4ccda;
  border-right: 4rpx solid #c4ccda;
  transform: rotate(45deg);
  margin-left: 16rpx;
}
.action-sub {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx 28rpx;
  margin-top: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.action-sub .arrow { margin-left: 16rpx; }

.bottom-tip {
  margin-top: 60rpx;
  text-align: center;
  font-size: 22rpx;
  color: #c3cad6;
  letter-spacing: 4rpx;
}
</style>
