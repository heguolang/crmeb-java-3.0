<template>
  <div class="divBg addContent-wrapper">
    <el-card shadow="never" class="mt16">
      <!-- 顶部统计条（分销商管理范式）：无筛选页，操作按钮收进统计条右侧 -->
      <div class="summary-bar summary-bar--flex">
        <span>目前有 <b>{{ tableData.length }}</b> 个分红等级；与分销商等级互不影响，可引用其升级条件再修改。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">添加分红等级</el-button>
          <el-button icon="el-icon-refresh" @click="fetchList">刷新</el-button>
        </div>
      </div>

      <!-- 列表照分销商管理定稿版式 v2：头部/尾部定宽留白列、ID 色块可复制、
           操作列 op-grid 等宽胶囊（不 fixed，避免横向溢出错位）、按钮不顶满右缘 -->
      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <!-- 头部留白列：定稿规则 30，定宽不参与弹性分配 -->
        <el-table-column width="30" />
        <!-- ID：等级主键独立一栏（非会员 ID，不做色块/复制——复制只用于会员 ID） -->
        <el-table-column label="ID" width="80" align="center">
          <template slot-scope="scope">{{ scope.row.id }}</template>
        </el-table-column>
        <!-- 分红等级 -->
        <el-table-column label="分红等级" min-width="160">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="升级条件（独立维护）" min-width="320">
          <template slot-scope="scope">
            <div v-if="conditionParts(scope.row).length" class="cond-chain">
              <template v-for="(part, i) in conditionParts(scope.row)">
                <span :key="'c' + i" class="cond-chip">{{ part.text }}</span>
                <span
                  v-if="i < conditionParts(scope.row).length - 1"
                  :key="'r' + i"
                  class="cond-rel"
                  :class="{ 'is-or': Number(part.relation) === 2 }"
                >{{ Number(part.relation) === 2 ? '或' : '与' }}</span>
              </template>
            </div>
            <span v-else class="text-muted">未设置（视为条件未配置，不参与达标）</span>
          </template>
        </el-table-column>
        <el-table-column label="分红比例（%）" min-width="120" align="center">
          <template slot-scope="scope"><span class="rate-num">{{ scope.row.dividendRate }}</span></template>
        </el-table-column>
        <el-table-column label="排序" width="70" align="center">
          <template slot-scope="scope">{{ scope.row.sort }}</template>
        </el-table-column>
        <!-- 操作：单行等宽胶囊（172 = 内边距 30 + 2 格×68 + 间隙 6），危险操作收尾 -->
        <el-table-column label="操作" width="172" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-neutral" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="mini" plain class="op-tag tint-danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <!-- 尾部留白列：定稿规则 150，按钮与表格右缘之间留白 -->
        <el-table-column width="150" />
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="880px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        size="small"
        class="level-form"
      >
        <!-- 基本信息 -->
        <div class="lf-sec">
          <div class="lf-sec__hd"><span class="lf-sec__bar"></span>基本信息</div>
          <div class="lf-sec__bd">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="分红等级名称" prop="name">
                  <el-input v-model="formData.name" maxlength="20" placeholder="如：分红A级" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分红比例（%）" prop="dividendRate">
                  <el-input-number v-model="formData.dividendRate" :min="0" :max="100" :precision="3" :controls="false" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="排序">
                  <el-input-number v-model="formData.sort" :min="0" :precision="0" :controls="false" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="引用条件">
                  <el-select v-model="copyFromLevelId" placeholder="引用分销商等级条件（可选）" clearable style="width: 100%" @change="copyConditionsFrom">
                    <el-option v-for="item in distributorLevelOptions" :key="item.id" :value="item.id" :label="item.name" />
                  </el-select>
                  <div class="form-tip">选一个分销商等级，把它的升级条件带入下方（带入后可自由修改，互不影响）</div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 升级条件 -->
        <div class="lf-sec">
          <div class="lf-sec__hd">
            <span class="lf-sec__bar"></span>升级条件
            <span class="lf-sec__n">门槛为 0 视为自动满足；全部为 0 视为条件未配置，会员不参与达标</span>
          </div>
          <div class="lf-sec__bd">
            <div class="cond-list">
              <div v-for="item in conditionFields" :key="item.key" class="cond-row">
                <span class="cond-label">{{ item.label.replace(/：$/, '') }}</span>
                <div class="cond-value">
                  <el-select
                    v-if="item.type === 'level'"
                    v-model="formData.directLevelId"
                    class="cond-select"
                    placeholder="选择分销商等级"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="level in distributorLevelOptions"
                      :key="level.id"
                      :label="level.name"
                      :value="level.id"
                    />
                  </el-select>
                  <div v-else-if="item.type === 'product'" class="cond-product">
                    <div class="cond-product__ops">
                      <el-button size="mini" type="primary" plain icon="el-icon-goods" @click="openProductPicker">
                        选择商品
                      </el-button>
                      <el-radio-group
                        v-model="formData.orderProductMode"
                        size="mini"
                        class="cond-product__mode"
                      >
                        <el-radio-button :label="1">任买一件即可</el-radio-button>
                        <el-radio-button :label="2">需全部购买</el-radio-button>
                      </el-radio-group>
                      <span v-if="selectedProducts.length" class="cond-product__sum">
                        已选 {{ selectedProducts.length }} 件
                      </span>
                    </div>
                    <div v-if="selectedProducts.length" class="cond-product__thumbs">
                      <div
                        v-for="p in selectedProducts"
                        :key="p.id"
                        class="cond-product__thumb"
                        :title="p.storeName"
                      >
                        <img v-if="p.image" :src="p.image" alt="" />
                        <div v-else class="cond-product__thumb-empty">
                          <i class="el-icon-picture-outline"></i>
                        </div>
                        <i class="el-icon-close cond-product__del" @click.stop="removeProduct(p.id)"></i>
                        <div class="cond-product__name">{{ p.storeName }}</div>
                      </div>
                    </div>
                    <div v-else class="cond-product__empty">未选择商品，该条件视为自动满足</div>
                  </div>
                  <el-input
                    v-if="item.type !== 'product'"
                    v-model="formData[item.key]"
                    class="cond-input"
                    @blur="normalizeCondition(item)"
                  >
                    <template slot="append">{{ item.unit }}</template>
                  </el-input>
                </div>
                <el-radio-group v-model="formData[item.relKey]" class="cond-relation">
                  <el-radio :label="1">与</el-radio>
                  <el-radio :label="2">或</el-radio>
                </el-radio-group>
              </div>
            </div>
            <div class="form-tip cond-tip">
              判定顺序＝条件自上而下，按每行选择的「与 / 或」连接；仅列出填写了门槛（&gt; 0）或已选择的条件。<br />
              「直推指定分销商等级」需同时选择目标等级与人数门槛；「下单指定商品」可多选商品并选择「任买一件即可 / 需全部购买」，不选商品时该条件视为自动满足。
            </div>
          </div>
        </div>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </div>
    </el-dialog>

    <!-- 选择商品弹窗：支持名称搜索 / 分类 / 分组筛选 -->
    <el-dialog :visible.sync="productPickerVisible" title="选择商品" width="900px" append-to-body>
      <goods-list
        v-if="productPickerVisible"
        :ischeckbox="true"
        :show-group="true"
        :select-ids="formData.orderProductIds"
        @getProductId="onPickProducts"
      />
    </el-dialog>
  </div>
