<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
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
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button v-if="checkPermi(['admin:stock:agent:save'])" type="success" @click="openEdit()">新增代理</el-button>
          <el-button @click="openLevel">层级设置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="tableData" size="small" highlight-current-row>
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="nickname" label="代理用户" min-width="120">
          <template slot-scope="scope">
            <div>{{ scope.row.nickname }}</div>
            <div class=" grey">{{ scope.row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="层级" width="100" />
        <el-table-column label="上级" min-width="110">
          <template slot-scope="scope">{{ scope.row.parentId > 0 ? scope.row.parentName : '总部' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mark" label="备注" min-width="110" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="checkPermi(['admin:stock:agent:update'])" type="text" size="small" @click="openEdit(scope.row)">修改</el-button>
            <el-button v-if="checkPermi(['admin:stock:agent:update'])" type="text" size="small" @click="onStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button v-if="checkPermi(['admin:stock:agent:delete'])" type="text" size="small" class="red" @click="onDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
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
    <el-dialog title="层级设置" :visible.sync="levelVisible" width="560px">
      <el-table :data="levels" size="small">
        <el-table-column prop="name" label="层级名称" width="130">
          <template slot-scope="scope"><el-input v-model="scope.row.name" size="mini" /></template>
        </el-table-column>
        <el-table-column prop="sort" label="排序（小=高）" width="130">
          <template slot-scope="scope"><el-input-number v-model="scope.row.sort" :min="1" size="mini" style="width: 110px" /></template>
        </el-table-column>
        <el-table-column prop="discount" label="默认折扣%">
          <template slot-scope="scope"><el-input-number v-model="scope.row.discount" :min="0" :max="100" :precision="2" size="mini" style="width: 120px" /></template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template slot-scope="scope"><el-button type="text" size="small" class="red" @click="delLevel(scope.row)">删除</el-button></template>
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
  </div>
</template>

<script>
// force-rebuild-20260917a
import { stockAgentListApi, stockAgentSaveApi, stockAgentUpdateApi, stockAgentStatusApi, stockAgentDeleteApi, stockLevelListApi, stockLevelSaveApi, stockLevelDeleteApi } from '@/api/stock';
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
      tableFrom: { page: 1, limit: 20, keywords: '', levelId: null, status: null },
      editVisible: false,
      levelVisible: false,
      editForm: { id: null, uid: '', levelId: null, parentId: 0, mark: '' }
    };
  },
  methods: {
    checkPermi,
    getList() {
      this.loading = true;
      stockAgentListApi(this.tableFrom).then(res => {
        this.tableData = res.data.list;
        this.total = res.data.total;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },
    loadLevels() {
      stockLevelListApi().then(res => { this.levels = res.data; });
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
      stockAgentListApi({ page: 1, limit: 1000 }).then(res => { this.agentOptions = res.data.list || []; });
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
    addLevel() {
      this.levels.push({ id: null, name: '', sort: (this.levels.length + 1) * 10, discount: 90, isDel: 0 });
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
.red { color: #f56c6c; }
.grey { color: #999; font-size: 12px; }
</style>
