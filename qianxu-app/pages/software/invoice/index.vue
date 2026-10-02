<template>
  <view class="svc-iv">
    <view class="status-pad"></view>
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">‹</text>
      <text class="nav-title">发票管理</text>
    </view>

    <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
      <view class="tab-row">
        <view
          v-for="(t, i) in tabs"
          :key="i"
          class="tab-item"
          :class="{ active: status === t.value }"
          @click="onTab(t.value)"
        >{{ t.name }}</view>
      </view>
    </scroll-view>

    <view class="page-body">
      <view v-if="list.length" class="iv-list">
        <view v-for="(v, i) in list" :key="i" class="iv-card" @click="navDetail(v.id)">
          <view class="iv-head">
            <text class="iv-title">{{ v.title || '—' }}</text>
            <view class="iv-status" :class="'st-' + v.status">{{ v.statusText || '—' }}</view>
          </view>
          <view class="iv-no">{{ v.invoiceNo }}</view>

          <view class="kv-row">
            <text class="kv-k">发票类型</text>
            <text class="kv-v">{{ v.typeText || '—' }}</text>
          </view>
          <view class="kv-row">
            <text class="kv-k">开票金额</text>
            <text class="kv-v strong">¥{{ fmtMoney(v.amount) }}</text>
          </view>
          <view v-if="v.contractNo" class="kv-row">
            <text class="kv-k">关联合同</text>
            <text class="kv-v mono">{{ v.contractNo }}</text>
          </view>
          <view v-if="v.content" class="kv-row">
            <text class="kv-k">开票内容</text>
            <text class="kv-v">{{ v.content }}</text>
          </view>

          <!-- 已驳回 -->
          <view v-if="v.status === 3" class="iv-reject">
            驳回原因：{{ v.rejectReason || '未填写' }}
          </view>

          <!-- 已开票：成品发票 + 邮寄 -->
          <view v-if="v.status === 2 && v.fileUrl" class="iv-file" @click.stop="openFile(v.fileUrl)">
            <view class="if-ic">{{ extOf(v.fileName || v.fileUrl) || 'PDF' }}</view>
            <view class="if-body">
              <view class="if-name">{{ v.fileName || '成品发票' }}</view>
              <view class="if-sub">点击查看 / 下载</view>
            </view>
            <text class="if-go">下载</text>
          </view>
          <view v-if="v.status === 2 && v.expressNo" class="iv-express">
            已寄出：{{ v.expressCo || '快递' }} {{ v.expressNo }}<template v-if="v.sentDate"> · {{ v.sentDate }}</template>
          </view>

          <view class="iv-meta">
            <text>提交 {{ fmtTime(v.createTime) }}</text>
            <text v-if="v.issueTime">开票 {{ fmtTime(v.issueTime) }}</text>
          </view>

          <view class="iv-ops">
            <view class="op-btn line" @click.stop="navDetail(v.id)">查看详情</view>
            <view v-if="v.status === 0" class="op-btn line gray" @click.stop="onCancel(v)">撤销</view>
          </view>
        </view>
      </view>

      <view v-else class="empty-wrap">
        <view class="empty-tip">还没有开票申请</view>
        <view class="empty-sub">提交开票信息后，服务顾问会为你开具发票</view>
      </view>
      <view style="height: 160rpx"></view>
    </view>

    <view class="fab" @click="navSubmit">＋ 申请开票</view>
  </view>
</template>

<script>
import { getSvcInvoices, cancelSvcInvoice } from '@/api/svc.js';

