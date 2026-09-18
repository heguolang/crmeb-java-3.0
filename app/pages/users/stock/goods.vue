<template>
  <view class="stock-goods">
    <view class="search-bar">
      <input v-model="keywords" class="search-input" placeholder="搜索商品名称" confirm-type="search" @confirm="reload" />
    </view>
    <!-- 库存类型 -->
    <view class="type-bar">
      <view class="type-item" :class="{ active: stockType === 1 }" @click="switchType(1)">
        <view class="t-name">实体库存</view>
        <view class="t-sub">付款后发货到家</view>
      </view>
      <view class="type-item" :class="{ active: stockType === 2 }" @click="switchType(2)">
        <view class="t-name">虚拟库存</view>
        <view class="t-sub">付款即入账，可提货</view>
      </view>
    </view>
    <view v-for="item in list" :key="item.id" class="goods-card">
      <image :src="item.image" class="goods-img" mode="aspectFill" />
      <view class="goods-info">
        <view class="goods-name">{{ item.storeName }}</view>
        <view class="goods-price">
          <text class="my-price">¥{{ item.myPrice }}</text>
          <text class="retail-price">零售 ¥{{ item.price }}</text>
        </view>
        <view class="goods-stock">
          云仓库存：{{ item.skuKey ? item.skuStock : item.stock }}
          <text v-if="item.skus && item.skus.length" class="sku-chip" @click="chooseSku(item)">
            {{ item.skuName || '选规格' }} ▾
          </text>
        </view>
      </view>
      <view class="goods-op">
        <view class="num-ctrl">
          <text class="ctrl-btn" @click="minus(item)">−</text>
          <input v-model="item.buyNum" type="number" class="num-input" />
          <text class="ctrl-btn" @click="plus(item)">＋</text>
        </view>
        <button class="buy-btn" size="mini" @click="buy(item)">加入订货单</button>
      </view>
    </view>
    <view v-if="!list.length && loaded" class="empty">暂无订货商品</view>

    <!-- 收货地址（实体库存需要） -->
    <view class="addr-bar" v-if="stockType === 1" @click="showAddr = true">
      <view v-if="selectedAddr" class="addr-info">
        <view class="addr-line1">{{ selectedAddr.realName }} {{ selectedAddr.phone }}</view>
        <view class="addr-line2">{{ addrText(selectedAddr) }}</view>
      </view>
      <view v-else class="addr-empty">请选择收货地址</view>
      <text class="addr-arrow">›</text>
    </view>
    <view class="addr-bar v-bar" v-else>
      <view class="addr-empty">虚拟库存无需收货地址，付款后自动入账，可在会员中心提货</view>
    </view>

    <view class="cart-bar" v-if="cartItems.length">
      <view class="cart-info">
        已选 {{ cartItems.length }} 种 · 合计 <text class="cart-total">¥{{ cartTotal }}</text>
      </view>
      <button class="submit-btn" size="mini" @click="submitOrder">提交并支付</button>
    </view>

    <!-- 地址选择弹层 -->
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
  </view>
</template>

