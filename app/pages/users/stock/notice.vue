<template>
  <view class="notice-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">消息通知</view>
      <view class="page-sub">{{ unreadCount ? ('您有 ' + unreadCount + ' 条未读消息，点击卡片可标记已读') : '消息已全部读完' }}</view>
      <view class="top-actions">
        <view v-if="unreadCount" class="readall-btn" :class="{ doing: readingAll }" @click.stop="readAll">
          {{ readingAll ? '处理中…' : '一键已读' }}
        </view>
      </view>
    </view>

    <view class="page-body">
      <view v-if="list.length" class="notice-list">
        <view v-for="n in list" :key="n.id" class="notice-card" :class="{ unread: !n.isRead }" @click="read(n)">
          <view class="n-ico" :class="'t' + n.type">{{ typeIcon(n.type) }}</view>
          <view class="n-main">
            <view class="n-line">
              <view class="n-title">
                {{ n.title }}
                <text v-if="!n.isRead" class="dot"></text>
              </view>
              <text class="n-type" :class="'t' + n.type">{{ typeName(n.type) }}</text>
            </view>
            <view class="n-content">{{ n.content }}</view>
            <view class="n-time">{{ n.createTime }}</view>
          </view>
        </view>
      </view>

      <view v-if="!list.length && loaded" class="empty-card">
        <view class="empty-ico">信</view>
        <view class="empty-txt">暂无消息</view>
        <view class="empty-sub">订单审核、发货、奖金到账通知都会在这里提醒您</view>
      </view>
    </view>
  </view>
</template>

<script>
	import { getStockNotices, readStockNotice, readAllStockNotices } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false,
				readingAll: false
			};
		},
		computed: {
			unreadCount() {
				return this.list.filter(n => !n.isRead).length;
			}
		},
		onShow() {
			this.load();
		},
		methods: {
			typeName(t) {
				return { 1: '订单审核', 2: '发货通知', 3: '奖金到账', 4: '提现审核' }[t] || '通知';
			},
			typeIcon(t) {
				return { 1: '审', 2: '发', 3: '奖', 4: '提' }[t] || '信';
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
			},
			readAll() {
				if (this.readingAll || !this.unreadCount) return;
				this.readingAll = true;
				readAllStockNotices().then(() => {
					this.list.forEach(n => { n.isRead = 1; });
					uni.showToast({ title: '已全部标记为已读', icon: 'none' });
					this.readingAll = false;
				}).catch(() => { this.readingAll = false; });
			}
		}
	};
</script>

<style lang="scss" scoped>
.notice-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 86rpx;
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
  color: rgba(255, 255, 255, 0.88);
  letter-spacing: 1rpx;
}
/* 一键已读：白底描边胶囊，跟随文档流右对齐（不能用绝对定位——正文 margin-top 上移会盖住它，导致点不到） */
.top-actions {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-end;
  margin-top: 22rpx;
}
.readall-btn {
  background: rgba(255, 255, 255, 0.92);
  color: #2b6fe3;
  font-size: 23rpx;
  font-weight: 700;
  border-radius: 999rpx;
  padding: 10rpx 30rpx;
  box-shadow: 0 6rpx 16rpx rgba(10, 31, 78, 0.18);
  &.doing { opacity: 0.6; }
}

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -56rpx;
}

/* ---------- 消息卡片 ---------- */
.notice-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 24rpx;
  margin-bottom: 18rpx;
  display: flex;
  align-items: flex-start;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
  /* 未读：蓝色描边 + 浅蓝底，已读恢复白卡 */
  &.unread {
    border: 2rpx solid #9cc3f5;
    background: #f7fbff;
    box-shadow: 0 6rpx 18rpx rgba(43, 111, 227, 0.10);
  }
}
/* 类型图标：彩色渐变圆底白字 */
.n-ico {
  width: 76rpx;
  height: 76rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  &.t1 { background: linear-gradient(135deg, #5aa7f8, #2b6fe3); box-shadow: 0 6rpx 14rpx rgba(43, 111, 227, 0.28); }
  &.t2 { background: linear-gradient(135deg, #43cf7d, #18a852); box-shadow: 0 6rpx 14rpx rgba(24, 168, 82, 0.26); }
  &.t3 { background: linear-gradient(135deg, #f6cd60, #e0a213); box-shadow: 0 6rpx 14rpx rgba(224, 162, 19, 0.26); }
  &.t4 { background: linear-gradient(135deg, #ffa25e, #ff7a45); box-shadow: 0 6rpx 14rpx rgba(255, 122, 69, 0.26); }
}
.n-main { flex: 1; margin-left: 20rpx; overflow: hidden; }
.n-line { display: flex; justify-content: space-between; align-items: center; }
.n-title {
  font-size: 28rpx;
  color: #26324b;
  font-weight: 700;
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin-right: 14rpx;
}
/* 未读红点 */
.dot {
  flex-shrink: 0;
  width: 14rpx;
  height: 14rpx;
  background: #e93323;
  border-radius: 50%;
  margin-left: 10rpx;
  box-shadow: 0 0 0 5rpx rgba(233, 51, 35, 0.14);
}
/* 类型签：浅底彩字圆角胶囊 */
.n-type {
  flex-shrink: 0;
  font-size: 21rpx;
  font-weight: 600;
  border-radius: 999rpx;
  padding: 4rpx 16rpx;
  &.t1 { color: #2b6fe3; background: #ecf3ff; }
  &.t2 { color: #18a852; background: #e9f9ec; }
  &.t3 { color: #d48806; background: #fff4e0; }
  &.t4 { color: #ff7a45; background: #fff1e8; }
}
.n-content {
  font-size: 25rpx;
  color: #606266;
  margin-top: 12rpx;
  line-height: 38rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}
.n-time { font-size: 22rpx; color: #a4adc0; margin-top: 12rpx; }

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
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #a4adc0; }
</style>
