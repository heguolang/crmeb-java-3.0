// 数据中心大屏大标题：默认值 + 配置合并工具（2026-09-30）
// 后端 eb_system_config 键 qx_bigdata_screen_setting，值为 JSON：{ operation: '...', market: '...' }

export const SCREEN_TITLES_DEFAULT = {
  operation: '黔序数据指挥中心',
  market: '市 场 大 数 据 中 心',
};

// raw：后端返回的 JSON 字符串（可能为空串/非法 JSON），合并出最终标题
export function mergeScreenTitles(raw) {
  let saved = {};
  if (raw) {
    try {
      saved = typeof raw === 'string' ? JSON.parse(raw) : raw;
    } catch (e) {
      saved = {};
    }
  }
  const pick = (v, dft) => (typeof v === 'string' && v.trim() ? v.trim() : dft);
  return {
    operation: pick(saved.operation, SCREEN_TITLES_DEFAULT.operation),
    market: pick(saved.market, SCREEN_TITLES_DEFAULT.market),
  };
}
