<template>
  <div class="divBg addContent-wrapper">
    <el-card :bordered="false" shadow="never" class="mt16">
      <div class="summary-bar">
        <span>招聘设置：控制会员端招聘首页 / 投递短信验证的开关，以及首页 banner 与联系方式（会员端展示）。</span>
      </div>

      <el-form ref="form" :model="form" label-width="200px" size="small" class="setting-form mt20" style="max-width: 680px">
        <el-divider content-position="left">前台页面与验证</el-divider>
        <el-form-item label="招聘首页设为商城主页">
          <el-switch v-model="form.rec_home_mode" active-value="1" inactive-value="0" />
          <div class="tip">
            启用后 H5 打开商城主页直接显示「招聘」首页（关闭则恢复原装修首页）
          </div>
        </el-form-item>
        <el-form-item label="投递短信验证">
          <el-switch v-model="form.rec_sms_verify" active-value="1" inactive-value="0" />
          <div class="tip">开启后用户投递需填写手机号并验证短信验证码；关闭则只填姓名+电话即可提交</div>
        </el-form-item>

        <el-divider content-position="left">首页展示</el-divider>
        <el-form-item label="公司名称">
          <el-input v-model="form.rec_company_name" maxlength="30" />
        </el-form-item>
        <el-form-item label="首页主标题">
          <el-input v-model="form.rec_banner_title" maxlength="40" placeholder="加入我们 · 与优秀的人一起做有价值的事" />
        </el-form-item>
        <el-form-item label="首页副标题">
          <el-input v-model="form.rec_banner_desc" maxlength="60" />
        </el-form-item>

        <el-divider content-position="left">联系方式与提示</el-divider>
        <el-form-item label="招聘联系人">
          <el-input v-model="form.rec_contact_name" maxlength="20" placeholder="人事部" />
        </el-form-item>
        <el-form-item label="招聘联系电话">
          <el-input v-model="form.rec_contact_phone" maxlength="20" placeholder="首页与职位详情展示" />
        </el-form-item>
        <el-form-item label="招聘联系微信">
          <el-input v-model="form.rec_contact_wechat" maxlength="30" />
        </el-form-item>
        <el-form-item label="投递页提示文案">
          <el-input v-model="form.rec_tip" maxlength="60" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="small" :loading="saving" @click="onSave">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { recruitSettingGetApi, recruitSettingSaveApi } from '@/api/recruit';

export default {
  name: 'RecruitSetting',
  data() {
    return {
      saving: false,
      form: {
        rec_home_mode: '0',
        rec_sms_verify: '0',
        rec_company_name: '',
        rec_banner_title: '',
        rec_banner_desc: '',
        rec_contact_name: '',
        rec_contact_phone: '',
        rec_contact_wechat: '',
        rec_tip: ''
      }
    };
  },
  mounted() {
    this.getSetting();
  },
  methods: {
    getSetting() {
      recruitSettingGetApi().then((res) => {
        Object.keys(this.form).forEach((k) => {
          if (res[k] !== undefined && res[k] !== null) this.form[k] = String(res[k]);
        });
      });
    },
    onSave() {
      this.saving = true;
      recruitSettingSaveApi(this.form)
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
