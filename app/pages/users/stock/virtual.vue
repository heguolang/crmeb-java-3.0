<template>
  <view class="virtual-page">
    <!-- 渐变头 -->
    <view class="head-card">
      <view class="badge">虚拟库存</view>
      <view class="head-title">我的虚拟库存</view>
      <view class="head-sub">付款即入账 · 随时提货 · 总部直发</view>
    </view>

    <!-- 库存列表 -->
    <view class="card-list">
      <view v-for="v in list" :key="v.id" class="v-card">
        <image :src="v.image" class="v-img" mode="aspectFill" />
        <view class="v-info">
          <view class="v-name">{{ v.productName }}</view>
          <view class="v-meta">累计入账 {{ v.num }} · 剩余可提 <text class="v-remain">{{ v.remainNum }}</text></view>
          <view class="v-op">
            <view class="num-ctrl">
              <text class="ctrl-btn" @click="minus(v)">−</text>
              <input v-model="v.pickNum" type="number" class="num-input" />
              <text class="ctrl-btn" @click="plus(v)">＋</text>
            </view>
            <button class="ex-btn" size="mini" @click="goExchange(v)">换货</button>
            <button class="pickup-btn" size="mini" @click="pickup(v)">提货</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <view v-if="!list.length && loaded" class="empty-box">
      <view class="empty-title">暂无虚拟库存</view>
      <view class="empty-sub">在商品中心选择「虚拟库存」下单，付款后自动入账</view>
      <button class="go-btn" size="mini" @click="navGoods">去订货</button>
    </view>

    <!-- 收货地址弹层 -->
    <view v-if="showAddr" class="addr-mask" @click="showAddr = false">
      <view class="addr-pop" @click.stop>
        <view class="addr-pop-title">选择收货地址</view>
        <scroll-view scroll-y class="addr-pop-list">
          <view v-for="a in addrList" :key="a.id" class="addr-item" :class="{ active: selectedAddr && selectedAddr.id === a.id }" @click="pickAddr(a)">
            <view class="addr-line1">{{ a.realName }} {{ a.phone }} <text v-if="a.isDefault" class="addr-default">默认</text></view>
            <view class="addr-line2">{{ addrText(a) }}</view>
          </view>
          <view v-if="!addrList.length" class="addr-none">暂无收货地址</view>
        </scroll-view>
        <button class="addr-add-btn" size="mini" @click="goAddAddress">＋ 新增收货地址</button>
      </view>
    </view>

    <view class="bottom-tip">— 虚拟库存 —</view>
  </view>
</template>

