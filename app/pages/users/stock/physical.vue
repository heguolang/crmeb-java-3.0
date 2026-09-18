<template>
  <view class="physical-page">
    <!-- 渐变头 -->
    <view class="head-card">
      <view class="badge">实体库存</view>
      <view class="head-title">我的云仓库存</view>
      <view class="head-sub">已付款采购入仓 · 下级订货自动扣减</view>
    </view>

    <!-- 库存列表 -->
    <view class="card-list">
      <view v-for="(p, idx) in list" :key="idx" class="p-card">
        <image :src="p.image" class="p-img" mode="aspectFill" />
        <view class="p-info">
          <view class="p-name">{{ p.productName }}</view>
          <view class="p-meta">可供应数量 <text class="p-num">{{ p.num }}</text></view>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <view v-if="!list.length && loaded" class="empty-box">
      <view class="empty-title">暂无实体库存</view>
      <view class="empty-sub">在商品中心选择「实体库存」下单，付款后总部发入云仓</view>
      <button class="go-btn" size="mini" @click="navGoods">去订货</button>
    </view>

    <view class="bottom-tip">— 实体库存 —</view>
  </view>
</template>

<script>
	import { getMyPhysicalStock } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false
			};
		},
		onLoad() {
			this.load();
		},
		methods: {
			load() {
				getMyPhysicalStock().then(res => {
					this.list = res.data || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			navGoods() {
				uni.navigateTo({ url: '/pages/users/stock/goods' });
			}
		}
	};
</script>

<style lang="scss" scoped>
.physical-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 40rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1f5fc4 0%, #2b7de9 60%, #5aa7f5 100%);
  border-radius: 24rpx;
  padding: 34rpx 32rpx 30rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(43, 125, 233, 0.28);
  &::before {
    content: '';
    position: absolute;
    width: 220rpx; height: 220rpx; border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
    right: -70rpx; top: -90rpx;
  }
}
.badge {
  position: relative; z-index: 1;
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.head-title { position: relative; z-index: 1; margin-top: 16rpx; font-size: 38rpx; font-weight: 700; }
.head-sub { position: relative; z-index: 1; margin-top: 10rpx; font-size: 23rpx; opacity: 0.85; }

.card-list { margin-top: 8rpx; }
.p-card {
  background: #fff;
  border-radius: 20rpx;
  margin-top: 20rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.p-img { width: 120rpx; height: 120rpx; border-radius: 14rpx; flex-shrink: 0; background: #f5f6fa; }
.p-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
.p-name { font-size: 28rpx; color: #303133; font-weight: 600; line-height: 38rpx; }
.p-meta { font-size: 23rpx; color: #909399; margin-top: 10rpx; }
.p-num { color: #2b6fe3; font-weight: 700; font-size: 30rpx; margin-left: 6rpx; }

.empty-box { display: flex; flex-direction: column; align-items: center; padding: 110rpx 0 40rpx; }
.empty-title { font-size: 28rpx; color: #606266; font-weight: 600; }
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #b0b8c4; }
.go-btn { margin-top: 30rpx; background: #2b6fe3; color: #fff; border-radius: 999rpx; padding: 0 50rpx; }

.bottom-tip { margin-top: 50rpx; text-align: center; font-size: 22rpx; color: #c3cad6; letter-spacing: 4rpx; }
</style>
