<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>企业服务合同：后台只做两件事——① 填甲方/乙方签约方信息并上传正文，② 在正文上摆好甲乙双方的公章/签字位置。甲乙双方各用会员账号登录 H5 手写签名，公章由系统按签约方名称自动生成圆形章；双方签完自动合成带双方签章的正式合同。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">创建合同</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="合同名称/编号" clearable style="width: 200px" size="small" @keyup.enter.native="getList(1)" />
        <el-input v-model.number="tableFrom.uid" placeholder="甲方会员UID" clearable style="width: 130px" size="small" class="ml10" />
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
        <el-table-column label="合同编号" prop="contractNo" min-width="145" />
        <el-table-column label="合同名称" prop="title" min-width="140" show-overflow-tooltip />
        <el-table-column label="签约主体" min-width="145">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.partyType === 1 ? 'primary' : 'info'">{{ scope.row.partyType === 1 ? '企业' : '个人' }}</el-tag>
            <span class="ml6">{{ scope.row.partyName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="甲方会员" min-width="115">
          <template slot-scope="scope">
            <span>{{ scope.row.nickname || '—' }}</span>
            <span class="c-gray">（{{ scope.row.uid }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="乙方账号" min-width="115">
          <template slot-scope="scope">
            <span v-if="scope.row.ourUid">{{ scope.row.ourNickname || '—' }}</span>
            <span v-else class="c-gray">未指定</span>
          </template>
        </el-table-column>
        <el-table-column label="签署进度" width="158">
          <template slot-scope="scope">
            <div class="prog-cell">
              <span class="prog-chip" :class="scope.row.custSigned ? 'is-ok' : 'is-wait'">甲{{ scope.row.custSigned ? '✓' : '…' }}</span>
              <span class="prog-chip" :class="scope.row.ourSigned ? 'is-ok' : 'is-wait'">乙{{ scope.row.ourSigned ? '✓' : '…' }}</span>
              <el-tag v-if="scope.row.finished" size="mini" type="success" class="ml4">成品</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="96" align="right">
          <template slot-scope="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="88" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onView(scope.row)">查看</el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onOpenEdit(scope.row)">修改</el-button>
              <el-button size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="60" />
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

    <!-- 新建/编辑：两步（① 合同与签约双方 → ② 签署位置与进度） -->
    <el-dialog :title="editForm.id ? '修改合同 · ' + (editForm.title || '') : '创建合同'" :visible.sync="editShow" width="860px" top="5vh" append-to-body :close-on-click-modal="false">
      <el-tabs v-model="step" class="edit-tabs">
        <el-tab-pane name="info">
          <span slot="label"><i :class="step === 'info' ? 'el-icon-edit' : 'el-icon-circle-check'" /> ① 合同与签约双方</span>
          <el-form ref="editForm" :model="editForm" :rules="rules" label-width="96px" size="small">
            <el-form-item label="合同名称" prop="title">
              <el-input v-model="editForm.title" maxlength="60" show-word-limit />
            </el-form-item>
            <el-form-item label="合同金额">
              <el-input-number v-model="editForm.amount" :min="0" :precision="2" controls-position="right" style="width: 180px" />
            </el-form-item>
            <el-form-item label="合同正文">
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
                  <div class="file-add">+ 上传合同正文（pdf / doc / docx / xls / 图片，≤30MB）</div>
                </el-upload>
              </div>
              <div class="tip">第一份即作为合同正文；Word/Excel 会在下一步由服务端自动转成 PDF 供摆放签署位置。换正文后已摆的位置与双方已签内容会作废重签。</div>
            </el-form-item>

            <el-divider content-position="left">签约双方（各填一个会员账号，各自登录 H5 签署）</el-divider>

            <div class="party-grid">
              <div class="party-card party-card--a">
                <div class="party-head"><span class="party-name">甲方 · 签约客户</span><span class="party-sub">用客户自己的会员账号签</span></div>
                <el-form-item label="会员账号" prop="uid">
                  <el-input-number v-model="editForm.uid" :min="1" controls-position="right" style="width: 150px" placeholder="客户会员UID" />
                </el-form-item>
                <el-form-item label="主体类型">
                  <el-radio-group v-model="editForm.partyType">
                    <el-radio :label="1">企业</el-radio>
                    <el-radio :label="2">个人</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item :label="editForm.partyType === 1 ? '企业名称' : '个人姓名'">
                  <el-input v-model="editForm.partyName" maxlength="60" :placeholder="editForm.partyType === 1 ? '营业执照全称（用于生成圆形公章）' : '签约个人姓名'" />
                </el-form-item>
                <el-form-item label="信用代码">
                  <el-input v-model="editForm.custCode" maxlength="32" placeholder="选填" />
                </el-form-item>
                <el-form-item label="法定代表人">
                  <el-input v-model="editForm.custLegal" maxlength="30" placeholder="选填" />
                </el-form-item>
                <el-form-item label="联系电话">
                  <el-input v-model="editForm.custPhone" maxlength="20" placeholder="选填" />
                </el-form-item>
                <el-form-item label="联系地址">
                  <el-input v-model="editForm.custAddr" maxlength="120" placeholder="选填" />
                </el-form-item>
              </div>

              <div class="party-card party-card--b">
                <div class="party-head">
                  <span class="party-name">乙方 · 黔序（我方）</span>
                  <span class="party-sub">用公司会员账号签</span>
                </div>
                <el-form-item label="会员账号">
                  <el-input-number v-model="editForm.ourUid" :min="0" controls-position="right" style="width: 150px" />
                  <el-button size="mini" plain class="ml6" :loading="savingDefault" @click="saveOurDefault">存为默认</el-button>
                </el-form-item>
                <el-form-item label="企业名称">
                  <el-input v-model="editForm.ourCompany" maxlength="60" placeholder="如：贵州黔序科技有限公司（用于生成圆形公章）" />
                </el-form-item>
                <el-form-item label="信用代码">
                  <el-input v-model="editForm.ourCode" maxlength="32" placeholder="选填" />
                </el-form-item>
                <el-form-item label="法定代表人">
                  <el-input v-model="editForm.ourLegal" maxlength="30" placeholder="选填" />
                </el-form-item>
                <el-form-item label="联系电话">
                  <el-input v-model="editForm.ourPhone" maxlength="20" placeholder="选填" />
                </el-form-item>
                <el-form-item label="联系地址">
                  <el-input v-model="editForm.ourAddr" maxlength="120" placeholder="选填" />
                </el-form-item>
                <div class="tip">「存为默认」后，新建合同自动带出乙方信息，不用重复填。</div>
              </div>
            </div>

            <el-form-item label="条款说明">
              <el-input v-model="editForm.content" type="textarea" :rows="3" placeholder="选填：条款要点补充说明（会员端展示在合同文件下方）" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" maxlength="100" />
            </el-form-item>
          </el-form>
          <div class="tip">公章 / 签字图不用传——双方在 H5 签署时，系统按上面的主体名称自动生成圆形公章，签字现场手写。</div>
        </el-tab-pane>

        <el-tab-pane name="layout" :disabled="!editForm.id">
          <span slot="label"><i class="el-icon-place" /> ② 签署位置与进度 <el-tag v-if="editForm.id" size="mini" :type="stampMeta.finished ? 'success' : 'info'" class="ml4">{{ stampMeta.finished ? '已完成' : '进行中' }}</el-tag></span>
          <div v-loading="preparing" element-loading-text="正在转换正文 PDF...">
            <div class="ly-row">
              <span class="ly-label">正文 PDF</span>
              <span v-if="stampMeta.pageCount" class="c-gray">已就绪：{{ stampMeta.pageCount }} 页</span>
              <span v-else class="c-gray">尚未转换</span>
              <el-button size="mini" plain :loading="preparing" class="ml10" @click="doPrepare">转换正文</el-button>
              <a v-if="stampMeta.srcPdf" class="file-link ml10" :href="stampMeta.srcPdf" target="_blank" rel="noopener noreferrer">预览正文 PDF</a>
            </div>
            <div class="ly-row">
              <span class="ly-label">签署位置</span>
              <span class="c-gray">{{ editForm.signLayout ? '已设置' : '未设置' }}</span>
              <el-button size="mini" type="primary" plain class="ml10" :disabled="!stampMeta.pageCount" @click="openLayout">摆位置：甲乙公章 / 签字</el-button>
              <span class="c-gray ml10">日期不用摆——双方签署时自动带在签字下方</span>
            </div>

            <div class="prog">
              <div class="prog-title">签署进度（双方签完自动合成正式合同，后台无需操作）</div>
              <div class="prow">
                <span class="who who--a">甲方 · {{ stampMeta.partyName || stampMeta.nickname || '—' }}</span>
                <span class="pill" :class="stampMeta.custSigned ? 'ok' : 'wait'">{{ stampMeta.custSigned ? '已签署' : '待签署' }}</span>
                <span class="when">{{ stampMeta.custSigned ? fmtTime(stampMeta.signTime) : '等待客户登录会员端签署' }}</span>
              </div>
              <div class="prow">
                <span class="who who--b">乙方 · {{ stampMeta.ourCompany || stampMeta.ourNickname || '—' }}</span>
                <span class="pill" :class="stampMeta.ourSigned ? 'ok' : 'wait'">{{ stampMeta.ourSigned ? '已签署' : '待签署' }}</span>
                <span class="when">{{ stampMeta.ourSigned ? fmtTime(stampMeta.ourSignedAt) : '等待我方登录会员端签署' }}</span>
              </div>
              <div class="prow">
                <span class="who">正式合同</span>
                <template v-if="stampMeta.finished">
                  <span class="pill ok">已生成</span>
                  <a class="file-link ml10" :href="stampMeta.signedFile" target="_blank" rel="noopener noreferrer">下载正式合同（带双方签章）</a>
                </template>
                <template v-else>
                  <span class="pill wait">待双方签完</span>
                  <span class="when">甲乙双方都签署后，服务端自动合成</span>
                </template>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button v-if="step === 'layout'" size="small" plain @click="step = 'info'">上一步</el-button>
        <el-button v-if="step === 'info'" size="small" type="primary" :loading="saving" @click="onSave(true)">保存并去摆位置 →</el-button>
        <el-button v-else size="small" type="primary" @click="editShow = false">完成</el-button>
      </div>
    </el-dialog>

    <!-- 用印位置设置 -->
    <svc-contract-layout
      ref="layoutEditor"
      :contract-id="editForm.id || 0"
      :layout="editForm.signLayout || ''"
      :page-total="stampMeta.pageCount || 0"
      @saved="onLayoutSaved"
    />

    <!-- 查看 -->
    <el-dialog title="合同详情" :visible.sync="viewShow" width="820px" append-to-body>
      <div v-if="viewRow" class="view-wrap">
        <p><b>合同编号：</b>{{ viewRow.contractNo }}</p>
        <p><b>合同名称：</b>{{ viewRow.title }}</p>
        <p><b>金额：</b>¥{{ viewRow.amount }}</p>
        <p><b>状态：</b>{{ statusText(viewRow.status) }}<template v-if="viewRow.status === 2">（{{ viewRow.rejectReason }}）</template></p>

        <el-divider content-position="left">甲方 · 签约客户</el-divider>
        <p><b>会员账号：</b>{{ viewRow.nickname || '—' }}（UID {{ viewRow.uid }}）</p>
        <p><b>签约主体：</b>{{ viewRow.partyType === 1 ? '企业' : '个人' }} — {{ viewRow.partyName || '—' }}</p>
        <p><b>信用代码 / 法人：</b>{{ viewRow.custCode || '—' }} / {{ viewRow.custLegal || '—' }}</p>
        <p><b>联系电话 / 地址：</b>{{ viewRow.custPhone || '—' }} / {{ viewRow.custAddr || '—' }}</p>
        <div class="seal-row">
          <div class="seal-box">
            <span class="c-gray">甲方签字</span>
            <el-image v-if="viewRow.signHand" :src="viewRow.signHand" :preview-src-list="[viewRow.signHand]" fit="contain" class="hand-img" />
            <div v-else class="seal-empty">未签署</div>
          </div>
          <div class="seal-box">
            <span class="c-gray">甲方公章（系统生成）</span>
            <el-image v-if="viewRow.custSeal" :src="viewRow.custSeal" :preview-src-list="[viewRow.custSeal]" fit="contain" class="seal-img" />
            <div v-else class="seal-empty">未签署</div>
          </div>
        </div>

        <el-divider content-position="left">乙方 · 黔序（我方）</el-divider>
        <p><b>会员账号：</b><template v-if="viewRow.ourUid">{{ viewRow.ourNickname || '—' }}（UID {{ viewRow.ourUid }}）</template><template v-else>未指定</template></p>
        <p><b>企业名称：</b>{{ viewRow.ourCompany || '—' }}</p>
        <p><b>信用代码 / 法人：</b>{{ viewRow.ourCode || '—' }} / {{ viewRow.ourLegal || '—' }}</p>
        <p><b>联系电话 / 地址：</b>{{ viewRow.ourPhone || '—' }} / {{ viewRow.ourAddr || '—' }}</p>
        <div class="seal-row">
          <div class="seal-box">
            <span class="c-gray">乙方签字（{{ viewRow.ourSigner || '签署人' }}）</span>
            <el-image v-if="viewRow.ourSign" :src="viewRow.ourSign" :preview-src-list="[viewRow.ourSign]" fit="contain" class="hand-img" />
            <div v-else class="seal-empty">未签署</div>
          </div>
          <div class="seal-box">
            <span class="c-gray">乙方公章（系统生成）</span>
            <el-image v-if="viewRow.ourSeal" :src="viewRow.ourSeal" :preview-src-list="[viewRow.ourSeal]" fit="contain" class="seal-img" />
            <div v-else class="seal-empty">未签署</div>
          </div>
        </div>

        <el-divider content-position="left">签署进度</el-divider>
        <p>
          <span class="prog-chip" :class="viewRow.custSigned ? 'is-ok' : 'is-wait'">甲方 {{ viewRow.custSigned ? '已签署 ' + fmtTime(viewRow.signTime) : '待签署' }}</span>
          <span class="prog-chip" :class="viewRow.ourSigned ? 'is-ok' : 'is-wait'">乙方 {{ viewRow.ourSigned ? '已签署 ' + fmtTime(viewRow.ourSignedAt) : '待签署' }}</span>
        </p>

        <el-divider content-position="left">合同文件</el-divider>
        <div v-if="viewRow.signedFile" class="done-file">
          <span class="file-badge ext-pdf">PDF</span>
          <a class="file-name file-link" :href="viewRow.signedFile" target="_blank" rel="noopener noreferrer">正式合同（双方签章齐全）</a>
        </div>
        <div v-if="viewFiles.length" class="file-area">
          <div v-for="(f, i) in viewFiles" :key="i" class="file-item">
            <span class="file-badge" :class="'ext-' + f.ext">{{ (f.ext || 'FILE').toUpperCase() }}</span>
            <a class="file-name file-link" :href="f.url" target="_blank" rel="noopener noreferrer" :title="f.name">{{ f.name }}</a>
          </div>
        </div>
        <div v-else class="c-gray">未上传合同文件</div>
        <div v-if="viewRow.fileHash" class="hash-line">存证摘要 SHA-256：{{ viewRow.fileHash }}</div>
        <div v-if="viewRow.content" class="pre-block">{{ viewRow.content }}</div>
      </div>
      <div slot="footer">
        <el-button size="small" @click="viewShow = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  svcContractListApi,
  svcContractInfoApi,
  svcContractSaveApi,
  svcContractDeleteApi,
  svcContractPrepareApi,
  svcContractOurDefaultApi,
  svcContractOurDefaultSaveApi
} from '@/api/enterprise';
import { fileFileApi } from '@/api/systemSetting';
import SvcContractLayout from '@/components/SvcContractLayout';

export default {
  name: 'SvcContract',
  components: { SvcContractLayout },
  data() {
    return {
      loading: false,
      saving: false,
      savingDefault: false,
      preparing: false,
      step: 'info',
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 10, keywords: '', uid: undefined, status: undefined },
      editShow: false,
      editForm: this.emptyForm(),
      stampMeta: this.emptyMeta(),
      acceptExt: '.pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.bmp,.webp',
      rules: {
        uid: [{ required: true, message: '请填写甲方会员UID', trigger: 'blur' }],
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
        id: null, uid: undefined, title: '', amount: 0, partyType: 1, partyName: '',
        custCode: '', custLegal: '', custPhone: '', custAddr: '',
        annex: '', annexName: '', signPage: 0, signPos: 'center', sealPos: 'center',
        content: '', remark: '',
        signLayout: '',
        ourUid: undefined, ourCompany: '', ourCode: '', ourLegal: '', ourPhone: '', ourAddr: ''
      };
    },
    emptyMeta() {
      return {
        srcPdf: '', srcPdfName: '', pageCount: 0,
        custSigned: false, ourSigned: false, finished: false,
        signedFile: '', signTime: '', ourSignedAt: '',
        partyName: '', nickname: '', ourCompany: '', ourNickname: ''
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
    uploading(text) {
      return this.$loading({
        lock: true,
        text: text || '上传中，请稍候...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
    },
    onUploadFile(param) {
      const formData = new FormData();
      formData.append('multipart', param.file);
      const loading = this.uploading();
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
      this.step = 'info';
      if (row) {
        svcContractInfoApi(row.id).then((res) => {
          this.editForm = {
            id: res.id,
            uid: res.uid,
            title: res.title,
            amount: Number(res.amount) || 0,
            partyType: res.partyType || 2,
            partyName: res.partyName || '',
            custCode: res.custCode || '',
            custLegal: res.custLegal || '',
            custPhone: res.custPhone || '',
            custAddr: res.custAddr || '',
            annex: res.annex || '',
            annexName: res.annexName || '',
            signPage: res.signPage || 0,
            signPos: res.signPos || 'center',
            sealPos: res.sealPos || 'center',
            content: res.content || '',
            remark: res.remark || '',
            signLayout: res.signLayout || '',
            ourUid: res.ourUid || undefined,
            ourCompany: res.ourCompany || '',
            ourCode: res.ourCode || '',
            ourLegal: res.ourLegal || '',
            ourPhone: res.ourPhone || '',
            ourAddr: res.ourAddr || ''
          };
          this.stampMeta = {
            srcPdf: res.srcPdf || '',
            srcPdfName: res.srcPdfName || '',
            pageCount: res.pageCount || 0,
            custSigned: !!res.custSigned,
            ourSigned: !!res.ourSigned,
            finished: !!res.finished,
            signedFile: res.signedFile || '',
            signTime: res.signTime || '',
            ourSignedAt: res.ourSignedAt || '',
            partyName: res.partyName || '',
            nickname: res.nickname || '',
            ourCompany: res.ourCompany || '',
            ourNickname: res.ourNickname || ''
          };
          this.editShow = true;
        });
      } else {
        this.editForm = this.emptyForm();
        this.stampMeta = this.emptyMeta();
        this.editShow = true;
        // 新建：自动带出乙方默认值（后台填一次，处处复用）
        svcContractOurDefaultApi().then((res) => {
          if (res && (res.ourUid || res.ourCompany)) {
            this.editForm.ourUid = res.ourUid || undefined;
            this.editForm.ourCompany = res.ourCompany || '';
            this.editForm.ourCode = res.ourCode || '';
            this.editForm.ourLegal = res.ourLegal || '';
            this.editForm.ourPhone = res.ourPhone || '';
            this.editForm.ourAddr = res.ourAddr || '';
          }
        });
      }
    },
    onSave(goLayout) {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcContractSaveApi(this.editForm)
          .then((id) => {
            this.$message.success('保存成功');
            if (id) {
              this.editForm.id = Number(id);
            }
            if (goLayout) {
              this.step = 'layout';
              if (!this.stampMeta.pageCount) {
                this.doPrepare();
              }
            } else {
              this.editShow = false;
            }
            this.getList();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    /** 乙方信息存为默认：新建合同自动带出 */
    saveOurDefault() {
      this.savingDefault = true;
      svcContractOurDefaultSaveApi({
        ourUid: this.editForm.ourUid || 0,
        ourCompany: this.editForm.ourCompany,
        ourCode: this.editForm.ourCode,
        ourLegal: this.editForm.ourLegal,
        ourPhone: this.editForm.ourPhone,
        ourAddr: this.editForm.ourAddr
      })
        .then(() => {
          this.$message.success('已保存为乙方默认值');
        })
        .finally(() => {
          this.savingDefault = false;
        });
    },
    /** 转换正文为 PDF（doc/docx/xls/图片），并拿到页数与签署进度 */
    doPrepare() {
      if (!this.editForm.id) {
        this.$message.warning('请先保存合同');
        return;
      }
      this.preparing = true;
      svcContractPrepareApi(this.editForm.id)
        .then((res) => {
          this.editForm.signLayout = res.signLayout || '';
          this.stampMeta = {
            srcPdf: res.srcPdf || '',
            srcPdfName: res.srcPdfName || '',
            pageCount: res.pageCount || 0,
            custSigned: !!res.custSigned,
            ourSigned: !!res.ourSigned,
            finished: !!res.finished,
            signedFile: res.signedFile || '',
            signTime: res.signTime || '',
            ourSignedAt: res.ourSignedAt || '',
            partyName: res.partyName || '',
            nickname: res.nickname || '',
            ourCompany: res.ourCompany || '',
            ourNickname: res.ourNickname || ''
          };
        })
        .finally(() => {
          this.preparing = false;
        });
    },
    openLayout() {
      if (!this.editForm.id) {
        this.$message.warning('请先保存合同');
        return;
      }
      if (!this.stampMeta.pageCount) {
        this.$message.warning('请先把合同正文转换成 PDF');
        return;
      }
      this.$refs.layoutEditor.open();
    },
    onLayoutSaved(json) {
      this.editForm.signLayout = json;
      this.getList();
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
.ml4 { margin-left: 4px; }
.c-gray { color: #909399; font-size: 12px; }
.tip { color: #909399; font-size: 12px; line-height: 1.6; }

/* 两步 Tab */
.edit-tabs ::v-deep .el-tabs__header { margin-bottom: 14px; }

/* 甲乙双卡 */
.party-grid { display: flex; gap: 14px; margin-bottom: 6px; }
.party-card {
  flex: 1; min-width: 0; border: 1px solid #ebeef5; border-radius: 8px; padding: 12px 14px 2px;
}
.party-card--a { border-top: 3px solid #e6a23c; background: #fffdf8; }
.party-card--b { border-top: 3px solid #409eff; background: #f8fbff; }
.party-head { display: flex; align-items: baseline; gap: 8px; margin-bottom: 10px; }
.party-name { font-weight: 600; font-size: 13px; }
.party-card--a .party-name { color: #b88230; }
.party-card--b .party-name { color: #1f5f9e; }
.party-sub { color: #c0c4cc; font-size: 12px; }

/* 签署进度 */
.prog-cell { display: flex; align-items: center; }
.prog-chip {
  display: inline-block; font-size: 11.5px; line-height: 20px; padding: 0 8px;
  border-radius: 10px; border: 1px solid; margin-right: 6px;
}
.prog-chip.is-ok { border-color: #c2e7b0; background: #f0f9eb; color: #529b2e; }
.prog-chip.is-wait { border-color: #f3d19e; background: #fdf6ec; color: #b88230; }

.prog { border: 1px solid #ebeef5; border-radius: 8px; padding: 12px 16px; margin-top: 14px; background: #fff; }
.prog-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.prow { display: flex; align-items: center; gap: 12px; padding: 9px 0; border-bottom: 1px dashed #ebeef5; }
.prow:last-child { border-bottom: 0; padding-bottom: 0; }
.prow .who { width: 210px; flex: none; font-size: 12.5px; font-weight: 600; color: #606266; }
.prow .who--a { color: #b88230; }
.prow .who--b { color: #1f5f9e; }
.pill { font-size: 11.5px; padding: 2px 9px; border-radius: 10px; border: 1px solid; }
.pill.ok { border-color: #c2e7b0; background: #f0f9eb; color: #529b2e; }
.pill.wait { border-color: #f3d19e; background: #fdf6ec; color: #b88230; }
.prow .when { color: #909399; font-size: 12px; margin-left: auto; }
.ly-row { display: flex; align-items: center; padding: 8px 0; }
.ly-label { width: 72px; flex: none; font-size: 13px; color: #606266; }

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
  height: 34px; line-height: 32px; text-align: center; padding: 0 14px;
  border: 1px dashed #d9d9d9; border-radius: 4px;
  color: #909399; font-size: 12px; cursor: pointer;
}
.file-add:hover { border-color: #409eff; color: #409eff; }
.file-upload { display: inline-block; }

/* 签署信息 */
.seal-row { display: flex; margin-top: 12px; gap: 16px; }
.seal-box { flex: none; width: 168px; }
.seal-img { width: 120px; height: 120px; margin-top: 6px; border: 1px solid #ebeef5; border-radius: 4px; background: #fafbfc; }
.hand-img { width: 160px; height: 66px; margin-top: 6px; border: 1px solid #ebeef5; border-radius: 4px; background: #fafbfc; }
.seal-empty { margin-top: 6px; color: #c0c4cc; font-size: 12px; height: 120px; display: flex; align-items: center; }

.done-file {
  display: flex; align-items: center; height: 34px; padding: 0 10px; margin-bottom: 8px;
  border: 1px solid #d6f0d0; border-radius: 4px; background: #f6fdf4;
}
.hash-line { margin-top: 8px; color: #909399; font-size: 12px; word-break: break-all; }

.view-wrap p { margin: 6px 0; }
.pre-block {
  margin-top: 10px; white-space: pre-wrap; background: #f7f8fa;
  border-radius: 6px; padding: 12px; font-size: 13px; line-height: 1.7;
}
</style>
