<template>
  <view class="exchange-page">
    <view v-if="canApply" class="apply-card">
      <view class="card-title">提交换货申请</view>
      <view class="form-item">
        <text class="f-label">库存类型</text>
        <view class="f-static">
          <text class="st-pill" :class="sourceType === 2 ? 'st-virtual' : (sourceType === 1 ? 'st-physical' : 'st-none')">
            {{ sourceType === 2 ? '虚拟库存' : (sourceType === 1 ? '实体库存' : '不区分') }}
          </text>
          <text class="f-hint">{{ sourceType === 2 ? '虚拟换货：总部直接发货，虚拟库存同步扣减' : (sourceType === 1 ? '实体换货：上级审核通过后发货' : '按原订单的库存类型自动判定') }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="f-label">原商品</text>
        <view class="f-static">
          <text class="f-strong">商品ID {{ form.productId }}</text>
          <text class="f-hint">{{ form.orderId ? ('原订单ID ' + form.orderId) : '未指定订单，系统将自动匹配最近一笔含此商品的已完成订单' }}</text>
        </view>
      </view>
      <view class="form-item">
        <text class="f-label">换货数量</text>
        <input v-model="form.num" type="number" class="f-input" placeholder="1" />
      </view>
      <view class="form-item">
        <view class="opt-head">
          <text class="f-label">换入商品</text>
          <button class="opt-load-btn" size="mini" @click="loadOptions">加载可换商品</button>
        </view>
        <view class="opt-list">
          <view v-for="(o, i) in visibleOptions" :key="i" class="opt-item" :class="{ active: selectedTarget && selectedTarget.targetProductId === o.targetProductId }" @click="selectedTarget = o">
            <image :src="o.image" class="opt-img" mode="aspectFill" />
            <view class="opt-info">
              <view class="opt-name">{{ o.targetProductName }}</view>
              <view v-if="o.skuName" class="opt-spec">规格：{{ o.skuName }}</view>
              <view class="opt-prices">
                <text class="p-retail">零售 ¥{{ o.retailPrice }}</text>
                <text class="p-whole">拿货 ¥{{ o.targetPrice }}</text>
                <text v-if="Number(o.diffPrice) > 0" class="p-diff">补差价 ¥{{ o.diffPrice }}</text>
                <text v-else class="p-same">无需补差价</text>
              </view>
            </view>
            <text v-if="selectedTarget && selectedTarget.targetProductId === o.targetProductId" class="opt-check">✓</text>
          </view>
          <view v-if="options.length > 3" class="opt-more" @click="showAllOptions = !showAllOptions">
            {{ showAllOptions ? '收起' : ('更多（共 ' + options.length + ' 个可换商品）') }}
          </view>
          <view v-if="!options.length" class="opt-none">该商品暂无可换入商品（需后台在「规格与订货设置」中配置）</view>
        </view>
      </view>
      <view v-if="selectedTarget" class="diff-tip">
        换 {{ form.num || 1 }} 件需补差价 <text class="diff-amt">¥{{ (selectedTarget.diffPrice * (form.num || 1)).toFixed(2) }}</text>
      </view>
      <view class="form-item">
        <text class="f-label">换货原因</text>
        <textarea v-model="form.reason" class="f-textarea" placeholder="请描述换货原因" />
      </view>
      <button class="submit-btn" @click="submit">提交申请</button>
    </view>

    <view class="list-title">换货记录</view>
    <view v-for="e in list" :key="e.id" class="ex-card">
      <view class="row-1">
        <text class="ex-no">{{ e.exchangeNo }}</text>
        <text class="ex-status" :class="'st' + e.status">{{ statusText(e.status) }}</text>
      </view>
      <view class="ex-row">{{ e.productName }} × {{ e.num }}（原单 {{ e.orderNo }}）</view>
      <view v-if="e.targetProductName" class="ex-row">换入：{{ e.targetProductName }}
        <text v-if="Number(e.diffPrice) > 0" class="diff-amt"> 需补差价 ¥{{ e.diffPrice }}</text>
      </view>
      <view class="ex-row grey">原因：{{ e.reason }}</view>
      <view v-if="e.status === -1" class="ex-row red">驳回原因：{{ e.rejectReason }}</view>
      <view v-if="e.backExpressNum" class="ex-row blue">旧品退回：{{ e.backExpressName }} {{ e.backExpressNum }}</view>
      <view v-if="e.newExpressNum" class="ex-row blue">新品发出：{{ e.newExpressName }} {{ e.newExpressNum }}</view>
      <view class="row-op" v-if="Number(e.diffPrice) > 0 && e.diffPayStatus !== 1 && e.status !== -1">
        <button class="op-btn primary" size="mini" @click="payDiff(e)">支付差价 ¥{{ e.diffPrice }}</button>
      </view>
      <view class="row-op" v-if="e.status === 2">
        <button class="op-btn primary" size="mini" @click="fillBack(e)">填写旧品退回快递</button>
      </view>
      <view class="row-op" v-if="e.status === 0">
        <button class="op-btn primary" size="mini" @click="audit(e, 1)">上级通过</button>
        <button class="op-btn danger" size="mini" @click="audit(e, -1)">驳回</button>
      </view>
    </view>
    <view v-if="!list.length && loaded" class="empty">暂无换货记录</view>
  </view>
</template>

<script>
	import { getMyExchanges, applyStockExchange, fillExchangeBackExpress, auditStockExchange, getExchangeOptions, payExchangeDiff } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false,
				canApply: false,
				options: [],
				selectedTarget: null,
				skuKeyParam: '',
				sourceType: 0,
				showAllOptions: false,
				form: { orderId: '', productId: '', num: '1', reason: '' }
			};
		},
		computed: {
			visibleOptions() {
				const list = this.options || [];
				return this.showAllOptions ? list : list.slice(0, 3);
			}
		},
		onLoad(opt) {
			this.sourceType = opt.type ? Number(opt.type) : 0;
			if (opt.orderId) {
				this.form.orderId = opt.orderId;
				this.canApply = true;
			}
			if (opt.productId) {
				this.form.productId = opt.productId;
				this.skuKeyParam = opt.skuKey || '';
				if (opt.num) this.form.num = opt.num;
				this.canApply = true;
				this.loadOptions();
			}
			this.load();
		},
		methods: {
			statusText(s) {
				return { 0: '待上级审核', 1: '待总部审核', 2: '待旧品退回', 3: '待发新品', 4: '已完成', '-1': '已驳回' }[s] || s;
			},
			load() {
				getMyExchanges({ page: 1, limit: 30 }).then(res => {
					this.list = res.data.list || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			loadOptions() {
				if (!this.form.productId) return this.$util.Tips({ title: '请先填写商品ID' });
				getExchangeOptions(Number(this.form.productId), this.skuKeyParam || '').then(res => {
					this.options = res.data || [];
					this.selectedTarget = this.options.length ? this.options[0] : null;
					if (!this.options.length) this.$util.Tips({ title: '该商品未开放换货或暂无可换入商品' });
				}).catch(() => {});
			},
			payDiff(e) {
				uni.showActionSheet({
					itemList: ['余额支付', '微信支付'],
					success: (r) => {
						const payType = r.tapIndex === 0 ? 'yue' : 'weixin';
						const body = { exchangeId: e.id, payType: payType };
						if (payType === 'weixin') body.payChannel = 'routine';
						payExchangeDiff(body).then(res => {
							const d = res.data || {};
							if (payType === 'weixin' && d.jsConfig) {
								const cfg = d.jsConfig;
								uni.requestPayment({
									provider: 'wxpay',
									timeStamp: cfg.timeStamp,
									nonceStr: cfg.nonceStr,
									package: cfg.packages,
									signType: cfg.signType,
									paySign: cfg.paySign,
									success: () => { uni.showToast({ title: '差价支付成功', icon: 'success' }); this.load(); },
									fail: () => { uni.showToast({ title: '支付已取消', icon: 'none' }); }
								});
							} else {
								uni.showToast({ title: '差价支付成功', icon: 'success' });
								this.load();
							}
						});
					}
				});
			},
			submit() {
				if (!this.form.productId) return this.$util.Tips({ title: '请选择要换货的商品' });
				if (!this.selectedTarget) return this.$util.Tips({ title: '请选择要换入的商品' });
				if (!this.form.reason) return this.$util.Tips({ title: '请填写换货原因' });
				applyStockExchange({
					orderId: this.form.orderId ? Number(this.form.orderId) : null,
					productId: Number(this.form.productId),
					skuKey: this.skuKeyParam || '',
					num: Number(this.form.num || 1),
					reason: this.form.reason,
					targetProductId: this.selectedTarget.targetProductId,
					targetSkuKey: this.selectedTarget.targetSkuKey || '',
					sourceStockType: this.sourceType || null
				}).then(() => {
					this.$util.Tips({ title: '申请已提交' });
					this.load();
				});
			},
			fillBack(e) {
				uni.showModal({
					title: '旧品退回快递',
					editable: true,
					placeholderText: '快递公司 快递单号（空格分隔）',
					success: (m) => {
						if (!m.confirm || !m.content) return;
						const parts = m.content.trim().split(/\s+/);
						fillExchangeBackExpress(e.id, { backExpressName: parts[0] || '', backExpressNum: parts[1] || '' }).then(() => {
							uni.showToast({ title: '已保存', icon: 'success' });
							this.load();
						});
					}
				});
			},
			audit(e, result) {
				const doAudit = (data) => {
					auditStockExchange(e.id, data).then(() => {
						uni.showToast({ title: '已操作', icon: 'success' });
						this.load();
					});
				};
				if (result === -1) {
					uni.showModal({
						title: '驳回换货',
						editable: true,
						placeholderText: '请填写驳回原因',
						success: (m) => { if (m.confirm) doAudit({ status: -1, reason: m.content || '' }); }
					});
				} else {
					uni.showModal({
						title: '通过审核',
						content: '通过后流转总部审核。',
						success: (m) => { if (m.confirm) doAudit({ status: 1 }); }
					});
				}
			}
		}
	};
</script>

<style lang="scss" scoped>
.f-static { display: flex; flex-direction: column; }
.f-strong { font-size: 26rpx; color: #303133; }
.f-hint { font-size: 22rpx; color: #a0a6b0; margin-top: 6rpx; line-height: 1.5; }
.st-pill { display: inline-block; align-self: flex-start; padding: 4rpx 18rpx; border-radius: 999rpx; font-size: 23rpx; }
.st-physical { background: #e8f2ff; color: #2b6fe3; }
.st-virtual { background: #fff4e0; color: #d48806; }
.st-none { background: #f2f3f5; color: #909399; }
.opt-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.opt-list { margin-top: 0; }
.opt-load-btn { background: #f2f3f5; color: #333; border-radius: 999rpx; margin: 0; }
.opt-item { display: flex; align-items: center; position: relative; border: 1rpx solid #ebeef5; border-radius: 12rpx; padding: 16rpx; margin-bottom: 12rpx; background: #fff; }
.opt-item.active { border-color: #2b6fe3; background: #f0f6ff; }
.opt-img { width: 130rpx; height: 130rpx; border-radius: 10rpx; background: #f5f6fa; flex-shrink: 0; }
.opt-info { flex: 1; margin-left: 18rpx; overflow: hidden; }
.opt-name { font-size: 25rpx; color: #303133; font-weight: 600; line-height: 34rpx; }
.opt-spec { font-size: 22rpx; color: #909399; margin-top: 6rpx; }
.opt-prices { margin-top: 8rpx; display: flex; flex-wrap: wrap; align-items: baseline; }
.p-retail { font-size: 21rpx; color: #b0b8c4; text-decoration: line-through; margin-right: 14rpx; }
.p-whole { font-size: 23rpx; color: #e6a23c; margin-right: 14rpx; }
.p-diff { font-size: 23rpx; color: #e93323; font-weight: 700; }
.p-same { font-size: 21rpx; color: #67c23a; }
.opt-check { position: absolute; right: 16rpx; top: 16rpx; color: #2b6fe3; font-size: 30rpx; font-weight: 700; }
.opt-more { text-align: center; font-size: 23rpx; color: #2b6fe3; padding: 10rpx 0 4rpx; }
.opt-none { font-size: 23rpx; color: #b0b8c4; padding: 12rpx 0; }
.diff-tip { margin: 10rpx 0 16rpx; font-size: 24rpx; color: #e6a23c; }
.diff-amt { color: #e93323; font-weight: 700; }
.exchange-page { min-height: 100vh; background: #f5f6f8; padding: 24rpx; }
.apply-card, .ex-card { background: #fff; border-radius: 16rpx; padding: 26rpx; margin-bottom: 20rpx; }
.card-title, .list-title { font-size: 30rpx; font-weight: 600; color: #333; margin-bottom: 20rpx; }
.list-title { margin: 10rpx 4rpx; }
.form-item { display: flex; align-items: flex-start; margin-bottom: 18rpx; }
.f-label { width: 170rpx; font-size: 26rpx; color: #666; padding-top: 10rpx; flex-shrink: 0; }
.f-input { flex: 1; background: #f5f6f8; border-radius: 10rpx; height: 70rpx; padding: 0 20rpx; font-size: 26rpx; }
.f-textarea { flex: 1; background: #f5f6f8; border-radius: 10rpx; padding: 16rpx 20rpx; font-size: 26rpx; height: 130rpx; }
.submit-btn { background: #2b6fe3; color: #fff; border-radius: 40rpx; font-size: 28rpx; margin-top: 10rpx; }
.row-1 { display: flex; justify-content: space-between; margin-bottom: 10rpx; }
.ex-no { font-size: 26rpx; font-weight: 600; color: #333; }
.ex-status { font-size: 24rpx; }
.st0 { color: #ff9a3c; } .st1 { color: #ff9a3c; } .st2 { color: #2b6fe3; } .st3 { color: #2b6fe3; }
.st4 { color: #5cc45c; } .st-1 { color: #f56c6c; }
.ex-row { font-size: 25rpx; color: #333; margin-bottom: 8rpx; }
.grey { color: #999; } .red { color: #f56c6c; } .blue { color: #2b6fe3; }
.row-op { display: flex; justify-content: flex-end; margin-top: 12rpx; gap: 16rpx; }
.op-btn { border-radius: 30rpx; font-size: 24rpx; background: #f2f3f5; color: #666; }
.op-btn.primary { background: #2b6fe3; color: #fff; }
.op-btn.danger { background: #f56c6c; color: #fff; }
.empty { text-align: center; color: #999; padding: 100rpx 0; font-size: 26rpx; }
</style>
