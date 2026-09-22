<template>
  <div class="financial-flow">
    <!-- 顶部面包屑 + 操作 -->
    <div class="page-head">
      <div class="page-head__crumbs">
        <span class="page-head__crumb">首页</span>
        <span class="page-head__sep">-</span>
        <span class="page-head__crumb page-head__crumb--current">资金流水</span>
      </div>
      <div class="page-head__actions">
        <el-button
          type="primary"
          size="small"
          icon="el-icon-download"
          @click="handleExportAll"
        >
          导出全部数据
        </el-button>
      </div>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card" :bordered="false" shadow="never">
      <div class="filter-head">
        <span class="filter-head__title">条件筛选</span>
      </div>
      <el-form :model="tableFrom" size="small" label-width="90px" class="filter-form">
        <!-- 第一组：时间 + 用户 -->
        <div class="filter-group">
          <div class="filter-grid">
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="timeVal"
                type="datetimerange"
                value-format="yyyy-MM-dd HH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
                @change="onchangeTime"
              />
            </el-form-item>
            <el-form-item label="用户搜索">
              <UserSearchInput ref="userSearchInput" v-model="tableFrom" />
            </el-form-item>
            <el-form-item label="关联单号">
              <el-input
                v-model="tableFrom.linkId"
                size="small"
                clearable
                placeholder="订单号 / 换货单号"
                @keyup.enter.native="getList(1)"
              />
            </el-form-item>
            <el-form-item label-width="0">
              <el-button type="primary" size="small" icon="el-icon-search" @click="getList(1)">搜索</el-button>
              <el-button size="small" icon="el-icon-refresh-left" @click="handleReset">重置</el-button>
            </el-form-item>
          </div>
        </div>
        <!-- 第二组：账户类型 tab + 项目类型下拉 -->
        <div class="filter-group">
          <el-tabs v-model="tableFrom.category" class="filter-tabs" @tab-click="onChangeCategory">
            <el-tab-pane label="不限" name="all" />
            <el-tab-pane label="佣金" name="brokerage_price" />
            <el-tab-pane label="积分" name="integral" />
            <el-tab-pane label="余额" name="now_money" />
          </el-tabs>
          <div class="filter-grid filter-grid--single">
            <el-form-item label="项目类型">
              <el-select
                v-model="tableFrom.title"
                size="small"
                clearable
                placeholder="全部"
                class="full-width"
                @change="onChangeTitle"
              >
                <el-option v-for="item in titleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="list-card" :bordered="false" shadow="never">
      <div class="list-table" :style="tableStyle">
        <div class="list-head">
          <div class="list-head__cell">用户信息</div>
          <div class="list-head__cell">订单</div>
          <div class="list-head__cell">资金</div>
          <div class="list-head__cell">来源</div>
          <div class="list-head__cell">资金类型</div>
        </div>
        <div class="list-body" v-loading="listLoading">
          <div v-if="!listLoading && tableData.data.length === 0" class="list-empty">
            <i class="el-icon-document"></i>
            <p>暂无资金流水记录</p>
          </div>
          <div
            v-for="(row, idx) in tableData.data"
            :key="idx"
            class="list-row"
          >
            <!-- 用户信息 -->
            <div class="list-cell list-cell--user">
              <div class="user-info">
                <div class="user-info__avatar">
                  <el-avatar :src="row.avatar || defaultAvatar" :size="32">{{ initialChar(row.nickName) }}</el-avatar>
                </div>
                <div class="user-info__detail">
                  <div class="user-info__name">
                    <span class="kv__v">{{ row.nickName || '-' }}</span>
                    <span class="kv__uid">ID:{{ row.uid }}</span>
                  </div>
                  <div class="kv">
                    <span class="kv__k">会员账号：</span>
                    <span class="kv__v kv__v--num">{{ row.phone || '—' }}</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 订单 -->
            <div class="list-cell">
              <div class="kv">
                <span class="kv__k">单号：</span>
                <span class="kv__v kv__v--num">{{ row.linkId && row.linkId !== '0' ? row.linkId : '—' }}</span>
                <span v-if="row.linkId && row.linkId !== '0'" class="kv__copy" @click="copyText(row.linkId)">
                  <i class="el-icon-document-copy"></i>
                </span>
              </div>
              <div class="kv">
                <span class="kv__k">时间：</span>
                <span class="kv__v kv__v--num">{{ row.createTime || '—' }}</span>
              </div>
            </div>
            <!-- 资金 -->
            <div class="list-cell">
              <div class="kv">
                <span class="kv__k">资金增减：</span>
                <span :class="['kv__v', 'kv__v--num', row.pm == 1 ? 'money-up' : 'money-down']">
                  {{ row.pm == 1 ? '+' : '-' }}{{ formatNumber(row.number) }}
                </span>
              </div>
              <div class="kv">
                <span class="kv__k">剩余资金：</span>
                <span class="kv__v kv__v--num">{{ formatNumber(row.balance) }}</span>
              </div>
            </div>
            <!-- 来源 -->
            <div class="list-cell">
              <div class="kv">
                <span class="kv__k">来源业务：</span>
                <span class="kv__v">{{ row.title || '—' }}</span>
              </div>
              <div class="kv">
                <span class="kv__k">备注：</span>
                <span class="kv__v kv__v--wrap">{{ row.mark || '—' }}</span>
              </div>
            </div>
            <!-- 资金类型 -->
            <div class="list-cell list-cell--last">
              <div class="status-tag" :class="categoryTagClass(row.category)">
                {{ categoryLabel(row.category) }}
              </div>
              <div class="status-tag status-tag--info type-tag">
                {{ row.title || '—' }}
              </div>
              <div class="kv" style="margin-top:6px">
                <span class="status-tag" :class="row.pm == 1 ? 'status-tag--success' : 'status-tag--danger'">
                  {{ row.pm == 1 ? '增加' : '扣减' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 分页 -->
      <div class="list-pager">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          background
          @size-change="handleSizeChange"
          @current-change="pageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { monitorListApi } from '@/api/financial';
import UserSearchInput from '@/components/base/UserSearchInput';

export default {
  name: 'FinancialFlow',
  components: { UserSearchInput },
  data() {
    return {
      defaultAvatar: '',
      timeVal: [],
      listLoading: false,
      tableFrom: {
        category: 'all',
        title: '',
        linkId: '',
        dateLimit: '',
        content: '',
        searchType: 'all',
        page: 1,
        limit: 20,
      },
      tableData: {
        data: [],
        total: 0,
      },
      // 项目类型下拉：根据当前账户类型 tab 动态切换
      titleOptionsByCategory: {
        all: [
          { value: 'recharge', label: '余额充值' },
          { value: 'payProduct', label: '购买商品' },
          { value: 'productRefund', label: '商品退款' },
          { value: 'admin', label: '后台操作' },
          { value: 'transferIn', label: '佣金转入' },
          { value: 'exchange', label: '换货差价' },
          { value: 'stock', label: '订货奖金' },
          { value: 'order', label: '订单佣金' },
          { value: 'withdraw', label: '佣金提现' },
          { value: 'yue', label: '佣金转余额' },
          { value: 'sign', label: '签到奖励' },
          { value: 'reward', label: '活动奖励' },
          { value: 'deduct', label: '消费抵扣' },
        ],
        now_money: [
          { value: 'recharge', label: '余额充值' },
          { value: 'payProduct', label: '购买商品' },
          { value: 'productRefund', label: '商品退款' },
          { value: 'admin', label: '后台操作' },
          { value: 'transferIn', label: '佣金转入' },
          { value: 'exchange', label: '换货差价' },
        ],
        brokerage_price: [
          { value: 'stock', label: '订货奖金' },
          { value: 'order', label: '订单佣金' },
          { value: 'withdraw', label: '佣金提现' },
          { value: 'yue', label: '佣金转余额' },
          { value: 'admin', label: '后台操作' },
        ],
        integral: [
          { value: 'admin', label: '后台操作' },
          { value: 'sign', label: '签到奖励' },
          { value: 'order', label: '下单赠送' },
          { value: 'reward', label: '活动奖励' },
          { value: 'deduct', label: '消费抵扣' },
        ],
      },
    };
  },
  computed: {
    titleOptions() {
      return this.titleOptionsByCategory[this.tableFrom.category] || this.titleOptionsByCategory.all;
    },
    tableStyle() {
      // 5 列：用户信息 / 订单 / 资金 / 来源 / 资金类型
      return { '--list-cols': 'minmax(220px, 1.2fr) minmax(220px, 1.1fr) minmax(200px, 1fr) minmax(220px, 1.2fr) minmax(160px, 0.8fr)' };
    },
  },
  mounted() {
    this.getList();
  },
  methods: {
    // 切换账户类型 tab：清掉项目类型，避免枚举不匹配
    onChangeCategory() {
      this.tableFrom.title = '';
      this.getList(1);
    },
    onChangeTitle() {
      this.getList(1);
    },
    // 重置
    handleReset() {
      this.tableFrom.title = '';
      this.tableFrom.linkId = '';
      this.tableFrom.dateLimit = '';
      this.tableFrom.content = '';
      this.tableFrom.searchType = 'all';
      this.tableFrom.category = 'all';
      this.timeVal = [];
      this.getList(1);
    },
    onchangeTime(e) {
      this.timeVal = e || [];
      this.tableFrom.dateLimit = e && e.length === 2 ? `${e[0]},${e[1]}` : '';
      this.getList(1);
    },
    // 拉列表
    getList(num) {
      this.listLoading = true;
      this.tableFrom.page = num || this.tableFrom.page;
      // 'all' 不传 category 给后端
      const params = { ...this.tableFrom };
      if (params.category === 'all') delete params.category;
      monitorListApi(params)
        .then((res) => {
          this.tableData.data = res.list || [];
          this.tableData.total = res.total || 0;
          this.listLoading = false;
        })
        .catch((err) => {
          this.$message.error(err && err.message ? err.message : '加载失败');
          this.listLoading = false;
        });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.getList(1);
    },
    handleExportAll() {
      this.$message.info('导出全部数据待后端提供导出接口');
    },
    copyText(text) {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => this.$message.success('已复制'));
      } else {
        const input = document.createElement('input');
        input.value = text;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        this.$message.success('已复制');
      }
    },
    formatNumber(n) {
      if (n === null || n === undefined) return '0';
      const num = Number(n);
      if (Number.isNaN(num)) return n;
      // 整数显示整数；2 位小数显示小数
      if (Math.abs(num - Math.round(num)) < 1e-6) return String(Math.round(num));
      return num.toFixed(2);
    },
    initialChar(name) {
      if (!name) return '?';
      return name.slice(0, 1).toUpperCase();
    },
    categoryLabel(c) {
      return { now_money: '余额', integral: '积分', brokerage_price: '佣金' }[c] || '其他';
    },
    categoryTagClass(c) {
      return {
        now_money: 'status-tag--primary',
        integral: 'status-tag--warning',
        brokerage_price: 'status-tag--success',
      }[c] || 'status-tag--info';
    },
  },
};
</script>

