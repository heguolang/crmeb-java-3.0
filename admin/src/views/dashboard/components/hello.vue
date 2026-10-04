<!--
  首页问候条 · 方案 B「极光科技」（2026-10-04 新增）

  作用：登录第一屏先给一句人话 + 日期，右侧放「刷新数据」。
  数据依赖为零（只用 store 里的登录账号 + 本地时间），不新增接口、不改变任何业务逻辑。
-->
<template>
  <div class="hello-bar">
    <div class="hello-av">{{ initial }}</div>
    <div class="hello-tx">
      <h3>{{ greeting }}，{{ userName }}</h3>
      <p>{{ dateText }}</p>
    </div>
    <div class="hello-ops">
      <el-button size="small" @click="$emit('refresh')">
        <i class="el-icon-refresh"></i>
        刷新数据
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DashboardHello',
  computed: {
    // store 里存的是登录账号；取不到时兜底「管理员」
    userName() {
      const name = (this.$store.state.user && this.$store.state.user.name) || '';
      return name || '管理员';
    },
    initial() {
      return this.userName.charAt(0).toUpperCase();
    },
    greeting() {
      const h = new Date().getHours();
      if (h < 6) return '凌晨好';
      if (h < 12) return '上午好';
      if (h < 14) return '中午好';
      if (h < 18) return '下午好';
      return '晚上好';
    },
    dateText() {
      const d = new Date();
      const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][d.getDay()];
      return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 ${week}`;
    },
  },
};
</script>

<style scoped lang="scss">
.hello-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  background: #fff;
  border: 1px solid var(--prev-border-color-light);
  border-radius: var(--prev-radius-card);
  box-shadow: var(--prev-shadow-card);
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  margin-bottom: 14px;
}
/* 左侧渐变条 + 右上角光晕（方案 B 的「极光」来源） */
.hello-bar::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #22d3ee, #4f8cff);
}
.hello-bar::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -70px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(31, 94, 255, 0.14), transparent 70%);
  pointer-events: none;
}

.hello-av {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  background: var(--prev-color-primary-gradient);
  box-shadow: var(--prev-shadow-glow);
  color: #fff;
  font-size: 19px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  position: relative;
  z-index: 2;
}
.hello-tx {
  position: relative;
  z-index: 2;
  min-width: 0;
}
.hello-tx h3 {
  font-size: 19px;
  font-weight: 600;
  color: var(--prev-color-text-title);
  margin: 0;
}
.hello-tx p {
  font-size: 12.5px;
  color: var(--prev-color-text-secondary);
  margin: 3px 0 0;
}
.hello-ops {
  margin-left: auto;
  position: relative;
  z-index: 2;
  flex: 0 0 auto;
}
</style>
