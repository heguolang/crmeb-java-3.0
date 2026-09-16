<template>
  <view class="team-page">
    <view class="top-card">
      <view class="tc-item"><view class="tc-num">{{ list.length }}</view><view class="tc-label">直接下级</view></view>
    </view>

    <button class="add-btn" @click="showAdd = true">＋ 新增下级代理</button>

    <view v-for="a in list" :key="a.id" class="agent-card">
      <view class="agent-line">
        <text class="agent-name">{{ a.nickname }}</text>
        <text class="agent-level">{{ a.levelName }}</text>
        <text class="agent-status" :class="a.status === 1 ? 'on' : 'off'">{{ a.status === 1 ? '正常' : '禁用' }}</text>
      </view>
      <view class="agent-sub">手机号：{{ a.phone || '-' }} · 加入时间 {{ a.createTime }}</view>
    </view>
    <view v-if="!list.length && loaded" class="empty">还没有下级代理，快去新增吧</view>

    <view v-if="showAdd" class="mask" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-title">新增下级代理</view>
        <view class="form-item">
          <text class="f-label">手机号</text>
          <input v-model="addForm.phone" type="number" class="f-input" placeholder="对方需已注册会员" />
        </view>
        <view class="form-item">
          <text class="f-label">层级</text>
          <picker :range="levelNames" @change="onLevelChange">
            <view class="f-picker">{{ levelNames[addForm.levelIndex] || '请选择层级（须低于自己）' }}</view>
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
.team-page { min-height: 100vh; background: #f5f6f8; padding: 24rpx; }
.top-card { background: linear-gradient(135deg, #2b6fe3, #4a9df8); border-radius: 20rpx; padding: 36rpx; display: flex; justify-content: center; }
.tc-item { text-align: center; color: #fff; }
.tc-num { font-size: 48rpx; font-weight: 600; }
.tc-label { font-size: 24rpx; opacity: .85; margin-top: 6rpx; }
.add-btn { margin: 24rpx 0; background: #fff; color: #2b6fe3; border-radius: 40rpx; font-size: 28rpx; border: 1rpx dashed #2b6fe3; }
.agent-card { background: #fff; border-radius: 16rpx; padding: 26rpx; margin-bottom: 18rpx; }
.agent-line { display: flex; align-items: center; }
.agent-name { font-size: 28rpx; color: #333; font-weight: 600; margin-right: 14rpx; }
.agent-level { background: #eef4ff; color: #2b6fe3; border-radius: 8rpx; font-size: 22rpx; padding: 4rpx 14rpx; margin-right: 14rpx; }
.agent-status { font-size: 22rpx; margin-left: auto; }
.on { color: #5cc45c; } .off { color: #999; }
.agent-sub { font-size: 24rpx; color: #999; margin-top: 10rpx; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 99; }
.modal { width: 600rpx; background: #fff; border-radius: 20rpx; padding: 40rpx 34rpx; }
.modal-title { font-size: 32rpx; font-weight: 600; color: #333; margin-bottom: 26rpx; text-align: center; }
.form-item { display: flex; align-items: center; margin-bottom: 20rpx; }
.f-label { width: 140rpx; font-size: 26rpx; color: #666; flex-shrink: 0; }
.f-input { flex: 1; background: #f5f6f8; border-radius: 10rpx; height: 70rpx; padding: 0 20rpx; font-size: 26rpx; }
.f-picker { flex: 1; background: #f5f6f8; border-radius: 10rpx; height: 70rpx; line-height: 70rpx; padding: 0 20rpx; font-size: 26rpx; color: #333; }
.submit-btn { background: #2b6fe3; color: #fff; border-radius: 40rpx; font-size: 28rpx; margin-top: 16rpx; }
.empty { text-align: center; color: #999; padding: 100rpx 0; font-size: 26rpx; }
</style>
