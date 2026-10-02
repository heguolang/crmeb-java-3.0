<template>
  <view class="records-page">
    <!-- 渐变头 -->
    <view class="head">
      <view class="head-title">核销记录</view>
      <view class="head-sub">共 {{ total }} 条核销明细</view>
    </view>

    <view class="list-wrap">
      <view v-for="item in list" :key="item.id" class="record-card">
        <view class="rc-top">
          <text class="rc-order">{{ item.orderNo }}</text>
          <text class="rc-fee">¥{{ item.serviceFee }}</text>
        </view>
        <view class="rc-fee-label">服务费</view>
        <view class="rc-product">{{ item.productInfo || '-' }}</view>
        <view class="rc-divider"></view>
        <view class="rc-bottom">
          <text class="rc-type">{{ item.verifyType === 1 ? '核销码核销' : '后台核销' }}</text>
          <view class="rc-meta">
            <text class="rc-store">{{ item.storeName || '门店' }}</text>
            <text class="rc-time">{{ item.createTime }}</text>
          </view>
        </view>
      </view>

      <emptyPage v-if="loaded && list.length === 0" :title="'暂无核销记录'"></emptyPage>

      <view v-if="list.length > 0" class="load-tip">
        {{ list.length < total ? '上拉加载更多' : '— 没有更多了 —' }}
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyVerifyRecords } from '@/api/merchantStore.js';
	import emptyPage from '@/components/emptyPage.vue';
	export default {
		components: { emptyPage },
		data() {
			return {
				loaded: false,
				list: [],
				page: 1,
				limit: 20,
				total: 0
			};
		},
		onLoad() {
			this.loadData();
		},
		onReachBottom() {
			if (this.list.length < this.total) {
				this.page += 1;
				this.loadData();
			}
		},
		methods: {
			loadData() {
				uni.showLoading({ title: '加载中' });
				getMyVerifyRecords({ page: this.page, limit: this.limit }).then(res => {
					uni.hideLoading();
					this.loaded = true;
					const data = res.data || {};
					const rows = data.list || [];
					this.total = data.total || 0;
					this.list = this.page === 1 ? rows : this.list.concat(rows);
				}).catch(() => {
					uni.hideLoading();
					this.loaded = true;
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.records-page {
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

/* ---------- 列表 ---------- */
.list-wrap {
  position: relative;
  z-index: 2;
  margin: -64rpx 24rpx 0;
}
.record-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx 28rpx 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.rc-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.rc-order {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}
.rc-fee {
  flex-shrink: 0;
  font-size: 34rpx;
  font-weight: 700;
  color: #ff5a2c;
}
.rc-fee-label {
  text-align: right;
  font-size: 20rpx;
  color: #b8c0cc;
  margin-top: 2rpx;
}
.rc-product {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #606266;
  line-height: 38rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.rc-divider {
  height: 1rpx;
  background: #f0f2f6;
  margin: 20rpx 0 18rpx;
}
.rc-bottom {
  display: flex;
  align-items: center;
}
.rc-type {
  flex-shrink: 0;
  font-size: 20rpx;
  color: #4d8dff;
  background: #ecf3ff;
  border-radius: 999rpx;
  padding: 4rpx 16rpx;
}
.rc-meta {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: hidden;
  margin-left: 16rpx;
}
.rc-store {
  flex-shrink: 0;
  max-width: 40%;
  font-size: 22rpx;
  color: #8a94a6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rc-time {
  flex-shrink: 0;
  margin-left: 14rpx;
  font-size: 22rpx;
  color: #b8c0cc;
}
.load-tip {
  padding: 26rpx 0 10rpx;
  text-align: center;
  font-size: 22rpx;
  color: #c3cad6;
  letter-spacing: 2rpx;
}
</style>
