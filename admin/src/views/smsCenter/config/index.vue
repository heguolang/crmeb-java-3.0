<template>
  <div class="sms-config-page">
    <!-- 统计条 -->
    <el-card shadow="never" class="mb14">
      <div class="summary-row">
        <div class="summary-item">
          <div class="summary-label">滨礼余额（条）</div>
          <div class="summary-value">{{ balanceText }}</div>
        </div>
        <div class="summary-divider" />
        <div class="summary-item">
          <div class="summary-label">当前通道</div>
          <div class="summary-value" :class="{ 'is-primary': form.channel === 'binli' }">{{ channelText }}</div>
        </div>
        <div class="summary-divider" />
        <div class="summary-item">
          <div class="summary-label">模拟发送</div>
          <div class="summary-value" :class="form.mock === '1' ? 'is-warning' : 'is-success'">{{ form.mock === '1' ? '开启' : '关闭' }}</div>
        </div>
        <div class="summary-tip">数据只读 · 保存配置后自动刷新</div>
      </div>
    </el-card>

    <!-- 通道配置 -->
    <el-card shadow="never" class="mb14">
      <div slot="header" class="card-title">短信通道配置</div>
      <el-form label-width="110px" class="config-form" @submit.native.prevent>
        <el-form-item label="发送通道">
          <el-radio-group v-model="form.channel">
            <el-radio label="binli">滨礼 binli</el-radio>
            <el-radio label="aliyun">阿里云 aliyun</el-radio>
          </el-radio-group>
          <div class="form-tip">切换后保存生效，验证码与通知短信全部走新通道</div>
        </el-form-item>
        <el-form-item label="滨礼账号">
          <el-input v-model="form.account" placeholder="滨礼账号" class="w260" />
        </el-form-item>
        <el-form-item label="接口密码">
          <el-input
            v-model="form.pwd"
            type="password"
            show-password
            class="w260"
            :placeholder="pwdSet ? '已设置，留空表示不修改' : '请输入接口密码'"
            autocomplete="new-password"
          />
          <div class="form-tip">调用时自动 MD5 加密传输，后台不回显明文</div>
        </el-form-item>
        <el-form-item label="模拟发送">
          <el-switch v-model="mockOn" active-text="开启" inactive-text="关闭" />
          <div class="form-tip is-warning">开启 = 只记日志不真发不扣量，用于本地联调；正式上线必须关闭</div>
        </el-form-item>
        <el-form-item label="注册验证码">
          <el-switch v-model="registerCaptchaOn" active-text="需要" inactive-text="不需要" />
          <div class="form-tip">开启后，H5 会员端注册必须填写短信验证码；关闭后注册跳过验证码校验</div>
        </el-form-item>
        <el-form-item label="验证码内容">
          <el-input
            v-model="form.verifyContent"
            type="textarea"
            :rows="2"
            class="w560"
            placeholder="【签名】您的验证码是{code}，{time}分钟内有效"
          />
          <div class="form-tip">支持占位符：{code}=验证码，{time}=有效分钟数</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
          <el-button @click="loadConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 测试发送 -->
    <el-card shadow="never">
      <div slot="header" class="card-title">
        测试发送
        <span class="card-sub">模拟模式下只落库返回 MOCK 批次号，不真实发送</span>
      </div>
      <div class="test-row">
        <el-input v-model="test.phone" placeholder="手机号" class="w160" />
        <el-input v-model="test.content" placeholder="短信内容，如：【黔序】短信接口联调测试" class="test-content" />
        <el-button type="primary" :loading="testing" @click="doTestSend">发送测试</el-button>
      </div>
      <div v-if="testResult" class="test-result">
        最近一次结果：{{ testResult }}
        <span v-if="lastTaskId" class="id-chip">{{ lastTaskId }}</span>
        <span class="result-tip">（可在「发送记录」页查看回执）</span>
      </div>
      <div class="callback-info">
        <div>状态推送回调地址（滨礼平台后台填写，收到后自动写回记录状态）：<span class="callback-url">https://apiqf.qianxutec.com/api/front/sms/push/report</span></div>
        <div>也可用「发送记录」页右上角的「同步回执」按钮主动拉取。</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { smsChannelConfigApi, smsChannelSaveApi, smsBalanceApi, smsTestSendApi } from '@/api/smsCenter';

