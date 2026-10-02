<template>
	<view :data-theme="theme">
		<view class="cash-page">
			<!-- 顶部：提现类型切换 -->
			<view class="seg-switch">
				<view class="seg-item" :class="{ on: extractCategory === 'brokerage' }" @click="switchCategory('brokerage')">佣金提现</view>
				<view class="seg-item" :class="{ on: extractCategory === 'balance' }" @click="switchCategory('balance')">余额提现</view>
			</view>

			<!-- 金额卡 -->
			<view class="amount-card" :style="gradBg">
				<view class="a-label">{{ extractCategory === 'balance' ? '可提现余额' : '可提现佣金' }}</view>
				<view class="a-num">
					<text class="yen">￥</text>
					<text class="num">{{ amountText }}</text>
				</view>
				<view class="a-sub" v-if="extractCategory === 'brokerage'">
					<text class="frozen-badge">冻结佣金 ￥{{ frozenText }}</text>
				</view>
				<view class="a-sub" v-else>审核通过后到账原收款账户</view>
			</view>

			<!-- 提现方式 + 表单 -->
			<view class="method-card">
				<view class="method-tabs">
					<view v-for="(m, i) in methodList" :key="i" class="method-tab" :class="{ on: methodTab === i }" @click="methodTab = i">
						<text class="iconfont" :class="m.icon"></text>
						<text class="t">{{ m.name }}</text>
					</view>
				</view>

				<!-- 回填提示 -->
				<view class="fill-tip" v-if="filledTip">已自动带入上次收款信息，确认无误后提交</view>

				<view class="form-box">
					<!-- 银行卡 -->
					<block v-if="methodTab === 0">
						<view class="form-item">
							<view class="f-name">持卡人</view>
							<view class="f-input">
								<input v-model="form.realName" placeholder="请输入持卡人姓名" placeholder-class="placeholder" maxlength="20" />
							</view>
						</view>
						<view class="form-item">
							<view class="f-name">卡号</view>
							<view class="f-input">
								<input v-model="form.bankCode" type="number" placeholder="请填写银行卡号" placeholder-class="placeholder" maxlength="19" />
							</view>
						</view>
						<view class="form-item" @click="openPicker">
							<view class="f-name">银行</view>
							<view class="f-input picker-line">
								<text class="bank-text" :class="{ ph: index === 0 }">{{ array[index] }}</text>
								<text class="iconfont icon-qiepian38"></text>
							</view>
							<picker class="picker-hit" @change="bindPickerChange" :value="index" :range="array"></picker>
						</view>
					</block>

					<!-- 微信 -->
					<block v-else-if="methodTab === 1">
						<view class="form-item">
							<view class="f-name">微信号</view>
							<view class="f-input">
								<input v-model="form.wechat" placeholder="请填写您的微信账号" placeholder-class="placeholder" maxlength="20" />
							</view>
						</view>
						<view class="form-item top">
							<view class="f-name">收款码</view>
							<view class="f-input acea-row">
								<view class="picEwm" v-if="qrcodeUrlW">
									<image :src="qrcodeUrlW"></image>
									<text class="iconfont icon-guanbi1 del" @click="DelPicW"></text>
								</view>
								<view class="pictrue acea-row row-center-wrapper row-column" @click="uploadpic('W')" v-else>
									<text class="iconfont icon-icon25201"></text>
									<view>上传图片</view>
								</view>
							</view>
						</view>
					</block>

					<!-- 支付宝 -->
					<block v-else>
						<view class="form-item">
							<view class="f-name">账号</view>
							<view class="f-input">
								<input v-model="form.alipayCode" placeholder="请填写您的支付宝账号" placeholder-class="placeholder" maxlength="30" />
							</view>
						</view>
						<view class="form-item top">
							<view class="f-name">收款码</view>
							<view class="f-input acea-row">
								<view class="picEwm" v-if="qrcodeUrlZ">
									<image :src="qrcodeUrlZ"></image>
									<text class="iconfont icon-guanbi1 del" @click="DelPicZ"></text>
								</view>
								<view class="pictrue acea-row row-center-wrapper row-column" @click="uploadpic('Z')" v-else>
									<text class="iconfont icon-icon25201"></text>
									<view>上传图片</view>
								</view>
							</view>
						</view>
					</block>

					<!-- 提现金额 -->
					<view class="form-item">
						<view class="f-name">提现金额</view>
						<view class="f-input">
							<input v-model="money" type="digit" :placeholder="'最低提现金额 ￥' + minPrice" placeholder-class="placeholder" />
						</view>
					</view>
				</view>

				<button class="submit-btn" @click="subCash">{{ submitText }}</button>
			</view>

			<!-- 佣金转余额（仅佣金提现态展示） -->
			<view class="transfer-card" v-if="extractCategory === 'brokerage'">
				<view class="t-head">
					<view class="t-left">
						<view class="t-title">佣金转余额</view>
						<view class="t-desc">实时到账，无需审核 · 当前余额 ￥{{ nowMoneyText }}</view>
					</view>
					<view class="t-btn" @click="transferOpen = !transferOpen">{{ transferOpen ? '收起' : '转入' }}</view>
				</view>
				<view class="t-body" v-if="transferOpen">
					<input v-model="transferPrice" type="digit" class="t-input" placeholder="请输入转入金额" placeholder-class="placeholder" />
					<view class="t-confirm" @click="doTransfer">确认转入</view>
				</view>
			</view>

			<!-- 提现说明 -->
			<view class="rule-card">
				<view class="r-title">提现说明</view>
				<view class="r-item" v-if="extractCategory === 'brokerage'">· 每笔佣金的冻结期为{{ commission.brokenDay }}天，到期后可提现</view>
				<view class="r-item" v-else>· 从账户余额发起提现申请，审核通过后到账</view>
				<view class="r-item">· 单笔最低提现金额 ￥{{ minPrice }}</view>
				<view class="r-item" v-if="commission.extractWeekdaysTip || commission.extractTimeTip">
					· 可提现时间：{{ commission.extractWeekdaysTip }} {{ commission.extractTimeTip }}
				</view>
				<view class="r-item">· 收款信息会自动保存，下次提现无需重复填写</view>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		extractCash,
		extractBank,
		extractUser,
		transferIn
	} from '@/api/user.js';
	import {
		toLogin
	} from '@/libs/login.js';
	import {
		mapGetters
	} from "vuex";
	import {
		Debounce
	} from '@/utils/validate.js';
	import {
		setThemeColor
	} from '@/utils/setTheme.js';
	const STORAGE_KEY = 'extract_pay_info';
	let app = getApp();
	export default {
		data() {
			return {
				methodList: [{
						'name': '银行卡',
						'icon': 'icon-yinhangqia'
					},
					{
						'name': '微信',
						'icon': 'icon-weixin2'
					},
					{
						'name': '支付宝',
						'icon': 'icon-icon34'
					}
				],
				methodTab: 0,
				extractCategory: 'brokerage',
				index: 0,
				array: [], //提现银行
				minPrice: 0.00, //最低提现金额
				commission: {},
				form: {
					realName: '',
					bankCode: '',
					wechat: '',
					alipayCode: ''
				},
				money: '',
				qrcodeUrlW: "",
				qrcodeUrlZ: "",
				filledTip: false, //是否已自动带入收款信息
				transferOpen: false,
				transferPrice: '',
				isCommitted: false, //防止多次提交
				bgColor: '#e93323',
				theme: app.globalData.theme
			};
		},
		computed: {
			...mapGetters(['isLogin', 'userInfo']),
			availableAmount() {
				if (this.extractCategory === 'balance') {
					return Number(this.commission.balanceCount) || 0;
				}
				return Number(this.commission.commissionCount) || 0;
			},
			amountText() {
				return this.availableAmount.toFixed(2);
			},
			frozenText() {
				return (Number(this.commission.brokenCommission) || 0).toFixed(2);
			},
			nowMoneyText() {
				return (Number(this.userInfo.nowMoney) || 0).toFixed(2);
			},
			gradBg() {
				return {
					background: 'linear-gradient(160deg, ' + this.lightenHex(this.bgColor, 0.22) + ' 0%, ' + this.bgColor + ' 100%)'
				};
			},
			submitText() {
				let way = this.methodList[this.methodTab] ? this.methodList[this.methodTab].name : '';
				return '提现到' + way;
			}
		},
		watch: {
			isLogin: {
				handler: function(newV) {
					if (newV) {
						this.getUserExtractBank();
						this.getExtractUser();
					}
				},
				deep: true
			}
		},
		onLoad(options) {
			this.bgColor = setThemeColor();
			if (options && options.category === 'balance') {
				this.extractCategory = 'balance';
				uni.setNavigationBarTitle({
					title: '余额提现'
				});
			}
			if (this.isLogin) {
				this.getUserExtractBank();
				this.getExtractUser();
			} else {
				toLogin();
			}
		},
		methods: {
			/* ===== 主题色派生 ===== */
			lightenHex: function(hex, ratio) {
				let c = this.hexToRgb(hex);
				if (!c) return hex;
				let r = Math.round(c.r + (255 - c.r) * ratio);
				let g = Math.round(c.g + (255 - c.g) * ratio);
				let b = Math.round(c.b + (255 - c.b) * ratio);
				return '#' + [r, g, b].map(function(v) {
					return ('0' + v.toString(16)).slice(-2);
				}).join('');
			},
			hexToRgb: function(hex) {
				if (!hex || typeof hex !== 'string') return null;
				let h = hex.replace('#', '');
				if (h.length === 3) h = h.split('').map(function(s) {
					return s + s;
				}).join('');
				if (h.length !== 6) return null;
				return {
					r: parseInt(h.slice(0, 2), 16),
					g: parseInt(h.slice(2, 4), 16),
					b: parseInt(h.slice(4, 6), 16)
				};
			},
			/* ===== 数据加载 ===== */
			switchCategory(cat) {
				if (this.extractCategory === cat) return;
				this.extractCategory = cat;
				uni.setNavigationBarTitle({
					title: cat === 'balance' ? '余额提现' : '佣金提现'
				});
				this.getExtractUser();
			},
			getExtractUser() {
				let that = this;
				extractUser(that.extractCategory).then(res => {
					that.commission = res.data || {};
					that.minPrice = that.commission.minPrice;
					that.applyHistory({
						extractType: that.commission.lastExtractType,
						realName: that.commission.lastRealName,
						bankCode: that.commission.lastBankCode,
						bankName: that.commission.lastBankName,
						wechat: that.commission.lastWechat,
						alipayCode: that.commission.lastAlipayCode,
						qrcodeUrl: that.commission.lastQrcodeUrl
					});
					// 接口无历史时，本地缓存兜底
					if (!that.commission.lastExtractType) {
						try {
							let cached = uni.getStorageSync(STORAGE_KEY);
							if (cached && cached.extractType) {
								that.applyHistory(cached);
							}
						} catch (e) {}
					}
				});
			},
			getUserExtractBank: function() {
				let that = this;
				extractBank().then(res => {
					let array = res.data || [];
					array.unshift("请选择银行");
					that.$set(that, 'array', array);
					// 银行列表就绪后，补一次银行名回填
					let lastBankName = that.commission.lastBankName;
					if (!lastBankName) {
						try {
							let cached = uni.getStorageSync(STORAGE_KEY);
							if (cached && cached.bankName) lastBankName = cached.bankName;
						} catch (e) {}
					}
					if (lastBankName) that.applyBankName(lastBankName);
				});
			},
			/* ===== 收款信息回填 ===== */
			applyHistory(d) {
				if (!d) return;
				let hit = false;
				if (d.realName && !this.form.realName) {
					this.form.realName = d.realName;
					hit = true;
				}
				if (d.bankCode && !this.form.bankCode) {
					this.form.bankCode = d.bankCode;
					hit = true;
				}
				if (d.wechat && !this.form.wechat) {
					this.form.wechat = d.wechat;
					hit = true;
				}
				if (d.alipayCode && !this.form.alipayCode) {
					this.form.alipayCode = d.alipayCode;
					hit = true;
				}
				if (d.bankName) this.applyBankName(d.bankName);
				if (d.qrcodeUrl) {
					if (d.extractType === 'weixin' && !this.qrcodeUrlW) {
						this.qrcodeUrlW = d.qrcodeUrl;
						hit = true;
					} else if (d.extractType === 'alipay' && !this.qrcodeUrlZ) {
						this.qrcodeUrlZ = d.qrcodeUrl;
						hit = true;
					}
				}
				if (hit) this.filledTip = true;
			},
			applyBankName(bankName) {
				if (!bankName || this.index > 0) return;
				let i = this.array.indexOf(bankName);
				if (i > 0) this.index = i;
			},
			/* ===== 收款信息保存（提交成功后本地缓存兜底） ===== */
			saveHistory(value) {
				try {
					uni.setStorageSync(STORAGE_KEY, {
						extractType: value.extractType,
						realName: this.form.realName,
						bankCode: this.form.bankCode,
						bankName: value.bankName,
						wechat: this.form.wechat,
						alipayCode: this.form.alipayCode,
						qrcodeUrl: value.qrcodeUrl
					});
				} catch (e) {}
			},
			/* ===== 收款码 ===== */
			uploadpic: function(type) {
				let that = this;
				that.$util.uploadImageOne({
					url: 'upload/image',
					name: 'multipart',
					model: "user",
					pid: 1
				}, function(res) {
					if (type === 'W') {
						that.qrcodeUrlW = res.data.url;
					} else {
						that.qrcodeUrlZ = res.data.url;
					}
				});
			},
			DelPicW: function() {
				this.qrcodeUrlW = "";
			},
			DelPicZ: function() {
				this.qrcodeUrlZ = "";
			},
			openPicker() {
				// picker 由覆盖层触发，iOS 下保证点击整行可弹出
			},
			bindPickerChange: function(e) {
				this.index = Number(e.detail.value);
			},
			/* ===== 佣金转余额 ===== */
			doTransfer: Debounce(function() {
				let that = this;
				let price = parseFloat(that.transferPrice);
				if (!price || price <= 0) return that.$util.Tips({
					title: '请输入正确的转入金额'
				});
				if (price > that.availableAmount) return that.$util.Tips({
					title: '转入金额不能超过可提现佣金'
				});
				if (that.isCommitted) return;
				that.isCommitted = true;
				transferIn({
					price: price
				}).then(() => {
					that.$store.commit("changInfo", {
						amount1: 'brokeragePrice',
						amount2: that.$util.$h.Sub(that.userInfo.brokeragePrice, price)
					});
					that.transferOpen = false;
					that.transferPrice = '';
					that.isCommitted = false;
					that.getExtractUser();
					return that.$util.Tips({
						title: '转入成功',
						icon: 'success'
					});
				}).catch(err => {
					that.isCommitted = false;
					return that.$util.Tips({
						title: err
					});
				});
			}),
			/* ===== 提交提现 ===== */
			subCash: Debounce(function() {
				let that = this,
					value = {};
				if (that.methodTab === 0) { //银行卡
					if (!that.form.realName) return that.$util.Tips({
						title: '请填写持卡人姓名'
					});
					if (!that.form.bankCode) return that.$util.Tips({
						title: '请填写卡号'
					});
					if (that.index === 0) return that.$util.Tips({
						title: '请选择银行'
					});
					value.extractType = 'bank';
					value.name = that.form.realName;
					value.cardum = that.form.bankCode;
					value.bankName = that.array[that.index];
					value.qrcodeUrl = '';
				} else if (that.methodTab === 1) { //微信
					if (!that.form.wechat) return that.$util.Tips({
						title: '请填写微信号'
					});
					value.extractType = 'weixin';
					value.name = that.form.realName || that.form.wechat;
					value.wechat = that.form.wechat;
					value.qrcodeUrl = that.qrcodeUrlW;
				} else { //支付宝
					if (!that.form.alipayCode) return that.$util.Tips({
						title: '请填写支付宝账号'
					});
					value.extractType = 'alipay';
					value.name = that.form.realName || that.form.alipayCode;
					value.alipayCode = that.form.alipayCode;
					value.qrcodeUrl = that.qrcodeUrlZ;
				}
				if (!that.money) return that.$util.Tips({
					title: '请填写提现金额'
				});
				if (!(/^(\d?)+(\.\d{0,2})?$/.test(that.money))) return that.$util.Tips({
					title: '提现金额保留2位小数'
				});
				if (parseFloat(that.money) < parseFloat(that.minPrice)) return that.$util.Tips({
					title: '提现金额不能低于' + that.minPrice
				});
				if (that.commission && that.commission.extractTimeAllowed === false) {
					return that.$util.Tips({
						title: '当前不在可提现时间：' + (that.commission.extractWeekdaysTip || '') + ' ' + (that.commission.extractTimeTip || '')
					});
				}
				if (that.isCommitted) return;
				that.isCommitted = true;
				value.money = that.money;
				value.extractCategory = that.extractCategory;
				extractCash(value).then(() => {
					that.saveHistory(value);
					return that.$util.Tips({
						title: "提现成功",
						icon: 'success'
					}, {
						tab: 2,
						url: that.extractCategory === 'balance' ?
							'/pages/users/user_money/index' :
							'/pages/promoter/user_spread_user/index'
					});
				}).catch(err => {
					that.isCommitted = false;
					return that.$util.Tips({
						title: err
					});
				});
			})
		}
	}
