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

      <!-- 定稿版式 v2 复用（2026-09-25）：记录 ID 不单独设列，UID 色块并入代理商信息列 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        size="small"
        class="admin-table table-lg"
        stripe
        highlight-current-row
      >
        <el-table-column width="30" />
        <!-- 代理商信息：内嵌圆头像 + 昵称 / ID 色块+复制 / 手机（标签灰、值黑） -->
        <el-table-column label="代理商信息" min-width="236">
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
        <el-table-column label="变更类型" width="110">
          <template slot-scope="scope">
            <el-tag :type="tagType(scope.row.type)" size="mini">{{ typeNames[scope.row.type] || scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变更前" min-width="146" show-overflow-tooltip>
          <template slot-scope="scope"><span class="info-v">{{ scope.row.oldValue || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="变更后" min-width="146" show-overflow-tooltip>
          <template slot-scope="scope"><span class="info-v">{{ scope.row.newValue || '—' }}</span></template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="变更时间" width="170">
          <template slot-scope="scope"><span class="info-v">{{ scope.row.createTime }}</span></template>
        </el-table-column>
        <el-table-column width="150" />
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
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
/* 信息列/ID 色块样式全部走 theme/styles.scss 全局定义 */
</style>
