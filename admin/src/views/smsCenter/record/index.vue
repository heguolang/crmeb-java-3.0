<template>
  <div class="sms-record-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="mb14">
      <div class="filter-row">
        <div class="filter-left">
          <el-input
            v-model="tableFrom.phone"
            placeholder="手机号"
            clearable
            class="w150"
            @keyup.enter.native="getList(1)"
          />
          <el-select v-model="tableFrom.status" placeholder="全部状态" clearable class="w130">
            <el-option label="全部状态" value="" />
            <el-option label="发送成功" value="DELIVRD" />
            <el-option label="发送失败" value="FAIL" />
          </el-select>
          <el-input
            v-model="tableFrom.keywords"
            placeholder="内容关键词"
            clearable
            class="w170"
            @keyup.enter.native="getList(1)"
          />
          <el-button type="primary" icon="el-icon-search" @click="getList(1)">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
        <div class="filter-right">
          <span class="balance-text">余额 <b>{{ balanceText }}</b> 条</span>
          <el-button icon="el-icon-refresh" :loading="syncing" @click="syncStatus">同步回执</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData.data" class="table-lg">
        <el-table-column label="编号" prop="id" width="64" />
        <el-table-column label="手机号" min-width="118">
          <template slot-scope="scope">
            <span>{{ scope.row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column label="渠道" width="64">
          <template slot-scope="scope">
            <span class="channel-tag">{{ channelName(scope.row.channel) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="短信内容 / 批次号" min-width="300">
          <template slot-scope="scope">
            <div class="content-line" :title="scope.row.content">{{ scope.row.content }}</div>
            <div v-if="scope.row.taskId" class="task-line">
              批次：<span class="id-chip">{{ scope.row.taskId }}</span>
              <i class="el-icon-document-copy copy-btn" title="复制批次号" @click="copyText(scope.row.taskId)" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="回执状态" width="96">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 'DELIVRD'" type="success" size="mini">发送成功</el-tag>
            <el-tag v-else-if="scope.row.status" type="danger" size="mini">发送失败</el-tag>
            <el-tag v-else type="info" size="mini">待回执</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="失败原因" min-width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.status && scope.row.status !== 'DELIVRD'" class="fail-reason">
              {{ scope.row.statusDesc || scope.row.status }}
            </span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="回执时间" width="170">
          <template slot-scope="scope">
            <span>{{ scope.row.statusTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template slot-scope="scope">
            <span>{{ (scope.row.createTime || '') | timeFormat }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
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
  </div>
</template>

<script>
import { smsRecordListApi, smsRecordSyncApi, smsBalanceApi } from '@/api/smsCenter';

export default {
  name: 'SmsCenterRecord',
  filters: {
    timeFormat(v) {
      if (!v) return '—';
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/.test(v)) return v.substring(0, 19);
      const d = new Date(v);
      if (isNaN(d.getTime())) return v;
      const p = (n) => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
    },
  },
  data() {
    return {
      loading: false,
      syncing: false,
      balance: null,
      tableFrom: {
        page: 1,
        limit: 20,
        phone: '',
        status: '',
        keywords: '',
      },
      tableData: { data: [], total: 0 },
    };
  },
  computed: {
    balanceText() {
      return this.balance === null || this.balance === undefined ? '—' : Number(this.balance).toLocaleString();
    },
  },
  mounted() {
    this.getList(1);
    this.loadBalance();
  },
  methods: {
    getList(page) {
      if (page) this.tableFrom.page = page;
      this.loading = true;
      smsRecordListApi(this.tableFrom)
        .then((res) => {
          // 拦截器已返回 CommonResult.data，此处 res 即 {count, data}
          const d = res || {};
          this.tableData.data = d.data || [];
          this.tableData.total = Number(d.count) || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    resetFilter() {
      this.tableFrom.phone = '';
      this.tableFrom.status = '';
      this.tableFrom.keywords = '';
      this.getList(1);
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(size) {
      this.tableFrom.limit = size;
      this.getList(1);
    },
    loadBalance() {
      smsBalanceApi()
        .then((res) => {
          this.balance = (res || {}).balance;
        })
        .catch(() => {
          this.balance = null;
        });
    },
    syncStatus() {
      this.syncing = true;
      smsRecordSyncApi()
        .then((res) => {
          const n = res || 0;
          this.$message.success(`回执同步完成，更新 ${n} 条记录`);
          this.getList(1);
          this.loadBalance();
        })
        .finally(() => {
          this.syncing = false;
        });
    },
    channelName(c) {
      return c === 'aliyun' ? '阿里云' : '滨礼';
    },
    copyText(text) {
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard
          .writeText(text)
          .then(() => this.$message.success(`已复制：${text}`))
          .catch(() => this.fallbackCopy(text));
      } else {
        this.fallbackCopy(text);
      }
    },
    fallbackCopy(text) {
      const input = document.createElement('textarea');
      input.value = text;
      input.style.position = 'fixed';
      input.style.opacity = '0';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        this.$message.success(`已复制：${text}`);
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
  },
};
</script>

<style scoped>
.sms-record-page {
  padding: 0 14px 14px;
}
.mb14 {
  margin-bottom: 14px;
}
.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.balance-text {
  font-size: 13px;
  color: #909399;
}
.balance-text b {
  color: #409eff;
  font-weight: 500;
}
.w150 {
  width: 150px;
}
.w130 {
  width: 130px;
}
.w170 {
  width: 170px;
}
.channel-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}
.content-line {
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.task-line {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}
.id-chip {
  display: inline-block;
  margin-left: 2px;
  padding: 0 6px;
  border-radius: 3px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  line-height: 18px;
  font-variant-numeric: tabular-nums;
}
.copy-btn {
  margin-left: 2px;
  color: #c0c4cc;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.15s;
}
.copy-btn:hover {
  color: #409eff;
}
.fail-reason {
  color: #f56c6c;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 4px 2px;
}
</style>
