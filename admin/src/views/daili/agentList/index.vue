<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条 -->
      <div class="summary-bar">目前有 <b>{{ tableData.total }}</b> 名代理。</div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <el-form inline size="small" :model="tableFrom" @submit.native.prevent>
          <el-form-item label="关键字">
            <el-input
              v-model="tableFrom.keywords"
              placeholder="UID/区域"
              clearable
              style="width: 180px"
              @keyup.enter.native="searchList"
            />
          </el-form-item>
          <el-form-item label="级别">
            <el-select v-model="tableFrom.level" placeholder="全部级别" clearable style="width: 140px">
              <el-option label="省级" :value="1" />
              <el-option label="市级" :value="2" />
              <el-option label="区级" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="searchList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetList">重置</el-button>
          <el-button v-if="checkPermi(['admin:agent:save'])" type="primary" plain icon="el-icon-plus" @click="openEdit()">添加代理</el-button>
        </div>
      </div>

      <el-table class="admin-table table-lg" v-loading="listLoading" :data="tableData.data" size="small" stripe highlight-current-row>
        <!-- 代理用户：昵称 + UID 两行 -->
        <el-table-column label="代理用户" min-width="140">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.nickname || '-' }}</div>
            <div class="info-line">UID：{{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <!-- 级别/状态/备注/区域：弹性分摊，避免单列独吞留白 -->
        <el-table-column label="级别" width="80">
          <template slot-scope="scope">
            <span class="lv-chip">{{ levelLabel(scope.row.level) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="代理区域" min-width="110" show-overflow-tooltip />
        <el-table-column label="奖励比例" width="85">
          <template slot-scope="scope">{{ scope.row.ratio }}%</template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template slot-scope="scope">
            <span class="st-dot st-dot-lg" :class="{ on: scope.row.status === 1, warn: scope.row.status === 0, danger: scope.row.status === 2 }">
              <i></i>{{ statusLabel(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="applyMark" label="备注" min-width="110" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.applyMark || '—' }}</template>
        </el-table-column>
        <!-- 时间：右侧留白，与操作列拉开间距 -->
        <el-table-column label="时间" width="165" class-name="time-cell">
          <template slot-scope="scope">
            <div class="info-line">创建 {{ scope.row.createTime || '-' }}</div>
            <div class="info-line">审核 {{ scope.row.checkTime || '—' }}</div>
          </template>
        </el-table-column>
        <!-- 操作：彩色小按钮网格 -->
        <el-table-column label="操作" width="196" fixed="right" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid">
              <el-button v-if="checkPermi(['admin:agent:update'])" size="mini" plain class="op-tag tint-primary" @click="openEdit(scope.row)">修改</el-button>
              <el-button v-if="checkPermi(['admin:agent:audit']) && scope.row.status === 0" size="mini" plain class="op-tag tint-warn" @click="onAudit(scope.row, 1)">通过</el-button>
              <el-button v-if="checkPermi(['admin:agent:audit']) && scope.row.status === 0" size="mini" plain class="op-tag tint-danger" @click="onAudit(scope.row, 2)">拒绝</el-button>
              <el-button v-if="checkPermi(['admin:agent:delete'])" size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          background
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, prev, pager, next, jumper"
          :total="tableData.total"
          @current-change="pageChange"
        />
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="editVisible" width="520px" @closed="auditMode = false">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="90px" size="small">
        <el-form-item label="代理用户：" prop="uid">
          <div class="user-picker">
            <el-input :value="userLabel" placeholder="请选择代理用户" readonly style="width: 300px">
              <template slot="append">
                <el-button :disabled="!!editForm.id" @click="openUserPicker">选择用户</el-button>
              </template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="代理级别：" prop="level">
          <el-select v-model="editForm.level" placeholder="请选择级别" style="width: 320px" @change="onLevelChange">
            <el-option label="省级" :value="1" />
            <el-option label="市级" :value="2" />
            <el-option label="区级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="代理区域：" prop="regionArr">
          <el-cascader
            :key="editForm.level"
            :options="regionOptions"
            :props="propsCity"
            filterable
            v-model="editForm.regionArr"
            style="width: 320px"
            :placeholder="regionPlaceholder"
          />
        </el-form-item>
        <el-form-item label="奖励比例：" prop="ratio">
          <el-input-number
            v-model="editForm.ratio"
            :min="0.01"
            :max="100"
            :precision="2"
            :step="0.5"
            style="width: 200px"
          />
          <span style="margin-left: 8px">%</span>
        </el-form-item>
        <el-form-item label="备注：">
          <el-input v-model="editForm.applyMark" type="textarea" :rows="2" style="width: 320px" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="onSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 选择代理用户 -->
    <el-dialog title="选择代理用户" :visible.sync="userPickerVisible" width="720px" append-to-body>
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
      <span slot="footer" class="dialog-footer">
        <el-button @click="userPickerVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pickUid" @click="confirmUser">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { agentListApi, agentSaveApi, agentUpdateApi, agentAuditApi, agentDeleteApi, cityTreeApi, agentSettingApi } from '@/api/daili';
import { userListApi } from '@/api/user';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
import { Debounce } from '@/utils/validate';

export default {
  name: 'AgentList',
  data() {
    return {
      listLoading: false,
      submitLoading: false,
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        level: undefined,
        status: undefined,
      },
      tableData: { data: [], total: 0 },
      editVisible: false,
      cityOptions: [],
      propsCity: {
        children: 'child',
        label: 'name',
        value: 'name',
      },
      editForm: {
        id: null,
        uid: '',
        level: 1,
        regionArr: [],
        ratio: 5,
        applyMark: '',
      },
      editRules: {
        uid: [{ required: true, message: '请选择代理用户', trigger: 'change' }],
        level: [{ required: true, message: '请选择代理级别', trigger: 'change' }],
        regionArr: [{ required: true, type: 'array', min: 1, message: '请选择代理区域', trigger: 'change' }],
        ratio: [{ required: true, message: '请填写奖励比例', trigger: 'blur' }],
      },
      // 用户选择器
      userPickerVisible: false,
      userKeyword: '',
      userLoading: false,
      userList: [],
      userTotal: 0,
      userFrom: { page: 1, limit: 10 },
      pickUid: null,
      pickedUser: null,
      selectedUser: null,
      // 审核模式：点「通过」时打开弹窗，确定=通过并保存比例，避免漏设
      auditMode: false,
      // 各级别默认奖励比例（代理设置里配置，读不到时用内置默认）
      defaultRatios: { 1: 5, 2: 3, 3: 2 },
    };
  },
  computed: {
    dialogTitle() {
      if (this.auditMode) return '通过代理申请';
      return this.editForm.id ? '修改代理' : '添加代理';
    },
    // 区域联动：省级只到省、市级到市、区级到区县
    regionOptions() {
      const depth = Number(this.editForm.level) || 1;
      const trim = (arr, remain) =>
        (arr || []).map((item) => {
          const o = { ...item };
          if (remain > 1 && item.child && item.child.length) {
            o.child = trim(item.child, remain - 1);
          } else {
            delete o.child;
          }
          return o;
        });
      return trim(this.cityOptions, depth);
    },
    regionPlaceholder() {
      const map = { 1: '请选择省份', 2: '请选择省市', 3: '请选择省市区' };
      return map[Number(this.editForm.level)] || '请选择区域';
    },
    userLabel() {
      if (this.selectedUser) {
        return this.selectedUser.nickname + '（UID:' + this.selectedUser.uid + '）';
      }
      return this.editForm.uid ? 'UID:' + this.editForm.uid : '';
    },
  },
  mounted() {
    this.getList();
    this.getCityList();
    this.loadDefaultRatios();
  },
  methods: {
    checkPermi,
    // 读取代理设置里的各级别默认奖励比例（无权限/失败时用内置默认：省5/市3/区2）
    loadDefaultRatios() {
      agentSettingApi()
        .then((res) => {
          if (!res) return;
          const map = {
            1: res.agent_default_ratio_province,
            2: res.agent_default_ratio_city,
            3: res.agent_default_ratio_district,
          };
          Object.keys(map).forEach((k) => {
            const n = Number(map[k]);
            if (!isNaN(n) && map[k] !== null && map[k] !== undefined && map[k] !== '') this.defaultRatios[k] = n;
          });
        })
        .catch(() => {});
    },
    defaultRatioFor(level) {
      const n = Number(this.defaultRatios[level]);
      return isNaN(n) ? 5 : n;
    },
    levelLabel(level) {
      const map = { 1: '省级', 2: '市级', 3: '区级' };
      return map[level] || '-';
    },
    statusLabel(status) {
      const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' };
      return map[status] || '-';
    },
    async getCityList() {
      try {
        const res = await cityTreeApi();
        this.cityOptions = res || [];
      } catch (e) {
        this.cityOptions = [];
      }
    },
    searchList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    resetList() {
      this.tableFrom = { page: 1, limit: 20, keywords: '', level: undefined, status: undefined };
      this.getList();
    },
    getList() {
      this.listLoading = true;
      const params = { ...this.tableFrom };
      ['level', 'status'].forEach((k) => {
        if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k];
      });
      if (!params.keywords) delete params.keywords;
      agentListApi(params)
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
    openEdit(row) {
      this.editForm = {
        id: row ? row.id : null,
        uid: row ? String(row.uid) : '',
        level: row ? row.level : 1,
        regionArr: row ? [row.province, row.city, row.district].filter((v) => v && v.length) : [],
        ratio: row ? Number(row.ratio) : 5,
        applyMark: row ? row.applyMark : '',
      };
      this.selectedUser = row ? { uid: row.uid, nickname: row.nickname || '-' } : null;
      this.editVisible = true;
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate();
      });
    },
    // ===== 选择代理用户 =====
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
      this.$refs.editForm && this.$refs.editForm.validateField('uid');
    },
    onLevelChange() {
      // 切换级别时截断区域选择：省级留1位、市级留2位
      const level = this.editForm.level;
      if (level === 1 && this.editForm.regionArr.length > 1) this.editForm.regionArr = [this.editForm.regionArr[0]];
      if (level === 2 && this.editForm.regionArr.length > 2) {
        this.editForm.regionArr = this.editForm.regionArr.slice(0, 2);
      }
      // 新增 / 审核通过时，切换级别自动带入该级别默认比例（修改已有代理不覆盖已定比例）
      if (this.auditMode || !this.editForm.id) {
        this.editForm.ratio = this.defaultRatioFor(level);
      }
    },
    onSubmit: Debounce(function () {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        const region = this.editForm.regionArr || [];
        const level = this.editForm.level;
        const data = {
          id: this.editForm.id,
          uid: Number(this.editForm.uid),
          level,
          province: region[0] || '',
          city: level >= 2 ? region[1] || '' : '',
          district: level >= 3 ? region[2] || '' : '',
          ratio: this.editForm.ratio,
          status: this.editForm.id ? undefined : 1,
          applyMark: this.editForm.applyMark,
        };
        if (this.auditMode) {
          // 审核模式：一次更新同时写入级别/区域/比例与「通过」状态（后端会记审核时间与变更日志）
          data.status = 1;
        }
        if (!data.province || (level >= 2 && !data.city) || (level >= 3 && !data.district)) {
          return this.$message.warning('代理区域与级别不匹配，请重新选择');
        }
        this.submitLoading = true;
        const req = this.editForm.id ? agentUpdateApi(data) : agentSaveApi(data);
        req
          .then(() => {
            this.$message.success(this.auditMode ? '已通过并保存设置' : this.editForm.id ? '修改成功' : '添加成功');
            this.editVisible = false;
            this.auditMode = false;
            this.submitLoading = false;
            this.getList();
          })
          .catch(() => {
            this.submitLoading = false;
          });
      });
    }),
    onAudit(row, status) {
      if (status !== 1) {
        this.$confirm('拒绝该代理申请？', '提示', { type: 'warning' })
          .then(() => agentAuditApi(row.id, 2))
          .then(() => {
            this.$message.success('操作成功');
            this.getList();
          })
          .catch(() => {});
        return;
      }
      // 通过：直接打开设置弹窗（比例按级别默认带入），确定=通过并保存，避免忘记设置比例
      this.auditMode = true;
      this.editForm = {
        id: row.id,
        uid: String(row.uid),
        level: row.level || 1,
        regionArr: [row.province, row.city, row.district].filter((v) => v && v.length),
        ratio: this.defaultRatioFor(row.level || 1),
        applyMark: row.applyMark || '',
      };
      this.selectedUser = { uid: row.uid, nickname: row.nickname || '-' };
      this.editVisible = true;
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate();
      });
    },
    onDelete(row) {
      this.$confirm('删除后该代理将不再获得区域奖励，确认删除？', '提示', { type: 'warning' })
        .then(() => agentDeleteApi(row.id))
        .then(() => {
          this.$message.success('删除成功');
          this.getList();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped lang="scss">
/* 列表范式（summary-bar/filter-panel/table-lg/op-grid 等）已全局定义于 theme/styles.scss */
.user-picker {
  display: inline-block;
}
</style>
