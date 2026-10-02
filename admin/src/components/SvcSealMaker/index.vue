<template>
  <el-dialog
    :title="dlgTitle"
    :visible.sync="show"
    width="880px"
    append-to-body
    :close-on-click-modal="false"
    @open="onOpen"
  >
    <div class="mk-wrap" v-loading="uploading" element-loading-text="正在上传签章图片...">
      <div class="mk-stage">
        <canvas ref="cv" class="mk-canvas" />
        <div class="mk-stage-tip">透明底 PNG，盖到合同上不会遮挡正文</div>
      </div>
      <div class="mk-form">
        <el-form label-width="80px" size="small">
          <el-form-item label="主体名称">
            <el-input v-model="name" maxlength="40" placeholder="企业全称，如：贵州黔序科技有限公司" @input="render" />
          </el-form-item>
          <el-form-item label="公章样式">
            <span class="mk-fixed">圆形公章（统一只用这一款）</span>
          </el-form-item>
          <el-form-item label="倾斜">
            <el-slider v-model="tiltDeg" :min="-15" :max="15" :step="1" :show-tooltip="false" />
            <span class="mk-tip">{{ tiltDeg }}°（模拟手工盖章的轻微倾斜，0 为不倾斜）</span>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div slot="footer">
      <el-button size="small" @click="show = false">取消</el-button>
      <el-button size="small" @click="render">重新生成</el-button>
      <el-button size="small" type="primary" :loading="uploading" @click="onUse">保存并使用</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { paintSeal } from '@/utils/sealPainter';
import { fileFileApi } from '@/api/systemSetting';

export default {
  name: 'SvcSealMaker',
  props: {
    /** 打开时的初始主体名称（也可由 open() 传入覆盖） */
    defaultName: { type: String, default: '' },
    /** 上传时的 model / pid，与合同附件保持一致 */
    uploadModel: { type: String, default: 'user' },
    uploadPid: { type: Number, default: 7 }
  },
  data() {
    return {
      show: false,
      uploading: false,
      dlgTitle: '生成乙方公章',
      name: '',
      tiltDeg: -4,
      resolver: null
    };
  },
  methods: {
    /** 以 Promise 打开：resolve(相对URL) 表示用户已生成并上传，resolve(null) 表示取消 */
    open(opts) {
      const o = opts || {};
      if (o.title) this.dlgTitle = o.title;
      if (o.defaultName) this.name = o.defaultName;
      this.show = true;
      return new Promise((resolve) => {
        this.resolver = resolve;
      });
    },
    onOpen() {
      if (!this.name) this.name = this.defaultName || '';
      this.render();
    },
    drawTo(canvas, size, tiltDeg) {
      const dpr = Math.min(2, window.devicePixelRatio || 1);
      const px = Math.round(size * dpr);
      canvas.width = px;
      canvas.height = px;
      canvas.style.width = size + 'px';
      canvas.style.height = size + 'px';
      const ctx = canvas.getContext('2d');
      ctx.setTransform(1, 0, 0, 1, 0, 0);
      ctx.clearRect(0, 0, px, px);
      ctx.scale(dpr, dpr);
      ctx.lineJoin = 'round';
      paintSeal(ctx, {
        style: 'round',
        size: size,
        name: this.name,
        tilt: ((tiltDeg || 0) * Math.PI) / 180
      });
    },
    render() {
      this.$nextTick(() => {
        const cv = this.$refs.cv;
        if (cv) this.drawTo(cv, 320, this.tiltDeg);
      });
    },
    onUse() {
      if (!String(this.name || '').trim()) {
        this.$message.warning('请先填写主体名称');
        return;
      }
      const cv = this.$refs.cv;
      if (!cv) return;
      cv.toBlob((blob) => {
        if (!blob) {
          this.$message.error('生成签章图片失败，请重试');
          return;
        }
        const file = new File([blob], 'seal.png', { type: 'image/png' });
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
            this.$message.success('签章已生成并上传');
            this.show = false;
            if (this.resolver) this.resolver(url);
          })
          .finally(() => {
            this.uploading = false;
          });
      }, 'image/png');
    }
  },
  watch: {
    show(v) {
      if (!v && this.resolver) {
        this.resolver(null);
        this.resolver = null;
      }
    },
    tiltDeg() {
      this.render();
    }
  }
};
</script>

<style scoped lang="scss">
.mk-wrap { display: flex; gap: 16px; align-items: flex-start; }
.mk-stage { flex: none; text-align: center; }
.mk-canvas {
  width: 320px; height: 320px; border-radius: 6px;
  background: repeating-conic-gradient(#f2f3f5 0% 25%, #fff 0% 50%) 50% / 16px 16px;
  border: 1px solid #ebeef5;
}
.mk-stage-tip { margin-top: 6px; color: #909399; font-size: 12px; }
.mk-form { flex: 1; min-width: 0; }
.mk-fixed { color: #606266; font-size: 13px; }
.mk-tip { color: #909399; font-size: 12px; margin-left: 8px; }
</style>
