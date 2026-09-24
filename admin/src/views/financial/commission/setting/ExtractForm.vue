<template>
  <el-form label-width="140px" size="small" style="max-width: 720px; margin-top: 10px">
    <el-form-item label="功能开关">
      <el-switch v-model="form[prefix + '_switch']" active-value="1" inactive-value="0" />
    </el-form-item>
    <el-form-item label="最低提现金额">
      <el-input-number v-model="minPriceNum" :min="0" :precision="2" style="width: 200px" />
      <span class="form-tip">元</span>
    </el-form-item>
    <el-form-item label="提现倍数">
      <el-input-number v-model="multipleNum" :min="0" :precision="2" style="width: 200px" />
      <span class="form-tip">元，0 表示不限制</span>
    </el-form-item>
    <el-form-item label="手续费类型">
      <el-radio-group v-model="form[prefix + '_fee_type']">
        <el-radio label="fixed">固定金额</el-radio>
        <el-radio label="ratio">比例</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item :label="form[prefix + '_fee_type'] === 'fixed' ? '手续费金额' : '手续费率'">
      <el-input-number v-model="feeNum" :min="0" :precision="2" style="width: 200px" />
      <span class="form-tip">{{ form[prefix + '_fee_type'] === 'fixed' ? '元' : '%' }}</span>
    </el-form-item>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="实际到账金额 = 提现金额 - 手续费"
      style="margin: 0 0 18px 140px; max-width: 420px"
    />
    <el-form-item label="可提现时间">
      <div>
        <el-checkbox-group :value="weekdayList" @input="onWeekdayChange">
          <el-checkbox v-for="d in weekdayOptions" :key="d.value" :label="d.value">{{ d.label }}</el-checkbox>
        </el-checkbox-group>
        <div style="margin-top: 12px">
          <el-select v-model="form[prefix + '_time_start']" style="width: 110px">
            <el-option v-for="h in hourOptionsStart" :key="'s' + h" :label="padHour(h)" :value="String(h)" />
          </el-select>
          <span style="margin: 0 8px">至</span>
          <el-select v-model="form[prefix + '_time_end']" style="width: 110px">
            <el-option v-for="h in hourOptionsEnd" :key="'e' + h" :label="padHour(h)" :value="String(h)" />
          </el-select>
          <span class="form-tip">（按服务器时间，结束时刻不含）</span>
        </div>
      </div>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="saving" @click="$emit('save')">保存</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'ExtractForm',
  props: {
    value: { type: Object, required: true },
    weekdayList: { type: Array, required: true },
    saving: { type: Boolean, default: false },
    prefix: { type: String, default: 'user_extract' },
  },
  data() {
    return {
      weekdayOptions: [
        { value: '1', label: '周一' },
        { value: '2', label: '周二' },
        { value: '3', label: '周三' },
        { value: '4', label: '周四' },
        { value: '5', label: '周五' },
        { value: '6', label: '周六' },
        { value: '7', label: '周日' },
      ],
    };
  },
  computed: {
    form: {
      get() {
        return this.value;
      },
      set(v) {
        this.$emit('input', v);
      },
    },
    minPriceNum: {
      get() {
        return Number(this.form[this.prefix + '_min_price']) || 0;
      },
      set(v) {
        this.$set(this.form, this.prefix + '_min_price', String(v));
      },
    },
    multipleNum: {
      get() {
        return Number(this.form[this.prefix + '_multiple']) || 0;
      },
      set(v) {
        this.$set(this.form, this.prefix + '_multiple', String(v));
      },
    },
    feeNum: {
      get() {
        return Number(this.form[this.prefix + '_fee']) || 0;
      },
      set(v) {
        this.$set(this.form, this.prefix + '_fee', String(v));
      },
    },
    hourOptionsStart() {
      const list = [];
      for (let i = 0; i <= 23; i++) list.push(i);
      return list;
    },
    hourOptionsEnd() {
      const list = [];
      for (let i = 1; i <= 24; i++) list.push(i);
      return list;
    },
  },
  methods: {
    padHour(h) {
      return String(h).padStart(2, '0') + ':00';
    },
    onWeekdayChange(val) {
      this.$emit('update:weekdayList', val);
    },
  },
};
</script>

<style scoped>
.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