export default {
  name: 'SmsCenterConfig',
  data() {
    return {
      form: {
        channel: 'binli',
        account: '',
        pwd: '',
        mock: '1',
        registerCaptcha: '1',
        verifyContent: '',
      },
      pwdSet: false,
      balance: null,
      saving: false,
      testing: false,
      test: { phone: '', content: '' },
      testResult: '',
      lastTaskId: '',
    };
  },
  computed: {
    mockOn: {
      get() {
        return this.form.mock === '1';
      },
      set(v) {
        this.form.mock = v ? '1' : '0';
      },
    },
    registerCaptchaOn: {
      get() {
        return this.form.registerCaptcha === '1';
      },
      set(v) {
        this.form.registerCaptcha = v ? '1' : '0';
      },
    },
    balanceText() {
      return this.balance === null || this.balance === undefined ? '—' : Number(this.balance).toLocaleString();
    },
    channelText() {
      return this.form.channel === 'binli' ? '滨礼' : '阿里云';
    },
  },
  mounted() {
    this.loadConfig();
    this.loadBalance();
  },
  methods: {
    loadConfig() {
      // 拦截器已返回 CommonResult.data，此处 res 即配置 map
      smsChannelConfigApi()
        .then((res) => {
          const d = res || {};
          this.form.channel = d.channel || 'binli';
          this.form.account = d.account || '';
          this.form.mock = d.mock === '0' ? '0' : '1';
          this.form.registerCaptcha = d.register_captcha === '0' ? '0' : '1';
          this.form.verifyContent = d.verify_content || '';
          this.form.pwd = '';
          this.pwdSet = !!d.pwd_set;
        })
        .catch(() => {});
    },
    loadBalance() {
      smsBalanceApi()
        .then((res) => {
          this.balance = (res || {}).balance;
        })
        .catch(() => {
          this.balance = null;
        });
    },
    saveConfig() {
      if (!this.form.account) {
        return this.$message.error('滨礼账号不能为空');
      }
      const data = {
        channel: this.form.channel,
        account: this.form.account,
        mock: this.form.mock,
        registerCaptcha: this.form.registerCaptcha,
        verifyContent: this.form.verifyContent,
      };
      if (this.form.pwd) {
        data.pwd = this.form.pwd;
      }
      this.saving = true;
      smsChannelSaveApi(data)
        .then(() => {
          this.$message.success('保存成功');
          this.form.pwd = '';
          this.loadConfig();
          this.loadBalance();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    doTestSend() {
      if (!this.test.phone) {
        return this.$message.error('请输入手机号');
      }
      if (!this.test.content) {
        return this.$message.error('请输入短信内容');
      }
      this.testing = true;
      smsTestSendApi({ phone: this.test.phone, content: this.test.content })
        .then((res) => {
          this.lastTaskId = (res && res.taskId) || '';
          this.testResult = '已提交';
          this.$message.success('发送成功');
          this.loadBalance();
        })
        .finally(() => {
          this.testing = false;
        });
    },
  },
};
</script>

<style scoped>
.sms-config-page {
  padding: 0 14px 14px;
}
.mb14 {
  margin-bottom: 14px;
}
.card-title {
  font-size: 15px;
  font-weight: 500;
}
.card-sub {
  margin-left: 10px;
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}
.summary-row {
  display: flex;
  align-items: center;
}
.summary-item {
  margin-right: 32px;
}
.summary-label {
  font-size: 13px;
  color: #909399;
}
.summary-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  font-variant-numeric: tabular-nums;
}
.summary-value.is-primary {
  color: #409eff;
}
.summary-value.is-warning {
  color: #e6a23c;
}
.summary-value.is-success {
  color: #67c23a;
}
.summary-divider {
  width: 1px;
  height: 36px;
  background: #ebeef5;
  margin-right: 32px;
}
.summary-tip {
  margin-left: auto;
  font-size: 13px;
  color: #c0c4cc;
}
.config-form {
  max-width: 720px;
}
.w260 {
  width: 260px;
}
.w160 {
  width: 160px;
}
.w560 {
  max-width: 560px;
}
.form-tip {
  font-size: 13px;
  color: #c0c4cc;
  line-height: 1.6;
}
.form-tip.is-warning {
  color: #e6a23c;
}
.test-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.test-content {
  flex: 1;
}
.test-result {
  margin-top: 12px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}
.id-chip {
  display: inline-block;
  margin-left: 2px;
  padding: 0 6px;
  border-radius: 3px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  line-height: 18px;
  font-variant-numeric: tabular-nums;
}
.result-tip {
  margin-left: 6px;
  color: #c0c4cc;
}
.callback-info {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #909399;
  line-height: 1.9;
}
.callback-url {
  color: #303133;
}
</style>
