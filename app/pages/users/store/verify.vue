<template>
  <view class="records-page">
    <view v-for="(item, index) in list" :key="item.id" class="record-card">
      <view class="r-head">
        <text class="r-store">{{ item.storeName || '门店' }}</text>
        <text class="r-time">{{ item.createTime }}</text>
      </view>
      <view class="r-line"><text class="r-label">核销订单</text><text>{{ item.orderNo }}</text></view>
      <view class="r-line"><text class="r-label">核销产品</text><text class="r-pro">{{ item.productInfo || '-' }}</text></view>
      <view class="r-line">
        <text class="r-label">核销方式</text><text>{{ item.verifyType === 1 ? '核销码核销' : item.verifyType }}</text>
        <text class="r-fee">服务费 ¥{{ item.serviceFee }}</text>
      </view>
    </view>
    <emptyPage v-if="loaded && list.length === 0" :title="'暂无核销记录'"></emptyPage>
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
.records-page { padding: 24rpx; }
.record-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.r-head { display: flex; justify-content: space-between; margin-bottom: 14rpx; }
.r-store { font-size: 28rpx; font-weight: bold; color: #333; }
.r-time { font-size: 22rpx; color: #999; }
.r-line { display: flex; font-size: 24rpx; color: #333; margin-bottom: 10rpx; align-items: center; }
.r-label { color: #999; width: 130rpx; flex-shrink: 0; }
.r-pro { flex: 1; }
.r-fee { margin-left: auto; color: #ff4d4f; }
</style>
