<template>
  <div class="divBox">
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.agentCount || 0 }}</div><div class="l">代理数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.bossCount || 0 }}</div><div class="l">老板数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.todayNew || 0 }}</div><div class="l">今日新增身份</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.commissionTotal || 0 }}</div><div class="l">佣金发放总额</div></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.todayCommission || 0 }}</div><div class="l">今日佣金</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.clawbackCount || 0 }}</div><div class="l">待追回笔数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.riskToday || 0 }}</div><div class="l">今日风控命中</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="n">{{ data.dividendTotal || 0 }}</div><div class="l">分红发放笔数</div></el-card></el-col>
    </el-row>
    <el-card style="margin-top: 16px">
      <div slot="header">团队层级分布</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="深度1">{{ (data.teamDepthDist && data.teamDepthDist.depth1) || 0 }}</el-descriptions-item>
        <el-descriptions-item label="深度2">{{ (data.teamDepthDist && data.teamDepthDist.depth2) || 0 }}</el-descriptions-item>
        <el-descriptions-item label="深度3+">{{ (data.teamDepthDist && data.teamDepthDist.depth3Plus) || 0 }}</el-descriptions-item>
        <el-descriptions-item label="进行中帮扶">{{ data.helpDoing || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card style="margin-top: 16px">
      <div slot="header">近7日趋势</div>
      <el-table :data="data.last7Days || []" border size="small">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="commissionCount" label="佣金笔数" />
        <el-table-column prop="notifyCount" label="晋升通知数" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { chainDashboardApi } from '@/api/chain';
export default {
  name: 'ChainDashboard',
  data() {
    return { loading: false, data: {} };
  },
  created() {
    this.loading = true;
    chainDashboardApi()
      .then((res) => { this.data = res.data || res || {}; })
      .finally(() => { this.loading = false; });
  },
};
</script>

<style scoped>
.n { font-size: 28px; font-weight: 600; color: #303133; }
.l { margin-top: 8px; color: #909399; font-size: 13px; }
</style>
