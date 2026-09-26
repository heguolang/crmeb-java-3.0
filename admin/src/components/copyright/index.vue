<template>
  <div class="ivu-global-footer i-copyright">
    <div class="footers acea-row">
      <div class="title" v-text="copyrightNew"></div>
    </div>
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

import { getCopyrightInfo } from '@/libs/copyPublic';

export default {
  name: 'i-copyright',
  data() {
    return {
      copyright: '黔序科技',
      copyrightNew: '',
    };
  },
  mounted() {
    this.getVersion();
  },
  methods: {
    getVersion() {
      getCopyrightInfo().then((res) => {
        const data = res || {};
        this.copyrightNew = data.companyName ? data.companyName : this.copyright;
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.ivu-global-footer {
  /* margin: 48px 0 24px 0; */
  /* padding: 0 16px; */
  // margin: 15px 0px;
  text-align: center;
  box-sizing: border-box;
  // margin-left: 210px;
}
.i-copyright {
  flex: 0 0 auto;
}
.ivu-global-footer-links {
  margin-bottom: 8px;
}
.ivu-global-footer-links a:not(:last-child) {
  margin-right: 40px;
}
.ivu-global-footer-links a {
  font-size: 14px;
  color: #808695;
  transition: all 0.2s ease-in-out;
}
.ivu-global-footer-copyright {
  color: #808695;
  font-size: 14px;
}
</style>
