<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="clearfix">
        <span class="title">签到配置</span>
        <span class="tips">会员端「我的 - 签到」连续签到天数的奖励设置，按天配置积分与经验</span>
      </div>
      <cm-data-list v-if="formData.id" :form-data="formData" />
      <div v-else v-loading="loading" element-loading-text="加载中..." class="loading-box"></div>
    </el-card>
  </div>
</template>

<script>
// 「营销 → 积分 → 签到配置」设置页
// 复用「维护 → 组合数据」里 id=55（移动端_我的_签到天数配置）这一数据组的
// 数据列表组件，等价于在原页面点该组的「数据列表」按钮。
import * as systemGroupApi from '@/api/systemGroup';
import cmDataList from '@/views/maintain/devconfig/combineDataList';

const SIGN_IN_GID = 55;

export default {
  name: 'integralSignIn',
  components: { cmDataList },
  data() {
    return {
      loading: true,
      formData: {},
    };
  },
  mounted() {
    this.getGroupInfo();
  },
  methods: {
    getGroupInfo() {
      systemGroupApi
        .groupInfo({ id: SIGN_IN_GID })
        .then((data) => {
          this.formData = data || {};
        })
        .finally(() => {
          this.loading = false;
        });
    },
  },
};
</script>

<style scoped lang="scss">
.title {
  font-size: 16px;
  font-weight: 600;
}
.tips {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
.loading-box {
  min-height: 180px;
}
</style>
