<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="toolbar">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="关键词">
            <el-input v-model="tableFrom.keywords" placeholder="昵称/手机号" clearable style="width: 180px" @keyup.enter.native="getList" />
          </el-form-item>
          <el-form-item label="层级">
            <el-select v-model="tableFrom.levelId" placeholder="全部层级" clearable style="width: 140px">
              <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
            </el-select>
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
          <el-button v-if="checkPermi(['admin:stock:agent:save'])" type="success" icon="el-icon-plus" @click="openEdit()">新增代理</el-button>
          <el-button icon="el-icon-setting" @click="openLevel">层级设置</el-button>
        </div>
      </div>
      <el-table class="admin-table" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <el-table-column prop="id" label="ID" width="38" />
        <el-table-column prop="nickname" label="代理用户" min-width="120">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div class=" grey">{{ scope.row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="层级" width="72" />
        <el-table-column label="上级" min-width="90">
          <template slot-scope="scope">{{ scope.row.parentId > 0 ? scope.row.parentName : '总部' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template slot-scope="scope">
            <span class="st-dot" :class="{ on: scope.row.status === 1 }"><i></i>{{ scope.row.status === 1 ? '启用' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="90" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="136" />
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <div class="op-links">
              <template v-if="checkPermi(['admin:stock:agent:update'])">
                <a class="op-link" @click="openEdit(scope.row)">修改</a>
                <el-divider direction="vertical"></el-divider>
                <a class="op-link" @click="onStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</a>
                <el-divider direction="vertical"></el-divider>
              </template>
              <a v-if="checkPermi(['admin:stock:agent:delete'])" class="op-link" @click="onDelete(scope.row)">删除</a>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>

    <!-- 代理编辑弹窗 -->
    <el-dialog :title="editForm.id ? '修改代理' : '新增代理'" :visible.sync="editVisible" width="480px">
      <el-form :model="editForm" label-width="90px" size="small">
        <el-form-item label="用户UID">
          <el-input v-model.number="editForm.uid" :disabled="!!editForm.id" placeholder="会员UID" />
        </el-form-item>
        <el-form-item label="层级">
          <el-select v-model="editForm.levelId" style="width: 100%">
            <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级代理">
          <el-select v-model="editForm.parentId" style="width: 100%" clearable filterable placeholder="不选=上级为总部">
            <el-option v-for="a in agentOptions" :key="a.id" :label="a.nickname + '（' + a.levelName + '）'" :value="a.id" />
          </el-select>
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

    <!-- 层级设置弹窗 -->
    <el-dialog title="层级设置" :visible.sync="levelVisible" width="1050px">
      <el-table class="admin-table" :data="levels" size="small">
        <el-table-column prop="name" label="层级名称" width="120">
          <template slot-scope="scope"><el-input v-model="scope.row.name" size="mini" /></template>
        </el-table-column>
        <el-table-column prop="sort" label="排序（小=高）" width="120">
          <template slot-scope="scope"><el-input-number v-model="scope.row.sort" :min="1" size="mini" style="width: 105px" /></template>
        </el-table-column>
        <el-table-column prop="discount" label="默认折扣%" width="115">
          <template slot-scope="scope"><el-input-number v-model="scope.row.discount" :min="0" :max="100" :precision="2" size="mini" style="width: 105px" /></template>
        </el-table-column>
        <el-table-column label="升级条件" min-width="150">
          <template slot-scope="scope">
            <span>{{ condSummary(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="peerRate" label="平级奖比例%" width="90">
          <template slot-scope="scope">{{ scope.row.peerRate != null ? scope.row.peerRate : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <div class="op-links">
              <a class="op-link" @click="openCond(scope.row)">升级条件</a>
              <el-divider direction="vertical"></el-divider>
              <a class="op-link" @click="delLevel(scope.row)">删除</a>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px">
        <el-button size="mini" @click="addLevel">+ 新增层级</el-button>
      </div>
      <div slot="footer">
        <el-button size="small" @click="levelVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveLevels">保存</el-button>
      </div>
    </el-dialog>

    <!-- 升级条件编辑弹窗 -->
    <el-dialog :title="'升级条件 - ' + (condLevel ? condLevel.name : '')" :visible.sync="condVisible" width="560px" append-to-body>
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
          <el-input-number v-if="condForm.condSelfBuy" v-model="condForm.selfBuyAmount" :min="0" :precision="2" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condSelfBuy" style="margin-left: 6px">元（累计已付款订单金额）</span>
        </el-form-item>
        <el-form-item label="直推订单业绩达标">
          <el-switch v-model="condForm.condDirect" />
          <el-input-number v-if="condForm.condDirect" v-model="condForm.directOrderAmount" :min="0" :precision="2" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condDirect" style="margin-left: 6px">元（直接下级累计业绩）</span>
        </el-form-item>
        <el-form-item label="团队伞下业绩达标">
          <el-switch v-model="condForm.condTeam" />
          <el-input-number v-if="condForm.condTeam" v-model="condForm.teamAmount" :min="0" :precision="2" size="mini" style="width: 140px; margin-left: 10px" />
          <span v-if="condForm.condTeam" style="margin-left: 6px">元（伞下全部下级业绩）</span>
        </el-form-item>
        <el-form-item label="购买指定商品">
          <el-switch v-model="condForm.condProduct" />
          <el-select v-if="condForm.condProduct" v-model="condForm.productIds" multiple filterable placeholder="选择指定商品" size="mini" style="width: 100%; margin-top: 6px">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.storeName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-divider />
        <el-form-item label="平级奖比例%">
          <el-input-number v-model="condForm.peerRate" :min="0" :max="100" :precision="2" size="small" style="width: 140px" />
          <span class="switch-tip">平推同级代理产生业绩时，本层级代理额外按此比例拿奖励（0=不拿）</span>
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
import { stockAgentListApi, stockAgentSaveApi, stockAgentUpdateApi, stockAgentStatusApi, stockAgentDeleteApi, stockLevelListApi, stockLevelSaveApi, stockLevelDeleteApi, stockProductListApi } from '@/api/stock';
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
      tableFrom: { page: 1, limit: 20, keywords: '', levelId: null, status: null },
      editVisible: false,
      levelVisible: false,
      editForm: { id: null, uid: '', levelId: null, parentId: 0, mark: '' },
      condVisible: false,
      condSaving: false,
      condLevel: null,
      condForm: {}
    };
  },
  methods: {
    checkPermi,
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
    openEdit(row) {
      if (row) {
        this.editForm = { id: row.id, uid: row.uid, levelId: row.levelId, parentId: row.parentId, mark: row.mark };
      } else {
        this.editForm = { id: null, uid: '', levelId: this.levels.length ? this.levels[this.levels.length - 1].id : null, parentId: 0, mark: '' };
      }
      stockAgentListApi({ page: 1, limit: 1000 }).then(res => { this.agentOptions = (res && res.list) || []; });
      this.editVisible = true;
    },
    onSave() {
      if (!this.editForm.uid) return this.$message.error('请填写用户UID');
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
    onDelete(row) {
      this.$confirm('确认删除该代理？', '提示').then(() => {
        stockAgentDeleteApi(row.id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      }).catch(() => {});
    },
    openLevel() {
      this.levelVisible = true;
    },
    condSummary(row) {
      const parts = [];
      if (row.condSelfBuy) parts.push('自购≥' + row.selfBuyAmount + '元');
      if (row.condDirect) parts.push('直推业绩≥' + row.directOrderAmount + '元');
      if (row.condTeam) parts.push('团队业绩≥' + row.teamAmount + '元');
      if (row.condProduct) parts.push('购指定商品');
      if (!parts.length) return '未设置';
      return (row.conditionLogic === 1 ? '且：' : '或：') + parts.join(row.conditionLogic === 1 ? ' 且 ' : ' 或 ');
    },
    openCond(row) {
      if (!row.id) return this.$message.warning('请先保存该层级后再设置升级条件');
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
        conditionLogic: row.conditionLogic != null ? row.conditionLogic : 0,
        peerRate: row.peerRate != null ? Number(row.peerRate) : 0
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
        peerRate: f.peerRate || 0
      };
      stockLevelSaveApi(data).then(() => {
        this.$message.success('升级条件已保存');
        this.condSaving = false;
        this.condVisible = false;
        this.loadLevels();
      }).catch(() => { this.condSaving = false; });
    },
    addLevel() {
      this.levels.push({ id: null, name: '', sort: (this.levels.length + 1) * 10, discount: 90, isDel: 0, condSelfBuy: false, condDirect: false, condTeam: false, condProduct: false, conditionLogic: 0, peerRate: 0 });
    },
    delLevel(row) {
      if (row.id) {
        stockLevelDeleteApi(row.id).then(() => {
          this.levels = this.levels.filter(l => l.id !== row.id);
          this.$message.success('已删除');
        });
      } else {
        this.levels = this.levels.filter(l => l !== row);
      }
    },
    saveLevels() {
      const tasks = this.levels.map(l => stockLevelSaveApi(l));
      Promise.all(tasks).then(() => {
        this.$message.success('已保存');
        this.levelVisible = false;
        this.loadLevels();
      });
    }
  },
  mounted() {
    this.getList();
    this.loadLevels();
  }
};
</script>

<style scoped>
/* 列表页通用规范（.toolbar/.pager/.admin-table/.op-wrap/.op-btn）已统一在 theme/styles.scss 全局定义 */
.red { color: #f56c6c; }
.grey { color: #999; font-size: 12px; }
.switch-tip { margin-left: 12px; font-size: 12px; color: #909399; line-height: 1.5; }
</style>
