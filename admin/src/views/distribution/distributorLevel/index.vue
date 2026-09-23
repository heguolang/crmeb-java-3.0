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
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              class="text-danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="860px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="150px" class="level-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级名称：" prop="name">
              <el-input v-model="formData.name" maxlength="50" placeholder="如：金牌分销商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级权重：" prop="grade">
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

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="自购返佣(%)：" prop="selfBrokerageRate">
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
            <el-form-item label="一级返佣(%)：" prop="brokerageRateOne">
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
            <el-form-item label="二级返佣(%)：" prop="brokerageRateTwo">
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

        <el-divider content-position="left">升级条件</el-divider>

        <div class="cond-list">
          <div v-for="item in conditionFields" :key="item.key" class="cond-row">
            <span class="cond-label">{{ item.label }}</span>
            <div class="cond-value">
              <el-select
                v-if="item.type === 'level'"
                v-model="formData.directLevelId"
                class="cond-select"
                placeholder="选择等级"
                clearable
                filterable
              >
                <el-option
                  v-for="level in userLevelOptions"
                  :key="level.id"
                  :label="level.name"
                  :value="level.id"
                />
              </el-select>
              <el-input
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
          判定顺序：直推会员人数 「与/或」 团队会员人数 「与/或」 直推指定等级 「与/或」 累计商城总消费额 「与/或」
          总充值额 「与/或」 团队商品总消费额 「与/或」 直推商城消费总额 「与/或」 直推会员商城消费总额。
          门槛为 0 的条件视为自动满足；「直推指定等级」需同时选择目标等级与人数门槛。
        </div>

        <el-form-item label="是否显示：" prop="isShow">
          <el-switch v-model="formData.isShow" active-text="显示" inactive-text="隐藏" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </div>
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
import { levelAllApi } from '@/api/user';

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
  directUserConsumeAmount: '0.00',
  directUserConsumeRelation: 1,
  isShow: true,
});

// 金额字段统一格式化
const fmtMoney = (v) => {
  const n = parseFloat(v);
  return Number.isNaN(n) ? '0.00' : n.toFixed(2);
};

export default {
  name: 'DistributorLevel',
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      userLevelOptions: [],
      dialogVisible: false,
      dialogTitle: '新增分销商等级',
      formData: defaultForm(),
      // 升级条件字段定义，顺序即展示顺序
      conditionFields: [
        { key: 'directUserCount', relKey: 'directUserRelation', label: '直推会员人数：', unit: '人', type: 'int' },
        { key: 'teamUserCount', relKey: 'teamUserRelation', label: '团队会员人数：', unit: '人', type: 'int' },
        { key: 'directLevelCount', relKey: 'directLevelRelation', label: '直推指定等级：', unit: '人', type: 'level' },
        { key: 'totalConsumeAmount', relKey: 'totalConsumeRelation', label: '累计商城总消费额：', unit: '元', type: 'money' },
        { key: 'totalRechargeAmount', relKey: 'totalRechargeRelation', label: '总充值额：', unit: '元', type: 'money' },
        { key: 'teamProductAmount', relKey: 'teamProductRelation', label: '团队商品总消费额：', unit: '元', type: 'money' },
        { key: 'directConsumeAmount', relKey: 'directConsumeRelation', label: '直推商城消费总额：', unit: '元', type: 'money' },
        { key: 'directUserConsumeAmount', relKey: 'directUserConsumeRelation', label: '直推会员商城消费总额：', unit: '元', type: 'money' },
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
    // 会员等级选项，仅用于「直推指定等级」条件
    async fetchUserLevels() {
      try {
        this.userLevelOptions = (await levelAllApi()) || [];
      } catch (e) {
        this.userLevelOptions = [];
      }
    },
    userNameOf(levelId) {
      const hit = this.userLevelOptions.find((item) => item.id === levelId);
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
          text: `直推${this.userNameOf(row.directLevelId)}人数≥${row.directLevelCount}人`,
          relation: row.directLevelRelation,
        });
      }
      const moneyFields = [
        ['totalConsumeAmount', '累计商城总消费额', 'totalConsumeRelation'],
        ['totalRechargeAmount', '总充值额', 'totalRechargeRelation'],
        ['teamProductAmount', '团队商品总消费额', 'teamProductRelation'],
        ['directConsumeAmount', '直推商城消费总额', 'directConsumeRelation'],
        ['directUserConsumeAmount', '直推会员商城消费总额', 'directUserConsumeRelation'],
      ];
      moneyFields.forEach(([key, label, relKey]) => {
        if (Number(row[key]) > 0) {
          parts.push({ text: `${label}≥${fmtMoney(row[key])}元`, relation: row[relKey] });
        }
      });
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
    // 新增
    handleAdd() {
      this.dialogTitle = '新增分销商等级';
      this.formData = defaultForm();
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
          directUserConsumeAmount: fmtMoney(data.directUserConsumeAmount),
          directUserConsumeRelation: data.directUserConsumeRelation != null ? Number(data.directUserConsumeRelation) : 1,
          isShow: data.isShow !== false,
        };
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
          this.$message.warning('直推指定等级人数大于 0 时，必须选择指定的会员等级');
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
          directUserConsumeAmount: Number(this.formData.directUserConsumeAmount) || 0,
          directUserConsumeRelation: this.formData.directUserConsumeRelation,
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

.text-danger {
  color: #f56c6c;
}

.dialog-footer {
  text-align: right;
}

.level-form {
  ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
  ::v-deep .el-divider--horizontal {
    margin: 6px 0 18px;
  }
}

/* ---------- 升级条件行：条件名 + 数值(带单位) + 与/或 ---------- */
.cond-list {
  padding: 0 10px 0 0;
}

.cond-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.cond-label {
  flex: 0 0 150px;
  width: 150px;
  text-align: right;
  padding-right: 12px;
  font-size: 14px;
  color: #606266;
  line-height: 32px;

  &::before {
    content: '*';
    color: #f56c6c;
    margin-right: 4px;
  }
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
  flex: 0 0 130px;
  margin-left: 20px;
  white-space: nowrap;

  ::v-deep .el-radio {
    margin-right: 12px;
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
  margin: 4px 0 18px 0;
  padding-left: 150px;
}
</style>
