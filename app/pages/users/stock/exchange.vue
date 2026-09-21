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
            <text class="f-hint">{{ sourceType === 2 ? '虚拟换货：可选择换入实体或虚拟库存' : '实体换货：只能换入实体商品，审核通过后发货' }}</text>
          </view>
        </view>

        <!-- 虚拟库存换货：选择换实体还是换虚拟 -->
        <view v-if="sourceType === 2" class="form-item">
          <view class="f-label">换成什么</view>
          <view class="type-row">
            <view class="type-pill" :class="{ active: targetType === 1 }" @click="switchTargetType(1)">换实体库存</view>
            <view class="type-pill" :class="{ active: targetType === 2 }" @click="switchTargetType(2)">换虚拟库存</view>
          </view>
          <text class="f-hint">{{ targetType === 2 ? '换虚拟：上级审核通过后新品虚拟库存直接入账，无需地址和发货' : '换实体：审核通过后按收货地址发实物商品' }}</text>
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
          <view class="num-row">
            <view class="num-ctrl" @click="stepNum(-1)">−</view>
            <input v-model="form.num" type="number" class="f-input num-input" placeholder="1" />
            <view class="num-ctrl" @click="stepNum(1)">＋</view>
            <view v-if="quota.loaded && !quota.blocked && quota.remain > 0" class="num-max" @click="fillMax">还可申请 {{ quota.remain }} 件</view>
          </view>
          <text v-if="quota.blocked" class="f-warn">⚠ {{ quota.blockedReason || '该订单已申请过换货，不能重复申请' }}</text>
          <text v-else-if="quota.loaded && quota.remain > 0" class="f-hint">
            本单该商品共购 <text class="q-strong">{{ quota.purchased }}</text> 件，已申请换货
            <text class="q-strong">{{ quota.exchanged }}</text> 件，还可申请
            <text class="q-remain">{{ quota.remain }}</text> 件
          </text>
          <text v-else class="f-hint">换货数量按原订单累计校验：多次申请的总量不能超过购买数量</text>
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

        <!-- 收货地址：换入实体商品时必选 -->
        <view v-if="needsAddress" class="form-item">
          <view class="f-label">收货地址<text class="req-star">*</text></view>
          <view class="addr-list">
            <view v-for="a in addresses" :key="a.id" class="addr-item" :class="{ active: selectedAddressId === a.id }" @click="selectedAddressId = a.id">
              <view class="addr-check" :class="{ on: selectedAddressId === a.id }"></view>
              <view class="addr-main">
                <view class="addr-line1">{{ a.realName }} <text class="addr-phone">{{ a.phone }}</text><text v-if="a.isDefault" class="addr-def">默认</text></view>
                <view class="addr-detail">{{ addressText(a) }}</view>
              </view>
            </view>
            <view v-if="!addresses.length" class="addr-empty" @click="goAddAddress">暂无收货地址，去添加 ›</view>
          </view>
          <text class="f-hint">换入实体商品需按此地址发货，请确认信息准确</text>
        </view>

        <view class="form-item">
          <view class="f-label">换货原因</view>
          <textarea v-model="form.reason" class="f-textarea" placeholder="请描述换货原因" />
        </view>

        <button class="submit-btn" @click="submit">提交申请</button>
      </view>

      <!-- 下级待我审核提示：换货审核入口在「订单审核」 -->
      <view v-if="auditCount > 0" class="audit-tip" @click="goAudit">
        <view class="at-left">
          <view class="at-num">{{ auditCount }}</view>
          <view class="at-txt">条下级换货申请待您审核</view>
        </view>
        <view class="at-go">去处理 ›</view>
      </view>

      <!-- 换货记录：我的换货（找上级）/ 下级换货（下级找我） -->
      <view class="section-title">
        <view class="st-bar"></view>
        <text class="st-text">换货记录</text>
        <view class="st-line"></view>
      </view>

      <view class="tab-bar">
        <view class="tab-item" :class="{ on: tab === 'mine' }" @click="switchTab('mine')">我的换货</view>
        <view class="tab-item" :class="{ on: tab === 'sub' }" @click="switchTab('sub')">下级换货</view>
      </view>

      <template v-if="tab === 'mine'">
      <view v-if="list.length" class="ex-list">
        <view v-for="e in list" :key="e.id" class="ex-card">
        <view class="row-1">
          <text class="ex-no">{{ e.exchangeNo }}</text>
          <text class="ex-status" :class="diffUnpaid(e) ? 'st-pay' : ('st' + e.status)">{{ diffUnpaid(e) ? '待支付' : statusText(e.status) }}</text>
        </view>
        <!-- 原商品 / 换入商品双缩略图，样式与上级审核订单一致 -->
        <view class="row-p">
          <image v-if="e.productImage" :src="e.productImage" class="p-img" mode="aspectFill" />
          <view v-else class="p-img p-img-empty">{{ (e.productName || '?').slice(0, 1) }}</view>
          <view class="p-info">
            <view class="p-name">{{ e.productName }}</view>
            <view class="p-num">换货数量 × {{ e.num }}<text class="p-x">原单 {{ e.orderNo }}</text></view>
          </view>
        </view>
        <view v-if="e.targetProductName" class="ex-target-card">
          <view class="et-head">
            <text class="et-tag">换入</text>
            <text class="et-type">{{ e.targetStockType === 2 ? '虚拟库存' : '实体商品' }}</text>
            <text v-if="Number(e.diffPrice) > 0" class="diff-amt">需补差价 ¥{{ e.diffPrice }}</text>
            <text v-else class="same-amt">无需补差价</text>
          </view>
          <view class="row-p">
            <image v-if="e.targetProductImage" :src="e.targetProductImage" class="p-img" mode="aspectFill" />
            <view v-else class="p-img p-img-empty">{{ (e.targetProductName || '?').slice(0, 1) }}</view>
            <view class="p-info">
              <view class="p-name">{{ e.targetProductName }}</view>
              <view class="p-num">
                <text v-if="e.targetSkuName">规格：{{ e.targetSkuName }} · </text>换入价 ¥{{ e.targetPrice }}<text class="p-x">× {{ e.num }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="ex-row grey">原因：{{ e.reason }}</view>
        <view v-if="e.status === -1" class="ex-notice n-red">驳回原因：{{ e.rejectReason }}</view>
        <view v-if="e.backExpressNum" class="ex-notice n-blue">旧品退回：{{ e.backExpressName }} {{ e.backExpressNum }}</view>
        <view v-if="e.newExpressNum" class="ex-notice n-blue">新品发出：{{ e.newExpressName }} {{ e.newExpressNum }}</view>
        <view class="row-op" v-if="Number(e.diffPrice) > 0 && e.diffPayStatus !== 1 && e.status !== -1">
          <button class="op-btn primary" @click="payDiff(e)">支付差价 ¥{{ e.diffPrice }}</button>
        </view>
        <!-- 寄回地址：上级为总部取后台配置，普通上级取其默认收货地址。独立块避免被 flex 挤压 -->
        <view v-if="e.status === 2" class="ex-back-addr">
          <view class="ba-top">
            <text class="ba-head">寄回给：{{ e.backTarget || '上级/总部' }}</text>
            <view class="ba-copy" @click.stop="copyAddress(e)">一键复制</view>
          </view>
          <view class="ba-body">{{ e.backAddress || '请联系上级获取寄回地址' }}</view>
        </view>
        <view class="row-op" v-if="e.status === 2">
          <button class="op-btn primary" @click="fillBack(e)">填写旧品退回快递</button>
        </view>
        </view>
      </view>

      <view v-if="!list.length && loaded" class="empty-card">
        <view class="empty-ico">换</view>
        <view class="empty-txt">暂无换货记录</view>
        <view class="empty-sub">在订货订单中选择商品即可发起换货</view>
      </view>
      </template>

      <!-- 下级换货：下级发起、经我（上级）处理的全部换货单 -->
      <template v-else>
      <view v-if="subList.length" class="ex-list">
        <view v-for="e in subList" :key="'s' + e.id" class="ex-card sub-ex-card">
          <view class="row-1">
            <text class="ex-no">{{ e.exchangeNo }}</text>
            <text class="ex-status" :class="'st' + e.status">{{ statusText(e.status) }}</text>
          </view>
          <view class="sub-user">
            <view class="su-avatar">{{ (e.nickname || '下').slice(0, 1) }}</view>
            <text class="su-name">{{ e.nickname }}</text>
            <text class="su-tag">下级申请</text>
          </view>
          <view class="row-p">
            <image v-if="e.productImage" :src="e.productImage" class="p-img" mode="aspectFill" />
            <view v-else class="p-img p-img-empty">{{ (e.productName || '?').slice(0, 1) }}</view>
            <view class="p-info">
              <view class="p-name">{{ e.productName }}</view>
              <view class="p-num">换货数量 × {{ e.num }}<text class="p-x">原单 {{ e.orderNo }}</text></view>
            </view>
          </view>
          <view v-if="e.targetProductName" class="ex-target-card">
            <view class="et-head">
              <text class="et-tag">换入</text>
              <text class="et-type">{{ e.targetStockType === 2 ? '虚拟库存' : '实体商品' }}</text>
              <text v-if="Number(e.diffPrice) > 0" class="diff-amt">需补差价 ¥{{ e.diffPrice }}</text>
              <text v-else class="same-amt">无需补差价</text>
            </view>
            <view class="row-p">
              <image v-if="e.targetProductImage" :src="e.targetProductImage" class="p-img" mode="aspectFill" />
              <view v-else class="p-img p-img-empty">{{ (e.targetProductName || '?').slice(0, 1) }}</view>
              <view class="p-info">
                <view class="p-name">{{ e.targetProductName }}</view>
                <view class="p-num">
                  <text v-if="e.targetSkuName">规格：{{ e.targetSkuName }} · </text>换入价 ¥{{ e.targetPrice }}<text class="p-x">× {{ e.num }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="ex-row grey">原因：{{ e.reason }}</view>
          <view v-if="e.status === -1" class="ex-notice n-red">驳回原因：{{ e.rejectReason }}</view>
          <view v-if="e.backExpressNum" class="ex-notice n-blue">旧品退回：{{ e.backExpressName }} {{ e.backExpressNum }}</view>
          <view v-if="e.newExpressNum" class="ex-notice n-blue">新品发出：{{ e.newExpressName }} {{ e.newExpressNum }}</view>
          <view class="row-op" v-if="e.status === 0 || e.status === 2 || e.status === 3">
            <button class="op-btn soft" @click="goAudit">去处理 ›</button>
          </view>
        </view>
      </view>

      <view v-if="!subList.length && subLoaded" class="empty-card">
        <view class="empty-ico">下</view>
        <view class="empty-txt">下级暂无换货记录</view>
        <view class="empty-sub">下级发起的换货申请会出现在这里</view>
      </view>
      </template>
    </view>

    <!-- 旧品退回快递填写弹窗 -->
    <view v-if="backModal.show" class="mask" @click="closeBack">
      <view class="modal" @click.stop>
        <view class="modal-title">填写旧品退回快递</view>
        <view class="modal-sub">{{ backModal.exchangeNo }}</view>
        <view class="m-field">
          <view class="m-label">快递公司</view>
          <input v-model="backModal.expressName" class="m-input" placeholder="如：顺丰速运" />
        </view>
        <view class="m-field">
          <view class="m-label">快递单号</view>
          <input v-model="backModal.expressNum" class="m-input" placeholder="请输入快递单号" />
        </view>
        <view class="m-tip">请寄回旧品后填写，上级/总部将核验入库后发出新品</view>
        <view class="m-btns">
          <button class="m-btn ghost" @click="closeBack">取消</button>
          <button class="m-btn main" :class="{ disabled: !backModal.expressName || !backModal.expressNum }" @click="saveBack">确定</button>
        </view>
      </view>
    </view>

    <!-- 统一样式提示弹窗（替代 uni.showModal，可定制图标/配色/圆角） -->
    <view v-if="tipModal.show" class="mask" @click="tipModal.show = false">
      <view class="modal tip-modal" @click.stop>
        <view class="tip-ico">!</view>
        <view class="tip-title">{{ tipModal.title }}</view>
        <view class="tip-msg">{{ tipModal.message }}</view>
        <button class="m-btn main tip-btn" @click="tipModal.show = false">知道了</button>
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyExchanges, applyStockExchange, fillExchangeBackExpress, getExchangeOptions, getExchangeQuota, payExchangeDiff, getExchangeAuditList } from '@/api/stock.js';
	import { getAddressList } from '@/api/user.js';
	export default {
		data() {
			return {
			list: [],
			loaded: false,
			// 换货记录页签：mine=我的换货（找上级） sub=下级换货（下级找我）
			tab: 'mine',
			subList: [],
			subLoaded: false,
			canApply: false,
				auditCount: 0,
				options: [],
				selectedTarget: null,
				skuKeyParam: '',
				sourceType: 0,
				showAllOptions: false,
				// 换入库存类型：1=实体 2=虚拟（虚拟库存换货时由会员选择）
				targetType: 1,
				addresses: [],
				selectedAddressId: 0,
				backModal: { show: false, id: 0, exchangeNo: '', expressName: '', expressNum: '' },
				// 换货可申请余量：purchased 原单购买 / exchanged 已换 / remain 还可申请 / blocked 一单一换已占用
				quota: { purchased: 0, exchanged: 0, remain: 0, blocked: false, blockedReason: '', loaded: false },
				// 统一样式提示弹窗（替代 uni.showModal，原生弹窗样式无法定制）
				tipModal: { show: false, title: '提示', message: '' },
				form: { orderId: '', productId: '', num: '1', reason: '' }
			};
		},
		computed: {
			visibleOptions() {
				const list = this.options || [];
				return this.showAllOptions ? list : list.slice(0, 3);
			},
			// 是否需要收货地址：换入实体商品（实体换货、或虚拟换实体）
			needsAddress() {
				return this.canApply && this.targetType === 1;
			}
		},
		onLoad(opt) {
			this.sourceType = opt.type ? Number(opt.type) : 0;
			// 虚拟库存换货默认换虚拟；实体换货固定换实体
			this.targetType = this.sourceType === 2 ? 2 : 1;
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
				this.loadQuota();
			}
			if (this.needsAddress) this.loadAddresses();
			this.load();
		},
		methods: {
			statusText(s) {
				return { 0: '待上级审核', 1: '待总部审核', 2: '待旧品退回', 3: '待发新品', 4: '已完成', '-1': '已驳回' }[s] || s;
			},
			// 有补差价且未支付：状态显示「待支付」
			diffUnpaid(e) {
				return Number(e.diffPrice) > 0 && e.diffPayStatus !== 1 && e.status !== -1;
			},
			switchTargetType(t) {
				if (this.targetType === t) return;
				this.targetType = t;
				if (t === 1 && !this.addresses.length) this.loadAddresses();
			},
			addressText(a) {
				return [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ');
			},
			loadAddresses() {
				getAddressList({ page: 1, limit: 20 }).then(res => {
					const d = res.data || {};
					this.addresses = d.list || d || [];
					const def = this.addresses.find(a => a.isDefault) || this.addresses[0];
					this.selectedAddressId = def ? def.id : 0;
				}).catch(() => { this.addresses = []; });
			},
			goAddAddress() {
				uni.navigateTo({ url: '/pages/users/user_address/index' });
			},
			load() {
				getMyExchanges({ page: 1, limit: 30 }).then(res => {
					this.list = res.data.list || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
				// 下级提交、待我审核的换货单数
				getExchangeAuditList({ page: 1, limit: 1 }).then(res => {
					this.auditCount = (res.data && res.data.total) || 0;
				}).catch(() => { this.auditCount = 0; });
			},
			goAudit() {
				uni.navigateTo({ url: '/pages/users/stock/order-list?tab=audit' });
			},
			// 切换换货记录页签；下级换货懒加载（status=-2 = 下级经我处理的全部单，含已完成/已驳回）
			switchTab(t) {
				if (this.tab === t) return;
				this.tab = t;
				if (t === 'sub' && !this.subLoaded) this.loadSub();
			},
			loadSub() {
				getExchangeAuditList({ page: 1, limit: 30, status: -2 }).then(res => {
					this.subList = (res.data && res.data.list) || [];
					this.subLoaded = true;
				}).catch(() => { this.subLoaded = true; });
			},
			loadOptions() {
				if (!this.form.productId) return this.$util.Tips({ title: '请先填写商品ID' });
				getExchangeOptions(Number(this.form.productId), this.skuKeyParam || '').then(res => {
					this.options = res.data || [];
					this.selectedTarget = this.options.length ? this.options[0] : null;
					if (!this.options.length) this.$util.Tips({ title: '该商品未开放换货或暂无可换入商品' });
				}).catch(() => {});
			},
			// 可申请余量：按原订单商品数量累计校验（多次申请总量 ≤ 购买数量）
			loadQuota() {
				if (!this.form.productId) return;
				// null 值参数不传，避免被序列化成 "null" 字符串导致后端 400
				const q = {
					productId: Number(this.form.productId),
					skuKey: this.skuKeyParam || '',
					sourceStockType: this.sourceType || undefined
				};
				if (this.form.orderId) q.orderId = Number(this.form.orderId);
				getExchangeQuota(q).then(res => {
					const d = res.data || {};
					this.quota = {
						purchased: Number(d.purchased) || 0,
						exchanged: Number(d.exchanged) || 0,
						remain: Number(d.remain) || 0,
						blocked: !!d.blocked,
						blockedReason: d.blockedReason || '',
						loaded: true
					};
					if (this.quota.remain > 0 && Number(this.form.num || 1) > this.quota.remain) {
						this.form.num = String(this.quota.remain);
					}
				}).catch(() => { this.quota.loaded = false; });
			},
			stepNum(delta) {
				let n = Number(this.form.num || 1) + delta;
				if (n < 1) n = 1;
				if (this.quota.loaded && this.quota.remain > 0 && n > this.quota.remain) n = this.quota.remain;
				this.form.num = String(n);
			},
			fillMax() {
				if (this.quota.loaded && this.quota.remain > 0) this.form.num = String(this.quota.remain);
			},
			// 统一样式提示弹窗（uni.showModal 样式无法定制，长文案还会被截断）
			showTip(message, title) {
				this.tipModal = { show: true, title: title || '换货申请未提交', message: message };
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
				if (this.needsAddress && !this.selectedAddressId) return this.$util.Tips({ title: '请选择收货地址' });
				const num = Number(this.form.num || 1);
				if (!(num > 0)) return this.$util.Tips({ title: '请填写正确的换货数量' });
				if (this.quota.loaded && this.quota.blocked) {
					return this.showTip(this.quota.blockedReason || '该订单已申请过换货，不能重复申请换货');
				}
				if (this.quota.loaded && this.quota.remain > 0 && num > this.quota.remain) {
					return this.showTip('换货数量按原订单累计计算：本单共购 ' + this.quota.purchased
						+ ' 件，已申请换货 ' + this.quota.exchanged + ' 件，本次最多可申请 ' + this.quota.remain + ' 件');
				}
				applyStockExchange({
					orderId: this.form.orderId ? Number(this.form.orderId) : null,
					productId: Number(this.form.productId),
					skuKey: this.skuKeyParam || '',
					num: num,
					reason: this.form.reason,
					targetProductId: this.selectedTarget.targetProductId,
					targetSkuKey: this.selectedTarget.targetSkuKey || '',
					sourceStockType: this.sourceType || null,
					targetStockType: this.sourceType === 2 ? this.targetType : 1,
					addressId: this.needsAddress ? this.selectedAddressId : null
				}).then(() => {
					this.$util.Tips({ title: '申请已提交' });
					this.load();
					this.loadQuota();
				}).catch(err => {
					// 后端校验失败的原因必须弹给用户，否则请求被 reject 后页面毫无反应
					this.showTip(typeof err === 'string' ? err : '提交失败，请稍后重试');
				});
			},
			// 一键复制寄回地址：直接写剪贴板，避免长地址手抄出错
			copyAddress(e) {
				const txt = (e.backAddress || '').trim();
				if (!txt) return this.$util.Tips({ title: '暂无可复制的寄回地址' });
				uni.setClipboardData({
					data: txt,
					success: () => { uni.showToast({ title: '地址已复制', icon: 'none' }); }
				});
			},
			fillBack(e) {
				this.backModal = { show: true, id: e.id, exchangeNo: e.exchangeNo, expressName: e.backExpressName || '', expressNum: '' };
			},
			closeBack() {
				this.backModal.show = false;
			},
			saveBack() {
				const m = this.backModal;
				if (!m.expressName || !m.expressNum) return this.$util.Tips({ title: '请填写快递公司和单号' });
				fillExchangeBackExpress(m.id, { backExpressName: m.expressName.trim(), backExpressNum: m.expressNum.trim() }).then(() => {
					this.backModal.show = false;
					uni.showToast({ title: '已保存', icon: 'success' });
					this.load();
				});
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
/* 换货数量步进器 + 余量 */
.num-row { display: flex; align-items: center; }
.num-ctrl {
  width: 64rpx;
  height: 64rpx;
  background: #eef2f8;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  color: #4a5468;
  font-weight: 700;
  flex-shrink: 0;
}
.num-input {
  width: 140rpx;
  height: 64rpx;
  margin: 0 14rpx;
  text-align: center;
  padding: 0;
  flex-shrink: 0;
}
.num-max {
  margin-left: auto;
  background: #ecf3ff;
  color: #2b6fe3;
  font-size: 22rpx;
  font-weight: 600;
  border-radius: 999rpx;
  padding: 8rpx 20rpx;
}
.q-strong { color: #303133; font-weight: 700; }
.q-remain { color: #e93323; font-weight: 700; font-size: 26rpx; }
.f-warn {
  display: block;
  margin-top: 10rpx;
  background: #fff1f0;
  color: #d5321f;
  font-size: 23rpx;
  line-height: 34rpx;
  border-radius: 10rpx;
  padding: 12rpx 16rpx;
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

/* ---------- 下级待我审核提示条 ---------- */
.audit-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fff7ec, #fffdf8);
  border: 2rpx solid #ffe0b8;
  border-radius: 18rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 22rpx;
}
.at-left { display: flex; align-items: center; }
.at-num {
  min-width: 44rpx;
  height: 44rpx;
  line-height: 44rpx;
  text-align: center;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ffb54d, #f08c2e);
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
  padding: 0 8rpx;
}
.at-txt { margin-left: 14rpx; font-size: 25rpx; color: #b8781f; font-weight: 600; }
.at-go { font-size: 24rpx; color: #f08c2e; }

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

/* 记录页签：我的换货 / 下级换货 */
.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.tab-item {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  font-size: 26rpx;
  font-weight: 600;
  color: #606266;
  border-radius: 12rpx;
  &.on {
    background: linear-gradient(135deg, #4a9df8, #2b6fe3);
    color: #fff;
    box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.25);
  }
}

/* 下级换货卡：橙色左条与我的换货区分 */
.sub-ex-card { border-left: 6rpx solid #ffb54d; }
.sub-user {
  display: flex;
  align-items: center;
  margin-bottom: 4rpx;
}
.su-avatar {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb056, #f08a1d);
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.su-name { margin-left: 12rpx; font-size: 24rpx; color: #3d4a5f; font-weight: 600; }
.su-tag {
  margin-left: 12rpx;
  font-size: 20rpx;
  color: #b8781f;
  background: #fff4e0;
  border-radius: 8rpx;
  padding: 2rpx 12rpx;
}

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
/* 待旧品退回：寄回地址块 */
.ex-back-addr {
  width: 100%;
  background: #f0f6ff;
  border: 1rpx solid #cfe3ff;
  border-radius: 14rpx;
  padding: 18rpx 20rpx;
  margin-top: 16rpx;
}
.ex-back-addr .ba-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}
.ex-back-addr .ba-head {
  font-size: 24rpx;
  font-weight: 600;
  color: #2b6fe3;
  flex: 1;
  min-width: 0;
}
/* 一键复制：右上角小胶囊 */
.ex-back-addr .ba-copy {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 21rpx;
  font-weight: 600;
  color: #2b6fe3;
  background: #fff;
  border: 1rpx solid #bcd8ff;
  border-radius: 999rpx;
  padding: 5rpx 18rpx;
}
.ex-back-addr .ba-body {
  font-size: 24rpx;
  color: #303133;
  line-height: 36rpx;
  word-break: break-all;
}
.n-red { background: #ffecec; color: #d9534f; }
.n-blue { background: #f0f6ff; color: #2b6fe3; }
.diff-amt { color: #e93323; font-weight: 700; }

.row-op {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
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
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

/* ---------- 换货类型选择 ---------- */
.type-row { display: flex; gap: 18rpx; }
.type-pill {
  flex: 1;
  height: 76rpx;
  line-height: 76rpx;
  text-align: center;
  border-radius: 16rpx;
  border: 2rpx solid #e3e9f4;
  background: #f8fafc;
  font-size: 26rpx;
  color: #606266;
  font-weight: 600;
  &.active {
    border-color: #2b6fe3;
    background: #f0f6ff;
    color: #2b6fe3;
    box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.12);
  }
}

/* ---------- 收货地址 ---------- */
.req-star { color: #e93323; margin-left: 4rpx; }
.addr-list { display: flex; flex-direction: column; gap: 14rpx; }
.addr-item {
  display: flex;
  align-items: center;
  border: 2rpx solid #eef1f6;
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  background: #fff;
  &.active {
    border-color: #2b6fe3;
    background: #f4f9ff;
  }
}
.addr-check {
  flex-shrink: 0;
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  border: 2rpx solid #d5dce8;
  margin-right: 16rpx;
  position: relative;
  &.on {
    border-color: #2b6fe3;
    background: #2b6fe3;
    &::after {
      content: '';
      position: absolute;
      left: 10rpx;
      top: 7rpx;
      width: 10rpx;
      height: 17rpx;
      border-right: 4rpx solid #fff;
      border-bottom: 4rpx solid #fff;
      transform: rotate(45deg);
    }
  }
}
.addr-main { flex: 1; min-width: 0; overflow: hidden; }
.addr-line1 { font-size: 26rpx; color: #26324b; font-weight: 600; }
.addr-phone { font-size: 23rpx; color: #909399; font-weight: 400; margin-left: 12rpx; }
.addr-def {
  margin-left: 12rpx;
  font-size: 20rpx;
  color: #d48806;
  background: #fff4e0;
  border-radius: 8rpx;
  padding: 2rpx 12rpx;
}
.addr-detail {
  margin-top: 8rpx;
  font-size: 23rpx;
  color: #606266;
  line-height: 34rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.addr-empty {
  font-size: 25rpx;
  color: #2b6fe3;
  background: #f0f6ff;
  border-radius: 14rpx;
  padding: 24rpx;
  text-align: center;
}

/* ---------- 旧品退回快递弹窗 ---------- */
.mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 25, 45, 0.55);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 70rpx;
}
.modal {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 36rpx 32rpx;
}
.modal-title { font-size: 32rpx; font-weight: 700; color: #26324b; text-align: center; }
.modal-sub { margin-top: 8rpx; font-size: 22rpx; color: #a4adc0; text-align: center; }
/* 样式化提示弹窗 */
.tip-modal { padding: 44rpx 36rpx 36rpx; display: flex; flex-direction: column; align-items: center; }
.tip-ico {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb056, #f08a1d);
  color: #fff;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding-bottom: 8rpx;
  box-shadow: 0 10rpx 24rpx rgba(240, 138, 29, 0.30);
}
.tip-title { margin-top: 24rpx; font-size: 32rpx; font-weight: 700; color: #26324b; }
.tip-msg {
  margin-top: 16rpx;
  font-size: 25rpx;
  color: #5b6678;
  line-height: 40rpx;
  text-align: center;
  word-break: break-all;
}
.tip-btn { margin-top: 34rpx; width: 100%; }
.m-field { margin-top: 26rpx; }
.m-label { font-size: 24rpx; color: #3d4a5f; font-weight: 600; margin-bottom: 10rpx; }
.m-input {
  background: #f4f6f9;
  border-radius: 14rpx;
  height: 78rpx;
  padding: 0 24rpx;
  font-size: 27rpx;
  color: #303133;
}
.m-tip { margin-top: 20rpx; font-size: 21rpx; color: #a0a6b0; line-height: 32rpx; }
.m-btns { display: flex; gap: 20rpx; margin-top: 34rpx; }
.m-btn {
  flex: 1;
  height: 78rpx;
  line-height: 78rpx;
  border-radius: 999rpx;
  font-size: 27rpx;
  font-weight: 600;
  margin: 0;
  padding: 0;
  &::after { border: none; }
  &.ghost { background: #f2f4f8; color: #606266; }
  &.main {
    background: linear-gradient(135deg, #4a9df8, #2b6fe3);
    color: #fff;
    box-shadow: 0 8rpx 18rpx rgba(43, 111, 227, 0.28);
    &.disabled { opacity: 0.5; box-shadow: none; }
  }
}

/* ---------- 待支付状态签 ---------- */
.st-pay { background: #ffecec; color: #e93323; }

/* ---------- 商品行 / 换入商品卡（与上级审核订单样式一致） ---------- */
.row-p {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
}
.p-img {
  width: 104rpx;
  height: 104rpx;
  border-radius: 14rpx;
  flex-shrink: 0;
  background: #f5f6fa;
}
.p-img-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c3cad6;
  font-size: 40rpx;
  font-weight: 700;
}
.p-info {
  flex: 1;
  margin: 0 18rpx;
  overflow: hidden;
}
.p-name {
  font-size: 26rpx;
  color: #303133;
  line-height: 36rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.p-num { font-size: 22rpx; color: #909399; margin-top: 8rpx; }
.p-x { color: #b8bfc9; margin-left: 10rpx; }

/* 换入商品卡：橙底与上方原商品区分 */
.ex-target-card {
  margin-top: 16rpx;
  background: #fff8ee;
  border-radius: 14rpx;
  padding: 16rpx 18rpx 18rpx;
  border: 1rpx solid #ffe3bd;
}
.et-head {
  display: flex;
  align-items: center;
}
.et-tag {
  background: linear-gradient(135deg, #ffb056, #f08a1d);
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
  border-radius: 8rpx;
  padding: 3rpx 12rpx;
  margin-right: 12rpx;
}
.et-type { font-size: 22rpx; color: #b8781f; font-weight: 600; }
.et-head .diff-amt { margin-left: auto; padding-left: 12rpx; font-size: 23rpx; }
.same-amt { color: #21a84f; margin-left: auto; padding-left: 12rpx; font-weight: 600; font-size: 23rpx; }
/* 卡片内嵌商品行不再需要额外上间距 */
.ex-target-card .row-p { margin-top: 14rpx; }
</style>
