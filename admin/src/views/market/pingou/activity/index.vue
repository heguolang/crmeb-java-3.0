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
        <el-table-column label="分享奖励" min-width="150">
          <template slot-scope="scope">
            <div v-for="(line, i) in tierLines(scope.row)" :key="i" class="info-line">
              {{ line }}
            </div>
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
    <el-dialog :title="editForm.id ? '修改拼购活动' : '添加拼购活动'" :visible.sync="editVisible" width="640px" top="6vh">
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="88px" label-position="left" size="small" class="edit-form">
        <!-- 基本信息 -->
        <div class="form-group">
          <div class="group-title">基本信息</div>
          <el-form-item label="活动名称" prop="name">
            <el-input v-model="editForm.name" placeholder="例如：国庆活动" />
          </el-form-item>
          <el-form-item label="拼购商品" prop="productId">
            <el-input :value="productLabel" placeholder="请选择拼购商品（中奖者所得商品）" readonly>
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
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 开奖与补贴 -->
        <div class="form-group">
          <div class="group-title">开奖与补贴</div>
          <div class="half-row">
            <el-form-item label="成团人数" prop="totalNum">
              <div class="num-cell">
                <el-input-number v-model="editForm.totalNum" :min="2" :max="9999" style="width: 110px" />
                <span class="unit">人</span>
              </div>
            </el-form-item>
            <el-form-item label="中奖人数" prop="winNum">
              <div class="num-cell">
                <el-input-number v-model="editForm.winNum" :min="1" :max="editForm.totalNum ? editForm.totalNum - 1 : 1" style="width: 110px" />
                <span class="unit">人</span>
              </div>
            </el-form-item>
          </div>
          <div class="group-tip">成团即开奖 · 中奖者随机抽取、获得拼购商品</div>
          <el-form-item label="未中奖补贴">
            <div class="num-cell">
              <el-input-number v-model="editForm.refundSubsidy" :min="0" :precision="2" style="width: 110px" />
              <span class="unit">元/人</span>
            </div>
            <div class="field-tip">发放到佣金账户，本金自动退回余额</div>
          </el-form-item>
        </div>

        <!-- 分享奖励 -->
        <div class="form-group">
          <div class="group-title">分享奖励</div>
          <template v-if="levelRows.length">
            <div v-for="lv in levelRows" :key="lv.id" class="level-card">
              <div class="level-head">
                <div class="level-name">{{ lv.name }}（直推≥{{ lv.minDirectNum }}人）</div>
                <el-radio-group v-model="lv.mode" size="mini">
                  <el-radio label="default">跟随默认（{{ lv.rewardRate }}%）</el-radio>
                  <el-radio label="custom">独立设置</el-radio>
                </el-radio-group>
              </div>
              <div v-if="lv.mode === 'custom'" class="level-body">
                <el-select v-model="lv.rewardType" size="mini" style="width: 130px">
                  <el-option label="比例（%）" :value="1" />
                  <el-option label="固定金额（元）" :value="2" />
                </el-select>
                <el-input-number v-model="lv.rewardValue" :min="0" :precision="2" size="mini" style="width: 110px" />
              </div>
            </div>
            <div class="field-tip block-tip">
              每个等级可选「跟随默认」（按拼购等级设置的比例）或「独立设置」本活动专属奖励；结算按直推人数匹配满足条件的最高一档
            </div>
            <div v-if="editForm.legacyTiers && editForm.legacyTiers.length" class="field-tip block-tip">
              另有 {{ editForm.legacyTiers.length }} 条历史阶梯配置将继续生效
            </div>
          </template>
          <template v-else>
            <div v-for="(t, i) in editForm.shareTiers" :key="i" class="tier-row">
              <span class="tier-label">直推</span>
              <el-input-number v-model="t.minDirectNum" :min="1" :precision="0" style="width: 110px" />
              <span class="tier-label">人（含）以上</span>
              <el-select v-model="t.rewardType" style="width: 130px">
                <el-option label="比例（%）" :value="1" />
                <el-option label="固定金额（元）" :value="2" />
              </el-select>
              <el-input-number v-model="t.rewardValue" :min="0" :precision="2" style="width: 120px" />
              <el-button size="mini" type="danger" plain icon="el-icon-delete" circle @click="removeTier(i)" />
            </div>
            <div class="tier-add">
              <el-button size="mini" type="primary" plain icon="el-icon-plus" @click="addTier">添加阶梯</el-button>
              <span class="field-tip">尚未设置拼购等级：建议先到「大市场 → 拼购等级」添加等级，再按等级设置分享奖励</span>
            </div>
          </template>
        </div>

        <!-- 其他设置 -->
        <div class="form-group">
          <div class="group-title">其他设置</div>
          <div class="half-row">
            <el-form-item label="限购数量">
              <div class="num-cell">
                <el-input-number v-model="editForm.limitNum" :min="0" style="width: 110px" />
                <span class="unit-tip">0=不限</span>
              </div>
            </el-form-item>
            <el-form-item label="排序">
              <div class="num-cell">
                <el-input-number v-model="editForm.sort" :min="0" style="width: 110px" />
                <span class="unit-tip">越大越靠前</span>
              </div>
            </el-form-item>
          </div>
          <el-form-item label="是否展示">
            <div class="num-cell">
              <el-switch v-model="editForm.isShow" :active-value="true" :inactive-value="false" />
              <span class="unit-tip">在会员端拼购列表展示</span>
            </div>
          </el-form-item>
        </div>
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
import { pingouActivityListApi, pingouActivitySaveApi, pingouActivityUpdateApi, pingouActivityStatusApi, pingouActivityDeleteApi, pingouProductListApi, pingouLevelListApi } from '@/api/pingou';
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
      // 拼购等级行（表单分享奖励区按等级逐行渲染；mode: default=跟随默认 custom=独立设置）
      levelRows: [],
      // 列表展示用的等级默认比例
      listLevels: [],
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
    this.loadListLevels();
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
        shareTiers: [],
        legacyTiers: [],
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
          this.tableData = res.list || [];
          this.total = res.total || 0;
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
      // 先拉最新等级（分享奖励按等级逐行渲染），再组装表单
      pingouLevelListApi()
        .then((res) => {
          const levels = Array.isArray(res) ? res : res.list || [];
          this.levelRows = levels
            .slice()
            .sort((a, b) => (a.minDirectNum || 0) - (b.minDirectNum || 0))
            .map((l) => ({
              id: l.id,
              name: l.name,
              minDirectNum: l.minDirectNum || 1,
              rewardRate: Number(l.rewardRate) || 0,
              mode: 'default',
              rewardType: 1,
              rewardValue: Number(l.rewardRate) || 0
            }));
          const form = { ...this.emptyForm(), ...(row || {}) };
          const tiers = row ? this.parseTiers(row.shareTiers, row) : [];
          if (row) {
            delete form.createTime;
            delete form.updateTime;
            delete form.isDel;
            delete form.productName;
            delete form.productPrice;
            delete form.shareRateLow;
            delete form.shareRateHigh;
            delete form.shareRateThreshold;
            delete form.shareTiersJson;
            this.pickedProduct = { storeName: row.productName, price: row.productPrice };
            this.dateRange = [row.startTime, row.endTime];
          } else {
            this.pickedProduct = null;
            this.dateRange = null;
          }
          if (this.levelRows.length) {
            // 等级模式：已保存的独立档归位到对应等级；旧数据（无 levelId）按直推人数匹配；匹配不上的留作历史阶梯
            const byLevel = {};
            const legacy = [];
            (tiers || []).forEach((t) => {
              if (t.levelId != null && this.levelRows.some((l) => l.id === t.levelId)) {
                byLevel[t.levelId] = t;
              } else {
                const lv = this.levelRows.find((l) => l.minDirectNum === t.minDirectNum);
                if (lv && !byLevel[lv.id]) {
                  byLevel[lv.id] = t;
                } else {
                  legacy.push(t);
                }
              }
            });
            this.levelRows.forEach((lv) => {
              const t = byLevel[lv.id];
              if (t) {
                lv.mode = 'custom';
                lv.rewardType = t.rewardType || 1;
                lv.rewardValue = Number(t.rewardValue) || 0;
              }
            });
            form.legacyTiers = legacy;
            form.shareTiers = [];
          } else {
            // 无等级：退回自由阶梯模式，保证旧数据可编辑
            form.shareTiers = tiers;
            form.legacyTiers = [];
          }
          this.editForm = form;
          this.editVisible = true;
          this.$nextTick(() => {
            this.$refs.editForm && this.$refs.editForm.clearValidate();
          });
        })
        .catch(() => {});
    },
    // ---------- 分享奖励阶梯 ----------
    parseTiers(json, row) {
      let tiers = [];
      if (json) {
        try {
          tiers = JSON.parse(json) || [];
        } catch (e) {
          tiers = [];
        }
      }
      if (!tiers.length && row && (row.shareRateLow || row.shareRateHigh)) {
        // 旧数据迁移：低/高‰ + 阈值 → 两档（‰ 换算为 %）
        if (row.shareRateLow) tiers.push({ minDirectNum: 1, rewardType: 1, rewardValue: +(row.shareRateLow / 10).toFixed(2) });
        if (row.shareRateHigh) tiers.push({ minDirectNum: row.shareRateThreshold || 10, rewardType: 1, rewardValue: +(row.shareRateHigh / 10).toFixed(2) });
      }
      return tiers;
    },
    tierLines(row) {
      const tiers = this.parseTiers(row.shareTiers, row);
      if (!tiers.length) return ['按默认等级比例'];
      const unit = (t) => (t.rewardType === 2 ? '元' : '%');
      const hasLevel = tiers.some((t) => t.levelId != null);
      if (hasLevel && this.listLevels.length) {
        const byLevel = {};
        tiers.forEach((t) => {
          if (t.levelId != null) byLevel[t.levelId] = t;
        });
        const lines = this.listLevels.map((lv) => {
          const t = byLevel[lv.id];
          return t ? `${lv.name}：独立 ${t.rewardValue}${unit(t)}` : `${lv.name}：默认 ${lv.rewardRate}%`;
        });
        tiers.forEach((t) => {
          if (t.levelId == null) lines.push(`直推≥${t.minDirectNum}人：${t.rewardValue}${unit(t)}`);
        });
        return lines;
      }
      return tiers.map((t) => `直推≥${t.minDirectNum}人：${t.rewardValue}${unit(t)}`);
    },
    loadListLevels() {
      pingouLevelListApi()
        .then((res) => {
          const levels = Array.isArray(res) ? res : res.list || [];
          this.listLevels = levels.slice().sort((a, b) => (a.minDirectNum || 0) - (b.minDirectNum || 0));
        })
        .catch(() => {});
    },
    addTier() {
      this.editForm.shareTiers.push({ minDirectNum: 1, rewardType: 1, rewardValue: 0.5 });
    },
    removeTier(i) {
      this.editForm.shareTiers.splice(i, 1);
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        if (!this.dateRange || !this.dateRange[0] || !this.dateRange[1]) {
          this.$message.error('请选择活动时间');
          return;
        }
        // 组装分享奖励：等级模式 → 每个独立设置的等级一条（带 levelId），跟随默认的等级不传（结算走默认比例）
        let shareTiers;
        if (this.levelRows.length) {
          shareTiers = this.levelRows
            .filter((l) => l.mode === 'custom')
            .map((l) => ({ levelId: l.id, minDirectNum: l.minDirectNum, rewardType: l.rewardType, rewardValue: l.rewardValue }))
            .concat(this.editForm.legacyTiers || []);
        } else {
          shareTiers = this.editForm.shareTiers;
        }
        const data = { ...this.editForm, shareTiers, startTime: this.dateRange[0], endTime: this.dateRange[1] };
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
      pingouProductListApi({ page: this.productFrom.page, limit: this.productFrom.limit, type: 1, keywords: this.productKeyword })
        .then((res) => {
          this.productList = (res.list || []).filter((p) => !p.isDel && p.isShow);
          this.productTotal = res.total || 0;
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
.tier-default {
  color: #1f8dd6;
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
/* ---------- 弹窗表单：四组分档（按图纸：640px / 88px 左置标签 / 并排半列） ---------- */
.edit-form {
  .group-title {
    font-size: 13px;
    font-weight: 600;
    color: #1f5fd6;
    line-height: 18px;
    margin: 0 0 16px;
  }
  .form-group {
    & + .form-group {
      margin-top: 20px;
    }
    ::v-deep .el-form-item {
      margin-bottom: 16px;
    }
    ::v-deep .el-form-item__label {
      font-size: 13px;
      color: #606266;
      padding-right: 12px;
      line-height: 32px;
    }
  }
  /* 并排半列：成团+中奖、限购+排序 */
  .half-row {
    display: flex;
    gap: 24px;
    ::v-deep .el-form-item {
      flex: 1;
      min-width: 0;
    }
  }
  .group-tip {
    font-size: 12px;
    color: #909399;
    line-height: 18px;
    margin: -6px 0 16px;
  }
  .num-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    .unit {
      font-size: 13px;
      color: #606266;
      white-space: nowrap;
    }
    .unit-tip {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
    }
  }
  .field-tip {
    font-size: 12px;
    color: #909399;
    line-height: 18px;
    margin-top: 2px;
    &.block-tip {
      margin: 2px 0 8px;
    }
  }
  .level-card {
    background: #f7f8fa;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 10px;
    .level-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 8px;
      .level-name {
        font-size: 13px;
        font-weight: 500;
        color: #303133;
      }
    }
    .level-body {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 10px;
    }
  }
  .tier-add {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
.pager {
  margin-top: 16px;
  text-align: right;
}
.tier-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  .tier-label {
    margin: 0 6px;
    font-size: 13px;
    color: #606266;
    white-space: nowrap;
  }
  .el-input-number,
  .el-select {
    margin-right: 6px;
  }
}
</style>
