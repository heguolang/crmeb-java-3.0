<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item label="用户UID">
          <el-input v-model.number="tableFrom.uid" placeholder="UID" clearable style="width: 110px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="tableFrom.type" placeholder="全部" clearable style="width: 140px">
            <el-option label="差价奖励" :value="1" />
            <el-option label="阶梯奖励" :value="2" />
            <el-option label="平级奖励" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="单号">
          <el-input v-model="tableFrom.orderNo" placeholder="关联单号" clearable style="width: 180px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column label="得奖代理" min-width="110">
          <template slot-scope="scope">{{ scope.row.nickname }}（UID {{ scope.row.uid }}）</template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template slot-scope="scope">
            <el-tag size="mini" :type="['', 'success', 'primary', 'warning'][scope.row.type]">{{ typeName(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联单号" width="170" />
        <el-table-column label="业绩来源" width="140">
          <template slot-scope="scope">
            <template v-if="scope.row.linkUid">
              <div class="cell-main">{{ scope.row.linkNickname || '—' }}</div>
              <div class="cell-sub">UID {{ scope.row.linkUid }}</div>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="计算基数" width="100" />
        <el-table-column prop="rate" label="比例%" width="80" />
        <el-table-column prop="rewardPrice" label="奖励金额" width="100">
          <template slot-scope="scope"><b class="green">¥{{ scope.row.rewardPrice }}</b></template>
        </el-table-column>
        <el-table-column prop="mark" label="说明" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="150" />
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { stockRewardListApi } from '@/api/stock';

export default {
  name: 'StockReward',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, uid: null, type: null, orderNo: '' }
    };
  },
  methods: {
    getList() {
      this.loading = true;
      stockRewardListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    typeName(t) {
      return { 1: '差价奖励', 2: '阶梯奖励', 3: '平级奖励', 4: '货款成本' }[t] || t;
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
.green { color: #67c23a; }
.cell-main { font-size: 13px; color: #303133; }
.cell-sub { font-size: 12px; color: #909399; }
</style>
