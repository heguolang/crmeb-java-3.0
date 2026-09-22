<template>
  <div class="divBox">
    <el-card class="box-card mb14" shadow="never">
      <div class="config-row">
        <div class="config-item">
          <div class="config-title">设置分组权限后，其它会员能否看到分组中商品</div>
          <div class="config-desc">
            如果选择「能」，会员可以在店铺首页和商品列表页看到分组中商品，但无法进入详情页购买。
          </div>
          <el-switch
            v-model="config.otherVisible"
            active-text="能"
            inactive-text="不能"
            v-hasPermi="['admin:store:product:group:config']"
            @change="saveConfig"
          />
        </div>
        <div class="config-item">
          <div class="config-title">自定义不符合商品分组权限条件的提示语</div>
          <div class="config-desc">如果选择「能」，商家可以自定义不符合商品分组权限条件的提示语。</div>
          <el-switch
            v-model="config.denyTipEnable"
            active-text="能"
            inactive-text="不能"
            v-hasPermi="['admin:store:product:group:config']"
            @change="saveConfig"
          />
          <el-input
            v-if="config.denyTipEnable"
            v-model="config.denyTip"
            class="mt10"
            maxlength="100"
            show-word-limit
            placeholder="请输入提示语"
            @blur="saveConfig"
          />
        </div>
      </div>
    </el-card>

    <el-card class="box-card" shadow="never">
      <div class="clearfix mb14">
        <el-button type="primary" size="small" v-hasPermi="['admin:store:product:group:save']" @click="goEdit()">
          新建分组
        </el-button>
        <div class="fr acea-row">
          <el-input
            v-model="tableFrom.name"
            placeholder="分组名称"
            clearable
            size="small"
            class="selWidth"
            @keyup.enter.native="search"
          />
          <el-button type="primary" size="small" class="ml10" icon="el-icon-search" @click="search">查询</el-button>
        </div>
      </div>
      <el-table v-loading="listLoading" :data="tableData.data" size="small" style="width: 100%">
        <el-table-column prop="name" label="分组名称" min-width="160" />
        <el-table-column prop="productCount" label="商品数量" width="100" />
        <el-table-column label="权限" min-width="120">
          <template slot-scope="{ row }">{{ permLabel(row.permissionType) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template slot-scope="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-switch
              v-model="row.status"
              v-hasPermi="['admin:store:product:group:update']"
              @change="(val) => changeStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="{ row }">
            <a v-hasPermi="['admin:store:product:group:update']" @click="goEdit(row.id)">编辑</a>
            <el-divider direction="vertical" />
            <a v-hasPermi="['admin:store:product:group:delete']" @click="handleDelete(row.id)">删除</a>
          </template>
        </el-table-column>
      </el-table>
      <div class="block">
        <el-pagination
          background
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import {
  productGroupListApi,
  productGroupDeleteApi,
  productGroupStatusApi,
  productGroupConfigApi,
  productGroupConfigSaveApi,
} from '@/api/productGroup';

const PERM_MAP = {
  all: '全部会员',
  promoter: '仅分销商',
  agent: '仅代理商',
  stock_agent: '仅订货商',
};

export default {
  name: 'StoreProductGroup',
  data() {
    return {
      listLoading: false,
      tableFrom: { page: 1, limit: 20, name: '' },
      tableData: { data: [], total: 0 },
      config: {
        otherVisible: false,
        denyTipEnable: false,
        denyTip: '您暂无权限查看该商品',
      },
    };
  },
  mounted() {
    this.loadConfig();
    this.getList();
  },
  methods: {
    permLabel(type) {
      return PERM_MAP[type] || type || '-';
    },
    loadConfig() {
      productGroupConfigApi()
        .then((res) => {
          this.config = {
            otherVisible: !!res.otherVisible,
            denyTipEnable: !!res.denyTipEnable,
            denyTip: res.denyTip || '您暂无权限查看该商品',
          };
        })
        .catch(() => {});
    },
    saveConfig() {
      productGroupConfigSaveApi({ ...this.config }).then(() => {
        this.$message.success('配置已保存');
      });
    },
    search() {
      this.tableFrom.page = 1;
      this.getList();
    },
    getList() {
      this.listLoading = true;
      productGroupListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list || [];
          this.tableData.total = res.total || 0;
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
      this.getList();
    },
    goEdit(id) {
      this.$router.push({
        path: id ? `/store/productGroup/edit/${id}` : '/store/productGroup/edit',
      });
    },
    changeStatus(row, val) {
      productGroupStatusApi({ id: row.id, status: val })
        .then(() => {
          this.$message.success('状态已更新');
        })
        .catch(() => {
          row.status = !val;
        });
    },
    handleDelete(id) {
      this.$modalSure('确定删除该商品分组吗？').then(() => {
        productGroupDeleteApi({ id }).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
  },
};
</script>

<style scoped lang="scss">
.config-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.config-item {
  flex: 1;
  min-width: 320px;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 4px;
}
.config-title {
  font-weight: 600;
  margin-bottom: 6px;
}
.config-desc {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  margin-bottom: 10px;
}
.selWidth {
  width: 220px;
}
.mt10 {
  margin-top: 10px;
}
.mb14 {
  margin-bottom: 14px;
}
.ml10 {
  margin-left: 10px;
}
</style>
