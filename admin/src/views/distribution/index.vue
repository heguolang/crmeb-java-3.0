<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条：与订货商管理/代理管理一致，先给总量再给明细 -->
      <div class="summary-bar">目前有 <b>{{ tableData.total }}</b> 名分销商。</div>

      <!-- 筛选面板：浅灰底块，与白卡片分层；查询/重置统一下沉到 .filter-actions 居中 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="成为时间">
            <optionDatePicker v-model="timeVal" @changeOptTime="onchangeTime"></optionDatePicker>
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

      <!-- 列宽按 Edge 实测内容宽度定（详见技能 crmeb-admin-ui-baseline）：
           原来 14 个平铺列在窄窗口（1100 视口容器仅 826px）必然横向溢出，
           而横向溢出正是 fixed="right" 操作列错位的真凶——固定层内层表取整表宽度。
           这里把「推广/佣金」两组相关数值收进列内多行小台账，14 列 → 6 列，一次解决。 -->
      <el-table class="admin-table table-lg" v-loading="listLoading" :data="tableData.data" size="small" stripe highlight-current-row>
        <!-- 头像：44px 头像 + 单元格左右内边距，60 是不裁切的最小宽度 -->
        <el-table-column label="头像" width="60" align="center">
          <template slot-scope="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" class="avatar-img" />
            <span v-else class="avatar-text">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
          </template>
        </el-table-column>
        <!-- 分销商信息：昵称 / ID+手机 / 上级 / 成为时间 四行，弹性列吸收剩余空间。
             ID 与手机同行省一行高度（两者都是"识别身份"的同类信息）；
             上级是「清除上级」操作的判断依据，必须与昵称同格可见，不能折叠进弹窗。
             min-width 160 = 最长行「成为：2026-09-24 19:00」实测 140px（13px 字号）+ 左右内边距 16px。 -->
        <el-table-column label="分销商信息" min-width="160">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname || '—' }}</div>
            <div class="info-line">ID：{{ scope.row.uid }}<span v-if="scope.row.phone"> · {{ scope.row.phone }}</span></div>
            <div class="info-line">
              上级：<span :class="{ hq: !hasParent(scope.row) }">{{ hasParent(scope.row) ? scope.row.spreadNickname : '无' }}</span>
            </div>
            <div class="info-line">成为：{{ fmtTime(scope.row.promoterTime) }}</div>
          </template>
        </el-table-column>
        <!-- 分销等级：着色 chip，与代理管理的「级别」列同款 -->
        <el-table-column label="分销等级" min-width="84">
          <template slot-scope="scope">
            <span v-if="scope.row.distributorLevelId > 0" class="lv-chip">{{ scope.row.distributorLevelName }}</span>
            <span v-else class="hq">未分级</span>
          </template>
        </el-table-column>
        <!-- 推广业绩：三行小台账（人数 / 单数 / 金额），比横排三列省 2 个列宽；
             三个标签统一 4 字，正好填满 .kv-k 的 52px 定宽，数值左缘对齐 -->
        <el-table-column label="推广业绩" min-width="150">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">一级用户</span><span class="kv-v">{{ scope.row.spreadCount || 0 }} 人</span></div>
            <div class="kv"><span class="kv-k">推广订单</span><span class="kv-v">{{ scope.row.spreadOrderNum || 0 }} 单</span></div>
            <div class="kv"><span class="kv-k">订单金额</span><span class="kv-v">{{ money(scope.row.spreadOrderTotalPrice) }} 元</span></div>
          </template>
        </el-table-column>
        <!-- 佣金：四行小台账，标签左定宽 + 数值右对齐，纵向可直接比对；
             可提现是唯一需要"一眼看到"的金额，加粗着主色，其余用中性色弱化。
             140 是实测下限：「已提现 0.00 · 0次」这行最宽（标签 39 + 数值 29 + 后缀 32 + 内边距 16）。 -->
        <el-table-column label="佣金（元）" min-width="140">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">可提现</span><span class="kv-v kv-v--strong">{{ money(scope.row.brokeragePrice) }}</span></div>
            <div class="kv"><span class="kv-k">总额</span><span class="kv-v">{{ money(scope.row.totalBrokeragePrice) }}</span></div>
            <div class="kv">
              <span class="kv-k">已提现</span
              ><span class="kv-v">{{ money(scope.row.extractCountPrice) }}<i class="kv-sub">· {{ scope.row.extractCountNum || 0 }}次</i></span>
            </div>
            <div class="kv"><span class="kv-k">冻结</span><span class="kv-v">{{ money(scope.row.freezeBrokeragePrice) }}</span></div>
          </template>
        </el-table-column>
        <!-- 操作：单行等宽三格（与「代理管理」同款 op-grid--auto）。
             顺序按操作主次与危险度递增：只读查看前置，破坏性「清除上级」收尾——
             和代理管理把「删除」放最后一格同一个思路，减少误触。
             「清除上级」只在有上级推广人的行渲染，--auto 会让剩余按钮自动铺满，不留空洞。
             208px = 单元格左右内边距 30 + 3 格等宽按钮（含 6px 间隙），每格 ≈55px，
             「推广订单」4 字（13px）需 54px 刚好不截断，再窄就会挤字换行。 -->
        <el-table-column label="操作" width="208" fixed="right" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button v-if="checkPermi(['admin:retail:spread:list'])" size="mini" plain class="op-tag tint-neutral" @click="onSpread(scope.row.uid, 'man', '推广人')">推广人</el-button>
              <el-button v-if="checkPermi(['admin:retail:spread:order:list'])" size="mini" plain class="op-tag tint-neutral" @click="onSpreadOrder(scope.row.uid, 'order', '推广订单')">推广订单</el-button>
              <el-button v-if="hasParent(scope.row) && checkPermi(['admin:retail:spread:clean'])" size="mini" plain class="op-tag tint-danger" @click="clearSpread(scope.row)">清除上级</el-button>
            </div>
          </template>
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

    <!--推广人 / 推广订单 明细弹窗-->
    <el-dialog :title="titleName + '列表'" :visible.sync="dialogVisible" width="900px" :before-close="handleClose">
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item v-if="onName !== 'man'" label="时间">
            <el-date-picker
              v-model="timeValSpread"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              size="small"
              type="daterange"
              placement="bottom-end"
              placeholder="自定义时间"
              style="width: 260px"
              @change="onchangeTimeSpread"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          <el-form-item label="用户类型">
            <el-select v-model="spreadFrom.type" style="width: 140px" @change="onChanges" placeholder="请选择用户类型">
              <el-option label="全部" :value="0"></el-option>
              <el-option label="一级推广人" :value="1"></el-option>
              <el-option label="二级推广人" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
              v-model="spreadFrom.nickName"
              :placeholder="onName === 'order' ? '请输入订单号' : '请输入姓名、电话、UID'"
              style="width: 220px"
              size="small"
              clearable
              @keyup.enter.native="onChanges"
            >
            </el-input>
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="onChanges">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleResetDialog">重置</el-button>
        </div>
      </div>
      <el-table
        v-if="onName === 'man'"
        key="men"
        v-loading="spreadLoading"
        :data="spreadData.data"
        class="admin-table"
        style="width: 100%"
        size="small"
        stripe
        highlight-current-row
      >
        <el-table-column prop="uid" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="160">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname || '—' }}</div>
            <div class="info-line">{{ scope.row.phone || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="是否推广员" width="110">
          <template slot-scope="scope">
            <span>{{ scope.row.isPromoter | filterYesOrNo }}</span>
          </template>
        </el-table-column>
        <el-table-column sortable label="推广人数" min-width="110" prop="spreadCount" />
        <el-table-column sortable label="订单数" min-width="110" prop="payCount" />
      </el-table>
      <el-table
        v-if="onName === 'order'"
        key="order"
        v-loading="spreadLoading"
        :data="spreadData.data"
        class="admin-table"
        style="width: 100%"
        size="small"
        stripe
        highlight-current-row
      >
        <el-table-column prop="orderId" label="订单ID" min-width="130" />
        <el-table-column label="用户信息" min-width="170">
          <template slot-scope="scope">
            <span>{{ scope.row.realName }}</span>
            <el-divider direction="vertical"></el-divider>
            <span>{{ scope.row.userPhone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="时间" min-width="170" />
        <el-table-column sortable label="返佣金额" min-width="120" prop="price" />
      </el-table>
      <div class="pager">
        <el-pagination
          background
          :page-sizes="[10, 20, 30, 40]"
          :page-size="spreadFrom.limit"
          :current-page="spreadFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="spreadData.total"
          @size-change="handleSizeChangeSpread"
          @current-change="pageChangeSpread"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { promoterListApi, spreadListApi, spreadOrderListApi, spreadClearApi } from '@/api/distribution';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'AccountsUser',
  data() {
    return {
      timeVal: [],
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      tableFrom: {
        dateLimit: '',
        content: '',
        searchType: 'all',
        page: 1,
        limit: 20,
      },
      dialogVisible: false,
      spreadData: {
        data: [],
        total: 0,
      },
      spreadFrom: {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: '',
      },
      timeValSpread: [],
      spreadLoading: false,
      uid: '',
      onName: '',
      titleName: '',
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    // 时间截到「分」：完整时间戳 186px 太占宽，秒对分销台账无决策价值
    fmtTime(t) {
      return t ? String(t).slice(0, 16) : '—';
    },
    // 金额千分位 + 两位小数；空值统一显示 0.00，避免列内出现空白造成"数据缺失"错觉
    money(v) {
      const n = Number(v);
      if (v === null || v === undefined || v === '' || isNaN(n)) return '0.00';
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    // 是否存在上级推广人：spreadUid 与 spreadNickname 任一有值即视为有上级
    hasParent(row) {
      return (row && row.spreadUid > 0) || !!(row && row.spreadNickname && row.spreadNickname !== '无');
    },
    handleResetDialog() {
      this.spreadFrom.dateLimit = '';
      this.spreadFrom.type = 0;
      this.spreadFrom.nickName = '';
      this.timeValSpread = [];
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    //重置
    handleReset() {
      this.tableFrom.dateLimit = '';
      this.tableFrom.content = '';
      this.tableFrom.searchType = 'all';
      this.timeVal = [];
      this.$refs.userSearchInput && this.$refs.userSearchInput.clearInput();
      this.getList();
    },
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    // 清除
    clearSpread(row) {
      this.$modalSure('解除【' + row.nickname + '】的上级推广人吗').then(() => {
        spreadClearApi(row.uid).then((res) => {
          this.$message.success('清除成功');
          this.getList();
        });
      });
    },
    onSpread(uid, n, p) {
      this.onName = n;
      this.titleName = p;
      this.uid = uid;
      this.dialogVisible = true;
      this.spreadFrom = {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: uid,
      };
      this.getListSpread();
    },
    handleClose() {
      this.dialogVisible = false;
    },
    // 具体日期
    onchangeTimeSpread(e) {
      this.timeValSpread = e;
      this.spreadFrom.dateLimit = e ? this.timeValSpread.join(',') : '';
      this.spreadFrom.page = 1;
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    onChanges() {
      this.spreadFrom.page = 1;
      this.onName === 'man' ? this.getListSpread() : this.getSpreadOrderList();
    },
    // 推广人列表
    getListSpread() {
      this.spreadLoading = true;
      spreadListApi({ page: this.spreadFrom.page, limit: this.spreadFrom.limit }, this.spreadFrom)
        .then((res) => {
          this.spreadData.data = res.list;
          this.spreadData.total = res.total;
          this.spreadLoading = false;
        })
        .catch(() => {
          this.spreadLoading = false;
        });
    },
    pageChangeSpread(page) {
      this.spreadFrom.page = page;
      this.onName === 'man' ? this.getListSpread(this.uid) : this.getSpreadOrderList(this.uid);
    },
    handleSizeChangeSpread(val) {
      this.spreadFrom.limit = val;
      this.onName === 'man' ? this.getListSpread(this.uid) : this.getSpreadOrderList(this.uid);
    },
    // 推广订单
    onSpreadOrder(uid, n, p) {
      this.uid = uid;
      this.onName = n;
      this.titleName = p;
      this.dialogVisible = true;
      this.spreadFrom = {
        page: 1,
        limit: 10,
        dateLimit: '',
        type: 0,
        nickName: '',
        uid: uid,
      };
      this.getSpreadOrderList();
    },
    getSpreadOrderList() {
      this.spreadLoading = true;
      spreadOrderListApi({ page: this.spreadFrom.page, limit: this.spreadFrom.limit }, this.spreadFrom)
        .then((res) => {
          this.spreadData.data = res.list;
          this.spreadData.total = res.total;
          this.spreadLoading = false;
        })
        .catch(() => {
          this.spreadLoading = false;
        });
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.tableFrom.dateLimit = e ? this.timeVal.join(',') : '';
      this.tableFrom.page = 1;
      this.getList();
    },
    // 列表
    getList() {
      this.listLoading = true;
      promoterListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
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
      this.getList();
    },
  },
};
</script>

<style scoped>
/* 列表范式（summary-bar / filter-panel / table-lg / op-grid / avatar / info-line / lv-chip / kv）
   已统一提升到 theme/styles.scss 全局定义，本页不再写 scoped 副本 */
::v-deep .op-cell .op-tag {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
