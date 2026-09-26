<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计概览：本页口径（后端无合计接口，不伪造"筛选结果"数字）。
           收入合计是唯一需要一眼抓到的数，红色加粗；笔数/用户数中性弱化 -->
      <div class="summary-bar stat-bar">
        <div class="stat-item">
          <span class="stat-label">本页收入合计</span>
          <span class="stat-value stat-value--in">¥ {{ money(statIncome) }}</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-label">本页记录</span>
          <span class="stat-value">{{ pageCount }} 笔</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-label">本页用户</span>
          <span class="stat-value">{{ statUsers }} 人</span>
        </div>
      </div>

      <!-- 筛选面板：单行排布，标签不再折行；查询/重置下沉 .filter-actions 靠右 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="变动类型">
            <el-select v-model="tableFrom.type" clearable placeholder="全部" class="selWidthd" @change="seachList">
              <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户搜索">
            <UserSearchInput ref="userSearchInput" v-model="tableFrom" @searchList="seachList" />
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="seachList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 列结构对照改版设计稿（后台改版-佣金记录.png）：
           用户信息置首 + 金额右对齐加粗 + 类型彩色标签 + 关联单号独立列 + 说明列 + 时间定宽 -->
      <el-table
        v-loading="listLoading"
        :data="tableData.data"
        class="admin-table table-lg"
        style="width: 100%"
        size="small"
        stripe
        highlight-current-row
      >
        <!-- 用户信息：头像 + 昵称 + ID 色块 chip（分销商管理定稿范式，列宽 200 固定，置首列） -->
        <el-table-column label="用户信息" width="200">
          <template slot-scope="scope">
            <div class="person-cell">
              <span class="person-avatar person-avatar--empty">{{ (scope.row.userName || '?').slice(0, 1) }}</span>
              <div class="person-info">
                <div class="info-name info-ellipsis">{{ scope.row.userName || '—' }}</div>
                <div class="info-line">
                  ID：<span class="id-chip">{{ scope.row.uid }}</span>
                  <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.uid)"></i>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <!-- 变动金额：右对齐 + 等宽数字 + 加粗，收入红 / 支出绿（国内财务习惯）
             列宽 110 固定，表头与设计稿一致为「变动金额」 -->
        <el-table-column label="变动金额" width="110" align="right" header-align="right">
          <template slot-scope="scope">
            <span class="amount" :class="scope.row.type == 1 ? 'amount--in' : 'amount--out'">
              {{ scope.row.type == 1 ? '+' : '-' }}{{ money(scope.row.price) }}
            </span>
          </template>
        </el-table-column>
        <!-- 变动类型：按类型关键词着色的 chip，分类一眼分辨（列宽 130 固定） -->
        <el-table-column label="变动类型" width="130">
          <template slot-scope="scope">
            <span class="type-tag" :class="tagClass(scope.row.title)">{{ scope.row.title || '推广返佣' }}</span>
          </template>
        </el-table-column>
        <!-- 关联单号：订单类记录展示单号（超长截断）+ 复制；提现/转余额类无单号显示 —（列宽 180 固定） -->
        <el-table-column label="关联单号" width="180">
          <template slot-scope="scope">
            <template v-if="scope.row.linkType === 'order' && scope.row.linkId">
              <span class="order-no">{{ shortNo(scope.row.linkId) }}</span>
              <i class="el-icon-document-copy copy-btn" title="复制单号" @click="copyText(scope.row.linkId)"></i>
            </template>
            <span v-else class="hq">—</span>
          </template>
        </el-table-column>
        <!-- 变动说明：唯一弹性列，单行截断 + 悬停完整内容 -->
        <el-table-column label="变动说明" min-width="220" prop="mark" show-overflow-tooltip />
        <!-- 时间：定宽去秒（完整时间戳 186px 太占宽，秒对台账无决策价值） -->
        <el-table-column label="时间" width="170" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.updateTime || scope.row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          background
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { brokerageListApi } from '@/api/financial';
export default {
  name: 'AccountsCapital',
  data() {
    return {
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      tableFrom: {
        type: '',
        content: '',
        searchType: 'all',
        page: 1,
        limit: 20,
      },
      // 与后端 UserBrokerageRecordMapper.xml 的 type 分支一一对应（勿改值）
      typeOptions: [
        { value: 1, label: '订单返佣' },
        { value: 2, label: '申请提现' },
        { value: 3, label: '提现失败' },
        { value: 4, label: '提现成功' },
        { value: 5, label: '佣金转余额' },
      ],
    };
  },
  computed: {
    // 统计口径 = 当前页数据（后端无列表合计接口，不跨页假合计）
    statIncome() {
      return (this.tableData.data || []).reduce((s, r) => (r.type == 1 ? s + Number(r.price || 0) : s), 0);
    },
    pageCount() {
      return (this.tableData.data || []).length;
    },
    statUsers() {
      return new Set((this.tableData.data || []).map((r) => r.uid)).size;
    },
  },
  mounted() {
    this.getList();
  },
  methods: {
    // 列表
    getList(num) {
      this.listLoading = true;
      this.tableFrom.page = num ? num : this.tableFrom.page;
      brokerageListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.listLoading = false;
        })
        .catch((res) => {
          this.listLoading = false;
        });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.getList();
    },
    //重置
    handleReset() {
      this.tableFrom.content = '';
      this.tableFrom.searchType = 'all';
      this.tableFrom.type = '';
      this.$refs.userSearchInput && this.$refs.userSearchInput.clearInput();
      this.getList();
    },
    // 搜索
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    // 时间截到「分」
    fmtTime(t) {
      return t ? String(t).slice(0, 16) : '—';
    },
    // 金额千分位 + 两位小数
    money(v) {
      const n = Number(v);
      if (v === null || v === undefined || v === '' || isNaN(n)) return '0.00';
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    // 类型标签配色：按 title 关键词映射（极差=蓝 / 平级=橙 / 代理=绿 / 提现·转余额=灰 / 其余=蓝）
    tagClass(title) {
      const t = String(title || '');
      if (t.indexOf('平级') > -1) return 'type-tag--warn';
      if (t.indexOf('代理') > -1 || t.indexOf('区域') > -1) return 'type-tag--success';
      if (t.indexOf('提现') > -1 || t.indexOf('余额') > -1) return 'type-tag--info';
      return 'type-tag--primary';
    },
    // 超长单号中间截断：前 8 + … + 后 4，完整值点复制图标获取
    shortNo(id) {
      const s = String(id);
      return s.length > 14 ? s.slice(0, 8) + '…' + s.slice(-4) : s;
    },
    // 复制文本到剪贴板：与用户列表页同款实现（中文提示 + execCommand 兜底）
    copyText(text) {
      const value = String(text);
      const done = () => this.$message.success('已复制：' + value);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(done).catch(() => this.fallbackCopy(value, done));
      } else {
        this.fallbackCopy(value, done);
      }
    },
    fallbackCopy(value, done) {
      const input = document.createElement('textarea');
      input.value = value;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.top = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
  },
};
</script>

