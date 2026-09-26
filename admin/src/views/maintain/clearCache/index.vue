<template>
  <div class="divBox">
    <el-card class="box-card">
      <el-button type="primary" v-hasPermi="['admin:system:config:clear:cache']" v-debounceClick="handleClear"
        >清除缓存</el-button
      >
    </el-card>
  </div>
</template>

<script>
// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
import { clearCacheApi } from '@/api/systemConfig';
export default {
  name: 'clearCache',
  methods: {
    handleClear() {
      clearCacheApi().then((response) => {
        this.$message.success('清除成功');
      });
    },
  },
};
</script>

<style scoped></style>
