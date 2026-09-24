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
      <!-- 列结构照分销商管理定稿版式 v2 复用（2026-09-25）：
           头部留白 30 / 头像 60 / 用户信息 min 208（昵称/ID色块可复制/手机）/
           团队等级 min 146（chip+Lv）/ 自购·团队·直推 3 个 kv 台账列 min 146 /
           更新时间 180（定宽）/ 尾部留白 150。10 列 → 8 列，6 个金额列按
           「列内合并」收成 3 个台账列（已完成=主数值加粗主色，已支付为辅）。 -->
      <el-table class="admin-table table-lg" v-loading="listLoading" :data="tableData.data" size="small" stripe highlight-current-row>
        <!-- 头部留白列：定宽，min-width 会参与弹性分配稀释比例 -->
        <el-table-column width="30" />
        <!-- 头像：44px 头像 + 单元格内边距，60 是不裁切最小宽度；无头像兜底昵称首字 -->
        <el-table-column label="头像" width="60" align="center">
          <template slot-scope="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" class="avatar-img" />
            <span v-else class="avatar-text">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
          </template>
        </el-table-column>
        <!-- 用户信息：昵称 / ID 色块可复制 / 手机（标签灰、值黑，与分销商页同款分层） -->
        <el-table-column label="用户信息" min-width="208">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname || '—' }}</div>
            <div class="info-line">
              ID：<span class="id-chip">{{ scope.row.uid }}</span>
              <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.uid)"></i>
            </div>
            <div class="info-line" v-if="scope.row.phone">手机：<span class="info-v">{{ scope.row.phone }}</span></div>
          </template>
        </el-table-column>
        <!-- 团队等级：chip + Lv 序号后缀（kv-sub 灰色小字） -->
        <el-table-column label="团队等级" min-width="146">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.teamLevelName || matchLevelName(scope.row.teamLevelId) }}</span>
            <i v-if="scope.row.grade" class="kv-sub">Lv.{{ scope.row.grade }}</i>
          </template>
        </el-table-column>
        <!-- 自购业绩：kv 台账（已完成=主数值） -->
        <el-table-column label="自购业绩" min-width="146">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">已支付</span><span class="kv-v">{{ scope.row.selfPaidAmount || 0 }}</span></div>
            <div class="kv"><span class="kv-k">已完成</span><span class="kv-v kv-v--strong">{{ scope.row.selfCompleteAmount || 0 }}</span></div>
          </template>
        </el-table-column>
        <!-- 团队业绩：kv 台账 -->
        <el-table-column label="团队业绩" min-width="146">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">已支付</span><span class="kv-v">{{ scope.row.teamPaidAmount || 0 }}</span></div>
            <div class="kv"><span class="kv-k">已完成</span><span class="kv-v kv-v--strong">{{ scope.row.teamCompleteAmount || 0 }}</span></div>
          </template>
        </el-table-column>
        <!-- 直推业绩：kv 台账 -->
        <el-table-column label="直推业绩" min-width="146">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">已支付</span><span class="kv-v">{{ scope.row.directPaidAmount || 0 }}</span></div>
            <div class="kv"><span class="kv-k">已完成</span><span class="kv-v kv-v--strong">{{ scope.row.directCompleteAmount || 0 }}</span></div>
          </template>
        </el-table-column>
        <!-- 更新时间：定宽 180（min-width 会被弹性拉到 300+，见技能坑 6） -->
        <el-table-column label="更新时间" width="180" class-name="time-cell">
          <template slot-scope="scope">
            <div class="info-line"><span class="info-v">{{ scope.row.updateTime || '—' }}</span></div>
          </template>
        </el-table-column>
        <!-- 尾部留白列：定宽，别顶满 -->
        <el-table-column width="150" />
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
    // 复制文本到剪贴板：与用户列表页/分销商页同款实现（中文提示 + execCommand 兜底）
    copyText(text) {
      const value = String(text);
      const done = () => this.$message.success('已复制：' + value);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(done).catch(() => this.fallbackCopy(value, done));
      } else {
        this.fallbackCopy(value, done);
      }
    },
    fallbackCopy(value, done) {
      const input = document.createElement('textarea');
      input.value = value;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.top = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
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
          '直推已支付(元)',
          '直推已完成(元)',
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
          'directPaidAmount',
          'directCompleteAmount',
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
          directPaidAmount: row.directPaidAmount,
          directCompleteAmount: row.directCompleteAmount,
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
/* 列表范式（summary-bar/table-lg/avatar/info/kv/id-chip 等）已全局定义于 theme/styles.scss，
   本页不再写 scoped 副本。 */

/* 时间列：右内边距收紧，180px 定宽刚好容纳完整时间戳 */
::v-deep .table-lg td.time-cell .cell {
  padding-right: 12px !important;
}
</style>
