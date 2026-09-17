<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item label="门店">
          <el-select v-model="tableFrom.storeId" placeholder="全部门店" clearable filterable style="width: 200px">
            <el-option v-for="s in storeOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="tableFrom.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="storeName" label="门店" min-width="130" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="核销订单" min-width="160" show-overflow-tooltip />
        <el-table-column prop="productInfo" label="核销产品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="verifyCode" label="核销码" width="110" />
        <el-table-column label="核销方式" width="95">
          <template slot-scope="scope">{{ scope.row.verifyType === 1 ? '核销码核销' : scope.row.verifyType }}</template>
        </el-table-column>
        <el-table-column label="服务费" width="85">
          <template slot-scope="scope">¥{{ scope.row.serviceFee }}</template>
        </el-table-column>
        <el-table-column label="订单金额" width="95">
          <template slot-scope="scope">¥{{ scope.row.payPrice }}</template>
        </el-table-column>
        <el-table-column label="核销人" width="110">
          <template slot-scope="scope">
            <span>{{ scope.row.verifyName || '-' }}</span>
            <el-tag size="mini" :type="scope.row.verifySource === 1 ? 'success' : 'info'" class="ml4">{{ scope.row.verifySource === 1 ? '门店端' : '后台' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="核销时间" width="150" />
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { merchantStoreVerifyListApi, merchantStoreListApi } from '@/api/merchantStore';

export default {
  name: 'MerchantStoreVerify',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      storeOptions: [],
      tableFrom: { page: 1, limit: 20, storeId: null, orderNo: '' }
    };
  },
  mounted() {
    this.getList();
    merchantStoreListApi({ page: 1, limit: 100 }).then(res => {
      this.storeOptions = (res && res.list) || [];
    });
  },
  methods: {
    getList() {
      this.loading = true;
      merchantStoreVerifyListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    }
  }
};
</script>

<style scoped>
.ml4 { margin-left: 4px; }
</style>
