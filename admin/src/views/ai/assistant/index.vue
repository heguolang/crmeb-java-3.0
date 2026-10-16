<template>
  <div class="ai-page divBg">
    <el-card shadow="never" class="ai-card">
      <div slot="header" class="ai-header">
        <div>
          <div class="ai-title">AI 助手</div>
          <div class="ai-sub">基于 DeepSeek，可咨询经营、订单、商品与后台使用问题（当前不直连店铺数据）</div>
        </div>
        <el-button size="small" @click="clearChat">清空对话</el-button>
      </div>

      <div ref="scrollBox" class="ai-messages" v-loading="loading">
        <div v-if="!messages.length" class="ai-empty">
          <p>你可以这样问：</p>
          <el-tag
            v-for="item in tips"
            :key="item"
            class="ai-tip"
            type="info"
            effect="plain"
            @click="useTip(item)"
          >{{ item }}</el-tag>
        </div>
        <div v-for="(item, idx) in messages" :key="idx" class="ai-row" :class="item.role">
          <div class="ai-bubble">
            <div class="ai-role">{{ item.role === 'user' ? '我' : 'AI' }}</div>
            <pre class="ai-content">{{ item.content }}</pre>
          </div>
        </div>
      </div>

      <div class="ai-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          maxlength="4000"
          show-word-limit
          placeholder="输入问题，Enter 发送，Shift+Enter 换行"
          @keydown.native="onKeydown"
        />
        <el-button type="primary" :loading="loading" class="ai-send" @click="send">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { aiChat } from '@/api/ai';

export default {
  name: 'AiAssistant',
  data() {
    return {
      input: '',
      loading: false,
      messages: [],
      tips: [
        '新商家入驻后，后台一般先配置哪些内容？',
        '订单一直待发货，可能是什么原因？',
        '如何理解分销佣金和团队奖的区别？',
      ],
    };
  },
  methods: {
    useTip(text) {
      this.input = text;
      this.send();
    },
    clearChat() {
      this.messages = [];
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
.ai-page {
  padding: 16px;
}
.ai-card {
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}
.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.ai-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.ai-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.ai-messages {
  height: calc(100vh - 320px);
  min-height: 360px;
  overflow-y: auto;
  padding: 12px 4px 20px;
  background: #f7f8fa;
  border-radius: 8px;
}
.ai-empty {
  padding: 24px;
  color: #606266;
  .ai-tip {
    margin: 8px 8px 0 0;
    cursor: pointer;
  }
}
.ai-row {
  display: flex;
  margin-bottom: 14px;
  &.user {
    justify-content: flex-end;
    .ai-bubble {
      background: #ecf5ff;
      border: 1px solid #d9ecff;
    }
  }
  &.assistant {
    justify-content: flex-start;
    .ai-bubble {
      background: #fff;
      border: 1px solid #ebeef5;
    }
  }
}
.ai-bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 10px;
}
.ai-role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.ai-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
.ai-input {
  margin-top: 14px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}
.ai-send {
  height: 40px;
}
</style>
