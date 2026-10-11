<template>
  <div class="divBox">
    <el-card>
      <el-tabs v-model="tab" @tab-click="onTab">
        <el-tab-pane label="数据概览" name="overview" />
        <el-tab-pane label="风控记录" name="risk" />
        <el-tab-pane label="团队分红" name="dividend" />
      </el-tabs>

      <div v-show="tab === 'overview'" v-loading="loading">
        <el-row :gutter="16">
          <el-col :span="6"><div class="stat"><div class="n">{{ data.agentCount || 0 }}</div><div class="l">代理数</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.bossCount || 0 }}</div><div class="l">老板数</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.todayNew || 0 }}</div><div class="l">今日新增身份</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.commissionTotal || 0 }}</div><div class="l">佣金发放总额</div></div></el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top:16px">
          <el-col :span="6"><div class="stat"><div class="n">{{ data.todayCommission || 0 }}</div><div class="l">今日佣金</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.clawbackCount || 0 }}</div><div class="l">待追回笔数</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.riskToday || 0 }}</div><div class="l">今日风控命中</div></div></el-col>
          <el-col :span="6"><div class="stat"><div class="n">{{ data.dividendTotal || 0 }}</div><div class="l">分红发放笔数</div></div></el-col>
        </el-row>
        <el-divider content-position="left">团队层级</el-divider>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="深度1">{{ (data.teamDepthDist && data.teamDepthDist.depth1) || 0 }}</el-descriptions-item>
          <el-descriptions-item label="深度2">{{ (data.teamDepthDist && data.teamDepthDist.depth2) || 0 }}</el-descriptions-item>
          <el-descriptions-item label="深度3+">{{ (data.teamDepthDist && data.teamDepthDist.depth3Plus) || 0 }}</el-descriptions-item>
          <el-descriptions-item label="进行中帮扶">{{ data.helpDoing || 0 }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">近7日</el-divider>
        <el-table :data="data.last7Days || []" border size="small">
          <el-table-column prop="date" label="日期" />
          <el-table-column prop="commissionCount" label="佣金笔数" />
          <el-table-column prop="notifyCount" label="晋升通知数" />
        </el-table>
        <div class="hint">日常佣金流水请到「财务 → 资金监控 / 佣金记录」查看。</div>
      </div>

      <div v-show="tab === 'risk'">
        <el-table :data="riskList" border v-loading="riskLoading" size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="用户" width="90" />
          <el-table-column prop="riskType" label="类型" width="90" />
          <el-table-column prop="riskKey" label="标识" min-width="140" />
          <el-table-column prop="hitCount" label="次数" width="80" />
          <el-table-column prop="action" label="动作" width="80" />
          <el-table-column prop="detail" label="详情" min-width="200" />
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="riskTotal"
          :page-size="riskLimit"
          :current-page.sync="riskPage"
          @current-change="loadRisk"
        />
      </div>

      <div v-show="tab === 'dividend'">
        <el-form inline size="small">
          <el-form-item label="账期">
            <el-input v-model="period" placeholder="yyyy-MM" clearable style="width:140px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadDividend">查询</el-button>
            <el-button type="warning" @click="runDividend">手动执行分红</el-button>
          </el-form-item>
        </el-form>
        <el-alert
          title="需先在「链动配置」开启团队分红。定时任务每月1日自动跑上月；手动执行可指定账期。"
          type="info"
          :closable="false"
          style="margin-bottom:12px"
        />
        <el-table :data="dividendList" border v-loading="dividendLoading" size="small">
          <el-table-column prop="period" label="账期" width="100" />
          <el-table-column prop="userId" label="老板UID" width="100" />
          <el-table-column prop="teamSize" label="团队人数" width="90" />
          <el-table-column prop="teamSales" label="团队销售额" width="120" />
          <el-table-column prop="ratio" label="比例%" width="80" />
          <el-table-column prop="amount" label="分红金额" width="120" />
          <el-table-column prop="status" label="状态" width="80" />
          <el-table-column prop="mark" label="备注" />
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="dividendTotal"
          :page-size="dividendLimit"
          :current-page.sync="dividendPage"
          @current-change="loadDividend"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { chainDashboardApi, chainRiskLogsApi, chainDividendApi, chainDividendRunApi } from '@/api/chain';

export default {
  name: 'ChainDashboard',
  data() {
    return {
      tab: 'overview',
      loading: false,
      data: {},
      riskLoading: false,
      riskList: [],
      riskTotal: 0,
      riskPage: 1,
      riskLimit: 20,
      dividendLoading: false,
      dividendList: [],
      dividendTotal: 0,
      dividendPage: 1,
      dividendLimit: 20,
      period: '',
    };
  },
  created() {
    this.loadOverview();
  },
  methods: {
    onTab() {
      if (this.tab === 'risk' && !this.riskList.length) this.loadRisk();
      if (this.tab === 'dividend' && !this.dividendList.length) this.loadDividend();
    },
    loadOverview() {
      this.loading = true;
      chainDashboardApi()
        .then((res) => {
          this.data = res.data || res || {};
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadRisk() {
      this.riskLoading = true;
      chainRiskLogsApi({ page: this.riskPage, limit: this.riskLimit })
        .then((res) => {
          const d = res.data || res || {};
          this.riskList = d.list || [];
          this.riskTotal = d.total || 0;
        })
        .finally(() => {
          this.riskLoading = false;
        });
    },
    loadDividend() {
      this.dividendLoading = true;
      chainDividendApi({ page: this.dividendPage, limit: this.dividendLimit, period: this.period })
        .then((res) => {
          const d = res.data || res || {};
          this.dividendList = d.list || [];
          this.dividendTotal = d.total || 0;
        })
        .finally(() => {
          this.dividendLoading = false;
        });
    },
    runDividend() {
      this.$confirm('确认执行团队分红？同一账期已发放用户会跳过。', '提示')
        .then(() => chainDividendRunApi({ period: this.period || undefined }))
        .then((res) => {
          const d = res.data || res || {};
          this.$message.success('已发放 ' + (d.issued || 0) + ' 人');
          this.loadDividend();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
.stat {
  background: #f8f9fb;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.n {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}
.l {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
.hint {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
