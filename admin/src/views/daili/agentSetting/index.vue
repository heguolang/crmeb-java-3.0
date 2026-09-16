<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="card-header"><span>代理设置</span></div>
      <el-form :model="form" label-width="150px" size="small" style="max-width: 640px" v-loading="loading">
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
      },
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
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    onSave: function () {
      this.saveLoading = true;
      agentSettingSaveApi({ ...this.form })
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
</style>
