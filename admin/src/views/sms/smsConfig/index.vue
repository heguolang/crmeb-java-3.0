<template>
  <div class="sms-config-placeholder">
    <div class="tips-card">
      <h3>阿里云短信配置说明</h3>
      <p>本系统短信验证码已切换为阿里云短信服务，请在「系统设置 → 第三方接口」中配置以下密钥：</p>
      <ul>
        <li><code>aliyun_sms_access_key_id</code> — AccessKeyId</li>
        <li><code>aliyun_sms_access_key_secret</code> — AccessKeySecret</li>
        <li><code>aliyun_sms_sign_name</code> — 短信签名</li>
        <li><code>aliyun_sms_region_id</code> — 地域（默认 cn-hangzhou）</li>
        <li><code>aliyun_sms_verify_template_code</code> — 验证码模板 CODE</li>
        <li><code>aliyun_sms_mock</code> — 模拟发送开关（1=开启，开发环境可用，不真实扣费）</li>
      </ul>
      <p>开启 mock 后，验证码会写入日志/返回调试信息，不会调用阿里云真实发送接口。</p>
      <p>业务通知类模板请在阿里云控制台申请，并在「消息通知」中配置对应本地模板 CODE。</p>
      <p class="brand">黔序商城 · 贵州黔序科技有限公司</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SmsConfig',
  data() {
    return {};
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
.tips-card ul {
  margin: 12px 0 16px;
  padding-left: 22px;
}
.tips-card code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.tips-card .brand {
  margin-top: 24px;
  color: #909399;
  font-size: 13px;
}
</style>
