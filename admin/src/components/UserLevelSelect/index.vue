<template>
  <el-select
    v-model="innerValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    :filterable="filterable"
    style="width: 100%"
    @change="onChange"
  >
    <el-option
      v-for="item in levelList"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </el-select>
</template>

<script>
import { levelAllApi } from '@/api/user';

export default {
  name: 'UserLevelSelect',
  props: {
    value: {
      type: [Number, String],
      default: null,
    },
    placeholder: {
      type: String,
      default: '请选择会员等级',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    clearable: {
      type: Boolean,
      default: true,
    },
    filterable: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      levelList: [],
    };
  },
  computed: {
    innerValue: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      },
    },
  },
  created() {
    this.fetchLevels();
  },
  methods: {
    fetchLevels() {
      levelAllApi()
        .then((res) => {
          this.levelList = res || [];
        })
        .catch(() => {
          this.levelList = [];
        });
    },
    onChange(val) {
      const level = this.levelList.find((item) => item.id === val) || null;
      this.$emit('change', val, level);
    },
  },
};
</script>
