<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条 -->
      <div class="summary-bar">目前有 <b>{{ total }}</b> 家门店。</div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="关键词">
            <el-input v-model="tableFrom.keywords" placeholder="门店名称/地址/电话" clearable style="width: 180px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button v-if="checkPermi(['admin:merchant:store:save'])" type="primary" plain icon="el-icon-plus" @click="openEdit()">新增门店</el-button>
        </div>
      </div>
      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <!-- 门店：名称 + 地址 两行 -->
        <el-table-column label="门店" min-width="160">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.name }}</div>
            <div class="info-line">{{ scope.row.address }}</div>
          </template>
        </el-table-column>
        <!-- 电话：132 保证 13~14 位号码不折行 -->
        <el-table-column prop="phone" label="电话" width="132" />
        <el-table-column prop="dayTime" label="营业时间" width="115" show-overflow-tooltip />
        <!-- 履约服务/负责人/状态：弹性分摊 -->
        <el-table-column label="履约服务" min-width="115">
          <template slot-scope="scope">
            <span v-if="scope.row.selfPickup" class="svc-tag svc-pickup">自提</span>
            <span v-if="scope.row.delivery" class="svc-tag svc-delivery">配送 {{ scope.row.deliveryRadius }}km</span>
            <span v-if="!scope.row.selfPickup && !scope.row.delivery" class="info-line">未开启</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" min-width="95" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.leaderUid > 0">{{ scope.row.leaderName }}（{{ scope.row.leaderUid }}）</span>
            <span v-else class="info-line">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="85">
          <template slot-scope="scope">
            <span class="st-dot st-dot-lg" :class="{ on: scope.row.isShow }"><i></i>{{ scope.row.isShow ? '启用' : '禁用' }}</span>
          </template>
        </el-table-column>
        <!-- 操作：彩色小按钮网格 -->
        <el-table-column label="操作" width="196" fixed="right" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid">
              <el-button v-if="checkPermi(['admin:merchant:store:update'])" size="mini" plain class="op-tag tint-primary" @click="openEdit(scope.row)">修改</el-button>
              <el-button v-if="checkPermi(['admin:merchant:store:update'])" size="mini" plain class="op-tag tint-warn" @click="onStatus(scope.row)">{{ scope.row.isShow ? '禁用' : '启用' }}</el-button>
              <el-button v-if="checkPermi(['admin:merchant:store:delete'])" size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 门店编辑弹窗 -->
    <el-dialog :title="editForm.id ? '修改门店' : '新增门店'" :visible.sync="editVisible" width="640px">
      <el-form :model="editForm" label-width="110px" size="small">
        <el-divider content-position="left">基础信息</el-divider>
        <el-form-item label="门店名称" required>
          <el-input v-model="editForm.name" placeholder="门店名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="门店地址" required>
          <el-input v-model="editForm.address" placeholder="省市区+街道（如：河南省郑州市金水区xx路xx号）" maxlength="200" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="editForm.detailedAddress" placeholder="门牌号等补充信息" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="editForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="editForm.dayTime" placeholder="如 08:30-21:30" maxlength="64" />
        </el-form-item>
        <el-divider content-position="left">定位信息</el-divider>
        <el-form-item label="经纬度" required>
          <el-col :span="11">
            <el-input v-model="editForm.latitude" placeholder="纬度（如 34.7466）" />
          </el-col>
          <el-col :span="2" class="tc">-</el-col>
          <el-col :span="11">
            <el-input v-model="editForm.longitude" placeholder="经度（如 113.6254）" />
          </el-col>
        </el-form-item>
        <el-divider content-position="left">履约服务</el-divider>
        <el-form-item label="到店自提">
          <el-switch v-model="editForm.selfPickup" />
          <span class="switch-tip">开启后用户可选择到店自取商品</span>
        </el-form-item>
        <el-form-item label="上门配送">
          <el-switch v-model="editForm.delivery" />
          <span class="switch-tip">开启后门店可承接配送订单</span>
        </el-form-item>
        <el-form-item label="配送半径(公里)" required>
          <el-input-number v-model="editForm.deliveryRadius" :min="0.1" :max="100" :precision="2" style="width: 160px" />
          <span class="switch-tip">超出半径的用户无法选择该门店配送</span>
        </el-form-item>
        <el-divider content-position="left">费用配置</el-divider>
        <el-form-item label="核销服务费">
          <el-input-number v-model="editForm.verifyFee" :min="0" :precision="2" style="width: 160px" />
          <span class="switch-tip">0=免费</span>
        </el-form-item>
        <el-form-item label="自提服务费">
          <el-input-number v-model="editForm.pickupFee" :min="0" :precision="2" style="width: 160px" />
          <span class="switch-tip">0=免费</span>
        </el-form-item>
        <el-form-item label="配送服务费">
          <el-input-number v-model="editForm.deliveryFee" :min="0" :precision="2" style="width: 160px" />
          <span class="switch-tip">0=免费</span>
        </el-form-item>
        <el-divider content-position="left">负责人</el-divider>
        <el-form-item label="负责人">
          <!-- 与「代理-代理管理」同款：弹窗搜索会员选择，避免手填 UID 填错人 -->
          <div class="user-picker">
            <el-input :value="leaderLabel" placeholder="请选择负责人（会员）" readonly style="width: 300px">
              <template slot="append">
                <el-button @click="openUserPicker">选择会员</el-button>
              </template>
            </el-input>
            <el-button v-if="editForm.leaderUid > 0" type="text" style="margin-left: 8px" @click="clearLeader">解绑</el-button>
          </div>
          <div class="switch-tip" style="margin-left: 0">绑定后负责人可在会员中心进入门店中心管理本店</div>
        </el-form-item>
        <el-form-item label="门店状态">
          <el-switch v-model="editForm.isShow" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 选择负责人（会员）——与「代理-代理管理」同款交互 -->
    <el-dialog title="选择负责人（会员）" :visible.sync="userPickerVisible" width="720px" append-to-body>
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
  </div>
