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
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="产品信息" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="cell-main">{{ scope.row.productName || ('商品ID ' + scope.row.productId) }}</div>
            <div class="cell-sub">{{ scope.row.skuKey || '整品' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="会员信息" width="150">
          <template slot-scope="scope">
            <template v-if="scope.row.uid">
              <div class="cell-main">{{ scope.row.nickName || '-' }}</div>
              <div class="cell-sub">{{ scope.row.phone }}（UID {{ scope.row.uid }}）</div>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="linkNo" label="关联单号" width="170" />
        <el-table-column label="类型" width="130">
          <template slot-scope="scope">{{ typeName(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column prop="changeNum" label="变动" width="80">
          <template slot-scope="scope"><span :style="{color: scope.row.changeNum<0?'#f56c6c':'#67c23a'}">{{ scope.row.changeNum }}</span></template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变动前" width="80" />
        <el-table-column prop="afterStock" label="变动后" width="80" />
        <el-table-column prop="mark" label="备注" min-width="130" show-overflow-tooltip />
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
    <el-dialog title="规格与订货设置" :visible.sync="priceVisible" width="820px" append-to-body :close-on-click-modal="false" custom-class="spec-dialog">
      <div v-if="priceRow" class="sp-head">
        <img class="sp-thumb" :src="priceRow.image" />
        <div class="sp-head-info">
          <div class="sp-name">{{ priceRow.storeName }}</div>
          <div class="sp-meta">零售价 ¥{{ priceRow.price }} · 云仓总库存 {{ priceRow.stock }} 件</div>
        </div>
      </div>

      <div v-if="priceRow" class="sp-card">
        <div class="sp-card-title">支持的库存类型</div>
        <div class="sp-inline">
          <label class="sp-switch-item">
            <el-switch v-model="stockTypeForm.supportVirtual" @change="saveStockType" />
            <span>虚拟库存</span>
          </label>
          <span class="sp-tip">付款即入账虚拟库存，可提货 / 换货</span>
          <label class="sp-switch-item" style="margin-left:24px">
            <el-switch v-model="stockTypeForm.supportPhysical" @change="saveStockType" />
            <span>实体库存</span>
          </label>
          <span class="sp-tip">走上级审核 → 发货流程</span>
        </div>
      </div>

      <div v-if="priceRow" class="sp-card">
        <div class="sp-card-title">规格选择<span class="sp-sub">（拿货价与换货设置都按规格保存）</span></div>
        <div class="sp-chips">
          <span class="sp-chip" :class="{ active: priceSkuKey === '' }" @click="switchSku('')">整品（不区分规格）</span>
          <span v-for="s in skuOptions" :key="s.skuKey" class="sp-chip"
                :class="{ active: priceSkuKey === s.skuKey }" @click="switchSku(s.skuKey)">
            {{ s.attrValueText || s.skuKey }}
          </span>
          <span v-if="!skuOptions.length" class="sp-tip">该商品没有多规格，按整品配置即可</span>
        </div>
        <div v-if="priceSkuKey" class="sp-sku-meta">
          当前规格云仓库存 {{ currentSku.stock }} 件 · 规格零售价 ¥{{ currentSku.price }}
        </div>
      </div>

      <div v-if="priceRow" class="sp-cols">
        <div class="sp-col">
          <div class="sp-card-title">各层级拿货价<span class="sp-sub">（{{ priceSkuKey ? currentSku.attrValueText || currentSku.skuKey : '整品' }}）</span></div>
          <el-table class="admin-table" :data="priceRow.levelPrices" size="small">
            <el-table-column prop="levelName" label="层级" width="100" />
            <el-table-column label="拿货价">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="mini" style="width: 140px" placeholder="留空=默认折扣" />
                <div class="sp-hint">默认 {{ scope.row.discount }}% = ¥{{ (priceRow.price * scope.row.discount / 100).toFixed(2) }}</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="sp-col">
          <div class="sp-card-title">换货设置<span class="sp-sub">（仅作用于{{ priceSkuKey ? '当前规格' : '整品' }}）</span></div>
          <div class="sp-inline">
            <label class="sp-switch-item">
              <el-switch v-model="exForm.enable" @change="saveExchangeConfig" />
              <span>{{ exForm.enable ? '允许换货' : '不允许换货' }}</span>
            </label>
          </div>
          <div class="sp-inline" style="margin-top:12px">
            <el-button size="mini" :disabled="!exForm.enable" @click="openExTargets">
              设置可换入商品（已选 {{ exTargets.length }}）
            </el-button>
          </div>
          <div class="sp-tip" style="display:block;margin-top:10px;line-height:1.7">
            · 只有「允许换货」的商品/规格，会员端才显示换货入口<br />
            · 只能换入清单内的商品，且换入拿货价不得低于原商品<br />
            · 规格未单独配置时，沿用整品设置
          </div>
        </div>
      </div>

      <div slot="footer">
        <el-button size="small" @click="priceVisible = false">关闭</el-button>
        <el-button size="small" type="primary" @click="savePrice">保存拿货价</el-button>
      </div>
    </el-dialog>

    <el-dialog title="设置可换入商品" :visible.sync="exTargetsVisible" width="520px" append-to-body :close-on-click-modal="false">
      <div class="sp-tip" style="display:block;margin-bottom:10px">
        来源：{{ priceRow && priceRow.storeName }}<span v-if="priceSkuKey">（{{ currentSku.attrValueText || currentSku.skuKey }}）</span>
        —— 允许换入以下商品（可多选，一次换货只能选其中一个）
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
  computed: {
    currentSku() {
      const list = this.skuOptions || [];
      return list.find(s => s.skuKey === this.priceSkuKey) || {};
    }
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
      // 快照商品级价，规格 tab 之间来回切换时用于还原
      (this.priceRow.levelPrices || []).forEach(p => { p._basePrice = p.price; });
      this.priceVisible = true;
      this.loadExchange(row.id);
    },
    loadExchange(productId) {
      this.exForm = { enable: true };
      this.exTargets = [];
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
        // 只有多规格商品才展示规格选择（单规格=默认规格，按整品配置即可）
        const raw = res || [];
        const list = raw.length > 1 ? raw : [];
        this.skuOptions = list.map(s => {
          let text = s.attrValue || s.skuKey;
          try {
            const obj = typeof s.attrValue === 'string' ? JSON.parse(s.attrValue) : s.attrValue;
            if (obj && typeof obj === 'object') {
              text = Object.keys(obj).map(k => obj[k]).join(' / ');
            }
          } catch (e) {
            // 解析失败时保留原文
          }
          return { ...s, attrValueText: text };
        });
        this.switchSku('');
      }).catch(() => { this.switchSku(''); });
    },
    saveStockType() {
      stockProductStockTypeSaveApi({
        productId: this.priceRow.id,
        supportVirtual: this.stockTypeForm.supportVirtual,
        supportPhysical: this.stockTypeForm.supportPhysical
      }).then(() => this.$message.success('已保存'));
    },
    switchSku(sku) {
      this.priceSkuKey = sku || '';
      const lv = this.priceRow.levelPrices || [];
      if (!this.priceSkuKey) {
        // 切回整品：还原商品级价
        lv.forEach(p => { p.price = p._basePrice === undefined ? null : p._basePrice; });
      } else {
        stockProductSkuPriceApi(this.priceRow.id, this.priceSkuKey).then(res => {
          const map = {};
          (res || []).forEach(x => { map[x.levelId] = x.price; });
          lv.forEach(p => { p.price = map[p.levelId] !== undefined ? Number(map[p.levelId]) : null; });
        }).catch(() => {});
      }
      // 换货设置按「当前规格」加载（未配置则视为允许，沿用整品兜底）
      this.exForm = { enable: true };
      this.exTargets = [];
      stockExchangeConfigApi(this.priceRow.id).then(res => {
        const cfg = (res || []).find(x => (x.skuKey || '') === this.priceSkuKey);
        this.exForm.enable = cfg ? !!cfg.enable : true;
      }).catch(() => {});
      stockExchangeTargetsApi(this.priceRow.id, this.priceSkuKey).then(res => {
        this.exTargets = (res || []).map(t => t.targetProductId);
      }).catch(() => {});
    },
    saveExchangeConfig() {
      stockExchangeConfigSaveApi({
        productId: this.priceRow.id,
        skuKey: this.priceSkuKey,
        enable: this.exForm.enable,
        minTargetPrice: 0
      }).then(() => this.$message.success((this.priceSkuKey ? '当前规格' : '整品') + (this.exForm.enable ? '已开放换货' : '已关闭换货')));
    },
    openExTargets() {
      this.exTargetsVisible = true;
    },
    saveExTargets() {
      stockExchangeTargetsSaveApi({
        productId: this.priceRow.id,
        skuKey: this.priceSkuKey,
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
/* 规格与订货设置弹窗 */
.sp-head { display: flex; align-items: center; padding-bottom: 14px; border-bottom: 1px solid #ebeef5; }
.sp-thumb { width: 56px; height: 56px; border-radius: 6px; object-fit: cover; background: #f5f7fa; flex-shrink: 0; }
.sp-head-info { margin-left: 12px; overflow: hidden; }
.sp-name { font-size: 14px; font-weight: 600; color: #303133; line-height: 1.4; }
.sp-meta { font-size: 12px; color: #909399; margin-top: 4px; }
.sp-card { margin-top: 16px; padding: 14px 16px; background: #fafbfc; border: 1px solid #ebeef5; border-radius: 6px; }
.sp-card-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 10px; }
.sp-sub { font-weight: 400; color: #909399; font-size: 12px; margin-left: 6px; }
.sp-inline { display: flex; align-items: center; flex-wrap: wrap; }
.sp-switch-item { display: inline-flex; align-items: center; font-size: 13px; color: #303133; }
.sp-switch-item span { margin-left: 8px; }
.sp-tip { color: #909399; font-size: 12px; margin-left: 10px; }
.sp-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.sp-chip { padding: 5px 14px; border: 1px solid #dcdfe6; border-radius: 14px; font-size: 12px; color: #606266; background: #fff; cursor: pointer; transition: all .15s; }
.sp-chip:hover { border-color: #2b6fe3; color: #2b6fe3; }
.sp-chip.active { background: #2b6fe3; border-color: #2b6fe3; color: #fff; }
.sp-sku-meta { margin-top: 10px; font-size: 12px; color: #606266; }
.sp-cols { display: flex; gap: 16px; margin-top: 16px; }
.sp-col { flex: 1; min-width: 0; padding: 14px 16px; background: #fff; border: 1px solid #ebeef5; border-radius: 6px; }
.sp-hint { font-size: 12px; color: #909399; line-height: 1.5; }
.cell-main { font-size: 13px; color: #303133; }
.cell-sub { font-size: 12px; color: #909399; }
</style>
