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
              <el-form-item label="送积分：">
                <el-switch
                  v-model="formValidate.isGiveIntegral"
                  :disabled="isDisabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <span class="form-tip" style="margin-left: 8px">关闭后本商品不赠送积分</span>
              </el-form-item>
            </el-col>
            <el-col v-bind="grid">
              <el-form-item label="积分：">
                <el-input-number
                  controls-position="right"
                  v-model="formValidate.giveIntegral"
                  :min="0"
                  placeholder="请输入赠送积分"
                  :disabled="isDisabled || !formValidate.isGiveIntegral"
                />
                <div class="form-tip" style="line-height: 1.5; margin-top: 4px">
                  大于 0 时按商品积分赠送，不再叠加公共「消费送积分」；为 0 时走公共设置
                </div>
              </el-form-item>
            </el-col>
            <el-col v-bind="grid">
              <el-form-item label="积分抵扣：">
                <el-input-number
                  controls-position="right"
                  v-model="formValidate.integralDeduct"
                  :min="0"
                  :precision="0"
                  placeholder="最多可用积分数"
                  :disabled="isDisabled"
                />
                <div class="form-tip" style="line-height: 1.5; margin-top: 4px">
                  每件最多可用积分数。填正数开启上限；填 0 表示：若其它商品已设上限则本商品不可抵扣，若全部为 0 则按整单旧逻辑抵扣
                </div>
              </el-form-item>
            </el-col>
            <el-col v-bind="grid">
              <el-form-item label="抵扣参与分佣：">
                <el-switch
                  v-model="formValidate.isIntegralDeductBrokerage"
                  :disabled="isDisabled"
                  active-text="参与"
                  inactive-text="不参与"
                />
                <div class="form-tip" style="line-height: 1.5; margin-top: 4px">
                  关闭后，本商品积分抵扣掉的金额不计入分销/代理/团队等分佣基数
                </div>
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
        <div v-show="currentTab == 4" class="cm">
          <!-- 规则速览 + 分组状态 -->
          <div class="cm-brief">
            <ul class="cm-brief__rules">
              <li><span class="cm-brief__k">留空</span>跟随全局 / 等级配置（门店服务费留空则跟随门店默认）</li>
              <li><span class="cm-brief__k">填 0</span>该商品无此项奖励 / 服务费</li>
              <li><span class="cm-brief__k">金额 + 比例</span>同时填写时金额优先</li>
            </ul>
            <div class="cm-brief__stats">
              <div
                v-for="item in commissionSummary"
                :key="item.key"
                class="cm-stat"
                :class="[`cm-stat--${item.key}`, `is-${item.state}`]"
                @click="scrollToCommission(item.key)"
              >
                <div class="cm-stat__row">
                  <span class="cm-stat__name">{{ item.name }}</span>
                  <span class="cm-stat__count">{{ item.count }}<i>项</i></span>
                </div>
                <span class="cm-stat__state"><i class="cm-stat__dot"></i>{{ item.stateText }}</span>
              </div>
            </div>
          </div>

          <!-- ==================== 1. 分销商返佣 ==================== -->
          <section v-if="moduleSwitches.spread" class="cm-sec cm-sec--dist">
            <header class="cm-sec__hd">
              <div class="cm-sec__title">
                <span class="cm-sec__dot"></span>
                <h4>分销商返佣</h4>
                <span class="cm-tag" :class="enabledClass(formValidate.commissionConfig.distributor.enabled)">{{
                  enabledText(formValidate.commissionConfig.distributor.enabled)
                }}</span>
                <span class="cm-sec__count">已配置 {{ distributorValueCount }} 项</span>
              </div>
            </header>
            <div class="cm-sec__bd">
              <p v-if="isSectionOff('distributor') && distributorValueCount" class="cm-notice cm-notice--warn">
                <i class="el-icon-warning-outline"></i>当前为「停用」，下方数值仍会保存，但不参与本商品结算
              </p>
              <div class="cm-hero">
                <div class="cm-hero__label">
                  分销返佣
                  <span class="cm-hero__sub">总开关</span>
                </div>
                <div class="cm-hero__ctrl">
                  <el-radio-group
                    v-model="formValidate.commissionConfig.distributor.enabled"
                    :disabled="isDisabled"
                    class="cm-radios"
                  >
                    <el-radio :label="null">跟随全局</el-radio>
                    <el-radio :label="true">启用</el-radio>
                    <el-radio :label="false">停用</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="cm-grid">
                <div class="cm-field">
                  <div class="cm-field__label">
                    一级返佣<span class="cm-field__sub">直属上级</span>
                    <el-tooltip content="推广者的直接上级获得的佣金" placement="top">
                      <i class="el-icon-question cm-field__q" />
                    </el-tooltip>
                  </div>
                  <div class="cm-field__pair">
                    <el-input
                      v-model="formValidate.commissionConfig.distributor.directAmount"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('distributor.directAmount') === 'error' }"
                    >
                      <template slot="append">元</template>
                    </el-input>
                    <span class="cm-field__or">或</span>
                    <el-input
                      v-model="formValidate.commissionConfig.distributor.directRate"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('distributor.directRate') === 'error' }"
                    >
                      <template slot="append">%</template>
                    </el-input>
                  </div>
                  <p v-if="fieldMsg('distributor.directAmount').text" class="cm-field__msg" :class="fieldMsg('distributor.directAmount').cls">
                    <i v-if="fieldMsg('distributor.directAmount').icon" :class="fieldMsg('distributor.directAmount').icon"></i>
                    {{ fieldMsg('distributor.directAmount').text }}
                  </p>
                </div>
                <div class="cm-field">
                  <div class="cm-field__label">
                    二级返佣<span class="cm-field__sub">间接上级</span>
                    <el-tooltip content="推广者的上二级（上级的上级）获得的佣金" placement="top">
                      <i class="el-icon-question cm-field__q" />
                    </el-tooltip>
                  </div>
                  <div class="cm-field__pair">
                    <el-input
                      v-model="formValidate.commissionConfig.distributor.indirectAmount"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('distributor.indirectAmount') === 'error' }"
                    >
                      <template slot="append">元</template>
                    </el-input>
                    <span class="cm-field__or">或</span>
                    <el-input
                      v-model="formValidate.commissionConfig.distributor.indirectRate"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('distributor.indirectRate') === 'error' }"
                    >
                      <template slot="append">%</template>
                    </el-input>
                  </div>
                  <p v-if="fieldMsg('distributor.indirectAmount').text" class="cm-field__msg" :class="fieldMsg('distributor.indirectAmount').cls">
                    <i
                      v-if="fieldMsg('distributor.indirectAmount').icon"
                      :class="fieldMsg('distributor.indirectAmount').icon"
                    ></i>
                    {{ fieldMsg('distributor.indirectAmount').text }}
                  </p>
                </div>
              </div>

              <div class="cm-sub" :class="{ 'is-open': levelPanel.distributor }">
                <div class="cm-sub__hd" @click="toggleLevelPanel('distributor')">
                  <i :class="levelPanel.distributor ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
                  <span class="cm-sub__t">按分销商等级单独设置</span>
                  <span v-if="levelConfiguredCount('distributor')" class="cm-stat__count">{{
                    levelConfiguredCount('distributor')
                  }}<i>项</i></span>
                  <span class="cm-sub__n">优先级：等级覆盖 &gt; 上方统一设置 &gt; 全局</span>
                  <el-button type="text" size="mini">{{ levelPanel.distributor ? '收起' : '展开' }}</el-button>
                </div>
                <div v-show="levelPanel.distributor" class="cm-sub__bd">
                  <div class="list-table cm-lv cm-lv--dist">
                    <div class="list-head">
                      <div class="list-head__cell">等级</div>
                      <div class="list-head__cell">自购返佣 · 金额 / 比例</div>
                      <div class="list-head__cell">一级返佣 · 金额 / 比例</div>
                      <div class="list-head__cell">二级返佣 · 金额 / 比例</div>
                      <div class="list-head__cell list-head__cell--right">操作</div>
                    </div>
                    <div class="list-body">
                      <div v-for="row in distributorLevelOptions" :key="`dl-${row.id}`" class="list-row">
                        <div class="list-cell">
                          <span class="status-tag status-tag--primary">{{ row.name }}</span>
                        </div>
                        <div class="list-cell">
                          <div
                            class="cm-field__pair cm-pair--sm"
                            :class="pairIssueClass(`distributor.levels.${row.id}.selfAmount`, `distributor.levels.${row.id}.selfRate`)"
                          >
                            <el-input
                              v-model="levelRow('distributor', row.id).selfAmount"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">元</template>
                            </el-input>
                            <span class="cm-field__slash">/</span>
                            <el-input
                              v-model="levelRow('distributor', row.id).selfRate"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">%</template>
                            </el-input>
                          </div>
                        </div>
                        <div class="list-cell">
                          <div
                            class="cm-field__pair cm-pair--sm"
                            :class="pairIssueClass(`distributor.levels.${row.id}.oneAmount`, `distributor.levels.${row.id}.oneRate`)"
                          >
                            <el-input
                              v-model="levelRow('distributor', row.id).oneAmount"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">元</template>
                            </el-input>
                            <span class="cm-field__slash">/</span>
                            <el-input
                              v-model="levelRow('distributor', row.id).oneRate"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">%</template>
                            </el-input>
                          </div>
                        </div>
                        <div class="list-cell">
                          <div
                            class="cm-field__pair cm-pair--sm"
                            :class="pairIssueClass(`distributor.levels.${row.id}.twoAmount`, `distributor.levels.${row.id}.twoRate`)"
                          >
                            <el-input
                              v-model="levelRow('distributor', row.id).twoAmount"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">元</template>
                            </el-input>
                            <span class="cm-field__slash">/</span>
                            <el-input
                              v-model="levelRow('distributor', row.id).twoRate"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">%</template>
                            </el-input>
                          </div>
                        </div>
                        <div class="list-cell cm-lv__op">
                          <el-button type="text" size="mini" :disabled="isDisabled" @click="clearLevelRow('distributor', row)"
                            >清空</el-button
                          >
                        </div>
                      </div>
                      <div v-if="!distributorLevelOptions.length" class="list-empty">
                        <i class="el-icon-folder-opened"></i>
                        <p>暂无分销商等级，请先到 运营 → 分销商等级 创建</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- ==================== 2. 区域代理 ==================== -->
          <section v-if="moduleSwitches.daili" class="cm-sec cm-sec--agent">
            <header class="cm-sec__hd">
              <div class="cm-sec__title">
                <span class="cm-sec__dot"></span>
                <h4>区域代理佣金</h4>
                <span class="cm-tag" :class="enabledClass(formValidate.commissionConfig.agent.enabled)">{{
                  enabledText(formValidate.commissionConfig.agent.enabled)
                }}</span>
                <span class="cm-sec__count">已配置 {{ agentValueCount }} 项</span>
              </div>
            </header>
            <div class="cm-sec__bd">
              <p v-if="isSectionOff('agent') && agentValueCount" class="cm-notice cm-notice--warn">
                <i class="el-icon-warning-outline"></i>当前为「停用」，下方数值仍会保存，但不参与本商品结算
              </p>
              <div class="cm-hero">
                <div class="cm-hero__label">
                  代理商返佣
                  <span class="cm-hero__sub">总开关</span>
                </div>
                <div class="cm-hero__ctrl">
                  <el-radio-group
                    v-model="formValidate.commissionConfig.agent.enabled"
                    :disabled="isDisabled"
                    class="cm-radios"
                  >
                    <el-radio :label="null">跟随全局</el-radio>
                    <el-radio :label="true">启用</el-radio>
                    <el-radio :label="false">停用</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="cm-grid">
                <div class="cm-field cm-field--tier">
                  <div class="cm-field__label">
                    <span class="cm-tier cm-tier--p">省</span>省级代理商
                  </div>
                  <div class="cm-field__pair">
                    <el-input
                      v-model="formValidate.commissionConfig.agent.provinceAmount"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.provinceAmount') === 'error' }"
                    >
                      <template slot="append">元</template>
                    </el-input>
                    <span class="cm-field__or">或</span>
                    <el-input
                      v-model="formValidate.commissionConfig.agent.provinceRate"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.provinceRate') === 'error' }"
                    >
                      <template slot="append">%</template>
                    </el-input>
                  </div>
                  <p v-if="fieldMsg('agent.provinceAmount').text" class="cm-field__msg" :class="fieldMsg('agent.provinceAmount').cls">
                    <i v-if="fieldMsg('agent.provinceAmount').icon" :class="fieldMsg('agent.provinceAmount').icon"></i>
                    {{ fieldMsg('agent.provinceAmount').text }}
                  </p>
                </div>
                <div class="cm-field cm-field--tier">
                  <div class="cm-field__label">
                    <span class="cm-tier cm-tier--c">市</span>市级代理商
                  </div>
                  <div class="cm-field__pair">
                    <el-input
                      v-model="formValidate.commissionConfig.agent.cityAmount"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.cityAmount') === 'error' }"
                    >
                      <template slot="append">元</template>
                    </el-input>
                    <span class="cm-field__or">或</span>
                    <el-input
                      v-model="formValidate.commissionConfig.agent.cityRate"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.cityRate') === 'error' }"
                    >
                      <template slot="append">%</template>
                    </el-input>
                  </div>
                  <p v-if="fieldMsg('agent.cityAmount').text" class="cm-field__msg" :class="fieldMsg('agent.cityAmount').cls">
                    <i v-if="fieldMsg('agent.cityAmount').icon" :class="fieldMsg('agent.cityAmount').icon"></i>
                    {{ fieldMsg('agent.cityAmount').text }}
                  </p>
                </div>
                <div class="cm-field cm-field--tier">
                  <div class="cm-field__label">
                    <span class="cm-tier cm-tier--d">区</span>区级代理商
                  </div>
                  <div class="cm-field__pair">
                    <el-input
                      v-model="formValidate.commissionConfig.agent.districtAmount"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.districtAmount') === 'error' }"
                    >
                      <template slot="append">元</template>
                    </el-input>
                    <span class="cm-field__or">或</span>
                    <el-input
                      v-model="formValidate.commissionConfig.agent.districtRate"
                      placeholder="跟随全局"
                      clearable
                      :disabled="isDisabled"
                      :class="{ 'is-bad': issueLevel('agent.districtRate') === 'error' }"
                    >
                      <template slot="append">%</template>
                    </el-input>
                  </div>
                  <p v-if="fieldMsg('agent.districtAmount').text" class="cm-field__msg" :class="fieldMsg('agent.districtAmount').cls">
                    <i v-if="fieldMsg('agent.districtAmount').icon" :class="fieldMsg('agent.districtAmount').icon"></i>
                    {{ fieldMsg('agent.districtAmount').text }}
                  </p>
                </div>
              </div>
            </div>
          </section>

          <!-- ==================== 3. 团队奖 ==================== -->
          <section v-if="moduleSwitches.teamReward" class="cm-sec cm-sec--team">
            <header class="cm-sec__hd">
              <div class="cm-sec__title">
                <span class="cm-sec__dot"></span>
                <h4>团队奖</h4>
                <span class="cm-tag" :class="enabledClass(formValidate.commissionConfig.team.enabled)">{{
                  enabledText(formValidate.commissionConfig.team.enabled)
                }}</span>
                <span class="cm-sec__count">等级配置 {{ levelConfiguredCount('team') }} 项</span>
              </div>
            </header>
            <div class="cm-sec__bd">
              <div class="cm-hero">
                <div class="cm-hero__label">
                  团队奖
                  <span class="cm-hero__sub">总开关</span>
                </div>
                <div class="cm-hero__ctrl">
                  <el-radio-group
                    v-model="formValidate.commissionConfig.team.enabled"
                    :disabled="isDisabled"
                    class="cm-radios"
                  >
                    <el-radio :label="null">跟随全局</el-radio>
                    <el-radio :label="true">启用</el-radio>
                    <el-radio :label="false">停用</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="cm-sub" :class="{ 'is-open': levelPanel.team }">
                <div class="cm-sub__hd" @click="toggleLevelPanel('team')">
                  <i :class="levelPanel.team ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
                  <span class="cm-sub__t">按团队等级设置</span>
                  <span v-if="levelConfiguredCount('team')" class="cm-stat__count"
                    >{{ levelConfiguredCount('team') }}<i>项</i></span
                  >
                  <span class="cm-sub__n">优先级：本表 &gt; 全局团队等级配置；留空即走全局</span>
                  <el-button type="text" size="mini">{{ levelPanel.team ? '收起' : '展开' }}</el-button>
                </div>
                <div v-show="levelPanel.team" class="cm-sub__bd">
                  <div class="list-table cm-lv cm-lv--team">
                    <div class="list-head">
                      <div class="list-head__cell">等级</div>
                      <div class="list-head__cell">极差奖 · 金额 / 比例</div>
                      <div class="list-head__cell">平级奖 · 金额 / 比例</div>
                      <div class="list-head__cell list-head__cell--right">操作</div>
                    </div>
                    <div class="list-body">
                      <div v-for="row in teamLevelOptions" :key="`tl-${row.id}`" class="list-row">
                        <div class="list-cell">
                          <span class="status-tag status-tag--primary">{{ row.name }}</span>
                        </div>
                        <div class="list-cell">
                          <div
                            class="cm-field__pair cm-pair--sm"
                            :class="pairIssueClass(`team.levels.${row.id}.diffAmount`, `team.levels.${row.id}.diffRate`)"
                          >
                            <el-input
                              v-model="levelRow('team', row.id).diffAmount"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">元</template>
                            </el-input>
                            <span class="cm-field__slash">/</span>
                            <el-input
                              v-model="levelRow('team', row.id).diffRate"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">%</template>
                            </el-input>
                          </div>
                        </div>
                        <div class="list-cell">
                          <div
                            class="cm-field__pair cm-pair--sm"
                            :class="pairIssueClass(`team.levels.${row.id}.peerAmount`, `team.levels.${row.id}.peerRate`)"
                          >
                            <el-input
                              v-model="levelRow('team', row.id).peerAmount"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">元</template>
                            </el-input>
                            <span class="cm-field__slash">/</span>
                            <el-input
                              v-model="levelRow('team', row.id).peerRate"
                              placeholder="留空"
                              size="mini"
                              clearable
                              :disabled="isDisabled"
                            >
                              <template slot="append">%</template>
                            </el-input>
                          </div>
                        </div>
                        <div class="list-cell cm-lv__op">
                          <el-button type="text" size="mini" :disabled="isDisabled" @click="clearLevelRow('team', row)"
                            >清空</el-button
                          >
                        </div>
                      </div>
                      <div v-if="!teamLevelOptions.length" class="list-empty">
                        <i class="el-icon-folder-opened"></i>
                        <p>暂无团队等级，请先到 运营 → 团队等级 创建</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- ==================== 4. 门店服务费 ==================== -->
          <section v-if="moduleSwitches.store" class="cm-sec cm-sec--store">
            <header class="cm-sec__hd">
              <div class="cm-sec__title">
                <span class="cm-sec__dot"></span>
                <h4>门店服务费</h4>
                <span class="cm-tag" :class="enabledClass(storeFeeEnabledSummary)">{{
                  storeFeeEnabledText(storeFeeEnabledSummary)
                }}</span>
                <span class="cm-sec__count">已配置 {{ storeValueCount }} 项</span>
              </div>
            </header>
            <div class="cm-sec__bd">
              <p class="cm-notice">
                <i class="el-icon-info"></i>
                接入门店系统的自提 / 核销 / 配送服务费。留空金额与比例表示跟随门店默认；同时填写时金额优先。停用后该项服务费为 0。
              </p>

              <div
                v-for="fee in storeFeeFields"
                :key="fee.key"
                class="cm-fee-block"
              >
                <div class="cm-hero">
                  <div class="cm-hero__label">
                    {{ fee.label }}
                    <span class="cm-hero__sub">开关</span>
                  </div>
                  <div class="cm-hero__ctrl">
                    <el-radio-group
                      v-model="formValidate.commissionConfig.store[fee.key].enabled"
                      :disabled="isDisabled"
                      class="cm-radios"
                    >
                      <el-radio :label="null">跟随门店默认</el-radio>
                      <el-radio :label="true">启用</el-radio>
                      <el-radio :label="false">停用</el-radio>
                    </el-radio-group>
                  </div>
                </div>
                <div class="cm-grid">
                  <div class="cm-field">
                    <div class="cm-field__label">服务费</div>
                    <div class="cm-field__pair">
                      <el-input
                        v-model="formValidate.commissionConfig.store[fee.key].amount"
                        placeholder="跟随门店默认"
                        clearable
                        :disabled="isDisabled || formValidate.commissionConfig.store[fee.key].enabled === false"
                        :class="{ 'is-bad': issueLevel('store.' + fee.key + '.amount') === 'error' }"
                      >
                        <template slot="append">元</template>
                      </el-input>
                      <span class="cm-field__or">或</span>
                      <el-input
                        v-model="formValidate.commissionConfig.store[fee.key].rate"
                        placeholder="跟随门店默认"
                        clearable
                        :disabled="isDisabled || formValidate.commissionConfig.store[fee.key].enabled === false"
                        :class="{ 'is-bad': issueLevel('store.' + fee.key + '.rate') === 'error' }"
                      >
                        <template slot="append">%</template>
                      </el-input>
                    </div>
                    <p
                      v-if="fieldMsg('store.' + fee.key + '.amount').text"
                      class="cm-field__msg"
                      :class="fieldMsg('store.' + fee.key + '.amount').cls"
                    >
                      <i
                        v-if="fieldMsg('store.' + fee.key + '.amount').icon"
                        :class="fieldMsg('store.' + fee.key + '.amount').icon"
                      ></i>
                      {{ fieldMsg('store.' + fee.key + '.amount').text }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
        <div class="cm-actions">
          <div class="cm-actions__tip">
            <template v-if="commissionErrorCount">
              <i class="el-icon-error"></i>佣金设置有 {{ commissionErrorCount }} 项填写错误，需修正后才能保存
            </template>
            <template v-else-if="commissionWarnCount">
              <i class="el-icon-warning-outline"></i>佣金设置有 {{ commissionWarnCount }} 项待确认，确认无误后可保存
            </template>
            <template v-else-if="commissionDirty">
              <i class="el-icon-edit-outline"></i>佣金设置已修改，尚未保存
            </template>
            <span v-else class="cm-actions__step">编辑第 {{ Number(currentTab) + 1 }} / 5 步</span>
          </div>
          <div class="cm-actions__btns">
            <el-button v-show="Number(currentTab) > 0" class="submission" @click="handleSubmitUp">上一步</el-button>
            <el-button
              v-show="Number(currentTab) < 4"
              class="submission"
              :class="Number(currentTab) == 0 ? 'onePrimary' : ''"
              @click="handleSubmitNest('formValidate')"
              >下一步</el-button
            >
            <el-button class="submission" @click="handleCancel">取消</el-button>
            <el-button v-show="!isDisabled" type="primary" class="submission" @click="handleSubmit('formValidate')"
              >保存商品</el-button
            >
          </div>
        </div>
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
import { distributorLevelListApi } from '@/api/distributorLevel';
import { teamLevelAllApi } from '@/api/teamLevel';
import { marketingSendApi } from '@/api/marketing';
import { shippingTemplatesList } from '@/api/logistics';
import { goodDesignList } from '@/api/systemGroup';
import { arraysEqual } from '@/utils';
import { clearTreeData } from '@/utils/QXKJUtil';
import CreatTemplates from '@/views/systemSetting/deliverGoods/freightSet/creatTemplates';
import creatAttr from '../components/creatAttr';
import Templates from '../../appSetting/wxAccount/wxTemplate/index';
import { Debounce } from '@/utils/validate';
import { copyConfigApi, copyProductApi } from '@/api/store';
import { hiddenSwitchesBrief } from '@/api/hidden';
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
  isGiveIntegral: true,
  integralDeduct: 0,
  isIntegralDeductBrokerage: true,
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
      levels: [],
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
    },
    store: {
      pickup: { enabled: null, amount: null, rate: null },
      verify: { enabled: null, amount: null, rate: null },
      delivery: { enabled: null, amount: null, rate: null },
    },
    team: {
      enabled: null,
      levels: [],
    },
  };
}

