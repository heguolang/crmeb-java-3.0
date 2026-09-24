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
      <!-- 列结构照分销商管理定稿版式 v2 复用（2026-09-25）：
           头部留白 30 / 头像 60 / 代理商信息 min 208（昵称/ID色块可复制/手机）/
           层级 min 146 / 订货业绩 min 146（金额+单数 kv 台账）/ 个人业绩 146 /
           团队业绩 146 / 奖励合计 160 / 尾部留白 150。
           UID 列并入信息列（ID 色块范式），手机号并入信息列。 -->
      <el-table class="admin-table table-lg" v-loading="loading" :data="rows" size="small" stripe highlight-current-row>
        <!-- 头部留白列：定宽，min-width 会参与弹性分配稀释比例 -->
        <el-table-column width="30" />
        <!-- 头像：44px 头像 + 单元格内边距，60 是不裁切最小宽度；无头像兜底昵称首字 -->
        <el-table-column label="头像" width="60" align="center">
          <template slot-scope="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" class="avatar-img" @error="scope.row.avatar = ''" />
            <span v-else class="avatar-text">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
          </template>
        </el-table-column>
        <!-- 代理商信息：昵称 / ID 色块可复制 / 手机（标签灰、值黑，与分销商页同款分层） -->
        <el-table-column label="代理商信息" min-width="208">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname || '—' }}</div>
            <div class="info-line">
              ID：<span class="id-chip">{{ scope.row.uid }}</span>
              <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.uid)"></i>
            </div>
            <div class="info-line" v-if="scope.row.phone">手机：<span class="info-v">{{ scope.row.phone }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="层级" min-width="146">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.levelName || '—' }}</span>
          </template>
        </el-table-column>
        <!-- 订货业绩：kv 台账（金额主数值 + 订单数） -->
        <el-table-column label="订货业绩" min-width="146">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">订货金额</span><span class="kv-v kv-v--strong">{{ fmtMoney(scope.row.orderAmount) }}</span></div>
            <div class="kv"><span class="kv-k">订单数</span><span class="kv-v">{{ scope.row.orderCount || 0 }}<i class="kv-sub">单</i></span></div>
          </template>
        </el-table-column>
        <el-table-column label="个人业绩" min-width="146">
          <template slot-scope="scope"><span class="kv-v">{{ fmtMoney(scope.row.selfPerformance) }}</span></template>
        </el-table-column>
        <el-table-column label="团队业绩" min-width="146">
          <template slot-scope="scope"><span class="kv-v">{{ fmtMoney(scope.row.teamPerformance) }}</span></template>
        </el-table-column>
        <!-- 奖励合计：绿色加粗（收入语义），区别于版式主色蓝 -->
        <el-table-column label="奖励合计" min-width="160">
          <template slot-scope="scope"><span class="reward-strong">¥{{ fmtMoney(scope.row.rewardSum) }}</span></template>
        </el-table-column>
        <!-- 尾部留白列：定宽，别顶满 -->
        <el-table-column width="150" />
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
    // 复制文本到剪贴板：与用户列表页/分销商页同款实现（中文提示 + execCommand 兜底）
    copyText(text) {
      const value = String(text);
      const done = () => this.$message.success('已复制：' + value);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(done).catch(() => this.fallbackCopy(value, done));
      } else {
        this.fallbackCopy(value, done);
      }
    },
    fallbackCopy(value, done) {
      const input = document.createElement('textarea');
      input.value = value;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.top = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
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

/* 代理商列范式（avatar/info/kv/id-chip/lv-chip）已全局定义于 theme/styles.scss */

/* 奖励合计：绿色加粗（收入语义，区别于 kv-v--strong 版式主色蓝） */
.reward-strong {
  font-size: 14px;
  font-weight: 600;
  color: #19be6b;
  font-variant-numeric: tabular-nums;
}
</style>
