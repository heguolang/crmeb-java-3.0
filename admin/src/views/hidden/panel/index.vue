<template>
  <div class="divBg addContent-wrapper" v-if="loaded">
    <el-card shadow="never" class="mt16">
      <el-alert
        title="系统运维面板 · 危险操作区"
        type="warning"
        :closable="false"
        description="清除操作立即生效且不做备份，请谨慎执行。所有操作仅在本机审计文件留痕，不写入系统日志。"
        style="margin-bottom: 20px"
      />

      <!-- 功能开关：模式推广 -->
      <div class="panel-section">
        <div class="panel-section-title">功能开关 · 模式推广</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in modeSwitchItems" :key="item.key">
            <div class="switch-cell">
              <span>{{ item.name }}</span>
              <el-switch
                v-model="switches[item.key]"
                :active-text="switches[item.key] ? '已开启' : '已关闭'"
                @change="onSwitchChange(item, switches)"
              />
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 功能开关：营销 -->
      <div class="panel-section">
        <div class="panel-section-title">功能开关 · 营销</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in marketingSwitchItems" :key="item.key">
            <div class="switch-cell">
              <span>{{ item.name }}</span>
              <el-switch
                v-model="marketingSwitches[item.key]"
                :active-text="marketingSwitches[item.key] ? '已开启' : '已关闭'"
                @change="onSwitchChange(item, marketingSwitches)"
              />
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 数据清除 -->
      <div class="panel-section">
        <div class="panel-section-title">数据清除（每次操作需输入确认串 DELETE）</div>

        <!-- 商品 -->
        <div class="clear-group-name">商品</div>
        <div class="product-clear-cell">
          <span class="product-clear-label">清空商品</span>
          <el-select v-model="productClearType" size="small" style="width: 220px; margin: 0 12px">
            <el-option
              v-for="opt in productTypeOptions"
              :key="opt.type"
              :label="`${opt.name}（${counts[opt.countKey] !== undefined ? counts[opt.countKey] : '-'} 条）`"
              :value="opt.type"
            />
          </el-select>
          <el-button size="small" type="danger" plain @click="onClear({ type: productClearType, name: '清空商品-' + currentProductTypeName })">清除</el-button>
          <span class="clear-cell-desc" style="margin: 0 0 0 12px">按所选页签范围删除商品及其规格/详情/分类关联</span>
        </div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in productClearItems" :key="item.type">
            <div class="clear-cell">
              <div class="clear-cell-name">
                {{ item.name }}
                <span class="clear-cell-count">{{ counts[item.countKey] !== undefined ? counts[item.countKey] : '-' }} 条</span>
              </div>
              <div class="clear-cell-desc">{{ item.desc }}</div>
              <el-button size="small" type="danger" plain @click="onClear(item)">清除</el-button>
            </div>
          </el-col>
        </el-row>

        <!-- 文章 -->
        <div class="clear-group-name">文章</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in articleClearItems" :key="item.type">
            <div class="clear-cell">
              <div class="clear-cell-name">
                {{ item.name }}
                <span class="clear-cell-count">{{ counts[item.countKey] !== undefined ? counts[item.countKey] : '-' }} 条</span>
              </div>
              <div class="clear-cell-desc">{{ item.desc }}</div>
              <el-button size="small" type="danger" plain @click="onClear(item)">清除</el-button>
            </div>
          </el-col>
        </el-row>

        <!-- 会员 -->
        <div class="clear-group-name">会员</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in memberClearItems" :key="item.type">
            <div class="clear-cell">
              <div class="clear-cell-name">
                {{ item.name }}
                <span class="clear-cell-count">{{ counts[item.countKey] !== undefined ? counts[item.countKey] : '-' }} 条</span>
              </div>
              <div class="clear-cell-desc">{{ item.desc }}</div>
              <el-button size="small" type="danger" plain @click="onClear(item)">清除</el-button>
            </div>
          </el-col>
        </el-row>

        <!-- 日志与资金 -->
        <div class="clear-group-name">日志与资金</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in systemClearItems" :key="item.type">
            <div class="clear-cell">
              <div class="clear-cell-name">
                {{ item.name }}
                <span class="clear-cell-count" v-if="item.countKey">{{ counts[item.countKey] !== undefined ? counts[item.countKey] : '-' }} 条</span>
              </div>
              <div class="clear-cell-desc">{{ item.desc }}</div>
              <el-button size="small" type="danger" plain @click="onClear(item)">清除</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
import { hiddenInfo, hiddenClear, hiddenSetSwitch } from '@/api/hidden';

