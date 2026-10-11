<template>
  <div class="divBox">
    <el-card>
      <el-form inline size="small">
        <el-form-item label="订单号">
          <el-input v-model="orderId" clearable placeholder="订单号/业务号" @keyup.enter.native="load" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="type" clearable placeholder="全部">
            <el-option value="direct" label="直推奖" />
            <el-option value="see" label="见点奖" />
            <el-option value="peer" label="平级奖" />
            <el-option value="help" label="帮扶基金冻结" />
            <el-option value="dividend" label="团队分红" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="exportExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户UID" width="90" />
        <el-table-column prop="sourceUid" label="来源UID" width="90" />
        <el-table-column prop="orderId" label="订单/业务号" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="120" align="center">
          <template slot-scope="scope">
            <span>{{ typeLabel(scope.row.rewardType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template slot-scope="scope">
            <span :class="{ gold: Number(scope.row.status) === 5 }">¥{{ money(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)" size="mini">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="165" />
        <template slot="empty"><div class="empty">暂无佣金记录</div></template>
      </el-table>
      <el-pagination
        style="margin-top: 12px"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="limit"
        :current-page.sync="page"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script>
import { chainCommissionApi, chainCommissionExportApi } from '@/api/chain';

const TYPE_MAP = {
  direct: '直推奖',
  see: '见点奖',
  peer: '平级奖',
  see_help: '帮扶基金(见点)',
  peer_help: '帮扶基金(平级)',
  help: '帮扶基金解冻',
  dividend: '团队分红',
};

const STATUS_MAP = {
  1: { label: '待结算', tag: 'info' },
  2: { label: '冻结中', tag: 'warning' },
  3: { label: '已结算', tag: 'success' },
  4: { label: '已失效', tag: 'info' },
  5: { label: '帮扶池冻结', tag: 'warning' },
  6: { label: '已追回', tag: 'danger' },
};

export default {
  name: 'ChainCommission',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      page: 1,
      limit: 20,
      orderId: '',
      type: '',
    };
  },
  created() {
    this.load();
  },
  methods: {
    typeLabel(t) {
      return TYPE_MAP[t] || t || '—';
    },
    statusLabel(s) {
      const it = STATUS_MAP[Number(s)];
      return it ? it.label : s;
    },
    statusTag(s) {
      const it = STATUS_MAP[Number(s)];
      return it ? it.tag : 'info';
    },
    money(v) {
      return Number(v || 0).toFixed(2);
    },
    load() {
      this.loading = true;
      chainCommissionApi({
        page: this.page,
        limit: this.limit,
        orderId: this.orderId || undefined,
        type: this.type || undefined,
      })
        .then((res) => {
          const data = res.data || res || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .catch(() => {
          this.list = [];
          this.total = 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    exportExcel() {
      chainCommissionExportApi({ orderId: this.orderId || undefined, type: this.type || undefined }).then((res) => {
        const payload = res.data || res || {};
        const fileName = payload.fileName || '';
        if (!fileName) {
          this.$message.error('导出失败');
          return;
        }
        window.open(fileName);
        this.$message.success('导出成功');
      });
    },
  },
};
</script>

<style scoped>
.gold {
  color: #c8961e;
  font-weight: 600;
}
.empty {
  padding: 24px 0;
  color: #909399;
}
</style>
