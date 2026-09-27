<template>
  <div class="financial-flow">
    <!-- 顶部：页标题 + 操作 -->
    <div class="page-head">
      <div class="page-head__title">资金流水</div>
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

    <!-- 筛选区：第一行=高频条件（账户类型/项目类型）+ 操作按钮；第二行=等宽栅格 -->
    <el-card class="filter-card" :bordered="false" shadow="never" :body-style="{ padding: 0 }">
      <el-form :model="tableFrom" size="small" label-position="top" class="filter-form">
        <!-- 第一行：高频维度 + 操作同排，动线不中断 -->
        <div class="filter-top">
          <div class="filter-top__main">
            <span class="filter-top__label">账户类型</span>
            <el-radio-group
              v-model="tableFrom.category"
              size="small"
              class="filter-segment"
              @change="onChangeCategory"
            >
              <el-radio-button label="all">不限</el-radio-button>
              <el-radio-button label="brokerage_price">佣金</el-radio-button>
              <el-radio-button label="integral">积分</el-radio-button>
              <el-radio-button label="now_money">余额</el-radio-button>
            </el-radio-group>
          </div>

          <div class="filter-top__side">
            <!-- 项目类型紧随账户类型：先选账户，再选该账户下的项目 -->
            <el-form-item label="项目类型" class="filter-type-item">
              <el-select
                v-model="tableFrom.title"
                size="small"
                clearable
                placeholder="全部"
                @change="onChangeTitle"
              >
                <el-option v-for="item in titleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <div class="filter-actions">
              <el-button type="primary" size="small" icon="el-icon-search" @click="getList(1)">搜索</el-button>
              <el-button size="small" icon="el-icon-refresh-left" @click="handleReset">重置</el-button>
            </div>
          </div>
        </div>

        <!-- 第二行：低频条件等宽三列，标签统一在控件上方左对齐 -->
        <div class="filter-group filter-group--grid">
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
            <el-form-item label="订单号">
              <el-input
                v-model="tableFrom.linkId"
                size="small"
                clearable
                placeholder="订单号 / 换货单号"
                @keyup.enter.native="getList(1)"
              />
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
                <span class="kv__k">订单号：</span>
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
          { value: 'orderDistribution', label: '分销佣金' },
          { value: 'orderRegion', label: '区域代理佣金' },
          { value: 'orderTeamGap', label: '团队级差奖' },
          { value: 'orderTeamPeer', label: '团队平级奖' },
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
          { value: 'orderDistribution', label: '分销佣金' },
          { value: 'orderRegion', label: '区域代理佣金' },
          { value: 'orderTeamGap', label: '团队级差奖' },
          { value: 'orderTeamPeer', label: '团队平级奖' },
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

/* 顶部：页标题 + 操作按钮 */
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 0 4px;

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    line-height: 24px;
  }
}

/* 筛选卡：贴近基座风格（body padding 已置 0，各层自带内边距） */
.filter-card {
  margin-bottom: 14px;
}

/* 标签与输入框间距：本页 12px。
   ⚠️ 必须 !important：theme/element.scss 433 行有
   `.el-form-item__label { padding: 0 6px 0 0 !important }`，
   不带 !important 的覆盖会被它压掉（2026-09-27 实测踩坑） */
.filter-form {
  ::v-deep .el-form-item__label {
    padding-bottom: 12px !important;
  }
}

/* 网格改 3 列：筛选项只剩 3 个（创建时间/用户搜索/订单号），
   继续用基线的 4 列会在右侧空出一格，项与项之间被拉得很远 */
.filter-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));

  @media (max-width: 1100px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  /* UserSearchInput 内部输入框带全局 .selWidth（styles.scss 272 行，width:260px !important），
     会把这一列锁死 260px、不随栅格拉伸，右侧留出缺口。
     全站几十处在用 selWidth 不能动，只在本页覆盖为撑满列宽（特异性 2 类 > 全局 1 类） */
  ::v-deep .selWidth {
    width: 100% !important;
  }
}

/* 第一行：高频条件（账户类型 / 项目类型）与操作按钮同排 */
.filter-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f2f5;

  @media (max-width: 980px) {
    flex-wrap: wrap;
    gap: 12px;

    &__side {
      width: 100%;
      justify-content: flex-end;
    }
  }

  &__main {
    display: flex;
    align-items: center;
    min-width: 0;
  }

  /* 行内标签：默认 14px（未进 .filter-grid，不吃基座 13px 灰字规则） */
  &__label {
    flex-shrink: 0;
    padding-right: 12px;
    color: #606266;
    line-height: 32px;
  }

  &__side {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

/* 账户类型：分段控件。原 el-tabs 自带下划线且占位整行，
   与同排的下拉、按钮不是同一种控件语汇，视觉权重也过重 */
.filter-segment {
  ::v-deep .el-radio-button__inner {
    min-width: 76px;
    text-align: center;
  }
}

/* 项目类型：与账户类型同排，标签拉回控件左侧。
   外层 el-form 是 label-position="top"，所以用 flex 把 label 与 content 拉回同一行 */
.filter-type-item {
  display: flex;
  align-items: center;
  /* element.scss 424 行 .el-form-item { margin-bottom: 20px !important }，压它需要同等 !important */
  margin-bottom: 0 !important;

  ::v-deep .el-form-item__label {
    flex-shrink: 0;
    padding: 0 8px 0 0 !important;
    line-height: 32px;
  }

  ::v-deep .el-form-item__content {
    line-height: 32px;
  }

  ::v-deep .el-select {
    width: 220px;
  }
}

/* 操作按钮：收进第一行右端，去掉基座的独立分隔线与外边距。
   ⚠️ 基座 .filter-actions 自带 border-top + padding 16px 0，scoped 选择器特异性更高，可直接覆盖 */
.filter-actions {
  padding: 0;
  border-top: none;
}

/* 第二行栅格：与第一行只用留白分隔，不再叠第二条线 */
.filter-group--grid {
  margin-top: 14px;
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

/* 键值行：标签紧贴值。
   ⚠️ theme/styles.scss 1238 行有全局 .kv { gap: 8px }（那是给 kv-k/kv-v 旧命名准备的），
   会误伤本页的 kv__k/kv__v，把标签和值撑开 8px+2px=10px —— 本页归零，
   只留 list-page.scss 里 kv__k 自带的 2px 右边距 */
.kv {
  gap: 0;
}

/* 长备注允许换行，避免撑破单元格 */
.kv__v--wrap {
  white-space: normal;
  word-break: break-all;
  line-height: 20px;
}
</style>