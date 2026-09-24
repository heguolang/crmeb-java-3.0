<template>
  <div>
    <el-drawer :visible.sync="dialogVisible" :direction="direction" size="1000px" :before-close="handleClose">
      <div v-if="orderDatalist" v-loading="loading">
        <div class="detailHead">
          <div class="full">
            <div class="order_icon"><span class="iconfont icondingdanguanli"></span></div>
            <div class="text">
              <div class="title">{{ orderDatalist.orderTypeText || '-' }}</div>
              <div>
                <span class="mr20">订单号：{{ orderDatalist.orderId }}</span>
              </div>
            </div>
          </div>
          <ul class="list">
            <li class="item" v-if="orderDatalist.statusStr">
              <div class="title">订单状态</div>
              <div class="color-warning">
                <span>{{ orderDatalist.statusStr.value }}</span>
              </div>
            </li>
            <li class="item">
              <div class="title">实际支付</div>
              <div>¥ {{ orderDatalist.payPrice || '0.0' }}</div>
            </li>
            <li class="item">
              <div class="title">支付方式</div>
              <div>{{ orderDatalist.payType | payTypeFilter }}</div>
            </li>
            <li class="item">
              <div class="title">创建时间</div>
              <div>{{ orderDatalist.createTime || '-' }}</div>
            </li>
          </ul>
        </div>
        <el-tabs type="border-card" v-model="activeName">
          <el-tab-pane label="订单信息" name="detail">
            <div class="detailSection" style="border: none">
              <div class="title">下单会员信息</div>
              <ul class="list">
                <li class="item">
                  <div class="lang">下单昵称：</div>
                  <div class="value">{{ orderDatalist.nikeName | filterEmpty }}</div>
                </li>
                <li class="item">
                  <div class="lang">会员ID：</div>
                  <div class="value">{{ orderDatalist.uid | filterEmpty }}</div>
                </li>
                <li class="item">
                  <div class="lang">会员手机号：</div>
                  <div class="value">{{ orderDatalist.phone | filterEmpty }}</div>
                </li>
              </ul>
            </div>
            <div class="detailSection" style="border: none">
              <div class="title">推荐人信息</div>
              <ul class="list">
                <li class="item">
                  <div class="lang">推荐人昵称：</div>
                  <div class="value">{{ orderDatalist.spreadName | filterEmpty }}</div>
                </li>
                <li class="item">
                  <div class="lang">推荐人ID：</div>
                  <div class="value">{{ orderDatalist.spreadUid | filterEmpty }}</div>
                </li>
                <li class="item">
                  <div class="lang">推荐人手机号：</div>
                  <div class="value">{{ orderDatalist.spreadPhone | filterEmpty }}</div>
                </li>
              </ul>
            </div>
            <div v-if="orderDatalist.shippingType < 2 && orderDatalist.secondType !== 2" class="detailSection">
              <div class="title">收货信息</div>
              <ul class="list">
                <li class="item">
                  <div class="lang">收货人：</div>
                  <div class="value">
                    {{ orderDatalist.realName }}
                  </div>
                </li>
                <li class="item">
                  <div class="lang">收货电话：</div>
                  <div class="value">
                    {{ orderDatalist.userPhone }}
                  </div>
                </li>
                <li class="item">
                  <div class="lang">收货地址：</div>
                  <div class="value">
                    {{ orderDatalist.userAddress }}
                  </div>
                </li>
              </ul>
            </div>
            <div v-if="orderDatalist.shippingType === 2" class="detailSection">
              <div class="title">核销信息</div>
              <ul class="list">
                <li class="item">
                  <div class="lang">核销姓名：</div>
                  <div class="value">
                    {{ orderDatalist.realName }}
                  </div>
                </li>
                <li class="item">
                  <div class="lang">核销电话：</div>
                  <div class="value">
                    {{ orderDatalist.userPhone }}
                  </div>
                </li>
              </ul>
            </div>
            <div class="detailSection">
              <div class="title">订单信息</div>
              <ul class="list">
                <li class="item">
                  <div class="lang">商品总价：</div>
                  <div class="value">{{ orderDatalist.proTotalPrice }}</div>
                </li>
                <li class="item">
                  <div class="lang">商品总数：</div>
                  <div class="value">{{ orderDatalist.totalNum }}</div>
                </li>
                <li class="item">
                  <div class="lang">优惠券：</div>
                  <div class="value">{{ orderDatalist.couponPrice }}</div>
                </li>
                <li class="item">
                  <div class="lang">实际支付：</div>
                  <div class="value">{{ orderDatalist.payPrice || '0.0' }}</div>
                </li>
                <li class="item">
                  <div class="lang">抵扣金额：</div>
                  <div class="value">{{ orderDatalist.deductionPrice || '0.0' }}</div>
                </li>
                <li class="item">
                  <div class="lang">退款金额：</div>
                  <div class="value">{{ orderDatalist.refundPrice || '0.0' }}</div>
                </li>
                <li class="item">
                  <div class="lang">支付邮费：</div>
                  <div class="value">{{ orderDatalist.payPostage }}</div>
                </li>
                <li class="item">
                  <div class="lang">支付方式：</div>
                  <div class="value">{{ orderDatalist.payType | payTypeFilter }}</div>
                </li>
                <li class="item">
                  <div class="lang">创建时间：</div>
                  <div class="value">{{ orderDatalist.createTime | filterEmpty }}</div>
                </li>
                <li class="item">
                  <div class="lang">推广人：</div>
                  <div class="value">{{ orderDatalist.spreadName | filterEmpty }}</div>
                </li>
              </ul>
            </div>
            <div class="detailSection">
              <div class="title">买家留言</div>
              <ul class="list">
                <li class="item">
                  <div>{{ orderDatalist.mark | filterEmpty }}</div>
                </li>
              </ul>
            </div>
            <div class="detailSection">
              <div class="title">商家备注</div>
              <ul class="list">
                <li class="item">
                  <div>{{ orderDatalist.remark | filterEmpty }}</div>
                </li>
              </ul>
            </div>
            <div class="detailSection">
              <div class="title">分销信息</div>
              <el-table
                v-if="orderDatalist.brokerageList && orderDatalist.brokerageList.length"
                :data="orderDatalist.brokerageList"
                size="small"
                border
                style="width: 100%"
              >
                <el-table-column label="奖励类型" min-width="110">
                  <template slot-scope="scope">
                    {{ brokerageLevelText(scope.row.brokerageLevel) }}
                  </template>
                </el-table-column>
                <el-table-column label="获得会员" min-width="160">
                  <template slot-scope="scope">
                    <div>{{ scope.row.userName || '-' }}</div>
                    <div class="color-909399">UID：{{ scope.row.uid }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="120" />
                <el-table-column label="金额" min-width="100">
                  <template slot-scope="scope">
                    <span style="color: #ff9900">+{{ scope.row.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" min-width="90">
                  <template slot-scope="scope">
                    {{ brokerageStatusText(scope.row.status) }}
                  </template>
                </el-table-column>
                <el-table-column prop="mark" label="备注" min-width="180" show-overflow-tooltip />
                <el-table-column label="时间" min-width="150">
                  <template slot-scope="scope">
                    {{ scope.row.createTime || '-' }}
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="color-909399" style="padding: 8px 0">暂无分佣记录</div>
            </div>
            <div v-if="orderExtend.length" class="detailSection">
              <div class="title">自定义留言</div>
              <ul class="list">
                <li class="item" v-for="(item, index) in orderExtend" :key="index">
                  <div class="lang" :title="item.title">{{ item.title }}</div>
                  <div>{{ item.title.includes(':') ? '' : '：' }}</div>
                  <div v-if="!Array.isArray(item.value)" class="value">{{ item.value | filterEmpty }}</div>
                  <div v-else class="flex conter">
                    <template v-if="item.value">
                      <div v-for="(pic, idx) in item.value" :key="idx">
                        <el-image v-if="pic.includes('http')" class="pictrue" :src="pic" :preview-src-list="[pic]" />
                        <div v-else class="text-14px fontColor333 ml-5px acea-row row-middle mr5">
                          {{ pic }}
                          <div style="margin-left: 6px" v-show="idx < item.value.length - 1">-</div>
                        </div>
                      </div>
                    </template>
                    <template v-else> - </template>
                  </div>
                </li>
              </ul>
            </div>
          </el-tab-pane>
          <el-tab-pane label="商品信息" name="goods" class="tabBox">
            <el-table class="mt20 orderDetailList" :data="orderDatalist.orderInfo" size="small">
              <el-table-column label="商品信息" min-width="400" :show-overflow-tooltip="true">
                <template slot-scope="scope">
                  <div class="acea-row row-middle">
                    <div class="demo-image__preview mr15">
                      <el-image :src="scope.row.info.image" :preview-src-list="[scope.row.info.image]" />
                    </div>
                    <div style="width: 408px">
                      <div class="line1 mb10">{{ scope.row.info.productName }}</div>
                      <div class="line1 color-909399 line-heightOne">规格：{{ scope.row.info.sku }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="商品售价" min-width="90">
                <template slot-scope="scope">
                  <div class="acea-row row-middle">
                    <div class="line1">
                      {{ scope.row.info.price }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="购买数量" min-width="90">
                <template slot-scope="scope">
                  <div class="acea-row row-middle">
                    <div class="line1">
                      {{ scope.row.info.payNum }}
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane v-if="orderDatalist.status > 0" label="发货记录" name="delivery" class="tabBox">
            <template>
              <div>
                <el-table class="mt20" size="small" :data="orderDatalist.orderInfo">
                  <el-table-column min-width="400">
                    <template slot="header" slot-scope="scope">
                      <template v-if="orderDatalist.deliveryType === 'express'">
                        <span class="font-color">【快递配送】</span>
                        <span>{{ orderDatalist.deliveryName + '：' + orderDatalist.deliveryId }}</span>
                        <span class="ml30">{{ orderDatalist.createTime }}</span>
                      </template>
                      <template v-else-if="orderDatalist.deliveryType === 'send'">
                        <span class="font-color">【商家送货】</span>
                        <span>{{ orderDatalist.deliveryName + '：' + orderDatalist.deliveryId }}</span>
                        <span class="ml30">{{ orderDatalist.createTime }}</span>
                      </template>
                      <template v-else>
                        <span class="font-color">【虚拟发货】</span>
                        <span class="ml30">{{ orderDatalist.createTime }}</span>
                      </template>
                    </template>
                    <template slot-scope="scope">
                      <div class="acea-row row-middle">
                        <div class="demo-image__preview mr15">
                          <el-image :src="scope.row.info.image" :preview-src-list="[scope.row.info.image]" />
                        </div>
                        <div style="width: 408px">
                          <div class="line1 mb10 line-heightOne">{{ scope.row.info.productName }}</div>
                          <div class="line1 color-909399 line-heightOne">规格：{{ scope.row.info.sku }}</div>
                        </div>
                        <div class="acea-row row-middle ml30">
                          <div class="line1 font12 color-text">X {{ scope.row.info.payNum }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column width="400" fixed="right">
                    <template slot="header" slot-scope="scope">
                      <div class="flex mr10" style="justify-content: flex-end">
                        <a
                          v-if="orderDatalist.deliveryType === 'express'"
                          @click="openLogistics(orderDatalist.orderId, orderDatalist.deliveryName)"
                          >查看物流
                        </a>
                      </div>
                    </template>
                    <template v-if="orderDatalist.deliveryType === 'noNeed'" slot-scope="scope">
                      <div class="acea-row row-middle">
                        <div class="font12 color-text">发货备注：{{ orderDatalist.deliveryMark }}</div>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
    <el-dialog v-if="orderDatalist" title="提示" :visible.sync="modal2" width="30%">
      <div class="logistics acea-row row-top">
        <div class="logistics_img"><img src="@/assets/imgs/expressi.jpg" /></div>
        <div class="logistics_cent">
          <span class="mb10">物流公司：{{ orderDatalist.deliveryName }}</span>
          <span>物流单号：{{ orderDatalist.deliveryId }}</span>
        </div>
      </div>
      <div class="acea-row row-column-around trees-coadd">
        <div class="scollhide">
          <el-timeline :reverse="reverse">
            <el-timeline-item v-for="(item, i) in result" :key="i">
              <p class="time" v-text="item.time"></p>
              <p class="content" v-text="item.status"></p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="modal2 = false">取消</el-button>
        <el-button type="primary" @click="modal2 = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

import { getLogisticsInfoApi, orderInvoiceListApi, orderDetailApi, refundOrderDetailApi } from '@/api/order';
import { checkPermi } from '@/utils/permission';
export default {
  name: 'OrderDetail',
  props: {
    orderId: {
      default: 0,
    },
  },
  data() {
    return {
      activeName: 'detail',
      direction: 'rtl',
      reverse: true,
      dialogVisible: false,
      orderDatalist: {},
      loading: false,
      modal2: false,
      result: [],
      resultInfo: {},
      InvoiceList: [],
      refundInfo: {},
      editDeliveryDialogVisible: false,
      editData: {},
      orderExtend: [], //系统表单数据
      expressName: '', //快递名称
    };
  },
  methods: {
    checkPermi,
    brokerageLevelText(level) {
      const map = {
        0: '自购返佣',
        1: '一级分销',
        2: '二级分销',
        10: '团队极差奖',
        11: '团队平级奖',
        12: '区域代理奖励',
      };
      return map[level] || `等级${level == null ? '-' : level}`;
    },
    brokerageStatusText(status) {
      const map = {
        1: '订单创建',
        2: '冻结中',
        3: '已到账',
        4: '已失效',
        5: '提现申请',
      };
      return map[status] || '-';
    },
    //修改物流信息
    handleEditLogistics(row) {
      this.editDeliveryDialogVisible = true;
      this.editData = row;
    },
    //关闭配送信息
    onCloseVisible() {
      this.editDeliveryDialogVisible = false;
    },
    handleClose() {
      this.dialogVisible = false;
    },
    // 获取订单退款信息
    getRefundOrderDetail(id) {
      refundOrderDetailApi(id).then(async (res) => {
        this.refundInfo = res;
      });
    },
    openLogistics() {
      this.getOrderData();
      this.modal2 = true;
    },
    // 获取订单物流信息
    getOrderData() {
      getLogisticsInfoApi({ orderNo: this.orderId }).then(async (res) => {
        this.result = res.list;
      });
    },
    getDetail(id) {
      this.loading = true;
      orderDetailApi({ orderNo: id })
        .then((res) => {
          this.orderDatalist = res;
          this.orderExtend = res.orderExtend ? JSON.parse(res.orderExtend) : [];
          this.activeName = 'detail';
          this.loading = false;
        })
        .catch(() => {
          this.orderDatalist = null;
          this.loading = false;
        });
    },
  },
};
</script>

<style scoped lang="scss">
::v-deep .el-tabs__content {
  padding: 0 20px !important;
}
.detailSection {
  padding: 25px 15px !important;
}
::v-deep .el-table th.el-table__cell > .cell,
::v-deep.el-table .cell,
.el-table--border .el-table__cell:first-child .cell {
  padding-left: 15px;
}
.InvoiceList {
  ::v-deep.el-collapse-item__header {
    font-size: 12px;
    color: #606266;
  }
}

.wrapper {
  background-color: #fff;
  margin-top: 7px;
  padding: 10px 12px;
  &-num {
    font-size: 10px;
    color: #999999;
  }

  &-title {
    color: #666666;
    font-size: 12px;
  }

  &-img {
    width: 60px;
    height: 60px;
    margin-right: 10px;
    border-radius: 7px;
    overflow: hidden;
    margin-bottom: 10px;

    image {
      width: 100%;
      height: 100%;
    }

    &:nth-child(5n) {
      margin-right: 0;
    }
  }
}

.title {
  font-size: 36px;
}

.demo-drawer__content {
  padding: 0 30px;
}

.demo-image__preview {
  display: inline-block;
  .el-image {
    width: 50px;
    height: 50px;
  }
}

.logistics {
  align-items: center;
  padding: 10px 0px;
  .logistics_img {
    width: 45px;
    height: 45px;
    margin-right: 12px;
    img {
      width: 100%;
      height: 100%;
    }
  }
  .logistics_cent {
    span {
      display: block;
      font-size: 12px;
    }
  }
}

.trees-coadd {
  width: 100%;
  height: 400px;
  border-radius: 4px;
  overflow: hidden;
  .scollhide {
    width: 100%;
    height: 100%;
    overflow: auto;
    margin-left: 18px;
    padding: 10px 0 10px 0;
    box-sizing: border-box;
    .content {
      font-size: 12px;
    }

    .time {
      font-size: 12px;
      color: #2d8cf0;
    }
  }
}

.title {
  margin-bottom: 14px;
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.description {
  &-term {
    display: table-cell;
    padding-bottom: 5px;
    line-height: 20px;
    width: 50%;
    font-size: 12px;
    color: #606266;
  }
  ::v-deep .el-divider--horizontal {
    margin: 12px 0 !important;
  }
}
::v-deep .el-tabs__item:focus.is-active.is-focus:not(:active) {
  -webkit-box-shadow: none;
  box-shadow: none;
}
::v-deep .el-drawer__header {
  display: block !important;
  margin-bottom: 0 !important;
  padding: 0 !important;
}
::v-deep .el-drawer__close-btn {
  position: absolute;
  right: 20px;
  top: 30px;
}
::v-deep .el-tabs__nav .el-tabs__item:nth-of-type(1) {
  padding-left: 20px !important;
}
</style>
