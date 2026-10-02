<template>
	<view class="pg-page">
		<!-- 顶部：该分组的装修内容（后台「商品分组 → 页面装修」产出） -->
		<PageDesign
			v-if="pageShow"
			:style="colorStyle"
			:diyData="currentDiyData"
			:isHome="true"
			:microPage="true"
			:isScrolled="isScrolled"
			:isFixed="false"
			:belongIndex="belongIndex"
			@bindHeight="bindHeight"
			@storeTap="storeTap"
			@changeLogin="changeLogin"
			@changeBarg="changeBarg"
			@newDataStatus="newDataStatus"
			@reconnect="reconnect"
		/>

		<!-- 分组商品列表：装修里已放「绑定本分组的商品选项卡」时，位置交给装修，这里不再追加 -->
		<view v-if="pageShow && !diyHasGroupGoods" class="pg-goods">
			<view class="pg-goods__hd">
				<text class="pg-goods__title">{{ groupName || '商品列表' }}</text>
				<text v-if="minBuy > 1" class="pg-goods__tip">{{ minBuy }} 件起售</text>
			</view>

			<view v-if="goodsList.length" class="pg-goods__list" :class="layoutClass">
				<view
					class="goods-item"
					v-for="item in goodsList"
					:key="item.id"
					@click="goDetail(item)"
				>
					<view class="goods-item__img">
						<image :src="item.image" mode="aspectFill" />
						<image v-if="badge" class="goods-item__badge" :src="badge" mode="aspectFit" />
					</view>
					<view class="goods-item__body">
						<view class="goods-item__name" :class="{ 'is-multi': titleMulti }">
							{{ item.store_name }}
						</view>
						<view class="goods-item__price">
							<text class="now">￥{{ formatPrice(item.price) }}</text>
							<text v-if="Number(item.ot_price) > 0" class="old">￥{{ formatPrice(item.ot_price) }}</text>
						</view>
						<view class="goods-item__sold">已售{{ item.sales || 0 }}{{ item.unit_name || '' }}</view>
					</view>
				</view>
			</view>

			<view v-else class="pg-goods__empty">该分组暂无商品</view>
		</view>
	</view>
</template>

<script>
import colors from '@/mixins/color';
import themePage from '@/mixins/themePage.js';
import PageDesign from '@/subpackage/diyComponents/pageDesign.vue';
import { getProductGroupDetail, getThemeProduct } from '@/api/api.js';
import { toLogin } from '@/libs/login.js';

// 装修器里「商品选项卡」在 H5 渲染器中叫 promotionList（与 pageDesign.vue 的 COMPONENT_NAME_MAP 一致）
const DIY_GOODS_COMPONENT = 'promotionList';
const DIY_GOODS_LEGACY_NAME = 'home_product';
// 「选择方式」= 指定分组
const DIY_GOODS_FROM_GROUP = 5;

function parseMaybeJson(value) {
	if (!value || typeof value !== 'string') return value;
	try {
		return JSON.parse(value);
	} catch (e) {
		return value;
	}
}

