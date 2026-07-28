<template>
  <div class="divBox">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="card-title">会员等级升级配置</span>
        <el-button
          type="primary"
          class="fr"
          @click="add"
          v-hasPermi="['admin:system:user:level:save']"
          >添加会员等级</el-button
        >
      </div>
      <el-alert
        class="mb15"
        type="info"
        :closable="false"
        title="说明：等级名称仅为别名（如创客、联创）。累计消费类用「经验/消费额」判断；单笔达标需选「单笔订单」。积分与分佣比例在此配置，订单结算逻辑将后续接入。"
        show-icon
      />
      <el-table v-loading="listLoading" :data="tableData.data" style="width: 100%" size="mini">
        <el-table-column prop="id" label="ID" min-width="50" />
        <el-table-column label="图标" min-width="70">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.icon"
              style="width: 36px; height: 36px"
              :src="scope.row.icon"
              :preview-src-list="[scope.row.icon]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="等级名称" min-width="90" />
        <el-table-column prop="grade" label="等级序号" min-width="80" />
        <el-table-column label="升级方式" min-width="100">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.upgradeType === 2 ? 'warning' : 'success'">
              {{ upgradeTypeText(scope.row.upgradeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="升级条件" min-width="120">
          <template slot-scope="scope">
            {{ formatUpgradeCondition(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="积分倍数" min-width="80">
          <template slot-scope="scope">
            {{ formatMultiple(scope.row.integralMultiple) }}
          </template>
        </el-table-column>
        <el-table-column label="直推/团队(%)" min-width="100">
          <template slot-scope="scope">
            {{ scope.row.directBrokerageRate || 0 }} / {{ scope.row.teamBrokerageRate || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣(%)" min-width="70" />
        <el-table-column label="状态" min-width="90">
          <template slot-scope="scope">
            <el-switch
              v-if="checkPermi(['admin:system:user:level:use'])"
              v-model="scope.row.isShow"
              :active-value="true"
              :inactive-value="false"
              active-text="开"
              inactive-text="关"
              disabled
              @click.native="onchangeIsShow(scope.row)"
            />
            <span v-else>{{ scope.row.isShow ? '开启' : '关闭' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <a @click="edit(scope.row)" v-hasPermi="['admin:system:user:level:update']">编辑</a>
            <el-divider direction="vertical"></el-divider>
            <a @click="handleDelete(scope.row.id)" v-hasPermi="['admin:system:user:level:delete']">删除</a>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <creat-grade ref="grades" />
  </div>
</template>

<script>
import { levelListApi, levelUseApi, levelDeleteApi } from '@/api/user';
import creatGrade from './creatGrade';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'Grade',
  components: { creatGrade },
  data() {
    return {
      listLoading: true,
      tableData: {
        data: [],
      },
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    checkPermi,
    upgradeTypeText(type) {
      return type === 2 ? '单笔订单' : '累计消费';
    },
    formatUpgradeCondition(row) {
      const amount = row.experience != null ? row.experience : 0;
      if (row.upgradeType === 2) {
        return `单笔 ≥ ${amount} 元`;
      }
      return `累计 ≥ ${amount} 元`;
    },
    formatMultiple(val) {
      if (val == null || val === '') return '1';
      return Number(val) + ' 倍';
    },
    add() {
      this.$refs.grades.formData = {
        name: '',
        grade: 1,
        discount: 100,
        experience: 0,
        upgradeType: 1,
        integralMultiple: 1,
        directBrokerageRate: 30,
        teamBrokerageRate: 0,
        icon: '',
        remark: '',
        id: null,
        isShow: true,
      };
      this.$refs.grades.dialogVisible = true;
    },
    edit(row) {
      this.$refs.grades.formData = { id: row.id };
      this.$refs.grades.dialogVisible = true;
    },
    getList() {
      this.listLoading = true;
      levelListApi()
        .then((res) => {
          this.tableData.data = res || [];
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    handleDelete(id) {
      this.$modalSure('删除吗？删除会导致对应用户等级数据清空，请谨慎操作！').then(() => {
        levelDeleteApi(id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
    onchangeIsShow(row) {
      if (row.isShow === false) {
        row.isShow = true;
        levelUseApi({ id: row.id, isShow: row.isShow })
          .then(() => {
            this.$message.success('修改成功');
            this.getList();
          })
          .catch(() => {
            row.isShow = false;
          });
      } else {
        this.$modalSure('该操作会导致对应用户等级隐藏，请谨慎操作').then(() => {
          row.isShow = false;
          levelUseApi({ id: row.id, isShow: row.isShow })
            .then(() => {
              this.$message.success('修改成功');
              this.getList();
            })
            .catch(() => {
              row.isShow = true;
            });
        });
      }
    },
  },
};
</script>

<style scoped lang="scss">
.card-title {
  font-weight: 600;
  line-height: 32px;
}
.mb15 {
  margin-bottom: 15px;
}
.fr {
  float: right;
}
.el-switch.is-disabled {
  opacity: 1;
}
::v-deep .el-switch__label {
  cursor: pointer !important;
}
</style>
