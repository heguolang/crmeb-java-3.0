<template>
  <div class="divBg addContent-wrapper">
    <!-- 分红设置 -->
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="filter-panel">
        <div class="level-tip">
          <b>全球分红</b>：分红级别直接复用<b>分销商等级</b>（升级条件同步复用），达到某等级（含更高等级）的会员，
          按该等级的分红比例参与<b>均分</b>。结算时按设置的周期自动执行：日分红每天 00:01、周分红每周周一 00:01、月分红每月 1 号 01:00，
          分发的是刚结束一个周期的数据。
        </div>
      </div>
      <el-form ref="configForm" :model="configForm" label-width="120px" class="config-form">
        <el-form-item label="全球分红开关">
          <el-switch v-model="configForm.status" active-text="开" inactive-text="关" />
        </el-form-item>
        <el-form-item label="分红周期">
          <el-radio-group v-model="configForm.cycle">
            <el-radio :label="1">日分红（每天 00:01）</el-radio>
            <el-radio :label="2">周分红（每周周一 00:01）</el-radio>
            <el-radio :label="3">月分红（每月 1 号 01:00）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分红基数">
          <el-radio-group v-model="configForm.base">
            <el-radio :label="1">订单总业绩</el-radio>
            <el-radio :label="2">订单利润（商品售价 - 成本价）</el-radio>
          </el-radio-group>
          <div class="form-tip">基数为期内已支付订单统计；比例与基数相乘后在各等级参与会员间均分</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingConfig" @click="saveConfig">保存设置</el-button>
          <el-button :loading="running" @click="runNow">手动结算上一周期</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 等级分红比例 -->
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="filter-panel">
        <div class="filter-group__title">等级分红比例（复用分销商等级）</div>
        <div class="filter-actions">
          <el-button size="small" icon="el-icon-refresh" @click="getInfo">刷新</el-button>
        </div>
      </div>
      <el-table class="admin-table table-lg" v-loading="loading" :data="levels" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="ID" width="70">
          <template slot-scope="scope">{{ scope.row.id }}</template>
        </el-table-column>
        <el-table-column label="等级名称" min-width="140">
          <template slot-scope="scope"><span class="level-name">{{ scope.row.name }}</span></template>
        </el-table-column>
        <el-table-column label="等级权重" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.grade }}</template>
        </el-table-column>
        <el-table-column label="分红比例（%）" width="160" align="center">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.dividendRate" :min="0" :max="100" :precision="3" :controls="false" style="width: 120px" />
          </template>
        </el-table-column>
        <el-table-column label="参与会员" min-width="160">
          <template slot-scope="scope">
            <span class="form-tip">等级权重 ≥ {{ scope.row.grade }} 的会员均分</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" :loading="savingLevelId === scope.row.id" @click="saveLevel(scope.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
        <el-table-column label="分红级别" width="140" align="center">
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
import { dividendInfoApi, dividendConfigApi, dividendLevelApi, dividendRecordListApi, dividendDetailListApi, dividendRunApi } from '@/api/dividend';

export default {
  name: 'Dividend',
  data() {
    return {
      loading: false,
      savingConfig: false,
      savingLevelId: 0,
      running: false,
      configForm: { status: false, cycle: 1, base: 1 },
      levels: [],
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
    this.getInfo();
    this.getRecords();
  },
  methods: {
    cycleText(v) {
      return { 1: '日分红', 2: '周分红', 3: '月分红' }[v] || '-';
    },
    baseText(v) {
      return { 1: '订单总业绩', 2: '订单利润' }[v] || '-';
    },
    getInfo() {
      this.loading = true;
      dividendInfoApi().then((res) => {
        this.configForm.status = !!res.status;
        this.configForm.cycle = res.cycle || 1;
        this.configForm.base = res.base || 1;
        this.levels = (res.levels || []).map((l) => ({ ...l, dividendRate: Number(l.dividendRate || 0) }));
      }).finally(() => { this.loading = false; });
    },
    saveConfig() {
      this.savingConfig = true;
      dividendConfigApi({ status: this.configForm.status, cycle: this.configForm.cycle, base: this.configForm.base }).then(() => {
        this.$message.success('保存成功');
      }).finally(() => { this.savingConfig = false; });
    },
    saveLevel(row) {
      this.savingLevelId = row.id;
      dividendLevelApi({ id: row.id, dividendRate: row.dividendRate }).then(() => {
        this.$message.success('「' + row.name + '」比例已保存');
      }).finally(() => { this.savingLevelId = 0; });
    },
    runNow() {
      this.$confirm('将立即结算上一个完整周期的分红（重复触发不会重复发放），是否继续？', '手动结算', { type: 'warning' }).then(() => {
        this.running = true;
        dividendRunApi(this.configForm.cycle).then((res) => {
          this.$message.success(res || '已触发');
          this.getRecords();
        }).finally(() => { this.running = false; });
      }).catch(() => {});
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
.config-form {
  max-width: 640px;
  margin-top: 10px;
}
.level-tip {
  font-size: 13px;
  color: #606266;
  line-height: 22px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 18px;
  margin-top: 2px;
}
.level-name {
  font-weight: 600;
  color: #303133;
}
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
