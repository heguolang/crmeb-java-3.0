<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="关键词">
            <el-input v-model="tableFrom.keywords" placeholder="门店名称/地址/电话" clearable style="width: 200px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="全部" clearable style="width: 110px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="toolbar-actions">
          <el-button v-if="checkPermi(['admin:merchant:store:save'])" type="success" icon="el-icon-plus" @click="openEdit()">新增门店</el-button>
        </div>
      </div>
      <el-table class="admin-table" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column label="门店" min-width="205">
          <template slot-scope="scope">
            <div class="store-name">{{ scope.row.name }}</div>
            <div class="store-addr">{{ scope.row.address }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="108" />
        <el-table-column prop="dayTime" label="营业时间" width="126" show-overflow-tooltip />
        <el-table-column label="履约服务" width="136">
          <template slot-scope="scope">
            <span v-if="scope.row.selfPickup" class="svc-tag svc-pickup">自提</span>
            <span v-if="scope.row.delivery" class="svc-tag svc-delivery">配送 {{ scope.row.deliveryRadius }}km</span>
            <span v-if="!scope.row.selfPickup && !scope.row.delivery" class="grey">未开启</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" min-width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.leaderUid > 0">{{ scope.row.leaderName }}（{{ scope.row.leaderUid }}）</span>
            <span v-else class="grey">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template slot-scope="scope">
            <span class="st-dot" :class="{ on: scope.row.isShow }"><i></i>{{ scope.row.isShow ? '启用' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <div class="op-links">
              <template v-if="checkPermi(['admin:merchant:store:update'])">
                <a class="op-link" @click="openEdit(scope.row)">修改</a>
                <el-divider direction="vertical"></el-divider>
                <a class="op-link" @click="onStatus(scope.row)">{{ scope.row.isShow ? '禁用' : '启用' }}</a>
                <el-divider direction="vertical"></el-divider>
              </template>
              <a v-if="checkPermi(['admin:merchant:store:delete'])" class="op-link" @click="onDelete(scope.row)">删除</a>
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
        <el-form-item label="负责人UID">
          <el-input v-model.number="editForm.leaderUid" placeholder="会员用户UID，0或留空=未绑定" style="width: 200px" />
          <span class="switch-tip">绑定后负责人可在会员中心进入门店中心管理本店</span>
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
  </div>
</template>

<script>
import { merchantStoreListApi, merchantStoreInfoApi, merchantStoreSaveApi, merchantStoreUpdateApi, merchantStoreStatusApi, merchantStoreDeleteApi } from '@/api/merchantStore';
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
      editForm: this.defaultForm()
    };
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
          this.editVisible = true;
        });
      } else {
        this.editForm = this.defaultForm();
        this.editVisible = true;
      }
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
/* 列表页通用规范（.toolbar/.pager/.admin-table/.op-links/.st-dot）已统一在 theme/styles.scss 全局定义 */
.grey { color: #999; font-size: 12px; }
.tc { text-align: center; }
.switch-tip { margin-left: 10px; color: #999; font-size: 12px; }
.store-name { font-size: 13px; font-weight: 600; color: #303133; line-height: 18px; }
.store-addr { font-size: 12px; color: #606266; line-height: 16px; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.svc-tag { display: inline-block; font-size: 11px; line-height: 18px; padding: 0 6px; border-radius: 3px; margin-right: 6px; white-space: nowrap; }
.svc-pickup { background: #e8f8f0; color: #0f9a58; }
.svc-delivery { background: #e8f1ff; color: #0256ff; }
</style>
