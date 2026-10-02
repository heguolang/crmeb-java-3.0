<template>
  <view class="svc-ds">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">提交需求</text>
    </view>

    <view class="page-body">
      <view class="form-card">
        <view class="fi">
          <view class="lb"><text class="rq">*</text>需求类型</view>
          <picker mode="selector" :range="typeOptions" @change="onTypeChange">
            <view class="fd" :class="{ ph: !form.type }">
              {{ form.type || '请选择需求类型' }}
              <text class="ar">›</text>
            </view>
          </picker>
        </view>

        <view class="fi">
          <view class="lb"><text class="rq">*</text>需求标题</view>
          <input v-model="form.title" class="fd input" maxlength="60" placeholder="一句话说清要做什么，如：门店会员小程序" />
        </view>

        <view class="fi col">
          <view class="lb"><text class="rq">*</text>需求描述</view>
          <textarea
            v-model="form.content"
            class="fd textarea"
            maxlength="1000"
            placeholder="功能范围、想解决的问题、必须对接的系统、参考产品等，越具体报价越准"
          />
          <view class="counter">{{ (form.content || '').length }}/1000</view>
        </view>
      </view>

      <view class="form-card">
        <view class="fi">
          <view class="lb">预算区间</view>
          <picker mode="selector" :range="budgetOptions" @change="onBudgetChange">
            <view class="fd" :class="{ ph: !form.budget }">
              {{ form.budget || '请选择预算区间' }}
              <text class="ar">›</text>
            </view>
          </picker>
        </view>

        <view class="fi">
          <view class="lb">期望完成</view>
          <picker mode="date" :value="form.hopeDate" :start="today" @change="onDateChange">
            <view class="fd" :class="{ ph: !form.hopeDate }">
              {{ form.hopeDate || '请选择期望完成时间' }}
              <text class="ar">›</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="form-card">
        <view class="fi col">
          <view class="lb">参考附件</view>
          <view class="up-row">
            <view v-for="(f, i) in attachList" :key="i" class="up-item">
              <image :src="f.url" class="up-img" mode="aspectFill" @click="preview(i)" />
              <text class="up-del" @click="removeAttach(i)">✕</text>
            </view>
            <view v-if="attachList.length < 6" class="up-add" @click="chooseImage">＋</view>
          </view>
          <view class="hint">支持上传截图 / 参考图，最多 6 张</view>
        </view>
      </view>

      <view class="form-card">
        <view class="fi">
          <view class="lb">联系人</view>
          <input v-model="form.contactName" class="fd input" maxlength="20" placeholder="怎么称呼您" />
        </view>
        <view class="fi">
          <view class="lb">手机号</view>
          <input v-model="form.contactPhone" class="fd input" type="number" maxlength="11" placeholder="便于顾问联系您" />
        </view>
        <view class="fi last">
          <view class="lb">公司 / 单位</view>
          <input v-model="form.company" class="fd input" maxlength="60" placeholder="选填，开票与合同会用到" />
        </view>
      </view>

      <view class="tip-box">提交后顾问会在 1 个工作日内联系你，报价与处理进度都能在「我的需求」里看到。</view>

      <view class="btn-primary" :class="{ disabled: submitting }" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交需求' }}
      </view>
      <view style="height: 60rpx"></view>
    </view>
  </view>
</template>

<script>
import { submitSvcDemand } from '@/api/svc.js';
import { mapGetters } from 'vuex';

