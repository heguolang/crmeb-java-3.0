<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center">
        <b>订货商层级显示</b>
        <el-button size="mini" icon="el-icon-refresh" @click="load">刷新</el-button>
      </div>

      <!-- 层级金字塔：从上（最高层级）到下 -->
      <div class="level-chain">
        <div class="chain-node root">
          <div class="node-title">总部（平台）</div>
          <div class="node-sub">供货方 / 结算方</div>
        </div>
        <div v-for="(lv, idx) in levels" :key="lv.id || idx" class="chain-wrap">
          <div class="chain-arrow"><i class="el-icon-bottom"></i></div>
          <div class="chain-node">
            <div class="node-title">
              {{ lv.name || '未命名层级' }}
              <span class="node-sort">排序 {{ lv.sort }}</span>
            </div>
            <div class="node-sub">
              拿货折扣 <b>{{ lv.discount }}%</b>
              <span class="dot">·</span>
              平级奖 <b>{{ lv.peerRate != null ? lv.peerRate : 0 }}%</b>
              <span class="dot">·</span>
              在册 <b>{{ levelCount(lv.id) }}</b> 人
            </div>
            <div class="node-cond">{{ condSummary(lv) }}</div>
          </div>
        </div>
        <div v-if="!levels.length" class="empty-tip">尚未配置层级，请到「订货商管理 - 层级设置」中添加</div>
      </div>
    </el-card>

    <el-card :bordered="false" shadow="never" class="mt16">
      <div slot="header"><b>层级明细</b></div>
      <el-table class="admin-table" v-loading="loading" :data="levels" size="small" stripe highlight-current-row>
        <el-table-column prop="sort" label="排序（小=高）" width="110" />
        <el-table-column prop="name" label="层级名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="discount" label="拿货折扣%" width="100" />
        <el-table-column prop="peerRate" label="平级奖%" width="90">
          <template slot-scope="scope">{{ scope.row.peerRate != null ? scope.row.peerRate : 0 }}</template>
        </el-table-column>
        <el-table-column label="升级条件" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">{{ condSummary(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="在册人数" width="90">
          <template slot-scope="scope">{{ levelCount(scope.row.id) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { stockLevelListApi, stockAgentListApi } from '@/api/stock';

export default {
  name: 'StockLevel',
  data() {
    return {
      loading: false,
      levels: [],
      agents: []
    };
  },
  methods: {
    load() {
      this.loading = true;
      stockLevelListApi().then(res => {
        this.levels = (res || []).slice().sort((a, b) => (a.sort || 0) - (b.sort || 0));
      }).catch(() => {});
      stockAgentListApi({ page: 1, limit: 1000 }).then(res => {
        this.agents = (res && res.list) || [];
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    levelCount(levelId) {
      if (!levelId) return 0;
      return this.agents.filter(a => a.levelId === levelId).length;
    },
    condSummary(row) {
      const parts = [];
      if (row.condSelfBuy) parts.push('自购≥' + row.selfBuyAmount + '元');
      if (row.condDirect) parts.push('直推业绩≥' + row.directOrderAmount + '元');
      if (row.condTeam) parts.push('团队业绩≥' + row.teamAmount + '元');
      if (row.condProduct) parts.push('购指定商品');
      if (!parts.length) return '未设置升级条件';
      return (row.conditionLogic === 1 ? '且：' : '或：') + parts.join(row.conditionLogic === 1 ? ' 且 ' : ' 或 ');
    }
  },
  mounted() {
    this.load();
  }
};
</script>

<style scoped>
.level-chain { padding: 6px 0 2px; }
.chain-wrap { display: flex; flex-direction: column; align-items: center; }
.chain-arrow { color: #c0c4cc; font-size: 16px; line-height: 1; margin: 4px 0; }
.chain-node {
  width: 340px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #f5f7fa;
  padding: 10px 14px;
  text-align: center;
}
.chain-node.root { background: #ecf5ff; border-color: #b3d8ff; }
.node-title { font-size: 14px; font-weight: 600; color: #303133; }
.node-sort { margin-left: 8px; font-size: 12px; font-weight: 400; color: #909399; }
.node-sub { margin-top: 4px; font-size: 12px; color: #606266; }
.node-sub b { color: #409eff; }
.node-cond { margin-top: 4px; font-size: 12px; color: #909399; }
.dot { margin: 0 6px; color: #dcdfe6; }
.empty-tip { text-align: center; color: #909399; font-size: 13px; padding: 16px 0; }
</style>
