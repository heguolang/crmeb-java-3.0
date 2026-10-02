/**
 * 电子签章绘制工具（后台版 · 浏览器原生 canvas）
 *
 * 与会员端 qianxuqifu-app/utils/sealPainter.js 是同一套笔触（写实公章 v2），
 * 差别只在 API：uni 的 CanvasContext 是 setXxx 风格，原生 canvas 是属性赋值。
 * 这里用一个极薄适配层把原生 ctx 包装成 uni 风格，绘制代码可以逐行一致，便于两边同步维护。
 *
 * 写实要点（2026-10-02 客户反馈「公章不真实」后固化）：
 *   ① 宋体字族（真实公章印文是宋体）
 *   ② 印泥红 #c8161e + 逐字墨色深浅抖动（印油不均）
 *   ③ 环线带轻微「手工盖章」不规则抖动
 *   ④ 几何按真实公章比例校正（星更大、弧排跨度按字数自适应、椭圆更扁）
 */

export const RED = '#c8161e';

const SONG =
  '"Songti SC","STSong","SimSun","FangSong","Noto Serif CJK SC","Source Han Serif SC","Noto Serif SC",serif';

/** 可选签章样式（顺序即展示顺序，round 为默认） */
export const SEAL_STYLES = [
  { key: 'round', name: '圆形公章' },
  { key: 'oval', name: '椭圆合同章' },
  { key: 'square', name: '方形老式章' },
  { key: 'modern', name: '现代电子章' }
];

/** 把原生 CanvasRenderingContext2D 包装成 uni 的 setXxx 风格，绘图代码可原样复用 */
function adapt(ctx) {
  ctx.setStrokeStyle = function (c) {
    this.strokeStyle = c;
  };
  ctx.setFillStyle = function (c) {
    this.fillStyle = c;
  };
  ctx.setLineWidth = function (w) {
    this.lineWidth = w;
  };
  ctx.setGlobalAlpha = function (a) {
    this.globalAlpha = a;
  };
  ctx.setLineCap = function (x) {
    this.lineCap = x;
  };
  ctx.setLineJoin = function (x) {
    this.lineJoin = x;
  };
  ctx.setTextAlign = function (x) {
    this.textAlign = x;
  };
  ctx.setTextBaseline = function (x) {
    this.textBaseline = x;
  };
  ctx.setFontSize = function () {};
  return ctx;
}

function jit(i, salt) {
  const x = Math.sin(i * 12.9898 + (salt || 0) * 78.233) * 43758.5453;
  return x - Math.floor(x);
}

function inkAlpha(i) {
  return 0.86 + 0.14 * jit(i, 7);
}

function setFont(ctx, size, weight) {
  const px = Math.max(1, Math.round(size));
  ctx.font = (weight ? weight + ' ' : '') + px + 'px ' + SONG;
}

function wobbleCircle(ctx, cx, cy, R, ring, seed) {
  const seg = 72;
  const rr = (i) => R * (1 + 0.006 * (jit(i, seed) - 0.5));
  for (let i = 0; i < seg; i++) {
    const a0 = (i / seg) * Math.PI * 2;
    const a1 = ((i + 1.35) / seg) * Math.PI * 2;
    ctx.beginPath();
    ctx.arc(cx, cy, rr(i), a0, a1);
    ctx.setStrokeStyle(RED);
    ctx.setLineWidth(ring * (0.9 + 0.2 * jit(i, seed + 3)));
    ctx.setGlobalAlpha(inkAlpha(i));
    ctx.setLineCap('round');
    ctx.stroke();
  }
  ctx.setGlobalAlpha(1);
}

