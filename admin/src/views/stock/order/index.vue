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
              <el-option label="未付款" :value="0" />
              <el-option label="已付款" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table class="admin-table" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column label="订货单号" min-width="112" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.orderNo }}</template>
        </el-table-column>
        <el-table-column label="代理" min-width="100">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div class="sub-text">{{ scope.row.levelName }} · UID {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型/库存" width="78">
          <template slot-scope="scope">
            <div>{{ orderTypeText(scope.row.orderType) }}</div>
            <div class="sub-text">{{ stockTypeText(scope.row.stockType) }}库存</div>
          </template>
        </el-table-column>
        <el-table-column label="收货人" min-width="96">
          <template slot-scope="scope">
            <template v-if="scope.row.realName">
              <div>{{ scope.row.realName }}</div>
              <div class="sub-text">{{ scope.row.phone }}</div>
            </template>
            <span v-else class="sub-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="数量/金额" width="78">
          <template slot-scope="scope">
            <div>{{ scope.row.totalNum }} 件</div>
            <div class="price-text">¥{{ scope.row.totalPrice }}</div>
          </template>
        </el-table-column>
        <el-table-column label="付款" width="78">
          <template slot-scope="scope">
            <span class="st-dot" :class="{ on: scope.row.payStatus === 1 }"><i></i>{{ scope.row.payStatus === 1 ? '已付' : '未付' }}</span>
            <div class="sub-text">{{ payTypeText(scope.row.payType) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <span class="st-dot" :class="{ warn: scope.row.status === 0 || scope.row.status === 1, on: scope.row.status === 4, danger: scope.row.status === -1 }"><i></i>{{ statusMap[scope.row.status] || scope.row.status }}</span>
            <div v-if="scope.row.status === -1" class="reject-text">{{ scope.row.rejectReason }}</div>
            <div v-else-if="scope.row.expressNum" class="sub-text">{{ scope.row.expressName }} {{ scope.row.expressNum }}</div>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="108">
          <template slot-scope="scope">{{ shortTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <div class="op-links">
              <a class="op-link" @click="detail(scope.row)">明细</a>
              <el-divider v-if="scope.row.status === 0 && checkPermi(['admin:stock:order:audit'])" direction="vertical"></el-divider>
              <a v-if="scope.row.status === 0 && checkPermi(['admin:stock:order:audit'])" class="op-link" @click="openAudit(scope.row)">介入审核</a>
              <el-divider v-if="scope.row.status === 1 && checkPermi(['admin:stock:order:pay'])" direction="vertical"></el-divider>
              <a v-if="scope.row.status === 1 && checkPermi(['admin:stock:order:pay'])" class="op-link" @click="onPay(scope.row)">确认收款</a>
              <el-divider v-if="scope.row.status === 2 && checkPermi(['admin:stock:order:send'])" direction="vertical"></el-divider>
              <a v-if="scope.row.status === 2 && checkPermi(['admin:stock:order:send'])" class="op-link" @click="openSend(scope.row)">发货</a>
              <el-divider v-if="scope.row.status === 3 && checkPermi(['admin:stock:order:send'])" direction="vertical"></el-divider>
              <a v-if="scope.row.status === 3 && checkPermi(['admin:stock:order:send'])" class="op-link" @click="onFinish(scope.row)">标记完成</a>
            </div>
          </template>
        </el-table-column>
      </el-table>
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
    <el-dialog title="订单发货" :visible.sync="sendVisible" width="560px">
      <div v-if="sendRow" class="recipient-box">
        <div class="recipient-row"><span class="label">收货人：</span><span>{{ sendRow.realName || '-' }}</span></div>
        <div class="recipient-row"><span class="label">联系电话：</span><span>{{ sendRow.phone || '-' }}</span></div>
        <div class="recipient-row"><span class="label">收货地址：</span><span>{{ sendRow.userAddress || '-' }}</span></div>
        <el-button size="mini" plain class="copy-btn" @click="copyRecipient">一键复制</el-button>
      </div>
      <el-form label-width="100px" size="small" style="margin-top: 14px">
        <el-form-item label="配送方式">
          <el-radio-group v-model="sendForm.deliveryType" @change="onDeliveryTypeChange">
            <el-radio label="express">快递发货</el-radio>
            <el-radio label="send">送货上门</el-radio>
            <el-radio label="fictitious">虚拟发货</el-radio>
          </el-radio-group>
        </el-form-item>
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
          <span class="sub-text">无需物流，直接标记已发货</span>
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
import { stockOrderListApi, stockOrderPayApi, stockOrderAuditApi, stockOrderSendApi, stockOrderFinishApi } from '@/api/stock';
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
      statusMap: { 0: '待上级审核', 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', '-1': '已驳回' },
      detailVisible: false,
      detailRow: null,
      sendVisible: false,
      sendRow: null,
      expressOptions: [],
      sendForm: { deliveryType: 'express', expressCode: '', expressName: '', expressNum: '', deliveryName: '', deliveryTel: '' },
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
    shortTime(t) {
      if (!t) return '-';
      return String(t).substring(0, 16);
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
    statusTag(s) {
      return { 0: 'warning', 1: 'warning', 2: 'primary', 3: 'primary', 4: 'success', '-1': 'danger' }[s] || 'info';
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
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.sub-text {
  color: #999;
  font-size: 12px;
}
.price-text {
  color: #e93323;
  font-weight: 600;
  font-size: 12px;
}
.reject-text {
  color: #f56c6c;
  font-size: 12px;
  line-height: 18px;
  margin-top: 2px;
}
</style>
