<template>
  <div class="ai-page divBg">
    <el-card shadow="never" class="ai-card">
      <div slot="header" class="ai-header">
        <div>
          <div class="ai-title">AI 助手</div>
          <div class="ai-sub">基于 DeepSeek，可直接查询今日销售额、订单量、待发货等经营数据；左侧可查看历史对话</div>
        </div>
        <div class="ai-header-actions">
          <el-button size="small" icon="el-icon-plus" @click="newChat">新对话</el-button>
          <el-button size="small" @click="clearChat">清空本对话</el-button>
        </div>
      </div>

      <div class="ai-body">
        <aside class="ai-history">
          <div class="ai-history-hd">历史记录</div>
          <div class="ai-history-list">
            <div
              v-for="item in sessions"
              :key="item.id"
              class="ai-history-item"
              :class="{ active: item.id === currentId }"
              @click="selectSession(item.id)"
            >
              <div class="ai-history-title">{{ item.title || '新对话' }}</div>
              <div class="ai-history-meta">
                <span>{{ formatTime(item.updatedAt) }}</span>
                <i class="el-icon-delete" title="删除" @click.stop="removeSession(item.id)" />
              </div>
            </div>
            <div v-if="!sessions.length" class="ai-history-empty">暂无历史</div>
          </div>
        </aside>

        <section class="ai-main">
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
        </section>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { aiChat } from '@/api/ai';
import {
  loadAiChatStore,
  saveAiChatStore,
  getCurrentSession,
  createAiSession,
  switchAiSession,
  deleteAiSession,
  persistCurrentMessages,
  formatSessionTime,
} from '@/utils/aiChatHistory';

export default {
  name: 'AiAssistant',
  data() {
    return {
      input: '',
      loading: false,
      messages: [],
      store: null,
      tips: ['今日成交金额', '今天有多少订单？待发货多少？', '本月GMV和客单价是多少？'],
    };
  },
  computed: {
    ...mapGetters(['name']),
    account() {
      return this.name || 'default';
    },
    sessions() {
      return (this.store && this.store.sessions) || [];
    },
    currentId() {
      return (this.store && this.store.currentId) || '';
    },
  },
  created() {
    this.bootstrap();
  },
  methods: {
    formatTime: formatSessionTime,
    bootstrap() {
      this.store = loadAiChatStore(this.account);
      const cur = getCurrentSession(this.store);
      this.messages = cur && cur.messages ? [...cur.messages] : [];
    },
    persist() {
      if (!this.store) return;
      persistCurrentMessages(this.store, this.messages);
      saveAiChatStore(this.account, this.store);
      // 触发视图刷新 sessions 标题/时间
      this.store = { ...this.store, sessions: [...this.store.sessions] };
    },
    newChat() {
      if (!this.store) this.bootstrap();
      createAiSession(this.store);
      this.messages = [];
      this.persist();
    },
    selectSession(id) {
      if (!this.store || id === this.currentId) return;
      persistCurrentMessages(this.store, this.messages);
      switchAiSession(this.store, id);
      const cur = getCurrentSession(this.store);
      this.messages = cur && cur.messages ? [...cur.messages] : [];
      saveAiChatStore(this.account, this.store);
      this.$nextTick(this.scrollBottom);
    },
    removeSession(id) {
      this.$confirm('确定删除该历史对话？', '提示', { type: 'warning' })
        .then(() => {
          deleteAiSession(this.store, id);
          const cur = getCurrentSession(this.store);
          this.messages = cur && cur.messages ? [...cur.messages] : [];
          saveAiChatStore(this.account, this.store);
          this.store = { ...this.store, sessions: [...this.store.sessions] };
        })
        .catch(() => {});
    },
    useTip(text) {
      this.input = text;
      this.send();
    },
    clearChat() {
      this.messages = [];
      this.persist();
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
      if (!this.store) this.bootstrap();
      const history = this.messages.map((m) => ({ role: m.role, content: m.content }));
      this.messages.push({ role: 'user', content: text });
      this.input = '';
      this.persist();
      this.loading = true;
      this.$nextTick(this.scrollBottom);
      try {
        const data = await aiChat({ message: text, history });
        this.messages.push({ role: 'assistant', content: (data && data.reply) || '暂无回复' });
      } catch (e) {
        this.messages.push({ role: 'assistant', content: '请求失败，请稍后重试。' });
      } finally {
        this.loading = false;
        this.persist();
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
}
.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.ai-header-actions {
  display: flex;
  gap: 8px;
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
.ai-body {
  display: flex;
  gap: 12px;
  min-height: calc(100vh - 240px);
}
.ai-history {
  width: 220px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-history-hd {
  padding: 10px 12px;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}
.ai-history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.ai-history-item {
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 6px;
  background: #fff;
  border: 1px solid transparent;
  &:hover {
    border-color: #d9ecff;
  }
  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }
}
.ai-history-title {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ai-history-meta {
  margin-top: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #909399;
  i {
    cursor: pointer;
    padding: 2px;
    &:hover {
      color: #f56c6c;
    }
  }
}
.ai-history-empty {
  padding: 16px 8px;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
.ai-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.ai-messages {
  flex: 1;
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
@media (max-width: 900px) {
  .ai-body {
    flex-direction: column;
  }
  .ai-history {
    width: 100%;
    max-height: 160px;
  }
}
</style>
