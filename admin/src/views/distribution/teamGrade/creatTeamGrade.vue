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

      <el-divider content-position="left">升级条件</el-divider>

      <div class="cond-row">
        <el-form-item prop="selfOrderAmount" label-width="200px" class="cond-item">
          <template slot="label">自购门槛(元)：</template>
          <el-input-number
            v-model="formData.selfOrderAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="统计时机：" prop="selfOrderTriggerType" label-width="80px" class="cond-item">
          <el-select v-model="formData.selfOrderTriggerType" placeholder="请选择" style="width: 130px">
            <el-option label="支付成功" :value="1" />
            <el-option label="订单完成" :value="2" />
          </el-select>
        </el-form-item>
      </div>

      <div class="cond-row">
        <el-form-item prop="teamOrderAmount" label-width="200px" class="cond-item">
          <template slot="label">
            <span class="label-join">
              <el-radio-group v-model="formData.selfTeamRelation">
                <el-radio :label="1">与</el-radio>
                <el-radio :label="2">或</el-radio>
              </el-radio-group>
            </span>
            团队门槛(元)：
          </template>
          <el-input-number
            v-model="formData.teamOrderAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="统计时机：" prop="teamOrderTriggerType" label-width="80px" class="cond-item">
          <el-select v-model="formData.teamOrderTriggerType" placeholder="请选择" style="width: 130px">
            <el-option label="支付成功" :value="1" />
            <el-option label="订单完成" :value="2" />
          </el-select>
        </el-form-item>
      </div>

      <div class="cond-row">
        <el-form-item prop="directOrderAmount" label-width="200px" class="cond-item">
          <template slot="label">
            <span class="label-join">
              <el-radio-group v-model="formData.teamDirectRelation">
                <el-radio :label="1">与</el-radio>
                <el-radio :label="2">或</el-radio>
              </el-radio-group>
            </span>
            直推门槛(元)：
          </template>
          <el-input-number
            v-model="formData.directOrderAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="统计时机：" prop="directOrderTriggerType" label-width="80px" class="cond-item">
          <el-select v-model="formData.directOrderTriggerType" placeholder="请选择" style="width: 130px">
            <el-option label="支付成功" :value="1" />
            <el-option label="订单完成" :value="2" />
          </el-select>
        </el-form-item>
      </div>

      <div class="cond-row">
        <el-form-item prop="directLevelCount" label-width="200px" class="cond-item">
          <template slot="label">
            <span class="label-join">
              <el-radio-group v-model="formData.directLevelRelation">
                <el-radio :label="1">与</el-radio>
                <el-radio :label="2">或</el-radio>
              </el-radio-group>
            </span>
            直推等级人数：
          </template>
          <el-input-number
            v-model="formData.directLevelCount"
            :min="0"
            :precision="0"
            controls-position="right"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="达到等级：" prop="directLevelId" label-width="80px" class="cond-item">
          <el-select
            v-model="formData.directLevelId"
            placeholder="选择用户级别"
            clearable
            filterable
            style="width: 130px"
          >
            <el-option v-for="item in userLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </div>

      <div class="cond-row">
        <el-form-item prop="teamLevelCount" label-width="200px" class="cond-item">
          <template slot="label">
            <span class="label-join">
              <el-radio-group v-model="formData.teamLevelRelation">
                <el-radio :label="1">与</el-radio>
                <el-radio :label="2">或</el-radio>
              </el-radio-group>
            </span>
            团队级别人数：
          </template>
          <el-input-number
            v-model="formData.teamLevelCount"
            :min="0"
            :precision="0"
            controls-position="right"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="达到等级：" prop="teamLevelId" label-width="80px" class="cond-item">
          <el-select
            v-model="formData.teamLevelId"
            placeholder="选择用户级别"
            clearable
            filterable
            style="width: 130px"
          >
            <el-option v-for="item in userLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-tip cond-tip">
        判定顺序：自购门槛 「与/或」 团队门槛 「与/或」 直推门槛 「与/或」 直推等级人数 「与/或」 团队级别人数。金额/人数门槛为
        0 的条件视为自动满足，用「或」连接时等同于跳过该条件；等级人数按用户当前会员等级实时统计（直推=一级推荐人，团队=整条推荐链的所有下级），等级来源于「会员等级」配置。
      </div>

      <el-divider content-position="left">团队奖配置</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="团队奖比例(%)：" prop="config.teamBrokerageRate">
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
import { levelListApi } from '@/api/user';
import { Debounce } from '@/utils/validate';

