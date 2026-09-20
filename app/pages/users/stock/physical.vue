<template>
  <view class="physical-page">
    <!-- 渐变头 -->
    <view class="head-card">
      <view class="badge">实体库存</view>
      <view class="head-title">我的云仓库存</view>
      <view class="head-sub">已付款采购入仓 · 下级订货自动扣减</view>
    </view>

    <!-- 库存列表 -->
    <view class="card-list">
      <view v-for="(p, idx) in list" :key="idx" class="p-card">
        <image :src="p.image" class="p-img" mode="aspectFill" />
        <view class="p-info">
          <view class="p-name">{{ p.productName }}</view>
          <view class="p-meta">可供应数量 <text class="p-num">{{ p.num }}</text></view>
        </view>
        <button class="ex-btn" size="mini" @click="goExchange(p)">换货</button>
        <button class="sell-btn" size="mini" @click="openSell(p)">线下销售</button>
      </view>
    </view>

    <!-- 空态 -->
    <view v-if="!list.length && loaded" class="empty-box">
      <view class="empty-title">暂无实体库存</view>
      <view class="empty-sub">在商品中心选择「实体库存」下单，付款后总部发入云仓</view>
      <button class="go-btn" size="mini" @click="navGoods">去订货</button>
    </view>

    <!-- 线下销售弹层 -->
    <view v-if="showSell" class="sell-mask" @click="showSell = false">
      <view class="sell-pop" @click.stop>
        <view class="sell-pop-title">线下销售出库</view>
        <view class="sell-goods">{{ sellRow.productName }}</view>
        <view class="sell-row">
          <text class="sell-label">销售数量</text>
          <view class="num-ctrl">
            <text class="ctrl-btn" @click="minus">−</text>
            <input v-model="sellNum" type="number" class="num-input" />
            <text class="ctrl-btn" @click="plus">＋</text>
          </view>
        </view>
        <view class="sell-tip">当前可供 {{ sellRow.num }} 件，出库后即时扣减云仓库存</view>
        <input v-model="sellMark" class="mark-input" placeholder="备注（选填）" />
        <button class="sell-submit" size="mini" @click="submitSell">确认出库</button>
      </view>
    </view>

    <view class="bottom-tip">— 实体库存 —</view>
  </view>
</template>

