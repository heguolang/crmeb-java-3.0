<template>
  <div class="divBox">
    <pages-header
      ref="pageHeader"
      :title="$route.params.id ? (isDisabled ? '商品详情' : '编辑商品') : '添加商品'"
      backUrl="/store/index"
    ></pages-header>
    <el-card v-if="$route.params.isCopy" class="mt14" shadow="never" :bordered="false">
      <div class="line-ht mb15">
        生成的商品默认是没有上架的，请手动上架商品！
        <span v-if="copyConfig.copyType && copyConfig.copyType == 1"
          >您当前剩余{{ copyConfig.copyNum }}条采集次数。
        </span>
        <div class="tips-bottom">
          商品采集设置：设置 > 系统设置 > 第三方接口设置 >
          采集商品配置（如配置一号通采集，请先登录一号通账号，无一号通，请选择99Api设置）
        </div>
      </div>
      <div :span="24" v-if="copyConfig.copyType">
        <el-input v-model.trim="url" placeholder="请输入链接地址" class="selWidth100" size="small">
          <el-button slot="append" icon="el-icon-search" @click="addProduct" size="small" />
        </el-input>
      </div>
    </el-card>
    <el-card class="box-card mt14">
      <el-tabs class="list-tabs" v-model="currentTab" @tab-click="tabsHandleClick">
        <el-tab-pane label="商品信息" name="0"></el-tab-pane>
        <el-tab-pane label="规格库存" name="1"></el-tab-pane>
        <el-tab-pane label="商品详情" name="2"></el-tab-pane>
        <el-tab-pane label="其他设置" name="3"></el-tab-pane>
        <el-tab-pane label="佣金设置" name="4"></el-tab-pane>
      </el-tabs>
      <el-form
        ref="formValidate"
        v-loading="fullscreenLoading"
        class="formValidate mt20"
        :rules="ruleValidate"
        :model="formValidate"
        label-width="90px"
        @submit.native.prevent
      >
        <el-row v-show="currentTab == 0" :gutter="24">
          <!-- 商品信息-->
          <el-col v-bind="grid2">
            <el-form-item label="商品名称：" prop="storeName">
              <el-input
                class="from-ipt-width"
                v-model="formValidate.storeName"
                maxlength="249"
                placeholder="请输入商品名称"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>
          <el-col v-bind="grid2">
            <el-form-item label="商品分类：" prop="cateIds">
              <el-cascader
                class="from-ipt-width"
                v-model="formValidate.cateIds"
                :options="merCateList"
                :props="props2"
                clearable
                :show-all-levels="false"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>
          <el-col v-bind="grid2">
            <el-form-item label="商品分组：">
              <el-select
                class="from-ipt-width"
                v-model="formValidate.productGroupIds"
                multiple
                clearable
                filterable
                placeholder="请选择商品分组（可多选）"
                :disabled="isDisabled"
              >
                <el-option v-for="item in productGroupOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-bind="grid2">
            <el-form-item label="商品关键字：" prop="keyword">
              <el-input
                class="from-ipt-width"
                v-model="formValidate.keyword"
                placeholder="请输入商品关键字"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>
          <el-col v-bind="grid2">
            <el-form-item label="单位：" prop="unitName">
              <el-input
                class="from-ipt-width"
                v-model="formValidate.unitName"
                placeholder="请输入单位"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品封面图：" prop="image">
              <div class="acea-row upLoadPicBox row-middle">
                <div v-if="formValidate.image" class="pictrue" @click="modalPicTap('1')">
                  <el-image
                    class="image"
                    :src="formValidate.image"
                    :preview-src-list="isDisabled ? [formValidate.image] : []"
                  >
                  </el-image>
                </div>
                <div v-else class="upLoad" @click="modalPicTap('1')">
                  <i class="el-icon-camera cameraIconfont" />
                </div>
              </div>
              <div class="from-tips" v-show="!isDisabled">建议尺寸：800*800px，上传小于500kb的图片</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品轮播图：" prop="sliderImages">
              <div class="acea-row">
                <div
                  v-for="(item, index) in formValidate.sliderImages"
                  :key="index"
                  class="pictrue"
                  draggable="true"
                  @dragstart="handleDragStart($event, item)"
                  @dragover.prevent="handleDragOver($event, item)"
                  @dragenter="handleDragEnter($event, item)"
                  @dragend="handleDragEnd($event, item)"
                >
                  <el-image class="image" :src="item" :preview-src-list="formValidate.sliderImages"> </el-image>
                  <i v-if="!isDisabled" class="el-icon-error btndel" @click="handleRemove(index)" />
                </div>
                <div
                  v-if="formValidate.sliderImages.length < 10 && !isDisabled"
                  class="upLoadPicBox"
                  @click="modalPicTap('2')"
                >
                  <div class="upLoad">
                    <i class="el-icon-camera cameraIconfont" />
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="保障服务：" prop="guarantee">
              <el-select
                v-model="guaranteeIdsList"
                multiple
                collapse-tags
                placeholder="请选择"
                class="from-ipt-width"
                @change="updateGuaranteeIds"
                :disabled="isDisabled"
              >
                <el-option v-for="item in guaranteeList" :key="item.id" :label="item.name" :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item label="主图视频：">
              <el-input
                class="from-ipt-width mr15"
                maxlength="250"
                v-model="videoLink"
                :disabled="isDisabled"
                placeholder="请输入视频链接"
              />
              <el-button v-if="videoLink" @click="zh_uploadFile" :disabled="isDisabled">确认添加</el-button>
              <el-button v-if="!videoLink" :disabled="isDisabled" @click="modalPicTap('3', '', '', 'video')"
                >选择视频</el-button
              >
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="iview-video-style" v-if="formValidate.videoLink">
              <video
                style="width: 100%; height: 100% !important; border-radius: 10px"
                :src="formValidate.videoLink"
                controls
                autoplay
                muted
              >
                您的浏览器不支持 video 标签。
              </video>
              <div class="mark"></div>
              <span class="iconv iconfont iconmd-trash" @click="delVideo()"></span>
            </div>
          </el-col>
          <el-col>
            <el-form-item label="运费模板：" prop="tempId">
              <el-select
                class="from-ipt-width mr15"
                v-model="formValidate.tempId"
                placeholder="请选择"
                :disabled="isDisabled"
                style="width: 100%"
              >
                <el-option v-for="item in shippingList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <el-button v-show="!isDisabled" class="mr15" @click="addTem">运费模板</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <creatAttr
          v-if="currentTab == 1"
          v-model="formValidate"
          :oneFormBatch="oneFormBatch"
          :isDisabled="isDisabled"
          :formThead="formThead"
          :manyTabDate="manyTabDate"
          :OneattrValue="OneattrValue"
          :ManyAttrValue="ManyAttrValue"
          :manyTabTit="manyTabTit"
          @changeManyAttrValue="changeManyAttrValue"
          @handleBatchDel="handleBatchDel"
        ></creatAttr>
        <!-- 商品详情-->
        <el-row v-show="currentTab == 2 && !isDisabled">
          <el-col :span="24">
            <el-form-item label="商品详情：">
              <Tinymce v-model.trim="formValidate.content" :key="htmlKey"></Tinymce>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-show="currentTab == 2 && isDisabled">
          <el-col :span="24">
            <el-form-item label="商品详情：">
              <span v-html="formValidate.content || '无'"></span>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 其他设置-->
        <el-row v-show="currentTab == 3">
          <el-col :span="24">
            <el-col v-bind="grid">
              <el-form-item label="排序：">
                <el-input-number
                  controls-position="right"
                  v-model="formValidate.sort"
                  :min="0"
                  placeholder="请输入排序"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col v-bind="grid">
              <el-form-item label="积分：">
                <el-input-number
                  controls-position="right"
                  v-model="formValidate.giveIntegral"
                  :min="0"
                  placeholder="请输入排序"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col v-bind="grid">
              <el-form-item label="虚拟销量：">
                <el-input-number
                  controls-position="right"
                  v-model="formValidate.ficti"
                  :min="0"
                  placeholder="请输入排序"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品推荐：">
              <el-checkbox-group v-model="checkboxGroup" size="small" @change="onChangeGroup" :disabled="isDisabled">
                <el-checkbox v-for="(item, index) in recommend" :key="index" :label="item.value">{{
                  item.name
                }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="门店服务：">
              <div class="store-service">
                <el-switch v-model="formValidate.isStore" :disabled="isDisabled" />
                <span class="store-service-switch-text">{{ formValidate.isStore ? '已开启门店服务' : '未开启门店服务' }}</span>
              </div>
              <div v-if="formValidate.isStore" class="store-service-sub">
                <span class="store-service-label">门店履约方式：</span>
                <el-checkbox v-model="formValidate.storeSelfPickup" :disabled="isDisabled">到店自提</el-checkbox>
                <el-checkbox v-model="formValidate.storeDelivery" :disabled="isDisabled">上门配送</el-checkbox>
                <span class="store-service-tip">不勾选则该商品不支持对应的门店履约方式</span>
              </div>
              <div class="store-service-tip">开启后该商品可在门店进行自提、配送与核销</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="活动优先级：">
              <div class="color-list acea-row row-middle">
                <div
                  :disabled="isDisabled"
                  class="color-item"
                  :class="activity[item]"
                  v-for="item in formValidate.activity"
                  :key="item"
                  draggable="true"
                  @dragstart="handleDragStart($event, item)"
                  @dragover.prevent="handleDragOver($event, item)"
                  @dragenter="handleDragEnterFont($event, item)"
                  @dragend="handleDragEnd($event, item)"
                >
                  {{ item }}
                </div>
                <div class="tip">可拖动按钮调整活动的优先展示顺序</div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="优惠券：" class="proCoupon">
              <div>
                <el-tag
                  v-for="(tag, index) in formValidate.coupons"
                  :key="index"
                  class="mr10"
                  :closable="!isDisabled"
                  :disable-transitions="false"
                  @close="handleCloseCoupon(tag)"
                >
                  {{ tag.name }}
                </el-tag>
                <span class="mr10" v-if="formValidate.couponIds == null">暂无优惠券</span>
                <el-button v-if="!isDisabled" class="mr15" @click="addCoupon">选择优惠券</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 佣金设置 -->
        <div v-show="currentTab == 4" class="commission-setting">
          <el-alert
            title="不填表示取全局/等级配置；显式填 0 表示该商品无此项奖励。金额(元)与比例(%)可二选一，同时填时以金额优先。"
            type="info"
            :closable="false"
            show-icon
            class="mb15"
          />
          <el-alert
            title="规格库存里的「单独分佣 / 一二佣」为旧字段：保存时会自动迁入本页「分销商」配置；结算与商详气泡优先读本页配置。"
            type="info"
            :closable="false"
            show-icon
            class="mb15"
          />
          <el-divider content-position="left">分销商</el-divider>
          <el-form-item label="分销返佣：">
            <el-radio-group v-model="formValidate.commissionConfig.distributor.enabled" :disabled="isDisabled">
              <el-radio :label="null">跟随全局</el-radio>
              <el-radio :label="true">开启</el-radio>
              <el-radio :label="false">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="直属上级：">
                <div class="commission-pair">
                  <el-input
                    v-model="formValidate.commissionConfig.distributor.directAmount"
                    placeholder="金额(元)"
                    clearable
                    :disabled="isDisabled"
                  />
                  <span class="mx6">或</span>
                  <el-input
                    v-model="formValidate.commissionConfig.distributor.directRate"
                    placeholder="比例(%)"
                    clearable
                    :disabled="isDisabled"
                  />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="间接上级：">
                <div class="commission-pair">
                  <el-input
                    v-model="formValidate.commissionConfig.distributor.indirectAmount"
                    placeholder="金额(元)"
                    clearable
                    :disabled="isDisabled"
                  />
                  <span class="mx6">或</span>
                  <el-input
                    v-model="formValidate.commissionConfig.distributor.indirectRate"
                    placeholder="比例(%)"
                    clearable
                    :disabled="isDisabled"
                  />
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">区域代理</el-divider>
          <el-form-item label="代理返佣：">
            <el-radio-group v-model="formValidate.commissionConfig.agent.enabled" :disabled="isDisabled">
              <el-radio :label="null">跟随全局</el-radio>
              <el-radio :label="true">开启</el-radio>
              <el-radio :label="false">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="同总设置：">
            <el-radio-group v-model="formValidate.commissionConfig.agent.syncMode" :disabled="isDisabled">
              <el-radio :label="null">关闭（各级独立）</el-radio>
              <el-radio :label="true">开启（上级比例含下级份额）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="上级领下级：">
            <el-radio-group v-model="formValidate.commissionConfig.agent.superiorClaim" :disabled="isDisabled">
              <el-radio :label="null">否</el-radio>
              <el-radio :label="true">是（无下级时代理领取下级份额）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="省代：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.agent.provinceAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.agent.provinceRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="市代：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.agent.cityAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.agent.cityRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区代：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.agent.districtAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.agent.districtRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="平级推荐奖：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.agent.peerAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.agent.peerRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="越级推荐奖：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.agent.leapAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.agent.leapRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">订货商</el-divider>
          <el-form-item label="返差价：">
            <el-radio-group v-model="formValidate.commissionConfig.stock.diffEnabled" :disabled="isDisabled">
              <el-radio :label="null">跟随全局</el-radio>
              <el-radio :label="true">开启</el-radio>
              <el-radio :label="false">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="平级销售奖：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.stock.peerAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.stock.peerRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="越级销售奖：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.stock.leapAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.stock.leapRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">门店</el-divider>
          <el-alert
            title="门店佣金/奖励金当前仅保存配置，结算链路待门店佣金模型上线后生效。"
            type="warning"
            :closable="false"
            show-icon
            class="mb15"
          />
          <el-form-item label="门店佣金：">
            <el-radio-group v-model="formValidate.commissionConfig.store.brokerageEnabled" :disabled="isDisabled">
              <el-radio :label="null">跟随全局</el-radio>
              <el-radio :label="true">开启</el-radio>
              <el-radio :label="false">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="佣金：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.store.brokerageAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.store.brokerageRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="奖励金：">
                <div class="commission-pair">
                  <el-input v-model="formValidate.commissionConfig.store.bonusAmount" placeholder="金额(元)" clearable :disabled="isDisabled" />
                  <span class="mx6">或</span>
                  <el-input v-model="formValidate.commissionConfig.store.bonusRate" placeholder="比例(%)" clearable :disabled="isDisabled" />
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="奖励金开关：">
            <el-radio-group v-model="formValidate.commissionConfig.store.bonusEnabled" :disabled="isDisabled">
              <el-radio :label="null">跟随全局</el-radio>
              <el-radio :label="true">开启</el-radio>
              <el-radio :label="false">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button v-show="Number(currentTab) > 0" class="submission" @click="handleSubmitUp">上一步</el-button>
          <el-button
            v-show="Number(currentTab) < 4"
            class="submission"
            :class="Number(currentTab) == 0 ? 'onePrimary' : ''"
            @click="handleSubmitNest('formValidate')"
            >下一步</el-button
          >
          <el-button v-show="!isDisabled" type="primary" class="submission" @click="handleSubmit('formValidate')"
            >提交</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
    <CreatTemplates ref="addTemplates" @getList="getShippingList" />
  </div>
</template>

<script>
import Tinymce from '@/components/Tinymce/index';
import {
  templateListApi,
  productCreateApi,
  categoryApi,
  productDetailApi,
  productUpdateApi,
  guaranteeListApi,
} from '@/api/store';
import { productGroupSimpleListApi } from '@/api/productGroup';
import { marketingSendApi } from '@/api/marketing';
import { shippingTemplatesList } from '@/api/logistics';
import { goodDesignList } from '@/api/systemGroup';
import { arraysEqual } from '@/utils';
import { clearTreeData } from '@/utils/ZBKJIutil';
import CreatTemplates from '@/views/systemSetting/deliverGoods/freightSet/creatTemplates';
import creatAttr from '../components/creatAttr';
import Templates from '../../appSetting/wxAccount/wxTemplate/index';
import { Debounce } from '@/utils/validate';
import { copyConfigApi, copyProductApi } from '@/api/store';
const defaultObj = {
  image: '',
  sliderImages: [],
  videoLink: '',
  sliderImage: '',
  storeName: '',
  keyword: '',
  cateIds: [], // 商品分类id
  cateId: null, // 商品分类id传值
  unitName: '',
  sort: 0,
  giveIntegral: 0,
  ficti: 0,
  isShow: false,
  isBenefit: false,
  isNew: false,
  isGood: false,
  isHot: false,
  isBest: false,
  tempId: '',
  attrValue: [
    {
      image: '',
      price: void 0,
      cost: void 0,
      otPrice: void 0,
      stock: void 0,
      barCode: '',
      weight: void 0,
      volume: void 0,
    },
  ],
  attr: [],
  selectRule: '',
  isSub: false,
  content: '',
  specType: false,
  id: 0,
  couponIds: [],
  coupons: [],
  guaranteeIds: '', // 服务保障id字符串
  productGroupIds: [], // 商品分组id列表
  isStore: false, // 是否支持门店服务
  storeSelfPickup: false, // 门店-是否支持自提
  storeDelivery: false, // 门店-是否支持配送
  activity: ['默认', '秒杀', '砍价', '拼团'],
  commissionConfig: createDefaultCommissionConfig(),
};

function createDefaultCommissionConfig() {
  return {
    distributor: {
      enabled: null,
      directAmount: null,
      directRate: null,
      indirectAmount: null,
      indirectRate: null,
    },
    agent: {
      enabled: null,
      syncMode: null,
      superiorClaim: null,
      provinceAmount: null,
      provinceRate: null,
      cityAmount: null,
      cityRate: null,
      districtAmount: null,
      districtRate: null,
      peerAmount: null,
      peerRate: null,
      leapAmount: null,
      leapRate: null,
    },
    stock: {
      diffEnabled: null,
      peerAmount: null,
      peerRate: null,
      leapAmount: null,
      leapRate: null,
    },
    store: {
      brokerageEnabled: null,
      brokerageAmount: null,
      brokerageRate: null,
      bonusEnabled: null,
      bonusAmount: null,
      bonusRate: null,
    },
  };
}

function toNullableNumber(val) {
  if (val === null || val === undefined || val === '') {
    return null;
  }
  const n = Number(val);
  return Number.isFinite(n) ? n : null;
}

function mergeCommissionConfig(src) {
  const base = createDefaultCommissionConfig();
  if (!src || typeof src !== 'object') {
    return base;
  }
  ['distributor', 'agent', 'stock', 'store'].forEach((section) => {
    if (!src[section] || typeof src[section] !== 'object') {
      return;
    }
    Object.keys(base[section]).forEach((key) => {
      if (Object.prototype.hasOwnProperty.call(src[section], key)) {
        const v = src[section][key];
        if (key === 'enabled' || key === 'diffEnabled' || key === 'brokerageEnabled' || key === 'bonusEnabled'
          || key === 'syncMode' || key === 'superiorClaim') {
          base[section][key] = v === null || v === undefined || v === '' ? null : !!v;
        } else {
          base[section][key] = toNullableNumber(v);
        }
      }
    });
  });
  return base;
}

function normalizeCommissionConfig(cfg) {
  return mergeCommissionConfig(cfg);
}

const objTitle = {
  price: {
    title: '售价',
  },
  cost: {
    title: '成本价',
  },
  otPrice: {
    title: '原价',
  },
  stock: {
    title: '库存',
  },
  barCode: {
    title: '商品编号',
  },
  weight: {
    title: '重量（KG）',
  },
  volume: {
    title: '体积(m³)',
  },
};
export default {
  name: 'SortCreat',
  components: { Templates, CreatTemplates, Tinymce, creatAttr },
  data() {
    return {
      htmlKey: 0,
      isDisabled: this.$route.params.isDisabled === '1' ? true : false,
      activity: { 默认: 'red', 秒杀: 'blue', 砍价: 'green', 拼团: 'yellow' },
      props2: {
        children: 'child',
        label: 'name',
        value: 'id',
        multiple: true,
        emitPath: false,
      },
      checkboxGroup: [],
      recommend: [],
      tabs: [],
      fullscreenLoading: false,
      props: { multiple: true },
      active: 0,
      OneattrValue: [Object.assign({}, defaultObj.attrValue[0])], // 单规格
      ManyAttrValue: [Object.assign({}, defaultObj.attrValue[0])], // 多规格
      ruleList: [],
      merCateList: [], // 商品分类筛选
      shippingList: [], // 运费模板
      formThead: Object.assign({}, objTitle),
      formValidate: Object.assign({}, defaultObj, { commissionConfig: createDefaultCommissionConfig() }),
      formDynamics: {
        ruleName: '',
        ruleValue: [],
      },
      tempData: {
        page: 1,
        limit: 9999,
      },
      manyTabTit: {},
      manyTabDate: {},
      grid2: {
        xl: 24,
        lg: 24,
        md: 24,
        sm: 24,
        xs: 24,
      },
      // 规格数据
      formDynamic: {
        attrsName: '',
        attrsVal: '',
      },
      isBtn: false,
      manyFormValidate: [],
      currentTab: 0,
      isChoice: '',
      grid: {
        xl: 24,
        lg: 24,
        md: 24,
        sm: 24,
        xs: 24,
      },
      ruleValidate: {
        storeName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        cateIds: [{ required: true, message: '请选择商品分类', trigger: 'change', type: 'array', min: '1' }],
        keyword: [{ required: true, message: '请输入商品关键字', trigger: 'blur' }],
        unitName: [{ required: true, message: '请输入单位', trigger: 'blur' }],
        tempId: [{ required: true, message: '请选择运费模板', trigger: 'change' }],
        image: [{ required: true, message: '请上传商品图', trigger: 'change' }],
        sliderImages: [{ required: true, message: '请上传商品轮播图', type: 'array', trigger: 'change' }],
        specType: [{ required: true, message: '请选择商品规格', trigger: 'change' }],
      },
      attrInfo: {},
      tableFrom: {
        page: 1,
        limit: 9999,
        keywords: '',
      },
      tempRoute: {},
      keyNum: 0,
      isAttr: false,
      showAll: false,
      videoLink: '',
      copyConfig: {},
      url: '',
      guaranteeList: [], // 服务保障列表
      guaranteeIdsList: [], // 服务保障选择id列表
      productGroupOptions: [], // 商品分组选项
      // 批量添加数据
      oneFormBatch: [Object.assign({}, defaultObj.attrValue[0])],
    };
  },
  computed: {
    attrValue() {
      const obj = Object.assign({}, defaultObj.attrValue[0]);
      delete obj.image;
      return obj;
    },
  },
  watch: {
    // 'formValidate.attr': {
    //   handler: function (val) {
    //     // 如果是多规格商品
    //     if (this.formValidate.specType) {
    //       // 生成规格属性表头
    //       this.generateHeader(val);
    //       // 生成规格属性数据
    //       this.ManyAttrValue = this.generateAttr(val);
    //     }
    //     // if (this.formValidate.specType) this.watCh(val); //重要！！！
    //   },
    //   immediate: false,
    //   deep: true,
    // },
  },
  created() {
    this.tempRoute = Object.assign({}, this.$route);
    if (this.$route.params.id && this.formValidate.specType) {
      // this.$watch('formValidate.attr', this.watCh);
    }
    // 获取服务保障列表
    this.getGuaranteeList();
    this.loadProductGroups();
  },
  mounted() {
    this.getCopyConfig();
    this.formValidate.sliderImages = [];
    if (this.$route.params.id) {
      this.setTagsViewTitle();
      this.getInfo();
    }
    this.getCategorySelect();
    this.getShippingList();
    this.getGoodsType();
  },
  methods: {
    addProduct() {
      if (this.url) {
        this.loading = true;
        this.copyConfig.copyType == 1
          ? copyProductApi({ url: this.url })
              .then((res) => {
                let info = res;
                this.formValidate = {
                  image: this.$selfUtil.setDomain(info.image),
                  sliderImage: info.sliderImage,
                  storeName: info.storeName,
                  keyword: info.keyword,
                  cateIds: info.cateId ? info.cateId.split(',') : [], // 商品分类id
                  cateId: info.cateId, // 商品分类id传值
                  unitName: info.unitName,
                  sort: 0,
                  isShow: 0,
                  isBenefit: false,
                  isNew: false,
                  isGood: false,
                  isHot: false,
                  isBest: false,
                  tempId: info.tempId,
                  attrValue: info.attrValue,
                  attr: info.attr || [],
                  selectRule: info.selectRule,
                  isSub: false,
                  content: this.$selfUtil.replaceImgSrcHttps(info.content),
                  specType: info.attr.length ? true : false,
                  id: info.id,
                  giveIntegral: info.giveIntegral,
                  ficti: info.ficti,
                  activity: ['默认', '秒杀', '砍价', '拼团'],
                };
                if (info.specType) {
                  // 设置多规格商品属性数据
                  this.generateManyAttr();
                } else {
                  this.OneattrValue = info.attrValue;
                }
                if (info.isHot) this.checkboxGroup.push('isHot');
                if (info.isGood) this.checkboxGroup.push('isGood');
                if (info.isBenefit) this.checkboxGroup.push('isBenefit');
                if (info.isBest) this.checkboxGroup.push('isBest');
                if (info.isNew) this.checkboxGroup.push('isNew');
                let imgs = JSON.parse(info.sliderImage);
                let imgss = [];
                Object.keys(imgs).map((i) => {
                  imgss.push(this.$selfUtil.setDomain(imgs[i]));
                });
                this.formValidate.sliderImages = imgss;
                if (this.formValidate.attr.length) {
                  this.oneFormBatch[0].image = this.$selfUtil.setDomain(info.image);
                  this.oneFormBatch[0].brokerage = 0; // 设置采集商品的默认一级佣金
                  this.oneFormBatch[0].brokerageTwo = 0; // 设置采集商品的默认二级佣金
                  for (var i = 0; i < this.formValidate.attr.length; i++) {
                    this.formValidate.attr[i].attrValue = JSON.parse(this.formValidate.attr[i].attrValues);
                  }
                }
                this.loading = false;
              })
              .catch(() => {
                this.loading = false;
              })
          : importProductApi({ url: this.url, form: this.form })
              .then((res) => {
                this.formValidate = {
                  image: this.$selfUtil.setDomain(res.image),
                  sliderImage: res.sliderImage,
                  storeName: res.storeName,
                  keyword: res.keyword,
                  cateIds: res.cateId ? res.cateId.split(',') : [], // 商品分类id
                  cateId: res.cateId, // 商品分类id传值
                  unitName: res.unitName,
                  sort: 0,
                  isShow: 0,
                  isBenefit: false,
                  isNew: false,
                  isGood: false,
                  isHot: false,
                  isBest: false,
                  tempId: res.tempId,
                  attrValue: res.attrValue,
                  attr: res.attr || [],
                  selectRule: res.selectRule,
                  isSub: false,
                  content: res.content,
                  specType: res.attr.length ? true : false,
                  id: res.id,
                  giveIntegral: res.giveIntegral,
                  ficti: res.ficti,
                  activity: ['默认', '秒杀', '砍价', '拼团'],
                };
                if (info.specType) {
                  // 设置多规格商品属性数据
                  this.generateManyAttr();
                } else {
                  this.OneattrValue = info.attrValue;
                  // this.formValidate.attr = [] //单规格商品规格设置为空
                }
                let imgs = JSON.parse(res.sliderImage);
                let imgss = [];
                Object.keys(imgs).map((i) => {
                  imgss.push(this.$selfUtil.setDomain(imgs[i]));
                });
                this.formValidate.sliderImages = imgss;
                if (this.formValidate.attr.length) {
                  this.oneFormBatch[0].image = this.$selfUtil.setDomain(res.image);
                  for (var i = 0; i < this.formValidate.attr.length; i++) {
                    this.formValidate.attr[i].attrValue = JSON.parse(this.formValidate.attr[i].attrValues);
                  }
                }
                this.loading = false;
              })
              .catch(() => {
                this.loading = false;
              });
      } else {
        this.$message.warning('请输入链接地址！');
      }
    },
    getCopyConfig() {
      copyConfigApi().then((res) => {
        this.copyConfig = res;
      });
    },
    tabsHandleClick(tab, event) {
      this.currentTab = tab.name;
    },
    keyupEvent(key, val, index, num) {
      var re = /([0-9]+.[0-9]{2})[0-9]*/;
      switch (num) {
        case 1:
          this.oneFormBatch[index][key] =
            key === 'stock' ? parseInt(val) : this.$set(this.oneFormBatch[index], key, String(val).replace(re, '$1'));
          break;
        case 2:
          this.OneattrValue[index][key] =
            key === 'stock' ? parseInt(val) : this.$set(this.OneattrValue[index], key, String(val).replace(re, '$1'));
          break;
        default:
          this.ManyAttrValue[index][key] =
            key === 'stock' ? parseInt(val) : this.$set(this.ManyAttrValue[index], key, String(val).replace(re, '$1'));
          break;
      }
    },
    handleCloseCoupon(tag) {
      this.isAttr = true;
      this.formValidate.coupons.splice(this.formValidate.coupons.indexOf(tag), 1);
      this.formValidate.couponIds.splice(this.formValidate.couponIds.indexOf(tag.id), 1);
    },
    addCoupon() {
      const _this = this;
      this.$modalCoupon(
        'wu',
        (this.keyNum += 1),
        this.formValidate.coupons,
        function (row) {
          _this.formValidate.couponIds = [];
          _this.formValidate.coupons = row;
          row.map((item) => {
            _this.formValidate.couponIds.push(item.id);
          });
        },
        '',
      );
    },
    setTagsViewTitle() {
      const title = this.isDisabled ? '商品详情' : '编辑商品';
      const route = Object.assign({}, this.tempRoute, { title: `${title}-${this.$route.params.id}` });
      this.$store.dispatch('tagsView/updateVisitedView', route);
    },
    onChangeGroup() {
      this.checkboxGroup.includes('isGood') ? (this.formValidate.isGood = true) : (this.formValidate.isGood = false);
      this.checkboxGroup.includes('isBenefit')
        ? (this.formValidate.isBenefit = true)
        : (this.formValidate.isBenefit = false);
      this.checkboxGroup.includes('isBest') ? (this.formValidate.isBest = true) : (this.formValidate.isBest = false);
      this.checkboxGroup.includes('isNew') ? (this.formValidate.isNew = true) : (this.formValidate.isNew = false);
      this.checkboxGroup.includes('isHot') ? (this.formValidate.isHot = true) : (this.formValidate.isHot = false);
    },
    // 运费模板
    addTem() {
      this.$refs.addTemplates.dialogVisible = true;
      this.$refs.addTemplates.getCityList();
    },
    // 商品分类；
    getCategorySelect() {
      categoryApi({ status: -1, type: 1 }).then((res) => {
        this.merCateList = this.addDisabled(res);
      });
    },
    //限制商品分类只能选择开启的
    addDisabled(dropdownList) {
      const list = [];
      try {
        dropdownList.forEach((e, index) => {
          let e_new = {
            id: e.id,
            name: e.name,
            level: e.level,
            pid: e.pid,
            sort: e.sort,
            status: e.status,
          };
          if (!e.status) {
            e_new = { ...e_new, disabled: true };
          }
          if (e.child) {
            const childList = this.addDisabled(e.child);
            e_new = { ...e_new, child: childList };
          }
          list.push(e_new);
        });
      } catch (error) {
        console.log(error);
        return [];
      }
      return list;
    },
    filerMerCateList(treeData) {
      return treeData.map((item) => {
        if (!item.child) {
          item.disabled = true;
        }
        item.label = item.name;
        return item;
      });
    },
    // 获取商品属性模板；
    productGetRule() {
      templateListApi(this.tableFrom).then((res) => {
        const list = res.list;
        for (var i = 0; i < list.length; i++) {
          list[i].ruleValue = JSON.parse(list[i].ruleValue);
        }
        this.ruleList = list;
      });
    },
    // 运费模板；
    getShippingList() {
      shippingTemplatesList(this.tempData).then((res) => {
        this.shippingList = res.list;
      });
    },
    // 详情
    getInfo() {
      this.fullscreenLoading = true;
      productDetailApi(this.$route.params.id)
        .then(async (res) => {
          // this.isAttr = true;
          let info = res;
          this.formValidate = {
            image: this.$selfUtil.setDomain(info.image),
            sliderImage: info.sliderImage,
            sliderImages: JSON.parse(info.sliderImage),
            storeName: info.storeName,
            keyword: info.keyword,
            cateIds: info.cateId.split(','), // 商品分类id
            cateId: info.cateId, // 商品分类id传值
            unitName: info.unitName,
            sort: info.sort,
            isShow: info.isShow,
            isBenefit: info.isBenefit,
            isNew: info.isNew,
            isGood: info.isGood,
            isHot: info.isHot,
            isBest: info.isBest,
            tempId: info.tempId,
            attr: info.attr || [],
            attrValue: info.attrValue || [],
            selectRule: info.selectRule,
            isSub: info.isSub,
            content: info.content ? this.$selfUtil.replaceImgSrcHttps(info.content) : '',
            specType: info.specType,
            id: info.id,
            giveIntegral: info.giveIntegral,
            ficti: info.ficti,
            coupons: info.coupons,
            couponIds: info.couponIds,
            isStore: !!info.isStore,
            storeSelfPickup: !!info.storeSelfPickup,
            storeDelivery: !!info.storeDelivery,
            activity: info.activity ? info.activity : ['默认', '秒杀', '砍价', '拼团'],
            productGroupIds: info.productGroupIds || [],
            commissionConfig: mergeCommissionConfig(info.commissionConfig),
          };
          // 获取服务保障被选id列表
          this.getGuranteeIdsList(info.guaranteeList);
          marketingSendApi({ type: 3 }).then((res) => {
            if (this.formValidate.couponIds !== null) {
              let ids = this.formValidate.couponIds.toString();
              let arr = res.list;
              let obj = {};
              for (let i in arr) {
                obj[arr[i].id] = arr[i];
              }
              let strArr = ids.split(',');
              let newArr = [];
              for (let item of strArr) {
                if (obj[item]) {
                  newArr.push(obj[item]);
                }
              }
              this.$set(this.formValidate, 'coupons', newArr); //在编辑回显时，让返回数据中的优惠券id，通过接口匹配显示,
            }
          });
          let imgs = JSON.parse(info.sliderImage);
          let imgss = [];
          Object.keys(imgs).map((i) => {
            imgss.push(this.$selfUtil.setDomain(imgs[i]));
          });
          this.formValidate.sliderImages = [...imgss];
          if (this.getFileType(this.formValidate.sliderImages[0]) == 'video') {
            //如果返回数据轮播图的第一张是视频，就将其赋值给videoLink做渲染，同时将其在轮播图中删除
            this.$set(this.formValidate, 'videoLink', this.formValidate.sliderImages[0]);
            this.formValidate.sliderImages.splice(0, 1);
          }
          if (info.isHot) this.checkboxGroup.push('isHot');
          if (info.isGood) this.checkboxGroup.push('isGood');
          if (info.isBenefit) this.checkboxGroup.push('isBenefit');
          if (info.isBest) this.checkboxGroup.push('isBest');
          if (info.isNew) this.checkboxGroup.push('isNew');
          this.productGetRule();
          if (info.specType) {
            // 设置多规格商品属性数据
            this.generateManyAttr();
          } else {
            this.OneattrValue =
              info.attrValue && info.attrValue.length
                ? info.attrValue
                : [Object.assign({}, defaultObj.attrValue[0])];
            // this.formValidate.attr = [] //单规格商品规格设置为空
          }
          this.fullscreenLoading = false;
        })
        .catch((res) => {
          this.fullscreenLoading = false;
          this.$message.error(res.message);
        });
    },
    handleRemove(i) {
      this.formValidate.sliderImages.splice(i, 1);
    },
    // 点击商品图
    modalPicTap(tit, num, i, status) {
      const _this = this;
      if (_this.isDisabled) return;
      this.$modalUpload(
        function (img) {
          if (tit === '1' && !num) {
            _this.formValidate.image = img[0].sattDir;
            _this.OneattrValue[0].image = img[0].sattDir;
          }
          if (tit === '2' && !num) {
            if (img.length > 10) return this.$message.warning('最多选择10张图片！');
            if (img.length + _this.formValidate.sliderImages.length > 10)
              return this.$message.warning('最多选择10张图片！');
            img.map((item) => {
              _this.formValidate.sliderImages.push(item.sattDir);
            });
          }
          if (tit === '3' && status === 'video') {
            let videoInfo = img[0];
            if (videoInfo.attType !== 'video/mp4') {
              this.$message.warning('请重新选择视频！');
            } else {
              _this.$set(_this.formValidate, 'videoLink', videoInfo.sattDir);
            }
          }
          if (tit === '1' && num === 'dan') {
            _this.OneattrValue[0].image = img[0].sattDir;
          }
          if (tit === '1' && num === 'duo') {
            _this.ManyAttrValue[i].image = img[0].sattDir;
          }
          if (tit === '1' && num === 'pi') {
            _this.oneFormBatch[0].image = img[0].sattDir;
          }
        },
        tit,
        'content',
      );
    },
    handleSubmitUp() {
      if (this.currentTab-- < 0) this.currentTab = 0;
      this.currentTab = this.currentTab.toString();
    },
    handleSubmitNest(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          if (this.currentTab++ > 3) this.currentTab = 0;
          this.currentTab = this.currentTab.toString();
        } else {
          if (
            !this.formValidate.store_name ||
            !this.formValidate.cate_id ||
            !this.formValidate.keyword ||
            !this.formValidate.unit_name ||
            !this.formValidate.store_info ||
            !this.formValidate.image ||
            !this.formValidate.slider_image
          ) {
            this.$message.warning('请填写完整商品信息！');
          }
        }
      });
    },
    //提交接口数据更新
    getFromData() {
      if (this.formValidate.specType && this.formValidate.attr.length < 1)
        return this.$message.warning('请填写多规格属性！');
      this.formValidate.cateId = this.formValidate.cateIds.join(',');
      if (this.formValidate.videoLink) {
        //如果有视频主图，将视频链接插入到轮播图第一的位置
        this.formValidate.sliderImages.unshift(this.formValidate.videoLink);
      }
      this.formValidate.sliderImage = JSON.stringify(this.formValidate.sliderImages);
      if (this.formValidate.specType) {
        this.formValidate.attrValue = this.ManyAttrValue.slice(1);
        this.formValidate.attr = this.formValidate.attr.map((item) => {
          return {
            attrName: item.attrName,
            id: item.id,
            attrValues: item.optionList.map((val) => val.value).join(','),
            isShowImage: item.isShowImage || false,
            optionList: item.optionList || [{ value: '默认' }],
          };
        });
        if (typeof (this.formValidate.attrValue[0].attrValue) == 'object') {
          this.formValidate.attrValue.forEach((item) => {
            item.attrValue = JSON.stringify(item.attrValue);
          });
        }
        // 如果不是采集商品
        if (!this.$route.params.isCopy) {
          for (var i = 0; i < this.formValidate.attrValue.length; i++) {
            this.$set(this.formValidate.attrValue[i], 'id', 0);
            this.$set(this.formValidate.attrValue[i], 'productId', 0);
            let attrValues = this.formValidate.attrValue[i].attrValue;
            // this.$set(this.formValidate.attrValue[i], 'attrValue', JSON.stringify(attrValues));
            delete this.formValidate.attrValue[i].value0;
          }
        }
      } else {
        const oldAttrId =
          this.formValidate.attr && this.formValidate.attr.length ? this.formValidate.attr[0].id : 0;
        this.formValidate.attr = [
          {
            attrName: '规格',
            attrValues: '默认',
            id: this.$route.params.id ? oldAttrId : 0,
            isShowImage: false,
            optionList: [{ value: '默认' }],
          },
        ];
        if (!this.OneattrValue || !this.OneattrValue.length) {
          this.OneattrValue = [Object.assign({}, defaultObj.attrValue[0])];
        }
        this.OneattrValue.map((item) => {
          this.$set(item, 'attrValue', JSON.stringify({ 规格: '默认' }));
          // 如果佣金设置为默认
          if (!this.formValidate.isSub) {
            this.$set(item, 'brokerage', 0);
            this.$set(item, 'brokerageTwo', 0);
          }
          //this.$set(item, 'productId', 0);
        });
        this.formValidate.attrValue = this.OneattrValue;
      }
    },
    // 提交
    handleSubmit: Debounce(function (name) {
      this.onChangeGroup();
      this.getFromData();
      this.formValidate.commissionConfig = normalizeCommissionConfig(this.formValidate.commissionConfig);
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.fullscreenLoading = true;
          this.$route.params.id
            ? productUpdateApi(this.formValidate)
                .then(async (res) => {
                  this.$message.success('编辑成功');
                  setTimeout(() => {
                    this.$router.push({ path: '/store/index' });
                  }, 500);
                  this.fullscreenLoading = false;
                })
                .catch((res) => {
                  this.fullscreenLoading = false;
                  this.restoreData();
                  if (this.formValidate.specType) this.ManyAttrValue = this.formValidate.attrValue;
                })
            : productCreateApi(this.formValidate)
                .then(async (res) => {
                  this.$message.success('新增成功');
                  setTimeout(() => {
                    this.$router.push({ path: '/store/index' });
                  }, 500);
                  this.fullscreenLoading = false;
                })
                .catch((res) => {
                  this.fullscreenLoading = false;
                  this.restoreData();
                });
        } else {
          if (
            !this.formValidate.storeName ||
            !this.formValidate.cateId ||
            !this.formValidate.keyword ||
            !this.formValidate.unitName ||
            !this.formValidate.image ||
            !this.formValidate.sliderImages
          ) {
            this.$message.warning('请填写完整商品信息！');
          }
        }
      });
    }),
    // 提交失败之后恢复数据
    restoreData() {
      for (var i = 0; i < this.formValidate.attrValue.length; i++) {
        let attrValues = this.formValidate.attrValue[i].attrValue;
        this.$set(this.formValidate.attrValue[i], 'attrValue', JSON.parse(attrValues));
      }
    },
    // 表单验证
    validate(prop, status, error) {
      if (status === false) {
        this.$message.warning(error);
      }
    },
    // 移动
    handleDragStart(e, item) {
      if (!this.isDisabled) this.dragging = item;
    },
    handleDragEnd(e, item) {
      if (!this.isDisabled) this.dragging = null;
    },
    handleDragOver(e) {
      if (!this.isDisabled) e.dataTransfer.dropEffect = 'move';
    },
    handleDragEnter(e, item) {
      if (!this.isDisabled) {
        e.dataTransfer.effectAllowed = 'move';
        if (item === this.dragging) {
          return;
        }
        const newItems = [...this.formValidate.sliderImages];
        const src = newItems.indexOf(this.dragging);
        const dst = newItems.indexOf(item);
        newItems.splice(dst, 0, ...newItems.splice(src, 1));
        this.formValidate.sliderImages = newItems;
      }
    },
    handleDragEnterFont(e, item) {
      if (!this.isDisabled) {
        e.dataTransfer.effectAllowed = 'move';
        if (item === this.dragging) {
          return;
        }
        const newItems = [...this.formValidate.activity];
        const src = newItems.indexOf(this.dragging);
        const dst = newItems.indexOf(item);
        newItems.splice(dst, 0, ...newItems.splice(src, 1));
        this.formValidate.activity = newItems;
      }
    },
    getGoodsType() {
      /** 让商品推荐列表的name属性与页面设置tab的name匹配**/
      goodDesignList({ gid: 70 }).then((response) => {
        let list = response.list;
        let arr = [],
          arr1 = [];
        const listArr = [{ name: '是否热卖', value: 'isGood', type: '5' }];
        let typeLists = [
          { name: '', value: 'isHot', type: '2' }, //热门榜单
          { name: '', value: 'isBenefit', type: '4' }, //促销单品
          { name: '', value: 'isBest', type: '1' }, //精品推荐
          { name: '', value: 'isNew', type: '3' },
        ]; //首发新品
        list.forEach((item) => {
          let obj = {};
          obj.value = JSON.parse(item.value);
          obj.id = item.id;
          obj.gid = item.gid;
          obj.status = item.status;
          arr.push(obj);
        });
        arr.forEach((item1) => {
          let obj1 = {};
          obj1.name = item1.value.fields[1].value;
          obj1.status = item1.status;
          obj1.type = item1.value.fields[3].value;
          arr1.push(obj1);
        });
        typeLists.forEach((item) => {
          arr1.forEach((item1) => {
            if (item.type == item1.type) {
              listArr.push({
                name: item1.name,
                value: item.value,
                type: item.type,
              });
            }
          });
        });
        this.recommend = listArr;
      });
    },
    // 删除视频；
    delVideo() {
      let that = this;
      that.$set(that.formValidate, 'videoLink', '');
    },
    zh_uploadFile() {
      if (this.videoLink) {
        this.$set(this.formValidate, 'videoLink', this.videoLink);
      }
    },
    getFileType(fileName) {
      // 后缀获取
      let suffix = '';
      // 获取类型结果
      let result = '';
      try {
        const flieArr = fileName.split('.');
        suffix = flieArr[flieArr.length - 1];
      } catch (err) {
        suffix = '';
      }
      // fileName无后缀返回 false
      if (!suffix) {
        return false;
      }
      suffix = suffix.toLocaleLowerCase();
      // 图片格式
      const imglist = ['png', 'jpg', 'jpeg', 'bmp', 'gif'];
      // 进行图片匹配
      result = imglist.find((item) => item === suffix);
      if (result) {
        return 'image';
      }
      // 匹配 视频
      const videolist = ['mp4', 'm2v', 'mkv', 'rmvb', 'wmv', 'avi', 'flv', 'mov', 'm4v'];
      result = videolist.find((item) => item === suffix);
      if (result) {
        return 'video';
      }
      // 其他 文件类型
      return 'other';
    },
    // 获取服务保障列表
    getGuaranteeList() {
      guaranteeListApi({
        isShow: 1,
      })
        .then((res) => {
          this.guaranteeList = res;
        })
        .catch((err) => {
          this.$message.error(err.message);
        });
    },
    loadProductGroups() {
      productGroupSimpleListApi()
        .then((res) => {
          this.productGroupOptions = Array.isArray(res) ? res : res.list || [];
        })
        .catch(() => {
          this.productGroupOptions = [];
        });
    },
    // 获取被选服务保障id列表
    getGuranteeIdsList(list) {
      if (list) {
        this.guaranteeIdsList = list.map((item) => {
          return item.id;
        });
      }
    },
    // 修改服务保障
    updateGuaranteeIds(list) {
      this.formValidate.guaranteeIds = list.join(',');
    },
    // 回调规格生成表格数据 多规格
    changeManyAttrValue(e) {
      // rows数组第一项 新增默认数据 oneFormBatch
      this.ManyAttrValue = e;
    },
    //批量清空规格中的批量数据
    handleBatchDel() {
      this.oneFormBatch = [
        {
          image: '',
          price: void 0,
          cost: void 0,
          otPrice: void 0,
          stock: void 0,
          weight: void 0,
          volume: void 0,
          brokerage: void 0,
          brokerageTwo: void 0,
          barCode: '',
        },
      ];
    },
    // 设置多规格商品的表格数据
    generateManyAttr() {
      // 多规格属性赋值（导库后可能缺规格，需兜底避免页面卡死）
      const list = Array.isArray(this.formValidate.attrValue) ? this.formValidate.attrValue : [];
      this.formValidate.attr = Array.isArray(this.formValidate.attr) ? this.formValidate.attr : [];
      this.ManyAttrValue = list.map((val) => {
        const row = Object.assign({}, val);
        row.image = this.$selfUtil.setDomain(row.image);
        if (typeof row.attrValue === 'string') {
          try {
            row.attrValue = JSON.parse(row.attrValue || '{}');
          } catch (e) {
            row.attrValue = {};
          }
        } else if (!row.attrValue || typeof row.attrValue !== 'object') {
          row.attrValue = {};
        }
        return row;
      });
      this.ManyAttrValue = [...this.oneFormBatch, ...this.ManyAttrValue];
      // 此处手动实现后台原本value0 value1的逻辑
      this.formValidate.attrValue = list;
      this.formValidate.attrValue.forEach((item) => {
        if (typeof item.attrValue === 'string') {
          try {
            item.attrValue = JSON.parse(item.attrValue || '{}');
          } catch (e) {
            item.attrValue = {};
          }
        }
        if (!item.attrValue || typeof item.attrValue !== 'object') {
          item.attrValue = {};
        }
        for (let attrValueKey in item.attrValue) {
          item[attrValueKey] = item.attrValue[attrValueKey];
        }
      });
    },
  },
};
</script>
<style scoped lang="scss">
.upLoadPicBox {
  ::v-deep.el-alert {
    padding: 0 !important;
  }
}

