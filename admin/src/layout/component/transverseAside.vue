<template>
  <div class="layout-columns-tra-aside el-menu-horizontal-warp">
    <el-scrollbar ref="elMenuHorizontalScrollRef" @wheel.native.prevent="onElMenuHorizontalScroll">
      <ul>
        <li
          v-for="(v, k) in columnsAsideList"
          :key="k"
          @click="onColumnsAsideMenuClick(v)"
          ref="columnsAsideOffsetLeftRefs"
          class="layout-columns"
          :class="{ 'layout-columns-active': v.k === liIndex }"
          :title="v.title"
        >
          <div :class="setColumnsAsidelayout">
            <!-- 图标 + 完整标题（原来是纯文字、且被截断到 3 个字） -->
            <i v-if="v.icon" :class="'el-icon-' + v.icon"></i>
            <div class="columns-title">{{ v.title }}</div>
          </div>
        </li>
        <div ref="columnsAsideActiveRef" :class="setColumnsAsideStyle"></div>
      </ul>
    </el-scrollbar>
  </div>
</template>

<script>
import { getMenuSider, getHeaderName, findFirstNonNullChildren } from '@/utils/system.js';
import Logo from '@/layout/logo/index.vue';

export default {
  name: 'layoutColumnsAside',
  components: { Logo },
  data() {
    return {
      columnsAsideList: [],
      liIndex: 0,
      difference: 0,
      routeSplit: [],
      activePath: '',
    };
  },
  computed: {
    // 设置分栏高亮风格
    setColumnsAsideStyle() {
      return this.$store.state.themeConfig.themeConfig.columnsAsideStyle;
    },
    // 设置分栏布局风格
    setColumnsAsidelayout() {
      return this.$store.state.themeConfig.themeConfig.columnsAsideLayout;
    },
    Layout() {
      return this.$store.state.themeConfig.themeConfig.Layout;
    },
    routesList() {
      this.$store.state.user.menuList;
    },
  },
  beforeDestroy() {
    this.bus.$off('routesListChange');
  },
  mounted() {
    this.bus.$on('routesListChange', () => {
      this.setFilterRoutes();
    });
    this.setFilterRoutes();
    // 自身拉起菜单：本组件不依赖 Asides/ColumnsAside，
    // 若首次挂载时 store.menuList 还是空的（新环境、非登录页进入），
    // 后续又没有任何事件能触发重建，顶部一级菜单就会一直是空的。
    this.$store
      .dispatch('user/getMenus')
      .then(() => {
        this.setFilterRoutes();
        this.$nextTick(() => this.initElMenuOffsetLeft());
      })
      .catch(() => {});
    this.$nextTick((e) => {
      this.initElMenuOffsetLeft();
    });
  },
  methods: {
    // 设置横向滚动条可以鼠标滚轮滚动
    onElMenuHorizontalScroll(e) {
      const eventDelta = e.wheelDelta || -e.deltaY * 40;
      this.$refs.elMenuHorizontalScrollRef.$refs.wrap.scrollLeft =
        this.$refs.elMenuHorizontalScrollRef.$refs.wrap.scrollLeft + eventDelta / 4;
    },
    // 初始化数据，页面刷新时，滚动条滚动到对应位置
    initElMenuOffsetLeft() {
      this.$nextTick(() => {
        let els = document.querySelector('.layout-columns.layout-columns-active');
        if (!els) return false;
        this.$refs.elMenuHorizontalScrollRef.$refs.wrap.scrollLeft = els.offsetLeft;
      });
    },
    // 设置菜单高亮位置移动
    setColumnsAsideMove(k) {
      if (k === undefined) return false;
      const els = this.$refs.columnsAsideOffsetLeftRefs;
      this.liIndex = k;
      // 项宽现已由内容决定（原先是固定 70px），高亮条需同步宽度否则会错位
      this.$refs.columnsAsideActiveRef.style.left = `${els[k].offsetLeft + this.difference}px`;
      this.$refs.columnsAsideActiveRef.style.width = `${els[k].offsetWidth}px`;
    },
    // 菜单高亮点击事件
    onColumnsAsideMenuClick(v) {
      let { path, redirect } = v;
      if (v.children.length) {
        this.$router.push(findFirstNonNullChildren(v.children).path);
      } else {
        this.$router.push(path);
      }
      // 一个路由设置自动收起菜单
      if (!v.children || v.children.length <= 1) this.$store.state.themeConfig.themeConfig.isCollapse = true;
      else if (v.children.length > 1) this.$store.state.themeConfig.themeConfig.isCollapse = false;
      // this.bus.$emit('setSendColumnsChildren', getMenuSider(this.columnsAsideList, path));
    },
    // 设置高亮动态位置
    onColumnsAsideDown(k) {
      this.$nextTick(() => {
        this.setColumnsAsideMove(k);
      });
    },
    // 设置/过滤路由（非静态路由/是否显示在菜单中）
    setFilterRoutes() {
      if (this.$store.state.user.menuList.length <= 0) return false;
      this.columnsAsideList = this.filterRoutesFun(this.$store.state.user.menuList);
      //   const resData = getHeaderName(this.$route.path, this.columnsAsideList);
      const resData = this.setSendChildren(getHeaderName(this.$route, this.columnsAsideList));
      // 防御：setSendChildren 找不到对应一级菜单时返回 {}，
      // 原写法 `!resData && !resData.item[0]...` 的 && 写反了，会抛错并中断菜单写入
      if (!resData || !resData.item || !resData.item[0]) {
        this.bus.$emit('setSendColumnsChildren', []);
        this.$store.commit('user/childMenuList', []);

        this.$store.state.themeConfig.themeConfig.isCollapse = true;
        return false;
      }
      this.bus.$emit('oneCatName', resData.item[0].title);
      this.onColumnsAsideDown(resData.item[0].k);
      // 刷新时，初始化一个路由设置自动收起菜单
      resData.item[0].children.length > 0
        ? (this.$store.state.themeConfig.themeConfig.isCollapse = false)
        : (this.$store.state.themeConfig.themeConfig.isCollapse = true);
      this.bus.$emit('setSendColumnsChildren', resData.item[0].children || []);
      this.$store.commit('user/childMenuList', resData.item[0].children || []);
    },
    // 传送当前子级数据到菜单中
    setSendChildren(path) {
      // const currentPathSplit = path.split('/');
      let currentData = {};
      this.columnsAsideList.map((v, k) => {
        v['k'] = k;
        if (v.path === path) {
          currentData['item'] = [{ ...v }];
          //   currentData['children'] = [{ ...v }];
          if (v.children.length) currentData['children'] = v.children;
        }
      });
      return currentData;
    },
    // 路由过滤递归函数
    filterRoutesFun(arr) {
      return arr
        .filter((item) => item.path)
        .map((item) => {
          item = Object.assign({}, item);
          if (item.children.length) item.children = this.filterRoutesFun(item.children);
          return item;
        });
    },
    // tagsView 点击时，根据路由查找下标 columnsAsideList，实现左侧菜单高亮
    setColumnsMenuHighlight(path) {
      // this.routeSplit = path.split('/');
      // this.routeSplit.shift();
      // const routeFirst = `/${this.routeSplit[0]}`;
      const currentSplitRoute = this.columnsAsideList.find((v) => v.path === path);
      if (!currentSplitRoute) {
        this.onColumnsAsideDown(0);
        return false;
      }
      // 延迟拿值，防止取不到
      setTimeout(() => {
        this.onColumnsAsideDown(currentSplitRoute.k);
      }, 0);
    },
  },
  watch: {
    // 监听 vuex 数据变化
    '$store.state': {
      handler(val) {
        val.themeConfig.themeConfig.columnsAsideStyle === 'columnsRound'
          ? (this.difference = 3)
          : (this.difference = 0);
        if (val.user.menuListlength === this.columnsAsideList.length) return false;
      },
      deep: true,
    },
    // 监听路由的变化
    $route: {
      handler(to) {
        this.setColumnsMenuHighlight(to.path);
        // this.setColumnsAsideMove();
        let HeadName = getHeaderName(to, this.columnsAsideList);
        let asideList = getMenuSider(this.columnsAsideList, HeadName)[0].children;
        const resData = this.setSendChildren(HeadName);
        if (resData && resData.item && resData.item.length) {
          this.onColumnsAsideDown(resData.item[0].k);
          this.bus.$emit('oneCatName', resData.item[0].title);
        }

        this.bus.$emit('setSendColumnsChildren', asideList || []);
        this.$store.commit('user/childMenuList', asideList || []);
      },
      deep: true,
    },
  },
};
</script>

