<template>
	<view class="page">
		<view v-for="(item, i) in list" :key="i" class="card" @click="read(item)">
			<view class="title">
				<text>{{ item.title }}</text>
				<text class="dot" v-if="!item.isRead">未读</text>
			</view>
			<view class="content">{{ item.content }}</view>
			<view class="time">{{ item.createTime }}</view>
		</view>
		<view class="empty" v-if="!list.length">暂无通知</view>
	</view>
</template>
<script>
	import { chainNotifyList, chainNotifyRead } from '@/api/chain.js';
	export default {
		data() { return { list: [] }; },
		onShow() { this.load(); },
		methods: {
			load() {
				chainNotifyList({ page: 1, limit: 50 }).then(res => {
					this.list = (res.data && res.data.list) || [];
				});
			},
			read(item) {
				if (!item.isRead) {
					chainNotifyRead(item.id).then(() => { item.isRead = 1; });
				}
			}
		}
	};
</script>
<style scoped>
	.page { padding: 24rpx; background: #f5f5f5; min-height: 100vh; }
	.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 16rpx; }
	.title { font-size: 30rpx; font-weight: 600; display: flex; justify-content: space-between; }
	.dot { color: #e74c3c; font-size: 22rpx; font-weight: normal; }
	.content { margin-top: 10rpx; color: #666; font-size: 26rpx; }
	.time { margin-top: 8rpx; color: #999; font-size: 22rpx; }
	.empty { text-align: center; color: #999; padding: 40rpx; }
</style>
