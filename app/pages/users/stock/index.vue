<template>
  <view class="stock-home">
    <template v-if="isAgent">
      <!-- 通栏渐变头：深蓝 + 斜光带 -->
      <view class="top-wrap">
        <view class="top-deco d1"></view>
        <view class="top-deco d2"></view>
        <view class="light-beam"></view>
        <view class="head-eyebrow">订货中心</view>
        <view class="user-row">
          <view class="avatar">{{ (agent.nickname || '?').slice(0, 1) }}</view>
          <view class="user-info">
            <view class="nickname">{{ agent.nickname }}</view>
            <view class="meta-line">
              <view class="badge">★ {{ agent.levelName || '订货代理' }}</view>
              <view class="upstream">上级 {{ parentUid > 0 ? (agent.parentName || '上级代理') : '总部' }}</view>
              <view v-if="parentUid > 0" class="up-id">ID {{ parentUid }}</view>
            </view>
          </view>
        </view>
      </view>

      <view class="page-body">
        <!-- 权益提示条 -->
        <view class="perm-bar">
          <view class="perm-ico">权</view>
          <view class="perm-txt">订货拿货专价已生效，团队订货奖励自动结算</view>
        </view>

        <!-- 交易管理 -->
        <view class="section-title">
          <view class="st-bar"></view>
          <text class="st-text">交易管理</text>
          <view class="st-line"></view>
        </view>
        <view class="group-card">
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/goods')">
            <view class="g-icon ic-blue">城</view>
            <view class="g-label">商品中心</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/physical')">
            <view class="g-icon ic-green">实</view>
            <view class="g-label">实体库存</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/virtual')">
            <view class="g-icon ic-gold">虚</view>
            <view class="g-label">虚拟库存</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/order-list')">
            <view class="g-icon ic-orange">单</view>
            <view class="g-label">订货订单</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/order-list?tab=audit')">
            <view class="g-icon ic-red">审</view>
            <view v-if="badges.audit > 0" class="g-badge">{{ badgeText(badges.audit) }}</view>
            <view class="g-label">订单审核</view>
          </view>
          <view v-if="parentDeliver" class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/order-list?tab=send')">
            <view class="g-icon ic-green">发</view>
            <view v-if="badges.send > 0" class="g-badge">{{ badgeText(badges.send) }}</view>
            <view class="g-label">订单发货</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/exchange')">
            <view class="g-icon ic-purple">换</view>
            <view v-if="badges.exchangeAudit > 0" class="g-badge">{{ badgeText(badges.exchangeAudit) }}</view>
            <view class="g-label">换货管理</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/stock-log')">
            <view class="g-icon ic-indigo">记</view>
            <view class="g-label">库存记录</view>
          </view>
        </view>

        <!-- 团队与收益 -->
        <view class="section-title">
          <view class="st-bar"></view>
          <text class="st-text">团队与收益</text>
          <view class="st-line"></view>
        </view>
        <view class="group-card">
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/team')">
            <view class="g-icon ic-cyan">队</view>
            <view class="g-label">我的团队</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/performance')">
            <view class="g-icon ic-teal">绩</view>
            <view class="g-label">业绩中心</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/bonus')">
            <view class="g-icon ic-gold">奖</view>
            <view class="g-label">奖金中心</view>
          </view>
          <view class="g-item" hover-class="g-press" :hover-stay-time="80" @click="nav('/pages/users/stock/notice')">
            <view class="g-icon ic-grey">信</view>
            <view v-if="badges.notice > 0" class="g-badge">{{ badgeText(badges.notice) }}</view>
            <view class="g-label">消息通知</view>
          </view>
        </view>

        <view class="bottom-tip">— 订货中心 —</view>
      </view>
    </template>

    <!-- 非订货商：品牌化空态 -->
    <view v-if="loaded && !isAgent" class="gate">
      <view class="gate-card">
        <view class="gate-ico">订</view>
        <view class="gate-title">您还不是订货代理</view>
        <view class="gate-desc">订货中心为您提供专价拿货、库存管理、团队订货奖励等服务</view>
        <view class="gate-line"></view>
        <view class="gate-tip">请联系您的上级代理或总部为您开通订货权限</view>
        <view class="gate-sub">开通后重新进入即可使用</view>
      </view>
      <view class="gate-back" @click="goHome">返回首页</view>
    </view>
  </view>
</template>

