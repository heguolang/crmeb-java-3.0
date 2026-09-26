<template>
  <div class="sms-config-placeholder">
    <div class="tips-card">
      <h3>阿里云短信设置</h3>
      <p>短信相关密钥请在「系统设置 → 第三方接口」中配置（aliyun_sms_* 系列配置项）。</p>
      <p>验证码与业务通知模板请在阿里云控制台申请，并在「消息通知」中绑定本地模板 CODE。</p>
      <p>开发联调可开启 <code>aliyun_sms_mock</code>，开启后不真实调用阿里云发送。</p>
      <el-button type="primary" class="mt16" @click="goConfig">前往短信配置说明</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SmsAliyunConfigNote',
  methods: {
    goConfig() {
      this.$router.push('/operation/systemSms/config');
    },
  },
};
</script>

<style scoped>
.sms-config-placeholder {
  padding: 24px;
}
.tips-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  line-height: 2;
  color: #303133;
}
.tips-card h3 {
  margin-bottom: 12px;
}
.tips-card code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.mt16 {
  margin-top: 16px;
}
</style>