.disLabel {
  ::v-deepel-form-item__label {
    margin-left: 36px !important;
  }
}

.disLabelmoren {
  ::v-deepel-form-item__label {
    margin-left: 120px !important;
  }
}

.priamry_border {
  border: 1px solid #1890ff;
  color: #1890ff;
}

.color-item {
  height: 30px;
  line-height: 30px;
  padding: 0 10px;
  color: #fff;
  margin-right: 10px;
}

.color-list .color-item.blue {
  background-color: #1e9fff;
}

.color-list .color-item.yellow {
  background-color: rgb(254, 185, 0);
}

.color-list .color-item.green {
  background-color: #009688;
}

.color-list .color-item.red {
  background-color: #ed4014;
}

.proCoupon {
  ::v-deepel-form-item__content {
    margin-top: 5px;
  }
}

.tabPic {
  width: 40px !important;
  height: 40px !important;

  img {
    width: 100%;
    height: 100%;
  }
}

.noLeft {
  ::v-deepel-form-item__content {
    margin-left: 0 !important;
  }
}

.tabNumWidth {
  ::v-deepel-input-number--medium {
    width: 121px !important;
  }

  ::v-deepel-input-number__increase {
    width: 20px !important;
    font-size: 12px !important;
  }

  ::v-deepel-input-number__decrease {
    width: 20px !important;
    font-size: 12px !important;
  }

  ::v-deepel-input-number--medium .el-input__inner {
    padding-left: 25px !important;
    padding-right: 25px !important;
  }

  ::v-deep thead {
    line-height: normal !important;
  }

  ::v-deep .el-table .cell {
    line-height: normal !important;
  }
}

