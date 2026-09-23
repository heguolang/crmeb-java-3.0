<template>
  <div class="divBox distributor-level-page">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">分销商等级</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增等级</el-button>
      </div>

      <el-alert
        class="mb15"
        type="info"
        :closable="false"
        show-icon
        title="分销商等级独立于会员等级，等级名称、等级权重、返佣比例与升级条件均在此单独维护。升级条件按「与 / 或」顺序组合判定，门槛为 0 的条件视为自动满足。"
      />

      <el-table v-loading="loading" :data="tableData" border stripe size="mini" style="width: 100%">
        <el-table-column prop="name" label="等级名称" min-width="110" show-overflow-tooltip />
        <el-table-column prop="grade" label="等级权重" width="90" align="center" />
        <el-table-column label="自购返佣(%)" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.selfBrokerageRate || 0 }}</template>
        </el-table-column>
        <el-table-column label="一级返佣(%)" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.brokerageRateOne || 0 }}</template>
        </el-table-column>
        <el-table-column label="二级返佣(%)" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.brokerageRateTwo || 0 }}</template>
        </el-table-column>
        <el-table-column label="升级条件" min-width="320" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="conditionSummary(scope.row)">{{ conditionSummary(scope.row) }}</span>
            <span v-else class="text-muted">未设置（点击编辑配置）</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isShow"
              :active-value="true"
              :inactive-value="false"
              active-text="显示"
              inactive-text="隐藏"
              @click.native="onChangeShow(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template slot-scope="scope">
            <a @click="handleEdit(scope.row)">编辑</a>
            <el-divider direction="vertical"></el-divider>
            <a @click="handleDelete(scope.row)">删除</a>
          </template>
        </el-table-column>
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
        <!-- 基础信息 -->
        <div class="lf-sec">
          <div class="lf-sec__hd"><span class="lf-sec__bar"></span>基础信息</div>
          <div class="lf-sec__bd">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="等级名称" prop="name">
                  <el-input v-model="formData.name" maxlength="50" placeholder="如：金牌分销商" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="等级权重" prop="grade">
                  <el-input-number
                    v-model="formData.grade"
                    :min="1"
                    :max="9999"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="自购返佣（%）" prop="selfBrokerageRate">
                  <el-input-number
                    v-model="formData.selfBrokerageRate"
                    :min="0"
                    :max="100"
                    :precision="1"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="一级返佣（%）" prop="brokerageRateOne">
                  <el-input-number
                    v-model="formData.brokerageRateOne"
                    :min="0"
                    :max="100"
                    :precision="1"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="二级返佣（%）" prop="brokerageRateTwo">
                  <el-input-number
                    v-model="formData.brokerageRateTwo"
                    :min="0"
                    :max="100"
                    :precision="1"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 升级条件 -->
        <div class="lf-sec">
          <div class="lf-sec__hd">
            <span class="lf-sec__bar"></span>升级条件
            <span class="lf-sec__n">门槛为 0 视为自动满足</span>
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
              判定顺序：直推会员人数 「与/或」 团队会员人数 「与/或」 直推指定分销商等级 「与/或」 累计商城总消费额 「与/或」
              总充值额 「与/或」 团队商品总消费额 「与/或」 直推商城消费总额 「与/或」 下单指定商品。
              「直推指定分销商等级」需同时选择目标等级与人数门槛；「下单指定商品」可多选商品并选择「任买一件即可 / 需全部购买」，
              不选商品时该条件视为自动满足。
            </div>
          </div>
        </div>

        <!-- 展示设置 -->
        <div class="lf-sec">
          <div class="lf-sec__hd"><span class="lf-sec__bar"></span>展示设置</div>
          <div class="lf-sec__bd">
            <el-form-item label="是否显示" prop="isShow">
              <el-switch v-model="formData.isShow" active-text="显示" inactive-text="隐藏" />
            </el-form-item>
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
import {
  distributorLevelListApi,
  distributorLevelInfoApi,
  distributorLevelSaveApi,
  distributorLevelUpdateApi,
  distributorLevelDeleteApi,
  distributorLevelUseApi,
} from '@/api/distributorLevel';
import { productDetailApi } from '@/api/store';
import goodsList from '@/components/goodsList';

const defaultForm = () => ({
  id: null,
  name: '',
  grade: 1,
  selfBrokerageRate: 0,
  brokerageRateOne: 0,
  brokerageRateTwo: 0,
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
  isShow: true,
});