function starPath(ctx, cx, cy, rOuter) {
  const rInner = rOuter * 0.382;
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

function arcTextTop(ctx, text, cx, cy, R, fs, seed) {
  const chars = String(text || '').split('');
  if (!chars.length) return;
  const charA = (fs * 1.18) / R;
  const span = Math.min(Math.PI * 1.14, Math.max(Math.PI * 0.61, charA * chars.length));
  const fs2 = Math.min(fs, (R * span * 0.86) / chars.length);
  ctx.setFillStyle(RED);
  setFont(ctx, fs2, 'bold');
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  const start = -Math.PI / 2 - span / 2;
  const step = span / Math.max(chars.length - 1, 1);
  chars.forEach((ch, i) => {
    const a = start + step * i;
    ctx.save();
    ctx.setGlobalAlpha(inkAlpha(i, seed));
    ctx.translate(cx + Math.cos(a) * R, cy + Math.sin(a) * R);
    ctx.rotate(a + Math.PI / 2);
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  });
}

function arcTextBottom(ctx, text, cx, cy, R, fs, seed) {
  const chars = String(text || '').split('');
  if (!chars.length) return;
  const span = Math.min(Math.PI * 0.62, (fs * 1.25 * chars.length) / R);
  ctx.setFillStyle(RED);
  setFont(ctx, fs, 'bold');
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  const start = Math.PI / 2 + span / 2;
  const step = span / Math.max(chars.length - 1, 1);
  chars.forEach((ch, i) => {
    const a = start - step * i;
    ctx.save();
    ctx.setGlobalAlpha(inkAlpha(i + 40, seed));
    ctx.translate(cx + Math.cos(a) * R, cy + Math.sin(a) * R);
    ctx.rotate(a - Math.PI / 2);
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  });
}

function hText(ctx, text, cx, y, fs, gap, seed, weight) {
  const chars = String(text || '').split('');
  if (!chars.length) return;
  const unit = fs + (gap || 0);
  const total = unit * chars.length - (gap || 0);
  let x = cx - total / 2 + fs / 2;
  ctx.setFillStyle(RED);
  setFont(ctx, fs, weight || 'bold');
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  chars.forEach((ch, i) => {
    ctx.save();
    ctx.setGlobalAlpha(inkAlpha(i + 80, seed));
    ctx.fillText(ch, x, y);
    ctx.restore();
    x += unit;
  });
}

/* ① 圆形公章：单圈外环 + 上弧公司名（字头朝外）+ 中央五角星 + 下弧「合同专用章」（字头朝内） */
function drawRound(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const R = S * 0.462;
  const ring = S * 0.03;

  wobbleCircle(ctx, cx, cy, R, ring, 1);

  const textR = R - ring * 2.1 - S * 0.018;
  arcTextTop(ctx, name, cx, cy, textR, S * 0.108, 2);

  starPath(ctx, cx, cy - S * 0.004, S * 0.115);
  ctx.setFillStyle(RED);
  ctx.setGlobalAlpha(0.94);
  ctx.fill();
  ctx.setGlobalAlpha(1);

  arcTextBottom(ctx, '合同专用章', cx, cy, R - ring * 2.1 - S * 0.012, S * 0.062, 3);
}

/* ② 椭圆合同章：上弧公司名 + 中间横排「合同专用章」+ 下方编号位横线 */
function drawOval(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const rx = S * 0.465;
  const ry = S * 0.3;
  const ring = S * 0.024;
  const k = ry / rx;

  const seg = 72;
  ctx.save();
  ctx.translate(cx, cy);
  ctx.scale(1, k);
  for (let i = 0; i < seg; i++) {
    const a0 = (i / seg) * Math.PI * 2;
    const a1 = ((i + 1.35) / seg) * Math.PI * 2;
    const r = rx * (1 + 0.005 * (jit(i, 11) - 0.5));
    ctx.beginPath();
    ctx.arc(0, 0, r, a0, a1);
    ctx.setStrokeStyle(RED);
    ctx.setLineWidth((ring * (0.9 + 0.2 * jit(i, 13))) / k);
    ctx.setGlobalAlpha(inkAlpha(i, 5));
    ctx.setLineCap('round');
    ctx.stroke();
  }
  ctx.restore();
  ctx.setGlobalAlpha(1);

  const chars = String(name).split('');
  const textRx = rx - ring * 3.2;
  const charA = (S * 0.082 * 1.16) / textRx;
  const span = Math.min(Math.PI * 0.78, Math.max(Math.PI * 0.42, charA * Math.max(chars.length - 1, 1)));
  const fs = Math.min(S * 0.082, (textRx * span * 0.88) / Math.max(chars.length, 1));
  ctx.setFillStyle(RED);
  setFont(ctx, fs, 'bold');
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  const start = -Math.PI / 2 - span / 2;
  const step = span / Math.max(chars.length - 1, 1);
  chars.forEach((ch, i) => {
    const a = start + step * i;
    ctx.save();
    ctx.setGlobalAlpha(inkAlpha(i, 9));
    ctx.translate(cx + Math.cos(a) * textRx, cy + Math.sin(a) * textRx * k);
    ctx.rotate(a + Math.PI / 2);
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  });

  hText(ctx, '合同专用章', cx, cy + S * 0.012, S * 0.082, S * 0.014, 6);
  ctx.beginPath();
  ctx.moveTo(cx - rx * 0.52, cy + ry * 0.6);
  ctx.lineTo(cx + rx * 0.52, cy + ry * 0.6);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(S * 0.006);
  ctx.setGlobalAlpha(0.5);
  ctx.stroke();
  ctx.setGlobalAlpha(1);
}

/* ③ 方形老式章：单线方框 + 文字竖排右起 */
function drawSquare(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const side = S * 0.8;
  const x = cx - side / 2;
  const y = cy - side / 2;
  const ring = S * 0.026;

  const jitter = (i) => S * 0.004 * (jit(i, 21) - 0.5);
  ctx.setStrokeStyle(RED);
  ctx.setLineCap('round');
  const edges = [
    [x, y, x + side, y],
    [x + side, y, x + side, y + side],
    [x + side, y + side, x, y + side],
    [x, y + side, x, y]
  ];
  edges.forEach((e, i) => {
    ctx.beginPath();
    ctx.moveTo(e[0] + jitter(i), e[1] + jitter(i + 9));
    ctx.lineTo(e[2] + jitter(i + 3), e[3] + jitter(i + 17));
    ctx.setLineWidth(ring * (0.92 + 0.16 * jit(i, 31)));
    ctx.setGlobalAlpha(inkAlpha(i, 15));
    ctx.stroke();
  });
  ctx.setGlobalAlpha(1);

  const full = String(name) + '合同专用章';
  const perCol = 5;
  const cols = [];
  for (let i = 0; i < full.length; i += perCol) cols.push(full.slice(i, i + perCol));
  const fs = Math.min(S * 0.105, (side * 0.78) / perCol);
  const lineH = fs * 1.1;
  const colW = (side * 0.72) / cols.length;
  cols.forEach((col, ci) => {
    const cxx = x + side - ring * 2.2 - colW * (ci + 0.5);
    const colStartY = cy - ((col.length - 1) * lineH) / 2;
    col.split('').forEach((ch, i) => {
      ctx.save();
      ctx.setGlobalAlpha(inkAlpha(ci * 5 + i, 41));
      ctx.setFillStyle(RED);
      setFont(ctx, fs, 'bold');
      ctx.setTextAlign('center');
      ctx.setTextBaseline('middle');
      ctx.fillText(ch, cxx, colStartY + i * lineH);
      ctx.restore();
    });
  });
  ctx.setGlobalAlpha(1);
}

/* ④ 现代电子章：圆角描边 + 居中公司名 + 底部「电子签章」小字 */
function drawModern(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const w = S * 0.82;
  const h = S * 0.52;
  const x = cx - w / 2;
  const y = cy - h / 2;
  const r = S * 0.07;

  roundRectPath(ctx, x, y, w, h, r);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(S * 0.022);
  ctx.setGlobalAlpha(0.95);
  ctx.stroke();
  roundRectPath(ctx, x + S * 0.014, y + S * 0.014, w - S * 0.028, h - S * 0.028, r * 0.8);
  ctx.setLineWidth(S * 0.006);
  ctx.setGlobalAlpha(0.55);
  ctx.stroke();
  ctx.setGlobalAlpha(1);

  const rows = splitRows(name, 9);
  const maxLen = Math.max.apply(null, rows.map((t) => t.length).concat([1]));
  const fs = Math.max(S * 0.042, Math.min(S * 0.088, (w * 0.84) / maxLen));
  const startY = rows.length > 1 ? y + h * 0.36 : y + h * 0.4;
  rows.forEach((t, i) => {
    hText(ctx, t, cx, startY + i * fs * 1.22, fs, 0, 61, 'bold');
  });

  hText(ctx, '电子签章', cx, y + h * 0.82, S * 0.048, S * 0.02, 63);
}

function roundRectPath(ctx, x, y, w, h, r) {
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

/**
 * 在原生 canvas 的 2d 上下文上绘制一枚电子签章
 * @param {CanvasRenderingContext2D} rawCtx
 * @param {{style?:string,size?:number,name?:string,x?:number,y?:number,tilt?:number}} opt
 *        tilt 为整体倾斜弧度（建议 -0.07，约 -4°）
 */
export function paintSeal(rawCtx, opt) {
  const ctx = adapt(rawCtx);
  const o = opt || {};
  const style = o.style || 'round';
  const S = o.size || 260;
  const name = String(o.name || '').trim() || '签约企业';
  ctx.save();
  ctx.translate(o.x || 0, o.y || 0);
  if (o.tilt) {
    ctx.translate(S / 2, S / 2);
    ctx.rotate(o.tilt);
    ctx.translate(-S / 2, -S / 2);
  }
  try {
    if (style === 'oval') drawOval(ctx, S, name);
    else if (style === 'square') drawSquare(ctx, S, name);
    else if (style === 'modern') drawModern(ctx, S, name);
    else drawRound(ctx, S, name);
  } finally {
    ctx.restore();
  }
}

export default { paintSeal, SEAL_STYLES, RED };
