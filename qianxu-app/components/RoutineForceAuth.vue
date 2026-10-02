<template>
	<view>
		<view class="force-auth-popup" v-if="isShow">
			<image :src="logoUrl"></image>
			<view class="title">授权提醒</view>
			<view class="tip">为了给您提供更好的服务，需要授权登录后才能继续使用，授权后将获取您的微信用户信息（unionId）</view>
			<view class="bottom">
				<view class="item grant" @click="goLogin">微信授权登录</view>
			</view>
		</view>
		<view class="force-auth-mask" v-if="isShow" @click="close"></view>
	</view>
</template>
<script>
import Cache from '../utils/cache';
import { getLogo } from '../api/public';
import { LOGO_URL } from '../config/cache';

export default {
	name: 'RoutineForceAuth',
	props: {
		isShow: {
			type: Boolean,
			default: false
		}
	},
	data() {
		return {
			logoUrl: ''
		};
	},
	mounted() {
		if (Cache.has(LOGO_URL)) {
			this.logoUrl = Cache.get(LOGO_URL);
			return;
		}
		getLogo().then(res => {
			this.logoUrl = res.data.logo_url;
			Cache.set(LOGO_URL, this.logoUrl);
		});
	},
	methods: {
		goLogin() {
			this.$emit('close', false);
			uni.navigateTo({
				url: '/pages/users/wechat_login/index'
			});
		},
		close() {
			this.$emit('close', false);
		}
	}
};
</script>

<style scoped lang="scss">
.force-auth-popup {
	width: 500rpx;
	background-color: #fff;
	position: fixed;
	top: 50%;
	left: 50%;
	margin-left: -250rpx;
	transform: translateY(-50%);
	z-index: 320;
	border-radius: 12rpx;
	overflow: visible;
}
.force-auth-popup image {
	width: 150rpx;
	height: 150rpx;
	margin: -75rpx auto 0 auto;
	display: block;
	border: 8rpx solid #fff;
	border-radius: 50%;
	background-color: #fff;
}
.force-auth-popup .title {
	font-size: 30rpx;
	color: #000;
	text-align: center;
	margin-top: 30rpx;
	font-weight: bold;
}
.force-auth-popup .tip {
	font-size: 24rpx;
	color: #555;
	padding: 0 30rpx;
	margin-top: 25rpx;
	line-height: 1.6;
	text-align: center;
}
.force-auth-popup .bottom .item {
	width: 100%;
	height: 86rpx;
	background-color: var(--view-theme);
	text-align: center;
	line-height: 86rpx;
	font-size: 28rpx;
	color: #fff;
	font-weight: bold;
	margin-top: 50rpx;
	border-radius: 0;
}
.force-auth-mask {
	position: fixed;
	top: 0;
	right: 0;
	left: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.65);
	z-index: 310;
}
</style>
