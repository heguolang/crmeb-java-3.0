<template>
  <div class="divBox">
    <el-card>
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户" width="90" />
        <el-table-column prop="riskType" label="类型" width="90" />
        <el-table-column prop="riskKey" label="标识" min-width="140" />
        <el-table-column prop="hitCount" label="次数" width="80" />
        <el-table-column prop="action" label="动作" width="80" />
        <el-table-column prop="detail" label="详情" min-width="200" />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
      <el-pagination style="margin-top:12px" layout="total, prev, pager, next" :total="total" :page-size="limit" :current-page.sync="page" @current-change="load" />
    </el-card>
  </div>
</template>
<script>
import { chainRiskLogsApi } from '@/api/chain';
export default {
  name: 'ChainRisk',
  data() { return { loading: false, list: [], total: 0, page: 1, limit: 20 }; },
  created() { this.load(); },
  methods: {
    load() {
      this.loading = true;
      chainRiskLogsApi({ page: this.page, limit: this.limit }).then(res => {
        const d = res.data || res || {};
        this.list = d.list || [];
        this.total = d.total || 0;
      }).finally(() => { this.loading = false; });
    },
  },
};
</script>
