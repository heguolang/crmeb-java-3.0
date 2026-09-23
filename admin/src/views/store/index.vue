<template>
  <div class="divBox relative">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small">
          <el-form-item label="商品分类：">
            <el-cascader
              v-model="tableFrom.cateId"
              :options="merCateList"
              :props="props"
              clearable
              class="selWidth"
              @change="seachList"
              size="small"
            />
          </el-form-item>
          <el-form-item label="商品搜索：">
            <el-input
              v-model="tableFrom.keywords"
              placeholder="请输入商品名称，关键字，商品ID"
              class="selWidth"
              size="small"
              clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="seachList" size="small" v-hasPermi="['admin:product:list']"
              >搜索</el-button
            >
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <div slot="header" class="clearfix">
        <el-tabs v-model="tableFrom.type" @tab-click="seachList" v-if="checkPermi(['admin:product:tabs:headers'])">
          <el-tab-pane
            :label="item.name + '(' + item.count + ')'"
            :name="item.type.toString()"
            v-for="(item, index) in headeNum"
            :key="index"
          />
        </el-tabs>
        <router-link :to="{ path: '/store/list/creatProduct' }">
          <el-button type="primary" class="mr14" v-hasPermi="['admin:product:save']">添加商品</el-button>
        </router-link>
        <!-- 商品采集入口已按需求隐藏（2026-09-17），恢复时取消下行注释 -->
        <!-- <el-button type="success" @click="onCopy" v-hasPermi="['admin:product:save']">商品采集</el-button> -->
        <el-button
          class="mr14"
          :disabled="!selectedIds.length"
          v-hasPermi="['admin:store:product:group:update']"
          @click="openBatchGroup"
        >批量分组{{ selectedIds.length ? `（${selectedIds.length}）` : '' }}</el-button>
        <el-button @click="exports" v-hasPermi="['admin:export:excel:product']">导出</el-button>
      </div>
      <!-- 商品列表（参考图样式：浅蓝表头 + 数据行） -->
      <div class="list-table" v-loading="listLoading">
        <div class="list-head">
          <div class="list-cell cell-check">
            <el-checkbox
              :indeterminate="isIndeterminate"
              v-model="checkAll"
              @change="handleCheckAll"
            />
          </div>
          <div class="list-cell cell-sort">排序</div>
          <div class="list-cell">图片</div>
          <div class="list-cell">商品信息</div>
          <div class="list-cell">售价</div>
          <div class="list-cell">商品数据</div>
          <div class="list-cell cell-state">状态</div>
          <div class="list-cell cell-ops">操作</div>
        </div>
        <div class="list-body">
          <div v-for="row in tableData.data" :key="row.id" class="list-row">
            <div class="list-cell cell-check">
              <el-checkbox
                :value="selectedIds.includes(row.id)"
                @change="(val) => toggleSelect(row.id, val)"
              />
            </div>
            <div class="list-cell cell-sort"><span class="sort-num">{{ row.sort }}</span></div>
            <div class="list-cell">
              <el-image class="goods-img" :src="row.image" :preview-src-list="[row.image]" fit="cover" />
            </div>
            <div class="list-cell">
              <div class="goods-name" :title="row.storeName">{{ row.storeName }}</div>
              <div class="goods-tags">
                <span v-for="(c, ci) in cateList(row.cateValues)" :key="ci" class="mini-chip">{{ c }}</span>
              </div>
              <div class="sub-text">商品编号：ID {{ row.id }}</div>
            </div>
            <div class="list-cell">
              <div class="irow"><span class="il">售价：</span><span class="iv">￥{{ fmtMoney(row.price) }}</span></div>
              <div class="irow"><span class="il">市场价：</span><span class="iv">￥{{ fmtMoney(row.otPrice) }}</span></div>
              <div class="irow"><span class="il">成本价：</span><span class="iv">￥{{ fmtMoney(row.cost) }}</span></div>
            </div>
            <div class="list-cell">
              <div class="irow"><span class="il">销量：</span><span class="iv">{{ row.sales || 0 }}</span></div>
              <div class="irow"><span class="il">库存：</span><span class="iv">{{ row.stock || 0 }}</span></div>
              <div class="irow"><span class="il">收藏：</span><span class="iv">{{ row.collectCount || 0 }}</span></div>
            </div>
            <div class="list-cell cell-state">
              <el-switch
                v-if="checkPermi(['admin:product:up', 'admin:product:down'])"
                :disabled="Number(tableFrom.type) > 2"
                v-model="row.isShow"
                :active-value="true"
                :inactive-value="false"
                @change="onchangeIsShow(row)"
              />
              <span v-else>{{ row.isShow ? '上架' : '下架' }}</span>
            </div>
            <div class="list-cell cell-ops">
              <el-button size="mini" type="primary" plain class="op-btn" v-hasPermi="['admin:product:info']">
                <router-link :to="{ path: '/store/list/creatProduct/' + row.id + '/1' }">详情</router-link>
              </el-button>
              <el-button
                v-if="tableFrom.type !== '5'"
                size="mini" type="primary" plain class="op-btn" v-hasPermi="['admin:product:update']"
              >
                <router-link :to="{ path: '/store/list/creatProduct/' + row.id }">编辑</router-link>
              </el-button>
              <el-button
                v-if="checkPermi(['admin:product:quick:stock:add']) && tableFrom.type != 2 && tableFrom.type != 5"
                size="mini" type="primary" plain class="op-btn" @click="handleStock(row)"
              >编辑库存</el-button>
              <el-button
                v-if="tableFrom.type === '5'"
                size="mini" type="success" plain class="op-btn" v-hasPermi="['admin:product:restore']"
                @click="handleRestore(row.id)"
              >恢复商品</el-button>
              <el-button
                size="mini" type="danger" plain class="op-btn" v-hasPermi="['admin:product:delete']"
                @click="handleDelete(row.id, tableFrom.type)"
              >{{ tableFrom.type === '5' ? '删除' : '回收站' }}</el-button>
            </div>
          </div>
          <div v-if="!tableData.data.length && !listLoading" class="empty-tip">暂无商品</div>
        </div>
      </div>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
          background
        />
      </div>
    </el-card>
    <el-dialog
      title="复制淘宝、天猫、京东、苏宁"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="1200px"
      class="taoBaoModal"
      :before-close="handleClose"
    >
      <tao-bao v-if="dialogVisible" @handleCloseMod="handleCloseMod"></tao-bao>
    </el-dialog>
    <!--编辑库存-->
    <el-drawer
      title="编辑库存"
      :visible.sync="drawer"
      :direction="direction"
      :size="1500"
      class="showHeader"
      :before-close="handleCloseEdit"
    >
      <store-edit @sucess="sucess" :productId="productId" v-if="drawer"></store-edit>
    </el-drawer>

    <el-dialog title="批量加入商品分组" :visible.sync="batchGroupVisible" width="520px" :close-on-click-modal="false">
      <div class="mb10 tip-text">已选 {{ selectedIds.length }} 个商品，将追加加入下方分组（不移除已有其它分组）</div>
      <el-select
        v-model="batchGroupIds"
        multiple
        filterable
        clearable
        placeholder="请选择商品分组"
        style="width: 100%"
      >
        <el-option v-for="g in productGroupOptions" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <div slot="footer">
        <el-button @click="batchGroupVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchGroupSaving" @click="submitBatchGroup">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  productLstApi,
  productDeleteApi,
  categoryApi,
  putOnShellApi,
  offShellApi,
  productHeadersApi,
  productExportApi,
  restoreApi,
  productExcelApi,
} from '@/api/store';
import { productGroupSimpleListApi, productGroupBatchBindApi } from '@/api/productGroup';
import { getToken } from '@/utils/auth';
import storeEdit from './components/storeEdit';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
import { Debounce } from '@/utils/validate.js';
export default {
  name: 'ProductList',
  components: { storeEdit },
  data() {
    return {
      direction: 'rtl',
      props: {
        children: 'child',
        label: 'name',
        value: 'id',
        emitPath: false,
      },
      // roterPre: roterPre,
      headeNum: [],
      listLoading: true,
      tableData: {
        data: [],
        total: 0,
      },
      tableFrom: {
        page: 1,
        limit: 20,
        cateId: '',
        keywords: '',
        type: '1',
      },
      categoryList: [],
      merCateList: [],
      objectUrl: process.env.VUE_APP_BASE_API,
      dialogVisible: false,
      drawer: false,
      productId: 0,
      selectedIds: [],
      checkAll: false,
      isIndeterminate: false,
      batchGroupVisible: false,
      batchGroupIds: [],
      batchGroupSaving: false,
      productGroupOptions: [],
    };
  },
  mounted() {
    this.goodHeade();
    this.getList();
    this.getCategorySelect();
  },
  methods: {
    checkPermi,
    toggleSelect(id, checked) {
      if (checked) {
        if (!this.selectedIds.includes(id)) this.selectedIds.push(id);
      } else {
        this.selectedIds = this.selectedIds.filter((i) => i !== id);
      }
      this.syncCheckAllState();
    },
    handleCheckAll(val) {
      const ids = (this.tableData.data || []).map((r) => r.id);
      if (val) {
        const set = new Set([...this.selectedIds, ...ids]);
        this.selectedIds = Array.from(set);
      } else {
        this.selectedIds = this.selectedIds.filter((id) => !ids.includes(id));
      }
      this.syncCheckAllState();
    },
    syncCheckAllState() {
      const ids = (this.tableData.data || []).map((r) => r.id);
      if (!ids.length) {
        this.checkAll = false;
        this.isIndeterminate = false;
        return;
      }
      const checkedCount = ids.filter((id) => this.selectedIds.includes(id)).length;
      this.checkAll = checkedCount === ids.length;
      this.isIndeterminate = checkedCount > 0 && checkedCount < ids.length;
    },
    clearSelection() {
      this.selectedIds = [];
      this.checkAll = false;
      this.isIndeterminate = false;
    },
    openBatchGroup() {
      if (!this.selectedIds.length) {
        this.$message.warning('请先勾选商品');
        return;
      }
      this.batchGroupIds = [];
      productGroupSimpleListApi()
        .then((res) => {
          this.productGroupOptions = Array.isArray(res) ? res : res.list || [];
          this.batchGroupVisible = true;
        })
        .catch(() => {
          this.productGroupOptions = [];
          this.batchGroupVisible = true;
        });
    },
    submitBatchGroup() {
      if (!this.batchGroupIds.length) {
        this.$message.warning('请选择商品分组');
        return;
      }
      this.batchGroupSaving = true;
      productGroupBatchBindApi({
        productIds: this.selectedIds,
        groupIds: this.batchGroupIds,
      })
        .then(() => {
          this.$message.success('已加入分组');
          this.batchGroupVisible = false;
          this.clearSelection();
          this.batchGroupSaving = false;
        })
        .catch(() => {
          this.batchGroupSaving = false;
        });
    },
    // ===== 列表辅助（参考图样式） =====
    fmtMoney(v) {
      const n = Number(v || 0);
      return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    // 分类文本拆 chip（cateValues 形如 "电子,数码"）
    cateList(v) {
      return v ? String(v).split(',').filter(Boolean) : [];
    },
    sucess() {
      this.$message.success('保存成功');
      this.drawer = false;
      this.getList();
      this.goodHeade();
    },
    handleCloseEdit() {
      this.drawer = false;
    },
    handleStock(row) {
      this.productId = row.id;
      this.drawer = true;
    },
    //重置
    handleReset() {
      this.tableFrom.cateId = '';
      this.tableFrom.keywords = '';
      this.goodHeade();
      this.getList();
    },
    // 恢复商品
    handleRestore: Debounce(function (id) {
      this.$modalSure('恢复商品').then(() => {
        restoreApi(id).then((res) => {
          this.$message.success('操作成功');
          this.goodHeade();
          this.getList();
        });
      });
    }),
    seachList() {
      this.tableFrom.page = 1;
      this.clearSelection();
      this.getList();
      this.goodHeade();
    },
    handleClose() {
      this.dialogVisible = false;
    },
    handleCloseMod(item) {
      this.dialogVisible = item;
      this.goodHeade();
      this.getList();
    },
    // 复制
    onCopy() {
      this.$router.push({
        name: 'SortCreat',
        params: {
          isCopy: 1,
        },
      });
    },
    // 导出
    exports() {
      productExcelApi({
        cateId: this.tableFrom.cateId,
        keywords: this.tableFrom.keywords,
        type: this.tableFrom.type,
      }).then((res) => {
        window.location.href = res.fileName;
      });
    },
    // 获取商品表单头数量
    goodHeade() {
      productHeadersApi(this.tableFrom)
        .then((res) => {
          this.headeNum = res;
        })
        .catch((res) => {
          this.$message.error(res.message);
        });
    },
    // 商户分类；
    getCategorySelect() {
      categoryApi({ status: -1, type: 1 })
        .then((res) => {
          this.merCateList = res;
        })
        .catch((res) => {
          this.$message.error(res.message);
        });
    },
    // 列表
    getList() {
      this.listLoading = true;
      productLstApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.listLoading = false;
          this.syncCheckAllState();
        })
        .catch((res) => {
          this.listLoading = false;
          this.$message.error(res.message);
        });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.clearSelection();
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.clearSelection();
      this.getList();
    },
    // 删除
    handleDelete(id, type) {
      this.$modalSure(
        this.tableFrom.type === '5' ? `删除 id 为 ${id} 的商品吗？` : `将 id 为 ${id} 的商品加入回收站吗？`,
      ).then(() => {
        const deleteFlag = type == 5 ? 'delete' : 'recycle';
        productDeleteApi(id, deleteFlag).then(() => {
          this.$message.success('操作成功');
          if (this.tableData.data.length === 1 && this.tableFrom.page > 1)
            this.tableFrom.page = this.tableFrom.page - 1;
          this.getList();
          this.goodHeade();
        });
      });
    },
    onchangeIsShow(row) {
      row.isShow
        ? putOnShellApi(row.id)
            .then(() => {
              this.$message.success('上架成功');
              this.getList();
              this.goodHeade();
            })
            .catch(() => {
              row.isShow = !row.isShow;
            })
        : offShellApi(row.id)
            .then(() => {
              this.$message.success('下架成功');
              this.getList();
              this.goodHeade();
            })
            .catch(() => {
              row.isShow = !row.isShow;
            });
    },
  },
};
</script>

