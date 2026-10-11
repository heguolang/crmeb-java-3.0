<template>
  <div class="divBox">
    <el-card v-loading="loading">
      <div slot="header" class="clearfix">
        <span>2+1 链动配置</span>
        <el-button style="float: right" type="primary" size="small" @click="save">保存</el-button>
      </div>
      <el-alert
        title="默认：礼包商品升代理；直推满N升老板；留下的N个代理各直推满X单解锁帮扶；未解锁见单按比例进帮扶基金；金额在下方奖励规则配置。"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form label-width="200px">
        <el-form-item label="链动开关">
          <el-radio-group v-model="form.chain_func_status">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="代理礼包商品ID">
          <el-input v-model="form.chain_gift_product_ids" placeholder="多个用英文逗号分隔；优先用「链动产品」菜单配置" class="selWidth" />
          <div style="color:#999;font-size:12px;margin-top:4px">推荐到「链动产品」里选商品并配置直推/见单/平级奖；保存产品后会自动同步到此处</div>
        </el-form-item>
        <el-form-item label="有效代理判定时机">
          <el-radio-group v-model="form.chain_effective_timing">
            <el-radio label="1">支付成功</el-radio>
            <el-radio label="2">订单完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="链动佣金到账时机">
          <el-radio-group v-model="form.chain_credit_timing">
            <el-radio label="1">支付成功</el-radio>
            <el-radio label="2">订单完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="冻结天数">
          <el-input-number v-model="form.chain_freeze_days" :min="0" :max="365" />
        </el-form-item>
        <el-form-item label="直推位容量N">
          <el-input-number v-model="form.chain_slide_capacity" :min="1" :max="10" />
          <span style="margin-left:8px;color:#999">2+1 的「2」：直推满 N 人升老板；满员后自动滑落</span>
        </el-form-item>
        <el-form-item label="帮扶解锁单数X">
          <el-input-number v-model="form.chain_help_unlock_orders" :min="1" :max="100" />
          <span style="margin-left:8px;color:#999">升老板后，留下的 N 个代理各再直推满 X 单礼包才解锁</span>
        </el-form-item>
        <el-form-item label="见单帮扶冻结%">
          <el-input-number v-model="form.chain_help_freeze_ratio" :min="0" :max="100" :precision="2" />
          <span style="margin-left:8px;color:#999">未解锁时见单按此比例进帮扶基金；0=关闭该机制；解锁后解冻且以后不再冻</span>
        </el-form-item>
        <el-form-item label="帮扶超时天数">
          <el-input-number v-model="form.chain_help_timeout_days" :min="0" :max="365" />
          <span style="margin-left:8px;color:#999">旧滑落帮扶超时释放，0=不超时</span>
        </el-form-item>
        <el-form-item label="见单奖代数">
          <el-input-number v-model="form.chain_see_generations" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="规则说明文案">
          <el-input v-model="form.chain_rules_content" type="textarea" :rows="5" class="selWidth" placeholder="前端规则页展示，留空用默认文案" />
        </el-form-item>
        <el-form-item label="退款降级身份">
          <el-radio-group v-model="form.chain_identity_downgrade">
            <el-radio label="0">否</el-radio>
            <el-radio label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="礼包跳过普通分销">
          <el-radio-group v-model="form.chain_skip_normal_brokerage">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider content-position="left">二期：风控 / 通知 / 分红</el-divider>
        <el-form-item label="风控开关">
          <el-radio-group v-model="form.chain_risk_status">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="风控时间窗(分钟)">
          <el-input-number v-model="form.chain_risk_window_minutes" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item label="同用户上限">
          <el-input-number v-model="form.chain_risk_max_uid" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="同手机上限">
          <el-input-number v-model="form.chain_risk_max_phone" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="同IP上限">
          <el-input-number v-model="form.chain_risk_max_ip" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="同设备上限">
          <el-input-number v-model="form.chain_risk_max_device" :min="1" :max="100" />
          <span style="margin-left:8px;color:#999">需前端传 Header: X-Device-Id</span>
        </el-form-item>
        <el-form-item label="站内通知">
          <el-radio-group v-model="form.chain_notify_status">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="团队分红">
          <el-radio-group v-model="form.chain_dividend_status">
            <el-radio label="1">开启</el-radio>
            <el-radio label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分红比例%">
          <el-input-number v-model="form.chain_dividend_ratio" :min="0" :max="100" :precision="2" />
        </el-form-item>
        <el-form-item label="分红最低团队人数">
          <el-input-number v-model="form.chain_dividend_min_team" :min="1" :max="1000" />
        </el-form-item>
      </el-form>

      <el-divider>奖励规则</el-divider>
      <el-table :data="rules" border size="small">
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="genLevel" label="代数" width="80" />
        <el-table-column label="固定金额" width="140">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.amount" :min="0" :precision="2" :step="1" size="mini" />
          </template>
        </el-table-column>
        <el-table-column label="比例%" width="140">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.ratio" :min="0" :precision="2" :step="1" size="mini" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 12px">
        <el-button type="primary" size="small" @click="saveRules">保存规则</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { chainConfigGetApi, chainConfigSaveApi, chainRulesSaveApi } from '@/api/chain';

