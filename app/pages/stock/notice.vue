<template>
  <view class="notice-page">
    <view v-for="n in list" :key="n.id" class="notice-card" :class="{ unread: !n.isRead }" @click="read(n)">
      <view class="n-line">
        <view class="n-title">
          <text v-if="!n.isRead" class="dot"></text>
          {{ n.title }}
        </view>
        <text class="n-type">{{ typeName(n.type) }}</text>
      </view>
      <view class="n-content">{{ n.content }}</view>
      <view class="n-time">{{ n.createTime }}</view>
    </view>
    <view v-if="!list.length && loaded" class="empty">暂无消息</view>
  </view>
</template>

<script>
	import { getStockNotices, readStockNotice } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			typeName(t) {
				return { 1: '订单审核', 2: '发货通知', 3: '奖金到账', 4: '提现审核' }[t] || '通知';
			},
			load() {
				getStockNotices({ page: 1, limit: 50 }).then(res => {
					this.list = res.data.list || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			read(n) {
				if (n.isRead) return;
				readStockNotice(n.id).then(() => { n.isRead = 1; });
			}
		}
	};
</script>

<style lang="scss" scoped>
.notice-page { min-height: 100vh; background: #f5f6f8; padding: 24rpx; }
.notice-card { background: #fff; border-radius: 16rpx; padding: 26rpx; margin-bottom: 18rpx; }
.notice-card.unread { border-left: 6rpx solid #2b6fe3; }
.n-line { display: flex; justify-content: space-between; align-items: center; }
.n-title { font-size: 28rpx; color: #333; font-weight: 600; display: flex; align-items: center; }
.dot { width: 14rpx; height: 14rpx; background: #e93323; border-radius: 50%; margin-right: 12rpx; }
.n-type { font-size: 22rpx; color: #2b6fe3; background: #eef4ff; border-radius: 8rpx; padding: 4rpx 14rpx; }
.n-content { font-size: 25rpx; color: #666; margin-top: 12rpx; line-height: 1.6; }
.n-time { font-size: 22rpx; color: #b0b6bf; margin-top: 10rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
