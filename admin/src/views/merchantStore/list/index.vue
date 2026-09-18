<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
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
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button v-if="checkPermi(['admin:merchant:store:save'])" type="success" @click="openEdit()">新增门店</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="name" label="门店名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="address" label="门店地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="115" />
        <el-table-column prop="dayTime" label="营业时间" width="110" />
        <el-table-column label="履约服务" width="130">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.selfPickup" size="mini" type="success" class="mr4">自提</el-tag>
            <el-tag v-if="scope.row.delivery" size="mini" type="primary" class="mr4">配送</el-tag>
            <span v-if="!scope.row.selfPickup && !scope.row.delivery" class="grey">未开启</span>
          </template>
        </el-table-column>
        <el-table-column label="配送半径" width="90">
          <template slot-scope="scope">{{ scope.row.deliveryRadius }}km</template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.leaderUid > 0">{{ scope.row.leaderName }}（{{ scope.row.leaderUid }}）</span>
            <span v-else class="grey">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isShow ? 'success' : 'info'" size="mini">{{ scope.row.isShow ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <div class="op-wrap">
              <el-button v-if="checkPermi(['admin:merchant:store:update'])" class="op-btn" type="primary" plain size="mini" @click="openEdit(scope.row)">修改</el-button>
              <el-button v-if="checkPermi(['admin:merchant:store:update'])" class="op-btn" type="warning" plain size="mini" @click="onStatus(scope.row)">{{ scope.row.isShow ? '禁用' : '启用' }}</el-button>
              <el-button v-if="checkPermi(['admin:merchant:store:delete'])" class="op-btn" type="danger" plain size="mini" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
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
.mr4 { margin-right: 4px; }
.grey { color: #999; }
.tc { text-align: center; }
.switch-tip { margin-left: 10px; color: #999; font-size: 12px; }
.op-wrap { display: flex; align-items: center; gap: 10px; padding: 2px 0; }
.op-btn { margin: 0 !important; padding: 5px 14px; font-size: 12px; line-height: 1; border-radius: 4px; }
</style>
