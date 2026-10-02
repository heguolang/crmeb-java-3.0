<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>客户案例：管理 H5 软件开发首页「客户案例」区块与案例页展示内容。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">添加案例</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="案例名称" clearable style="width: 200px" size="small" @keyup.enter.native="getList(1)" />
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="配图" width="90" align="center">
          <template slot-scope="scope">
            <el-image v-if="scope.row.image" :src="scope.row.image" :preview-src-list="[scope.row.image]" fit="cover" class="case-thumb" />
            <span v-else class="c-gray">—</span>
          </template>
        </el-table-column>
        <el-table-column label="案例名称" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="分类" prop="category" width="100" align="center" />
        <el-table-column label="描述" prop="description" min-width="160" show-overflow-tooltip />
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="显示" width="80" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isShow === 1 ? 'success' : 'info'">{{ scope.row.isShow === 1 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
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

    <el-dialog :title="editForm.id ? '修改案例' : '添加案例'" :visible.sync="editShow" width="560px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="90px" size="small">
        <el-form-item label="案例名称" prop="title">
          <el-input v-model="editForm.title" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" allow-create filterable default-first-option style="width: 180px">
            <el-option label="小程序" value="小程序" />
            <el-option label="APP开发" value="APP开发" />
            <el-option label="系统定制" value="系统定制" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" maxlength="50" show-word-limit placeholder="如：电商零售 · 45 天交付" />
        </el-form-item>
        <el-form-item label="配图URL">
          <el-input v-model="editForm.image" placeholder="案例配图URL" />
          <div v-if="editForm.image" class="annex-preview">
            <el-image :src="editForm.image" :preview-src-list="[editForm.image]" fit="cover" class="annex-thumb" />
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="editForm.isShow" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { svcCaseListApi, svcCaseSaveApi, svcCaseDeleteApi } from '@/api/enterprise';

export default {
  name: 'SvcCase',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '' },
      editShow: false,
      editForm: this.emptyForm(),
      rules: {
        title: [{ required: true, message: '请填写案例名称', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    emptyForm() {
      return { id: null, title: '', category: '小程序', description: '', image: '', sort: 0, isShow: 1 };
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcCaseListApi(this.tableFrom)
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
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    onOpenEdit(row) {
      this.editForm = row
        ? {
            id: row.id,
            title: row.title,
            category: row.category || '小程序',
            description: row.description || '',
            image: row.image || '',
            sort: row.sort || 0,
            isShow: row.isShow
          }
        : this.emptyForm();
      this.editShow = true;
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcCaseSaveApi(this.editForm)
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
    onDelete(row) {
      this.$confirm(`确认删除案例「${row.title}」？`, '提示', { type: 'warning' })
        .then(() => svcCaseDeleteApi(row.id))
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
.annex-preview { margin-top: 8px; }
.annex-thumb { width: 64px; height: 64px; border-radius: 4px; border: 1px solid #ebeef5; }
.case-thumb { width: 56px; height: 56px; border-radius: 4px; }
</style>
