<template>
  <view :style="[bottomBgColor]">
    <view :style="[boxStyle]">
      <slot></slot>
    </view>
  </view>
</template>

<script>
export default {
  name: "commonWrapper",
  props: {
    config: {
      type: Object,
      default: () => ({}),
    },
    // 沉浸式顶部延展(px)：背景上移 T 顶到屏幕最上方，内容位置不变。
    // 仅会员中心页通过 provide/inject 注入，其他页面默认 0 无任何影响
    extendTop: {
      type: Number,
      default: 0,
    },
  },
  computed: {
    boxStyle() {
      const config = this.config || {};
      const marginConfig = config.marginConfig || {
        val: 0,
        valList: [
          {
            val: 0,
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
      };
      const paddingConfig = config.paddingConfig || {
        val: 0,
        valList: [
          {
            val: 0,
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
      };
      const componentBgConfig = config.componentBgConfig;
      const borderConfig = config.borderConfig;
      const shadowConfig = config.shadowConfig;
      const fillet = config.fillet;
      const zIndexConfig = config.zIndexConfig;

      let style = {
        overflow: "hidden",
      };

      // Margin
      if (!marginConfig.isAll) {
        style["margin"] = `${marginConfig.val * 2}rpx`;
      } else {
        style["margin-top"] = `${marginConfig.valList[0].val * 2}rpx`;
        style["margin-bottom"] = `${marginConfig.valList[2].val * 2}rpx`;
        style["margin-left"] = `${marginConfig.valList[3].val * 2}rpx`;
        style["margin-right"] = `${marginConfig.valList[1].val * 2}rpx`;
      }

      // Padding
      if (!paddingConfig.isAll) {
        style["padding"] = `${paddingConfig.val * 2}rpx`;
      } else {
        style["padding-top"] = `${paddingConfig.valList[0].val * 2}rpx`;
        style["padding-bottom"] = `${paddingConfig.valList[2].val * 2}rpx`;
        style["padding-left"] = `${paddingConfig.valList[3].val * 2}rpx`;
        style["padding-right"] = `${paddingConfig.valList[1].val * 2}rpx`;
      }

      // Background
      if (componentBgConfig) {
        if (componentBgConfig.tabVal === 0) {
          // Color
          const colorConfig = componentBgConfig.colorConfig;
          const colorDirection = componentBgConfig.colorDirection;

          if (colorConfig && colorConfig.color) {
            const colors = colorConfig.color.map((c) => c.item);
            if (colors.length > 1) {
              let deg = "90deg";
              if (colorDirection) {
                switch (colorDirection.tabVal) {
                  case 0:
                    deg = "90deg";
                    break;
                  case 1:
                    deg = "180deg";
                    break;
                  case 2:
                    deg = "135deg";
                    break;
                  case 3:
                    deg = "200deg";
                    break;
                }
              }
              style.background = `linear-gradient(${deg}, ${colors[0]} 0%, ${colors[1]} 100%)`;
            } else {
              style.background = colors[0];
            }
          }
        } else if (componentBgConfig.tabVal === 1) {
          // Image
          const imageConfig =
            componentBgConfig.imageConfig || componentBgConfig.imgConfig;
          if (imageConfig && imageConfig.url) {
            style["background-image"] = `url(${imageConfig.url})`;
            style["background-repeat"] = "no-repeat";
            style["background-size"] = "cover";
            style["background-position"] = "center";
          }
        }
      }
      // Border
      if (borderConfig && borderConfig.tabVal) {
        const color = borderConfig.colorConfig
          ? borderConfig.colorConfig.color[0].item
          : "#000";
        const width = borderConfig.widthConfig
          ? borderConfig.widthConfig.val
          : 1;
        // Style: solid, dashed, dotted
        let borderStyle = "solid";
        if (borderConfig.styleConfig) {
          const styleVal = borderConfig.styleConfig.tabVal;
          if (styleVal === 1) borderStyle = "dashed";
          if (styleVal === 2) borderStyle = "dotted";
        }
        style["border"] = `${width * 2}rpx ${borderStyle} ${color}`;
      }

      // Shadow
      if (shadowConfig && shadowConfig.tabVal) {
        const x = shadowConfig.xConfig ? shadowConfig.xConfig.val : 0;
        const y = shadowConfig.yConfig ? shadowConfig.yConfig.val : 0;
        const blur = shadowConfig.blurConfig ? shadowConfig.blurConfig.val : 0;
        const spread = shadowConfig.spreadConfig
          ? shadowConfig.spreadConfig.val
          : 0;
        const color = shadowConfig.colorConfig
          ? shadowConfig.colorConfig.color[0].item
          : "#000";
        style["box-shadow"] = `${x * 2}rpx ${y * 2}rpx ${blur * 2}rpx ${
          spread * 2
        }rpx ${color}`;
      }
      // Radius (Fillet)
      if (fillet) {
        if (fillet.type) {
          // 4 corners
          const valList = fillet.valList;
          style["border-radius"] = `${valList[0].val * 2}rpx ${
            valList[1].val * 2
          }rpx ${valList[3].val * 2}rpx ${valList[2].val * 2}rpx`;
        } else {

          style["border-radius"] = `${fillet.val * 2}rpx`;
        }
      }

      // Z-Index
      if (zIndexConfig) {
        style["z-index"] = zIndexConfig.val;
        // style["position"] = "relative";
      }

      // 沉浸式顶部延展：整个背景(渐变/纯色/图片)向上顶 T px，
      // 用 transform 移动绘制盒(不影响文档流、不触发 margin 塌陷)，
      // padding-top 补回 T 保证内容仍在原位置；背景因此从屏幕最顶端开始渲染
      if (this.extendTop > 0) {
        const T = this.extendTop;
        const baseTopRpx = paddingConfig.isAll
          ? paddingConfig.valList[0].val * 2
          : paddingConfig.val * 2;
        if (style["padding"] !== undefined) {
          const uniform = `${paddingConfig.val * 2}rpx`;
          style["padding-top"] = `calc(${baseTopRpx}rpx + ${T}px)`;
          style["padding-bottom"] = uniform;
          style["padding-left"] = uniform;
          style["padding-right"] = uniform;
          delete style["padding"];
        } else {
          style["padding-top"] = `calc(${baseTopRpx}rpx + ${T}px)`;
        }
        style["transform"] = `translateY(-${T}px)`;
      }
      return style;
    },
    bottomBgColor() {
      const config = this.config || {};
      let style = {
        overflow: "hidden",
      };
      // 延展时外层不能再裁剪内层向上伸出的背景
      if (this.extendTop > 0) {
        style.overflow = "visible";
        // 外层背景色同步向上延伸 T px（负 margin + 等量 padding，布局不变）。
        // 必须做：渐变起始色是半透明的，头部段透出的是页面底色、
        // 卡片段透出的是本层背景色，两者不一致会在交界处形成一条色差纹理
        const T = this.extendTop;
        style["margin-top"] = -T + "px";
        style["padding-top"] = T + "px";
      }
      if (config.bottomBgColor) {
        style.background = config.bottomBgColor.color
          ? config.bottomBgColor.color[0].item
          : "";
      }
      return style;
    },
  },
};
</script>

<style scoped></style>
