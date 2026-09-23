<template>
  <div class="pg-editor">
    <!-- ================= 顶部操作条 ================= -->
    <div class="pg-topbar">
      <div class="pg-topbar__left">
        <i class="el-icon-back" @click="$router.back()" />
        <span class="pg-topbar__title">{{ form.id ? '编辑商品分组' : '新建商品分组' }}</span>
        <span v-if="form.name" class="pg-topbar__sub">{{ form.name }}</span>
      </div>
      <div class="pg-topbar__tabs">
        <span :class="['pg-tab', { on: activeTab === 'design' }]" @click="switchTab('design')">页面装修</span>
        <span :class="['pg-tab', { on: activeTab === 'base' }]" @click="switchTab('base')">基础设置</span>
      </div>
      <div class="pg-topbar__actions">
        <el-button
          v-if="activeTab === 'design' && form.id"
          size="small"
          icon="el-icon-view"
          @click="openPreview"
        >
          预览页面
        </el-button>
        <el-button size="small" @click="$router.back()">取消</el-button>
        <el-button
          v-if="activeTab === 'design'"
          type="primary"
          size="small"
          :loading="saving"
          v-hasPermi="['admin:store:product:group:update']"
          @click="saveDiy"
        >
          保存装修
        </el-button>
        <el-button
          v-else
          type="primary"
          size="small"
          :loading="saving"
          v-hasPermi="[isEdit ? 'admin:store:product:group:update' : 'admin:store:product:group:save']"
          @click="submit"
        >
          保存
        </el-button>
      </div>
    </div>

    <!-- ================= 页面装修：内嵌系统装修器 ================= -->
    <div v-if="activeTab === 'design'" class="pg-diy">
      <div v-if="!form.id" class="pg-diy__empty">
        <i class="el-icon-info" />
        <p>请先在「基础设置」保存分组，保存后即可装修分组页面</p>
        <el-button type="primary" size="small" @click="switchTab('base')">去基础设置</el-button>
      </div>
      <div v-else-if="!diyReady" v-loading="diyLoading" class="pg-diy__empty" />
      <template v-else>
        <!-- 分组商品提示：让装修时知道商品在哪、怎么摆位置 -->
        <div class="pg-goodsbar" :class="{ 'is-empty': !goodsCount }">
          <i class="el-icon-goods pg-goodsbar__icon" />
          <div class="pg-goodsbar__text">
            <template v-if="goodsCount">
              本分组有 <b>{{ goodsCount }}</b> 件商品，添加「商品选项卡」组件即可在装修里自由摆放位置；
              <span class="pg-goodsbar__muted">未添加时，商品会自动展示在装修内容下方。</span>
            </template>
            <template v-else>
              本分组还没有商品，先到「基础设置 → 功能设置 → 选择商品」添加，再回来装修。
            </template>
          </div>
          <el-button
            size="mini"
            type="primary"
            plain
            :disabled="!goodsCount"
            @click="insertGroupGoods"
          >
            一键插入分组商品
          </el-button>
        </div>
        <diy-index :key="diyKey" ref="diy" />
      </template>
    </div>

    <div v-show="activeTab === 'base'" class="pg-body" v-loading="loading">
      <!-- ================= 左：手机预览 ================= -->
      <div class="pg-preview">
        <div class="phone">
          <div class="phone__status">
            <span>9:41</span>
            <span class="phone__status-icons">
              <i class="el-icon-connection" />
              <i class="el-icon-bell" />
            </span>
          </div>
          <div class="phone__nav">
            <i class="el-icon-close" />
            <span class="phone__nav-title">{{ form.name || '商品分组' }}</span>
            <i class="el-icon-more" />
          </div>
          <div class="phone__search">
            <i class="el-icon-search" />
            <span>商品搜索：输入商品、店铺名称</span>
          </div>
          <div class="phone__filter">
            <span class="on">默认</span>
            <span>价格</span>
            <span>销量</span>
            <span>人气</span>
          </div>

          <div class="phone__scroll">
            <div
              class="phone__goods"
              :class="[form.layout === 'single' ? 'is-single' : 'is-double', 'style-' + (form.style || 1)]"
            >
              <div v-for="(g, idx) in previewGoods" :key="idx" class="goods-card">
                <div class="goods-card__img">
                  <img :src="g.image" alt="" />
                  <img v-if="form.badge" class="goods-card__badge" :src="form.badge" alt="" />
                </div>
                <div class="goods-card__body">
                  <div class="goods-card__name" :class="{ 'is-multi': form.titleMulti }">
                    {{ g.name }}
                  </div>
                  <div class="goods-card__price">
                    <span class="now">¥{{ g.price }}</span>
                    <span v-if="g.otPrice" class="old">¥{{ g.otPrice }}</span>
                  </div>
                  <div class="goods-card__sold">已售{{ g.sold }}件</div>
                  <div class="goods-card__btn">立即购买</div>
                </div>
              </div>

              <!-- 未选商品：占位卡 -->
              <template v-if="!previewGoods.length">
                <div v-for="n in 4" :key="'ph' + n" class="goods-card is-placeholder">
                  <div class="goods-card__img">
                    <i class="el-icon-picture-outline" />
                  </div>
                  <div class="goods-card__body">
                    <div class="goods-card__name">第{{ ['一', '二', '三', '四'][n - 1] }}个商品</div>
                    <div class="goods-card__price"><span class="now">¥100.00</span></div>
                    <div class="goods-card__btn">立即购买</div>
                  </div>
                </div>
              </template>
            </div>

            <div class="phone__extras">
              <span v-if="form.minBuy > 1">{{ form.minBuy }} 件起售</span>
              <span v-if="form.limitOne">每人限购一件</span>
              <span v-if="!form.status" class="is-off">分组已关闭，前台不展示</span>
            </div>
          </div>
        </div>
        <div class="pg-preview__foot">
          {{ isEdit ? '当前商品数：' + selectedGoods.length : '选择商品后此处实时预览' }}
        </div>
      </div>

      <!-- ================= 右：基础设置 ================= -->
      <section class="pg-config" ref="configScroll">
        <h4 class="pg-config__title">基础设置</h4>

        <el-form ref="form" :model="form" :rules="rules" label-width="110px" size="small">
          <!-- 基本信息 -->
          <div class="pg-sec">
            <h5 class="pg-sec__title">基本信息</h5>
            <el-form-item label="分组名" prop="name">
              <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="请输入分组名称" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
              <span class="tip ml10">数值越大越靠前</span>
            </el-form-item>
            <el-form-item label="状态">
              <el-switch v-model="form.status" active-text="开启" inactive-text="关闭" />
              <span class="tip ml10">关闭后前台不展示该分组</span>
            </el-form-item>
          </div>

          <!-- 权限设置 -->
          <div class="pg-sec">
            <h5 class="pg-sec__title">权限设置</h5>
            <el-form-item label="可见权限" prop="permissionType">
              <el-radio-group v-model="form.permissionType" class="pg-perm" @change="onPermissionChange">
                <el-radio label="all">全部会员</el-radio>
                <el-radio label="promoter">仅分销商</el-radio>
                <el-radio label="agent">仅区域代理</el-radio>
                <el-radio label="stock_agent">仅订货商</el-radio>
                <el-radio label="team">仅社群团队</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="isMemberScope" label="会员分组">
              <el-select
                v-model="form.userGroupIds"
                multiple
                clearable
                filterable
                placeholder="选择分组(可多选)，不选则不限"
              >
                <el-option
                  v-for="item in userGroupOptions"
                  :key="item.id"
                  :label="item.groupName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item :label="levelLabel">
              <el-select v-model="currentLevels" multiple clearable filterable :placeholder="'所有' + levelLabel">
                <el-option v-for="item in currentLevelOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <div class="tip">等级数据来源：{{ levelSourceTip }}</div>
            </el-form-item>

            <el-form-item label="仅限所选等级">
              <el-radio-group v-model="form.levelOnly">
                <el-radio :label="false">否</el-radio>
                <el-radio :label="true">是</el-radio>
              </el-radio-group>
              <div class="tip">选「是」时，仅所选等级的用户可见、可购买</div>
            </el-form-item>
          </div>

          <!-- 商品设置 -->
          <div class="pg-sec">
            <h5 class="pg-sec__title">商品设置</h5>
            <el-form-item label="关联商品">
              <el-button type="primary" plain size="mini" @click="goodsDialog = true">选择商品</el-button>
              <span class="tip ml10">已选 {{ selectedGoods.length }} 个</span>
              <div v-if="selectedGoods.length" class="goods-wrap mt10">
                <div class="goods-item" v-for="(g, idx) in selectedGoods" :key="g.id">
                  <img :src="g.image" alt="" />
                  <div class="name">{{ g.storeName || g.store_name }}</div>
                  <i class="el-icon-close" @click="removeGoods(idx)" />
                </div>
              </div>
            </el-form-item>
            <el-form-item label="起卖数" prop="minBuy">
              <el-input-number v-model="form.minBuy" :min="1" :max="9999" controls-position="right" />
              <span class="tip ml10">件起售</span>
            </el-form-item>
            <el-form-item label="限购一件">
              <el-radio-group v-model="form.limitOne">
                <el-radio :label="false">否</el-radio>
                <el-radio :label="true">是</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="图片角标">
              <div class="acea-row row-middle">
                <div v-if="form.badge" class="badge-box">
                  <img :src="form.badge" alt="" />
                  <i class="el-icon-close" @click="form.badge = ''" />
                </div>
                <el-button size="mini" @click="openImage">{{ form.badge ? '修改' : '上传' }}</el-button>
                <span class="tip ml10">建议尺寸 60×40</span>
              </div>
            </el-form-item>
          </div>
        </el-form>
      </section>
    </div>

    <!-- ================= 页面预览 ================= -->
    <el-dialog
      title="页面预览"
      :visible.sync="previewVisible"
      width="840px"
      append-to-body
      destroy-on-close
      custom-class="pg-pv-dialog"
      @opened="renderPreviewQr"
    >
      <div class="pv">
        <div class="pv__tip" :class="{ 'is-warn': isDirty }">
          <i :class="isDirty ? 'el-icon-warning' : 'el-icon-info'" />
          <span v-if="isDirty">
            当前有<b>未保存</b>的装修改动，下方预览的是<b>已保存</b>的内容，请先点右上角「保存装修」。
          </span>
          <span v-else>
            预览的是已保存的装修内容；改动后请重新「保存装修」，再点「刷新预览」。
          </span>
        </div>

        <div class="pv__body">
          <!-- 左：内嵌 H5 实时预览 -->
          <div class="pv__phone">
            <div class="pv__phone-nav">
              <span class="pv__phone-dot" />
              <span class="pv__phone-title">{{ form.name || '商品分组' }}</span>
              <span class="pv__phone-refresh" @click="refreshPreview">
                <i class="el-icon-refresh" /> 刷新预览
              </span>
            </div>
            <iframe
              v-if="previewUrl"
              :key="previewKey"
              :src="previewUrl"
              class="pv__frame"
              frameborder="0"
            />
            <div v-else class="pv__frame-empty">请先在右侧填写 H5 访问域名</div>
          </div>

          <!-- 右：二维码 + 域名 + 链接 -->
          <div class="pv__side">
            <div ref="previewQr" class="pv__qr" />
            <p class="pv__qr-tip">手机扫码预览</p>

            <div class="pv__field">
              <label>H5 访问域名</label>
              <el-input
                v-model="previewBase"
                size="small"
                placeholder="如 http://app.qianxutec.com"
                @change="savePreviewBase"
              />
              <p class="pv__field-tip">本机调试填 HBuilderX 起的 H5 地址即可</p>
            </div>

            <div class="pv__url">{{ previewUrl || '—' }}</div>

            <div class="pv__ops">
              <el-button size="mini" type="primary" plain @click="copyPreviewUrl">复制链接</el-button>
              <el-button size="mini" plain @click="openPreviewWindow">新窗口打开</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="goodsDialog" title="选择商品" width="900px" append-to-body>
      <goods-list v-if="goodsDialog" :ischeckbox="true" :isdiy="true" @getProductId="onSelectGoods" />
    </el-dialog>
  </div>
