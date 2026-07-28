<template>
  <el-form :model="ruleForm" ref="ruleForm" label-width="0" class="demo-ruleForm">
    <el-form-item>
      <el-alert title="手动调整后，后续订单统计仍可能自动变更团队等级，请谨慎操作。" type="warning" :closable="false"></el-alert>
    </el-form-item>
    <el-form-item label="团队等级：" label-width="86px">
      <el-select style="width: 100%" v-model="ruleForm.teamLevelId" clearable placeholder="请选择团队等级">
        <el-option label="无（清空）" :value="0"></el-option>
        <el-option
          v-for="item in teamLevelList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
          :disabled="item.isShow === false"
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
import { userTeamLevelUpdateApi } from '@/api/user';
import { Debounce } from '@/utils/validate';
export default {
  props: {
    teamLevelInfo: {
      type: Object,
      default: () => ({}),
    },
    teamLevelList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      ruleForm: {
        teamLevelId: 0,
        uid: this.teamLevelInfo.uid,
      },
    };
  },
  created() {
    this.ruleForm.teamLevelId =
      this.teamLevelInfo.teamLevel !== undefined && this.teamLevelInfo.teamLevel !== null
        ? Number(this.teamLevelInfo.teamLevel)
        : 0;
  },
  watch: {
    teamLevelInfo(val) {
      this.ruleForm.uid = val.uid || 0;
      this.ruleForm.teamLevelId =
        val.teamLevel !== undefined && val.teamLevel !== null ? Number(val.teamLevel) : 0;
    },
  },
  methods: {
    submitForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (this.ruleForm.teamLevelId === '' || this.ruleForm.teamLevelId === null || this.ruleForm.teamLevelId === undefined) {
            this.$message.warning('请选择团队等级');
            return;
          }
          userTeamLevelUpdateApi(this.ruleForm).then(() => {
            this.$message.success('编辑成功');
            this.$parent.$parent.getList();
            this.$parent.$parent.teamLevelVisible = false;
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
      this.$parent.$parent.teamLevelVisible = false;
    },
  },
};
</script>
