<template>
  <el-dialog
    title="设置用印位置"
    :visible.sync="show"
    width="1100px"
    top="4vh"
    append-to-body
    :close-on-click-modal="false"
    @open="onOpen"
    @closed="onClosed"
  >
    <div v-loading="loading" element-loading-text="正在生成合同正文预览，请稍候...">
      <div class="ly-bar">
        <span class="ly-group ly-group--a">甲方 · 客户</span>
        <el-button-group>
          <el-button
            v-for="t in groupItems('a')"
            :key="t.type"
            size="mini"
            :type="hasType(t.type) ? 'warning' : 'default'"
            :plain="!hasType(t.type)"
            @click="addItem(t)"
          >{{ t.label }}</el-button>
        </el-button-group>
        <span class="ly-gap" />
        <span class="ly-group ly-group--b">乙方 · 黔序</span>
        <el-button-group>
          <el-button
            v-for="t in groupItems('b')"
            :key="t.type"
            size="mini"
            :type="hasType(t.type) ? 'primary' : 'default'"
            :plain="!hasType(t.type)"
            @click="addItem(t)"
          >{{ t.label }}</el-button>
        </el-button-group>
        <span class="ly-gap" />
        <el-button size="mini" :disabled="page <= 1" @click="gotoPage(page - 1)">上一页</el-button>
        <span class="ly-page">{{ page }} / {{ pageCount || 1 }}</span>
        <el-button size="mini" :disabled="page >= pageCount" @click="gotoPage(page + 1)">下一页</el-button>
        <span class="ly-gap" />
        <el-button size="mini" type="danger" plain :disabled="!selected" @click="removeSelected">删除选中</el-button>
        <el-button size="mini" plain :disabled="!items.length" @click="clearAll">清空全部</el-button>
      </div>

      <div class="ly-body">
        <div class="ly-stage-col">
          <div ref="stage" class="ly-stage" :style="stageStyle">
            <img v-if="bgUrl" :src="bgUrl" class="ly-bg" alt="" />
            <div v-else class="ly-empty">暂无预览</div>
            <div
              v-for="it in pageItems"
              :key="it.uid"
              class="ly-item"
              :class="{ 'is-active': it.uid === selectedUid }"
              :style="itemStyle(it)"
              @mousedown.stop="startDrag($event, it)"
            >
              <img v-if="previewOf(it)" :src="previewOf(it)" class="ly-item-img" alt="" />
              <span v-else class="ly-item-label">{{ labelOf(it.type) }}</span>
              <i class="ly-handle" title="拖动缩放" @mousedown.stop="startResize($event, it)" />
            </div>
          </div>
          <div class="ly-hint">
            点按钮把<b>公章 / 签字</b>摆到合同页面上，拖右下角小方块缩放；位置按百分比保存，与分辨率无关。
            <b>日期不用摆</b>——双方各自在会员端签署时自动带在签字下方。公章由系统按签约方名称自动生成圆形章，无需上传。
          </div>
        </div>

        <div class="ly-side">
          <div v-if="!selected" class="ly-side-empty">点选左侧任意用印元素，可精确调整它的位置与大小</div>
          <el-form v-else label-width="80px" size="mini">
            <el-form-item label="元素">
              <el-tag size="mini" :type="tagTypeOf(selected.type)">{{ labelOf(selected.type) }}</el-tag>
              <span class="ly-side-page">第 {{ selected.page }} 页</span>
            </el-form-item>
            <el-form-item label="横向 X">
              <el-slider v-model="selected.x" :min="0" :max="100" :step="0.1" :show-tooltip="false" />
              <el-input-number v-model="selected.x" :min="0" :max="100" :step="0.5" :precision="2" controls-position="right" class="ly-num" />
            </el-form-item>
            <el-form-item label="纵向 Y">
              <el-slider v-model="selected.y" :min="0" :max="100" :step="0.1" :show-tooltip="false" />
              <el-input-number v-model="selected.y" :min="0" :max="100" :step="0.5" :precision="2" controls-position="right" class="ly-num" />
            </el-form-item>
            <el-form-item label="宽度 W">
              <el-input-number v-model="selected.w" :min="1" :max="100" :step="0.5" :precision="2" controls-position="right" class="ly-num" />
            </el-form-item>
            <el-form-item label="高度 H">
              <el-input-number v-model="selected.h" :min="0.5" :max="100" :step="0.5" :precision="2" controls-position="right" class="ly-num" />
            </el-form-item>
            <el-form-item label="倾斜角">
              <el-input-number v-model="selected.tilt" :min="-30" :max="30" :step="1" :precision="1" controls-position="right" class="ly-num" />
              <div class="ly-side-tip">轻微倾斜更像真实用印（0 表示不倾斜）</div>
            </el-form-item>
            <el-form-item label="所在页">
              <el-input-number v-model="selected.page" :min="1" :max="pageCount || 1" controls-position="right" class="ly-num" />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="ly-summary">
        <span v-if="!items.length" class="ly-warn">尚未放置任何用印元素</span>
        <span v-else>已放置 {{ items.length }} 个元素：{{ summaryText }}</span>
      </div>
    </div>

    <div slot="footer">
      <el-button size="small" @click="show = false">取消</el-button>
      <el-button size="small" type="primary" :loading="saving" @click="onSave">保存用印位置</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { svcContractPageObjectUrl, svcContractLayoutSaveApi, releaseObjectUrl } from '@/api/enterprise';

