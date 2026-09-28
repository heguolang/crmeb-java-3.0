<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <!-- 顶部统计条（分销商管理范式）：说明文案 + 添加按钮右侧 -->
      <div class="summary-bar summary-bar--flex">
        <span>拼购等级是<b>全局默认分享奖励阶梯</b>：推荐人的直推人数达到某等级门槛（含）时，按满足条件的最高一档比例发放分享奖励；拼购活动未单独设置分享奖励阶梯的，结算自动按这里的默认等级比例执行。</span>
        <div>
          <el-button type="primary" plain icon="el-icon-plus" @click="onOpenEdit()">添加等级</el-button>
        </div>
      </div>

      <el-table class="admin-table table-lg" v-loading="loading" :data="tableData" size="small" stripe highlight-current-row>
        <!-- 头部留白列：定稿规则 30，定宽不参与弹性分配 -->
        <el-table-column width="30" />
        <!-- ID：等级主键独立一栏（非会员 ID，不做色块/复制——复制只用于会员 ID） -->
        <el-table-column label="ID" width="80" align="center">
          <template slot-scope="scope">{{ scope.row.id }}</template>
        </el-table-column>
        <el-table-column label="等级名称" min-width="160">
          <template slot-scope="scope">
            <div class="info-name">{{ scope.row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="直推人数（含）" width="140" align="center">
          <template slot-scope="scope">{{ scope.row.minDirectNum }} 人</template>
        </el-table-column>
        <el-table-column label="分享奖励比例" width="140" align="center">
          <template slot-scope="scope"><span class="c-red">{{ scope.row.rewardRate }}%</span></template>
        </el-table-column>
        <el-table-column label="排序" width="70" align="center">
          <template slot-scope="scope">{{ scope.row.sort }}</template>
        </el-table-column>
        <!-- 时间列定宽：14px 下完整 yyyy-MM-dd HH:mm:ss 需 186px（含内边距），别用 min-width -->
        <el-table-column label="更新时间" width="186" align="center" class-name="time-cell">
          <template slot-scope="scope">{{ fmtTime(scope.row.updateTime) }}</template>
        </el-table-column>
        <!-- 操作：单行等宽胶囊（172 = 内边距 30 + 2 格×68 + 间隙 6），危险操作收尾，不 fixed -->
        <el-table-column label="操作" width="172" class-name="op-cell" label-class-name="op-cell">
          <template slot-scope="scope">
            <div class="op-grid op-grid--auto">
              <el-button size="mini" plain class="op-tag tint-neutral" @click="onOpenEdit(scope.row)">修改</el-button>
              <el-button size="mini" plain class="op-tag tint-danger" @click="onDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <!-- 尾部留白列：定稿规则 150，按钮与表格右缘之间留白 -->
        <el-table-column width="150" />
      </el-table>
    </el-card>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="editForm.id ? '修改拼购等级' : '添加拼购等级'" :visible.sync="editShow" width="480px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="120px" size="small">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="editForm.name" maxlength="20" placeholder="如：A级" style="width: 240px" />
        </el-form-item>
        <el-form-item label="直推人数" prop="minDirectNum">
          <el-input-number v-model="editForm.minDirectNum" :min="1" :precision="0" style="width: 240px" />
          <div class="form-tip">直推人数达到该值（含）时可享受本等级比例</div>
        </el-form-item>
        <el-form-item label="奖励比例" prop="rewardRate">
          <el-input-number v-model="editForm.rewardRate" :min="0" :max="100" :precision="2" style="width: 240px" />
          <span class="form-tip" style="margin-left: 8px">%</span>
          <div class="form-tip">按被推荐人的拼购参与金额计算</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :precision="0" style="width: 240px" />
          <div class="form-tip">数字越大列表越靠前</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editShow = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="onSave">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pingouLevelListApi, pingouLevelSaveApi, pingouLevelUpdateApi, pingouLevelDeleteApi } from '@/api/pingou';

export default {
  name: 'PingouLevel',
  data() {
    return {
      loading: false,
      saving: false,
      tableData: [],
      editShow: false,
      editForm: this.emptyForm(),
      rules: {
        name: [{ required: true, message: '请填写等级名称', trigger: 'blur' }],
        minDirectNum: [{ required: true, message: '请填写直推人数', trigger: 'blur' }],
        rewardRate: [{ required: true, message: '请填写奖励比例', trigger: 'blur' }],
      },
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    emptyForm() {
      return { id: null, name: '', minDirectNum: 1, rewardRate: 5, sort: 0 };
    },
    getList() {
      this.loading = true;
      pingouLevelListApi()
        .then((res) => {
          // admin request.js 成功时直接返回 payload
          this.tableData = Array.isArray(res) ? res : res.list || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
    },
    onOpenEdit(row) {
      this.editForm = row ? { ...row } : this.emptyForm();
      this.editShow = true;
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate();
      });
    },
    onSave() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return;
        this.saving = true;
        const req = this.editForm.id ? pingouLevelUpdateApi : pingouLevelSaveApi;
        req(this.editForm)
          .then(() => {
            this.$message.success(this.editForm.id ? '修改成功' : '添加成功');
            this.editShow = false;
            this.getList();
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    onDelete(row) {
      this.$confirm(`确定删除等级「${row.name}」吗？`, '提示', { type: 'warning' })
        .then(() => pingouLevelDeleteApi(row.id))
        .then(() => {
          this.$message.success('删除成功');
          this.getList();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
/* 列表范式（summary-bar / table-lg / op-grid / op-tag / info-name）已统一提升到
   theme/styles.scss 全局定义，本页不写 scoped 副本 */
/* 无筛选页变体：统计条左侧文案 + 右侧操作按钮（与分红等级页同款） */
.summary-bar--flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
/* 操作列：胶囊强制横排不换行（与分销商管理同款） */
::v-deep .op-cell .op-tag {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.c-red {
  color: #e93323;
  font-weight: 600;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
