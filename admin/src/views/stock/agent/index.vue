<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条 -->
      <div class="summary-bar">目前有 <b>{{ total }}</b> 名订货商。</div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="昵称/手机">
            <el-input v-model="tableFrom.keywords" placeholder="昵称/手机" clearable style="width: 180px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="订货商UID">
            <el-input v-model.number="tableFrom.uid" placeholder="UID精确查询" clearable style="width: 150px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="层级">
            <el-select v-model="tableFrom.levelId" placeholder="所有层级" clearable style="width: 140px">
              <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="所有状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
              <el-option label="待对方同意" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button v-if="checkPermi(['admin:stock:agent:save'])" type="primary" plain icon="el-icon-plus" @click="openEdit()">新增订货商</el-button>
        </div>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <!-- 头像 -->
        <el-table-column label="头像" width="70" align="center">
          <template slot-scope="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" class="avatar-img" />
            <span v-else class="avatar-text">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
          </template>
        </el-table-column>
        <!-- 订货商信息：昵称/手机/ID/上级 多行 -->
        <el-table-column label="订货商信息" width="220">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname }}</div>
            <div class="info-line">{{ scope.row.phone || '—' }}</div>
            <div class="info-line">ID：{{ scope.row.uid }}</div>
            <div class="info-line">上级：<span :class="{ hq: !(scope.row.parentId > 0) }">{{ scope.row.parentId > 0 ? scope.row.parentName : '总部' }}</span></div>
          </template>
        </el-table-column>
        <!-- 等级/状态/备注：三列弹性均分剩余空间，避免备注单列独吞出现大片空白 -->
        <el-table-column label="等级" min-width="100">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.levelName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template slot-scope="scope">
            <span
              class="st-dot st-dot-lg"
              :class="{ on: scope.row.status === 1, warn: scope.row.status === 2 }"
              ><i></i>{{ agentStatusText(scope.row.status) }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.mark || '—' }}</template>
        </el-table-column>
        <!-- 创建时间：右侧留白，与操作列拉开间距 -->
        <el-table-column label="创建时间" width="150" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
        </el-table-column>
        <!-- 操作：着色小按钮网格（左留间距拉开时间列，右留白使整组按钮左移） -->
        <el-table-column label="操作" width="250" fixed="right" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-stack">
              <!-- 上排：写操作（修改 / 状态切换 / 删除）。删除固定第 3 格，位置稳定好记，
                   不再夹在「库存记录」旁边误触 -->
              <div class="op-grid">
                <el-button size="mini" plain class="op-tag tint-primary" @click="openEdit(scope.row)">修改</el-button>
                <el-button v-if="checkPermi(['admin:stock:agent:update'])" size="mini" plain class="op-tag tint-warn" @click="onStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
                <el-button v-if="checkPermi(['admin:stock:agent:delete'])" size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
              </div>
              <!-- 下排：只读查看（团队 / 库存 / 库存记录）。统一中性色 + 小一档字号，
                   与上排写操作在视觉上分层，一眼能区分"改数据"和"看数据" -->
              <div class="op-grid">
                <el-button size="mini" plain class="op-tag op-tag--sm tint-neutral" @click="openTeam(scope.row)">团队</el-button>
                <el-button size="mini" plain class="op-tag op-tag--sm tint-neutral" @click="openStock(scope.row)">库存</el-button>
                <el-button size="mini" plain class="op-tag op-tag--sm tint-neutral" @click="openStockLog(scope.row)">库存记录</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 订货商编辑弹窗 -->
    <el-dialog :title="editForm.id ? '修改订货商' : '新增订货商'" :visible.sync="editVisible" width="480px">
      <el-form :model="editForm" label-width="90px" size="small">
        <el-form-item label="订货商用户">
          <!-- 与「代理-代理管理」同款：弹窗搜索选择会员，避免手填 UID 填错人 -->
          <div class="user-picker">
            <el-input :value="userLabel" placeholder="请选择订货用户" readonly style="width: 300px">
              <template slot="append">
                <el-button :disabled="!!editForm.id" @click="openUserPicker">选择用户</el-button>
              </template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="层级">
          <el-select v-model="editForm.levelId" style="width: 100%">
            <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级订货商">
          <!-- 与「选择用户」同款：弹窗内搜索选择，避免订货商多了以后在下拉里翻不到 -->
          <div class="user-picker">
            <el-input :value="parentLabel" placeholder="请选择上级订货商" readonly style="width: 300px">
              <template slot="append">
                <el-button @click="openParentPicker">选择上级</el-button>
              </template>
            </el-input>
            <el-button v-if="editForm.parentId > 0" type="text" style="margin-left: 8px" @click="clearParent">设为总部</el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.mark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 选择订货用户（与「代理-代理管理」同款交互） -->
    <el-dialog title="选择订货用户" :visible.sync="userPickerVisible" width="720px" append-to-body>
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item>
          <el-input v-model="userKeyword" placeholder="UID / 手机号 / 昵称" clearable style="width: 240px" @keyup.enter.native="searchUsers" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="searchUsers">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table class="admin-table" v-loading="userLoading" :data="userList" size="small" stripe highlight-current-row max-height="380">
        <el-table-column label="" width="50">
          <template slot-scope="scope">
            <el-radio v-model="pickUid" :label="scope.row.uid" @change="onPickUser(scope.row)"><span></span></el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="UID" width="90" />
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :page-size="userFrom.limit" :current-page="userFrom.page" :total="userTotal" @current-change="userPageChange" />
      </div>
      <span slot="footer">
        <el-button size="small" @click="userPickerVisible = false">取消</el-button>
        <el-button size="small" type="primary" :disabled="!pickUid" @click="confirmUser">确定</el-button>
      </span>
    </el-dialog>

    <!-- 选择上级订货商（与「选择订货用户」同款交互） -->
    <el-dialog title="选择上级订货商" :visible.sync="parentPickerVisible" width="780px" append-to-body>
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item>
          <el-input v-model="parentKeyword" placeholder="UID / 手机号 / 昵称" clearable style="width: 240px" @keyup.enter.native="searchParents" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="searchParents">搜索</el-button>
        </el-form-item>
      </el-form>
      <!-- 总部固定项：不需要挂具体订货商时直接选它 -->
      <div class="hq-row">
        <el-radio v-model="pickParentId" :label="0" @change="onPickParent(null)">总部（无上级）</el-radio>
      </div>
      <el-table v-loading="parentLoading" :data="parentRows" size="small" stripe highlight-current-row max-height="380">
        <el-table-column label="" width="50">
          <template slot-scope="scope">
            <el-radio v-model="pickParentId" :label="scope.row.id" @change="onPickParent(scope.row)"><span></span></el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="UID" width="90" />
        <el-table-column prop="nickname" label="昵称" min-width="130" show-overflow-tooltip />
        <el-table-column label="级别" width="120">
          <template slot-scope="scope">
            <span class="lv-chip">{{ scope.row.levelName || '未分级' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="其上级" min-width="110" show-overflow-tooltip>
          <template slot-scope="scope">
            <span :class="{ hq: !(scope.row.parentId > 0) }">{{ scope.row.parentId > 0 ? scope.row.parentName : '总部' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :page-size="parentFrom.limit" :current-page="parentFrom.page" :total="parentTotal" @current-change="parentPageChange" />
      </div>
      <span slot="footer">
        <el-button size="small" @click="parentPickerVisible = false">取消</el-button>
        <el-button size="small" type="primary" :disabled="pickParentId === null" @click="confirmParent">确定</el-button>
      </span>
    </el-dialog>

    <!-- 下级团队弹窗 -->
    <el-dialog :title="'下级团队 - ' + (teamAgent ? teamAgent.nickname : '')" :visible.sync="teamVisible" width="720px">
      <div class="team-tip">共 {{ teamRows.length }} 位下级，含伞下全部层级</div>
      <el-table class="admin-table" :data="teamRows" size="small" stripe max-height="440">
        <el-table-column label="属于第几层" width="100">
          <template slot-scope="scope">
            <span class="depth-tag">第 {{ scope.row.depth }} 层</span>
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="UID" width="80" />
        <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="levelName" label="层级" width="90" />
        <el-table-column label="状态" width="70">
          <template slot-scope="scope">
            <span class="st-dot" :class="{ on: scope.row.status === 1, warn: scope.row.status === 2 }"
              ><i></i>{{ agentStatusText(scope.row.status) }}</span
            >
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button size="small" @click="teamVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 库存调整弹窗 -->
    <el-dialog :title="'调整库存 - ' + (stockAgent ? stockAgent.nickname : '')" :visible.sync="stockVisible" width="560px">
      <el-form :model="stockForm" label-width="100px" size="small">
        <el-form-item label="库存类型">
          <el-radio-group v-model="stockForm.stockType">
            <el-radio :label="2">虚拟库存</el-radio>
            <el-radio :label="1">实体库存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="stockForm.productId" filterable placeholder="选择商品" style="width: 100%" @change="onStockProductChange">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.storeName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" v-if="skuOptions.length">
          <el-select v-model="stockForm.skuKey" clearable placeholder="不选=整品" style="width: 100%">
            <el-option v-for="s in skuOptions" :key="s.skuKey" :label="s.attrValueText" :value="s.skuKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockForm.num" :step="1" style="width: 160px" />
          <span class="switch-tip">正数增加、负数扣减</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.mark" type="textarea" :rows="2" placeholder="调整原因" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="stockVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="stockSaving" @click="submitStock">确定</el-button>
      </div>
    </el-dialog>

    <!-- 库存记录（溯源）+ 当前库存情况 -->
    <el-drawer :title="'库存记录 - ' + (logAgent ? logAgent.nickname : '')" :visible.sync="logVisible" size="760px">
      <div style="padding: 0 20px 20px">
        <el-divider content-position="left">当前库存情况</el-divider>
        <el-table :data="curPhysical" size="mini" v-loading="logLoading" style="margin-bottom: 10px">
          <el-table-column label="实体库存" min-width="200">
            <template slot-scope="scope">
              <img v-if="scope.row.image" :src="scope.row.image" style="width: 24px; height: 24px; margin-right: 6px; border-radius: 4px" />
              <span>{{ scope.row.productName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="num" label="可供应量" width="100" />
          <el-table-column label="换货中" width="80">
            <template slot-scope="scope">
              <span v-if="scope.row.exchangeNum > 0" style="color: #c47400; font-weight: 600">{{ scope.row.exchangeNum }}</span>
              <span v-else>0</span>
            </template>
          </el-table-column>
        </el-table>
        <el-table :data="curVirtual" size="mini" v-loading="logLoading" style="margin-bottom: 10px">
          <el-table-column label="虚拟库存" min-width="200">
            <template slot-scope="scope">
              <img v-if="scope.row.image" :src="scope.row.image" style="width: 24px; height: 24px; margin-right: 6px; border-radius: 4px" />
              <span>{{ scope.row.productName }}</span>
              <span v-if="scope.row.skuKey" style="color: #909399">（{{ scope.row.skuKey }}）</span>
            </template>
          </el-table-column>
          <el-table-column prop="num" label="累计" width="80" />
          <el-table-column prop="remainNum" label="剩余" width="80" />
        </el-table>
        <div v-if="!curPhysical.length && !curVirtual.length" class="switch-tip">该会员暂无库存</div>

        <el-divider content-position="left">库存修改记录</el-divider>
        <el-table :data="logList" size="mini" v-loading="logLoading">
          <el-table-column prop="createTime" label="时间" width="160">
            <template slot-scope="scope">{{ fmtTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="stockTypeText" label="类型" width="90" />
          <el-table-column label="商品" min-width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.productName || ('商品' + scope.row.productId) }}</span>
              <span v-if="scope.row.skuKey" style="color: #909399">（{{ scope.row.skuKey }}）</span>
            </template>
          </el-table-column>
          <el-table-column label="变动" width="80">
            <template slot-scope="scope">
              <span :style="{ color: scope.row.num >= 0 ? '#f56c6c' : '#67c23a' }">{{ scope.row.num > 0 ? '+' : '' }}{{ scope.row.num }}</span>
            </template>
          </el-table-column>
          <el-table-column label="采购人（下级）" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <span v-if="scope.row.linkNickname">{{ scope.row.linkNickname }}<span v-if="scope.row.linkAgentName" style="color: #b8860b">（{{ scope.row.linkAgentName }}）</span></span>
              <span v-else style="color: #c0c4cc">—</span>
            </template>
          </el-table-column>
          <el-table-column label="关联单号" width="180" show-overflow-tooltip>
            <template slot-scope="scope">
              <span v-if="scope.row.orderNo">{{ scope.row.orderNo }}</span>
              <span v-else style="color: #c0c4cc">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="mark" label="原因" min-width="200" show-overflow-tooltip />
        </el-table>
        <div v-if="!logList.length && !logLoading" class="switch-tip" style="padding: 12px 0">暂无修改记录</div>
        <div class="pager" style="text-align: right; margin-top: 10px">
          <el-pagination
            background
            small
            layout="total, prev, pager, next"
            :page-size="logFrom.limit"
            :current-page="logFrom.page"
            :total="logTotal"
            @current-change="onLogPageChange"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { stockAgentListApi, stockAgentSaveApi, stockAgentUpdateApi, stockAgentStatusApi, stockAgentDeleteApi, stockAgentTeamApi, stockAgentVirtualAdjustApi, stockAgentPhysicalAdjustApi, stockLevelListApi, stockProductListApi, stockProductSkuListApi, stockAdjustLogListApi, stockAgentStockApi } from '@/api/stock';
import { userListApi } from '@/api/user';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'StockAgent',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      levels: [],
      agentOptions: [],
      productOptions: [],
      tableFrom: { page: 1, limit: 20, keywords: '', uid: null, levelId: null, status: null },
      editVisible: false,
      editForm: { id: null, uid: '', levelId: null, parentId: 0, mark: '' },
      // 选择订货用户（与「代理-代理管理」同款）
      userPickerVisible: false,
      userKeyword: '',
      userList: [],
      userTotal: 0,
      userFrom: { page: 1, limit: 10 },
      userLoading: false,
      pickUid: null,
      pickedUser: null,
      selectedUser: null,
      // 选择上级订货商（与「选择订货用户」同款）
      parentPickerVisible: false,
      parentKeyword: '',
      parentList: [],
      parentTotal: 0,
      parentFrom: { page: 1, limit: 10 },
      parentLoading: false,
      pickParentId: null,
      pickedParent: null,
      selectedParent: null,
      teamVisible: false,
      teamAgent: null,
      teamRows: [],
      stockVisible: false,
      stockSaving: false,
      stockAgent: null,
      logVisible: false,
      logLoading: false,
      logAgent: null,
      logList: [],
      logTotal: 0,
      logFrom: { page: 1, limit: 10 },
      curPhysical: [],
      curVirtual: [],
      skuOptions: [],
      stockForm: { stockType: 2, productId: null, skuKey: '', num: 0, mark: '' }
    };
  },
  computed: {
    // 选择用户后展示「昵称（UID:xxx）」，与「代理-代理管理」保持一致
    userLabel() {
      if (this.selectedUser) return (this.selectedUser.nickname || '-') + '（UID:' + this.selectedUser.uid + '）';
      return this.editForm.uid ? 'UID:' + this.editForm.uid : '';
    },
    // 上级订货商回显：昵称（级别）；挂总部时直接显示总部
    parentLabel() {
      if (this.selectedParent && this.selectedParent.id > 0) {
        return this.selectedParent.nickname + (this.selectedParent.levelName ? '（' + this.selectedParent.levelName + '）' : '');
      }
      if (this.editForm.parentId > 0) return '订货商ID：' + this.editForm.parentId;
      return '总部（无上级）';
    },
    // 上级候选：排除自己，避免把自己设为上级形成自环
    parentRows() {
      return this.parentList.filter((a) => a.id !== this.editForm.id);
    }
  },
  methods: {
    checkPermi,
    fmtTime(t) {
      return t ? String(t).slice(0, 16) : '—';
    },
    getList() {
      this.loading = true;
      stockAgentListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    loadLevels() {
      stockLevelListApi().then(res => { this.levels = res || []; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, keywords: '', uid: null, levelId: null, status: null };
      this.getList();
    },
    openEdit(row) {
      if (row) {
        this.editForm = { id: row.id, uid: row.uid, levelId: row.levelId, parentId: row.parentId || 0, mark: row.mark };
        // 编辑时回显已绑定的会员
        this.selectedUser = { uid: row.uid, nickname: row.nickname };
        // 回显当前上级（级别名下面用预载列表补全）
        this.selectedParent = row.parentId > 0 ? { id: row.parentId, nickname: row.parentName || '', levelName: '' } : null;
      } else {
        this.editForm = { id: null, uid: '', levelId: this.levels.length ? this.levels[this.levels.length - 1].id : null, parentId: 0, mark: '' };
        this.selectedUser = null;
        this.selectedParent = null;
      }
      // 预载候选：仅用于把「上级」的级别名补全到回显上，实际选择走弹窗搜索
      stockAgentListApi({ page: 1, limit: 1000 }).then(res => {
        this.agentOptions = (res && res.list) || [];
        if (this.editForm.parentId > 0) {
          const p = this.agentOptions.find((a) => a.id === this.editForm.parentId);
          if (p) this.selectedParent = { id: p.id, nickname: p.nickname, levelName: p.levelName };
        }
      });
      this.editVisible = true;
    },
    // ===== 选择订货用户（与「代理-代理管理」同款交互） =====
    openUserPicker() {
      this.userPickerVisible = true;
      this.userKeyword = '';
      this.pickUid = this.editForm.uid ? Number(this.editForm.uid) : null;
      this.pickedUser = this.selectedUser;
      this.userFrom.page = 1;
      this.searchUsers();
    },
    searchUsers() {
      this.userLoading = true;
      this.userFrom.page = 1;
      this.loadUsers();
    },
    loadUsers() {
      this.userLoading = true;
      const params = { page: this.userFrom.page, limit: this.userFrom.limit, searchType: 'all' };
      if (this.userKeyword) params.content = this.userKeyword;
      userListApi(params)
        .then((res) => {
          this.userList = (res && res.list) || [];
          this.userTotal = (res && res.total) || 0;
          this.userLoading = false;
        })
        .catch(() => {
          this.userLoading = false;
        });
    },
    userPageChange(page) {
      this.userFrom.page = page;
      this.loadUsers();
    },
    onPickUser(row) {
      this.pickedUser = { uid: row.uid, nickname: row.nickname, phone: row.phone };
    },
    confirmUser() {
      if (!this.pickedUser && this.pickUid) {
        this.pickedUser = this.userList.find((u) => u.uid === this.pickUid) || null;
      }
      if (!this.pickedUser) return;
      this.selectedUser = { uid: this.pickedUser.uid, nickname: this.pickedUser.nickname };
      this.editForm.uid = String(this.pickedUser.uid);
      this.userPickerVisible = false;
    },
    // ===== 选择上级订货商（与「选择订货用户」同款交互） =====
    openParentPicker() {
      this.parentPickerVisible = true;
      this.parentKeyword = '';
      this.pickParentId = this.editForm.parentId > 0 ? this.editForm.parentId : 0;
      this.pickedParent = this.selectedParent;
      this.parentFrom.page = 1;
      this.searchParents();
    },
    searchParents() {
      this.parentFrom.page = 1;
      this.loadParents();
    },
    loadParents() {
      this.parentLoading = true;
      const params = { page: this.parentFrom.page, limit: this.parentFrom.limit };
      if (this.parentKeyword) params.keywords = this.parentKeyword;
      stockAgentListApi(params)
        .then((res) => {
          this.parentList = (res && res.list) || [];
          this.parentTotal = (res && res.total) || 0;
          this.parentLoading = false;
        })
        .catch(() => { this.parentLoading = false; });
    },
    parentPageChange(page) {
      this.parentFrom.page = page;
      this.loadParents();
    },
    onPickParent(row) {
      this.pickedParent = row ? { id: row.id, nickname: row.nickname, levelName: row.levelName } : null;
    },
    confirmParent() {
      // 0=总部；null=还没选
      if (this.pickParentId === 0) {
        this.selectedParent = null;
        this.editForm.parentId = 0;
        this.parentPickerVisible = false;
        return;
      }
      if (!this.pickedParent || this.pickedParent.id !== this.pickParentId) {
        const hit = this.parentRows.find((a) => a.id === this.pickParentId);
        this.pickedParent = hit ? { id: hit.id, nickname: hit.nickname, levelName: hit.levelName } : null;
      }
      if (!this.pickedParent) return;
      this.selectedParent = { id: this.pickedParent.id, nickname: this.pickedParent.nickname, levelName: this.pickedParent.levelName };
      this.editForm.parentId = this.pickedParent.id;
      this.parentPickerVisible = false;
    },
    clearParent() {
      this.selectedParent = null;
      this.editForm.parentId = 0;
    },
    onSave() {
      if (!this.editForm.uid) return this.$message.error('请选择订货用户');
      if (!this.editForm.levelId) return this.$message.error('请选择层级');
      this.saving = true;
      const api = this.editForm.id ? stockAgentUpdateApi : stockAgentSaveApi;
      api(this.editForm).then(() => {
        this.$message.success('保存成功');
        this.editVisible = false;
        this.saving = false;
        this.getList();
      }).catch(() => { this.saving = false; });
    },
    onStatus(row) {
      stockAgentStatusApi(row.id, row.status === 1 ? 0 : 1).then(() => {
        this.$message.success('操作成功');
        this.getList();
      });
    },
    // 订货商状态文案：0=禁用 1=启用 2=待对方同意
    agentStatusText(status) {
      return { 0: '禁用', 1: '启用', 2: '待对方同意' }[status] || '启用';
    },
    onDelete(row) {
      this.$confirm('确认删除该订货商？', '提示').then(() => {
        stockAgentDeleteApi(row.id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      }).catch(() => {});
    },
    // ===== 下级团队 =====
    openTeam(row) {
      this.teamAgent = row;
      this.teamRows = [];
      this.teamVisible = true;
      stockAgentTeamApi({ agentId: row.id }).then(res => {
        this.teamRows = res || [];
      });
    },
    // ===== 库存调整 =====
    openStock(row) {
      this.stockAgent = row;
      this.stockForm = { stockType: 2, productId: null, skuKey: '', num: 0, mark: '' };
      this.skuOptions = [];
      this.stockVisible = true;
      if (!this.productOptions.length) {
        stockProductListApi({ page: 1, limit: 500 }).then(res => {
          this.productOptions = (res && res.list) || [];
        });
      }
    },
    onStockProductChange(productId) {
      this.stockForm.skuKey = '';
      this.skuOptions = [];
      if (!productId) return;
      stockProductSkuListApi(productId).then(res => {
        const raw = res || [];
        const list = raw.length > 1 ? raw : [];
        this.skuOptions = list.map(s => {
          let text = s.attrValue || s.skuKey;
          try {
            const obj = typeof s.attrValue === 'string' ? JSON.parse(s.attrValue) : s.attrValue;
            if (obj && typeof obj === 'object') {
              text = Object.keys(obj).map(k => obj[k]).join(' / ');
            }
          } catch (e) {
            // 解析失败保留原文
          }
          return { ...s, attrValueText: text };
        });
      });
    },
    submitStock() {
      if (!this.stockForm.productId) return this.$message.error('请选择商品');
      if (!this.stockForm.num) return this.$message.error('调整数量不能为0');
      this.stockSaving = true;
      const data = {
        agentId: this.stockAgent.id,
        uid: this.stockAgent.uid,
        productId: this.stockForm.productId,
        skuKey: this.stockForm.skuKey || '',
        num: this.stockForm.num,
        mark: this.stockForm.mark || ''
      };
      const api = this.stockForm.stockType === 1 ? stockAgentPhysicalAdjustApi : stockAgentVirtualAdjustApi;
      api(data).then(() => {
        this.$message.success('调整成功');
        this.stockSaving = false;
        this.stockVisible = false;
        if (this.logVisible && this.logAgent && this.stockAgent && this.logAgent.id === this.stockAgent.id) {
          this.loadStockLog();
        }
      }).catch(() => { this.stockSaving = false; });
    },
    openStockLog(row) {
      this.logAgent = row;
      this.logFrom.page = 1;
      this.logVisible = true;
      this.loadStockLog();
    },
    loadStockLog() {
      this.logLoading = true;
      // 注意：request 拦截器已经 return res.data，这里拿到的就是内层载荷，不能再 .data
      stockAdjustLogListApi({ agentId: this.logAgent.id, page: this.logFrom.page, limit: this.logFrom.limit })
        .then((res) => {
          this.logList = (res && res.list) || [];
          this.logTotal = (res && res.total) || 0;
          this.logLoading = false;
        })
        .catch(() => { this.logLoading = false; });
      stockAgentStockApi(this.logAgent.uid)
        .then((res) => {
          this.curPhysical = (res && res.physical) || [];
          this.curVirtual = (res && res.virtual) || [];
        })
        .catch(() => {});
    },
    onLogPageChange(page) {
      this.logFrom.page = page;
      this.loadStockLog();
    }
  },
  mounted() {
    this.getList();
    this.loadLevels();
  }
};
</script>

<style scoped>
/* 列表页范式（.summary-bar/.filter-panel/.table-lg/.op-grid/.op-tag/头像/两行文本/圆点状态）
   已统一提升到 theme/styles.scss 全局定义，本页只保留弹窗与团队表格等页面特有样式 */

.switch-tip { margin-left: 12px; font-size: 12px; color: #909399; line-height: 1.5; }
.team-tip { font-size: 13px; color: #909399; margin-bottom: 10px; }
.depth-tag { display: inline-block; padding: 1px 8px; border-radius: 10px; background: #ecf5ff; color: #409eff; font-size: 12px; }
/* 选择订货用户（与「代理-代理管理」同款） */
.user-picker { display: inline-block; }
/* 选择上级订货商弹窗里的「总部」固定项 */
.hq-row { padding: 4px 10px; margin-bottom: 8px; background: #f7f8fa; border-radius: 4px; }
</style>
