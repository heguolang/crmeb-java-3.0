<template>
  <div
    class="page-account"
    :style="
      backgroundImages
        ? { backgroundImage: 'url(' + backgroundImages + ')' }
        : { backgroundImage: 'url(' + backgroundImageMo + ')' }
    "
  >
    <div class="login-card" :class="{ 'login-card--mobile': fullWidth <= 768 }">
      <!-- 顶部 logo：优先后台配置的登录 logo，未配置时用默认盾牌 -->
      <div class="card-logo">
        <img v-if="loginLogo" :src="loginLogo" alt="logo" class="card-logo-img" />
        <div v-else class="card-logo-icon">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="#2b6fe3">
            <path d="M12 2l8 3.6v5.2c0 5-3.4 9.6-8 11.2-4.6-1.6-8-6.2-8-11.2V5.6L12 2zm-1.2 13.6l5.6-5.6-1.5-1.5-4.1 4.1-1.9-1.9-1.5 1.5 3.4 3.4z" />
          </svg>
        </div>
      </div>

      <div class="form-title">欢迎回来</div>
      <div class="form-desc">请使用管理员账号登录后台管理系统</div>

      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        autocomplete="on"
        label-position="left"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="account">
          <el-input
            ref="account"
            v-model="loginForm.account"
            prefix-icon="el-icon-user"
            placeholder="管理员账号"
            name="username"
            type="text"
            tabindex="1"
          />
        </el-form-item>

        <el-form-item prop="pwd">
          <el-input
            :key="passwordType"
            ref="pwd"
            v-model="loginForm.pwd"
            prefix-icon="el-icon-lock"
            :type="passwordType"
            placeholder="登录密码"
            name="pwd"
            tabindex="2"
          />
          <span class="show-pwd" @click="showPwd">
            <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>

        <el-form-item v-if="captchaSwitch" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              class="captcha-input"
              prefix-icon="el-icon-key"
              placeholder="验证码"
              name="captchaCode"
              maxlength="4"
              tabindex="3"
            />
            <div class="captcha-img" title="点击刷新" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else class="captcha-img-loading">加载中</span>
            </div>
            <div class="captcha-refresh" title="刷新验证码" @click="loadCaptcha">
              <i class="el-icon-refresh-right" />
            </div>
          </div>
        </el-form-item>

        <div class="form-tools">
          <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
        </div>

        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="login-btn"
            @click.native.prevent="handleLogin"
            :disabled="disabled"
            >登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">建议使用 Chrome / Edge 浏览器访问 · 分辨率 1440×900 以上</div>
    </div>
  </div>
</template>

<script>
import '@/assets/js/canvas-nest.min.js';
import { getLoginPicApi, getImageCaptchaApi } from '@/api/user';
import { getStoreStaff } from '@/libs/public';
import { frontDomainApi, mediaDomainApi } from '@/api/systemConfig';
export default {
  name: 'Login',
  data() {
    return {
      loginLogo: '',
      backgroundImages: '',
      backgroundImageMo: require('@/assets/imgs/bg.jpg'),
      fullWidth: document.body.clientWidth,
      rememberAccount: false,
      captchaImage: '',
      // 后台登录数字验证码开关（系统设置-系统配置-基础配置），默认开启，以接口返回为准
      captchaSwitch: true,
      loginForm: {
        account: '',
        pwd: '',
        captchaKey: '',
        captchaCode: '',
        captchaVO: {},
      },
      passwordType: 'password',
      loading: false,
      redirect: undefined,
      otherQuery: {},
      disabled: false,
    };
  },
  computed: {
    // 验证码开关关闭时不校验验证码字段
    loginRules() {
      const rules = {
        account: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
        pwd: [{ required: true, trigger: 'blur', message: '请输入密码' }],
      };
      if (this.captchaSwitch) {
        rules.captchaCode = [{ required: true, trigger: 'blur', message: '请输入验证码' }];
      }
      return rules;
    },
  },
  watch: {
    fullWidth(val) {
      if (!this.timer) {
        this.screenWidth = val;
        this.timer = true;
        const that = this;
        setTimeout(function () {
          that.timer = false;
        }, 400);
      }
    },
    rememberAccount(val) {
      if (!val) localStorage.removeItem('adminRememberAccount');
    },
    $route: {
      handler: function (route) {
        const query = route.query;
        if (query) {
          this.redirect = query.redirect;
          this.otherQuery = this.getOtherQuery(query);
        }
      },
      immediate: true,
    },
  },
  created() {
    const _this = this;
    document.onkeydown = function (e) {
      if (_this.$route.path.indexOf('login') !== -1) {
        const key = window.event.keyCode;
        if (key === 13) {
          _this.handleLogin();
        }
      }
    };
    window.addEventListener('resize', this.handleResize);
    const remembered = localStorage.getItem('adminRememberAccount');
    if (remembered) {
      this.loginForm.account = remembered;
      this.rememberAccount = true;
    }
  },
  mounted() {
    this.getInfo();
    this.loadCaptcha();
    this.$nextTick(() => {
      if (this.screenWidth < 768) {
        document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
      } else {
        document.getElementsByTagName('canvas')[0].className = 'index_bg';
      }
    });
    if (this.loginForm.account === '') {
      this.$refs.account.focus();
    } else if (this.loginForm.pwd === '') {
      this.$refs.pwd.focus();
    }
  },
  beforeCreate() {
    if (this.fullWidth < 768) {
      document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
    } else {
      document.getElementsByTagName('canvas')[0].className = 'index_bg';
    }
  },
  destroyed() {
    // window.removeEventListener('storage', this.afterQRScan)
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.handleResize);
    document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
  },
  methods: {
    // 获取图形验证码（开关关闭时不需要加载）
    loadCaptcha() {
      if (!this.captchaSwitch) return;
      getImageCaptchaApi()
        .then((res) => {
          this.captchaImage = res.image;
          this.loginForm.captchaKey = res.key;
          this.loginForm.captchaCode = '';
        })
        .catch(() => {
          this.captchaImage = '';
        });
    },
    // 获取移动端域名-图片域名
    async getUrl() {
      frontDomainApi().then((res) => {
        this.$store.commit('settings/SET_FrontDomain', res);
      });
      mediaDomainApi().then((res) => {
        this.$store.commit('settings/SET_mediaDomain', res);
      });
    },
    handleResize(event) {
      this.fullWidth = document.body.clientWidth;
      if (this.fullWidth < 768) {
        document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
      } else {
        document.getElementsByTagName('canvas')[0].className = 'index_bg';
      }
    },
    getInfo() {
      getLoginPicApi().then((res) => {
        this.loginLogo = res.loginLogo;
        this.backgroundImages = res.backgroundImage;
        if (res.siteName) {
          localStorage.setItem('singleAdminSiteName', res.siteName);
        }
        // 数字验证码开关：'1' 开启 / '0' 关闭，接口未下发时保持开启
        const sw = res.captchaSwitch;
        if (sw !== undefined && sw !== null && String(sw) !== '1') {
          this.captchaSwitch = false;
          this.captchaImage = '';
          this.loginForm.captchaKey = '';
          this.loginForm.captchaCode = '';
        }
      });
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = '';
      } else {
        this.passwordType = 'password';
      }
      this.$nextTick(() => {
        this.$refs.pwd.focus();
      });
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          if (this.rememberAccount) {
            localStorage.setItem('adminRememberAccount', this.loginForm.account);
          } else {
            localStorage.removeItem('adminRememberAccount');
          }
          this.success(null);
        } else {
          return false;
        }
      });
    },
    success(params, type) {
      const loading = this.$loading({
        lock: true,
        text: '正在登录中.',
      });
      this.$store
        .dispatch('user/login', this.loginForm)
        .then(() => {
          this.$router.push({
            path: this.redirect || '/',
            query: this.otherQuery,
          });
          getStoreStaff();
          loading.close();
          this.disabled = true;
          this.getUrl();
          this.$store
            .dispatch('user/getMenus', {
              that: this,
            })
            .then((res) => {
              this.$router.push({ path: this.redirect || '/dashboard', query: this.otherQuery });
              //location.reload();
            });
        })
        .catch(async (err) => {
          loading.close();
          this.disabled = false;
          this.loadCaptcha();
        });
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur];
        }
        return acc;
      }, {});
    },
  },
};
</script>

