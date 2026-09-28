<template>
  <div class="divBg addContent-wrapper">
    <!-- 结算记录 -->
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="filter-panel">
        <div class="filter-group__title">结算记录</div>
        <div class="filter-actions">
          <el-button size="small" icon="el-icon-refresh" @click="getRecords">刷新</el-button>
        </div>
      </div>
      <el-table class="admin-table table-lg" v-loading="recordLoading" :data="records" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="期号" width="150">
          <template slot-scope="scope">{{ scope.row.periodNo }}</template>
        </el-table-column>
        <el-table-column label="周期" width="90" align="center">
          <template slot-scope="scope">{{ cycleText(scope.row.cycleType) }}</template>
        </el-table-column>
        <el-table-column label="基数" width="120" align="center">
          <template slot-scope="scope">{{ baseText(scope.row.baseType) }}</template>
        </el-table-column>
        <el-table-column label="基数总额" width="130" align="center">
          <template slot-scope="scope"><span class="c-red">¥{{ scope.row.baseAmount }}</span></template>
        </el-table-column>
        <el-table-column label="发放总额" width="130" align="center">
          <template slot-scope="scope"><span class="c-red">¥{{ scope.row.totalPaid }}</span></template>
        </el-table-column>
        <el-table-column label="参与人次" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.memberCount }}</template>
        </el-table-column>
        <el-table-column label="订单数" width="90" align="center">
          <template slot-scope="scope">{{ scope.row.orderCount }}</template>
        </el-table-column>
        <el-table-column label="结算时间" width="170" align="center">
          <template slot-scope="scope">{{ scope.row.createTime }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.mark }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDetail(scope.row)">发放明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="recordTotal"
          :page-size="recordLimit" :current-page.sync="recordPage" @current-change="getRecords" />
      </div>
    </el-card>

    <!-- 发放明细弹窗 -->
    <el-dialog :title="'发放明细 - ' + detailPeriod" :visible.sync="detailVisible" width="760px" :before-close="closeDetail">
      <el-table class="admin-table table-lg" v-loading="detailLoading" :data="detailList" size="small" stripe highlight-current-row max-height="420">
        <el-table-column width="30" />
        <el-table-column label="ID" prop="uid" width="90" align="center" />
        <el-table-column label="分红等级" width="140" align="center">
          <template slot-scope="scope">{{ scope.row.levelName }}</template>
        </el-table-column>
        <el-table-column label="比例" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.rate }}%</template>
        </el-table-column>
        <el-table-column label="分红金额" width="140" align="center">
          <template slot-scope="scope"><span class="c-red">¥{{ scope.row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="发放时间" min-width="170" align="center">
          <template slot-scope="scope">{{ scope.row.createTime }}</template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="detailTotal"
          :page-size="detailLimit" :current-page.sync="detailPage" @current-change="getDetail" />
      </div>
      <span slot="footer">
        <el-button @click="closeDetail">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { dividendRecordListApi, dividendDetailListApi } from '@/api/dividend';

export default {
  name: 'DividendRecord',
  data() {
    return {
      records: [],
      recordPage: 1,
      recordLimit: 10,
      recordTotal: 0,
      recordLoading: false,
      detailVisible: false,
      detailLoading: false,
      detailPeriod: '',
      detailRecordId: 0,
      detailList: [],
      detailPage: 1,
      detailLimit: 10,
      detailTotal: 0,
    };
  },
  mounted() {
    this.getRecords();
  },
  methods: {
    cycleText(v) {
      return { 1: '日分红', 2: '周分红', 3: '月分红' }[v] || '-';
    },
    baseText(v) {
      return { 1: '订单总业绩', 2: '订单利润' }[v] || '-';
    },
    getRecords() {
      this.recordLoading = true;
      dividendRecordListApi({ page: this.recordPage, limit: this.recordLimit }).then((res) => {
        this.records = res.list || [];
        this.recordTotal = res.total || 0;
      }).finally(() => { this.recordLoading = false; });
    },
    showDetail(row) {
      this.detailRecordId = row.id;
      this.detailPeriod = row.periodNo;
      this.detailPage = 1;
      this.detailVisible = true;
      this.getDetail();
    },
    getDetail() {
      this.detailLoading = true;
      dividendDetailListApi({ recordId: this.detailRecordId, page: this.detailPage, limit: this.detailLimit }).then((res) => {
        this.detailList = res.list || [];
        this.detailTotal = res.total || 0;
      }).finally(() => { this.detailLoading = false; });
    },
    closeDetail() {
      this.detailVisible = false;
    },
  },
};
</script>

<style scoped lang="scss">
.c-red {
  color: #e93323;
}
.filter-group__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.pager-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 4px;
}
</style>
