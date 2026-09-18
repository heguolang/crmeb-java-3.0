<template>
  <view class="exchange-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">换货管理</view>
      <view class="page-sub">换货需上级 / 总部审核，差价随单结算，旧品需退回</view>
    </view>

    <view class="page-body" :class="{ lift: canApply }">
      <!-- 提交换货申请 -->
      <view v-if="canApply" class="apply-card">
        <view class="card-head">
          <view class="ch-bar"></view>
          <text class="card-title">提交换货申请</text>
        </view>

        <view class="form-item">
          <view class="f-label">库存类型</view>
          <view class="f-static">
            <text class="st-pill" :class="sourceType === 2 ? 'st-virtual' : (sourceType === 1 ? 'st-physical' : 'st-none')">
              {{ sourceType === 2 ? '虚拟库存' : (sourceType === 1 ? '实体库存' : '不区分') }}
            </text>
            <text class="f-hint">{{ sourceType === 2 ? '虚拟换货：总部直接发货，虚拟库存同步扣减' : (sourceType === 1 ? '实体换货：上级审核通过后发货' : '按原订单的库存类型自动判定') }}</text>
          </view>
        </view>

        <view class="form-item">
          <view class="f-label">原商品</view>
          <view class="f-static">
            <text class="f-strong">商品ID {{ form.productId }}</text>
            <text class="f-hint">{{ form.orderId ? ('原订单ID ' + form.orderId) : '未指定订单，系统将自动匹配最近一笔含此商品的已完成订单' }}</text>
          </view>
        </view>

        <view class="form-item">
          <view class="f-label">换货数量</view>
          <input v-model="form.num" type="number" class="f-input" placeholder="1" />
        </view>

        <view class="form-item">
          <view class="f-label">换入商品</view>
          <view class="f-static">
            <button class="opt-load-btn" @click="loadOptions">{{ options.length ? '重新加载可换商品' : '加载可换商品' }}</button>
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
                <view class="opt-check" :class="{ on: selectedTarget && selectedTarget.targetProductId === o.targetProductId }"></view>
              </view>
              <view v-if="options.length > 3" class="opt-more" @click="showAllOptions = !showAllOptions">
                {{ showAllOptions ? '收起' : ('更多（共 ' + options.length + ' 个可换商品）') }}
              </view>
              <view v-if="!options.length" class="opt-none">该商品暂无可换入商品（需后台在「规格与订货设置」中配置）</view>
            </view>
          </view>
        </view>

        <view v-if="selectedTarget" class="diff-tip">
          换 {{ form.num || 1 }} 件需补差价 <text class="diff-amt">¥{{ (selectedTarget.diffPrice * (form.num || 1)).toFixed(2) }}</text>
        </view>

        <view class="form-item">
          <view class="f-label">换货原因</view>
          <textarea v-model="form.reason" class="f-textarea" placeholder="请描述换货原因" />
        </view>

        <button class="submit-btn" @click="submit">提交申请</button>
      </view>

      <!-- 换货记录 -->
      <view class="section-title">
        <view class="st-bar"></view>
        <text class="st-text">换货记录</text>
        <view class="st-line"></view>
      </view>

      <view v-if="list.length" class="ex-list">
        <view v-for="e in list" :key="e.id" class="ex-card">
        <view class="row-1">
          <text class="ex-no">{{ e.exchangeNo }}</text>
          <text class="ex-status" :class="'st' + e.status">{{ statusText(e.status) }}</text>
        </view>
        <view class="ex-row">{{ e.productName }} × {{ e.num }}<text class="ex-sub">（原单 {{ e.orderNo }}）</text></view>
        <view v-if="e.targetProductName" class="ex-row">换入：{{ e.targetProductName }}
          <text v-if="Number(e.diffPrice) > 0" class="diff-amt"> 需补差价 ¥{{ e.diffPrice }}</text>
        </view>
        <view class="ex-row grey">原因：{{ e.reason }}</view>
        <view v-if="e.status === -1" class="ex-notice n-red">驳回原因：{{ e.rejectReason }}</view>
        <view v-if="e.backExpressNum" class="ex-notice n-blue">旧品退回：{{ e.backExpressName }} {{ e.backExpressNum }}</view>
        <view v-if="e.newExpressNum" class="ex-notice n-blue">新品发出：{{ e.newExpressName }} {{ e.newExpressNum }}</view>
        <view class="row-op" v-if="Number(e.diffPrice) > 0 && e.diffPayStatus !== 1 && e.status !== -1">
          <button class="op-btn primary" @click="payDiff(e)">支付差价 ¥{{ e.diffPrice }}</button>
        </view>
        <view class="row-op" v-if="e.status === 2">
          <button class="op-btn primary" @click="fillBack(e)">填写旧品退回快递</button>
        </view>
        <view class="row-op" v-if="e.status === 0">
          <button class="op-btn soft" @click="audit(e, 1)">上级通过</button>
          <button class="op-btn danger" @click="audit(e, -1)">驳回</button>
        </view>
        </view>
      </view>

      <view v-if="!list.length && loaded" class="empty-card">
        <view class="empty-ico">换</view>
        <view class="empty-txt">暂无换货记录</view>
        <view class="empty-sub">在订货订单中选择商品即可发起换货</view>
      </view>
    </view>
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
.exchange-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 74rpx;
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
  /* 有申请卡时上提压住渐变；纯记录视图则与渐变头保持间距，避免标题浮在渐变上 */
  margin-top: 30rpx;
  &.lift { margin-top: -48rpx; }
}

