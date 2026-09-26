<template>
  <div class="sms-config-placeholder">
    <div class="tips-card">
      <h3>短信 / 物流服务说明</h3>
      <p>本系统短信验证码与物流查询已统一使用第三方云服务商接口。</p>
      <p>请前往「系统设置 → 第三方接口」配置对应的密钥；原一号通在线控制台已下线。</p>
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
.tips-card .brand {
  margin-top: 24px;
  color: #909399;
  font-size: 13px;
}
</style>
