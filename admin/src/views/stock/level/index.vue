<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header" class="lv-header">
        <div class="lv-header-main">
          <b>订货商级别设置</b>
          <span class="lv-header-tip">自上而下由高到低：排序越小级别越高、拿货折扣越小（拿货价越低）</span>
        </div>
        <div>
          <el-button size="small" icon="el-icon-plus" @click="addLevel">新增级别</el-button>
          <el-button size="small" type="primary" icon="el-icon-check" :loading="saving" @click="saveLevels">保存全部</el-button>
        </div>
      </div>

      <div v-loading="loading" class="level-board">
        <!-- 列头 -->
        <div class="row head">
          <div class="col-rank">级别 / 排序</div>
          <div class="col-name">级别名称</div>
          <div class="col-num">拿货折扣%</div>
          <div class="col-num">平级奖比例%</div>
          <div class="col-cond">升级条件</div>
          <div class="col-ops">操作</div>
        </div>

        <!-- 级别行 -->
        <div v-for="(lv, idx) in levels" :key="idx" class="row" :class="{ 'is-top': idx === 0 }">
          <div class="col-rank">
            <div class="rank-badge">{{ idx === 0 ? '最高' : idx + 1 }}</div>
            <div class="rank-edit">
              <span class="rank-label">排序</span>
              <el-input-number v-model="lv.sort" :min="1" :controls="false" size="mini" class="rank-input" />
            </div>
          </div>
          <div class="col-name">
            <el-input v-model="lv.name" size="small" placeholder="如：省级订货商" maxlength="20" />
          </div>
          <div class="col-num">
            <el-input-number v-model="lv.discount" :min="0" :max="100" :precision="2" :controls="false" size="small" class="num-input" />
          </div>
          <div class="col-num">
            <el-input-number v-model="lv.peerRate" :min="0" :max="100" :precision="2" :controls="false" size="small" class="num-input" />
          </div>
          <div class="col-cond">
            <span class="cond-text" :title="condSummary(lv)">{{ condSummary(lv) }}</span>
          </div>
          <div class="col-ops">
            <div class="op-links">
              <a class="op-link" @click="openCond(lv)">升级条件</a>
              <el-divider direction="vertical"></el-divider>
              <a class="op-link danger" @click="delLevel(lv)">删除</a>
            </div>
          </div>
        </div>

        <div v-if="!levels.length && !loading" class="empty-tip">
          尚未配置级别，<a @click="addLevel">立即新增第一个级别</a>
        </div>
      </div>

      <div class="board-foot">修改级别名称、折扣或排序后，点击右上角「保存全部」生效；升级条件与指定商品在「升级条件」中单独保存</div>
    </el-card>

    <!-- 升级条件编辑弹窗 -->
    <el-dialog :title="'升级条件 - ' + (condLevel ? (condLevel.name || '未命名级别') : '')" :visible.sync="condVisible" width="560px">
      <el-form label-width="150px" size="small">
        <el-form-item label="条件组合方式">
          <el-radio-group v-model="condForm.conditionLogic">
            <el-radio :label="0">任一满足（或）</el-radio>
            <el-radio :label="1">全部满足（与）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider />
        <el-form-item label="自购消费达标">
          <el-switch v-model="condForm.condSelfBuy" />
          <el-input-number v-if="condForm.condSelfBuy" v-model="condForm.selfBuyAmount" :min="0" :precision="2" :controls="false" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condSelfBuy" style="margin-left: 6px">元（累计已付款订单金额）</span>
        </el-form-item>
        <el-form-item label="直推订单业绩达标">
          <el-switch v-model="condForm.condDirect" />
          <el-input-number v-if="condForm.condDirect" v-model="condForm.directOrderAmount" :min="0" :precision="2" :controls="false" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condDirect" style="margin-left: 6px">元（直接下级累计业绩）</span>
        </el-form-item>
        <el-form-item label="团队伞下业绩达标">
          <el-switch v-model="condForm.condTeam" />
          <el-input-number v-if="condForm.condTeam" v-model="condForm.teamAmount" :min="0" :precision="2" :controls="false" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condTeam" style="margin-left: 6px">元（伞下全部下级业绩）</span>
        </el-form-item>
        <el-form-item label="购买指定商品">
          <el-switch v-model="condForm.condProduct" />
          <el-select v-if="condForm.condProduct" v-model="condForm.productIds" multiple filterable placeholder="选择指定商品" size="mini" style="width: 100%; margin-top: 6px">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.storeName" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="condVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="condSaving" @click="saveCond">保存条件</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockLevelListApi, stockLevelSaveApi, stockLevelDeleteApi, stockProductListApi } from '@/api/stock';

