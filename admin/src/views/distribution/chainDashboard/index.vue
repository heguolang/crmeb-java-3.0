<template>
  <div class="divBox" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">{{ data.agentCount || 0 }}</div><div class="l">{{ lv.agent }}数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">{{ data.bossCount || 0 }}</div><div class="l">{{ lv.boss }}数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">{{ data.todayNew || 0 }}</div><div class="l">今日新增身份</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">¥{{ money(data.commissionTotal) }}</div><div class="l">佣金发放总额</div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">¥{{ money(data.todayCommission) }}</div><div class="l">今日佣金</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">{{ data.clawbackCount || 0 }}</div><div class="l">待追回笔数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n gold">¥{{ money(data.helpFundTotal) }}</div><div class="l">帮扶基金冻结中</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="n">{{ data.helpLockedBossCount || 0 }}</div><div class="l">未解锁{{ lv.boss }}数</div></el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <div slot="header">团队层级分布</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="深度 1">{{ (data.teamDepthDist && data.teamDepthDist.depth1) || 0 }}</el-descriptions-item>
        <el-descriptions-item label="深度 2">{{ (data.teamDepthDist && data.teamDepthDist.depth2) || 0 }}</el-descriptions-item>
        <el-descriptions-item label="深度 3+">{{ (data.teamDepthDist && data.teamDepthDist.depth3Plus) || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px">
      <div slot="header">近 7 日趋势</div>
      <el-table :data="data.last7Days || []" border size="small">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="commissionCount" label="佣金笔数" />
        <el-table-column prop="notifyCount" label="晋升通知数" />
        <template slot="empty"><div class="empty">暂无数据</div></template>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { chainDashboardApi, chainConfigGetApi } from '@/api/chain';

export default {
  name: 'ChainDashboard',
  data() {
    return {
      loading: false,
      data: {},
      lv: { agent: '代理', boss: '老板' },
    };
  },
  created() {
    this.load();
    this.loadLevelNames();
  },
  methods: {
    load() {
      this.loading = true;
      chainDashboardApi()
        .then((res) => {
          this.data = res.data || res || {};
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false;
        });
    },
    loadLevelNames() {
      chainConfigGetApi()
        .then((res) => {
          const cfg = res.data || res || {};
          this.lv = {
            agent: cfg.chain_level_agent_name || '代理',
            boss: cfg.chain_level_boss_name || '老板',
          };
        })
        .catch(() => {});
    },
    money(v) {
      const n = Number(v || 0);
      return n.toFixed(2);
    },
  },
};
</script>

<style scoped>
.n {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}
.n.gold {
  color: #c8961e;
}
.l {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}
.empty {
  padding: 20px 0;
  color: #909399;
}
</style>
