<template>
  <view class="svc-dd">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">需求详情</text>
    </view>

    <view v-if="detail" class="page-body">
      <view class="head-card">
        <view class="hc-row">
          <text class="hc-name">{{ detail.title }}</text>
          <view class="hc-status">{{ detail.statusText || '—' }}</view>
        </view>
        <view class="hc-sub">
          <view>{{ detail.demandNo }}<template v-if="detail.type"> · {{ detail.type }}</template></view>
          <view>提交 {{ fmtTime(detail.createTime) }}</view>
        </view>
      </view>

      <!-- 需求信息 -->
      <view class="section-card">
        <view class="sec-title">需求信息</view>
        <view class="kv-list">
          <view class="kv-row">
            <text class="kv-k">预算区间</text>
            <text class="kv-v">{{ detail.budget || '待沟通' }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">期望完成</text>
            <text class="kv-v">{{ detail.hopeDate || '待沟通' }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">联系人</text>
            <text class="kv-v">{{ detail.contactName || '—' }} {{ detail.contactPhone || '' }}</text>
          </view>
          <view v-if="detail.company" class="kv-row">
            <text class="kv-k">公司</text>
            <text class="kv-v">{{ detail.company }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">跟进顾问</text>
            <text class="kv-v">{{ detail.follower || '待指派' }}</text>
          </view>
        </view>

        <view class="content-title">需求描述</view>
        <view class="content-box">{{ detail.content || '—' }}</view>

        <view v-if="attachList.length" class="attach-row">
          <image
            v-for="(u, i) in attachList"
            :key="i"
            :src="u"
            class="attach-img"
            mode="aspectFill"
            @click="preview(i)"
          />
        </view>
      </view>

      <!-- 报价 -->
      <view v-if="showQuote" class="section-card">
        <view class="sec-head">
          <text class="sec-title">报价与方案</text>
          <text v-if="detail.status === 2" class="sec-link">待您确认</text>
        </view>
        <view class="quote-amount">¥{{ fmtMoney(detail.amount) }}</view>
        <view v-if="detail.quoteDesc" class="quote-desc">{{ detail.quoteDesc }}</view>
        <view v-if="detail.quoteFile" class="quote-file" @click="openFile(detail.quoteFile)">
          <view class="qf-ic">FILE</view>
          <view class="qf-body">
            <view class="qf-name">{{ detail.quoteName || '方案与报价单' }}</view>
            <view class="qf-sub">点击查看</view>
          </view>
        </view>
        <view v-if="detail.status === 2" class="quote-btn" @click="onAccept">同意报价</view>
      </view>

      <!-- 关联合同 -->
      <view v-if="detail.contractNo" class="section-card">
        <view class="sec-head">
          <text class="sec-title">关联合同</text>
          <text class="sec-link" @click="navContract">查看 ›</text>
        </view>
        <view class="kv-list">
          <view class="kv-row">
            <text class="kv-k">合同编号</text>
            <text class="kv-v mono">{{ detail.contractNo }}</text>
          </view>
        </view>
      </view>

      <!-- 跟进记录 -->
      <view class="section-card">
        <view class="sec-title">处理进度</view>
        <view v-if="followList.length" class="tl-list">
          <view v-for="(r, i) in followList" :key="i" class="tl-item">
            <view class="tl-left">
              <view class="tl-dot" :class="{ last: i === 0 }"></view>
              <view v-if="i !== followList.length - 1" class="tl-line"></view>
            </view>
            <view class="tl-body">
              <view class="tl-head">
                <text class="tl-stage">{{ r.operator || '系统' }}</text>
                <text class="tl-time">{{ r.time }}</text>
              </view>
              <view class="tl-remark">{{ r.content }}</view>
            </view>
          </view>
        </view>
        <view v-else class="empty-tip">暂无处理记录</view>
      </view>

      <view style="height: 60rpx"></view>
    </view>

    <view v-else-if="!loading" class="empty-wrap">
      <view class="empty-tip">需求不存在或无权查看</view>
    </view>
  </view>
</template>

<script>
import { getSvcDemandInfo, acceptSvcDemandQuote } from '@/api/svc.js';

export default {
  data() {
    return { id: null, detail: null, loading: true };
  },
  computed: {
    attachList() {
      return (this.detail && this.detail.attachList) || [];
    },
    followList() {
      return (this.detail && this.detail.followList) || [];
    },
    showQuote() {
      const d = this.detail || {};
      return d.status === 2 || (d.status === 3 && Number(d.amount) > 0);
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
      getSvcDemandInfo(this.id)
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
    fullUrl(u) {
      const s = String(u || '');
      if (!s) return '';
      if (/^https?:\/\//.test(s)) return s;
      return s.indexOf('/') === 0 ? s : '/' + s;
    },
    preview(i) {
      uni.previewImage({ urls: this.attachList.map(this.fullUrl), current: i });
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
    onAccept() {
      uni.showModal({
        title: '同意报价',
        content: `报价 ¥${this.fmtMoney(this.detail.amount)}，同意后顾问会为您生成合同。`,
        success: (r) => {
          if (!r.confirm) return;
          acceptSvcDemandQuote(this.detail.id)
            .then(() => {
              uni.showToast({ title: '已同意，等待生成合同', icon: 'none' });
              this.loadDetail();
            })
            .catch((e) => uni.showToast({ title: typeof e === 'string' ? e : '操作失败', icon: 'none' }));
        }
      });
    },
    navContract() {
      if (!this.detail || !this.detail.contractId) {
        uni.showToast({ title: '合同尚未生成', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/software/contract/detail?id=' + this.detail.contractId, fail: () => {} });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/software/user/index', fail: () => {} }) });
    }
  }
};
</script>

<style scoped lang="scss">
.svc-dd { min-height: 100vh; background: #f4f6fb; }
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
.hc-sub { margin-top: 16rpx; font-size: 22rpx; opacity: 0.9; line-height: 1.7; }

.section-card { background: #fff; border-radius: 24rpx; padding: 30rpx; margin-top: 24rpx; }
.sec-head { display: flex; align-items: center; justify-content: space-between; }
.sec-title { font-size: 32rpx; font-weight: 700; color: #222; }
.sec-link { font-size: 24rpx; color: #1d6bf3; }

.kv-list { margin-top: 20rpx; }
.kv-row { display: flex; align-items: flex-start; padding: 10rpx 0; }
.kv-k { width: 150rpx; flex-shrink: 0; font-size: 26rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 26rpx; color: #333; word-break: break-all; }
.mono { font-family: Consolas, Monaco, monospace; }

.content-title { font-size: 26rpx; color: #6b7785; margin-top: 22rpx; }
.content-box {
  font-size: 26rpx; color: #333; line-height: 1.8; margin-top: 12rpx;
  background: #f7f9fc; border-radius: 16rpx; padding: 20rpx 22rpx;
  white-space: pre-wrap; word-break: break-all;
}
.attach-row { display: flex; flex-wrap: wrap; margin-top: 20rpx; }
.attach-img { width: 150rpx; height: 150rpx; border-radius: 16rpx; background: #eef1f6; margin: 0 16rpx 16rpx 0; }

.quote-amount { font-size: 46rpx; font-weight: 700; color: #1d6bf3; margin-top: 20rpx; }
.quote-desc { font-size: 25rpx; color: #666; line-height: 1.75; margin-top: 14rpx; white-space: pre-wrap; }
.quote-file {
  display: flex; align-items: center; margin-top: 18rpx;
  background: #f7f9fc; border-radius: 16rpx; padding: 16rpx 18rpx;
}
.qf-ic {
  width: 64rpx; height: 64rpx; border-radius: 14rpx; background: #eef3fd;
  color: #1d6bf3; font-size: 18rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.qf-body { flex: 1; margin-left: 16rpx; min-width: 0; }
.qf-name { font-size: 24rpx; color: #333; }
.qf-sub { font-size: 20rpx; color: #9aa4b2; margin-top: 4rpx; }
.quote-btn {
  margin-top: 24rpx; height: 88rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 30rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 10rpx 24rpx rgba(29, 107, 243, 0.28);
}

.tl-list { margin-top: 26rpx; }
.tl-item { display: flex; }
.tl-left { display: flex; flex-direction: column; align-items: center; width: 30rpx; }
.tl-dot { width: 18rpx; height: 18rpx; border-radius: 50%; background: #cbd5e1; margin-top: 8rpx; flex-shrink: 0; }
.tl-dot.last { background: #1d6bf3; box-shadow: 0 0 0 8rpx rgba(29, 107, 243, 0.15); }
.tl-line { flex: 1; width: 2rpx; background: #e6ebf3; margin: 6rpx 0; }
.tl-body { flex: 1; margin-left: 20rpx; padding-bottom: 34rpx; }
.tl-head { display: flex; align-items: center; justify-content: space-between; }
.tl-stage { font-size: 26rpx; font-weight: 600; color: #222; }
.tl-time { font-size: 22rpx; color: #9aa4b2; }
.tl-remark { font-size: 24rpx; color: #666; margin-top: 10rpx; line-height: 1.6; }

.empty-wrap { padding: 200rpx 0; }
.empty-tip { text-align: center; color: #9aa4b2; font-size: 26rpx; }
</style>
