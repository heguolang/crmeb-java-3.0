<template>
  <div class="divBox relative">
    <el-card :bordered="false" shadow="never" class="ivu-mt" :body-style="{ padding: 0 }">
      <div class="padding-add">
        <el-form inline size="small" label-width="75px">
          <el-form-item label="订单号码：">
            <el-input
              v-model="tableFrom.orderNo"
              @blur="seachList"
              @clear="seachList"
              placeholder="请输入订单号"
              class="selWidth"
              size="small"
              clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item label="用户搜索：" label-for="nickname">
            <UserSearchInput v-model="tableFrom" @searchList="seachList" />
          </el-form-item>
          <el-form-item label="创建时间：">
            <optionDatePicker v-model="timeVal" @changeOptTime="onchangeTime"></optionDatePicker>
          </el-form-item>
          <el-form-item label="订单类型：">
            <el-select v-model="tableFrom.type" placeholder="状态" class="selWidth" @change="seachList">
              <el-option v-for="(item, i) in options" :key="i" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="物流单号：">
            <el-input
              v-model="tableFrom.deliveryId"
              @blur="seachList"
              @clear="seachList"
              placeholder="请输入物流单号"
              class="selWidth"
              size="small"
              clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="seachList">搜索</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <div slot="header" class="clearfix">
        <el-tabs
          v-model="tableFrom.status"
          @tab-click="seachList"
          v-if="checkPermi(['admin:order:status:num']) && isNumber(orderChartType.all)"
        >
          <el-tab-pane name="all" :label="`全部(${orderChartType.all ? orderChartType.all : 0})`" />
          <el-tab-pane name="unPaid" :label="`未支付(${orderChartType.unPaid ? orderChartType.unPaid : 0})`" />
          <el-tab-pane
            name="notShipped"
            :label="`未发货(${orderChartType.notShipped ? orderChartType.notShipped : 0})`"
          />
          <el-tab-pane name="spike" :label="`待收货(${orderChartType.spike ? orderChartType.spike : 0})`" />
          <el-tab-pane name="bargain" :label="`待评价(${orderChartType.bargain ? orderChartType.bargain : 0})`" />
          <el-tab-pane name="complete" :label="`交易完成(${orderChartType.complete ? orderChartType.complete : 0})`" />
          <el-tab-pane
            name="toBeWrittenOff"
            :label="`待核销(${orderChartType.toBeWrittenOff ? orderChartType.toBeWrittenOff : 0})`"
          />
          <el-tab-pane name="refunding" :label="`退款中(${orderChartType.refunding ? orderChartType.refunding : 0})`" />
          <el-tab-pane name="refunded" :label="`已退款(${orderChartType.refunded ? orderChartType.refunded : 0})`" />
          <el-tab-pane name="deleted" :label="`已删除(${orderChartType.deleted ? orderChartType.deleted : 0})`" />
        </el-tabs>
        <el-button @click="exports" v-hasPermi="['admin:export:excel:order']">导出</el-button>
      </div>
      <!-- 订单卡片列表（复用订货商-订货商订单页样式） -->
      <div v-loading="listLoading" class="order-list">
        <div v-for="row in tableData.data" :key="row.orderId" class="order-card">
          <!-- 卡片头：时间 + 单号 + 类型 | 状态 | 快捷链接 -->
          <div class="card-head">
            <span class="head-time">{{ shortTime(row.createTime) }}</span>
            <span class="head-label">订单号：</span>
            <span class="head-no">{{ row.orderId }}</span>
            <span class="mini-chip">{{ row.orderType || '普通商品' }}</span>
            <span v-if="row.isDel" class="head-reason">用户已删除</span>
            <el-popover v-if="row.refundStatus === 1 || row.refundStatus === 2" trigger="hover" placement="top" :open-delay="500">
              <b :class="statusClass(row.statusStr.key) + ' head-status'" slot="reference">{{ row.statusStr.value }}</b>
              <div class="pup_card flex-column">
                <span>退款原因：{{ row.refundReasonWap }}</span>
                <span>备注说明：{{ row.refundReasonWapExplain }}</span>
                <span>退款时间：{{ row.refundReasonTime }}</span>
                <span class="acea-row">
                  退款凭证：
                  <template v-if="row.refundReasonWapImg">
                    <div
                      v-for="(item, index) in row.refundReasonWapImg.split(',')"
                      :key="index"
                      class="demo-image__preview"
                      style="width: 35px; height: auto; display: inline-block"
                    >
                      <el-image :src="item" :preview-src-list="[item]" />
                    </div>
                  </template>
                  <span v-else style="display: inline-block">无</span>
                </span>
              </div>
            </el-popover>
            <span v-else :class="statusClass(row.statusStr.key) + ' head-status'">{{ row.statusStr.value }}</span>
            <div class="head-links">
              <a v-if="checkPermi(['admin:order:status:list'])" @click="onOrderLog(row.orderId)">订单记录</a>
              <a v-if="row.statusStr.key !== 'unPaid'" @click="onOrderPrint(row)">打印小票</a>
            </div>
          </div>
          <!-- 卡片体：商品 | 用户收货 | 金额 | 支付状态 | 操作 -->
          <div class="card-body">
            <div class="col col-goods">
              <div v-for="(val, i) in row.productList" :key="i" class="goods-item">
                <el-image :src="val.info.image" :preview-src-list="[val.info.image]" class="goods-img" fit="cover" />
                <div class="goods-info">
                  <div class="goods-name">{{ val.info.productName }}</div>
                  <div v-if="val.info.sku" class="goods-sku-row">
                    <span class="mini-chip">{{ val.info.sku }}</span>
                  </div>
                </div>
                <div class="goods-side">
                  <div class="goods-num">× {{ val.info.payNum }}</div>
                  <div class="goods-price">￥{{ val.info.price }}</div>
                </div>
              </div>
              <div v-if="!(row.productList || []).length" class="sub-text">—</div>
            </div>
            <div class="col col-recv">
              <div class="kv"><span class="k">用户：</span><span class="v">{{ row.realName || '-' }}</span></div>
              <div class="kv"><span class="k">昵称：</span><span class="v">{{ row.nickname || '-' }}</span></div>
              <div class="kv"><span class="k" style="flex-shrink:0">手机：</span><span class="v">{{ row.userPhone || '-' }}</span></div>
              <div class="kv">
                <span class="k">地址：</span>
                <el-tooltip v-if="row.userAddress" effect="dark" :content="row.userAddress" placement="top">
                  <span class="v">{{ row.userAddress }}</span>
                </el-tooltip>
                <span v-else class="v">-</span>
              </div>
            </div>
            <div class="col col-amount">
              <div class="kv"><span class="k">总价：</span><span class="v">￥{{ fmtMoney(row.proTotalPrice || row.payPrice) }}</span></div>
              <div class="kv"><span class="k">实付：</span><span class="v">￥{{ fmtMoney(row.payPrice) }}</span></div>
            </div>
            <div class="col col-state">
              <div class="pay-text">{{ row.paid ? '已支付' : '未支付' }}</div>
              <div class="sub-text">{{ row.payTypeStr || '—' }}</div>
              <div v-if="row.payTime" class="sub-text">付款时间：{{ shortTime(row.payTime) }}</div>
            </div>
            <div class="col col-ops">
              <el-button v-if="checkPermi(['admin:order:info'])" size="mini" type="success" plain class="op-btn" @click="onOrderDetails(row.orderId)">订单详情</el-button>
              <el-button
                v-if="row.statusStr.key === 'notShipped' && row.refundStatus === 0 && checkPermi(['admin:order:send'])"
                size="mini" type="primary" plain class="op-btn" @click="sendOrder(row)"
              >发货</el-button>
              <el-button
                v-if="row.paid === false && !row.isAlterPrice && checkPermi(['admin:order:update:price'])"
                size="mini" type="warning" plain class="op-btn" @click="edit(row)"
              >编辑</el-button>
              <!--视频号订单不可修改-->
              <el-button
                v-if="row.statusStr.key === 'spike' && row.type === 0 && checkPermi(['admin:order:tracking:number:update']) && !row.shipmentTaskId"
                size="mini" type="primary" plain class="op-btn" @click="handleUpdateNumber(row)"
              >修改快递单号</el-button>
              <el-button
                v-if="row.statusStr.key === 'toBeWrittenOff' && row.paid == true && row.refundStatus === 0 && checkPermi(['admin:order:write:update'])"
                size="mini" type="success" plain class="op-btn" @click="onWriteOff(row)"
              >立即核销</el-button>
              <el-dropdown v-if="hasMoreActions(row)" trigger="click" class="op-more">
                <span class="el-dropdown-link">更多<i class="el-icon-arrow-down el-icon--right" /></span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="onOrderMark(row)" v-if="checkPermi(['admin:order:mark'])">订单备注</el-dropdown-item>
                  <el-dropdown-item v-if="row.refundStatus === 1 && checkPermi(['admin:order:refund:refuse'])" @click.native="onOrderRefuse(row)">拒绝退款</el-dropdown-item>
                  <el-dropdown-item v-if="row.refundStatus === 1 && checkPermi(['admin:order:refund'])" @click.native="onOrderRefund(row)">立即退款</el-dropdown-item>
                  <el-dropdown-item v-if="row.statusStr.key === 'deleted' && checkPermi(['admin:order:delete'])" @click.native="handleDelete(row)">删除订单</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>

        <div v-if="!tableData.data.length && !listLoading" class="empty-tip">暂无订单</div>
      </div>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="tableFrom.limit"
          :current-page="tableFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
          background
        />
      </div>
    </el-card>
    <!--编辑-->
    <el-dialog title="编辑订单" :visible.sync="dialogVisible" width="500px" :before-close="handleClose">
      <zb-parser
        v-if="dialogVisible"
        :form-id="104"
        :is-create="isCreate"
        :edit-data="editData"
        @submit="handlerSubmit"
        @resetForm="resetForm"
        @closeDialog="dialogVisible = false"
      />
    </el-dialog>

    <!--记录-->
    <el-dialog title="操作记录" :visible.sync="dialogVisibleJI" width="700px">
      <el-table v-loading="LogLoading" border :data="tableDataLog.data" style="width: 100%">
        <el-table-column prop="oid" label="ID" min-width="80" />
        <el-table-column prop="changeMessage" label="操作记录" min-width="280" />
        <el-table-column prop="createTime" label="操作时间" min-width="280" />
      </el-table>
      <div class="block">
        <el-pagination
          :page-sizes="[10, 20, 30, 40]"
          :page-size="tableFromLog.limit"
          :current-page="tableFromLog.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableDataLog.total"
          @size-change="handleSizeChangeLog"
          @current-change="pageChangeLog"
        />
      </div>
    </el-dialog>

    <!--详情-->
    <details-from ref="orderDetail" :orderId="orderId" />

    <!-- 发送货 -->
    <order-send
      ref="send"
      :orderId="orderId"
      @submitFail="getList"
      :expressListNormal="expressListNormal"
      :expressListElec="expressListElec"
      :orderDetail="orderDetail"
    ></order-send>

    <!-- 发送货视频号商品 -->
    <order-video-send ref="videoSend" :orderId="orderId" @submitFail="getList"></order-video-send>

    <!--拒绝退款-->
    <el-dialog
      title="拒绝退款原因"
      v-if="RefuseVisible"
      :visible.sync="RefuseVisible"
      width="500px"
      :before-close="RefusehandleClose"
    >
      <zb-parser
        :form-id="106"
        :is-create="1"
        :edit-data="RefuseData"
        @submit="RefusehandlerSubmit"
        @resetForm="resetFormRefusehand"
        @closeDialog="RefuseVisible = false"
      />
    </el-dialog>

    <!--立即退款-->
    <el-dialog title="退款处理" :visible.sync="refundVisible" width="500px" :before-close="refundhandleClose">
      <zb-parser
        :form-id="107"
        :is-create="1"
        :edit-data="refundData"
        @submit="refundhandlerSubmit"
        v-if="refundVisible"
        @resetForm="resetFormRefundhandler"
        @closeDialog="refundVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script>
