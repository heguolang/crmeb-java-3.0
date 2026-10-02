/**
 * 电子签章绘制工具（供「签章样式选择」预览 与 「签署页合成」共用）
 *
 * 只依赖传入的 canvas ctx，纯绘制、无副作用，方便在预览画布和签署页画布上复用同一套笔触，
 * 保证「会员看到的章」和「签进签署页的章」完全一致。
 *
 * 注意：不要在签署页里用网络图片 URL 去 drawImage（线上 H5 与图片域名跨域会把画布污染，
 * 导致 canvasToTempFilePath 失败），所以签章一律用本模块直接绘制。
 */

const RED = '#d3232f';

/** 可选签章样式（顺序即前端展示顺序，round 为默认） */
export const SEAL_STYLES = [
  { key: 'round', name: '圆形标准章' },
  { key: 'oval', name: '椭圆公章' },
  { key: 'square', name: '方形合同章' },
  { key: 'modern', name: '现代电子章' }
];

/** 公司名折行：一行放得下就一行；两行时按中点均分（避免"9 字+1 字"的头重脚轻）；超长末尾省略 */
function splitRows(name, per) {
  const s = String(name || '').trim();
  if (!s) return [''];
  if (s.length <= per) return [s];
  if (s.length <= per * 2) {
    const half = Math.ceil(s.length / 2);
    return [s.slice(0, half), s.slice(half)];
  }
  return [s.slice(0, per), s.slice(per, per * 2 - 1) + '…'];
}

/** 横排文字：逐字均分绘制，等效 letterSpacing，兼容不支持字距的端 */
function hText(ctx, text, cx, y, fs, color, gap) {
  const chars = String(text || '').split('');
  if (!chars.length) return;
  const unit = fs + (gap || 0);
  const total = unit * chars.length - (gap || 0);
  let x = cx - total / 2 + fs / 2;
  ctx.setFillStyle(color);
  ctx.setFontSize(fs);
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  chars.forEach((ch) => {
    ctx.fillText(ch, x, y);
    x += unit;
  });
}

/** 五角星路径 */
function starPath(ctx, cx, cy, rOuter, rInner) {
  ctx.beginPath();
  for (let i = 0; i < 10; i++) {
    const r = i % 2 === 0 ? rOuter : rInner;
    const a = -Math.PI / 2 + (i * Math.PI) / 5;
    const px = cx + r * Math.cos(a);
    const py = cy + r * Math.sin(a);
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  }
  ctx.closePath();
}

/** 圆角矩形路径 */
function roundRect(ctx, x, y, w, h, r) {
  const rr = Math.min(r, w / 2, h / 2);
  ctx.beginPath();
  ctx.moveTo(x + rr, y);
  ctx.lineTo(x + w - rr, y);
  ctx.arc(x + w - rr, y + rr, rr, -Math.PI / 2, 0);
  ctx.lineTo(x + w, y + h - rr);
  ctx.arc(x + w - rr, y + h - rr, rr, 0, Math.PI / 2);
  ctx.lineTo(x + rr, y + h);
  ctx.arc(x + rr, y + h - rr, rr, Math.PI / 2, Math.PI);
  ctx.lineTo(x, y + rr);
  ctx.arc(x + rr, y + rr, rr, Math.PI, Math.PI * 1.5);
  ctx.closePath();
}

/* ==================== ① 圆形标准章 ==================== */
function drawRound(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const R = S * 0.45;
  const ring = S * 0.034;

  ctx.beginPath();
  ctx.arc(cx, cy, R, 0, Math.PI * 2);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(ring);
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(cx, cy, R - ring * 2.4, 0, Math.PI * 2);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(ring * 0.26);
  ctx.stroke();

  // 公司名沿上半圈逐字环绕（9 点 → 12 点 → 3 点），字头朝外
  const chars = String(name).split('');
  const textR = R - ring - S * 0.05;
  const base = S * 0.084;
  const fit = (Math.PI * textR * 0.92) / Math.max(chars.length, 1);
  const fs = Math.max(S * 0.042, Math.min(base, fit));
  ctx.setFillStyle(RED);
  ctx.setFontSize(fs);
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  const startA = Math.PI;
  const step = Math.PI / Math.max(chars.length, 1);
  chars.forEach((ch, i) => {
    const a = startA + step * (i + 0.5);
    ctx.save();
    ctx.translate(cx + Math.cos(a) * textR, cy + Math.sin(a) * textR);
    ctx.rotate(a + Math.PI / 2);
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  });

  starPath(ctx, cx, cy - S * 0.015, S * 0.1, S * 0.041);
  ctx.setFillStyle(RED);
  ctx.fill();

  hText(ctx, '合同专用章', cx, cy + R * 0.55, S * 0.062, RED, S * 0.012);
}

