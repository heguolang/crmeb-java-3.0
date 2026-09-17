<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header"><b>奖励规则配置</b></div>
      <el-form label-width="170px" size="small" style="max-width: 720px">
        <el-form-item label="订单上级审核">
          <el-switch v-model="form.stock_order_audit" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：下级订货单须经【直接上级】审核通过后才流转总部云仓；关闭：下单直接进入待付款</span>
        </el-form-item>
        <el-form-item label="上级代理发货">
          <el-switch v-model="form.stock_parent_deliver" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：有上级的订单由上级代理在【会员端】填快递发货，后台不可代发；关闭：总部后台统一发货</span>
        </el-form-item>
        <el-form-item label="上级无库存等待时长">
          <el-input-number v-model="upSearchHoursNum" :min="1" :max="168" :precision="0" size="small" style="width: 140px" />
          <span class="switch-tip">小时。上级库存不足时订单挂起，超过该时长自动为下级向上匹配有货的更高级上级（都没有则挂总部）</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="差价奖励">
          <el-switch v-model="form.stock_diff_reward_status" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：直接上级赚取（下级拿货价 − 上级拿货价）× 数量</span>
        </el-form-item>
        <el-form-item label="换货单参与差价奖励">
          <el-switch v-model="form.stock_exchange_diff" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：换货完成时同样按差价发放奖励</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="阶梯业绩奖励">
          <el-switch v-model="form.stock_ladder_status" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：团队业绩达到阶梯后按【固定金额 或 业绩×比例】二选一奖励，每个订货商规则相同，按下方周期一次性自动结算</span>
        </el-form-item>
        <el-form-item label="结算周期">
          <el-radio-group v-model="form.stock_ladder_cycle">
            <el-radio label="1">月度</el-radio>
            <el-radio label="2">季度</el-radio>
            <el-radio label="3">年度</el-radio>
          </el-radio-group>
          <span class="switch-tip">每月 1 号凌晨 1 点系统自动结算上一周期（季度=季度首月 1 号，年度=1 月 1 号）</span>
        </el-form-item>
        <el-form-item label="手动补结算">
          <el-date-picker v-model="settleMonth" type="month" value-format="yyyy-MM" placeholder="选择月份" size="small" style="width: 150px" />
          <el-button size="small" type="warning" style="margin-left: 10px" @click="onMonthlySettle">立即结算该周期</el-button>
          <span class="switch-tip">选择周期内任一月份，系统自动归集（幂等，可重复执行）</span>
        </el-form-item>
        <el-form-item label="业绩阶梯（业绩 → 固定金额 或 比例%）">
          <div>
            <div v-for="(l, idx) in ladders" :key="idx" style="margin-bottom: 6px">
              <el-input-number v-model="l.minAmount" :min="0" :precision="0" size="mini" style="width: 120px" />
              <span style="margin: 0 4px">~</span>
              <el-input-number v-model="l.maxAmount" :min="0" :precision="0" size="mini" style="width: 120px" placeholder="0=不限" />
              <span style="margin: 0 6px">→</span>
              <el-input-number v-model="l.reward" :min="0" :precision="2" size="mini" style="width: 110px" placeholder="固定奖励" />
              <span style="margin: 0 2px">元</span>
              <span style="margin: 0 4px">+</span>
              <el-input-number v-model="l.rate" :min="0" :max="100" :precision="2" size="mini" style="width: 100px" />
              <span style="margin-left: 4px">%</span>
              <el-button type="text" size="mini" class="red" @click="ladders.splice(idx, 1)">删除</el-button>
            </div>
            <el-button size="mini" @click="ladders.push({ minAmount: 0, maxAmount: 0, reward: 0, rate: 0 })">+ 添加阶梯</el-button>
            <div class="switch-tip" style="margin-top: 6px; margin-left: 0">示例：月业绩 1~2 万 → 奖 500 元；2~5 万 → 奖 3000 元。每档【固定金额 / 比例%】二选一：填了固定金额直接发固定，否则按 团队业绩 × 比例% 发放</div>
          </div>
        </el-form-item>
        <el-divider />
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存全部规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { stockSettingApi, stockSettingSaveApi, stockMonthlySettleApi } from '@/api/stock';

export default {
  name: 'StockSetting',
  data() {
    return {
      saving: false,
      loaded: false,
      form: {
        stock_order_audit: '1',
        stock_diff_reward_status: '1',
        stock_exchange_diff: '0',
        stock_ladder_status: '1',
        stock_ladder_cycle: '1',
        stock_parent_deliver: '0',
        stock_up_search_hours: '12'
      },
      ladders: [],
      settleMonth: ''
    };
  },
  computed: {
    upSearchHoursNum: {
      get() { return Number(this.form.stock_up_search_hours) || 12; },
      set(v) { this.form.stock_up_search_hours = String(v); }
    }
  },
  methods: {
    load() {
      stockSettingApi().then(res => {
        Object.keys(this.form).forEach(k => {
          if (res && res[k] !== undefined && res[k] !== null) this.form[k] = String(res[k]);
        });
        this.ladders = (res && res.ladders) || [];
        this.loaded = true;
      }).catch(() => {
        this.$message.error('奖励规则加载失败，请刷新重试');
      });
    },
    onSave() {
      if (!this.ladders.length) {
        this.$confirm('当前没有配置任何业绩阶梯，保存后将清空服务端阶梯配置，确认继续？', '提示', { type: 'warning' })
          .then(() => this.doSave()).catch(() => {});
        return;
      }
      this.doSave();
    },
    doSave() {
      this.saving = true;
      stockSettingSaveApi({ ...this.form, ladders: this.ladders }).then(() => {
        this.$message.success('规则已保存');
        this.saving = false;
      }).catch(() => {
        this.$message.error('保存失败，请重试');
        this.saving = false;
      });
    },
    onMonthlySettle() {
      if (!this.settleMonth) return this.$message.error('请选择周期内任一月份');
      const type = Number(this.form.stock_ladder_cycle) || 1;
      const name = type === 1 ? '月度' : (type === 2 ? '季度' : '年度');
      this.$confirm('将重算 ' + this.settleMonth + ' 所属' + name + '周期的全部代理阶梯业绩奖励（幂等，可重复执行），确认？', '阶梯业绩结算').then(() => {
        stockMonthlySettleApi({ type: type, month: this.settleMonth }).then(() => {
          this.$message.success('结算完成');
        });
      }).catch(() => {});
    }
  },
  mounted() {
    this.load();
  }
};
</script>

<style scoped>
.red { color: #f56c6c; }
.switch-tip { margin-left: 12px; font-size: 12px; color: #909399; line-height: 1.5; }
</style>
