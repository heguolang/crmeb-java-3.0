<template>
  <view class="perf-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">业绩中心</view>
      <view class="page-sub">个人与团队订货业绩实时汇总</view>
      <picker :range="rangeNames" @change="onRangeChange">
        <view class="range-pill">
          <text class="rp-label">统计范围</text>
          <text class="rp-value">{{ rangeNames[rangeIndex] }}</text>
          <text class="rp-arrow">▾</text>
        </view>
      </picker>
    </view>

    <view class="page-body">
      <!-- 统计卡 -->
      <view class="stat-card">
        <view class="stat-item" :class="{ active: source === 1 }" @click="pickSource(1)">
          <view class="stat-num">¥{{ perf.selfPerformance || 0 }}</view>
          <view class="stat-label">个人业绩</view>
          <view class="stat-sub">{{ perf.selfOrderCount || 0 }} 单 · 点击查看</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" :class="{ active: source === 2 }" @click="pickSource(2)">
          <view class="stat-num">¥{{ perf.teamPerformance || 0 }}</view>
          <view class="stat-label">团队业绩</view>
          <view class="stat-sub">{{ perf.teamOrderCount || 0 }} 单 · 点击查看</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-num">{{ perf.subAgentCount || 0 }}</view>
          <view class="stat-label">团队代理</view>
          <view class="stat-sub">含间接</view>
        </view>
      </view>

      <!-- 口径说明 -->
      <view class="note-bar">
        <view class="note-ico">i</view>
        <view class="note-txt">个人业绩 = 我的已完成订货单金额；团队业绩 = 名下全部下级的已完成订货单金额。</view>
      </view>

      <!-- 业绩订单记录 -->
      <view class="ord-sec">
        <view class="ord-head">
          <text class="ord-title">业绩订单记录</text>
          <text class="ord-total">共 {{ orderTotal }} 单</text>
        </view>
        <view class="ord-tabs">
          <view
            v-for="t in sourceTabs"
            :key="String(t.value)"
            class="ord-tab"
            :class="{ active: source === t.value }"
            @click="pickSource(t.value)"
          >{{ t.label }}</view>
        </view>

        <view v-for="o in orders" :key="o.id" class="ord-card">
          <view class="ord-row1">
            <text class="ord-no">{{ o.orderNo }}</text>
            <text class="src-pill" :class="o.perfSource === 1 ? 'is-self' : 'is-team'">
              {{ o.perfSourceText || (o.perfSource === 1 ? '个人业绩' : '团队业绩') }}
            </text>
          </view>
          <view class="ord-meta">
            <text class="ord-nick">{{ o.nickname }}</text>
            <text class="ord-dot">·</text>
            <text>{{ o.levelName || '—' }}</text>
            <text class="ord-dot">·</text>
            <text>{{ shortTime(o.finishTime || o.createTime) }}</text>
          </view>
          <view v-for="p in (o.productList || [])" :key="p.id" class="ord-p">
            <text class="ord-pname">{{ p.productName }}</text>
            <text class="ord-pnum">× {{ p.num }}</text>
          </view>
          <view class="ord-foot">
            <text class="ord-tag">{{ o.stockType === 2 ? '虚拟库存' : '实体库存' }}</text>
            <text class="ord-amt">¥{{ o.totalPrice }}</text>
          </view>
        </view>

        <view v-if="!orders.length && ordersLoaded" class="ord-empty">当前没有任何业绩订单记录</view>
        <view v-if="orders.length < orderTotal" class="ord-more" @click="loadMore">加载更多</view>
      </view>
    </view>
  </view>
</template>

<script>
	import { getMyStockPerformance, getMyStockPerformanceOrders } from '@/api/stock.js';
	export default {
		data() {
			return {
				perf: {},
				rangeIndex: 0,
				rangeNames: ['全部', '本月', '上月', '近30天'],
				dateLimit: '',
				// 业绩来源：null=全部 1=个人业绩 2=团队业绩
				source: null,
				sourceTabs: [
					{ value: null, label: '全部' },
					{ value: 1, label: '个人业绩' },
					{ value: 2, label: '团队业绩' }
				],
				orders: [],
				orderTotal: 0,
				orderPage: 1,
				ordersLoaded: false,
				orderLoading: false
			};
		},
		onShow() {
			this.load();
		},
		methods: {
			onRangeChange(e) {
				this.rangeIndex = Number(e.detail.value);
				this.load();
			},
			load() {
				let dateLimit = '';
				if (this.rangeIndex === 1) dateLimit = this.monthStr(0);
				else if (this.rangeIndex === 2) dateLimit = this.monthStr(-1);
				this.dateLimit = dateLimit;
				getMyStockPerformance({ dateLimit }).then(res => { this.perf = res.data || {}; });
				this.loadOrders(true);
			},
			// 点统计卡或顶部标签筛选业绩来源；再次点击同一项取消筛选
			pickSource(v) {
				this.source = this.source === v ? null : v;
				this.loadOrders(true);
			},
			loadOrders(reset) {
				if (this.orderLoading) return;
				this.orderLoading = true;
				if (reset) {
					this.orderPage = 1;
					this.ordersLoaded = false;
				}
				const params = { page: this.orderPage, limit: 20 };
				if (this.dateLimit) params.dateLimit = this.dateLimit;
				if (this.source) params.source = this.source;
				getMyStockPerformanceOrders(params).then(res => {
					const d = (res && res.data) || {};
					const list = d.list || [];
					this.orders = reset ? list : this.orders.concat(list);
					this.orderTotal = d.total || this.orders.length;
					this.ordersLoaded = true;
					this.orderLoading = false;
				}).catch(() => {
					this.ordersLoaded = true;
					this.orderLoading = false;
				});
			},
			loadMore() {
				this.orderPage += 1;
				this.loadOrders(false);
			},
			shortTime(t) {
				if (!t) return '—';
				return String(t).substring(0, 16).replace('T', ' ');
			},
			monthStr(offset) {
				const d = new Date();
				let y = d.getFullYear(), m = d.getMonth() + 1 + offset;
				if (m <= 0) { m += 12; y -= 1; }
				return y + '-' + (m < 10 ? '0' + m : m);
			}
		}
	};
