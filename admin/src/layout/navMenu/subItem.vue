<!--
 * @Author: From-wh from-wh@hotmail.com
 * @Date: 2023-03-09 15:45:51
 * @FilePath: /admin/src/layout/navMenu/subItem.vue
 * @Description:
-->
<template>
  <div>
    <template v-for="val in chil">
      <el-submenu :index="val.path" :key="val.path" v-if="val.children && val.children.length > 0">
        <template slot="title">
          <i class="ivu-icon" :class="'el-icon-' + val.icon"></i>
          <span>{{ val.title }}</span>
        </template>
        <sub-item :chil="val.children" :depth="depth + 1" />
      </el-submenu>
      <template v-else>
        <!-- 新标签页打开的页面（数据大屏类）：渲染普通 <li>+<a>，不走 el-menu 路由，点击仅新开标签 -->
        <div
          v-if="val.isLink && !val.isIframe"
          :key="val.path"
          class="el-menu-item qx-newtab-item"
          :style="{ paddingLeft: 20 + depth * 20 + 'px' }"
        >
          <a :href="val.isLink" target="_blank" class="qx-newtab-link" :class="{ 'has-icon': val.icon }">
            <!-- 与普通 el-menu-item 分支保持一致：i 恒渲染（无图标时空占位），否则文字缩进和别的菜单对不齐 -->
            <i class="ivu-icon" :class="val.icon ? 'el-icon-' + val.icon : ''"></i>
            <span>{{ val.title }}</span>
          </a>
        </div>
        <el-menu-item v-else :index="val.path" :key="val.path">
          <template>
            <i class="ivu-icon" :class="val.icon ? 'el-icon-' + val.icon : ''"></i>
            <span>{{ val.title }}</span>
          </template>
        </el-menu-item>
      </template>
    </template>
  </div>
</template>

<script>
export default {
  name: 'subItem',
  props: {
    chil: {
      type: Array,
      default() {
        return [];
      },
    },
    // 菜单嵌套深度（1 = 顶级子菜单的直接子项），用于复刻 element-ui 的内联 padding-left（20 + 每层 20）
    depth: {
      type: Number,
      default: 1,
    },
  },
};
</script>

<style scoped>
.qx-newtab-link {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  color: inherit;
  text-decoration: none;
}
/* 有图标时才留图标与文字间距（无图标时与普通 el-menu-item 完全一致，避免多出空隙） */
.qx-newtab-link.has-icon {
  gap: 8px;
}
/* 新标签页菜单项样式兜底（与 el-menu-item 视觉一致）；
   padding-left 由内联样式按嵌套深度计算（复刻 element-ui：20 + 每层子菜单 20） */
.qx-newtab-item {
  list-style: none;
  line-height: 36px;
  padding: 0 20px;
  min-width: 160px;
  box-sizing: border-box;
  font-size: 14px;
  color: inherit;
  cursor: pointer;
  white-space: nowrap;
}
.qx-newtab-item:hover {
  background: rgba(31, 94, 255, 0.08);
}
</style>
