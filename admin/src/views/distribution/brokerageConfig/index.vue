<template>
  <div class="brokerage-config-page">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">会员返佣配置</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增配置</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="levelId" label="会员等级ID" width="120" align="center" />
        <el-table-column label="会员等级名称" min-width="150">
          <template slot-scope="scope">
            {{ getLevelName(scope.row.levelId) }}
          </template>
        </el-table-column>
        <el-table-column prop="selfBrokerageRate" label="自购返佣比例(%)" width="140" align="center">
          <template slot-scope="scope">
            {{ scope.row.selfBrokerageRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="brokerageRateOne" label="一级返佣比例(%)" width="140" align="center">
          <template slot-scope="scope">
            {{ scope.row.brokerageRateOne || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="brokerageRateTwo" label="二级返佣比例(%)" width="140" align="center">
          <template slot-scope="scope">
            {{ scope.row.brokerageRateTwo || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" />
        <el-table-column label="操作" width="190" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,'编辑配置')"
            >
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
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
      >
        <el-form-item label="会员等级" prop="levelId">
          <UserLevelSelect
            v-model="formData.levelId"
            placeholder="请选择会员等级"
            :disabled="isEdit"
            @change="onLevelChange"
          />
        </el-form-item>
        <el-form-item label="自购返佣比例(%)" prop="selfBrokerageRate">
          <el-input-number
            v-model="formData.selfBrokerageRate"
            :min="0"
            :max="100"
            :step="0.1"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="一级返佣比例(%)" prop="brokerageRateOne">
          <el-input-number
            v-model="formData.brokerageRateOne"
            :min="0"
            :max="100"
            :step="0.1"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="二级返佣比例(%)" prop="brokerageRateTwo">
          <el-input-number
            v-model="formData.brokerageRateTwo"
            :min="0"
            :max="100"
            :step="0.1"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import UserLevelSelect from '@/components/UserLevelSelect';
import {
  brokerageListApi,
  brokerageInfoApi,
  brokerageSaveApi,
  brokerageDeleteApi,
} from '@/api/distribution';
import { levelAllApi } from '@/api/user';

export default {
  name: 'BrokerageConfig',
  components: { UserLevelSelect },
  data() {
    return {
      loading: false,
      tableData: [],
      levelList: [],
      dialogVisible: false,
      dialogTitle: '新增配置',
      isEdit: false,
      submitLoading: false,
      formData: {
        levelId: null,
        selfBrokerageRate: 0,
        brokerageRateOne: 0,
        brokerageRateTwo: 0,
      },
      formRules: {
        levelId: [
          { required: true, message: '请选择会员等级', trigger: 'change' },
        ],
        selfBrokerageRate: [
          { required: true, message: '请输入自购返佣比例', trigger: 'blur' },
        ],
        brokerageRateOne: [
          { required: true, message: '请输入一级返佣比例', trigger: 'blur' },
        ],
        brokerageRateTwo: [
          { required: true, message: '请输入二级返佣比例', trigger: 'blur' },
        ],
      },
    };
  },
  created() {
    this.fetchList();
    this.fetchLevelList();
  },
  methods: {
    // 获取返佣配置列表
    async fetchList() {
      this.loading = true;
      try {
        this.tableData = await brokerageListApi();
        // console.log('----------res',res);
        // this.tableData = res.data || [];
        // console.log('--------------',this.tableData);
      } catch (error) {
        this.$message.error('获取列表失败');
      } finally {
        this.loading = false;
      }
    },
    // 获取会员等级列表
    async fetchLevelList() {
      try {
        const res = await levelAllApi();
        this.levelList = res || [];
      } catch (error) {
        console.error('获取会员等级列表失败', error);
      }
    },
    // 根据等级ID获取等级名称
    getLevelName(levelId) {
      const level = this.levelList.find((item) => item.id === levelId);
      return level ? level.name : `等级${levelId}`;
    },
    // 新增配置
    handleAdd() {
      this.dialogTitle = '新增配置';
      this.isEdit = false;
      this.formData = {
        levelId: null,
        selfBrokerageRate: 0,
        brokerageRateOne: 0,
        brokerageRateTwo: 0,
      };
      this.dialogVisible = true;
    },
    // 编辑配置
    async handleEdit(row,title='编辑配置') {
      this.dialogTitle = title;
      this.isEdit = true;
      this.submitLoading = true;
      try {
        const data = await brokerageInfoApi(row.levelId);
        // const data = res.data || {};
        this.formData = {
          levelId: data.levelId,
          selfBrokerageRate: data.selfBrokerageRate || 0,
          brokerageRateOne: data.brokerageRateOne || 0,
          brokerageRateTwo: data.brokerageRateTwo || 0,
        };
        this.dialogVisible = true;
      } catch (error) {
        this.$message.error('获取详情失败');
      } finally {
        this.submitLoading = false;
      }
    },
    // 删除配置
    handleDelete(row) {
      this.$confirm(`确定要删除该会员等级的返佣配置吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          try {
            await brokerageDeleteApi(row.levelId);
            this.$message.success('删除成功');
            this.fetchList();
          } catch (error) {
            this.$message.error('删除失败');
          }
        })
        .catch(() => {});
    },
    // 提交表单
    handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await brokerageSaveApi(this.formData);
          this.$message.success('保存成功');
          this.dialogVisible = false;
          this.fetchList();
        } catch (error) {
          this.$message.error('保存失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },
    // 关闭对话框
    handleDialogClose() {
      this.$refs.formRef && this.$refs.formRef.resetFields();
    },
    // 等级选择变化
    onLevelChange(levelId, level) {
      // 可以在这里添加额外逻辑
    },
  },
};
</script>

<style scoped lang="scss">
.brokerage-config-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  margin: -20px -20px 0;
}

.box-card {
  border-radius: 4px;
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

.text-danger {
  color: #f56c6c;
}

::v-deep .el-table {
  th {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
}

.dialog-footer {
  text-align: right;
}
</style>
