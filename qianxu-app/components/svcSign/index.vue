<template>
  <view v-if="show" class="sign-mask">
    <!-- 顶栏 -->
    <view class="sign-head">
      <view class="head-left">
        <text class="sign-title">手写签名</text>
        <text class="sign-sub">{{ landscape ? '请在下方空白处工整书写姓名' : '建议横屏握持手机，书写空间更大' }}</text>
      </view>
      <view class="head-close" @click="close">
        <text class="close-x">✕</text>
      </view>
    </view>

    <!-- 画布区 -->
    <view class="canvas-stage">
      <view class="canvas-wrap" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
        <canvas
          id="svcSignCanvas"
          canvas-id="svcSignCanvas"
          class="sign-canvas"
          :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
          disable-scroll="true"
          @touchstart="onStart"
          @touchmove="onMove"
          @touchend="onEnd"
          @touchcancel="onEnd"
        ></canvas>
        <view v-if="empty" class="canvas-ph">在此处签名</view>
        <view class="baseline"></view>
      </view>
      <text v-if="!landscape" class="rotate-tip">把手机横过来书写效果更好</text>
    </view>

    <!-- 底栏 -->
    <view class="sign-actions">
      <view class="sign-btn ghost" @click="clear">重新签名</view>
      <view class="sign-btn primary" @click="confirm">确认签名</view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SvcSign',
  data() {
    return {
      show: false,
      canvasW: 300,
      canvasH: 180,
      landscape: false,
      empty: true,
      ctx: null,
      canvasRect: null,
      lastX: 0,
      lastY: 0,
      drawing: false,
      /** 笔迹数据：签署页按矢量重绘用（避免 H5 跨域贴图污染画布） */
      strokes: [],
      curStroke: null,
      resizeHandler: null,
      resizeTimer: null
    };
  },
  beforeDestroy() {
    this.unbindResize();
  },
  methods: {
    open() {
      this.show = true;
      this.empty = true;
      this.tryLockLandscape();
      this.$nextTick(() => {
        this.bindResize();
        // 锁屏/旋转有延迟，稍后再校准一次尺寸
        setTimeout(() => {
          this.initCanvas();
        }, 60);
        setTimeout(() => {
          this.handleResize();
        }, 420);
      });
    },
    close() {
      this.unbindResize();
      this.unlockOrientation();
      this.show = false;
    },

    /* ---------- 横屏 ---------- */
    tryLockLandscape() {
      // #ifdef H5
      try {
        const so = window.screen && window.screen.orientation;
        if (so && typeof so.lock === 'function') {
          const p = so.lock('landscape');
          if (p && typeof p.catch === 'function') p.catch(() => {});
        }
      } catch (e) {
        /* 浏览器未授权全屏时无法锁定方向，忽略，画布仍按横屏尺寸铺满 */
      }
      // #endif
      // #ifdef APP-PLUS
      try {
        plus.screen.lockOrientation('landscape-primary');
      } catch (e) {
        /* ignore */
      }
      // #endif
    },
    unlockOrientation() {
      // #ifdef H5
      try {
        const so = window.screen && window.screen.orientation;
        if (so && typeof so.unlock === 'function') so.unlock();
      } catch (e) {
        /* ignore */
      }
      // #endif
      // #ifdef APP-PLUS
      try {
        plus.screen.lockOrientation('portrait-primary');
      } catch (e) {
        /* ignore */
      }
      // #endif
    },
    bindResize() {
      // #ifdef H5
      this.resizeHandler = () => this.handleResize();
      window.addEventListener('resize', this.resizeHandler);
      window.addEventListener('orientationchange', this.resizeHandler);
      // #endif
      if (typeof uni.onWindowResize === 'function') {
        if (!this.resizeHandler) {
          this.resizeHandler = () => this.handleResize();
          uni.onWindowResize(this.resizeHandler);
        }
      }
    },
    unbindResize() {
      // #ifdef H5
      if (this.resizeHandler) {
        window.removeEventListener('resize', this.resizeHandler);
        window.removeEventListener('orientationchange', this.resizeHandler);
      }
      // #endif
      if (this.resizeHandler && typeof uni.offWindowResize === 'function') {
        uni.offWindowResize(this.resizeHandler);
      }
      this.resizeHandler = null;
      if (this.resizeTimer) {
        clearTimeout(this.resizeTimer);
        this.resizeTimer = null;
      }
    },
    handleResize() {
      if (!this.show) return;
      if (this.resizeTimer) clearTimeout(this.resizeTimer);
      this.resizeTimer = setTimeout(() => {
        const size = this.calcSize();
        if (Math.abs(size.w - this.canvasW) < 24 && Math.abs(size.h - this.canvasH) < 24) return;
        const hadDrawn = !this.empty;
        this.initCanvas();
        if (hadDrawn) {
          this.empty = true;
          uni.showToast({ title: '屏幕已切换，请重新签名', icon: 'none' });
        }
      }, 200);
    },

    /* ---------- 画布 ---------- */
    calcSize() {
      const sys = uni.getSystemInfoSync();
      const w = Math.floor(sys.windowWidth || 375);
      const h = Math.floor(sys.windowHeight || 667);
      const landscape = w > h;
      // 顶栏 + 底栏约占 170px
      const availH = Math.max(160, h - 170);
      const width = Math.max(240, w - 24);
      const height = landscape ? availH : Math.min(availH, Math.round(width * 1.15));
      return { w: width, h: Math.max(150, height), landscape: landscape };
    },
    initCanvas() {
      const size = this.calcSize();
      this.canvasW = size.w;
      this.canvasH = size.h;
      this.landscape = size.landscape;
      this.ctx = uni.createCanvasContext('svcSignCanvas', this);
      this.clearCanvas();
      this.measureCanvas();
    },
    measureCanvas() {
      // H5 下触摸事件给的是 clientX/clientY，需要减去画布在视口中的偏移
      try {
        uni
          .createSelectorQuery()
          .in(this)
          .select('#svcSignCanvas')
          .boundingClientRect((rect) => {
            if (rect) this.canvasRect = rect;
          })
          .exec();
      } catch (e) {
        /* ignore */
      }
    },
    clearCanvas() {
      if (!this.ctx) return;
      this.ctx.setFillStyle('#ffffff');
      this.ctx.fillRect(0, 0, this.canvasW, this.canvasH);
      this.ctx.draw();
      this.empty = true;
      this.strokes = [];
      this.curStroke = null;
    },
    clear() {
      this.clearCanvas();
    },

    /* ---------- 绘制 ---------- */
    pointOf(e) {
      const t = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]) || {};
      if (t.x !== undefined && t.y !== undefined && t.x !== null && t.y !== null) {
        return { x: t.x, y: t.y };
      }
      const rect = this.canvasRect;
      return {
        x: (t.clientX || 0) - (rect ? rect.left : 0),
        y: (t.clientY || 0) - (rect ? rect.top : 0)
      };
    },
    onStart(e) {
      if (!this.ctx) this.initCanvas();
      const p = this.pointOf(e);
      this.drawing = true;
      this.lastX = p.x;
      this.lastY = p.y;
      if (this.empty) this.empty = false;
      // 记录笔迹：线宽与绘制时保持一致，供签署页等比重绘
      this.curStroke = { w: this.canvasW > 500 ? 5 : 4, pts: [{ x: p.x, y: p.y }] };
      this.strokes.push(this.curStroke);
    },
    onMove(e) {
      if (!this.drawing || !this.ctx) return;
      const p = this.pointOf(e);
      if (this.curStroke && this.curStroke.pts.length < 3000) {
        this.curStroke.pts.push({ x: p.x, y: p.y });
      }
      this.ctx.setStrokeStyle('#111111');
      // 横屏画布面积更大，线宽随之加粗，签名更清晰
      this.ctx.setLineWidth(this.canvasW > 500 ? 5 : 4);
      this.ctx.setLineCap('round');
      this.ctx.setLineJoin('round');
      this.ctx.beginPath();
      this.ctx.moveTo(this.lastX, this.lastY);
      this.ctx.lineTo(p.x, p.y);
      this.ctx.stroke();
      this.ctx.draw(true);
      this.lastX = p.x;
      this.lastY = p.y;
    },
    onEnd() {
      this.drawing = false;
      this.curStroke = null;
    },

    /* ---------- 导出 ---------- */
    confirm() {
      if (this.empty) {
        uni.showToast({ title: '请先手写签名', icon: 'none' });
        return;
      }
      const finish = (path) => {
        this.close();
        // 第二个参数：笔迹数据包，供签署页按同一笔触矢量重绘
        this.$emit('confirm', path, {
          strokes: JSON.parse(JSON.stringify(this.strokes)),
          w: this.canvasW,
          h: this.canvasH
        });
      };
      uni.canvasToTempFilePath(
        {
          canvasId: 'svcSignCanvas',
          fileType: 'png',
          quality: 1,
          success: (res) => {
            const path = res.tempFilePath || (res.tempFilePaths && res.tempFilePaths[0]);
            if (path) finish(path);
            else uni.showToast({ title: '签名生成失败', icon: 'none' });
          },
          fail: () => {
            uni.showToast({ title: '签名生成失败，请重试', icon: 'none' });
          }
        },
        this
      );
    }
  }
};
</script>

