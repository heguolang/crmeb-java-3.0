<script>
import render from '@/components/FormGenerator/components/render/render.js';

// ---------------------------------------------------------------------------
// 标签宽度自适应（2026-10-03「页面UI有问题」修复）
// 这些表单由「表单设计器」生成，labelWidth 是人工填的、普遍偏小 ——
// 全库有 63 个字段的标签比配置宽度还宽（如「会员中心是否授权手机号：」需 194px，只配了 140px）。
// el-form-item__label 是 float:left + 固定宽度，放不下就折成两行：第一行与控件同排，
// 第二行只剩「机号：」「开关：」这类碎片，且行高由 40px 变 80px —— 整表看起来就是标签被裁、错位。
// 这里按渲染字体量一遍所有标签，取「配置值」与「最长标签所需宽度」的较大者作为整表标签宽度。
// ---------------------------------------------------------------------------
const LABEL_PADDING_RIGHT = 12; // element: .el-form-item__label { padding: 0 12px 0 0 }
const LABEL_ASTERISK_WIDTH = 14; // element: .is-required .el-form-item__label:before { content:'*'; margin-right:4px }
const LABEL_WIDTH_BUFFER = 2; // 取整/抗锯齿余量

let measureCtx = null;

function measureLabelText(text) {
  if (!text) return 0;
  if (!measureCtx) measureCtx = document.createElement('canvas').getContext('2d');
  // 标签字号固定 14px（element 的 .el-form-item__label，small/mini 只改 line-height 不改字号），
  // 字体族取页面继承值，保证量出来的宽度与真实渲染一致
  const fontFamily = window.getComputedStyle(document.body).fontFamily || 'sans-serif';
  measureCtx.font = `14px ${fontFamily}`;
  return measureCtx.measureText(text).width;
}

const ruleTrigger = {
  'el-input': 'blur',
  'el-input-number': 'blur',
  'el-select': 'change',
  'el-radio-group': 'change',
  'el-checkbox-group': 'change',
  'el-cascader': 'change',
  'el-time-picker': 'change',
  'el-date-picker': 'change',
  'el-rate': 'change',
};

function renderFrom(h) {
  const { formConfCopy } = this;
  return (
    <el-row gutter={formConfCopy.gutter}>
      <el-form
        size={formConfCopy.size}
        label-position={formConfCopy.labelPosition}
        disabled={formConfCopy.disabled}
        label-width={`${this.labelWidthPx}px`}
        ref={formConfCopy.formRef}
        // model不能直接赋值 https://github.com/vuejs/jsx/issues/49#issuecomment-472013664
        props={{ model: this[formConfCopy.formModel] }}
        rules={this[formConfCopy.formRules]}
      >
        {renderFormItem.call(this, h, formConfCopy.fields)}
        {formConfCopy.formBtns && formBtns.call(this, h)}
      </el-form>
    </el-row>
  );
}

function formBtns(h) {
  return (
    <el-col>
      <el-form-item class="dialog-footer-inner zdy-btn">
        <el-button onClick={this.closeDialog} class="closeBtn">
          取消
        </el-button>
        <el-button class="saveBtn" type="primary" v-debounceClick={this.submitForm}>
          提交
        </el-button>
      </el-form-item>
    </el-col>
  );
}

function renderFormItem(h, elementList) {
  return elementList.map((scheme) => {
    const config = scheme.__config__;
    const layout = layouts[config.layout];
    if (layout) {
      return layout.call(this, h, scheme);
    }
    throw new Error(`没有与${config.layout}匹配的layout`);
  });
}

function renderChildren(h, scheme) {
  const config = scheme.__config__;
  if (!Array.isArray(config.children)) return null;
  return renderFormItem.call(this, h, config.children);
}

function setValue(event, config, scheme) {
  this.$set(config, 'defaultValue', event);
  this.$set(this[this.formConf.formModel], scheme.__vModel__, event);
}

function buildListeners(scheme) {
  const config = scheme.__config__;
  const methods = this.formConf.__methods__ || {};
  const listeners = {};

  // 给__methods__中的方法绑定this和event
  Object.keys(methods).forEach((key) => {
    listeners[key] = (event) => methods[key].call(this, event);
  });
  // 响应 render.js 中的 vModel $emit('input', val)
  listeners.input = (event) => setValue.call(this, event, config, scheme);

  return listeners;
}
const layouts = {
  colFormItem(h, scheme) {
    const config = scheme.__config__;
    const listeners = buildListeners.call(this, scheme);
    // 字段级 labelWidth 一律不再采用：配置里普遍写小了（全库 63 处标签比它宽），
    // 统一继承 el-form 上的 labelWidthPx（配置值与最长标签所需宽度的较大者），
    // 保证标签不折行、各控件左边界对齐；不显示标签的字段仍给 0，让控件占满整行。
    let labelWidth = null;
    if (config.showLabel === false) labelWidth = '0';
    // 系统设置不再展示第三方帮助链接（历史 tipsIsLink 逻辑已移除）
    if (config.tips && config.tipsDesc && !config.tipsIsLink) {
      return (
        <el-col span={config.span}>
          <el-form-item label-width={labelWidth} prop={scheme.__vModel__} label={config.showLabel ? config.label : ''}>
            <render conf={scheme} {...{ on: listeners }} />
            <div>
              <span class="tips-info">{config.tipsDesc}</span>
            </div>
          </el-form-item>
        </el-col>
      );
    } else {
      return (
        <el-col span={config.span}>
          <el-form-item label-width={labelWidth} prop={scheme.__vModel__} label={config.showLabel ? config.label : ''}>
            <render conf={scheme} {...{ on: listeners }} />
          </el-form-item>
        </el-col>
      );
    }
  },
  rowFormItem(h, scheme) {
    let child = renderChildren.apply(this, arguments);
    if (scheme.type === 'flex') {
      child = (
        <el-row type={scheme.type} justify={scheme.justify} align={scheme.align}>
          {child}
        </el-row>
      );
    }
    return (
      <el-col span={scheme.span}>
        <el-row gutter={scheme.gutter}>{child}</el-row>
      </el-col>
    );
  },
};