/** 分销商等级覆盖行的数字字段 */
const DISTRIBUTOR_LEVEL_KEYS = ['selfAmount', 'selfRate', 'oneAmount', 'oneRate', 'twoAmount', 'twoRate'];
/** 团队奖等级覆盖行的数字字段 */
const TEAM_LEVEL_KEYS = ['diffAmount', 'diffRate', 'peerAmount', 'peerRate'];

function sanitizeLevelRows(list, keys) {
  if (!Array.isArray(list)) {
    return [];
  }
  return list
    .filter((row) => row && Number(row.levelId) > 0)
    .map((row) => {
      const out = { levelId: Number(row.levelId) };
      keys.forEach((k) => {
        out[k] = toNullableNumber(row[k]);
      });
      return out;
    });
}

function toNullableNumber(val) {
  if (val === null || val === undefined || val === '') {
    return null;
  }
  const n = Number(val);
  return Number.isFinite(n) ? n : null;
}

function createDefaultFeeItem() {
  return { enabled: null, amount: null, rate: null };
}

function mergeFeeItem(src) {
  const base = createDefaultFeeItem();
  if (!src || typeof src !== 'object') {
    return base;
  }
  const v = src.enabled;
  base.enabled = v === null || v === undefined || v === '' ? null : !!v;
  base.amount = toNullableNumber(src.amount);
  base.rate = toNullableNumber(src.rate);
  return base;
}

