<template>
  <view class="stock-goods">
    <view class="search-bar">
      <input v-model="keywords" class="search-input" placeholder="搜索商品名称" confirm-type="search" @confirm="reload" />
    </view>
    <view v-for="item in list" :key="item.id" class="goods-card">
      <image :src="item.image" class="goods-img" mode="aspectFill" />
      <view class="goods-info">
        <view class="goods-name">{{ item.storeName }}</view>
        <view class="goods-price">
          <text class="my-price">¥{{ item.myPrice }}</text>
          <text class="retail-price">零售 ¥{{ item.price }}</text>
        </view>
        <view class="goods-stock">云仓库存：{{ item.stock }}</view>
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

    <view class="cart-bar" v-if="cartItems.length">
      <view class="cart-info">
        已选 {{ cartItems.length }} 种 · 合计 <text class="cart-total">¥{{ cartTotal }}</text>
      </view>
      <button class="submit-btn" size="mini" @click="submitOrder">提交订货单</button>
    </view>
  </view>
</template>

<script>
	import { getStockProducts, createStockOrder } from '@/api/stock.js';
	export default {
		data() {
			return {
				keywords: '',
				list: [],
				loaded: false,
				cartItems: []
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
		},
		methods: {
			load() {
				getStockProducts({ keywords: this.keywords, page: 1, limit: 50 }).then(res => {
					this.list = (res.data.list || []).map(i => ({ ...i, buyNum: 0 }));
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
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
			buy(item) {
				if (!item.buyNum || item.buyNum <= 0) {
					this.$set(item, 'buyNum', 1);
					this.syncCart(item);
				} else {
					this.plus(item);
				}
			},
			submitOrder() {
				const items = this.cartItems.filter(i => i.buyNum > 0).map(i => ({ productId: i.id, num: Number(i.buyNum) }));
				if (!items.length) return this.$util.Tips({ title: '请先选择商品数量' });
				uni.showModal({
					title: '确认提交',
					content: '共 ' + items.length + ' 种商品，合计 ¥' + this.cartTotal + '。提交后由直接上级审核。',
					success: (m) => {
						if (!m.confirm) return;
						createStockOrder({ items, payType: 2 }).then(() => {
							this.$util.Tips({ title: '订货单已提交，等待上级审核' });
							this.cartItems = [];
							this.list.forEach(i => { i.buyNum = 0; });
							setTimeout(() => { uni.navigateTo({ url: '/pages/users/stock/order-list' }); }, 800);
						});
					}
				});
			}
		}
	};
</script>

<style lang="scss" scoped>
.stock-goods { min-height: 100vh; background: #f5f6f8; padding: 24rpx 24rpx 140rpx; }
.search-bar { margin-bottom: 20rpx; }
.search-input { background: #fff; border-radius: 40rpx; height: 72rpx; padding: 0 30rpx; font-size: 26rpx; }
.goods-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; display: flex; align-items: center; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; flex-shrink: 0; }
.goods-info { flex: 1; margin: 0 20rpx; overflow: hidden; }
.goods-name { font-size: 28rpx; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.goods-price { margin-top: 8rpx; }
.my-price { color: #e93323; font-size: 32rpx; font-weight: 600; margin-right: 14rpx; }
.retail-price { color: #999; font-size: 22rpx; text-decoration: line-through; }
.goods-stock { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.goods-op { display: flex; flex-direction: column; align-items: flex-end; }
.num-ctrl { display: flex; align-items: center; margin-bottom: 12rpx; }
.ctrl-btn { width: 48rpx; height: 48rpx; background: #f2f3f5; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; color: #333; }
.num-input { width: 70rpx; height: 48rpx; text-align: center; font-size: 26rpx; }
.buy-btn { background: #2b6fe3; color: #fff; font-size: 24rpx; border-radius: 30rpx; }
.cart-bar {
	position: fixed; left: 0; right: 0; bottom: 0; background: #fff; padding: 20rpx 30rpx;
	display: flex; align-items: center; justify-content: space-between; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,.06);
}
.cart-info { font-size: 26rpx; color: #666; }
.cart-total { color: #e93323; font-size: 34rpx; font-weight: 600; }
.submit-btn { background: #e93323; color: #fff; border-radius: 40rpx; font-size: 28rpx; padding: 0 50rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>