.selWidth {
  width: 100%;
}

.selWidthd {
  width: 300px;
}

.button-new-tag {
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}

.pictrue {
  width: 60px;
  height: 60px;
  border: 1px dotted rgba(0, 0, 0, 0.1);
  margin-right: 10px;
  position: relative;
  cursor: pointer;

  img,
  .image {
    width: 100%;
    height: 100%;
  }

  video {
    width: 100%;
    height: 100%;
  }
}

.btndel {
  position: absolute;
  z-index: 1;
  width: 20px !important;
  height: 20px !important;
  left: 43px;
  top: 1px;
}

.labeltop {
  ::v-deepel-form-item__label {
    float: none !important;
    display: inline-block !important;
    width: auto !important;
  }
}

.iview-video-style {
  width: 300px;
  height: 180px;
  border-radius: 10px;
  background-color: #707070;
  margin: 0 120px 20px;
  position: relative;
  overflow: hidden;
}

.iview-video-style .iconv {
  color: #fff;
  line-height: 180px;
  width: 50px;
  height: 50px;
  display: inherit;
  font-size: 26px;
  position: absolute;
  top: -74px;
  left: 50%;
  margin-left: -25px;
}

.iview-video-style .mark {
  position: absolute;
  width: 100%;
  height: 30px;
  top: 0;
  background-color: rgba(0, 0, 0, 0.5);
  text-align: center;
}

