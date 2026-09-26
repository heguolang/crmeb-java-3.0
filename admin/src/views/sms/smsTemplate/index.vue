<template>
  <div class="sms-config-placeholder">
    <div class="tips-card">
      <h3>短信模板说明</h3>
      <p>请在阿里云控制台申请短信模板，并在「消息通知」中配置本地模板 CODE。</p>
      <p>验证码模板对应配置项：<code>aliyun_sms_verify_template_code</code>。</p>
      <p>其他业务通知模板请在系统设置 / 消息通知中按场景绑定阿里云模板 CODE。</p>
      <el-button type="primary" class="mt16" @click="goNotification">前往消息通知</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SmsTemplate',
  methods: {
    goNotification() {
      this.$router.push('/operation/notification');
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
