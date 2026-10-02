<template>
  <view class="ct-detail" v-if="contract">
    <view class="status-pad"></view>
    <view class="page-body">
      <!-- 合同头 -->
      <view class="head-card">
        <view class="hc-title">{{ contract.title }}</view>
        <view class="hc-no">编号 {{ contract.contractNo }}</view>
        <view class="hc-row">
          <view class="hc-amount">¥{{ contract.amount }}</view>
          <view class="hc-status" :class="'st-' + contract.status">{{ statusText(contract.status) }}</view>
        </view>
        <view class="hc-party">
          <text class="hc-party-tag">{{ contract.partyType === 1 ? '企业' : '个人' }}</text>
          <text>{{ contract.partyName || '—' }}</text>
        </view>
        <view v-if="contract.signTime" class="hc-sign-time">签约时间：{{ fmtTime(contract.signTime, true) }}</view>
        <view v-if="contract.status === 2 && contract.rejectReason" class="hc-reject">驳回原因：{{ contract.rejectReason }}</view>
      </view>

      <!-- 合同文件 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">合同文件</text>
          <text v-if="fileList.length" class="sec-link">共 {{ fileList.length }} 份</text>
        </view>
        <view v-if="fileList.length" class="file-list">
          <view v-for="(f, i) in fileList" :key="i" class="file-item" @click="openFile(f)">
            <text class="file-badge" :class="'ext-' + f.ext">{{ (f.ext || 'FILE').toUpperCase() }}</text>
            <view class="file-main">
              <text class="file-name">{{ f.name }}</text>
              <text class="file-op">点击查看 / 下载</text>
            </view>
            <text class="file-arrow">›</text>
          </view>
        </view>
        <view v-else class="empty-tip">合同文件待上传</view>
        <view v-if="contract.content" class="ct-content">{{ contract.content }}</view>
      </view>

      <!-- 签署区（独立于合同文件） -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">签署信息</text>
          <text class="sec-link">{{ contract.signedFile ? '双方已签署' : '待签署' }}</text>
        </view>

        <!-- 乙方（黔序）签约方信息 -->
        <view v-if="contract.ourCompany || contract.ourLegal" class="party-box">
          <view class="party-head">
            <text class="party-role">乙方（黔序）</text>
            <text v-if="contract.ourStampedFile" class="party-badge">已用印</text>
          </view>
          <view class="info-row"><text class="info-label">签约方</text><text>{{ contract.ourCompany || '—' }}</text></view>
          <view v-if="contract.ourCode" class="info-row"><text class="info-label">信用代码</text><text>{{ contract.ourCode }}</text></view>
          <view v-if="contract.ourLegal" class="info-row"><text class="info-label">法定代表人</text><text>{{ contract.ourLegal }}</text></view>
          <view v-if="contract.ourSignedAt" class="info-row"><text class="info-label">用印时间</text><text>{{ fmtTime(contract.ourSignedAt, true) }}</text></view>
        </view>

        <!-- 已签署完成的成品合同：带双方公章与签字 -->
        <view v-if="contract.signedFile" class="done-box">
          <view class="done-head">
            <text class="done-title">已签署合同</text>
            <text class="done-badge">公章 · 签字 · 日期</text>
          </view>
          <view class="file-item" @click="openFile({ url: contract.signedFile, name: contract.signedFileName })">
            <text class="file-badge ext-pdf">PDF</text>
            <view class="file-main">
              <text class="file-name">{{ contract.signedFileName || '已签署合同.pdf' }}</text>
              <text class="file-op">点击查看 / 下载，手机上可直接保存原件</text>
            </view>
            <text class="file-arrow">›</text>
          </view>
          <text v-if="contract.fileHash" class="hash-tip">存证摘要 SHA-256：{{ contract.fileHash }}</text>
        </view>

        <!-- 尚未签署时，以缩略形式告知双方用印状态 -->
        <template v-else>
          <view class="sign-block">
            <text class="block-label">乙方（黔序）公章与法人签字</text>
            <view v-if="contract.ourStampedFile" class="slot-done">已用印，见合同正文</view>
            <view v-else class="slot-empty">待乙方用印</view>
          </view>
          <view class="sign-block">
            <text class="block-label">甲方（本人 / 本单位）签字</text>
            <template v-if="contract.status === 1 && contract.signHand">
              <image :src="absUrl(contract.signHand)" class="sign-img" mode="aspectFit" @click="previewImg(contract.signHand)" />
              <text class="sign-date">{{ fmtDate(contract.signTime) }}</text>
            </template>
            <view v-else class="slot-empty">待您手写签名</view>
          </view>
          <view v-if="contract.partyType === 1" class="sign-block">
            <text class="block-label">甲方（企业）公章</text>
            <image
              v-if="contract.custSeal || contract.sealImage"
              :src="absUrl(contract.custSeal || contract.sealImage)"
              class="seal-img"
              mode="aspectFit"
              @click="previewImg(contract.custSeal || contract.sealImage)"
            />
            <view v-else class="slot-empty">待加盖公章</view>
          </view>
        </template>

        <!-- 旧数据：独立签署页（仅当历史合同还带签署页时才展示） -->
        <view v-if="!contract.signedFile && contract.signSheet" class="sheet-box">
          <view class="sheet-head">
            <text class="sheet-name">签署页</text>
            <text class="sheet-badge">签章 · 签字 · 日期</text>
          </view>
          <image :src="absUrl(contract.signSheet)" class="sheet-img" mode="widthFix" @click="previewImg(contract.signSheet)" />
          <text class="sheet-op">点击查看大图，长按可保存</text>
        </view>

        <view v-if="contract.status === 1" class="info-box">
          <view class="info-row"><text class="info-label">签约负责人</text><text>{{ contract.signName || '—' }}</text></view>
          <view class="info-row"><text class="info-label">签约手机号</text><text>{{ contract.signPhone || '—' }}</text></view>
          <view class="info-row"><text class="info-label">签约时间</text><text>{{ fmtTime(contract.signTime, true) }}</text></view>
        </view>
      </view>

      <!-- 签约操作 -->
      <view class="section-card sign-card" v-if="contract.status === 0">
        <view class="sign-guide">签约流程：核对合同文件 → 填写负责人信息 → 短信验证（如开启）→ 横屏手写签名 → 提交签约。提交后系统会把您的签字按约定位置合成到合同正文上。</view>
        <view v-if="contract.ourStampedFile" class="our-stamp-tip">本合同正文已加盖乙方（黔序）公章与法定代表人签字，请核对后签署</view>
        <view v-else class="our-stamp-tip is-warn">本合同尚未完成乙方用印，如有疑问请联系客服</view>
        <view class="f-row">
          <text class="f-label">负责人姓名</text>
          <input v-model="form.signName" class="f-input" placeholder="请输入负责人真实姓名" placeholder-class="ph" />
        </view>
        <view class="f-row">
          <text class="f-label">手机号</text>
          <input v-model="form.phone" class="f-input" type="number" maxlength="11" placeholder="请输入负责人手机号" placeholder-class="ph" />
        </view>
        <view v-if="signSms" class="f-row">
          <text class="f-label">验证码</text>
          <input v-model="form.code" class="f-input" type="number" maxlength="6" placeholder="请输入短信验证码" placeholder-class="ph" />
          <view class="code-btn" :class="{ disabled: counting }" @click="onSendCode">
            {{ counting ? countdown + 's' : '获取验证码' }}
          </view>
        </view>

        <!-- 企业客户且后台未预设公章：现场用主体名称自动生成圆形公章 -->
        <view v-if="contract.partyType === 1 && !contract.custSeal" class="seal-pick">
          <view class="seal-pick-head">
            <text class="sp-title">客户公章</text>
            <text class="sp-tip">用主体名称自动生成，签署时盖到约定位置</text>
          </view>
          <svc-seal ref="sealPad" v-model="sealStyle" :company="contract.partyName" />
        </view>
        <view v-else-if="contract.partyType === 1 && contract.custSeal" class="seal-preset">
          <view class="seal-pick-head">
            <text class="sp-title">客户公章</text>
            <text class="sp-tip">由平台预先设置，签署时自动加盖</text>
          </view>
          <image :src="absUrl(contract.custSeal)" class="seal-img" mode="aspectFit" @click="previewImg(contract.custSeal)" />
        </view>

        <view class="sign-area" @click="onOpenSign">
          <template v-if="signHand">
            <image :src="absUrl(signHand)" class="sign-preview" mode="aspectFit" />
            <text class="sign-redo">点击重新签名</text>
          </template>
          <template v-else>
            <text class="sign-plus">✎ 横屏手写签名</text>
            <text class="sign-tip-inline">点击后进入全屏横向手写板书写姓名</text>
          </template>
        </view>
        <view class="agree-tip">提交即表示您已阅读并认可合同内容。系统会将您的签字与公章按约定位置合成到合同正文，生成带双方公章与签字的成品合同，可随时查看与下载。</view>
        <view class="sign-btn" :class="{ disabled: submitting }" @click="onSubmit">
          {{ submitting ? '成品合同生成中...' : '确认签约' }}
        </view>
      </view>
      <view class="section-card sign-card" v-else>
        <view class="sign-btn disabled">{{ statusText(contract.status) }}</view>
      </view>

      <view style="height: 60rpx"></view>
    </view>

    <svc-sign ref="signPad" @confirm="onSignDone" />
    <svc-sign-sheet ref="sheetPad" />
  </view>
