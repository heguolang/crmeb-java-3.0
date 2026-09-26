<template>
	<view class="profile-setup" :data-theme="theme">
		<view class="setup-box">
			<view class="setup-title">完善个人信息</view>
			<view class="setup-desc">设置头像和昵称，完成后进入商城</view>
			<form @submit="formSubmit">
				<view class="avatar-row">
					<button class="avatar-wrapper" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
						<image class="avatar" :src="newAvatar || defaultAvatar" mode="aspectFill"></image>
					</button>
					<view class="avatar-tip">点击选择头像</view>
				</view>
				<view class="nick-row">
					<view class="nick-label">昵称</view>
					<view class="nick-input-box">
						<input class="nick-input" type="nickname" name="nickname" :value="nickname"
							placeholder="请输入昵称" placeholder-class="nick-placeholder" maxlength="20" />
					</view>
				</view>
				<button class="save-btn bg-color" form-type="submit">保存并进入商城</button>
			</form>
			<view class="skip-btn" @click="skip">暂不设置，直接进入</view>
		</view>
	</view>
</template>

<script>
	const app = getApp();
	import {
		userEdit
	} from '@/api/user.js';
	import {
		mapGetters
	} from "vuex";
	export default {
		data() {
			return {
				theme: app.globalData.theme,
				newAvatar: '', //选择并上传后的头像地址
				nickname: '',
				defaultAvatar: `${this.$Cache.get("imgHost") || ''}qianxuimage/perset/staticImg/f.png`
			};
		},
		computed: mapGetters(['userInfo']),
		onLoad() {
			this.nickname = this.userInfo.nickname || '';
			//老用户已有头像则带出
			if (this.userInfo.avatar) {
				this.newAvatar = this.userInfo.avatar;
			}
		},
		methods: {
			/**
			 * 小程序端选择头像并上传(微信官方 chooseAvatar 能力)
			 */
			onChooseAvatar(e) {
				const {
					avatarUrl
				} = e.detail;
				uni.showLoading({
					title: '上传中...'
				});
				this.$util.uploadImgs(avatarUrl, {
					url: 'upload/image',
					name: 'multipart',
					model: 'user',
					pid: 7
				}, (res) => {
					this.newAvatar = res.data.url;
					uni.hideLoading();
				}, (err) => {
					uni.hideLoading();
					this.$util.Tips({
						title: '头像上传失败'
					});
				});
			},
			/**
			 * 保存头像昵称
			 */
			formSubmit(e) {
				let that = this,
					value = e.detail.value || {};
				let nickname = (value.nickname || that.nickname || '').trim();
				if (!nickname) return that.$util.Tips({
					title: '请输入昵称'
				});
				let avatar = that.newAvatar || that.userInfo.avatar || '';
				uni.showLoading({
					title: '保存中...'
				});
				userEdit({
					avatar: avatar,
					nickname: nickname
				}).then(res => {
					uni.hideLoading();
					//同步本地用户信息，个人中心直接显示新资料
					that.$store.commit("UPDATE_USERINFO", Object.assign({}, that.userInfo, {
						avatar: avatar,
						nickname: nickname
					}));
					that.$util.Tips({
						title: '保存成功',
						icon: 'success'
					});
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/index/index'
						});
					}, 1500);
				}).catch(msg => {
					uni.hideLoading();
					return that.$util.Tips({
						title: msg || '保存失败'
					});
				});
			},
			/**
			 * 跳过
			 */
			skip() {
				uni.reLaunch({
					url: '/pages/index/index'
				});
			}
		}
	}
</script>

<style lang="scss" scoped>
	.profile-setup {
		min-height: 100vh;
		background: #fff;
	}

	.setup-box {
		padding: 80rpx 60rpx 0;
	}

	.setup-title {
		font-size: 44rpx;
		font-weight: 600;
		color: #333;
		text-align: center;
	}

	.setup-desc {
		margin-top: 20rpx;
		font-size: 28rpx;
		color: #999;
		text-align: center;
	}

	.avatar-row {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: 90rpx;
	}

	.avatar-wrapper {
		width: 160rpx;
		height: 160rpx;
		padding: 0;
		margin: 0;
		border-radius: 50%;
		overflow: hidden;
		background: #F5F5F5;

		&::after {
			border: none;
		}
	}

	.avatar {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
	}

	.avatar-tip {
		margin-top: 20rpx;
		font-size: 24rpx;
		color: #BBBBBB;
	}

	.nick-row {
		display: flex;
		align-items: center;
		margin-top: 80rpx;
		height: 100rpx;
		border-bottom: 2rpx solid #F0F0F0;
	}

	.nick-label {
		width: 140rpx;
		font-size: 30rpx;
		color: #333;
	}

	.nick-input-box {
		flex: 1;
		height: 100%;
	}

	.nick-input {
		width: 100%;
		height: 100%;
		font-size: 30rpx;
		color: #333;
	}

	.nick-placeholder {
		color: #BBBBBB;
	}

	.save-btn {
		width: 100%;
		height: 86rpx;
		line-height: 86rpx;
		margin-top: 100rpx;
		border-radius: 120rpx;
		font-size: 32rpx;
		color: #fff;

		&::after {
			border: none;
		}
	}

	.skip-btn {
		margin-top: 40rpx;
		font-size: 28rpx;
		color: #999;
		text-align: center;
		padding-bottom: 40rpx;
	}
</style>
