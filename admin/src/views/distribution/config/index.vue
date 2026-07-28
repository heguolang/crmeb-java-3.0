<template>
  <div class="divBox">
    <el-card class="box-card">
      <el-form
        ref="promoterForm"
        :model="promoterForm"
        :rules="rules"
        label-width="200px"
        class="demo-promoterForm"
        v-loading="loading"
      >
        <el-form-item prop="brokerageFuncStatus">
          <span slot="label">
            <span>分销启用：</span>
            <el-tooltip class="item" effect="dark" content="商城分销功能开启关闭" placement="top-start">
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.brokerageFuncStatus">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="storeBrokerageQuota">
          <span slot="label">
            <span>满额分销最低金额：</span>
            <el-tooltip class="item" effect="dark" content="满额分销满足金额开通分销权限" placement="top-start">
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-input-number
            controls-position="right"
            v-model="promoterForm.storeBrokerageQuota"
            placeholder="满额分销满足金额开通分销权限"
            :min="-1"
            :step="1"
            class="selWidth"
            @keydown.native="channelInputLimit"
          ></el-input-number>
        </el-form-item>
        <!-- <el-form-item prop="storeBrokerageStatus">
          <span slot="label">
            <span>分销模式：</span>
            <el-tooltip class="item" effect="dark" content="人人分销”默认每个人都可以分销，“指定分销”仅可后台手动设置推广员" placement="top-start">
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.storeBrokerageStatus">
            <el-radio label="1">指定分销</el-radio>
            <el-radio label="2">人人分销</el-radio>
          </el-radio-group>
        </el-form-item> -->
        <el-form-item prop="brokerageBindind">
          <span slot="label">
            <span>分销关系绑定：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="所有用户”指所有没有上级推广人的用户，“新用户”指新注册的用户"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.brokerageBindind">
            <el-radio label="0">所有用户</el-radio>
            <el-radio label="1">新用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="registerDefaultIsPromoter">
          <span slot="label">
            <span>注册默认推广员：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="用户首次注册/登录时是否自动成为推广员（可发展下级）"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.registerDefaultIsPromoter">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="registerDefaultUserLevel">
          <span slot="label">
            <span>注册默认会员等级：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="用户首次注册/登录时默认赋予的会员等级，选「不设置」则保持无等级"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-select v-model="promoterForm.registerDefaultUserLevel" placeholder="请选择" class="selWidth" clearable>
            <el-option :value="0" label="不设置"></el-option>
            <el-option
              v-for="item in levelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="storeBrokerageIsBubble">
          <span slot="label">
            <span>分销气泡：</span>
            <el-tooltip class="item" effect="dark" content="基础商品详情页分销气泡功能开启关闭" placement="top-start">
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.storeBrokerageIsBubble">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="storeBrokerageRatio">
          <span slot="label">
            <span>一级返佣比例：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="订单交易成功后给上级返佣的比例0 - 100,例:5 = 反订单金额的5%"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-input-number
            controls-position="right"
            v-model="promoterForm.storeBrokerageRatio"
            step-strictly
            :min="0"
            :max="100"
            class="selWidth"
            placeholder="订单交易成功后给上级返佣的比例0 - 100,例:5 = 反订单金额的5%"
          ></el-input-number>
          <span>%</span>
        </el-form-item>
        <el-form-item prop="storeBrokerageTwo">
          <span slot="label">
            <span>二级返佣比例：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="订单交易成功后给上级返佣的比例0 ~ 100,例:5 = 反订单金额的5%"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-input-number
            controls-position="right"
            v-model="promoterForm.storeBrokerageTwo"
            step-strictly
            :min="0"
            :max="100"
            class="selWidth"
            placeholder="订单交易成功后给上级返佣的比例0 ~ 100,例:5 = 反订单金额的5%"
          ></el-input-number>
          <span>%</span>
        </el-form-item>
        <el-form-item prop="extractTime">
          <span slot="label">
            <span>冻结时间：</span>
            <el-tooltip class="item" effect="dark" content="佣金冻结时间(天)，仅在到账方式为订单完成时作为初始冻结天数参考" placement="top-start">
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-input-number
            controls-position="right"
            v-model="promoterForm.extractTime"
            :min="0"
            class="selWidth"
            placeholder="佣金冻结时间(天)"
          ></el-input-number>
        </el-form-item>
        <el-form-item prop="brokerageCreditTiming">
          <span slot="label">
            <span>佣金到账方式：</span>
            <el-tooltip
              class="item"
              effect="dark"
              content="支付订单到账：付款成功立即入账；订单完成到账：用户确认收货后入账"
              placement="top-start"
            >
              <i class="el-icon-warning-outline" />
            </el-tooltip>
          </span>
          <el-radio-group v-model="promoterForm.brokerageCreditTiming">
            <el-radio :label="1">支付订单到账</el-radio>
            <el-radio :label="2">订单完成到账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="提现最低金额、手续费、银行卡请到「财务 → 提现设置」配置"
          style="margin: 0 0 16px 200px; max-width: 480px"
        />
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="submitForm('promoterForm')"
            v-hasPermi="['admin:retail:spread:manage:set']"
            >提交</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { configApi, configUpdateApi, productCheckApi } from '@/api/distribution';
