<template>
  <div class="divBox">
    <el-card>
      <el-alert
        title="「走人留人」机制：代理晋升老板时本人脱离原上级团队，其前 N 个直推位继续留在原上级名下（下「走人留人轨迹」）。管理员手工调整安置上级会留痕（下「关系调整审计」）。"
        type="info"
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-tabs v-model="tab" @tab-click="onTab">
        <el-tab-pane label="走人留人轨迹" name="slide">
          <el-table :data="slideList" border v-loading="slideLoading">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userId" label="用户UID" width="100" />
            <el-table-column label="原上级" width="200">
              <template slot-scope="scope">
                <span>{{ scope.row.fromParentId || '—' }}</span>
                <span class="arrow">→</span>
                <span>{{ scope.row.toParentId || '脱离(走人)' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="260" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="时间" width="170" />
            <template slot="empty">
              <div class="empty">暂无关系变更记录</div>
            </template>
          </el-table>
          <el-pagination
            style="margin-top: 12px"
            layout="total, prev, pager, next"
            :total="slideTotal"
            :page-size="limit"
            :current-page.sync="slidePage"
            @current-change="loadSlide"
          />
        </el-tab-pane>

        <el-tab-pane label="关系调整审计" name="audit">
          <el-table :data="auditList" border v-loading="auditLoading">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="userId" label="用户UID" width="100" />
            <el-table-column label="变更前" width="110">
              <template slot-scope="scope">{{ scope.row.beforeVal || '—' }}</template>
            </el-table-column>
            <el-table-column label="变更后" width="110">
              <template slot-scope="scope">{{ scope.row.afterVal || '—' }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170" />
            <template slot="empty">
              <div class="empty">暂无人工调整记录</div>
            </template>
          </el-table>
          <el-pagination
            style="margin-top: 12px"
            layout="total, prev, pager, next"
            :total="auditTotal"
            :page-size="limit"
            :current-page.sync="auditPage"
            @current-change="loadAudit"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { chainSlideLogsApi, chainAuditLogsApi } from '@/api/chain';

export default {
  name: 'ChainRelation',
  data() {
    return {
      tab: 'slide',
      limit: 20,
      // 走人留人轨迹
      slideLoading: false,
      slideList: [],
      slideTotal: 0,
      slidePage: 1,
      // 关系调整审计
      auditLoading: false,
      auditList: [],
      auditTotal: 0,
      auditPage: 1,
    };
  },
  created() {
    this.loadSlide();
  },
  methods: {
    onTab(tab) {
      if (tab.name === 'audit' && this.auditTotal === 0 && this.auditList.length === 0) {
        this.loadAudit();
      }
    },
    loadSlide() {
      this.slideLoading = true;
      chainSlideLogsApi({ page: this.slidePage, limit: this.limit })
        .then((res) => {
          const data = res.data || res || {};
          this.slideList = data.list || [];
          this.slideTotal = data.total || 0;
        })
        .catch(() => {
          this.slideList = [];
          this.slideTotal = 0;
        })
        .finally(() => {
          this.slideLoading = false;
        });
    },
    loadAudit() {
      this.auditLoading = true;
      chainAuditLogsApi({ page: this.auditPage, limit: this.limit })
        .then((res) => {
          const data = res.data || res || {};
          this.auditList = data.list || [];
          this.auditTotal = data.total || 0;
        })
        .catch(() => {
          this.auditList = [];
          this.auditTotal = 0;
        })
        .finally(() => {
          this.auditLoading = false;
        });
    },
  },
};
</script>

<style scoped>
.arrow {
  margin: 0 6px;
  color: #909399;
}
.empty {
  padding: 24px 0;
  color: #909399;
  font-size: 13px;
}
</style>