</template>

<script>
import { getSvcContractInfo, signSvcContract, sendSvcSignCode, getSvcConfig } from '@/api/svc.js';
import svcSign from '@/components/svcSign/index.vue';
import svcSeal from '@/components/svcSeal/index.vue';
import svcSignSheet from '@/components/svcSignSheet/index.vue';

export default {
  components: { svcSign, svcSeal, svcSignSheet },
  data() {
    return {
      contract: null,
      signSms: true,
      config: {},
      signHand: '',
      /** 手写签名笔迹包（签署页按同一笔触矢量重绘，避免跨域贴图） */
      signPack: null,
      /** 签章样式：round=圆形标准章 oval=椭圆公章 square=方形合同章 modern=现代电子章 */
      sealStyle: 'round',
      submitting: false,
      form: { signName: '', phone: '', code: '' },
      counting: false,
      countdown: 60,
      timer: null
    };
  },
  computed: {
    /** 合同文件列表：annex（URL 逗号分隔）与 annexName（文件名逗号分隔）按顺序配对 */
    fileList() {
      const c = this.contract || {};
      const urls = String(c.annex || '').split(',').map((x) => x.trim()).filter(Boolean);
      const names = String(c.annexName || '').split(',').map((x) => x.trim());
      return urls.map((url, i) => {
        const name = names[i] || this.nameFromUrl(url);
        return { url, name, ext: this.extOf(name) };
      });
    }
  },
  onLoad(options) {
    if (options && options.id) {
      this.loadDetail(options.id);
    }
    getSvcConfig()
      .then((res) => {
        this.config = res.data || {};
        this.signSms = this.config.signSms !== false;
      })
      .catch(() => {});
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    loadDetail(id) {
      getSvcContractInfo(id)
        .then((res) => {
          this.contract = res.data || {};
        })
        .catch((e) => {
          uni.showToast({ title: typeof e === 'string' ? e : '加载失败', icon: 'none' });
        });
    },
    statusText(s) {
      return { 0: '待签约', 1: '已签约', 2: '已驳回' }[s] || '—';
    },
    fmtTime(t, full) {
      if (!t) return '';
      const s = String(t).replace('T', ' ');
      return full ? s.slice(0, 19) : s.slice(0, 16);
    },
    fmtDate(t) {
      return t ? String(t).replace('T', ' ').slice(0, 10) : '';
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
    extOf(name) {
      const s = String(name || '');
      const i = s.lastIndexOf('.');
      return i >= 0 ? s.substring(i + 1).toLowerCase() : '';
    },
    absUrl(u) {
      const s = String(u || '');
      // #ifdef H5
      if (s && s.indexOf('http') !== 0 && typeof window !== 'undefined' && window.location) {
        return window.location.origin + (s.charAt(0) === '/' ? s : '/' + s);
      }
      // #endif
      return s;
    },
    /** 查看 / 下载合同文件：H5 交给浏览器（pdf 直接预览，doc 走下载），App/小程序走下载+打开 */
    openFile(f) {
      const url = this.absUrl(f && f.url);
      if (!url) return;
      // #ifdef H5
      window.open(url);
      return;
      // #endif
      // #ifndef H5
      uni.showLoading({ title: '打开中...' });
      uni.downloadFile({
        url: url,
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode !== 200) {
            uni.showToast({ title: '文件下载失败', icon: 'none' });
            return;
          }
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            fail: () => {
              uni.showToast({ title: '该格式暂不支持预览，请下载后打开', icon: 'none' });
            }
          });
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '文件下载失败', icon: 'none' });
        }
      });
      // #endif
    },
    previewImg(u) {
      const url = this.absUrl(u);
      if (url) uni.previewImage({ urls: [url] });
    },
    onSendCode() {
      if (this.counting) return;
      const phone = (this.form.phone || '').trim();
      if (!/^1[3-9]\d{9}$/.test(phone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' });
        return;
      }
      sendSvcSignCode(phone)
        .then(() => {
          uni.showToast({ title: '验证码已发送', icon: 'none' });
          this.counting = true;
          this.countdown = 60;
          this.timer = setInterval(() => {
            this.countdown -= 1;
            if (this.countdown <= 0) {
              clearInterval(this.timer);
              this.counting = false;
            }
          }, 1000);
        })
        .catch((e) => {
          uni.showToast({ title: typeof e === 'string' ? e : '发送失败', icon: 'none' });
        });
    },
    onOpenSign() {
      if (!this.form.signName || !this.form.signName.trim()) {
        uni.showToast({ title: '请先填写负责人姓名', icon: 'none' });
        return;
      }
      this.$refs.signPad.open();
    },
    onSignDone(tempPath, pack) {
      // 留一份笔迹，签署页按同一笔触矢量重绘
      this.signPack = pack || null;
      uni.showLoading({ title: '上传签名...' });
      this.$util.uploadImgs(
        tempPath,
        { url: 'api/front/upload/image', name: 'multipart', model: 'user', pid: 7 },
        (data) => {
          uni.hideLoading();
          const url = data.data && (data.data.url || data.data.fileUrl);
          if (url) {
            this.signHand = url;
            uni.showToast({ title: '签名已生成', icon: 'success' });
          } else {
            uni.showToast({ title: '签名上传失败', icon: 'none' });
          }
        },
        () => {
          uni.hideLoading();
          uni.showToast({ title: '签名上传失败', icon: 'none' });
        }
      );
    },
    onSubmit() {
      if (this.submitting) return;
      const f = this.form;
      if (!f.signName || !f.signName.trim()) {
        uni.showToast({ title: '请输入负责人姓名', icon: 'none' });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test((f.phone || '').trim())) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' });
        return;
      }
      if (this.signSms && (!f.code || !f.code.trim())) {
        uni.showToast({ title: '请输入短信验证码', icon: 'none' });
        return;
      }
      if (!this.signHand || !this.signPack) {
        uni.showToast({ title: '请手写签名', icon: 'none' });
        return;
      }
      const who = f.signName.trim();
      const isCompany = this.contract.partyType === 1;
      const presetSeal = !!(this.contract.custSeal && String(this.contract.custSeal).trim());
      uni.showModal({
        title: '确认签约',
        content: isCompany && !presetSeal
          ? '确认以「' + who + '」名义签署本合同？系统将自动生成电子公章，并把公章与您的签字按约定位置合成到合同上，签署后不可撤销。'
          : '确认以「' + who + '」名义签署本合同？签署后不可撤销。',
        success: (res) => {
          if (res.confirm) this.doSign();
        }
      });
    },

    /**
     * 签署流程
     *   新流程（后台已设置用印位置）：只上传手写签名（企业且后台未预设公章时再生成公章图），
     *                                由服务端把公章 / 签字 / 日期按坐标合成进合同正文，产出成品 PDF。
     *   旧流程（历史合同没有用印位置）：沿用「生成独立签署页」的老做法，保证老数据不受影响。
     */
    doSign() {
      const isCompany = this.contract.partyType === 1;
      const newFlow = !!(this.contract.signLayout && String(this.contract.signLayout).trim());
      const presetSeal = !!(this.contract.custSeal && String(this.contract.custSeal).trim());
      this.submitting = true;
      uni.showLoading({ title: newFlow ? '正在生成成品合同...' : '正在生成签署文件...', mask: true });

      Promise.resolve()
        .then(() => {
          // 组件就绪校验：缺 ref / 方法不存在时给可读提示，别把原生 TypeError 弹给客户
          if (!isCompany || presetSeal) return '';
          const seal = this.$refs.sealPad;
          if (!seal || typeof seal.toTempFile !== 'function') {
            throw new Error('签章组件未就绪，请退出后重新进入合同页');
          }
          return seal.toTempFile();
        })
        .then((sealPath) => {
          if (newFlow) {
            // 新流程不再生成独立签署页
            return { sealPath, sheetPath: '' };
          }
          const sheet = this.$refs.sheetPad;
          if (!sheet || typeof sheet.generate !== 'function') {
            throw new Error('签署页组件未就绪，请退出后重新进入合同页');
          }
          return sheet
            .generate({
              title: this.contract.title,
              contractNo: this.contract.contractNo,
              company: this.contract.partyName,
              sealStyle: this.sealStyle,
              needSeal: isCompany,
              signerName: this.form.signName.trim(),
              signPack: this.signPack,
              dateText: this.cnDate(new Date())
            })
            .then((sheetPath) => ({ sealPath, sheetPath }));
        })
        .then((res) => {
          const needSealUpload = isCompany && !presetSeal;
          const sealTask = needSealUpload ? this.uploadSealFile(res.sealPath) : Promise.resolve('');
          return sealTask.then((sealUrl) => {
            if (!res.sheetPath) return { sealUrl, sheetUrl: '' };
            return this.uploadSealFile(res.sheetPath).then((sheetUrl) => ({ sealUrl, sheetUrl }));
          });
        })
        .then(({ sealUrl, sheetUrl }) => this.submitSign(sealUrl, sheetUrl))
        .catch((e) => {
          this.submitting = false;
          uni.hideLoading();
          uni.showToast({
            title: (e && e.message) || (typeof e === 'string' ? e : '签署文件生成失败，请重试'),
            icon: 'none'
          });
        });
    },

    /** 上传签章 / 签署页本地图片，返回服务器 URL */
    uploadSealFile(path) {
      return new Promise((resolve, reject) => {
        if (!path) {
          reject(new Error('签署文件生成失败，请重试'));
          return;
        }
        uni.showLoading({ title: '上传签署文件...', mask: true });
        this.$util.uploadImgs(
          path,
          { url: 'api/front/upload/image', name: 'multipart', model: 'user', pid: 7 },
          (data) => {
            const url = data.data && (data.data.url || data.data.fileUrl);
            if (url) resolve(url);
            else reject(new Error('签署文件上传失败，请重试'));
          },
          () => reject(new Error('签署文件上传失败，请重试'))
        );
      });
    },

    submitSign(sealUrl, sheetUrl) {
      uni.showLoading({ title: '提交中...', mask: true });
      signSvcContract({
        id: this.contract.id,
        signName: this.form.signName.trim(),
        phone: this.form.phone.trim(),
        code: this.form.code ? this.form.code.trim() : '',
        signImage: this.signHand,
        sealStyle: this.sealStyle,
        sealImage: sealUrl,
        signSheet: sheetUrl
      })
        .then(() => {
          this.submitting = false;
          uni.hideLoading();
          uni.showToast({ title: '签约成功', icon: 'success' });
          this.signHand = '';
          this.signPack = null;
          this.form.code = '';
          this.loadDetail(this.contract.id);
        })
        .catch((e) => {
          this.submitting = false;
          uni.hideLoading();
          uni.showToast({
            title: typeof e === 'string' ? e : (e && e.message) || '签约失败',
            icon: 'none'
          });
        });
    },

    /** 中文日期：2026年10月02日 */
    cnDate(d) {
      const t = d || new Date();
      const p = (n) => (n < 10 ? '0' + n : '' + n);
      return t.getFullYear() + '年' + p(t.getMonth() + 1) + '月' + p(t.getDate()) + '日';
    }
  }
};
</script>