/* ==================== ② 椭圆公章 ==================== */
function drawOval(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const rx = S * 0.46;
  const ry = S * 0.31;
  const ring = S * 0.03;

  ctx.save();
  ctx.translate(cx, cy);
  ctx.scale(1, ry / rx);
  ctx.beginPath();
  ctx.arc(0, 0, rx, 0, Math.PI * 2);
  ctx.setStrokeStyle(RED);
  // scale 会一并压扁线宽，按压缩比反补，视觉上接近等宽
  ctx.setLineWidth(ring * (rx / ry));
  ctx.stroke();
  ctx.beginPath();
  ctx.arc(0, 0, rx - ring * 2.4, 0, Math.PI * 2);
  ctx.setLineWidth(ring * 0.28 * (rx / ry));
  ctx.stroke();
  ctx.restore();

  const rows = splitRows(name, 10);
  const base = S * 0.082;
  const maxLen = Math.max(...rows.map((r) => r.length), 1);
  const fs = Math.max(S * 0.042, Math.min(base, (rx * 1.62) / maxLen));
  const startY = rows.length > 1 ? cy - ry * 0.5 : cy - ry * 0.34;
  rows.forEach((r, i) => {
    hText(ctx, r, cx, startY + i * fs * 1.16, fs, RED, 0);
  });

  const starY = cy + ry * (rows.length > 1 ? 0 : 0.02);
  starPath(ctx, cx, starY, S * 0.055, S * 0.023);
  ctx.setFillStyle(RED);
  ctx.fill();

  hText(ctx, '合同专用章', cx, cy + ry * 0.56, S * 0.058, RED, S * 0.01);
}

/* ==================== ③ 方形合同章 ==================== */
function drawSquare(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const side = S * 0.76;
  const x = cx - side / 2;
  const y = cy - side / 2;
  const ring = S * 0.032;

  ctx.beginPath();
  ctx.rect(x, y, side, side);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(ring);
  ctx.stroke();

  ctx.beginPath();
  ctx.rect(x + ring * 2.6, y + ring * 2.6, side - ring * 5.2, side - ring * 5.2);
  ctx.setLineWidth(ring * 0.26);
  ctx.stroke();

  const rows = splitRows(name, 9);
  const base = S * 0.086;
  const maxLen = Math.max(...rows.map((r) => r.length), 1);
  const fs = Math.max(S * 0.044, Math.min(base, (side * 0.82) / maxLen));
  const startY = cy - side * (rows.length > 1 ? 0.16 : 0.1);
  rows.forEach((r, i) => {
    hText(ctx, r, cx, startY + i * fs * 1.2, fs, RED, 0);
  });

  const starY = cy + side * (rows.length > 1 ? 0.06 : 0.11);
  starPath(ctx, cx, starY, S * 0.062, S * 0.026);
  ctx.setFillStyle(RED);
  ctx.fill();

  hText(ctx, '合同专用章', cx, cy + side * 0.34, S * 0.06, RED, S * 0.011);
}

/* ==================== ④ 现代电子章 ==================== */
function drawModern(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const w = S * 0.8;
  const h = S * 0.5;
  const x = cx - w / 2;
  const y = cy - h / 2;
  const r = S * 0.06;

  roundRect(ctx, x, y, w, h, r);
  ctx.setFillStyle(RED);
  ctx.fill();

  roundRect(ctx, x + S * 0.018, y + S * 0.018, w - S * 0.036, h - S * 0.036, r * 0.75);
  ctx.setStrokeStyle('rgba(255,255,255,0.85)');
  ctx.setLineWidth(S * 0.008);
  ctx.stroke();

  hText(ctx, '电子签章', cx, y + h * 0.26, S * 0.05, '#ffffff', S * 0.02);

  const rows = splitRows(name, 9);
  const maxLen = Math.max(...rows.map((t) => t.length), 1);
  const fs = Math.max(S * 0.042, Math.min(S * 0.082, (w * 0.8) / maxLen));
  const startY = rows.length > 1 ? y + h * 0.54 : y + h * 0.58;
  rows.forEach((t, i) => {
    hText(ctx, t, cx, startY + i * fs * 1.2, fs, '#ffffff', 0);
  });

  ctx.setFillStyle('rgba(255,255,255,0.9)');
  ctx.setFontSize(S * 0.04);
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  ctx.fillText('具有法律效力', cx, y + h * 0.85);
}

/**
 * 在画布上绘制一枚电子签章
 * @param ctx  uni.createCanvasContext 得到的上下文
 * @param opt  { style: 'round'|'oval'|'square'|'modern', size: 边长(px), name: 公司名, x, y: 左上角坐标 }
 */
export function paintSeal(ctx, opt) {
  const o = opt || {};
  const style = o.style || 'round';
  const S = o.size || 260;
  const name = String(o.name || '').trim() || '签约企业';
  ctx.save();
  ctx.translate(o.x || 0, o.y || 0);
  try {
    if (style === 'oval') drawOval(ctx, S, name);
    else if (style === 'square') drawSquare(ctx, S, name);
    else if (style === 'modern') drawModern(ctx, S, name);
    else drawRound(ctx, S, name);
  } finally {
    ctx.restore();
  }
}

export default { paintSeal, SEAL_STYLES };
