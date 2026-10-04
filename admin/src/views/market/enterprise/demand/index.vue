<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>服务需求：客户在会员端提交的需求都会落到这里，后台受理、指派跟进人、报价，再转成合同。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">代客录入</el-button>
        </div>
      </div>

      <div class="stat-row">
        <div class="stat-box">
          <div class="stat-num warn">{{ stats.waitCount || 0 }}</div>
          <div class="stat-lbl">待受理</div>
        </div>
        <div class="stat-box">
          <div class="stat-num primary">{{ stats.followingCount || 0 }}</div>
          <div class="stat-lbl">跟进中</div>
        </div>
        <div class="stat-box">
          <div class="stat-num purple">{{ stats.quotedCount || 0 }}</div>
          <div class="stat-lbl">已报价</div>
        </div>
        <div class="stat-box">
          <div class="stat-num success">{{ stats.dealCount || 0 }}</div>
          <div class="stat-lbl">已成单</div>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="编号/标题/客户/手机号" clearable style="width: 220px" size="small" @keyup.enter.native="getList(1)" />
        <el-select v-model="tableFrom.type" placeholder="需求类型" clearable style="width: 150px" size="small" class="ml10">
          <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="tableFrom.status" placeholder="状态" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="待受理" :value="0" />
          <el-option label="跟进中" :value="1" />
          <el-option label="已报价" :value="2" />
          <el-option label="已成单" :value="3" />
          <el-option label="已关闭" :value="4" />
        </el-select>
        <el-input v-model.number="tableFrom.uid" placeholder="会员UID" clearable style="width: 120px" size="small" class="ml10" />
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="需求编号" width="160">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.demandNo }}</div>
            <div class="sub">{{ scope.row.type || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户（甲方）" width="150">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.company || scope.row.contactName || '—' }}</div>
            <div class="sub">{{ scope.row.contactName }} {{ scope.row.contactPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="需求概要" min-width="200">
          <template slot-scope="scope">
            <div class="hd ell">{{ scope.row.title || '—' }}</div>
            <div class="sub ell">{{ scope.row.content || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="预算 / 期望" width="130">
          <template slot-scope="scope">
            <div class="sub">预算 {{ scope.row.budget || '待定' }}</div>
            <div class="sub">期望 {{ scope.row.hopeDate || '待定' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态 / 跟进" width="160">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ scope.row.statusText || '—' }}</el-tag>
            <div class="sub">
              报价
              <b v-if="Number(scope.row.amount) > 0" class="amount">¥{{ money(scope.row.amount) }}</b>
              <span v-else>—</span>
            </div>
            <div class="sub">{{ scope.row.follower ? `跟进：${scope.row.follower}` : '未指派跟进人' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="150" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenDetail(scope.row)">详情 / 报价</el-button>
              <el-button
                size="mini"
                plain
                class="op-tag tint-success"
                :disabled="!!scope.row.contractId"
                @click="onOpenConvert(scope.row)"
              >
                {{ scope.row.contractId ? '已转合同' : '转合同' }}
              </el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onClose(scope.row)">关闭</el-button>
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

    <!-- 详情 / 报价 -->
    <el-drawer :title="`需求详情 — ${detail.demandNo || ''}`" :visible.sync="detailShow" size="62%" append-to-body>
      <div v-loading="detailLoading" class="drawer-body">
        <!-- 概览 -->
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="状态">
            <el-tag size="mini" :type="statusType(detail.status)">{{ detail.statusText || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ fmtTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="需求类型">{{ detail.type || '—' }}</el-descriptions-item>
          <el-descriptions-item label="预算 / 期望">{{ detail.budget || '—' }} / {{ detail.hopeDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contactName || '—' }} {{ detail.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ detail.company || '—' }}</el-descriptions-item>
          <el-descriptions-item label="会员">
            {{ detail.nickname || '—' }}<span class="c-gray">（UID {{ detail.uid }}）</span>
          </el-descriptions-item>
          <el-descriptions-item label="关联合同">
            <span v-if="detail.contractNo">{{ detail.contractNo }}</span>
            <span v-else class="c-gray">未转合同</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 需求内容 -->
        <div class="grp-title">需求内容</div>
        <div class="req-title">{{ detail.title || '—' }}</div>
        <div class="req-content">{{ detail.content || '—' }}</div>
        <div v-if="detail.attachList && detail.attachList.length" class="attach-row">
          <a
            v-for="(u, i) in detail.attachList"
            :key="i"
            class="attach-chip"
            :href="fullUrl(u)"
            target="_blank"
          >📎 {{ (detail.attachNameList && detail.attachNameList[i]) || nameFromUrl(u) }}</a>
        </div>

        <!-- 报价 -->
        <div class="grp-title">报价与指派</div>
        <el-form ref="quoteForm" :model="quoteForm" :rules="quoteRules" label-width="100px" size="small">
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="报价金额" prop="amount">
                <el-input-number v-model="quoteForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="跟进人">
                <el-input v-model="quoteForm.follower" maxlength="20" placeholder="如：王小虎" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="跟进人UID">
                <el-input-number v-model="quoteForm.followerUid" :min="0" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="方案说明">
            <el-input v-model="quoteForm.quoteDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="含开发范围、交付周期、付款方式等" />
          </el-form-item>
          <el-form-item label="报价附件">
            <div class="file-row">
              <div v-if="quoteForm.quoteFile" class="file-chip">
                <span class="file-badge">{{ extOf(quoteForm.quoteName || quoteForm.quoteFile) || 'FILE' }}</span>
                <span class="file-name">{{ quoteForm.quoteName || nameFromUrl(quoteForm.quoteFile) }}</span>
                <a :href="fullUrl(quoteForm.quoteFile)" target="_blank" class="file-act">查看</a>
                <span class="file-act danger" @click="clearQuoteFile">移除</span>
              </div>
              <el-upload action :http-request="onUploadQuote" :show-file-list="false">
                <el-button size="mini" plain icon="el-icon-upload2">上传方案 / 报价单</el-button>
              </el-upload>
              <span class="c-gray">支持 PDF / Word / 图片，客户在 H5「我的需求」可见</span>
            </div>
          </el-form-item>
          <div class="quote-actions">
            <el-button size="small" :loading="saving" @click="onSaveQuote(0)">仅指派跟进人</el-button>
            <el-button size="small" type="primary" :loading="saving" @click="onSaveQuote(1)">发布报价</el-button>
          </div>
        </el-form>

        <!-- 跟进记录 -->
        <div class="grp-title">跟进记录</div>
        <div class="follow-add">
          <el-input v-model="followContent" size="small" maxlength="200" placeholder="补充一条跟进备注…" @keyup.enter.native="onAddFollow" />
          <el-button size="small" type="primary" :loading="saving" @click="onAddFollow">添加记录</el-button>
        </div>
        <el-timeline v-if="detail.followList && detail.followList.length" class="log-timeline">
          <el-timeline-item v-for="(it, i) in detail.followList" :key="i" :timestamp="it.time" placement="top">
            <b>{{ it.operator || '系统' }}</b>
            <p class="c-gray log-remark">{{ it.content }}</p>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="c-gray">暂无跟进记录</div>
      </div>
    </el-drawer>

    <!-- 转合同 -->
    <el-dialog title="需求转合同" :visible.sync="convertShow" width="760px" append-to-body top="6vh">
      <el-alert
        class="mb12"
        type="info"
        :closable="false"
        show-icon
        title="甲方信息由需求单自动带入；转出后可在「合同管理」里继续改状态、传成品件。"
      />
      <el-form ref="convertForm" :model="convertForm" :rules="convertRules" label-width="100px" size="small">
        <el-form-item label="关联需求">
          <span>{{ convertForm.demandNo }} · {{ convertForm.demandTitle }}</span>
        </el-form-item>
        <el-form-item label="归属会员">
          <span v-if="convertForm.uid">
            {{ convertForm.nickname || 'UID ' + convertForm.uid }}
            <span class="c-gray">（UID {{ convertForm.uid }}）· 客户端「我的合同」可见</span>
          </span>
          <span v-else class="c-gray">
            该需求未关联会员 —— 请先在「代客录入 / 修改需求」里补填会员UID，否则无法转合同
          </span>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="14">
            <el-form-item label="合同名称" prop="title">
              <el-input v-model="convertForm.title" maxlength="60" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="合同金额" prop="amount">
              <el-input-number v-model="convertForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="签约渠道">
          <el-radio-group v-model="convertForm.signChannel">
            <el-radio label="paper">纸质合同（线下签署）</el-radio>
            <el-radio label="electronic">电子合同（第三方平台）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row v-if="convertForm.signChannel === 'paper'" :gutter="12">
          <el-col :span="8">
            <el-form-item label="签署方式">
              <el-input v-model="convertForm.signType" maxlength="20" placeholder="如：快递往返" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="快递公司">
              <el-input v-model="convertForm.expressCo" maxlength="20" placeholder="如：顺丰速运" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="快递单号">
              <el-input v-model="convertForm.expressNo" maxlength="30" placeholder="寄出后回填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-else :gutter="12">
          <el-col :span="10">
            <el-form-item label="签约平台">
              <el-select v-model="convertForm.signPlatform" allow-create filterable default-first-option clearable style="width: 100%" placeholder="选择或手工输入">
                <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="平台编号">
              <el-input v-model="convertForm.platformNo" maxlength="60" placeholder="平台合同编号 / 存证编号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="签约状态">
              <el-select v-model="convertForm.status" style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="待签署" :value="1" />
                <el-option label="已签署" :value="2" />
                <el-option label="已生效" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="服务期限">
              <el-input v-model="convertForm.serviceTerm" maxlength="40" placeholder="如：2026-10-02 ~ 2027-10-01" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="合同正文">
          <div class="file-row">
            <div v-if="convertForm.annex" class="file-chip">
              <span class="file-badge">{{ extOf(convertForm.annexName || convertForm.annex) || 'FILE' }}</span>
              <span class="file-name">{{ convertForm.annexName || nameFromUrl(convertForm.annex) }}</span>
              <a :href="fullUrl(convertForm.annex)" target="_blank" class="file-act">查看</a>
              <span class="file-act danger" @click="clearAnnex">移除</span>
            </div>
            <el-upload action :http-request="onUploadAnnex" :show-file-list="false">
              <el-button size="mini" plain icon="el-icon-upload2">上传合同正文</el-button>
            </el-upload>
            <span class="c-gray">可稍后在「合同管理」里补传</span>
          </div>
        </el-form-item>

        <el-divider content-position="left">甲方（客户）信息</el-divider>
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="企业名称">
              <el-input v-model="convertForm.partyName" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="convertForm.custPhone" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">
          乙方（我方）信息
          <el-button type="text" size="mini" @click="saveOurDefault">存为默认</el-button>
        </el-divider>
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="企业名称">
              <el-input v-model="convertForm.ourCompany" maxlength="60" placeholder="贵州黔序科技有限公司" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="信用代码">
              <el-input v-model="convertForm.ourCode" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="法定代表人">
              <el-input v-model="convertForm.ourLegal" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="convertForm.ourPhone" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地址">
              <el-input v-model="convertForm.ourAddr" maxlength="60" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="convertShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onConfirmConvert">确认转合同</el-button>
      </div>
    </el-dialog>

    <!-- 代客录入 -->
    <el-dialog :title="editForm.id ? '修改需求' : '代客录入需求'" :visible.sync="editShow" width="620px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px" size="small">
        <el-form-item label="会员UID" prop="uid">
          <el-input-number v-model="editForm.uid" :min="1" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="10">
            <el-form-item label="需求类型">
              <el-select v-model="editForm.type" allow-create filterable default-first-option style="width: 100%">
                <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="需求标题" prop="title">
              <el-input v-model="editForm.title" maxlength="60" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="联系人">
              <el-input v-model="editForm.contactName" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.contactPhone" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司">
              <el-input v-model="editForm.company" maxlength="60" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算">
              <el-input v-model="editForm.budget" maxlength="20" placeholder="5~10 万" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="期望">
              <el-input v-model="editForm.hopeDate" maxlength="20" placeholder="2026-12-31" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="需求描述">
          <el-input v-model="editForm.content" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 160px">
            <el-option label="待受理" :value="0" />
            <el-option label="跟进中" :value="1" />
            <el-option label="已报价" :value="2" />
            <el-option label="已成单" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSaveDemand">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  svcDemandListApi,
  svcDemandInfoApi,
  svcDemandSaveApi,
  svcDemandQuoteApi,
  svcDemandFollowApi,
  svcDemandCloseApi,
  svcDemandToContractApi,
  svcDemandStatsApi,
  svcPlatformEnabledApi,
  svcContractOurDefaultApi,
  svcContractOurDefaultSaveApi
} from '@/api/enterprise';
import { fileFileApi } from '@/api/systemSetting';

export default {
  name: 'SvcDemand',
  data() {
    return {
      loading: false,
      saving: false,
      detailLoading: false,
      tableData: [],
      total: 0,
      stats: {},
      typeOptions: ['小程序', 'APP开发', '系统定制', 'UI 设计', '网站建设', '其他'],
      tableFrom: { page: 1, limit: 10, keywords: '', type: undefined, status: undefined, uid: undefined },
      platforms: [],

      detailShow: false,
      detail: {},
      quoteForm: { id: null, amount: 0, quoteDesc: '', quoteFile: '', quoteName: '', follower: '', followerUid: 0 },
      quoteRules: {},
      followContent: '',

      convertShow: false,
      convertForm: {},
      convertRules: {
        title: [{ required: true, message: '请填写合同名称', trigger: 'blur' }],
        amount: [{ required: true, message: '请填写合同金额', trigger: 'blur' }]
      },

      editShow: false,
      editForm: {},
      editRules: {
        uid: [{ required: true, message: '请填写会员UID', trigger: 'blur' }],
        title: [{ required: true, message: '请填写需求标题', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.getList();
    this.getStats();
    svcPlatformEnabledApi().then((res) => {
      this.platforms = Array.isArray(res) ? res : [];
    });
  },
  methods: {
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      svcDemandListApi(this.tableFrom)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = res.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getStats() {
      svcDemandStatsApi().then((res) => {
        this.stats = res || {};
      });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    statusType(s) {
      return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'info' }[s] || 'info';
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
    doUpload(param, cb, tip) {
      const formData = new FormData();
      formData.append('multipart', param.file);
      const loading = this.uploading(tip);
      fileFileApi(formData, { model: 'user', pid: 7 })
        .then((res) => {
          loading.close();
          const url = this.relUrl(res && (res.url || res.sattDir));
          if (!url) {
            this.$message.error('上传失败，请重试');
            return;
          }
          const name = (res && res.fileName) || (param.file && param.file.name) || this.nameFromUrl(url);
          cb(url, name);
          this.$message.success('上传成功');
        })
        .catch(() => {
          loading.close();
        });
    },

    // ---------- 详情 / 报价 ----------
    onOpenDetail(row) {
      this.detailShow = true;
      this.detailLoading = true;
      this.followContent = '';
      svcDemandInfoApi(row.id)
        .then((res) => {
          this.detail = res || {};
          this.quoteForm = {
            id: res.id,
            amount: Number(res.amount || 0),
            quoteDesc: res.quoteDesc || '',
            quoteFile: res.quoteFile || '',
            quoteName: res.quoteName || '',
            follower: res.follower || '',
            followerUid: res.followerUid || 0
          };
        })
        .finally(() => {
          this.detailLoading = false;
        });
    },
    onUploadQuote(param) {
      this.doUpload(param, (url, name) => {
        this.quoteForm.quoteFile = url;
        this.quoteForm.quoteName = name;
      }, '方案上传中...');
    },
    clearQuoteFile() {
      this.quoteForm.quoteFile = '';
      this.quoteForm.quoteName = '';
    },
    onSaveQuote(publish) {
      if (publish === 1 && !(Number(this.quoteForm.amount) > 0)) {
        this.$message.warning('请填写报价金额');
        return;
      }
      this.saving = true;
      svcDemandQuoteApi({ ...this.quoteForm, publish })
        .then(() => {
          this.$message.success(publish === 1 ? '报价已发布，客户在会员端可见' : '已指派跟进人');
          this.onOpenDetail({ id: this.quoteForm.id });
          this.getList();
          this.getStats();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    onAddFollow() {
      const content = String(this.followContent || '').trim();
      if (!content) {
        this.$message.warning('请填写跟进内容');
        return;
      }
      this.saving = true;
      svcDemandFollowApi({ id: this.detail.id, content, operator: '后台' })
        .then(() => {
          this.$message.success('已添加');
          this.followContent = '';
          this.onOpenDetail({ id: this.detail.id });
          this.getList();
          this.getStats();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    onClose(row) {
      this.$prompt('请填写关闭原因（可留空）', `关闭需求 ${row.demandNo}`, {
        inputPlaceholder: '如：客户暂时不做 / 已线下成单',
        inputValue: ''
      })
        .then(({ value }) => svcDemandCloseApi({ id: row.id, reason: value || '' }))
        .then(() => {
          this.$message.success('已关闭');
          this.getList();
          this.getStats();
        })
        .catch(() => {});
    },

    // ---------- 转合同 ----------
    onOpenConvert(row) {
      this.convertForm = {
        demandId: row.id,
        demandNo: row.demandNo,
        demandTitle: row.title,
        uid: row.uid || null,
        nickname: row.nickname || '',
        title: row.title || '',
        amount: Number(row.amount || 0),
        signChannel: 'paper',
        signType: '快递往返',
        expressCo: '',
        expressNo: '',
        signPlatform: '',
        platformNo: '',
        status: 1,
        serviceTerm: '',
        annex: '',
        annexName: '',
        partyName: row.company || row.contactName || '',
        custPhone: row.contactPhone || '',
        ourUid: 0,
        ourCompany: '',
        ourCode: '',
        ourLegal: '',
        ourPhone: '',
        ourAddr: ''
      };
      svcContractOurDefaultApi().then((res) => {
        const d = res || {};
        this.convertForm.ourUid = d.ourUid || 0;
        this.convertForm.ourCompany = d.ourCompany || '';
        this.convertForm.ourCode = d.ourCode || '';
        this.convertForm.ourLegal = d.ourLegal || '';
        this.convertForm.ourPhone = d.ourPhone || '';
        this.convertForm.ourAddr = d.ourAddr || '';
      });
      this.convertShow = true;
    },
    onUploadAnnex(param) {
      this.doUpload(param, (url, name) => {
        this.convertForm.annex = url;
        this.convertForm.annexName = name;
      }, '合同正文上传中...');
    },
    clearAnnex() {
      this.convertForm.annex = '';
      this.convertForm.annexName = '';
    },
    saveOurDefault() {
      svcContractOurDefaultSaveApi({
        ourUid: this.convertForm.ourUid,
        ourCompany: this.convertForm.ourCompany,
        ourCode: this.convertForm.ourCode,
        ourLegal: this.convertForm.ourLegal,
        ourPhone: this.convertForm.ourPhone,
        ourAddr: this.convertForm.ourAddr
      }).then(() => this.$message.success('已存为默认乙方信息'));
    },
    onConfirmConvert() {
      this.$refs.convertForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        const { demandId, ...body } = this.convertForm;
        svcDemandToContractApi(demandId, body)
          .then(() => {
            this.$message.success('已转为合同，可在「合同管理」继续跟进');
            this.convertShow = false;
            this.getList();
            this.getStats();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },

    // ---------- 代客录入 ----------
    onOpenEdit() {
      this.editForm = {
        id: null,
        uid: undefined,
        type: '小程序',
        title: '',
        content: '',
        budget: '',
        hopeDate: '',
        contactName: '',
        contactPhone: '',
        company: '',
        status: 0
      };
      this.editShow = true;
    },
    onSaveDemand() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        svcDemandSaveApi(this.editForm)
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
    }
  }
};
</script>

<style scoped lang="scss">
.filter-bar { margin: 14px 0; }
.ml10 { margin-left: 10px; }
.mb12 { margin-bottom: 12px; }
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
    &.primary { color: #1F5EFF; }
    &.purple { color: #8e6ad8; }
    &.success { color: #67c23a; }
  }
  .stat-lbl { font-size: 12.5px; color: #909399; margin-top: 2px; }
}

.hd { color: #303133; font-weight: 600; }
.sub { color: #909399; font-size: 12px; margin-top: 2px; }
.ell { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.amount { color: #303133; }

.drawer-body { padding: 0 20px 20px; }
.grp-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #303133;
  margin: 18px 0 10px;
  padding-left: 8px;
  border-left: 3px solid #1F5EFF;
}
.grp-title:first-child { margin-top: 0; }

.req-title { font-size: 14px; font-weight: 600; color: #303133; }
.req-content { font-size: 13px; color: #606266; line-height: 1.75; white-space: pre-wrap; margin-top: 4px; }
.attach-row { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
.attach-chip {
  font-size: 12.5px;
  color: #1F5EFF;
  background: #f4f6f9;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 4px 10px;
  text-decoration: none;
}

.quote-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 4px; }
.follow-add { display: flex; gap: 8px; margin-bottom: 12px; }

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
  background: #1F5EFF;
  color: #fff;
  font-size: 11px;
  border-radius: 3px;
  padding: 1px 5px;
}
.file-name { font-size: 12.5px; color: #303133; }
.file-act {
  font-size: 12.5px;
  color: #1F5EFF;
  cursor: pointer;
  text-decoration: none;
  &.danger { color: #f56c6c; }
}
.log-timeline { padding-left: 4px; }
.log-remark { margin-top: 2px; line-height: 1.6; }

::v-deep .el-drawer__body { overflow-y: auto; }
::v-deep .el-descriptions__body { background: #fafbfc; }
</style>
