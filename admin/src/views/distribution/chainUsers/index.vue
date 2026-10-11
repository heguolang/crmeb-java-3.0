<template>
  <div class="divBox">
    <el-card>
      <el-form inline size="small">
        <el-form-item label="关键词">
          <el-input v-model="keywords" clearable placeholder="昵称/手机/UID" />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="identity" clearable placeholder="全部">
            <el-option :value="1" label="代理" />
            <el-option :value="2" label="老板" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
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
        <el-table-column label="操作" width="160">
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
        @current-change="load"
      />
    </el-card>

    <el-dialog title="调整安置上级" :visible.sync="dialog" width="420px">
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
import { chainUsersApi, chainRelationUpdateApi } from '@/api/chain';

export default {
  name: 'ChainUsers',
  data() {
    return {
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
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
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
        this.load();
      });
    },
  },
};
</script>
