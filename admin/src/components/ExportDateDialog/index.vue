<template>
  <el-dialog
    title="导出数据"
    :visible.sync="dialogVisible"
    width="480px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form label-width="100px" size="small">
      <el-form-item label="时间范围：">
        <el-date-picker
          v-model="timeVal"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 100%"
          :picker-options="pickerOptions"
        />
      </el-form-item>
      <el-form-item>
        <span class="tip">不选时间则导出全部数据</span>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button size="small" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" size="small" :loading="loading" @click="handleConfirm">确定导出</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ExportDateDialog',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      timeVal: [],
      pickerOptions: {
        shortcuts: [
          {
            text: '今天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              picker.$emit('pick', [start, end]);
            },
          },
          {
            text: '最近7天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 6);
              picker.$emit('pick', [start, end]);
            },
          },
          {
            text: '最近30天',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 29);
              picker.$emit('pick', [start, end]);
            },
          },
        ],
      },
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit('update:visible', val);
      },
    },
  },
  methods: {
    handleConfirm() {
      const dateLimit =
        this.timeVal && this.timeVal.length === 2 ? `${this.timeVal[0]},${this.timeVal[1]}` : '';
      this.$emit('confirm', dateLimit);
    },
    handleClose() {
      this.timeVal = [];
    },
  },
};
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 12px;
}
</style>
