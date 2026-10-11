<template>
  <div class="divBox">
    <el-card>
      <el-form inline size="small">
        <el-form-item label="订单号">
          <el-input v-model="orderId" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="type" clearable placeholder="全部">
            <el-option value="direct" label="直推" />
            <el-option value="see" label="见单" />
            <el-option value="peer" label="平级" />
            <el-option value="help" label="帮扶" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="exportExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户" width="90" />
        <el-table-column prop="sourceUid" label="来源用户" width="90" />
        <el-table-column prop="orderId" label="订单/业务号" min-width="160" />
        <el-table-column prop="rewardType" label="类型" width="90" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column prop="mark" label="备注" min-width="180" />
        <el-table-column prop="createTime" label="时间" width="160" />
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
    load() {
      this.loading = true;
      chainCommissionApi({
        page: this.page,
        limit: this.limit,
        orderId: this.orderId,
        type: this.type,
      })
        .then((res) => {
          const data = res.data || res || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    exportExcel() {
      chainCommissionExportApi({ orderId: this.orderId, type: this.type }).then((res) => {
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