const PALETTE = [
  { type: 'custSeal', label: '甲方公章', group: 'a', w: 13, h: 13, tilt: 5 },
  { type: 'custSign', label: '甲方签字', group: 'a', w: 16, h: 7, tilt: 0 },
  { type: 'ourSeal', label: '乙方公章', group: 'b', w: 13, h: 13, tilt: -4 },
  { type: 'ourSign', label: '乙方签字', group: 'b', w: 16, h: 7, tilt: 0 }
];

let uidSeed = 1;

export default {
  name: 'SvcContractLayout',
  props: {
    contractId: { type: Number, default: 0 },
    layout: { type: String, default: '' },
    pageTotal: { type: Number, default: 0 },
    /** 已上传/生成的图片 URL，用于所见即所得预览 */
    images: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      show: false,
      loading: false,
      saving: false,
      palette: PALETTE,
      items: [],
      page: 1,
      pageCount: 1,
      ratio: 1.414, // 页面高宽比，加载底图后按实际值修正
      bgUrl: '',
      selectedUid: 0,
      drag: null
    };
  },
  computed: {
    stageStyle() {
      return { paddingTop: (this.ratio * 100).toFixed(4) + '%' };
    },
    pageItems() {
      return this.items.filter((it) => it.page === this.page);
    },
    selected() {
      return this.items.find((it) => it.uid === this.selectedUid) || null;
    },
    summaryText() {
      const parts = PALETTE.filter((t) => this.hasType(t.type)).map((t) => t.label + '×' + this.countType(t.type));
      return parts.join('、');
    }
  },
  beforeDestroy() {
    releaseObjectUrl(this.bgUrl);
    this.unbindDrag();
  },
  methods: {
    open() {
      this.show = true;
    },
    groupItems(group) {
      return PALETTE.filter((t) => t.group === group);
    },
    onOpen() {
      this.items = this.parseLayout(this.layout);
      this.pageCount = this.pageTotal > 0 ? this.pageTotal : 1;
      this.page = 1;
      this.selectedUid = 0;
      this.loadBg();
    },
    onClosed() {
      releaseObjectUrl(this.bgUrl);
      this.bgUrl = '';
      this.unbindDrag();
    },
    parseLayout(json) {
      if (!json) return [];
      let arr = json;
      if (typeof json === 'string') {
        try {
          arr = JSON.parse(json);
        } catch (e) {
          return [];
        }
      }
      if (!Array.isArray(arr)) return [];
      return arr.map((it) => ({
        uid: uidSeed++,
        type: it.type === 'date' ? 'ourDate' : it.type,
        page: Number(it.page) || 1,
        x: Number(it.x) || 0,
        y: Number(it.y) || 0,
        w: Number(it.w) || 12,
        h: Number(it.h) || 12,
        tilt: Number(it.tilt) || 0
      }));
    },
    loadBg() {
      releaseObjectUrl(this.bgUrl);
      this.bgUrl = '';
      if (!this.contractId) return;
      this.loading = true;
      svcContractPageObjectUrl(this.contractId, this.page, 1240)
        .then((url) => {
          this.bgUrl = url;
          const img = new Image();
          img.onload = () => {
            if (img.naturalWidth > 0) {
              this.ratio = img.naturalHeight / img.naturalWidth;
            }
          };
          img.src = url;
        })
        .catch((e) => {
          this.$message.error((e && e.message) || '合同正文预览加载失败');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    gotoPage(p) {
      if (p < 1 || (this.pageCount && p > this.pageCount)) return;
      this.page = p;
      this.loadBg();
    },
    hasType(type) {
      return this.items.some((it) => it.type === type);
    },
    countType(type) {
      return this.items.filter((it) => it.type === type).length;
    },
    labelOf(type) {
      const t = PALETTE.find((x) => x.type === type);
      return t ? t.label : type;
    },
    tagTypeOf(type) {
      return type.indexOf('our') === 0 ? 'primary' : 'warning';
    },
    previewOf(it) {
      if (it.type === 'ourSeal' || it.type === 'ourSign' || it.type === 'custSeal' || it.type === 'custSign') {
        return this.images[it.type] || '';
      }
      return '';
    },
    itemStyle(it) {
      return {
        left: it.x + '%',
        top: it.y + '%',
        width: it.w + '%',
        height: it.h + '%',
        transform: 'rotate(' + it.tilt + 'deg)'
      };
    },
    addItem(t) {
      const it = {
        uid: uidSeed++,
        type: t.type,
        page: this.page,
        x: Math.max(0, Math.min(100 - t.w, 60)),
        y: Math.max(0, Math.min(100 - t.h, 68)),
        w: t.w,
        h: t.h,
        tilt: 0
      };
      this.items.push(it);
      this.selectedUid = it.uid;
      this.$nextTick(this.bindDrag);
    },
    removeSelected() {
      const i = this.items.findIndex((x) => x.uid === this.selectedUid);
      if (i >= 0) this.items.splice(i, 1);
      this.selectedUid = 0;
      this.unbindDrag();
    },
    clearAll() {
      this.$confirm('确认清空所有已放置的用印位置？', '提示', { type: 'warning' })
        .then(() => {
          this.items = [];
          this.selectedUid = 0;
        })
        .catch(() => {});
    },
    /** ---- 拖拽 / 缩放：统一用全局事件，避免鼠标移出元素后跟丢 ---- */
    bindDrag() {
      window.addEventListener('mousemove', this.onDragMove);
      window.addEventListener('mouseup', this.onDragEnd);
    },
    unbindDrag() {
      window.removeEventListener('mousemove', this.onDragMove);
      window.removeEventListener('mouseup', this.onDragEnd);
      this.drag = null;
    },
    stageRect() {
      return this.$refs.stage ? this.$refs.stage.getBoundingClientRect() : { width: 1, height: 1 };
    },
    startDrag(e, it) {
      this.selectedUid = it.uid;
      const rect = this.stageRect();
      this.drag = {
        mode: 'move',
        uid: it.uid,
        startX: e.clientX,
        startY: e.clientY,
        x0: it.x,
        y0: it.y,
        w: it.w,
        h: it.h,
        rw: rect.width || 1,
        rh: rect.height || 1
      };
      this.bindDrag();
    },
    startResize(e, it) {
      this.selectedUid = it.uid;
      const rect = this.stageRect();
      this.drag = {
        mode: 'resize',
        uid: it.uid,
        startX: e.clientX,
        startY: e.clientY,
        w0: it.w,
        h0: it.h,
        rw: rect.width || 1,
        rh: rect.height || 1
      };
      this.bindDrag();
    },
    onDragMove(e) {
      if (!this.drag) return;
      const it = this.items.find((x) => x.uid === this.drag.uid);
      if (!it) return;
      const d = this.drag;
      const dx = ((e.clientX - d.startX) / d.rw) * 100;
      const dy = ((e.clientY - d.startY) / d.rh) * 100;
      if (d.mode === 'move') {
        it.x = this.round2(Math.max(0, Math.min(100 - it.w, d.x0 + dx)));
        it.y = this.round2(Math.max(0, Math.min(100 - it.h, d.y0 + dy)));
      } else {
        it.w = this.round2(Math.max(2, Math.min(100 - it.x, d.w0 + dx)));
        it.h = this.round2(Math.max(1, Math.min(100 - it.y, d.h0 + dy)));
      }
    },
    onDragEnd() {
      this.unbindDrag();
    },
    round2(n) {
      return Math.round(n * 100) / 100;
    },
    onSave() {
      if (!this.items.length) {
        this.$message.warning('请先放置至少一个用印元素');
        return;
      }
      const out = this.items.map((it) => ({
        type: it.type,
        page: it.page,
        x: this.round2(it.x),
        y: this.round2(it.y),
        w: this.round2(it.w),
        h: this.round2(it.h),
        tilt: Math.round(it.tilt * 10) / 10
      }));
      this.saving = true;
      svcContractLayoutSaveApi({ id: this.contractId, signLayout: JSON.stringify(out) })
        .then(() => {
          this.$message.success('用印位置已保存');
          this.show = false;
          this.$emit('saved', JSON.stringify(out));
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style scoped lang="scss">
.ly-bar { display: flex; align-items: center; flex-wrap: wrap; padding-bottom: 8px; }
.ly-gap { display: inline-block; width: 14px; }
.ly-group { font-size: 12px; font-weight: 600; margin-right: 6px; }
.ly-group--a { color: #b88230; }
.ly-group--b { color: #1f5f9e; }
.ly-page { margin: 0 8px; font-size: 12px; color: #606266; }

.ly-body { display: flex; gap: 12px; align-items: flex-start; }
.ly-stage-col { flex: 1; min-width: 0; }
.ly-stage {
  position: relative;
  height: 0;
  background: #f5f6f8;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  user-select: none;
}
.ly-bg { position: absolute; left: 0; top: 0; width: 100%; height: 100%; object-fit: fill; }
.ly-empty {
  position: absolute; left: 0; top: 0; right: 0; bottom: 0;
  display: flex; align-items: center; justify-content: center;
  color: #c0c4cc; font-size: 13px;
}
.ly-hint { margin-top: 8px; color: #909399; font-size: 12px; line-height: 1.7; }

.ly-item {
  position: absolute;
  box-sizing: border-box;
  border: 1px dashed #409eff;
  background: rgba(64, 158, 255, 0.08);
  cursor: move;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.ly-item.is-active { border-color: #d3232f; background: rgba(211, 35, 47, 0.1); box-shadow: 0 0 0 1px rgba(211, 35, 47, 0.35); }
.ly-item-img { width: 100%; height: 100%; object-fit: contain; pointer-events: none; }
.ly-item-label { color: #409eff; font-size: 12px; white-space: nowrap; pointer-events: none; }
.ly-handle {
  position: absolute; right: -5px; bottom: -5px;
  width: 10px; height: 10px; border-radius: 2px;
  background: #409eff; border: 1px solid #fff; cursor: nwse-resize;
}
.ly-item.is-active .ly-handle { background: #d3232f; }

.ly-side { width: 300px; flex: none; border-left: 1px solid #ebeef5; padding-left: 12px; }
.ly-side-empty { color: #909399; font-size: 12px; line-height: 1.8; padding-top: 8px; }
.ly-side-page { margin-left: 8px; color: #909399; font-size: 12px; }
.ly-side-tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 2px; }
.ly-num { width: 100px; }

.ly-summary { margin-top: 10px; padding: 8px 10px; background: #f7f8fa; border-radius: 4px; font-size: 12px; color: #606266; }
.ly-warn { color: #e6a23c; }
</style>
