<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="clearfix">
        <span class="title">充值设置</span>
        <span class="tips">会员端充值页面的可选金额档位与赠送金额，可添加多档</span>
      </div>
      <cm-data-list v-if="formData.id" :form-data="formData" />
      <div v-else v-loading="loading" element-loading-text="加载中..." class="loading-box"></div>
    </el-card>
  </div>
</template>

<script>
// 「财务 → 财务操作 → 充值设置」设置页
// 复用「维护 → 组合数据」里 id=62（移动端_充值金额设置）这一数据组的
// 数据列表组件，等价于在原页面点该组的「数据列表」按钮。
import * as systemGroupApi from '@/api/systemGroup';
import cmDataList from '@/views/maintain/devconfig/combineDataList';

const RECHARGE_GID = 62;

export default {
  name: 'rechargeSetting',
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
        .groupInfo({ id: RECHARGE_GID })
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