<style scoped lang="scss">
/* ===== 商品列表（参考图样式：浅蓝表头 + 数据行） ===== */
.sub-text {
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}
.list-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  min-height: 120px;
}

/* 表头 */
.list-head {
  display: grid;
  grid-template-columns: 44px 56px 80px minmax(220px, 2.45fr) minmax(120px, 1fr) minmax(125px, 1fr) minmax(60px, 0.5fr) minmax(125px, 1fr);
  align-items: center;
  height: 46px;
  background: #ecf3fd;
}
.list-head .list-cell {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding: 0 16px;
  white-space: nowrap;
}
.list-head .cell-sort,
.list-head .cell-check {
  padding: 0 8px;
  text-align: center;
}

/* 数据行 */
.list-row {
  display: grid;
  grid-template-columns: 44px 56px 80px minmax(220px, 2.45fr) minmax(120px, 1fr) minmax(125px, 1fr) minmax(60px, 0.5fr) minmax(125px, 1fr);
  align-items: center;
  border-top: 1px solid #f0f2f5;
  transition: background 0.15s;
}
.list-row:hover {
  background: #fafcff;
}
.list-cell {
  padding: 12px 16px;
  min-width: 0;
  align-self: center;
}
.cell-sort,
.cell-check {
  text-align: center;
  padding-left: 8px;
  padding-right: 8px;
}
.tip-text {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.mb10 {
  margin-bottom: 10px;
}
.sort-num {
  display: inline-block;
  min-width: 40px;
  padding: 2px 8px;
  border: 1px solid #ebeef5;
  border-radius: 3px;
  background: #fafbfc;
  font-size: 13px;
  color: #606266;
  font-variant-numeric: tabular-nums;
}

/* 图片 */
.goods-img {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  background: #f5f7fa;
  display: block;
}

/* 商品信息列 */
.goods-name {
  font-size: 14px;
  color: #303133;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.goods-tags {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.mini-chip {
  padding: 0 8px;
  border-radius: 3px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  color: #909399;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}
.goods-name + .goods-tags + .sub-text {
  margin-top: 6px;
}

/* 键值行（售价 / 商品数据列）：label 固定宽，value 紧贴左对齐
   注意不要用 .kv —— 那是全局基座 list-page.scss 的两端对齐布局，会撞名 */
.irow {
  display: flex;
  align-items: baseline;
  font-size: 13px;
  line-height: 24px;
  margin: 3px 0;
}
.irow .il {
  color: #909399;
  flex-shrink: 0;
  width: 58px;
  text-align: justify;
  text-align-last: justify;
}
.irow .iv {
  color: #303133;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
  margin-left: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 状态列开关居中 */
.cell-state {
  text-align: center;
}

/* 操作列：按钮竖排 */
.cell-ops {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.op-btn {
  min-width: 76px;
  margin: 3px 0 !important;
  margin-left: 0 !important;
  display: block;
}
.op-btn a {
  color: inherit;
  text-decoration: none;
}
.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 32px 0;
}

.taoBaoModal {
  //  z-index: 3333 !important;
}

::v-deep .el-drawer__header {
  padding-bottom: 20px !important;
  border-bottom: 1px solid #eee !important;
  font-size: 16px;
}
</style>