<script>
	import { getStockProducts, createStockOrder, payStockOrder } from '@/api/stock.js';
	import { getAddressList } from '@/api/user.js';
	export default {
		data() {
			return {
				keywords: '',
				list: [],
				loaded: false,
				cartItems: [],
				stockType: 1,
				addrList: [],
				selectedAddr: null,
				showAddr: false
			};
		},
		computed: {
			cartTotal() {
				let t = 0;
				this.cartItems.forEach(i => { t += i.myPrice * i.buyNum; });
				return t.toFixed(2);
			}
		},
		onLoad() {
			this.load();
			this.loadAddr();
		},
		onShow() {
			// 从新增地址页返回后刷新地址簿
			this.loadAddr();
		},
		methods: {
			switchType(t) {
				if (this.stockType === t) return;
				this.stockType = t;
				this.cartItems = [];
				this.list = [];
				this.load();
			},
			load() {
				getStockProducts({ keywords: this.keywords, page: 1, limit: 50, stockType: this.stockType }).then(res => {
					this.list = (res.data.list || []).map(i => ({ ...i, buyNum: 0 }));
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			loadAddr() {
				getAddressList({ page: 1, limit: 100 }).then(res => {
					// address/list 为分页结构，地址在 res.data.list
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
			reload() {
				this.load();
			},
			plus(item) {
				if (item.buyNum >= item.stock) return this.$util.Tips({ title: '已达库存上限' });
				this.$set(item, 'buyNum', Number(item.buyNum || 0) + 1);
				this.syncCart(item);
			},
			minus(item) {
				if (item.buyNum <= 0) return;
				this.$set(item, 'buyNum', Number(item.buyNum) - 1);
				this.syncCart(item);
			},
			syncCart(item) {
				const idx = this.cartItems.findIndex(c => c.id === item.id);
				if (item.buyNum > 0) {
					if (idx >= 0) this.cartItems.splice(idx, 1, item);
					else this.cartItems.push(item);
				} else if (idx >= 0) {
					this.cartItems.splice(idx, 1);
				}
			},
			chooseSku(item) {
				const skus = item.skus || [];
				if (!skus.length) return;
				const names = skus.map(s => (s.attrValue || s.skuKey) + '　拿货价 ¥' + s.myPrice + '（库存 ' + s.stock + '）');
				uni.showActionSheet({
					itemList: names,
					success: (res) => {
						const s = skus[res.tapIndex];
						this.$set(item, 'skuKey', s.skuKey);
						this.$set(item, 'skuName', s.attrValue || s.skuKey);
						this.$set(item, 'skuStock', s.stock);
						this.$set(item, 'myPrice', s.myPrice);
						this.syncCart(item);
					}
				});
			},
			buy(item) {
				if (!item.buyNum || item.buyNum <= 0) {
					this.$set(item, 'buyNum', 1);
					this.syncCart(item);
				} else {
					this.plus(item);
				}
			},
			submitOrder() {
				const isVirtual = this.stockType === 2;
				if (!isVirtual && !this.selectedAddr) return this.$util.Tips({ title: '请选择收货地址' });
				const items = this.cartItems.filter(i => i.buyNum > 0).map(i => ({ productId: i.id, num: Number(i.buyNum), skuKey: i.skuKey || '' }));
				if (!items.length) return this.$util.Tips({ title: '请先选择商品数量' });
				const tip = isVirtual
					? '共 ' + items.length + ' 种商品，合计 ¥' + this.cartTotal + '。付款后虚拟库存即时入账，后续可在【虚拟库存】中提货。'
					: '共 ' + items.length + ' 种商品，合计 ¥' + this.cartTotal + '。提交后需先完成付款，付款后进入审核/发货流程。';
				uni.showModal({
					title: '确认提交',
					content: tip,
					success: (m) => {
						if (!m.confirm) return;
						const payload = { items, stockType: this.stockType };
						if (!isVirtual) payload.addressId = this.selectedAddr.id;
						createStockOrder(payload).then(res => {
							const d = res.data || {};
							if (d.upSearchWaiting && d.upSearchMessage) {
								uni.showModal({
									title: '上级暂无库存',
									content: d.upSearchMessage,
									showCancel: false,
									success: () => this.choosePay(d.orderNo)
								});
							} else {
								this.choosePay(d.orderNo);
							}
						});
					}
				});
			},
			choosePay(orderNo) {
				uni.showActionSheet({
					itemList: ['余额支付', '微信支付'],
					success: (r) => {
						if (r.tapIndex === 0) this.payYue(orderNo);
						else this.payWeixin(orderNo);
					},
					fail: () => {
						uni.showToast({ title: '未支付，可在订单列表继续支付', icon: 'none' });
						setTimeout(() => { uni.navigateTo({ url: '/pages/users/stock/order-list?tab=waitPay' }); }, 800);
					}
				});
			},
			payYue(orderNo) {
				payStockOrder({ orderNo, payType: 'yue' }).then(() => {
					uni.showToast({ title: this.stockType === 2 ? '支付成功，虚拟库存已入账' : '支付成功', icon: 'none' });
					this.cartItems = [];
					this.list.forEach(i => { i.buyNum = 0; });
					setTimeout(() => {
						uni.navigateTo({ url: this.stockType === 2 ? '/pages/users/stock/virtual' : '/pages/users/stock/order-list' });
					}, 800);
				});
			},
			payWeixin(orderNo) {
				payStockOrder({ orderNo, payType: 'weixin', payChannel: 'routine' }).then(res => {
					const js = (res.data && res.data.jsConfig) || {};
					uni.requestPayment({
						provider: 'wxpay',
						appId: js.appId,
						nonceStr: js.nonceStr,
						package: js.packages,
						signType: js.signType || 'MD5',
						timeStamp: js.timeStamp,
						paySign: js.paySign,
						success: () => {
							uni.showToast({ title: this.stockType === 2 ? '支付成功，虚拟库存已入账' : '支付成功', icon: 'none' });
							this.cartItems = [];
							this.list.forEach(i => { i.buyNum = 0; });
							setTimeout(() => {
								uni.navigateTo({ url: this.stockType === 2 ? '/pages/users/stock/virtual' : '/pages/users/stock/order-list' });
							}, 800);
						},
						fail: () => {
							uni.showToast({ title: '支付未完成，可在订单列表继续支付', icon: 'none' });
							setTimeout(() => { uni.navigateTo({ url: '/pages/users/stock/order-list?tab=waitPay' }); }, 800);
						}
					});
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.stock-goods { min-height: 100vh; background: #f5f6f8; padding: 24rpx 24rpx 200rpx; }
.search-bar { margin-bottom: 20rpx; }
.search-input { background: #fff; border-radius: 40rpx; height: 72rpx; padding: 0 30rpx; font-size: 26rpx; }
.type-bar { display: flex; margin-bottom: 20rpx; }
.type-item {
	flex: 1; background: #fff; border-radius: 16rpx; padding: 20rpx 24rpx; margin-right: 16rpx;
	border: 2rpx solid transparent;
	&:last-child { margin-right: 0; }
	&.active { border-color: #2b6fe3; background: #f0f6ff; }
}
.t-name { font-size: 28rpx; color: #303133; font-weight: 600; }
.type-item.active .t-name { color: #2b6fe3; }
.t-sub { font-size: 22rpx; color: #909399; margin-top: 6rpx; }
.goods-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; display: flex; align-items: center; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; flex-shrink: 0; }
.goods-info { flex: 1; margin: 0 20rpx; overflow: hidden; }
.goods-name { font-size: 28rpx; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.goods-price { margin-top: 8rpx; }
.my-price { color: #e93323; font-size: 32rpx; font-weight: 600; margin-right: 14rpx; }
.retail-price { color: #999; font-size: 22rpx; text-decoration: line-through; }
.goods-stock { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.sku-chip { margin-left: 12rpx; color: #2b6fe3; border: 1rpx solid #2b6fe3; border-radius: 999rpx; padding: 0 14rpx; font-size: 21rpx; }
.goods-op { display: flex; flex-direction: column; align-items: flex-end; }
.num-ctrl { display: flex; align-items: center; margin-bottom: 12rpx; }
.ctrl-btn { width: 48rpx; height: 48rpx; background: #f2f3f5; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #333; }
.num-input { width: 70rpx; height: 48rpx; text-align: center; font-size: 26rpx; }
.buy-btn { background: #2b6fe3; color: #fff; font-size: 24rpx; border-radius: 30rpx; }
.addr-bar {
	position: fixed; left: 0; right: 0; bottom: 110rpx; background: #fff;
	padding: 20rpx 30rpx; display: flex; align-items: center; justify-content: space-between;
	box-shadow: 0 -4rpx 20rpx rgba(0,0,0,.06);
}
.addr-bar.v-bar { cursor: default; }
.addr-bar.v-bar .addr-empty { line-height: 36rpx; }
.addr-info { flex: 1; overflow: hidden; }
.addr-empty { font-size: 26rpx; color: #999; }
.addr-line1 { font-size: 26rpx; color: #303133; font-weight: 600; }
.addr-line2 { font-size: 23rpx; color: #909399; margin-top: 4rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.addr-arrow { font-size: 40rpx; color: #c0c4cc; margin-left: 16rpx; }
.cart-bar {
	position: fixed; left: 0; right: 0; bottom: 0; background: #fff; padding: 20rpx 30rpx;
	display: flex; align-items: center; justify-content: space-between; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,.06);
}
.cart-info { font-size: 26rpx; color: #666; }
.cart-total { color: #e93323; font-size: 34rpx; font-weight: 600; }
.submit-btn { background: #e93323; color: #fff; border-radius: 40rpx; font-size: 28rpx; padding: 0 50rpx; }
.addr-mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 99; display: flex; align-items: flex-end; }
.addr-pop { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 30rpx 30rpx 40rpx; }
.addr-pop-title { text-align: center; font-size: 30rpx; font-weight: 600; color: #303133; margin-bottom: 20rpx; }
.addr-pop-list { max-height: 50vh; }
.addr-item { padding: 20rpx 16rpx; border-bottom: 1rpx solid #f0f2f6; border-radius: 12rpx; }
.addr-item.active { background: #f0f6ff; }
.addr-default { font-size: 20rpx; color: #2b6fe3; background: #ecf3ff; border-radius: 6rpx; padding: 2rpx 10rpx; margin-left: 10rpx; }
.addr-none { text-align: center; color: #999; font-size: 26rpx; padding: 60rpx 0; }
.addr-add-btn { margin-top: 20rpx; background: #2b6fe3; color: #fff; border-radius: 40rpx; width: 100%; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
