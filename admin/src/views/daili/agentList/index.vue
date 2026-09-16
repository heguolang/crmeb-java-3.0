<template>
  <div class="divBox">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" :model="tableFrom" label-width="80px">
          <el-form-item label="关键字：">
            <el-input
              v-model="tableFrom.keywords"
              placeholder="UID/区域"
              clearable
              class="selWidth"
              @keyup.enter.native="searchList"
            />
          </el-form-item>
          <el-form-item label="级别：">
            <el-select v-model="tableFrom.level" placeholder="全部级别" clearable class="selWidth">
              <el-option label="省级" :value="1" />
              <el-option label="市级" :value="2" />
              <el-option label="区级" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态：">
            <el-select v-model="tableFrom.status" placeholder="全部状态" clearable class="selWidth">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchList">搜索</el-button>
            <el-button @click="resetList">重置</el-button>
            <el-button v-if="checkPermi(['admin:agent:save'])" type="success" @click="openEdit()">添加代理</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="mini" highlight-current-row>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="代理用户" min-width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname || '-' }}</div>
            <div class="sub-text">UID: {{ scope.row.uid }}</div>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="90">
          <template slot-scope="scope">
            <el-tag size="mini">{{ levelLabel(scope.row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="代理区域" min-width="150" show-overflow-tooltip />
        <el-table-column label="奖励比例" width="100">
          <template slot-scope="scope">{{ scope.row.ratio }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyMark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="checkTime" label="审核时间" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="checkPermi(['admin:agent:update'])"
              type="text"
              size="small"
              @click="openEdit(scope.row)"
              >修改</el-button
            >
            <el-button
              v-if="checkPermi(['admin:agent:audit']) && scope.row.status === 0"
              type="text"
              size="small"
              @click="onAudit(scope.row, 1)"
              >通过</el-button
            >
            <el-button
              v-if="checkPermi(['admin:agent:audit']) && scope.row.status === 0"
              type="text"
              size="small"
              class="danger-text"
              @click="onAudit(scope.row, 2)"
              >拒绝</el-button
            >
            <el-button
              v-if="checkPermi(['admin:agent:delete'])"
              type="text"
              size="small"
              class="danger-text"
              @click="onDelete(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
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

    <el-dialog :title="editForm.id ? '修改代理' : '添加代理'" :visible.sync="editVisible" width="520px">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="90px" size="small">
        <el-form-item label="代理用户：" prop="uid">
          <el-input v-model="editForm.uid" placeholder="请输入用户UID" style="width: 320px" />
        </el-form-item>
        <el-form-item label="代理级别：" prop="level">
          <el-select v-model="editForm.level" placeholder="请选择级别" style="width: 320px" @change="onLevelChange">
            <el-option label="省级" :value="1" />
            <el-option label="市级" :value="2" />
            <el-option label="区级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="代理区域：" prop="regionArr">
          <el-cascader
            :options="cityOptions"
            :props="propsCity"
            filterable
            v-model="editForm.regionArr"
            style="width: 320px"
            placeholder="请选择省市区"
          />
        </el-form-item>
        <el-form-item label="奖励比例：" prop="ratio">
          <el-input-number
            v-model="editForm.ratio"
            :min="0.01"
            :max="100"
            :precision="2"
            :step="0.5"
            style="width: 200px"
          />
          <span style="margin-left: 8px">%</span>
        </el-form-item>
        <el-form-item label="备注：">
          <el-input v-model="editForm.applyMark" type="textarea" :rows="2" style="width: 320px" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="onSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { agentListApi, agentSaveApi, agentUpdateApi, agentAuditApi, agentDeleteApi, cityTreeApi } from '@/api/daili';
import { Debounce } from '@/utils/validate';

export default {
  name: 'AgentList',
  data() {
    return {
      listLoading: false,
      submitLoading: false,
      tableFrom: {
        page: 1,
        limit: 20,
        keywords: '',
        level: undefined,
        status: undefined,
      },
      tableData: { data: [], total: 0 },
      editVisible: false,
      cityOptions: [],
      propsCity: {
        children: 'child',
        label: 'name',
        value: 'name',
      },
      editForm: {
        id: null,
        uid: '',
        level: 1,
        regionArr: [],
        ratio: 5,
        applyMark: '',
      },
      editRules: {
        uid: [{ required: true, message: '请输入用户UID', trigger: 'blur' }],
        level: [{ required: true, message: '请选择代理级别', trigger: 'change' }],
        regionArr: [{ required: true, type: 'array', min: 1, message: '请选择代理区域', trigger: 'change' }],
        ratio: [{ required: true, message: '请填写奖励比例', trigger: 'blur' }],
      },
    };
  },
  mounted() {
    this.getList();
    this.getCityList();
  },
  methods: {
    levelLabel(level) {
      const map = { 1: '省级', 2: '市级', 3: '区级' };
      return map[level] || '-';
    },
    statusLabel(status) {
      const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' };
      return map[status] || '-';
    },
    statusTagType(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'danger' };
      return map[status] || 'info';
    },
    async getCityList() {
      try {
        const res = await cityTreeApi();
        this.cityOptions = res || [];
      } catch (e) {
        this.cityOptions = [];
      }
    },
    searchList() {
      this.tableFrom.page = 1;
      this.getList();
    },
    resetList() {
      this.tableFrom = { page: 1, limit: 20, keywords: '', level: undefined, status: undefined };
      this.getList();
    },
    getList() {
      this.listLoading = true;
      const params = { ...this.tableFrom };
      ['level', 'status'].forEach((k) => {
        if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k];
      });
      if (!params.keywords) delete params.keywords;
      agentListApi(params)
        .then((res) => {
          this.tableData.data = (res && res.list) || [];
          this.tableData.total = (res && res.total) || 0;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.tableFrom.page = 1;
      this.getList();
    },
    openEdit(row) {
      this.editForm = {
        id: row ? row.id : null,
        uid: row ? String(row.uid) : '',
        level: row ? row.level : 1,
        regionArr: row ? [row.province, row.city, row.district].filter((v) => v && v.length) : [],
        ratio: row ? Number(row.ratio) : 5,
        applyMark: row ? row.applyMark : '',
      };
      this.editVisible = true;
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate();
      });
    },
    onLevelChange() {
      // 切换级别时截断区域选择：省级留1位、市级留2位
      const level = this.editForm.level;
      if (level === 1 && this.editForm.regionArr.length > 1) this.editForm.regionArr = [this.editForm.regionArr[0]];
      if (level === 2 && this.editForm.regionArr.length > 2) {
        this.editForm.regionArr = this.editForm.regionArr.slice(0, 2);
      }
    },
    onSubmit: Debounce(function () {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        const region = this.editForm.regionArr || [];
        const level = this.editForm.level;
        const data = {
          id: this.editForm.id,
          uid: Number(this.editForm.uid),
          level,
          province: region[0] || '',
          city: level >= 2 ? region[1] || '' : '',
          district: level >= 3 ? region[2] || '' : '',
          ratio: this.editForm.ratio,
          status: this.editForm.id ? undefined : 1,
          applyMark: this.editForm.applyMark,
        };
        if (!data.province || (level >= 2 && !data.city) || (level >= 3 && !data.district)) {
          return this.$message.warning('代理区域与级别不匹配，请重新选择');
        }
        this.submitLoading = true;
        const req = this.editForm.id ? agentUpdateApi(data) : agentSaveApi(data);
        req
          .then(() => {
            this.$message.success(this.editForm.id ? '修改成功' : '添加成功');
            this.editVisible = false;
            this.submitLoading = false;
            this.getList();
          })
          .catch(() => {
            this.submitLoading = false;
          });
      });
    }),
    onAudit(row, status) {
      const tip = status === 1 ? '通过该代理申请？通过后请设置奖励比例' : '拒绝该代理申请？';
      this.$confirm(tip, '提示', { type: 'warning' })
        .then(() => agentAuditApi(row.id, status))
        .then(() => {
          this.$message.success('操作成功');
          this.getList();
        })
        .catch(() => {});
    },
    onDelete(row) {
      this.$confirm('删除后该代理将不再获得区域奖励，确认删除？', '提示', { type: 'warning' })
        .then(() => agentDeleteApi(row.id))
        .then(() => {
          this.$message.success('删除成功');
          this.getList();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped lang="scss">
.selWidth {
  width: 220px;
}
.sub-text {
  color: #999;
  font-size: 12px;
}
.danger-text {
  color: #f56c6c;
}
</style>