<style scoped lang="scss">
.ct-detail { min-height: 100vh; background: #f4f6fb; }
.status-pad { height: var(--status-bar-height, 0px); }
.page-body { padding: 30rpx; }

.head-card {
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  border-radius: 24rpx; padding: 34rpx 30rpx; color: #fff;
}
.hc-title { font-size: 36rpx; font-weight: 700; }
.hc-no { font-size: 22rpx; opacity: 0.75; margin-top: 12rpx; }
.hc-row { display: flex; align-items: center; justify-content: space-between; margin-top: 26rpx; }
.hc-amount { font-size: 44rpx; font-weight: 700; }
.hc-status {
  font-size: 20rpx; border-radius: 18rpx; padding: 6rpx 20rpx;
  background: rgba(255, 255, 255, 0.22); color: #fff;
}
.hc-party { display: flex; align-items: center; margin-top: 22rpx; font-size: 26rpx; }
.hc-party-tag {
  font-size: 20rpx; background: rgba(255, 255, 255, 0.22);
  border-radius: 16rpx; padding: 2rpx 14rpx; margin-right: 14rpx;
}
.hc-sign-time { font-size: 22rpx; opacity: 0.8; margin-top: 16rpx; }
.hc-reject { font-size: 22rpx; margin-top: 12rpx; background: rgba(255, 255, 255, 0.14); border-radius: 12rpx; padding: 12rpx 16rpx; }

.section-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx; margin-top: 24rpx;
}
.sec-head { display: flex; align-items: center; justify-content: space-between; }
.sec-title { font-size: 30rpx; font-weight: 700; color: #222; }
.sec-link { font-size: 22rpx; color: #9aa4b2; }

/* 合同文件 */
.file-list { margin-top: 20rpx; }
.file-item {
  display: flex; align-items: center; padding: 20rpx;
  border: 1rpx solid #eef1f6; border-radius: 14rpx;
  background: #fbfcfe; margin-bottom: 16rpx;
}
.file-badge {
  flex: none; width: 84rpx; height: 40rpx; line-height: 40rpx; text-align: center;
  border-radius: 8rpx; font-size: 20rpx; font-weight: 700;
  background: #ecf5ff; color: #1d6bf3;
}
.file-badge.ext-pdf { background: #fdecec; color: #d3232f; }
.file-badge.ext-doc, .file-badge.ext-docx { background: #ecf5ff; color: #1d6bf3; }
.file-badge.ext-xls, .file-badge.ext-xlsx { background: #eaf7e6; color: #34a853; }
.file-main { flex: 1; margin-left: 18rpx; overflow: hidden; }
.file-name {
  display: block; font-size: 27rpx; color: #222;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.file-op { display: block; font-size: 21rpx; color: #9aa4b2; margin-top: 6rpx; }
.file-arrow { flex: none; font-size: 34rpx; color: #c3cad6; margin-left: 10rpx; }

.ct-content {
  margin-top: 20rpx; font-size: 26rpx; color: #444; line-height: 1.8;
  white-space: pre-wrap; word-break: break-all;
}
.empty-tip { text-align: center; color: #9aa4b2; font-size: 24rpx; padding: 30rpx 0; }

/* 签署区 */
.sign-block {
  margin-top: 22rpx; padding: 22rpx;
  border: 2rpx dashed #cfd8e6; border-radius: 16rpx;
  background: #f7faff; display: flex; flex-direction: column;
}
.sign-block.pos-left { align-items: flex-start; }
.sign-block.pos-center { align-items: center; }
.sign-block.pos-right { align-items: flex-end; }
.block-label { font-size: 24rpx; color: #5a6675; font-weight: 600; }
.slot-empty {
  margin-top: 14rpx; font-size: 23rpx; color: #a8b3c4;
  border: 2rpx dashed #cfd8e6; border-radius: 12rpx; padding: 22rpx 26rpx;
}
.seal-stamp {
  margin-top: 14rpx;
  border: 3rpx solid #d3232f; border-radius: 50%;
  width: 150rpx; height: 150rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  transform: rotate(-12deg); opacity: 0.88;
}
.seal-name { font-size: 20rpx; color: #d3232f; font-weight: 700; text-align: center; line-height: 1.3; }
.seal-sub { font-size: 16rpx; color: #d3232f; margin-top: 4rpx; }
.sign-img { width: 380rpx; height: 150rpx; margin-top: 10rpx; }
.sign-date { font-size: 20rpx; color: #667; margin-top: 6rpx; }
.slot-done {
  margin-top: 14rpx; font-size: 23rpx; color: #1f8a4c;
  background: #eefaf1; border: 2rpx solid #bfe6cd; border-radius: 12rpx; padding: 20rpx 26rpx;
}

/* 乙方（黔序）签约方信息 */
.party-box {
  margin-top: 20rpx; padding: 22rpx; border-radius: 16rpx; background: #f7faff;
  border: 1rpx solid #e6eefb;
}
.party-head { display: flex; align-items: center; margin-bottom: 12rpx; }
.party-role { font-size: 26rpx; font-weight: 700; color: #222; }
.party-badge {
  margin-left: 14rpx; font-size: 20rpx; color: #1f8a4c;
  background: #eefaf1; border-radius: 16rpx; padding: 2rpx 14rpx;
}

/* 已签署成品合同 */
.done-box {
  margin-top: 22rpx; padding: 22rpx; border-radius: 16rpx;
  background: #f4fbf5; border: 1rpx solid #cfe9d8;
}
.done-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
.done-title { font-size: 27rpx; font-weight: 700; color: #1f8a4c; }
.done-badge {
  font-size: 20rpx; color: #1f8a4c; background: #e3f5e8;
  border-radius: 16rpx; padding: 4rpx 16rpx;
}
.hash-tip { display: block; margin-top: 12rpx; font-size: 19rpx; color: #9aa4b2; word-break: break-all; line-height: 1.6; }

.info-box {
  margin-top: 24rpx; padding-top: 20rpx; border-top: 1rpx solid #eef1f6;
}
.info-row { display: flex; margin-top: 14rpx; font-size: 26rpx; color: #333; }
.info-row:first-child { margin-top: 0; }
.info-label { width: 170rpx; color: #9aa4b2; }

/* 签约表单 */
.sign-guide { font-size: 22rpx; color: #7c8798; background: #f5f8ff; border-radius: 12rpx; padding: 16rpx 20rpx; }
.our-stamp-tip {
  margin-top: 18rpx; font-size: 22rpx; line-height: 1.7; color: #1f8a4c;
  background: #eefaf1; border-radius: 12rpx; padding: 16rpx 20rpx;
}
.our-stamp-tip.is-warn { color: #b8781f; background: #fff7ec; }
.f-row { display: flex; align-items: center; margin-top: 26rpx; }
.f-label { width: 170rpx; font-size: 26rpx; color: #333; }
.f-input { flex: 1; font-size: 28rpx; border-bottom: 1rpx solid #e6ebf3; padding: 10rpx 4rpx; }
.ph { color: #c3cad6; }
.code-btn {
  margin-left: 18rpx; font-size: 24rpx; color: #1d6bf3;
  border: 1rpx solid #1d6bf3; border-radius: 30rpx; padding: 10rpx 22rpx;
}
.code-btn.disabled { color: #a8b3c4; border-color: #dfe5ee; }
.sign-area {
  margin-top: 28rpx; border: 2rpx dashed #cfd8e6; border-radius: 16rpx;
  min-height: 180rpx; display: flex; flex-direction: column;
  align-items: center; justify-content: center; background: #fbfcfe;
}
.sign-plus { font-size: 32rpx; color: #1d6bf3; font-weight: 600; }
.sign-tip-inline { font-size: 22rpx; color: #9aa4b2; margin-top: 10rpx; }
.sign-preview { width: 100%; height: 180rpx; }
.sign-redo { font-size: 22rpx; color: #9aa4b2; margin-top: 8rpx; }
.agree-tip { font-size: 22rpx; color: #9aa4b2; margin-top: 24rpx; }
.sign-btn {
  margin-top: 22rpx; height: 88rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 30rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.sign-btn.disabled { background: #c3cad6; }

/* 电子签章（生成图） */
.seal-img { width: 150rpx; height: 150rpx; margin-top: 12rpx; }

/* 独立签署页 */
.sheet-box {
  margin-top: 24rpx; padding: 20rpx; border: 1rpx solid #eef1f6;
  border-radius: 16rpx; background: #fbfcfe;
}
.sheet-head { display: flex; align-items: center; justify-content: space-between; }
.sheet-name { font-size: 26rpx; font-weight: 700; color: #222; }
.sheet-badge {
  font-size: 20rpx; color: #1d6bf3; background: #ecf5ff;
  border-radius: 16rpx; padding: 4rpx 16rpx;
}
.sheet-img {
  width: 100%; margin-top: 18rpx; border-radius: 10rpx;
  background: #fff; border: 1rpx solid #eef1f6;
}
.sheet-op { display: block; text-align: center; font-size: 21rpx; color: #9aa4b2; margin-top: 12rpx; }
.sheet-empty {
  margin-top: 24rpx; text-align: center; font-size: 22rpx; color: #a8b3c4;
  background: #f7faff; border-radius: 12rpx; padding: 22rpx 0;
}

/* 签约卡片内的签章选择 */
.seal-pick {
  margin-top: 30rpx; padding-top: 24rpx; border-top: 1rpx solid #eef1f6;
}
.seal-preset { margin-top: 30rpx; padding-top: 24rpx; border-top: 1rpx solid #eef1f6; }
.seal-pick-head { display: flex; align-items: baseline; justify-content: space-between; }
.sp-title { font-size: 27rpx; font-weight: 600; color: #222; }
.sp-tip { font-size: 21rpx; color: #9aa4b2; }
</style>
