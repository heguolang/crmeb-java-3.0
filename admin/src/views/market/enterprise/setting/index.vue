<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar">
        <span>服务设置：控制 H5 软件开发首页 / 会员中心 / 底部导航的启用状态，以及公司名称与联系方式（首页 CTA 展示）。</span>
      </div>

      <el-form ref="form" :model="form" label-width="200px" size="small" class="setting-form mt20" style="max-width: 640px">
        <el-divider content-position="left">前台页面切换</el-divider>
        <el-form-item label="软件开发首页设为商城主页">
          <el-switch v-model="form.svc_home_mode" active-value="1" inactive-value="0" />
          <div class="tip">启用后 H5 打开商城主页显示「软件开发服务」首页（关闭则恢复原商城首页）</div>
        </el-form-item>
        <el-form-item label="软件开发会员中心">
          <el-switch v-model="form.svc_user_mode" active-value="1" inactive-value="0" />
          <div class="tip">启用后 H5 会员中心显示服务型会员中心（项目/合同统计）</div>
        </el-form-item>
        <el-form-item label="软件开发底部导航栏">
          <el-switch v-model="form.svc_nav_mode" active-value="1" inactive-value="0" />
          <div class="tip">启用后软件开发页面底部显示「首页 / 案例 / 项目 / 我的」导航</div>
        </el-form-item>
        <el-form-item label="签约短信验证">
          <el-switch v-model="form.svc_sign_sms" active-value="1" inactive-value="0" />
          <div class="tip">开启后会员签约需填写负责人手机号并验证短信验证码；关闭则只需手写签名</div>
        </el-form-item>

        <el-divider content-position="left">基础信息</el-divider>
        <el-form-item label="公司名称">
          <el-input v-model="form.svc_company_name" maxlength="30" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.svc_contact_phone" maxlength="20" placeholder="首页「免费获取开发方案」展示" />
        </el-form-item>
        <el-form-item label="联系微信">
          <el-input v-model="form.svc_contact_wechat" maxlength="30" />
        </el-form-item>
        <el-form-item label="响应承诺文案">
          <el-input v-model="form.svc_contact_time" maxlength="50" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="small" :loading="saving" @click="onSave">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { svcSettingGetApi, svcSettingSaveApi } from '@/api/enterprise';

export default {
  name: 'SvcSetting',
  data() {
    return {
      saving: false,
      form: {
        svc_home_mode: '0',
        svc_user_mode: '0',
        svc_nav_mode: '0',
        svc_sign_sms: '1',
        svc_company_name: '',
        svc_contact_phone: '',
        svc_contact_wechat: '',
        svc_contact_time: ''
      }
    };
  },
  mounted() {
    this.getSetting();
  },
  methods: {
    getSetting() {
      svcSettingGetApi().then((res) => {
        Object.keys(this.form).forEach((k) => {
          if (res[k] !== undefined && res[k] !== null) this.form[k] = String(res[k]);
        });
      });
    },
    onSave() {
      this.saving = true;
      svcSettingSaveApi(this.form)
        .then(() => {
          this.$message.success('保存成功');
        })
        .finally(() => {
          this.saving = false;
        });
    }
  }
};
</script>

<style scoped lang="scss">
.tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 2px; }
</style>
