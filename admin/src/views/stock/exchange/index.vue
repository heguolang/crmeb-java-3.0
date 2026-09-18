<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 150px" @change="getList">
              <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 换货单卡片列表 -->
      <div v-loading="loading" class="order-list">
        <div v-for="row in tableData" :key="row.id" class="order-card">
          <!-- 卡片头：单号 + 时间 + 状态 -->
          <div class="card-head">
            <span class="head-label">换货单号：</span>
            <span class="head-no">{{ row.exchangeNo }}</span>
            <span class="head-time">{{ shortTime(row.createTime) }}</span>
            <span class="mini-chip" :class="{ 'is-virtual': row.stockType === 2 }">{{ row.stockType === 2 ? '虚拟库存' : '实体库存' }}</span>
            <span class="head-status" :class="statusClass(row.status)">{{ statusMap[row.status] || row.status }}</span>
            <span v-if="row.status === -1 && row.rejectReason" class="head-reason" :title="row.rejectReason">（{{ row.rejectReason }}）</span>
          </div>
          <!-- 卡片体：商品 | 收货 | 代理 | 退回/新发快递 | 操作 -->
          <div class="card-body">
            <div class="col col-goods">
              <div class="goods-item">
                <img v-if="row.productImage" :src="row.productImage" class="goods-img" />
                <span v-else class="goods-img goods-img-empty">{{ (row.productName || '?').slice(0, 1) }}</span>
                <div class="goods-info">
                  <div class="goods-name" :title="row.productName">{{ row.productName }}</div>
                  <div class="goods-num">共 {{ row.num }} 件<span v-if="row.reason" class="goods-reason"> · {{ row.reason }}</span></div>
                </div>
              </div>
            </div>
            <div class="col col-recv">
              <template v-if="row.realName">
                <div class="recv-name">{{ row.realName }}<span class="sub-text recv-phone">{{ row.phone }}</span></div>
                <div class="recv-addr" :title="row.userAddress">{{ row.userAddress || '—' }}</div>
              </template>
              <div v-else class="sub-text">无需收货信息</div>
            </div>
            <div class="col col-agent">
              <div class="agent-line">
                <span class="avatar">{{ (row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
                <div class="agent-text">
                  <div class="agent-name" :title="row.nickname">{{ row.nickname }}</div>
                  <div class="sub-text ellipsis" :title="'原单 ' + row.orderNo">原单 {{ row.orderNo }}</div>
                </div>
              </div>
            </div>
            <div class="col col-express">
              <template v-if="row.backExpressNum || row.newExpressNum">
                <div v-if="row.backExpressNum" class="express-line">退：{{ row.backExpressName }} {{ row.backExpressNum }}</div>
                <div v-if="row.newExpressNum" class="express-line">新：{{ row.newExpressName }} {{ row.newExpressNum }}</div>
              </template>
              <span v-else class="sub-text">暂无快递信息</span>
            </div>
            <div class="col col-ops">
              <el-button v-if="row.status === 0 && checkPermi(['admin:stock:exchange:audit'])" size="mini" type="warning" plain class="op-btn" @click="audit(row)">介入审核</el-button>
              <el-button v-if="row.status === 1 && checkPermi(['admin:stock:exchange:audit'])" size="mini" type="warning" plain class="op-btn" @click="audit(row)">总部审核</el-button>
              <el-button v-if="row.status === 2 && checkPermi(['admin:stock:exchange:back'])" size="mini" type="success" plain class="op-btn" @click="confirmBack(row)">旧品入库</el-button>
              <el-button v-if="row.status === 3 && checkPermi(['admin:stock:exchange:send'])" size="mini" type="primary" plain class="op-btn" @click="openSend(row)">发新品</el-button>
            </div>
          </div>
        </div>

        <div v-if="!tableData.length && !loading" class="empty-tip">暂无换货单</div>
      </div>

      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <el-dialog title="换货审核" :visible.sync="auditVisible" width="480px">
      <!-- 换货单信息卡 -->
      <div v-if="auditRow" class="audit-info">
        <div class="ai-row"><span class="ai-label">换货单号</span><span class="ai-value">{{ auditRow.exchangeNo }}</span></div>
        <div class="ai-row"><span class="ai-label">商品</span><span class="ai-value">{{ auditRow.productName }} × {{ auditRow.num }}</span></div>
        <div class="ai-row"><span class="ai-label">代理</span><span class="ai-value">{{ auditRow.nickname }}</span></div>
        <div class="ai-row"><span class="ai-label">换货原因</span><span class="ai-value ai-reason" :title="auditRow.reason">{{ auditRow.reason || '—' }}</span></div>
      </div>
      <el-alert v-if="auditRow && auditRow.status === 0" type="warning" :closable="false" style="margin-top: 12px"
                title="该换货单尚在【待上级审核】阶段，总部介入后将跳过上级审核，直接进入待旧品退回" />

      <!-- 审核结果选择卡 -->
      <div class="audit-types">
        <div class="audit-type" :class="{ active: auditForm.status === 1 }" @click="auditForm.status = 1">
          <i class="el-icon-circle-check st-icon"></i>
          <div class="st-name">通过</div>
          <div class="st-desc">{{ auditRow && auditRow.status === 0 ? '跳过上级，直接待旧品退回' : '进入待旧品退回' }}</div>
        </div>
        <div class="audit-type reject" :class="{ active: auditForm.status === -1 }" @click="auditForm.status = -1">
          <i class="el-icon-circle-close st-icon"></i>
          <div class="st-name">驳回</div>
          <div class="st-desc">终止换货，需填原因</div>
        </div>
      </div>

      <el-form v-if="auditForm.status === -1" size="small" label-width="80px" style="margin-top: 14px">
        <el-form-item label="驳回原因">
          <el-input v-model="auditForm.reason" type="textarea" :rows="2" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="auditVisible = false">取消</el-button>
        <el-button size="small" :type="auditForm.status === -1 ? 'danger' : 'primary'" @click="saveAudit">{{ auditForm.status === -1 ? '确认驳回' : '确认通过' }}</el-button>
      </div>
    </el-dialog>

    <el-dialog title="发新品" :visible.sync="sendVisible" width="520px">
      <!-- 收货信息卡 -->
      <div v-if="sendRow" class="ship-recipient">
        <i class="el-icon-location-outline rc-icon"></i>
        <div class="rc-main">
          <div class="rc-line1">
            <b>{{ sendRow.realName || '-' }}</b>
            <span class="rc-phone">{{ sendRow.phone || '-' }}</span>
          </div>
          <div class="rc-addr" :title="sendRow.userAddress">{{ sendRow.userAddress || '-' }}</div>
        </div>
        <el-button size="mini" plain icon="el-icon-document-copy" class="rc-copy" @click="copyRecipient">复制</el-button>
      </div>
      <div v-if="sendRow" class="ship-goods">
        <span class="sub-text">新品：{{ sendRow.targetProductName || sendRow.productName }} × {{ sendRow.num }}</span>
      </div>

      <el-form label-width="90px" size="small" style="margin-top: 16px">
        <el-form-item label="快递公司"><el-input v-model="sendForm.expressName" placeholder="请输入快递公司" /></el-form-item>
        <el-form-item label="快递单号"><el-input v-model="sendForm.expressNum" placeholder="请输入快递单号" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="sendVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveSend">确定发货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockExchangeListApi, stockExchangeAuditApi, stockExchangeBackApi, stockExchangeSendApi } from '@/api/stock';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'StockExchange',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, status: null },
      statusMap: { 0: '待上级审核', 1: '待总部审核', 2: '待旧品退回', 3: '待发新品', 4: '已完成', '-1': '已驳回' },
      auditVisible: false,
      auditRow: null,
      auditForm: { status: 1, reason: '' },
      sendVisible: false,
      sendRow: null,
      sendForm: { expressName: '', expressNum: '' }
    };
  },
  methods: {
    checkPermi,
    getList() {
      this.loading = true;
      stockExchangeListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, status: null };
      this.getList();
    },
    // 状态配色：待审核橙、待退回/待发新蓝、已完成绿、已驳回红
    statusClass(s) {
      return { 0: 'is-warn', 1: 'is-warn', 2: 'is-info', 3: 'is-info', 4: 'is-ok', '-1': 'is-danger' }[s] || '';
    },
    shortTime(t) {
      if (!t) return '-';
      return String(t).substring(0, 16);
    },
    audit(row) {
      this.auditRow = row;
      this.auditForm = { status: 1, reason: '' };
      this.auditVisible = true;
    },
    saveAudit() {
      if (this.auditForm.status === -1 && !this.auditForm.reason) return this.$message.error('请填写驳回原因');
      const tip = this.auditRow.status === 0 ? '总部介入审核后将跳过上级审核，确认？' : '确认提交审核结果？';
      this.$confirm(tip, '换货审核').then(() => {
        stockExchangeAuditApi(this.auditRow.id, this.auditForm).then(() => {
          this.$message.success('已审核');
          this.auditVisible = false;
          this.getList();
        });
      }).catch(() => {});
    },
    confirmBack(row) {
      this.$confirm('确认旧品已核验入库？入库后将自动回补云仓库存。', '提示').then(() => {
        stockExchangeBackApi(row.id).then(() => {
          this.$message.success('已入库，库存已回补');
          this.getList();
        });
      }).catch(() => {});
    },
    openSend(row) {
      this.sendRow = row;
      this.sendForm = { expressName: '', expressNum: '' };
      this.sendVisible = true;
    },
    // 复制收货信息（姓名 + 电话 + 地址）
    copyRecipient() {
      const row = this.sendRow || {};
      const text = [row.realName, row.phone, row.userAddress].filter(Boolean).join(' ');
      if (!text) {
        this.$message.warning('暂无收货信息可复制');
        return;
      }
      const input = document.createElement('textarea');
      input.value = text;
      input.style.position = 'fixed';
      input.style.left = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        this.$message.success('收货信息已复制');
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
    saveSend() {
      if (!this.sendForm.expressName || !this.sendForm.expressNum) return this.$message.error('请填写快递信息');
      stockExchangeSendApi(this.sendRow.id, this.sendForm).then(() => {
        this.$message.success('新品已发出，库存已扣减');
        this.sendVisible = false;
        this.getList();
      });
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.sub-text {
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 换货单卡片列表（与订货订单同款） ===== */
.order-list {
  min-height: 120px;
}
.order-card {
  margin-bottom: 14px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}
.order-card:hover {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

/* 卡片头 */
.card-head {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 40px;
  background: #f0f4fb;
  font-size: 13px;
}
.head-label {
  color: #606266;
}
.head-no {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.head-time {
  margin-left: 14px;
  color: #909399;
}
.head-status {
  margin-left: 14px;
  font-weight: 600;
  font-size: 14px;
}
.head-status.is-warn { color: #ff9900; }
.head-status.is-info { color: #409eff; }
.head-status.is-ok { color: #19be6b; }
.head-status.is-danger { color: #f56c6c; }
/* 库存类型小标签：实体蓝灰、虚拟橙 */
.mini-chip {
  margin-left: 10px;
  padding: 0 8px;
  border-radius: 3px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  color: #409eff;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}
.mini-chip.is-virtual {
  background: #fdf6ec;
  border-color: #f5dab1;
  color: #e6a23c;
}
.head-reason {
  color: #f56c6c;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片体 */
.card-body {
  display: grid;
  grid-template-columns: minmax(260px, 1.4fr) 210px 200px 210px 112px;
  align-items: center;
  padding: 16px 0;
}
.col {
  padding: 4px 16px;
  min-width: 0;
  align-self: center;
}
.col + .col {
  border-left: 1px solid #f2f4f7;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 商品列：缩略图 + 名称 + 件数/原因 */
.goods-item {
  display: flex;
  align-items: center;
}
.goods-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
  background: #f5f7fa;
  margin-right: 10px;
}
.goods-img-empty {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 16px;
}
.goods-info {
  min-width: 0;
}
.goods-name {
  font-size: 14px;
  color: #303133;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-num {
  font-size: 13px;
  color: #909399;
  line-height: 20px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 收货列 */
.recv-name {
  font-size: 14px;
  color: #303133;
  line-height: 22px;
}
.recv-phone {
  margin-left: 8px;
}
.recv-addr {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 代理列 */
.agent-line {
  display: flex;
  align-items: center;
}
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  flex-shrink: 0;
}
.agent-text {
  min-width: 0;
}
.agent-name {
  font-size: 14px;
  color: #303133;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 原因列 */
.reason-text {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 快递列 */
.express-line {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 操作列：按钮竖排 */
.col-ops {
  align-items: center;
}
.op-btn {
  width: 92px;
  margin: 4px 0 !important;
  margin-left: 0 !important;
  display: block;
}
.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 32px 0;
}

/* ===== 发新品弹窗：收货信息卡（与订货订单发货弹窗同款） ===== */
.ship-goods {
  margin-top: 10px;
}
.ship-recipient {
  display: flex;
  align-items: flex-start;
  padding: 12px 14px;
  background: #f0f4fb;
  border-radius: 6px;
}
.rc-icon {
  font-size: 18px;
  color: #409eff;
  margin: 2px 10px 0 0;
}
.rc-main {
  flex: 1;
  min-width: 0;
}
.rc-line1 {
  font-size: 13px;
  color: #303133;
  line-height: 20px;
}
.rc-phone {
  margin-left: 8px;
  color: #909399;
}
.rc-addr {
  font-size: 12px;
  color: #606266;
  line-height: 18px;
  margin-top: 2px;
}
.rc-copy {
  flex-shrink: 0;
  margin-left: 12px;
}

/* ===== 换货审核弹窗 ===== */
/* 单据信息卡 */
.audit-info {
  padding: 12px 14px;
  background: #f0f4fb;
  border-radius: 6px;
}
.ai-row {
  display: flex;
  font-size: 13px;
  line-height: 24px;
}
.ai-label {
  flex-shrink: 0;
  width: 70px;
  color: #909399;
}
.ai-value {
  flex: 1;
  min-width: 0;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ai-reason {
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 审核结果选择卡 */
.audit-types {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.audit-type {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
}
.audit-type:hover {
  border-color: #c6e2ff;
}
.audit-type.active {
  border-color: #409eff;
  background: #f0f7ff;
}
.audit-type.reject.active {
  border-color: #f56c6c;
  background: #fef4f4;
}
.st-icon {
  font-size: 22px;
  color: #909399;
}
.audit-type.active .st-icon {
  color: #409eff;
}
.audit-type.reject.active .st-icon {
  color: #f56c6c;
}
.st-name {
  margin-top: 6px;
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}
.audit-type.active .st-name {
  color: #409eff;
}
.audit-type.reject.active .st-name {
  color: #f56c6c;
}
.st-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
