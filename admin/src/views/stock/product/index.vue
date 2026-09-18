<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="商品名称">
            <el-input v-model="tableFrom.keywords" placeholder="商品名称" clearable style="width: 200px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="toolbar-actions">
          <el-button v-if="checkPermi(['admin:stock:price:save'])" type="success" icon="el-icon-plus" @click="openAdd">添加商品</el-button>
        </div>
      </div>
      <el-alert type="info" :closable="false" style="margin-bottom: 12px" title="仅显示已加入订货模块的商品；点击右上角「添加商品」从商城商品中选择加入后才能设置拿货价与库存" />
      <el-table class="admin-table" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column label="商品" min-width="160">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center">
              <img :src="scope.row.image" style="width:36px;height:36px;margin-right:8px;border-radius:4px">
              <span>{{ scope.row.storeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="零售价" width="78" />
        <el-table-column label="云仓库存" width="88">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.stock <= 10 ? '#f56c6c' : '' }">{{ scope.row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column v-for="lv in levels" :key="lv.id" :label="lv.name + '价'" width="88">
          <template slot-scope="scope">{{ priceOf(scope.row, lv.id) || '按折扣' + lv.discount + '%' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="196" fixed="right">
          <template slot-scope="scope">
            <div class="op-links">
              <a v-if="checkPermi(['admin:stock:price:save'])" class="op-link" @click="openPrice(scope.row)">设置拿货价</a>
              <el-divider direction="vertical"></el-divider>
              <a v-if="checkPermi(['admin:stock:log:adjust'])" class="op-link" @click="openAdjust(scope.row)">调整库存</a>
              <el-divider direction="vertical"></el-divider>
              <a v-if="checkPermi(['admin:stock:price:save'])" class="op-link" @click="onRemove(scope.row)">移除</a>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header"><b>库存变动日志</b></div>
      <el-table class="admin-table" :data="logs" size="small">
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
      <div class="pager">
        <el-pagination background layout="prev, pager, next" :page-size="logFrom.limit" :current-page="logFrom.page" :total="logTotal" @current-change="logPage" />
      </div>
    </el-card>

    <!-- 添加商品弹窗：append-to-body 避免被布局层挡住点击；row-key 保证勾选可用 -->
    <el-dialog
      title="添加商品（从商城商品中选择加入订货）"
      :visible.sync="addVisible"
      width="640px"
      append-to-body
      :close-on-click-modal="false"
      @closed="onAddClosed"
    >
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item>
          <el-input v-model="selectFrom.keywords" placeholder="搜索商品名称" clearable style="width: 240px" @keyup.enter.native="getSelectList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getSelectList">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table
        class="admin-table"
        ref="selectTable"
        v-loading="selectLoading"
        :data="selectData"
        row-key="id"
        size="small"
        max-height="360"
        @selection-change="onSelectChange"
      >
        <el-table-column type="selection" width="50" :reserve-selection="true" />
        <el-table-column label="商品" min-width="240">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center">
              <img :src="scope.row.image" style="width:32px;height:32px;margin-right:8px;border-radius:4px">
              <span>{{ scope.row.storeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="零售价" width="90" />
        <el-table-column prop="stock" label="库存" width="80" />
      </el-table>
      <div class="block">
        <el-pagination background layout="total, prev, pager, next" :page-size="selectFrom.limit" :current-page="selectFrom.page" :total="selectTotal" @current-change="selectPage" />
      </div>
      <div slot="footer">
        <el-button size="small" @click="closeAdd">取消</el-button>
        <el-button size="small" type="primary" :loading="adding" :disabled="!selectedIds.length" @click="doAdd">确定添加（{{ selectedIds.length }}）</el-button>
      </div>
    </el-dialog>

    <!-- 拿货价弹窗 -->
    <el-dialog title="设置各层级拿货价" :visible.sync="priceVisible" width="440px" append-to-body :close-on-click-modal="false">
      <div v-if="priceRow" style="margin-bottom:10px;color:#999">{{ priceRow.storeName }}（零售价 ¥{{ priceRow.price }}）</div>
      <div v-if="priceRow" class="ex-block">
        <div class="ex-row">
          <span class="ex-label">支持虚拟库存</span>
          <el-switch v-model="stockTypeForm.supportVirtual" @change="saveStockType" />
          <span class="ex-tip">开启后该商品可用「虚拟库存」下单（付款即入账虚拟库存，可提货/换货）</span>
        </div>
        <div class="ex-row">
          <span class="ex-label">支持实体库存</span>
          <el-switch v-model="stockTypeForm.supportPhysical" @change="saveStockType" />
          <span class="ex-tip">开启后该商品可用「实体库存」下单（走上级审核/发货流程）</span>
        </div>
        <div class="ex-row">
          <span class="ex-label">是否支持换货</span>
          <el-switch v-model="exForm.enable" @change="saveExchangeConfig" />
          <span class="ex-tip">{{ exForm.enable ? '允许换货' : '不允许换货' }}（未配置过的规格沿用整品设置；换货按钮精确到规格）</span>
        </div>
        <div class="ex-row">
          <span class="ex-label">可换入商品</span>
          <el-button size="mini" :disabled="!exForm.enable" @click="openExTargets">设置可换商品（已选 {{ exTargets.length }}）</el-button>
          <span class="ex-tip">只允许换入清单内的商品，且目标拿货价不得低于原商品价</span>
        </div>
        <div class="ex-row">
          <span class="ex-label">定价规格</span>
          <el-select v-model="priceSkuKey" size="mini" style="width: 220px" @change="onSkuChange">
            <el-option label="整品（商品级拿货价）" value="" />
            <el-option v-for="s in skuOptions" :key="s.skuKey" :label="s.attrValue || s.skuKey" :value="s.skuKey" />
          </el-select>
          <span class="ex-tip">选择规格后，下方「拿货价」就是该规格的层级价（未配置则回退商品级价）</span>
        </div>
      </div>
      <el-divider v-if="priceRow" />
      <el-table class="admin-table" v-if="priceRow" :data="priceRow.levelPrices" size="small">
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

    <el-dialog title="设置可换入商品" :visible.sync="exTargetsVisible" width="460px" append-to-body :close-on-click-modal="false">
      <div style="margin-bottom:8px;color:#999;font-size:12px">
        {{ priceRow && priceRow.storeName }} → 允许换入以下商品（可多选，一次换货只能选其中一个）
      </div>
      <el-select v-model="exTargets" multiple filterable placeholder="请选择可换入商品" style="width:100%">
        <el-option v-for="p in productOptions" :key="p.id" :label="p.storeName" :value="p.id" :disabled="priceRow && p.id === priceRow.id" />
      </el-select>
      <div slot="footer">
        <el-button size="small" @click="exTargetsVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveExTargets">保存</el-button>
      </div>
    </el-dialog>

    <!-- 调库存弹窗 -->
    <el-dialog title="调整云仓库存" :visible.sync="adjustVisible" width="400px" append-to-body :close-on-click-modal="false">
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
import { stockProductListApi, stockProductSelectListApi, stockProductAddApi, stockProductRemoveApi, stockPriceSaveApi, stockAdjustApi, stockLogListApi, stockExchangeConfigApi, stockExchangeConfigSaveApi, stockExchangeTargetsApi, stockExchangeTargetsSaveApi, stockProductStockTypeApi, stockProductStockTypeSaveApi, stockProductSkuListApi, stockProductSkuPriceApi } from '@/api/stock';
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
      exForm: { enable: true },
      exTargets: [],
      exTargetsVisible: false,
      productOptions: [],
      skuOptions: [],
      priceSkuKey: '',
      stockTypeForm: { supportVirtual: true, supportPhysical: true },
      adjustVisible: false,
      adjustRow: null,
      adjustForm: { productId: null, changeNum: 0, mark: '' },
      addVisible: false,
      selectLoading: false,
      selectData: [],
      selectTotal: 0,
      selectFrom: { page: 1, limit: 10, keywords: '' },
      selectedIds: [],
      adding: false
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
    openAdd() {
      this.selectedIds = [];
      this.addVisible = true;
      this.$nextTick(() => {
        if (this.$refs.selectTable) {
          this.$refs.selectTable.clearSelection();
        }
      });
      this.getSelectList();
    },
    closeAdd() {
      this.addVisible = false;
    },
    onAddClosed() {
      this.selectedIds = [];
      this.adding = false;
      if (this.$refs.selectTable) {
        this.$refs.selectTable.clearSelection();
      }
    },
    getSelectList() {
      this.selectLoading = true;
      stockProductSelectListApi(this.selectFrom).then(res => {
        this.selectData = (res && res.list) || [];
        this.selectTotal = (res && res.total) || 0;
        this.selectLoading = false;
      }).catch(() => { this.selectLoading = false; });
    },
    selectPage(page) {
      this.selectFrom.page = page;
      this.getSelectList();
    },
    onSelectChange(rows) {
      this.selectedIds = (rows || []).map(r => r.id).filter(id => id != null);
    },
    doAdd() {
      if (!this.selectedIds.length) {
        return this.$message.warning('请先勾选要添加的商品');
      }
      this.adding = true;
      stockProductAddApi(this.selectedIds).then(() => {
        this.$message.success('添加成功');
        this.adding = false;
        this.addVisible = false;
        this.selectedIds = [];
        this.selectFrom = { page: 1, limit: 10, keywords: '' };
        this.selectData = [];
        this.getList();
      }).catch(() => { this.adding = false; });
    },
    onRemove(row) {
      this.$confirm('确认将商品「' + row.storeName + '」移出订货模块？移除后订货商端将无法订购该商品', '提示', { type: 'warning' }).then(() => {
        stockProductRemoveApi(row.id).then(() => {
          this.$message.success('已移除');
          this.getList();
        });
      }).catch(() => {});
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
      this.loadExchange(row.id);
    },
    loadExchange(productId) {
      this.exForm = { enable: true };
      this.exTargets = [];
      stockExchangeConfigApi(productId).then(res => {
        const cfg = (res || []).find(x => !x.skuKey);
        this.exForm.enable = cfg ? !!cfg.enable : true;
      }).catch(() => {});
      stockExchangeTargetsApi(productId, '').then(res => {
        this.exTargets = (res || []).map(t => t.targetProductId);
      }).catch(() => {});
      if (!this.productOptions.length) {
        stockProductListApi({ page: 1, limit: 200 }).then(res => {
          this.productOptions = (res && res.list) || [];
        }).catch(() => {});
      }
      this.priceSkuKey = '';
      this.stockTypeForm = { supportVirtual: true, supportPhysical: true };
      stockProductStockTypeApi(productId).then(res => {
        if (res) {
          this.stockTypeForm = { supportVirtual: !!res.supportVirtual, supportPhysical: !!res.supportPhysical };
        }
      }).catch(() => {});
      stockProductSkuListApi(productId).then(res => {
        this.skuOptions = res || [];
      }).catch(() => {});
    },
    saveStockType() {
      stockProductStockTypeSaveApi({
        productId: this.priceRow.id,
        supportVirtual: this.stockTypeForm.supportVirtual,
        supportPhysical: this.stockTypeForm.supportPhysical
      }).then(() => this.$message.success('已保存'));
    },
    onSkuChange() {
      const sku = this.priceSkuKey;
      const lv = this.priceRow.levelPrices || [];
      if (!sku) {
        // 回到商品级价：重新拉取商品级价格
        stockProductListApi({ page: 1, limit: 200 }).then(() => {}).catch(() => {});
        lv.forEach(p => { p.price = p.productPrice !== undefined ? p.productPrice : p.price; });
        return;
      }
      stockProductSkuPriceApi(this.priceRow.id, sku).then(res => {
        const map = {};
        (res || []).forEach(x => { map[x.levelId] = x.price; });
        lv.forEach(p => { p.price = map[p.levelId] !== undefined ? Number(map[p.levelId]) : null; });
      }).catch(() => {});
    },
    saveExchangeConfig() {
      stockExchangeConfigSaveApi({
        productId: this.priceRow.id,
        skuKey: '',
        enable: this.exForm.enable,
        minTargetPrice: 0
      }).then(() => this.$message.success(this.exForm.enable ? '该商品已开放换货' : '该商品已关闭换货'));
    },
    openExTargets() {
      this.exTargetsVisible = true;
    },
    saveExTargets() {
      stockExchangeTargetsSaveApi({
        productId: this.priceRow.id,
        skuKey: '',
        targets: this.exTargets.map(id => ({ targetProductId: id, targetSkuKey: '' }))
      }).then(() => {
        this.$message.success('可换商品已保存');
        this.exTargetsVisible = false;
      });
    },
    savePrice() {
      const prices = this.priceRow.levelPrices.map(p => ({ levelId: p.levelId, price: p.price }));
      stockPriceSaveApi({ productId: this.priceRow.id, prices, skuKey: this.priceSkuKey || '' }).then(() => {
        this.$message.success(this.priceSkuKey ? '该规格拿货价已保存' : '已保存');
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
/* 列表页通用规范（.toolbar/.pager/.admin-table/.op-wrap/.op-btn）已统一在 theme/styles.scss 全局定义 */
.red { color: #f56c6c; }
.ex-block { border: 1px solid #ebeef5; border-radius: 6px; padding: 10px 12px; background: #fafbfc; }
.ex-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.ex-row:last-child { margin-bottom: 0; }
.ex-label { width: 84px; color: #303133; font-size: 13px; flex-shrink: 0; }
.ex-tip { color: #909399; font-size: 12px; }
</style>