export default {
  components: {
    render,
  },
  props: {
    formConf: {
      type: Object,
      required: true,
    },
    formEditData: {
      type: Object,
    },
    isEdit: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    // 整表实际使用的标签宽度 = max(配置值, 最长标签所需宽度)
    labelWidthPx() {
      const conf = this.formConfCopy;
      const configured = parseFloat(conf.labelWidth);
      let width = Number.isNaN(configured) ? 0 : configured;
      // 标签在顶部时宽度无意义，保持配置值
      if (conf.labelPosition === 'top') return width || 100;
      const walk = (fields) => {
        (fields || []).forEach((field) => {
          const config = field.__config__ || {};
          if (config.label && config.showLabel !== false) {
            const need =
              Math.ceil(measureLabelText(config.label)) +
              LABEL_PADDING_RIGHT +
              LABEL_WIDTH_BUFFER +
              (config.required ? LABEL_ASTERISK_WIDTH : 0);
            if (need > width) width = need;
          }
          if (config.children) walk(config.children);
        });
      };
      walk(conf.fields);
      return width || 100;
    },
  },
  data() {
    if (this.isEdit) {
      // 初始化待编辑数据
      this.formConf.fields.forEach((conf) => {
        // 设置现有的数据
        const hasValueForEdit = this.formEditData[conf.__vModel__];
        if (hasValueForEdit) {
          conf.__config__.defaultValue = hasValueForEdit;
        }
        // 如果是el-select标签 判断数据后改变实现默认选中效果
        if (conf.__config__.tag === 'el-select' || conf.__config__.tag === 'el-radio-group') {
          const perValue = conf.__slot__.options.filter((option) => option.value == this.formEditData[conf.__vModel__]);
          if (perValue.length > 0) {
            // 有表单数据
            conf.__config__.defaultValue = perValue[0].value;
          }
        }
      });
    }
    const data = {
      formConfCopy: JSON.parse(JSON.stringify(this.formConf)),
      [this.formConf.formModel]: {},
      [this.formConf.formRules]: {},
    };
    this.initFormData(data.formConfCopy.fields, data[this.formConf.formModel]);
    this.buildRules(data.formConfCopy.fields, data[this.formConf.formRules]);
    return data;
  },
  methods: {
    initFormData(componentList, formData) {
      componentList.forEach((cur) => {
        const config = cur.__config__;
        if (cur.__vModel__) formData[cur.__vModel__] = config.defaultValue;
        if (config.children) this.initFormData(config.children, formData);
      });
    },
    buildRules(componentList, rules) {
      componentList.forEach((cur) => {
        const config = cur.__config__;
        if (Array.isArray(config.regList)) {
          if (config.required) {
            const required = { required: config.required, message: cur.placeholder };
            if (Array.isArray(config.defaultValue)) {
              required.type = 'array';
              required.message = `请至少选择一个${config.label}`;
            }
            required.message === undefined && (required.message = `${config.label}不能为空`);
            config.regList.push(required);
          }
          rules[cur.__vModel__] = config.regList.map((item) => {
            item.pattern && (item.pattern = eval(item.pattern));
            item.trigger = ruleTrigger && ruleTrigger[config.tag];
            return item;
          });
        }
        if (config.children) this.buildRules(config.children, rules);
      });
    },
    resetForm() {
      this.$emit('resetForm', this.formConf);
      this.formConfCopy = JSON.parse(JSON.stringify(this.formConf));
      this.$refs[this.formConf.formRef].resetFields();
    },
    closeDialog() {
      this.$emit('closeDialog');
    },
    submitForm() {
      this.$refs[this.formConf.formRef].validate((valid) => {
        if (!valid) return false;
        // 触发sumit事件
        this.$emit('submit', this[this.formConf.formModel]);
        return true;
      });
    },
  },
  render(h) {
    return renderFrom.call(this, h);
  },
};
</script>
<style scoped lang="scss">
// 兜底：万一量宽有偏差也不让标签折行 —— 折行会让 form-item 行高从 40px 变 80px，
// 第二行只剩「机号：」这类碎片，右侧控件与标签整体错位（2026-10-03「页面UI有问题」）
::v-deep .el-form-item__label {
  white-space: nowrap;
}
.tips-info {
  line-height: 18px;
  //margin-top: 3px;
  color: #c0c4cc;
  font-size: 12px;
}
.saveBtn {
  font-size: 12px;
}
</style>