<script>
	import { getStockAgentInfo } from '@/api/stock.js';
	import { guardModule } from '@/libs/moduleSwitch.js';
	export default {
		data() {
			return {
				loaded: false,
				isAgent: false,
				parentUid: 0,
				parentDeliver: false,
				agent: {},
				// 角标计数：audit=待审核订单 / send=待发货订单 / exchangeAudit=待审核换货 / notice=未读消息
				badges: { audit: 0, send: 0, exchangeAudit: 0, notice: 0 }
			};
		},
		onShow() {
			guardModule('stock');
			this.loadInfo();
		},
		methods: {
			loadInfo() {
				getStockAgentInfo().then(res => {
					this.isAgent = res.data.isAgent;
					this.agent = res.data.agent || {};
					// 上级ID：0 表示上级是总部（此时不展示ID，只显示"总部"）
					this.parentUid = res.data.parentUid || 0;
					// 上级发货模式：开启时展示「订单发货」入口（仅实体订货单由上级发货）
					this.parentDeliver = !!(res.data.parentDeliver);
					// 未处理事项角标
					const d = res.data || {};
					this.badges = {
						audit: Number(d.audit) || 0,
						send: Number(d.send) || 0,
						exchangeAudit: Number(d.exchangeAudit) || 0,
						notice: Number(d.notice) || 0
					};
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			// 角标文本：超过 99 显示 99+
			badgeText(n) {
				return Number(n) > 99 ? '99+' : String(n);
			},
			goHome() {
				uni.switchTab({ url: '/pages/index/index' });
			},
			nav(url) {
				uni.navigateTo({ url });
			}
		}
	};
</script>

<style lang="scss" scoped>
.stock-home {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 60rpx;
}

/* ---------- 通栏渐变头：深蓝双色 + 斜光带 ---------- */
.top-wrap {
  position: relative;
  padding: 40rpx 36rpx 110rpx;
  background: linear-gradient(155deg, #16337c 0%, #1f5fd6 48%, #3a8df2 100%);
  overflow: hidden;
  .top-deco { position: absolute; border-radius: 50%; }
  .d1 { width: 300rpx; height: 300rpx; right: -90rpx; top: -130rpx; background: rgba(255,255,255,0.08); }
  .d2 { width: 160rpx; height: 160rpx; right: 150rpx; bottom: -70rpx; background: rgba(255,255,255,0.06); }
}
/* 斜光带：半透明白斜条穿过头部 */
.light-beam {
  position: absolute;
  top: -60rpx;
  right: -40rpx;
  width: 260rpx;
  height: 560rpx;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.09) 50%, rgba(255,255,255,0) 100%);
  transform: rotate(24deg);
}
/* 页面小标：头部顶部一行 */
.head-eyebrow {
  position: relative;
  z-index: 1;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 4rpx;
}
/* 等级徽章：金色实底胶囊，突出订货商等级 */
.badge {
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #f6cd60, #e0a213);
  color: #fff;
  border-radius: 999rpx;
  padding: 9rpx 26rpx;
  font-size: 25rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
  text-shadow: 0 1rpx 4rpx rgba(120, 84, 10, 0.30);
  box-shadow: 0 6rpx 16rpx rgba(224, 162, 19, 0.42);
}
/* 用户行：头像 + 昵称 +（等级徽章 / 上级） */
.user-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  margin-top: 34rpx;
}
.avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.20);
  border: 2rpx solid rgba(255, 255, 255, 0.55);
  color: #fff;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8rpx 22rpx rgba(10, 31, 78, 0.28);
}
.user-info { margin-left: 24rpx; flex: 1; min-width: 0; overflow: hidden; }
.nickname {
  font-size: 42rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.14);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 第二行：等级徽章 + 上级（等级与身份同框，最显眼）；超宽自动折行 */
.meta-line {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  row-gap: 10rpx;
}
.upstream {
  margin-left: 16rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}
/* 上级ID：细胶囊，总部时不渲染 */
.up-id {
  margin-left: 12rpx;
  flex-shrink: 0;
  font-size: 21rpx;
  line-height: 32rpx;
  color: #fff;
  background: rgba(255, 255, 255, 0.22);
  border: 1rpx solid rgba(255, 255, 255, 0.30);
  border-radius: 999rpx;
  padding: 0 14rpx;
  letter-spacing: 1rpx;
}

.page-body {
  position: relative;
  z-index: 2;
  padding: 0 24rpx;
  margin-top: -64rpx;
}

