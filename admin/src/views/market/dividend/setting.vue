<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部信息条：长说明压缩成一句话 -->
      <div class="info-bar">
        分红等级在「<b>分红等级</b>」页独立维护；满足升级条件的会员按等级比例参与<b>均分</b>，每次分发刚结束一个周期的数据
      </div>

      <div class="setting-body">
        <!-- 分红开关 -->
        <div class="form-group">
          <div class="group-title">分红开关</div>
          <div class="switch-row">
            <el-switch v-model="configForm.status" active-text="开" inactive-text="关" />
            <span class="row-tip">关闭后停止所有周期的自动结算，已产生数据保留</span>
          </div>
        </div>

        <!-- 分红周期 -->
        <div class="form-group">
          <div class="group-title">分红周期</div>
          <el-radio-group v-model="configForm.cycle">
            <el-radio :label="1">日分红</el-radio>
            <el-radio :label="2">周分红</el-radio>
            <el-radio :label="3">月分红</el-radio>
          </el-radio-group>
          <div class="field-tip">{{ cycleTip }}</div>
        </div>

        <!-- 分红基数 -->
        <div class="form-group">
          <div class="group-title">分红基数</div>
          <el-radio-group v-model="configForm.base">
            <el-radio :label="1">订单总业绩</el-radio>
            <el-radio :label="2">订单利润（商品售价 − 成本价）</el-radio>
          </el-radio-group>
          <div class="field-tip">基数为期内已支付订单统计；比例 × 基数后，在各等级参与会员间均分</div>
        </div>
      </div>

      <!-- 底部操作区：保存主按钮 + 手动结算次级按钮（附风险说明） -->
      <div class="action-bar">
        <el-button type="primary" :loading="savingConfig" @click="saveConfig">保存设置</el-button>
        <el-button :loading="running" @click="runNow">手动结算上一周期</el-button>
        <span class="row-tip">重复触发不会重复发放，仅结算上一个完整周期</span>
      </div>
    </el-card>
  </div>
</template>

<script>
import { dividendInfoApi, dividendConfigApi, dividendRunApi } from '@/api/dividend';

export default {
  name: 'DividendSetting',
  data() {
    return {
      savingConfig: false,
      running: false,
      configForm: { status: false, cycle: 1, base: 1 },
    };
  },
  computed: {
    // 选中周期后动态展示执行时间
    cycleTip() {
      const map = { 1: '每天 00:01 自动执行', 2: '每周一 00:01 自动执行', 3: '每月 1 号 01:00 自动执行' };
      return (map[this.configForm.cycle] || map[1]) + '，分发刚结束一个周期的数据';
    },
  },
  mounted() {
    this.getInfo();
  },
  methods: {
    getInfo() {
      dividendInfoApi().then((res) => {
        this.configForm.status = !!res.status;
        this.configForm.cycle = res.cycle || 1;
        this.configForm.base = res.base || 1;
      });
    },
    saveConfig() {
      this.savingConfig = true;
      dividendConfigApi({ status: this.configForm.status, cycle: this.configForm.cycle, base: this.configForm.base }).then(() => {
        this.$message.success('保存成功');
      }).finally(() => { this.savingConfig = false; });
    },
    runNow() {
      this.$confirm('将立即结算上一个完整周期的分红（重复触发不会重复发放），是否继续？', '手动结算', { type: 'warning' }).then(() => {
        this.running = true;
        dividendRunApi(this.configForm.cycle).then((res) => {
          this.$message.success(res || '已触发');
        }).finally(() => { this.running = false; });
      }).catch(() => {});
    },
  },
};
</script>

<style scoped lang="scss">
.info-bar {
  background: #edf3ff;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  color: #1f5fd6;
  line-height: 20px;
  b {
    font-weight: 600;
  }
}
.setting-body {
  max-width: 900px;
}
.form-group {
  padding-top: 22px;
  .group-title {
    font-size: 13px;
    font-weight: 600;
    color: #1f5fd6;
    line-height: 18px;
    margin-bottom: 12px;
  }
  .field-tip {
    font-size: 12px;
    color: #909399;
    line-height: 18px;
    margin-top: 6px;
  }
}
.switch-row {
  display: flex;
  align-items: center;
  gap: 12px;
  .row-tip {
    font-size: 12px;
    color: #909399;
    line-height: 18px;
  }
}
.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  border-top: 1px solid #e5e6eb;
  margin-top: 24px;
  padding-top: 20px;
  .row-tip {
    font-size: 12px;
    color: #909399;
    line-height: 18px;
  }
}
</style>