</template>

<script>
import { dividendInfoApi, dividendLevelSaveApi, dividendLevelUpdateApi, dividendLevelDeleteApi } from '@/api/dividend';
import { productDetailApi } from '@/api/store';
import goodsList from '@/components/goodsList';

const defaultForm = () => ({
  id: null,
  name: '',
  dividendRate: 0,
  directUserCount: 0,
  directUserRelation: 1,
  teamUserCount: 0,
  teamUserRelation: 1,
  directLevelId: null,
  directLevelCount: '0',
  directLevelRelation: 1,
  totalConsumeAmount: '0.00',
  totalConsumeRelation: 1,
  totalRechargeAmount: '0.00',
  totalRechargeRelation: 1,
  teamProductAmount: '0.00',
  teamProductRelation: 1,
  directConsumeAmount: '0.00',
  directConsumeRelation: 1,
  orderProductIds: [],
  orderProductRelation: 1,
  orderProductMode: 1,
  sort: 0,
});

// 金额字段统一格式化
const fmtMoney = (v) => {
  const n = parseFloat(v);
  return Number.isNaN(n) ? '0.00' : n.toFixed(2);
};

// 升级条件字段 → 分红等级模型字段名映射（用于引用拷贝）
const CONDITION_KEYS = [
  'directUserCount', 'directUserRelation',
  'teamUserCount', 'teamUserRelation',
  'directLevelId', 'directLevelCount', 'directLevelRelation',
  'totalConsumeAmount', 'totalConsumeRelation',
  'totalRechargeAmount', 'totalRechargeRelation',
  'teamProductAmount', 'teamProductRelation',
  'directConsumeAmount', 'directConsumeRelation',
  'orderProductIds', 'orderProductRelation', 'orderProductMode',
];