<script>
	import { getMyPhysicalStock, sellOffline } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false,
				showSell: false,
				sellRow: {},
				sellNum: 1,
				sellMark: ''
			};
		},
		onLoad() {
			this.load();
		},
		methods: {
			load() {
				getMyPhysicalStock().then(res => {
					this.list = res.data || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			navGoods() {
				uni.navigateTo({ url: '/pages/users/stock/goods' });
			},
			goExchange(p) {
				// 换货数量由后端按原订单购买数量校验，这里固定带 1 件（带库存总量会被判超范围）
				uni.navigateTo({ url: '/pages/users/stock/exchange?productId=' + p.productId + '&num=1&type=1' });
			},
			openSell(p) {
				this.sellRow = p;
				this.sellNum = 1;
				this.sellMark = '';
				this.showSell = true;
			},
			plus() {
				const max = Number(this.sellRow.num || 0);
				if (this.sellNum >= max) return this.$util.Tips({ title: '不能超过可供应数量' });
				this.sellNum = Number(this.sellNum || 0) + 1;
			},
			minus() {
				if (this.sellNum <= 1) return;
				this.sellNum = Number(this.sellNum) - 1;
			},
			submitSell() {
				const num = Number(this.sellNum);
				if (!num || num <= 0) return this.$util.Tips({ title: '请填写销售数量' });
				if (num > Number(this.sellRow.num || 0)) return this.$util.Tips({ title: '超过可供应数量' });
				const that = this;
				uni.showModal({
					title: '确认出库',
					content: '将登记 ' + this.sellRow.productName + ' ×' + num + ' 的线下销售，并即时扣减云仓库存。',
					success: function(m) {
						if (!m.confirm) return;
						sellOffline({ productId: that.sellRow.productId, num: num, mark: that.sellMark }).then(() => {
							uni.showToast({ title: '出库成功，库存已扣减', icon: 'none' });
							that.showSell = false;
							that.load();
						});
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.physical-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 40rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1f5fc4 0%, #2b7de9 60%, #5aa7f5 100%);
  border-radius: 24rpx;
  padding: 34rpx 32rpx 30rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(43, 125, 233, 0.28);
  &::before {
    content: '';
    position: absolute;
    width: 220rpx; height: 220rpx; border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
    right: -70rpx; top: -90rpx;
  }
}
.badge {
  position: relative; z-index: 1;
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.head-title { position: relative; z-index: 1; margin-top: 16rpx; font-size: 38rpx; font-weight: 700; }
.head-sub { position: relative; z-index: 1; margin-top: 10rpx; font-size: 23rpx; opacity: 0.85; }

.card-list { margin-top: 8rpx; }
.p-card {
  background: #fff;
  border-radius: 20rpx;
  margin-top: 20rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.p-img { width: 120rpx; height: 120rpx; border-radius: 14rpx; flex-shrink: 0; background: #f5f6fa; }
.p-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
.p-name { font-size: 28rpx; color: #303133; font-weight: 600; line-height: 38rpx; }
.p-meta { font-size: 23rpx; color: #909399; margin-top: 10rpx; }
.p-num { color: #2b6fe3; font-weight: 700; font-size: 30rpx; margin-left: 6rpx; }
.sell-btn { background: linear-gradient(135deg, #1f5fc4, #2b7de9); color: #fff; border-radius: 999rpx; font-size: 24rpx; padding: 0 28rpx; flex-shrink: 0; }
.ex-btn { background: #fff; color: #2b6fe3; border: 1rpx solid #2b6fe3; border-radius: 999rpx; font-size: 24rpx; padding: 0 24rpx; flex-shrink: 0; margin-right: 12rpx; }

.empty-box { display: flex; flex-direction: column; align-items: center; padding: 110rpx 0 40rpx; }
.empty-title { font-size: 28rpx; color: #606266; font-weight: 600; }
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #b0b8c4; }
.go-btn { margin-top: 30rpx; background: #2b6fe3; color: #fff; border-radius: 999rpx; padding: 0 50rpx; }

.sell-mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 99; display: flex; align-items: center; justify-content: center; }
.sell-pop { width: 82%; background: #fff; border-radius: 24rpx; padding: 34rpx 32rpx 36rpx; }
.sell-pop-title { text-align: center; font-size: 30rpx; font-weight: 600; color: #303133; }
.sell-goods { margin-top: 12rpx; text-align: center; font-size: 25rpx; color: #606266; }
.sell-row { display: flex; align-items: center; justify-content: space-between; margin-top: 30rpx; }
.sell-label { font-size: 26rpx; color: #303133; }
.num-ctrl { display: flex; align-items: center; }
.ctrl-btn { width: 52rpx; height: 52rpx; background: #f2f3f5; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #333; }
.num-input { width: 90rpx; height: 52rpx; text-align: center; font-size: 28rpx; }
.sell-tip { margin-top: 16rpx; font-size: 22rpx; color: #909399; }
.mark-input { margin-top: 20rpx; background: #f5f6fa; border-radius: 12rpx; height: 72rpx; padding: 0 20rpx; font-size: 26rpx; }
.sell-submit { margin-top: 28rpx; width: 100%; background: linear-gradient(135deg, #1f5fc4, #2b7de9); color: #fff; border-radius: 999rpx; height: 76rpx; line-height: 76rpx; font-size: 28rpx; }

.bottom-tip { margin-top: 50rpx; text-align: center; font-size: 22rpx; color: #c3cad6; letter-spacing: 4rpx; }
</style>
