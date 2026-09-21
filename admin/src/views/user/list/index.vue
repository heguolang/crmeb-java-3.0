<template>
  <div class="divBox relative">
    <el-card :bordered="false" shadow="never" class="ivu-mt filter-card" :body-style="{ padding: 0 }">
      <div class="filter-head">
        <span class="filter-head__title">条件筛选</span>
        <a class="filter-head__toggle" @click="collapse = !collapse">
          <template v-if="!collapse"> 展开 <i class="el-icon-arrow-down" /> </template>
          <template v-else> 收起 <i class="el-icon-arrow-up" /> </template>
        </a>
      </div>
      <el-form size="small" :model="userFrom" ref="userFrom" label-position="top" class="filter-form">
        <div class="filter-group">
          <div class="filter-group__title">用户信息</div>
          <div class="filter-grid">
            <el-form-item label="用户搜索">
              <UserSearchInput ref="userSearchInput" v-model="userFrom" @searchList="userSearchs" />
            </el-form-item>
            <el-form-item label="用户标签">
              <el-select
                v-model="labelData"
                @visible-change="userSearchs"
                @remove-tag="userSearchs"
                @clear="userSearchs"
                placeholder="请选择用户标签"
                clearable
                filterable
                multiple
              >
                <el-option
                  :value="item.id"
                  v-for="(item, index) in labelLists"
                  :key="index"
                  :label="item.name"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="消费情况">
              <el-select
                v-model="userFrom.payCount"
                @visible-change="userSearchs"
                @clear="userSearchs"
                placeholder="请选择消费情况"
                clearable
              >
                <el-option value="0" label="0"></el-option>
                <el-option value="1" label="1+"></el-option>
                <el-option value="2" label="2+"></el-option>
                <el-option value="3" label="3+"></el-option>
                <el-option value="4" label="4+"></el-option>
                <el-option value="5" label="5+"></el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>
        <div class="filter-group" v-show="collapse">
          <div class="filter-group__title">更多筛选</div>
          <div class="filter-grid">
            <el-form-item label="用户分组">
              <el-select
                v-model="groupData"
                @visible-change="userSearchs"
                @remove-tag="userSearchs"
                @clear="userSearchs"
                placeholder="请选择用户分组"
                clearable
                filterable
                multiple
              >
                <el-option
                  :value="item.id"
                  v-for="(item, index) in groupList"
                  :key="index"
                  :label="item.groupName"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="国家">
              <el-select
                v-model="userFrom.country"
                @visible-change="userSearchs"
                @clear="userSearchs"
                placeholder="请选择国家"
                clearable
                @on-change="changeCountry"
              >
                <el-option value="CN" label="中国"></el-option>
                <el-option value="OTHER" label="国外"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="省份">
              <el-cascader
                :options="addresData"
                :props="propsCity"
                filterable
                v-model="address"
                @clear="userSearchs"
                @change="handleChange"
                clearable
              ></el-cascader>
            </el-form-item>
            <el-form-item label="访问情况">
              <el-select
                v-model="userFrom.accessType"
                @visible-change="userSearchs"
                @clear="userSearchs"
                placeholder="请选择访问情况"
                clearable
              >
                <el-option :value="1" label="首次访问"></el-option>
                <el-option :value="2" label="时间段访问过"></el-option>
                <el-option :value="3" label="时间段未访问"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="时间选择" v-if="userFrom.accessType">
              <optionDatePicker v-model="timeVal" @changeOptTime="onchangeTime"></optionDatePicker>
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="userFrom.sex" @visible-change="userSearchs" @clear="userSearchs" placeholder="请选择性别">
                <el-option :value="0" label="未知"></el-option>
                <el-option :value="1" label="男"></el-option>
                <el-option :value="2" label="女"></el-option>
                <el-option :value="3" label="保密"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="身份">
              <el-select
                v-model="userFrom.isPromoter"
                @visible-change="userSearchs"
                @clear="userSearchs"
                placeholder="请选择身份"
              >
                <el-option :value="1" label="推广员"></el-option>
                <el-option :value="0" label="普通用户"></el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="ios-search" @click="userSearchs">搜索</el-button>
          <el-button class="ResetSearch" @click="reset('userFrom')">一键重置</el-button>
        </div>
      </el-form>
    </el-card>
    <el-card class="box-card mt14">
      <div slot="header" class="list-toolbar">
        <el-tabs v-model="loginType" @tab-click="getList(1)">
          <el-tab-pane :label="item.name" :name="item.type.toString()" v-for="(item, index) in headeNum" :key="index" />
        </el-tabs>
        <div class="list-toolbar__actions">
          <span class="list-toolbar__tip" v-if="selectionList.length">已选 {{ selectionList.length }} 人</span>
          <el-button size="small" @click="onSend" type="primary" v-hasPermi="['admin:coupon:user:receive']"
            >发送优惠券</el-button
          >
          <el-button
            size="small"
            :disabled="!selectionList.length"
            @click="setBatch('group')"
            v-hasPermi="['admin:user:group']"
            >批量设置分组</el-button
          >
          <el-button
            size="small"
            :disabled="!selectionList.length"
            @click="setBatch('label')"
            v-hasPermi="['admin:user:tag']"
            >批量设置标签</el-button
          >
        </div>
      </div>
      <div class="list-table">
        <!-- 表头：浅蓝底，与下方数据行共用同一套列宽，保证严格对齐 -->
        <div class="list-head">
          <div class="list-head__cell mh-check"></div>
          <div class="list-head__cell">会员</div>
          <div class="list-head__cell">会员信息</div>
          <div class="list-head__cell">资金</div>
          <div class="list-head__cell">下级与消费</div>
          <div class="list-head__cell">状态</div>
          <div class="list-head__cell">操作</div>
        </div>

        <div class="list-body" v-loading="listLoading">
          <div
            class="list-row"
            :class="{ 'is-checked': item._checked }"
            v-for="item in tableData.data"
            :key="item.uid"
          >
            <div class="list-cell mi-check">
              <el-checkbox v-model="item._checked" @change="onCheckItem(item)"></el-checkbox>
            </div>

            <!-- 会员：头像 + 昵称 + 身份标签 + 来源时间 -->
            <div class="list-cell mi-member">
              <el-image class="member-avatar" :src="item.avatar" :preview-src-list="[item.avatar]" fit="cover" />
              <div class="member-base">
                <div class="member-tags">
                  <span class="status-tag status-tag--primary">{{ matchLevelName(item.level) }}</span>
                  <span class="status-tag" :class="item.isPromoter ? 'status-tag--warning' : 'status-tag--info'">{{
                    item.isPromoter ? '推广员' : '普通会员'
                  }}</span>
                  <span class="status-tag status-tag--danger" v-if="item.isLogoff">已注销</span>
                </div>
                <div class="member-sub">来源：{{ userTypeText(item.userType) }}</div>
              </div>
            </div>

            <!-- 会员信息 -->
            <div class="list-cell mi-info">
              <div class="kv">
                <span class="kv__k">用户名：</span>
                <span class="kv__v" :class="isRedFont(item)">{{ item.nickname | filterEmpty }}</span>
              </div>
              <div class="kv">
                <span class="kv__k">会员ID：</span><span class="kv__v kv__v--num">{{ item.uid }}</span>
                <i class="kv__copy el-icon-document-copy" title="复制会员ID" @click="copyText(item.uid)"></i>
              </div>
              <div class="kv">
                <span class="kv__k">推荐人：</span>
                <span class="kv__v">{{ item.spreadNickname | filterEmpty }}</span>
                <span class="kv__uid" v-if="item.spreadUid">ID:{{ item.spreadUid }}</span>
                <i
                  class="kv__copy el-icon-document-copy"
                  v-if="item.spreadUid"
                  title="复制推荐人ID"
                  @click="copyText(item.spreadUid)"
                ></i>
              </div>
              <div class="kv"><span class="kv__k">手机号：</span><span class="kv__v kv__v--num">{{ item.phone | filterEmpty }}</span></div>
              <div class="kv"><span class="kv__k">注册时间：</span><span class="kv__v kv__v--num">{{ item.createTime | filterEmpty }}</span></div>
              <div class="kv"><span class="kv__k">备注：</span><span class="kv__v">{{ item.mark | filterEmpty }}</span></div>
            </div>

            <!-- 资金 -->
            <div class="list-cell mi-money">
              <div class="kv"><span class="kv__k">佣金：</span><span class="kv__v kv__v--num kv__v--money">{{ item.brokeragePrice }}</span></div>
              <div class="kv"><span class="kv__k">积分：</span><span class="kv__v kv__v--num">{{ item.integral }}</span></div>
              <div class="kv"><span class="kv__k">余额：</span><span class="kv__v kv__v--num kv__v--money">{{ item.nowMoney }}</span></div>
            </div>

            <!-- 下级与消费 -->
            <div class="list-cell mi-team">
              <div class="kv"><span class="kv__k">直推人数：</span><span class="kv__v kv__v--num">{{ item.spreadCount || 0 }}</span></div>
              <div class="kv"><span class="kv__k">消费次数：</span><span class="kv__v kv__v--num">{{ item.payCount || 0 }}</span></div>
              <div class="kv"><span class="kv__k">团队等级：</span><span class="kv__v">{{ matchTeamLevelName(item.teamLevel) }}</span></div>
              <div class="kv"><span class="kv__k">分组：</span><span class="kv__v">{{ item.groupName || '-' }}</span></div>
              <div class="kv"><span class="kv__k">标签：</span><span class="kv__v">{{ item.tagName || '-' }}</span></div>
            </div>

            <!-- 状态 -->
            <div class="list-cell mi-status">
              <el-switch v-model="item.status" @change="onStatusChange(item)" />
              <span class="status-label" :class="item.status ? 'is-on' : 'is-off'">{{ item.status ? '正常' : '禁用' }}</span>
            </div>

            <!-- 操作 -->
            <div class="list-cell mi-ops op-bar">
              <el-button
                class="op-btn"
                size="mini"
                type="primary"
                plain
                @click="onDetails(item.uid)"
                v-hasPermi="['admin:user:topdetail']"
                >详情</el-button
              >
              <el-button
                class="op-btn"
                size="mini"
                type="primary"
                plain
                @click="editUser(item.uid)"
                v-hasPermi="['admin:user:infobycondition']"
                >编辑</el-button
              >
              <el-dropdown trigger="click" class="more-drop">
                <el-button size="mini">
                  更多<i class="el-icon-arrow-down el-icon--right" />
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    @click.native="editPoint(item.uid)"
                    v-if="checkPermi(['admin:user:operate:founds'])"
                    >账户充减</el-dropdown-item
                  >
                  <el-dropdown-item @click.native="setBatch('group', item)" v-if="checkPermi(['admin:user:group'])"
                    >设置分组</el-dropdown-item
                  >
                  <el-dropdown-item @click.native="setBatch('label', item)" v-if="checkPermi(['admin:user:tag'])"
                    >设置标签</el-dropdown-item
                  >
                  <el-dropdown-item @click.native="setPhone(item)" v-if="checkPermi(['admin:user:update:phone'])"
                    >修改手机号</el-dropdown-item
                  >
                  <el-dropdown-item
                    @click.native="onLevel(item.uid, item.level)"
                    v-if="checkPermi(['admin:user:update:level'])"
                    >修改用户等级</el-dropdown-item
                  >
                  <el-dropdown-item
                    @click.native="onTeamLevel(item.uid, item.teamLevel)"
                    v-if="checkPermi(['admin:user:update:level'])"
                    >修改团队等级</el-dropdown-item
                  >
                  <el-dropdown-item
                    @click.native="setPassword(item)"
                    v-if="checkPermi(['admin:user:update:password'])"
                    >修改登录密码</el-dropdown-item
                  >
                  <el-dropdown-item
                    @click.native="setExtension(item)"
                    v-if="checkPermi(['admin:user:update:spread'])"
                    >修改推荐人</el-dropdown-item
                  >
                  <el-dropdown-item
                    @click.native="clearSpread(item)"
                    v-if="checkPermi(['admin:retail:spread:clean'])"
                    >清除上级推广人</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>

          <div class="list-empty" v-if="!listLoading && !tableData.data.length">
            <i class="el-icon-document"></i>
            <p>暂无用户数据</p>
          </div>
        </div>
      </div>
      <div class="block">
        <el-pagination
          :page-sizes="[20, 40, 60, 80]"
          :page-size="userFrom.limit"
          :current-page="userFrom.page"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tableData.total"
          @size-change="handleSizeChange"
          @current-change="pageChange"
          background
        />
      </div>
    </el-card>
    <!--修改推荐人-->
    <el-dialog title="修改推荐人" :visible.sync="extensionVisible" width="540px" :before-close="handleCloseExtension">
      <el-form
        class="formExtension"
        ref="formExtension"
        :model="formExtension"
        :rules="ruleInline"
        label-width="95px"
        @submit.native.prevent
        v-loading="loading"
      >
        <el-form-item label="推荐人账户：" prop="spreadUid">
          <div v-if="formExtension.spreadUid" class="spread-selected">
            <img v-if="formExtension.image" class="spread-avatar" :src="formExtension.image" />
            <div v-else class="spread-avatar spread-avatar--empty">
              <i class="el-icon-user" />
            </div>
            <div class="spread-info">
              <div class="spread-nick">{{ formExtension.spreadNickname || '未命名用户' }}</div>
              <div class="spread-uid">ID：{{ formExtension.spreadUid }}</div>
            </div>
            <div class="spread-ops">
              <el-button type="text" @click="openUserPicker">重新选择</el-button>
              <el-button type="text" class="danger-text" @click="clearSpreadUid">清除</el-button>
            </div>
          </div>
          <el-button v-else icon="el-icon-search" @click="openUserPicker">点击选择推荐人账户</el-button>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="extensionVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubExtension('formExtension')">确定</el-button>
      </span>
    </el-dialog>
    <!--选择推荐人账户-->
    <el-dialog class="user-dialog" title="选择推荐人账户" :visible.sync="userVisible" width="1000px">
      <user-list @closeDialog="userVisible = false" v-if="userVisible" @getTemplateRow="getTemplateRow"></user-list>
    </el-dialog>
    <!--批量设置-->
    <el-dialog title="设置" :visible.sync="dialogVisible" width="540px" :before-close="handleClose">
      <el-form
        :model="dynamicValidateForm"
        ref="dynamicValidateForm"
        label-width="75px"
        class="demo-dynamic"
        v-loading="loading"
      >
        <el-form-item
          prop="groupId"
          label="用户分组："
          :rules="[{ required: true, message: '请选择用户分组', trigger: 'change' }]"
          v-if="batchName === 'group'"
          key="1"
        >
          <el-select v-model="dynamicValidateForm.groupId" placeholder="请选择分组" style="width: 100%" filterable>
            <el-option
              :value="item.id"
              v-for="(item, index) in groupList"
              :key="index"
              :label="item.groupName"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          prop="groupId"
          label="用户标签："
          :rules="[{ required: true, message: '请选择用户标签', trigger: 'change' }]"
          v-else
        >
          <el-select
            v-model="dynamicValidateForm.groupId"
            placeholder="请选择标签"
            style="width: 100%"
            multiple
            filterable
          >
            <el-option :value="item.id" v-for="(item, index) in labelLists" :key="index" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm('dynamicValidateForm')">确定</el-button>
      </span>
    </el-dialog>
    <!--编辑-->
    <el-dialog title="编辑" :visible.sync="visible" width="900px">
      <edit-from v-if="visible" :uid="uid" @resetForm="resetForm"></edit-from>
    </el-dialog>
    <!--账户充减（余额 / 积分 / 佣金）-->
    <el-dialog
      title="账户充减"
      :visible.sync="VisiblePoint"
      width="540px"
      :close-on-click-modal="false"
      :before-close="handlePointClose"
    >
      <el-form
        :model="PointValidateForm"
        ref="PointValidateForm"
        label-width="100px"
        class="demo-dynamic"
        v-loading="loadingPoint"
      >
        <el-form-item label="修改余额：" required>
          <el-radio-group v-model="PointValidateForm.moneyType">
            <el-radio :label="1">增加</el-radio>
            <el-radio :label="2">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="余额：" required>
          <el-input-number
            controls-position="right"
            type="text"
            v-model="PointValidateForm.moneyValue"
            :precision="2"
            :step="0.1"
            :min="0"
            :max="999999"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="修改积分：" required>
          <el-radio-group v-model="PointValidateForm.integralType">
            <el-radio :label="1">增加</el-radio>
            <el-radio :label="2">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分：" required>
          <el-input-number
            controls-position="right"
            type="text"
            step-strictly
            v-model="PointValidateForm.integralValue"
            :min="0"
            :max="999999"
          ></el-input-number>
        </el-form-item>
        <el-divider />
        <el-form-item label="修改佣金：" required>
          <el-radio-group v-model="PointValidateForm.brokerageType">
            <el-radio :label="1">增加</el-radio>
            <el-radio :label="2">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="佣金金额：" required>
          <el-input-number
            controls-position="right"
            v-model="PointValidateForm.brokerageValue"
            :precision="2"
            :step="0.1"
            :min="0"
            :max="999999"
          ></el-input-number>
          <div class="point-tip">填 0 表示本次不调整佣金</div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handlePointClose">取消</el-button>
        <el-button type="primary" :loading="loadingBtn" @click="submitPointForm('PointValidateForm')">确定</el-button>
      </span>
    </el-dialog>
    <!--账户详情-->
    <user-details ref="userDetailFrom" :userNo="uid"></user-details>
    <!-- 用户等级 -->
    <el-dialog title="修改用户等级" :visible.sync="levelVisible" width="540px" :before-close="Close">
      <level-edit :levelInfo="levelInfo" :levelList="levelList"></level-edit>
    </el-dialog>
    <!-- 用户团队等级 -->
    <el-dialog title="修改用户团队等级" :visible.sync="teamLevelVisible" width="540px" :before-close="CloseTeamLevel">
      <team-level-edit :teamLevelInfo="teamLevelInfo" :teamLevelList="teamLevelList"></team-level-edit>
    </el-dialog>
  </div>
