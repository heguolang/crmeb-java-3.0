<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>企业服务合同：后台创建合同并上传合同文件（pdf / doc / docx / xls / 图片），设置签章与签字在签署区的位置；会员在 H5「合同管理」中查看合同文件、横屏手写签名 + 短信验证完成签约。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">创建合同</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="合同名称/编号" clearable style="width: 200px" size="small" @keyup.enter.native="getList(1)" />
        <el-input v-model.number="tableFrom.uid" placeholder="会员UID" clearable style="width: 120px" size="small" class="ml10" />
        <el-select v-model="tableFrom.status" placeholder="状态" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="待签约" :value="0" />
          <el-option label="已签约" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="合同编号" prop="contractNo" min-width="150" />
        <el-table-column label="合同名称" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="签约主体" min-width="150">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.partyType === 1 ? 'primary' : 'info'">{{ scope.row.partyType === 1 ? '企业' : '个人' }}</el-tag>
            <span class="ml6">{{ scope.row.partyName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="会员" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.nickname || '—' }}</span>
            <span class="c-gray">（ID {{ scope.row.uid }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="合同文件" width="100" align="center">
          <template slot-scope="scope">
            <span v-if="countOf(scope.row.annex)" class="c-gray">{{ countOf(scope.row.annex) }} 份</span>
            <span v-else class="c-gray">—</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template slot-scope="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签约负责人" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.signName || '—' }}</template>
        </el-table-column>
        <el-table-column label="签约时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.signTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onView(scope.row)">查看</el-button>
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

    <!-- 新建/编辑 -->
    <el-dialog :title="editForm.id ? '修改合同' : '创建合同'" :visible.sync="editShow" width="760px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="110px" size="small">
        <el-form-item label="会员UID" prop="uid">
          <el-input-number v-model="editForm.uid" :min="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="合同名称" prop="title">
          <el-input v-model="editForm.title" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="签约主体">
          <el-radio-group v-model="editForm.partyType">
            <el-radio :label="1">企业</el-radio>
            <el-radio :label="2">个人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="editForm.partyType === 1 ? '企业名称' : '个人姓名'">
          <el-input v-model="editForm.partyName" maxlength="60" :placeholder="editForm.partyType === 1 ? '签约企业全称' : '签约个人姓名'" />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input-number v-model="editForm.amount" :min="0" :precision="2" controls-position="right" style="width: 180px" />
        </el-form-item>

        <el-form-item label="合同文件">
          <div class="file-area">
            <div v-for="(f, i) in editFiles" :key="i" class="file-item">
              <span class="file-badge" :class="'ext-' + f.ext">{{ (f.ext || 'FILE').toUpperCase() }}</span>
              <span class="file-name" :title="f.name">{{ f.name }}</span>
              <i class="el-icon-close file-del" title="移除" @click="removeFile(i)" />
            </div>
            <el-upload
              class="file-upload"
              action
              :show-file-list="false"
              :http-request="onUploadFile"
              :accept="acceptExt"
              multiple
            >
              <div class="file-add">+ 上传合同文件</div>
            </el-upload>
          </div>
          <div class="tip">支持 pdf / doc / docx / xls / xlsx / 图片（jpg png 等），单个文件不超过 30MB；可上传多份（合同正文 + 附件）</div>
        </el-form-item>

        <el-form-item label="签章位置">
          <el-radio-group v-model="editForm.sealPos">
            <el-radio-button label="left">左侧</el-radio-button>
            <el-radio-button label="center">居中</el-radio-button>
            <el-radio-button label="right">右侧</el-radio-button>
          </el-radio-group>
          <span class="tip-inline">会员端签署区中「客户签章」的排版位置</span>
        </el-form-item>
        <el-form-item label="签字位置">
          <el-radio-group v-model="editForm.signPos">
            <el-radio-button label="left">左侧</el-radio-button>
            <el-radio-button label="center">居中</el-radio-button>
            <el-radio-button label="right">右侧</el-radio-button>
          </el-radio-group>
          <span class="tip-inline">会员端签署区中「客户签字」的排版位置</span>
        </el-form-item>

        <el-form-item label="合同条款说明">
          <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="选填：条款要点补充说明（会员端展示在合同文件下方）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>

    <!-- 查看 -->
    <el-dialog title="合同详情" :visible.sync="viewShow" width="760px" append-to-body>
      <div v-if="viewRow" class="view-wrap">
        <p><b>合同编号：</b>{{ viewRow.contractNo }}</p>
        <p><b>合同名称：</b>{{ viewRow.title }}</p>
        <p><b>签约主体：</b>{{ viewRow.partyType === 1 ? '企业' : '个人' }} — {{ viewRow.partyName || '—' }}</p>
        <p><b>会员：</b>{{ viewRow.nickname || '—' }}（ID {{ viewRow.uid }}）</p>
        <p><b>金额：</b>¥{{ viewRow.amount }}</p>
        <p><b>状态：</b>{{ statusText(viewRow.status) }}<template v-if="viewRow.status === 2">（{{ viewRow.rejectReason }}）</template></p>
        <el-divider content-position="left">签署信息</el-divider>
        <p><b>签约负责人：</b>{{ viewRow.signName || '—' }}</p>
        <p><b>签约手机号：</b>{{ viewRow.signPhone || '—' }}</p>
        <p><b>签约时间：</b>{{ fmtTime(viewRow.signTime) }}</p>
        <div class="seal-row">
          <div class="seal-box">
            <span class="c-gray">客户签章</span>
            <div v-if="viewRow.partyType === 1" class="seal-stamp">合同专用章</div>
            <div v-else class="seal-empty">个人签约，无需盖章</div>
          </div>
          <div class="seal-box">
            <span class="c-gray">客户签字</span>
            <el-image v-if="viewRow.signHand" :src="viewRow.signHand" :preview-src-list="[viewRow.signHand]" fit="contain" class="hand-img" />
            <div v-else class="seal-empty">未签字</div>
          </div>
        </div>
        <el-divider content-position="left">合同文件</el-divider>
        <div v-if="viewFiles.length" class="file-area">
          <div v-for="(f, i) in viewFiles" :key="i" class="file-item">
            <span class="file-badge" :class="'ext-' + f.ext">{{ (f.ext || 'FILE').toUpperCase() }}</span>
            <a class="file-name file-link" :href="f.url" target="_blank" rel="noopener noreferrer" :title="f.name">{{ f.name }}</a>
          </div>
        </div>
        <div v-else class="c-gray">未上传合同文件</div>
        <div v-if="viewRow.content" class="pre-block">{{ viewRow.content }}</div>
      </div>
      <div slot="footer">
        <el-button size="small" @click="viewShow = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { svcContractListApi, svcContractInfoApi, svcContractSaveApi, svcContractDeleteApi } from '@/api/enterprise';
