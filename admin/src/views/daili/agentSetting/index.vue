<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="card-header"><span>代理设置</span></div>
      <el-form :model="form" label-width="140px" class="setting-form" style="max-width: 760px" v-loading="loading">
        <!-- 分区一：功能开关（蓝色竖条小标题做扫读锚点） -->
        <div class="section-title">功能开关</div>
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
        <!-- 分区二：申请与奖励 -->
        <div class="section-title section-title--gap">申请与奖励</div>
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
/* 字号三档锁定：卡片头/分区标题 15px、表单标签与控件 14px、说明文字 12px */
.card-header {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
/* 分区小标题：蓝色竖条锚点，第二组与上一组拉开间距 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 18px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
  margin: 4px 0 20px;

  &--gap {
    margin-top: 36px;
  }
}
.setting-form {
  .tips {
    color: #909399;
    font-size: 12px;
    line-height: 1.6;
    margin-top: 6px;
  }
  /* radio/checkbox 选项间距统一 */
  ::v-deep .el-radio,
  ::v-deep .el-checkbox {
    margin-right: 28px;
  }
}
/* 比例行：单一节奏（行距 12px），小标签与表单标签拉开视觉权重（不加粗、灰色降级） */
.ratio-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
    font-size: 14px;
  }
}
</style>
