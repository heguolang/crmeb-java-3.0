<template>
  <el-dialog
    :title="dlgTitle"
    :visible.sync="show"
    width="820px"
    append-to-body
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <div v-loading="uploading" element-loading-text="正在上传签字图片...">
      <div class="sg-stick">
        <span>签字人：{{ signer || signerName || '—' }}</span>
        <span class="sg-stick-tip">用鼠标或触屏在下方框内手写，保存时会自动裁掉四周空白（透明底 PNG）</span>
      </div>
      <canvas
        ref="cv"
        class="sg-canvas"
        @mousedown="onDown"
        @mousemove="onMove"
        @mouseup="onUp"
        @mouseleave="onUp"
        @touchstart.prevent="onDown"
        @touchmove.prevent="onMove"
        @touchend.prevent="onUp"
      />
      <div class="sg-bar">
        <el-button size="mini" :disabled="!strokes.length" @click="undo">撤销一笔</el-button>
        <el-button size="mini" :disabled="!strokes.length" @click="clear">清空重写</el-button>
        <el-slider v-model="penWidth" :min="2" :max="14" :step="1" :show-tooltip="false" class="sg-width" />
        <span class="sg-tip">笔画粗细</span>
      </div>
    </div>
    <div slot="footer">
      <el-button size="small" @click="show = false">取消</el-button>
      <el-button size="small" type="primary" :disabled="!strokes.length" :loading="uploading" @click="onUse">保存并使用</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { fileFileApi } from '@/api/systemSetting';

const W = 760;
const H = 260;
const INK = '#1b1b1b';

