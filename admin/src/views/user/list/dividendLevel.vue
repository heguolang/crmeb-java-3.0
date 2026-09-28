<template>
  <el-form :model="ruleForm" ref="ruleForm" label-width="0" class="demo-ruleForm">
    <el-form-item>
      <el-alert
        title="分红等级默认按各等级自身的升级条件自动判定（可同时达标多个）。人工指定后，该会员只在所选等级参与分红，不再参与其它等级；选「恢复自动判定」可清除。"
        type="warning"
        :closable="false"
      ></el-alert>
    </el-form-item>
    <el-form-item label="分红等级：" label-width="86px">
      <el-select style="width: 100%" v-model="ruleForm.dividendLevelId" clearable placeholder="请选择分红等级">
        <el-option label="恢复自动判定（不指定）" :value="0"></el-option>
        <el-option
          v-for="item in dividendLevelList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item class="dialog-footer-inner">
      <el-button @click="resetForm('ruleForm')">取消</el-button>
      <el-button type="primary" @click="submitForm('ruleForm')">确定</el-button>
    </el-form-item>
  </el-form>
</template>
<script>
import { userDividendLevelUpdateApi } from '@/api/user';
import { Debounce } from '@/utils/validate';
export default {
  props: {
    dividendLevelInfo: {
      type: Object,
      default: () => ({}),
    },
    dividendLevelList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      ruleForm: {
        dividendLevelId: 0,
        uid: this.dividendLevelInfo.uid,
      },
    };
  },
  created() {
    this.syncForm(this.dividendLevelInfo);
  },
  watch: {
    dividendLevelInfo(val) {
      this.syncForm(val);
    },
  },
  methods: {
    syncForm(val) {
      this.ruleForm.uid = val.uid || 0;
      this.ruleForm.dividendLevelId =
        val.dividendLevelId !== undefined && val.dividendLevelId !== null
          ? Number(val.dividendLevelId)
          : 0;
    },
    submitForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          userDividendLevelUpdateApi(this.ruleForm).then(() => {
            this.$message.success('编辑成功');
            this.$parent.$parent.getList();
            this.$parent.$parent.dividendLevelVisible = false;
            this.$refs[formName].resetFields();
          });
        } else {
          return false;
        }
      });
    }),
    resetForm(formName) {
      this.$nextTick(() => {
        this.$refs[formName].resetFields();
      });
      this.$parent.$parent.dividendLevelVisible = false;
    },
  },
};
</script>
