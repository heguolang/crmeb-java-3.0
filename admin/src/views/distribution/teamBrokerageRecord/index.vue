<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" :model="tableFrom" label-width="90px">
          <el-form-item label="关键字：">
            <el-input
              v-model="tableFrom.keywords"
              placeholder="UID/昵称/手机号/订单号"
              clearable
              class="selWidth"
              @keyup.enter.native="searchList"
            />
          </el-form-item>
          <el-form-item label="奖项类型：">
            <el-select v-model="tableFrom.brokerageLevel" placeholder="全部团队奖" clearable class="selWidth">
              <el-option
                v-for="item in teamBrokerageLevelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态：">
            <el-select v-model="tableFrom.status" placeholder="全部状态" clearable class="selWidth">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间：">
            <el-date-picker
              v-model="timeVal"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              align="right"
              @change="onDateChange"
            />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.userName || '-' }}</div>
            <div class="sub-text">UID: {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="100">
          <template slot-scope="scope">
            <span class="color_red">+{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="奖项类型" min-width="120">
          <template slot-scope="scope">
            <el-tag size="mini" :type="getBrokerageLevelTagType(scope.row.brokerageLevel)">
              {{ getBrokerageLevelLabel(scope.row.brokerageLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="130" />
        <el-table-column prop="linkId" label="关联订单" min-width="160" show-overflow-tooltip />
        <el-table-column prop="mark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" min-width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="时间" width="170" />
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
import { teamBrokerageRecordListApi } from '@/api/teamLevel';
import {
  BROKERAGE_LEVEL_TEAM_DIFF,
  BROKERAGE_LEVEL_TEAM_PEER,
  getBrokerageLevelLabel,
  getBrokerageLevelTagType,
} from '@/utils/brokerage';

export default {
  name: 'TeamBrokerageRecord',
  data() {
    return {
      listLoading: false,
      timeVal: [],
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        brokerageLevel: undefined,
        status: undefined,
        dateLimit: '',
      },
      tableData: {
        data: [],
        total: 0,
      },
      teamBrokerageLevelOptions: [
        { value: BROKERAGE_LEVEL_TEAM_DIFF, label: '团队极差奖' },
        { value: BROKERAGE_LEVEL_TEAM_PEER, label: '团队平级奖' },
      ],
      statusOptions: [
        { value: 1, label: '待入账' },
        { value: 2, label: '冻结中' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已失效' },
      ],
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    getBrokerageLevelLabel,
    getBrokerageLevelTagType,
    statusLabel(status) {
      const map = { 1: '待入账', 2: '冻结中', 3: '已完成', 4: '已失效', 5: '提现申请' };
      return map[status] || '-';
    },
    statusTagType(status) {
      const map = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger', 5: '' };
      return map[status] || 'info';
    },
    onDateChange(val) {
      if (!val || val.length !== 2) {
        this.tableFrom.dateLimit = '';
        return;
      }
      this.tableFrom.dateLimit = `${val[0]},${val[1]}`;
    },
    searchList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    resetList() {
      this.timeVal = [];
      this.tableFrom = {
        page: 1,
        limit: 20,
        keywords: '',
        brokerageLevel: undefined,
        status: undefined,
        dateLimit: '',
      };
      this.getList();
    },
    getList() {
      this.listLoading = true;
      const params = { ...this.tableFrom };
      if (params.brokerageLevel === '' || params.brokerageLevel === null) {
        delete params.brokerageLevel;
      }
      if (params.status === '' || params.status === null) {
        delete params.status;
      }
      if (!params.dateLimit) {
        delete params.dateLimit;
      }
      if (!params.keywords) {
        delete params.keywords;
      }
      teamBrokerageRecordListApi(params)
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
