<template>
  <view class="svc-ivs">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">申请开票</text>
    </view>

    <view class="page-body">
      <!-- 关联合同 -->
      <view class="form-card">
        <view class="fi last">
          <view class="lb">关联合同</view>
          <picker mode="selector" :range="contractLabels" @change="onContractChange">
            <view class="fd" :class="{ ph: !form.contractId }">
              {{ contractLabel || '选填，选择后自动带出金额与抬头' }}
              <text class="ar">›</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 发票信息 -->
      <view class="form-card">
        <view class="fi">
          <view class="lb"><text class="rq">*</text>发票类型</view>
          <picker mode="selector" :range="typeOptions" @change="onTypeChange">
            <view class="fd" :class="{ ph: !form.type }">
              {{ typeText || '请选择发票类型' }}
              <text class="ar">›</text>
            </view>
          </picker>
        </view>

        <view class="fi">
          <view class="lb"><text class="rq">*</text>抬头类型</view>
          <view class="seg">
            <view
              v-for="(t, i) in titleTypeOptions"
              :key="i"
              class="seg-item"
              :class="{ on: form.titleType === t.value }"
              @click="form.titleType = t.value"
            >{{ t.name }}</view>
          </view>
        </view>

        <view class="fi">
          <view class="lb"><text class="rq">*</text>{{ form.titleType === 2 ? '个人姓名' : '公司名称' }}</view>
          <input
            v-model="form.title"
            class="fd input"
            maxlength="60"
            :placeholder="form.titleType === 2 ? '请填写个人姓名' : '请填写公司全称，须与营业执照一致'"
          />
        </view>

        <view v-if="form.titleType === 1" class="fi">
          <view class="lb"><text class="rq">*</text>纳税人识别号</view>
          <input v-model="form.taxNo" class="fd input" maxlength="25" placeholder="统一社会信用代码 / 税号" />
        </view>

        <view class="fi">
          <view class="lb"><text class="rq">*</text>开票金额（元）</view>
          <input v-model="form.amount" class="fd input" type="digit" placeholder="请输入开票金额" />
        </view>

        <view class="fi last">
          <view class="lb">开票内容</view>
          <input v-model="form.content" class="fd input" maxlength="40" placeholder="如 技术服务费" />
        </view>
      </view>

      <!-- 专票附加信息 -->
      <view v-if="isSpecial" class="form-card">
        <view class="card-hd">专用发票附加信息</view>
        <view class="fi">
          <view class="lb"><text class="rq">*</text>开户银行</view>
          <input v-model="form.bank" class="fd input" maxlength="50" placeholder="如 中国工商银行贵阳分行" />
        </view>
        <view class="fi">
          <view class="lb"><text class="rq">*</text>银行账号</view>
          <input v-model="form.bankAccount" class="fd input" type="number" maxlength="30" placeholder="对公账户账号" />
        </view>
        <view class="fi">
          <view class="lb"><text class="rq">*</text>注册地址</view>
          <input v-model="form.addr" class="fd input" maxlength="80" placeholder="营业执照上的注册地址" />
        </view>
        <view class="fi last">
          <view class="lb"><text class="rq">*</text>注册电话</view>
          <input v-model="form.phone" class="fd input" maxlength="20" placeholder="营业执照上的登记电话" />
        </view>
      </view>

      <!-- 接收信息 -->
      <view class="form-card">
        <view class="card-hd">{{ isElectronic ? '接收信息（电子发票）' : '收件信息（纸质发票邮寄）' }}</view>

        <view v-if="isElectronic" class="fi">
          <view class="lb"><text class="rq">*</text>接收邮箱</view>
          <input v-model="form.email" class="fd input" maxlength="60" placeholder="发票文件将发送至该邮箱" />
        </view>

        <template v-else>
          <view class="fi">
            <view class="lb"><text class="rq">*</text>收件人</view>
            <input v-model="form.receiver" class="fd input" maxlength="20" placeholder="收件人姓名" />
          </view>
          <view class="fi">
            <view class="lb"><text class="rq">*</text>收件电话</view>
            <input v-model="form.receiverPhone" class="fd input" type="number" maxlength="11" placeholder="便于快递联系" />
          </view>
          <view class="fi">
            <view class="lb"><text class="rq">*</text>收件地址</view>
            <textarea v-model="form.receiverAddr" class="fd textarea sm" maxlength="120" placeholder="省 / 市 / 区 + 详细地址" />
          </view>
          <view class="fi last">
            <view class="lb">备用邮箱</view>
            <input v-model="form.email" class="fd input" maxlength="60" placeholder="选填，可同时接收电子版" />
          </view>
        </template>
      </view>

      <!-- 备注 -->
      <view class="form-card">
        <view class="fi last col">
          <view class="lb">备注</view>
          <textarea v-model="form.remark" class="fd textarea" maxlength="200" placeholder="选填，如指定开票月份、备注合同号等" />
        </view>
      </view>

      <view class="tip-box">提交后服务顾问会核对信息并开票，进度与成品发票都能在「发票管理」里查看。</view>

      <view class="btn-primary" :class="{ disabled: submitting }" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交开票申请' }}
      </view>
      <view style="height: 60rpx"></view>
    </view>
  </view>
</template>

<script>
import { submitSvcInvoice, getSvcContracts } from '@/api/svc.js';
import { mapGetters } from 'vuex';

