<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>项目进度管理：后台创建项目并设置进度，会员在 H5「我的项目/工单进度」中只读查看。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">创建项目</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="项目名称/项目经理" clearable style="width: 200px" size="small" @keyup.enter.native="getList(1)" />
        <el-input v-model.number="tableFrom.uid" placeholder="会员UID" clearable style="width: 120px" size="small" class="ml10" />
        <el-select v-model="tableFrom.status" placeholder="状态" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="需求确认" :value="1" />
          <el-option label="开发中" :value="2" />
          <el-option label="待验收" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已终止" :value="-1" />
        </el-select>
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="项目名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" prop="type" width="90" align="center" />
        <el-table-column label="会员" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.nickname || '—' }}</span>
            <span class="c-gray">（ID {{ scope.row.uid }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ scope.row.statusText || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160">
          <template slot-scope="scope">
            <el-progress :percentage="Number(scope.row.percent) || 0" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="当前阶段" prop="stage" min-width="90" />
        <el-table-column label="项目经理" prop="manager" width="90" align="center" />
        <el-table-column label="更新时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenProgress(scope.row)">设置进度</el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onOpenEdit(scope.row)">修改</el-button>
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

    <!-- 新建/编辑项目 -->
    <el-dialog :title="editForm.id ? '修改项目' : '创建项目'" :visible.sync="editShow" width="560px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px" size="small">
        <el-form-item label="会员UID" prop="uid">
          <el-input-number v-model="editForm.uid" :min="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="editForm.name" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="editForm.type" allow-create filterable default-first-option style="width: 180px">
            <el-option label="小程序" value="小程序" />
            <el-option label="APP开发" value="APP开发" />
            <el-option label="系统定制" value="系统定制" />
            <el-option label="UI 设计" value="UI 设计" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 180px">
            <el-option label="需求确认" :value="1" />
            <el-option label="开发中" :value="2" />
            <el-option label="待验收" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已终止" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前阶段">
          <el-input v-model="editForm.stage" maxlength="30" placeholder="如：UI 设计 / 联调中" />
        </el-form-item>
        <el-form-item label="项目经理">
          <el-input v-model="editForm.manager" maxlength="20" style="width: 180px" />
        </el-form-item>
        <el-form-item label="交付周期">
          <el-input v-model="editForm.cycle" maxlength="30" placeholder="如：45 天交付" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" controls-position="right" style="width: 180px" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>

    <!-- 设置进度 -->
    <el-dialog :title="`设置进度 — ${progressForm.projectName}`" :visible.sync="progressShow" width="620px" append-to-body>
      <el-form ref="progressForm" :model="progressForm" :rules="progressRules" label-width="100px" size="small">
        <el-form-item label="阶段名称" prop="stage">
          <el-input v-model="progressForm.stage" maxlength="30" placeholder="如：原型评审 / 开发交付" />
        </el-form-item>
        <el-form-item label="进度(%)" prop="percent">
          <el-slider v-model="progressForm.percent" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="同步状态">
          <el-switch v-model="progressForm.syncStatus" active-text="按进度自动更新项目状态（100% → 待验收）" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="progressForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="会员端「最新动态」展示该说明" />
        </el-form-item>
      </el-form>
      <el-divider content-position="left">进度记录</el-divider>
      <el-timeline v-if="progressRecords.length" class="progress-timeline">
        <el-timeline-item v-for="item in progressRecords" :key="item.id" :timestamp="fmtTime(item.createTime)" placement="top">
          <b>{{ item.stage }}</b>
          <span class="c-gray ml10">{{ item.percent }}%</span>
          <p v-if="item.remark" class="c-gray remark">{{ item.remark }}</p>
        </el-timeline-item>
      </el-timeline>
      <div v-else class="c-gray">暂无进度记录</div>
      <div slot="footer">
        <el-button size="small" @click="progressShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSaveProgress">保存进度</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { svcProjectListApi, svcProjectInfoApi, svcProjectSaveApi, svcProgressSaveApi, svcProgressListApi, svcProjectDeleteApi } from '@/api/enterprise';

export default {
  name: 'SvcProject',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '', uid: undefined, status: undefined },
      editShow: false,
      editForm: this.emptyForm(),
      rules: {
        uid: [{ required: true, message: '请填写会员UID', trigger: 'blur' }],
        name: [{ required: true, message: '请填写项目名称', trigger: 'blur' }]
      },
      progressShow: false,
      progressForm: { projectId: null, projectName: '', stage: '', percent: 0, remark: '', syncStatus: true },
      progressRules: {
        stage: [{ required: true, message: '请填写阶段名称', trigger: 'blur' }],
        percent: [{ required: true, message: '请填写进度', trigger: 'change' }]
      },
      progressRecords: []
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    emptyForm() {
      return { id: null, uid: undefined, name: '', type: '小程序', status: 1, stage: '', manager: '', cycle: '', sort: 0 };
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcProjectListApi(this.tableFrom)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = res.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    statusType(s) {
      return { 1: 'info', 2: 'primary', 3: 'warning', 4: 'success', [-1]: 'danger' }[s] || 'info';
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    onOpenEdit(row) {
      if (row) {
        this.editForm = {
          id: row.id,
          uid: row.uid,
          name: row.name,
          type: row.type || '小程序',
          status: row.status,
          stage: row.stage || '',
          manager: row.manager || '',
          cycle: row.cycle || '',
          sort: row.sort || 0
        };
      } else {
        this.editForm = this.emptyForm();
      }
      this.editShow = true;
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcProjectSaveApi(this.editForm)
          .then(() => {
            this.$message.success('保存成功');
            this.editShow = false;
            this.getList();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onOpenProgress(row) {
      this.progressForm = {
        projectId: row.id,
        projectName: row.name,
        stage: row.stage || '',
        percent: Number(row.percent) || 0,
        remark: '',
        syncStatus: true
      };
      this.progressShow = true;
      this.loadRecords(row.id);
    },
    loadRecords(projectId) {
      svcProgressListApi(projectId).then((res) => {
        this.progressRecords = Array.isArray(res) ? res : [];
      });
    },
    onSaveProgress() {
      this.$refs.progressForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcProgressSaveApi({
          projectId: this.progressForm.projectId,
          stage: this.progressForm.stage,
          percent: this.progressForm.percent,
          remark: this.progressForm.remark,
          syncStatus: this.progressForm.syncStatus ? 1 : 0
        })
          .then(() => {
            this.$message.success('进度已保存，会员端可见');
            this.loadRecords(this.progressForm.projectId);
            this.getList();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onDelete(row) {
      this.$confirm(`确认删除项目「${row.name}」？删除后会员端不可见。`, '提示', { type: 'warning' })
        .then(() => svcProjectDeleteApi(row.id))
        .then(() => {
          this.$message.success('删除成功');
          this.getList();
        })
        .catch(() => {});
    }
  }
};
</script>

<style scoped lang="scss">
.filter-bar { margin: 14px 0; }
.ml10 { margin-left: 10px; }
.c-gray { color: #909399; font-size: 12px; }
.progress-timeline { padding-left: 4px; }
.remark { margin-top: 2px; }
</style>