export default {
  data() {
    return {
      status: undefined,
      list: [],
      tabs: [
        { name: '全部', value: undefined },
        { name: '待开票', value: 0 },
        { name: '开票中', value: 1 },
        { name: '已开票', value: 2 },
        { name: '已驳回', value: 3 }
      ]
    };
  },
  onShow() {
    this.loadList();
  },
  methods: {
    onTab(v) {
      this.status = v;
      this.loadList();
    },
    loadList() {
      const data = { page: 1, limit: 50 };
      if (this.status !== undefined) data.status = this.status;
      getSvcInvoices(data)
        .then((res) => {
          this.list = (res.data && res.data.list) || [];
        })
        .catch(() => {});
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
    navDetail(id) {
      uni.navigateTo({ url: '/pages/software/invoice/detail?id=' + id });
    },
    navSubmit() {
      uni.navigateTo({ url: '/pages/software/invoice/submit' });
    },
    onCancel(v) {
      uni.showModal({
        title: '撤销申请',
        content: `确认撤销「${v.invoiceNo}」这笔开票申请？撤销后不可恢复。`,
        success: (r) => {
          if (!r.confirm) return;
          cancelSvcInvoice(v.id)
            .then(() => {
              uni.showToast({ title: '已撤销', icon: 'success' });
              this.loadList();
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
.svc-iv { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.nav-bar { display: flex; align-items: center; position: relative; padding: 16rpx 30rpx; }
.nav-back { position: absolute; left: 24rpx; font-size: 52rpx; color: #222; line-height: 1; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 700; color: #222; }

.tab-scroll { background: #fff; white-space: nowrap; }
.tab-row { display: inline-flex; padding: 0 20rpx; }
.tab-item { font-size: 28rpx; color: #666; padding: 20rpx 26rpx; position: relative; }
.tab-item.active { color: #1d6bf3; font-weight: 700; }
.tab-item.active::after {
  content: ''; position: absolute; left: 24rpx; right: 24rpx; bottom: 0;
  height: 6rpx; border-radius: 4rpx; background: #1d6bf3;
}

.page-body { padding: 24rpx 30rpx 0; }

.iv-card { background: #fff; border-radius: 24rpx; padding: 30rpx; margin-bottom: 24rpx; }
.iv-head { display: flex; align-items: flex-start; justify-content: space-between; }
.iv-title { font-size: 30rpx; font-weight: 700; color: #222; flex: 1; margin-right: 16rpx; line-height: 1.4; }
.iv-status { font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx; color: #fff; flex-shrink: 0; }
.iv-status.st-0 { background: #f59e0b; }
.iv-status.st-1 { background: #1d6bf3; }
.iv-status.st-2 { background: #16a34a; }
.iv-status.st-3 { background: #9aa4b2; }
.iv-no { font-size: 22rpx; color: #9aa4b2; margin-top: 12rpx; }

.kv-row { display: flex; align-items: flex-start; margin-top: 14rpx; }
.kv-k { width: 140rpx; flex-shrink: 0; font-size: 24rpx; color: #9aa4b2; }
.kv-v { flex: 1; font-size: 26rpx; color: #333; word-break: break-all; }
.kv-v.strong { font-weight: 700; color: #1d6bf3; }
.mono { font-family: Consolas, Monaco, monospace; }

.iv-reject {
  margin-top: 16rpx; font-size: 22rpx; color: #c0392b;
  background: #fdf0ef; border-radius: 12rpx; padding: 14rpx 16rpx; line-height: 1.6;
}
.iv-file {
  display: flex; align-items: center; margin-top: 20rpx;
  background: #f7f9fc; border-radius: 16rpx; padding: 16rpx 18rpx;
}
.if-ic {
  width: 64rpx; height: 64rpx; border-radius: 14rpx; background: #eef3fd;
  color: #1d6bf3; font-size: 18rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.if-body { flex: 1; margin-left: 16rpx; min-width: 0; }
.if-name { font-size: 24rpx; color: #333; }
.if-sub { font-size: 20rpx; color: #9aa4b2; margin-top: 4rpx; }
.if-go { font-size: 24rpx; color: #1d6bf3; flex-shrink: 0; }

.iv-express {
  margin-top: 14rpx; font-size: 22rpx; color: #1f8a4c;
  background: #f0faf3; border-radius: 12rpx; padding: 14rpx 18rpx; line-height: 1.6;
}

.iv-meta {
  display: flex; justify-content: space-between;
  margin-top: 18rpx; font-size: 22rpx; color: #9aa4b2;
}

.iv-ops { display: flex; justify-content: flex-end; margin-top: 20rpx; }
.op-btn {
  font-size: 25rpx; padding: 12rpx 30rpx; border-radius: 32rpx; margin-left: 16rpx;
  &.line { border: 1rpx solid #d6dde8; color: #5a6675; background: #fff; }
  &.line.gray { color: #9aa4b2; }
}

.empty-wrap { padding: 160rpx 0 0; }
.empty-tip { text-align: center; color: #6b7785; font-size: 28rpx; }
.empty-sub { text-align: center; color: #9aa4b2; font-size: 24rpx; margin-top: 14rpx; line-height: 1.7; }

.fab {
  position: fixed; right: 40rpx; bottom: 80rpx;
  height: 88rpx; padding: 0 40rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 28rpx; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12rpx 28rpx rgba(29, 107, 243, 0.35);
  z-index: 20;
}
</style>
