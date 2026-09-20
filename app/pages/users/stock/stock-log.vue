<template>
  <view class="sl-page">
    <!-- 渐变头 -->
    <view class="head-card">
      <view class="hd-deco c1"></view>
      <view class="hd-deco c2"></view>
      <view class="badge">库存记录</view>
      <view class="head-title">我的库存变动</view>
      <view class="head-sub">后台调整与订单变动，全部留痕于此</view>
      <view class="head-stat">
        <view class="stat-item">
          <text class="stat-num">{{ physicalNum }}</text>
          <text class="stat-label">实体可供应量</text>
        </view>
        <view class="stat-div"></view>
        <view class="stat-item">
          <text class="stat-num">{{ virtualNum }}</text>
          <text class="stat-label">虚拟剩余可提</text>
        </view>
      </view>
    </view>

    <!-- 库存类型切换 -->
    <view class="tabs">
      <view class="tab" :class="{ on: tab === 1 }" @click="switchTab(1)">实体库存</view>
      <view class="tab" :class="{ on: tab === 2 }" @click="switchTab(2)">虚拟库存</view>
    </view>

    <!-- 记录列表 -->
    <view class="list">
      <view v-for="r in list" :key="r.id" class="rec">
        <image v-if="r.image" :src="r.image" class="rec-img" mode="aspectFill" />
        <view v-else class="rec-img rec-img-ph">{{ (r.productName || '?').slice(0, 1) }}</view>
        <view class="rec-main">
          <view class="rec-name">
            {{ r.productName || ('商品' + r.productId) }}
            <text v-if="r.skuKey" class="rec-sku">（{{ r.skuKey }}）</text>
          </view>
          <view class="rec-meta">
            <text class="rec-time">{{ fmtTime(r.createTime) }}</text>
            <text class="rec-type" :class="r.stockType === 2 ? 'ty-vir' : 'ty-phy'">{{ r.stockTypeText }}</text>
          </view>
          <view class="rec-mark">{{ r.mark || '后台手动调整' }}</view>
          <!-- 虚拟库存转卖溯源：谁采购了本次扣减 -->
          <view v-if="r.linkNickname" class="rec-buyer">
            <text class="rb-tag">采购人</text>
            <text class="rb-name">{{ r.linkNickname }}</text>
            <text v-if="r.linkAgentName" class="rb-lv">{{ r.linkAgentName }}</text>
            <text v-if="r.linkPhone" class="rb-phone">{{ r.linkPhone }}</text>
          </view>
          <view v-if="r.orderNo" class="rec-orderno">下级订单号：{{ r.orderNo }}</view>
        </view>
        <view class="rec-num" :class="Number(r.num) >= 0 ? 'up' : 'down'">{{ Number(r.num) > 0 ? '+' : '' }}{{ r.num }}</view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading && !list.length" class="loading-box">加载中…</view>

    <!-- 空态 -->
    <view v-if="!list.length && loaded && !loading" class="empty-box">
      <view class="empty-ico">记</view>
      <view class="empty-title">暂无{{ tab === 1 ? '实体' : '虚拟' }}库存变动记录</view>
      <view class="empty-sub">后台调整库存、供货给下级后，这里会留下明细</view>
    </view>

    <view v-if="list.length && finished" class="bottom-tip">— 已显示全部 —</view>
    <view v-else-if="list.length" class="bottom-tip">上拉加载更多</view>
  </view>
</template>

<script>
	import { getMyStockLogs, getMyVirtualStock, getMyPhysicalStock } from '@/api/stock.js';
	export default {
		data() {
			return {
				tab: 1,
				list: [],
				page: 1,
				limit: 20,
				total: 0,
				loading: false,
				loaded: false,
				finished: false,
				physicalNum: 0,
				virtualNum: 0
			};
		},
		onLoad(options) {
			const t = Number(options && options.stockType);
			if (t === 1 || t === 2) this.tab = t;
			this.load(true);
			this.loadSummary();
		},
		onPullDownRefresh() {
			this.load(true, () => uni.stopPullDownRefresh());
			this.loadSummary();
		},
		onReachBottom() {
			if (this.finished || this.loading) return;
			this.load(false);
		},
		methods: {
			switchTab(t) {
				if (this.tab === t) return;
				this.tab = t;
				this.load(true);
			},
			load(reset, done) {
				if (reset) {
					this.page = 1;
					this.finished = false;
				}
				this.loading = true;
				getMyStockLogs({ stockType: this.tab, page: this.page, limit: this.limit }).then(res => {
					const d = (res && res.data) || {};
					const rows = d.list || [];
					this.list = reset ? rows : this.list.concat(rows);
					this.total = d.total || 0;
					// 已加载条数达到总数，或本页不足一页 → 到底了
					this.finished = this.list.length >= this.total || rows.length < this.limit;
					this.loading = false;
					this.loaded = true;
					if (done) done();
				}).catch(() => {
					this.loading = false;
					this.loaded = true;
					if (done) done();
				});
			},
			loadSummary() {
				getMyPhysicalStock().then(res => {
					const rows = (res && res.data) || [];
					this.physicalNum = rows.reduce((s, r) => s + Number(r.num || 0), 0);
				}).catch(() => {});
				getMyVirtualStock().then(res => {
					const rows = (res && res.data) || [];
					this.virtualNum = rows.reduce((s, r) => s + Number(r.remainNum || 0), 0);
				}).catch(() => {});
			},
			fmtTime(t) {
				if (!t) return '';
				// 后端返回 "2026-09-20 11:47:41"，这里省略秒
				return String(t).slice(0, 16);
			}
		}
	};
</script>