</template>

<script>
import goodsList from '@/components/goodsList';
import DiyIndex from '@/views/design/theme_editor/devise/diyIndex.vue';
import { groupListApi, levelListApi } from '@/api/user';
import { stockLevelListApi } from '@/api/stock';
import { teamLevelAllApi } from '@/api/teamLevel';
import { distributorLevelListApi } from '@/api/distributorLevel';
import { productListbyidsApi } from '@/api/store';
import QRCode from 'qrcodejs2';
import {
  productGroupInfoApi,
  productGroupSaveApi,
  productGroupUpdateApi,
  productGroupThemeApi,
} from '@/api/productGroup';

/** H5 商城域名：与 app/config/app.js 的 HTTP_H5_URL 保持一致，可被用户改写并本地记忆 */
const DEFAULT_H5_BASE = 'http://app.qianxutec.com';
/** H5 预览域名的 localStorage key */
const H5_PREVIEW_BASE_KEY = 'pg_h5_preview_base';

/**
 * 区域代理等级：系统里是固定三级（eb_agent.level → 1=省级 2=市级 3=区级），
 * 不存在配置表，所以写死。千万不要接 stockLevelListApi —— 那是订货商的等级。
 */
const AGENT_LEVEL_OPTIONS = [
  { id: 1, name: '省代' },
  { id: 2, name: '市代' },
  { id: 3, name: '区代' },
];