import {
  orderListDataApi,
  orderStatusNumApi,
  writeUpdateApi,
  orderListApi,
  updatePriceApi,
  orderLogApi,
  orderMarkApi,
  orderDeleteApi,
  orderRefuseApi,
  orderRefundApi,
  orderPrint,
  orderDetailApi,
} from '@/api/order';
import zbParser from '@/components/FormGenerator/components/parser/ZBParser';
import detailsFrom from './orderDetail';
import orderSend from './orderSend';
import orderVideoSend from './orderVideoSend';
import { storeStaffListApi } from '@/api/storePoint';
import Cookies from 'js-cookie';
import { orderExcelApi } from '@/api/store';
import { expressAllApi } from '@/api/sms';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
export default {
  name: 'orderlistDetails',
  components: {
    zbParser,
    detailsFrom,
    orderSend,
    orderVideoSend,
  },
  data() {
    return {
      options: [
        {
          value: 2,
          label: '全部',
        },
        {
          value: 0,
          label: '普通订单',
        },
        {
          value: 1,
          label: '视频号订单',
        },
      ],
      RefuseVisible: false,
      RefuseData: {},
      orderId: '',
      refundVisible: false,
      refundData: {},
      dialogVisibleJI: false,
      tableDataLog: {
        data: [],
        total: 0,
      },
      tableFromLog: {
        page: 1,
        limit: 10,
        orderNo: 0,
      },
      LogLoading: false,
      isCreate: 1,
      editData: null,
      dialogVisible: false,
      tableData: {
        data: [],
        total: 0,
      },
      listLoading: true,
      //订单状态（all 总数； 未支付 unPaid； 未发货 notShipped；待收货 spike；待评价 bargain；已完成 complete；
      // 待核销 toBeWrittenOff；退款中:refunding；已退款:refunded；已删除:deleted
      tableFrom: {
        status: 'all',
        dateLimit: '',
        orderNo: '',
        deliveryId: '', // 物流单号
        searchType: 'all',
        content: '',
        page: 1,
        limit: 20,
        type: 2,
      },
      orderChartType: {},
      timeVal: [],
      fromList: this.$constants.fromList,
      fromType: [
        { value: 'all', text: '全部' },
        { value: 'info', text: '普通' },
        { value: 'pintuan', text: '拼团' },
        { value: 'bragin', text: '砍价' },
        { value: 'miaosha', text: '秒杀' },
      ],
      selectionList: [],
      ids: '',
      orderids: '',
      cardLists: [],
      expressListNormal: [], //全部物流公司 normal
      expressListElec: [], //全部物流公司 elec
      orderDetail: null, //订单详情
    };
  },
  mounted() {
    this.getList();
    this.getOrderStatusNum();
    // this.getOrderListData();
    if (checkPermi(['admin:express:list'])) this.getExpress();
  },
  methods: {
    checkPermi,
    // ===== 卡片列表辅助（对齐订货商-订货商订单页） =====
    shortTime(t) {
      if (!t) return '-';
      return String(t).substring(0, 16);
    },
    fmtMoney(v) {
      const n = Number(v || 0);
      return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    // 订单类型文案：type 0=普通 1=视频号
    orderTypeText(row) {
      return row.type === 1 ? '视频号订单' : '普通订单';
    },
    // 「更多」下拉是否还有可见项（无则不渲染下拉）
    hasMoreActions(row) {
      return (
        checkPermi(['admin:order:mark']) ||
        (row.refundStatus === 1 && (checkPermi(['admin:order:refund:refuse']) || checkPermi(['admin:order:refund']))) ||
        (row.statusStr.key === 'deleted' && checkPermi(['admin:order:delete']))
      );
    },
    // 状态配色：未支付/待核销橙、未发货/待收货/待评价蓝、完成绿、退款/删除红灰
    statusClass(key) {
      return (
        {
          unPaid: 'is-warn',
          notShipped: 'is-info',
          spike: 'is-info',
          bargain: 'is-info',
          complete: 'is-ok',
          toBeWrittenOff: 'is-warn',
          refunding: 'is-danger',
          refunded: 'is-danger',
          deleted: 'is-muted',
        }[key] || ''
      );
    },
    //重置
    handleReset() {
      this.tableFrom.type = 2;
      this.tableFrom.dateLimit = '';
      this.tableFrom.orderNo = '';
      this.tableFrom.deliveryId = '';
      this.tableFrom.content = '';
      this.tableFrom.searchType = 'all';
      this.timeVal = [];
      this.getList();
      this.getOrderStatusNum();
    },
    isNumber(val) {
      return typeof val === 'number' && !Number.isNaN(val);
    },
    // 物流公司列表
    async getExpress() {
      this.expressListNormal = await this.getExpressList('normal'); //全部物流公司
      this.expressListElec = await this.getExpressList('elec');
    },
    // 物流公司列表
    async getExpressList(expressType) {
      return new Promise((resolve, reject) => {
        expressAllApi({ type: expressType }).then((res) => {
          resolve(res);
        });
      });
    },
    resetFormRefundhandler() {
      this.refundVisible = false;
    },
    resetFormRefusehand() {
      this.RefuseVisible = false;
    },
    resetForm(formValue) {
      this.dialogVisible = false;
    },
    // 核销订单
    onWriteOff(row) {
      this.$modalSure('核销订单吗').then(() => {
        writeUpdateApi(row.verifyCode).then(() => {
          this.$message.success('核销成功');
          this.tableFrom.page = 1;
          this.getList();
        });
      });
    },
    seachList() {
      this.tableFrom.page = 1;
      this.getList();
      this.getOrderStatusNum();
    },
    // 拒绝退款
    RefusehandleClose() {
      this.RefuseVisible = false;
    },
    onOrderRefuse(row) {
      this.orderids = row.orderId;
      this.RefuseData = {
        orderId: row.orderId,
        reason: '',
      };
      this.RefuseVisible = true;
    },
    RefusehandlerSubmit(formValue) {
      orderRefuseApi({ orderNo: this.orderids, reason: formValue.reason }).then((data) => {
        this.$message.success('操作成功');
        this.RefuseVisible = false;
        this.getList();
      });
    },
    // 立即退款
    refundhandleClose() {
      this.refundVisible = false;
    },
    onOrderRefund(row) {
      this.refundData = {
        orderId: row.orderId,
        amount: row.payPrice,
        type: '',
      };
      this.orderids = row.orderId;
      this.refundVisible = true;
    },
    refundhandlerSubmit(formValue) {
      orderRefundApi({ amount: formValue.amount, orderNo: this.orderids }).then((data) => {
        this.$message.success('操作成功');
        this.refundVisible = false;
        this.getList();
      });
    },
    // 详情接口
    getDetail(id) {
      orderDetailApi({ orderNo: id })
        .then((res) => {
          this.orderDetail = res;
          this.editData = {
            orderId: id,
            totalPrice: this.orderDetail.proTotalPrice,
            totalPostage: this.orderDetail.payPostage,
            payPrice: this.orderDetail.payPrice,
            payPostage: this.orderDetail.payPostage,
          };
          this.loading = true;
        })
        .catch(() => {
          this.orderDetail = null;
          this.loading = false;
        });
    },
    //修改快递单号
    handleUpdateNumber(row) {
      this.orderId = row.orderId;
      this.$refs.send.modals = true;
      this.$refs.send.loading = true;
      //默认加载Normal物流公司
      this.$refs.send.express = this.expressListNormal;
      this.getDetail(row.orderId);
    },
    // 发送
    sendOrder(row) {
      this.orderDetail = null;
      if (row.type === 0) {
        this.$refs.send.modals = true;
        //默认加载Normal物流公司
        this.$refs.send.express = this.expressListNormal;
        this.$refs.send.setRecipient({
          realName: row.realName,
          userPhone: row.userPhone,
          userAddress: row.userAddress,
        });
        this.$refs.send.sheetInfo();
      } else {
        this.$refs.videoSend.modals = true;
        if (!JSON.parse(sessionStorage.getItem('videoExpress'))) this.$refs.videoSend.companyGetList();
      }
      this.orderId = row.orderId;
    },
    // 订单删除
    handleDelete(row) {
      if (row.isDel) {
        this.$modalSure().then(() => {
          orderDeleteApi({ orderNo: row.orderId }).then(() => {
            this.$message.success('删除成功');
            if (this.tableData.data.length === 1 && this.tableFrom.page > 1)
              this.tableFrom.page = this.tableFrom.page - 1;
            this.getList();
          });
        });
      } else {
        this.$confirm('您选择的的订单存在用户未删除的订单，无法删除用户未删除的订单！', '提示', {
          confirmButtonText: '确定',
          type: 'error',
        });
      }
    },
    // 详情
    onOrderDetails(id) {
      this.orderId = id;
      this.$refs.orderDetail.getDetail(id);
      this.$refs.orderDetail.dialogVisible = true;
    },
    // 订单记录
    onOrderLog(id) {
      this.tableFromLog.limit = 10;
      this.orderId = id;
      this.dialogVisibleJI = true;
      this.getOrderStatusList(id);
    },
    //订单记录请求列表
    getOrderStatusList(id) {
      this.LogLoading = true;
      this.tableFromLog.orderNo = id;
      orderLogApi(this.tableFromLog)
        .then((res) => {
          this.tableDataLog.data = res.list;
          this.tableDataLog.total = res.total;
          this.LogLoading = false;
        })
        .catch(() => {
          this.LogLoading = false;
        });
    },
    pageChangeLog(page) {
      this.tableFromLog.page = page;
      this.getOrderStatusList(this.orderId);
    },
    handleSizeChangeLog(val) {
      this.tableFromLog.limit = val;
      this.getOrderStatusList(this.orderId);
    },
    handleClose() {
      this.dialogVisible = false;
    },
    // 备注
    onOrderMark(row) {
      this.$modalPrompt('textarea', '订单备注', null, '订单备注').then((V) => {
        orderMarkApi({ mark: V, orderNo: row.orderId }).then(() => {
          this.$message.success('操作成功');
          this.getList();
        });
      });
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.tableFrom.dateLimit = e ? this.timeVal.join(',') : '';
      this.tableFrom.page = 1;
      this.getList();
      this.getOrderStatusNum();
      // this.getOrderListData();
    },
    // 编辑
    edit(row) {
      //this.getDetail(row.orderId);
      this.orderId = row.orderId;

      this.editData = {
        orderId: row.orderId,
        proTotalPrice: row.proTotalPrice,
        beforePayPrice: row.beforePayPrice ? row.beforePayPrice : row.payPrice,
        payPrice: row.payPrice,
      };
      this.dialogVisible = true;
    },
    handlerSubmit(formValue) {
      let data = {
        orderNo: formValue.orderId,
        payPrice: formValue.payPrice,
      };
      updatePriceApi(data).then((data) => {
        this.$message.success('编辑成功');
        this.dialogVisible = false;
        this.getList();
      });
    },
    // 列表
    getList() {
      this.listLoading = true;
      orderListApi(this.tableFrom)
        .then((res) => {
          this.tableData.data = res.list || [];
          this.tableData.total = res.total;
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
    },
    // 数据统计
    getOrderListData() {
      orderListDataApi({ dateLimit: this.tableFrom.dateLimit }).then((res) => {
        this.cardLists = [
          { name: '订单数量', count: res.count, color: '#1890FF', class: 'one', icon: 'icondingdan' },
          { name: '订单金额', count: res.amount, color: '#A277FF', class: 'two', icon: 'icondingdanjine' },
          {
            name: '微信支付金额',
            count: res.weChatAmount,
            color: '#EF9C20',
            class: 'three',
            icon: 'iconweixinzhifujine',
          },
          { name: '余额支付金额', count: res.yueAmount, color: '#1BBE6B', class: 'four', icon: 'iconyuezhifujine2' },
        ];
      });
    },
    // 获取各状态数量
    getOrderStatusNum() {
      orderStatusNumApi({
        dateLimit: this.tableFrom.dateLimit,
        type: this.tableFrom.type,
        orderNo: this.tableFrom.orderNo,
        deliveryId: this.tableFrom.deliveryId,
        searchType: this.tableFrom.searchType,
        content: this.tableFrom.content,
      }).then((res) => {
        this.orderChartType = res;
      });
    },
    pageChange(page) {
      this.tableFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.tableFrom.limit = val;
      this.getList();
    },
    exports() {
      let data = {
        dateLimit: this.tableFrom.dateLimit,
        orderNo: this.tableFrom.orderNo,
        deliveryId: this.tableFrom.deliveryId,
        status: this.tableFrom.status,
        type: this.tableFrom.type,
      };
      orderExcelApi(data).then((res) => {
        window.open(res.fileName);
      });
    },
    //打印小票
    onOrderPrint(data) {
      orderPrint(data.orderId)
        .then((res) => {
          this.$modal.msgSuccess('打印成功');
        })
        .catch((error) => {
          this.$modal.msgError(error.message);
        });
    },
  },
};
</script>
<style lang="scss" scoped>
/* ===== 订单卡片列表（复用订货商-订货商订单页样式） ===== */
.sub-text {
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}
.order-list {
  min-height: 120px;
}
.order-card {
  margin-bottom: 14px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}
.order-card:hover {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

/* 卡片头 */
.card-head {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 40px;
  background: #f0f4fb;
  font-size: 13px;
}
.head-label {
  color: #606266;
  margin-left: 14px;
}
.head-no {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.head-time {
  color: #909399;
}
.head-status {
  margin-left: 14px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}
.head-status.is-warn { color: #ff9900; }
.head-status.is-info { color: #409eff; }
.head-status.is-ok { color: #19be6b; }
.head-status.is-danger { color: #f56c6c; }
.head-status.is-muted { color: #909399; }
.head-reason {
  color: #f56c6c;
  margin-left: 14px;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 卡片头右侧快捷链接 */
.head-links {
  margin-left: auto;
  display: flex;
  gap: 14px;
}
.head-links a {
  color: #606266;
  font-size: 13px;
  cursor: pointer;
}
.head-links a:hover {
  color: #0256ff;
}
.card-head .mini-chip {
  margin-left: 14px;
}

/* 卡片体：网格分列，列间细分隔线 */
.card-body {
  display: grid;
  grid-template-columns: minmax(300px, 2fr) minmax(200px, 1.1fr) 150px 150px 140px;
  align-items: center;
  padding: 16px 0;
}
.col {
  padding: 4px 16px;
  min-width: 0;
  align-self: center;
}
.col + .col {
  border-left: 1px solid #f2f4f7;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 键值行（用户/金额列） */
.kv {
  display: flex;
  font-size: 13px;
  line-height: 22px;
  min-width: 0;
}
.kv .k {
  color: #909399;
  flex-shrink: 0;
}
.kv .v {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 商品列：大图 + 多行名称 + 右侧数量价格 */
.goods-item {
  display: flex;
  align-items: flex-start;
  margin: 4px 0;
}
.goods-img {
  width: 72px;
  height: 72px;
  border-radius: 4px;
  flex-shrink: 0;
  background: #f5f7fa;
  margin-right: 12px;
}
.goods-info {
  flex: 1;
  min-width: 0;
}
.goods-name {
  font-size: 14px;
  color: #303133;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.goods-sku-row {
  margin-top: 6px;
}
.goods-side {
  flex-shrink: 0;
  margin-left: 12px;
  text-align: right;
}
.goods-num {
  font-size: 12px;
  color: #909399;
  line-height: 20px;
}
.goods-price {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 20px;
  margin-top: 4px;
}

/* 收货人列 */
.recv-name {
  font-size: 14px;
  color: #303133;
  line-height: 22px;
}
.recv-phone {
  margin-left: 8px;
}
.recv-addr {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 金额列（总价/实付小字多行） */
.col-amount .kv .v {
  color: #303133;
  font-weight: 500;
}
.amount {
  font-size: 18px;
  font-weight: 600;
  color: #e93323;
  font-family: DIN, 'Helvetica Neue', Arial, sans-serif;
  line-height: 26px;
}

/* 标签 chip */
.recv-tags {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}
.mini-chip {
  padding: 0 8px;
  border-radius: 3px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  color: #909399;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}

/* 支付状态列 */
.pay-text {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  line-height: 22px;
}

/* 操作列：小按钮自动换行 */
.col-ops {
  align-items: center;
}
.op-btn {
  min-width: 76px;
  margin: 4px 0 !important;
  margin-left: 0 !important;
  display: block;
}
.op-more {
  margin-top: 6px;
}
.op-more .el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
}
.op-more .el-icon-arrow-down {
  font-size: 12px;
}
.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 32px 0;
}

/* 退款气泡内容 */
.pup_card {
  width: 200px;
  border-radius: 5px;
  padding: 5px;
  box-sizing: border-box;
  font-size: 12px;
  line-height: 16px;
}
.flex-column {
  display: flex;
  flex-direction: column;
}

.block {
  margin-bottom: 20px;
}
</style>