// 金额字段统一格式化
const fmtMoney = (v) => {
  const n = parseFloat(v);
  return Number.isNaN(n) ? '0.00' : n.toFixed(2);
};

export default {
  name: 'DistributorLevel',
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
      dialogTitle: '新增分销商等级',
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
        name: [{ required: true, message: '请填写等级名称', trigger: 'blur' }],
        grade: [{ required: true, message: '请填写等级权重', trigger: 'change' }],
        selfBrokerageRate: [{ required: true, message: '请填写自购返佣比例', trigger: 'change' }],
        brokerageRateOne: [{ required: true, message: '请填写一级返佣比例', trigger: 'change' }],
        brokerageRateTwo: [{ required: true, message: '请填写二级返佣比例', trigger: 'change' }],
      },
    };
  },
  created() {
    this.fetchList();
    this.fetchUserLevels();
  },
  methods: {
    // 列表
    async fetchList() {
      this.loading = true;
      try {
        this.tableData = await distributorLevelListApi();
      } catch (e) {
        this.$message.error('获取分销商等级列表失败');
      } finally {
        this.loading = false;
      }
    },
    // 分销商等级选项，仅用于「直推指定等级」条件
    async fetchUserLevels() {
      try {
        this.distributorLevelOptions = (await distributorLevelListApi()) || [];
      } catch (e) {
        this.distributorLevelOptions = [];
      }
    },
    levelNameOf(levelId) {
      const hit = this.distributorLevelOptions.find((item) => item.id === levelId);
      return hit ? hit.name : `等级${levelId}`;
    },
    // 列表里的升级条件摘要
    conditionSummary(row) {
      const rel = (v) => (Number(v) === 2 ? '或' : '与');
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
      if (!parts.length) return '';
      return parts
        .map((item, index) => (index === 0 ? item.text : `${rel(item.relation)} ${item.text}`))
        .join(' ');
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
      this.dialogTitle = '新增分销商等级';
      this.formData = defaultForm();
      this.selectedProducts = [];
      this.dialogVisible = true;
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate());
    },
    // 编辑
    async handleEdit(row) {
      this.dialogTitle = '编辑分销商等级';
      this.submitLoading = true;
      try {
        const data = await distributorLevelInfoApi(row.id);
        this.formData = {
          ...defaultForm(),
          id: data.id,
          name: data.name || '',
          grade: data.grade != null ? Number(data.grade) : 1,
          selfBrokerageRate: data.selfBrokerageRate != null ? Number(data.selfBrokerageRate) : 0,
          brokerageRateOne: data.brokerageRateOne != null ? Number(data.brokerageRateOne) : 0,
          brokerageRateTwo: data.brokerageRateTwo != null ? Number(data.brokerageRateTwo) : 0,
          directUserCount: data.directUserCount != null ? Number(data.directUserCount) : 0,
          directUserRelation: data.directUserRelation != null ? Number(data.directUserRelation) : 1,
          teamUserCount: data.teamUserCount != null ? Number(data.teamUserCount) : 0,
          teamUserRelation: data.teamUserRelation != null ? Number(data.teamUserRelation) : 1,
          directLevelId: data.directLevelId != null && Number(data.directLevelId) > 0 ? Number(data.directLevelId) : null,
          directLevelCount: data.directLevelCount != null ? Number(data.directLevelCount) : 0,
          directLevelRelation: data.directLevelRelation != null ? Number(data.directLevelRelation) : 1,
          totalConsumeAmount: fmtMoney(data.totalConsumeAmount),
          totalConsumeRelation: data.totalConsumeRelation != null ? Number(data.totalConsumeRelation) : 1,
          totalRechargeAmount: fmtMoney(data.totalRechargeAmount),
          totalRechargeRelation: data.totalRechargeRelation != null ? Number(data.totalRechargeRelation) : 1,
          teamProductAmount: fmtMoney(data.teamProductAmount),
          teamProductRelation: data.teamProductRelation != null ? Number(data.teamProductRelation) : 1,
          directConsumeAmount: fmtMoney(data.directConsumeAmount),
          directConsumeRelation: data.directConsumeRelation != null ? Number(data.directConsumeRelation) : 1,
          orderProductIds: this.parseOrderProductIds(data.orderProductIds),
          orderProductRelation: data.orderProductRelation != null ? Number(data.orderProductRelation) : 1,
          orderProductMode: data.orderProductMode != null ? Number(data.orderProductMode) : 1,
          isShow: data.isShow !== false,
        };
        this.echoSelectedProducts(this.formData.orderProductIds);
        this.dialogVisible = true;
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate());
      } catch (e) {
        this.$message.error('获取分销商等级详情失败');
      } finally {
        this.submitLoading = false;
      }
    },
    // 状态切换
    onChangeShow(row) {
      const next = row.isShow;
      distributorLevelUseApi(row.id, next)
        .then(() => {
          this.$message.success(next ? '已显示' : '已隐藏');
        })
        .catch(() => {
          row.isShow = !next;
        });
    },
    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除分销商等级「${row.name}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          try {
            await distributorLevelDeleteApi(row.id);
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
          grade: this.formData.grade,
          selfBrokerageRate: this.formData.selfBrokerageRate,
          brokerageRateOne: this.formData.brokerageRateOne,
          brokerageRateTwo: this.formData.brokerageRateTwo,
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
          orderProductIds: this.formData.orderProductIds || [],
          orderProductRelation: this.formData.orderProductRelation,
          orderProductMode: this.formData.orderProductMode,
          isShow: this.formData.isShow !== false,
        };
        try {
          if (this.formData.id) {
            await distributorLevelUpdateApi(this.formData.id, payload);
          } else {
            await distributorLevelSaveApi(payload);
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
      this.productPickerVisible = false;
    },
  },
};
</script>

