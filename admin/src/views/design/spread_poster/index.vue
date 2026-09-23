<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt">
      <div slot="header" class="clearfix">
        <span class="title">推广海报</span>
        <span class="tips">用于会员端「我的推广」页面的分享海报，可添加多张轮播展示，图片建议尺寸 750 × 1334px</span>
      </div>
      <cm-data-list v-if="formData.id" :form-data="formData" />
      <div v-else v-loading="loading" element-loading-text="加载中..." class="loading-box"></div>
    </el-card>
  </div>
</template>

<script>
// 「我的推广 - 分享海报」设置页
// 直接复用「开发配置 → 组合数据」里 id=60（Constants.GROUP_DATA_ID_SPREAD_BANNER_LIST）
// 这一数据组的数据列表组件，等价于在原页面点该组的「数据列表」按钮。
import * as systemGroupApi from '@/api/systemGroup';
import cmDataList from '@/views/maintain/devconfig/combineDataList';

const SPREAD_POSTER_GID = 60;

export default {
  name: 'spreadPoster',
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
        .groupInfo({ id: SPREAD_POSTER_GID })
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
