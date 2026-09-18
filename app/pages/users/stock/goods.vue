<template>
  <view class="stock-goods">
    <!-- 顶部：蓝色渐变区（搜索 + 库存类型） -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="search-bar">
        <view class="s-ico"></view>
        <input v-model="keywords" class="search-input" placeholder="搜索商品名称" confirm-type="search" @confirm="reload" />
      </view>
      <!-- 库存类型 -->
      <view class="type-bar">
        <view class="type-item" :class="{ active: stockType === 1 }" @click="switchType(1)">
          <view class="t-name">实体库存</view>
          <view class="t-sub">付款后发货到家</view>
          <view v-if="stockType === 1" class="t-check"></view>
        </view>
        <view class="type-item" :class="{ active: stockType === 2 }" @click="switchType(2)">
          <view class="t-name">虚拟库存</view>
          <view class="t-sub">付款即入账，可提货</view>
          <view v-if="stockType === 2" class="t-check"></view>
        </view>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="goods-list">
      <view v-for="item in list" :key="item.id" class="goods-card">
        <image :src="item.image" class="goods-img" mode="aspectFill" />
        <view class="goods-info">
          <view class="goods-name">{{ item.storeName }}</view>
          <view class="price-row">
            <text class="p-cny">¥</text>
            <text class="my-price">{{ item.myPrice }}</text>
            <text class="zhuan-tag">专价</text>
            <text class="retail-price">零售 ¥{{ item.price }}</text>
          </view>
          <view class="stock-row">
            <text>云仓库存</text>
            <text class="stock-num">{{ item.skuKey ? item.skuStock : item.stock }}</text>
            <text v-if="item.skus && item.skus.length > 1" class="sku-chip" @click="openSku(item)">
              {{ item.skuName || '选择规格' }}
            </text>
          </view>
          <view class="op-row">
            <view class="num-ctrl">
              <text class="ctrl-btn" :class="{ off: !item.buyNum }" @click="minus(item)">−</text>
              <input v-model="item.buyNum" type="number" class="num-input" />
              <text class="ctrl-btn" @click="plus(item)">＋</text>
            </view>
            <button class="buy-btn" @click="buy(item)">加入订货单</button>
          </view>
        </view>
      </view>
      <view v-if="!list.length && loaded" class="empty">暂无订货商品</view>
    </view>

    <!-- 规格选择弹窗 -->
    <view v-if="skuVisible" class="sku-mask" @click="closeSku">
      <view class="sku-pop" @click.stop>
        <view class="sku-top">
          <image :src="skuRow.image" class="sku-img" mode="aspectFill" />
          <view class="sku-top-info">
            <view class="sku-name">{{ skuRow.storeName }}</view>
            <view class="sku-price-row">
              <text class="sku-price">¥{{ skuRow.myPrice }}</text>
              <text class="sku-retail">零售 ¥{{ skuRow.price }}</text>
            </view>
            <view class="sku-stock">库存 {{ skuRow.skuKey ? (skuRow.skuStock || 0) : skuRow.stock }} 件</view>
            <view class="sku-chosen">已选：{{ skuRow.skuName || '请选择规格' }}</view>
          </view>
          <text class="sku-close" @click="closeSku">✕</text>
        </view>
        <scroll-view scroll-y class="sku-body">
          <view v-for="(g, gi) in skuGroups" :key="gi" class="sku-group">
            <view class="sku-group-name">{{ g.name }}</view>
            <view class="sku-values">
              <view v-for="(v, vi) in g.values" :key="vi" class="sku-value"
                    :class="{ active: skuPicked[g.name] === v }" @click="pickAttr(g.name, v)">{{ v }}</view>
            </view>
          </view>
          <view class="sku-group">
            <view class="sku-group-name">数量</view>
            <view class="num-ctrl lg">
              <text class="ctrl-btn" @click="minusSku">−</text>
              <input v-model="skuQty" type="number" class="num-input" />
              <text class="ctrl-btn" @click="plusSku">＋</text>
            </view>
          </view>
        </scroll-view>
        <button class="sku-confirm" @click="confirmSku">加入订货单</button>
      </view>
    </view>

    <!-- 收货地址（实体库存需要） -->
    <view class="addr-bar" :class="{ raised: cartItems.length }" v-if="stockType === 1" @click="showAddr = true">
      <view class="addr-ico"></view>
      <view v-if="selectedAddr" class="addr-info">
        <view class="addr-line1">
          <text class="addr-tag">收货地址</text>
          <text class="addr-who">{{ selectedAddr.realName }} {{ selectedAddr.phone }}</text>
        </view>
        <view class="addr-line2">{{ addrText(selectedAddr) }}</view>
      </view>
      <view v-else class="addr-info">
        <view class="addr-line1"><text class="addr-who">请选择收货地址</text></view>
        <view class="addr-line2">实体库存订货需先填写收货信息</view>
      </view>
      <view class="addr-switch">{{ selectedAddr ? '切换' : '去填写' }}<text class="addr-arrow">›</text></view>
    </view>
    <view class="addr-bar v-bar" :class="{ raised: cartItems.length }" v-else>
      <view class="v-ico">i</view>
      <view class="addr-empty">虚拟库存无需收货地址，付款后自动入账，可在会员中心提货</view>
    </view>

    <view class="cart-bar" v-if="cartItems.length">
      <view class="cart-info">
        已选 {{ cartItems.length }} 种 · 合计 <text class="cart-total">¥{{ cartTotal }}</text>
      </view>
      <button class="submit-btn" @click="submitOrder">提交并支付</button>
    </view>

    <!-- 地址选择弹层 -->
    <view v-if="showAddr" class="addr-mask" @click="showAddr = false">
      <view class="addr-pop" @click.stop>
        <view class="addr-grabber"></view>
        <view class="addr-pop-title">选择收货地址</view>
        <text class="addr-close" @click="showAddr = false">✕</text>
        <scroll-view scroll-y class="addr-pop-list">
          <view v-for="a in addrList" :key="a.id" class="addr-item" :class="{ active: selectedAddr && selectedAddr.id === a.id }" @click="pickAddr(a)">
            <view class="ai-ico"></view>
            <view class="ai-body">
              <view class="ai-line1">
                <text class="ai-name">{{ a.realName }}</text>
                <text class="ai-phone">{{ a.phone }}</text>
                <text v-if="a.isDefault" class="addr-default">默认</text>
              </view>
              <view class="ai-line2">{{ addrText(a) }}</view>
            </view>
            <view class="ai-check" :class="{ on: selectedAddr && selectedAddr.id === a.id }"></view>
          </view>
          <view v-if="!addrList.length" class="addr-none">暂无收货地址，点击下方按钮新增</view>
        </scroll-view>
        <button class="addr-add-btn" @click="goAddAddress">＋ 新增收货地址</button>
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
				skuVisible: false,
				skuRow: {},
				skuGroups: [],
				skuPicked: {},
				skuQty: 1,
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
			parseAttr(attrValue) {
				try {
					const o = typeof attrValue === 'string' ? JSON.parse(attrValue) : attrValue;
					return (o && typeof o === 'object') ? o : {};
				} catch (e) { return {}; }
			},
			buildSkuGroups(skus) {
				const map = {};
				skus.forEach(s => {
					const av = this.parseAttr(s.attrValue);
					Object.keys(av).forEach(k => {
						if (!map[k]) map[k] = [];
						if (map[k].indexOf(av[k]) < 0) map[k].push(av[k]);
					});
				});
				return Object.keys(map).map(k => ({ name: k, values: map[k] }));
			},
			openSku(item) {
				this.skuRow = item;
				this.skuGroups = this.buildSkuGroups(item.skus || []);
				this.skuPicked = {};
				const cur = (item.skus || []).find(s => s.skuKey === item.skuKey);
				if (cur) {
					const av = this.parseAttr(cur.attrValue);
					Object.keys(av).forEach(k => { this.$set(this.skuPicked, k, av[k]); });
				}
				this.skuQty = item.buyNum > 0 ? item.buyNum : 1;
				this.$set(item, 'skuName', item.skuName || '');
				this.skuVisible = true;
			},
			pickAttr(name, val) {
				this.$set(this.skuPicked, name, val);
				const keys = this.skuGroups.map(g => g.name);
				if (!keys.every(k => this.skuPicked[k])) return;
				const hit = (this.skuRow.skus || []).find(s => {
					const av = this.parseAttr(s.attrValue);
					return keys.every(k => av[k] === this.skuPicked[k]);
				});
				if (!hit) return;
				this.$set(this.skuRow, 'skuKey', hit.skuKey);
				this.$set(this.skuRow, 'skuName', keys.map(k => this.skuPicked[k]).join(' / '));
				this.$set(this.skuRow, 'skuStock', hit.stock);
				this.$set(this.skuRow, 'myPrice', hit.myPrice);
			},
			minusSku() {
				if (this.skuQty <= 1) return;
				this.skuQty = Number(this.skuQty) - 1;
			},
			plusSku() {
				const max = this.skuRow.skuKey ? Number(this.skuRow.skuStock || 0) : Number(this.skuRow.stock || 0);
				if (this.skuQty >= max) return this.$util.Tips({ title: '已达库存上限' });
				this.skuQty = Number(this.skuQty || 0) + 1;
			},
			closeSku() {
				this.skuVisible = false;
			},
			confirmSku() {
				const item = this.skuRow;
				if (this.skuGroups.length && !item.skuKey) return this.$util.Tips({ title: '请选择规格' });
				const num = Number(this.skuQty || 0);
				if (!num || num <= 0) return this.$util.Tips({ title: '请填写数量' });
				const max = item.skuKey ? Number(item.skuStock || 0) : Number(item.stock || 0);
				if (num > max) return this.$util.Tips({ title: '超过库存上限' });
				this.$set(item, 'buyNum', num);
				this.syncCart(item);
				this.skuVisible = false;
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
.stock-goods { min-height: 100vh; background: #f4f6fb; padding-bottom: 220rpx; }

/* ---------- 顶部：蓝色渐变区自然过渡到灰底 ---------- */
.top-wrap {
  position: relative;
  padding: 24rpx 24rpx 90rpx;
  background: linear-gradient(160deg, #2b6fe3 0%, #4a9df8 70%, #6dadf9 100%);
  overflow: hidden;
  .top-deco { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.10); }
  .d1 { width: 240rpx; height: 240rpx; right: -70rpx; top: -100rpx; background: rgba(255,255,255,0.14); }
  .d2 { width: 130rpx; height: 130rpx; left: -50rpx; bottom: -20rpx; }
}

/* 搜索框：白底胶囊 + CSS 放大镜 */
.search-bar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 999rpx;
  height: 76rpx;
  padding: 0 28rpx;
  box-shadow: 0 8rpx 22rpx rgba(16, 58, 133, 0.18);
}
.s-ico {
  position: relative;
  width: 26rpx;
  height: 26rpx;
  border: 4rpx solid #9aa7bd;
  border-radius: 50%;
  margin-right: 18rpx;
  flex-shrink: 0;
  &::after {
    content: '';
    position: absolute;
    width: 4rpx;
    height: 14rpx;
    background: #9aa7bd;
    border-radius: 2rpx;
    transform: rotate(-45deg);
    right: -8rpx;
    bottom: -8rpx;
  }
}
.search-input { flex: 1; height: 100%; font-size: 27rpx; color: #303133; }

/* 库存类型：双卡片 + 选中角标 */
.type-bar {
  position: relative;
  z-index: 1;
  display: flex;
  margin-top: 22rpx;
}
.type-item {
  position: relative;
  flex: 1;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 18rpx;
  padding: 22rpx 24rpx;
  margin-right: 18rpx;
  border: 2rpx solid transparent;
  overflow: hidden;
  &:last-child { margin-right: 0; }
  &.active {
    border-color: #fff;
    background: #fff;
    box-shadow: 0 10rpx 24rpx rgba(16, 58, 133, 0.22);
  }
}
.t-name { font-size: 29rpx; color: #303133; font-weight: 700; }
.type-item.active .t-name { color: #2b6fe3; }
.t-sub { font-size: 22rpx; color: #909399; margin-top: 8rpx; }
.t-check {
  position: absolute;
  right: 0;
  top: 0;
  width: 0;
  height: 0;
  border-top: 44rpx solid #2b6fe3;
  border-left: 44rpx solid transparent;
  &::after {
    content: '';
    position: absolute;
    left: -26rpx;
    top: -40rpx;
    width: 16rpx;
    height: 8rpx;
    border-left: 3rpx solid #fff;
    border-bottom: 3rpx solid #fff;
    transform: rotate(-45deg);
  }
}

/* ---------- 商品列表：卡片上提压住渐变区 ---------- */
.goods-list {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -62rpx;
}
.goods-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 22rpx;
  margin-bottom: 20rpx;
  display: flex;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.goods-img {
  width: 150rpx;
  height: 150rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #f5f6fa;
}
.goods-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
.goods-name {
  font-size: 28rpx;
  color: #26324b;
  font-weight: 600;
  line-height: 38rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}
.price-row { display: flex; align-items: baseline; margin-top: 10rpx; }
.p-cny { color: #e93323; font-size: 24rpx; font-weight: 600; }
.my-price { color: #e93323; font-size: 36rpx; font-weight: 700; margin-right: 12rpx; }
.zhuan-tag {
  font-size: 20rpx;
  color: #e93323;
  background: #ffecec;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-right: 12rpx;
}
.retail-price { color: #a4adc0; font-size: 22rpx; text-decoration: line-through; }
.stock-row { color: #909399; font-size: 22rpx; margin-top: 8rpx; display: flex; align-items: center; }
.stock-num { color: #606266; font-weight: 600; margin-left: 6rpx; }
.sku-chip {
  margin-left: 14rpx;
  color: #2b6fe3;
  border: 1rpx solid #9cc3f5;
  background: #f0f6ff;
  border-radius: 999rpx;
  padding: 2rpx 16rpx;
  font-size: 21rpx;
}

/* 卡片底部操作行：步进器 + 加购按钮 */
.op-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}
.num-ctrl {
  display: flex;
  align-items: center;
  background: #f4f6f9;
  border-radius: 999rpx;
  padding: 4rpx;
}
.ctrl-btn {
  width: 52rpx;
  height: 52rpx;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #303133;
  box-shadow: 0 2rpx 8rpx rgba(31, 45, 61, 0.10);
  &.off { color: #c3cad6; }
}
.num-input { width: 76rpx; height: 52rpx; text-align: center; font-size: 27rpx; color: #303133; }
.buy-btn {
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  font-size: 25rpx;
  border-radius: 999rpx;
  padding: 0 30rpx;
  height: 60rpx;
  line-height: 60rpx;
  margin: 0;
  box-shadow: 0 8rpx 18rpx rgba(43, 111, 227, 0.28);
  &::after { border: none; }
}

.empty { text-align: center; color: #9aa7bd; padding: 140rpx 0; font-size: 26rpx; }

/* ---------- 规格弹窗 ---------- */
.sku-mask { position: fixed; inset: 0; background: rgba(15, 25, 45, 0.5); z-index: 99; display: flex; align-items: flex-end; }
.sku-pop { width: 100%; background: #fff; border-radius: 28rpx 28rpx 0 0; padding: 30rpx 30rpx 44rpx; }
.sku-top { display: flex; position: relative; }
.sku-img { width: 180rpx; height: 180rpx; border-radius: 16rpx; background: #f5f6fa; flex-shrink: 0; }
.sku-top-info { flex: 1; margin-left: 22rpx; overflow: hidden; }
.sku-name { font-size: 26rpx; color: #26324b; line-height: 36rpx; font-weight: 600; }
.sku-price-row { display: flex; align-items: baseline; margin-top: 10rpx; }
.sku-price { color: #e93323; font-size: 40rpx; font-weight: 700; }
.sku-retail { color: #a4adc0; font-size: 22rpx; text-decoration: line-through; margin-left: 12rpx; }
.sku-stock { color: #909399; font-size: 23rpx; margin-top: 8rpx; }
.sku-chosen { color: #606266; font-size: 23rpx; margin-top: 8rpx; }
.sku-close { position: absolute; right: 0; top: -6rpx; color: #c0c4cc; font-size: 34rpx; padding: 0 6rpx; }
.sku-body { max-height: 46vh; margin-top: 24rpx; }
.sku-group { margin-bottom: 22rpx; }
.sku-group-name { font-size: 26rpx; color: #26324b; font-weight: 600; margin-bottom: 14rpx; }
.sku-values { display: flex; flex-wrap: wrap; }
.sku-value {
  padding: 12rpx 30rpx;
  border-radius: 999rpx;
  background: #f4f6f9;
  color: #303133;
  font-size: 25rpx;
  margin: 0 16rpx 16rpx 0;
  border: 2rpx solid transparent;
  &.active {
    background: #f0f6ff;
    border-color: #2b6fe3;
    color: #2b6fe3;
    font-weight: 600;
  }
}
.num-ctrl.lg { margin-top: 4rpx; }
.num-ctrl.lg .ctrl-btn { width: 60rpx; height: 60rpx; }
.num-ctrl.lg .num-input { width: 110rpx; height: 60rpx; }
.sku-confirm {
  margin-top: 6rpx;
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  height: 84rpx;
  line-height: 84rpx;
  font-size: 29rpx;
  box-shadow: 0 10rpx 24rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}

/* ---------- 底部：地址栏 + 结算栏 ---------- */
.addr-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 20rpx 26rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(15, 25, 45, 0.06);
  z-index: 10;
  &.raised { bottom: 118rpx; }
}
/* 定位图标：浅蓝圆底 + 水滴 pin */
.addr-ico {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: #ecf3ff;
  flex-shrink: 0;
  margin-right: 18rpx;
  position: relative;
  &::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 50%;
    width: 24rpx;
    height: 24rpx;
    border: 5rpx solid #2b6fe3;
    border-radius: 50% 50% 50% 0;
    transform: translate(-50%, -50%) rotate(-45deg);
  }
  &::after {
    content: '';
    position: absolute;
    left: 50%;
    top: 50%;
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: #2b6fe3;
    transform: translate(-130%, -130%);
  }
}
.addr-info { flex: 1; overflow: hidden; }
.addr-line1 { display: flex; align-items: center; }
.addr-tag {
  flex-shrink: 0;
  font-size: 20rpx;
  color: #2b6fe3;
  background: #ecf3ff;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-right: 12rpx;
}
.addr-who { font-size: 27rpx; color: #26324b; font-weight: 600; }
.addr-line2 { font-size: 23rpx; color: #909399; margin-top: 6rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.addr-empty { flex: 1; font-size: 25rpx; color: #909399; line-height: 38rpx; }
/* 右侧切换胶囊 */
.addr-switch {
  flex-shrink: 0;
  margin-left: 16rpx;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #2b6fe3;
  background: #f0f6ff;
  border-radius: 999rpx;
  padding: 10rpx 22rpx;
}
.addr-arrow { font-size: 30rpx; margin-left: 6rpx; line-height: 1; }

/* 虚拟库存提示条：浅蓝信息条 */
.addr-bar.v-bar {
  background: #eef5ff;
  box-shadow: none;
  .v-ico {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: #2b6fe3;
    color: #fff;
    font-size: 26rpx;
    font-style: italic;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 16rpx;
  }
  .addr-empty { color: #3d6db5; font-size: 24rpx; }
}

.cart-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 20rpx rgba(15, 25, 45, 0.06);
  z-index: 11;
}
.cart-info { font-size: 26rpx; color: #606266; }
.cart-total { color: #e93323; font-size: 36rpx; font-weight: 700; }
.submit-btn {
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  font-size: 28rpx;
  padding: 0 50rpx;
  height: 76rpx;
  line-height: 76rpx;
  margin: 0;
  box-shadow: 0 8rpx 20rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}

.addr-mask { position: fixed; inset: 0; background: rgba(15, 25, 45, 0.5); z-index: 99; display: flex; align-items: flex-end; }
.addr-pop {
  position: relative;
  width: 100%;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 18rpx 26rpx calc(30rpx + env(safe-area-inset-bottom));
}
/* 顶部抓手条 */
.addr-grabber {
  width: 76rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: #e3e8f0;
  margin: 0 auto 22rpx;
}
.addr-pop-title { text-align: center; font-size: 31rpx; font-weight: 700; color: #26324b; }
.addr-close {
  position: absolute;
  right: 30rpx;
  top: 52rpx;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #f2f4f8;
  color: #9aa7bd;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.addr-pop-list {
  max-height: 52vh;
  margin-top: 24rpx;
  background: #f6f8fc;
  border-radius: 20rpx;
  padding: 18rpx;
  box-sizing: border-box;
}
/* 地址项：白卡 + 定位图标 + 选中圆勾 */
.addr-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 18rpx;
  padding: 22rpx 22rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid transparent;
  &:last-child { margin-bottom: 0; }
  &.active {
    border-color: #2b6fe3;
    background: #f4f9ff;
    box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.12);
  }
}
.ai-ico {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #ecf3ff;
  flex-shrink: 0;
  margin-right: 18rpx;
  position: relative;
  &::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 50%;
    width: 20rpx;
    height: 20rpx;
    border: 4rpx solid #2b6fe3;
    border-radius: 50% 50% 50% 0;
    transform: translate(-50%, -50%) rotate(-45deg);
  }
}
.addr-item.active .ai-ico { background: #dcE9ff; }
.ai-body { flex: 1; overflow: hidden; }
.ai-line1 { display: flex; align-items: center; }
.ai-name { font-size: 28rpx; color: #26324b; font-weight: 700; }
.ai-phone { font-size: 24rpx; color: #909399; margin-left: 14rpx; }
.addr-default {
  font-size: 20rpx;
  color: #2b6fe3;
  background: #ecf3ff;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-left: 12rpx;
}
.ai-line2 {
  font-size: 23rpx;
  color: #909399;
  margin-top: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 选中圆勾 */
.ai-check {
  flex-shrink: 0;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  border: 2rpx solid #d5dce8;
  margin-left: 16rpx;
  position: relative;
  &.on {
    border-color: #2b6fe3;
    background: #2b6fe3;
    &::after {
      content: '';
      position: absolute;
      left: 12rpx;
      top: 8rpx;
      width: 12rpx;
      height: 20rpx;
      border-right: 4rpx solid #fff;
      border-bottom: 4rpx solid #fff;
      transform: rotate(45deg);
    }
  }
}
.addr-none { text-align: center; color: #9aa7bd; font-size: 26rpx; padding: 70rpx 0; }
.addr-add-btn {
  margin-top: 22rpx;
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  font-size: 29rpx;
  font-weight: 600;
  box-shadow: 0 10rpx 24rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}
</style>
