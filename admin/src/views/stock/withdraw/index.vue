<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small">
        <el-form-item label="状态">
          <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 130px" @change="getList">
            <el-option label="待审核" :value="0" />
            <el-option label="已打款" :value="1" />
            <el-option label="已驳回" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="withdrawNo" label="提现单号" width="190" />
        <el-table-column label="代理" min-width="120">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div style="color:#999;font-size:12px">{{ scope.row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="金额" width="110">
          <template slot-scope="scope"><b class="red">¥{{ scope.row.price }}</b></template>
        </el-table-column>
        <el-table-column prop="mark" label="申请备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)" size="mini">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditMark" label="审核备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="150" />
        <el-table-column label="操作" width="110" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0 && checkPermi(['admin:stock:withdraw:audit'])" type="text" size="small" @click="audit(scope.row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <el-dialog title="提现审核" :visible.sync="auditVisible" width="420px">
      <div v-if="auditRow" style="margin-bottom:10px;color:#666">{{ auditRow.nickname }} 申请提现 <b>¥{{ auditRow.price }}</b></div>
      <el-form size="small" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过打款</el-radio>
            <el-radio :label="-1">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auditForm.auditMark" type="textarea" :rows="2" placeholder="打款凭证号/驳回原因" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="auditVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveAudit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockWithdrawListApi, stockWithdrawAuditApi } from '@/api/stock';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'StockWithdraw',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, status: null },
      auditVisible: false,
      auditRow: null,
      auditForm: { status: 1, auditMark: '' }
    };
  },
  methods: {
    checkPermi,
    getList() {
      this.loading = true;
      stockWithdrawListApi(this.tableFrom).then(res => {
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
      return { 0: 'warning', 1: 'success', '-1': 'danger' }[s] || 'info';
    },
    statusText(s) {
      return { 0: '待审核', 1: '已打款', '-1': '已驳回' }[s] || s;
    },
    audit(row) {
      this.auditRow = row;
      this.auditForm = { status: 1, auditMark: '' };
      this.auditVisible = true;
    },
    saveAudit() {
      stockWithdrawAuditApi(this.auditRow.id, this.auditForm).then(() => {
        this.$message.success('已审核');
        this.auditVisible = false;
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
.red { color: #f56c6c; }
</style>