const defaultForm = () => ({
  name: '',
  grade: 1,
  selfOrderAmount: 0,
  teamOrderAmount: 0,
  directOrderAmount: 0,
  selfOrderTriggerType: 2,
  teamOrderTriggerType: 2,
  directOrderTriggerType: 2,
  selfTeamRelation: 1,
  teamDirectRelation: 1,
  directLevelRelation: 1,
  directLevelId: null,
  directLevelCount: 0,
  teamLevelRelation: 1,
  teamLevelId: null,
  teamLevelCount: 0,
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
      userLevelOptions: [],
      rules: {
        name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入等级序号', trigger: 'blur' }],
        selfOrderAmount: [{ required: true, message: '请输入自购门槛', trigger: 'blur' }],
        teamOrderAmount: [{ required: true, message: '请输入团队门槛', trigger: 'blur' }],
        directOrderAmount: [{ required: true, message: '请输入直推门槛', trigger: 'blur' }],
        directLevelCount: [{ required: true, message: '请输入直推等级人数门槛', trigger: 'blur' }],
        directLevelId: [
          {
            validator: (rule, value, callback) => {
              if (this.formData.directLevelCount > 0 && !value) {
                callback(new Error('人数门槛大于0时必须选择目标用户级别'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
        teamLevelCount: [{ required: true, message: '请输入团队级别人数门槛', trigger: 'blur' }],
        teamLevelId: [
          {
            validator: (rule, value, callback) => {
              if (this.formData.teamLevelCount > 0 && !value) {
                callback(new Error('人数门槛大于0时必须选择目标用户级别'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
        selfOrderTriggerType: [{ required: true, message: '请选择自购订单统计时机', trigger: 'change' }],
        teamOrderTriggerType: [{ required: true, message: '请选择团队订单统计时机', trigger: 'change' }],
        directOrderTriggerType: [{ required: true, message: '请选择直推订单统计时机', trigger: 'change' }],
        'config.teamBrokerageRate': [{ required: true, message: '请输入团队奖比例', trigger: 'blur' }],
        'config.peerAwardRate': [{ required: true, message: '请输入平级奖比例', trigger: 'blur' }],
      },
    };
  },
  methods: {
    openCreate() {
      this.formData = defaultForm();
      this.dialogVisible = true;
      this.loadUserLevels();
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
      });
    },
    openEdit(id) {
      this.formData = { ...defaultForm(), id };
      this.dialogVisible = true;
      this.loadUserLevels();
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
        this.loadInfo(id);
      });
    },
    // 用户级别（会员等级）选项，用于「直推等级人数」条件
    loadUserLevels() {
      if (this.userLevelOptions.length) return;
      levelListApi()
        .then((res) => {
          this.userLevelOptions = res || [];
        })
        .catch(() => {
          this.userLevelOptions = [];
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
            directOrderAmount: level.directOrderAmount != null ? Number(level.directOrderAmount) : 0,
            selfOrderTriggerType: level.selfOrderTriggerType != null ? Number(level.selfOrderTriggerType) : 2,
            teamOrderTriggerType: level.teamOrderTriggerType != null ? Number(level.teamOrderTriggerType) : 2,
            directOrderTriggerType: level.directOrderTriggerType != null ? Number(level.directOrderTriggerType) : 2,
            selfTeamRelation: level.selfTeamRelation != null ? Number(level.selfTeamRelation) : 1,
            teamDirectRelation: level.teamDirectRelation != null ? Number(level.teamDirectRelation) : 1,
            directLevelRelation: level.directLevelRelation != null ? Number(level.directLevelRelation) : 1,
            directLevelId:
              level.directLevelId != null && Number(level.directLevelId) > 0 ? Number(level.directLevelId) : null,
            directLevelCount: level.directLevelCount != null ? Number(level.directLevelCount) : 0,
            teamLevelRelation: level.teamLevelRelation != null ? Number(level.teamLevelRelation) : 1,
            teamLevelId:
              level.teamLevelId != null && Number(level.teamLevelId) > 0 ? Number(level.teamLevelId) : null,
            teamLevelCount: level.teamLevelCount != null ? Number(level.teamLevelCount) : 0,
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
          directOrderAmount: this.formData.directOrderAmount,
          selfOrderTriggerType: this.formData.selfOrderTriggerType,
          teamOrderTriggerType: this.formData.teamOrderTriggerType,
          directOrderTriggerType: this.formData.directOrderTriggerType,
          selfTeamRelation: this.formData.selfTeamRelation,
          teamDirectRelation: this.formData.teamDirectRelation,
          directLevelRelation: this.formData.directLevelRelation,
          directLevelId: this.formData.directLevelId || 0,
          directLevelCount: this.formData.directLevelCount || 0,
          teamLevelRelation: this.formData.teamLevelRelation,
          teamLevelId: this.formData.teamLevelId || 0,
          teamLevelCount: this.formData.teamLevelCount || 0,
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

.form-tip {
  font-size: 12px;
  line-height: 18px;
  color: #909399;
}

.cond-row {
  display: flex;
  align-items: center;
  .label-join {
    ::v-deep .el-radio-group {
      vertical-align: middle;
      margin-right: 2px;
    }
    ::v-deep .el-radio {
      margin-right: 8px;
      .el-radio__label {
        padding-left: 4px;
        font-size: inherit;
      }
    }
  }
  .cond-item {
    margin-bottom: 0;
    margin-right: 8px;
    ::v-deep .el-form-item__error {
      padding-top: 2px;
    }
  }
}

.cond-tip {
  margin: 14px 0 14px 0;
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