export default {
  name: 'DividendLevel',
  components: { goodsList },
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      distributorLevelOptions: [],
      productPickerVisible: false,
      selectedProducts: [],
      dialogVisible: false,
      dialogTitle: '新增分红等级',
      copyFromLevelId: null,
      formData: defaultForm(),
      // 升级条件字段定义，顺序即展示顺序
      conditionFields: [
        { key: 'directUserCount', relKey: 'directUserRelation', label: '直推会员人数：', unit: '人', type: 'int' },
        { key: 'teamUserCount', relKey: 'teamUserRelation', label: '团队会员人数：', unit: '人', type: 'int' },
        { key: 'directLevelCount', relKey: 'directLevelRelation', label: '直推指定分销商等级：', unit: '人', type: 'level' },
        { key: 'totalConsumeAmount', relKey: 'totalConsumeRelation', label: '累计商城总消费额：', unit: '元', type: 'money' },
        { key: 'totalRechargeAmount', relKey: 'totalRechargeRelation', label: '总充值额：', unit: '元', type: 'money' },
        { key: 'teamProductAmount', relKey: 'teamProductRelation', label: '团队商品总消费额：', unit: '元', type: 'money' },
        { key: 'directConsumeAmount', relKey: 'directConsumeRelation', label: '直推商城消费总额：', unit: '元', type: 'money' },
        { key: 'orderProductIds', relKey: 'orderProductRelation', label: '下单指定商品：', type: 'product' },
      ],
      formRules: {
        name: [{ required: true, message: '请填写分红等级名称', trigger: 'blur' }],
        dividendRate: [{ required: true, message: '请填写分红比例', trigger: 'blur' }],
      },
    };
  },
  created() {
    this.fetchList();
    this.fetchDistributorLevels();
  },
  methods: {
    // 列表
    async fetchList() {
      this.loading = true;
      try {
        const res = await dividendInfoApi();
        this.tableData = (res.levels || []).map((l) => ({ ...l, dividendRate: Number(l.dividendRate || 0) }));
      } catch (e) {
        this.$message.error('获取分红等级列表失败');
      } finally {
        this.loading = false;
      }
    },
    // 分销商等级选项（引用条件 + 直推指定等级用）
    async fetchDistributorLevels() {
      try {
        const res = await dividendInfoApi();
        this.distributorLevelOptions = res.distributorLevels || [];
      } catch (e) {
        this.distributorLevelOptions = [];
      }
    },
    levelNameOf(levelId) {
      const hit = this.distributorLevelOptions.find((item) => item.id === levelId);
      return hit ? hit.name : `等级${levelId}`;
    },
    // 引用分销商等级条件：带入表单（可再修改，互不影响）
    copyConditionsFrom(levelId) {
      if (!levelId) return;
      const hit = this.distributorLevelOptions.find((item) => item.id === levelId);
      if (!hit) return;
      CONDITION_KEYS.forEach((key) => {
        if (key === 'directLevelId') {
          this.formData.directLevelId = hit.directLevelId != null && Number(hit.directLevelId) > 0 ? Number(hit.directLevelId) : null;
        } else if (key === 'orderProductIds') {
          this.formData.orderProductIds = this.parseOrderProductIds(hit.orderProductIds);
          this.echoSelectedProducts(this.formData.orderProductIds);
        } else if (key === 'orderProductMode') {
          this.formData.orderProductMode = hit.orderProductMode != null ? Number(hit.orderProductMode) : 1;
        } else if (key.endsWith('Relation')) {
          this.formData[key] = hit[key] != null ? Number(hit[key]) : 1;
        } else {
          this.formData[key] = hit[key] != null ? hit[key] : 0;
        }
      });
      this.$message.success(`已引用「${hit.name}」的升级条件，可继续修改`);
    },
    // 列表里的升级条件摘要（拆成条件片段 + 关系，供条件链渲染）
    conditionParts(row) {
      const parts = [];
      if (Number(row.directUserCount) > 0) {
        parts.push({ text: `直推会员人数≥${row.directUserCount}人`, relation: row.directUserRelation });
      }
      if (Number(row.teamUserCount) > 0) {
        parts.push({ text: `团队会员人数≥${row.teamUserCount}人`, relation: row.teamUserRelation });
      }
      if (Number(row.directLevelCount) > 0 && Number(row.directLevelId) > 0) {
        parts.push({
          text: `直推${this.levelNameOf(row.directLevelId)}人数≥${row.directLevelCount}人`,
          relation: row.directLevelRelation,
        });
      }
      const moneyFields = [
        ['totalConsumeAmount', '累计商城总消费额', 'totalConsumeRelation'],
        ['totalRechargeAmount', '总充值额', 'totalRechargeRelation'],
        ['teamProductAmount', '团队商品总消费额', 'teamProductRelation'],
        ['directConsumeAmount', '直推商城消费总额', 'directConsumeRelation'],
      ];
      moneyFields.forEach(([key, label, relKey]) => {
        if (Number(row[key]) > 0) {
          parts.push({ text: `${label}≥${fmtMoney(row[key])}元`, relation: row[relKey] });
        }
      });
      const productIds = this.parseOrderProductIds(row.orderProductIds);
      if (productIds.length) {
        const modeText = Number(row.orderProductMode) === 2 ? '全部购买' : '任买一件';
        parts.push({ text: `下单指定商品（${productIds.length}件·${modeText}）`, relation: row.orderProductRelation });
      }
      return parts;
    },
    // 条件输入规范化
    normalizeCondition(item) {
      const value = this.formData[item.key];
      if (item.type === 'int') {
        const digits = String(value == null ? '' : value).replace(/[^\d]/g, '');
        this.formData[item.key] = digits === '' ? 0 : parseInt(digits, 10);
      } else {
        this.formData[item.key] = fmtMoney(value);
      }
    },
    // 解析「下单指定商品」逗号分隔ID串
    parseOrderProductIds(str) {
      if (!str) return [];
      return String(str)
        .split(',')
        .map((s) => parseInt(s, 10))
        .filter((n) => !isNaN(n) && n > 0);
    },
    // 编辑回显：为已选商品补拉名称，保证标签能显示
    echoSelectedProducts(ids) {
      this.selectedProducts = (ids || []).map((id) => {
        const hit = this.selectedProducts.find((p) => p.id === id);
        return hit || { id, storeName: `商品#${id}` };
      });
      const needFetch = (ids || []).filter((id) => {
        const hit = this.selectedProducts.find((p) => p.id === id);
        return !hit || hit.storeName.indexOf('商品#') === 0;
      });
      if (!needFetch.length) return;
      Promise.all(needFetch.map((id) => productDetailApi(id).catch(() => null))).then((results) => {
        results.forEach((d) => {
          if (d && d.id) {
            const hit = this.selectedProducts.find((p) => p.id === d.id);
            if (hit) {
              hit.storeName = d.storeName;
              hit.image = d.image;
            } else {
              this.selectedProducts.push({ id: d.id, storeName: d.storeName, image: d.image });
            }
          }
        });
      });
    },
    // 打开选品弹窗
    openProductPicker() {
      this.productPickerVisible = true;
    },
    // 选品弹窗提交（images: [{image, product_id, store_name}]）
    onPickProducts(images) {
      this.selectedProducts = (images || []).map((item) => ({
        id: item.product_id,
        storeName: item.store_name || `商品#${item.product_id}`,
        image: item.image,
      }));
      this.formData.orderProductIds = this.selectedProducts.map((p) => p.id);
      this.productPickerVisible = false;
    },
    // 移除已选商品
    removeProduct(id) {
      this.selectedProducts = this.selectedProducts.filter((p) => p.id !== id);
      this.formData.orderProductIds = this.formData.orderProductIds.filter((pid) => pid !== id);
    },
    // 新增
    handleAdd() {
      this.dialogTitle = '新增分红等级';
      this.formData = defaultForm();
      this.selectedProducts = [];
      this.copyFromLevelId = null;
      this.dialogVisible = true;
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate());
    },
    // 编辑
    handleEdit(row) {
      this.dialogTitle = '编辑分红等级';
      this.formData = {
        ...defaultForm(),
        id: row.id,
        name: row.name || '',
        dividendRate: Number(row.dividendRate) || 0,
        sort: Number(row.sort) || 0,
        directUserCount: row.directUserCount != null ? Number(row.directUserCount) : 0,
        directUserRelation: row.directUserRelation != null ? Number(row.directUserRelation) : 1,
        teamUserCount: row.teamUserCount != null ? Number(row.teamUserCount) : 0,
        teamUserRelation: row.teamUserRelation != null ? Number(row.teamUserRelation) : 1,
        directLevelId: row.directLevelId != null && Number(row.directLevelId) > 0 ? Number(row.directLevelId) : null,
        directLevelCount: row.directLevelCount != null ? Number(row.directLevelCount) : 0,
        directLevelRelation: row.directLevelRelation != null ? Number(row.directLevelRelation) : 1,
        totalConsumeAmount: fmtMoney(row.totalConsumeAmount),
        totalConsumeRelation: row.totalConsumeRelation != null ? Number(row.totalConsumeRelation) : 1,
        totalRechargeAmount: fmtMoney(row.totalRechargeAmount),
        totalRechargeRelation: row.totalRechargeRelation != null ? Number(row.totalRechargeRelation) : 1,
        teamProductAmount: fmtMoney(row.teamProductAmount),
        teamProductRelation: row.teamProductRelation != null ? Number(row.teamProductRelation) : 1,
        directConsumeAmount: fmtMoney(row.directConsumeAmount),
        directConsumeRelation: row.directConsumeRelation != null ? Number(row.directConsumeRelation) : 1,
        orderProductIds: this.parseOrderProductIds(row.orderProductIds),
        orderProductRelation: row.orderProductRelation != null ? Number(row.orderProductRelation) : 1,
        orderProductMode: row.orderProductMode != null ? Number(row.orderProductMode) : 1,
      };
      this.echoSelectedProducts(this.formData.orderProductIds);
      this.copyFromLevelId = null;
      this.dialogVisible = true;
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate());
    },
    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除分红等级「${row.name}」吗？已产生的结算记录不受影响。`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          try {
            await dividendLevelDeleteApi(row.id);
            this.$message.success('删除成功');
            this.fetchList();
          } catch (e) {
            this.$message.error('删除失败');
          }
        })
        .catch(() => {});
    },
    // 提交
    handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;
        const directLevelCount = Number(this.formData.directLevelCount) || 0;
        if (directLevelCount > 0 && !this.formData.directLevelId) {
          this.$message.warning('直推指定等级人数大于 0 时，必须选择指定的分销商等级');
          return;
        }
        this.submitLoading = true;
        const payload = {
          name: this.formData.name,
          dividendRate: Number(this.formData.dividendRate) || 0,
          sort: Number(this.formData.sort) || 0,
          directUserCount: Number(this.formData.directUserCount) || 0,
          directUserRelation: this.formData.directUserRelation,
          teamUserCount: Number(this.formData.teamUserCount) || 0,
          teamUserRelation: this.formData.teamUserRelation,
          directLevelId: this.formData.directLevelId || 0,
          directLevelCount,
          directLevelRelation: this.formData.directLevelRelation,
          totalConsumeAmount: Number(this.formData.totalConsumeAmount) || 0,
          totalConsumeRelation: this.formData.totalConsumeRelation,
          totalRechargeAmount: Number(this.formData.totalRechargeAmount) || 0,
          totalRechargeRelation: this.formData.totalRechargeRelation,
          teamProductAmount: Number(this.formData.teamProductAmount) || 0,
          teamProductRelation: this.formData.teamProductRelation,
          directConsumeAmount: Number(this.formData.directConsumeAmount) || 0,
          directConsumeRelation: this.formData.directConsumeRelation,
          orderProductIds: (this.formData.orderProductIds || []).join(','),
          orderProductRelation: this.formData.orderProductRelation,
          orderProductMode: this.formData.orderProductMode,
        };
        try {
          if (this.formData.id) {
            await dividendLevelUpdateApi(payload);
          } else {
            await dividendLevelSaveApi(payload);
          }
          this.$message.success(this.formData.id ? '编辑成功' : '新增成功');
          this.dialogVisible = false;
          this.fetchList();
        } catch (e) {
          // 业务错误信息由请求拦截统一提示
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handleDialogClose() {
      this.$refs.formRef && this.$refs.formRef.resetFields();
      this.formData = defaultForm();
      this.selectedProducts = [];
      this.copyFromLevelId = null;
      this.productPickerVisible = false;
    },
  },
};
</script>

<style scoped lang="scss">
/* ---------- 列表页 ---------- */
/* 列表范式（summary-bar / table-lg / op-grid / op-tag / info-line）
   已统一提升到 theme/styles.scss 全局定义，本页不再写 scoped 副本 */
/* 无筛选页变体：统计条左侧文案 + 右侧操作按钮 */
.summary-bar--flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
/* 操作列：胶囊强制横排不换行（与分销商管理同款） */
::v-deep .op-cell .op-tag {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 升级条件链：条件标签 + 与/或连接符 */
.cond-chain {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  .cond-chip {
    display: inline-block;
    padding: 3px 8px;
    border: 1px solid #e5e6eb;
    border-radius: 4px;
    background: #f5f7fa;
    font-size: 12px;
    color: #606266;
    line-height: 16px;
    white-space: nowrap;
  }
  .cond-rel {
    display: inline-block;
    padding: 1px 6px;
    border-radius: 3px;
    background: #edf3ff;
    font-size: 11px;
    font-weight: 600;
    color: #1f5fd6;
    line-height: 16px;
    &.is-or {
      background: #fdf0e6;
      color: #e6782e;
    }
  }
}
.rate-num {
  font-size: 15px;
  font-weight: 600;
  color: #1f5fd6;
}
.text-muted {
  color: #909399;
}

/* ---------- 弹窗：与分销商等级页同一套 ---------- */
.level-form {
  ::v-deep .el-form-item {
    margin-bottom: 14px;
  }
  ::v-deep .el-form-item__label {
    padding: 0 0 4px;
    font-size: 13px;
    line-height: 18px;
    font-weight: 600;
    color: #303133;
  }
  ::v-deep .el-input-number .el-input__inner {
    text-align: left;
  }
}

/* 分区卡片：基本信息 / 升级条件 */
.lf-sec {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;

  &:last-child {
    margin-bottom: 0;
  }

  &__hd {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    border-bottom: 1px solid #f0f2f5;
    background: linear-gradient(90deg, var(--prev-color-primary-light-9, #ecf5ff) 0%, rgba(255, 255, 255, 0) 62%);
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;

    .lf-sec__n {
      margin-left: 10px;
      font-size: 12px;
      font-weight: 400;
      color: #a8abb2;
    }
  }

  &__bar {
    width: 3px;
    height: 14px;
    margin-right: 8px;
    border-radius: 2px;
    background: var(--prev-color-primary, #0256ff);
  }

  &__bd {
    padding: 16px 16px 4px;

    .cond-tip {
      margin-bottom: 12px;
    }
  }
}

.dialog-footer {
  text-align: right;
}

/* ---------- 升级条件行：条件名 + 数值(带单位) + 与/或 ---------- */
.cond-list {
  margin-bottom: 10px;
}
.cond-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.cond-label {
  flex: 0 0 162px;
  width: 162px;
  padding-right: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  line-height: 32px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cond-value {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  min-width: 0;

  .cond-select {
    width: 100%;
  }
  .cond-input {
    width: 100%;

    ::v-deep .el-input__inner {
      text-align: right;
    }
    ::v-deep .el-input-group__append {
      padding: 0 12px;
      background: #f5f7fa;
      color: #909399;
    }
  }
}
.cond-product {
  &__ops {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  &__mode {
    margin-left: 4px;
  }
  &__sum {
    color: #909399;
    font-size: 12px;
  }
  &__thumbs {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
  &__thumb {
    width: 72px;
    position: relative;

    img {
      width: 72px;
      height: 72px;
      object-fit: cover;
      border-radius: 4px;
      display: block;
    }

    &-empty {
      width: 72px;
      height: 72px;
      border-radius: 4px;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;
      font-size: 20px;
    }

    .cond-product__del {
      position: absolute;
      top: -6px;
      right: -6px;
      background: #f56c6c;
      color: #fff;
      border-radius: 50%;
      width: 18px;
      height: 18px;
      line-height: 18px;
      text-align: center;
      font-size: 12px;
      cursor: pointer;
    }

    .cond-product__name {
      font-size: 12px;
      color: #606266;
      line-height: 16px;
      margin-top: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  &__empty {
    color: #909399;
    font-size: 12px;
    margin-top: 4px;
  }
}
.cond-relation {
  flex-shrink: 0;
  margin-left: 12px;
  white-space: nowrap;

  ::v-deep .el-radio {
    margin-right: 10px;

    &:last-child {
      margin-right: 0;
    }
  }
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 18px;
  margin-top: 2px;
}
.cond-tip {
  margin: 2px 0 0;
  padding: 8px 12px;
  border-radius: 4px;
  background: #f7f8fa;
}
</style>