::v-deep .el-tabs__nav-scroll {
  margin-top: -20px;
}
.selWidth100 {
  width: 100%;
}
.tips-bottom {
  margin-top: 10px;
}
.onePrimary {
  margin-left: 0 !important;
}
.formValidate.mt20 {
  padding: 0 30px;
}
.mr16 {
  margin-right: 16px;
}
::v-deep .el-radio__label {
  font-size: 12px !important;
}
::v-deep .el-radio__input {
  font-size: 12px !important;
}
.inputWid {
  width: 500px;
}
.noLeft {
  margin-left: -60px;
}
/* 门店服务开关区 */
.store-service {
  display: flex;
  align-items: center;
}
.store-service-switch-text {
  margin-left: 10px;
  font-size: 13px;
  color: #303133;
}
.store-service-sub {
  display: flex;
  align-items: center;
  margin: 8px 0 4px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  width: fit-content;
}
.store-service-label {
  font-size: 13px;
  color: #606266;
}
.store-service-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}
.commission-setting {
  padding: 0 10px 20px;
}
.commission-pair {
  display: flex;
  align-items: center;
}
.commission-pair .el-input {
  width: 140px;
}
.mx6 {
  margin: 0 6px;
  color: #909399;
  flex-shrink: 0;
}
.mb15 {
  margin-bottom: 15px;
}
</style>
