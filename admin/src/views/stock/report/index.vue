<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small">
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始" end-placeholder="结束" size="small" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-row :gutter="16">
        <el-col v-for="card in cards" :key="card.label" :span="6">
          <div class="stat-card">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">¥{{ card.value }}</div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center">
        <b>代理业绩/奖励汇总</b>
        <el-button size="mini" @click="exportCsv">导出 CSV</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" size="small" highlight-current-row>
        <el-table-column prop="nickname" label="代理" min-width="110" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="levelName" label="层级" width="90" />
        <el-table-column prop="orderAmount" label="订货金额" width="110" />
        <el-table-column prop="orderCount" label="订货单数" width="90" />
        <el-table-column prop="selfPerformance" label="个人业绩" width="110" />
        <el-table-column prop="teamPerformance" label="团队业绩" width="110" />
        <el-table-column prop="rewardSum" label="奖励合计" width="110">
          <template slot-scope="scope"><b class="green">¥{{ scope.row.rewardSum }}</b></template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { stockReportApi } from '@/api/stock';

export default {
  name: 'StockReport',
  data() {
    return {
      loading: false,
      dateRange: null,
      summary: {},
      rows: [],
      total: 0,
      tableFrom: { page: 1, limit: 20 }
    };
  },
  computed: {
    cards() {
      const s = this.summary || {};
      return [
        { label: '订货总金额', value: s.totalAmount || 0, sub: '共 ' + (s.totalCount || 0) + ' 单' },
        { label: '已完成金额', value: s.doneAmount || 0, sub: (s.doneCount || 0) + ' 单已完成' },
        { label: '差价奖励', value: s.diffSum || 0, sub: '已入账' },
        { label: '阶梯+平级奖励', value: Number(s.ladderSum || 0) + Number(s.peerSum || 0), sub: '已入账' }
      ];
    },
    dateLimit() {
      return this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] + ',' + this.dateRange[1] : '';
    }
  },
  methods: {
    getList() {
      this.loading = true;
      stockReportApi({ dateLimit: this.dateLimit, page: this.tableFrom.page, limit: this.tableFrom.limit }).then(res => {
        this.summary = res || {};
        this.rows = (res && res.agentRows) || [];
        this.total = (res && res.total) || this.rows.length;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    exportCsv() {
      const header = '代理,手机号,层级,订货金额,订货单数,个人业绩,团队业绩,奖励合计';
      const lines = this.rows.map(r => [r.nickname, r.phone, r.levelName, r.orderAmount, r.orderCount, r.selfPerformance, r.teamPerformance, r.rewardSum].join(','));
      const csv = '\ufeff' + header + '\n' + lines.join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = '订货报表.csv';
      link.click();
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.stat-card {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.stat-label { color: #909399; font-size: 13px; }
.stat-value { font-size: 22px; font-weight: 600; margin: 6px 0 2px; }
.stat-sub { color: #c0c4cc; font-size: 12px; }
.green { color: #67c23a; }
</style>