/* ---------- 权益提示条 ---------- */
.perm-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 18rpx;
  padding: 18rpx 22rpx;
  box-shadow: 0 8rpx 24rpx rgba(22, 51, 124, 0.10);
}
.perm-ico {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #f6cd60, #e0a213);
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16rpx;
  box-shadow: 0 4rpx 10rpx rgba(224, 162, 19, 0.30);
}
.perm-txt { flex: 1; font-size: 23rpx; color: #6b5a25; line-height: 34rpx; }

/* ---------- 分组标题 ---------- */
.section-title {
  display: flex;
  align-items: center;
  margin: 34rpx 6rpx 20rpx;
}
.st-bar {
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4a9df8, #2b6fe3);
  margin-right: 14rpx;
}
.st-text { font-size: 30rpx; font-weight: 700; color: #26324b; }
.st-line { flex: 1; height: 1rpx; margin-left: 20rpx; background: linear-gradient(90deg, #e3e9f4, rgba(227, 233, 244, 0)); }

/* ---------- 宫格卡片 ---------- */
.group-card {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  border-radius: 24rpx;
  padding: 14rpx 0 6rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 45, 61, 0.05);
}
.g-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 0 24rpx;
}
.g-press { opacity: 0.75; }
.g-icon {
  width: 92rpx;
  height: 92rpx;
  border-radius: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
  transition: transform 0.12s;
}
.g-item:active .g-icon { transform: scale(0.92); }
.ic-blue   { background: linear-gradient(135deg, #5aa7f8, #2b6fe3); box-shadow: 0 8rpx 18rpx rgba(43, 111, 227, 0.32); }
.ic-green  { background: linear-gradient(135deg, #43cf7d, #18a852); box-shadow: 0 8rpx 18rpx rgba(24, 168, 82, 0.30); }
.ic-gold   { background: linear-gradient(135deg, #f6cd60, #e0a213); box-shadow: 0 8rpx 18rpx rgba(224, 162, 19, 0.30); }
.ic-orange { background: linear-gradient(135deg, #ffa25e, #ff7a45); box-shadow: 0 8rpx 18rpx rgba(255, 122, 69, 0.30); }
.ic-red    { background: linear-gradient(135deg, #ff9090, #f56c6c); box-shadow: 0 8rpx 18rpx rgba(245, 108, 108, 0.30); }
.ic-purple { background: linear-gradient(135deg, #b48ff8, #8e5cf0); box-shadow: 0 8rpx 18rpx rgba(142, 92, 240, 0.30); }
.ic-cyan   { background: linear-gradient(135deg, #45cfcf, #1e9e9e); box-shadow: 0 8rpx 18rpx rgba(30, 158, 158, 0.30); }
.ic-teal   { background: linear-gradient(135deg, #3fbf9a, #1f9e7e); box-shadow: 0 8rpx 18rpx rgba(31, 158, 126, 0.30); }
.ic-grey   { background: linear-gradient(135deg, #aab5c6, #7b8698); box-shadow: 0 8rpx 18rpx rgba(123, 134, 152, 0.30); }
.ic-indigo { background: linear-gradient(135deg, #7f8ff4, #4b5bd6); box-shadow: 0 8rpx 18rpx rgba(75, 91, 214, 0.30); }
.g-label { margin-top: 14rpx; font-size: 24rpx; color: #3d4a5f; }

/* ---------- 入口红点角标 ---------- */
.g-item { position: relative; }
.g-badge {
  position: absolute;
  top: 16rpx;
  left: 50%;
  margin-left: 14rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 9rpx;
  box-sizing: border-box;
  text-align: center;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff7a7a, #e93323);
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
  border: 3rpx solid #fff;
  box-shadow: 0 4rpx 10rpx rgba(233, 51, 35, 0.35);
  z-index: 2;
}

/* ---------- 非订货商品牌化空态 ---------- */
.gate {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 60rpx 120rpx;
  background: linear-gradient(180deg, #eaf2ff 0%, #f4f6fb 42%);
}
.gate-card {
  width: 100%;
  background: #fff;
  border-radius: 28rpx;
  padding: 66rpx 44rpx 54rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 14rpx 44rpx rgba(22, 51, 124, 0.10);
}
.gate-ico {
  width: 128rpx;
  height: 128rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #5aa7f8, #2b6fe3);
  color: #fff;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 28rpx rgba(43, 111, 227, 0.32);
}
.gate-title { margin-top: 34rpx; font-size: 34rpx; font-weight: 700; color: #26324b; }
.gate-desc {
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #8a94a6;
  line-height: 38rpx;
  text-align: center;
}
.gate-line {
  width: 88rpx;
  height: 6rpx;
  border-radius: 3rpx;
  margin: 34rpx 0 26rpx;
  background: linear-gradient(90deg, #4a9df8, #2b6fe3);
}
.gate-tip { font-size: 26rpx; color: #3d4a5f; font-weight: 600; text-align: center; line-height: 40rpx; }
.gate-sub { margin-top: 10rpx; font-size: 22rpx; color: #a4adc0; }
.gate-back {
  margin-top: 46rpx;
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 88rpx;
  border-radius: 999rpx;
  background: #fff;
  border: 2rpx solid #b9cff2;
  color: #2b6fe3;
  font-size: 27rpx;
  font-weight: 600;
}

.bottom-tip {
  margin-top: 60rpx;
  text-align: center;
  font-size: 22rpx;
  color: #c3cad6;
  letter-spacing: 4rpx;
}
</style>
