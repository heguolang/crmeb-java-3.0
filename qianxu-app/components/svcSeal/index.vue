<template>
  <view class="seal-picker">
    <!-- 样式选择 -->
    <view class="seal-grid">
      <view
        v-for="s in styles"
        :key="s.key"
        class="seal-opt"
        :class="{ active: value === s.key }"
        @click="pick(s.key)"
      >
        <view class="opt-mark" :class="'mk-' + s.key">
          <text class="opt-star">★</text>
        </view>
        <text class="opt-name">{{ s.name }}</text>
      </view>
    </view>

    <!-- 实时预览 -->
    <view class="seal-preview">
      <view class="seal-canvas-wrap" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
        <canvas
          id="svcSealCanvas"
          canvas-id="svcSealCanvas"
          class="seal-canvas"
          :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
        ></canvas>
      </view>
      <text class="seal-cap">依据签约主体「{{ company || '—' }}」自动生成</text>
    </view>
  </view>
</template>

<script>
import { paintSeal, SEAL_STYLES } from '@/utils/sealPainter.js';

export default {
  name: 'SvcSeal',
  props: {
    /** 公司名称（取合同签约主体 partyName） */
    company: { type: String, default: '' },
    /** 当前选中的签章样式 key */
    value: { type: String, default: 'round' }
  },
  data() {
    return {
      canvasW: 260,
      canvasH: 260,
      styles: SEAL_STYLES
    };
  },
  watch: {
    company() {
      this.render();
    },
    value() {
      this.render();
    }
  },
  mounted() {
    this.$nextTick(() => {
      setTimeout(() => this.render(), 60);
    });
  },
  methods: {
    pick(key) {
      if (this.value === key) return;
      this.$emit('input', key);
      this.$emit('change', key);
    },
    /** 用同一套笔触在预览画布上画一遍 */
    render() {
      const ctx = uni.createCanvasContext('svcSealCanvas', this);
      paintSeal(ctx, {
        style: this.value,
        size: this.canvasW,
        name: this.company
      });
      ctx.draw();
    },
    /** 导出当前章为 PNG 本地路径 */
    toTempFile() {
      const S = this.canvasW;
      const self = this;
      return new Promise((resolve, reject) => {
        // 导出前强制重绘一次：端上（小程序）为省性能会懒渲染视口外画布，不重绘可能导到空图
        this.render();
        setTimeout(() => {
          uni.canvasToTempFilePath(
            {
              canvasId: 'svcSealCanvas',
              fileType: 'png',
              quality: 1,
              destWidth: S * 3,
              destHeight: S * 3,
              success: (res) => {
                const path = res.tempFilePath || (res.tempFilePaths && res.tempFilePaths[0]);
                if (path) resolve(path);
                else reject(new Error('签章生成失败，请重试'));
              },
              fail: () => reject(new Error('签章生成失败，请重试'))
            },
            self
          );
        }, 220);
      });
    }
  }
};
</script>

<style scoped lang="scss">
.seal-picker { margin-top: 20rpx; }

/* 样式选择 */
.seal-grid { display: flex; justify-content: space-between; }
.seal-opt {
  flex: 1; margin: 0 8rpx; padding: 16rpx 6rpx 14rpx;
  border: 2rpx solid #eef1f6; border-radius: 14rpx;
  background: #fbfcfe; display: flex; flex-direction: column; align-items: center;
}
.seal-opt:first-child { margin-left: 0; }
.seal-opt:last-child { margin-right: 0; }
.seal-opt.active { border-color: #c8161e; background: #fff7f7; }
.opt-mark {
  width: 46rpx; height: 46rpx; border: 3rpx solid #c8161e; opacity: 0.75;
  display: flex; align-items: center; justify-content: center;
}
.mk-round { border-radius: 50%; }
.mk-oval { border-radius: 50%; width: 58rpx; height: 38rpx; }
.mk-square { border-radius: 4rpx; }
/* 现代电子章在 v2 里是圆角描边（不再是实底色块），这里跟着改，保证「选项图标 = 实际章型」 */
.mk-modern { border-radius: 12rpx; width: 56rpx; height: 34rpx; }
.opt-star { font-size: 18rpx; color: #c8161e; line-height: 1; }
.opt-name { font-size: 20rpx; color: #5a6675; margin-top: 10rpx; }
.seal-opt.active .opt-name { color: #c8161e; font-weight: 600; }

/* 预览 */
.seal-preview {
  margin-top: 22rpx; display: flex; flex-direction: column; align-items: center;
  background: #fbfcfe; border: 1rpx dashed #e2e8f2; border-radius: 16rpx; padding: 20rpx 0 16rpx;
}
.seal-canvas-wrap { position: relative; }
.seal-canvas { background: transparent; }
.seal-cap { font-size: 21rpx; color: #9aa4b2; margin-top: 10rpx; }
</style>
