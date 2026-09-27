<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条 -->
      <div class="summary-bar">
        累计开团 <b>{{ total }}</b> 期
        <template v-if="headerCount">
          ，进行中 <b class="c-open">{{ headerCount.open }}</b> 期，已成团 <b class="c-finish">{{ headerCount.finish }}</b> 期
        </template>
      </div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="活动">
            <el-select v-model="tableFrom.activityId" placeholder="全部活动" clearable filterable style="width: 220px">
              <el-option v-for="a in activityOptions" :key="a.id" :label="a.name" :value="a.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="tab-bar">
          <span v-for="t in tabs" :key="t.value" class="tab-item" :class="{ on: tableFrom.status === t.value }" @click="onTab(t.value)">{{ t.label }}</span>
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
        </div>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="ID" width="70">
          <template slot-scope="scope">{{ scope.row.id }}</template>
        </el-table-column>
        <el-table-column label="拼团活动信息" min-width="220">
          <template slot-scope="scope">
            <div class="goods-cell">
              <img v-if="scope.row.image" :src="scope.row.image" class="goods-img" />
              <div>
                <div class="info-name">{{ scope.row.activityName }}</div>
                <div class="info-line">{{ scope.row.productName }}</div>
                <div class="info-line">成团人数：{{ scope.row.totalNum }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="期数" width="90" align="center">
          <template slot-scope="scope">第 {{ scope.row.teamNo }} 期</template>
        </el-table-column>
        <el-table-column label="开团时间" width="150" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.openTime) }}</template>
        </el-table-column>
        <el-table-column label="已参团成员" min-width="200">
          <template slot-scope="scope">
            <div class="avatar-row">
              <template v-if="scope.row.joins && scope.row.joins.length">
                <el-tooltip v-for="j in scope.row.joins.slice(0, 10)" :key="j.id" :content="joinTip(j)" placement="top">
                  <span class="avatar-item" :class="{ win: j.isWin === 1 }">
                    <img v-if="j.avatar" :src="j.avatar" />
                    <i v-else class="el-icon-user" />
                  </span>
                </el-tooltip>
                <span v-if="scope.row.joins.length > 10" class="avatar-more">+{{ scope.row.joins.length - 10 }}</span>
              </template>
              <span v-else class="info-line">暂无成员</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="剩余名额" width="90" align="center">
          <template slot-scope="scope">{{ scope.row.remainNum }}</template>
        </el-table-column>
        <el-table-column label="开奖时间" width="150" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.winTime) }}</template>
        </el-table-column>
        <el-table-column label="拼团状态" width="100" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 1" class="st-dot st-dot-lg on"><i></i>已成团</span>
            <span v-else class="st-dot st-dot-lg warn"><i></i>进行中</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid">
              <el-button size="mini" plain class="op-tag tint-primary" @click="openDetail(scope.row)">查看详情</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="150" />
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 团详情弹窗：成员与中奖情况 -->
    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="720px" top="6vh">
      <div class="detail-head">
        <div class="info-line">参团 {{ detailTeam.totalNum }} 人，中奖 {{ detailTeam.winNum }} 人，未中奖者本金退回余额账户并获得约定补贴佣金</div>
      </div>
      <el-table :data="detailJoins" size="small" max-height="420" v-loading="detailLoading">
        <el-table-column label="成员" min-width="180">
          <template slot-scope="scope">
            <div class="goods-cell">
              <span class="avatar-item">
                <img v-if="scope.row.avatar" :src="scope.row.avatar" />
                <i v-else class="el-icon-user" />
              </span>
              <div>
                <div class="info-name">{{ scope.row.nickname || '未找到用户' }}</div>
                <div class="info-line">ID：{{ scope.row.uid }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参与金额" width="100" align="center">
          <template slot-scope="scope">￥{{ scope.row.payPrice }}</template>
        </el-table-column>
        <el-table-column label="参与时间" width="150" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="结果" width="200">
          <template slot-scope="scope">
            <span v-if="scope.row.isWin === 1" class="st-dot st-dot-lg on"><i></i>中奖（得商品，正常发货）</span>
            <span v-else class="st-dot st-dot-lg fail"><i></i>未中奖（退款 + 补贴）</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pingouTeamListApi, pingouTeamDetailApi, pingouActivityListApi } from '@/api/pingou';

export default {
  name: 'PingouTeam',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      headerCount: null,
      activityOptions: [],
      tabs: [
        { label: '全部', value: null },
        { label: '进行中', value: 0 },
        { label: '已成团', value: 1 }
      ],
      tableFrom: { page: 1, limit: 20, activityId: null, status: null },
      // 详情
      detailVisible: false,
      detailLoading: false,
      detailTitle: '',
      detailTeam: {},
      detailJoins: []
    };
  },
  mounted() {
    this.loadActivities();
    this.getList();
  },
  methods: {
    loadActivities() {
      pingouActivityListApi({ page: 1, limit: 100 }).then((res) => {
        this.activityOptions = res.list || [];
      });
    },
    getList() {
      this.loading = true;
      pingouTeamListApi(this.tableFrom)
        .then((res) => {
          this.tableData = res.list || [];
          this.total = res.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    onTab(v) {
      this.tableFrom.status = v;
      this.tableFrom.page = 1;
      this.getList();
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, activityId: null, status: null };
      this.getList();
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    fmtTime(t) {
      return t ? String(t).slice(0, 16) : '-';
    },
    joinTip(j) {
      return (j.nickname || '用户') + (j.isWin === 1 ? '（中奖）' : '（未中奖）');
    },
    openDetail(row) {
      this.detailVisible = true;
      this.detailLoading = true;
      this.detailTitle = '拼购团详情 - 第 ' + row.teamNo + ' 期';
      this.detailTeam = row;
      this.detailJoins = [];
      pingouTeamDetailApi(row.id)
        .then((res) => {
          const team = (res || [])[0] || {};
          this.detailTeam = team;
          this.detailJoins = team.joins || [];
        })
        .finally(() => {
          this.detailLoading = false;
        });
    }
  }
};
</script>

<style scoped lang="scss">
.summary-bar {
  padding: 4px 0 12px;
  font-size: 14px;
  color: #606266;
  b {
    color: #1f5fd6;
    &.c-open {
      color: #e6a23c;
    }
    &.c-finish {
      color: #67c23a;
    }
  }
}
.tab-bar {
  display: inline-block;
  margin-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
  .tab-item {
    display: inline-block;
    padding: 8px 18px;
    font-size: 14px;
    color: #606266;
    cursor: pointer;
    &.on {
      color: #1f5fd6;
      border-bottom: 2px solid #1f5fd6;
      margin-bottom: -2px;
      font-weight: 500;
    }
  }
}
.goods-cell {
  display: flex;
  align-items: center;
  .goods-img {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    margin-right: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }
}
.avatar-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.avatar-item {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
  border: 2px solid transparent;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  &.win {
    border-color: #e93323;
    position: relative;
    &::after {
      content: '中';
      position: absolute;
      right: -4px;
      bottom: -4px;
      font-size: 10px;
      line-height: 1;
      background: #e93323;
      color: #fff;
      border-radius: 50%;
      padding: 2px;
    }
  }
}
.avatar-more {
  font-size: 12px;
  color: #909399;
}
.info-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.info-line {
  font-size: 13px;
  color: #909399;
  line-height: 20px;
}
.detail-head {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