import { fileFileApi } from '@/api/systemSetting';

export default {
  name: 'SvcContract',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '', uid: undefined, status: undefined },
      editShow: false,
      editForm: this.emptyForm(),
      acceptExt: '.pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.bmp,.webp',
      rules: {
        uid: [{ required: true, message: '请填写会员UID', trigger: 'blur' }],
        title: [{ required: true, message: '请填写合同名称', trigger: 'blur' }]
      },
      viewShow: false,
      viewRow: null
    };
  },
  computed: {
    editFiles() {
      return this.pairFiles(this.editForm.annex, this.editForm.annexName);
    },
    viewFiles() {
      return this.pairFiles(this.viewRow && this.viewRow.annex, this.viewRow && this.viewRow.annexName);
    }
  },
  mounted() {
    this.getList();
  },
  methods: {
    emptyForm() {
      return {
        id: null, uid: undefined, title: '', amount: 0, partyType: 2, partyName: '',
        annex: '', annexName: '', signPage: 0, signPos: 'center', sealPos: 'center',
        content: '', remark: ''
      };
    },
    /** 把 annex（URL 逗号分隔）与 annexName（文件名逗号分隔）按顺序配对 */
    pairFiles(annex, annexName) {
      const urls = String(annex || '').split(',').map((x) => x.trim()).filter(Boolean);
      const names = String(annexName || '').split(',').map((x) => x.trim());
      return urls.map((url, i) => {
        const name = names[i] || this.nameFromUrl(url);
        return { url, name, ext: this.extOf(name) };
      });
    },
    nameFromUrl(url) {
      const s = String(url || '').split('?')[0];
      const last = s.substring(s.lastIndexOf('/') + 1);
      try {
        return decodeURIComponent(last);
      } catch (e) {
        return last;
      }
    },
    extOf(name) {
      const s = String(name || '');
      const i = s.lastIndexOf('.');
      return i >= 0 ? s.substring(i + 1).toLowerCase() : '';
    },
    /** 上传返回的可能是带域名的完整地址，落库统一存相对路径（与历史图片附件保持一致） */
    relUrl(u) {
      const s = String(u || '');
      const i = s.indexOf('qianxuimage/');
      return i >= 0 ? s.slice(i) : s;
    },
    countOf(annex) {
      return String(annex || '').split(',').map((x) => x.trim()).filter(Boolean).length;
    },
    onUploadFile(param) {
      const formData = new FormData();
      formData.append('multipart', param.file);
      const loading = this.$loading({
        lock: true,
        text: '上传中，请稍候...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      fileFileApi(formData, { model: 'user', pid: 7 })
        .then((res) => {
          loading.close();
          const url = this.relUrl(res && (res.url || res.sattDir));
          if (!url) {
            this.$message.error('上传失败，请重试');
            return;
          }
          const name = (res && res.fileName) || (param.file && param.file.name) || this.nameFromUrl(url);
          const list = this.editFiles.slice();
          list.push({ url, name, ext: this.extOf(name) });
          this.applyFiles(list);
          this.$message.success('上传成功');
        })
        .catch(() => {
          loading.close();
        });
    },
    applyFiles(list) {
      this.editForm.annex = list.map((f) => f.url).join(',');
      // 文件名不允许含逗号，避免破坏逗号分隔结构
      this.editForm.annexName = list.map((f) => String(f.name || '').replace(/,/g, '_')).join(',');
    },
    removeFile(i) {
      const list = this.editFiles.slice();
      list.splice(i, 1);
      this.applyFiles(list);
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcContractListApi(this.tableFrom)
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
    statusText(s) {
      return { 0: '待签约', 1: '已签约', 2: '已驳回' }[s] || '—';
    },
    statusType(s) {
      return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info';
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    onOpenEdit(row) {
      if (row) {
        svcContractInfoApi(row.id).then((res) => {
          this.editForm = {
            id: res.id,
            uid: res.uid,
            title: res.title,
            amount: Number(res.amount) || 0,
            partyType: res.partyType || 2,
            partyName: res.partyName || '',
            annex: res.annex || '',
            annexName: res.annexName || '',
            signPage: res.signPage || 0,
            signPos: res.signPos || 'center',
            sealPos: res.sealPos || 'center',
            content: res.content || '',
            remark: res.remark || ''
          };
          this.editShow = true;
        });
      } else {
        this.editForm = this.emptyForm();
        this.editShow = true;
      }
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcContractSaveApi(this.editForm)
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
    onView(row) {
      svcContractInfoApi(row.id).then((res) => {
        this.viewRow = res;
        this.viewShow = true;
      });
    },
    onDelete(row) {
      this.$confirm(`确认删除合同「${row.title}」？`, '提示', { type: 'warning' })
        .then(() => svcContractDeleteApi(row.id))
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
.ml6 { margin-left: 6px; }
.c-gray { color: #909399; font-size: 12px; }
.tip { color: #909399; font-size: 12px; line-height: 1.6; }
.tip-inline { color: #909399; font-size: 12px; margin-left: 10px; }

/* 合同文件 */
.file-area { display: flex; flex-direction: column; gap: 8px; }
.file-item {
  display: flex; align-items: center; height: 34px; padding: 0 10px;
  border: 1px solid #ebeef5; border-radius: 4px; background: #fafbfc;
}
.file-badge {
  flex: none; width: 42px; height: 20px; line-height: 20px; text-align: center;
  border-radius: 3px; font-size: 11px; font-weight: 600;
  background: #ecf5ff; color: #409eff; border: 1px solid #d9ecff;
}
.file-badge.ext-pdf { background: #fef0f0; color: #f56c6c; border-color: #fde2e2; }
.file-badge.ext-doc, .file-badge.ext-docx { background: #ecf5ff; color: #409eff; border-color: #d9ecff; }
.file-badge.ext-xls, .file-badge.ext-xlsx { background: #f0f9eb; color: #67c23a; border-color: #e1f3d8; }
.file-name {
  flex: 1; margin: 0 10px; font-size: 13px; color: #303133;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.file-link { color: #409eff; text-decoration: none; }
.file-link:hover { text-decoration: underline; }
.file-del { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.file-del:hover { color: #f56c6c; }
.file-add {
  width: 140px; height: 34px; line-height: 32px; text-align: center;
  border: 1px dashed #d9d9d9; border-radius: 4px;
  color: #909399; font-size: 12px; cursor: pointer;
}
.file-add:hover { border-color: #409eff; color: #409eff; }
.file-upload { display: inline-block; }

/* 签署信息 */
.seal-row { display: flex; margin-top: 12px; }
.seal-box { flex: 1; }
.seal-stamp {
  margin-top: 6px; width: 130px; height: 130px; border: 2px solid #d3232f; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #d3232f; font-size: 12px; font-weight: 600; transform: rotate(-12deg); opacity: 0.88;
}
.hand-img {
  width: 220px; height: 90px; border: 1px solid #ebeef5; border-radius: 4px;
  background: #fafbfc; margin-top: 6px;
}
.seal-empty { margin-top: 6px; color: #c0c4cc; font-size: 12px; }

.view-wrap p { margin: 6px 0; }
.pre-block {
  margin-top: 10px; white-space: pre-wrap; background: #f7f8fa;
  border-radius: 6px; padding: 12px; font-size: 13px; line-height: 1.7;
}
</style>
