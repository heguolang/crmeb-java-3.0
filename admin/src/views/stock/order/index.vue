<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="订单号">
            <el-input v-model="tableFrom.orderNo" placeholder="订货单号" clearable style="width: 190px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="用户UID">
            <el-input v-model.number="tableFrom.uid" placeholder="UID" clearable style="width: 110px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 130px">
              <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
            </el-select>
          </el-form-item>
          <el-form-item label="付款">
            <el-select v-model="tableFrom.payStatus" placeholder="全部" clearable style="width: 110px">
              <el-option label="未支付" :value="0" />
              <el-option label="已支付" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单卡片列表 -->
      <div v-loading="loading" class="order-list">
        <div v-for="row in tableData" :key="row.id" class="order-card">
          <!-- 卡片头：单号 + 时间 + 状态 -->
          <div class="card-head">
            <span class="head-label">订单编号：</span>
            <span class="head-no">{{ row.orderNo }}</span>
            <span class="head-time">{{ shortTime(row.createTime) }}</span>
            <span class="head-status" :class="statusClass(row.status)">{{ statusMap[row.status] || row.status }}</span>
            <span v-if="row.status === -1 && row.rejectReason" class="head-reason" :title="row.rejectReason">（{{ row.rejectReason }}）</span>
            <span v-if="row.status === 10 && row.waitDurationText" class="head-reason" :title="'等待期间订单可被人工跳过匹配，直接向上寻找有库存的上级'">{{ row.waitDurationText }}</span>
            <span v-if="row.exchanged === 1" class="head-reason" :title="'换货单号：' + (row.exchangeNo || '')">（已换货）</span>
          </div>
          <!-- 卡片体：商品 | 收货 | 金额 | 下单人 | 上级 | 付款/状态 | 操作 -->
          <div class="card-body">
            <div class="col col-goods">
              <div v-for="(g, gi) in (row.productList || [])" :key="gi" class="goods-item">
                <img v-if="g.image" :src="g.image" class="goods-img" />
                <div class="goods-info">
                  <div class="goods-name" :title="g.productName">{{ g.productName }}</div>
                  <div class="goods-num">¥{{ fmtMoney(g.price) }} × {{ g.num }}</div>
                </div>
              </div>
              <div v-if="!(row.productList || []).length" class="sub-text">—</div>
            </div>
            <div class="col col-recv">
              <template v-if="row.realName">
                <div class="recv-name">{{ row.realName }}<span class="sub-text recv-phone">{{ row.phone }}</span></div>
                <div class="recv-addr" :title="row.userAddress">{{ row.userAddress || '—' }}</div>
              </template>
              <div v-else class="sub-text">无需收货信息</div>
              <div class="recv-tags">
                <span class="mini-chip">{{ orderTypeText(row.orderType) }}</span>
                <span class="mini-chip">{{ stockTypeText(row.stockType) }}库存</span>
              </div>
            </div>
            <div class="col col-amount">
              <div class="amount">¥{{ fmtMoney(row.totalPrice) }}</div>
              <div class="sub-text">共 {{ row.totalNum }} 件</div>
            </div>
            <div class="col col-agent">
              <div class="agent-line">
                <img v-if="row.avatar" :src="row.avatar" class="avatar avatar-img" alt="" @error="row.avatar = ''" />
                <span v-else class="avatar">{{ (row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
                <div class="agent-text">
                  <div class="agent-name" :title="row.nickname">{{ row.nickname }}</div>
                  <div class="sub-text">{{ row.levelName }} · UID {{ row.uid }}</div>
                  <div class="sub-text" :title="row.agentPhone">{{ row.agentPhone || '—' }}</div>
                </div>
              </div>
            </div>
            <!-- 上级信息：有上级代理则展示其昵称/UID/手机号；无上级代理则为总部审核 -->
            <div class="col col-parent">
              <template v-if="row.parentIsHeadquarters === 1">
                <div class="agent-line">
                  <span class="avatar avatar-hq">总</span>
                  <div class="agent-text">
                    <div class="agent-name">总部审核</div>
                    <div class="sub-text">无上级代理，由总部直接审核</div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="agent-line">
                  <img
                    v-if="row.parentAvatar"
                    :src="row.parentAvatar"
                    class="avatar avatar-up avatar-img"
                    alt=""
                    @error="row.parentAvatar = ''"
                  />
                  <span v-else class="avatar avatar-up">{{
                    (row.parentNickname || '?').slice(0, 1).toUpperCase()
                  }}</span>
                  <div class="agent-text">
                    <div class="agent-name" :title="row.parentNickname">{{ row.parentNickname }}</div>
                    <div class="sub-text">{{ row.parentLevelName || '上级代理' }} · UID {{ row.parentUid }}</div>
                    <div class="sub-text" :title="row.parentPhone">{{ row.parentPhone || '—' }}</div>
                  </div>
                </div>
              </template>
            </div>
            <div class="col col-state">
              <span class="state-text" :class="statusClass(row.status)">{{ statusMap[row.status] || row.status }}</span>
              <div class="sub-text">{{ payStatusText(row) }}</div>
              <div v-if="row.expressNum" class="sub-text ellipsis" :title="(row.expressName || '') + ' ' + row.expressNum">{{ row.expressName }} {{ row.expressNum }}</div>
            </div>
            <div class="col col-ops">
              <el-button size="mini" type="primary" plain class="op-btn" @click="detail(row)">查看明细</el-button>
              <el-button v-if="row.status === 0 && checkPermi(['admin:stock:order:audit'])" size="mini" type="warning" plain class="op-btn" @click="openAudit(row)">介入审核</el-button>
              <el-button v-if="row.status === 1 && checkPermi(['admin:stock:order:pay'])" size="mini" type="warning" plain class="op-btn" @click="onPay(row)">确认收款</el-button>
              <el-button v-if="row.status === 2 && checkPermi(['admin:stock:order:send'])" size="mini" type="primary" plain class="op-btn" @click="openSend(row)">订单发货</el-button>
              <el-button v-if="row.status === 3 && checkPermi(['admin:stock:order:send'])" size="mini" type="success" plain class="op-btn" @click="onFinish(row)">标记完成</el-button>
              <el-button v-if="row.status === 10 && checkPermi(['admin:stock:order:audit'])" size="mini" type="warning" plain class="op-btn" @click="onSkipMatch(row)">跳过匹配上级</el-button>
              <el-button v-if="row.status === 10 && checkPermi(['admin:stock:order:audit'])" size="mini" type="primary" plain class="op-btn" @click="openAudit(row)">介入审核</el-button>
            </div>
          </div>
        </div>

        <div v-if="!tableData.length && !loading" class="empty-tip">暂无订单</div>
      </div>

      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 明细弹窗 -->
    <el-dialog title="订单明细" :visible.sync="detailVisible" width="640px">
      <div v-if="detailRow" style="margin-bottom:8px;color:#666">
        {{ detailRow.orderNo }} · {{ detailRow.nickname }} · 总额 ¥{{ detailRow.totalPrice }}
        <span v-if="detailRow.mark" style="color:#999">（备注：{{ detailRow.mark }}）</span>
      </div>
      <div v-if="detailRow && detailRow.realName" style="margin-bottom:10px;padding:8px 12px;background:#f5f7fa;border-radius:4px;font-size:13px;color:#303133">
        收货：{{ detailRow.realName }} {{ detailRow.phone }} · {{ detailRow.userAddress }}
      </div>
      <el-table v-if="detailRow" :data="detailRow.productList || []" size="small">
        <el-table-column label="商品" min-width="180">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center">
              <img :src="scope.row.image" style="width:32px;height:32px;margin-right:6px;border-radius:4px">
              <span>{{ scope.row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="数量" width="70" />
        <el-table-column prop="price" label="拿货价" width="90" />
        <el-table-column prop="parentPrice" label="上级拿价" width="90" />
        <el-table-column prop="totalPrice" label="小计" width="90" />
      </el-table>
    </el-dialog>

    <!-- 介入审核弹窗 -->
    <el-dialog title="总部介入审核" :visible.sync="auditVisible" width="400px">
      <el-alert type="warning" :closable="false" style="margin-bottom:12px"
                title="该订单尚在【待上级审核】阶段，总部介入通过后将跳过上级审核，直接扣云仓库存并进入待付款" />
      <div v-if="auditRow" style="margin-bottom:10px;color:#666;font-size:13px">
        {{ auditRow.orderNo }} · {{ auditRow.nickname }} · ¥{{ auditRow.totalPrice }}
      </div>
      <el-form size="small" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过（进入待发货）</el-radio>
            <el-radio :label="-1">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.status === -1" label="驳回原因">
          <el-input v-model="auditForm.reason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="auditVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveAudit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 发货弹窗：复用原生发货（物流公司下拉 + 收货信息） -->
    <el-dialog title="订单发货" :visible.sync="sendVisible" width="520px">
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

      <!-- 配送方式选择卡 -->
      <div class="ship-types">
        <div v-for="t in deliveryTypes" :key="t.value" class="ship-type" :class="{ active: sendForm.deliveryType === t.value }" @click="setDeliveryType(t.value)">
          <i :class="t.icon" class="st-icon"></i>
          <div class="st-name">{{ t.name }}</div>
          <div class="st-desc">{{ t.desc }}</div>
        </div>
      </div>

      <el-form label-width="90px" size="small" style="margin-top: 16px">
        <template v-if="sendForm.deliveryType === 'express'">
          <el-form-item label="快递公司">
            <el-select v-model="sendForm.expressCode" filterable placeholder="请选择快递公司" style="width: 100%" @change="onExpressChange">
              <el-option v-for="item in expressOptions" :key="item.code" :label="item.name" :value="item.code" />
            </el-select>
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="sendForm.expressNum" placeholder="请输入快递单号" />
          </el-form-item>
        </template>
        <template v-else-if="sendForm.deliveryType === 'send'">
          <el-form-item label="送货人姓名">
            <el-input v-model="sendForm.deliveryName" placeholder="请输入送货人姓名" />
          </el-form-item>
          <el-form-item label="送货人电话">
            <el-input v-model="sendForm.deliveryTel" placeholder="请输入送货人电话" />
          </el-form-item>
        </template>
        <el-form-item v-else label="虚拟发货">
          <span class="sub-text">无需物流，确认后直接标记已发货</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="sendVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveSend">确定发货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockOrderListApi, stockOrderPayApi, stockOrderAuditApi, stockOrderSendApi, stockOrderFinishApi, stockOrderSkipMatchApi } from '@/api/stock';
import { expressAllApi } from '@/api/sms';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'StockOrder',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, orderNo: '', uid: null, status: null, payStatus: null },
      statusMap: { 0: '待上级审核', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', '-1': '已驳回', 10: '等待匹配上级', '-2': '已取消' },
      detailVisible: false,
      detailRow: null,
      sendVisible: false,
      sendRow: null,
      expressOptions: [],
      sendForm: { deliveryType: 'express', expressCode: '', expressName: '', expressNum: '', deliveryName: '', deliveryTel: '' },
      deliveryTypes: [
        { value: 'express', name: '快递发货', desc: '填写快递单号', icon: 'el-icon-truck' },
        { value: 'send', name: '送货上门', desc: '登记送货人', icon: 'el-icon-user' },
        { value: 'fictitious', name: '虚拟发货', desc: '无需物流', icon: 'el-icon-message' }
      ],
      auditVisible: false,
      auditRow: null,
      auditForm: { status: 1, reason: '' }
    };
  },
  methods: {
    checkPermi,
    orderTypeText(t) {
      return { 1: '采购', 2: '提货', 3: '换货' }[t] || '采购';
    },
    // 库存类型标注：1=实体 2=虚拟
    stockTypeText(t) {
      return t === 2 ? '虚拟' : '实体';
    },
    // 真实支付方式：1=微信线上支付 2=后台记账欠款 3=余额支付
    payTypeText(t) {
      if (t === 3) return '余额支付';
      if (t === 2) return '记账欠款';
      return t === 1 ? '微信支付' : '未支付';
    },
    // 付款状态：只有已支付/未支付；已支付时附支付方式（历史数据 payType 为空时只显示已支付）
    payStatusText(row) {
      if (row.payStatus !== 1) return '未支付';
      return row.payType ? '已支付 · ' + this.payTypeText(row.payType) : '已支付';
    },
    // 状态配色：待审核/待付款橙、待发货/待收货蓝、已完成绿、已驳回红
    statusClass(s) {
      return { 0: 'is-warn', 1: 'is-warn', 2: 'is-info', 3: 'is-info', 4: 'is-ok', '-1': 'is-danger' }[s] || '';
    },
    shortTime(t) {
      if (!t) return '-';
      return String(t).substring(0, 16);
    },
    fmtMoney(v) {
      const n = Number(v || 0);
      return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    getList() {
      this.loading = true;
      stockOrderListApi(this.tableFrom).then(res => {
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
      this.tableFrom = { page: 1, limit: 20, orderNo: '', uid: null, status: null, payStatus: null };
      this.getList();
    },
    detail(row) {
      this.detailRow = row;
      this.detailVisible = true;
    },
    openAudit(row) {
      this.auditRow = row;
      this.auditForm = { status: 1, reason: '' };
      this.auditVisible = true;
    },
    saveAudit() {
      if (this.auditForm.status === -1 && !this.auditForm.reason) return this.$message.error('请填写驳回原因');
      const tip = this.auditForm.status === 1
        ? '总部介入通过后将跳过上级审核，直接扣云仓库存并进入待付款，确认？'
        : '确认驳回该订单？';
      this.$confirm(tip, '总部介入审核').then(() => {
        stockOrderAuditApi(this.auditRow.id, this.auditForm).then(() => {
          this.$message.success('已审核');
          this.auditVisible = false;
          this.getList();
        });
      }).catch(() => {});
    },
    onPay(row) {
      this.$confirm('确认已收到该订单款项 ¥' + row.totalPrice + '？', '确认收款').then(() => {
        stockOrderPayApi(row.id).then(() => {
          this.$message.success('已确认收款');
          this.getList();
        });
      }).catch(() => {});
    },
    openSend(row) {
      this.sendRow = row;
      this.sendForm = { deliveryType: 'express', expressCode: '', expressName: '', expressNum: '', deliveryName: '', deliveryTel: '' };
      this.sendVisible = true;
      if (!this.expressOptions.length) {
        expressAllApi({ type: 'normal' }).then(res => {
          this.expressOptions = res || [];
        }).catch(() => { this.expressOptions = []; });
      }
    },
    onExpressChange(code) {
      const item = this.expressOptions.find(e => e.code === code);
      this.sendForm.expressName = item ? item.name : '';
    },
    onDeliveryTypeChange() {
      this.sendForm.expressCode = '';
      this.sendForm.expressName = '';
      this.sendForm.expressNum = '';
      this.sendForm.deliveryName = '';
      this.sendForm.deliveryTel = '';
    },
    // 点击方式卡切换配送方式（重复点击同项不重置表单）
    setDeliveryType(v) {
      if (this.sendForm.deliveryType === v) return;
      this.sendForm.deliveryType = v;
      this.onDeliveryTypeChange();
    },
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
      const f = this.sendForm;
      let expressName = '';
      let expressNum = '';
      if (f.deliveryType === 'express') {
        if (!f.expressCode) return this.$message.error('请选择快递公司');
        if (!f.expressNum) return this.$message.error('请填写快递单号');
        expressName = f.expressName;
        expressNum = f.expressNum;
      } else if (f.deliveryType === 'send') {
        if (!f.deliveryName) return this.$message.error('请填写送货人姓名');
        if (!f.deliveryTel) return this.$message.error('请填写送货人电话');
        expressName = '送货上门';
        expressNum = f.deliveryName + ' ' + f.deliveryTel;
      } else {
        expressName = '虚拟发货';
        expressNum = '无需物流';
      }
      stockOrderSendApi(this.sendRow.id, { expressName, expressNum }).then(() => {
        this.$message.success('发货成功');
        this.sendVisible = false;
        this.getList();
      });
    },
    onFinish(row) {
      this.$confirm('确认标记该订单为已完成？完成后将自动核算代理奖励。', '提示').then(() => {
        stockOrderFinishApi(row.id).then(() => {
          this.$message.success('已完成，奖励已核算');
          this.getList();
        });
      }).catch(() => {});
    },
    onSkipMatch(row) {
      this.$confirm('跳过当前直接上级，立即沿上级链向上寻找有库存的上级？若全链无货将挂到总部。', '跳过匹配上级', { type: 'warning' }).then(() => {
        stockOrderSkipMatchApi(row.id).then(() => {
          this.$message.success('已重新匹配上级');
          this.getList();
        });
      }).catch(() => {});
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

/* ===== 订单卡片列表 ===== */
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
.head-reason {
  color: #f56c6c;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片体：网格分列，列间细分隔线（固定含「上级」列） */
.card-body {
  display: grid;
  grid-template-columns: minmax(210px, 1.3fr) 180px 112px 172px 184px 142px 110px;
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

/* 商品列 */
.goods-item {
  display: flex;
  align-items: center;
  margin: 4px 0;
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
.recv-tags {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}
.mini-chip {
  padding: 0 8px;
  border-radius: 3px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  color: #909399;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}

/* 金额列 */
.col-amount {
  text-align: center;
}
.amount {
  font-size: 18px;
  font-weight: 600;
  color: #e93323;
  font-family: DIN, 'Helvetica Neue', Arial, sans-serif;
  line-height: 26px;
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
/* 上级代理：橙色头像；总部审核：灰蓝头像 */
.avatar-up {
  background: #fff7e6;
  color: #ff9900;
}
/* 使用会员真实头像时去掉底色，只保留圆形裁切 */
.avatar-img {
  background: #f2f4f8;
  object-fit: cover;
}
.avatar-hq {
  background: #eef1f6;
  color: #606266;
}
.agent-name {
  font-size: 14px;
  color: #303133;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 付款/状态列 */
.state-text {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}
.state-text.is-warn { color: #ff9900; }
.state-text.is-info { color: #409eff; }
.state-text.is-ok { color: #19be6b; }
.state-text.is-danger { color: #f56c6c; }

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

/* ===== 发货弹窗 ===== */
/* 收货信息卡 */
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

/* 配送方式选择卡 */
.ship-types {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.ship-type {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
}
.ship-type:hover {
  border-color: #c6e2ff;
}
.ship-type.active {
  border-color: #409eff;
  background: #f0f7ff;
}
.st-icon {
  font-size: 20px;
  color: #909399;
}
.ship-type.active .st-icon {
  color: #409eff;
}
.st-name {
  margin-top: 6px;
  font-size: 13px;
  color: #303133;
  font-weight: 600;
}
.ship-type.active .st-name {
  color: #409eff;
}
.st-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
