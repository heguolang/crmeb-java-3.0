<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>职位管理：后台发布招聘职位，会员端「招聘」页面实时展示，用户可在线留资投递。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">发布职位</el-button>
        </div>
      </div>

      <div class="stat-row mt16">
        <div class="stat-item">
          <div class="stat-num">{{ stats.jobOn || 0 }}</div>
          <div class="stat-label">在招职位</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.jobOff || 0 }}</div>
          <div class="stat-label">已关闭</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.applyTotal || 0 }}</div>
          <div class="stat-label">累计投递</div>
        </div>
        <div class="stat-item is-warn">
          <div class="stat-num">{{ stats.applyWait || 0 }}</div>
          <div class="stat-label">待联系</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.applyMonth || 0 }}</div>
          <div class="stat-label">本月投递</div>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="职位名称 / 部门 / 城市" clearable style="width: 220px" size="small" @keyup.enter.native="getList(1)" />
        <el-select v-model="tableFrom.category" placeholder="职位类别" clearable size="small" style="width: 140px" class="ml10">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="tableFrom.status" placeholder="状态" clearable size="small" style="width: 120px" class="ml10">
          <el-option label="招聘中" :value="1" />
          <el-option label="已关闭" :value="0" />
        </el-select>
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="职位" min-width="200">
          <template slot-scope="scope">
            <div class="job-cell">
              <span class="job-title">{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.isHot === 1" size="mini" type="danger" class="ml6">热招</el-tag>
            </div>
            <div class="job-sub">
              <span v-if="scope.row.category" class="job-tag">{{ scope.row.category }}</span>
              <span>{{ scope.row.jobType || '全职' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="部门 / 城市" min-width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.dept || '—' }}</div>
            <div class="c-gray">{{ scope.row.city || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="薪资" width="120" align="center">
          <template slot-scope="scope">{{ scope.row.salary || '面议' }}</template>
        </el-table-column>
        <el-table-column label="经验 / 学历" width="150" align="center">
          <template slot-scope="scope">{{ scope.row.experience || '不限' }} · {{ scope.row.education || '不限' }}</template>
        </el-table-column>
        <el-table-column label="人数" prop="recruitNum" width="70" align="center" />
        <el-table-column label="浏览 / 投递" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.viewCount || 0 }} / {{ scope.row.applyCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '招聘中' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="更新时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenEdit(scope.row)">编辑</el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onToggle(scope.row)">
                {{ scope.row.status === 1 ? '关闭' : '开启' }}
              </el-button>
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

    <el-dialog :title="editForm.id ? '编辑职位' : '发布职位'" :visible.sync="editShow" width="760px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="90px" size="small">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="editForm.title" maxlength="32" placeholder="如：Java 开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位类别">
              <el-select v-model="editForm.category" allow-create filterable clearable placeholder="技术 / 产品 / 运营 / 销售 / 职能" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-input v-model="editForm.dept" maxlength="32" placeholder="如：技术部" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作城市">
              <el-input v-model="editForm.city" maxlength="20" placeholder="如：贵阳" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="工作地点">
              <el-input v-model="editForm.address" maxlength="64" placeholder="如：贵阳市观山湖区金融城 A 座 12 层" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="工作性质">
              <el-select v-model="editForm.jobType" style="width: 100%">
                <el-option label="全职" value="全职" />
                <el-option label="兼职" value="兼职" />
                <el-option label="实习" value="实习" />
                <el-option label="远程" value="远程" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="招聘人数">
              <el-input-number v-model="editForm.recruitNum" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="薪资范围">
              <el-input v-model="editForm.salary" maxlength="16" placeholder="8K-12K" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经验要求">
              <el-select v-model="editForm.experience" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="应届生" value="应届生" />
                <el-option label="1年以内" value="1年以内" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
                <el-option label="5-10年" value="5-10年" />
                <el-option label="10年以上" value="10年以上" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学历要求">
              <el-select v-model="editForm.education" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="高中" value="高中" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发布日期">
              <el-input v-model="editForm.onlineDate" maxlength="10" placeholder="2026-10-07" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="福利标签">
              <el-input v-model="editForm.tags" maxlength="120" placeholder="多个用逗号分隔，如：五险一金,双休,年终奖,定期团建" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="职位描述">
              <el-input v-model="editForm.description" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="岗位要做什么、汇报关系、团队情况…" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="任职要求">
              <el-input v-model="editForm.requirement" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="一行一条，会员端按行展示" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="editForm.hrName" maxlength="16" placeholder="人事部" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.hrPhone" maxlength="20" placeholder="手机号 / 座机" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="editForm.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="招聘中" inactive-text="已关闭" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="热门推荐">
              <el-switch v-model="editForm.isHot" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
              <span class="c-gray ml10">开启后会员端展示「热招」标识</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  recruitJobListApi,
  recruitJobSaveApi,
  recruitJobStatusApi,
  recruitJobDeleteApi,
  recruitJobStatsApi
} from '@/api/recruit';

const DEFAULT_CATEGORIES = ['技术', '产品', '设计', '运营', '销售', '市场', '职能'];

export default {
  name: 'RecruitJob',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '', category: '', status: '' },
      stats: {},
      editShow: false,
      editForm: this.emptyForm(),
      rules: {
        title: [{ required: true, message: '请填写职位名称', trigger: 'blur' }]
      }
    };
  },
  computed: {
    categories() {
      const set = new Set(DEFAULT_CATEGORIES);
      this.tableData.forEach((r) => {
        if (r.category) set.add(r.category);
      });
      return Array.from(set);
    }
  },
  mounted() {
    this.getList();
    this.getStats();
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        title: '',
        category: '',
        dept: '',
        city: '',
        address: '',
        jobType: '全职',
        salary: '',
        salaryMin: 0,
        salaryMax: 0,
        recruitNum: 1,
        experience: '不限',
        education: '不限',
        tags: '',
        description: '',
        requirement: '',
        hrName: '',
        hrPhone: '',
        onlineDate: '',
        status: 1,
        isHot: 0,
        sort: 0
      };
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      const params = { ...this.tableFrom };
      if (params.status === '') delete params.status;
      if (!params.category) delete params.category;
      this.loading = true;
      recruitJobListApi(params)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = Number(res.total || 0);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getStats() {
      recruitJobStatsApi().then((res) => {
        this.stats = res || {};
      });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    onOpenEdit(row) {
      this.editForm = row ? { ...this.emptyForm(), ...row } : this.emptyForm();
      this.editShow = true;
      this.$nextTick(() => {
        if (this.$refs.editForm) this.$refs.editForm.clearValidate();
      });
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        recruitJobSaveApi(this.editForm)
          .then(() => {
            this.$message.success('保存成功');
            this.editShow = false;
            this.getList();
            this.getStats();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onToggle(row) {
      const next = row.status === 1 ? 0 : 1;
      recruitJobStatusApi(row.id, next).then(() => {
        this.$message.success(next === 1 ? '已开启招聘' : '已关闭职位');
        this.getList();
        this.getStats();
      });
    },
    onDelete(row) {
      this.$confirm(`确认删除职位「${row.title}」？删除后会员端不再展示，历史投递记录不受影响。`, '提示', { type: 'warning' })
        .then(() => recruitJobDeleteApi(row.id))
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
.job-sub { margin-top: 2px; color: #909399; font-size: 12px; }
.job-tag {
  display: inline-block; margin-right: 6px; padding: 0 6px; border-radius: 8px;
  color: #1d6bf3; background: rgba(29, 107, 243, 0.1);
}
.stat-row {
  display: flex; flex-wrap: wrap; gap: 12px; margin-top: 12px;
}
.stat-item {
  flex: 1; min-width: 120px; background: #f7f9fc; border: 1px solid #ebeef5;
  border-radius: 8px; padding: 12px 16px;
}
.stat-item.is-warn { background: #fff7f2; border-color: #ffe0cc; }
.stat-num { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-item.is-warn .stat-num { color: #e8761a; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
