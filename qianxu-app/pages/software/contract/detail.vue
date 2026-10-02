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
        <view v-if="myRole" class="hc-role">
          <text class="hc-role-tag">{{ myRole === 'cust' ? '我是甲方（签约客户）' : '我是乙方（黔序）' }}</text>
        </view>
        <!-- 双方签署进度 -->
        <view class="hc-prog">
          <view class="hc-prog-item" :class="{ done: contract.custSigned }">
            <text class="hp-dot">{{ contract.custSigned ? '✓' : '1' }}</text>
            <text>甲方签署</text>
          </view>
          <view class="hc-prog-line"></view>
          <view class="hc-prog-item" :class="{ done: contract.ourSigned }">
            <text class="hp-dot">{{ contract.ourSigned ? '✓' : '2' }}</text>
            <text>乙方签署</text>
          </view>
          <view class="hc-prog-line"></view>
          <view class="hc-prog-item" :class="{ done: contract.finished }">
            <text class="hp-dot">{{ contract.finished ? '✓' : '3' }}</text>
            <text>正式合同</text>
          </view>
        </view>
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

      <!-- 已签署完成的正式合同：带双方公章与签字 -->
      <view v-if="contract.finished" class="section-card">
        <view class="done-box">
          <view class="done-head">
            <text class="done-title">正式合同已生成</text>
            <text class="done-badge">双方公章 · 签字 · 日期</text>
          </view>
          <view class="file-item" @click="openFile({ url: contract.signedFile, name: contract.signedFileName })">
            <text class="file-badge ext-pdf">PDF</text>
            <view class="file-main">
              <text class="file-name">{{ contract.signedFileName || '正式合同.pdf' }}</text>
              <text class="file-op">点击查看 / 下载，手机上可直接保存原件</text>
            </view>
            <text class="file-arrow">›</text>
          </view>
          <text v-if="contract.fileHash" class="hash-tip">存证摘要 SHA-256：{{ contract.fileHash }}</text>
        </view>
      </view>

      <!-- 双方签署信息 -->
      <view class="section-card">
        <view class="sec-head">
          <text class="sec-title">签署信息</text>
          <text class="sec-link">{{ contract.finished ? '双方已签署' : '签署进行中' }}</text>
        </view>

        <!-- 甲方（签约客户） -->
        <view class="party-box">
          <view class="party-head">
            <text class="party-role">甲方（签约客户）</text>
            <text class="party-badge" :class="contract.custSigned ? 'is-ok' : ''">{{ contract.custSigned ? '已签署' : '待签署' }}</text>
          </view>
          <view class="info-row"><text class="info-label">签约主体</text><text>{{ contract.partyName || '—' }}</text></view>
          <view v-if="contract.custSigned" class="info-row"><text class="info-label">签署人</text><text>{{ contract.signName || '—' }}</text></view>
          <view v-if="contract.custSigned" class="info-row"><text class="info-label">签署时间</text><text>{{ fmtTime(contract.signTime, true) }}</text></view>
          <view v-if="contract.custSigned" class="imgs-row">
            <view class="img-col">
              <text class="img-label">手写签字</text>
              <image :src="absUrl(contract.signHand)" class="sign-img" mode="aspectFit" @click="previewImg(contract.signHand)" />
            </view>
            <view v-if="contract.custSeal" class="img-col">
              <text class="img-label">圆形公章（系统生成）</text>
              <image :src="absUrl(contract.custSeal)" class="seal-img" mode="aspectFit" @click="previewImg(contract.custSeal)" />
            </view>
          </view>
        </view>

        <!-- 乙方（黔序） -->
        <view class="party-box">
          <view class="party-head">
            <text class="party-role">乙方（黔序）</text>
            <text class="party-badge" :class="contract.ourSigned ? 'is-ok' : ''">{{ contract.ourSigned ? '已签署' : '待签署' }}</text>
          </view>
          <view class="info-row"><text class="info-label">签约方</text><text>{{ contract.ourCompany || '—' }}</text></view>
          <view v-if="contract.ourSigned" class="info-row"><text class="info-label">签署人</text><text>{{ contract.ourSigner || '—' }}</text></view>
          <view v-if="contract.ourSigned" class="info-row"><text class="info-label">签署时间</text><text>{{ fmtTime(contract.ourSignedAt, true) }}</text></view>
          <view v-if="contract.ourSigned" class="imgs-row">
            <view class="img-col">
              <text class="img-label">手写签字</text>
              <image :src="absUrl(contract.ourSign)" class="sign-img" mode="aspectFit" @click="previewImg(contract.ourSign)" />
            </view>
            <view v-if="contract.ourSeal" class="img-col">
              <text class="img-label">圆形公章（系统生成）</text>
              <image :src="absUrl(contract.ourSeal)" class="seal-img" mode="aspectFit" @click="previewImg(contract.ourSeal)" />
            </view>
          </view>
        </view>

        <!-- 我已签、对方未签：给本人单方签署版 -->
        <view v-if="myStampedFile && !contract.finished" class="mine-box">
          <view class="file-item" @click="openFile({ url: myStampedFile, name: myStampedName })">
            <text class="file-badge ext-pdf">PDF</text>
            <view class="file-main">
              <text class="file-name">{{ myStampedName || '我的签署版.pdf' }}</text>
              <text class="file-op">我已签署的版本（待对方签署后出正式合同）</text>
            </view>
            <text class="file-arrow">›</text>
          </view>
        </view>

        <!-- 旧数据：独立签署页（仅当历史合同还带签署页时才展示） -->
        <view v-if="!contract.finished && contract.signSheet" class="sheet-box">
          <view class="sheet-head">
            <text class="sheet-name">签署页</text>
            <text class="sheet-badge">签章 · 签字 · 日期</text>
          </view>
          <image :src="absUrl(contract.signSheet)" class="sheet-img" mode="widthFix" @click="previewImg(contract.signSheet)" />
          <text class="sheet-op">点击查看大图，长按可保存</text>
        </view>
      </view>

      <!-- 签约操作：按角色各签一次 -->
      <view class="section-card sign-card" v-if="canSign">
        <view class="role-banner">{{ roleBanner }}</view>
        <view class="sign-guide">{{ signGuide }}</view>
        <view class="f-row">
          <text class="f-label">签署人姓名</text>
          <input v-model="form.signName" class="f-input" :placeholder="myRole === 'cust' ? '请输入负责人真实姓名' : '请输入签署人姓名'" placeholder-class="ph" />
        </view>
        <view class="f-row">
          <text class="f-label">手机号</text>
          <input v-model="form.phone" class="f-input" type="number" maxlength="11" placeholder="请输入手机号" placeholder-class="ph" />
        </view>
        <view v-if="signSms" class="f-row">
          <text class="f-label">验证码</text>
          <input v-model="form.code" class="f-input" type="number" maxlength="6" placeholder="请输入短信验证码" placeholder-class="ph" />
          <view class="code-btn" :class="{ disabled: counting }" @click="onSendCode">
            {{ counting ? countdown + 's' : '获取验证码' }}
          </view>
        </view>

        <!-- 旧流程（历史合同没有用印位置）：企业主体现场生成圆形公章 + 独立签署页 -->
        <view v-if="legacyFlow && needSeal" class="seal-pick">
          <view class="seal-pick-head">
            <text class="sp-title">{{ myRole === 'cust' ? '甲方公章' : '乙方公章' }}</text>
            <text class="sp-tip">用主体名称自动生成，签署时盖到约定位置</text>
          </view>
          <svc-seal ref="sealPad" v-model="sealStyle" :company="sealCompany" />
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
        <view class="agree-tip">{{ agreeTip }}</view>
        <view class="sign-btn" :class="{ disabled: submitting }" @click="onSubmit">
          {{ submitting ? '正在生成签署文件...' : '确认签署' }}
        </view>
      </view>
      <view class="section-card sign-card" v-else-if="contract.status === 0">
        <view class="wait-tip">
          <template v-if="myRole === 'cust' && contract.custSigned">您已签署，等待乙方（黔序）签署后自动生成正式合同</template>
          <template v-else-if="myRole === 'our' && contract.ourSigned">乙方已签署，等待甲方签署后自动生成正式合同</template>
          <template v-else-if="!myRole">本合同与当前登录账号无关</template>
          <template v-else>请等待签署</template>
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
      /** 手写签名笔迹包（旧流程签署页按同一笔触矢量重绘，避免跨域贴图） */
      signPack: null,
      /** 旧流程签章样式（仅保留圆形） */
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
    },
    /** 当前登录者在合同中的角色（后端按 uid / our_uid 判定） */
    myRole() {
      return (this.contract && this.contract.myRole) || '';
    },
    /** 我是否已签过 */
    selfSigned() {
      const c = this.contract || {};
      return this.myRole === 'cust' ? !!c.custSigned : !!c.ourSigned;
    },
    /** 是否可签：待签约状态 + 我是甲/乙方之一 + 我还没签 */
    canSign() {
      const c = this.contract;
      return !!c && c.status === 0 && !!this.myRole && !this.selfSigned;
    },
    /** 旧流程：后台没有设置用印位置的历史合同 */
    legacyFlow() {
      const c = this.contract || {};
      return !String(c.signLayout || '').trim();
    },
    /** 旧流程是否需要现场生成公章（新流程公章一律由服务端生成，客户端不管） */
    needSeal() {
      const c = this.contract || {};
      if (this.myRole === 'our') return !String(c.ourSeal || '').trim();
      return c.partyType === 1 && !String(c.custSeal || '').trim();
    },
    sealCompany() {
      const c = this.contract || {};
      return this.myRole === 'our' ? (c.ourCompany || '') : (c.partyName || '');
    },
    roleBanner() {
      return this.myRole === 'cust'
        ? '您是本合同的甲方（签约客户），签署时系统将按签约方名称自动生成圆形公章'
        : '您是本合同的乙方（黔序），签署时系统将按公司名称自动生成圆形公章';
    },
    signGuide() {
      const other = this.myRole === 'cust'
        ? (this.contract && this.contract.ourSigned ? '乙方已签署' : '乙方尚未签署')
        : (this.contract && this.contract.custSigned ? '甲方已签署' : '甲方尚未签署');
      return `签约流程：核对合同文件 → 填写签署人信息 → 短信验证（如开启）→ 横屏手写签名 → 提交。当前${other}；双方都签完后系统自动合成正式合同。`;
    },
    agreeTip() {
      return this.myRole === 'cust'
        ? '提交即表示您已阅读并认可合同内容。系统会将您的签字与按主体名称生成的圆形公章合成到合同正文；双方都签署后自动生成正式合同。'
        : '提交即代表乙方确认签署。系统会将签署人签字与按公司名称生成的圆形公章合成到合同正文；双方都签署后自动生成正式合同。';
    },
    /** 我已签后的单方签署版（对方未签时可先下载查看） */
    myStampedFile() {
      const c = this.contract || {};
      return this.myRole === 'cust' ? (c.custStampedFile || '') : (c.ourStampedFile || '');
    },
    myStampedName() {
      const c = this.contract || {};
      return this.myRole === 'cust' ? (c.custStampedName || '') : (c.ourStampedName || '');
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
        uni.showToast({ title: '请先填写签署人姓名', icon: 'none' });
        return;
      }
      this.$refs.signPad.open();
    },
    onSignDone(tempPath, pack) {
      // 留一份笔迹，旧流程签署页按同一笔触矢量重绘
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
        uni.showToast({ title: '请输入签署人姓名', icon: 'none' });
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
      const roleText = this.myRole === 'cust' ? '甲方' : '乙方';
      uni.showModal({
        title: '确认签署',
        content: this.legacyFlow && this.needSeal
          ? `确认以「${roleText} · ${who}」名义签署本合同？系统将自动生成电子公章，并把公章与签字合成到合同上，签署后不可撤销。`
          : `确认以「${roleText} · ${who}」名义签署本合同？系统将生成圆形公章与您的签字一并盖入合同，签署后不可撤销。`,
        success: (res) => {
          if (res.confirm) this.doSign();
        }
      });
    },

    /**
     * 签署流程（按角色自动判定甲/乙方，公章一律由服务端按签约方名称生成）
     *   新流程（后台已设置用印位置）：只上传手写签名，服务端把公章 / 签字 / 日期按坐标合成；
     *     双方齐备时服务端自动合成正式合同。
     *   旧流程（历史合同没有用印位置）：沿用「生成独立签署页」的老做法，保证老数据不受影响。
     */
    doSign() {
      const isLegacy = this.legacyFlow;
      this.submitting = true;
      uni.showLoading({ title: isLegacy ? '正在生成签署文件...' : '正在提交签署...', mask: true });

      Promise.resolve()
        .then(() => {
          if (!isLegacy || !this.needSeal) return '';
          const seal = this.$refs.sealPad;
          if (!seal || typeof seal.toTempFile !== 'function') {
            throw new Error('签章组件未就绪，请退出后重新进入合同页');
          }
          return seal.toTempFile();
        })
        .then((sealPath) => {
          if (!isLegacy) {
            // 新流程不再生成独立签署页
            return { sealPath: '', sheetPath: '' };
          }
          const sheet = this.$refs.sheetPad;
          if (!sheet || typeof sheet.generate !== 'function') {
            throw new Error('签署页组件未就绪，请退出后重新进入合同页');
          }
          return sheet
            .generate({
              title: this.contract.title,
              contractNo: this.contract.contractNo,
              company: this.sealCompany,
              sealStyle: this.sealStyle,
              needSeal: this.needSeal,
              signerName: this.form.signName.trim(),
              signPack: this.signPack,
              dateText: this.cnDate(new Date())
            })
            .then((sheetPath) => ({ sealPath, sheetPath }));
        })
        .then((res) => {
          const sealTask = res.sealPath ? this.uploadSealFile(res.sealPath) : Promise.resolve('');
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
        sealImage: sealUrl || '',
        signSheet: sheetUrl || ''
      })
        .then(() => {
          this.submitting = false;
          uni.hideLoading();
          uni.showToast({ title: '签署成功', icon: 'success' });
          this.signHand = '';
          this.signPack = null;
          this.form.code = '';
          this.loadDetail(this.contract.id);
        })
        .catch((e) => {
          this.submitting = false;
          uni.hideLoading();
          uni.showToast({
            title: typeof e === 'string' ? e : (e && e.message) || '签署失败',
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
.hc-role { margin-top: 16rpx; }
.hc-role-tag {
  font-size: 22rpx; color: #fff; background: rgba(255, 214, 102, 0.9); color: #5a4300;
  border-radius: 16rpx; padding: 4rpx 18rpx; font-weight: 600;
}
.hc-prog { display: flex; align-items: center; margin-top: 24rpx; }
.hc-prog-item {
  display: flex; flex-direction: column; align-items: center; gap: 8rpx;
  font-size: 20rpx; opacity: 0.72;
}
.hc-prog-item.done { opacity: 1; font-weight: 600; }
.hp-dot {
  width: 40rpx; height: 40rpx; border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex; align-items: center; justify-content: center; font-size: 20rpx;
}
.hc-prog-item.done .hp-dot { background: #36d399; color: #04361d; }
.hc-prog-line { flex: 1; height: 2rpx; background: rgba(255, 255, 255, 0.3); margin: 0 14rpx 26rpx; }
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

/* 双方签署信息 */
.party-box {
  margin-top: 20rpx; padding: 22rpx; border-radius: 16rpx; background: #f7faff;
  border: 1rpx solid #e6eefb;
}
.party-head { display: flex; align-items: center; margin-bottom: 12rpx; }
.party-role { font-size: 26rpx; font-weight: 700; color: #222; }
.party-badge {
  margin-left: 14rpx; font-size: 20rpx; color: #b8781f;
  background: #fff7ec; border-radius: 16rpx; padding: 2rpx 14rpx;
}
.party-badge.is-ok { color: #1f8a4c; background: #eefaf1; }
.imgs-row { display: flex; gap: 26rpx; margin-top: 14rpx; }
.img-col { display: flex; flex-direction: column; }
.img-label { font-size: 20rpx; color: #9aa4b2; margin-bottom: 8rpx; }
.sign-img { width: 300rpx; height: 120rpx; }
.seal-img { width: 130rpx; height: 130rpx; }

/* 我已签的单方签署版 */
.mine-box { margin-top: 20rpx; }
.mine-box .file-item { margin-bottom: 0; }

/* 已签署成品合同 */
.done-box {
  padding: 22rpx; border-radius: 16rpx;
  background: #f4fbf5; border: 1rpx solid #cfe9d8;
}
.done-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
.done-title { font-size: 27rpx; font-weight: 700; color: #1f8a4c; }
.done-badge {
  font-size: 20rpx; color: #1f8a4c; background: #e3f5e8;
  border-radius: 16rpx; padding: 4rpx 16rpx;
}
.hash-tip { display: block; margin-top: 12rpx; font-size: 19rpx; color: #9aa4b2; word-break: break-all; line-height: 1.6; }

.info-row { display: flex; margin-top: 14rpx; font-size: 26rpx; color: #333; }
.info-row:first-child { margin-top: 0; }
.info-label { width: 170rpx; color: #9aa4b2; flex: none; }

/* 签约表单 */
.sign-card { padding-bottom: 34rpx; }
.role-banner {
  font-size: 24rpx; font-weight: 600; color: #1d6bf3;
  background: #eef4ff; border-radius: 12rpx; padding: 16rpx 20rpx; line-height: 1.6;
}
.sign-guide { font-size: 22rpx; color: #7c8798; background: #f5f8ff; border-radius: 12rpx; padding: 16rpx 20rpx; margin-top: 16rpx; }
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
.agree-tip { font-size: 22rpx; color: #9aa4b2; margin-top: 24rpx; line-height: 1.7; }
.sign-btn {
  margin-top: 22rpx; height: 88rpx; border-radius: 44rpx;
  background: linear-gradient(135deg, #0d4fc4, #1d6bf3);
  color: #fff; font-size: 30rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.sign-btn.disabled { background: #c3cad6; }
.wait-tip { text-align: center; font-size: 26rpx; color: #7c8798; padding: 20rpx 0; line-height: 1.8; }

/* 电子签章（旧流程生成图） */
.seal-img { width: 130rpx; height: 130rpx; }

/* 独立签署页（旧流程） */
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

/* 旧流程签章生成 */
.seal-pick { margin-top: 30rpx; padding-top: 24rpx; border-top: 1rpx solid #eef1f6; }
.seal-pick-head { display: flex; align-items: baseline; justify-content: space-between; }
.sp-title { font-size: 27rpx; font-weight: 600; color: #222; }
.sp-tip { font-size: 21rpx; color: #9aa4b2; }
</style>
