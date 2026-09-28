<template>
  <!-- label-width 90px：75px 放不下「用户编号：」5 个字会折行（2026-09-28 用户截图反馈）。
       必填规则已按需求全部移除（rules 删空，后端 @NotNull 同步去掉），所有字段均可留空。 -->
  <el-form :model="ruleForm" ref="ruleForm" label-width="90px" class="demo-ruleForm">
    <el-form-item label="用户编号：" prop="id">
      <el-input v-model="ruleForm.id" disabled></el-input>
    </el-form-item>
    <el-form-item label="用户昵称：" prop="nickname">
      <el-input v-model="ruleForm.nickname" placeholder="请输入用户昵称"></el-input>
    </el-form-item>
    <el-form-item label="用户地址：" prop="addres">
      <el-input v-model="ruleForm.addres"></el-input>
    </el-form-item>
    <el-form-item label="用户备注：" prop="mark">
      <el-input v-model="ruleForm.mark" type="textarea"></el-input>
    </el-form-item>
    <el-form-item label="用户分组：" prop="groupId">
      <el-select style="width: 100%" v-model="ruleForm.groupId" placeholder="请选择" clearable filterable>
        <el-option :value="item.id" v-for="(item, index) in groupList" :key="index" :label="item.groupName"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="用户标签：" prop="labelData">
      <el-select style="width: 100%" v-model="labelData" placeholder="请选择" clearable filterable multiple>
        <el-option :value="item.id" v-for="(item, index) in labelLists" :key="index" :label="item.name"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="推广员：" prop="isPromoter">
      <el-radio-group v-model="ruleForm.isPromoter">
        <el-radio :label="true">开启</el-radio>
        <el-radio :label="false">关闭</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="状态：" prop="status">
      <el-radio-group v-model="ruleForm.status">
        <el-radio :label="true">开启</el-radio>
        <el-radio :label="false">关闭</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item class="dialog-footer-inner">
      <el-button @click="resetForm('ruleForm')">取消</el-button>
      <el-button type="primary" @click="submitForm('ruleForm')" v-hasPermi="['admin:user:update']">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { groupListApi, levelListApi, tagListApi, userInfoApi, userUpdateApi } from '@/api/user';
import { Debounce } from '@/utils/validate';
const defaultObj = {
  // birthday: '',
  // cardId: '',
  id: null,
  nickname: '',
  mark: '',
  //  phone: '',
  // realName: '',
  addres: '',
  groupId: '',
  level: '',
  isPromoter: false,
  status: false,
};
export default {
  name: 'UserEdit',
  props: {
    uid: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      ruleForm: Object.assign({}, defaultObj),
      groupData: [],
      labelData: [],
      labelLists: [],
      levelList: [],
      groupList: [],
      rules: {}, // 必填限制已全部取消（2026-09-28），保留空对象避免 $refs 校验报错
    };
  },
  mounted() {
    if (this.uid) this.userInfo();
    this.groupLists();
    this.levelLists();
    this.getTagList();
  },
  methods: {
    // 详情
    userInfo() {
      userInfoApi({ id: this.uid }).then(async (res) => {
        this.ruleForm = {
          // birthday: res.birthday,
          // cardId: res.cardId,
          id: res.uid,
          nickname: res.nickname || '',
          mark: res.mark,
          // phone: res.phone,
          // realName: res.realName,
          status: res.status,
          addres: res.addres,
          groupId: Number(res.groupId) || '',
          level: res.level || '',
          isPromoter: res.isPromoter,
          tagId: res.tagId || '',
        };
        this.labelData = res.tagId ? res.tagId.split(',').map(Number) : [];
      });
    },
    // 分组列表
    groupLists() {
      groupListApi({ page: 1, limit: 9999 }).then(async (res) => {
        this.groupList = res.list;
      });
    },
    //标签列表
    getTagList() {
      tagListApi({ page: 1, limit: 9999 }).then((res) => {
        this.labelLists = res.list;
      });
    },
    // 等级列表
    levelLists() {
      levelListApi().then(async (res) => {
        this.levelList = res.list;
      });
    },
    submitForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.ruleForm.tagId = this.labelData.join(',');
          userUpdateApi({ id: this.ruleForm.id }, this.ruleForm).then(async (res) => {
            this.$message.success('编辑成功');
            this.$parent.$parent.visible = false;
            this.$parent.$parent.getList();
          });
        } else {
          return false;
        }
      });
    }),
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.$emit('resetForm');
    },
  },
};
</script>

<style scoped>
.selWidth {
  width: 90%;
}
</style>
