<template>
  <div class="divBox">
    <el-card>
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="userId" label="用户" width="100" />
        <el-table-column prop="beforeVal" label="变更前" width="120" />
        <el-table-column prop="afterVal" label="变更后" width="120" />
        <el-table-column prop="reason" label="原因" />
        <el-table-column prop="createTime" label="时间" width="170" />
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
import { chainAuditLogsApi } from '@/api/chain';
export default {
  name: 'ChainAudit',
  data() {
    return { loading: false, list: [], total: 0, page: 1, limit: 20 };
  },
  created() { this.load(); },
  methods: {
    load() {
      this.loading = true;
      chainAuditLogsApi({ page: this.page, limit: this.limit })
        .then((res) => {
          const data = res.data || res || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .finally(() => { this.loading = false; });
    },
  },
};
</script>
