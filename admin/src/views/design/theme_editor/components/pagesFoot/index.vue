<template>
  <!-- 底部固定 -->
  <div
    class="page-footer"
    :class="{ 'page-footer--hl': activeStyle == 1 }"
    :style="{ background: bgColor, paddingTop: topConfig + 'px', paddingBottom: bottomConfig + 'px' }"
    v-if="navConfig == 0"
  >
    <div
      class="foot-item"
      :class="[{ on: navStyleConfig == 1 }, { 'is-active': index == isSpecial }]"
      v-for="(item, index) in menuList"
      :key="index"
    >
      <span class="foot-hl" v-if="activeStyle == 1 && index == isSpecial" :style="{ background: hlColor }"></span>
      <div v-if="navStyleConfig != 1">
        <img :src="item.imgList ? item.imgList[0] : noPic" alt="" v-if="index == isSpecial" />
        <img :src="item.imgList ? item.imgList[1] : noPic" alt="" v-else />
      </div>
      <div v-if="navStyleConfig != 2">
        <p v-if="index == isSpecial" :style="{ color: activeStyle == 1 ? '#fff' : toneConfig ? activeTxtColor : colorStyle.theme }">
          {{ item.name || '自定义' }}
        </p>
        <p v-else :style="{ color: toneConfig ? txtColor : '#1A1A1A' }">{{ item.name || '自定义' }}</p>
      </div>
    </div>
  </div>
  <!-- 底部悬浮（胶囊 / 毛玻璃） -->
  <div
    class="page-footer page-footer2"
    :class="{
      'page-footer--h': navStyleConfig == 3,
      'page-footer--glass': glassConfig == 1,
      'page-footer--hl': activeStyle == 1,
    }"
    v-else
    :style="{
      background: bgColor2,
      paddingTop: topConfig + 'px',
      paddingBottom: bottomConfig + 'px',
      marginLeft: prConfig + 'px',
      marginRight: prConfig + 'px',
      borderRadius: bgRadius,
    }"
  >
    <div class="list">
      <div
        class="foot-item"
        :class="[{ on: navStyleConfig == 1 }, { 'is-active': index == isSpecial }]"
        v-for="(item, index) in menuList"
        :key="index"
      >
        <span class="foot-hl" v-if="activeStyle == 1 && index == isSpecial" :style="{ background: hlColor }"></span>
        <div v-if="navStyleConfig != 1">
          <img :src="item.imgList ? item.imgList[0] : noPic" alt="" v-if="index == isSpecial" />
          <img :src="item.imgList ? item.imgList[1] : noPic" alt="" v-else />
        </div>
        <div v-if="navStyleConfig != 2">
          <p v-if="index == isSpecial" :style="{ color: activeStyle == 1 ? '#fff' : toneConfig ? activeTxtColor : colorStyle.theme }">
            {{ item.name || '自定义' }}
          </p>
          <p v-else :style="{ color: toneConfig ? txtColor : '#1A1A1A' }">{{ item.name || '自定义' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import theme from '@/views/design/theme_editor/mixins/theme';
export default {
  name: 'index.vue',
  props: {
    configObj: {
      type: Object,
      default: function () {
        return {};
      },
    },
    configNme: {
      type: String,
      default: '',
    },
  },
  mixins: [theme],
  data() {
    return {
      txtColor: '',
      activeTxtColor: '',
      bgColor: '',
      bgColor2: '',
      menuList: [],
      isSpecial: 2,
      toneConfig: 0,
      topConfig: 0,
      bottomConfig: 0,
      navStyleConfig: 0,
      navConfig: 0,
      prConfig: 0,
      mTop: 0,
      bgRadius: 0,
      activeStyle: 0,
      glassConfig: 0,
      noPic: require('../../assets/images/noPictrue.png'),
    };
  },
  computed: {
    ...mapState('mobildConfig', ['pageFooter']),
    hlColor() {
      return this.toneConfig ? this.activeTxtColor : this.colorStyle.theme;
    },
  },
  watch: {
    pageFooter: {
      handler(nVal, oVal) {
        this.setConfig(nVal);
      },
      deep: true,
    },
  },
  mounted() {
    let data = this.$store.state.mobildConfig.pageFooter;
    this.setConfig(data);
  },
  methods: {
    setConfig(data) {
      this.txtColor = data.txtColor.color[0].item;
      this.activeTxtColor = data.activeTxtColor.color[0].item;
      this.bgColor = data.bgColor.color[0].item;
      this.bgColor2 = data.bgColor2.color[0].item;
      this.navStyleConfig = data.navStyleConfig.tabVal;
      this.toneConfig = data.toneConfig.tabVal;
      this.navConfig = data.navConfig.tabVal;
      this.topConfig = data.topConfig.val;
      this.bottomConfig = data.bottomConfig.val;
      this.prConfig = data.prConfig.val;
      this.mTop = data.mbConfig.val;
      this.activeStyle = data.activeStyleConfig ? data.activeStyleConfig.tabVal : 0;
      this.glassConfig = data.glassConfig ? data.glassConfig.tabVal : 0;
      let fillet = data.fillet.type;
      let filletVal = data.fillet.val;
      let valList = data.fillet.valList;
      this.bgRadius = fillet
        ? valList[0].val + 'px ' + valList[1].val + 'px ' + valList[3].val + 'px ' + valList[2].val + 'px'
        : filletVal + 'px';
      this.$store.commit('mobildConfig/footType', this.navConfig);
      this.$store.commit('mobildConfig/footBottom', this.mTop);
      this.menuList = [];
      this.$set(this, 'menuList', data.menuList.length ? data.menuList : 5);
      if (data.status.title == '是否显示') {
        this.isSpecial = 2;
      } else {
        this.isSpecial = 0;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.page-footer2 {
  backdrop-filter: blur(10px);
  .list {
    display: contents;
  }
}
.page-footer {
  display: flex;
  background: #fff;
  .foot-item {
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 50px;
    &.on {
      p {
        font-size: 16px;
      }
    }
    img {
      position: relative;
      z-index: 1;
      width: 24px;
      height: 24px;
    }
    p {
      position: relative;
      z-index: 1;
      font-size: 12px;
      color: #282828;
      margin-top: 1px;
      &.on {
        color: #00a4f8;
      }
    }
  }

  /* 悬浮 + 毛玻璃 */
  &.page-footer--glass {
    backdrop-filter: blur(18px) saturate(160%);
    -webkit-backdrop-filter: blur(18px) saturate(160%);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), inset 0 0 0 0.5px rgba(255, 255, 255, 0.6);
  }

  /* 选中胶囊高亮 */
  .foot-hl {
    position: absolute;
    top: 50%;
    left: 50%;
    z-index: 0;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    transform: translate(-50%, -50%);
  }

  &.page-footer--hl .foot-item.is-active img {
    filter: brightness(0) invert(1);
  }

  /* 纯文字模式：文字胶囊 */
  &.page-footer2 .foot-hl {
    width: auto;
    min-width: 52px;
    height: 34px;
    padding: 0 10px;
    border-radius: 17px;
    box-sizing: border-box;
  }

  /* 图文横排（支付宝同款） */
  &.page-footer--h .foot-item {
    flex-direction: row;
    img {
      width: 20px;
      height: 20px;
      margin-right: 5px;
    }
    p {
      margin-top: 0;
      font-size: 12px;
    }
  }

  &.page-footer--h .foot-hl {
    width: calc(100% - 6px);
    height: 40px;
    border-radius: 20px;
  }
}
</style>
