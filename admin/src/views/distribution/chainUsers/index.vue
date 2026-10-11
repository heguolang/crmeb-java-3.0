<template>
  <div class="divBox">
    <el-card>
      <el-tabs v-model="tab" @tab-click="onTab">
        <el-tab-pane label="用户列表" name="users" />
        <el-tab-pane label="关系变更记录" name="audit" />
      </el-tabs>

      <template v-if="tab === 'users'">
        <el-form inline size="small">
          <el-form-item label="关键词">
            <el-input v-model="keywords" clearable placeholder="昵称/手机/UID" @keyup.enter.native="loadUsers" />
          </el-form-item>
          <el-form-item label="身份">
            <el-select v-model="identity" clearable placeholder="全部">
              <el-option :value="1" label="代理" />
              <el-option :value="2" label="老板" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadUsers">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="list" border v-loading="loading">
          <el-table-column prop="uid" label="UID" width="80" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="phone" label="手机" width="120" />
          <el-table-column prop="identityName" label="身份" width="80" />
          <el-table-column prop="chainParentId" label="安置上级" width="100" />
          <el-table-column prop="chainInviteUid" label="邀请人" width="100" />
          <el-table-column prop="effectiveDirectCount" label="有效直推" width="90" />
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="openEdit(scope.row)">改上级</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="total"
          :page-size="limit"
          :current-page.sync="page"
          @current-change="loadUsers"
        />
      </template>

      <template v-else>
        <el-table :data="auditList" border v-loading="auditLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="userId" label="用户" width="100" />
          <el-table-column prop="beforeVal" label="变更前" width="120" />
          <el-table-column prop="afterVal" label="变更后" width="120" />
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>
        <el-pagination
          style="margin-top: 12px"
          layout="total, prev, pager, next"
          :total="auditTotal"
          :page-size="auditLimit"
          :current-page.sync="auditPage"
          @current-change="loadAudit"
        />
      </template>
    </el-card>

    <el-dialog title="调整安置上级" :visible.sync="dialog" width="420px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="用户">{{ current.uid }} / {{ current.nickname }}</el-form-item>
        <el-form-item label="新上级UID">
          <el-input-number v-model="parentId" :min="0" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="reason" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { chainUsersApi, chainRelationUpdateApi, chainAuditLogsApi } from '@/api/chain';

export default {
  name: 'ChainUsers',
  data() {
    return {
      tab: 'users',
      loading: false,
      list: [],
      total: 0,
      page: 1,
      limit: 20,
      keywords: '',
      identity: undefined,
      dialog: false,
      current: {},
      parentId: 0,
      reason: '',
      auditLoading: false,
      auditList: [],
      auditTotal: 0,
      auditPage: 1,
      auditLimit: 20,
    };
  },
  created() {
    this.loadUsers();
  },
  methods: {
    onTab() {
      if (this.tab === 'audit' && !this.auditList.length) {
        this.loadAudit();
      }
    },
    loadUsers() {
      this.loading = true;
      chainUsersApi({
        page: this.page,
        limit: this.limit,
        keywords: this.keywords,
        identity: this.identity,
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
    loadAudit() {
      this.auditLoading = true;
      chainAuditLogsApi({ page: this.auditPage, limit: this.auditLimit })
        .then((res) => {
          const data = res.data || res || {};
          this.auditList = data.list || [];
          this.auditTotal = data.total || 0;
        })
        .finally(() => {
          this.auditLoading = false;
        });
    },
    openEdit(row) {
      this.current = row;
      this.parentId = row.chainParentId || 0;
      this.reason = '';
      this.dialog = true;
    },
    submit() {
      chainRelationUpdateApi({
        userId: this.current.uid,
        parentId: this.parentId,
        reason: this.reason,
      }).then(() => {
        this.$message.success('已调整');
        this.dialog = false;
        this.loadUsers();
      });
    },
  },
};
</script>
