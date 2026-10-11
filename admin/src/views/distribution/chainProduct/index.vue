<template>
  <div class="divBox">
    <el-card>
      <div slot="header" class="clearfix">
        <span>链动产品</span>
        <el-button style="float: right" type="primary" size="small" @click="openAdd">添加链动产品</el-button>
      </div>
      <el-alert
        title="从商品库选择礼包商品并配置该商品的链动奖励。购买该商品即走链动升级与分佣；商品奖励优先于「链动配置」里的全局奖励规则。留空则沿用全局默认。"
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
        <el-table-column label="直推奖" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="num(scope.row.directAmount) > 0">¥{{ scope.row.directAmount }}</span>
            <span v-else class="sub">沿用全局</span>
          </template>
        </el-table-column>
        <el-table-column label="见点奖" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="num(scope.row.seeRatio) > 0">{{ scope.row.seeRatio }}%</span>
            <span v-else-if="num(scope.row.seeAmount) > 0" class="sub">¥{{ scope.row.seeAmount }}(旧)</span>
            <span v-else class="sub">沿用全局</span>
          </template>
        </el-table-column>
        <el-table-column label="平级奖" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="num(scope.row.peerRatio) > 0">{{ scope.row.peerRatio }}%</span>
            <span v-else class="sub">沿用全局</span>
          </template>
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
        <template slot="empty"><div class="empty">还没有链动产品，点右上角「添加链动产品」选择礼包商品</div></template>
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

    <el-dialog
      :title="form.id ? '编辑链动产品' : '添加链动产品'"
      :visible.sync="dialog"
      width="560px"
      append-to-body
    >
      <el-form label-width="120px" size="small">
        <el-form-item label="选择商品" required>
          <div v-if="form.productId" class="goods pick">
            <img v-if="form.image" :src="form.image" class="img" />
            <div>
              <div>{{ form.storeName || '商品#' + form.productId }}</div>
              <div class="sub">ID {{ form.productId }}<span v-if="form.price"> · 售价 ¥{{ form.price }}</span></div>
            </div>
            <el-button type="text" @click="pickGoods">重选</el-button>
          </div>
          <el-button v-else type="primary" plain @click="pickGoods">从商品库选择</el-button>
        </el-form-item>
        <el-form-item label="直推奖(元)">
          <el-input-number v-model="form.directAmount" :min="0" :precision="2" :step="10" />
          <span class="hint">推荐人直接获得，固定金额</span>
        </el-form-item>
        <el-form-item label="见点奖(%)">
          <el-input-number v-model="form.seeRatio" :min="0" :max="100" :precision="2" :step="1" />
          <span class="hint">直属上级{{ lv.boss }}获得，订单实付 × 比例</span>
        </el-form-item>
        <el-form-item label="平级奖(%)">
          <el-input-number v-model="form.peerRatio" :min="0" :max="100" :precision="2" :step="1" />
          <span class="hint">占下级见点奖比例</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="如：399 代理礼包" />
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
import { chainProductListApi, chainProductSaveApi, chainProductDeleteApi, chainConfigGetApi } from '@/api/chain';

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
      lv: { agent: '代理', boss: '老板' },
    };
  },
  created() {
    this.load();
    this.loadLevelNames();
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
        seeRatio: 10,
        peerRatio: 10,
        status: 1,
        remark: '',
      };
    },
    num(v) {
      return Number(v || 0);
    },
    loadLevelNames() {
      chainConfigGetApi()
        .then((res) => {
          const cfg = res.data || res || {};
          this.lv = {
            agent: cfg.chain_level_agent_name || '代理',
            boss: cfg.chain_level_boss_name || '老板',
          };
        })
        .catch(() => {});
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
          const data = res.data || res || {};
          this.list = data.list || [];
          this.total = data.total || 0;
        })
        .catch(() => {
          this.list = [];
          this.total = 0;
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
        directAmount: this.num(row.directAmount),
        seeRatio: this.num(row.seeRatio) > 0 ? this.num(row.seeRatio) : 10,
        peerRatio: this.num(row.peerRatio),
        status: row.status === 0 ? 0 : 1,
        remark: row.remark || '',
      };
      this.dialog = true;
    },
    pickGoods() {
      const self = this;
      this.$modalGoodList(function (row) {
        if (!row || !row.id) {
          return;
        }
        self.$set(self.form, 'productId', row.id);
        self.$set(self.form, 'storeName', row.storeName || row.name || '');
        self.$set(self.form, 'image', row.image || '');
        self.$set(self.form, 'price', row.price);
      });
    },
    submit() {
      if (!this.form.productId) {
        this.$message.warning('请先从商品库选择商品，再保存');
        return;
      }
      this.saving = true;
      chainProductSaveApi({
        id: this.form.id,
        productId: this.form.productId,
        directAmount: this.form.directAmount,
        seeRatio: this.form.seeRatio,
        peerRatio: this.form.peerRatio,
        status: this.form.status,
        remark: this.form.remark,
      })
        .then(() => {
          this.$message.success('保存成功');
          this.dialog = false;
          this.load();
        })
        .catch(() => {})
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
.empty {
  padding: 24px 0;
  color: #909399;
}
</style>
