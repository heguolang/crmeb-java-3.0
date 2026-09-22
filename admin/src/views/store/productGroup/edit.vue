<template>
  <div class="divBox">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>{{ form.id ? '编辑商品分组' : '新建商品分组' }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" size="small" v-loading="loading">
        <el-form-item label="分组名" prop="name">
          <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="请输入分组名称" class="w400" />
        </el-form-item>
        <el-form-item label="起卖数" prop="minBuy">
          <el-input-number v-model="form.minBuy" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="权限" prop="permissionType">
          <el-radio-group v-model="form.permissionType">
            <el-radio label="all">全部会员</el-radio>
            <el-radio label="promoter">仅分销商</el-radio>
            <el-radio label="agent">仅代理商</el-radio>
            <el-radio label="stock_agent">仅订货商</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会员分组">
          <el-select
            v-model="form.userGroupIds"
            multiple
            clearable
            filterable
            placeholder="选择分组(可多选)，不选则不限"
            class="w400"
          >
            <el-option v-for="item in userGroupOptions" :key="item.id" :label="item.groupName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select
            v-model="form.userLevelIds"
            multiple
            clearable
            filterable
            placeholder="所有等级"
            class="w400"
            @change="onLevelChange"
          >
            <el-option v-for="item in userLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否仅限所选等级">
          <el-radio-group v-model="form.levelOnly">
            <el-radio :label="false">否</el-radio>
            <el-radio :label="true">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否限购一件">
          <el-radio-group v-model="form.limitOne">
            <el-radio :label="false">否</el-radio>
            <el-radio :label="true">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="布局方式">
          <el-radio-group v-model="form.layout">
            <el-radio label="double">双列商品</el-radio>
            <el-radio label="single">单列商品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容效果">
          <el-radio-group v-model="form.style">
            <el-radio :label="1">样式一</el-radio>
            <el-radio :label="2">样式二</el-radio>
            <el-radio :label="3">样式三</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片角标">
          <div class="acea-row row-middle">
            <div v-if="form.badge" class="badge-box" v-viewer>
              <img :src="form.badge" />
              <i class="el-icon-close" @click="form.badge = ''" />
            </div>
            <el-button size="mini" @click="openImage">{{ form.badge ? '修改' : '上传' }}</el-button>
            <span class="tip ml10">建议尺寸：60*40</span>
          </div>
        </el-form-item>
        <el-form-item label="标题多行显示">
          <el-checkbox v-model="form.titleMulti">多行显示</el-checkbox>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="tip ml10">序号越大越靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="关联商品">
          <el-button type="primary" plain size="mini" @click="goodsDialog = true">选择商品</el-button>
          <div class="goods-wrap mt10" v-if="selectedGoods.length">
            <div class="goods-item" v-for="(g, idx) in selectedGoods" :key="g.id">
              <img :src="g.image" />
              <div class="name">{{ g.storeName || g.store_name }}</div>
              <i class="el-icon-close" @click="removeGoods(idx)" />
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog :visible.sync="goodsDialog" title="选择商品" width="900px" append-to-body>
      <goods-list v-if="goodsDialog" :ischeckbox="true" :isdiy="true" @getProductId="onSelectGoods" />
    </el-dialog>
  </div>
</template>

<script>
import goodsList from '@/components/goodsList';
import { groupListApi, levelListApi } from '@/api/user';
import { productListbyidsApi } from '@/api/store';
import { productGroupInfoApi, productGroupSaveApi, productGroupUpdateApi } from '@/api/productGroup';

export default {
  name: 'StoreProductGroupEdit',
  components: { goodsList },
  data() {
    return {
      loading: false,
      saving: false,
      goodsDialog: false,
      userGroupOptions: [],
      userLevelOptions: [],
      selectedGoods: [],
      form: {
        id: null,
        name: '',
        permissionType: 'all',
        userGroupIds: [],
        userLevelIds: [],
        levelOnly: false,
        minBuy: 1,
        limitOne: false,
        layout: 'double',
        style: 1,
        badge: '',
        titleMulti: false,
        sort: 0,
        status: true,
        productIds: [],
      },
      rules: {
        name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
        permissionType: [{ required: true, message: '请选择权限', trigger: 'change' }],
        minBuy: [{ required: true, message: '请输入起卖数', trigger: 'blur' }],
      },
    };
  },
  mounted() {
    this.loadOptions();
    const id = this.$route.params.id;
    if (id) {
      this.form.id = Number(id);
      this.loadInfo(this.form.id);
    }
  },
  methods: {
    loadOptions() {
      groupListApi({ page: 1, limit: 200 })
        .then((res) => {
          this.userGroupOptions = res.list || [];
        })
        .catch(() => {});
      levelListApi()
        .then((res) => {
          this.userLevelOptions = Array.isArray(res) ? res : res.list || [];
        })
        .catch(() => {});
    },
    loadInfo(id) {
      this.loading = true;
      productGroupInfoApi({ id })
        .then((res) => {
          this.form = {
            id: res.id,
            name: res.name,
            permissionType: res.permissionType || 'all',
            userGroupIds: this.parseIds(res.userGroupIds),
            userLevelIds: this.parseIds(res.userLevelIds),
            levelOnly: !!res.levelOnly,
            minBuy: res.minBuy || 1,
            limitOne: !!res.limitOne,
            layout: res.layout || 'double',
            style: res.style || 1,
            badge: res.badge || '',
            titleMulti: !!res.titleMulti,
            sort: res.sort || 0,
            status: res.status !== false,
            productIds: res.productIds || [],
          };
          if (this.form.productIds.length) {
            productListbyidsApi(this.form.productIds.join(','))
              .then((list) => {
                this.selectedGoods = Array.isArray(list) ? list : list.list || [];
              })
              .catch(() => {});
          }
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    parseIds(val) {
      if (Array.isArray(val)) return val.map(Number).filter(Boolean);
      if (!val) return [];
      return String(val)
        .split(',')
        .map((s) => Number(s.trim()))
        .filter((n) => n > 0);
    },
    onLevelChange(val) {
      if (!val || !val.length) {
        this.form.levelOnly = false;
      }
    },
    openImage() {
      this.$modalUpload((img) => {
        if (Array.isArray(img) && img.length) {
          this.form.badge = img[0].sattDir || img[0].url || '';
        } else if (img) {
          this.form.badge = img.sattDir || img.url || img;
        }
      }, '1');
    },
    onSelectGoods(data) {
      this.goodsDialog = false;
      const list = Array.isArray(data) ? data : [data];
      const map = new Map();
      [...this.selectedGoods, ...list].forEach((g) => {
        if (g && g.id) map.set(g.id, g);
      });
      this.selectedGoods = Array.from(map.values());
      this.form.productIds = this.selectedGoods.map((g) => g.id);
    },
    removeGoods(idx) {
      this.selectedGoods.splice(idx, 1);
      this.form.productIds = this.selectedGoods.map((g) => g.id);
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        const payload = {
          name: this.form.name,
          permissionType: this.form.permissionType,
          userGroupIds: this.form.userGroupIds,
          userLevelIds: this.form.userLevelIds,
          levelOnly: this.form.levelOnly,
          minBuy: this.form.minBuy,
          limitOne: this.form.limitOne,
          layout: this.form.layout,
          style: this.form.style,
          badge: this.form.badge,
          titleMulti: this.form.titleMulti,
          sort: this.form.sort,
          status: this.form.status,
          productIds: this.form.productIds,
        };
        this.saving = true;
        const req = this.form.id
          ? productGroupUpdateApi({ id: this.form.id }, payload)
          : productGroupSaveApi(payload);
        req
          .then(() => {
            this.$message.success('保存成功');
            this.saving = false;
            this.$router.push('/store/productGroup');
          })
          .catch(() => {
            this.saving = false;
          });
      });
    },
  },
};
</script>

<style scoped lang="scss">
.w400 {
  width: 400px;
}
.tip {
  color: #909399;
  font-size: 12px;
}
.ml10 {
  margin-left: 10px;
}
.mt10 {
  margin-top: 10px;
}
.badge-box {
  position: relative;
  width: 60px;
  height: 40px;
  margin-right: 10px;
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
  .el-icon-close {
    position: absolute;
    top: -6px;
    right: -6px;
    cursor: pointer;
    background: #fff;
    border-radius: 50%;
  }
}
.goods-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.goods-item {
  position: relative;
  width: 90px;
  text-align: center;
  img {
    width: 70px;
    height: 70px;
    object-fit: cover;
    border-radius: 4px;
  }
  .name {
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .el-icon-close {
    position: absolute;
    top: -4px;
    right: 4px;
    cursor: pointer;
    background: #fff;
    border-radius: 50%;
  }
}
</style>
