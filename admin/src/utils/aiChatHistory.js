// AI 对话历史（按管理员账号存 localStorage，全屏页与浮窗共用）

const MAX_SESSIONS = 40;
const STORAGE_PREFIX = 'qianxu_ai_chat_';

function storageKey(account) {
  return STORAGE_PREFIX + (account || 'default');
}

function uid() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

function emptyStore() {
  const id = uid();
  return {
    currentId: id,
    sessions: [
      {
        id,
        title: '新对话',
        updatedAt: Date.now(),
        messages: [],
      },
    ],
  };
}

export function loadAiChatStore(account) {
  try {
    const raw = localStorage.getItem(storageKey(account));
    if (!raw) return emptyStore();
    const data = JSON.parse(raw);
    if (!data || !Array.isArray(data.sessions) || !data.sessions.length) {
      return emptyStore();
    }
    if (!data.currentId || !data.sessions.some((s) => s.id === data.currentId)) {
      data.currentId = data.sessions[0].id;
    }
    return data;
  } catch (e) {
    return emptyStore();
  }
}

export function saveAiChatStore(account, store) {
  try {
    const copy = {
      currentId: store.currentId,
      sessions: (store.sessions || []).slice(0, MAX_SESSIONS),
    };
    localStorage.setItem(storageKey(account), JSON.stringify(copy));
  } catch (e) {
    // quota / private mode
  }
}

export function getCurrentSession(store) {
  if (!store || !store.sessions) return null;
  return store.sessions.find((s) => s.id === store.currentId) || store.sessions[0] || null;
}

export function createAiSession(store) {
  const session = {
    id: uid(),
    title: '新对话',
    updatedAt: Date.now(),
    messages: [],
  };
  store.sessions.unshift(session);
  store.currentId = session.id;
  if (store.sessions.length > MAX_SESSIONS) {
    store.sessions = store.sessions.slice(0, MAX_SESSIONS);
  }
  return session;
}

export function switchAiSession(store, id) {
  if (!store.sessions.some((s) => s.id === id)) return false;
  store.currentId = id;
  return true;
}

export function deleteAiSession(store, id) {
  const idx = store.sessions.findIndex((s) => s.id === id);
  if (idx < 0) return;
  store.sessions.splice(idx, 1);
  if (!store.sessions.length) {
    createAiSession(store);
    return;
  }
  if (store.currentId === id) {
    store.currentId = store.sessions[0].id;
  }
}

export function persistCurrentMessages(store, messages) {
  const session = getCurrentSession(store);
  if (!session) return;
  session.messages = messages || [];
  session.updatedAt = Date.now();
  if ((!session.title || session.title === '新对话') && session.messages.length) {
    const firstUser = session.messages.find((m) => m.role === 'user');
    if (firstUser && firstUser.content) {
      const t = String(firstUser.content).replace(/\s+/g, ' ').trim();
      session.title = t.length > 24 ? t.slice(0, 24) + '…' : t;
    }
  }
}

export function formatSessionTime(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  const pad = (n) => (n < 10 ? '0' + n : '' + n);
  const now = new Date();
  const sameDay =
    d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate();
  if (sameDay) {
    return pad(d.getHours()) + ':' + pad(d.getMinutes());
  }
  return pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
}
