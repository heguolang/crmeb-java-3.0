<template>
  <view class="team-page">
    <!-- 顶部渐变头 -->
    <view class="top-wrap">
      <view class="top-deco d1"></view>
      <view class="top-deco d2"></view>
      <view class="page-title">我的团队</view>
      <view class="page-sub">发展下级代理，团队订货奖励自动结算</view>
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
          <view class="stat-num c-grey">{{ stats.disabled }}</view>
          <view class="stat-label">已禁用</view>
        </view>
      </view>

      <button class="add-btn" @click="showAdd = true">＋ 新增下级代理</button>

      <!-- 成员列表 -->
      <view v-if="list.length" class="section-title">
        <view class="st-bar"></view>
        <text class="st-text">团队成员</text>
        <view class="st-line"></view>
      </view>

      <view v-for="a in list" :key="a.id" class="agent-card">
        <view class="agent-avatar">{{ (a.nickname || '?').slice(0, 1) }}</view>
        <view class="agent-info">
          <view class="agent-line">
            <text class="agent-name">{{ a.nickname }}</text>
            <text class="agent-level">{{ a.levelName }}</text>
          </view>
          <view class="agent-sub">
            <text class="as-item">{{ a.phone || '暂无手机号' }}</text>
            <text class="as-item">{{ a.createTime }} 加入</text>
          </view>
        </view>
        <text class="agent-status" :class="a.status === 1 ? 'on' : 'off'">
          <text class="st-dot-mini" :class="a.status === 1 ? 'd-on' : 'd-off'"></text>{{ a.status === 1 ? '正常' : '禁用' }}
        </text>
      </view>

      <view v-if="!list.length && loaded" class="empty-card">
        <view class="empty-ico">队</view>
        <view class="empty-txt">还没有下级代理</view>
        <view class="empty-sub">点击上方「新增下级代理」邀请伙伴加入</view>
      </view>
    </view>

    <!-- 新增下级代理弹窗 -->
    <view v-if="showAdd" class="mask" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-title">新增下级代理</view>
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
        <button class="submit-btn" @click="submitAdd">确定新增</button>
      </view>
    </view>
  </view>
</template>

<script>
	import { getSubAgentList, getStockLevels, createSubAgent } from '@/api/stock.js';
	export default {
		data() {
			return {
				list: [],
				loaded: false,
				showAdd: false,
				levels: [],
				addForm: { phone: '', levelIndex: -1 }
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
					disabled: this.list.filter(a => a.status !== 1).length
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
			onLevelChange(e) {
				this.addForm.levelIndex = Number(e.detail.value);
			},
			submitAdd() {
				if (!this.addForm.phone) return this.$util.Tips({ title: '请填写手机号' });
				if (this.addForm.levelIndex < 0) return this.$util.Tips({ title: '请选择层级' });
				createSubAgent({ phone: this.addForm.phone, levelId: this.levels[this.addForm.levelIndex].id }).then(() => {
					this.$util.Tips({ title: '新增成功' });
					this.showAdd = false;
					this.addForm = { phone: '', levelIndex: -1 };
					this.load();
				});
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
/* 状态：圆点 + 彩字 */
.agent-status {
  flex-shrink: 0;
  font-size: 23rpx;
  display: flex;
  align-items: center;
  &.on { color: #18a852; font-weight: 600; }
  &.off { color: #9aa7bd; }
}
.st-dot-mini {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 8rpx;
  &.d-on { background: #21c26a; box-shadow: 0 0 0 6rpx rgba(33, 194, 106, 0.14); }
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
.submit-btn {
  margin-top: 14rpx;
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
</style>
