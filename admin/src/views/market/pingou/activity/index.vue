<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条 -->
      <div class="summary-bar">目前有 <b>{{ total }}</b> 个拼购活动。</div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="关键字">
            <el-input v-model="tableFrom.keywords" placeholder="活动名称/商品名称" clearable style="width: 200px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="tableFrom.status" placeholder="所有状态" clearable style="width: 120px">
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button v-if="checkPermi(['admin:pingou:activity:save'])" type="primary" plain icon="el-icon-plus" @click="openEdit()">添加拼购活动</el-button>
        </div>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column width="30" />
        <el-table-column label="活动名称" min-width="180">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="拼购商品" min-width="180">
          <template slot-scope="scope">
            <div class="goods-cell">
              <img v-if="scope.row.image" :src="scope.row.image" class="goods-img" />
              <div>
                <div class="info-v">{{ scope.row.productName }}</div>
                <div class="info-line">￥{{ scope.row.productPrice }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成团/中奖" width="100" align="center">
          <template slot-scope="scope">
            <span class="info-v">{{ scope.row.totalNum }}人 / 中{{ scope.row.winNum }}人</span>
          </template>
        </el-table-column>
        <el-table-column label="未中奖补贴" width="100" align="center">
          <template slot-scope="scope">
            <span class="info-v">￥{{ scope.row.refundSubsidy }} /人</span>
          </template>
        </el-table-column>
        <el-table-column label="分享奖励" min-width="140">
          <template slot-scope="scope">
            <div class="info-line">
              {{ scope.row.shareRateLow }}‰ ~ {{ scope.row.shareRateHigh }}‰
            </div>
            <div class="info-line">直推满{{ scope.row.shareRateThreshold }}人享高比例</div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="300" class-name="time-cell">
          <template slot-scope="scope">
            <div class="info-line">开始：{{ fmtTime(scope.row.startTime) }}</div>
            <div class="info-line">结束：{{ fmtTime(scope.row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="scope">
            <span class="st-dot st-dot-lg" :class="{ on: scope.row.status }"><i></i>{{ scope.row.status ? '启用' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid">
              <el-button v-if="checkPermi(['admin:pingou:activity:update'])" size="mini" plain class="op-tag tint-primary" @click="openEdit(scope.row)">修改</el-button>
              <el-button v-if="checkPermi(['admin:pingou:activity:status'])" size="mini" plain class="op-tag tint-warn" @click="onStatus(scope.row)">{{ scope.row.status ? '禁用' : '启用' }}</el-button>
              <el-button v-if="checkPermi(['admin:pingou:activity:delete'])" size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="150" />
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="editForm.id ? '修改拼购活动' : '添加拼购活动'" :visible.sync="editVisible" width="560px" top="6vh">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="110px" size="small">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="editForm.name" placeholder="例如：国庆活动" style="width: 320px" />
        </el-form-item>
        <el-form-item label="拼购商品" prop="productId">
          <el-input :value="productLabel" placeholder="请选择拼购商品（中奖者所得商品）" readonly style="width: 320px">
            <template slot="append">
              <el-button @click="openProductPicker">选择商品</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="活动时间" prop="startTime">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            value-format="yyyy-MM-dd HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item label="成团人数" prop="totalNum">
          <el-input-number v-model="editForm.totalNum" :min="2" :max="9999" style="width: 150px" />
          <span class="form-tip">人（满员即开奖）</span>
        </el-form-item>
        <el-form-item label="中奖人数" prop="winNum">
          <el-input-number v-model="editForm.winNum" :min="1" :max="editForm.totalNum ? editForm.totalNum - 1 : 1" style="width: 150px" />
          <span class="form-tip">人（随机抽取，中奖得商品）</span>
        </el-form-item>
        <el-form-item label="未中奖补贴">
          <el-input-number v-model="editForm.refundSubsidy" :min="0" :precision="2" style="width: 150px" />
          <span class="form-tip">元/人（发放到佣金账户，本金自动退回余额）</span>
        </el-form-item>
        <el-form-item label="分享奖励(低)">
          <el-input-number v-model="editForm.shareRateLow" :min="0" :precision="2" style="width: 150px" />
          <span class="form-tip">‰（直推未达阈值时）</span>
        </el-form-item>
        <el-form-item label="分享奖励(高)">
          <el-input-number v-model="editForm.shareRateHigh" :min="0" :precision="2" style="width: 150px" />
          <span class="form-tip">‰（直推达到阈值时）</span>
        </el-form-item>
        <el-form-item label="高比例阈值">
          <el-input-number v-model="editForm.shareRateThreshold" :min="1" style="width: 150px" />
          <span class="form-tip">人（直推参与满 N 人享高比例）</span>
        </el-form-item>
        <el-form-item label="限购数量">
          <el-input-number v-model="editForm.limitNum" :min="0" style="width: 150px" />
          <span class="form-tip">件/人（0 = 不限）</span>
        </el-form-item>
        <el-form-item label="是否展示">
          <el-switch v-model="editForm.isShow" :active-value="true" :inactive-value="false" />
          <span class="form-tip">在会员端拼购列表展示</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" style="width: 150px" />
          <span class="form-tip">数字越大越靠前</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 选择商品弹窗 -->
    <el-dialog title="选择拼购商品" :visible.sync="productPickerVisible" width="640px">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item>
          <el-input v-model="productKeyword" placeholder="商品名称关键字" clearable style="width: 240px" @keyup.enter.native="loadProducts(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadProducts(1)">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="productLoading" :data="productList" size="small" max-height="360">
        <el-table-column label="商品" min-width="260">
          <template slot-scope="scope">
            <div class="goods-cell">
              <img v-if="scope.row.image" :src="scope.row.image" class="goods-img" />
              <span class="info-v">{{ scope.row.storeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100" align="center">
          <template slot-scope="scope">￥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="pickProduct(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="productFrom.limit" :current-page="productFrom.page" layout="total, prev, pager, next" :total="productTotal" @current-change="loadProducts" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pingouActivityListApi, pingouActivitySaveApi, pingouActivityUpdateApi, pingouActivityStatusApi, pingouActivityDeleteApi, pingouProductListApi } from '@/api/pingou';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'PingouActivity',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, keywords: '', status: null },
      editVisible: false,
      dateRange: null,
      editForm: this.emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
        productId: [{ required: true, message: '请选择拼购商品', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
      },
      // 商品选择
      productPickerVisible: false,
      productLoading: false,
      productList: [],
      productTotal: 0,
      productKeyword: '',
      productFrom: { page: 1, limit: 10 },
      pickedProduct: null
    };
  },
  computed: {
    productLabel() {
      if (this.editForm.productId && this.pickedProduct) {
        return this.pickedProduct.storeName + '（￥' + this.pickedProduct.price + '）';
      }
      return '';
    }
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    emptyForm() {
      return {
        id: null,
        name: '',
        image: '',
        productId: null,
        startTime: null,
        endTime: null,
        totalNum: 10,
        winNum: 2,
        refundSubsidy: 0,
        shareRateLow: 5,
        shareRateHigh: 10,
        shareRateThreshold: 10,
        limitNum: 0,
        isShow: true,
        sort: 0,
        status: true
      };
    },
    getList() {
      this.loading = true;
      pingouActivityListApi(this.tableFrom)
        .then((res) => {
          this.tableData = res.data.list || [];
          this.total = res.data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    reset() {
      this.tableFrom = { page: 1, limit: 20, keywords: '', status: null };
      this.getList();
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    fmtTime(t) {
      return t ? String(t).slice(0, 16) : '—';
    },
    openEdit(row) {
      if (row) {
        const form = { ...this.emptyForm(), ...row };
        delete form.createTime;
        delete form.updateTime;
        delete form.isDel;
        delete form.productName;
        delete form.productPrice;
        this.editForm = form;
        this.pickedProduct = { storeName: row.productName, price: row.productPrice };
        this.dateRange = [row.startTime, row.endTime];
      } else {
        this.editForm = this.emptyForm();
        this.pickedProduct = null;
        this.dateRange = null;
      }
      this.editVisible = true;
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate();
      });
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        if (!this.dateRange || !this.dateRange[0] || !this.dateRange[1]) {
          this.$message.error('请选择活动时间');
          return;
        }
        const data = { ...this.editForm, startTime: this.dateRange[0], endTime: this.dateRange[1] };
        this.saving = true;
        const req = data.id ? pingouActivityUpdateApi(data) : pingouActivitySaveApi(data);
        req
          .then(() => {
            this.$message.success(data.id ? '修改成功' : '添加成功');
            this.editVisible = false;
            this.getList();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onStatus(row) {
      pingouActivityStatusApi(row.id, !row.status).then(() => {
        this.$message.success(!row.status ? '已启用' : '已禁用');
        this.getList();
      });
    },
    onDelete(row) {
      this.$confirm('删除后会员端不再展示该活动，确认删除？', '提示', { type: 'warning' }).then(() => {
        pingouActivityDeleteApi(row.id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
    // ---------- 商品选择 ----------
    openProductPicker() {
      this.productPickerVisible = true;
      if (!this.productList.length) this.loadProducts(1);
    },
    loadProducts(page) {
      if (page) this.productFrom.page = page;
      this.productLoading = true;
      pingouProductListApi({ page: this.productFrom.page, limit: this.productFrom.limit, keywords: this.productKeyword })
        .then((res) => {
          this.productList = (res.data.list || []).filter((p) => !p.isDel && p.isShow);
          this.productTotal = res.data.total || 0;
        })
        .finally(() => {
          this.productLoading = false;
        });
    },
    pickProduct(row) {
      this.pickedProduct = row;
      this.editForm.productId = row.id;
      this.productPickerVisible = false;
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
  }
}
.goods-cell {
  display: flex;
  align-items: center;
  .goods-img {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    margin-right: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }
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
.info-v {
  font-size: 13px;
  color: #303133;
}
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
