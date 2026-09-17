<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header"><b>奖励规则配置</b></div>
      <el-form label-width="170px" size="small" style="max-width: 720px">
        <el-form-item label="订单上级审核">
          <el-switch v-model="form.stock_order_audit" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：下级订货单须经【直接上级】审核通过后才流转总部云仓；关闭：下单直接进入待付款</span>
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
        <el-form-item label="团队级差奖励">
          <el-switch v-model="form.stock_ladder_status" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：按团队业绩所属阶梯，取【与最大直推下级的比例差额】结算</span>
        </el-form-item>
        <el-form-item label="级差结算周期">
          <el-radio-group v-model="form.stock_ladder_cycle">
            <el-radio label="1">按订单结算</el-radio>
            <el-radio label="2">按月统计结算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.stock_ladder_cycle === '2'" label="级差月结">
          <el-date-picker v-model="settleMonth" type="month" value-format="yyyy-MM" placeholder="选择月份" size="small" style="width: 150px" />
          <el-button size="small" type="warning" style="margin-left: 10px" @click="onMonthlySettle">执行月结</el-button>
        </el-form-item>
        <el-form-item label="级差阶梯（团队业绩 → 比例）">
          <div>
            <div v-for="(l, idx) in ladders" :key="idx" style="margin-bottom: 6px">
              <el-input-number v-model="l.minAmount" :min="0" :precision="0" size="mini" style="width: 120px" />
              <span style="margin: 0 4px">~</span>
              <el-input-number v-model="l.maxAmount" :min="0" :precision="0" size="mini" style="width: 120px" placeholder="0=不限" />
              <span style="margin: 0 6px">→</span>
              <el-input-number v-model="l.rate" :min="0" :max="100" :precision="2" size="mini" style="width: 100px" />
              <span style="margin-left: 4px">%</span>
              <el-button type="text" size="mini" class="red" @click="ladders.splice(idx, 1)">删除</el-button>
            </div>
            <el-button size="mini" @click="ladders.push({ minAmount: 0, maxAmount: 0, rate: 2 })">+ 添加阶梯</el-button>
          </div>
        </el-form-item>
        <el-divider />
        <el-form-item label="平级奖励">
          <el-switch v-model="form.stock_peer_status" :active-value="'1'" :inactive-value="'0'" />
          <span class="switch-tip">开启：A 推荐 B 成为【同级】代理后，B 产生订货业绩时 A 按比例拿奖励</span>
        </el-form-item>
        <el-form-item label="平级奖励比例%">
          <el-input-number v-model="peerRateNum" :min="0" :max="100" :precision="2" size="small" style="width: 140px" />
          <span class="switch-tip">按业绩金额的百分比</span>
        </el-form-item>
        <el-form-item label="平级奖励代数">
          <el-input-number v-model="peerGenNum" :min="1" :max="3" :precision="0" size="small" style="width: 140px" />
          <span class="switch-tip">1 = 只拿直接平推的同级（不拿平级的平级）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存全部规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
// force-rebuild-20260917a
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
        stock_peer_status: '1',
        stock_peer_rate: '5',
        stock_peer_generations: '1'
      },
      ladders: [],
      settleMonth: ''
    };
  },
  computed: {
    peerRateNum: {
      get() { return Number(this.form.stock_peer_rate); },
      set(v) { this.form.stock_peer_rate = String(v); }
    },
    peerGenNum: {
      get() { return Number(this.form.stock_peer_generations); },
      set(v) { this.form.stock_peer_generations = String(v); }
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
        this.$confirm('当前没有配置任何级差阶梯，保存后将清空服务端阶梯配置，确认继续？', '提示', { type: 'warning' })
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
      if (!this.settleMonth) return this.$message.error('请选择结算月份');
      this.$confirm('将重算 ' + this.settleMonth + ' 全部代理级差奖励（幂等，可重复执行），确认？', '级差月结').then(() => {
        stockMonthlySettleApi({ month: this.settleMonth }).then(() => {
          this.$message.success('月结完成');
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