</template>

<script>
import {
  userListApi,
  groupListApi,
  levelListApi,
  tagListApi,
  groupPiApi,
  tagPiApi,
  foundsApi,
  brokerageApi,
  updateSpreadApi,
  updatePhoneApi,
  updatePasswordApi,
  userUpdateApi,
} from '@/api/user';
import { teamLevelAllApi } from '@/api/teamLevel';
import { spreadClearApi } from '@/api/distribution';
import editFrom from './edit';
import userDetails from './userDetails';
import levelEdit from './level';
import teamLevelEdit from './teamLevel';
import userList from '@/components/userList';
import * as logistics from '@/api/logistics.js';
import Cookies from 'js-cookie';
import { checkPermi } from '@/utils/permission'; // 权限判断函数
import { Debounce } from '@/utils/validate';

// 后端 UserUpdateSpreadRequest.image 标注了 @NotBlank，但 editSpread 实现里并未使用该字段。
// 所选推荐人没有头像时补一个 1x1 透明 PNG 占位，避免后端校验失败。
const DEFAULT_AVATAR_PLACEHOLDER =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';

export default {
  name: 'UserIndex',
  components: { editFrom, userDetails, userList, levelEdit, teamLevelEdit },
  filters: {
    sexFilter(status) {
      const statusMap = {
        0: '未知',
        1: '男',
        2: '女',
        3: '保密',
      };
      return statusMap[status];
    },
  },
  data() {
    return {
      formExtension: {
        image: '',
        spreadUid: '',
        spreadNickname: '',
        userId: '',
      },
      ruleInline: {
        spreadUid: [{ required: true, message: '请选择推荐人账户', trigger: 'change' }],
      },
      extensionVisible: false,
      userVisible: false,
      levelInfo: {},
      teamLevelInfo: {},
      pickerOptions: this.$timeOptions,
      loadingBtn: false,
      PointValidateForm: {
        integralType: 2,
        integralValue: 0,
        moneyType: 2,
        moneyValue: 0,
        brokerageType: 1,
        brokerageValue: 0,
        uid: '',
      },
      loadingPoint: false,
      VisiblePoint: false,
      visible: false,
      userIds: '',
      dialogVisible: false,
      levelVisible: false,
      teamLevelVisible: false,
      groupData: [],
      labelData: [],
      levelList: [],
      teamLevelList: [],
      selData: [],
      labelPosition: 'right',
      collapse: false,
      props: {
        children: 'child',
        label: 'name',
        value: 'name',
        emitPath: false,
      },
      propsCity: {
        children: 'child',
        label: 'name',
        value: 'name',
      },
      headeNum: [
        { type: '', name: '全部用户' },
        { type: 'wechat', name: '微信公众号用户' },
        { type: 'routine', name: '微信小程序用户' },
        { type: 'h5', name: 'H5用户' },
      ],
      listLoading: true,
      tableData: {
        data: [],
        total: 0,
      },
      loginType: '0',
      userFrom: {
        searchType: 'all',
        content: '',
        labelId: '',
        userType: '',
        sex: '',
        isPromoter: '',
        country: '',
        payCount: '',
        accessType: '',
        dateLimit: '',
        keywords: '',
        province: '',
        city: '',
        page: 1,
        limit: 20,
        groupId: '',
      },
      grid: {
        xl: 8,
        lg: 12,
        md: 12,
        sm: 24,
        xs: 24,
      },
      labelLists: [],
      groupList: [],
      selectedData: [],
      timeVal: [],
      addresData: [],
      dynamicValidateForm: {
        groupId: [],
      },
      loading: false,
      groupIdFrom: [],
      selectionList: [],
      batchName: '',
      uid: 0,
      keyNum: 0,
      address: [],
      multipleSelectionAll: [],
      idKey: 'uid',
      card_select_show: false,
      checkAll: false,
      checkedCities: ['ID', '头像', '姓名', '分组', '推荐人', '手机号', '余额', '积分', '团队等级'],
      columnData: ['ID', '头像', '姓名', '分组', '推荐人', '手机号', '余额', '积分', '团队等级'],
      isIndeterminate: true,
    };
  },
  computed: {
    // 是否注销
    isRedFont() {
      return (info) => {
        if (info.isLogoff) {
          return 'red-fonts';
        } else {
          return '';
        }
      };
    },
  },
  activated() {
    this.userFrom.keywords = '';
    this.loginType = '0';
    this.getList(1);
  },
  mounted() {
    this.getList();
    this.groupLists();
    this.levelLists();
    this.teamLevelLists();
    this.getTagList();
    if (checkPermi(['admin:system:city:list:tree'])) this.getCityList();
  },
  methods: {
    checkPermi,
    // 一键复制：优先 Clipboard API，非 https 或旧浏览器降级到 execCommand
    copyText(text) {
      const value = String(text);
      const done = () => this.$message.success('已复制：' + value);
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(done).catch(() => this.fallbackCopy(value, done));
      } else {
        this.fallbackCopy(value, done);
      }
    },
    fallbackCopy(value, done) {
      const input = document.createElement('textarea');
      input.value = value;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.top = '-9999px';
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        this.$message.error('复制失败，请手动复制');
      }
      document.body.removeChild(input);
    },
    // 卡片多选
    onCheckItem(row) {
      if (row._checked) {
        if (!this.selectionList.some((i) => i.uid === row.uid)) this.selectionList.push(row);
      } else {
        this.selectionList = this.selectionList.filter((i) => i.uid !== row.uid);
      }
      this.changePageCoreRecordData();
      const data = [];
      if (this.multipleSelectionAll.length) {
        this.multipleSelectionAll.map((item) => {
          data.push(item.uid);
        });
      }
      this.userIds = data.join(',');
    },
    // 用户等级名称
    matchLevelName(levelId) {
      const hit = (this.levelList || []).find((i) => Number(i.id) === Number(levelId));
      return (hit && hit.name) || '普通会员';
    },
    // 用户来源文案
    userTypeText(type) {
      const map = { wechat: '微信公众号', routine: '微信小程序', h5: 'H5' };
      return map[type] || '未知来源';
    },
    // 启用 / 禁用用户
    onStatusChange(row) {
      const next = row.status;
      userUpdateApi({ id: row.uid }, { status: next, isPromoter: !!row.isPromoter })
        .then(() => {
          this.$message.success(next ? '已启用该用户' : '已禁用该用户');
        })
        .catch(() => {
          row.status = !next;
        });
    },
    setPhone(row) {
      this.$prompt('', '修改手机号', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputErrorMessage: '请输入修改手机号',
        inputType: 'text',
        inputValue: row.phone,
        inputPlaceholder: '请输入手机号',
        closeOnClickModal: false,
        inputValidator: (value) => {
          if (!value) return '请填写手机号';
        },
      })
        .then(({ value }) => {
          updatePhoneApi({ id: row.uid, phone: value }).then(() => {
            this.$message.success('编辑成功');
            this.getList();
          });
        })
        .catch(() => {
          this.$message.info('取消输入');
        });
    },
    // 修改登录密码（管理员直接设置新密码）
    setPassword(row) {
      this.$prompt('', '修改【' + (row.nickname || row.phone || row.uid) + '】的登录密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputErrorMessage: '请输入6-18位新密码',
        inputType: 'password',
        inputValue: '',
        inputPlaceholder: '请输入6-18位新密码',
        closeOnClickModal: false,
        inputValidator: (value) => {
          if (!value) return '请填写新密码';
          if (value.length < 6 || value.length > 18) return '密码长度需为6-18位';
        },
      })
        .then(({ value }) => {
          updatePasswordApi({ uid: row.uid, password: value }).then(() => {
            this.$message.success('修改成功');
            this.getList();
          });
        })
        .catch(() => {
          this.$message.info('取消输入');
        });
    },
    // 清除
    clearSpread(row) {
      this.$modalSure('解除【' + row.nickname + '】的上级推广人吗').then(() => {
        spreadClearApi(row.uid).then((res) => {
          this.$message.success('清除成功');
          this.getList();
        });
      });
    },
    onSubExtension(formName) {
      if (!this.formExtension.spreadUid) {
        this.$message.warning('请选择推荐人账户');
        return;
      }
      if (this.formExtension.spreadUid === this.formExtension.userId) {
        this.$message.warning('推荐人不能是用户本人');
        return;
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          updateSpreadApi({
            userId: this.formExtension.userId,
            image: this.formExtension.image || DEFAULT_AVATAR_PLACEHOLDER,
            spreadUid: this.formExtension.spreadUid,
          }).then((res) => {
            this.$message.success('设置成功');
            this.extensionVisible = false;
            this.getList();
          });
        } else {
          return false;
        }
      });
    },
    // 选人弹窗回填推荐人
    getTemplateRow(row) {
      this.formExtension.image = row.avatar || '';
      this.formExtension.spreadUid = row.uid;
      this.formExtension.spreadNickname = row.nickname || '未命名用户';
      this.userVisible = false;
      this.$nextTick(() => {
        this.$refs.formExtension && this.$refs.formExtension.validateField('spreadUid');
      });
    },
    setExtension(row) {
      this.formExtension = {
        image: '',
        spreadUid: '',
        spreadNickname: '',
        userId: row.uid,
      };
      this.extensionVisible = true;
      this.$nextTick(() => {
        this.$refs.formExtension && this.$refs.formExtension.clearValidate();
      });
    },
    handleCloseExtension() {
      this.extensionVisible = false;
    },
    // 打开「选择推荐人账户」选人弹窗
    openUserPicker() {
      this.userVisible = true;
    },
    // 清除已选推荐人
    clearSpreadUid() {
      this.formExtension.image = '';
      this.formExtension.spreadUid = '';
      this.formExtension.spreadNickname = '';
      this.$nextTick(() => {
        this.$refs.formExtension && this.$refs.formExtension.clearValidate('spreadUid');
      });
    },
    resetForm() {
      this.visible = false;
    },
    reset(formName) {
      this.userFrom = {
        searchType: 'all',
        content: '',
        labelId: '',
        userType: '',
        sex: '',
        isPromoter: '',
        country: '',
        payCount: '',
        accessType: '',
        dateLimit: '',
        keywords: '',
        province: '',
        city: '',
        page: 1,
        limit: 20,
        groupId: '',
      };
      this.address = [];
      this.groupData = [];
      this.labelData = [];
      this.timeVal = [];
      this.$refs.userSearchInput.clearInput(); // 清空用户搜索输入框
      this.getList();
    },
    // 列表
    async getCityList() {
      let res = await logistics.cityListTree();
      //res.forEach((el, index) => {
      //     el.child.forEach((cel, j) => {
      //       delete cel.child
      //     })
      //   })
      this.addresData = res;
      // })
    },
    // 发送文章
    sendNews() {
      if (this.selectionList.length === 0) return this.$message.warning('请先选择用户');
      const _this = this;
      this.$modalArticle(function (row) {}, 'send');
    },
    // 发送优惠劵
    onSend() {
      if (this.selectionList.length === 0) return this.$message.warning('请选择要设置的用户');
      const _this = this;
      this.$modalCoupon(
        'send',
        (this.keyNum += 1),
        [],
        function (row) {
          _this.formValidate.give_coupon_ids = [];
          _this.couponData = [];
          row.map((item) => {
            _this.formValidate.give_coupon_ids.push(item.coupon_id);
            _this.couponData.push(item.title);
          });
          _this.selectionList = [];
        },
        this.userIds,
        'user',
      );
    },
    // 账户详情
    onDetails(id) {
      this.uid = id;
      this.$refs.userDetailFrom.getUserDetail(id);
      this.$refs.userDetailFrom.dialogUserDetail = true;
    },
    Close() {
      this.levelVisible = false;
    },
    // 会员等级
    onLevel(uid, level) {
      this.levelInfo = {
        uid: uid,
        level: level || 0,
      };
      const current = (this.levelList || []).find((item) => Number(item.id) === Number(level));
      if (current) {
        this.levelInfo.gradeLevel = current.grade;
      }
      this.levelVisible = true;
    },
    levelLists() {
      levelListApi()
        .then((res) => {
          this.levelList = res || [];
        })
        .catch(() => {
          this.levelList = [];
        });
    },
    CloseTeamLevel() {
      this.teamLevelVisible = false;
    },
    // 团队等级
    onTeamLevel(uid, teamLevel) {
      this.teamLevelInfo = {
        uid: uid,
        teamLevel: teamLevel || 0,
      };
      this.teamLevelVisible = true;
    },
    // 账户充减
    editPoint(id) {
      this.uid = id;
      this.VisiblePoint = true;
    },
    // 账户充减（余额 / 积分 / 佣金）
    submitPointForm: Debounce(function (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const { moneyType, moneyValue, integralType, integralValue, brokerageType, brokerageValue } =
            this.PointValidateForm;
          this.loadingBtn = true;
          foundsApi({
            uid: this.uid,
            moneyType,
            moneyValue,
            integralType,
            integralValue,
          })
            .then(() => {
              // 佣金金额大于 0 时才调整佣金
              if (brokerageValue > 0) {
                return brokerageApi({
                  uid: this.uid,
                  brokerageType,
                  brokerageValue,
                });
              }
              return null;
            })
            .then(() => {
              this.$message.success('设置成功');
              this.loadingBtn = false;
              this.handlePointClose();
              this.getList();
            })
            .catch(() => {
              this.loadingBtn = false;
            });
        } else {
          return false;
        }
      });
    }),
    // 账户充减
    handlePointClose() {
      this.VisiblePoint = false;
      this.PointValidateForm = {
        integralType: 2,
        integralValue: 0,
        moneyType: 2,
        moneyValue: 0,
        brokerageType: 1,
        brokerageValue: 0,
        uid: '',
      };
    },
    editUser(id) {
      this.uid = id;
      this.visible = true;
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.loading = true;
          this.batchName === 'group'
            ? groupPiApi({ groupId: this.dynamicValidateForm.groupId, id: this.userIds })
                .then((res) => {
                  this.$message.success('设置成功');
                  this.loading = false;
                  this.handleClose();
                  this.getList();
                })
                .catch(() => {
                  this.loading = false;
                })
            : tagPiApi({ tagId: this.dynamicValidateForm.groupId.join(','), id: this.userIds })
                .then((res) => {
                  this.$message.success('设置成功');
                  this.loading = false;
                  this.handleClose();
                  this.getList();
                })
                .catch(() => {
                  this.loading = false;
                });
        } else {
          return false;
        }
      });
    },
    setBatch(name, row) {
      this.batchName = name;
      if (row) {
        this.userIds = row.uid;
        if (this.batchName === 'group') {
          this.dynamicValidateForm.groupId = row.groupId ? Number(row.groupId) : '';
        } else {
          this.dynamicValidateForm.groupId = row.tagId ? row.tagId.split(',').map(Number) : [];
        }
      } else {
        this.dynamicValidateForm.groupId = '';
      }
      if (this.multipleSelectionAll.length === 0 && !row) return this.$message.warning('请选择要设置的用户');
      this.dialogVisible = true;
    },
    handleClose() {
      this.dialogVisible = false;
      this.$refs['dynamicValidateForm'].resetFields();
    },
    // 全选
    onSelectTab(selection) {
      this.selectionList = selection;
      setTimeout(() => {
        this.changePageCoreRecordData();
        let data = [];
        if (this.multipleSelectionAll.length) {
          this.multipleSelectionAll.map((item) => {
            data.push(item.uid);
          });
          this.userIds = data.join(',');
        }
      }, 50);
    },
    // 搜索
    userSearchs() {
      this.userFrom.page = 1;
      this.getList();
    },
    // 选择国家
    changeCountry() {
      if (this.userFrom.country === 'OTHER' || !this.userFrom.country) {
        this.selectedData = [];
        this.userFrom.province = '';
        this.userFrom.city = '';
        this.address = [];
      }
    },
    // 选择地址
    handleChange(value) {
      this.userFrom.province = value[0];
      this.userFrom.city = value[1];
      this.userSearchs();
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e;
      this.userFrom.dateLimit = e ? this.timeVal.join(',') : '';
      this.userSearchs();
    },
    // 分组列表
    groupLists() {
      groupListApi({ page: 1, limit: 9999 }).then(async (res) => {
        this.groupList = res.list;
      });
    },
    //标签列表
    getTagList() {
      tagListApi({ page: 1, limit: 9999 }).then((res) => {
        this.labelLists = res.list;
      });
    },
    // 团队等级列表
    teamLevelLists() {
      teamLevelAllApi()
        .then((res) => {
          this.teamLevelList = res || [];
        })
        .catch(() => {
          this.teamLevelList = [];
        });
    },
    matchTeamLevelName(teamLevelId) {
      if (teamLevelId === undefined || teamLevelId === null || teamLevelId === '' || Number(teamLevelId) === 0) {
        return '-';
      }
      const level = (this.teamLevelList || []).find((item) => Number(item.id) === Number(teamLevelId));
      if (!level) return '-';
      return level.grade ? `${level.name}（Lv${level.grade}）` : level.name;
    },
    // 列表
    getList(num) {
      this.listLoading = true;
      this.userFrom.page = num ? num : this.userFrom.page;
      this.userFrom.userType = this.loginType;
      if (this.loginType == 0) this.userFrom.userType = '';
      this.userFrom.groupId = this.groupData.join(',');
      this.userFrom.labelId = this.labelData.join(',');
      userListApi(this.userFrom)
        .then((res) => {
          this.tableData.data = res.list;
          this.tableData.total = res.total;
          this.$nextTick(function () {
            this.setSelectRow(); // 调用跨页选中方法
          });
          this.listLoading = false;
        })
        .catch(() => {
          this.listLoading = false;
        });
      this.checkedCities = this.$cache.local.has('user_stroge')
        ? this.$cache.local.getJSON('user_stroge')
        : this.checkedCities;
      // 合并新增列，避免旧缓存隐藏团队等级列
      const merged = Array.from(new Set([...(this.checkedCities || []), '团队等级']));
      this.checkedCities = merged.filter((item) => this.columnData.includes(item));
      this.$cache.local.setJSON('user_stroge', this.checkedCities);
      this.$set(this, 'card_select_show', false);
    },
    // 设置选中的方法（卡片多选回填）
    setSelectRow() {
      const idKey = this.idKey;
      const selectAllIds = (this.multipleSelectionAll || []).map((row) => row[idKey]);
      (this.tableData.data || []).forEach((row) => {
        this.$set(row, '_checked', selectAllIds.indexOf(row[idKey]) >= 0);
      });
    },
    // 记忆选择核心方法
    changePageCoreRecordData() {
      // 标识当前行的唯一键的名称
      const idKey = this.idKey;
      const that = this;
      // 如果总记忆中还没有选择的数据，那么就直接取当前页选中的数据，不需要后面一系列计算
      if (this.multipleSelectionAll.length <= 0) {
        this.multipleSelectionAll = this.selectionList;
        return;
      }
      // 总选择里面的key集合
      const selectAllIds = [];
      this.multipleSelectionAll.forEach((row) => {
        selectAllIds.push(row[idKey]);
      });
      const selectIds = [];
      // 获取当前页选中的id
      this.selectionList.forEach((row) => {
        selectIds.push(row[idKey]);
        // 如果总选择里面不包含当前页选中的数据，那么就加入到总选择集合里
        if (selectAllIds.indexOf(row[idKey]) < 0) {
          that.multipleSelectionAll.push(row);
        }
      });
      const noSelectIds = [];
      // 得到当前页没有选中的id
      this.tableData.data.forEach((row) => {
        if (selectIds.indexOf(row[idKey]) < 0) {
          noSelectIds.push(row[idKey]);
        }
      });
      noSelectIds.forEach((uid) => {
        if (selectAllIds.indexOf(uid) >= 0) {
          for (let i = 0; i < that.multipleSelectionAll.length; i++) {
            if (that.multipleSelectionAll[i][idKey] == uid) {
              // 如果总选择中有未被选中的，那么就删除这条
              that.multipleSelectionAll.splice(i, 1);
              break;
            }
          }
        }
      });
    },
    pageChange(page) {
      this.changePageCoreRecordData();
      this.userFrom.page = page;
      this.getList();
    },
    handleSizeChange(val) {
      this.changePageCoreRecordData();
      this.userFrom.limit = val;
      this.getList();
    },
    // 删除
    handleDelete(id, idx) {
      this.$modalSure().then(() => {
        productDeleteApi(id).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
    onchangeIsShow(row) {
      row.isShow
        ? putOnShellApi(row.id)
            .then(() => {
              this.$message.success('上架成功');
              this.getList();
            })
            .catch(() => {
              row.isShow = !row.isShow;
            })
        : offShellApi(row.id)
            .then(() => {
              this.$message.success('下架成功');
              this.getList();
            })
            .catch(() => {
              row.isShow = !row.isShow;
            });
    },
    handleCheckAllChange(val) {
      this.checkedCities = val ? this.columnData : [];
      this.isIndeterminate = false;
    },
    handleCheckedCitiesChange(value) {
      let checkedCount = value.length;
      this.checkAll = checkedCount === this.columnData.length;
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.columnData.length;
    },
    checkSave() {
      this.card_select_show = false;
      this.$modal.loading('正在保存到本地，请稍候...');
      this.$cache.local.setJSON('user_stroge', this.checkedCities);
      setTimeout(this.$modal.closeLoading(), 1000);
    },
  },
};
</script>

<style scoped lang="scss">
/*.timeBox{*/
/*width: 100%;*/
/*::v-deep.el-form-item__content{*/
/*width: 87% !important;*/
/*}*/
/*}*/
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.text-right {
  text-align: right;
}

.point-tip {
  display: inline-block;
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 32px;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 33.33%;
}

.seachTiele {
  line-height: 30px;
}

.container {
  min-width: 821px;

  ::v-deepel-form-item {
    width: 100%;
  }

  ::v-deepel-form-item__content {
    width: 72%;
  }
}

.ivu-ml-8 {
  font-size: 12px;
  color: var(--prev-color-primary) !important;
}

.relative {
  position: relative;
}

.card_abs {
  position: absolute;
  padding-bottom: 15px;
  right: 40px;
  width: 200px;
  background: #fff;
  z-index: 99999;
  box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
}

.cell_ht {
  height: 50px;
  padding: 15px 20px;
  box-sizing: border-box;
  border-bottom: 1px solid #eeeeee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.check_cell {
  width: 100%;
  padding: 15px 20px 0;
}

::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #606266;
}
::v-deep .user-dialog .el-dialog__body {
  padding: 0;
  height: 600px;
}
.search-box {
  // position: absolute;
  // top: 20px;
  // right: 0;
  .search-btn-group-box {
    flex-shrink: 0;
  }
}
::v-deep .search-form {
  position: relative;
}
.search-form-sub-bottom {
  position: absolute;
  right: 0;
  bottom: 0;
}
.red-fonts {
  color: #ed4014;
}
.flex-between {
  justify-content: space-between;
}

