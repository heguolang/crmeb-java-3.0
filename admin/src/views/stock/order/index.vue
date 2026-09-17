<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
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
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="orderNo" label="订货单号" width="190" />
        <el-table-column label="代理" min-width="120">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div style="color:#999;font-size:12px">{{ scope.row.levelName }} · UID {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalNum" label="数量" width="70" />
        <el-table-column prop="totalPrice" label="金额" width="100" />
        <el-table-column label="付款方式" width="100">
          <template slot-scope="scope">{{ scope.row.payType === 1 ? '微信支付' : '记账欠款' }}</template>
        </el-table-column>
        <el-table-column label="付款" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'warning'" size="mini">{{ scope.row.payStatus === 1 ? '已付' : '未付' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)" size="mini">{{ statusMap[scope.row.status] || scope.row.status }}</el-tag>
            <div v-if="scope.row.status === -1" style="color:#f56c6c;font-size:12px">{{ scope.row.rejectReason }}</div>
          </template>
        </el-table-column>
        <el-table-column label="快递" width="150">
          <template slot-scope="scope">
            <div v-if="scope.row.expressNum">{{ scope.row.expressName }}</div>
            <div v-if="scope.row.expressNum" style="font-size:12px">{{ scope.row.expressNum }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="detail(scope.row)">明细</el-button>
            <el-button v-if="scope.row.status === 1 && checkPermi(['admin:stock:order:pay'])" type="text" size="small" class="green" @click="onPay(scope.row)">确认收款</el-button>
            <el-button v-if="scope.row.status === 2 && checkPermi(['admin:stock:order:send'])" type="text" size="small" @click="openSend(scope.row)">发货</el-button>
            <el-button v-if="scope.row.status === 3 && checkPermi(['admin:stock:order:send'])" type="text" size="small" @click="onFinish(scope.row)">标记完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 明细弹窗 -->
    <el-dialog title="订单明细" :visible.sync="detailVisible" width="640px">
      <div v-if="detailRow" style="margin-bottom:8px;color:#666">
        {{ detailRow.orderNo }} · {{ detailRow.nickname }} · 总额 ¥{{ detailRow.totalPrice }}
        <span v-if="detailRow.mark" style="color:#999">（备注：{{ detailRow.mark }}）</span>
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

    <!-- 发货弹窗 -->
    <el-dialog title="订单发货" :visible.sync="sendVisible" width="400px">
      <el-form label-width="90px" size="small">
        <el-form-item label="快递公司">
          <el-input v-model="sendForm.expressName" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="sendForm.expressNum" />
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
// force-rebuild-20260917a
import { stockOrderListApi, stockOrderPayApi, stockOrderSendApi, stockOrderFinishApi } from '@/api/stock';
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
      sendForm: { expressName: '', expressNum: '' }
    };
  },
  methods: {
    checkPermi,
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
      this.sendForm = { expressName: '', expressNum: '' };
      this.sendVisible = true;
    },
    saveSend() {
      if (!this.sendForm.expressName || !this.sendForm.expressNum) return this.$message.error('请填写快递信息');
      stockOrderSendApi(this.sendRow.id, this.sendForm).then(() => {
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
.red { color: #f56c6c; }
.green { color: #67c23a; }
</style>