export default {
  data() {
    return {
      submitting: false,
      typeOptions: ['小程序', 'APP开发', '系统定制', 'UI 设计', '网站建设', '其他'],
      budgetOptions: ['1 万以下', '1~5 万', '5~10 万', '10~30 万', '30 万以上', '面议'],
      attachList: [],
      today: '',
      form: {
        type: '',
        title: '',
        content: '',
        budget: '',
        hopeDate: '',
        attach: '',
        attachName: '',
        contactName: '',
        contactPhone: '',
        company: ''
      }
    };
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  onLoad() {
    const d = new Date();
    const pad = (n) => (n < 10 ? '0' + n : '' + n);
    this.today = d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
    const u = this.userInfo || {};
    this.form.contactName = u.nickname || '';
    this.form.contactPhone = u.phone || '';
  },
  methods: {
    onTypeChange(e) {
      this.form.type = this.typeOptions[e.detail.value];
    },
    onBudgetChange(e) {
      this.form.budget = this.budgetOptions[e.detail.value];
    },
    onDateChange(e) {
      this.form.hopeDate = e.detail.value;
    },
    chooseImage() {
      const left = 6 - this.attachList.length;
      if (left <= 0) return;
      this.$util.uploadImageOne(
        { url: 'upload/image', name: 'pics', model: 'svc', pid: 7, count: left },
        (res) => {
          const url = (res.data && (res.data.url || res.data.sattDir)) || '';
          if (!url) {
            uni.showToast({ title: '上传失败', icon: 'none' });
            return;
          }
          this.attachList.push({ url, name: this.nameFromUrl(url) });
          this.syncAttach();
        },
        () => {}
      );
    },
    preview(i) {
      const urls = this.attachList.map((f) => f.url);
      uni.previewImage({ urls, current: i });
    },
    removeAttach(i) {
      this.attachList.splice(i, 1);
      this.syncAttach();
    },
    syncAttach() {
      this.form.attach = this.attachList.map((f) => f.url).join(',');
      this.form.attachName = this.attachList.map((f) => String(f.name || '').replace(/,/g, '_')).join(',');
    },
    nameFromUrl(url) {
      const s = String(url || '').split('?')[0];
      const last = s.substring(s.lastIndexOf('/') + 1);
      try {
        return decodeURIComponent(last);
      } catch (e) {
        return last;
      }
    },
    onSubmit() {
      if (this.submitting) return;
      if (!this.form.type) return uni.showToast({ title: '请选择需求类型', icon: 'none' });
      if (!String(this.form.title || '').trim()) return uni.showToast({ title: '请填写需求标题', icon: 'none' });
      if (!String(this.form.content || '').trim()) return uni.showToast({ title: '请填写需求描述', icon: 'none' });
      if (!String(this.form.contactName || '').trim()) return uni.showToast({ title: '请填写联系人', icon: 'none' });
      if (!/^1[3-9]\d{9}$/.test(String(this.form.contactPhone || '').trim())) {
        return uni.showToast({ title: '请填写正确的手机号', icon: 'none' });
      }
      this.submitting = true;
      submitSvcDemand(this.form)
        .then(() => {
          uni.showToast({ title: '提交成功', icon: 'success' });
          setTimeout(() => {
            uni.redirectTo({ url: '/pages/software/demand/index', fail: () => uni.navigateBack() });
          }, 800);
        })
        .catch((e) => {
          uni.showToast({ title: typeof e === 'string' ? e : '提交失败，请稍后重试', icon: 'none' });
        })
        .finally(() => {
          this.submitting = false;
        });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-ds { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.page-body { padding: 20rpx 30rpx 0; }

.form-card {
  background: #fff; border-radius: 24rpx; padding: 10rpx 30rpx; margin-bottom: 24rpx;
}
.fi { padding: 22rpx 0; border-bottom: 1rpx solid #f2f4f8; position: relative; }
.fi.last { border-bottom: none; }
.fi.col { display: block; }
.lb { font-size: 26rpx; color: #6b7785; margin-bottom: 12rpx; }
.rq { color: #e24c4c; margin-right: 6rpx; }
.fd {
  font-size: 28rpx; color: #222; min-height: 44rpx; line-height: 44rpx;
  display: flex; align-items: center; justify-content: space-between;
  &.ph { color: #b8c1cc; }
}
.input {
  width: 100%; height: 60rpx; line-height: 60rpx;
  font-size: 28rpx; color: #222;
}
.textarea {
  width: 100%; min-height: 180rpx; font-size: 28rpx; color: #222; line-height: 1.7;
}
.counter { text-align: right; font-size: 22rpx; color: #b8c1cc; }
.ar { color: #c0c8d4; font-size: 34rpx; }

.up-row { display: flex; flex-wrap: wrap; }
.up-item { position: relative; margin-right: 20rpx; margin-bottom: 10rpx; }
.up-img { width: 140rpx; height: 140rpx; border-radius: 16rpx; background: #eef1f6; }
.up-del {
  position: absolute; right: -10rpx; top: -10rpx;
  width: 36rpx; height: 36rpx; border-radius: 50%;
  background: rgba(0, 0, 0, 0.55); color: #fff; font-size: 22rpx;
  display: flex; align-items: center; justify-content: center;
}
.up-add {
  width: 140rpx; height: 140rpx; border-radius: 16rpx;
  border: 2rpx dashed #d6dde8; color: #b8c1cc; font-size: 46rpx;
  display: flex; align-items: center; justify-content: center;
}
.hint { font-size: 22rpx; color: #9aa4b2; margin-top: 12rpx; }

.tip-box {
  font-size: 23rpx; color: #6b7785; line-height: 1.7;
  background: #eef4ff; border-radius: 16rpx; padding: 20rpx 22rpx; margin-bottom: 30rpx;
}
.btn-primary {
  height: 92rpx; border-radius: 46rpx; background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 30rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 10rpx 24rpx rgba(29, 107, 243, 0.28);
  &.disabled { opacity: 0.6; }
}
</style>
