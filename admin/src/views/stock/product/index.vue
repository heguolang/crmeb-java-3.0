<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="tableFrom.keywords" placeholder="商品名称" clearable style="width: 200px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column label="商品" min-width="220">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center">
              <img :src="scope.row.image" style="width:36px;height:36px;margin-right:8px;border-radius:4px">
              <span>{{ scope.row.storeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="零售价" width="90" />
        <el-table-column label="云仓库存" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.stock <= 10 ? '#f56c6c' : '' }">{{ scope.row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column v-for="lv in levels" :key="lv.id" :label="lv.name + '价'" width="100">
          <template slot-scope="scope">{{ priceOf(scope.row, lv.id) || '按折扣' + lv.discount + '%' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="checkPermi(['admin:stock:price:save'])" type="text" size="small" @click="openPrice(scope.row)">设置拿货价</el-button>
            <el-button v-if="checkPermi(['admin:stock:log:adjust'])" type="text" size="small" @click="openAdjust(scope.row)">调整库存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header"><b>库存变动日志</b></div>
      <el-table :data="logs" size="small">
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="linkNo" label="关联单号" width="180" />
        <el-table-column label="类型" width="130">
          <template slot-scope="scope">{{ typeName(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column prop="changeNum" label="变动" width="80">
          <template slot-scope="scope"><span :style="{color: scope.row.changeNum<0?'#f56c6c':'#67c23a'}">{{ scope.row.changeNum }}</span></template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变动前" width="80" />
        <el-table-column prop="afterStock" label="变动后" width="80" />
        <el-table-column prop="mark" label="备注" min-width="160" />
        <el-table-column prop="createTime" label="时间" width="150" />
      </el-table>
      <div class="block">
        <el-pagination background layout="prev, pager, next" :page-size="logFrom.limit" :current-page="logFrom.page" :total="logTotal" @current-change="logPage" />
      </div>
    </el-card>

    <!-- 拿货价弹窗 -->
    <el-dialog title="设置各层级拿货价" :visible.sync="priceVisible" width="440px">
      <div v-if="priceRow" style="margin-bottom:10px;color:#999">{{ priceRow.storeName }}（零售价 ¥{{ priceRow.price }}）</div>
      <el-table v-if="priceRow" :data="priceRow.levelPrices" size="small">
        <el-table-column prop="levelName" label="层级" width="110" />
        <el-table-column label="拿货价">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="mini" style="width: 150px" placeholder="留空=默认折扣" />
            <div style="font-size:12px;color:#999">默认 {{ scope.row.discount }}% = ¥{{ (priceRow.price * scope.row.discount / 100).toFixed(2) }}</div>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button size="small" @click="priceVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="savePrice">保存</el-button>
      </div>
    </el-dialog>

    <!-- 调库存弹窗 -->
    <el-dialog title="调整云仓库存" :visible.sync="adjustVisible" width="400px">
      <el-form v-if="adjustRow" label-width="90px" size="small">
        <el-form-item label="商品">{{ adjustRow.storeName }}</el-form-item>
        <el-form-item label="当前库存">{{ adjustRow.stock }}</el-form-item>
        <el-form-item label="变动数量">
          <el-input-number v-model="adjustForm.changeNum" :precision="0" size="small" />
          <div style="font-size:12px;color:#999">正数入库，负数出库</div>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.mark" type="textarea" :rows="2" placeholder="如：盘点修正/采购入库" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="adjustVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveAdjust">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockProductListApi, stockPriceSaveApi, stockAdjustApi, stockLogListApi } from '@/api/stock';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'StockProduct',
  data() {
    return {
      loading: false,
      tableData: [],
      levels: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, keywords: '' },
      logs: [],
      logTotal: 0,
      logFrom: { page: 1, limit: 10, productId: null, type: null },
      priceVisible: false,
      priceRow: null,
      adjustVisible: false,
      adjustRow: null,
      adjustForm: { productId: null, changeNum: 0, mark: '' }
    };
  },
  methods: {
    checkPermi,
    getList() {
      this.loading = true;
      stockProductListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.levels = (res && res.levels) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    loadLogs() {
      stockLogListApi(this.logFrom).then(res => {
        this.logs = (res && res.list) || [];
        this.logTotal = (res && res.total) || 0;
      });
    },
    logPage(page) {
      this.logFrom.page = page;
      this.loadLogs();
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    priceOf(row, levelId) {
      const item = (row.levelPrices || []).find(p => p.levelId === levelId);
      return item && item.price;
    },
    openPrice(row) {
      this.priceRow = JSON.parse(JSON.stringify(row));
      this.priceVisible = true;
    },
    savePrice() {
      const prices = this.priceRow.levelPrices.map(p => ({ levelId: p.levelId, price: p.price }));
      stockPriceSaveApi({ productId: this.priceRow.id, prices }).then(() => {
        this.$message.success('已保存');
        this.priceVisible = false;
        this.getList();
      });
    },
    openAdjust(row) {
      this.adjustRow = row;
      this.adjustForm = { productId: row.id, changeNum: 0, mark: '' };
      this.adjustVisible = true;
    },
    saveAdjust() {
      if (!this.adjustForm.changeNum) return this.$message.error('变动数量不能为0');
      stockAdjustApi(this.adjustForm).then(() => {
        this.$message.success('调整成功');
        this.adjustVisible = false;
        this.getList();
        this.loadLogs();
      });
    },
    typeName(t) {
      return { 1: '审核通过扣库存', 2: '驳回回补', 3: '换货新品扣', 4: '换货旧品回补', 5: '手动调整' }[t] || t;
    }
  },
  mounted() {
    this.getList();
    this.loadLogs();
  }
};
</script>

<style scoped>
.red { color: #f56c6c; }
</style>
