<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>发票管理：客户在会员端提交开票信息，这里查看信息、开票、上传成品发票文件并变更状态。</span>
      </div>

      <div class="stat-row">
        <div class="stat-box">
          <div class="stat-num warn">{{ stats.waitCount || 0 }}</div>
          <div class="stat-lbl">待开票</div>
        </div>
        <div class="stat-box">
          <div class="stat-num primary">{{ stats.issuingCount || 0 }}</div>
          <div class="stat-lbl">开票中</div>
        </div>
        <div class="stat-box">
          <div class="stat-num success">{{ stats.issuedCount || 0 }}</div>
          <div class="stat-lbl">已开票</div>
        </div>
        <div class="stat-box">
          <div class="stat-num">¥{{ money(stats.monthAmount) }}</div>
          <div class="stat-lbl">本月开票金额</div>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="单号/抬头/税号/合同号" clearable style="width: 230px" size="small" @keyup.enter.native="getList(1)" />
        <el-select v-model="tableFrom.status" placeholder="状态" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="待开票" :value="0" />
          <el-option label="开票中" :value="1" />
          <el-option label="已开票" :value="2" />
          <el-option label="已驳回" :value="3" />
        </el-select>
        <el-select v-model="tableFrom.type" placeholder="发票类型" clearable style="width: 160px" size="small" class="ml10">
          <el-option label="增值税普通发票" :value="1" />
          <el-option label="增值税专用发票" :value="2" />
          <el-option label="电子普通发票" :value="3" />
        </el-select>
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="申请单号" width="160">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.invoiceNo }}</div>
            <div class="sub">{{ scope.row.contractNo || '未关联合同' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="开票抬头" min-width="180">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.title || '—' }}</div>
            <div class="sub">{{ scope.row.taxNo || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="发票类型" width="150" align="center">
          <template slot-scope="scope">{{ scope.row.typeText || '—' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right" class-name="num-cell">
          <template slot-scope="scope">
            <b>¥{{ money(scope.row.amount) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="客户" width="140">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.nickname || '—' }}</div>
            <div class="sub">UID {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ scope.row.statusText || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenHandle(scope.row)">开票 / 处理</el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onOpenHandle(scope.row, true)">详情</el-button>
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

    <!-- 开票 / 处理 -->
    <el-dialog
      :title="handleReadonly ? '开票申请详情' : `开票 / 处理 — ${handleForm.invoiceNo}`"
      :visible.sync="handleShow"
      width="860px"
      append-to-body
      top="6vh"
    >
      <div class="dlg-scroll">
        <!-- 申请信息 -->
        <div class="grp-title">申请信息</div>
        <el-descriptions :column="3" size="small" border>
          <el-descriptions-item label="申请单号">{{ handleForm.invoiceNo }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ fmtTime(handleForm.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="客户">
            {{ handleForm.nickname || '—' }}
            <span class="c-gray">（UID {{ handleForm.uid }}）</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 开票信息 -->
        <div class="grp-title">开票信息</div>
        <el-form ref="handleForm" :model="handleForm" :rules="rules" label-width="100px" size="small">
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="发票类型">
                <el-select v-model="handleForm.type" style="width: 100%">
                  <el-option label="增值税普通发票" :value="1" />
                  <el-option label="增值税专用发票" :value="2" />
                  <el-option label="电子普通发票" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="抬头类型">
                <el-radio-group v-model="handleForm.titleType">
                  <el-radio :label="1">企业</el-radio>
                  <el-radio :label="2">个人</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="开票金额" prop="amount">
                <el-input-number v-model="handleForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="发票抬头" prop="title">
                <el-input v-model="handleForm.title" maxlength="60" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="税号">
                <el-input v-model="handleForm.taxNo" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="开户银行">
                <el-input v-model="handleForm.bank" maxlength="60" placeholder="专票必填" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="银行账号">
                <el-input v-model="handleForm.bankAccount" maxlength="30" placeholder="专票必填" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="注册电话">
                <el-input v-model="handleForm.phone" maxlength="20" placeholder="专票必填" />
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="注册地址">
                <el-input v-model="handleForm.addr" maxlength="120" placeholder="专票必填" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开票内容">
                <el-input v-model="handleForm.content" maxlength="60" placeholder="如：技术服务费" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="接收邮箱">
                <el-input v-model="handleForm.email" maxlength="60" placeholder="电子发票用" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 收件信息（纸质） -->
          <div class="grp-title sub">收件信息（纸质发票邮寄用）</div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="收件人">
                <el-input v-model="handleForm.receiver" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="收件电话">
                <el-input v-model="handleForm.receiverPhone" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="寄出日期">
                <el-date-picker v-model="handleForm.sentDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" placeholder="邮寄后回填" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收件地址">
                <el-input v-model="handleForm.receiverAddr" maxlength="120" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="快递公司">
                <el-input v-model="handleForm.expressCo" maxlength="20" placeholder="如：顺丰" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="快递单号">
                <el-input v-model="handleForm.expressNo" maxlength="30" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="客户备注">
            <el-input v-model="handleForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>

          <!-- 开票处理 -->
          <div class="grp-title sub">开票处理</div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select v-model="handleForm.status" style="width: 100%">
                  <el-option label="待开票" :value="0" />
                  <el-option label="开票中" :value="1" />
                  <el-option label="已开票" :value="2" />
                  <el-option label="已驳回" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="经办人">
                <el-input v-model="handleForm.operator" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="驳回原因">
                <el-input v-model="handleForm.rejectReason" maxlength="100" placeholder="选「已驳回」时必填" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="成品发票">
            <div class="file-row">
              <div v-if="handleForm.fileUrl" class="file-chip">
                <span class="file-badge">{{ extOf(handleForm.fileName || handleForm.fileUrl) || 'FILE' }}</span>
                <span class="file-name">{{ handleForm.fileName || nameFromUrl(handleForm.fileUrl) }}</span>
                <a :href="fullUrl(handleForm.fileUrl)" target="_blank" class="file-act">查看</a>
                <span class="file-act danger" @click="clearFile">移除</span>
              </div>
              <el-upload action :http-request="onUploadFile" :show-file-list="false" :disabled="handleReadonly">
                <el-button size="mini" plain icon="el-icon-upload2" :disabled="handleReadonly">上传发票文件</el-button>
              </el-upload>
              <span class="c-gray">支持 PDF / 图片；置为「已开票」前必须先上传</span>
            </div>
          </el-form-item>

          <el-form-item label="变更备注">
            <el-input v-model="handleForm.logRemark" maxlength="120" placeholder="会记入状态日志，客户在 H5 合同/发票详情里看得到" />
          </el-form-item>
        </el-form>

        <!-- 状态日志 -->
        <div class="grp-title sub">状态记录</div>
        <el-timeline v-if="handleForm.logList && handleForm.logList.length" class="log-timeline">
          <el-timeline-item v-for="(it, i) in handleForm.logList" :key="i" :timestamp="it.time" placement="top">
            <b>{{ it.statusName }}</b>
            <span class="c-gray ml10">{{ it.operator }}</span>
            <p v-if="it.remark" class="c-gray log-remark">{{ it.remark }}</p>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="c-gray">暂无状态记录</div>
      </div>

      <div slot="footer">
        <el-button size="small" @click="handleShow = false">{{ handleReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!handleReadonly" size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  svcInvoiceListApi,
  svcInvoiceInfoApi,
  svcInvoiceSaveApi,
  svcInvoiceDeleteApi,
  svcInvoiceStatsApi
} from '@/api/enterprise';
import { fileFileApi } from '@/api/systemSetting';

export default {
  name: 'SvcInvoice',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      stats: {},
      tableFrom: { page: 1, limit: 10, keywords: '', status: undefined, type: undefined },
      handleShow: false,
      handleReadonly: false,
      handleForm: this.emptyForm(),
      rules: {
        title: [{ required: true, message: '请填写发票抬头', trigger: 'blur' }],
        amount: [{ required: true, message: '请填写开票金额', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.getList();
    this.getStats();
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        invoiceNo: '',
        uid: null,
        nickname: '',
        createTime: '',
        contractId: null,
        contractNo: '',
        type: 1,
        titleType: 1,
        title: '',
        taxNo: '',
        bank: '',
        bankAccount: '',
        addr: '',
        phone: '',
        amount: 0,
        content: '',
        receiver: '',
        receiverPhone: '',
        receiverAddr: '',
        email: '',
        remark: '',
        status: 0,
        fileUrl: '',
        fileName: '',
        expressCo: '',
        expressNo: '',
        sentDate: '',
        rejectReason: '',
        operator: '',
        logRemark: '',
        logList: []
      };
    },
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcInvoiceListApi(this.tableFrom)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = res.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getStats() {
      svcInvoiceStatsApi().then((res) => {
        this.stats = res || {};
      });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    statusType(s) {
      return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }[s] || 'info';
    },
    money(v) {
      const n = Number(v || 0);
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
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
      return i >= 0 ? s.substring(i + 1).toUpperCase() : '';
    },
    fullUrl(u) {
      const s = String(u || '');
      if (!s) return '';
      if (/^https?:\/\//.test(s)) return s;
      return s.indexOf('/') === 0 ? s : '/' + s;
    },
    /** 上传返回的可能是带域名的完整地址，落库统一存相对路径 */
    relUrl(u) {
      const s = String(u || '');
      const i = s.indexOf('qianxuimage/');
      return i >= 0 ? s.slice(i) : s;
    },
    onOpenHandle(row, readonly) {
      this.handleReadonly = !!readonly;
      this.handleForm = this.emptyForm();
      this.handleShow = true;
      svcInvoiceInfoApi(row.id).then((res) => {
        const d = res || {};
        this.handleForm = Object.assign(this.emptyForm(), {
          id: d.id,
          invoiceNo: d.invoiceNo,
          uid: d.uid,
          nickname: d.nickname,
          createTime: d.createTime,
          contractId: d.contractId,
          contractNo: d.contractNo,
          type: d.type == null ? 1 : d.type,
          titleType: d.titleType == null ? 1 : d.titleType,
          title: d.title || '',
          taxNo: d.taxNo || '',
          bank: d.bank || '',
          bankAccount: d.bankAccount || '',
          addr: d.addr || '',
          phone: d.phone || '',
          amount: Number(d.amount || 0),
          content: d.content || '',
          receiver: d.receiver || '',
          receiverPhone: d.receiverPhone || '',
          receiverAddr: d.receiverAddr || '',
          email: d.email || '',
          remark: d.remark || '',
          status: d.status == null ? 0 : d.status,
          fileUrl: d.fileUrl || '',
          fileName: d.fileName || '',
          expressCo: d.expressCo || '',
          expressNo: d.expressNo || '',
          sentDate: d.sentDate ? String(d.sentDate).slice(0, 10) : '',
          rejectReason: d.rejectReason || '',
          operator: d.operator || '',
          logRemark: '',
          logList: d.logList || []
        });
      });
    },
    clearFile() {
      this.handleForm.fileUrl = '';
      this.handleForm.fileName = '';
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
          this.handleForm.fileUrl = url;
          this.handleForm.fileName = (res && res.fileName) || (param.file && param.file.name) || this.nameFromUrl(url);
          this.$message.success('上传成功');
        })
        .catch(() => {
          loading.close();
        });
    },
    onSave() {
      this.$refs.handleForm.validate((valid) => {
        if (!valid) return;
        if (this.handleForm.status === 2 && !this.handleForm.fileUrl) {
          this.$message.warning('请先上传成品发票文件，再置为「已开票」');
          return;
        }
        if (this.handleForm.status === 3 && !this.handleForm.rejectReason) {
          this.$message.warning('驳回时请填写驳回原因');
          return;
        }
        this.saving = true;
        svcInvoiceSaveApi(this.handleForm)
          .then(() => {
            this.$message.success('保存成功');
            this.handleShow = false;
            this.getList();
            this.getStats();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onDelete(row) {
      this.$confirm(`确认删除开票申请「${row.invoiceNo}」？删除后客户在会员端不再可见。`, '提示', { type: 'warning' })
        .then(() => svcInvoiceDeleteApi(row.id))
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
.ml10 { margin-left: 10px; }
.c-gray { color: #909399; font-size: 12px; }

.stat-row {
  display: flex;
  gap: 12px;
  margin-top: 14px;
  .stat-box {
    flex: 1;
    min-width: 0;
    background: #f7f8fa;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    padding: 12px 16px;
  }
  .stat-num {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    line-height: 1.3;
    &.warn { color: #e6a23c; }
    &.primary { color: #409eff; }
    &.success { color: #67c23a; }
  }
  .stat-lbl { font-size: 12.5px; color: #909399; margin-top: 2px; }
}

.hd { color: #303133; font-weight: 600; }
.sub { color: #909399; font-size: 12px; margin-top: 2px; }

.dlg-scroll { max-height: 66vh; overflow-y: auto; padding-right: 6px; }
.grp-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #303133;
  margin: 14px 0 10px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
  &.sub { margin-top: 18px; }
}
.grp-title:first-child { margin-top: 0; }

.file-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.file-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #f4f6f9;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 5px 10px;
}
.file-badge {
  background: #409eff;
  color: #fff;
  font-size: 11px;
  border-radius: 3px;
  padding: 1px 5px;
}
.file-name { font-size: 12.5px; color: #303133; }
.file-act {
  font-size: 12.5px;
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
  &.danger { color: #f56c6c; }
}
.log-timeline { padding-left: 4px; }
.log-remark { margin-top: 2px; }

::v-deep .el-descriptions__body { background: #fafbfc; }
</style>