<style lang="scss" scoped>
.sl-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 60rpx;
  background: #f5f6fa;
  box-sizing: border-box;
}

/* ---------- 渐变头 ---------- */
.head-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #16337c 0%, #2b6fe3 58%, #4a9df8 100%);
  border-radius: 24rpx;
  padding: 34rpx 32rpx 30rpx;
  color: #fff;
  box-shadow: 0 12rpx 30rpx rgba(22, 51, 124, 0.26);
  .hd-deco { position: absolute; border-radius: 50%; }
  .c1 { width: 260rpx; height: 260rpx; right: -80rpx; top: -110rpx; background: rgba(255, 255, 255, 0.10); }
  .c2 { width: 140rpx; height: 140rpx; right: 120rpx; bottom: -70rpx; background: rgba(255, 255, 255, 0.07); }
}
.badge {
  position: relative; z-index: 1;
  display: inline-block;
  background: rgba(255, 255, 255, 0.24);
  border: 1rpx solid rgba(255, 255, 255, 0.42);
  border-radius: 999rpx;
  padding: 4rpx 18rpx;
  font-size: 22rpx;
  letter-spacing: 2rpx;
}
.head-title { position: relative; z-index: 1; margin-top: 16rpx; font-size: 38rpx; font-weight: 700; }
.head-sub { position: relative; z-index: 1; margin-top: 10rpx; font-size: 23rpx; opacity: 0.85; }
.head-stat {
  position: relative; z-index: 1;
  margin-top: 28rpx;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.14);
  border-radius: 16rpx;
  padding: 20rpx 0;
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 40rpx; font-weight: 700; line-height: 46rpx; }
.stat-label { margin-top: 6rpx; font-size: 21rpx; opacity: 0.82; }
.stat-div { width: 1rpx; height: 56rpx; background: rgba(255, 255, 255, 0.25); }

/* ---------- 类型切换 ---------- */
.tabs {
  margin-top: 22rpx;
  display: flex;
  background: #fff;
  border-radius: 999rpx;
  padding: 8rpx;
  box-shadow: 0 4rpx 18rpx rgba(31, 45, 61, 0.06);
}
.tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  font-size: 27rpx;
  color: #6b7789;
  border-radius: 999rpx;
  transition: all 0.16s;
}
.tab.on {
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, #2b6fe3, #4a9df8);
  box-shadow: 0 6rpx 16rpx rgba(43, 111, 227, 0.32);
}

/* ---------- 记录卡片 ---------- */
.list { margin-top: 8rpx; }
.rec {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 20rpx;
  margin-top: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(31, 45, 61, 0.05);
}
.rec-img {
  width: 96rpx; height: 96rpx;
  border-radius: 14rpx;
  flex-shrink: 0;
  background: #f2f4f8;
}
.rec-img-ph {
  display: flex; align-items: center; justify-content: center;
  font-size: 34rpx; color: #aab5c6; font-weight: 700;
}
.rec-main { flex: 1; margin: 0 18rpx; overflow: hidden; }
.rec-name { font-size: 27rpx; color: #2b3445; font-weight: 600; line-height: 38rpx; }
.rec-sku { font-size: 23rpx; color: #909399; font-weight: 400; }
.rec-meta { margin-top: 8rpx; display: flex; align-items: center; }
.rec-time { font-size: 22rpx; color: #a3adbd; }
.rec-type {
  margin-left: 14rpx;
  font-size: 20rpx;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
}
.ty-vir { color: #b8860b; background: #fdf6e3; }
.ty-phy { color: #1f9e7e; background: #e8f7f2; }
.rec-mark {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #7c8798;
  line-height: 32rpx;
}
/* 采购人：虚拟库存被下级买走时的溯源行 */
.rec-buyer {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  background: #f3f7ff;
  border-radius: 10rpx;
  padding: 8rpx 14rpx;
}
.rb-tag {
  flex-shrink: 0;
  font-size: 19rpx;
  color: #1f5fd6;
  background: #e2edff;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-right: 12rpx;
}
.rb-name { font-size: 23rpx; color: #2b3445; font-weight: 600; }
.rb-lv {
  font-size: 19rpx;
  color: #b8860b;
  background: #fdf6e3;
  border-radius: 6rpx;
  padding: 2rpx 10rpx;
  margin-left: 12rpx;
}
.rb-phone { font-size: 22rpx; color: #909399; margin-left: 12rpx; }
.rec-orderno {
  margin-top: 8rpx;
  font-size: 21rpx;
  color: #a3adbd;
  letter-spacing: 0.5rpx;
}
.rec-num {
  flex-shrink: 0;
  font-size: 34rpx;
  font-weight: 700;
  padding-top: 4rpx;
}
/* 中文习惯：增加红、减少绿 */
.rec-num.up { color: #e93323; }
.rec-num.down { color: #18a852; }

/* ---------- 状态 ---------- */
.loading-box { padding: 70rpx 0; text-align: center; font-size: 25rpx; color: #a3adbd; }
.empty-box { display: flex; flex-direction: column; align-items: center; padding: 100rpx 0 40rpx; }
.empty-ico {
  width: 120rpx; height: 120rpx; border-radius: 34rpx;
  background: linear-gradient(135deg, #eef3fd, #dbe7fb);
  color: #9db6de; font-size: 46rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 26rpx;
}
.empty-title { font-size: 28rpx; color: #606266; font-weight: 600; }
.empty-sub { margin-top: 12rpx; font-size: 23rpx; color: #b0b8c4; text-align: center; padding: 0 60rpx; line-height: 34rpx; }

.bottom-tip { margin-top: 44rpx; text-align: center; font-size: 22rpx; color: #c3cad6; letter-spacing: 2rpx; }
</style>
