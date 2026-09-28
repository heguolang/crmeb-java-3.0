<template>
  <el-form :model="ruleForm" ref="ruleForm" label-width="0" class="demo-ruleForm">
    <el-form-item>
      <el-alert
        title="拼购分享奖励默认按推荐人的直推人数命中等级阶梯。人工指定后，该会员在任何拼购活动里都固定按所选等级的档位拿分享奖励，不再看直推人数；选「恢复自动判定」可清除。"
        type="warning"
        :closable="false"
      ></el-alert>
    </el-form-item>
    <el-form-item label="拼购等级：" label-width="86px">
      <el-select style="width: 100%" v-model="ruleForm.pingouLevelId" clearable placeholder="请选择拼购等级">
        <el-option label="恢复自动判定（不指定）" :value="0"></el-option>
        <el-option
          v-for="item in pingouLevelList"
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
import { userPingouLevelUpdateApi } from '@/api/user';
import { Debounce } from '@/utils/validate';
export default {
  props: {
    pingouLevelInfo: {
      type: Object,
      default: () => ({}),
    },
    pingouLevelList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      ruleForm: {
        pingouLevelId: 0,
        uid: this.pingouLevelInfo.uid,
      },
    };
  },
  created() {
    this.syncForm(this.pingouLevelInfo);
  },
  watch: {
    pingouLevelInfo(val) {
      this.syncForm(val);
    },
  },
  methods: {
    syncForm(val) {
      this.ruleForm.uid = val.uid || 0;
      this.ruleForm.pingouLevelId =
        val.pingouLevelId !== undefined && val.pingouLevelId !== null
          ? Number(val.pingouLevelId)
          : 0;
    },
    submitForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          userPingouLevelUpdateApi(this.ruleForm).then(() => {
            this.$message.success('编辑成功');
            this.$parent.$parent.getList();
            this.$parent.$parent.pingouLevelVisible = false;
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
      this.$parent.$parent.pingouLevelVisible = false;
    },
  },
};
</script>
