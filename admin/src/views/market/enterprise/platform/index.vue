<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>签约渠道配置：登记合同时「电子合同」下的平台下拉选项由这里决定。停用后不再出现在下拉里，历史合同记录照旧保留。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">新增平台</el-button>
        </div>
      </div>

      <el-alert
        class="mt10"
        type="info"
        :closable="false"
        show-icon
        title="「纸质合同」与「电子合同（其他）」是固定渠道，不可删除；未收录的平台可在登记合同时选「电子合同（其他）」并手工填写平台名与存证编号。"
      />

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="平台名称" clearable style="width: 200px" size="small" @keyup.enter.native="getList(1)" />
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="渠道 / 平台" prop="name" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.kind || '电子签平台' }}</template>
        </el-table-column>
        <el-table-column label="资质与定位" prop="description" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
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
                {{ scope.row.status === 1 ? '停用' : '启用' }}
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

    <el-dialog :title="editForm.id ? '编辑平台' : '新增平台'" :visible.sync="editShow" width="560px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px" size="small">
        <el-form-item label="平台名称" prop="name">
          <el-input v-model="editForm.name" maxlength="32" show-word-limit placeholder="如：法大大 / e签宝" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="editForm.kind" maxlength="16" placeholder="电子签平台" />
        </el-form-item>
        <el-form-item label="资质与定位">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="2"
            maxlength="120"
            show-word-limit
            placeholder="如：全资持 CA 牌照 · 司法闭环强"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" controls-position="right" style="width: 180px" />
          <span class="c-gray ml10">数值越大越靠前</span>
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
import { svcPlatformListApi, svcPlatformSaveApi, svcPlatformDeleteApi } from '@/api/enterprise';

export default {
  name: 'SvcPlatform',
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
        name: [{ required: true, message: '请填写平台名称', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    emptyForm() {
      return { id: null, name: '', kind: '电子签平台', description: '', status: 1, sort: 0 };
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcPlatformListApi(this.tableFrom)
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
            name: row.name,
            kind: row.kind || '电子签平台',
            description: row.description || '',
            status: row.status === 0 ? 0 : 1,
            sort: row.sort || 0
          }
        : this.emptyForm();
      this.editShow = true;
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcPlatformSaveApi(this.editForm)
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
    onToggle(row) {
      const next = row.status === 1 ? 0 : 1;
      svcPlatformSaveApi({
        id: row.id,
        name: row.name,
        kind: row.kind,
        description: row.description,
        status: next,
        sort: row.sort
      }).then(() => {
        this.$message.success(next === 1 ? '已启用' : '已停用');
        this.getList();
      });
    },
    onDelete(row) {
      this.$confirm(`确认删除平台「${row.name}」？删除后登记合同不再可选，历史合同记录不受影响。`, '提示', {
        type: 'warning'
      })
        .then(() => svcPlatformDeleteApi(row.id))
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
.mt10 { margin-top: 10px; }
.c-gray { color: #909399; font-size: 12px; }
</style>
