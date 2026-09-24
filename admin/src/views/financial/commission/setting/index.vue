<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="佣金提现" name="commission">
          <extract-form
            v-model="commissionForm"
            :weekday-list.sync="commissionWeekdays"
            :saving="savingCommission"
            @save="onSaveCommission"
          />
        </el-tab-pane>
        <el-tab-pane label="余额提现" name="balance">
          <extract-form
            v-model="balanceForm"
            :weekday-list.sync="balanceWeekdays"
            :saving="savingBalance"
            prefix="user_balance_extract"
            @save="onSaveBalance"
          />
        </el-tab-pane>
        <el-tab-pane label="支持银行" name="bank">
          <el-form label-width="140px" size="small" style="max-width: 720px; margin-top: 10px">
            <el-form-item label="支持银行">
              <div>
                <div class="bank-tags">
                  <el-tag
                    v-for="(bank, idx) in bankList"
                    :key="bank + idx"
                    closable
                    size="medium"
                    style="margin: 0 8px 8px 0"
                    @close="removeBank(idx)"
                  >{{ bank }}</el-tag>
                </div>
                <div class="bank-add">
                  <el-input
                    v-model="bankInput"
                    placeholder="输入银行名称后回车或点击添加"
                    style="width: 280px"
                    maxlength="32"
                    @keyup.enter.native="addBank"
                  />
                  <el-button type="primary" plain style="margin-left: 8px" @click="addBank">添加</el-button>
                </div>
                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  title="用户添加提现银行卡时，仅可从上述银行中选择"
                  style="margin-top: 14px; max-width: 480px"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingBank" @click="onSaveBank">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { extractSettingGetApi, extractSettingSaveApi } from '@/api/financial';
import ExtractForm from './ExtractForm.vue';

const defaultCommission = () => ({
  user_extract_switch: '1',
  user_extract_min_price: '1',
  user_extract_multiple: '0',
  user_extract_fee_type: 'ratio',
  user_extract_fee: '0',
  user_extract_weekdays: '1,2,3,4,5,6,7',
  user_extract_time_start: '0',
  user_extract_time_end: '24',
});

const defaultBalance = () => ({
  user_balance_extract_switch: '0',
  user_balance_extract_min_price: '1',
  user_balance_extract_multiple: '0',
  user_balance_extract_fee_type: 'ratio',
  user_balance_extract_fee: '0',
  user_balance_extract_weekdays: '1,2,3,4,5,6,7',
  user_balance_extract_time_start: '0',
  user_balance_extract_time_end: '24',
});

export default {
  name: 'ExtractSetting',
  components: { ExtractForm },
  data() {
    return {
      activeTab: 'commission',
      savingCommission: false,
      savingBalance: false,
      savingBank: false,
      commissionForm: defaultCommission(),
      balanceForm: defaultBalance(),
      commissionWeekdays: ['1', '2', '3', '4', '5', '6', '7'],
      balanceWeekdays: ['1', '2', '3', '4', '5', '6', '7'],
      bankList: [],
      bankInput: '',
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    parseWeekdays(str) {
      const list = (str || '')
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      return list.length ? list : ['1', '2', '3', '4', '5', '6', '7'];
    },
    load() {
      extractSettingGetApi()
        .then((res) => {
          Object.keys(this.commissionForm).forEach((k) => {
            if (res && res[k] !== undefined && res[k] !== null && res[k] !== '') {
              this.commissionForm[k] = String(res[k]);
            }
          });
          Object.keys(this.balanceForm).forEach((k) => {
            if (res && res[k] !== undefined && res[k] !== null && res[k] !== '') {
              this.balanceForm[k] = String(res[k]);
            }
          });
          this.commissionWeekdays = this.parseWeekdays(this.commissionForm.user_extract_weekdays);
          this.balanceWeekdays = this.parseWeekdays(this.balanceForm.user_balance_extract_weekdays);
          const bankStr = (res && res.user_extract_bank) || '';
          this.bankList = bankStr
            .replace(/\\n/g, '\n')
            .split(/\r?\n/)
            .map((s) => s.trim())
            .filter(Boolean);
        })
        .catch(() => {
          this.$message.error('提现设置加载失败');
        });
    },
    validateTime(weekdays, start, end) {
      if (!weekdays.length) {
        this.$message.warning('请至少选择一个可提现日');
        return false;
      }
      if (!(Number(end) > Number(start))) {
        this.$message.warning('结束时间须大于开始时间');
        return false;
      }
      return true;
    },
    onSaveCommission() {
      if (!this.validateTime(this.commissionWeekdays, this.commissionForm.user_extract_time_start, this.commissionForm.user_extract_time_end)) {
        return;
      }
      this.commissionForm.user_extract_weekdays = this.commissionWeekdays
        .slice()
        .sort((a, b) => Number(a) - Number(b))
        .join(',');
      this.savingCommission = true;
      extractSettingSaveApi({ ...this.commissionForm })
        .then(() => this.$message.success('保存成功'))
        .catch(() => this.$message.error('保存失败'))
        .finally(() => {
          this.savingCommission = false;
        });
    },
    onSaveBalance() {
      if (!this.validateTime(this.balanceWeekdays, this.balanceForm.user_balance_extract_time_start, this.balanceForm.user_balance_extract_time_end)) {
        return;
      }
      this.balanceForm.user_balance_extract_weekdays = this.balanceWeekdays
        .slice()
        .sort((a, b) => Number(a) - Number(b))
        .join(',');
      this.savingBalance = true;
      extractSettingSaveApi({ ...this.balanceForm })
        .then(() => this.$message.success('保存成功'))
        .catch(() => this.$message.error('保存失败'))
        .finally(() => {
          this.savingBalance = false;
        });
    },
    addBank() {
      const name = (this.bankInput || '').trim();
      if (!name) {
        this.$message.warning('请输入银行名称');
        return;
      }
      if (this.bankList.includes(name)) {
        this.$message.warning('该银行已存在');
        return;
      }
      this.bankList.push(name);
      this.bankInput = '';
    },
    removeBank(idx) {
      this.bankList.splice(idx, 1);
    },
    onSaveBank() {
      if (!this.bankList.length) {
        this.$message.warning('请至少添加一个支持银行');
        return;
      }
      this.savingBank = true;
      extractSettingSaveApi({ user_extract_bank: this.bankList.join('\n') })
        .then(() => this.$message.success('保存成功'))
        .catch(() => this.$message.error('保存失败'))
        .finally(() => {
          this.savingBank = false;
        });
    },
  },
};
</script>

<style scoped>
.bank-tags {
  min-height: 32px;
}
.bank-add {
  margin-top: 8px;
}
</style>
