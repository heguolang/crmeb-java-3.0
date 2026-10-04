<!--
  首页 快捷入口 / 经营数据 · 方案 B「极光科技」（2026-10-04 视觉改造）

  改造点（功能零变更）：
  1. 快捷入口图标：单色 bgColor 方块 + iconfont → 渐变色圆角块 + 白色线性 SVG + 同色光晕
  2. 卡片标题：细色条 → 渐变短条 + 加粗字距
  3. 数字卡：28px 等宽数字（tabular-nums）+ 悬停上浮
  数据源、权限过滤（checkPermi）、跳转 path 全部保持原样。
-->
<template>
  <div class="divBox">
    <el-row :gutter="14">
      <el-col :xs="24" :sm="24" :md="24" :lg="12">
        <el-card class="box-card">
          <div class="header_title">快捷入口</div>
          <div class="nav_grid">
            <div class="nav_grid_item" v-for="(item, index) in permList" :key="index" @click="navigatorTo(item.url)">
              <div class="pic_badge" :style="{ background: item.bg, boxShadow: item.glow }">
                <!-- 图标：内联 SVG（不用 v-html，v-html 插入的节点拿不到 scoped 的 data-v 属性，样式会失效） -->
                <svg v-if="item.key === 'user'" viewBox="0 0 24 24">
                  <circle cx="12" cy="8" r="3.6" />
                  <path d="M4.5 20c0-3.6 3.4-6.4 7.5-6.4s7.5 2.8 7.5 6.4" />
                </svg>
                <svg v-else-if="item.key === 'setting'" viewBox="0 0 24 24">
                  <circle cx="12" cy="12" r="3" />
                  <path d="M12 2.6v2.2M12 19.2v2.2M21.4 12h-2.2M4.8 12H2.6M18.6 5.4 17 7M7 17l-1.6 1.6M18.6 18.6 17 17M7 7 5.4 5.4" />
                </svg>
                <svg v-else-if="item.key === 'goods'" viewBox="0 0 24 24">
                  <path d="M3 7.5 12 3l9 4.5v9L12 21l-9-4.5z" />
                  <path d="M3 7.5 12 12l9-4.5" />
                  <path d="M12 12v9" />
                </svg>
                <svg v-else-if="item.key === 'order'" viewBox="0 0 24 24">
                  <path d="M6 3h9l5 5v13H6z" />
                  <path d="M15 3v5h5" />
                  <path d="M9 13h7" />
                  <path d="M9 17h5" />
                </svg>
                <svg v-else-if="item.key === 'article'" viewBox="0 0 24 24">
                  <rect x="5" y="3" width="14" height="18" rx="2" />
                  <path d="M9 8h6" />
                  <path d="M9 12h6" />
                  <path d="M9 16h4" />
                </svg>
                <svg v-else-if="item.key === 'distribution'" viewBox="0 0 24 24">
                  <circle cx="6" cy="12" r="2.4" />
                  <circle cx="18" cy="6" r="2.4" />
                  <circle cx="18" cy="18" r="2.4" />
                  <path d="M8.2 11 15.8 7" />
                  <path d="M8.2 13 15.8 17" />
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <rect x="3.5" y="6.5" width="17" height="11" rx="2" />
                  <path d="M14.5 6.5v11" stroke-dasharray="2.5 2.5" />
                  <path d="M7.5 12h2.5" />
                </svg>
              </div>
              <p>{{ item.title }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="24" :lg="12">
        <el-card class="box-card">
          <div class="header_title">经营数据</div>
          <div class="nav_grid">
            <div
              class="nav_grid_item"
              v-for="(item, index) in businessList"
              :key="index"
              @click="navigatorTo(item.path)"
            >
              <p class="num_data">{{ item.num }}</p>
              <p class="label">{{ item.title }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import echartsNew from '@/components/echartsNew/index';
import { businessData } from '@/api/dashboard';
import { checkPermi } from '@/utils/permission'; // 权限判断函数

export default {
  components: {
    echartsNew,
  },
  data() {
    return {
      grid: {
        xl: 3,
        lg: 3,
        md: 6,
        sm: 8,
        xs: 8,
      },
      nav_list: [
        {
          key: 'user',
          bg: 'linear-gradient(135deg, #4f8cff, #1f5eff)',
          glow: '0 6px 14px rgba(31,94,255,.32)',
          title: '用户管理',
          url: '/user/index',
          perms: ['admin:user:list'],
        },
        {
          key: 'setting',
          bg: 'linear-gradient(135deg, #34d399, #12b76a)',
          glow: '0 6px 14px rgba(18,183,106,.3)',
          title: '系统设置',
          url: '/operation/setting',
          perms: ['admin:system:config:info'],
        },
        {
          key: 'goods',
          bg: 'linear-gradient(135deg, #fbbf24, #f59e0b)',
          glow: '0 6px 14px rgba(245,158,11,.3)',
          title: '商品管理',
          url: '/store/index',
          perms: ['admin:product:list'],
        },
        {
          key: 'order',
          bg: 'linear-gradient(135deg, #fb7185, #f04438)',
          glow: '0 6px 14px rgba(240,68,56,.3)',
          title: '订单管理',
          url: '/order/index',
          perms: ['admin:order:list'],
        },
        {
          key: 'article',
          bg: 'linear-gradient(135deg, #a78bfa, #7c3aed)',
          glow: '0 6px 14px rgba(124,58,237,.3)',
          title: '文章管理',
          url: '/content/articleManager',
          perms: ['admin:article:list'],
        },
        {
          key: 'distribution',
          bg: 'linear-gradient(135deg, #22d3ee, #0891b2)',
          glow: '0 6px 14px rgba(8,145,178,.3)',
          title: '分销管理',
          url: '/distribution/index',
          perms: ['admin:retail:list'],
        },
        {
          key: 'coupon',
          bg: 'linear-gradient(135deg, #f472b6, #db2777)',
          glow: '0 6px 14px rgba(219,39,119,.28)',
          title: '优惠券',
          url: '/marketing/coupon/list',
          perms: ['admin:coupon:list'],
        },
      ],
      statisticData: [
        { title: '待发货订单', num: 0, path: '/order/index', perms: ['admin:order:list'] },
        { title: '退款中订单', num: 0, path: '/order/index', perms: ['admin:order:list'] },
        { title: '待核销订单', num: 0, path: '/order/index', perms: ['admin:order:list'] },
        { title: '库存预警', num: 0, path: '/store/index', perms: ['admin:product:list'] },
        { title: '上架商品', num: 0, path: '/store/index', perms: ['admin:product:list'] },
        { title: '仓库商品', num: 0, path: '/store/index', perms: ['admin:product:list'] },
        { title: '提现待审核', num: 0, path: '/financial/commission/template', perms: ['admin:finance:apply:list'] },
        { title: '账户充值', num: 0, path: '/financial/record/charge', perms: ['admin:recharge:list'] },
      ],
      optionData: {},
      applyNum: 0,
      style: { height: '250px' },
    };
  },
  computed: {
    //鉴权处理
    permList: function () {
      let arr = [];
      this.nav_list.forEach((item) => {
        if (this.checkPermi(item.perms)) {
          arr.push(item);
        }
      });
      return arr;
    },
    businessList: function () {
      let arr = [];
      this.statisticData.forEach((item) => {
        if (this.checkPermi(item.perms)) {
          arr.push(item);
        }
      });
      return arr;
    },
  },
  mounted() {
    this.getbusinessData();
  },
  methods: {
    checkPermi,
    navigatorTo(path) {
      this.$router.push(path);
    },
    getbusinessData() {
      businessData().then((res) => {
        this.statisticData[0].num = res.notShippingOrderNum; //待发货订单
        this.statisticData[1].num = res.refundingOrderNum; //退款中订单
        this.statisticData[2].num = res.notWriteOffOrderNum; //待核销订单
        this.statisticData[3].num = res.vigilanceInventoryNum; //库存预警
        this.statisticData[4].num = res.onSaleProductNum; //上架商品
        this.statisticData[5].num = res.notSaleProductNum; //仓库商品
        this.statisticData[6].num = res.notAuditNum; //提现待审核
        this.statisticData[7].num = res.totalRechargeAmount; //账户充值
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.ivu-mb {
  margin-bottom: 14px;
}

.dashboard-console-grid {
  text-align: center;
  .ivu-card-body {
    padding: 0;
  }
  i {
    font-size: 32px;
  }
  a {
    display: block;
    color: inherit;
  }
  p {
    margin-top: 8px;
  }
}
.mb20 {
  margin-bottom: 20px;
}
/* 与 KPI 卡同一语言的标题：渐变短条 + 加粗 */
.header_title {
  font-size: 15px;
  font-weight: 600;
  color: var(--prev-color-text-title, #0f172a);
  letter-spacing: 0.2px;
  padding-left: 11px;
  position: relative;
  &::before {
    position: absolute;
    content: '';
    width: 3px;
    height: 15px;
    border-radius: 2px;
    background: var(--prev-color-primary-gradient-strong, linear-gradient(135deg, #1f5eff, #22d3ee));
    top: 2px;
    left: 0;
  }
}
.nav_grid {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 10px;
}
.nav_grid_item {
  width: 20%;
  height: 90px;
  margin-top: 26px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  img {
    width: 58px;
    height: 58px;
  }
  .pic_badge {
    width: 54px;
    height: 54px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    border-radius: 14px;
    background: var(--prev-color-primary-gradient, linear-gradient(135deg, #1f5eff, #4f8cff));
    box-shadow: 0 6px 14px rgba(31, 94, 255, 0.3);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    svg {
      width: 26px;
      height: 26px;
      display: block;
      stroke: #fff;
      fill: none;
      stroke-width: 1.7;
      stroke-linecap: round;
      stroke-linejoin: round;
    }
  }
  &:hover .pic_badge {
    /* 各入口的投影是行内 style，优先级高于样式表，这里只做上浮 + 微提亮 */
    transform: translateY(-2px);
    filter: brightness(1.07);
  }
  p {
    height: 17px;
    font-size: 14px;
    font-weight: 400;
    color: var(--prev-color-text-regular, #1f2937);
    line-height: 17px;
    margin-top: 12px;
  }
  .num_data {
    font-size: 28px;
    font-weight: 700;
    color: var(--prev-color-text-title, #0f172a);
    text-align: center;
    margin-bottom: 18px;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.5px;
    transition: color 0.2s ease;
  }
  .label {
    font-size: 13px;
    color: var(--prev-color-text-secondary, #94a3b8);
    text-align: center;
  }
  &:hover .num_data {
    color: var(--prev-color-primary, #1f5eff);
  }
}
</style>
