<template>
  <el-dialog
    :close-on-click-modal="false"
    :visible.sync="modals"
    title="发送货"
    class="order_box"
    :before-close="handleClose"
    width="900px"
  >
    <el-form
      ref="formItem"
      v-loading="loading"
      :model="formItem"
      label-width="130px"
      @submit.native.prevent
      :rules="rules"
    >
      <el-form-item label="收货信息：">
        <div class="recipient-box">
          <div class="recipient-row">
            <span class="label">收货人：</span>
            <span>{{ recipient.realName || '-' }}</span>
          </div>
          <div class="recipient-row">
            <span class="label">收货电话：</span>
            <span>{{ recipient.userPhone || '-' }}</span>
          </div>
          <div class="recipient-row">
            <span class="label">收货地址：</span>
            <span>{{ recipient.userAddress || '-' }}</span>
          </div>
          <el-button type="primary" size="mini" plain class="copy-btn" @click="copyRecipient">一键复制</el-button>
        </div>
      </el-form-item>
      <el-form-item label="选择类型：">
        <el-radio-group
          v-model="formItem.deliveryType"
          :disabled="isEdit"
          @change="changeRadioType(formItem.deliveryType)"
          required
        >
          <el-radio label="express">发货</el-radio>
          <el-radio label="send">送货</el-radio>
          <el-radio label="fictitious">虚拟</el-radio>
        </el-radio-group>
      </el-form-item>
      <!--发货：仅手动填写快递单号-->
      <div v-if="formItem.deliveryType === 'express'">
        <el-form-item v-model="nowCompany" label="快递公司：" prop="company">
          <el-select
            style="width: 100%"
            value-key="code"
            v-model="formItem.company"
            filterable
            @change="onChangeExport"
          >
            <el-option v-for="(item, i) in express" :value="item" :key="item.code" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号：" prop="expressNumber">
          <el-input v-model="formItem.expressNumber" placeholder="请输入快递单号"></el-input>
        </el-form-item>
      </div>
      <!--送货-->
      <div v-if="formItem.deliveryType === 'send'">
        <el-form-item label="送货人姓名：" prop="deliveryName">
          <el-input v-model="formItem.deliveryName" placeholder="请输入送货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="送货人电话：" prop="deliveryTel">
          <el-input v-model="formItem.deliveryTel" placeholder="请输入送货人电话"></el-input>
        </el-form-item>
      </div>
    </el-form>
    <div slot="footer">
      <el-button @click="cancel('formItem')">取消</el-button>
      <el-button type="primary" @click="putSend('formItem')">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { orderSendApi, updateTrackingNumberApi } from '@/api/order';
import { Debounce } from '@/utils/validate';
const validatePhone = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请填写手机号'));
  } else if (!/^1[3456789]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确!'));
  } else {
    callback();
  }
};
export default {
  name: 'orderSend',
  props: {
    orderId: {
      type: String,
      default: '',
    },
    expressListNormal: {
      type: Array,
      default: [],
    },
    expressListElec: {
      type: Array,
      default: [],
    },
    orderDetail: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      formItem: {
        deliveryType: 'express',
        expressRecordType: '1',
        expressCode: '',
        company: '', //快递公司
        deliveryName: '',
        deliveryTel: '',
        expressName: '',
        expressNumber: '',
        orderNo: '',
      },
      modals: false,
      rules: {
        company: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
        expressNumber: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
        deliveryName: [{ required: true, message: '请输入送货人姓名', trigger: 'blur' }],
        deliveryTel: [{ required: true, validator: validatePhone, trigger: 'blur' }],
      },
      loading: false,
      express: [], //物流公司
      isEdit: false, //是否是编辑
      nowCompany: '',
      recipient: {
        realName: '',
        userPhone: '',
        userAddress: '',
      },
    };
  },
  watch: {
    orderDetail: {
      handler: function (val) {
        if (val) {
          this.loading = true;
          this.isEdit = true;
          this.getExpressDetail(val);
          this.setRecipient({
            realName: val.realName,
            userPhone: val.userPhone,
            userAddress: val.userAddress,
          });
        } else {
          this.isEdit = false;
          this.loading = false;
        }
      },
      immediate: false,
      deep: true,
    },
  },
  mounted() {
    this.express = this.expressListNormal;
  },
  methods: {
    setRecipient(info) {
      this.recipient = {
        realName: (info && info.realName) || '',
        userPhone: (info && info.userPhone) || '',
        userAddress: (info && info.userAddress) || '',
      };
    },
    copyRecipient() {
      const text = [
        this.recipient.realName || '',
        this.recipient.userPhone || '',
        this.recipient.userAddress || '',
      ]
        .filter(Boolean)
        .join(' ');
      if (!text) {
        this.$message.warning('暂无收货人信息可复制');
        return;
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('收货人信息已复制');
        }).catch(() => {
          this.fallbackCopy(text);
        });
      } else {
        this.fallbackCopy(text);
      }
    },
    fallbackCopy(text) {
      const input = document.createElement('textarea');
      input.value = text;
      input.style.position = 'fixed';
      input.style.left = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        this.$message.success('收货人信息已复制');
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
    //物流信息详情, 快递单号，快递公司，快递公司code
    getExpressDetail(val) {
      if (val.deliveryType === 'send') {
        this.formItem.deliveryTel = val.deliveryId;
        this.formItem.deliveryName = val.deliveryName;
      } else {
        this.formItem.expressName = val.deliveryName;
        this.formItem.expressNumber = val.deliveryId;
      }
      this.formItem.deliveryType = val.deliveryType;
      this.formItem.expressCode = val.deliveryCode;
      this.formItem.expressRecordType = '1';
      this.formItem.company = { code: val.deliveryCode, name: val.deliveryName };
      this.loading = false;
    },
    // 快递公司选择
    onChangeExport(val) {
      this.formItem.expressCode = val.code;
      this.formItem.expressName = val.name;
    },
    //选择类型
    changeRadioType() {
      if (this.formItem.deliveryType === 'fictitious') {
        this.formItem.expressId = '';
        this.formItem.expressCode = '';
      }
      if (this.formItem.deliveryType === 'express') {
        this.formItem.expressRecordType = '1';
        this.express = this.expressListNormal;
      }
    },
    // 提交
    putSend: Debounce(function (name) {
      this.formItem.orderNo = this.orderId;
      this.formItem.expressRecordType = '1';
      this.$refs[name].validate((valid) => {
        if (valid) {
          !this.isEdit
            ? orderSendApi(this.formItem).then(() => {
                this.$message.success('发送货成功');
                this.modals = false;
                this.$refs[name].resetFields();
                this.$emit('submitFail');
              })
            : updateTrackingNumberApi(this.formItem).then(() => {
                this.$message.success('修改快递单号成功');
                this.modals = false;
                this.$refs[name].resetFields();
                this.$emit('submitFail');
              });
        } else {
          this.$message.error('请填写信息');
        }
      });
    }),
    handleClose() {
      this.cancel('formItem');
    },
    cancel(name) {
      this.modals = false;
      this.$refs[name].resetFields();
      this.formItem.deliveryType = 'express';
      this.formItem.expressRecordType = '1';
    },
  },
};
</script>

<style scoped lang="scss">
.recipient-box {
  background: #f7f8fa;
  border-radius: 4px;
  padding: 12px 16px;
  line-height: 1.8;
  position: relative;
  .recipient-row .label {
    color: #909399;
    display: inline-block;
    min-width: 70px;
  }
  .copy-btn {
    position: absolute;
    right: 12px;
    top: 12px;
  }
}
</style>
