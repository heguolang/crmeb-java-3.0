<template>
  <div class="divBox">
    <el-card class="box-card">
      <el-form inline @submit.native.prevent>
        <!-- 汪总要求（2026-09-18）：移除「清空日志」功能 -->
      </el-form>
      <el-table v-loading="listLoading" :data="listData.list" size="mini">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="管理员账号" prop="adminAccount" min-width="110">
          <template slot-scope="scope">
            <span>{{ scope.row.adminAccount | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" prop="description" min-width="140">
          <template slot-scope="scope">
            <span>{{ scope.row.description | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" prop="requestMethod" width="90">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.requestMethod | filterEmpty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求地址" prop="url" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ scope.row.url | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="IP" prop="ip" min-width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.ip | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status == 0" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">异常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="createTime" min-width="170">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template slot-scope="scope">
            <a @click="handlerOpenDetail(scope.row)">详情</a>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :current-page="listPram.page"
        :page-sizes="constants.page.limit"
        :layout="constants.page.layout"
        :total="listData.total"
        @size-change="handleSizeChange"
        background
        @current-change="handleCurrentChange"
      />
    </el-card>
    <el-dialog :visible.sync="detailVisible" title="日志详情" width="720px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="管理员账号">{{ detailData.adminAccount | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ detailData.description | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="操作方法">{{ detailData.method | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ detailData.url | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip | filterEmpty }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <div class="breakAll">{{ detailData.requestParam | filterEmpty }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回参数">
          <div class="breakAll">{{ detailData.result | filterEmpty }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误消息" v-if="detailData.errorMsg">
          <div class="breakAll">{{ detailData.errorMsg }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.createTime | filterEmpty }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { sensitiveListApi } from '@/api/systemadmin.js';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'adminOperateLog',
  data() {
    return {
      constants: this.$constants,
      listLoading: true,
      listData: { list: [], total: 0 },
      listPram: {
        page: 1,
        limit: this.$constants.page.limit[0],
      },
      detailVisible: false,
      detailData: {},
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    handleSizeChange(val) {
      this.listPram.limit = val;
      this.getList();
    },
    handleCurrentChange(val) {
      this.listPram.page = val;
      this.getList();
    },
    handlerOpenDetail(row) {
      this.detailData = row;
      this.detailVisible = true;
    },
    getList() {
      this.listLoading = true;
      sensitiveListApi(this.listPram)
        .then((data) => {
          this.listData = data;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
  },
};
</script>

<style scoped>
.breakAll {
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