export default {
  data() {
    return {
      submitting: false,
      contracts: [],
      typeOptions: ['增值税普通发票', '增值税专用发票', '电子普通发票'],
      titleTypeOptions: [
        { name: '企业', value: 1 },
        { name: '个人', value: 2 }
      ],
      form: {
        contractId: null,
        type: null,
        titleType: 1,
        title: '',
        taxNo: '',
        bank: '',
        bankAccount: '',
        addr: '',
        phone: '',
        amount: '',
        content: '技术服务费',
        receiver: '',
        receiverPhone: '',
        receiverAddr: '',
        email: '',
        remark: ''
      }
    };
  },
  computed: {
    ...mapGetters(['userInfo']),
    typeText() {
      if (this.form.type === null || this.form.type === undefined) return '';
      return this.typeOptions[this.form.type - 1] || '';
    },
    isSpecial() {
      return this.form.type === 2;
    },
    isElectronic() {
      return this.form.type === 3;
    },
    contractLabels() {
      const labels = ['不关联合同'];
      this.contracts.forEach((c) => {
        labels.push((c.contractNo || '') + (c.title ? ' · ' + c.title : ''));
      });
      return labels;
    },
    contractLabel() {
      if (!this.form.contractId) return '';
      const c = this.contracts.find((x) => x.id === this.form.contractId);
      return c ? (c.contractNo || '') + (c.title ? ' · ' + c.title : '') : '';
    }
  },
  onLoad() {
    const u = this.userInfo || {};
    this.form.receiver = u.nickname || '';
    this.form.receiverPhone = u.phone || '';
    this.loadContracts();
  },
  methods: {
    loadContracts() {
      getSvcContracts({ page: 1, limit: 50 })
        .then((res) => {
          this.contracts = (res.data && res.data.list) || [];
        })
        .catch(() => {});
    },
    onTypeChange(e) {
      this.form.type = Number(e.detail.value) + 1;
    },
    onContractChange(e) {
      const idx = Number(e.detail.value);
      if (idx === 0) {
        this.form.contractId = null;
        return;
      }
      const c = this.contracts[idx - 1];
      if (!c) return;
      this.form.contractId = c.id;
      // 带出金额与抬头
      if (c.amount !== null && c.amount !== undefined && String(c.amount) !== '') {
        this.form.amount = String(c.amount);
      }
      if (c.partyName && !String(this.form.title || '').trim()) {
        this.form.title = c.partyName;
      }
      if (c.custCode && !String(this.form.taxNo || '').trim()) {
        this.form.taxNo = c.custCode;
      }
      if (c.partyType === 2) {
        this.form.titleType = 2;
      }
    },
    onSubmit() {
      if (this.submitting) return;
      const f = this.form;
      if (!f.type) return uni.showToast({ title: '请选择发票类型', icon: 'none' });
      if (!String(f.title || '').trim()) {
        return uni.showToast({ title: f.titleType === 2 ? '请填写个人姓名' : '请填写公司名称', icon: 'none' });
      }
      if (f.titleType === 1 && !String(f.taxNo || '').trim()) {
        return uni.showToast({ title: '请填写纳税人识别号', icon: 'none' });
      }
      const amt = Number(f.amount);
      if (!f.amount || isNaN(amt) || amt <= 0) {
        return uni.showToast({ title: '请填写正确的开票金额', icon: 'none' });
      }
      if (this.isSpecial) {
        if (!String(f.bank || '').trim()) return uni.showToast({ title: '请填写开户银行', icon: 'none' });
        if (!String(f.bankAccount || '').trim()) return uni.showToast({ title: '请填写银行账号', icon: 'none' });
        if (!String(f.addr || '').trim()) return uni.showToast({ title: '请填写注册地址', icon: 'none' });
        if (!String(f.phone || '').trim()) return uni.showToast({ title: '请填写注册电话', icon: 'none' });
      }
      if (this.isElectronic) {
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(f.email || '').trim())) {
          return uni.showToast({ title: '请填写正确的接收邮箱', icon: 'none' });
        }
      } else {
        if (!String(f.receiver || '').trim()) return uni.showToast({ title: '请填写收件人', icon: 'none' });
        if (!/^1[3-9]\d{9}$/.test(String(f.receiverPhone || '').trim())) {
          return uni.showToast({ title: '请填写正确的收件电话', icon: 'none' });
        }
        if (!String(f.receiverAddr || '').trim()) return uni.showToast({ title: '请填写收件地址', icon: 'none' });
        if (f.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(f.email).trim())) {
          return uni.showToast({ title: '备用邮箱格式不正确', icon: 'none' });
        }
      }

      const payload = Object.assign({}, f, { amount: amt });
      if (!payload.contractId) delete payload.contractId;
      this.submitting = true;
      submitSvcInvoice(payload)
        .then(() => {
          uni.showToast({ title: '提交成功', icon: 'success' });
          setTimeout(() => {
            uni.redirectTo({ url: '/pages/software/invoice/index', fail: () => uni.navigateBack() });
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
.svc-ivs { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.page-body { padding: 20rpx 30rpx 0; }

.form-card { background: #fff; border-radius: 24rpx; padding: 10rpx 30rpx; margin-bottom: 24rpx; }
.card-hd {
  font-size: 26rpx; font-weight: 700; color: #1d6bf3;
  padding: 24rpx 0 6rpx;
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
.input { width: 100%; height: 60rpx; line-height: 60rpx; font-size: 28rpx; color: #222; }
.textarea { width: 100%; min-height: 170rpx; font-size: 28rpx; color: #222; line-height: 1.7; }
.textarea.sm { min-height: 110rpx; }
.ar { color: #c0c8d4; font-size: 34rpx; }

.seg { display: flex; }
.seg-item {
  min-width: 140rpx; text-align: center; font-size: 26rpx; color: #5a6675;
  padding: 14rpx 30rpx; border: 1rpx solid #d6dde8; margin-right: 20rpx;
  border-radius: 14rpx; background: #fff;
  &.on { border-color: #1d6bf3; color: #1d6bf3; background: #eef4ff; font-weight: 600; }
}

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
