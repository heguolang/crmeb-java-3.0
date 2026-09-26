<template>
  <div class="sms-config-placeholder">
    <div class="tips-card">
      <h3>短信套餐说明</h3>
      <p>本系统已接入阿里云短信，套餐与余额请在阿里云控制台购买与管理。</p>
      <p>请在阿里云控制台申请模板，并在「消息通知」中配置本地模板 CODE。</p>
      <el-button type="primary" class="mt16" @click="goConfig">查看短信配置说明</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SmsPay',
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
.mt16 {
  margin-top: 16px;
}
</style>