</script>

<style lang="scss" scoped>
.perf-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 86rpx;
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
/* 统计范围胶囊：嵌在渐变头内 */
.range-pill {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  margin-top: 24rpx;
  background: rgba(255, 255, 255, 0.20);
  border: 1rpx solid rgba(255, 255, 255, 0.40);
  border-radius: 999rpx;
  padding: 10rpx 26rpx;
}
.rp-label { font-size: 22rpx; color: rgba(255, 255, 255, 0.75); margin-right: 14rpx; }
.rp-value { font-size: 25rpx; color: #fff; font-weight: 600; }
.rp-arrow { font-size: 22rpx; color: rgba(255, 255, 255, 0.8); margin-left: 10rpx; }

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -56rpx;
}

/* ---------- 统计卡 ---------- */
.stat-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 36rpx 16rpx;
  display: flex;
  align-items: stretch;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.stat-item { flex: 1; text-align: center; padding: 0 8rpx; }
.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #2b6fe3;
  line-height: 1.15;
  word-break: break-all;
}
.stat-label { margin-top: 12rpx; font-size: 24rpx; color: #3d4a5f; font-weight: 600; }
.stat-sub { margin-top: 6rpx; font-size: 21rpx; color: #a4adc0; }
.stat-divider { width: 1rpx; background: #eef1f6; }

/* ---------- 口径说明 ---------- */
.note-bar {
  display: flex;
  align-items: flex-start;
  margin-top: 20rpx;
  background: #eef5ff;
  border-radius: 16rpx;
  padding: 18rpx 22rpx;
}
.note-ico {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  background: #2b6fe3;
  color: #fff;
  font-size: 22rpx;
  font-style: italic;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 14rpx;
}
.note-txt { flex: 1; font-size: 22rpx; color: #3d6db5; line-height: 34rpx; }

/* ---------- 统计卡可点 ---------- */
.stat-item { border-radius: 16rpx; transition: background 0.2s; }
.stat-item.active { background: #eef5ff; }

/* ---------- 业绩订单记录 ---------- */
.ord-sec { margin-top: 24rpx; }
.ord-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 0 6rpx 14rpx;
}
.ord-title { font-size: 30rpx; font-weight: 700; color: #2b3445; }
.ord-total { font-size: 22rpx; color: #a4adc0; }

.ord-tabs {
  display: flex;
  background: #fff;
  border-radius: 999rpx;
  padding: 8rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 4rpx 16rpx rgba(31, 45, 61, 0.05);
}
.ord-tab {
  flex: 1;
  text-align: center;
  font-size: 25rpx;
  color: #606266;
  padding: 14rpx 0;
  border-radius: 999rpx;
  transition: all 0.2s;
}
.ord-tab.active {
  background: linear-gradient(135deg, #2b6fe3, #4a9df8);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(43, 111, 227, 0.30);
}

.ord-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 4rpx 18rpx rgba(31, 45, 61, 0.05);
}
.ord-row1 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ord-no {
  font-size: 26rpx;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5rpx;
}
.src-pill {
  flex-shrink: 0;
  font-size: 21rpx;
  line-height: 1;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-weight: 500;
}
.src-pill.is-self { background: #eef4ff; color: #2b6fe3; }
.src-pill.is-team { background: #f0fff4; color: #21a84f; }

.ord-meta {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #909399;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.ord-nick { color: #3d4a5f; font-weight: 600; }
.ord-dot { margin: 0 10rpx; color: #c8cfd9; }

.ord-p {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14rpx;
  font-size: 23rpx;
}
.ord-pname {
  flex: 1;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}
.ord-pnum { color: #909399; flex-shrink: 0; }

.ord-foot {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f2f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ord-tag {
  font-size: 20rpx;
  color: #7c4dd4;
  background: #f3ecff;
  border-radius: 6rpx;
  padding: 4rpx 12rpx;
}
.ord-amt { font-size: 30rpx; font-weight: 700; color: #e93323; }

.ord-empty {
  text-align: center;
  font-size: 24rpx;
  color: #b0b8c4;
  padding: 60rpx 0;
  background: #fff;
  border-radius: 20rpx;
}
.ord-more {
  margin-top: 8rpx;
  text-align: center;
  font-size: 24rpx;
  color: #2b6fe3;
  background: #fff;
  border-radius: 999rpx;
  padding: 20rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(31, 45, 61, 0.05);
}
</style>