<style scoped lang="scss">
.sign-mask {
  position: fixed; left: 0; top: 0; right: 0; bottom: 0;
  background: #ffffff; z-index: 9999;
  display: flex; flex-direction: column;
}

/* 顶栏 */
.sign-head {
  flex: none; display: flex; align-items: center; justify-content: space-between;
  padding: 16rpx 30rpx 10rpx; border-bottom: 1rpx solid #eef1f6;
}
.head-left { display: flex; flex-direction: column; }
.sign-title { font-size: 30rpx; font-weight: 700; color: #222; }
.sign-sub { font-size: 21rpx; color: #9aa4b2; margin-top: 4rpx; }
.head-close { padding: 8rpx 12rpx; }
.close-x { font-size: 32rpx; color: #9aa4b2; }

/* 画布 */
.canvas-stage {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; overflow: hidden;
}
.canvas-wrap {
  position: relative; border: 2rpx dashed #cfd8e6; border-radius: 16rpx;
  background: #ffffff; overflow: hidden;
}
.sign-canvas { background: #ffffff; }
.canvas-ph {
  position: absolute; left: 0; right: 0; top: 50%; margin-top: -22rpx;
  text-align: center; color: #c3cad6; font-size: 30rpx; pointer-events: none;
}
.baseline {
  position: absolute; left: 8%; right: 8%; bottom: 18%;
  border-bottom: 1rpx dashed #e6ebf3; pointer-events: none;
}
.rotate-tip { font-size: 21rpx; color: #a8b3c4; margin-top: 14rpx; }

/* 底栏 */
.sign-actions {
  flex: none; display: flex; padding: 16rpx 30rpx calc(24rpx + env(safe-area-inset-bottom));
}
.sign-btn {
  flex: 1; height: 84rpx; border-radius: 42rpx;
  display: flex; align-items: center; justify-content: center;
  font-size: 28rpx; font-weight: 600;
}
.sign-btn.ghost { background: #f2f5fa; color: #5a6675; margin-right: 20rpx; }
.sign-btn.primary { background: linear-gradient(135deg, #0d4fc4, #1d6bf3); color: #fff; }
</style>
