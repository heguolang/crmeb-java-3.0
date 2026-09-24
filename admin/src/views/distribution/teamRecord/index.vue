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
      <!-- 定稿版式 v2 复用（2026-09-25）：记录 ID 不单独设列，UID 色块并入用户信息列 -->
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="small" class="admin-table table-lg" stripe highlight-current-row>
        <el-table-column width="30" />
        <!-- 用户信息：内嵌圆头像 + 昵称 / ID 色块+复制 / 手机（标签灰、值黑） -->
        <el-table-column label="用户信息" min-width="254">
          <template slot-scope="scope">
            <div class="person-cell">
              <img v-if="scope.row.avatar" class="person-avatar" :src="scope.row.avatar" @error="scope.row.avatar = ''" />
              <span v-else class="person-avatar person-avatar--empty">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
              <div class="person-info">
                <div class="info-name">{{ scope.row.nickname || '-' }}</div>
                <div class="info-line">
                  ID：<span class="id-chip">{{ scope.row.uid }}</span>
                  <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.uid)"></i>
                </div>
                <div class="info-line" v-if="scope.row.phone">手机：<span class="info-v">{{ scope.row.phone }}</span></div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="团队等级" min-width="146">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.teamLevelName || matchLevelName(scope.row.teamLevelId) }}</span>
            <span v-if="scope.row.grade" class="kv-sub">Lv{{ scope.row.grade }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mark" label="变更备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="变更时间" width="170">
          <template slot-scope="scope"><span class="info-v">{{ scope.row.createTime }}</span></template>
        </el-table-column>
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
  },
};
</script>

<style scoped lang="scss">
/* 信息列/ID 色块样式全部走 theme/styles.scss 全局定义 */
</style>
