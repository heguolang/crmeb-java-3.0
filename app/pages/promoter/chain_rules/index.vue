<template>
	<view class="page">
		<view class="card">
			<view class="title">链动规则说明</view>
			<text class="content">{{ content }}</text>
		</view>
	</view>
</template>

<script>
	import { chainRules } from '@/api/chain.js';
	export default {
		data() {
			return { content: '加载中...' };
		},
		onShow() {
			chainRules().then(res => {
				this.content = (res.data && res.data.content) || '暂无规则说明';
			}).catch(() => {
				this.content = '加载失败';
			});
		}
	};
</script>

<style scoped>
	.page { padding: 24rpx; background: #f5f5f5; min-height: 100vh; }
	.card { background: #fff; border-radius: 16rpx; padding: 28rpx; }
	.title { font-size: 34rpx; font-weight: 600; margin-bottom: 20rpx; }
	.content { white-space: pre-wrap; font-size: 28rpx; color: #333; line-height: 1.7; }
</style>