function mergeCommissionConfig(src) {
  const base = createDefaultCommissionConfig();
  if (!src || typeof src !== 'object') {
    return base;
  }
  ['distributor', 'agent', 'store', 'team'].forEach((section) => {
    if (!src[section] || typeof src[section] !== 'object') {
      return;
    }
    Object.keys(base[section]).forEach((key) => {
      if (!Object.prototype.hasOwnProperty.call(src[section], key)) {
        return;
      }
      const v = src[section][key];
      if (key === 'levels') {
        return; // 等级数组单独处理
      }
      if (section === 'store' && (key === 'pickup' || key === 'verify' || key === 'delivery')) {
        base[section][key] = mergeFeeItem(v);
        return;
      }
      if (key === 'enabled' || key === 'diffEnabled' || key === 'brokerageEnabled' || key === 'bonusEnabled'
        || key === 'syncMode' || key === 'superiorClaim') {
        base[section][key] = v === null || v === undefined || v === '' ? null : !!v;
      } else {
        base[section][key] = toNullableNumber(v);
      }
    });
  });
  base.distributor.levels = sanitizeLevelRows(src.distributor && src.distributor.levels, DISTRIBUTOR_LEVEL_KEYS);
  base.team.levels = sanitizeLevelRows(src.team && src.team.levels, TEAM_LEVEL_KEYS);
  return base;
}

