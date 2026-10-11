<template>
  <div class="divBox">
    <el-card>
      <el-form inline size="small">
        <el-form-item label="关键词">
          <el-input v-model="keywords" clearable placeholder="昵称/手机/UID" style="width: 180px" @keyup.enter.native="load" />
        </el-form-item>
        <el-form-item label="用户身份">
          <el-select v-model="identity" clearable placeholder="全部" style="width: 130px">
            <el-option :value="1" :label="lv.agent" />
            <el-option :value="2" :label="lv.boss" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐人UID">
          <el-input v-model="inviterUid" clearable placeholder="精确UID" style="width: 140px" @keyup.enter.native="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" border v-loading="loading">
        <el-table-column label="用户信息" min-width="190">
          <template slot-scope="scope">
            <div class="user-cell">
              <el-avatar :size="32" :src="scope.row.avatar" icon="el-icon-user-solid" />
              <div class="user-txt">
                <div class="name">{{ scope.row.nickname }}</div>
                <div class="sub">ID: {{ scope.row.uid }}</div>
                <div v-if="scope.row.phone" class="sub">手机: {{ scope.row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="当前等级" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.identity === 2" type="warning" size="mini">{{ scope.row.identityName }}</el-tag>
            <el-tag v-else-if="scope.row.identity === 1" type="success" size="mini">{{ scope.row.identityName }}</el-tag>
            <span v-else class="sub">{{ scope.row.identityName || '普通用户' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="升级时间" width="170">
          <template slot-scope="scope">
            <div class="sub">成为{{ lv.agent }}：{{ fmtTime(scope.row.agentTime) }}</div>
            <div v-if="scope.row.bossTime" class="sub">成为{{ lv.boss }}：{{ fmtTime(scope.row.bossTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="邀请人信息" min-width="150">
          <template slot-scope="scope">
            <template v-if="scope.row.chainInviteUid">
              <div class="name">{{ scope.row.inviteNickname || ('UID ' + scope.row.chainInviteUid) }}</div>
              <div class="sub">ID: {{ scope.row.chainInviteUid }}</div>
              <div v-if="scope.row.invitePhone" class="sub">手机: {{ scope.row.invitePhone }}</div>
            </template>
            <span v-else class="sub">—</span>
          </template>
        </el-table-column>
        <el-table-column label="上级信息" min-width="150">
          <template slot-scope="scope">
            <template v-if="scope.row.chainParentId">
              <div class="name">{{ scope.row.parentNickname || ('UID ' + scope.row.chainParentId) }}</div>
              <div class="sub">ID: {{ scope.row.chainParentId }}</div>
              <div v-if="scope.row.parentIdentityName" class="sub">{{ scope.row.parentIdentityName }}</div>
            </template>
            <span v-else class="sub">脱离（走人）</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDirectCount" label="有效直推" width="90" align="center" />
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openEdit(scope.row)">改上级</el-button>
          </template>
        </el-table-column>
        <template slot="empty"><div class="empty">暂无链动用户</div></template>
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

    <el-dialog title="调整安置上级" :visible.sync="dialog" width="440px">
      <el-form label-width="110px" size="small">
        <el-form-item label="用户">{{ current.uid }} / {{ current.nickname }}</el-form-item>
        <el-form-item label="原上级">
          <span class="sub">{{ current.chainParentId || '无' }}</span>
        </el-form-item>
        <el-form-item label="新上级UID">
          <el-input-number v-model="parentId" :min="0" />
          <span class="hint">填 0 表示脱离上级（走人）</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="reason" placeholder="如：人工纠错" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { chainUsersApi, chainRelationUpdateApi, chainConfigGetApi } from '@/api/chain';

export default {
  name: 'ChainUsers',
  data() {
    return {
      loading: false,
      saving: false,
      list: [],
      total: 0,
      page: 1,
      limit: 20,
      keywords: '',
      identity: undefined,
      inviterUid: '',
      dialog: false,
      current: {},
      parentId: 0,
      reason: '',
      lv: { agent: '代理', boss: '老板' },
    };
  },
  created() {
    this.load();
    this.loadLevelNames();
  },
  methods: {
    loadLevelNames() {
      chainConfigGetApi()
        .then((res) => {
          const cfg = res.data || res || {};
          this.lv = {
            agent: cfg.chain_level_agent_name || '代理',
            boss: cfg.chain_level_boss_name || '老板',
          };
        })
        .catch(() => {});
    },
    search() {
      this.page = 1;
      this.load();
    },
    reset() {
      this.keywords = '';
      this.identity = undefined;
      this.inviterUid = '';
      this.search();
    },
    load() {
      this.loading = true;
      const inviter = parseInt(this.inviterUid, 10);
      chainUsersApi({
        page: this.page,
        limit: this.limit,
        keywords: this.keywords || undefined,
        identity: this.identity,
        inviterUid: !isNaN(inviter) && inviter > 0 ? inviter : undefined,
      })
        .then((res) => {
          const data = res.data || res || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .catch(() => {
          this.list = [];
          this.total = 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    fmtTime(t) {
      if (!t) return '—';
      return String(t).replace('T', ' ').slice(0, 19);
    },
    openEdit(row) {
      this.current = row;
      this.parentId = row.chainParentId || 0;
      this.reason = '';
      this.dialog = true;
    },
    submit() {
      if (!this.reason) {
        this.$message.warning('请填写调整原因');
        return;
      }
      this.saving = true;
      chainRelationUpdateApi({
        userId: this.current.uid,
        parentId: this.parentId,
        reason: this.reason,
      })
        .then(() => {
          this.$message.success('已调整');
          this.dialog = false;
          this.load();
        })
        .catch(() => {})
        .finally(() => {
          this.saving = false;
        });
    },
  },
};
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
}
.user-txt {
  margin-left: 8px;
  line-height: 1.5;
}
.name {
  color: #303133;
  font-size: 13px;
}
.sub {
  color: #999;
  font-size: 12px;
}
.hint {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
.empty {
  padding: 24px 0;
  color: #909399;
}
</style>
