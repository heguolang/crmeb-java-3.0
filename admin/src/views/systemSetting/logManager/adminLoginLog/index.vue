<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" @submit.native.prevent>
          <el-form-item label="登录账号：">
            <el-input v-model="listPram.adminAccount" placeholder="请输入登录账号" clearable class="selWidth" />
          </el-form-item>
          <el-form-item label="登录状态：">
            <el-select v-model="listPram.status" placeholder="全部" clearable class="selWidth">
              <el-option :value="1" label="登录成功" />
              <el-option :value="0" label="登录失败" />
            </el-select>
          </el-form-item>
          <el-form-item label="登录IP：">
            <el-input v-model="listPram.ip" placeholder="请输入登录IP" clearable class="selWidth" />
          </el-form-item>
          <el-form-item label="登录时间：">
            <optionDatePicker v-model="timeVal" @changeOptTime="onchangeTime"></optionDatePicker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <el-form inline @submit.native.prevent>
        <!-- 汪总要求（2026-09-18）：移除「清空日志」功能 -->
      </el-form>
      <el-table v-loading="listLoading" :data="listData.list" size="mini">
        <el-table-column label="登录账号" prop="adminAccount" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.adminAccount | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="登录状态" min-width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status == 1" type="success" size="small">成功</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提示信息" prop="msg" min-width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.msg | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="登录IP" prop="ip" min-width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.ip | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="登录地点" prop="location" min-width="110">
          <template slot-scope="scope">
            <span>{{ scope.row.location | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="浏览器" prop="browser" min-width="140">
          <template slot-scope="scope">
            <span>{{ scope.row.browser | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作系统" prop="os" min-width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.os | filterEmpty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="登录时间" prop="createTime" min-width="170">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | filterEmpty }}</span>
          </template>
        </el-table-column>
        <!-- 汪总要求（2026-09-18）：登录日志只保留查询，移除单条删除 -->
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
  </div>
</template>

<script>
import { loginLogListApi } from '@/api/systemadmin.js';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'adminLoginLog',
  data() {
    return {
      constants: this.$constants,
      listLoading: true,
      listData: { list: [], total: 0 },
      listPram: {
        adminAccount: '',
        status: '',
        ip: '',
        dateLimit: '',
        page: 1,
        limit: this.$constants.page.limit[0],
      },
      timeVal: [],
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.listPram.dateLimit = e && e.length ? e.join(',') : '';
      this.handleSearch();
    },
    handleSearch() {
      this.listPram.page = 1;
      this.getList();
    },
    handleReset() {
      this.listPram.adminAccount = '';
      this.listPram.status = '';
      this.listPram.ip = '';
      this.listPram.dateLimit = '';
      this.timeVal = [];
      this.handleSearch();
    },
    handleSizeChange(val) {
      this.listPram.limit = val;
      this.getList();
    },
    handleCurrentChange(val) {
      this.listPram.page = val;
      this.getList();
    },
    getList() {
      this.listLoading = true;
      loginLogListApi(this.listPram)
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

<style scoped></style>