function normalizeCommissionConfig(cfg) {
  return mergeCommissionConfig(cfg);
}

/* ==================== 佣金设置：UI 侧元数据（仅用于展示与校验，不参与提交） ==================== */
/**
 * 数值字段登记表
 * path：formValidate.commissionConfig 下的取值路径
 * type：amount=金额(元) / rate=比例(%)
 * pair：与之互斥的另一个字段（同时填写时金额优先）
 */
const COMMISSION_FIELDS = [
  { path: 'distributor.directAmount', type: 'amount', pair: 'distributor.directRate' },
  { path: 'distributor.directRate', type: 'rate', pair: 'distributor.directAmount' },
  { path: 'distributor.indirectAmount', type: 'amount', pair: 'distributor.indirectRate' },
  { path: 'distributor.indirectRate', type: 'rate', pair: 'distributor.indirectAmount' },
  { path: 'agent.provinceAmount', type: 'amount', pair: 'agent.provinceRate' },
  { path: 'agent.provinceRate', type: 'rate', pair: 'agent.provinceAmount' },
  { path: 'agent.cityAmount', type: 'amount', pair: 'agent.cityRate' },
  { path: 'agent.cityRate', type: 'rate', pair: 'agent.cityAmount' },
  { path: 'agent.districtAmount', type: 'amount', pair: 'agent.districtRate' },
  { path: 'agent.districtRate', type: 'rate', pair: 'agent.districtAmount' },
  { path: 'store.pickup.amount', type: 'amount', pair: 'store.pickup.rate' },
  { path: 'store.pickup.rate', type: 'rate', pair: 'store.pickup.amount' },
  { path: 'store.verify.amount', type: 'amount', pair: 'store.verify.rate' },
  { path: 'store.verify.rate', type: 'rate', pair: 'store.verify.amount' },
  { path: 'store.delivery.amount', type: 'amount', pair: 'store.delivery.rate' },
  { path: 'store.delivery.rate', type: 'rate', pair: 'store.delivery.amount' },
];