</template>

<script>
import { merchantStoreListApi, merchantStoreInfoApi, merchantStoreSaveApi, merchantStoreUpdateApi, merchantStoreStatusApi, merchantStoreDeleteApi } from '@/api/merchantStore';
import { userListApi } from '@/api/user';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'MerchantStoreList',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, keywords: '', status: null },
      editVisible: false,
      editForm: this.defaultForm(),
      // 选择负责人（与「代理-代理管理」同款）
      userPickerVisible: false,
      userKeyword: '',
      userList: [],
      userTotal: 0,
      userFrom: { page: 1, limit: 10 },
      userLoading: false,
      pickUid: null,
      pickedUser: null,
      selectedUser: null
    };
  },
  computed: {
    // 选择后展示「昵称（UID:xxx）」，与「代理-代理管理」保持一致
    leaderLabel() {
      if (this.selectedUser) return (this.selectedUser.nickname || '-') + '（UID:' + this.selectedUser.uid + '）';
      return this.editForm.leaderUid > 0 ? 'UID:' + this.editForm.leaderUid : '';
    }
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    defaultForm() {
      return {
        id: null, name: '', address: '', detailedAddress: '', phone: '', dayTime: '',
        latitude: '', longitude: '', isShow: true,
        selfPickup: true, delivery: false, deliveryRadius: 5,
        verifyFee: 0, pickupFee: 0, deliveryFee: 0,
        leaderUid: 0
      };
    },
    getList() {
      this.loading = true;
      merchantStoreListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, keywords: '', status: null };
      this.getList();
    },
    openEdit(row) {
      if (row) {
        merchantStoreInfoApi(row.id).then(res => {
          const d = res || row;
          this.editForm = {
            id: d.id, name: d.name, address: d.address, detailedAddress: d.detailedAddress,
            phone: d.phone, dayTime: d.dayTime, latitude: d.latitude, longitude: d.longitude,
            isShow: !!d.isShow, selfPickup: !!d.selfPickup, delivery: !!d.delivery,
            deliveryRadius: Number(d.deliveryRadius) || 5,
            verifyFee: Number(d.verifyFee) || 0, pickupFee: Number(d.pickupFee) || 0, deliveryFee: Number(d.deliveryFee) || 0,
            leaderUid: d.leaderUid || 0
          };
          // 回显已绑定的负责人
          this.selectedUser = d.leaderUid > 0 ? { uid: d.leaderUid, nickname: d.leaderName || '' } : null;
          this.editVisible = true;
        });
      } else {
        this.editForm = this.defaultForm();
        this.selectedUser = null;
        this.editVisible = true;
      }
    },
    // ===== 选择负责人（会员，与「代理-代理管理」同款交互） =====
    openUserPicker() {
      this.userPickerVisible = true;
      this.userKeyword = '';
      this.pickUid = this.editForm.leaderUid > 0 ? Number(this.editForm.leaderUid) : null;
      this.pickedUser = this.selectedUser;
      this.userFrom.page = 1;
      this.searchUsers();
    },
    searchUsers() {
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
      this.editForm.leaderUid = Number(this.pickedUser.uid) || 0;
      this.userPickerVisible = false;
    },
    clearLeader() {
      this.selectedUser = null;
      this.editForm.leaderUid = 0;
    },
    onSave() {
      if (!this.editForm.name) return this.$message.error('请填写门店名称');
      if (!this.editForm.address) return this.$message.error('请填写门店地址');
      if (!this.editForm.phone) return this.$message.error('请填写联系电话');
      if (!this.editForm.latitude || !this.editForm.longitude) return this.$message.error('请填写门店经纬度');
      if (!this.editForm.deliveryRadius || this.editForm.deliveryRadius <= 0) return this.$message.error('请填写正确的配送半径');
      this.saving = true;
      const api = this.editForm.id ? merchantStoreUpdateApi : merchantStoreSaveApi;
      api(this.editForm).then(() => {
        this.$message.success(this.editForm.id ? '修改成功' : '创建成功');
        this.editVisible = false;
        this.saving = false;
        this.getList();
      }).catch(() => { this.saving = false; });
    },
    onStatus(row) {
      merchantStoreStatusApi(row.id, !row.isShow).then(() => {
        this.$message.success(row.isShow ? '已禁用' : '已启用');
        this.getList();
      });
    },
    onDelete(row) {
      this.$confirm('确定删除门店「' + row.name + '」吗？删除后该门店所有履约服务停止。', '提示', { type: 'warning' }).then(() => {
        merchantStoreDeleteApi(row.id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
/* 列表范式（summary-bar/filter-panel/table-lg/op-grid/info-* 等）已全局定义于 theme/styles.scss */
.tc { text-align: center; }
.switch-tip { margin-left: 10px; color: #999; font-size: 12px; }
.svc-tag { display: inline-block; font-size: 12px; line-height: 20px; padding: 0 6px; border-radius: 3px; margin-right: 6px; white-space: nowrap; }
.svc-pickup { background: #e8f8f0; color: #0f9a58; }
.svc-delivery { background: #e8f1ff; color: #0256ff; }
/* 选择负责人（与「代理-代理管理」同款） */
.user-picker { display: inline-block; }
</style>
