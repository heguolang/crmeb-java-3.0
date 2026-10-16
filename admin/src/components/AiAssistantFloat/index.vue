<template>
  <div class="ai-float" v-if="!hidden">
    <transition name="el-fade-in">
      <div v-show="visible" class="ai-panel">
        <div class="ai-panel-hd">
          <span>AI 助手</span>
          <div>
            <el-button type="text" size="mini" @click="newChat">新对话</el-button>
            <el-button type="text" size="mini" @click="goPage">全屏</el-button>
            <el-button type="text" size="mini" @click="visible = false">关闭</el-button>
          </div>
        </div>
        <div class="ai-panel-history">
          <el-dropdown trigger="click" @command="onHistoryCommand">
            <span class="ai-history-trigger">
              历史：{{ currentTitle }}
              <i class="el-icon-arrow-down el-icon--right" />
            </span>
            <el-dropdown-menu slot="dropdown" class="ai-history-dropdown">
              <el-dropdown-item
                v-for="item in sessions"
                :key="item.id"
                :command="'switch:' + item.id"
                :class="{ 'is-active': item.id === currentId }"
              >
                <span class="drop-title">{{ item.title || '新对话' }}</span>
                <span class="drop-time">{{ formatTime(item.updatedAt) }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="clear">清空本对话</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div ref="scrollBox" class="ai-panel-bd">
          <div v-if="!messages.length" class="ai-empty">试试问：今日成交金额、待发货多少</div>
          <div v-for="(item, idx) in messages" :key="idx" class="ai-row" :class="item.role">
            <div
              v-if="item.role === 'assistant'"
              class="ai-content ai-md"
              v-html="renderMd(item.content)"
            />
            <pre v-else class="ai-content">{{ item.content }}</pre>
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
    <el-button class="ai-fab" type="primary" circle icon="el-icon-chat-dot-round" @click="toggle" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { aiChat } from '@/api/ai';
import { renderAiMarkdown } from '@/utils/aiMarkdown';
import {
  loadAiChatStore,
  saveAiChatStore,
  getCurrentSession,
  createAiSession,
  switchAiSession,
  persistCurrentMessages,
  formatSessionTime,
} from '@/utils/aiChatHistory';

export default {
  name: 'AiAssistantFloat',
  data() {
    return {
      visible: false,
      input: '',
      loading: false,
      messages: [],
      store: null,
    };
  },
  computed: {
    ...mapGetters(['name']),
    account() {
      return this.name || 'default';
    },
    hidden() {
      const p = this.$route.path || '';
      // 登录页、AI 全屏页不显示浮标，避免挡住发送按钮
      return p === '/login' || p === '/ai/assistant' || p.startsWith('/ai/assistant/');
    },
    sessions() {
      return (this.store && this.store.sessions) || [];
    },
    currentId() {
      return (this.store && this.store.currentId) || '';
    },
    currentTitle() {
      const cur = getCurrentSession(this.store);
      return (cur && cur.title) || '新对话';
    },
  },
  watch: {
    visible(val) {
      if (val) {
        this.bootstrap();
        this.$nextTick(this.scrollBottom);
      }
    },
    '$route.path'(path) {
      if (path === '/ai/assistant' || (path && path.startsWith('/ai/assistant/'))) {
        this.visible = false;
      }
    },
  },
  methods: {
    formatTime: formatSessionTime,
    renderMd: renderAiMarkdown,
    toggle() {
      this.visible = !this.visible;
    },
    bootstrap() {
      this.store = loadAiChatStore(this.account);
      const cur = getCurrentSession(this.store);
      this.messages = cur && cur.messages ? [...cur.messages] : [];
    },
    persist() {
      if (!this.store) return;
      persistCurrentMessages(this.store, this.messages);
      saveAiChatStore(this.account, this.store);
      this.store = { ...this.store, sessions: [...this.store.sessions] };
    },
    newChat() {
      if (!this.store) this.bootstrap();
      createAiSession(this.store);
      this.messages = [];
      this.persist();
    },
    onHistoryCommand(cmd) {
      if (cmd === 'clear') {
        this.messages = [];
        this.persist();
        return;
      }
      if (String(cmd).startsWith('switch:')) {
        const id = String(cmd).slice(7);
        if (!this.store || id === this.currentId) return;
        persistCurrentMessages(this.store, this.messages);
        switchAiSession(this.store, id);
        const cur = getCurrentSession(this.store);
        this.messages = cur && cur.messages ? [...cur.messages] : [];
        saveAiChatStore(this.account, this.store);
        this.$nextTick(this.scrollBottom);
      }
    },
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
  height: 520px;
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
.ai-panel-history {
  padding: 6px 12px;
  border-bottom: 1px solid #f0f2f5;
  background: #fafafa;
}
.ai-history-trigger {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  &:hover {
    color: #409eff;
  }
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
  .ai-md {
    white-space: normal;
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
.ai-md ::v-deep strong {
  font-weight: 600;
}
.ai-md ::v-deep .ai-md-li {
  margin: 3px 0;
}
.ai-panel-ft {
  padding: 10px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
</style>

<style lang="scss">
.ai-history-dropdown {
  max-width: 280px;
  max-height: 320px;
  overflow-y: auto;
  .el-dropdown-menu__item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    line-height: 1.4;
    &.is-active {
      color: #409eff;
      background: #ecf5ff;
    }
  }
  .drop-title {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .drop-time {
    flex-shrink: 0;
    font-size: 11px;
    color: #909399;
  }
}
</style>