/* ---------- 卡片公共 ---------- */
.apply-card, .ex-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 28rpx 26rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.card-head { display: flex; align-items: center; margin-bottom: 24rpx; }
.ch-bar {
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4a9df8, #2b6fe3);
  margin-right: 14rpx;
}
.card-title { font-size: 30rpx; font-weight: 700; color: #26324b; }

/* ---------- 表单 ---------- */
.form-item { margin-bottom: 24rpx; }
.f-label { font-size: 26rpx; color: #3d4a5f; font-weight: 600; margin-bottom: 12rpx; }
.f-static { display: flex; flex-direction: column; }
.f-strong { font-size: 27rpx; color: #26324b; font-weight: 600; }
.f-hint { font-size: 22rpx; color: #a0a6b0; margin-top: 8rpx; line-height: 1.5; }
.st-pill {
  display: inline-block;
  align-self: flex-start;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  font-size: 23rpx;
  font-weight: 600;
}
.st-physical { background: #ecf3ff; color: #2b6fe3; }
.st-virtual { background: #fff4e0; color: #d48806; }
.st-none { background: #f2f3f5; color: #909399; }
.f-input {
  background: #f4f6f9;
  border-radius: 14rpx;
  height: 76rpx;
  padding: 0 24rpx;
  font-size: 27rpx;
  color: #303133;
}
.f-textarea {
  width: 100%;
  box-sizing: border-box;
  background: #f4f6f9;
  border-radius: 14rpx;
  padding: 20rpx 24rpx;
  font-size: 26rpx;
  height: 140rpx;
  color: #303133;
}

/* ---------- 可换商品 ---------- */
.opt-load-btn {
  align-self: flex-start;
  background: #f0f6ff;
  color: #2b6fe3;
  border-radius: 999rpx;
  font-size: 24rpx;
  padding: 0 26rpx;
  height: 58rpx;
  line-height: 58rpx;
  margin: 0 0 16rpx;
  &::after { border: none; }
}
.opt-list { margin-top: 0; }
.opt-item {
  display: flex;
  align-items: center;
  position: relative;
  border: 2rpx solid #eef1f6;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 14rpx;
  background: #fff;
  &.active {
    border-color: #2b6fe3;
    background: #f4f9ff;
    box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.12);
  }
}
.opt-img { width: 130rpx; height: 130rpx; border-radius: 14rpx; background: #f5f6fa; flex-shrink: 0; }
.opt-info { flex: 1; margin-left: 18rpx; overflow: hidden; }
.opt-name { font-size: 26rpx; color: #26324b; font-weight: 600; line-height: 36rpx; }
.opt-spec { font-size: 22rpx; color: #909399; margin-top: 6rpx; }
.opt-prices { margin-top: 10rpx; display: flex; flex-wrap: wrap; align-items: baseline; }
.p-retail { font-size: 21rpx; color: #a4adc0; text-decoration: line-through; margin-right: 14rpx; }
.p-whole { font-size: 24rpx; color: #e6a23c; font-weight: 600; margin-right: 14rpx; }
.p-diff { font-size: 23rpx; color: #e93323; font-weight: 700; }
.p-same { font-size: 21rpx; color: #21a852; }
/* 选中圆勾 */
.opt-check {
  flex-shrink: 0;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  border: 2rpx solid #d5dce8;
  margin-left: 14rpx;
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
.opt-more { text-align: center; font-size: 24rpx; color: #2b6fe3; padding: 12rpx 0 2rpx; }
.opt-none { font-size: 23rpx; color: #a4adc0; padding: 14rpx 0; background: #f8f9fc; border-radius: 12rpx; padding: 18rpx 20rpx; }
.diff-tip {
  margin: 4rpx 0 22rpx;
  font-size: 24rpx;
  color: #b8781f;
  background: #fff7ec;
  border-radius: 12rpx;
  padding: 14rpx 20rpx;
}
.diff-amt { color: #e93323; font-weight: 700; }

.submit-btn {
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  font-size: 29rpx;
  font-weight: 600;
  height: 84rpx;
  line-height: 84rpx;
  margin-top: 14rpx;
  box-shadow: 0 10rpx 24rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}

/* ---------- 换货记录 ---------- */
.section-title { display: flex; align-items: center; margin: 14rpx 6rpx 20rpx; }
.st-bar {
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4a9df8, #2b6fe3);
  margin-right: 14rpx;
}
.st-text { font-size: 30rpx; font-weight: 700; color: #26324b; }
.st-line { flex: 1; height: 1rpx; margin-left: 20rpx; background: linear-gradient(90deg, #e3e9f4, rgba(227, 233, 244, 0)); }

.row-1 { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14rpx; }
.ex-no { font-size: 26rpx; font-weight: 700; color: #26324b; }
/* 状态签：浅底彩字 */
.ex-status {
  font-size: 22rpx;
  font-weight: 600;
  padding: 4rpx 18rpx;
  border-radius: 999rpx;
}
.st0 { background: #fff4e0; color: #d48806; }
.st1 { background: #fff4e0; color: #d48806; }
.st2 { background: #ecf3ff; color: #2b6fe3; }
.st3 { background: #ecf3ff; color: #2b6fe3; }
.st4 { background: #e9f9ec; color: #18a852; }
.st-1 { background: #ffecec; color: #f56c6c; }

.ex-row { font-size: 25rpx; color: #3d4a5f; margin-bottom: 10rpx; line-height: 36rpx; }
.ex-sub { font-size: 22rpx; color: #a4adc0; }
.grey { color: #909399; }
/* 快递/驳回提示条 */
.ex-notice {
  font-size: 23rpx;
  border-radius: 12rpx;
  padding: 12rpx 18rpx;
  margin-bottom: 10rpx;
  line-height: 34rpx;
}
.n-red { background: #ffecec; color: #d9534f; }
.n-blue { background: #f0f6ff; color: #2b6fe3; }
.diff-amt { color: #e93323; font-weight: 700; }

.row-op {
  display: flex;
  justify-content: flex-end;
  margin-top: 16rpx;
  gap: 16rpx;
}
.op-btn {
  border-radius: 999rpx;
  font-size: 24rpx;
  height: 62rpx;
  line-height: 62rpx;
  padding: 0 32rpx;
  margin: 0;
  &::after { border: none; }
  &.primary {
    background: linear-gradient(135deg, #4a9df8, #2b6fe3);
    color: #fff;
    box-shadow: 0 8rpx 18rpx rgba(43, 111, 227, 0.28);
  }
  &.soft {
    background: #f0f6ff;
    color: #2b6fe3;
    font-weight: 600;
  }
  &.danger {
    background: linear-gradient(135deg, #ff9090, #f56c6c);
    color: #fff;
    box-shadow: 0 8rpx 18rpx rgba(245, 108, 108, 0.26);
  }
}

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
