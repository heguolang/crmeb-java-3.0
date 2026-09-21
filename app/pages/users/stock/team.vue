<template>
  <view class="team-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">我的团队</view>
      <view class="page-sub">发展下级订货商，团队订货奖励自动结算</view>
    </view>

    <view class="page-body">
      <!-- 统计卡 -->
      <view class="stat-card">
        <view class="stat-item">
          <view class="stat-num">{{ stats.total }}</view>
          <view class="stat-label">直接下级</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-num c-green">{{ stats.active }}</view>
          <view class="stat-label">正常</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-num c-orange">{{ stats.pending }}</view>
          <view class="stat-label">待对方同意</view>
        </view>
      </view>

      <button class="add-btn" @click="showAdd = true">＋ 新增下级订货商</button>

      <!-- 成员列表 -->
      <view v-if="list.length" class="section-title">
        <view class="st-bar"></view>
        <text class="st-text">团队成员</text>
        <view class="st-line"></view>
      </view>

      <view v-for="a in list" :key="a.id" class="agent-card">
        <image
          v-if="avatarUrl(a) && !avatarErr[a.id]"
          class="agent-avatar-img"
          :src="avatarUrl(a)"
          mode="aspectFill"
          @error="onAvatarErr(a.id)"
        />
        <view v-else class="agent-avatar">{{ (a.nickname || '?').slice(0, 1) }}</view>
        <view class="agent-info">
          <view class="agent-line">
            <text class="agent-name">{{ a.nickname }}</text>
            <text class="agent-level">{{ a.levelName }}</text>
          </view>
          <view class="agent-sub">
            <text class="as-item as-id">ID {{ a.uid }}</text>
            <text class="as-item">{{ a.phone || '暂无手机号' }}</text>
          </view>
          <view class="agent-sub as-time">
            <text class="as-item">{{ a.createTime }}</text>
          </view>
        </view>
        <view class="agent-right">
          <text class="agent-status" :class="a.status === 1 ? 'on' : (a.status === 2 ? 'wait' : 'off')">
            <text
              class="st-dot-mini"
              :class="a.status === 1 ? 'd-on' : (a.status === 2 ? 'd-wait' : 'd-off')"
            ></text>{{ statusText(a.status) }}
          </text>
          <view class="ord-btn" @click.stop="viewOrders(a)">查看订单</view>
        </view>
      </view>

      <view v-if="!list.length && loaded" class="empty-card">
        <view class="empty-ico">队</view>
        <view class="empty-txt">还没有下级订货商</view>
        <view class="empty-sub">点击上方「新增下级订货商」邀请伙伴加入</view>
      </view>
    </view>

    <!-- 新增下级订货商弹窗 -->
    <view v-if="showAdd" class="mask" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-title">新增下级订货商</view>
        <text class="modal-close" @click="showAdd = false">✕</text>
        <view class="form-item">
          <view class="f-label">手机号</view>
          <input v-model="addForm.phone" type="number" class="f-input" placeholder="对方需已注册会员" />
        </view>
        <view class="form-item">
          <view class="f-label">层级</view>
          <picker :range="levelNames" @change="onLevelChange">
            <view class="f-picker" :class="{ picked: addForm.levelIndex >= 0 }">
              {{ levelNames[addForm.levelIndex] || '请选择层级（须低于自己）' }}
              <text class="f-arrow">›</text>
            </view>
          </picker>
        </view>
        <view class="invite-note">提交后对方会收到邀请，需对方在订货中心点击「同意」后才正式成为订货商</view>
        <button class="submit-btn" @click="submitAdd">发出邀请</button>
      </view>
    </view>

    <!-- 下级订单抽屉 -->
    <view v-if="ordVisible" class="mask" @click="ordVisible = false">
      <view class="sheet" @click.stop>
        <view class="sheet-head">
          <view class="sheet-title">{{ ordAgent.nickname }} 的订货订单</view>
          <text class="modal-close sheet-close" @click="ordVisible = false">✕</text>
        </view>
        <view class="sheet-sub">{{ ordAgent.levelName }} · ID {{ ordAgent.uid }} · 共 {{ ordTotal }} 单</view>

        <view class="ord-tabs">
          <view
            v-for="t in ordStatusTabs"
            :key="String(t.value)"
            class="ord-tab"
            :class="{ active: ordStatus === t.value }"
            @click="pickOrdStatus(t.value)"
          >{{ t.label }}</view>
        </view>

        <scroll-view scroll-y class="sheet-body">
          <view v-for="o in ordList" :key="o.id" class="o-card">
            <view class="o-row1">
              <text class="o-no">{{ o.orderNo }}</text>
              <text class="o-st" :class="'os' + o.status">{{ ordStatusText(o.status) }}</text>
            </view>
            <view v-for="p in (o.productList || [])" :key="p.id" class="o-p">
              <text class="o-pname">{{ p.productName }}</text>
              <text class="o-pnum">× {{ p.num }}</text>
            </view>
            <view class="o-foot">
              <text class="o-tag">{{ o.stockType === 2 ? '虚拟库存' : '实体库存' }}</text>
              <text class="o-time">{{ shortTime(o.createTime) }}</text>
              <text class="o-amt">¥{{ o.totalPrice }}</text>
            </view>
          </view>
          <view v-if="!ordList.length && ordLoaded" class="o-empty">该成员暂无订单</view>
          <view v-if="ordList.length < ordTotal" class="o-more" @click="loadMoreOrders">加载更多</view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
	import { getSubAgentList, getStockLevels, createSubAgent, getSubAgentOrders } from '@/api/stock.js';
	import { HTTP_REQUEST_URL } from '@/config/app';
	export default {
		data() {
			return {
				list: [],
				avatarErr: {},
				imgHost: HTTP_REQUEST_URL,
				loaded: false,
				showAdd: false,
				levels: [],
				addForm: { phone: '', levelIndex: -1 },
				// 下级订单抽屉
				ordVisible: false,
				ordAgent: {},
				ordList: [],
				ordTotal: 0,
				ordPage: 1,
				ordLoaded: false,
				ordLoading: false,
				ordStatus: null,
				ordStatusTabs: [
					{ value: null, label: '全部' },
					{ value: 1, label: '待付款' },
					{ value: 0, label: '待审核' },
					{ value: 2, label: '待发货' },
					{ value: 3, label: '待收货' },
					{ value: 4, label: '已完成' }
				]
			};
		},
		computed: {
			levelNames() {
				return this.levels.map(l => l.name);
			},
			stats() {
				return {
					total: this.list.length,
					active: this.list.filter(a => a.status === 1).length,
					pending: this.list.filter(a => a.status === 2).length
				};
			}
		},
		onShow() {
			this.load();
			getStockLevels().then(res => { this.levels = res.data || []; });
		},
		methods: {
			load() {
				getSubAgentList().then(res => {
					this.list = res.data || [];
					this.loaded = true;
				}).catch(() => { this.loaded = true; });
			},
			// 头像加载失败时回退成昵称首字
			onAvatarErr(id) {
				this.$set(this.avatarErr, id, true);
			},
			// 头像为相对路径（crmebimage/...）时补全为可访问地址；已是 http(s) 直接返回
			avatarUrl(a) {
				if (!a || !a.avatar) return '';
				if (/^https?:\/\//i.test(a.avatar)) return a.avatar;
				return this.imgHost + '/' + a.avatar.replace(/^\/+/, '');
			},
			onLevelChange(e) {
				this.addForm.levelIndex = Number(e.detail.value);
			},
			// 订货商状态文案：0=禁用 1=正常 2=待对方同意
			statusText(s) {
				return { 0: '禁用', 1: '正常', 2: '待同意' }[s] || '正常';
			},
			submitAdd() {
				if (!this.addForm.phone) return this.$util.Tips({ title: '请填写手机号' });
				if (this.addForm.levelIndex < 0) return this.$util.Tips({ title: '请选择层级' });
				createSubAgent({ phone: this.addForm.phone, levelId: this.levels[this.addForm.levelIndex].id }).then(() => {
					uni.showModal({
						title: '邀请已发出',
						content: '已邀请对方成为「' + this.levelNames[this.addForm.levelIndex] + '」订货商。对方进入订货中心点击「同意」后即正式生效。',
						showCancel: false
					});
					this.showAdd = false;
					this.addForm = { phone: '', levelIndex: -1 };
					this.load();
				});
			},
			// ---------- 查看下级订单 ----------
			viewOrders(a) {
				this.ordAgent = a || {};
				this.ordStatus = null;
				this.ordVisible = true;
				this.loadSubOrders(true);
			},
			pickOrdStatus(v) {
				this.ordStatus = v;
				this.loadSubOrders(true);
			},
			loadSubOrders(reset) {
				if (this.ordLoading) return;
				this.ordLoading = true;
				if (reset) {
					this.ordPage = 1;
					this.ordLoaded = false;
				}
				const params = { uid: this.ordAgent.uid, page: this.ordPage, limit: 20 };
				if (this.ordStatus !== null) params.status = this.ordStatus;
				getSubAgentOrders(params).then(res => {
					const d = (res && res.data) || {};
					const list = d.list || [];
					this.ordList = reset ? list : this.ordList.concat(list);
					this.ordTotal = d.total || this.ordList.length;
					this.ordLoaded = true;
					this.ordLoading = false;
				}).catch(() => {
					this.ordLoaded = true;
					this.ordLoading = false;
				});
			},
			loadMoreOrders() {
				this.ordPage += 1;
				this.loadSubOrders(false);
			},
			ordStatusText(s) {
				return { 0: '待上级审核', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', '-1': '已驳回', 10: '匹配上级中', '-2': '已取消' }[s] || s;
			},
			shortTime(t) {
				if (!t) return '—';
				return String(t).substring(0, 16).replace('T', ' ');
			}
		}
	};
</script>

<style lang="scss" scoped>
.team-page { min-height: 100vh; background: #f4f6fb; padding-bottom: 60rpx; }

/* ---------- 顶部渐变头 ---------- */
.top-wrap {
  position: relative;
  padding: 34rpx 32rpx 78rpx;
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
  margin-top: -50rpx;
}

/* ---------- 统计卡 ---------- */
.stat-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 30rpx 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.stat-item { flex: 1; text-align: center; }
.stat-num { font-size: 46rpx; font-weight: 700; color: #26324b; line-height: 1.1; }
.stat-num.c-green { color: #18a852; }
.stat-num.c-grey { color: #7b8698; }
.stat-num.c-orange { color: #f08c2e; }
.stat-label { margin-top: 8rpx; font-size: 23rpx; color: #909399; }
.stat-divider { width: 1rpx; height: 56rpx; background: #eef1f6; }

/* ---------- 新增按钮 ---------- */
.add-btn {
  margin: 22rpx 0 8rpx;
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
  height: 84rpx;
  line-height: 84rpx;
  box-shadow: 0 10rpx 24rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}

/* ---------- 分组标题 ---------- */
.section-title { display: flex; align-items: center; margin: 26rpx 6rpx 20rpx; }
.st-bar {
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background: linear-gradient(180deg, #4a9df8, #2b6fe3);
  margin-right: 14rpx;
}
.st-text { font-size: 30rpx; font-weight: 700; color: #26324b; }
.st-line { flex: 1; height: 1rpx; margin-left: 20rpx; background: linear-gradient(90deg, #e3e9f4, rgba(227, 233, 244, 0)); }

/* ---------- 成员卡片 ---------- */
.agent-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 24rpx;
  margin-bottom: 18rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 6rpx 22rpx rgba(31, 45, 61, 0.05);
}
.agent-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #e8f3ff;
  color: #1a73e8;
  font-size: 34rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: inset 0 0 0 1rpx #cfe4ff;
}
.agent-avatar-img {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #e8f3ff;
  box-shadow: inset 0 0 0 1rpx #cfe4ff;
}
.agent-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
.agent-line { display: flex; align-items: center; }
.agent-name { font-size: 28rpx; color: #26324b; font-weight: 700; margin-right: 14rpx; }
.agent-level {
  background: #ecf3ff;
  color: #2b6fe3;
  border-radius: 999rpx;
  font-size: 21rpx;
  padding: 4rpx 16rpx;
}
.agent-sub { margin-top: 10rpx; display: flex; }
.as-item { font-size: 23rpx; color: #909399; margin-right: 24rpx; }
.as-id { color: #5b6b85; font-weight: 600; }
.as-time { margin-top: 4rpx; }
/* 状态：圆点 + 彩字 */
.agent-status {
  flex-shrink: 0;
  font-size: 23rpx;
  display: flex;
  align-items: center;
  &.on { color: #18a852; font-weight: 600; }
  &.wait { color: #f08c2e; font-weight: 600; }
  &.off { color: #9aa7bd; }
}
.st-dot-mini {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 8rpx;
  &.d-on { background: #21c26a; box-shadow: 0 0 0 6rpx rgba(33, 194, 106, 0.14); }
  &.d-wait { background: #f0a04b; box-shadow: 0 0 0 6rpx rgba(240, 160, 75, 0.16); }
  &.d-off { background: #b8c0cd; }
}

/* ---------- 空态白卡 ---------- */
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

/* ---------- 新增弹窗 ---------- */
.mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 25, 45, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
}
.modal {
  position: relative;
  width: 620rpx;
  background: #fff;
  border-radius: 28rpx;
  padding: 44rpx 36rpx 40rpx;
}
.modal-title { font-size: 32rpx; font-weight: 700; color: #26324b; margin-bottom: 30rpx; text-align: center; }
.modal-close {
  position: absolute;
  right: 28rpx;
  top: 30rpx;
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
.form-item { margin-bottom: 24rpx; }
.f-label { font-size: 26rpx; color: #3d4a5f; font-weight: 600; margin-bottom: 12rpx; }
.f-input {
  background: #f4f6f9;
  border-radius: 14rpx;
  height: 80rpx;
  padding: 0 24rpx;
  font-size: 27rpx;
  color: #303133;
}
.f-picker {
  position: relative;
  background: #f4f6f9;
  border-radius: 14rpx;
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 24rpx;
  font-size: 27rpx;
  color: #a4adc0;
  &.picked { color: #303133; }
}
.f-arrow {
  position: absolute;
  right: 24rpx;
  top: 0;
  font-size: 34rpx;
  color: #c0c4cc;
}
.invite-note {
  margin-top: 4rpx;
  font-size: 21rpx;
  color: #a4adc0;
  line-height: 32rpx;
  background: #f6f9ff;
  border-radius: 12rpx;
  padding: 14rpx 18rpx;
}
.submit-btn {
  margin-top: 22rpx;
  background: linear-gradient(135deg, #4a9df8, #2b6fe3);
  color: #fff;
  border-radius: 999rpx;
  font-size: 29rpx;
  font-weight: 600;
  height: 84rpx;
  line-height: 84rpx;
  box-shadow: 0 10rpx 24rpx rgba(43, 111, 227, 0.30);
  &::after { border: none; }
}

/* ---------- 卡片右侧：状态 + 查看订单 ---------- */
.agent-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.ord-btn {
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #2b6fe3;
  background: #ecf3ff;
  border: 1rpx solid #d6e4ff;
  border-radius: 999rpx;
  padding: 8rpx 20rpx;
  line-height: 1;
}

/* ---------- 下级订单抽屉 ---------- */
.mask { align-items: flex-end; }
.sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 32rpx 24rpx 30rpx;
  box-sizing: border-box;
}
.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sheet-title { font-size: 32rpx; font-weight: 700; color: #26324b; }
.sheet-close { position: static; }
.sheet-sub {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #909399;
}
.ord-tabs {
  display: flex;
  background: #f4f6f9;
  border-radius: 999rpx;
  padding: 6rpx;
  margin-top: 22rpx;
  overflow-x: auto;
  white-space: nowrap;
}
.ord-tab {
  flex: 1;
  flex-shrink: 0;
  text-align: center;
  font-size: 24rpx;
  color: #606266;
  padding: 12rpx 16rpx;
  border-radius: 999rpx;
  transition: all 0.2s;
}
.ord-tab.active {
  background: linear-gradient(135deg, #2b6fe3, #4a9df8);
  color: #fff;
  font-weight: 600;
}
.sheet-body {
  margin-top: 20rpx;
  height: 760rpx;
}
.o-card {
  background: #f8f9fc;
  border-radius: 16rpx;
  padding: 20rpx 22rpx;
  margin-bottom: 16rpx;
}
.o-row1 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.o-no { font-size: 25rpx; font-weight: 600; color: #303133; }
.o-st {
  font-size: 21rpx;
  line-height: 1;
  padding: 7rpx 14rpx;
  border-radius: 999rpx;
  font-weight: 500;
  background: #eceff4;
  color: #7b8698;
}
.o-st.os0, .o-st.os1 { background: #fff4e5; color: #f08c2e; }
.o-st.os2, .o-st.os3 { background: #ecf3ff; color: #2b6fe3; }
.o-st.os4 { background: #e9f9ec; color: #21a84f; }
.o-st.os-1 { background: #ffecec; color: #f56c6c; }
.o-st.os10 { background: #f3ecff; color: #7c4dd4; }
.o-st.os-2 { background: #f2f3f5; color: #909399; }
.o-p {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14rpx;
  font-size: 23rpx;
}
.o-pname {
  flex: 1;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}
.o-pnum { color: #909399; flex-shrink: 0; }
.o-foot {
  margin-top: 14rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #e8ecf3;
  display: flex;
  align-items: center;
}
.o-tag {
  font-size: 20rpx;
  color: #7c4dd4;
  background: #f3ecff;
  border-radius: 6rpx;
  padding: 4rpx 12rpx;
  flex-shrink: 0;
}
.o-time { flex: 1; margin-left: 16rpx; font-size: 21rpx; color: #a4adc0; }
.o-amt { font-size: 27rpx; font-weight: 700; color: #e93323; }
.o-empty {
  text-align: center;
  font-size: 24rpx;
  color: #b0b8c4;
  padding: 80rpx 0;
}
.o-more {
  margin: 6rpx 0 20rpx;
  text-align: center;
  font-size: 24rpx;
  color: #2b6fe3;
  padding: 20rpx 0;
}
</style>