export default {
  name: 'StockLevel',
  data() {
    return {
      loading: false,
      saving: false,
      levels: [],
      productOptions: [],
      condVisible: false,
      condSaving: false,
      condLevel: null,
      condForm: {}
    };
  },
  methods: {
    getList() {
      this.loading = true;
      stockLevelListApi().then(res => {
        this.levels = (res || []).slice().sort((a, b) => (a.sort || 0) - (b.sort || 0)).map(l => ({
          ...l,
          sort: l.sort != null ? Number(l.sort) : 10,
          discount: l.discount != null ? Number(l.discount) : 100,
          peerRate: l.peerRate != null ? Number(l.peerRate) : 0
        }));
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    fmtNum(v) {
      const n = parseFloat(v);
      return isNaN(n) ? '0' : String(n);
    },
    condSummary(row) {
      const parts = [];
      if (row.condSelfBuy) parts.push('自购≥' + this.fmtNum(row.selfBuyAmount) + '元');
      if (row.condDirect) parts.push('直推业绩≥' + this.fmtNum(row.directOrderAmount) + '元');
      if (row.condTeam) parts.push('团队业绩≥' + this.fmtNum(row.teamAmount) + '元');
      if (row.condProduct) parts.push('购指定商品');
      if (!parts.length) return '未设置升级条件';
      const prefix = parts.length > 1 ? (row.conditionLogic === 1 ? '需全部满足：' : '满足任一：') : '';
      return prefix + parts.join(row.conditionLogic === 1 ? ' 且 ' : ' 或 ');
    },
    openCond(row) {
      if (!row.id) return this.$message.warning('请先保存该级别后再设置升级条件');
      this.condLevel = row;
      let productIds = [];
      if (row.upgradeProductIds) {
        productIds = String(row.upgradeProductIds).split(',').map(s => parseInt(s)).filter(n => !isNaN(n));
      }
      this.condForm = {
        condSelfBuy: !!row.condSelfBuy,
        selfBuyAmount: row.selfBuyAmount != null ? Number(row.selfBuyAmount) : 0,
        condDirect: !!row.condDirect,
        directOrderAmount: row.directOrderAmount != null ? Number(row.directOrderAmount) : 0,
        condTeam: !!row.condTeam,
        teamAmount: row.teamAmount != null ? Number(row.teamAmount) : 0,
        condProduct: !!row.condProduct,
        productIds: productIds,
        conditionLogic: row.conditionLogic != null ? row.conditionLogic : 0
      };
      if (!this.productOptions.length) {
        stockProductListApi({ page: 1, limit: 500 }).then(res => {
          this.productOptions = (res && res.list) || [];
        });
      }
      this.condVisible = true;
    },
    saveCond() {
      const f = this.condForm;
      if (f.condSelfBuy && (!f.selfBuyAmount || f.selfBuyAmount <= 0)) return this.$message.error('请填写自购消费金额');
      if (f.condDirect && (!f.directOrderAmount || f.directOrderAmount <= 0)) return this.$message.error('请填写直推业绩金额');
      if (f.condTeam && (!f.teamAmount || f.teamAmount <= 0)) return this.$message.error('请填写团队业绩金额');
      if (f.condProduct && (!f.productIds || !f.productIds.length)) return this.$message.error('请选择指定商品');
      this.condSaving = true;
      const data = {
        id: this.condLevel.id,
        condSelfBuy: f.condSelfBuy,
        selfBuyAmount: f.selfBuyAmount || 0,
        condDirect: f.condDirect,
        directOrderAmount: f.directOrderAmount || 0,
        condTeam: f.condTeam,
        teamAmount: f.teamAmount || 0,
        condProduct: f.condProduct,
        upgradeProductIds: f.productIds.join(','),
        conditionLogic: f.conditionLogic,
        peerRate: this.condLevel.peerRate || 0
      };
      stockLevelSaveApi(data).then(() => {
        this.$message.success('升级条件已保存');
        this.condSaving = false;
        this.condVisible = false;
        this.getList();
      }).catch(() => { this.condSaving = false; });
    },
    addLevel() {
      const maxSort = this.levels.reduce((m, l) => Math.max(m, l.sort || 0), 0);
      this.levels.push({ id: null, name: '', sort: maxSort + 10, discount: 90, isDel: 0, condSelfBuy: false, condDirect: false, condTeam: false, condProduct: false, conditionLogic: 0, peerRate: 0 });
    },
    delLevel(row) {
      if (row.id) {
        this.$confirm('确认删除「' + (row.name || '未命名级别') + '」？删除后属于该级别的订货商将失去级别归属。', '提示').then(() => {
          stockLevelDeleteApi(row.id).then(() => {
            this.levels = this.levels.filter(l => l !== row);
            this.$message.success('已删除');
          });
        }).catch(() => {});
      } else {
        this.levels = this.levels.filter(l => l !== row);
      }
    },
    saveLevels() {
      if (this.levels.some(l => !l.name)) return this.$message.error('存在未命名的级别，请补全级别名称');
      const sorts = this.levels.map(l => l.sort);
      if (new Set(sorts).size !== sorts.length) return this.$message.error('排序值重复，请调整后再保存');
      this.saving = true;
      Promise.all(this.levels.map(l => stockLevelSaveApi(l))).then(() => {
        this.$message.success('已保存');
        this.saving = false;
        this.getList();
      }).catch(() => { this.saving = false; });
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
/* 操作链规范（.op-links/.op-link）来自 theme/styles.scss 全局定义 */
.lv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.lv-header-main {
  min-width: 0;
  margin-right: 16px;
}
.lv-header-main b {
  font-size: 14px;
  color: #303133;
}
.lv-header-tip {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

/* 级别设置板：列头 + 行，网格严格对齐 */
.level-board {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}
.row {
  display: grid;
  grid-template-columns: 96px minmax(170px, 1.1fr) 110px 110px minmax(200px, 1.6fr) 140px;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.row:last-of-type {
  border-bottom: none;
}
.row.head {
  padding-top: 10px;
  padding-bottom: 10px;
  background: #f5f7fa;
  font-size: 12px;
  font-weight: 600;
  color: #303133;
}
.row.is-top {
  background: #f4f9ff;
}

/* 级别徽标列 */
.col-rank {
  text-align: center;
}
.rank-badge {
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border-radius: 50%;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.is-top .rank-badge {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
  font-size: 12px;
}
.rank-edit {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rank-label {
  font-size: 12px;
  color: #909399;
  margin-right: 4px;
  flex-shrink: 0;
}
.rank-input {
  width: 48px;
}
.rank-input >>> .el-input__inner {
  padding: 0 4px;
  text-align: center;
  height: 24px;
  line-height: 24px;
}

/* 数字输入列 */
.num-input {
  width: 90px;
}
.num-input >>> .el-input__inner {
  text-align: left;
}

/* 条件列：单行截断 */
.col-cond {
  min-width: 0;
  padding-right: 16px;
}
.cond-text {
  display: block;
  font-size: 12px;
  color: #606266;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作列 */
.col-ops {
  text-align: right;
}
.op-link.danger {
  color: #f56c6c;
}
.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 24px 0;
}
.empty-tip a {
  color: #409eff;
  cursor: pointer;
}
.board-foot {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
