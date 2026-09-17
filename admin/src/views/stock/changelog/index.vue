<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item label="订货商">
          <el-input v-model="tableFrom.uid" placeholder="用户UID" clearable style="width: 140px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-select v-model="tableFrom.type" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(name, t) in typeNames" :key="t" :label="name" :value="Number(t)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="变更类型" width="110">
          <template slot-scope="scope">
            <el-tag :type="tagType(scope.row.type)" size="mini">{{ typeNames[scope.row.type] || scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订货商" min-width="140">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div class="grey">{{ scope.row.phone }}（UID:{{ scope.row.uid }}）</div>
          </template>
        </el-table-column>
        <el-table-column label="变更前" min-width="130">
          <template slot-scope="scope">{{ scope.row.oldValue || '—' }}</template>
        </el-table-column>
        <el-table-column label="变更后" min-width="130">
          <template slot-scope="scope">{{ scope.row.newValue || '—' }}</template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="变更时间" width="160" />
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { stockChangelogListApi } from '@/api/stock';

export default {
  name: 'StockChangeLog',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, uid: null, type: null },
      typeNames: { 1: '新增', 2: '层级变更', 3: '上级变更', 4: '状态变更', 5: '删除' }
    };
  },
  methods: {
    getList() {
      this.loading = true;
      stockChangelogListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    tagType(t) {
      return { 1: 'success', 2: 'primary', 3: 'primary', 4: 'warning', 5: 'danger' }[t] || 'info';
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.grey { color: #999; font-size: 12px; }
</style>
