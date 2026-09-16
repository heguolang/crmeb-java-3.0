<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" :model="tableFrom" label-width="80px">
          <el-form-item label="代理UID：">
            <el-input v-model="tableFrom.uid" placeholder="请输入UID" clearable class="selWidth" @keyup.enter.native="searchList" />
          </el-form-item>
          <el-form-item label="订单号：">
            <el-input v-model="tableFrom.orderId" placeholder="请输入订单号" clearable class="selWidth" @keyup.enter.native="searchList" />
          </el-form-item>
          <el-form-item label="状态：">
            <el-select v-model="tableFrom.status" placeholder="全部状态" clearable class="selWidth">
              <el-option label="待入账" :value="1" />
              <el-option label="已入账" :value="2" />
              <el-option label="已失效" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchList">搜索</el-button>
            <el-button @click="resetList">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="mini" highlight-current-row>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="代理" min-width="130">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname || '-' }}</div>
            <div class="sub-text">UID: {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="代理区域" min-width="140" show-overflow-tooltip />
        <el-table-column prop="orderId" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="orderPayPrice" label="订单实付" width="100" />
        <el-table-column label="比例" width="80">
          <template slot-scope="scope">{{ scope.row.ratio }}%</template>
        </el-table-column>
        <el-table-column label="奖励金额" width="110">
          <template slot-scope="scope">
            <span class="color_red">+{{ scope.row.rewardPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditTime" label="入账时间" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { agentRewardListApi } from '@/api/daili';

export default {
  name: 'AgentReward',
  data() {
    return {
      listLoading: false,
      tableFrom: {
        page: 1,
        limit: 20,
        uid: '',
        orderId: '',
        status: undefined,
      },
      tableData: { data: [], total: 0 },
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    statusLabel(status) {
      const map = { 1: '待入账', 2: '已入账', 3: '已失效' };
      return map[status] || '-';
    },
    statusTagType(status) {
      const map = { 1: 'warning', 2: 'success', 3: 'danger' };
      return map[status] || 'info';
    },
    searchList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    resetList() {
      this.tableFrom = { page: 1, limit: 20, uid: '', orderId: '', status: undefined };
      this.getList();
    },
    getList() {
      this.listLoading = true;
      const params = { ...this.tableFrom };
      if (params.status === '' || params.status === null || params.status === undefined) delete params.status;
      if (!params.uid) delete params.uid;
      if (!params.orderId) delete params.orderId;
      agentRewardListApi(params)
        .then((res) => {
          this.tableData.data = (res && res.list) || [];
          this.tableData.total = (res && res.total) || 0;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.tableFrom.page = 1;
      this.getList();
    },
  },
};
</script>

<style scoped lang="scss">
.selWidth {
  width: 220px;
}
.sub-text {
  color: #999;
  font-size: 12px;
}
.color_red {
  color: #f5222d;
}
</style>