export default {
  name: 'HiddenPanel',
  data() {
    return {
      loaded: false,
      switches: {
        sys_switch_team_reward: false,
        sys_switch_stock: false,
        sys_switch_store: false,
        sys_switch_daili: false,
        sys_switch_spread: false,
      },
      modeSwitchItems: [
        { key: 'sys_switch_team_reward', name: '团队奖' },
        { key: 'sys_switch_stock', name: '订货商' },
        { key: 'sys_switch_store', name: '门店' },
        { key: 'sys_switch_daili', name: '区域代理' },
        { key: 'sys_switch_spread', name: '分销' },
      ],
      marketingSwitches: {
        sys_switch_integral: false,
        sys_switch_seckill: false,
        sys_switch_bargain: false,
        sys_switch_combination: false,
        sys_switch_coupon: false,
      },
      marketingSwitchItems: [
        { key: 'sys_switch_integral', name: '积分' },
        { key: 'sys_switch_seckill', name: '秒杀管理' },
        { key: 'sys_switch_bargain', name: '砍价管理' },
        { key: 'sys_switch_combination', name: '拼团管理' },
        { key: 'sys_switch_coupon', name: '优惠券' },
      ],
      counts: {},
      // 清空商品：页签范围
      productClearType: 'productSelling',
      productTypeOptions: [
        { type: 'productSelling', name: '出售中商品', countKey: 'productSelling' },
        { type: 'productWarehouse', name: '仓库中商品', countKey: 'productWarehouse' },
        { type: 'productSoldout', name: '已售罄商品', countKey: 'productSoldout' },
        { type: 'productAlert', name: '警戒库存商品', countKey: 'productAlert' },
        { type: 'productRecycle', name: '商品回收站', countKey: 'productRecycle' },
      ],
      productClearItems: [
        { type: 'productCate', name: '商品分类', countKey: 'productCate', desc: '清空商品分类及商品分类关联（eb_category type=1）' },
        { type: 'productAttr', name: '商品规格', countKey: 'productAttr', desc: '清空全部商品规格、SKU 及规格模板' },
        { type: 'productReply', name: '商品评论', countKey: 'productReply', desc: '清空全部商品评论（eb_store_product_reply）' },
      ],
      articleClearItems: [
        { type: 'articleCate', name: '文章分类', countKey: 'articleCate', desc: '清空文章分类（eb_category type=3）' },
        { type: 'article', name: '文章管理', countKey: 'article', desc: '清空全部图文文章（eb_article）' },
      ],
      memberClearItems: [
        { type: 'spreadRelation', name: '会员推荐关系', countKey: 'spreadRelation', desc: '全部会员的推广人/推广时间/推广人数清零' },
        { type: 'agentUser', name: '代理商会员', countKey: 'agent', desc: '清空代理管理（区域代理）会员及奖励/变更记录' },
        { type: 'stockAgentUser', name: '订货商会员', countKey: 'stockAgent', desc: '清空订货商管理会员（eb_stock_agent）' },
        { type: 'user', name: '会员及关联数据', countKey: 'user', desc: '清空全部会员及订单/资金/代理/订货/门店店员等关联数据' },
      ],
      systemClearItems: [
        { type: 'loginLog', name: '登录日志', countKey: 'adminLoginLog', desc: '清空后台登录日志（eb_admin_login_log）' },
        { type: 'operateLog', name: '操作日志', countKey: 'operateLog', desc: '清空后台敏感操作日志（eb_sensitive_method_log）' },
        { type: 'money', name: '资金记录', countKey: 'userBill', desc: '清空账单/佣金记录/充值/提现记录，佣金归零' },
        { type: 'balance', name: '账户余额', countKey: '', desc: '全部会员余额清零（now_money = 0）' },
        { type: 'integral', name: '积分', countKey: 'integralRecord', desc: '全部会员积分清零并清空积分流水' },
        { type: 'all', name: '一键全清', countKey: '', desc: '以上全部：日志/资金/余额/积分/会员及关联数据' },
      ],
    };
  },
  computed: {
    currentProductTypeName() {
      const opt = this.productTypeOptions.find((o) => o.type === this.productClearType);
      return opt ? opt.name : '';
    },
  },
  mounted() {
    this.fetchInfo();
  },
  methods: {
    fetchInfo() {
      hiddenInfo()
        .then((res) => {
          this.counts = (res && res.counts) || {};
          this.switches = (res && res.switches) || this.switches;
          this.marketingSwitches = (res && res.marketingSwitches) || this.marketingSwitches;
          this.loaded = true;
        })
        .catch(() => {
          this.loaded = false;
        });
    },
    onSwitchChange(item, groupObj) {
      hiddenSetSwitch(item.key, groupObj[item.key])
        .then(() => {
          const target = groupObj[item.key] ? '开启' : '关闭';
          this.$message.success(`「${item.name}」已${target}，菜单已同步更新`);
          // 清掉菜单缓存，原地重新拉取最新（已按开关过滤）的菜单并重建侧边栏。
          // 不再用 location.reload()：整页刷新会重新走登录态/菜单/路由全套启动请求，
          // 弱网或接口抖动时任何一步失败就是整页白屏（2026-09-25 线上实测）。
          localStorage.removeItem('MerPlatAdmin_MenuList');
          localStorage.removeItem('MerPlatAdmin_oneLvRoutes');
          this.$store
            .dispatch('user/getMenus')
            .then(() => {
              // 触发 aside / columnsAside 重新按新菜单树渲染
              this.bus.$emit('routesListChange');
            })
            .catch(() => {
              this.$message.error('菜单刷新失败，请手动刷新页面');
            });
        })
        .catch(() => {
          groupObj[item.key] = !groupObj[item.key];
        });
    },
    onClear(item) {
      this.$prompt(`即将执行「${item.name}」，该操作不可恢复！请输入 DELETE 确认执行`, '危险操作确认', {
        confirmButtonText: '确认清除',
        cancelButtonText: '取消',
        inputPattern: /^DELETE$/,
        inputErrorMessage: '请输入大写 DELETE',
        type: 'warning',
      })
        .then(({ value }) => hiddenClear(item.type, value))
        .then((res) => {
          this.$message.success(`「${item.name}」完成，影响 ${res && res.affected !== undefined ? res.affected : '?'} 行`);
          this.fetchInfo();
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
.panel-section {
  margin-bottom: 26px;
}
.panel-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.switch-cell {
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 14px 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}
.clear-group-name {
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  margin: 6px 0 10px;
}
.product-clear-cell {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 14px 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.product-clear-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.clear-cell {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.clear-cell-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.clear-cell-count {
  font-size: 12px;
  font-weight: normal;
  color: #409eff;
  margin-left: 8px;
}
.clear-cell-desc {
  font-size: 12px;
  color: #909399;
  line-height: 20px;
  margin-bottom: 10px;
  min-height: 20px;
}
</style>
