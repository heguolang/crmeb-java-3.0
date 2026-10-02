<template>
  <!-- 隐藏画布：仅用于合成签署页，不参与页面布局 -->
  <view class="sheet-holder">
    <canvas
      id="svcSheetCanvas"
      canvas-id="svcSheetCanvas"
      class="sheet-canvas"
      :style="{ width: w + 'px', height: h + 'px' }"
    ></canvas>
  </view>
</template>

<script>
import { paintSheet } from '@/utils/sheetPainter.js';

const W = 620;   // 签署页逻辑宽度（px），近似 A4 黄金比例
const H = 1080;

export default {
  name: 'SvcSignSheet',
  data() {
    return { w: W, h: H };
  },
  methods: {
    /**
     * 生成签署页 PNG
     * @param o {
     *   title, contractNo,        // 合同名称 / 编号
     *   company,                  // 签约主体名称（企业名，用于生成签章）
     *   sealStyle,                // 签章样式
     *   needSeal,                 // 是否盖章（个人签约为 false）
     *   signerName,               // 签约负责人
     *   signPack,                 // { strokes, w, h } 手写签名笔迹
     *   dateText                  // 签署日期（YYYY年MM月DD日）
     * }
     * @returns Promise<本地临时文件路径>
     */
    generate(o) {
      const ctx = uni.createCanvasContext('svcSheetCanvas', this);
      paintSheet(ctx, o || {}, W, H);
      return new Promise((resolve) => {
        ctx.draw(false, () => {
          setTimeout(() => {
            this.exportFile().then(resolve).catch(() => resolve(''));
          }, 120);
        });
      });
    },

    /* ==================== 导出 ==================== */
    exportFile() {
      const self = this;
      return new Promise((resolve, reject) => {
        uni.canvasToTempFilePath(
          {
            canvasId: 'svcSheetCanvas',
            fileType: 'png',
            quality: 1,
            destWidth: W * 2,
            destHeight: H * 2,
            success: (res) => {
              const p = res.tempFilePath || (res.tempFilePaths && res.tempFilePaths[0]);
              if (p) resolve(p);
              else reject(new Error('签署页生成失败'));
            },
            fail: () => reject(new Error('签署页生成失败'))
          },
          self
        );
      });
    }
  }
};
</script>

<style scoped lang="scss">
/* 画布移出视口：不占布局、不遮挡，但仍保持渲染，可正常导出 */
.sheet-holder { position: fixed; left: -3000px; top: 0; width: 0; height: 0; overflow: hidden; }
.sheet-canvas { background: #ffffff; }
</style>