<style scoped lang="scss">
/* 筛选下拉 150px（对照参数文档） */
.selWidthd {
  width: 150px;
}

/* ============ 统计概览（对照参数文档）：纵向排布 ============
   标签 12px 在上 / 数值 22px Bold 在下，纵向间距 6px；
   分隔竖线 1×38，前后间距 24px */
.stat-bar {
  display: flex;
  align-items: center;
}
.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-right: 24px;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 26px;
  color: #303133;
  font-variant-numeric: tabular-nums;
}
.stat-value--in {
  color: #f56c6c;
}
.stat-divider {
  width: 1px;
  height: 38px;
  background: #ebeef5;
  margin-right: 24px;
}

/* ============ 表格（对照参数文档） ============
   列间距 12px（列与列）、首/末列边缘 16px；
   表头行高 46px、数据行高 56px；
   说明列之外全部固定宽，由模板列属性锁定 */
::v-deep .admin-table {
  /* 全局 .admin-table .cell 用了 8px !important，此处必须同级 !important 才能覆盖 */
  th.el-table__cell .cell,
  td.el-table__cell .cell {
    padding-left: 12px !important;
    padding-right: 12px !important;
  }
  th.el-table__cell:first-child .cell,
  td.el-table__cell:first-child .cell {
    padding-left: 16px !important;
  }
  th.el-table__cell:last-child .cell,
  td.el-table__cell:last-child .cell {
    padding-right: 16px !important;
  }
  /* 表头 46px（15px 字 + 上下 12px） */
  th.el-table__cell {
    padding-top: 12px;
    padding-bottom: 12px;
  }
  /* 数据行 56px（14px 字 / 22px 行高 + 上下 17px） */
  td.el-table__cell {
    padding-top: 17px;
    padding-bottom: 17px;
  }
}

/* 用户列：头像 28×28（参数文档），覆盖全局 36px；与文字间距 8px */
::v-deep .person-avatar {
  width: 28px;
  height: 28px;
  margin-right: 8px;
  font-size: 12px;
}
.info-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 金额：右对齐由列 align 提供，这里管字重与语义色 */
.amount {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.amount--in {
  color: #f56c6c;
}
.amount--out {
  color: #67c23a;
}

/* 类型标签：Element 语义色浅底 chip，与 .lv-chip 同款尺寸 */
.type-tag {
  display: inline-block;
  padding: 0 8px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}
.type-tag--primary {
  background: #ecf5ff;
  color: #409eff;
}
.type-tag--warn {
  background: #fdf6ec;
  color: #e6a23c;
}
.type-tag--success {
  background: #f0f9eb;
  color: #67c23a;
}
.type-tag--info {
  background: #f4f4f5;
  color: #909399;
}

/* 关联单号：链接蓝 + 等宽数字 */
.order-no {
  color: #409eff;
  font-variant-numeric: tabular-nums;
}
</style>