/** 等级覆盖行中的「金额/比例」成对字段，用于同列互斥提醒 */
const COMMISSION_LEVEL_PAIRS = [
  { section: 'distributor', pairs: [['selfAmount', 'selfRate'], ['oneAmount', 'oneRate'], ['twoAmount', 'twoRate']] },
  { section: 'team', pairs: [['diffAmount', 'diffRate'], ['peerAmount', 'peerRate']] },
];

/** 层级关系校验：上一级数值不应低于下一级（金额、比例分别比较） */
const COMMISSION_LAYER_RULES = [
  { hi: 'distributor.directAmount', lo: 'distributor.indirectAmount', loName: '二级返佣金额' },
  { hi: 'distributor.directRate', lo: 'distributor.indirectRate', loName: '二级返佣比例' },
  { hi: 'agent.provinceAmount', lo: 'agent.cityAmount', loName: '市级佣金金额' },
  { hi: 'agent.provinceRate', lo: 'agent.cityRate', loName: '市级佣金比例' },
  { hi: 'agent.cityAmount', lo: 'agent.districtAmount', loName: '区级佣金金额' },
  { hi: 'agent.cityRate', lo: 'agent.districtRate', loName: '区级佣金比例' },
  { hi: 'agent.provinceAmount', lo: 'agent.districtAmount', loName: '区级佣金金额' },
  { hi: 'agent.provinceRate', lo: 'agent.districtRate', loName: '区级佣金比例' },
];

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
      // 模块开关（运维面板）：关闭的模块隐藏佣金设置对应分节
      // spread=分销返佣 daili=区域代理 teamReward=团队奖 store=门店服务费
      moduleSwitches: { spread: true, daili: true, teamReward: true, store: true },
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
      distributorLevelOptions: [], // 分销商等级选项
      teamLevelOptions: [], // 团队等级选项
      levelPanel: { distributor: false, team: false }, // 等级表格展开/收起
      commissionSnapshot: '', // 佣金配置基线快照（用于判断是否有未保存改动）
      storeFeeFields: [
        { key: 'pickup', label: '自提服务费' },
        { key: 'verify', label: '核销服务费' },
        { key: 'delivery', label: '配送服务费' },
      ],
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
    /** 佣金设置的全部校验结果：{ path: { level: error|warn, msg } } */
    commissionIssues() {
      const cfg = this.formValidate.commissionConfig || {};
      const map = {};
      const put = (path, level, msg) => {
        if (!map[path]) map[path] = { level, msg };
      };
      COMMISSION_FIELDS.forEach((f) => {
        const raw = this.getCfgValue(cfg, f.path);
        if (raw === null || raw === undefined || raw === '') return;
        const num = Number(raw);
        if (!Number.isFinite(num)) {
          put(f.path, 'error', '请输入有效数字');
          return;
        }
        if (num < 0) {
          put(f.path, 'error', f.type === 'rate' ? '比例不能为负数' : '金额不能为负数');
          return;
        }
        if (f.type === 'rate' && num > 100) {
          put(f.path, 'error', '比例需在 0 ~ 100 之间');
          return;
        }
        if (f.type === 'rate' && f.pair) {
          const pairRaw = this.getCfgValue(cfg, f.pair);
          if (pairRaw !== null && pairRaw !== undefined && pairRaw !== '') {
            put(f.path, 'warn', '金额已填写，该项比例不生效（金额优先）');
          }
        }
      });
      COMMISSION_LAYER_RULES.forEach((r) => {
        const hi = this.numOf(this.getCfgValue(cfg, r.hi));
        const lo = this.numOf(this.getCfgValue(cfg, r.lo));
        if (hi === null || lo === null) return;
        if (lo > hi) put(r.lo, 'warn', `${r.loName}高于上一级，请确认层级设置`);
      });
      // 等级覆盖行：负数 / 比例越界 / 同一列金额与比例冲突
      COMMISSION_LEVEL_PAIRS.forEach((grp) => {
        const section = cfg[grp.section];
        const levels = section && Array.isArray(section.levels) ? section.levels : [];
        levels.forEach((row) => {
          if (!row || row.levelId === undefined || row.levelId === null) return;
          grp.pairs.forEach((keys) => {
            const amountKey = keys[0];
            const rateKey = keys[1];
            const aPath = `${grp.section}.levels.${row.levelId}.${amountKey}`;
            const rPath = `${grp.section}.levels.${row.levelId}.${rateKey}`;
            const a = this.numOf(row[amountKey]);
            const r = this.numOf(row[rateKey]);
            if (a !== null && !Number.isFinite(a)) put(aPath, 'error', '请输入有效数字');
            else if (a !== null && a < 0) put(aPath, 'error', '金额不能为负数');
            if (r !== null) {
              if (!Number.isFinite(r)) put(rPath, 'error', '请输入有效数字');
              else if (r < 0) put(rPath, 'error', '比例不能为负数');
              else if (r > 100) put(rPath, 'error', '比例需在 0 ~ 100 之间');
              else if (a !== null && Number.isFinite(a) && a >= 0) {
                put(rPath, 'warn', '金额已填写，该项比例不生效（金额优先）');
              }
            }
          });
        });
      });
      return map;
    },
    commissionErrorCount() {
      const issues = this.commissionIssues;
      return Object.keys(issues).filter((k) => issues[k].level === 'error').length;
    },
    commissionWarnCount() {
      const issues = this.commissionIssues;
      return Object.keys(issues).filter((k) => issues[k].level === 'warn').length;
    },
    distributorValueCount() {
      return this.countCommissionValues('distributor');
    },
    agentValueCount() {
      return this.countCommissionValues('agent');
    },
    storeValueCount() {
      const store = (this.formValidate.commissionConfig || {}).store || {};
      let n = this.countCommissionValues('store');
      ['pickup', 'verify', 'delivery'].forEach((k) => {
        const item = store[k];
        if (item && item.enabled !== null && item.enabled !== undefined) n += 1;
      });
      return n;
    },
    /** 门店服务费总览开关态：任一启用→on；全部停用→off；否则跟随门店 */
    storeFeeEnabledSummary() {
      const store = (this.formValidate.commissionConfig || {}).store || {};
      const flags = ['pickup', 'verify', 'delivery'].map((k) => (store[k] ? store[k].enabled : null));
      if (flags.every((e) => e === false)) return false;
      if (flags.some((e) => e === true)) return true;
      return null;
    },
    /** 顶部状态卡数据（仅展示用途，不改变提交内容） */
    commissionSummary() {
      return [
        {
          key: 'dist',
          name: '分销商返佣',
          enabled: this.formValidate.commissionConfig.distributor.enabled,
          count: this.distributorValueCount + this.levelConfiguredCount('distributor'),
        },
        {
          key: 'agent',
          name: '区域代理',
          enabled: this.formValidate.commissionConfig.agent.enabled,
          count: this.agentValueCount,
        },
        {
          key: 'team',
          name: '团队奖',
          enabled: this.formValidate.commissionConfig.team.enabled,
          count: this.levelConfiguredCount('team'),
        },
        {
          key: 'store',
          name: '门店服务费',
          enabled: this.storeFeeEnabledSummary,
          count: this.storeValueCount,
        },
      ].map((item) => ({
        ...item,
        state: item.enabled === true ? 'on' : item.enabled === false ? 'off' : 'global',
        stateText: item.key === 'store' ? this.storeFeeEnabledText(item.enabled) : this.enabledText(item.enabled),
      }));
    },
    /** 佣金段（不含等级行）是否有未保存改动 */
    commissionDirty() {
      if (!this.commissionSnapshot) return false;
      const cfg = this.formValidate.commissionConfig;
      return JSON.stringify(cfg, (k, v) => (k === 'levels' ? undefined : v)) !== this.commissionSnapshot;
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
    this.loadLevelOptions();
    this.markCommissionBaseline();
    this.fetchModuleSwitches();
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
    // 拉取模块开关：运维面板关闭的模块（分销/区域代理/团队奖/门店）隐藏对应佣金分节
    fetchModuleSwitches() {
      hiddenSwitchesBrief()
        .then((res) => {
          if (res && typeof res === 'object') {
            this.moduleSwitches = { ...this.moduleSwitches, ...res };
          }
        })
        .catch(() => {});
    },
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
                  isGiveIntegral: info.isGiveIntegral !== false && info.isGiveIntegral !== 0,
                  integralDeduct: info.integralDeduct != null ? Number(info.integralDeduct) : 0,
                  isIntegralDeductBrokerage: info.isIntegralDeductBrokerage !== false && info.isIntegralDeductBrokerage !== 0,
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
                  isGiveIntegral: res.isGiveIntegral !== false && res.isGiveIntegral !== 0,
                  integralDeduct: res.integralDeduct != null ? Number(res.integralDeduct) : 0,
                  isIntegralDeductBrokerage: res.isIntegralDeductBrokerage !== false && res.isIntegralDeductBrokerage !== 0,
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
            isGiveIntegral: info.isGiveIntegral !== false && info.isGiveIntegral !== 0,
            integralDeduct: info.integralDeduct != null ? Number(info.integralDeduct) : 0,
            isIntegralDeductBrokerage: info.isIntegralDeductBrokerage !== false && info.isIntegralDeductBrokerage !== 0,
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
          this.ensureLevelRows();
          this.markCommissionBaseline();
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
      if (this.locateCommissionErrors()) return;
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
    // 佣金设置：拉取分销商/团队两套等级，用于按等级展开配置
    loadLevelOptions() {
      distributorLevelListApi()
        .then((res) => {
          this.distributorLevelOptions = Array.isArray(res) ? res : res.list || [];
          this.ensureLevelRows();
        })
        .catch(() => {});
      teamLevelAllApi()
        .then((res) => {
          const list = Array.isArray(res) ? res : res.list || [];
          this.teamLevelOptions = list.map((it) => ({ id: it.id, name: it.name }));
          this.ensureLevelRows();
        })
        .catch(() => {});
    },
    // 为每个等级确保有一行覆盖记录（找不到时模板绑定的临时对象不会写回，所以必须先建行）
    ensureLevelRows() {
      const cfg = this.formValidate.commissionConfig;
      if (!cfg) return;
      this.ensureRows(cfg.distributor, this.distributorLevelOptions, DISTRIBUTOR_LEVEL_KEYS);
      this.ensureRows(cfg.team, this.teamLevelOptions, TEAM_LEVEL_KEYS);
    },
    ensureRows(section, options, keys) {
      if (!section || !Array.isArray(options) || !options.length) return;
      if (!Array.isArray(section.levels)) {
        this.$set(section, 'levels', []);
      }
      options.forEach((opt) => {
        let row = section.levels.find((r) => Number(r.levelId) === Number(opt.id));
        if (!row) {
          row = { levelId: opt.id };
          keys.forEach((k) => {
            row[k] = null;
          });
          section.levels.push(row);
        } else {
          // 老数据行缺新字段时补齐（Vue2 需 $set 才有响应性，如后加的 discount）
          keys.forEach((k) => {
            if (row[k] === undefined) {
              this.$set(row, k, null);
            }
          });
        }
      });
    },
    // 等级表格展开/收起
    toggleLevelPanel(key) {
      this.levelPanel[key] = !this.levelPanel[key];
    },
    // 统计某板块已填写覆盖值的等级行数（用于收起状态提示）
    levelConfiguredCount(sectionKey) {
      const section = this.formValidate.commissionConfig[sectionKey];
      const keys = {
        distributor: DISTRIBUTOR_LEVEL_KEYS,
        team: TEAM_LEVEL_KEYS,
      }[sectionKey] || [];
      if (!section || !Array.isArray(section.levels)) return 0;
      return section.levels.filter((r) =>
        keys.some((k) => r[k] !== null && r[k] !== undefined && r[k] !== '')
      ).length;
    },
    // 模板按等级取行（行由 ensureLevelRows 预建）
    levelRow(sectionKey, levelId) {
      const section = this.formValidate.commissionConfig[sectionKey];
      const levels = section && Array.isArray(section.levels) ? section.levels : [];
      return levels.find((r) => Number(r.levelId) === Number(levelId)) || {};
    },
    clearLevelRow(sectionKey, option) {
      const section = this.formValidate.commissionConfig[sectionKey];
      if (!section || !Array.isArray(section.levels)) return;
      const target = section.levels.find((r) => Number(r.levelId) === Number(option.id));
      if (!target) return;
      const keys = {
        distributor: DISTRIBUTOR_LEVEL_KEYS,
        team: TEAM_LEVEL_KEYS,
      }[sectionKey] || [];
      keys.forEach((k) => {
        target[k] = null;
      });
    },
    /* ---------- 佣金设置：展示与校验辅助（只改 UI，不改变提交数据） ---------- */
    /** 按 'a.b.c' 路径取值，路径中断返回 null */
    getCfgValue(root, path) {
      const keys = String(path).split('.');
      let cur = root;
      for (let i = 0; i < keys.length; i += 1) {
        if (cur === null || cur === undefined) return null;
        cur = cur[keys[i]];
      }
      return cur;
    },
    /** 空值归一：'' / null / undefined → null，其余转数字（非数字即为 NaN） */
    numOf(val) {
      if (val === null || val === undefined || val === '') return null;
      return Number(val);
    },
    /** 统计某板块已填写的数值项（不含等级覆盖行） */
    countCommissionValues(section) {
      const cfg = this.formValidate.commissionConfig || {};
      return COMMISSION_FIELDS.filter((f) => f.path.indexOf(`${section}.`) === 0).filter((f) => {
        const v = this.getCfgValue(cfg, f.path);
        return v !== null && v !== undefined && v !== '';
      }).length;
    },
    enabledClass(val) {
      return val === true ? 'is-on' : val === false ? 'is-off' : 'is-global';
    },
    enabledText(val) {
      return val === true ? '已启用' : val === false ? '已停用' : '跟随全局';
    },
    storeFeeEnabledText(val) {
      return val === true ? '已启用' : val === false ? '已停用' : '跟随门店默认';
    },
    isSectionOff(section) {
      const cfg = this.formValidate.commissionConfig || {};
      const part = cfg[section];
      return !!part && part.enabled === false;
    },
    issueLevel(path) {
      const issue = this.commissionIssues[path];
      return issue ? issue.level : '';
    },
    /** 统一反显：仅在有错误/警告时返回内容，正常状态不显示小字（返回空 text 由 v-if 隐藏） */
    fieldMsg(path) {
      const issues = this.commissionIssues;
      const own = issues[path];
      const field = COMMISSION_FIELDS.filter((f) => f.path === path)[0];
      const pairIssue = field && field.pair ? issues[field.pair] : null;
      const pick = own || pairIssue;
      if (!pick) {
        return { cls: '', icon: '', text: '' };
      }
      return {
        cls: pick.level === 'error' ? 'is-error' : 'is-warn',
        icon: pick.level === 'error' ? 'el-icon-error' : 'el-icon-warning-outline',
        text: pick.msg,
      };
    },
    /** 等级表格里「金额 / 比例」这一组的整体状态样式 */
    pairIssueClass(amountPath, ratePath) {
      const a = this.commissionIssues[amountPath];
      const r = this.commissionIssues[ratePath];
      if ((a && a.level === 'error') || (r && r.level === 'error')) return 'is-error';
      if (a || r) return 'is-warn';
      return '';
    },
    scrollToCommission(key) {
      const el = this.$el.querySelector(`.cm-sec--${key}`);
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    },
    markCommissionBaseline() {
      const cfg = this.formValidate.commissionConfig;
      this.commissionSnapshot = JSON.stringify(cfg, (k, v) => (k === 'levels' ? undefined : v));
    },
    /** 保存前拦截：存在硬性错误时定位到佣金设置并阻止提交 */
    locateCommissionErrors() {
      const issues = this.commissionIssues;
      const paths = Object.keys(issues).filter((k) => issues[k].level === 'error');
      if (!paths.length) return false;
      this.currentTab = '4';
      this.$message.error(`佣金设置有 ${paths.length} 项填写错误，请修正后再保存`);
      this.$nextTick(() => {
        const el = this.$el.querySelector('.cm');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
      return true;
    },
    handleCancel() {
      if (this.isDisabled) {
        this.$router.push({ path: '/store/index' });
        return;
      }
      this.$confirm('离开后当前未保存的商品信息将丢失，确定取消编辑吗？', '取消编辑', {
        confirmButtonText: '确定离开',
        cancelButtonText: '继续编辑',
        type: 'warning',
      })
        .then(() => {
          this.$router.push({ path: '/store/index' });
        })
        .catch(() => {});
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
.form-tip {
  font-size: 12px;
  color: #999;
}
/* ==================== 佣金设置（重构：卡片分组 + 统一栅格 + 校验反馈） ==================== */
/* 约定：仅做视觉/布局，不触碰任何字段绑定与提交逻辑 */
@keyframes cm-rise {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.cm {
  padding: 6px 0 4px;
  font-size: 13px;
  color: #303133;

  /* ---------- 顶部：口径速览 + 分组状态卡 ---------- */
  .cm-brief {
    margin-bottom: 16px;
    padding: 12px 16px 14px;
    border: 1px solid #e6ecf7;
    border-radius: 6px;
    background: linear-gradient(180deg, #f7faff 0%, #ffffff 100%);

    &__rules {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 4px 24px;
      margin: 0 0 12px;
      padding: 0 0 11px;
      border-bottom: 1px dashed #e4e9f2;
      list-style: none;

      li {
        display: flex;
        align-items: center;
        font-size: 12px;
        line-height: 20px;
        color: #4b5567;
      }

      .cm-brief__k {
        margin-right: 6px;
        padding: 0 7px;
        border-radius: 3px;
        font-size: 12px;
        font-weight: 600;
        line-height: 20px;
        color: var(--prev-color-primary, #0256ff);
        background: var(--prev-color-primary-light-9, #ecf5ff);
      }
    }

    &__stats {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 10px;
    }
  }

  .cm-stat {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 3px;
    padding: 9px 12px;
    border: 1px solid #e8edf6;
    border-left: 3px solid #c0c8d6;
    border-radius: 4px;
    background: #fff;
    cursor: pointer;
    transition: box-shadow 0.18s ease, transform 0.18s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(15, 34, 67, 0.08);
      transform: translateY(-1px);
    }

    &__row {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: 8px;
    }

    &__name {
      overflow: hidden;
      font-size: 12px;
      color: #606266;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__count {
      flex: 0 0 auto;
      font-size: 16px;
      font-weight: 600;
      line-height: 20px;
      color: #303133;
      font-variant-numeric: tabular-nums;

      i {
        margin-left: 1px;
        font-size: 11px;
        font-style: normal;
        font-weight: 400;
        color: #a8abb2;
      }
    }

    &__state {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #909399;
    }

    &__dot {
      flex: 0 0 auto;
      width: 6px;
      height: 6px;
      margin-right: 5px;
      border-radius: 50%;
      background: #c0c8d6;
    }

    &.is-on .cm-stat__state {
      color: #18a058;
    }

    &.is-on .cm-stat__dot {
      background: #18a058;
    }

    &.is-off .cm-stat__state {
      color: #d07200;
    }

    &.is-off .cm-stat__dot {
      background: #d07200;
    }

    &.is-global .cm-stat__dot {
      background: #3582f6;
    }

    &.cm-stat--dist {
      border-left-color: #0256ff;
    }

    &.cm-stat--agent {
      border-left-color: #12a06f;
    }

    &.cm-stat--team {
      border-left-color: #e8850c;
    }

    &.cm-stat--store {
      border-left-color: #6c4cf1;
    }
  }

  /* ---------- 分组卡片 ---------- */
  .cm-sec {
    --cm-accent: #0256ff;
    --cm-accent-soft: rgba(2, 86, 255, 0.07);

    margin-bottom: 16px;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    background: #fff;
    overflow: hidden;
    animation: cm-rise 0.34s cubic-bezier(0.16, 1, 0.3, 1) both;

    & + .cm-sec {
      animation-delay: 0.06s;
    }

    &.cm-sec--agent {
      --cm-accent: #12a06f;
      --cm-accent-soft: rgba(18, 160, 111, 0.08);
    }

    &.cm-sec--team {
      --cm-accent: #e8850c;
      --cm-accent-soft: rgba(232, 133, 12, 0.09);
    }

    &.cm-sec--store {
      --cm-accent: #6c4cf1;
      --cm-accent-soft: rgba(108, 76, 241, 0.08);
    }

    .cm-fee-block {
      & + .cm-fee-block {
        margin-top: 14px;
        padding-top: 14px;
        border-top: 1px dashed #eef0f4;
      }
    }

    &__hd {
      padding: 12px 16px 10px;
      border-bottom: 1px solid #f0f2f5;
      background: linear-gradient(90deg, var(--cm-accent-soft) 0%, rgba(255, 255, 255, 0) 62%);
    }

    &__title {
      display: flex;
      align-items: center;
    }

    &__title h4 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
    }

    &__dot {
      width: 3px;
      height: 14px;
      margin-right: 8px;
      border-radius: 2px;
      background: var(--cm-accent);
    }

    &__count {
      margin-left: auto;
      font-size: 12px;
      color: #909399;
      font-variant-numeric: tabular-nums;
    }

    &__bd {
      padding: 14px 16px 16px;
    }
  }

  .cm-tag {
    display: inline-block;
    height: 20px;
    margin-left: 8px;
    padding: 0 7px;
    border-radius: 3px;
    font-size: 12px;
    line-height: 20px;
    color: #7d838c;
    background: #f4f4f5;

    &.is-on {
      color: #18a058;
      background: rgba(24, 160, 88, 0.12);
    }

    &.is-off {
      color: #d07200;
      background: rgba(232, 133, 12, 0.14);
    }
  }

  /* ---------- 停用提示条 ---------- */
  .cm-notice {
    display: flex;
    align-items: center;
    margin: 0 0 12px;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 12px;
    line-height: 18px;

    i {
      margin-right: 6px;
    }

    &--warn {
      border: 1px solid #ffe0bd;
      background: #fff7ec;
      color: #d07200;
    }
  }

  /* ---------- 开关行（突出关键操作） ---------- */
  .cm-hero {
    display: flex;
    align-items: center;
    margin-bottom: 14px;
    padding: 10px 14px;
    border: 1px solid #eef1f6;
    border-radius: 4px;
    background: #fafbfd;

    &--sub {
      margin: 14px 0 0;
    }

    &__label {
      flex: 0 0 auto;
      position: relative;
      min-width: 96px;
      padding-left: 10px;
      font-size: 13px;
      font-weight: 600;
      color: #303133;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        width: 3px;
        height: 12px;
        border-radius: 2px;
        background: var(--cm-accent);
        transform: translateY(-50%);
      }
    }

    &__sub {
      margin-left: 6px;
      font-size: 11px;
      font-weight: 400;
      color: #a8abb2;
    }

    &__ctrl {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
    }
  }

  .cm-radios {
    ::v-deep .el-radio {
      margin-right: 18px;
    }

    ::v-deep .el-radio__label {
      font-size: 13px !important;
    }
  }

  /* ---------- 数值输入区 ---------- */
  .cm-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(318px, 1fr));
    gap: 14px 20px;
  }

  .cm-field {
    &__label {
      display: flex;
      align-items: center;
      margin-bottom: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #303133;
    }

    &__sub {
      margin-left: 6px;
      font-size: 12px;
      font-weight: 400;
      color: #a8abb2;
    }

    &__q {
      margin-left: 4px;
      font-size: 13px;
      color: #c0c4cc;
      cursor: help;
    }

    &__pair {
      display: flex;
      align-items: center;
    }

    &__pair ::v-deep .el-input {
      width: 132px;
    }

    &__pair ::v-deep .el-input-group__append {
      padding: 0 10px;
      background: #f5f7fa;
      color: #909399;
    }

    &__pair.is-warn ::v-deep .el-input__inner {
      border-color: #e6a23c;
    }

    &__pair.is-error ::v-deep .el-input__inner,
    &__pair ::v-deep .el-input.is-bad .el-input__inner {
      border-color: #e04c4c;
      box-shadow: 0 0 0 2px rgba(224, 76, 76, 0.1);
    }

    &__or,
    &__slash {
      flex: 0 0 auto;
      margin: 0 8px;
      font-size: 12px;
      color: #a8abb2;
    }

    &__msg {
      margin: 6px 0 0;
      font-size: 12px;
      line-height: 18px;
      color: #a8abb2;

      i {
        margin-right: 4px;
      }

      &.is-error {
        color: #e04c4c;
      }

      &.is-warn {
        color: #d07200;
      }
    }
  }

  /* 省市区层级徽标 */
  .cm-tier {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    margin-right: 6px;
    border-radius: 3px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;

    &--p {
      background: #12a06f;
    }

    &--c {
      background: #35b98c;
    }

    &--d {
      background: #6fd0ad;
    }
  }

  /* ---------- 折叠区（按等级设置） ---------- */
  .cm-sub {
    margin-top: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    overflow: hidden;

    &__hd {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      background: var(--cm-accent-soft);
      cursor: pointer;
      user-select: none;

      i {
        margin-right: 8px;
        font-size: 14px;
        color: var(--cm-accent);
      }
    }

    &__t {
      font-size: 13px;
      font-weight: 600;
      color: #303133;
    }

    &__n {
      flex: 1;
      margin-left: 12px;
      overflow: hidden;
      font-size: 12px;
      color: #98a0ad;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__bd {
      padding: 10px 12px 12px;
    }
  }

  /* 等级覆盖表：沿用全站 list-table 规范，仅覆盖模板与列宽 */
  .cm-lv {
    border-color: #e6ecf7;
    border-radius: 4px;

    .list-head {
      height: 42px;
      padding: 0 12px;
    }

    .list-head__cell {
      font-size: 13px;
      font-weight: 600;
      color: #2c3e50;

      &--right {
        text-align: right;
      }
    }

    .list-row {
      padding: 10px 12px;
    }

    .list-cell {
      padding-right: 12px;
    }

    .list-empty {
      padding: 28px 0;
    }

    .cm-lv__op {
      padding-right: 0;
      text-align: right;
    }
  }

  .cm-lv.cm-lv--dist {
    --list-cols: 128px repeat(3, minmax(206px, 1fr)) 72px;
  }

  .cm-lv.cm-lv--team {
    --list-cols: 128px repeat(2, minmax(238px, 1fr)) 72px;
  }

  .cm-pair--sm {
    ::v-deep .el-input {
      width: 96px;
    }

    ::v-deep .el-input__inner {
      height: 28px;
      line-height: 28px;
    }

    ::v-deep .el-input-group__append {
      padding: 0 8px;
      background: #f5f7fa;
      color: #909399;
    }
  }
}

/* ---------- 底部操作栏 ---------- */
.cm-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
  padding-top: 14px;
  border-top: 1px solid #f0f2f5;

  &__tip {
    font-size: 12px;
    color: #909399;

    i {
      margin-right: 4px;
    }

    i.el-icon-error {
      color: #e04c4c;
    }

    i.el-icon-warning-outline {
      color: #d07200;
    }

    i.el-icon-edit-outline {
      color: var(--prev-color-primary, #0256ff);
    }
  }

  &__step {
    color: #a8abb2;
  }

  &__btns {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;

    .submission {
      margin-left: 0;
      margin-right: 0;
    }
  }
}

@media (prefers-reduced-motion: reduce) {
  .cm .cm-sec,
  .cm .cm-stat {
    animation: none;
    transition: none;
  }
}

</style>
