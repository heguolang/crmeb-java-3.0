<template>
  <div
    class="layout-logo"
    v-if="$store.state.themeConfig.themeConfig.layout !== 'columns' && !$store.state.themeConfig.themeConfig.isCollapse"
    @click="onThemeConfigChange"
  >
    <img v-if="maxLogo" class="layout-logo-medium-img" :src="maxLogo" />
  </div>
  <div class="layout-logo-size" v-else @click="onThemeConfigChange">
    <img v-if="minLogo" class="layout-logo-size-img" :src="minLogo" />
  </div>
</template>

<script>
import Cookies from 'js-cookie';
import { getSiteLogoApi } from '@/api/systemConfig';
export default {
  name: 'layoutLogo',
  data() {
    return {
      platMerLoginInfo: {},
    };
  },
  computed: {
    maxLogo() {
      return (
        this.platMerLoginInfo.siteLogoSquare ||
        this.platMerLoginInfo.siteLogoLeftTop ||
        this.platMerLoginInfo.logoSquare ||
        this.platMerLoginInfo.logo_square ||
        this.platMerLoginInfo.logo ||
        this.platMerLoginInfo.loginLogo ||
        ''
      );
    },
    minLogo() {
      return (
        this.platMerLoginInfo.siteLogoLeftTop ||
        this.platMerLoginInfo.siteLogoSquare ||
        this.platMerLoginInfo.logoLeftTop ||
        this.platMerLoginInfo.logo_square ||
        this.platMerLoginInfo.logo ||
        this.platMerLoginInfo.loginLogo ||
        ''
      );
    },
    // 获取布局配置信息
    getThemeConfig() {
      return this.$store.state.themeConfig.themeConfig;
    },
    // 设置 logo 是否显示
    setShowLogo() {
      let { isCollapse, layout } = this.$store.state.themeConfig.themeConfig;
      return !isCollapse || layout === 'classic' || document.body.clientWidth < 1000;
    },
  },
  mounted() {
    this.getLogoInfo();
  },
  methods: {
    getLogoInfo() {
      getSiteLogoApi()
        .then((res) => {
          this.platMerLoginInfo = res || {};
        })
        .catch(() => {
          const logoInfo = Cookies.get('logoInfo');
          this.platMerLoginInfo = this.parseLogoInfo(logoInfo);
        });
    },
    parseLogoInfo(logoInfo) {
      if (!logoInfo) return {};
      try {
        return JSON.parse(logoInfo);
      } catch (e) {
        return {};
      }
    },
    // logo 点击实现菜单展开/收起
    onThemeConfigChange() {
      this.$router.push(`/dashboard`);
    },
  },
};
</script>

<style scoped lang="scss">
.layout-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--prev-color-primary);
  font-size: 16px;
  cursor: pointer;
  animation: logoAnimation 0.3s ease-in-out;
  /* 2026-10-04：顶栏导航层为 56px，原 64px 会溢出 8px（横向布局下尤其明显）。
     width 保持 206px 不动 —— classic 侧栏布局依赖这个值。 */
  height: 56px;
  line-height: 56px;
  width: 206px;
  flex: 0 0 auto;
  &:hover {
    span {
      opacity: 0.9;
    }
  }
  &-medium-img {
    height: 30px;
    border-radius: 8px;
  }
}
/* 方案 B：logo 在深空渐变底上加一点柔和光晕，增强品牌块的存在感 */
.nav-row .layout-logo-medium-img {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
}
.layout-logo-size {
  display: flex;
  cursor: pointer;
  margin: auto;
  height: 50px;
  &-img {
    width: 50px;
    height: auto;
    margin: auto;
    animation: logoAnimation 0.3s ease-in-out;
  }
}
</style>
