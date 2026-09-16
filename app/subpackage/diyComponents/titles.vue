<template>
  <!-- 标题 -->
  <common-wrapper :config="configData" v-show="!isSortType">
    <view :style="[titleWrapStyle]">
      <view
        @click="goLink"
        class="title acea-row row-middle row-between"
        :style="[titleLocation]"
      >
        <view :style="[titleStyle]">{{ dataConfig.titleConfig.value }}</view>
        <view
          class="more"
          v-if="!dataConfig.buttonConfig.tabVal"
          :style="[moreStyle]"
        >
          {{ dataConfig.titleConfigRight.value }}
          <text class="iconfont icon-ic_rightarrow"></text>
        </view>
      </view>
    </view>
  </common-wrapper>
</template>

<script>
import commonWrapper from "./commonWrapper.vue";
export default {
  components: { commonWrapper },
  name: "titles",
  props: {
    dataConfig: {
      type: Object,
      default: () => {},
    },
    isSortType: {
      type: [String, Number],
      default: 0,
    },
  },
  computed: {
    configData() {
      return {
        ...this.dataConfig,
        paddingConfig: this.dataConfig.paddingConfig || {
          isAll: false,
          valList: [
            {
              val: this.dataConfig.topConfig
                ? this.dataConfig.topConfig.val
                : 0,
            },
            {
              val: this.dataConfig.prConfig ? this.dataConfig.prConfig.val : 0,
            },
            {
              val: this.dataConfig.bottomConfig
                ? this.dataConfig.bottomConfig.val
                : 0,
            },
            {
              val: this.dataConfig.prConfig ? this.dataConfig.prConfig.val : 0,
            },
          ],
        },
        marginConfig: this.dataConfig.marginConfig || {
          isAll: false,
          valList: [
            {
              val: this.dataConfig.mbConfig ? this.dataConfig.mbConfig.val : 0,
            },
            {
              val: 0,
            },
            {
              val: 0,
            },
            {
              val: 0,
            },
          ],
        },
      };
    },
    titleWrapStyle() {
      const fillet = this.dataConfig.fillet || {};
      let borderRadius = `${(fillet.val || 0) * 2}rpx`;
      if (fillet.type && Array.isArray(fillet.valList)) {
        borderRadius = `${(fillet.valList[0] ? fillet.valList[0].val : 0) * 2}rpx ${
          (fillet.valList[1] ? fillet.valList[1].val : 0) * 2
        }rpx ${(fillet.valList[3] ? fillet.valList[3].val : 0) * 2}rpx ${
          (fillet.valList[2] ? fillet.valList[2].val : 0) * 2
        }rpx`;
      }
      // 兜底：moduleColor 缺失或结构不完整时使用主题默认色，避免整页渲染崩溃
      let moduleColor = this.dataConfig.moduleColor;
      if (!moduleColor || !moduleColor.color || !moduleColor.color.length) {
        moduleColor = { color: [{ item: "#FFFFFF" }, { item: "#FFFFFF" }] };
      }
      const c0 = moduleColor.color[0] ? moduleColor.color[0].item : "#FFFFFF";
      const c1 = moduleColor.color[1]
        ? moduleColor.color[1].item
        : c0 || "#FFFFFF";
      return {
        "border-radius": borderRadius,
        background: `linear-gradient(90deg, ${c0} 0%, ${c1} 100%)`,
      };
    },
    titleStyle() {
      const fontSize = this.dataConfig.fontSize || {};
      let style = {
        "font-size": `${(fontSize.val || 16) * 2}rpx`,
        color: this._firstColor(this.dataConfig.themeColor, "#333333"),
      };
      const textStyle = this.dataConfig.textStyle || {};
      switch (textStyle.tabVal) {
        case 1:
          style["font-style"] = "italic";
          break;
        case 2:
          style["font-weight"] = "bold";
          break;
      }
      return style;
    },
    titleLocation() {
      if (this.dataConfig.buttonConfig && this.dataConfig.buttonConfig.tabVal) {
        let style = {};
        const textPosition = this.dataConfig.textPosition || {};
        switch (textPosition.tabVal) {
          case 1:
            style["justify-content"] = "center";
            break;
          case 2:
            style["justify-content"] = "flex-end";
            break;
        }
        return style;
      }
    },
    moreStyle() {
      const buttonText = this.dataConfig.buttonText || {};
      return {
        "font-size": `${(buttonText.val || 12) * 2}rpx`,
        color: this._firstColor(this.dataConfig.buttonColor, "#999999"),
      };
    },
  },
  methods: {
    // 安全取色：兼容 字段缺失 / color 为空 / item 缺失 三种情况
    _firstColor(obj, fallback) {
      if (obj && Array.isArray(obj.color) && obj.color.length && obj.color[0]) {
        return obj.color[0].item || fallback;
      }
      if (obj && obj.default && obj.default.length && obj.default[0]) {
        return obj.default[0].item || fallback;
      }
      return fallback;
    },
    goLink() {
      this.$util.JumpPath(this.dataConfig.linkConfig.value);
    },
  },
};
</script>

<style lang="scss">
.title {
  justify-content: space-between;
  padding: 26rpx 24rpx;
  border-radius: 16rpx 16rpx 0rpx 0rpx;
  font-weight: 500;
  font-size: 32rpx;
  line-height: 44rpx;
  color: #333333;

  .more {
    font-weight: 400;
    font-size: 24rpx;
    line-height: 34rpx;
    color: #999999;
  }

  .iconfont {
    font-size: 24rpx;
  }
}
</style>
