<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="card-header"><span>代理设置</span></div>
      <el-form :model="form" label-width="150px" style="max-width: 720px; font-size: 14px" v-loading="loading">
        <el-form-item label="代理功能：">
          <el-radio-group v-model="form.agent_func_status">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
          <div class="tips">关闭后：订单不再自动发放区域代理奖励</div>
        </el-form-item>
        <el-form-item label="开放代理申请：">
          <el-radio-group v-model="form.agent_apply_status">
            <el-radio label="1">开放（会员可申请）</el-radio>
            <el-radio label="0">关闭（仅后台设置代理）</el-radio>
          </el-radio-group>
          <div class="tips">关闭后会员端不再展示申请入口，仅可在后台「代理管理」中直接设置代理</div>
        </el-form-item>
        <el-form-item label="奖励结算时机：">
          <el-radio-group v-model="form.agent_credit_timing">
            <el-radio label="1">订单付款成功后结算</el-radio>
            <el-radio label="2">订单完成后结算</el-radio>
          </el-radio-group>
          <div class="tips">区域代理奖励自动进入代理账户佣金，可按佣金提现规则提现</div>
        </el-form-item>
        <el-form-item label="可申请的代理区域：">
          <el-checkbox-group v-model="applyRegions">
            <el-checkbox :label="1">省级代理</el-checkbox>
            <el-checkbox :label="2">市级代理</el-checkbox>
            <el-checkbox :label="3">区级代理</el-checkbox>
          </el-checkbox-group>
          <div class="tips">会员端申请代理时只能选择此处勾选的区域级别；全部不勾表示会员端不可申请任何区域</div>
        </el-form-item>
        <el-form-item label="默认奖励比例：">
          <div class="ratio-list">
            <div class="ratio-row">
              <span class="ratio-label">省级代理</span>
              <el-input-number v-model="form.agent_default_ratio_province" :min="0" :max="100" :precision="2" :step="0.5" style="width: 160px" />
              <span class="ratio-unit">%</span>
            </div>
            <div class="ratio-row">
              <span class="ratio-label">市级代理</span>
              <el-input-number v-model="form.agent_default_ratio_city" :min="0" :max="100" :precision="2" :step="0.5" style="width: 160px" />
              <span class="ratio-unit">%</span>
            </div>
            <div class="ratio-row">
              <span class="ratio-label">区级代理</span>
              <el-input-number v-model="form.agent_default_ratio_district" :min="0" :max="100" :precision="2" :step="0.5" style="width: 160px" />
              <span class="ratio-unit">%</span>
            </div>
          </div>
          <div class="tips">代理申请「通过」时按级别自动带入默认比例，可在弹窗里修改后保存</div>
        </el-form-item>
        <el-form-item>
          <el-button v-if="checkPermi(['admin:agent:setting:save'])" type="primary" :loading="saveLoading" @click="onSave"
            >保存设置</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { agentSettingApi, agentSettingSaveApi } from '@/api/daili';
import { checkPermi } from '@/utils/permission'; // 权限判断函数

export default {
  name: 'AgentSetting',
  data() {
    return {
      loading: false,
      saveLoading: false,
      form: {
        agent_func_status: '1',
        agent_apply_status: '1',
        agent_credit_timing: '1',
        agent_apply_regions: '1,2,3',
        agent_default_ratio_province: 5,
        agent_default_ratio_city: 3,
        agent_default_ratio_district: 2,
      },
      applyRegions: [1, 2, 3],
    };
  },
  mounted() {
    this.getSetting();
  },
  methods: {
    checkPermi,
    getSetting() {
      this.loading = true;
      agentSettingApi()
        .then((res) => {
          Object.keys(this.form).forEach((k) => {
            if (res && res[k] !== undefined && res[k] !== null) this.form[k] = String(res[k]);
          });
          // 默认比例保持数字类型，供 el-input-number 使用（后端返回字符串）
          ['agent_default_ratio_province', 'agent_default_ratio_city', 'agent_default_ratio_district'].forEach((k) => {
            const n = Number(this.form[k]);
            if (!isNaN(n)) this.form[k] = n;
          });
          const regions = (res && res.agent_apply_regions) || this.form.agent_apply_regions || '1,2,3';
          this.applyRegions = String(regions)
            .split(',')
            .map((v) => parseInt(v.trim()))
            .filter((n) => n === 1 || n === 2 || n === 3);
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    onSave: function () {
      this.saveLoading = true;
      const payload = { ...this.form, agent_apply_regions: this.applyRegions.slice().sort().join(',') };
      agentSettingSaveApi(payload)
        .then(() => {
          this.$message.success('保存成功');
          this.saveLoading = false;
        })
        .catch(() => {
          this.saveLoading = false;
        });
    },
  },
};
</script>

<style scoped lang="scss">
.card-header {
  font-weight: 600;
}
.tips {
  color: #999;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
.ratio-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ratio-row {
  display: flex;
  align-items: center;
  .ratio-label {
    width: 72px;
    flex-shrink: 0;
    color: #606266;
    font-size: 14px;
    white-space: nowrap;
  }
  .ratio-unit {
    margin-left: 8px;
    color: #606266;
  }
}
</style>