<style lang="scss" scoped>
.page-account {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: auto;

  @media (min-width: 768px) {
    background-repeat: no-repeat;
    background-position: center;
    background-size: cover;
  }
}

.login-card {
  width: 460px;
  box-sizing: border-box;
  padding: 44px 44px 26px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 24px 60px rgba(9, 30, 66, 0.35);
  z-index: 1;

  &--mobile {
    width: auto;
    margin: 0 16px;
    padding: 36px 24px 22px;
  }
}

/* 顶部 logo */
.card-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;

  .card-logo-img {
    max-height: 60px;
    max-width: 180px;
    object-fit: contain;
  }

  .card-logo-icon {
    width: 62px;
    height: 62px;
    border-radius: 16px;
    background: #eef4ff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a2233;
}

.form-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #8a94a6;
}

.login-form {
  margin-top: 26px;
  position: relative;
  max-width: 100%;
  overflow: hidden;
}

.login-form ::v-deep .el-input__inner {
  height: 46px;
  line-height: 46px;
  border-radius: 8px;
  background: #f7f8fc;
  border-color: #e6eaf2;

  &:focus {
    background: #fff;
    border-color: #2b6fe3;
  }
}

.login-form ::v-deep .el-input__icon {
  line-height: 46px;
}

.show-pwd {
  position: absolute;
  right: 12px;
  top: 14px;
  font-size: 16px;
  color: #889aa4;
  cursor: pointer;
  user-select: none;
}

.captcha-row {
  display: flex;
  align-items: center;

  .captcha-input {
    flex: 1;
    min-width: 0;
  }

  .captcha-img {
    width: 130px;
    height: 46px;
    margin-left: 12px;
    border-radius: 8px;
    overflow: hidden;
    background: #eef1f7;
    cursor: pointer;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      display: block;
    }
  }

  .captcha-img-loading {
    color: #8a94a6;
    font-size: 12px;
  }

  .captcha-refresh {
    width: 44px;
    height: 46px;
    margin-left: 8px;
    border-radius: 8px;
    border: 1px solid #e6eaf2;
    background: #f7f8fc;
    color: #2b6fe3;
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    flex-shrink: 0;

    &:hover {
      background: #eef4ff;
    }
  }
}

.form-tools {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 18px;

  ::v-deep .el-checkbox__label {
    color: #5a6478;
    font-size: 13px;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 6px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #2b6fe3 0%, #3a8df2 100%);
  box-shadow: 0 8px 18px rgba(43, 111, 227, 0.32);

  &:hover {
    opacity: 0.92;
  }
}

.form-footer {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: #aab2c0;
}
</style>
