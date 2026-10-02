<template>
  <view class="svc-ivd">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">开票详情</text>
    </view>

    <view v-if="detail" class="page-body">
      <!-- 头卡 -->
      <view class="head-card">
        <view class="hc-row">
          <text class="hc-name">{{ detail.title || '—' }}</text>
          <view class="hc-status">{{ detail.statusText || '—' }}</view>
        </view>
        <view class="hc-money">¥{{ fmtMoney(detail.amount) }}</view>
        <view class="hc-sub">
          <view>申请单号 {{ detail.invoiceNo }}</view>
          <view v-if="detail.typeText">{{ detail.typeText }}</view>
        </view>
      </view>

      <!-- 驳回提示 -->
      <view v-if="detail.status === 3" class="reject-card">
        <view class="rj-title">该申请已被驳回</view>
        <view class="rj-body">{{ detail.rejectReason || '未填写驳回原因，请联系服务顾问' }}</view>
      </view>

      <!-- 开票信息 -->
      <view class="section-card">
        <view class="sec-title">开票信息</view>
        <view class="kv-list">
          <view class="kv-row">
            <text class="kv-k">发票类型</text>
            <text class="kv-v">{{ detail.typeText || '—' }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">抬头类型</text>
            <text class="kv-v">{{ detail.titleTypeText || '—' }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">发票抬头</text>
            <text class="kv-v">{{ detail.title || '—' }}</text>
          </view>
          <view v-if="detail.taxNo" class="kv-row">
            <text class="kv-k">纳税人识别号</text>
            <text class="kv-v mono">{{ detail.taxNo }}</text>
          </view>
          <view v-if="detail.bank" class="kv-row">
            <text class="kv-k">开户银行</text>
            <text class="kv-v">{{ detail.bank }}</text>
          </view>
          <view v-if="detail.bankAccount" class="kv-row">
            <text class="kv-k">银行账号</text>
            <text class="kv-v mono">{{ detail.bankAccount }}</text>
          </view>
          <view v-if="detail.addr" class="kv-row">
            <text class="kv-k">注册地址</text>
            <text class="kv-v">{{ detail.addr }}</text>
          </view>
          <view v-if="detail.phone" class="kv-row">
            <text class="kv-k">注册电话</text>
            <text class="kv-v mono">{{ detail.phone }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">开票金额</text>
            <text class="kv-v strong">¥{{ fmtMoney(detail.amount) }}</text>
          </view>
          <view v-if="detail.content" class="kv-row">
            <text class="kv-k">开票内容</text>
            <text class="kv-v">{{ detail.content }}</text>
          </view>
          <view v-if="detail.contractId" class="kv-row" @click="navContract(detail.contractId)">
            <text class="kv-k">关联合同</text>
            <text class="kv-v link mono">{{ detail.contractNo || '查看合同 ›' }}</text>
          </view>
        </view>
      </view>

      <!-- 成品发票 -->
      <view class="section-card">
        <view class="sec-title">成品发票</view>
        <view class="dl-list">
          <view class="dl-item">
            <view class="dl-ic">{{ extOf(detail.fileName || detail.fileUrl) || 'PDF' }}</view>
            <view class="dl-body">
              <view class="dl-name">{{ detail.fileName || '发票文件' }}</view>
              <view class="dl-sub">{{ detail.fileUrl ? '点击查看或下载' : '开票完成后可在此下载' }}</view>
            </view>
            <text v-if="detail.fileUrl" class="dl-go" @click="openFile(detail.fileUrl)">下载</text>
            <text v-else class="dl-none">未生成</text>
          </view>
        </view>
        <view v-if="detail.status === 2 && detail.expressNo" class="tip-line">
          发票已寄出：{{ detail.expressCo || '快递' }} {{ detail.expressNo }}<template v-if="detail.sentDate"> · {{ detail.sentDate }}</template>
        </view>
        <view v-else-if="detail.status === 2 && detail.email" class="tip-line">
          电子发票已发送至 {{ detail.email }}，请留意查收。
        </view>
      </view>

      <!-- 状态记录 -->
      <view class="section-card">
        <view class="sec-title">状态记录</view>
        <view v-if="logs.length" class="tl-list">
          <view v-for="(r, i) in logs" :key="i" class="tl-item">
            <view class="tl-left">
              <view class="tl-dot" :class="{ last: i === 0 }"></view>
              <view v-if="i !== logs.length - 1" class="tl-line"></view>
            </view>
            <view class="tl-body">
              <view class="tl-head">
                <text class="tl-stage">{{ r.statusName }}</text>
                <text class="tl-time">{{ r.time }}</text>
              </view>
              <view v-if="r.remark" class="tl-remark">{{ r.remark }}</view>
              <view v-if="r.operator" class="tl-op">{{ r.operator }}</view>
            </view>
          </view>
        </view>
        <view v-else class="empty-tip">暂无状态记录</view>
      </view>

      <!-- 收件信息 -->
      <view v-if="hasReceiver" class="section-card">
        <view class="sec-title">收件信息</view>
        <view class="kv-list">
          <view v-if="detail.receiver" class="kv-row">
            <text class="kv-k">收件人</text>
            <text class="kv-v">{{ detail.receiver }}</text>
          </view>
          <view v-if="detail.receiverPhone" class="kv-row">
            <text class="kv-k">收件电话</text>
            <text class="kv-v mono">{{ detail.receiverPhone }}</text>
          </view>
          <view v-if="detail.receiverAddr" class="kv-row">
            <text class="kv-k">收件地址</text>
            <text class="kv-v">{{ detail.receiverAddr }}</text>
          </view>
          <view v-if="detail.email" class="kv-row">
            <text class="kv-k">接收邮箱</text>
            <text class="kv-v mono">{{ detail.email }}</text>
          </view>
        </view>
      </view>

      <!-- 申请人与备注 -->
      <view class="section-card">
        <view class="sec-title">申请信息</view>
        <view class="kv-list">
          <view v-if="detail.nickname || detail.phoneNo" class="kv-row">
            <text class="kv-k">申请人</text>
            <text class="kv-v">{{ [detail.nickname, detail.phoneNo].filter(Boolean).join(' · ') }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">提交时间</text>
            <text class="kv-v mono">{{ fmtTime(detail.createTime) }}</text>
          </view>
          <view v-if="detail.issueTime" class="kv-row">
            <text class="kv-k">开票时间</text>
            <text class="kv-v mono">{{ fmtTime(detail.issueTime) }}</text>
          </view>
          <view v-if="detail.operator" class="kv-row">
            <text class="kv-k">经办人</text>
            <text class="kv-v">{{ detail.operator }}</text>
          </view>
          <view v-if="detail.remark" class="kv-row">
            <text class="kv-k">客户备注</text>
            <text class="kv-v">{{ detail.remark }}</text>
          </view>
        </view>
      </view>

      <view v-if="detail.status === 0" class="btn-cancel" @click="onCancel">撤销这笔申请</view>

      <view class="foot-tip">发票由服务顾问核对后开具，进度与成品件都会同步到本页。如有疑问请联系您的服务顾问。</view>
      <view style="height: 60rpx"></view>
    </view>

    <view v-else-if="!loading" class="empty-wrap">
      <view class="empty-tip">开票申请不存在或无权查看</view>
    </view>
  </view>
</template>

<script>
import { getSvcInvoiceInfo, cancelSvcInvoice } from '@/api/svc.js';

export default {
  data() {
    return { id: null, detail: null, loading: true };
  },
  computed: {
    logs() {
      return (this.detail && this.detail.logList) || [];
    },
    hasReceiver() {
      const d = this.detail || {};
      return !!(d.receiver || d.receiverPhone || d.receiverAddr || d.email);
    }
  },
  onLoad(options) {
    if (options && options.id) {
      this.id = options.id;
      this.loadDetail();
    } else {
      this.loading = false;
    }
  },
  methods: {
    loadDetail() {
      this.loading = true;
      getSvcInvoiceInfo(this.id)
        .then((res) => {
          this.detail = res.data || null;
        })
        .catch((e) => {
          uni.showToast({ title: typeof e === 'string' ? e : '加载失败', icon: 'none' });
          this.detail = null;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    fmtMoney(v) {
      const n = Number(v) || 0;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fmtTime(t) {
      return t ? String(t).replace('T', ' ').slice(0, 16) : '';
    },
    extOf(name) {
      const s = String(name || '');
      const i = s.lastIndexOf('.');
      return i >= 0 ? s.substring(i + 1).toUpperCase() : '';
    },
    fullUrl(u) {
      const s = String(u || '');
      if (!s) return '';
      if (/^https?:\/\//.test(s)) return s;
      return s.indexOf('/') === 0 ? s : '/' + s;
    },
    openFile(url) {
      const full = this.fullUrl(url);
      if (!full) return;
      // #ifdef H5
      window.open(full, '_blank');
      // #endif
      // #ifndef H5
      uni.showLoading({ title: '打开中...' });
      uni.downloadFile({
        url: full,
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode === 200) {
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              fail: () => uni.showToast({ title: '该文件暂不支持预览', icon: 'none' })
            });
          } else {
            uni.showToast({ title: '文件下载失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '文件下载失败', icon: 'none' });
        }
      });
      // #endif
    },
    navContract(id) {
      if (!id) return;
      uni.navigateTo({ url: '/pages/software/contract/detail?id=' + id, fail: () => {} });
    },
    onCancel() {
      uni.showModal({
        title: '撤销申请',
        content: `确认撤销「${(this.detail && this.detail.invoiceNo) || ''}」这笔开票申请？撤销后不可恢复。`,
        success: (r) => {
          if (!r.confirm) return;
          cancelSvcInvoice(this.id)
            .then(() => {
              uni.showToast({ title: '已撤销', icon: 'success' });
              this.loadDetail();
            })
            .catch((e) => uni.showToast({ title: typeof e === 'string' ? e : '撤销失败', icon: 'none' }));
        }
      });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-ivd { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.page-body { padding: 20rpx 30rpx 0; }

.head-card {
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  border-radius: 24rpx; padding: 34rpx 30rpx; color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(13, 79, 196, 0.18);
}
.hc-row { display: flex; align-items: flex-start; justify-content: space-between; }
.hc-name { font-size: 32rpx; font-weight: 700; flex: 1; margin-right: 16rpx; line-height: 1.4; }
.hc-status {
  font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx;
  background: rgba(255, 255, 255, 0.22); color: #fff; flex-shrink: 0;
}
.hc-money { font-size: 48rpx; font-weight: 700; margin-top: 20rpx; }
.hc-sub { margin-top: 14rpx; font-size: 22rpx; opacity: 0.9; line-height: 1.7; }

.reject-card {
  margin-top: 24rpx; background: #fdf0ef; border-radius: 24rpx; padding: 26rpx 30rpx;
}
.rj-title { font-size: 28rpx; font-weight: 700; color: #c0392b; }
.rj-body { font-size: 24rpx; color: #a5503f; margin-top: 10rpx; line-height: 1.7; }

.section-card { background: #fff; border-radius: 24rpx; padding: 30rpx; margin-top: 24rpx; }
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }

.kv-list { margin-top: 20rpx; }
.kv-row { display: flex; align-items: flex-start; padding: 12rpx 0; }
.kv-k { width: 176rpx; flex-shrink: 0; font-size: 26rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 26rpx; color: #333; word-break: break-all; }
.kv-v.strong { font-weight: 700; color: #1d6bf3; }
.kv-v.link { color: #1d6bf3; }
.mono { font-family: Consolas, Monaco, monospace; }
.tip-line {
  margin-top: 18rpx; font-size: 22rpx; color: #1f8a4c;
  background: #f0faf3; border-radius: 12rpx; padding: 16rpx 18rpx; line-height: 1.6;
}

.dl-list { margin-top: 20rpx; }
.dl-item { display: flex; align-items: center; padding: 4rpx 0; }
.dl-ic {
  width: 76rpx; height: 76rpx; border-radius: 16rpx; background: #eef3fd;
  color: #1d6bf3; font-size: 20rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.dl-body { flex: 1; margin-left: 20rpx; min-width: 0; }
.dl-name { font-size: 26rpx; color: #333; font-weight: 500; }
.dl-sub { font-size: 22rpx; color: #9aa4b2; margin-top: 6rpx; }
.dl-go { font-size: 26rpx; color: #1d6bf3; padding: 10rpx 6rpx; flex-shrink: 0; }
.dl-none { font-size: 24rpx; color: #c0c8d4; flex-shrink: 0; }

.tl-list { margin-top: 26rpx; }
.tl-item { display: flex; }
.tl-left { display: flex; flex-direction: column; align-items: center; width: 30rpx; }
.tl-dot { width: 18rpx; height: 18rpx; border-radius: 50%; background: #cbd5e1; margin-top: 8rpx; flex-shrink: 0; }
.tl-dot.last { background: #1d6bf3; box-shadow: 0 0 0 8rpx rgba(29, 107, 243, 0.15); }
.tl-line { flex: 1; width: 2rpx; background: #e6ebf3; margin: 6rpx 0; }
.tl-body { flex: 1; margin-left: 20rpx; padding-bottom: 34rpx; }
.tl-head { display: flex; align-items: center; justify-content: space-between; }
.tl-stage { font-size: 28rpx; font-weight: 600; color: #222; }
.tl-time { font-size: 22rpx; color: #9aa4b2; }
.tl-remark { font-size: 24rpx; color: #666; margin-top: 10rpx; line-height: 1.6; }
.tl-op { font-size: 22rpx; color: #9aa4b2; margin-top: 8rpx; }

.btn-cancel {
  margin-top: 30rpx; height: 88rpx; border-radius: 44rpx;
  border: 1rpx solid #d6dde8; background: #fff; color: #6b7785;
  font-size: 28rpx; display: flex; align-items: center; justify-content: center;
}

.foot-tip {
  font-size: 22rpx; color: #9aa4b2; line-height: 1.7;
  padding: 30rpx 10rpx 0; text-align: center;
}
.empty-wrap { padding: 200rpx 0; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; }
</style>