/* 说明：筛选区、列表表格、键值行、状态标签等通用样式已提取到
   src/theme/list-page.scss（全站列表页基座），本文件只保留业务特有样式。 */

/* 分类 tab 所在的卡片头内边距 */
::v-deep .box-card > .el-card__header {
  padding: 0 20px;
  border-bottom: 1px solid #f0f2f5;
}

/* 本页列宽：复选 / 会员 / 会员信息 / 资金 / 下级与消费 / 状态 / 操作
   通过 --list-cols 注入，表头与数据行共用（基座里的 .list-head / .list-row 读取它） */
.list-table {
  --list-cols: 40px minmax(170px, 1.1fr) minmax(280px, 1.8fr) minmax(140px, 0.85fr) minmax(200px, 1.25fr)
    minmax(112px, 0.7fr) minmax(205px, 1.2fr);
}

.mi-check {
  display: flex;
  justify-content: flex-start;
  padding-right: 0;
}

/* 会员列：头像 + 昵称 + 标签 + 来源时间 */
.mi-member {
  display: flex;
  align-items: flex-start;
}

.member-avatar {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #f5f7fa;
}

.member-base {
  margin-left: 12px;
  min-width: 0;
  flex: 1;
}

/* 标签本体样式见全局 .status-tag */
.member-tags {
  margin: 0 0 8px;
}

.member-sub {
  font-size: 13px;
  line-height: 21px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* .kv（键值行）、.list-empty（空态）、.inline-status（状态文字）均见全局 list-page.scss */

.mi-status {
  display: flex;
  align-items: center;
}

/* 作为 grid 单元格只保留右侧间距；
   按钮间距与配色统一交给全局 .op-bar / .op-btn（src/theme/element.scss），此处不再重复定义 */
.mi-ops {
  padding-right: 0;
}

/* 选择推荐人账户 —— 已选中的账户卡片 */
.spread-selected {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;

  .spread-avatar {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    margin-right: 10px;
    border-radius: 50%;
    object-fit: cover;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      color: #c0c4cc;
      background: #f0f0f0;
    }
  }

  .spread-info {
    flex: 1;
    min-width: 0;
    line-height: 20px;

    .spread-nick {
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .spread-uid {
      font-size: 12px;
      color: #909399;
    }
  }

  .spread-ops {
    flex-shrink: 0;
    margin-left: 10px;
  }
}

.danger-text {
  color: #f56c6c !important;
}
</style>