<style scoped lang="scss">
.distributor-level-page {
  .mb15 {
    margin-bottom: 15px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.text-muted {
  color: #c0c4cc;
}

.level-form {
  ::v-deep .el-form-item {
    margin-bottom: 14px;
  }

  /* 顶部标签：统一字号/字重/行高，杜绝大小不一 */
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

/* ---------- 分区卡片：基础信息 / 升级条件 / 展示设置 ---------- */
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
  }

  &__bar {
    width: 3px;
    height: 14px;
    margin-right: 8px;
    border-radius: 2px;
    background: var(--prev-color-primary, #0256ff);
  }

  &__n {
    margin-left: 10px;
    font-size: 12px;
    font-weight: 400;
    color: #a8abb2;
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
}

.cond-select {
  flex: 0 0 150px;
  width: 150px;
  margin-right: 10px;
}

.cond-select--product {
  flex: 1 1 auto;
  width: 100%;
  min-width: 0;
}

/* 下单指定商品：选择按钮 + 判定模式 + 缩略图 */
.cond-product {
  flex: 1 1 auto;
  min-width: 0;
}

.cond-product__ops {
  display: flex;
  align-items: center;
  flex-wrap: wrap;

  .cond-product__mode {
    margin-left: 12px;
  }

  .cond-product__sum {
    margin-left: 12px;
    font-size: 12px;
    color: #909399;
  }
}

.cond-product__thumbs {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
  gap: 8px;
}

.cond-product__thumb {
  position: relative;
  width: 56px;
  height: 56px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &-empty {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
    font-size: 20px;
  }

  &:hover {
    border-color: var(--prev-color-primary, #0256ff);
    box-shadow: 0 2px 8px rgba(2, 86, 255, 0.16);
  }
}

.cond-product__del {
  position: absolute;
  top: 0;
  right: 0;
  width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-bottom-left-radius: 4px;
  opacity: 0;
  transition: opacity 0.18s ease;

  &:hover {
    background: #f56c6c;
  }
}

.cond-product__thumb:hover .cond-product__del {
  opacity: 1;
}

/* 名称悬浮显示（默认藏在底部） */
.cond-product__name {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 2px 4px;
  font-size: 10px;
  line-height: 14px;
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  opacity: 0;
  transform: translateY(100%);
  transition: all 0.18s ease;
}

.cond-product__thumb:hover .cond-product__name {
  opacity: 1;
  transform: translateY(0);
}

.cond-product__empty {
  margin-top: 8px;
  font-size: 12px;
  color: #a8abb2;
}

.cond-input {
  flex: 1 1 auto;
  min-width: 0;

  ::v-deep .el-input__inner {
    text-align: right;
  }
  ::v-deep .el-input-group__append {
    padding: 0 12px;
    background: #f5f7fa;
    color: #909399;
  }
}

.cond-relation {
  flex: 0 0 108px;
  margin-left: 16px;
  white-space: nowrap;
  text-align: right;

  ::v-deep .el-radio {
    margin-right: 10px;
    &:last-child {
      margin-right: 0;
    }
  }
}

.form-tip {
  font-size: 12px;
  line-height: 20px;
  color: #909399;
}

.cond-tip {
  margin: 2px 0 0;
  padding: 8px 12px;
  border-radius: 4px;
  background: #f7f8fa;
}
</style>
