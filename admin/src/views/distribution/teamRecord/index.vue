<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" :model="tableFrom" label-width="100px">
          <el-form-item label="关键字：">
            <el-input v-model="tableFrom.keywords" placeholder="请输入UID/昵称/手机号" clearable class="selWidth" />
          </el-form-item>
          <el-form-item label="团队等级：">
            <el-select v-model="tableFrom.teamLevelId" placeholder="请选择" clearable class="selWidth" filterable>
              <el-option v-for="item in levelList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态：">
            <el-select v-model="tableFrom.status" placeholder="请选择" clearable class="selWidth">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchList">搜索</el-button>
            <el-button @click="resetList">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="mini" highlight-current-row>
        <el-table-column prop="id" label="ID" min-width="70" />
        <el-table-column prop="uid" label="UID" min-width="70" />
        <el-table-column label="用户信息" min-width="180">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname || '-' }}</div>
            <div class="sub-text">{{ scope.row.phone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="团队等级" min-width="140">
          <template slot-scope="scope">
            {{ scope.row.teamLevelName || matchLevelName(scope.row.teamLevelId) }}
            <span v-if="scope.row.grade">（Lv{{ scope.row.grade }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mark" label="变更备注" min-width="280" show-overflow-tooltip />
        <el-table-column prop="createTime" label="变更时间" min-width="160" />
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { teamLevelAllApi, teamLevelRecordListApi } from '@/api/teamLevel';

export default {
  name: 'TeamLevelRecord',
  data() {
    return {
      listLoading: false,
      levelList: [],
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        teamLevelId: '',
        status: '',
      },
      tableData: {
        data: [],
        total: 0,
      },
    };
  },
  mounted() {
    this.getLevelList();
    this.getList();
  },
  methods: {
    normalizeListResult(res) {
      if (Array.isArray(res)) {
        return { data: res, total: res.length };
      }
      if (res && Array.isArray(res.list)) {
        return { data: res.list, total: Number(res.total || 0) };
      }
      if (res && Array.isArray(res.data)) {
        return { data: res.data, total: Number(res.total || res.data.length || 0) };
      }
      return { data: [], total: 0 };
    },
    getLevelList() {
      teamLevelAllApi().then((res) => {
        this.levelList = res || [];
      });
    },
    matchLevelName(teamLevelId) {
      const level = this.levelList.find((item) => item.id === teamLevelId);
      return level ? level.name : '-';
    },
    getList() {
      this.listLoading = true;
      const params = {
        page: this.tableFrom.page,
        limit: this.tableFrom.limit,
        keywords: this.tableFrom.keywords,
        teamLevelId: this.tableFrom.teamLevelId,
        status: this.tableFrom.status,
      };
      teamLevelRecordListApi(params)
        .then((res) => {
          const parsed = this.normalizeListResult(res);
          this.tableData.data = parsed.data;
          this.tableData.total = parsed.total;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    searchList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    resetList() {
      this.tableFrom = {
        page: 1,
        limit: 20,
        keywords: '',
        teamLevelId: '',
        status: '',
      };
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.getList();
    },
    pageChange(val) {
      this.tableFrom.page = val;
      this.getList();
    },
  },
};
</script>

<style scoped lang="scss">
.sub-text {
  color: #909399;
  font-size: 12px;
}
</style>
