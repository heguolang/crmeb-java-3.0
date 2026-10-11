<template>
  <div class="divBox chain-config">
    <el-card v-loading="loading" shadow="never">
      <div slot="header" class="cfg-head">
        <div>
          <span class="cfg-title">2+1 链动配置</span>
          <span class="cfg-sub">改完记得点击右上角「保存全部设置」</span>
        </div>
        <el-button type="primary" size="small" @click="save">保存全部设置</el-button>
      </div>

      <!-- ① 基础设置 -->
      <div class="grp">
        <div class="grp-h"><i>1</i>基础设置</div>
        <el-form label-width="180px" size="small">
          <el-form-item label="链动功能开关">
            <el-radio-group v-model="form.chain_func_status">
              <el-radio label="1">开启</el-radio>
              <el-radio label="0">关闭</el-radio>
            </el-radio-group>
            <span class="tip">关闭后会员端链动中心提示未开启，且不再产生任何链动奖励</span>
          </el-form-item>
          <el-form-item label="激活门槛价(元)">
            <el-input-number v-model="form.chain_active_threshold" :min="0" :precision="2" :step="10" />
            <span class="tip">订单实付 ≥ 该值才算有效订单（参与升级与分润）；0 = 不限</span>
          </el-form-item>
          <el-form-item label="售后保护期(天)">
            <el-input-number v-model="form.chain_after_sale_days" :min="0" :max="90" />
            <span class="tip">
              支付满 N 天且无退款才计入链动；0 = 支付即生效。
              <b class="warn">设 &gt; 0 时，订单不会在支付瞬间结算，由每日定时任务在保护期满后统一结算</b>
            </span>
          </el-form-item>
          <el-form-item label="有效订单判定时机">
            <el-radio-group v-model="form.chain_effective_timing">
              <el-radio label="1">支付成功</el-radio>
              <el-radio label="2">订单完成</el-radio>
            </el-radio-group>
            <span class="tip">用于判定「有效代理」，影响何时触发升老板</span>
          </el-form-item>
          <el-form-item label="佣金到账时机">
            <el-radio-group v-model="form.chain_credit_timing">
              <el-radio label="1">支付成功</el-radio>
              <el-radio label="2">订单完成</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="佣金冻结天数">
            <el-input-number v-model="form.chain_freeze_days" :min="0" :max="365" />
            <span class="tip">0 = 不冻结；&gt; 0 时佣金进入冻结，到期转为可提现</span>
          </el-form-item>
          <el-form-item label="礼包订单跳过普通分销">
            <el-radio-group v-model="form.chain_skip_normal_brokerage">
              <el-radio label="1">跳过</el-radio>
              <el-radio label="0">不跳过</el-radio>
            </el-radio-group>
            <span class="tip">跳过可避免链动奖励与普通分销/团队奖叠加</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- ② 身份与升级 -->
      <div class="grp">
        <div class="grp-h"><i>2</i>身份与升级</div>
        <el-form label-width="180px" size="small">
          <el-form-item label="级别名称：代理">
            <el-input v-model="form.chain_level_agent_name" maxlength="6" show-word-limit class="midWidth" placeholder="代理" />
            <span class="tip">可自定义，如「VIP代理」「创客」。会员端与后台统一显示该名称</span>
          </el-form-item>
          <el-form-item label="级别名称：老板">
            <el-input v-model="form.chain_level_boss_name" maxlength="6" show-word-limit class="midWidth" placeholder="老板" />
            <span class="tip">可自定义，如「合伙人」「服务商」</span>
          </el-form-item>
          <el-form-item label="直推位容量 N">
            <el-input-number v-model="form.chain_direct_capacity" :min="1" :max="10" />
            <span class="tip">2+1 的「2」：直推满 N 名有效代理自动晋升老板</span>
          </el-form-item>
          <el-form-item label="走人留人机制">
            <el-radio-group v-model="form.chain_leave_team_enabled">
              <el-radio label="1">开启</el-radio>
              <el-radio label="0">关闭</el-radio>
            </el-radio-group>
            <span class="tip">
              开启后：晋升老板时本人脱离原上级团队独立发展；升级前的前 N 名直推继续留在原上级名下继续贡献见点奖，
              第 N+1 名起随本人走。关闭则等同于普通直推关系
            </span>
          </el-form-item>
          <el-form-item label="退款是否降级身份">
            <el-radio-group v-model="form.chain_identity_downgrade">
              <el-radio label="0">否</el-radio>
              <el-radio label="1">是</el-radio>
            </el-radio-group>
            <span class="tip">默认「否」：身份只升不降，退款不回收身份</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- ③ 奖励设置 -->
      <div class="grp">
        <div class="grp-h"><i>3</i>奖励设置</div>
        <el-form label-width="180px" size="small">
          <el-form-item label="单笔分润上限(%)">
            <el-input-number v-model="form.chain_max_brokerage_ratio" :min="0" :max="100" :precision="2" />
            <span class="tip">单笔订单的链动分润合计不超过订单实付的该比例（默认 50%）</span>
          </el-form-item>
        </el-form>

        <el-table :data="rules" border size="small" class="rule-tb">
          <el-table-column label="奖励类型" width="130">
            <template slot-scope="scope">
              <el-tag :type="tagType(scope.row.type)" size="mini">{{ ruleLabel(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发放对象" width="150">
            <template slot-scope="scope">{{ payerLabel(scope.row.type) }}</template>
          </el-table-column>
          <el-table-column label="计算方式" width="220">
            <template slot-scope="scope">{{ calcLabel(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="固定金额(元)" width="150">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.amount"
                :min="0"
                :precision="2"
                :step="10"
                size="mini"
                :disabled="!isFixed(scope.row.type)"
              />
            </template>
          </el-table-column>
          <el-table-column label="比例(%)" width="150">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.ratio"
                :min="0"
                :max="100"
                :precision="2"
                :step="1"
                size="mini"
                :disabled="isFixed(scope.row.type)"
              />
            </template>
          </el-table-column>
          <el-table-column label="启用" width="90">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template slot-scope="scope">
              <el-input v-model="scope.row.remark" size="mini" placeholder="选填" />
            </template>
          </el-table-column>
        </el-table>
        <div class="rule-tip">
          直推奖 = 固定金额；见点奖 = 订单实付 × 比例（仅直属上级{{ bossName() }}，1 级）；
          平级奖 = 下级{{ bossName() }}的见点奖 × 比例（仅上级{{ bossName() }}，1 级）。
          <b>商品级奖励可在「链动产品」里按商品单独覆盖</b>。
        </div>
        <div style="margin-top: 12px">
          <el-button type="primary" size="small" @click="saveRules">保存奖励规则</el-button>
        </div>
      </div>

      <!-- ④ 帮扶基金 -->
      <div class="grp">
        <div class="grp-h"><i>4</i>帮扶基金</div>
        <el-form label-width="180px" size="small">
          <el-form-item label="冻结比例(%)">
            <el-input-number v-model="form.chain_help_freeze_ratio" :min="0" :max="100" :precision="2" />
            <span class="tip">每笔见点奖 / 平级奖按此比例进入帮扶基金冻结；0 = 关闭该机制</span>
          </el-form-item>
          <el-form-item label="解冻条件 N">
            <el-input-number v-model="form.chain_help_unlock_n" :min="0" :max="9999" />
            <span class="tip">留人座位「各自团队」累计有效订单数需达标（默认 10）</span>
          </el-form-item>
          <el-form-item label="解冻条件 M">
            <el-input-number v-model="form.chain_help_unlock_m" :min="0" :max="99999" />
            <span class="tip">老板「本人团队」累计有效订单数需达标（默认 20）</span>
          </el-form-item>
          <el-form-item label="解冻方式">
            <el-radio-group v-model="form.chain_help_release_mode">
              <el-radio label="1">条件达成一次性解冻</el-radio>
              <el-radio label="2">按订单完成比例逐笔释放</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-alert type="info" :closable="false" class="fb-alert">
            <template slot="title">
              解冻需同时满足：① 本人名下的留人座位均已晋升{{ bossName() }}；
              ② 每个留人座位的团队有效订单 ≥ N；③ 本人团队有效订单 ≥ M。
              三项达成后系统自动解冻，无需会员申请。
            </template>
          </el-alert>
        </el-form>
      </div>

      <!-- ⑤ 规则说明与通知 -->
      <div class="grp">
        <div class="grp-h"><i>5</i>规则说明与通知</div>
        <el-form label-width="180px" size="small">
          <el-form-item label="规则说明文案">
            <el-input
              v-model="form.chain_rules_content"
              type="textarea"
              :rows="6"
              class="selWidth"
              placeholder="留空则由系统按上面的配置自动生成文案（推荐留空）"
            />
          </el-form-item>
          <el-form-item label="站内通知">
            <el-radio-group v-model="form.chain_notify_status">
              <el-radio label="1">开启</el-radio>
              <el-radio label="0">关闭</el-radio>
            </el-radio-group>
            <span class="tip">升级、佣金到账、帮扶基金解冻的通知</span>
          </el-form-item>
          <el-divider content-position="left">团队分红（独立奖励，可单独关闭）</el-divider>
          <el-form-item label="团队分红开关">
            <el-radio-group v-model="form.chain_dividend_status">
              <el-radio label="1">开启</el-radio>
              <el-radio label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="分红比例(%)">
            <el-input-number v-model="form.chain_dividend_ratio" :min="0" :max="100" :precision="2" />
          </el-form-item>
          <el-form-item label="分红最低团队人数">
            <el-input-number v-model="form.chain_dividend_min_team" :min="1" :max="10000" />
          </el-form-item>
        </el-form>
      </div>

      <div class="foot">
        <el-button type="primary" @click="save">保存全部设置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { chainConfigGetApi, chainConfigSaveApi, chainRulesSaveApi } from '@/api/chain';

const STR_KEYS = ['chain_func_status', 'chain_gift_product_ids', 'chain_rules_content', 'chain_level_agent_name', 'chain_level_boss_name'];
const NUM_KEYS = [
  'chain_freeze_days',
  'chain_dividend_ratio',
  'chain_dividend_min_team',
  'chain_help_freeze_ratio',
];

const DEFAULT_FORM = {
  chain_func_status: '1',
  chain_gift_product_ids: '',
  chain_active_threshold: 399,
  chain_after_sale_days: 7,
  chain_effective_timing: '1',
  chain_credit_timing: '1',
  chain_freeze_days: 1,
  chain_skip_normal_brokerage: '1',
  chain_level_agent_name: '代理',
  chain_level_boss_name: '老板',
  chain_direct_capacity: 2,
  chain_leave_team_enabled: '1',
  chain_identity_downgrade: '0',
  chain_max_brokerage_ratio: 50,
  chain_help_freeze_ratio: 30,
  chain_help_unlock_n: 10,
  chain_help_unlock_m: 20,
  chain_help_release_mode: '1',
  chain_rules_content: '',
  chain_notify_status: '1',
  chain_dividend_status: '0',
  chain_dividend_ratio: 5,
  chain_dividend_min_team: 2,
};

export default {
  name: 'ChainConfig',
  data() {
    return {
      loading: false,
      form: { ...DEFAULT_FORM },
      rules: [],
    };
  },
  created() {
    this.load();
  },
  methods: {
    bossName() {
      return this.form.chain_level_boss_name || '老板';
    },
    isFixed(type) {
      return type === 'direct';
    },
    tagType(type) {
      return { direct: '', see: 'success', peer: 'warning' }[type] || 'info';
    },
    ruleLabel(type) {
      return { direct: '直推奖', see: '见点奖', peer: '平级奖' }[type] || type;
    },
    payerLabel(type) {
      const agent = this.form.chain_level_agent_name || '代理';
      const boss = this.form.chain_level_boss_name || '老板';
      return { direct: `${agent} / ${boss}`, see: `仅${boss}`, peer: `上级${boss}` }[type] || '-';
    },
    calcLabel(row) {
      if (row.type === 'direct') return '直推会员购买礼包，固定金额';
      if (row.type === 'see') return '团队有效订单实付 × 比例';
      if (row.type === 'peer') return `下级${this.bossName()}的见点奖 × 比例`;
      return '-';
    },
    load() {
      this.loading = true;
      chainConfigGetApi()
        .then((res) => {
          const data = (res && res.data) || res || {};
          const form = { ...DEFAULT_FORM };
          Object.keys(form).forEach((k) => {
            const v = data[k];
            if (v === undefined || v === null || v === '') return;
            if (STR_KEYS.indexOf(k) > -1) {
              form[k] = String(v);
            } else if (NUM_KEYS.indexOf(k) > -1) {
              form[k] = Number(v);
            } else if (k === 'chain_effective_timing' || k === 'chain_credit_timing' || k === 'chain_leave_team_enabled'
              || k === 'chain_identity_downgrade' || k === 'chain_skip_normal_brokerage' || k === 'chain_notify_status'
              || k === 'chain_dividend_status' || k === 'chain_help_release_mode') {
              form[k] = String(v);
            } else {
              form[k] = Number(v);
            }
          });
          this.form = form;
          this.rules = data.rules || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    save() {
      const payload = {};
      Object.keys(this.form).forEach((k) => {
        payload[k] = String(this.form[k] === undefined || this.form[k] === null ? '' : this.form[k]);
      });
      chainConfigSaveApi(payload).then(() => {
        this.$message.success('保存成功');
        this.load();
      });
    },
    saveRules() {
      chainRulesSaveApi(this.rules).then(() => {
        this.$message.success('奖励规则已保存');
        this.load();
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.chain-config {
  .cfg-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .cfg-title {
    font-size: 15px;
    font-weight: 600;
  }
  .cfg-sub {
    margin-left: 12px;
    font-size: 12px;
    color: #909399;
  }

  .grp {
    border: 1px solid #ebeef5;
    border-radius: 6px;
    padding: 16px 18px 4px;
    margin-bottom: 18px;
  }
  .grp-h {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 14px;
    i {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      border-radius: 5px;
      background: #2b5fdd;
      color: #fff;
      font-size: 12px;
      font-style: normal;
      margin-right: 8px;
    }
  }

  .tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;
    line-height: 1.6;
    &.warn { color: #e6a23c; }
  }

  .selWidth { width: 460px; }
  .midWidth { width: 220px; }

  .rule-tb { margin-top: 4px; }
  .rule-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #909399;
    line-height: 1.8;
    b { color: #2b5fdd; }
  }
  .fb-alert { margin: 4px 0 16px 180px; width: calc(100% - 200px); }
  .foot { text-align: center; padding: 6px 0 4px; }
}
</style>
