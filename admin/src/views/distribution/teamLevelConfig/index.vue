<template>
  <div class="divBox team-level-config-page">
    <el-card class="box-card mb15" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">团队奖全局设置</span>
      </div>
      <el-alert
        class="mb15"
        type="info"
        :closable="false"
        title="开启团队极差奖后，订单结算将按团队等级配置的极差比例向上分配；追溯层数为 0 表示不限制向上追溯层数。"
        show-icon
      />
      <el-form
        ref="manageFormRef"
        :model="manageForm"
        :rules="manageRules"
        label-width="160px"
        v-loading="manageLoading"
        class="manage-form"
      >
        <el-form-item label="团队极差奖开关：" prop="teamBrokerageStatus">
          <el-radio-group v-model="manageForm.teamBrokerageStatus">
            <el-radio :label="1">开启</el-radio>
            <el-radio :label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="向上追溯层数：" prop="teamBrokerageMaxDepth">
          <el-input-number
            v-model="manageForm.teamBrokerageMaxDepth"
            :min="0"
            :precision="0"
            controls-position="right"
            style="width: 200px"
          />
          <span class="form-tip">0 表示不限制层数</span>
        </el-form-item>
        <el-form-item label="团队奖到账方式：" prop="teamBrokerageCreditTiming">
          <el-radio-group v-model="manageForm.teamBrokerageCreditTiming">
            <el-radio :label="1">支付订单到账</el-radio>
            <el-radio :label="2">订单完成到账</el-radio>
          </el-radio-group>
          <div class="form-tip">支付订单到账：付款成功立即入账；订单完成到账：确认收货后入账</div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="manageSaving"
            @click="saveManage"
            v-hasPermi="['admin:system:team:level:update']"
            >保存全局设置</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 极差/平级比例已在「团队等级」新增/编辑中配置，此处重复模块暂时屏蔽 -->
    <el-card v-if="showLevelBrokerageConfig" class="box-card" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">团队等级返佣配置</span>
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['admin:system:team:level:update']"
          >新增配置</el-button
        >
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%" size="mini">
        <el-table-column prop="teamLevelId" label="团队等级ID" width="110" align="center" />
        <el-table-column label="团队等级名称" min-width="140">
          <template slot-scope="scope">
            {{ getLevelName(scope.row.teamLevelId) }}
          </template>
        </el-table-column>
        <el-table-column label="团队极差比例(%)" width="140" align="center">
          <template slot-scope="scope">
            {{ scope.row.teamBrokerageRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="平级奖比例(%)" width="130" align="center">
          <template slot-scope="scope">
            {{ scope.row.peerAwardRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
              v-hasPermi="['admin:system:team:level:update']"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              class="text-danger"
              @click="handleDelete(scope.row)"
              v-hasPermi="['admin:system:team:level:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-if="showLevelBrokerageConfig"
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="150px">
        <el-form-item label="团队等级" prop="teamLevelId">
          <el-select
            v-model="formData.teamLevelId"
            placeholder="请选择团队等级"
            :disabled="isEdit"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in levelList"
              :key="item.id"
              :label="`${item.name}（LV${item.grade}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="团队极差比例(%)" prop="teamBrokerageRate">
          <el-input-number
            v-model="formData.teamBrokerageRate"
            :min="0"
            :max="100"
            :precision="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="平级奖比例(%)" prop="peerAwardRate">
          <el-input-number
            v-model="formData.peerAwardRate"
            :min="0"
            :max="100"
            :precision="0"
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
import {
  teamLevelAllApi,
  teamBrokerageManageGetApi,
  teamBrokerageManageSetApi,
  teamLevelConfigListApi,
  teamLevelConfigInfoApi,
  teamLevelConfigSaveApi,
  teamLevelConfigDeleteApi,
} from '@/api/teamLevel';

export default {
  name: 'TeamLevelConfig',
  data() {
    return {
      // 与「团队等级」编辑中的极差/平级配置重复，默认屏蔽；需要时改为 true
      showLevelBrokerageConfig: false,
      manageLoading: false,
      manageSaving: false,
      manageForm: {
        teamBrokerageStatus: 0,
        teamBrokerageMaxDepth: 0,
        teamBrokerageCreditTiming: 1,
      },
      manageRules: {
        teamBrokerageStatus: [{ required: true, message: '请选择开关状态', trigger: 'change' }],
        teamBrokerageMaxDepth: [{ required: true, message: '请输入追溯层数', trigger: 'blur' }],
        teamBrokerageCreditTiming: [{ required: true, message: '请选择到账方式', trigger: 'change' }],
      },
      loading: false,
      tableData: [],
      levelList: [],
      dialogVisible: false,
      dialogTitle: '新增配置',
      isEdit: false,
      submitLoading: false,
      formData: {
        teamLevelId: null,
        teamBrokerageRate: 0,
        peerAwardRate: 0,
      },
      formRules: {
        teamLevelId: [{ required: true, message: '请选择团队等级', trigger: 'change' }],
        teamBrokerageRate: [{ required: true, message: '请输入团队极差比例', trigger: 'blur' }],
        peerAwardRate: [{ required: true, message: '请输入平级奖比例', trigger: 'blur' }],
      },
    };
  },
  created() {
    this.fetchManage();
    if (this.showLevelBrokerageConfig) {
      this.fetchList();
      this.fetchLevelList();
    }
  },
  methods: {
    async fetchManage() {
      this.manageLoading = true;
      try {
        const data = await teamBrokerageManageGetApi();
        this.manageForm = {
          teamBrokerageStatus: data.teamBrokerageStatus != null ? Number(data.teamBrokerageStatus) : 0,
          teamBrokerageMaxDepth: data.teamBrokerageMaxDepth != null ? Number(data.teamBrokerageMaxDepth) : 0,
          teamBrokerageCreditTiming:
            data.teamBrokerageCreditTiming != null ? Number(data.teamBrokerageCreditTiming) : 1,
        };
      } catch (e) {
        this.$message.error('获取全局配置失败');
      } finally {
        this.manageLoading = false;
      }
    },
    saveManage() {
      this.$refs.manageFormRef.validate(async (valid) => {
        if (!valid) return;
        this.manageSaving = true;
        try {
          await teamBrokerageManageSetApi(this.manageForm);
          this.$message.success('保存成功');
        } catch (e) {
          this.$message.error('保存失败');
        } finally {
          this.manageSaving = false;
        }
      });
    },
    async fetchList() {
      this.loading = true;
      try {
        this.tableData = await teamLevelConfigListApi();
      } catch (e) {
        this.$message.error('获取配置列表失败');
      } finally {
        this.loading = false;
      }
    },
    async fetchLevelList() {
      try {
        this.levelList = await teamLevelAllApi();
      } catch (e) {
        console.error('获取团队等级列表失败', e);
      }
    },
    getLevelName(teamLevelId) {
      const level = this.levelList.find((item) => item.id === teamLevelId);
      return level ? level.name : `等级${teamLevelId}`;
    },
    handleAdd() {
      this.dialogTitle = '新增配置';
      this.isEdit = false;
      this.formData = {
        teamLevelId: null,
        teamBrokerageRate: 0,
        peerAwardRate: 0,
      };
      this.dialogVisible = true;
    },
    async handleEdit(row) {
      this.dialogTitle = '编辑配置';
      this.isEdit = true;
      this.submitLoading = true;
      try {
        const data = await teamLevelConfigInfoApi(row.teamLevelId);
        this.formData = {
          teamLevelId: data.teamLevelId,
          teamBrokerageRate: data.teamBrokerageRate || 0,
          peerAwardRate: data.peerAwardRate || 0,
        };
        this.dialogVisible = true;
      } catch (e) {
        this.$message.error('获取详情失败');
      } finally {
        this.submitLoading = false;
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该团队等级的返佣配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          try {
            await teamLevelConfigDeleteApi(row.teamLevelId);
            this.$message.success('删除成功');
            this.fetchList();
          } catch (e) {
            this.$message.error('删除失败');
          }
        })
        .catch(() => {});
    },
    handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await teamLevelConfigSaveApi(this.formData);
          this.$message.success('保存成功');
          this.dialogVisible = false;
          this.fetchList();
        } catch (e) {
          this.$message.error('保存失败');
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handleDialogClose() {
      this.$refs.formRef && this.$refs.formRef.resetFields();
    },
  },
};
</script>

<style scoped lang="scss">
.team-level-config-page {
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

.manage-form {
  max-width: 640px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.dialog-footer {
  text-align: right;
}
</style>
