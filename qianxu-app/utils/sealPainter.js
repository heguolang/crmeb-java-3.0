/**
 * 电子签章绘制工具 v2 —— 写实版（供「签章样式选择」预览 与 「签署页合成」共用）
 *
 * 相比 v1 的四点关键变化（2026-10-02 客户反馈「公章不真实」）：
 *   1. 明确宋体字族（真实公章印文是宋体；v1 没设字体 → 落到系统黑体，一眼假）
 *   2. 印泥红（#c8161e）+ 逐字墨色深浅抖动（模拟盖章印油不均）
 *   3. 环线带轻微「手工盖章」不规则抖动，去掉 v1 多出来的内圈细线
 *   4. 几何按真实公章比例校正（星更大、名字弧排跨度按字数自适应、椭圆更扁）
 *
 * 只依赖 uni canvas ctx，纯绘制无副作用；预览画布与签署页画布共用同一套笔触。
 *
 * ⚠️ uni-app 的 CanvasContext 不是原生 canvas：它是「延迟动作队列 + 路径快照」模型，
 *    scale/translate/rotate 记录下来，路径（arc/moveTo…）要等到 stroke()/fill() 那一刻才
 *    连同当时的变换一起提交。所以**路径与它的变换必须包在同一段 save/restore 内**，
 *    不能先 restore 再 stroke —— 否则椭圆会被画成正圆（2026-10-02 踩过）。
 *
 * ⚠️ 字号必须取整：uni-h5 的 font 解析正则只认整数 px（/^\s*([\w-]+\s)*(\d+r?px)/），
 *    传 11.48px 会解析失败 → 字族被静默丢弃 → 退回黑体。这里统一 Math.round。
 *
 * 注意：不要在签署页里用网络图片 URL 去 drawImage（线上 H5 与图片域名跨域会把画布污染，
 * 导致 canvasToTempFilePath 失败），所以签章一律用本模块直接绘制。
 */

/* 印泥红：真实盖章的朱红偏沉，不是正红 */
const RED = '#c8161e';

/* 宋体字族栈：iOS/macOS 命中 Songti，Windows 命中 SimSun，安卓命中 Noto/思源宋体，兜底 serif */
const SONG = '"Songti SC","STSong","SimSun","FangSong","Noto Serif CJK SC","Source Han Serif SC","Noto Serif SC",serif';

/** 可选签章样式（顺序即前端展示顺序，round 为默认） */
export const SEAL_STYLES = [
  { key: 'round', name: '圆形公章' },
  { key: 'oval', name: '椭圆合同章' },
  { key: 'square', name: '方形老式章' },
  { key: 'modern', name: '现代电子章' }
];

/** 伪随机（确定性，同一字符每次画的墨色一致） */
function jit(i, salt) {
  const x = Math.sin(i * 12.9898 + (salt || 0) * 78.233) * 43758.5453;
  return x - Math.floor(x);
}

/** 逐字墨色深浅：0.86 ~ 1.0，模拟印油不均 */
function inkAlpha(i) {
  return 0.86 + 0.14 * jit(i, 7);
}

/** 设字体：先整串设 font（带字族），再 setFontSize 兜底（不支持的端只保证字号）。
 *  字号取整是硬要求，否则 uni-h5 的 font 正则解析失败、字族丢失。 */
function setFont(ctx, size, weight) {
  const px = Math.max(1, Math.round(size));
  const f = (weight ? weight + ' ' : '') + px + 'px ' + SONG;
  try {
    ctx.font = f;
  } catch (e) { /* 某些端不支持 font 属性，忽略 */ }
  ctx.setFontSize(px);
}

/** 带「手工盖章」轻微不规则的正圆环（多段小弧，半径微抖） */
function wobbleCircle(ctx, cx, cy, R, ring, seed) {
  const seg = 72;
  const rr = (i) => R * (1 + 0.006 * (jit(i, seed) - 0.5));
  for (let i = 0; i < seg; i++) {
    const a0 = (i / seg) * Math.PI * 2;
    const a1 = ((i + 1.35) / seg) * Math.PI * 2; // 重叠 35% 避免接缝
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

/** 五角星路径（一角朝上） */
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

/** 上弧排字：字头朝外（沿圆弧从左往右读），真实公章的公司名排法 */
function arcTextTop(ctx, text, cx, cy, R, fs, seed) {
  const chars = String(text || '').split('');
  if (!chars.length) return;
  /* 每个字占的弧长 ≈ 字宽 × 1.18，弧跨度自适应并限制在 110°~205° */
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

/** 下弧排字：字头朝内（真实公章底部编号的排法，左→右读） */
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

/** 横排文字：逐字均分（等效字距），带墨色抖动 */
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

/* ==================== ① 圆形公章（写实） ==================== */
/* 布局 = 真实公章：单圈外环 + 上弧公司名（字头朝外）+ 中央五角星 + 下弧「合同专用章」（字头朝内） */
function drawRound(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const R = S * 0.462;
  const ring = S * 0.030;

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

/* ==================== ② 椭圆合同章（写实） ==================== */
/* 布局 = 真实椭圆专用章：上弧公司名 + 中间横排「合同专用章」+ 下方编号位横线 */
function drawOval(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const rx = S * 0.465;
  const ry = S * 0.300;
  const ring = S * 0.024;
  const k = ry / rx;

  /* 椭圆环：把圆环在 y 向压扁得到；线宽按压缩比反补，视觉近似等宽。
     关键：scale 与 arc+stroke 必须在同一个 save/restore 区间内 ——
     uni 的 CanvasContext 会把 path 推迟到 stroke() 时才连同当前变换提交，
     先 restore 再 stroke 会按单位矩阵画成正圆。 */
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

  /* 公司名沿上弧（椭圆弧：用参数方程逐字取点，位置在外部算好，不依赖画布变换） */
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
  /* 下方编号位：一条细横线占位（真实椭圆章此处是数字，我们不造假编号） */
  ctx.beginPath();
  ctx.moveTo(cx - rx * 0.52, cy + ry * 0.6);
  ctx.lineTo(cx + rx * 0.52, cy + ry * 0.6);
  ctx.setStrokeStyle(RED);
  ctx.setLineWidth(S * 0.006);
  ctx.setGlobalAlpha(0.5);
  ctx.stroke();
  ctx.setGlobalAlpha(1);
}

/* ==================== ③ 方形老式章（写实） ==================== */
/* 布局 = 真实老式方章：单线方框 + 文字竖排右起（从右往左一列列读） */
function drawSquare(ctx, S, name) {
  const cx = S / 2;
  const cy = S / 2;
  const side = S * 0.8;
  const x = cx - side / 2;
  const y = cy - side / 2;
  const ring = S * 0.026;

  /* 方框：四条边各自带轻微抖动，模拟手工刻制 */
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

  /* 竖排：总文 = 公司名 + 合同专用章，右起往左，每列最多 5 字，逐列在框内垂直居中 */
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

/* ==================== ④ 现代电子章 ==================== */
/* 圆角描边 + 居中公司名 + 底部「电子签章」小字；比 v1 的实底色块更像「章」而不是按钮 */
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
  const maxLen = Math.max(...rows.map((t) => t.length), 1);
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

/**
 * 在画布上绘制一枚电子签章
 * @param ctx  uni.createCanvasContext 得到的上下文
 * @param opt  { style, size, name, x, y, tilt: 整体倾斜弧度（签署页建议 -0.07，约 -4°） }
 */
export function paintSeal(ctx, opt) {
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

export default { paintSeal, SEAL_STYLES };
