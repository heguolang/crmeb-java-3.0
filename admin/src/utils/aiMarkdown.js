/**
 * AI 回复 Markdown → 安全 HTML（先转义再替换常见语法，避免引入额外依赖）
 */
export function renderAiMarkdown(text) {
  if (text == null || text === '') return '';
  let s = String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  // ```code```
  s = s.replace(/```([\s\S]*?)```/g, function (_, code) {
    return '<pre class="ai-md-code"><code>' + code.replace(/^\n+|\n+$/g, '') + '</code></pre>';
  });

  // `inline code`
  s = s.replace(/`([^`\n]+)`/g, '<code class="ai-md-inline">$1</code>');

  // **bold** / __bold__
  s = s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  s = s.replace(/__(.+?)__/g, '<strong>$1</strong>');

  // *italic* / _italic_（避开已处理的 **）
  s = s.replace(/(^|[^*])\*([^*\n]+)\*(?!\*)/g, '$1<em>$2</em>');

  // 标题 ### ## #
  s = s.replace(/^###\s+(.+)$/gm, '<div class="ai-md-h3">$1</div>');
  s = s.replace(/^##\s+(.+)$/gm, '<div class="ai-md-h2">$1</div>');
  s = s.replace(/^#\s+(.+)$/gm, '<div class="ai-md-h1">$1</div>');

  // 无序列表 - item / * item
  s = s.replace(/^[\-\*]\s+(.+)$/gm, '<div class="ai-md-li">• $1</div>');

  // 有序列表 1. item
  s = s.replace(/^\d+\.\s+(.+)$/gm, '<div class="ai-md-li">$1</div>');

  // 换行
  s = s.replace(/\n/g, '<br>');

  // 清理多余空行带来的连续 br
  s = s.replace(/(<br>\s*){3,}/g, '<br><br>');

  return s;
}
