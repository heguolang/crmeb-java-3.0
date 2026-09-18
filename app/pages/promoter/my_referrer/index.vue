<template>
  <view class="referrer-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">我的推荐人</view>
      <view class="page-sub">{{ referrer.hasReferrer ? '邀请我加入的上级推广人' : '您还没有推荐人' }}</view>
    </view>

    <view class="page-body">
      <!-- 推荐人卡片 -->
      <view v-if="referrer.hasReferrer" class="referrer-card">
        <image v-if="referrer.avatar" :src="referrer.avatar" class="r-avatar" mode="aspectFill" />
        <view v-else class="r-avatar r-avatar-text">{{ (referrer.nickname || '?').slice(0, 1) }}</view>
        <view class="r-name">{{ referrer.nickname }}</view>
        <view class="r-uid">推荐人ID：{{ referrer.uid }}</view>
      </view>

      <!-- 空态 -->
      <view v-if="loaded && !referrer.hasReferrer" class="empty-card">
        <view class="empty-ico">荐</view>
        <view class="empty-txt">暂无推荐人</view>
        <view class="empty-sub">通过好友的推广名片注册并绑定后，推荐人会显示在这里</view>
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyReferrer } from '@/api/user.js';
	export default {
		data() {
			return {
				referrer: {},
				loaded: false
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			load() {
				getMyReferrer().then(res => {
					this.referrer = res.data || {};
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			}
		}
	};
</script>

<style lang="scss" scoped>
.referrer-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 96rpx;
  background: linear-gradient(160deg, #2b6fe3 0%, #4a9df8 70%, #6dadf9 100%);
  overflow: hidden;
  .top-deco { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.10); }
  .d1 { width: 240rpx; height: 240rpx; right: -70rpx; top: -100rpx; background: rgba(255,255,255,0.14); }
  .d2 { width: 130rpx; height: 130rpx; left: -50rpx; bottom: -30rpx; }
}
.page-title {
  position: relative;
  z-index: 1;
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.12);
}
.page-sub {
  position: relative;
  z-index: 1;
  margin-top: 12rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1rpx;
}

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -64rpx;
}

/* ---------- 推荐人卡片 ---------- */
.referrer-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 26rpx rgba(31, 45, 61, 0.06);
}
.r-avatar {
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: #e8f3ff;
  box-shadow: 0 0 0 6rpx #eef5ff, 0 10rpx 24rpx rgba(43, 111, 227, 0.18);
}
.r-avatar-text {
  color: #1a73e8;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.r-name {
  margin-top: 26rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #26324b;
}
.r-uid {
  margin-top: 14rpx;
  font-size: 25rpx;
  color: #909399;
  background: #f4f6f9;
  border-radius: 999rpx;
  padding: 8rpx 30rpx;
}

/* ---------- 空态白卡 ---------- */
.empty-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 90rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.empty-ico {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: #f0f6ff;
  color: #9cc3f5;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-txt { margin-top: 26rpx; font-size: 28rpx; color: #3d4a5f; font-weight: 600; }
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #a4adc0; text-align: center; line-height: 36rpx; }
</style>