import { levelAllApi } from '@/api/user';
import * as selfUtil from '@/utils/ZBKJIutil.js';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
import { Debounce } from '@/utils/validate';
export default {
  name: 'Index',
  data() {
    return {
      promoterForm: {},
      levelList: [],
      loading: true,
      rules: {
        brokerageFuncStatus: [{ required: true, message: '请选择是否启用分销', trigger: 'change' }],
        storeBrokerageRatio: [{ required: true, message: '请输入一级返佣比例', trigger: 'blur' }],
        storeBrokerageTwo: [{ required: true, message: '请输入二级返佣比例', trigger: 'blur' }],
        registerDefaultIsPromoter: [{ required: true, message: '请选择注册是否默认推广员', trigger: 'change' }],
        registerDefaultUserLevel: [{ required: true, message: '请选择注册默认会员等级', trigger: 'change' }],
      },
    };
  },
  mounted() {
    this.getLevelList();
    this.getDetal();
  },
  methods: {
    checkPermi,
    channelInputLimit(e) {
      let key = e.key;
      // 不允许输入'e'和'.'
      if (key === 'e' || key === '.') {
        e.returnValue = false;
        return false;
      }
      return true;
    },
    getLevelList() {
      levelAllApi()
        .then((res) => {
          this.levelList = res || [];
        })
        .catch(() => {
          this.levelList = [];
        });
    },
    getDetal() {
      this.loading = true;
      configApi()
        .then((res) => {
          this.loading = false;
          this.promoterForm = res;
          this.promoterForm.storeBrokerageIsBubble = res.storeBrokerageIsBubble.toString();
          this.promoterForm.brokerageFuncStatus = res.brokerageFuncStatus.toString();
          this.promoterForm.brokerageBindind = res.brokerageBindind.toString();
          this.promoterForm.registerDefaultIsPromoter = (
            res.registerDefaultIsPromoter == null ? 0 : res.registerDefaultIsPromoter
          ).toString();
          this.promoterForm.registerDefaultUserLevel =
            res.registerDefaultUserLevel == null ? 0 : Number(res.registerDefaultUserLevel);
          this.promoterForm.brokerageCreditTiming =
            res.brokerageCreditTiming == null ? 1 : Number(res.brokerageCreditTiming);
        })
        .catch((res) => {
          this.$message.error(res.message);
        });
    },
    submitForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (selfUtil.Add(this.promoterForm.storeBrokerageRatio, this.promoterForm.storeBrokerageTwo) > 100)
            return this.$message.warning('返佣比例相加不能超过100%');
          this.loading = true;
          const payload = {
            ...this.promoterForm,
            registerDefaultIsPromoter: Number(this.promoterForm.registerDefaultIsPromoter),
            registerDefaultUserLevel: Number(this.promoterForm.registerDefaultUserLevel || 0),
            brokerageCreditTiming: Number(this.promoterForm.brokerageCreditTiming || 1),
          };
          configUpdateApi(payload)
            .then((res) => {
              this.loading = false;
              this.$message.success('提交成功');
            })
            .catch((err) => {
              this.loading = false;
            });
        } else {
          return false;
        }
      });
    }),
  },
};
</script>

<style scoped lang="scss">
.selWidth {
  width: 300px;
}
</style>
