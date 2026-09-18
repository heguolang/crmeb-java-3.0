<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small">
        <el-form-item label="状态">
          <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 150px" @change="getList">
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="exchangeNo" label="换货单号" width="190" />
        <el-table-column label="代理" min-width="110">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div style="color:#999;font-size:12px">原单 {{ scope.row.orderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品" min-width="140" show-overflow-tooltip />
        <el-table-column prop="num" label="数量" width="70" />
        <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)" size="mini">{{ statusMap[scope.row.status] || scope.row.status }}</el-tag>
            <div v-if="scope.row.status === -1" style="color:#f56c6c;font-size:12px">{{ scope.row.rejectReason }}</div>
          </template>
        </el-table-column>
        <el-table-column label="退回/新发快递" width="170">
          <template slot-scope="scope">
            <div v-if="scope.row.backExpressNum" style="font-size:12px">退：{{ scope.row.backExpressName }} {{ scope.row.backExpressNum }}</div>
            <div v-if="scope.row.newExpressNum" style="font-size:12px">新：{{ scope.row.newExpressName }} {{ scope.row.newExpressNum }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0 && checkPermi(['admin:stock:exchange:audit'])" type="text" size="small" class="orange" @click="audit(scope.row)">介入审核</el-button>
            <el-button v-if="scope.row.status === 1 && checkPermi(['admin:stock:exchange:audit'])" type="text" size="small" class="green" @click="audit(scope.row)">总部审核</el-button>
            <el-button v-if="scope.row.status === 2 && checkPermi(['admin:stock:exchange:back'])" type="text" size="small" class="green" @click="confirmBack(scope.row)">旧品入库</el-button>
            <el-button v-if="scope.row.status === 3 && checkPermi(['admin:stock:exchange:send'])" type="text" size="small" @click="openSend(scope.row)">发新品</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <el-dialog title="换货审核" :visible.sync="auditVisible" width="400px">
      <el-alert v-if="auditRow && auditRow.status === 0" type="warning" :closable="false" style="margin-bottom:12px"
                title="该换货单尚在【待上级审核】阶段，总部介入后将跳过上级审核，直接进入待旧品退回" />
      <el-form size="small" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">{{ auditRow && auditRow.status === 0 ? '通过（跳过上级，直接待旧品退回）' : '通过（待旧品退回）' }}</el-radio>
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

    <el-dialog title="发新品" :visible.sync="sendVisible" width="400px">
      <el-form label-width="90px" size="small">
        <el-form-item label="快递公司"><el-input v-model="sendForm.expressName" /></el-form-item>
        <el-form-item label="快递单号"><el-input v-model="sendForm.expressNum" /></el-form-item>
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
    statusTag(s) {
      return { 0: 'warning', 1: 'warning', 2: 'primary', 3: 'primary', 4: 'success', '-1': 'danger' }[s] || 'info';
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
.green { color: #67c23a; }
.orange { color: #e6a23c; }
</style>
