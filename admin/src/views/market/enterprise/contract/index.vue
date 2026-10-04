<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar summary-bar--flex">
        <span>合同管理：合同线下签署，线上只做「登记 → 选状态 → 归档」。状态随时可在列表里改，每次变更留日志，客户在会员端只读。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">登记合同</el-button>
        </div>
      </div>

      <div class="stat-row">
        <div class="stat-box">
          <div class="stat-num warn">{{ stats.waitSignCount || 0 }}</div>
          <div class="stat-lbl">待签署</div>
        </div>
        <div class="stat-box">
          <div class="stat-num primary">{{ stats.signedCount || 0 }}</div>
          <div class="stat-lbl">已签署</div>
        </div>
        <div class="stat-box">
          <div class="stat-num success">{{ stats.effectiveCount || 0 }}</div>
          <div class="stat-lbl">已生效</div>
        </div>
        <div class="stat-box">
          <div class="stat-num">¥{{ money(stats.monthAmount) }}</div>
          <div class="stat-lbl">本月签约总额</div>
        </div>
      </div>

      <div class="filter-bar">
        <el-input v-model="tableFrom.keywords" placeholder="合同编号 / 合同名称" clearable style="width: 220px" size="small" @keyup.enter.native="getList(1)" />
        <el-select v-model="tableFrom.signChannel" placeholder="签约渠道" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="纸质合同" value="paper" />
          <el-option label="电子合同" value="electronic" />
        </el-select>
        <el-select v-model="tableFrom.status" placeholder="签约状态" clearable style="width: 130px" size="small" class="ml10">
          <el-option label="草稿" :value="0" />
          <el-option label="待签署" :value="1" />
          <el-option label="已签署" :value="2" />
          <el-option label="已生效" :value="3" />
          <el-option label="已终止" :value="-1" />
        </el-select>
        <el-input v-model.number="tableFrom.uid" placeholder="会员UID" clearable style="width: 120px" size="small" class="ml10" />
        <el-button type="primary" size="small" class="ml10" @click="getList(1)">搜索</el-button>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="合同编号" width="170">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.contractNo }}</div>
            <div class="sub ell">{{ scope.row.title || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户（甲方）" width="150">
          <template slot-scope="scope">
            <div class="hd">{{ scope.row.partyName || scope.row.nickname || '—' }}</div>
            <div class="sub">{{ scope.row.nickname || '' }} {{ scope.row.phone || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="签约渠道" width="130">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.signChannel === 'electronic' ? 'primary' : 'info'">
              {{ scope.row.channelText || '纸质合同' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签约状态" width="130">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ scope.row.statusText || '—' }}</el-tag>
            <div class="sub">{{ fmtTime(scope.row.updateTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="流水号 / 存证编号" min-width="170">
          <template slot-scope="scope">
            <div v-if="scope.row.signChannel === 'electronic'" class="sub">
              {{ scope.row.signPlatform || '—' }}
              <span class="mono">{{ scope.row.platformNo || '—' }}</span>
            </div>
            <div v-else class="sub">
              {{ scope.row.expressCo || '—' }}
              <span class="mono">{{ scope.row.expressNo || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额 / 日期" width="150">
          <template slot-scope="scope">
            <b>¥{{ money(scope.row.amount) }}</b>
            <div class="sub">{{ dateOf(scope.row) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="246" class-name="op-cell" label-class-name="op-cell" fixed="right">
          <template slot-scope="scope">
            <div class="op-grid">
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenDetail(scope.row)">详情</el-button>
              <el-button size="mini" plain class="op-tag tint-primary" @click="onOpenStatus(scope.row)">改状态</el-button>
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onOpenEdit(scope.row, true)">成品件</el-button>
              <el-button
                size="mini"
                plain
                class="op-tag tint-danger"
                :disabled="scope.row.status === -1"
                @click="onTerminate(scope.row)"
              >作废</el-button>
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

    <!-- 登记 / 编辑合同 -->
    <el-dialog :title="editForm.id ? '编辑合同' : '登记合同'" :visible.sync="editShow" width="860px" append-to-body top="5vh">
      <el-alert
        v-if="!editForm.id"
        class="mb12"
        type="info"
        :closable="false"
        show-icon
        title="合同编号自动生成。纸质合同传「定稿待打印版」，电子合同传「平台待签署版」；成品件签完再回填，不阻塞登记。"
      />
      <div class="dlg-scroll">
        <el-form ref="editForm" :model="editForm" :rules="rules" label-width="100px" size="small">
          <div class="grp-title">合同基本信息</div>
          <el-form-item v-if="editForm.demandNo" label="关联需求">
            <span>{{ editForm.demandNo }}</span>
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="14">
              <el-form-item label="合同名称" prop="title">
                <el-input v-model="editForm.title" maxlength="60" show-word-limit placeholder="如：小程序定制开发服务合同" />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="合同金额" prop="amount">
                <el-input-number v-model="editForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="14">
              <el-form-item label="服务期限">
                <el-input v-model="editForm.serviceTerm" maxlength="40" placeholder="如：2026-10-02 ~ 2027-10-01" />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="签约状态">
                <el-select v-model="editForm.status" style="width: 100%">
                  <el-option label="草稿" :value="0" />
                  <el-option label="待签署" :value="1" />
                  <el-option label="已签署" :value="2" />
                  <el-option label="已生效" :value="3" />
                  <el-option label="已终止" :value="-1" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="grp-title">甲方（客户）</div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="会员UID" prop="uid">
                <el-input-number v-model="editForm.uid" :min="1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="签约主体">
                <el-radio-group v-model="editForm.partyType">
                  <el-radio :label="1">企业</el-radio>
                  <el-radio :label="2">个人</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="主体名称" prop="partyName">
                <el-input v-model="editForm.partyName" maxlength="60" placeholder="企业名称 / 个人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="信用代码">
                <el-input v-model="editForm.custCode" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="法定代表人">
                <el-input v-model="editForm.custLegal" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="联系电话">
                <el-input v-model="editForm.custPhone" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="联系地址">
                <el-input v-model="editForm.custAddr" maxlength="120" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="grp-title">
            乙方（我方）
            <el-button type="text" size="mini" @click="saveOurDefault">存为默认</el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="企业名称">
                <el-input v-model="editForm.ourCompany" maxlength="60" placeholder="贵州黔序科技有限公司" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="信用代码">
                <el-input v-model="editForm.ourCode" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="法定代表人">
                <el-input v-model="editForm.ourLegal" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话">
                <el-input v-model="editForm.ourPhone" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地址">
                <el-input v-model="editForm.ourAddr" maxlength="120" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="grp-title">合同正文</div>
          <el-form-item label="正文文件">
            <div class="file-row">
              <div v-if="editForm.annex" class="file-chip">
                <span class="file-badge">{{ extOf(editForm.annexName || editForm.annex) || 'FILE' }}</span>
                <span class="file-name">{{ editForm.annexName || nameFromUrl(editForm.annex) }}</span>
                <a :href="fullUrl(editForm.annex)" target="_blank" class="file-act">查看</a>
                <span class="file-act danger" @click="clearAnnex">移除</span>
              </div>
              <el-upload action :http-request="onUploadAnnex" :show-file-list="false">
                <el-button size="mini" plain icon="el-icon-upload2">上传合同正文</el-button>
              </el-upload>
              <span class="c-gray">支持 PDF / Word / Excel / 图片</span>
            </div>
          </el-form-item>

          <div class="grp-title">签约渠道</div>
          <el-form-item label="渠道">
            <el-radio-group v-model="editForm.signChannel">
              <el-radio label="paper">纸质合同（线下签署）</el-radio>
              <el-radio label="electronic">电子合同（第三方平台）</el-radio>
            </el-radio-group>
          </el-form-item>

          <div v-if="editForm.signChannel === 'paper'" class="channel-box paper">
            <div class="channel-hd">纸质合同 · 登记字段</div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="签署方式">
                  <el-select v-model="editForm.signType" allow-create filterable default-first-option style="width: 100%">
                    <el-option label="快递往返" value="快递往返" />
                    <el-option label="当面签署" value="当面签署" />
                    <el-option label="扫描件回传" value="扫描件回传" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="打印份数">
                  <el-input v-model="editForm.printCount" maxlength="10" placeholder="如：贰 份" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="寄出日期">
                  <el-date-picker v-model="editForm.sentDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="快递公司">
                  <el-input v-model="editForm.expressCo" maxlength="20" placeholder="如：顺丰速运" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="寄出单号">
                  <el-input v-model="editForm.expressNo" maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="回寄单号">
                  <el-input v-model="editForm.expressNoBack" maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="回寄签收日">
                  <el-date-picker v-model="editForm.backDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div v-else class="channel-box elec">
            <div class="channel-hd">电子合同 · 登记字段</div>
            <el-row :gutter="12">
              <el-col :span="10">
                <el-form-item label="签约平台">
                  <el-select v-model="editForm.signPlatform" allow-create filterable default-first-option clearable style="width: 100%" placeholder="选择或手工输入">
                    <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.name" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="7">
                <el-form-item label="发起日期">
                  <el-date-picker v-model="editForm.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="7">
                <el-form-item label="平台编号">
                  <el-input v-model="editForm.platformNo" maxlength="60" placeholder="合同/存证编号" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="查看链接">
                  <el-input v-model="editForm.platformUrl" maxlength="300" placeholder="平台上该合同的查看地址" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="grp-title">成品件<span class="grp-tip">（签完再回填，不阻塞登记）</span></div>
          <el-form-item label="成品合同">
            <div class="file-row">
              <div v-if="editForm.signedFile" class="file-chip">
                <span class="file-badge">{{ extOf(editForm.signedFileName || editForm.signedFile) || 'FILE' }}</span>
                <span class="file-name">{{ editForm.signedFileName || nameFromUrl(editForm.signedFile) }}</span>
                <a :href="fullUrl(editForm.signedFile)" target="_blank" class="file-act">查看</a>
                <span class="file-act danger" @click="clearSignedFile">移除</span>
              </div>
              <el-upload action :http-request="onUploadSigned" :show-file-list="false">
                <el-button size="mini" plain icon="el-icon-upload2">上传成品合同</el-button>
              </el-upload>
              <span class="c-gray">双方签署完成的纸质扫描件 / 平台导出件</span>
            </div>
          </el-form-item>
          <el-form-item v-if="editForm.signChannel === 'electronic'" label="存证报告">
            <div class="file-row">
              <div v-if="editForm.voucherFile" class="file-chip">
                <span class="file-badge">{{ extOf(editForm.voucherName || editForm.voucherFile) || 'FILE' }}</span>
                <span class="file-name">{{ editForm.voucherName || nameFromUrl(editForm.voucherFile) }}</span>
                <a :href="fullUrl(editForm.voucherFile)" target="_blank" class="file-act">查看</a>
                <span class="file-act danger" @click="clearVoucher">移除</span>
              </div>
              <el-upload action :http-request="onUploadVoucher" :show-file-list="false">
                <el-button size="mini" plain icon="el-icon-upload2">上传存证报告</el-button>
              </el-upload>
              <span class="c-gray">仅电子渠道需要</span>
            </div>
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="editForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="记入合同日志，客户在会员端可见" />
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </el-dialog>

    <!-- 变更签约状态 -->
    <el-dialog title="变更签约状态" :visible.sync="statusShow" width="600px" append-to-body>
      <div class="cur-box">
        <div class="hd">{{ statusRow.contractNo }}</div>
        <div class="sub">{{ statusRow.title }} · {{ statusRow.partyName || statusRow.nickname || '' }}</div>
        <div class="cur-foot">
          <el-tag size="mini" :type="statusRow.signChannel === 'electronic' ? 'primary' : 'info'">
            {{ statusRow.channelText || '纸质合同' }}
          </el-tag>
          <el-tag size="mini" class="ml10" :type="statusType(statusRow.status)">
            当前：{{ statusRow.statusText }}
          </el-tag>
        </div>
      </div>

      <el-form ref="statusForm" :model="statusForm" :rules="statusRules" label-width="100px" size="small">
        <el-form-item label="变更为" prop="status">
          <el-select v-model="statusForm.status" style="width: 220px">
            <el-option label="草稿" :value="0" />
            <el-option label="待签署" :value="1" />
            <el-option label="已签署" :value="2" />
            <el-option label="已生效" :value="3" />
            <el-option label="已终止" :value="-1" />
          </el-select>
          <div class="st-hint">{{ statusHint }}</div>
        </el-form-item>
        <el-form-item label="经办人">
          <el-input v-model="statusForm.operator" maxlength="20" style="width: 220px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="statusForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="会记入合同日志，并显示在客户会员端的合同详情里"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="statusShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSaveStatus">确认变更</el-button>
      </div>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer :title="`合同详情 — ${detail.contractNo || ''}`" :visible.sync="detailShow" size="60%" append-to-body>
      <div v-loading="detailLoading" class="drawer-body">
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="合同名称">{{ detail.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="合同金额">
            <b>¥{{ money(detail.amount) }}</b>
          </el-descriptions-item>
          <el-descriptions-item label="签约状态">
            <el-tag size="mini" :type="statusType(detail.status)">{{ detail.statusText || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="签约渠道">{{ detail.channelText || '纸质合同' }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.partyName || detail.nickname || '—' }}</el-descriptions-item>
          <el-descriptions-item label="会员">
            {{ detail.nickname || '—' }}<span class="c-gray">（UID {{ detail.uid }}）</span>
          </el-descriptions-item>
          <el-descriptions-item label="服务期限">{{ detail.serviceTerm || '—' }}</el-descriptions-item>
          <el-descriptions-item label="关联需求">{{ detail.demandNo || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="grp-title">签约信息</div>
        <el-descriptions :column="2" size="small" border>
          <template v-if="detail.signChannel === 'electronic'">
            <el-descriptions-item label="签约平台">{{ detail.signPlatform || '—' }}</el-descriptions-item>
            <el-descriptions-item label="平台编号">{{ detail.platformNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发起日期">{{ detail.startDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="查看链接">
              <a v-if="detail.platformUrl" :href="fullUrl(detail.platformUrl)" target="_blank">打开平台</a>
              <span v-else>—</span>
            </el-descriptions-item>
          </template>
          <template v-else>
            <el-descriptions-item label="签署方式">{{ detail.signType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="打印份数">{{ detail.printCount || '—' }}</el-descriptions-item>
            <el-descriptions-item label="快递公司">{{ detail.expressCo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="寄出单号">{{ detail.expressNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="寄出日期">{{ detail.sentDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="回寄单号">{{ detail.expressNoBack || '—' }}</el-descriptions-item>
            <el-descriptions-item label="回寄签收日">{{ detail.backDate || '—' }}</el-descriptions-item>
          </template>
        </el-descriptions>

        <div class="grp-title">甲方（客户）</div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="主体类型">{{ detail.partyType === 1 ? '企业' : '个人' }}</el-descriptions-item>
          <el-descriptions-item label="主体名称">{{ detail.partyName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="信用代码">{{ detail.custCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ detail.custLegal || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.custPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系地址">{{ detail.custAddr || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="grp-title">乙方（我方）</div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="企业名称">{{ detail.ourCompany || '—' }}</el-descriptions-item>
          <el-descriptions-item label="信用代码">{{ detail.ourCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ detail.ourLegal || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.ourPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.ourAddr || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="grp-title">合同文件</div>
        <div class="file-list">
          <div class="file-line">
            <span class="file-badge">正文</span>
            <template v-if="detail.annex">
              <span class="file-name">{{ detail.annexName || nameFromUrl(detail.annex) }}</span>
              <a :href="fullUrl(detail.annex)" target="_blank" class="file-act">查看</a>
            </template>
            <span v-else class="c-gray">未上传</span>
          </div>
          <div class="file-line">
            <span class="file-badge">成品件</span>
            <template v-if="detail.signedFile">
              <span class="file-name">{{ detail.signedFileName || nameFromUrl(detail.signedFile) }}</span>
              <a :href="fullUrl(detail.signedFile)" target="_blank" class="file-act">查看</a>
            </template>
            <span v-else class="c-gray">未生成（签署完成后回填）</span>
          </div>
          <div v-if="detail.signChannel === 'electronic'" class="file-line">
            <span class="file-badge">存证报告</span>
            <template v-if="detail.voucherFile">
              <span class="file-name">{{ detail.voucherName || nameFromUrl(detail.voucherFile) }}</span>
              <a :href="fullUrl(detail.voucherFile)" target="_blank" class="file-act">查看</a>
            </template>
            <span v-else class="c-gray">未上传</span>
          </div>
        </div>

        <div v-if="detail.project" class="grp-title">关联项目进度</div>
        <div v-if="detail.project" class="proj-mini">
          <div class="proj-hd">
            <span class="proj-name">{{ detail.project.name }}</span>
            <el-tag size="mini">{{ detail.project.statusText || '—' }}</el-tag>
          </div>
          <el-progress :percentage="Number(detail.project.percent) || 0" :stroke-width="8" />
          <div class="proj-meta">{{ detail.project.stage || '—' }} · 进度 {{ detail.project.percent || 0 }}%</div>
        </div>

        <div class="grp-title">状态记录</div>
        <el-timeline v-if="detail.statusLogList && detail.statusLogList.length" class="log-timeline">
          <el-timeline-item v-for="(it, i) in detail.statusLogList" :key="i" :timestamp="it.time" placement="top">
            <b>{{ it.statusName }}</b>
            <span class="c-gray ml10">{{ it.operator }}</span>
            <p v-if="it.remark" class="c-gray log-remark">{{ it.remark }}</p>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="c-gray">暂无状态记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  svcContractListApi,
  svcContractInfoApi,
  svcContractSaveApi,
  svcContractStatusSaveApi,
  svcContractStatsApi,
  svcContractOurDefaultApi,
  svcContractOurDefaultSaveApi,
  svcPlatformEnabledApi
} from '@/api/enterprise';
import { fileFileApi } from '@/api/systemSetting';

export default {
  name: 'SvcContract',
  data() {
    return {
      loading: false,
      saving: false,
      detailLoading: false,
      tableData: [],
      total: 0,
      stats: {},
      platforms: [],
      tableFrom: { page: 1, limit: 10, keywords: '', signChannel: undefined, status: undefined, uid: undefined },

      editShow: false,
      editForm: this.emptyForm(),
      rules: {
        uid: [{ required: true, message: '请填写会员UID', trigger: 'blur' }],
        title: [{ required: true, message: '请填写合同名称', trigger: 'blur' }],
        partyName: [{ required: true, message: '请填写甲方主体名称', trigger: 'blur' }]
      },

      statusShow: false,
      statusRow: {},
      statusForm: { id: null, status: 1, operator: '', remark: '' },
      statusRules: {
        status: [{ required: true, message: '请选择要变更的状态', trigger: 'change' }]
      },
      statusHints: {
        0: '刚登记，还没定稿。可随意改，不算正式合同。',
        1: '定稿了，等线下签署（纸质等回寄 / 平台等回传）。',
        2: '双方已签字盖章，原件在途或已回寄。成品件可以传上来了。',
        3: '已签收归档，正式生效。将自动建立对应项目，客户可在会员端看进度。',
        '-1': '作废或中止，不再推进。记录保留，不再出现在待办统计里。'
      },

      detailShow: false,
      detail: {}
    };
  },
  computed: {
    statusHint() {
      return this.statusHints[this.statusForm.status] || '';
    }
  },
  mounted() {
    this.getList();
    this.getStats();
    svcPlatformEnabledApi().then((res) => {
      this.platforms = Array.isArray(res) ? res : [];
    });
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        demandNo: '',
        uid: undefined,
        title: '',
        amount: 0,
        partyType: 1,
        partyName: '',
        custCode: '',
        custLegal: '',
        custPhone: '',
        custAddr: '',
        annex: '',
        annexName: '',
        signChannel: 'paper',
        signType: '快递往返',
        printCount: '',
        expressCo: '',
        expressNo: '',
        expressNoBack: '',
        sentDate: '',
        backDate: '',
        signPlatform: '',
        platformNo: '',
        platformUrl: '',
        startDate: '',
        voucherFile: '',
        voucherName: '',
        signedFile: '',
        signedFileName: '',
        serviceTerm: '',
        status: 1,
        remark: '',
        ourUid: 0,
        ourCompany: '',
        ourCode: '',
        ourLegal: '',
        ourPhone: '',
        ourAddr: ''
      };
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
    getStats() {
      svcContractStatsApi().then((res) => {
        this.stats = res || {};
      });
    },
    onPageChange(p) {
      this.tableFrom.page = p;
      this.getList();
    },
    statusType(s) {
      return { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', '-1': 'danger' }[s] || 'info';
    },
    money(v) {
      const n = Number(v || 0);
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    dateOf(row) {
      if (row.signChannel === 'electronic') return row.startDate ? `发起 ${row.startDate}` : '—';
      return row.sentDate ? `寄出 ${row.sentDate}` : '—';
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
    doUpload(param, cb, tip) {
      const formData = new FormData();
      formData.append('multipart', param.file);
      const loading = this.$loading({
        lock: true,
        text: tip || '上传中，请稍候...',
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
          cb(url, name);
          this.$message.success('上传成功');
        })
        .catch(() => {
          loading.close();
        });
    },
    onUploadAnnex(param) {
      this.doUpload(param, (url, name) => {
        this.editForm.annex = url;
        this.editForm.annexName = name;
      });
    },
    onUploadSigned(param) {
      this.doUpload(param, (url, name) => {
        this.editForm.signedFile = url;
        this.editForm.signedFileName = name;
      });
    },
    onUploadVoucher(param) {
      this.doUpload(param, (url, name) => {
        this.editForm.voucherFile = url;
        this.editForm.voucherName = name;
      });
    },
    clearAnnex() {
      this.editForm.annex = '';
      this.editForm.annexName = '';
    },
    clearSignedFile() {
      this.editForm.signedFile = '';
      this.editForm.signedFileName = '';
    },
    clearVoucher() {
      this.editForm.voucherFile = '';
      this.editForm.voucherName = '';
    },

    onOpenEdit(row, focusSigned) {
      if (row) {
        this.editForm = Object.assign(this.emptyForm(), {
          id: row.id,
          uid: row.uid,
          title: row.title || '',
          amount: Number(row.amount || 0),
          partyType: row.partyType || 1,
          partyName: row.partyName || '',
          custCode: row.custCode || '',
          custLegal: row.custLegal || '',
          custPhone: row.custPhone || '',
          custAddr: row.custAddr || '',
          annex: row.annex || '',
          annexName: row.annexName || '',
          signChannel: row.signChannel || 'paper',
          signType: row.signType || '',
          printCount: row.printCount || '',
          expressCo: row.expressCo || '',
          expressNo: row.expressNo || '',
          expressNoBack: row.expressNoBack || '',
          sentDate: row.sentDate ? String(row.sentDate).slice(0, 10) : '',
          backDate: row.backDate ? String(row.backDate).slice(0, 10) : '',
          signPlatform: row.signPlatform || '',
          platformNo: row.platformNo || '',
          platformUrl: row.platformUrl || '',
          startDate: row.startDate ? String(row.startDate).slice(0, 10) : '',
          voucherFile: row.voucherFile || '',
          voucherName: row.voucherName || '',
          signedFile: row.signedFile || '',
          signedFileName: row.signedFileName || '',
          serviceTerm: row.serviceTerm || '',
          status: row.status,
          remark: '',
          ourUid: row.ourUid || 0,
          ourCompany: row.ourCompany || '',
          ourCode: row.ourCode || '',
          ourLegal: row.ourLegal || '',
          ourPhone: row.ourPhone || '',
          ourAddr: row.ourAddr || ''
        });
        this.editShow = true;
        if (focusSigned) {
          this.$message.info('请在「成品件」区上传双方签署完成的文件，然后保存');
        }
        return;
      }
      this.editForm = this.emptyForm();
      svcContractOurDefaultApi().then((res) => {
        const d = res || {};
        this.editForm.ourUid = d.ourUid || 0;
        this.editForm.ourCompany = d.ourCompany || '';
        this.editForm.ourCode = d.ourCode || '';
        this.editForm.ourLegal = d.ourLegal || '';
        this.editForm.ourPhone = d.ourPhone || '';
        this.editForm.ourAddr = d.ourAddr || '';
      });
      this.editShow = true;
    },
    saveOurDefault() {
      svcContractOurDefaultSaveApi({
        ourUid: this.editForm.ourUid,
        ourCompany: this.editForm.ourCompany,
        ourCode: this.editForm.ourCode,
        ourLegal: this.editForm.ourLegal,
        ourPhone: this.editForm.ourPhone,
        ourAddr: this.editForm.ourAddr
      }).then(() => this.$message.success('已存为默认乙方信息'));
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
            this.getStats();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },

    onOpenStatus(row) {
      this.statusRow = row;
      this.statusForm = {
        id: row.id,
        status: row.status,
        operator: '',
        remark: ''
      };
      this.statusShow = true;
    },
    onSaveStatus() {
      this.$refs.statusForm.validate((valid) => {
        if (!valid) return;
        if (this.statusForm.status === this.statusRow.status) {
          this.$message.warning('状态没有变化');
          return;
        }
        this.saving = true;
        svcContractStatusSaveApi(this.statusForm)
          .then(() => {
            this.$message.success('状态已变更，客户在会员端可见');
            this.statusShow = false;
            this.getList();
            this.getStats();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onTerminate(row) {
      this.$confirm(`确认将合同「${row.contractNo}」置为已终止（作废）？记录会保留，但不再出现在待办统计里。`, '提示', {
        type: 'warning'
      })
        .then(() =>
          svcContractStatusSaveApi({ id: row.id, status: -1, operator: '', remark: '后台作废' })
        )
        .then(() => {
          this.$message.success('已作废');
          this.getList();
          this.getStats();
        })
        .catch(() => {});
    },

    onOpenDetail(row) {
      this.detailShow = true;
      this.detailLoading = true;
      svcContractInfoApi(row.id)
        .then((res) => {
          this.detail = res || {};
        })
        .finally(() => {
          this.detailLoading = false;
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
.mono { font-family: Consolas, Monaco, monospace; }

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
    &.success { color: #67c23a; }
  }
  .stat-lbl { font-size: 12.5px; color: #909399; margin-top: 2px; }
}

.hd { color: #303133; font-weight: 600; }
.sub { color: #909399; font-size: 12px; margin-top: 2px; }
.ell { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.dlg-scroll { max-height: 70vh; overflow-y: auto; padding-right: 6px; }
.grp-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 10px;
  padding-left: 8px;
  border-left: 3px solid #1F5EFF;
  .grp-tip { font-weight: 400; color: #909399; font-size: 12px; margin-left: 6px; }
}
.grp-title:first-child { margin-top: 0; }

.channel-box {
  border-radius: 8px;
  padding: 14px 16px 0;
  margin-bottom: 4px;
  &.paper { background: #f6f7f9; border: 1px solid #e3e6eb; }
  &.elec { background: #f2f7ff; border: 1px solid #d6e6fb; }
  .channel-hd { font-size: 12.5px; font-weight: 600; color: #606266; margin-bottom: 12px; }
}

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

.cur-box {
  background: #f7f8fa;
  border-radius: 6px;
  padding: 12px 14px;
  margin-bottom: 16px;
  .cur-foot { margin-top: 8px; }
}
.st-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.7;
  margin-top: 6px;
}

.drawer-body { padding: 0 20px 20px; }
.file-list { display: flex; flex-direction: column; gap: 8px; }
.file-line { display: flex; align-items: center; gap: 8px; }
.proj-mini {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px 14px;
  .proj-hd { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
  .proj-name { font-weight: 600; color: #303133; }
  .proj-meta { font-size: 12px; color: #909399; margin-top: 6px; }
}
.log-timeline { padding-left: 4px; }
.log-remark { margin-top: 2px; line-height: 1.6; }

::v-deep .el-drawer__body { overflow-y: auto; }
::v-deep .el-descriptions__body { background: #fafbfc; }
</style>
