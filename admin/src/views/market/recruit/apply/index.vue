<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar">
        <span>应聘管理：会员端投递的简历与联系方式汇总在这里，改状态 / 约面试 / 写评价都会记进跟进日志。</span>
      </div>

      <div class="stat-row mt16">
        <div class="stat-item">
          <div class="stat-num">{{ stats.s0 || 0 }}</div>
          <div class="stat-label">待联系</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.s1 || 0 }}</div>
          <div class="stat-label">已联系</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.s2 || 0 }}</div>
          <div class="stat-label">面试中</div>
        </div>
        <div class="stat-item is-ok">
          <div class="stat-num">{{ stats.s3 || 0 }}</div>
          <div class="stat-label">已录用</div>
        </div>
        <div class="stat-item is-off">
          <div class="stat-num">{{ stats.s4 || 0 }}</div>
          <div class="stat-label">不合适</div>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="姓名 / 电话 / 职位" clearable style="width: 220px" size="small" @keyup.enter.native="getList(1)" />
        <el-select v-model="tableFrom.jobId" placeholder="应聘职位" clearable filterable size="small" style="width: 180px" class="ml10">
          <el-option v-for="j in jobOptions" :key="j.id" :label="j.title" :value="j.id" />
        </el-select>
        <el-select v-model="tableFrom.status" placeholder="处理状态" clearable size="small" style="width: 130px" class="ml10">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="应聘人" min-width="150">
          <template slot-scope="scope">
            <div class="job-cell">
              <span class="job-title">{{ scope.row.name }}</span>
              <span class="c-gray ml6">{{ scope.row.gender || '' }} {{ scope.row.age ? scope.row.age + '岁' : '' }}</span>
            </div>
            <div class="job-sub">{{ scope.row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="应聘职位" min-width="160">
          <template slot-scope="scope">
            <div>{{ scope.row.jobTitle || '人才自荐' }}</div>
            <div class="c-gray">{{ scope.row.expectCity || '—' }} · {{ scope.row.expectSalary || '薪资面议' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="学历 / 年限" width="140" align="center">
          <template slot-scope="scope">{{ scope.row.education || '—' }} · {{ scope.row.experience || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面试时间" width="150" align="center">
          <template slot-scope="scope">{{ scope.row.interviewDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="处理人" prop="handler" width="100" align="center" />
        <el-table-column label="投递时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenDetail(scope.row)">处理</el-button>
              <el-button size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="150" />
      </el-table>

      <el-pagination
        class="mt16"
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        :page-size="tableFrom.limit"
        :current-page="tableFrom.page"
        @current-change="onPageChange"
      />
    </el-card>

    <el-drawer :visible.sync="detailShow" :with-header="true" title="应聘详情" size="560px" append-to-body>
      <div v-if="detail.id" class="detail-wrap">
        <div class="detail-head">
          <div>
            <div class="detail-name">{{ detail.name }} <span class="c-gray">· {{ detail.phone }}</span></div>
            <div class="detail-sub">
              {{ detail.jobTitle || '人才自荐' }}
              <el-tag size="mini" class="ml6" :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
            </div>
          </div>
          <div class="detail-no">{{ detail.applyNo }}</div>
        </div>

        <el-descriptions :column="2" size="small" border class="mt10">
          <el-descriptions-item label="性别">{{ detail.gender || '—' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ detail.age || '—' }}</el-descriptions-item>
          <el-descriptions-item label="最高学历">{{ detail.education || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">{{ detail.experience || '—' }}</el-descriptions-item>
          <el-descriptions-item label="毕业院校">{{ detail.school || '—' }}</el-descriptions-item>
          <el-descriptions-item label="意向城市">{{ detail.expectCity || '—' }}</el-descriptions-item>
          <el-descriptions-item label="期望薪资">{{ detail.expectSalary || '—' }}</el-descriptions-item>
          <el-descriptions-item label="投递时间">{{ fmtTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="个人留言" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
          <el-descriptions-item label="简历附件" :span="2">
            <a v-if="detail.resume" :href="detail.resume" target="_blank" class="link">{{ detail.resumeName || '查看简历' }}</a>
            <span v-else>未上传</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">跟进处理</el-divider>
        <el-form :model="handleForm" label-width="90px" size="small">
          <el-form-item label="处理状态">
            <el-radio-group v-model="handleForm.status">
              <el-radio-button v-for="s in statusOptions" :key="s.value" :label="s.value">{{ s.label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="面试时间">
            <el-input v-model="handleForm.interviewDate" maxlength="20" placeholder="如：2026-10-10 10:00" />
          </el-form-item>
          <el-form-item label="备注 / 评价">
            <el-input v-model="handleForm.adminRemark" type="textarea" :rows="3" maxlength="300" placeholder="沟通情况、面试评价…" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" :loading="saving" @click="onHandle">保存处理</el-button>
          </el-form-item>
        </el-form>

        <el-divider content-position="left">跟进日志</el-divider>
        <el-timeline v-if="logList.length">
          <el-timeline-item v-for="(l, i) in logList" :key="i" :timestamp="l.time" placement="top">
            <div class="log-line">
              <strong>{{ l.action }}</strong>
              <span v-if="l.operator" class="c-gray ml6">{{ l.operator }}</span>
            </div>
            <div class="log-content">{{ l.content }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="empty-log">暂无跟进记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  recruitApplyListApi,
  recruitApplyInfoApi,
  recruitApplyHandleApi,
  recruitApplyDeleteApi,
  recruitApplyStatsApi,
  recruitJobEnabledApi
} from '@/api/recruit';

const STATUS_OPTIONS = [
  { value: 0, label: '待联系' },
  { value: 1, label: '已联系' },
  { value: 2, label: '面试中' },
  { value: 3, label: '已录用' },
  { value: 4, label: '不合适' }
];

export default {
  name: 'RecruitApply',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '', jobId: '', status: '' },
      statusOptions: STATUS_OPTIONS,
      jobOptions: [],
      stats: {},
      detailShow: false,
      detail: {},
      handleForm: { id: null, status: 0, interviewDate: '', adminRemark: '', handler: '' },
      logList: []
    };
  },
  mounted() {
    this.getList();
    this.getStats();
    this.getJobs();
  },
  methods: {
    getList(page) {
      if (page) this.tableFrom.page = page;
      const params = { ...this.tableFrom };
      if (params.status === '') delete params.status;
      if (!params.jobId) delete params.jobId;
      this.loading = true;
      recruitApplyListApi(params)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = Number(res.total || 0);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getStats() {
      recruitApplyStatsApi().then((res) => {
        this.stats = res || {};
      });
    },
    getJobs() {
      recruitJobEnabledApi().then((res) => {
        this.jobOptions = res || [];
      });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    statusText(s) {
      const item = STATUS_OPTIONS.find((o) => o.value === s);
      return item ? item.label : '待联系';
    },
    statusType(s) {
      if (s === 3) return 'success';
      if (s === 4) return 'info';
      if (s === 2) return 'warning';
      if (s === 1) return 'primary';
      return 'danger';
    },
    parseLog(log) {
      if (!log) return [];
      try {
        return JSON.parse(log) || [];
      } catch (e) {
        return [];
      }
    },
    onOpenDetail(row) {
      recruitApplyInfoApi(row.id).then((res) => {
        this.detail = res || {};
        this.logList = this.parseLog(res.log);
        this.handleForm = {
          id: res.id,
          status: res.status == null ? 0 : res.status,
          interviewDate: res.interviewDate || '',
          adminRemark: res.adminRemark || '',
          handler: (this.$store.getters && this.$store.getters.name) || ''
        };
        this.detailShow = true;
      });
    },
    onHandle() {
      this.saving = true;
      recruitApplyHandleApi(this.handleForm)
        .then(() => {
          this.$message.success('已保存');
          this.detailShow = false;
          this.getList();
          this.getStats();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    onDelete(row) {
      this.$confirm(`确认删除「${row.name}」的应聘记录？删除后不可恢复。`, '提示', { type: 'warning' })
        .then(() => recruitApplyDeleteApi(row.id))
        .then(() => {
          this.$message.success('删除成功');
          this.getList();
          this.getStats();
        })
        .catch(() => {});
    }
  }
};
</script>

<style scoped lang="scss">
.filter-bar { margin: 14px 0; }
.ml6 { margin-left: 6px; }
.ml10 { margin-left: 10px; }
.c-gray { color: #909399; font-size: 12px; }
.job-cell { display: flex; align-items: center; }
.job-title { font-weight: 600; color: #303133; }
.job-sub { margin-top: 2px; color: #606266; font-size: 12px; }
.stat-row { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 12px; }
.stat-item {
  flex: 1; min-width: 110px; background: #f7f9fc; border: 1px solid #ebeef5;
  border-radius: 8px; padding: 12px 16px;
}
.stat-item.is-ok { background: #f2faf3; border-color: #cfe8d5; }
.stat-item.is-off { background: #fafafa; border-color: #e6e6e6; }
.stat-num { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-item.is-ok .stat-num { color: #2ba36b; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.detail-wrap { padding: 0 20px 30px; }
.detail-head { display: flex; justify-content: space-between; align-items: flex-start; }
.detail-name { font-size: 18px; font-weight: 700; color: #303133; }
.detail-sub { margin-top: 6px; color: #606266; font-size: 13px; }
.detail-no { color: #909399; font-size: 12px; }
.log-line { font-size: 13px; color: #303133; }
.log-content { font-size: 12px; color: #606266; margin-top: 2px; }
.empty-log { color: #909399; font-size: 12px; padding: 10px 0; }
.link { color: #1d6bf3; }
</style>
