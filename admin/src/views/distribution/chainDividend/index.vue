<template>
  <div class="divBox">
    <el-card>
      <el-form inline size="small">
        <el-form-item label="账期">
          <el-input v-model="period" placeholder="yyyy-MM" clearable style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="warning" @click="run">手动执行分红</el-button>
        </el-form-item>
      </el-form>
      <el-alert title="需先在链动配置开启团队分红。定时任务每月1日自动跑上月；手动执行可指定账期，默认上月。" type="info" :closable="false" style="margin-bottom:12px" />
      <el-table :data="list" border v-loading="loading">
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
      <el-pagination style="margin-top:12px" layout="total, prev, pager, next" :total="total" :page-size="limit" :current-page.sync="page" @current-change="load" />
    </el-card>
  </div>
</template>
<script>
import { chainDividendApi, chainDividendRunApi } from '@/api/chain';
export default {
  name: 'ChainDividend',
  data() { return { loading: false, list: [], total: 0, page: 1, limit: 20, period: '' }; },
  created() { this.load(); },
  methods: {
    load() {
      this.loading = true;
      chainDividendApi({ page: this.page, limit: this.limit, period: this.period }).then(res => {
        const d = res.data || res || {};
        this.list = d.list || [];
        this.total = d.total || 0;
      }).finally(() => { this.loading = false; });
    },
    run() {
      this.$confirm('确认执行团队分红？同一账期已发放用户会跳过。', '提示').then(() => {
        chainDividendRunApi({ period: this.period || undefined }).then(res => {
          const d = res.data || res || {};
          this.$message.success('已发放 ' + (d.issued || 0) + ' 人');
          this.load();
        });
      }).catch(() => {});
    },
  },
};
</script>
