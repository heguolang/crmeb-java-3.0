<template>
  <div class="ai-float" v-if="!hiddenOnLogin">
    <transition name="el-fade-in">
      <div v-show="visible" class="ai-panel">
        <div class="ai-panel-hd">
          <span>AI 助手</span>
          <div>
            <el-button type="text" size="mini" @click="goPage">全屏</el-button>
            <el-button type="text" size="mini" @click="visible = false">关闭</el-button>
          </div>
        </div>
        <div ref="scrollBox" class="ai-panel-bd">
          <div v-if="!messages.length" class="ai-empty">试试问：今日成交金额、待发货多少</div>
          <div v-for="(item, idx) in messages" :key="idx" class="ai-row" :class="item.role">
            <pre class="ai-content">{{ item.content }}</pre>
          </div>
        </div>
        <div class="ai-panel-ft">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            maxlength="4000"
            placeholder="输入问题后发送"
            @keydown.native="onKeydown"
          />
          <el-button type="primary" size="mini" :loading="loading" @click="send">发送</el-button>
        </div>
      </div>
    </transition>
    <el-button class="ai-fab" type="primary" circle icon="el-icon-chat-dot-round" @click="visible = !visible" />
  </div>
</template>

<script>
import { aiChat } from '@/api/ai';

export default {
  name: 'AiAssistantFloat',
  data() {
    return {
      visible: false,
      input: '',
      loading: false,
      messages: [],
    };
  },
  computed: {
    hiddenOnLogin() {
      return this.$route.path === '/login';
    },
  },
  methods: {
    goPage() {
      this.visible = false;
      if (this.$route.path !== '/ai/assistant') {
        this.$router.push('/ai/assistant');
      }
    },
    onKeydown(e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.send();
      }
    },
    async send() {
      const text = (this.input || '').trim();
      if (!text || this.loading) return;
      const history = this.messages.map((m) => ({ role: m.role, content: m.content }));
      this.messages.push({ role: 'user', content: text });
      this.input = '';
      this.loading = true;
      this.$nextTick(this.scrollBottom);
      try {
        const data = await aiChat({ message: text, history });
        this.messages.push({ role: 'assistant', content: (data && data.reply) || '暂无回复' });
      } catch (e) {
        this.messages.push({ role: 'assistant', content: '请求失败，请稍后重试。' });
      } finally {
        this.loading = false;
        this.$nextTick(this.scrollBottom);
      }
    },
    scrollBottom() {
      const el = this.$refs.scrollBox;
      if (el) el.scrollTop = el.scrollHeight;
    },
  },
};
</script>

<style scoped lang="scss">
.ai-float {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3000;
}
.ai-fab {
  width: 52px;
  height: 52px;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.35);
}
.ai-panel {
  position: absolute;
  right: 0;
  bottom: 64px;
  width: 360px;
  height: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-panel-hd {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
}
.ai-panel-bd {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f7f8fa;
}
.ai-empty {
  color: #909399;
  font-size: 13px;
  padding: 12px 4px;
}
.ai-row {
  margin-bottom: 10px;
  .ai-content {
    margin: 0;
    padding: 8px 10px;
    border-radius: 8px;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: inherit;
    font-size: 13px;
    line-height: 1.55;
  }
  &.user .ai-content {
    background: #ecf5ff;
    margin-left: 28px;
  }
  &.assistant .ai-content {
    background: #fff;
    border: 1px solid #ebeef5;
    margin-right: 28px;
  }
}
.ai-panel-ft {
  padding: 10px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
</style>
