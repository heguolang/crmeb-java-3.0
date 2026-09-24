<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <el-form inline size="small" @submit.native.prevent>
        <el-form-item label="用户UID">
          <el-input v-model.number="tableFrom.uid" placeholder="UID" clearable style="width: 110px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="tableFrom.type" placeholder="全部" clearable style="width: 140px">
            <el-option label="差价奖励" :value="1" />
            <el-option label="阶梯奖励" :value="2" />
            <el-option label="平级奖励" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="单号">
          <el-input v-model="tableFrom.orderNo" placeholder="关联单号" clearable style="width: 180px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
      <!-- 定稿版式 v2 复用（2026-09-25）：人员列内嵌圆头像（得奖人/业绩来源）；
           记录 ID 不单独设列；计算基数+比例合并 kv 台账；时间定宽防弹性拉伸 -->
      <el-table v-loading="loading" :data="tableData" size="small" class="admin-table table-lg" stripe highlight-current-row>
        <!-- 头部留白：定宽，不参与弹性分配 -->
        <el-table-column width="30" />
        <!-- 得奖订货商：内嵌圆头像 + 昵称 / ID 色块+复制 -->
        <el-table-column label="得奖订货商" min-width="216">
          <template slot-scope="scope">
            <div class="person-cell">
              <img v-if="scope.row.avatar" class="person-avatar" :src="scope.row.avatar" @error="scope.row.avatar = ''" />
              <span v-else class="person-avatar person-avatar--empty">{{ (scope.row.nickname || '?').slice(0, 1).toUpperCase() }}</span>
              <div class="person-info">
                <div class="info-name">{{ scope.row.nickname || '—' }}</div>
                <div class="info-line">
                  ID：<span class="id-chip">{{ scope.row.uid }}</span>
                  <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.uid)"></i>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="['', 'success', 'primary', 'warning'][scope.row.type]">{{ typeName(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联单号" width="160" />
        <!-- 业绩来源：内嵌圆头像 + 昵称 + ID 色块（关联人 ID 同样可复制） -->
        <el-table-column label="业绩来源" min-width="192">
          <template slot-scope="scope">
            <template v-if="scope.row.linkUid">
              <div class="person-cell">
                <img v-if="scope.row.linkAvatar" class="person-avatar" :src="scope.row.linkAvatar" @error="scope.row.linkAvatar = ''" />
                <span v-else class="person-avatar person-avatar--empty">{{ (scope.row.linkNickname || '?').slice(0, 1).toUpperCase() }}</span>
                <div class="person-info">
                  <div class="info-name">{{ scope.row.linkNickname || '—' }}</div>
                  <div class="info-line">
                    ID：<span class="id-chip">{{ scope.row.linkUid }}</span>
                    <i class="el-icon-document-copy copy-btn" title="复制 ID" @click="copyText(scope.row.linkUid)"></i>
                  </div>
                </div>
              </div>
            </template>
            <span v-else class="hq">—</span>
          </template>
        </el-table-column>
        <!-- 计算：基数 / 比例 两行小台账，比横排两列省 1 个列宽 -->
        <el-table-column label="计算" min-width="130">
          <template slot-scope="scope">
            <div class="kv"><span class="kv-k">基数</span><span class="kv-v">¥{{ scope.row.basePrice }}</span></div>
            <div class="kv"><span class="kv-k">比例</span><span class="kv-v">{{ scope.row.rate }}%</span></div>
          </template>
        </el-table-column>
        <el-table-column label="奖励金额" min-width="130">
          <template slot-scope="scope"><b class="reward-strong">¥{{ scope.row.rewardPrice }}</b></template>
        </el-table-column>
        <el-table-column prop="mark" label="说明" min-width="140" show-overflow-tooltip />
        <el-table-column label="时间" width="170">
          <template slot-scope="scope"><span class="info-v">{{ scope.row.createTime }}</span></template>
        </el-table-column>
        <!-- 尾部留白：按钮区/表格右缘呼吸空间，定宽 -->
        <el-table-column width="150" />
      </el-table>
      <div class="block">
        <el-pagination background :page-size="tableFrom.limit" :current-page="tableFrom.page" layout="total, prev, pager, next, jumper" :total="total" @current-change="pageChange" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { stockRewardListApi } from '@/api/stock';

export default {
  name: 'StockReward',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      tableFrom: { page: 1, limit: 20, uid: null, type: null, orderNo: '' }
    };
  },
  methods: {
    getList() {
      this.loading = true;
      stockRewardListApi(this.tableFrom).then(res => {
        this.tableData = (res && res.list) || [];
        this.total = (res && res.total) || 0;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    typeName(t) {
      return { 1: '差价奖励', 2: '阶梯奖励', 3: '平级奖励', 4: '货款成本' }[t] || t;
    },
    copyText(text) {
      const value = String(text);
      const done = () => this.$message.success('已复制：' + value);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(done).catch(() => this.fallbackCopy(value, done));
      } else {
        this.fallbackCopy(value, done);
      }
    },
    fallbackCopy(value, done) {
      const input = document.createElement('textarea');
      input.value = value;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.top = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    }
  },
  mounted() {
    this.getList();
  }
};
</script>

<style scoped>
/* 台账/信息列/ID 色块样式全部走 theme/styles.scss 全局定义；
   奖励是收入语义，用绿色加粗（与数据报表页奖励合计同款），不用版式主色蓝 */
.reward-strong {
  color: #19be6b;
  font-variant-numeric: tabular-nums;
}
</style>
