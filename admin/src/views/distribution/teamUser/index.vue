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
          <el-form-item>
            <el-button type="primary" @click="searchList">搜索</el-button>
            <el-button @click="resetList">重置</el-button>
            <el-button @click="openExportDialog">导出</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <export-date-dialog
      :visible.sync="exportDialogVisible"
      :loading="exportLoading"
      @confirm="onExportConfirm"
    />
    <el-card class="box-card mt14">
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="mini" highlight-current-row>
        <el-table-column prop="uid" label="UID" min-width="70" />
        <el-table-column label="用户信息" min-width="180">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-image v-if="scope.row.avatar" :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" class="avatar" />
              <div>
                <div>{{ scope.row.nickname || '-' }}</div>
                <div class="sub-text">{{ scope.row.phone || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="团队等级" min-width="140">
          <template slot-scope="scope">
            {{ scope.row.teamLevelName || matchLevelName(scope.row.teamLevelId) }}
            <span v-if="scope.row.grade">（Lv{{ scope.row.grade }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="selfPaidAmount" label="自购已支付(元)" min-width="120" />
        <el-table-column prop="selfCompleteAmount" label="自购已完成(元)" min-width="120" />
        <el-table-column prop="teamPaidAmount" label="团队已支付(元)" min-width="120" />
        <el-table-column prop="teamCompleteAmount" label="团队已完成(元)" min-width="120" />
        <el-table-column prop="updateTime" label="更新时间" min-width="160" />
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
import { teamLevelAllApi, teamLevelUserListApi } from '@/api/teamLevel';
import ExportDateDialog from '@/components/ExportDateDialog';
import { runListExport } from '@/utils/listExport';

export default {
  name: 'TeamLevelUser',
  components: { ExportDateDialog },
  data() {
    return {
      exportDialogVisible: false,
      exportLoading: false,
      listLoading: false,
      levelList: [],
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        teamLevelId: '',
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
    openExportDialog() {
      this.exportDialogVisible = true;
    },
    async onExportConfirm(dateLimit) {
      this.exportLoading = true;
      const ok = await runListExport({
        apiFn: teamLevelUserListApi,
        params: {
          keywords: this.tableFrom.keywords,
          teamLevelId: this.tableFrom.teamLevelId,
        },
        dateLimit,
        clientDateField: 'updateTime',
        filename: '团队关联用户导出',
        header: [
          'UID',
          '昵称',
          '手机号',
          '团队等级',
          '等级序号',
          '自购已支付(元)',
          '自购已完成(元)',
          '团队已支付(元)',
          '团队已完成(元)',
          '更新时间',
        ],
        filterVal: [
          'uid',
          'nickname',
          'phone',
          'teamLevelName',
          'grade',
          'selfPaidAmount',
          'selfCompleteAmount',
          'teamPaidAmount',
          'teamCompleteAmount',
          'updateTime',
        ],
        mapRow: (row) => ({
          uid: row.uid,
          nickname: row.nickname || '',
          phone: row.phone || '',
          teamLevelName: row.teamLevelName || this.matchLevelName(row.teamLevelId),
          grade: row.grade || '',
          selfPaidAmount: row.selfPaidAmount,
          selfCompleteAmount: row.selfCompleteAmount,
          teamPaidAmount: row.teamPaidAmount,
          teamCompleteAmount: row.teamCompleteAmount,
          updateTime: row.updateTime || '',
        }),
      });
      this.exportLoading = false;
      if (ok) this.exportDialogVisible = false;
    },
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
      };
      teamLevelUserListApi(params)
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
.user-cell {
  display: flex;
  align-items: center;
}
.avatar {
  width: 36px;
  height: 36px;
  margin-right: 8px;
}
.sub-text {
  color: #909399;
  font-size: 12px;
}
</style>
