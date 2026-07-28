<template>
  <div class="divBox">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="card-title">团队等级</span>
        <el-button
          type="primary"
          class="fr"
          @click="add"
          v-hasPermi="['admin:system:team:level:save']"
          >添加团队等级</el-button
        >
      </div>
      <el-alert
        class="mb15"
        type="info"
        :closable="false"
        title="说明：团队等级独立于会员等级。升级门槛为自购订单金额与团队订单金额；自购门槛须不低于上一级，团队门槛须高于上一级；团队极差比例须随等级序号递增。"
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
        <el-table-column label="自购门槛(元)" min-width="100">
          <template slot-scope="scope">
            {{ scope.row.selfOrderAmount != null ? scope.row.selfOrderAmount : 0 }}
          </template>
        </el-table-column>
        <el-table-column label="团队门槛(元)" min-width="100">
          <template slot-scope="scope">
            {{ scope.row.teamOrderAmount != null ? scope.row.teamOrderAmount : 0 }}
          </template>
        </el-table-column>
        <el-table-column label="极差/平级(%)" min-width="100">
          <template slot-scope="scope">
            {{ getConfigRate(scope.row, 'teamBrokerageRate') }} / {{ getConfigRate(scope.row, 'peerAwardRate') }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="权益描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" min-width="90">
          <template slot-scope="scope">
            <el-switch
              v-if="checkPermi(['admin:system:team:level:use'])"
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
            <a @click="edit(scope.row)" v-hasPermi="['admin:system:team:level:update']">编辑</a>
            <el-divider direction="vertical"></el-divider>
            <a @click="handleDelete(scope.row.id)" v-hasPermi="['admin:system:team:level:delete']">删除</a>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <creat-team-grade ref="teamGrades" />
  </div>
</template>

<script>
import {
  teamLevelListApi,
  teamLevelUseApi,
  teamLevelDeleteApi,
} from '@/api/teamLevel';
import creatTeamGrade from './creatTeamGrade';
import { checkPermi } from '@/utils/permission';

export default {
  name: 'TeamGrade',
  components: { creatTeamGrade },
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
    getConfigRate(row, field) {
      if (row.config && row.config[field] != null) {
        return row.config[field];
      }
      return 0;
    },
    add() {
      this.$refs.teamGrades.openCreate();
    },
    edit(row) {
      this.$refs.teamGrades.openEdit(row.id);
    },
    getList() {
      this.listLoading = true;
      teamLevelListApi()
        .then((res) => {
          this.tableData.data = res || [];
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    handleDelete(id) {
      this.$modalSure('删除吗？将同时删除该等级的团队奖配置，请谨慎操作！').then(() => {
        teamLevelDeleteApi(id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
    onchangeIsShow(row) {
      if (row.isShow === false) {
        row.isShow = true;
        teamLevelUseApi({ id: row.id, isShow: row.isShow })
          .then(() => {
            this.$message.success('修改成功');
            this.getList();
          })
          .catch(() => {
            row.isShow = false;
          });
      } else {
        this.$modalSure('该操作会导致该团队等级隐藏，请谨慎操作').then(() => {
          row.isShow = false;
          teamLevelUseApi({ id: row.id, isShow: row.isShow })
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
