<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="代理商UID">
            <el-input v-model="tableFrom.uid" placeholder="会员UID" clearable style="width: 150px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="变更类型">
            <el-select v-model="tableFrom.type" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="(name, t) in typeNames" :key="t" :label="name" :value="Number(t)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="getList">查询</el-button>
            <el-button size="small" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        size="small"
        class="admin-table"
        stripe
        highlight-current-row
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="变更类型" width="110">
          <template slot-scope="scope">
            <el-tag :type="tagType(scope.row.type)" size="mini">{{ typeNames[scope.row.type] || scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="代理商" width="170">
          <template slot-scope="scope">
            <div class="cell-main">{{ scope.row.nickname || '-' }}</div>
            <div class="cell-sub">{{ scope.row.phone }}（UID:{{ scope.row.uid }}）</div>
          </template>
        </el-table-column>
        <el-table-column label="变更前" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.oldValue || '—' }}</template>
        </el-table-column>
        <el-table-column label="变更后" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.newValue || '—' }}</template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="变更时间" width="160" />
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          :total="total"
          @current-change="pageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { agentChangeLogListApi } from '@/api/daili';

export default {
  name: 'AgentChangeLog',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, uid: null, type: null },
      typeNames: { 1: '新增代理', 2: '级别/区域变更', 3: '状态变更', 4: '删除代理', 5: '比例变更' }
    };
  },
  methods: {
    getList() {
      this.loading = true;
      agentChangeLogListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, uid: null, type: null };
      this.getList();
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    tagType(t) {
      return { 1: 'success', 2: 'primary', 3: 'warning', 4: 'danger', 5: 'info' }[t] || 'info';
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.cell-main { font-size: 13px; font-weight: 600; color: #303133; }
.cell-sub { font-size: 12px; color: #909399; }
</style>