export default {
	mixins: [colors, themePage],
	components: {
		PageDesign,
	},
	data() {
		return {
			pageShow: false,
			currentDiyData: {},
			groupId: 0,
			groupName: '',
			badge: '',
			layout: 'double',
			titleMulti: false,
			minBuy: 1,
			goodsList: [],
			/** 装修数据里是否已有「绑定本分组的商品选项卡」，有则不再自动追加商品区 */
			diyHasGroupGoods: false,
			isScrolled: false,
			belongIndex: 0,
		};
	},
	computed: {
		layoutClass() {
			return this.layout === 'single' ? 'is-single' : 'is-double';
		},
	},
	onLoad(options) {
		this.groupId = Number(options.id || options.group_id || 0);
		this.init();
	},
	onPullDownRefresh() {
		this.init().finally(() => {
			uni.stopPullDownRefresh();
		});
	},
	onPageScroll(e) {
		this.isScrolled = e.scrollTop > 10;
	},
	methods: {
		async init() {
			this.pageShow = false;
			try {
				if (!this.groupId) {
					this.goodsList = [];
					return;
				}
				// request 封装 resolve 的是整个 CommonResult，业务数据在 .data
				const detailRes = await getProductGroupDetail(this.groupId);
				const info = (detailRes && detailRes.data) || {};
				this.groupName = info.name || '';
				this.badge = info.badge || '';
				this.layout = info.layout || 'double';
				this.titleMulti = !!info.title_multi;
				this.minBuy = Number(info.min_buy || 1);
				if (this.groupName) {
					uni.setNavigationBarTitle({ title: this.groupName });
				}
				// 装修内容：theme_id 为该分组的装修页（后台「页面装修」写入）
				const themeId = Number(info.theme_id || 0);
				if (themeId > 0) {
					const data = await this.initThemePage('home', { id: themeId });
					this.currentDiyData = data || {};
				} else {
					this.currentDiyData = {};
				}
				// 装修里已经放了本分组的商品 → 商品位置由装修决定，不重复追加
				this.diyHasGroupGoods = this.checkDiyHasGroupGoods();
				if (this.diyHasGroupGoods) {
					this.goodsList = [];
				} else {
					// 分组商品
					const listRes = await getThemeProduct({ group_ids: String(this.groupId), limit: 20 });
					const raw = (listRes && listRes.data) || [];
					this.goodsList = Array.isArray(raw) ? raw : raw.list || [];
				}
			} catch (e) {
				this.goodsList = [];
			} finally {
				this.pageShow = true;
			}
		},
		goDetail(item) {
			if (!item || !item.id) return;
			uni.navigateTo({
				url: `/pages/goods/goods_details/index?id=${item.id}`,
			});
		},
		/**
		 * 判断装修数据里是否已存在「绑定本分组的商品选项卡」。
		 * 装修数据结构有嵌套（type/value 包裹），这里做限定深度的递归扫描，
		 * 不依赖具体层级，避免因包装层变化而漏判。
		 */
		checkDiyHasGroupGoods() {
			return this.findGroupGoodsComponent(this.currentDiyData, 0);
		},
		findGroupGoodsComponent(node, depth) {
			const data = parseMaybeJson(node);
			if (!data || typeof data !== 'object' || depth > 4) return false;
			if (this.isGroupGoodsComponent(data)) return true;
			const list = Array.isArray(data) ? data : Object.keys(data).map((k) => data[k]);
			for (let i = 0; i < list.length; i++) {
				if (this.findGroupGoodsComponent(list[i], depth + 1)) return true;
			}
			return false;
		},
		isGroupGoodsComponent(comp) {
			if (!comp || typeof comp !== 'object') return false;
			const name = comp.name || comp.defaultName;
			if (name !== DIY_GOODS_COMPONENT && name !== DIY_GOODS_LEGACY_NAME) return false;
			const tab = comp.tabConfig && comp.tabConfig.list && comp.tabConfig.list[0];
			if (!tab || Number(tab.tabVal) !== DIY_GOODS_FROM_GROUP) return false;
			const cfg = parseMaybeJson(tab.productGroupConfig) || {};
			const ids = Array.isArray(cfg.activeValue) ? cfg.activeValue : [cfg.activeValue];
			return ids.map(Number).includes(Number(this.groupId));
		},
		formatPrice(v) {
			return Number(v || 0).toFixed(2);
		},
		reconnect() {
			this.init();
		},
		bindHeight() {},
		storeTap() {},
		changeLogin() {
			toLogin();
		},
		changeBarg() {},
		newDataStatus() {},
	},
};
</script>

<style lang="scss" scoped>
.pg-page {
	min-height: 100vh;
	background-color: #f5f5f5;
}

.pg-goods {
	padding: 10rpx 20rpx 40rpx;

	&__hd {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		padding: 20rpx 0;
	}

	&__title {
		font-size: 32rpx;
		font-weight: 600;
		color: #303133;
	}

	&__tip {
		font-size: 24rpx;
		color: #909399;
	}

	&__list {
		display: grid;
		grid-gap: 20rpx;

		&.is-double {
			grid-template-columns: repeat(2, 1fr);
		}

		&.is-single {
			grid-template-columns: 1fr;
		}
	}

	&__empty {
		padding: 120rpx 0;
		text-align: center;
		font-size: 26rpx;
		color: #909399;
	}
}

.goods-item {
	background-color: #fff;
	border-radius: 12rpx;
	overflow: hidden;

	&__img {
		position: relative;
		width: 100%;
		padding-top: 100%;
		background-color: #f7f7f7;

		image {
			position: absolute;
			left: 0;
			top: 0;
			width: 100%;
			height: 100%;
		}
	}

	&__badge {
		left: 0 !important;
		top: 0 !important;
		width: 80rpx !important;
		height: 56rpx !important;
	}

	&__body {
		padding: 16rpx 20rpx 24rpx;
	}

	&__name {
		font-size: 26rpx;
		line-height: 36rpx;
		color: #303133;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;

		&.is-multi {
			white-space: normal;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}
	}

	&__price {
		margin-top: 10rpx;

		.now {
			font-size: 30rpx;
			font-weight: 600;
			color: #e93323;
		}

		.old {
			margin-left: 10rpx;
			font-size: 24rpx;
			color: #c0c4cc;
			text-decoration: line-through;
		}
	}

	&__sold {
		margin-top: 8rpx;
		font-size: 22rpx;
		color: #909399;
	}
}
</style>
