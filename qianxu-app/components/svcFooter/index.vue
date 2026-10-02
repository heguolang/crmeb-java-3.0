<template>
  <view class="svc-footer" :style="{ paddingBottom: bottomSafe }">
    <view
      v-for="(item, index) in list"
      :key="index"
      class="sf-item"
      :class="{ active: item.path === activePath }"
      @click="go(item)"
    >
      <view class="sf-ico">
        <image v-if="item.path === activePath && item.iconOn" :src="item.iconOn" class="sf-img" />
        <image v-else-if="item.iconOff" :src="item.iconOff" class="sf-img" />
        <text v-else class="sf-txt-ico">{{ item.ico }}</text>
      </view>
      <text class="sf-label">{{ item.name }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SvcFooter',
  props: {
    active: { type: String, default: 'home' }
  },
  data() {
    return {
      list: [
        { key: 'home', name: '首页', path: '/pages/software/home/index', ico: '⌂', iconOn: '', iconOff: '' },
        { key: 'case', name: '案例', path: '/pages/software/case/index', ico: '▦', iconOn: '', iconOff: '' },
        { key: 'project', name: '项目', path: '/pages/software/project/index', ico: '▤', iconOn: '', iconOff: '' },
        { key: 'user', name: '我的', path: '/pages/software/user/index', ico: '☉', iconOn: '', iconOff: '' }
      ],
      bottomSafe: '0rpx'
    };
  },
  computed: {
    activePath() {
      const item = this.list.find((x) => x.key === this.active);
      return item ? item.path : '';
    }
  },
  mounted() {
    try {
      const info = uni.getSystemInfoSync();
      if (info && info.safeAreaInsets && info.safeAreaInsets.bottom) {
        this.bottomSafe = info.safeAreaInsets.bottom + 'px';
      }
    } catch (e) {}
  },
  methods: {
    go(item) {
      if (item.path === this.activePath) return;
      uni.switchTab({
        url: item.path,
        fail: () => {
          uni.redirectTo({ url: item.path });
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  align-items: stretch;
  background: #ffffff;
  border-top: 1rpx solid #eef1f6;
  padding-top: 12rpx;
  padding-bottom: 12rpx;
}
.sf-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.sf-ico {
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sf-img {
  width: 44rpx;
  height: 44rpx;
}
.sf-txt-ico {
  font-size: 34rpx;
  line-height: 44rpx;
  color: #9aa4b2;
}
.sf-label {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}
.sf-item.active .sf-txt-ico,
.sf-item.active .sf-label {
  color: #1d6bf3;
  font-weight: 600;
}
</style>
