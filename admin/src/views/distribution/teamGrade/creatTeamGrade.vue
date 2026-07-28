<template>
  <el-dialog
    :title="formData.id ? '编辑团队等级' : '添加团队等级'"
    :visible.sync="dialogVisible"
    width="720px"
    :close-on-click-modal="false"
    :before-close="handleClose"
  >
    <el-form :model="formData" :rules="rules" ref="teamForm" label-width="150px" class="demo-ruleForm" v-loading="loading">
      <el-form-item label="等级名称：" prop="name">
        <el-input v-model="formData.name" placeholder="如：初级团队、金牌团队" maxlength="50" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="等级序号：" prop="grade">
            <el-input-number
              v-model="formData.grade"
              :min="1"
              :max="999"
              controls-position="right"
              placeholder="数字越大等级越高"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!formData.id">
          <el-form-item label="启用状态：">
            <el-switch v-model="formData.isShow" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="自购门槛(元)：" prop="selfOrderAmount">
            <el-input-number
              v-model="formData.selfOrderAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="团队门槛(元)：" prop="teamOrderAmount">
            <el-input-number
              v-model="formData.teamOrderAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="自购订单统计时机：" prop="selfOrderTriggerType">
            <el-select v-model="formData.selfOrderTriggerType" placeholder="请选择" style="width: 100%">
              <el-option label="支付成功" :value="1" />
              <el-option label="订单完成" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="团队订单统计时机：" prop="teamOrderTriggerType">
            <el-select v-model="formData.teamOrderTriggerType" placeholder="请选择" style="width: 100%">
              <el-option label="支付成功" :value="1" />
              <el-option label="订单完成" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">团队奖配置</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="团队极差比例(%)：" prop="config.teamBrokerageRate">
            <el-input-number
              v-model="formData.config.teamBrokerageRate"
              :min="0"
              :max="100"
              :precision="0"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="平级奖比例(%)：" prop="config.peerAwardRate">
            <el-input-number
              v-model="formData.config.peerAwardRate"
              :min="0"
              :max="100"
              :precision="0"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="等级图标：" prop="icon">
        <div class="upLoadPicBox" @click="modalPicTap('1', 'icon')">
          <div v-if="formData.icon" class="pictrue"><img :src="formData.icon" /></div>
          <div v-else class="upLoad">
            <i class="el-icon-camera cameraIconfont" />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="等级权益描述：">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="2"
          placeholder="选填，描述该团队等级的权益内容"
          maxlength="500"
        />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        @click="submitForm"
        v-hasPermi="['admin:system:team:level:update', 'admin:system:team:level:save']"
        >确定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import { teamLevelSaveApi, teamLevelInfoApi, teamLevelUpdateApi } from '@/api/teamLevel';
import { Debounce } from '@/utils/validate';

const defaultForm = () => ({
  name: '',
  grade: 1,
  selfOrderAmount: 0,
  teamOrderAmount: 0,
  selfOrderTriggerType: 2,
  teamOrderTriggerType: 2,
  description: '',
  icon: '',
  id: null,
  isShow: true,
  config: {
    teamBrokerageRate: 0,
    peerAwardRate: 0,
  },
});

export default {
  name: 'CreatTeamGrade',
  data() {
    return {
      dialogVisible: false,
      formData: defaultForm(),
      loading: false,
      rules: {
        name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入等级序号', trigger: 'blur' }],
        selfOrderAmount: [{ required: true, message: '请输入自购门槛', trigger: 'blur' }],
        teamOrderAmount: [{ required: true, message: '请输入团队门槛', trigger: 'blur' }],
        selfOrderTriggerType: [{ required: true, message: '请选择自购订单统计时机', trigger: 'change' }],
        teamOrderTriggerType: [{ required: true, message: '请选择团队订单统计时机', trigger: 'change' }],
        icon: [{ required: true, message: '请上传等级图标', trigger: 'change' }],
        'config.teamBrokerageRate': [{ required: true, message: '请输入团队极差比例', trigger: 'blur' }],
        'config.peerAwardRate': [{ required: true, message: '请输入平级奖比例', trigger: 'blur' }],
      },
    };
  },
  methods: {
    openCreate() {
      this.formData = defaultForm();
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
      });
    },
    openEdit(id) {
      this.formData = { ...defaultForm(), id };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
        this.loadInfo(id);
      });
    },
    modalPicTap(tit, num) {
      const _this = this;
      this.$modalUpload(
        function (img) {
          if (tit === '1' && num === 'icon') {
            _this.$set(_this.formData, 'icon', img[0].sattDir);
          }
        },
        tit,
        'user',
      );
    },
    loadInfo(id) {
      this.loading = true;
      teamLevelInfoApi({ id })
        .then((res) => {
          const level = res.level || {};
          const config = res.config || {};
          this.formData = {
            ...defaultForm(),
            ...level,
            id: level.id || id,
            selfOrderAmount: level.selfOrderAmount != null ? Number(level.selfOrderAmount) : 0,
            teamOrderAmount: level.teamOrderAmount != null ? Number(level.teamOrderAmount) : 0,
            selfOrderTriggerType: level.selfOrderTriggerType != null ? Number(level.selfOrderTriggerType) : 2,
            teamOrderTriggerType: level.teamOrderTriggerType != null ? Number(level.teamOrderTriggerType) : 2,
            config: {
              teamBrokerageRate: config.teamBrokerageRate != null ? Number(config.teamBrokerageRate) : 0,
              peerAwardRate: config.peerAwardRate != null ? Number(config.peerAwardRate) : 0,
            },
          };
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false;
        });
    },
    handleClose() {
      this.dialogVisible = false;
      this.formData = defaultForm();
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
      });
    },
    submitForm: Debounce(function () {
      this.$refs.teamForm.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        const data = {
          name: this.formData.name,
          grade: this.formData.grade,
          selfOrderAmount: this.formData.selfOrderAmount,
          teamOrderAmount: this.formData.teamOrderAmount,
          selfOrderTriggerType: this.formData.selfOrderTriggerType,
          teamOrderTriggerType: this.formData.teamOrderTriggerType,
          description: this.formData.description || '',
          icon: this.formData.icon,
          isShow: this.formData.isShow !== false,
          config: {
            teamBrokerageRate: this.formData.config.teamBrokerageRate,
            peerAwardRate: this.formData.config.peerAwardRate,
          },
        };
        const request = this.formData.id
          ? teamLevelUpdateApi(this.formData.id, data)
          : teamLevelSaveApi(data);
        request
          .then(() => {
            this.$message.success(this.formData.id ? '编辑成功' : '添加成功');
            this.handleClose();
            this.$parent.getList();
          })
          .catch(() => {})
          .finally(() => {
            this.loading = false;
          });
      });
    }),
  },
};
</script>

<style scoped lang="scss">
.demo-ruleForm {
  ::v-deep .el-form-item {
    margin-bottom: 14px;
  }
  ::v-deep .el-input-number {
    width: 100%;
  }
}

.upLoadPicBox {
  cursor: pointer;
  .pictrue {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
  .upLoad {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    .cameraIconfont {
      font-size: 24px;
      color: #8c939d;
    }
  }
}
</style>
