<template>
  <div class="divBox">
    <el-card>
      <div slot="header" class="clearfix">
        <span>链动产品</span>
        <el-button style="float: right" type="primary" size="small" @click="openAdd">添加链动产品</el-button>
      </div>
      <el-alert
        title="从商品库选择礼包商品，并配置直推奖、见单奖、平级奖。启用后购买该商品即走链动升级与分佣；产品奖励优先于「链动配置」里的全局奖励规则。"
        type="info"
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-form inline size="small">
        <el-form-item label="关键词">
          <el-input v-model="keywords" clearable placeholder="商品ID/名称" @keyup.enter.native="load" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="status" clearable placeholder="全部">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" border v-loading="loading">
        <el-table-column label="商品" min-width="220">
          <template slot-scope="scope">
            <div class="goods">
              <img v-if="scope.row.image" :src="scope.row.image" class="img" />
              <div>
                <div>{{ scope.row.storeName || '商品#' + scope.row.productId }}</div>
                <div class="sub">ID {{ scope.row.productId }} · 售价 ¥{{ scope.row.price || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="直推奖" width="110" align="center">
          <template slot-scope="scope">¥{{ scope.row.directAmount }}</template>
        </el-table-column>
        <el-table-column label="见单奖" width="110" align="center">
          <template slot-scope="scope">¥{{ scope.row.seeAmount }}</template>
        </el-table-column>
        <el-table-column label="平级奖" width="110" align="center">
          <template slot-scope="scope">{{ scope.row.peerRatio }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color:#f56c6c" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 12px"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="limit"
        :current-page.sync="page"
        @current-change="load"
      />
    </el-card>

    <el-dialog :title="form.id ? '编辑链动产品' : '添加链动产品'" :visible.sync="dialog" width="520px">
      <el-form label-width="110px" size="small">
        <el-form-item label="选择商品" required>
          <div v-if="form.productId" class="goods pick">
            <img v-if="form.image" :src="form.image" class="img" />
            <div>
              <div>{{ form.storeName || '商品#' + form.productId }}</div>
              <div class="sub">ID {{ form.productId }}</div>
            </div>
            <el-button type="text" @click="pickGoods">重选</el-button>
          </div>
          <el-button v-else type="primary" plain @click="pickGoods">从商品库选择</el-button>
        </el-form-item>
        <el-form-item label="直推奖(元)">
          <el-input-number v-model="form.directAmount" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="见单奖(元)">
          <el-input-number v-model="form.seeAmount" :min="0" :precision="2" :step="1" />
          <span class="hint">老板见单奖励；各代默认按此金额</span>
        </el-form-item>
        <el-form-item label="平级奖(%)">
          <el-input-number v-model="form.peerRatio" :min="0" :max="100" :precision="2" :step="1" />
          <span class="hint">占见单奖比例</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="如：398代理礼包" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { chainProductListApi, chainProductSaveApi, chainProductDeleteApi } from '@/api/chain';

export default {
  name: 'ChainProduct',
  data() {
    return {
      loading: false,
      saving: false,
      list: [],
      total: 0,
      page: 1,
      limit: 20,
      keywords: '',
      status: undefined,
      dialog: false,
      form: this.emptyForm(),
    };
  },
  created() {
    this.load();
  },
  methods: {
    emptyForm() {
      return {
        id: undefined,
        productId: undefined,
        storeName: '',
        image: '',
        price: undefined,
        directAmount: 100,
        seeAmount: 100,
        peerRatio: 10,
        status: 1,
        remark: '',
      };
    },
    load() {
      this.loading = true;
      chainProductListApi({
        page: this.page,
        limit: this.limit,
        keywords: this.keywords || undefined,
        status: this.status,
      })
        .then((res) => {
          const data = res.data || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    openAdd() {
      this.form = this.emptyForm();
      this.dialog = true;
    },
    openEdit(row) {
      this.form = {
        id: row.id,
        productId: row.productId,
        storeName: row.storeName,
        image: row.image,
        price: row.price,
        directAmount: Number(row.directAmount || 0),
        seeAmount: Number(row.seeAmount || 0),
        peerRatio: Number(row.peerRatio || 0),
        status: row.status === 0 ? 0 : 1,
        remark: row.remark || '',
      };
      this.dialog = true;
    },
    pickGoods() {
      const self = this;
      this.$modalGoodList(function (row) {
        self.form.productId = row.id;
        self.form.storeName = row.storeName || row.name || '';
        self.form.image = row.image || '';
        self.form.price = row.price;
      });
    },
    submit() {
      if (!this.form.productId) {
        this.$message.warning('请选择商品');
        return;
      }
      this.saving = true;
      chainProductSaveApi({
        id: this.form.id,
        productId: this.form.productId,
        directAmount: this.form.directAmount,
        seeAmount: this.form.seeAmount,
        peerRatio: this.form.peerRatio,
        status: this.form.status,
        remark: this.form.remark,
      })
        .then(() => {
          this.$message.success('保存成功');
          this.dialog = false;
          this.load();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    remove(row) {
      this.$confirm('确定删除该链动产品？删除后不再作为代理礼包。', '提示', { type: 'warning' })
        .then(() => chainProductDeleteApi({ id: row.id }))
        .then(() => {
          this.$message.success('已删除');
          this.load();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
.goods {
  display: flex;
  align-items: center;
  gap: 10px;
}
.goods.pick {
  justify-content: flex-start;
}
.img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f5f5;
}
.sub {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}
.hint {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>
