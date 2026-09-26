<template>
  <div>
    <parser
      v-if="formConf.fields.length > 0"
      v-loading="loading"
      :is-edit="isCreate === 1"
      :form-conf="formConf"
      :form-edit-data="editData"
      @submit="handlerSubmit"
      @resetForm="resetForm"
      @closeDialog="closeDialog"
      :key="keyNum"
    />
    <!--editData:{{ editData }}-->
    <!--    formConf:{{ formConf }}-->
    <!--    isCreate:{{ isCreate }}-->
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

/**
 * 注意：和Parser唯一的区别就是这里仅仅传入表单配置id即可自动加载已配置的表单
 *      数据后渲染表单，
 *      其他业务和Parser保持一致
 */
import { getFormTempByNameApi } from '@/api/systemFormConfig.js';
import parser from '@/components/FormGenerator/components/parser/Parser';
import { Debounce } from '@/utils/validate';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'ZBParser',
  components: { parser },
  props: {
    formName: {
      type: String,
      required: '',
    },
    isCreate: {
      type: Number,
      default: 0, // 0=create 1=edit
    },
    editData: {
      type: Object,
    },
    keyNum: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      loading: false,
      formConf: { fields: [] },
    };
  },
  watch: {
    keyNum: {
      handler(val) {
        this.formConf = { fields: [] };
        this.handlerGetFormConfig(this.formName);
      },
      deep: true,
      immediate: false,
    },
  },
  mounted() {
    this.handlerGetFormConfig(this.formName);
  },
  methods: {
    checkPermi,
    handlerGetFormConfig(formName) {
      // 获取表单配置后生成table列
      this.loading = true;
      const _pram = { name: encodeURIComponent(formName) };
      getFormTempByNameApi(_pram)
        .then((data) => {
          this.formConf = JSON.parse(data.content);
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handlerSubmit: Debounce(function (formValue) {
      this.$emit('submit', formValue);
    }),
    closeDialog() {
      this.$msgbox.close();
    },
    resetForm(formValue) {
      this.$emit('resetForm', formValue);
    },
  },
};
</script>

<style scoped></style>
