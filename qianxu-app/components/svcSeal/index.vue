<template>
  <view class="seal-picker">
    <!-- 实时预览（公章统一用圆形标准章，不再提供其它样式） -->
    <view class="seal-preview">
      <view class="seal-canvas-wrap" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
        <canvas
          id="svcSealCanvas"
          canvas-id="svcSealCanvas"
          class="seal-canvas"
          :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
        ></canvas>
      </view>
      <text class="seal-cap">依据签约主体「{{ company || '—' }}」自动生成圆形公章</text>
    </view>
  </view>
</template>

<script>
import { paintSeal } from '@/utils/sealPainter.js';

export default {
  name: 'SvcSeal',
  props: {
    /** 公司名称（取合同签约主体 partyName） */
    company: { type: String, default: '' },
    /** 兼容旧调用（v-model），现在固定为 round，不再可选 */
    value: { type: String, default: 'round' }
  },
  data() {
    return {
      canvasW: 260,
      canvasH: 260
    };
  },
  watch: {
    company() {
      this.render();
    }
  },
  mounted() {
    this.$nextTick(() => {
      setTimeout(() => this.render(), 60);
    });
  },
  methods: {
    /** 用同一套笔触在预览画布上画一遍 */
    render() {
      const ctx = uni.createCanvasContext('svcSealCanvas', this);
      paintSeal(ctx, {
        style: 'round',
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

/* 预览 */
.seal-preview {
  display: flex; flex-direction: column; align-items: center;
  background: #fbfcfe; border: 1rpx dashed #e2e8f2; border-radius: 16rpx; padding: 20rpx 0 16rpx;
}
.seal-canvas-wrap { position: relative; }
.seal-canvas { background: transparent; }
.seal-cap { font-size: 21rpx; color: #9aa4b2; margin-top: 10rpx; }
</style>
