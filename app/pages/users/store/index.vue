<template>
  <view class="store-home">
    <view class="head-card">
      <view v-if="isLeader" class="store-info">
        <view class="store-line">
          <text class="store-tag">门店负责人</text>
          <text class="store-name">{{ store.name }}</text>
        </view>
        <view class="info-sub">地址：{{ store.address }}</view>
        <view class="info-sub">电话：{{ store.phone }}　营业时间：{{ store.dayTime || '-' }}</view>
        <view class="info-sub">
          服务：
          <text v-if="store.selfPickup" class="svc on">自提</text>
          <text v-else class="svc off">自提</text>
          <text v-if="store.delivery" class="svc on">配送({{ store.deliveryRadius }}km)</text>
          <text v-else class="svc off">配送</text>
        </view>
      </view>
      <view v-else class="store-none">
        <text>您还不是门店负责人</text>
        <text class="tip">请联系管理员在后台门店管理中绑定负责人</text>
      </view>
    </view>

    <view v-if="isLeader" class="grid">
      <view class="grid-item" @click="nav('/pages/users/store/verify-order')">
        <view class="gi-icon gi-red">核销</view>
        <view class="gi-label">门店核销</view>
      </view>
      <view class="grid-item" @click="nav('/pages/users/store/verify')">
        <view class="gi-icon gi-blue">记录</view>
        <view class="gi-label">核销记录</view>
      </view>
      <view class="grid-item">
        <view class="gi-icon gi-orange num">{{ pendingVerifyCount }}</view>
        <view class="gi-label">待核销订单</view>
      </view>
      <view class="grid-item">
        <view class="gi-icon gi-green num">{{ serviceFeeSum }}</view>
        <view class="gi-label">累计服务费(¥)</view>
      </view>
    </view>

    <emptyPage v-if="loaded && !isLeader" :title="'暂无门店权限'"></emptyPage>
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
.store-home { padding: 24rpx; }
.head-card {
  background: linear-gradient(135deg, #ff7e5f, #feb47b);
  border-radius: 20rpx; padding: 30rpx; color: #fff;
}
.store-line { display: flex; align-items: center; }
.store-tag { background: rgba(255,255,255,.25); border-radius: 8rpx; padding: 4rpx 12rpx; font-size: 22rpx; margin-right: 14rpx; }
.store-name { font-size: 34rpx; font-weight: bold; }
.info-sub { font-size: 24rpx; opacity: .95; margin-top: 12rpx; }
.svc { margin-right: 10rpx; padding: 2rpx 10rpx; border-radius: 6rpx; font-size: 22rpx; }
.svc.on { background: rgba(255,255,255,.3); }
.svc.off { background: rgba(0,0,0,.15); opacity: .6; }
.store-none { display: flex; flex-direction: column; .tip { font-size: 24rpx; opacity: .8; margin-top: 10rpx; } }
.grid { display: flex; flex-wrap: wrap; margin-top: 24rpx; }
.grid-item {
  width: calc((100% - 24rpx) / 2); background: #fff; border-radius: 16rpx;
  padding: 30rpx 0; margin: 0 24rpx 24rpx 0; text-align: center;
  &:nth-child(2n) { margin-right: 0; }
}
.gi-icon {
  width: 72rpx; height: 72rpx; line-height: 72rpx; margin: 0 auto 12rpx;
  border-radius: 16rpx; color: #fff; font-size: 26rpx;
}
.gi-icon.num { font-size: 30rpx; font-weight: bold; }
.gi-red { background: #ff4d4f; }
.gi-blue { background: #1890ff; }
.gi-orange { background: #fa8c16; }
.gi-green { background: #52c41a; }
.gi-label { font-size: 26rpx; color: #333; }
</style>