<style scoped lang="scss">
::v-deep .el-scrollbar__bar.is-horizontal {
  height: 0;
}
.el-menu-horizontal-warp {
  ::v-deep .el-scrollbar__bar.is-vertical {
    display: none;
  }
  ::v-deep .el-scrollbar__wrap {
    overflow-y: hidden !important;
    overflow-x: scroll !important;
  }
  ::v-deep a {
    width: 100%;
  }
  .el-menu.el-menu--horizontal {
    display: flex;
    height: 100%;
    width: 100%;
    box-sizing: border-box;
  }
}

.layout-columns-tra-aside {
  height: 100%;
  /* 透明：让顶栏容器的渐变整条连续透下来。
     若在这里再写一遍渐变，因为渐变原点是各自元素，Logo 区与菜单区会出现断层 */
  background: transparent;
  overflow-y: hidden;

  ul {
    position: relative;
    display: flex;
    /* 高度必须明确写死：中间隔着 el-scrollbar__wrap / __view 两层无高度的 div，
       用 height:100% 会塌缩成内容高度（实测只剩 17px，菜单变成一条细线） */
    height: 56px;
    align-items: stretch;

    li {
      color: var(--prev-bg-topBarColor);
      height: 100%;
      text-align: center;
      display: flex;
      cursor: pointer;
      position: relative;
      z-index: 1;
      transition: background-color 0.16s ease;

      /* 顶栏是深色渐变，悬停用半透明白
         （--prev-bg-menu-hover-ba-color 已改为白底左侧菜单用的淡蓝，此处不适用） */
      &:hover {
        background: rgba(255, 255, 255, 0.16);
      }

      .columns-horizontal,
      .columns-vertical {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        padding: 0 20px;
        /* 2026-09-25 用户要求：顶栏字号小一号（15→14） */
        font-size: 14px;

        i {
          margin-right: 6px;
          font-size: 16px;
        }
      }

      a {
        text-decoration: none;
        color: inherit;
      }
    }

    .layout-columns-active {
      color: var(--prev-MenuActiveColor);
      background: rgba(255, 255, 255, 0.08);
    }

    /* 选中高亮条：横向排列，贴在底部（宽度由 JS 按项宽同步） */
    .columns-round {
      background: var(--prev-color-primary);
      position: absolute;
      left: 0;
      bottom: 0;
      height: 3px;
      border-radius: 0;
      z-index: 0;
      transition: 0.3s ease-in-out;
    }

    .columns-card {
      @extend .columns-round;
    }
  }
}
::v-deep .el-scrollbar {
  height: 100%;
}
::v-deep .el-scrollbar__bar.is-horizontal {
  display: none;
}
::v-deep .el-scrollbar__thumb {
  display: none;
}
</style>
