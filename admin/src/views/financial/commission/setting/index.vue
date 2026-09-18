<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="佣金提现" name="commission">
          <el-form label-width="140px" size="small" style="max-width: 720px; margin-top: 10px">
            <el-form-item label="功能开关">
              <el-switch v-model="form.user_extract_switch" active-value="1" inactive-value="0" />
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
              <el-radio-group v-model="form.user_extract_fee_type">
                <el-radio label="fixed">固定金额</el-radio>
                <el-radio label="ratio">比例</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="form.user_extract_fee_type === 'fixed' ? '手续费金额' : '手续费率'">
              <el-input-number v-model="feeNum" :min="0" :precision="2" style="width: 200px" />
              <span class="form-tip">{{ form.user_extract_fee_type === 'fixed' ? '元' : '%' }}</span>
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
                <el-checkbox-group v-model="weekdayList">
                  <el-checkbox v-for="d in weekdayOptions" :key="d.value" :label="d.value">{{ d.label }}</el-checkbox>
                </el-checkbox-group>
                <div style="margin-top: 12px">
                  <el-select v-model="form.user_extract_time_start" style="width: 110px">
                    <el-option v-for="h in hourOptionsStart" :key="'s' + h" :label="padHour(h)" :value="String(h)" />
                  </el-select>
                  <span style="margin: 0 8px">至</span>
                  <el-select v-model="form.user_extract_time_end" style="width: 110px">
                    <el-option v-for="h in hourOptionsEnd" :key="'e' + h" :label="padHour(h)" :value="String(h)" />
                  </el-select>
                  <span class="form-tip">（按服务器时间，结束时刻不含）</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { extractSettingGetApi, extractSettingSaveApi } from '@/api/financial';

export default {
  name: 'ExtractSetting',
  data() {
    return {
      activeTab: 'commission',
      saving: false,
      form: {
        user_extract_switch: '1',
        user_extract_min_price: '100',
        user_extract_multiple: '0',
        user_extract_fee_type: 'ratio',
        user_extract_fee: '0',
        user_extract_weekdays: '1,2,3,4,5,6,7',
        user_extract_time_start: '0',
        user_extract_time_end: '24',
      },
      weekdayList: ['1', '2', '3', '4', '5', '6', '7'],
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
    minPriceNum: {
      get() {
        return Number(this.form.user_extract_min_price) || 0;
      },
      set(v) {
        this.form.user_extract_min_price = String(v);
      },
    },
    multipleNum: {
      get() {
        return Number(this.form.user_extract_multiple) || 0;
      },
      set(v) {
        this.form.user_extract_multiple = String(v);
      },
    },
    feeNum: {
      get() {
        return Number(this.form.user_extract_fee) || 0;
      },
      set(v) {
        this.form.user_extract_fee = String(v);
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
  mounted() {
    this.load();
  },
  methods: {
    padHour(h) {
      return String(h).padStart(2, '0') + ':00';
    },
    load() {
      extractSettingGetApi()
        .then((res) => {
          Object.keys(this.form).forEach((k) => {
            if (res && res[k] !== undefined && res[k] !== null && res[k] !== '') {
              this.form[k] = String(res[k]);
            }
          });
          this.weekdayList = (this.form.user_extract_weekdays || '')
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean);
          if (!this.weekdayList.length) {
            this.weekdayList = ['1', '2', '3', '4', '5', '6', '7'];
          }
        })
        .catch(() => {
          this.$message.error('提现设置加载失败');
        });
    },
    onSave() {
      if (!this.weekdayList.length) {
        this.$message.warning('请至少选择一个可提现日');
        return;
      }
      const start = Number(this.form.user_extract_time_start);
      const end = Number(this.form.user_extract_time_end);
      if (!(end > start)) {
        this.$message.warning('结束时间须大于开始时间');
        return;
      }
      this.form.user_extract_weekdays = this.weekdayList.slice().sort((a, b) => Number(a) - Number(b)).join(',');
      this.saving = true;
      extractSettingSaveApi({ ...this.form })
        .then(() => {
          this.$message.success('保存成功');
        })
        .catch(() => {
          this.$message.error('保存失败');
        })
        .finally(() => {
          this.saving = false;
        });
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