<style lang="scss" scoped>
.financial-flow {
  padding: 14px;
}

/* 顶部面包屑 + 操作按钮 */
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 0 4px;

  &__crumbs {
    font-size: 14px;
    color: #909399;
  }

  &__crumb {
    &--current {
      color: #303133;
      font-weight: 600;
    }
  }

  &__sep {
    margin: 0 8px;
    color: #c0c4cc;
  }
}

/* 筛选卡：贴近基座风格 */
.filter-card {
  margin-bottom: 14px;
}

.filter-tabs {
  margin: 0 0 12px;

  ::v-deep .el-tabs__header {
    margin-bottom: 0;
  }

  ::v-deep .el-tabs__item {
    height: 36px;
    line-height: 36px;
    font-size: 14px;
  }
}

.filter-grid--single {
  grid-template-columns: 1fr;
}

.full-width {
  width: 100%;
}

/* 列表 */
.list-card {
  .list-table {
    /* 在 script 里通过 --list-cols 注入列宽 */
  }
}

.list-pager {
  margin-top: 14px;
  text-align: right;
}

/* 用户信息列 */
.list-cell--user {
  .user-info {
    display: flex;
    align-items: flex-start;
    gap: 12px;

    &__avatar {
      flex-shrink: 0;
    }

    &__detail {
      min-width: 0;
      flex: 1;
    }

    &__name {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
    }
  }
}

/* 金额增/减色（中国惯例：增加=红，减少=绿） */
.money-up {
  color: #e04c4c;
  font-weight: 600;
}

.money-down {
  color: #52a832;
  font-weight: 600;
}

/* 资金类型列的明细标签：独立一行，与账户标签区分 */
.type-tag {
  display: inline-block;
  margin-top: 6px;
}

/* 长备注允许换行，避免撑破单元格 */
.kv__v--wrap {
  white-space: normal;
  word-break: break-all;
  line-height: 20px;
}
</style>