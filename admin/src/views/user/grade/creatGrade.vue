<template>
  <el-dialog
    v-if="dialogVisible"
    :title="formData.id ? '编辑会员等级' : '添加会员等级'"
    :visible.sync="dialogVisible"
    width="760px"
    :before-close="handleClose"
  >
    <el-form :model="formData" :rules="rules" ref="user" label-width="140px" class="demo-ruleForm" v-loading="loading">
      <!-- 第一行：等级名称 -->
      <el-form-item label="等级名称：" prop="name">
        <el-input v-model="formData.name" placeholder="如：创客、联创" maxlength="50" />
      </el-form-item>

      <!-- 第二行：会员等级 + 升级条件类型 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="会员等级：" prop="grade">
            <el-input-number v-model="formData.grade" :min="1" :max="999" controls-position="right" placeholder="数字越大等级越高" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!formData.id">
          <el-form-item label="启用状态：">
            <el-switch v-model="formData.isShow" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="升级条件：" prop="upgradeType">
        <el-radio-group v-model="formData.upgradeType" @change="handleUpgradeTypeChange">
          <el-radio :label="1">累计消费金额</el-radio>
          <el-radio :label="2">累计订单数</el-radio>
          <el-radio :label="3">两者同时满足</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 第三行：升级条件值（动态显示） -->
      <el-row :gutter="20" v-if="formData.upgradeType === 1 || formData.upgradeType === 3">
        <el-col :span="12">
          <el-form-item label="累计消费(元)：" prop="experience">
            <el-input-number
              v-model="formData.experience"
              :min="0"
              :precision="0"
              controls-position="right"
              placeholder="请输入金额"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="formData.upgradeType === 1 || formData.upgradeType === 3">
          <el-form-item label="消费统计时机：" prop="consumptionTriggerType">
            <el-select v-model="formData.consumptionTriggerType" placeholder="请选择" style="width: 100%">
              <el-option label="已付款" :value="1" />
              <el-option label="交易完成" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-if="formData.upgradeType === 2 || formData.upgradeType === 3">
        <el-col :span="12">
          <el-form-item label="累计订单数：" prop="upgradeValue">
            <el-input-number
              v-model="formData.upgradeValue"
              :min="0"
              :precision="0"
              controls-position="right"
              placeholder="请输入订单数"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="formData.upgradeType === 2 || formData.upgradeType === 3">
          <el-form-item label="订单统计时机：" prop="orderCountTriggerType">
            <el-select v-model="formData.orderCountTriggerType" placeholder="请选择" style="width: 100%">
              <el-option label="已付款" :value="1" />
              <el-option label="交易完成" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第四行：享受折扣 + 赠送积分 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="享受折扣(%)：" prop="discount">
            <el-input-number v-model="formData.discount" :min="1" :max="100" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="赠送积分：" prop="giveIntegral">
            <el-input-number
              v-model="formData.giveIntegral"
              :min="0"
              :precision="0"
              controls-position="right"
              placeholder="每单固定赠送"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第五行： 等级图标 -->
      <el-form-item label="等级图标：" prop="icon">
        <div class="upLoadPicBox" @click="modalPicTap('1', 'icon')">
          <div v-if="formData.icon" class="pictrue"><img :src="formData.icon" /></div>
          <div v-else class="upLoad">
            <i class="el-icon-camera cameraIconfont" />
          </div>
        </div>
      </el-form-item>

      <!-- 第六行：等级权益描述 -->
      <el-form-item label="等级权益描述：">
        <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="选填，描述该等级的权益内容" maxlength="255" />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        @click="submitForm"
        v-hasPermi="['admin:system:user:level:update', 'admin:system:user:level:save']"
        >确定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import { levelSaveApi, levelInfoApi, levelUpdateApi } from '@/api/user';
import { Debounce } from '@/utils/validate';

const defaultForm = () => ({
  name: '',
  grade: 1,
  discount: 100,
  experience: 0,
  upgradeValue: 0,
  upgradeType: 1,
  consumptionTriggerType: 1,
  orderCountTriggerType: 1,
  giveIntegral: 0,
  icon: '',
  description: '',
  remark: '',
  id: null,
  isShow: true,
});