export default {
  name: 'ChainConfig',
  data() {
    return {
      loading: false,
      form: {
        chain_func_status: '0',
        chain_gift_product_ids: '',
        chain_effective_timing: '1',
        chain_credit_timing: '1',
        chain_freeze_days: 1,
        chain_slide_enabled: '1',
        chain_slide_capacity: 2,
        chain_slide_depth: 2,
        chain_slide_mode: 'min_team',
        chain_help_unlock_orders: 1,
        chain_help_freeze_ratio: 50,
        chain_help_timeout_days: 0,
        chain_see_generations: 3,
        chain_identity_downgrade: '0',
        chain_skip_normal_brokerage: '1',
        chain_rules_content: '',
        chain_risk_status: '1',
        chain_risk_window_minutes: 60,
        chain_risk_max_uid: 3,
        chain_risk_max_phone: 3,
        chain_risk_max_ip: 8,
        chain_risk_max_device: 5,
        chain_notify_status: '1',
        chain_dividend_status: '0',
        chain_dividend_ratio: 5,
        chain_dividend_min_team: 2,
      },
      rules: [],
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      this.loading = true;
      chainConfigGetApi()
        .then((res) => {
          // request 拦截器已解包 CommonResult.data，配置对象在 res 上
          const data = res.data || res || {};
          Object.keys(this.form).forEach((k) => {
            if (data[k] !== undefined && data[k] !== null) {
              this.form[k] = String(data[k]);
            }
          });
          this.form.chain_freeze_days = Number(data.chain_freeze_days || 1);
          this.form.chain_slide_capacity = Number(data.chain_slide_capacity || 2);
          this.form.chain_slide_depth = Number(data.chain_slide_depth || 2);
          this.form.chain_help_unlock_orders = Number(data.chain_help_unlock_orders || 1);
          this.form.chain_help_freeze_ratio = Number(data.chain_help_freeze_ratio || 50);
          this.form.chain_help_timeout_days = Number(data.chain_help_timeout_days || 0);
          this.form.chain_see_generations = Number(data.chain_see_generations || 3);
          this.form.chain_risk_window_minutes = Number(data.chain_risk_window_minutes || 60);
          this.form.chain_risk_max_uid = Number(data.chain_risk_max_uid || 3);
          this.form.chain_risk_max_phone = Number(data.chain_risk_max_phone || 3);
          this.form.chain_risk_max_ip = Number(data.chain_risk_max_ip || 8);
          this.form.chain_risk_max_device = Number(data.chain_risk_max_device || 5);
          this.form.chain_dividend_ratio = Number(data.chain_dividend_ratio || 5);
          this.form.chain_dividend_min_team = Number(data.chain_dividend_min_team || 2);
          this.rules = data.rules || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    save() {
      const payload = { ...this.form };
      payload.chain_func_status = String(this.form.chain_func_status);
      payload.chain_freeze_days = String(this.form.chain_freeze_days);
      payload.chain_slide_capacity = String(this.form.chain_slide_capacity);
      payload.chain_slide_depth = String(this.form.chain_slide_depth);
      payload.chain_help_unlock_orders = String(this.form.chain_help_unlock_orders);
      payload.chain_help_freeze_ratio = String(this.form.chain_help_freeze_ratio);
      payload.chain_help_timeout_days = String(this.form.chain_help_timeout_days);
      payload.chain_see_generations = String(this.form.chain_see_generations);
      payload.chain_risk_window_minutes = String(this.form.chain_risk_window_minutes);
      payload.chain_risk_max_uid = String(this.form.chain_risk_max_uid);
      payload.chain_risk_max_phone = String(this.form.chain_risk_max_phone);
      payload.chain_risk_max_ip = String(this.form.chain_risk_max_ip);
      payload.chain_risk_max_device = String(this.form.chain_risk_max_device);
      payload.chain_dividend_ratio = String(this.form.chain_dividend_ratio);
      payload.chain_dividend_min_team = String(this.form.chain_dividend_min_team);
      chainConfigSaveApi(payload).then(() => {
        this.$message.success('保存成功');
        this.load();
      });
    },
    saveRules() {
      chainRulesSaveApi(this.rules).then(() => {
        this.$message.success('规则已保存');
        this.load();
      });
    },
  },
};
</script>

<style scoped>
.selWidth {
  width: 420px;
}
</style>