export default {
  name: 'StoreProductGroupEdit',
  components: { goodsList, DiyIndex },
  // 内嵌的装修器（diyIndex）要求宿主提供这两个注入
  provide() {
    return {
      // 恢复默认等场景要求宿主重建编辑器
      reload: () => {
        this.diyKey += 1;
      },
      setDirty: (dirty) => {
        this.isDirty = dirty;
      },
    };
  },
  data() {
    return {
      loading: false,
      saving: false,
      goodsDialog: false,
      /** design=页面装修（内嵌装修器） base=基础设置 */
      activeTab: 'design',
      /** 分组的装修页 id（eb_theme.id），装修器读写它 */
      themeId: 0,
      diyReady: false,
      diyLoading: false,
      /** 变更即重建装修器，用于「恢复默认」 */
      diyKey: 0,
      isDirty: false,
      /** 预览弹窗 */
      previewVisible: false,
      /** H5 访问域名（可在弹窗里改，localStorage 记忆） */
      previewBase: '',
      /** 变更即强制 iframe 重载 */
      previewKey: 0,
      /** 分组内商品数量，用于装修页提示（详情接口已返回 productCount） */
      goodsCount: 0,
      userGroupOptions: [],
      memberLevelOptions: [],
      distributorLevelOptions: [],
      stockLevelOptions: [],
      /** 社群团队等级（运营 - 团队等级配置） */
      teamLevelOptions: [],
      selectedGoods: [],
      form: {
        id: null,
        name: '',
        permissionType: 'all',
        userGroupIds: [],
        userLevelIds: [],
        distributorLevelIds: [],
        /** 区域代理等级（固定 1=省代 2=市代 3=区代） */
        agentLevelIds: [],
        stockLevelIds: [],
        /** 社群团队等级（eb_system_team_level.id） */
        teamLevelIds: [],
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
  computed: {
    isEdit() {
      return !!this.form.id;
    },
    /** 是否按会员口径（会员分组字段只在全部会员下显示） */
    isMemberScope() {
      return this.form.permissionType === 'all';
    },
    /** 等级来源 key：按权限联动 */
    levelSource() {
      const t = this.form.permissionType;
      if (t === 'promoter') return 'distributor';
      if (t === 'agent') return 'agent';
      if (t === 'stock_agent') return 'stock';
      if (t === 'team') return 'team';
      return 'member';
    },
    levelLabel() {
      return {
        member: '会员等级',
        distributor: '分销商等级',
        agent: '区域代理等级',
        stock: '订货商等级',
        team: '社群团队等级',
      }[this.levelSource];
    },
    levelSourceTip() {
      return {
        member: '会员等级（用户 - 用户等级）',
        distributor: '分销商等级（运营 - 分销商 - 分销商等级）',
        agent: '区域代理级别（区域代理 - 代理商管理，固定省代/市代/区代三级）',
        stock: '订货商等级（订货商 - 订货商级别设置）',
        team: '社群团队等级（运营 - 团队等级配置）',
      }[this.levelSource];
    },
    currentLevelOptions() {
      return {
        member: this.memberLevelOptions,
        distributor: this.distributorLevelOptions,
        agent: AGENT_LEVEL_OPTIONS,
        stock: this.stockLevelOptions,
        team: this.teamLevelOptions,
      }[this.levelSource];
    },
    /** 当前权限对应的等级字段名 */
    currentLevelKey() {
      return {
        member: 'userLevelIds',
        distributor: 'distributorLevelIds',
        agent: 'agentLevelIds',
        stock: 'stockLevelIds',
        team: 'teamLevelIds',
      }[this.levelSource];
    },
    currentLevels: {
      get() {
        return this.form[this.currentLevelKey] || [];
      },
      set(val) {
        this.$set(this.form, this.currentLevelKey, val || []);
      },
    },
    previewGoods() {
      return this.selectedGoods.slice(0, 6).map((g) => {
        const raw = g.price != null && g.price !== '' ? g.price : g.otPrice;
        const ot = Number(g.otPrice || 0);
        return {
          id: g.id,
          name: g.storeName || g.store_name || '',
          image: g.image || '',
          price: raw != null ? Number(raw).toFixed(2) : '0.00',
          otPrice: ot > 0 ? ot.toFixed(2) : '',
          sold: g.sales || g.sold || 0,
        };
      });
    },
    /**
     * H5 落地页路径。
     * uni-app 的 h5 是 hash 路由（manifest.json → h5.router.mode = hash），必须带 `#/`
     */
    previewPath() {
      return `#/pages/activity/product_group/index?id=${this.form.id || 0}`;
    },
    /** 完整预览地址，域名没填时为空 */
    previewUrl() {
      const base = String(this.previewBase || '')
        .trim()
        .replace(/\/+$/, '');
      if (!base) return '';
      return `${base}/${this.previewPath}`;
    },
  },
  watch: {
    // 域名变化时重画二维码，并让 iframe 重新加载
    previewUrl() {
      this.previewKey += 1;
      if (this.previewVisible) {
        this.renderPreviewQr();
      }
    },
    // 装修器保存成功时会 setDirty(false)，据此收起「保存装修」的 loading
    isDirty(val) {
      if (!val && this.saving && this.activeTab === 'design') {
        this.saving = false;
      }
    },
  },
  mounted() {
    this.loadOptions();
    const id = this.$route.params.id;
    if (id) {
      this.form.id = Number(id);
      this.loadInfo(this.form.id);
    } else {
      // 新建分组：没有分组 id 就没有装修页，先落到基础设置
      this.activeTab = 'base';
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
          this.memberLevelOptions = Array.isArray(res) ? res : res.list || [];
        })
        .catch(() => {});
      distributorLevelListApi()
        .then((res) => {
          this.distributorLevelOptions = Array.isArray(res) ? res : res.list || [];
        })
        .catch(() => {});
      stockLevelListApi()
        .then((res) => {
          this.stockLevelOptions = Array.isArray(res) ? res : res.list || [];
        })
        .catch(() => {});
      teamLevelAllApi()
        .then((res) => {
          const list = Array.isArray(res) ? res : res.list || [];
          this.teamLevelOptions = list.map((it) => ({ id: it.id, name: it.name }));
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
            distributorLevelIds: this.parseIds(res.distributorLevelIds),
            agentLevelIds: this.parseIds(res.agentLevelIds),
            stockLevelIds: this.parseIds(res.stockLevelIds),
            teamLevelIds: this.parseIds(res.teamLevelIds),
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
          this.goodsCount = Number(res.productCount || (res.productIds || []).length || 0);
          if (this.form.productIds.length) {
            productListbyidsApi(this.form.productIds.join(','))
              .then((list) => {
                this.selectedGoods = Array.isArray(list) ? list : list.list || [];
              })
              .catch(() => {});
          }
          this.loading = false;
          // 默认停在「页面装修」，直接准备好装修器
          if (this.activeTab === 'design') {
            this.initDiy();
          }
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
    switchTab(tab) {
      if (tab === 'design' && !this.form.id) {
        this.$message.warning('请先保存分组基础信息，再装修页面');
        return;
      }
      this.activeTab = tab;
      if (tab === 'design' && !this.diyReady) {
        this.initDiy();
      }
    },
    /**
     * 进入装修：先向后端取（必要时懒创建）该分组的装修页 id，
     * 再把 query 铺好，最后才渲染 diyIndex ——
     * 装修器在 created 里读 $route.query（id=装修页id, type=home, page_type=micro），
     * 必须赶在它挂载之前把 query 写好。
     */
    async initDiy() {
      if (!this.form.id) return;
      this.diyLoading = true;
      try {
        const res = await productGroupThemeApi(this.form.id);
        const themeId = typeof res === 'object' && res !== null ? res.themeId || res.id : res;
        if (!themeId) throw new Error('未取得装修页');
        this.themeId = Number(themeId);
        await this.$router.replace({
          path: `/store/productGroup/edit/${this.form.id}`,
          query: {
            id: this.themeId,
            name: this.form.name || '商品分组',
            type: 'home',
            page_type: 'micro',
          },
        });
        this.diyReady = true;
      } catch (e) {
        this.diyReady = false;
        this.$message.error('装修页初始化失败，请稍后重试');
      } finally {
        this.diyLoading = false;
      }
    },
    saveDiy() {
      const diy = this.$refs.diy;
      if (!diy) {
        return this.$message.warning('装修器尚未加载完成');
      }
      this.saving = true;
      // saveConfig(1) = 只保存，不跳转（2 才会跳回微页面列表）
      diy.saveConfig(1);
      // 兜底：保存失败时 isDirty 不会被置回 false，避免按钮一直转圈
      clearTimeout(this._diySaveTimer);
      this._diySaveTimer = setTimeout(() => {
        this.saving = false;
      }, 15000);
    },
    /**
     * 一键往装修数据里插一个「商品选项卡」组件，商品来源自动选中当前分组。
     * 这样商品会出现在装修预览里，可直接拖动决定它在页面上的位置。
     */
    insertGroupGoods() {
      const diy = this.$refs.diy;
      if (!diy || typeof diy.appendProductGroupComponent !== 'function') {
        return this.$message.warning('装修器尚未加载完成，请稍候再试');
      }
      if (!this.goodsCount) {
        return this.$message.warning('本分组还没有商品，请先在「基础设置」里选择商品');
      }
      if (!diy.appendProductGroupComponent(this.form.id)) {
        return this.$message.error('插入失败，请稍后重试');
      }
      this.$message.success('已插入「商品选项卡」，拖动它即可调整位置；记得点「保存装修」');
    },
    /* ---------------- 页面预览 ---------------- */
    /** 打开预览弹窗：域名优先取上次记忆的，否则用内置默认 */
    openPreview() {
      if (!this.form.id) {
        return this.$message.warning('请先保存分组基础信息，再预览页面');
      }
      this.previewBase = localStorage.getItem(H5_PREVIEW_BASE_KEY) || DEFAULT_H5_BASE;
      this.previewVisible = true;
    },
    /** 改域名：存起来、重载 iframe、重画二维码 */
    savePreviewBase(val) {
      const base = String(val || '')
        .trim()
        .replace(/\/+$/, '');
      this.previewBase = base;
      localStorage.setItem(H5_PREVIEW_BASE_KEY, base);
      this.previewKey += 1;
      this.renderPreviewQr();
    },
    /** 生成预览地址二维码（依赖已引入的 qrcodejs2，与系统其它预览一致） */
    renderPreviewQr() {
      if (!this.previewUrl) return;
      this.$nextTick(() => {
        const el = this.$refs.previewQr;
        if (!el) return;
        el.innerHTML = '';
        // eslint-disable-next-line no-new
        new QRCode(el, {
          text: this.previewUrl,
          width: 168,
          height: 168,
          colorDark: '#000000',
          colorLight: '#ffffff',
          correctLevel: QRCode.CorrectLevel.H,
        });
      });
    },
    /** 重载 iframe：装修保存后 H5 里还是旧内容，需要手动刷新 */
    refreshPreview() {
      this.previewKey += 1;
      this.$message.success('已刷新预览');
    },
    copyPreviewUrl() {
      if (!this.previewUrl) {
        return this.$message.warning('请先填写 H5 访问域名');
      }
      const ok = () => this.$message.success('预览链接已复制');
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(this.previewUrl).then(ok).catch(() => this.legacyCopy(ok));
        return;
      }
      this.legacyCopy(ok);
    },
    /** http 环境下 navigator.clipboard 不可用时的兜底复制 */
    legacyCopy(ok) {
      const ta = document.createElement('textarea');
      ta.value = this.previewUrl;
      ta.setAttribute('readonly', 'readonly');
      ta.style.position = 'fixed';
      ta.style.top = '-9999px';
      document.body.appendChild(ta);
      ta.select();
      let done = false;
      try {
        done = document.execCommand('copy');
      } catch (e) {
        done = false;
      }
      document.body.removeChild(ta);
      done ? ok() : this.$message.warning('复制失败，请手动选择链接复制');
    },
    openPreviewWindow() {
      if (!this.previewUrl) {
        return this.$message.warning('请先填写 H5 访问域名');
      }
      window.open(this.previewUrl, '_blank');
    },
    onPermissionChange() {
      // 切换权限后等级源随之变化，非会员口径下会员分组不参与
      if (!this.isMemberScope) {
        this.form.userGroupIds = [];
      }
      if (!this.currentLevels.length) {
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
        if (!valid) {
          // 功能设置已全部平铺在右栏，校验失败时滚回顶部便于看到红字提示
          if (this.$refs.configScroll) {
            this.$refs.configScroll.scrollTop = 0;
          }
          return;
        }
        const payload = {
          name: this.form.name,
          permissionType: this.form.permissionType,
          userGroupIds: this.form.userGroupIds,
          userLevelIds: this.form.userLevelIds,
          distributorLevelIds: this.form.distributorLevelIds,
          agentLevelIds: this.form.agentLevelIds,
          stockLevelIds: this.form.stockLevelIds,
          teamLevelIds: this.form.teamLevelIds,
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
$primary: var(--prev-color-primary, #1890ff);

.pg-editor {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
  background: #f0f2f5;
}

/* ---------- 顶部 ---------- */
.pg-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
    .el-icon-back {
      cursor: pointer;
      font-size: 16px;
      color: #606266;
      &:hover {
        color: $primary;
      }
    }
  }
  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  &__sub {
    font-size: 13px;
    color: #909399;
  }
  /* 中间 Tab：页面装修 / 基础设置 */
  &__tabs {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.pg-tab {
  display: inline-block;
  padding: 5px 16px;
  font-size: 13px;
  color: #606266;
  background: #f4f6f8;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    color: $primary;
  }
  &.on {
    color: #fff;
    background: $primary;
  }
}

/* ---------- 装修页顶部的分组商品提示条 ---------- */
.pg-goodsbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  padding: 10px 14px;
  background: var(--prev-color-primary-light-9, #e8f4ff);
  border: 1px solid var(--prev-color-primary-light-7, #c6e2ff);
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  &__icon {
    font-size: 16px;
    color: $primary;
  }
  &__text {
    flex: 1;
    line-height: 20px;
    b {
      color: $primary;
      font-variant-numeric: tabular-nums;
    }
  }
  &__muted {
    color: #909399;
  }
  &.is-empty {
    background: #fdf6ec;
    border-color: #faecd8;
    .pg-goodsbar__icon {
      color: #e6a23c;
    }
  }
}

/* ---------- 页面装修（内嵌装修器） ---------- */
.pg-diy {
  flex: 1;
  min-height: calc(100vh - 180px);
  padding: 12px;
  background: #f0f2f5;
  // 装修器自身的 el-card 已经有内边距，这里去掉外层多余留白
  ::v-deep .el-card__body {
    padding: 0;
  }
  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 360px;
    background: #fff;
    border-radius: 4px;
    color: #909399;
    .el-icon-info {
      font-size: 34px;
      margin-bottom: 12px;
      color: #c0c4cc;
    }
    p {
      margin: 0 0 16px;
      font-size: 14px;
    }
  }
}

/* ---------- 主体三栏 ---------- */
.pg-body {
  display: flex;
  flex: 1;
  gap: 12px;
  padding: 12px;
  align-items: stretch;
  min-height: 620px;
}

/* ---------- 左栏：装修组件库 ---------- */
.pg-aside {
  width: 232px;
  flex: none;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  &__hd {
    flex: none;
    height: 44px;
    line-height: 44px;
    padding: 0 15px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #ebeef5;
  }
  &__scroll {
    flex: 1;
    overflow-y: auto;
    padding: 12px 15px 6px;
  }
  &__tip {
    flex: none;
    padding: 8px 12px;
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
    background: #fafafa;
    border-top: 1px solid #f0f0f0;
  }
}

/* 分组 */
.pg-group {
  &__tips {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 12px;
    font-size: 13px;
    color: #303133;
    cursor: pointer;
    user-select: none;
    i {
      font-size: 12px;
      color: #909399;
    }
  }
  &__grid {
    display: flex;
    flex-wrap: wrap;
    padding-bottom: 6px;
  }
}

/* 组件格子：与装修器左栏一致的观感 */
.pg-comp {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 58px;
  margin: 0 8px 10px 0;
  padding: 0 2px;
  font-size: 12px;
  color: #666;
  text-align: center;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s;
  &:nth-child(3n) {
    margin-right: 0;
  }
  &:hover {
    box-shadow: 0 0 5px 0 rgba(24, 144, 255, 0.3);
    transform: scale(1.06);
  }
  &.on {
    background: rgba(24, 144, 255, 0.08);
    color: $primary;
  }
  &__icon {
    width: 26px;
    height: 26px;
    margin-bottom: 4px;
    display: block;
  }
  &__name {
    margin: 0;
    font-size: 12px;
    line-height: 14px;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* ---------- 中栏：手机预览 ---------- */
.pg-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 0;
  min-width: 360px;
  &__foot {
    margin-top: 12px;
    font-size: 12px;
    color: #909399;
  }
}

.phone {
  width: 320px;
  height: 640px;
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 6px solid #1f1f1f;
  box-sizing: content-box;

  &__status {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 6px 14px 0;
    font-size: 11px;
    color: #303133;
    &-icons i {
      margin-left: 4px;
      font-size: 11px;
    }
  }
  &__nav {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 14px;
    i {
      font-size: 15px;
      color: #303133;
    }
    &-title {
      flex: 1;
      text-align: center;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }
  &__search {
    margin: 0 12px 8px;
    height: 30px;
    line-height: 30px;
    padding: 0 10px;
    background: #f5f6f8;
    border-radius: 15px;
    font-size: 12px;
    color: #b2b6bd;
    i {
      margin-right: 4px;
    }
  }
  &__filter {
    display: flex;
    gap: 18px;
    padding: 0 14px 8px;
    font-size: 12px;
    color: #606266;
    .on {
      color: $primary;
      font-weight: 600;
    }
  }
  &__scroll {
    flex: 1;
    overflow-y: auto;
    padding: 0 10px 12px;
  }
  &__extras {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-top: 8px;
    span {
      padding: 2px 8px;
      font-size: 11px;
      color: $primary;
      background: rgba(24, 144, 255, 0.08);
      border-radius: 3px;
      &.is-off {
        color: #e6a23c;
        background: rgba(230, 162, 60, 0.1);
      }
    }
  }
}

.phone__goods {
  display: grid;
  gap: 8px;
  &.is-double {
    grid-template-columns: repeat(2, 1fr);
  }
  &.is-single {
    grid-template-columns: 1fr;
    .goods-card {
      display: flex;
      gap: 8px;
    }
    .goods-card__img {
      position: relative;
      width: 90px;
      height: 90px;
      padding-top: 0;
      flex: none;
    }
    .goods-card__body {
      flex: 1;
    }
  }
}

.goods-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  &__img {
    position: relative;
    width: 100%;
    padding-top: 100%;
    background: #f5f6f8;
    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    i {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 26px;
      color: #c8cdd6;
    }
    .goods-card__badge {
      top: 0;
      left: 0;
      width: 40px;
      height: 26px;
      object-fit: contain;
    }
  }
  &__body {
    padding: 6px 8px 8px;
  }
  &__name {
    font-size: 12px;
    color: #303133;
    line-height: 16px;
    height: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    &.is-multi {
      height: 32px;
      white-space: normal;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
  &__price {
    margin-top: 4px;
    .now {
      font-size: 13px;
      font-weight: 700;
      color: #ff2a2a;
      font-variant-numeric: tabular-nums;
    }
    .old {
      margin-left: 4px;
      font-size: 11px;
      color: #b2b6bd;
      text-decoration: line-through;
    }
  }
  &__sold {
    font-size: 10px;
    color: #b2b6bd;
  }
  &__btn {
    margin-top: 6px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    font-size: 11px;
    color: #fff;
    background: #ff2a2a;
    border-radius: 11px;
  }
  &.is-placeholder {
    .goods-card__btn {
      background: #ffb0b0;
    }
  }
}

/* 内容效果：样式二去卡片阴影，样式三紧凑 */
.phone__goods.style-2 .goods-card {
  box-shadow: none;
  border: 1px solid #f0f2f5;
}
.phone__goods.style-3 {
  gap: 6px;
  .goods-card__body {
    padding: 4px 6px 6px;
  }
  .goods-card__btn {
    height: 20px;
    line-height: 20px;
  }
}

/* ---------- 右栏：功能设置 ---------- */
.pg-config {
  width: 420px;
  flex: none;
  padding: 16px 18px 24px;
  overflow-y: auto;
  background: #fff;
  border-radius: 6px;
  &__title {
    margin: 0 0 14px;
    padding-left: 8px;
    font-size: 15px;
    font-weight: 600;
    line-height: 1.2;
    color: #303133;
    border-left: 3px solid $primary;
  }
  .el-select {
    width: 100%;
  }
  ::v-deep .el-form-item {
    margin-bottom: 16px;
  }
  ::v-deep .el-form-item__label {
    color: #606266;
  }
}

/* 选中装修组件的参考条 */
.pg-refbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.5;
  color: #b8860b;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 4px;
  &__icon {
    width: 20px;
    height: 20px;
    flex: none;
  }
  &__text {
    flex: 1;
    b {
      color: #c47f00;
    }
  }
  &__close {
    flex: none;
    cursor: pointer;
    color: #c0a16b;
    &:hover {
      color: #b8860b;
    }
  }
}

/* 功能设置区块：卡片式分节 */
.pg-sec {
  padding: 14px 14px 2px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-radius: 6px;

  & + .pg-sec {
    margin-top: 12px;
  }

  &__title {
    margin: 0 0 14px;
    padding-left: 8px;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.2;
    color: #303133;
    border-left: 3px solid $primary;
  }
}

/* 权限单选：多选项自动换行，两列对齐 */
.pg-perm {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;

  ::v-deep .el-radio {
    width: 50%;
    margin-right: 0;
    margin-left: 0;
  }
}

.tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
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
/* ---------- 页面预览弹窗 ---------- */
.pv__tip {
  display: flex;
  align-items: flex-start;
  padding: 9px 12px;
  margin-bottom: 14px;
  font-size: 13px;
  line-height: 20px;
  color: #606266;
  background: var(--prev-color-primary-light-9, #ecf5ff);
  border-radius: 4px;

  i {
    margin-top: 3px;
    margin-right: 6px;
    font-size: 15px;
    color: var(--prev-color-primary, #409eff);
  }

  b {
    color: #303133;
  }

  &.is-warn {
    background: #fdf6ec;

    i {
      color: #e6a23c;
    }
  }
}

.pv__body {
  display: flex;
  gap: 18px;
}

.pv__phone {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.pv__phone-nav {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.pv__phone-dot {
  width: 8px;
  height: 8px;
  margin-right: 8px;
  background: #c0c4cc;
  border-radius: 50%;
}

.pv__phone-title {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pv__phone-refresh {
  color: var(--prev-color-primary, #409eff);
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.pv__frame {
  display: block;
  width: 100%;
  height: 560px;
  background: #fff;
  border: 0;
}

.pv__frame-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 560px;
  font-size: 13px;
  color: #909399;
}

.pv__side {
  flex: none;
  width: 246px;
  text-align: center;
}

.pv__qr {
  display: inline-block;
  min-width: 184px;
  min-height: 184px;
  padding: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;

  img {
    display: block;
    margin: 0 auto;
  }
}

.pv__qr-tip {
  margin: 8px 0 16px;
  font-size: 12px;
  color: #909399;
}

.pv__field {
  text-align: left;

  label {
    display: block;
    margin-bottom: 6px;
    font-size: 12px;
    color: #606266;
  }
}

.pv__field-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #c0c4cc;
}

.pv__url {
  padding: 8px 10px;
  margin: 12px 0;
  font-size: 12px;
  line-height: 18px;
  color: #606266;
  text-align: left;
  word-break: break-all;
  background: #f5f7fa;
  border-radius: 4px;
}

.pv__ops {
  display: flex;
  justify-content: center;
}
</style>