<script>
	import { getMyVirtualStock, pickupVirtual } from '@/api/stock.js';
	import { getAddressList } from '@/api/user.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false,
				addrList: [],
				selectedAddr: null,
				showAddr: false
			};
		},
		onLoad() {
			this.load();
			this.loadAddr();
		},
		onShow() {
			this.loadAddr();
		},
		methods: {
			load() {
				getMyVirtualStock().then(res => {
					this.list = (res.data || []).map(v => ({ ...v, pickNum: 1 }));
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			loadAddr() {
				getAddressList({ page: 1, limit: 100 }).then(res => {
					const d = res.data || {};
					this.addrList = Array.isArray(d) ? d : (d.list || []);
					if (!this.selectedAddr && this.addrList.length) {
						this.selectedAddr = this.addrList.find(a => a.isDefault) || this.addrList[0];
					}
				}).catch(() => {});
			},
			addrText(a) {
				return [a.province, a.city, a.district, a.detail].filter(Boolean).join('');
			},
			pickAddr(a) {
				this.selectedAddr = a;
				this.showAddr = false;
			},
			goAddAddress() {
				this.showAddr = false;
				uni.navigateTo({ url: '/pages/users/user_address/index' });
			},
			navGoods() {
				uni.navigateTo({ url: '/pages/users/stock/goods' });
			},
			goExchange(v) {
				uni.navigateTo({ url: '/pages/users/stock/exchange?productId=' + v.productId + '&skuKey=' + (v.skuKey || '') + '&num=' + (v.remainNum || 1) + '&type=2' });
			},
			plus(v) {
				if (v.pickNum >= v.remainNum) return this.$util.Tips({ title: '已达可提数量上限' });
				this.$set(v, 'pickNum', Number(v.pickNum || 0) + 1);
			},
			minus(v) {
				if (v.pickNum <= 1) return;
				this.$set(v, 'pickNum', Number(v.pickNum) - 1);
			},
			pickup(v) {
				const num = Number(v.pickNum);
				if (!num || num <= 0) return this.$util.Tips({ title: '请填写提货数量' });
				if (num > v.remainNum) return this.$util.Tips({ title: '超过可提数量' });
				if (!this.selectedAddr) {
					this.showAddr = true;
					return this.$util.Tips({ title: '请先选择收货地址' });
				}
				uni.showModal({
					title: '确认提货',
					content: '将 ' + v.productName + ' ×' + num + ' 提货为实物，由总部直接发货到您的收货地址。',
					success: (m) => {
						if (!m.confirm) return;
						pickupVirtual({ virtualId: v.id, num, addressId: this.selectedAddr.id }).then(() => {
							uni.showToast({ title: '提货单已提交，等待总部发货', icon: 'none' });
							this.load();
						});
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.virtual-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 40rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #b8860b 0%, #daa520 60%, #f0c060 100%);
  border-radius: 24rpx;
  padding: 34rpx 32rpx 30rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(218, 165, 32, 0.28);
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
.v-card {
  background: #fff;
  border-radius: 20rpx;
  margin-top: 20rpx;
  padding: 26rpx;
  display: flex;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.06);
}
.v-img { width: 140rpx; height: 140rpx; border-radius: 14rpx; flex-shrink: 0; background: #f5f6fa; }
.v-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
.v-name { font-size: 28rpx; color: #303133; font-weight: 600; line-height: 38rpx; }
.v-meta { font-size: 23rpx; color: #909399; margin-top: 8rpx; }
.v-remain { color: #e93323; font-weight: 700; font-size: 27rpx; }
.v-op { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; }
.num-ctrl { display: flex; align-items: center; }
.ctrl-btn { width: 48rpx; height: 48rpx; background: #f2f3f5; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #333; }
.num-input { width: 80rpx; height: 48rpx; text-align: center; font-size: 26rpx; }
.pickup-btn { background: linear-gradient(135deg, #b8860b, #daa520); color: #fff; border-radius: 999rpx; font-size: 24rpx; padding: 0 40rpx; }
.ex-btn { background: #fff; color: #2b6fe3; border: 1rpx solid #2b6fe3; border-radius: 999rpx; font-size: 24rpx; padding: 0 24rpx; margin-right: 12rpx; }

.empty-box { display: flex; flex-direction: column; align-items: center; padding: 110rpx 0 40rpx; }
.empty-title { font-size: 28rpx; color: #606266; font-weight: 600; }
.empty-sub { margin-top: 10rpx; font-size: 23rpx; color: #b0b8c4; }
.go-btn { margin-top: 30rpx; background: #2b6fe3; color: #fff; border-radius: 999rpx; padding: 0 50rpx; }

.addr-mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 99; display: flex; align-items: flex-end; }
.addr-pop { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 30rpx 30rpx 40rpx; }
.addr-pop-title { text-align: center; font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 20rpx; }
.addr-pop-list { max-height: 50vh; }
.addr-item { padding: 20rpx 16rpx; border-bottom: 1rpx solid #f0f2f6; border-radius: 12rpx; }
.addr-item.active { background: #f0f6ff; }
.addr-default { font-size: 20rpx; color: #2b6fe3; background: #ecf3ff; border-radius: 6rpx; padding: 2rpx 10rpx; margin-left: 10rpx; }
.addr-line1 { font-size: 26rpx; color: #303133; font-weight: 600; }
.addr-line2 { font-size: 23rpx; color: #909399; margin-top: 4rpx; }
.addr-none { text-align: center; color: #999; font-size: 26rpx; padding: 60rpx 0; }
.addr-add-btn { margin-top: 20rpx; background: #2b6fe3; color: #fff; border-radius: 40rpx; width: 100%; }

.bottom-tip { margin-top: 50rpx; text-align: center; font-size: 22rpx; color: #c3cad6; letter-spacing: 4rpx; }
</style>
