<template>
  <el-dialog
    :title="formData.id ? '编辑团队等级' : '添加团队等级'"
    :visible.sync="dialogVisible"
    width="760px"
    top="6vh"
    custom-class="team-grade-dialog"
    :close-on-click-modal="false"
    :before-close="handleClose"
  >
    <el-form :model="formData" :rules="rules" ref="teamForm" label-width="110px" v-loading="loading">
      <!-- 基本信息 -->
      <div class="section-title">基本信息</div>
      <el-form-item label="等级名称：" prop="name">
        <el-input v-model="formData.name" placeholder="如：初级团队、金牌团队" maxlength="50" style="width: 320px" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="等级序号：" prop="grade">
            <el-input-number
              v-model="formData.grade"
              :min="1"
              :max="999"
              controls-position="right"
              style="width: 180px"
            />
            <span class="inline-tip">数字越大等级越高</span>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!formData.id">
          <el-form-item label="启用状态：">
            <el-switch v-model="formData.isShow" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 升级条件：表格化布局 -->
      <div class="section-title">升级条件</div>
      <div class="cond-table">
        <div class="cond-header">
          <span class="col-relation">关系</span>
          <span class="col-name">升级条件</span>
          <span class="col-value">门槛值</span>
          <span class="col-extra">统计时机 / 分销商等级</span>
        </div>

        <!-- 自购门槛 -->
        <div class="cond-row">
          <div class="col-relation">
            <span class="relation-muted" title="首个条件无需连接词">—</span>
          </div>
          <div class="col-name">自购门槛(元)</div>
          <div class="col-value">
            <el-form-item prop="selfOrderAmount">
              <el-input-number
                v-model="formData.selfOrderAmount"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 160px"
              />
            </el-form-item>
          </div>
          <div class="col-extra">
            <el-form-item prop="selfOrderTriggerType">
              <el-select v-model="formData.selfOrderTriggerType" style="width: 160px">
                <el-option label="支付成功" :value="1" />
                <el-option label="订单完成" :value="2" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <!-- 团队门槛 -->
        <div class="cond-row">
          <div class="col-relation">
            <el-radio-group v-model="formData.selfTeamRelation">
              <el-radio :label="1">与</el-radio>
              <el-radio :label="2">或</el-radio>
            </el-radio-group>
          </div>
          <div class="col-name">团队门槛(元)</div>
          <div class="col-value">
            <el-form-item prop="teamOrderAmount">
              <el-input-number
                v-model="formData.teamOrderAmount"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 160px"
              />
            </el-form-item>
          </div>
          <div class="col-extra">
            <el-form-item prop="teamOrderTriggerType">
              <el-select v-model="formData.teamOrderTriggerType" style="width: 160px">
                <el-option label="支付成功" :value="1" />
                <el-option label="订单完成" :value="2" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <!-- 直推门槛 -->
        <div class="cond-row">
          <div class="col-relation">
            <el-radio-group v-model="formData.teamDirectRelation">
              <el-radio :label="1">与</el-radio>
              <el-radio :label="2">或</el-radio>
            </el-radio-group>
          </div>
          <div class="col-name">直推门槛(元)</div>
          <div class="col-value">
            <el-form-item prop="directOrderAmount">
              <el-input-number
                v-model="formData.directOrderAmount"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 160px"
              />
            </el-form-item>
          </div>
          <div class="col-extra">
            <el-form-item prop="directOrderTriggerType">
              <el-select v-model="formData.directOrderTriggerType" style="width: 160px">
                <el-option label="支付成功" :value="1" />
                <el-option label="订单完成" :value="2" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <!-- 直推分销商等级人数 -->
        <div class="cond-row">
          <div class="col-relation">
            <el-radio-group v-model="formData.directLevelRelation">
              <el-radio :label="1">与</el-radio>
              <el-radio :label="2">或</el-radio>
            </el-radio-group>
          </div>
          <div class="col-name">直推分销商等级人数</div>
          <div class="col-value">
            <el-form-item prop="directLevelCount">
              <el-input-number
                v-model="formData.directLevelCount"
                :min="0"
                :precision="0"
                controls-position="right"
                style="width: 160px"
              />
            </el-form-item>
          </div>
          <div class="col-extra">
            <el-form-item prop="directLevelId">
              <el-select
                v-model="formData.directLevelId"
                placeholder="选择分销商等级"
                clearable
                filterable
                style="width: 160px"
              >
                <el-option v-for="item in distributorLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <!-- 团队分销商等级人数 -->
        <div class="cond-row">
          <div class="col-relation">
            <el-radio-group v-model="formData.teamLevelRelation">
              <el-radio :label="1">与</el-radio>
              <el-radio :label="2">或</el-radio>
            </el-radio-group>
          </div>
          <div class="col-name">团队分销商等级人数</div>
          <div class="col-value">
            <el-form-item prop="teamLevelCount">
              <el-input-number
                v-model="formData.teamLevelCount"
                :min="0"
                :precision="0"
                controls-position="right"
                style="width: 160px"
              />
            </el-form-item>
          </div>
          <div class="col-extra">
            <el-form-item prop="teamLevelId">
              <el-select
                v-model="formData.teamLevelId"
                placeholder="选择分销商等级"
                clearable
                filterable
                style="width: 160px"
              >
                <el-option v-for="item in distributorLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </div>

      <div class="form-tip cond-tip">
        <div class="tip-title"><i class="el-icon-info" /> 判定规则</div>
        <ul class="tip-list">
          <li>按「自购门槛 → 团队门槛 → 直推门槛 → 直推分销商等级人数 → 团队分销商等级人数」顺序，以「与/或」依次连接判定；</li>
          <li>金额/人数为 0 的条件视为自动满足，用「或」连接时等同于跳过该条件；</li>
          <li>等级人数按用户当前分销商等级实时统计（直推=一级推荐人，团队=整条推荐链的所有下级），等级来源于「分销商等级」配置。</li>
        </ul>
      </div>

      <!-- 团队奖配置 -->
      <div class="section-title">团队奖配置</div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="团队奖比例：" prop="config.teamBrokerageRate">
            <el-input-number
              v-model="formData.config.teamBrokerageRate"
              :min="0"
              :max="100"
              :precision="0"
              controls-position="right"
              style="width: 160px"
            />
            <span class="inline-tip">%</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="平级奖比例：" prop="config.peerAwardRate">
            <el-input-number
              v-model="formData.config.peerAwardRate"
              :min="0"
              :max="100"
              :precision="0"
              controls-position="right"
              style="width: 160px"
            />
            <span class="inline-tip">%</span>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 图标与权益 -->
      <div class="section-title">图标与权益</div>
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
          show-word-limit
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
import { distributorLevelListApi } from '@/api/distributorLevel';
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
      distributorLevelOptions: [],
      rules: {
        name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        grade: [{ required: true, message: '请输入等级序号', trigger: 'blur' }],
        selfOrderAmount: [{ required: true, message: '请输入自购门槛', trigger: 'blur' }],
        teamOrderAmount: [{ required: true, message: '请输入团队门槛', trigger: 'blur' }],
        directOrderAmount: [{ required: true, message: '请输入直推门槛', trigger: 'blur' }],
        directLevelCount: [{ required: true, message: '请输入直推分销商等级人数门槛', trigger: 'blur' }],
        directLevelId: [
          {
            validator: (rule, value, callback) => {
              if (this.formData.directLevelCount > 0 && !value) {
                callback(new Error('人数门槛大于0时必须选择目标分销商等级'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
        teamLevelCount: [{ required: true, message: '请输入团队分销商等级人数门槛', trigger: 'blur' }],
        teamLevelId: [
          {
            validator: (rule, value, callback) => {
              if (this.formData.teamLevelCount > 0 && !value) {
                callback(new Error('人数门槛大于0时必须选择目标分销商等级'));
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
      this.loadDistributorLevels();
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
      });
    },
    openEdit(id) {
      this.formData = { ...defaultForm(), id };
      this.dialogVisible = true;
      this.loadDistributorLevels();
      this.$nextTick(() => {
        if (this.$refs.teamForm) {
          this.$refs.teamForm.clearValidate();
        }
        this.loadInfo(id);
      });
    },
    // 分销商等级选项，用于「直推分销商等级人数 / 团队分销商等级人数」条件
    loadDistributorLevels() {
      if (this.distributorLevelOptions.length) return;
      distributorLevelListApi()
        .then((res) => {
          this.distributorLevelOptions = res || [];
        })
        .catch(() => {
          this.distributorLevelOptions = [];
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
::v-deep .team-grade-dialog {
  .el-dialog__body {
    max-height: calc(88vh - 130px);
    overflow-y: auto;
    padding: 16px 24px 8px;
  }
  .el-dialog__footer {
    border-top: 1px solid #f0f0f0;
    padding: 12px 24px;
  }
}

.demo-ruleForm,
.el-form {
  ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
}

.section-title {
  position: relative;
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.88);
  line-height: 22px;
  padding-left: 10px;
  margin: 8px 0 16px;
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 3px;
    width: 3px;
    height: 16px;
    background: #1890ff;
    border-radius: 2px;
  }
}

.inline-tip {
  margin-left: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

/* 升级条件迷你表格 */
.cond-table {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}
.cond-header,
.cond-row {
  display: flex;
  align-items: flex-start;
  padding: 0 16px;
}
.cond-header {
  height: 36px;
  align-items: center;
  background: #fafafa;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.cond-row {
  padding-top: 12px;
  padding-bottom: 4px;
  & + .cond-row {
    border-top: 1px solid #f0f0f0;
  }
  ::v-deep .el-form-item {
    margin-bottom: 12px;
  }
}
.col-relation {
  width: 88px;
  flex-shrink: 0;
  text-align: center;
  line-height: 32px;
  ::v-deep .el-radio {
    margin-right: 8px;
    .el-radio__label {
      padding-left: 4px;
      font-size: 13px;
      color: rgba(0, 0, 0, 0.65);
    }
    .el-radio__label {
      padding-left: 4px;
    }
  }
  .relation-muted {
    color: rgba(0, 0, 0, 0.25);
  }
}
.col-name {
  width: 130px;
  flex-shrink: 0;
  line-height: 32px;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}
.col-value {
  width: 176px;
  flex-shrink: 0;
}
.col-extra {
  flex: 1;
}

/* 判定规则提示框 */
.form-tip {
  font-size: 12px;
  line-height: 20px;
  color: rgba(0, 0, 0, 0.45);
}
.cond-tip {
  margin: 12px 0 16px;
  padding: 10px 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  .tip-title {
    font-weight: 500;
    color: rgba(0, 0, 0, 0.65);
    margin-bottom: 4px;
    i {
      color: #1890ff;
      margin-right: 4px;
    }
  }
  .tip-list {
    margin: 0;
    padding-left: 16px;
    li {
      margin-bottom: 2px;
    }
  }
}

.upLoadPicBox {
  cursor: pointer;
  .pictrue {
    width: 80px;
    height: 80px;
    border: 1px solid #d9d9d9;
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
    transition: border-color 0.2s;
    &:hover {
      border-color: #1890ff;
    }
    .cameraIconfont {
      font-size: 24px;
      color: #8c939d;
    }
  }
}
</style>
