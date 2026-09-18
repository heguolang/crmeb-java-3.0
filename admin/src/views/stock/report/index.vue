<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="代理商UID">
            <el-input v-model.number="tableFrom.uid" placeholder="按UID查询单个代理商" clearable style="width: 180px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              size="small"
              style="width: 250px"
              :picker-options="pickerOptions"
              @change="getList"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 汇总指标卡 -->
      <el-row :gutter="16">
        <el-col v-for="card in cards" :key="card.label" :span="6">
          <div class="stat-card">
            <div class="stat-top">
              <span class="stat-label">{{ card.label }}</span>
              <i class="stat-icon" :style="{ background: card.color }"></i>
            </div>
            <div class="stat-value"><i>¥</i>{{ card.value }}</div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header" class="tbl-header">
        <div class="tbl-header-main">
          <b>代理业绩 / 奖励汇总</b>
          <span class="tbl-header-tip">按代理商汇总所选时间范围内的订货业绩与已入账奖励</span>
        </div>
        <el-button size="small" icon="el-icon-download" @click="exportCsv">导出 CSV</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" size="small" class="admin-table" stripe highlight-current-row>
        <el-table-column prop="uid" label="UID" width="60" />
        <el-table-column label="代理商" min-width="130" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="agent-cell">
              <span class="avatar">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
              <span class="agent-name">{{ scope.row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="110" />
        <el-table-column label="层级" width="96">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.levelName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订货金额" min-width="100" align="right">
          <template slot-scope="scope">{{ fmtMoney(scope.row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="订货单数" width="80" align="center">
          <template slot-scope="scope">{{ scope.row.orderCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="个人业绩" min-width="100" align="right">
          <template slot-scope="scope">{{ fmtMoney(scope.row.selfPerformance) }}</template>
        </el-table-column>
        <el-table-column label="团队业绩" min-width="100" align="right">
          <template slot-scope="scope">{{ fmtMoney(scope.row.teamPerformance) }}</template>
        </el-table-column>
        <el-table-column label="奖励合计" min-width="100" align="right">
          <template slot-scope="scope"><b class="green">¥{{ fmtMoney(scope.row.rewardSum) }}</b></template>
        </el-table-column>
      </el-table>
      <div class="pager">
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
      tableFrom: { page: 1, limit: 20, uid: null },
      pickerOptions: {
        shortcuts: [
          { text: '近7天', onClick(picker) { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 6 * 86400000); picker.$emit('pick', [start, end]); } },
          { text: '近30天', onClick(picker) { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 29 * 86400000); picker.$emit('pick', [start, end]); } },
          { text: '本月', onClick(picker) { const end = new Date(); const start = new Date(end.getFullYear(), end.getMonth(), 1); picker.$emit('pick', [start, end]); } }
        ]
      }
    };
  },
  computed: {
    cards() {
      const s = this.summary || {};
      return [
        { label: '订货总金额', value: this.fmtMoney(s.totalAmount || 0), sub: '共 ' + (s.totalCount || 0) + ' 单', color: '#409eff' },
        { label: '已完成金额', value: this.fmtMoney(s.doneAmount || 0), sub: (s.doneCount || 0) + ' 单已完成', color: '#19be6b' },
        { label: '差价奖励', value: this.fmtMoney(s.diffSum || 0), sub: '已入账', color: '#ff9900' },
        { label: '阶梯+平级奖励', value: this.fmtMoney(Number(s.ladderSum || 0) + Number(s.peerSum || 0)), sub: '已入账', color: '#9041d9' }
      ];
    },
    dateLimit() {
      return this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] + ',' + this.dateRange[1] : '';
    }
  },
  methods: {
    fmtMoney(v) {
      const n = Number(v || 0);
      return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    getList() {
      this.loading = true;
      stockReportApi({ uid: this.tableFrom.uid, dateLimit: this.dateLimit, page: this.tableFrom.page, limit: this.tableFrom.limit }).then(res => {
        this.summary = res || {};
        this.rows = (res && res.agentRows) || [];
        this.total = (res && res.total) || this.rows.length;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, uid: null };
      this.dateRange = null;
      this.getList();
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    exportCsv() {
      const header = '代理商UID,代理,手机号,层级,订货金额,订货单数,个人业绩,团队业绩,奖励合计';
      const lines = this.rows.map(r => [r.uid, r.nickname, r.phone, r.levelName, r.orderAmount, r.orderCount, r.selfPerformance, r.teamPerformance, r.rewardSum].join(','));
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
/* 列表页通用规范（.toolbar/.admin-table/.pager）已统一在 theme/styles.scss 全局定义 */

/* 汇总指标卡：白底细边框，左对齐，右上角色点区分指标 */
.stat-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px 12px;
  background: #fff;
}
.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-label {
  font-size: 13px;
  color: #606266;
}
.stat-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.stat-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  font-family: DIN, 'Helvetica Neue', Arial, sans-serif;
  line-height: 26px;
}
.stat-value i {
  font-style: normal;
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  margin-right: 2px;
}
.stat-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

/* 表格卡头 */
.tbl-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tbl-header-main b {
  font-size: 14px;
  color: #303133;
}
.tbl-header-tip {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

/* 代理商列：头像字 + 名称 */
.agent-cell {
  display: flex;
  align-items: center;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  flex-shrink: 0;
}
.agent-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 层级彩签：不换行，保证 5 字级别名完整显示 */
.lv-chip {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}
.green {
  color: #19be6b;
}
</style>