export default {
  name: 'SvcSignMaker',
  props: {
    /** 打开时的签字人（也可由 open() 传入覆盖） */
    signerName: { type: String, default: '' },
    uploadModel: { type: String, default: 'user' },
    uploadPid: { type: Number, default: 7 }
  },
  data() {
    return {
      show: false,
      uploading: false,
      dlgTitle: '手写签名',
      signer: '',
      penWidth: 6,
      strokes: [],
      drawing: false,
      resolver: null
    };
  },
  methods: {
    /** 以 Promise 打开：resolve(相对URL) 表示已上传，resolve(null) 表示取消 */
    open(opts) {
      const o = opts || {};
      if (o.title) this.dlgTitle = o.title;
      if (o.signerName) this.signer = o.signerName;
      this.show = true;
      return new Promise((resolve) => {
        this.resolver = resolve;
      });
    },
    onOpen() {
      this.clear();
      this.$nextTick(this.setupCanvas);
    },
    setupCanvas() {
      const cv = this.$refs.cv;
      if (!cv) return;
      const dpr = Math.min(2, window.devicePixelRatio || 1);
      cv.width = Math.round(W * dpr);
      cv.height = Math.round(H * dpr);
      cv.style.width = W + 'px';
      cv.style.height = H + 'px';
      this.ctx = cv.getContext('2d');
      this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      this.redraw();
    },
    posOf(e) {
      const cv = this.$refs.cv;
      const rect = cv.getBoundingClientRect();
      const p = e.touches && e.touches.length ? e.touches[0] : e;
      return {
        x: ((p.clientX - rect.left) / rect.width) * W,
        y: ((p.clientY - rect.top) / rect.height) * H
      };
    },
    onDown(e) {
      this.drawing = true;
      this.strokes.push({ w: this.penWidth, pts: [this.posOf(e)] });
      this.redraw();
    },
    onMove(e) {
      if (!this.drawing) return;
      const s = this.strokes[this.strokes.length - 1];
      if (!s) return;
      s.pts.push(this.posOf(e));
      this.redraw();
    },
    onUp() {
      this.drawing = false;
    },
    undo() {
      this.strokes.pop();
      this.redraw();
    },
    clear() {
      this.strokes = [];
      this.drawing = false;
      if (this.ctx) this.redraw();
    },
    redraw() {
      const ctx = this.ctx;
      if (!ctx) return;
      ctx.clearRect(0, 0, W, H);
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.strokeStyle = INK;
      this.strokes.forEach((s) => {
        if (!s.pts.length) return;
        ctx.lineWidth = s.w;
        ctx.beginPath();
        ctx.moveTo(s.pts[0].x, s.pts[0].y);
        for (let i = 1; i < s.pts.length; i++) {
          ctx.lineTo(s.pts[i].x, s.pts[i].y);
        }
        if (s.pts.length === 1) {
          // 单击落一个点
          ctx.lineTo(s.pts[0].x + 0.1, s.pts[0].y);
        }
        ctx.stroke();
      });
    },
    /** 裁掉四周空白，导出一张紧凑的透明 PNG */
    exportBlob() {
      let minX = Infinity;
      let minY = Infinity;
      let maxX = -Infinity;
      let maxY = -Infinity;
      let maxW = 1;
      this.strokes.forEach((s) => {
        maxW = Math.max(maxW, s.w);
        s.pts.forEach((p) => {
          if (p.x < minX) minX = p.x;
          if (p.y < minY) minY = p.y;
          if (p.x > maxX) maxX = p.x;
          if (p.y > maxY) maxY = p.y;
        });
      });
      if (!isFinite(minX)) return Promise.resolve(null);
      const pad = maxW * 1.4;
      const x0 = Math.max(0, minX - pad);
      const y0 = Math.max(0, minY - pad);
      const x1 = Math.min(W, maxX + pad);
      const y1 = Math.min(H, maxY + pad);
      const bw = Math.max(8, x1 - x0);
      const bh = Math.max(8, y1 - y0);

      const dpr = Math.min(2, window.devicePixelRatio || 1);
      const out = document.createElement('canvas');
      out.width = Math.round(bw * dpr);
      out.height = Math.round(bh * dpr);
      const octx = out.getContext('2d');
      octx.setTransform(dpr, 0, 0, dpr, 0, 0);
      octx.translate(-x0, -y0);
      octx.lineCap = 'round';
      octx.lineJoin = 'round';
      octx.strokeStyle = INK;
      this.strokes.forEach((s) => {
        if (!s.pts.length) return;
        octx.lineWidth = s.w;
        octx.beginPath();
        octx.moveTo(s.pts[0].x, s.pts[0].y);
        for (let i = 1; i < s.pts.length; i++) {
          octx.lineTo(s.pts[i].x, s.pts[i].y);
        }
        if (s.pts.length === 1) {
          octx.lineTo(s.pts[0].x + 0.1, s.pts[0].y);
        }
        octx.stroke();
      });
      return new Promise((resolve) => out.toBlob(resolve, 'image/png'));
    },
    onUse() {
      this.exportBlob().then((blob) => {
        if (!blob) {
          this.$message.warning('请先手写签名');
          return;
        }
        const file = new File([blob], 'sign.png', { type: 'image/png' });
        const formData = new FormData();
        formData.append('multipart', file);
        this.uploading = true;
        fileFileApi(formData, { model: this.uploadModel, pid: this.uploadPid })
          .then((res) => {
            const raw = res && (res.url || res.sattDir);
            if (!raw) {
              this.$message.error('上传失败，请重试');
              return;
            }
            const i = String(raw).indexOf('qianxuimage/');
            const url = i >= 0 ? String(raw).slice(i) : raw;
            this.$message.success('签名已保存');
            this.show = false;
            if (this.resolver) this.resolver(url);
          })
          .finally(() => {
            this.uploading = false;
          });
      });
    }
  },
  watch: {
    show(v) {
      if (!v && this.resolver) {
        this.resolver(null);
        this.resolver = null;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.sg-stick { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; font-size: 13px; color: #303133; }
.sg-stick-tip { color: #909399; font-size: 12px; }
.sg-canvas {
  display: block; width: 760px; height: 260px;
  border: 1px dashed #dcdfe6; border-radius: 6px; background: #fff;
  cursor: crosshair; touch-action: none;
}
.sg-bar { display: flex; align-items: center; margin-top: 10px; }
.sg-width { width: 160px; margin-left: 16px; }
.sg-tip { margin-left: 10px; color: #909399; font-size: 12px; }
</style>
