<template>
	<view v-if="isPhoneBox">
		<view class="mobile-bg" @click="close"></view>
		<view class="mobile-mask animated" :class="{slideInUp:isUp}">
			<view class="info-box">
				<image :src="logoUrl"></image>
				<view class="title">{{ mode === 'bind' ? '手机号授权' : '获取授权' }}</view>
				<view class="txt">{{ mode === 'bind' ? '授权微信手机号，便于为您提供订单及售后服务' : '获取手机号授权' }}</view>
			</view>
			<button class="sub_btn" open-type="getPhoneNumber" @getphonenumber="getphonenumber">获取手机号</button>
		</view>
	</view>
</template>
<script>
	const app = getApp();
	import Routine from '@/libs/routine';
	import {
		loginMobile,
		registerVerify,
		getCodeApi,
		getUserInfo
	} from "@/api/user";
	import { getLogo, getUserPhone } from '@/api/public';
	import { updateUserPhone } from '@/api/user';
	export default{
		name:'routine_phone',
		props:{
			isPhoneBox:{
				type:Boolean,
				default:false,
			},
			logoUrl:{
				type:String,
				default:'',
			},
			authKey:{
				type:String,
				default:'',
			},
			// bind=已登录会员补绑手机号（会员中心授权弹框）；register=注册流程绑定（默认）
			mode:{
				type:String,
				default:'register',
			}
		},
		data(){
			return {
				keyCode:'',
				account:'',
				codeNum:'',
				isStatus:false
			}
		},
		mounted() {
		},
		methods:{
			// #ifdef MP
			// 小程序获取手机号码
			getphonenumber(e){
				if (e.detail.errMsg != 'getPhoneNumber:ok' && e.detail.errMsg != 'getphonenumber:ok') {
					return;
				}
				uni.showLoading({ title: '加载中' });
				Routine.getCode()
					.then(code => {
						if (this.mode === 'bind') {
							this.bindUserPhone(e.detail.encryptedData, e.detail.iv, code);
						} else {
							this.getUserPhoneNumber(e.detail.encryptedData, e.detail.iv, code);
						}
					})
					.catch(error => {
						uni.hideLoading();
					});
			},
			// 已登录会员绑定手机号（走 /api/front/user/update/phone）
			bindUserPhone(encryptedData, iv, code) {
				updateUserPhone({
					encryptedData: encryptedData,
					iv: iv,
					code: code,
					type: 'routine'
				})
					.then(res => {
						uni.hideLoading();
						this.isStatus = true;
						this.$util.Tips({
							title: '手机号授权成功',
							icon: 'success'
						});
						this.close();
					})
					.catch(res => {
						uni.hideLoading();
						this.$util.Tips({
							title: res
						});
					});
			},
			// 小程序获取手机号码回调
			getUserPhoneNumber(encryptedData, iv, code) {
				getUserPhone({
					encryptedData: encryptedData,
					iv: iv,
					code: code,
					key:this.authKey,
					type: 'routine'
				})
					.then(res => {
						this.$store.commit('LOGIN', {
							token: res.data.token
						});
						this.$store.commit("SETUID", res.data.uid);
						this.getUserInfo();
					})
					.catch(res => {
						uni.hideLoading();
						this.$util.Tips({
							title: res
						});
					});
			},
			/**
			 * 获取个人用户信息
			 */
			getUserInfo: function() {
				let that = this;
				getUserInfo().then(res => {
					uni.hideLoading();
					that.userInfo = res.data
					that.$store.commit("UPDATE_USERINFO", res.data);
					that.isStatus = true
					this.close()
				});
			},
			// #endif
			close(){
				this.$emit('close',{isStatus:this.isStatus})
			}
		}
	}
	
</script>

<style lang="scss">
	.mobile-bg{
		position: fixed;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		background: rgba(0,0,0,0.5);
	}
	.mobile-mask {
		z-index: 20;
		position: fixed;
		left: 0;
		bottom: 0;
		width: 100%;
		padding: 67rpx 30rpx;
		background: #fff;
		.info-box{
			display:flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			image{
				width: 150rpx;
				height: 150rpx;
				border-radius: 10rpx;
			}
			.title{
				margin-top: 30rpx;
				margin-bottom: 20rpx;
				font-size: 36rpx;
			}
			.txt{
				font-size: 30rpx;
				color: #868686;
			}
		}
		.sub_btn{
			width: 690rpx;
			height: 86rpx;
			line-height: 86rpx;
			margin-top: 60rpx;
			background: $theme-color;
			border-radius: 43rpx;
			color: #fff;
			font-size: 28rpx;
			text-align: center;
		}
	}
	.animated{
		animation-duration:.4s
	}
</style>