export default {
  name: 'CreatGrade',
  data() {
    return {
      dialogVisible: false,
      formData: defaultForm(),
      loading: false,
      rules: {
        name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入等级序号', trigger: 'blur' }],
        upgradeType: [{ required: true, message: '请选择升级条件', trigger: 'change' }],
        discount: [{ required: true, message: '请输入折扣', trigger: 'blur' }],
        experience: [
          {
            validator: (rule, value, callback) => {
              if ((this.formData.upgradeType === 1 || this.formData.upgradeType === 3) && (!value || value <= 0)) {
                callback(new Error('请输入累计消费金额'));
              } else {
                callback();
              }
            },
            trigger: 'blur',
          },
        ],
        upgradeValue: [
          {
            validator: (rule, value, callback) => {
              if ((this.formData.upgradeType === 2 || this.formData.upgradeType === 3) && (!value || value <= 0)) {
                callback(new Error('请输入累计订单数'));
              } else {
                callback();
              }
            },
            trigger: 'blur',
          },
        ],
        consumptionTriggerType: [
          {
            validator: (rule, value, callback) => {
              if ((this.formData.upgradeType === 1 || this.formData.upgradeType === 3) && !value) {
                callback(new Error('请选择消费统计时机'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
        orderCountTriggerType: [
          {
            validator: (rule, value, callback) => {
              if ((this.formData.upgradeType === 2 || this.formData.upgradeType === 3) && !value) {
                callback(new Error('请选择订单统计时机'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
        giveIntegral: [{ required: true, message: '请输入赠送积分', trigger: 'blur' }],
        icon: [{ required: true, message: '请上传等级图标', trigger: 'change' }],
      },
    };
  },
  watch: {
    dialogVisible(val) {
      if (val && this.formData.id) {
        this.loadInfo(this.formData.id);
      }
    },
  },
  methods: {
    handleUpgradeTypeChange() {
      // 切换升级类型时重置相关字段
      if (this.formData.upgradeType === 1) {
        this.formData.upgradeValue = 0;
        this.formData.orderCountTriggerType = 1;
      } else if (this.formData.upgradeType === 2) {
        this.formData.experience = 0;
        this.formData.consumptionTriggerType = 1;
      }
      // 清除验证
      this.$nextTick(() => {
        if (this.$refs.user) {
          this.$refs.user.clearValidate();
        }
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
      levelInfoApi({ id })
        .then((res) => {
          let data = res.level
          this.formData = {
            ...defaultForm(),
            ...data,
            giveIntegral: data.giveIntegral != null ? Number(data.giveIntegral) : 0,
            experience: data.experience != null ? Number(data.experience) : 0,
            upgradeValue: data.upgradeValue != null ? Number(data.upgradeValue) : 0,
          };
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleClose() {
      this.dialogVisible = false;
      this.formData = defaultForm();
      this.$nextTick(() => {
        if (this.$refs.user) {
          this.$refs.user.clearValidate();
        }
      });
    },
    submitForm: Debounce(function () {
      this.$refs.user.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        const data = {
          name: this.formData.name,
          grade: this.formData.grade,
          discount: this.formData.discount,
          experience: this.formData.experience,
          upgradeValue: this.formData.upgradeValue,
          upgradeType: this.formData.upgradeType,
          consumptionTriggerType: this.formData.consumptionTriggerType,
          orderCountTriggerType: this.formData.orderCountTriggerType,
          giveIntegral: this.formData.giveIntegral,
          icon: this.formData.icon,
          description: this.formData.description || '',
          remark: this.formData.remark || '',
          isShow: this.formData.isShow !== false,
        };
        const request = this.formData.id
          ? levelUpdateApi(this.formData.id, data)
          : levelSaveApi(data);
        request
          .then(() => {
            this.$message.success(this.formData.id ? '编辑成功' : '添加成功');
            this.loading = false;
            this.handleClose();
            this.$parent.getList();
          })
          .catch(() => {
            this.loading = false;
          });
      });
    }),
  },
};
</script>

<style scoped lang="scss">
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}

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