</script>

<style lang="scss" scoped>
	page {
		background-color: #f5f5f5 !important;
	}

	.cash-page {
		min-height: 100vh;
		padding: 24rpx 24rpx 50rpx 24rpx;
		box-sizing: border-box;
	}

	/* ===== 顶部类型切换 ===== */
	.cash-page .seg-switch {
		display: flex;
		background-color: #fff;
		border-radius: 44rpx;
		padding: 8rpx;
		margin-bottom: 24rpx;
	}

	.cash-page .seg-switch .seg-item {
		flex: 1;
		height: 64rpx;
		line-height: 64rpx;
		text-align: center;
		font-size: 28rpx;
		color: #666;
		border-radius: 36rpx;
		transition: all 0.25s;
	}

	.cash-page .seg-switch .seg-item.on {
		@include main_bg_color(theme);
		color: #fff;
		font-weight: 600;
	}

	/* ===== 金额卡 ===== */
	.cash-page .amount-card {
		border-radius: 32rpx;
		padding: 36rpx 32rpx;
		color: #fff;
		margin-bottom: 24rpx;
		box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
	}

	.cash-page .amount-card .a-label {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.85);
	}

	.cash-page .amount-card .a-num {
		display: flex;
		align-items: baseline;
		margin-top: 16rpx;
	}

	.cash-page .amount-card .a-num .yen {
		font-size: 40rpx;
		font-weight: 600;
		margin-right: 4rpx;
	}

	.cash-page .amount-card .a-num .num {
		font-size: 72rpx;
		font-weight: 700;
		line-height: 1.1;
	}

	.cash-page .amount-card .a-sub {
		margin-top: 16rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
	}

	.cash-page .amount-card .a-sub .frozen-badge {
		display: inline-block;
		padding: 6rpx 20rpx;
		background-color: rgba(255, 255, 255, 0.18);
		border-radius: 24rpx;
	}

	/* ===== 方式卡 ===== */
	.cash-page .method-card {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 8rpx 30rpx 40rpx 30rpx;
	}

	.cash-page .method-card .method-tabs {
		display: flex;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.cash-page .method-card .method-tabs .method-tab {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 96rpx;
		font-size: 28rpx;
		color: #666;
		position: relative;
	}

	.cash-page .method-card .method-tabs .method-tab .iconfont {
		font-size: 32rpx;
		margin-right: 8rpx;
	}

	.cash-page .method-card .method-tabs .method-tab.on {
		@include main_color(theme);
		font-weight: 600;
	}

	.cash-page .method-card .method-tabs .method-tab.on:after {
		content: "";
		position: absolute;
		left: 50%;
		bottom: 0;
		transform: translateX(-50%);
		width: 48rpx;
		height: 6rpx;
		border-radius: 6rpx;
		@include main_bg_color(theme);
	}

	/* 回填提示 */
	.cash-page .method-card .fill-tip {
		margin-top: 20rpx;
		padding: 14rpx 20rpx;
		background-color: #fff7e6;
		border: 1rpx solid #ffe1b3;
		border-radius: 12rpx;
		font-size: 24rpx;
		color: #b26a00;
	}

	/* 表单 */
	.cash-page .method-card .form-box .form-item {
		display: flex;
		align-items: center;
		min-height: 100rpx;
		border-bottom: 1rpx solid #f0f0f0;
		font-size: 30rpx;
		color: #333;
		position: relative;
	}

	.cash-page .method-card .form-box .form-item.top {
		align-items: flex-start;
		padding-top: 24rpx;
	}

	.cash-page .method-card .form-box .form-item .f-name {
		width: 150rpx;
		flex-shrink: 0;
	}

	.cash-page .method-card .form-box .form-item .f-input {
		flex: 1;
	}

	.cash-page .method-card .form-box .form-item .f-input input {
		width: 100%;
		height: 100rpx;
		font-size: 30rpx;
	}

	.cash-page .method-card .form-box .form-item .f-input .placeholder {
		color: #bbb;
	}

	.cash-page .method-card .form-box .form-item .picker-line {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 100rpx;
	}

	.cash-page .method-card .form-box .form-item .picker-line .bank-text.ph {
		color: #bbb;
	}

	.cash-page .method-card .form-box .form-item .picker-line .iconfont {
		color: #ccc;
		font-size: 26rpx;
	}

	.cash-page .method-card .form-box .form-item .picker-hit {
		position: absolute;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		opacity: 0;
	}

	/* 收款码 */
	.cash-page .method-card .form-box .form-item .picEwm,
	.cash-page .method-card .form-box .form-item .pictrue {
		width: 140rpx;
		height: 140rpx;
		border-radius: 8rpx;
		position: relative;
		margin-right: 23rpx;
	}

	.cash-page .method-card .form-box .form-item .picEwm image {
		width: 100%;
		height: 100%;
		border-radius: 8rpx;
	}

	.cash-page .method-card .form-box .form-item .picEwm .del {
		position: absolute;
		right: -14rpx;
		top: -16rpx;
		font-size: 40rpx;
		@include main_color(theme);
	}

	.cash-page .method-card .form-box .form-item .pictrue {
		border: 1px solid #ddd;
		font-size: 22rpx;
		color: #bbb;
	}

	.cash-page .method-card .form-box .form-item .pictrue .icon-icon25201 {
		font-size: 47rpx;
		color: #ddd;
		margin-bottom: 6rpx;
	}

	/* 提交按钮 */
	.cash-page .method-card .submit-btn {
		margin-top: 44rpx;
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		font-size: 32rpx;
		color: #fff;
		border-radius: 44rpx;
		@include main_bg_color(theme);
	}

	/* ===== 佣金转余额 ===== */
	.cash-page .transfer-card {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 30rpx;
		margin-top: 24rpx;
	}

	.cash-page .transfer-card .t-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.cash-page .transfer-card .t-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #282828;
	}

	.cash-page .transfer-card .t-desc {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #999;
	}

	.cash-page .transfer-card .t-btn {
		padding: 12rpx 36rpx;
		border-radius: 32rpx;
		font-size: 26rpx;
		@include main_color(theme);
		@include coupons_border_color(theme);
		border: 1rpx solid transparent;
		background-color: rgba(0, 0, 0, 0.02);
	}

	.cash-page .transfer-card .t-body {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
	}

	.cash-page .transfer-card .t-body .t-input {
		flex: 1;
		height: 76rpx;
		padding: 0 24rpx;
		background-color: #f7f7f7;
		border-radius: 12rpx;
		font-size: 28rpx;
	}

	.cash-page .transfer-card .t-body .t-confirm {
		margin-left: 20rpx;
		padding: 0 36rpx;
		height: 76rpx;
		line-height: 76rpx;
		border-radius: 38rpx;
		font-size: 26rpx;
		color: #fff;
		@include main_bg_color(theme);
	}

	/* ===== 提现说明 ===== */
	.cash-page .rule-card {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 30rpx;
		margin-top: 24rpx;
	}

	.cash-page .rule-card .r-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #282828;
		margin-bottom: 16rpx;
	}

	.cash-page .rule-card .r-item {
		font-size: 24rpx;
		color: #999;
		line-height: 44rpx;
	}
</style>
