<template>
  <view class="svc-cd">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">合同详情</text>
    </view>

    <view v-if="detail" class="page-body">
      <!-- 头卡 -->
      <view class="head-card">
        <view class="hc-row">
          <text class="hc-name">{{ detail.title }}</text>
          <view class="hc-status" :class="'st-' + detail.status">{{ detail.statusText || '—' }}</view>
        </view>
        <view class="hc-money">¥{{ fmtMoney(detail.amount) }}</view>
        <view class="hc-sub">
          <view>合同编号 {{ detail.contractNo }}</view>
          <view v-if="detail.serviceTerm">服务期限 {{ detail.serviceTerm }}</view>
        </view>
      </view>

      <!-- 签约信息 -->
      <view class="section-card">
        <view class="sec-title">签约信息</view>
        <view class="kv-list">
          <view class="kv-row">
            <text class="kv-k">签约渠道</text>
            <text class="kv-v">{{ channelText }}</text>
          </view>
          <template v-if="detail.signChannel === 'electronic'">
            <view v-if="detail.signPlatform" class="kv-row">
              <text class="kv-k">签约平台</text>
              <text class="kv-v">{{ detail.signPlatform }}</text>
            </view>
            <view v-if="detail.platformNo" class="kv-row">
              <text class="kv-k">存证编号</text>
              <text class="kv-v mono">{{ detail.platformNo }}</text>
            </view>
            <view v-if="detail.startDate" class="kv-row">
              <text class="kv-k">发起日期</text>
              <text class="kv-v mono">{{ detail.startDate }}</text>
            </view>
          </template>
          <template v-else>
            <view v-if="detail.signType" class="kv-row">
              <text class="kv-k">签署方式</text>
              <text class="kv-v">{{ detail.signType }}</text>
            </view>
            <view v-if="detail.expressCo || detail.expressNo" class="kv-row">
              <text class="kv-k">快递单号</text>
              <text class="kv-v mono">{{ [detail.expressCo, detail.expressNo].filter(Boolean).join(' ') || '—' }}</text>
            </view>
            <view v-if="detail.sentDate" class="kv-row">
              <text class="kv-k">寄出日期</text>
              <text class="kv-v mono">{{ detail.sentDate }}</text>
            </view>
            <view v-if="detail.backDate" class="kv-row">
              <text class="kv-k">回寄签收</text>
              <text class="kv-v mono">{{ detail.backDate }}</text>
            </view>
          </template>
          <view class="kv-row">
            <text class="kv-k">签署日期</text>
            <text class="kv-v mono">{{ signedDate }}</text>
          </view>
        </view>
        <view class="tip-line">{{ channelTip }}</view>
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

      <!-- 关联项目进度 -->
      <view v-if="detail.project" class="section-card" @click="navProject(detail.project.id)">
        <view class="sec-head">
          <text class="sec-title">关联项目进度</text>
          <text class="sec-link">详情 ›</text>
        </view>
        <view class="pj-head">
          <text class="pj-name">{{ detail.project.name }}</text>
          <view class="pj-status" :class="'st-' + detail.project.status">{{ detail.project.statusText }}</view>
        </view>
        <view class="pj-bar">
          <view class="pj-bar-inner" :style="{ width: (detail.project.percent || 0) + '%' }"></view>
        </view>
        <view class="pj-meta">
          <text>{{ detail.project.stage || '—' }} · 进度 {{ detail.project.percent || 0 }}%</text>
          <text v-if="detail.project.manager">项目经理 {{ detail.project.manager }}</text>
        </view>
      </view>

      <!-- 合同文件 -->
      <view class="section-card">
        <view class="sec-title">合同文件</view>
        <view class="dl-list">
          <view class="dl-item">
            <view class="dl-ic">PDF</view>
            <view class="dl-body">
              <view class="dl-name">{{ detail.signedFileName || '双方签署完成的成品件' }}</view>
              <view class="dl-sub">{{ detail.signedFile ? '点击查看或下载' : '待签署完成并归档后可下载' }}</view>
            </view>
            <text v-if="detail.signedFile" class="dl-go" @click="openFile(detail.signedFile)">下载</text>
            <text v-else class="dl-none">未生成</text>
          </view>

          <view v-if="detail.annex" class="dl-item">
            <view class="dl-ic">{{ extOf(detail.annexName || detail.annex) || 'FILE' }}</view>
            <view class="dl-body">
              <view class="dl-name">{{ detail.annexName || '合同正文' }}</view>
              <view class="dl-sub">合同正文（定稿版）</view>
            </view>
            <text class="dl-go" @click="openFile(detail.annex)">查看</text>
          </view>

          <view v-if="detail.signChannel === 'electronic' && detail.voucherFile" class="dl-item">
            <view class="dl-ic">{{ extOf(detail.voucherName || detail.voucherFile) || 'FILE' }}</view>
            <view class="dl-body">
              <view class="dl-name">{{ detail.voucherName || '存证报告' }}</view>
              <view class="dl-sub">平台存证 / 出证报告</view>
            </view>
            <text class="dl-go" @click="openFile(detail.voucherFile)">查看</text>
          </view>

          <view v-if="detail.platformUrl" class="dl-item">
            <view class="dl-ic">链接</view>
            <view class="dl-body">
              <view class="dl-name">平台查看链接</view>
              <view class="dl-sub">{{ detail.signPlatform || '第三方平台' }}</view>
            </view>
            <text class="dl-go" @click="openFile(detail.platformUrl)">打开</text>
          </view>
        </view>
      </view>

      <!-- 双方信息 -->
      <view class="section-card">
        <view class="sec-title">签约双方</view>
        <view class="party-box">
          <view class="party-label">甲方（客户）</view>
          <view class="kv-list">
            <view class="kv-row">
              <text class="kv-k">主体类型</text>
              <text class="kv-v">{{ detail.partyType === 1 ? '企业' : '个人' }}</text>
            </view>
            <view class="kv-row">
              <text class="kv-k">名称</text>
              <text class="kv-v">{{ detail.partyName || '—' }}</text>
            </view>
            <view v-if="detail.custCode" class="kv-row">
              <text class="kv-k">信用代码</text>
              <text class="kv-v mono">{{ detail.custCode }}</text>
            </view>
            <view v-if="detail.custPhone" class="kv-row">
              <text class="kv-k">联系电话</text>
              <text class="kv-v mono">{{ detail.custPhone }}</text>
            </view>
          </view>
        </view>
        <view class="party-box">
          <view class="party-label">乙方（我方）</view>
          <view class="kv-list">
            <view class="kv-row">
              <text class="kv-k">名称</text>
              <text class="kv-v">{{ detail.ourCompany || '—' }}</text>
            </view>
            <view v-if="detail.ourCode" class="kv-row">
              <text class="kv-k">信用代码</text>
              <text class="kv-v mono">{{ detail.ourCode }}</text>
            </view>
            <view v-if="detail.ourLegal" class="kv-row">
              <text class="kv-k">法定代表人</text>
              <text class="kv-v">{{ detail.ourLegal }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="foot-tip">合同在线下签署，本页信息由服务顾问登记与更新。如有疑问请联系您的服务顾问。</view>
      <view style="height: 60rpx"></view>
    </view>

    <view v-else-if="!loading" class="empty-wrap">
      <view class="empty-tip">合同不存在或无权查看</view>
    </view>
  </view>
</template>

<script>
import { getSvcContractInfo } from '@/api/svc.js';

export default {
  data() {
    return { id: null, detail: null, loading: true };
  },
  computed: {
    channelText() {
      const d = this.detail || {};
      const base = d.channelText || '纸质合同';
      if (d.signChannel === 'electronic' && d.signPlatform) return base + ' · ' + d.signPlatform;
      if (d.signChannel !== 'electronic' && d.signType) return base + ' · ' + d.signType;
      return base;
    },
    channelTip() {
      const d = this.detail || {};
      if (d.status === 1 && d.signChannel !== 'electronic') {
        return d.expressNo
          ? `合同已寄出（${d.expressCo || '快递'} ${d.expressNo}），签署完成后由顾问登记状态。`
          : '合同定稿待签署，顾问会把进度同步到这里。';
      }
      if (d.status === 1) return '电子合同已在平台发起，签署完成后由顾问登记状态。';
      if (d.status === 2) return '双方已签字盖章，原件在途或已回寄，归档后生成成品件。';
      if (d.status === 3) return '合同已生效并归档，可在下方下载成品件。';
      if (d.status === -1) return '该合同已终止（作废或中止），记录仅作留档。';
      return '合同刚登记，还没定稿。';
    },
    logs() {
      return (this.detail && this.detail.statusLogList) || [];
    },
    signedDate() {
      const d = this.detail || {};
      if (d.status === 3 || d.status === 2) {
        const l = (d.statusLogList || []).slice().reverse().find((x) => x.status === 2 || x.status === 3);
        if (l && l.time) return String(l.time).slice(0, 10);
        if (d.backDate) return d.backDate;
      }
      return '—';
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
      getSvcContractInfo(this.id)
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
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    },
    navProject(id) {
      if (!id) return;
      uni.navigateTo({ url: '/pages/software/project/detail?id=' + id, fail: () => {} });
    },
    fmtMoney(v) {
      const n = Number(v) || 0;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
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
    }
  }
};
</script>

<style scoped lang="scss">
.svc-cd { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar {
  display: flex; align-items: center; position: relative;
  padding: 16rpx 30rpx;
}
.nav-back {
  position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1;
}
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

.section-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-top: 24rpx;
}
.sec-head { display: flex; align-items: center; justify-content: space-between; }
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }
.sec-link { font-size: 24rpx; color: #9aa4b2; }

.kv-list { margin-top: 20rpx; }
.kv-row { display: flex; align-items: flex-start; padding: 12rpx 0; }
.kv-k { width: 150rpx; flex-shrink: 0; font-size: 26rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 26rpx; color: #333; word-break: break-all; }
.mono { font-family: Consolas, Monaco, monospace; }
.tip-line {
  margin-top: 16rpx; font-size: 22rpx; color: #a06b16;
  background: #fdf6ec; border-radius: 12rpx; padding: 16rpx 18rpx; line-height: 1.6;
}

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

.pj-head { display: flex; align-items: center; justify-content: space-between; margin-top: 20rpx; }
.pj-name { font-size: 30rpx; font-weight: 600; color: #222; flex: 1; margin-right: 16rpx; }
.pj-status { font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff; flex-shrink: 0; }
.pj-status.st-1 { background: #f59e0b; }
.pj-status.st-2 { background: #1d6bf3; }
.pj-status.st-3 { background: #f59e0b; }
.pj-status.st-4 { background: #16a34a; }
.pj-status.st--1 { background: #9aa4b2; }
.pj-bar { margin-top: 18rpx; height: 14rpx; border-radius: 8rpx; background: #edf1f7; overflow: hidden; }
.pj-bar-inner { height: 100%; border-radius: 8rpx; background: linear-gradient(90deg, #1d6bf3, #4b95ff); }
.pj-meta { display: flex; justify-content: space-between; margin-top: 14rpx; font-size: 22rpx; color: #9aa4b2; }

.dl-list { margin-top: 20rpx; }
.dl-item {
  display: flex; align-items: center; padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
}
.dl-item:last-child { border-bottom: none; padding-bottom: 6rpx; }
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

.party-box { margin-top: 22rpx; }
.party-label { font-size: 26rpx; font-weight: 600; color: #1d6bf3; }

.foot-tip {
  font-size: 22rpx; color: #9aa4b2; line-height: 1.7;
  padding: 30rpx 10rpx 0; text-align: center;
}
.empty-wrap { padding: 200rpx 0; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; }
</style>
