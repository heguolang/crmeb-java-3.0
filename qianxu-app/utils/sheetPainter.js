/**
 * 独立签署页绘制（供「签署页合成组件」与静态测试页共用）
 *
 * 只依赖传入的 ctx（uni CanvasContext 或经适配的浏览器原生 2D 上下文），
 * 版面：标题 → 合同名称/编号 → 说明 → 签约主体（盖章）→ 签约负责人（签字）→ 签署日期 → 页脚
 */

import { paintSeal } from './sealPainter.js';

const PAD = 56;

/* 页码正文字族（与签章的宋体区分开：正文用无衬线，签章用宋体） */
const SANS = '-apple-system,"PingFang SC","Microsoft YaHei","Helvetica Neue",Arial,sans-serif';

/**
 * 显式设置字体。两个原因必须每次都设：
 *   ① uni 的 ctx.font 解析只认整数 px，传小数会静默丢掉字族（退回 sans-serif）；
 *   ② 签章绘制会把画布的 font 改成宋体字族，若不重设，后面的正文会跟着变宋体。
 */
function setFont(ctx, size, weight) {
  const px = Math.max(1, Math.round(size));
  try {
    ctx.font = (weight ? weight + ' ' : '') + px + 'px ' + SANS;
  } catch (e) { /* 某些端不支持 font 属性，忽略 */ }
  ctx.setFontSize(px);
}

/** 带字距的居中标题 */
function spaced(ctx, text, cx, y, fs, color, gap) {
  const chars = String(text).split('');
  const unit = fs + (gap || 0);
  let x = cx - (unit * chars.length - (gap || 0)) / 2 + fs / 2;
  ctx.setTextAlign('center');
  ctx.setTextBaseline('middle');
  ctx.setFillStyle(color);
  setFont(ctx, fs);
  chars.forEach((ch) => {
    ctx.fillText(ch, x, y);
    x += unit;
  });
}

/** 左侧竖色条 + 区块标题 */
function blockTitle(ctx, text, x, y, fs) {
  ctx.setFillStyle('#c8161e');
  ctx.fillRect(x, y - fs * 0.64, 6, fs * 1.22);
  ctx.setTextAlign('left');
  ctx.setTextBaseline('middle');
  ctx.setFillStyle('#1a1a1a');
  setFont(ctx, fs);
  ctx.fillText(text, x + 18, y);
}

/** 浅底圆角框 */
function box(ctx, x, y, w, h) {
  const r = 14;
  ctx.setFillStyle('#fbfcfe');
  ctx.setStrokeStyle('#e2e8f2');
  ctx.setLineWidth(2);
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.lineTo(x + w - r, y);
  ctx.arc(x + w - r, y + r, r, -Math.PI / 2, 0);
  ctx.lineTo(x + w, y + h - r);
  ctx.arc(x + w - r, y + h - r, r, 0, Math.PI / 2);
  ctx.lineTo(x + r, y + h);
  ctx.arc(x + r, y + h - r, r, Math.PI / 2, Math.PI);
  ctx.lineTo(x, y + r);
  ctx.arc(x + r, y + r, r, Math.PI, Math.PI * 1.5);
  ctx.closePath();
  ctx.fill();
  ctx.stroke();
}

/**
 * 重绘手写签名笔迹（按内容包围盒等比缩放居中到目标区域）。
 * 用矢量重绘而不是贴图片：H5 下签名图在另一域名，drawImage 会污染画布导致导出失败。
 */
function drawStrokes(ctx, pack, area) {
  const strokes = (pack && pack.strokes) || [];
  if (!strokes.length) return false;

  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  strokes.forEach((s) => {
    (s.pts || []).forEach((p) => {
      if (p.x < minX) minX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.x > maxX) maxX = p.x;
      if (p.y > maxY) maxY = p.y;
    });
  });
  if (!isFinite(minX)) return false;

  const pad = 8;
  minX -= pad;
  minY -= pad;
  maxX += pad;
  maxY += pad;
  const bw = Math.max(maxX - minX, 1);
  const bh = Math.max(maxY - minY, 1);
  const scale = Math.min(area.w / bw, area.h / bh);
  const dw = bw * scale;
  const dh = bh * scale;
  const ox = area.x + (area.w - dw) / 2 - minX * scale;
  const oy = area.y + (area.h - dh) / 2 - minY * scale;

  ctx.setStrokeStyle('#111111');
  ctx.setLineCap('round');
  ctx.setLineJoin('round');
  ctx.setFillStyle('#111111');
  strokes.forEach((s) => {
    const pts = s.pts || [];
    if (!pts.length) return;
    const lw = Math.max(1, (s.w || 4) * scale);
    if (pts.length === 1) {
      ctx.beginPath();
      ctx.arc(ox + pts[0].x * scale, oy + pts[0].y * scale, lw / 2, 0, Math.PI * 2);
      ctx.fill();
      return;
    }
    ctx.setLineWidth(lw);
    ctx.beginPath();
    pts.forEach((p, i) => {
      const px = ox + p.x * scale;
      const py = oy + p.y * scale;
      if (i === 0) ctx.moveTo(px, py);
      else ctx.lineTo(px, py);
    });
    ctx.stroke();
  });
  return true;
}

/** 按像素宽度粗略折行（中文按 1 字宽、西文按 0.55 字宽估算） */
function wrap(text, maxW, fs, maxLines) {
  const lines = [];
  let cur = '';
  let w = 0;
  const src = String(text == null ? '' : text);
  for (let i = 0; i < src.length; i++) {
    const ch = src.charAt(i);
    if (ch === '\n') {
      lines.push(cur);
      cur = '';
      w = 0;
      continue;
    }
    const cw = ch.charCodeAt(0) > 255 ? fs : fs * 0.55;
    if (w + cw > maxW && cur) {
      lines.push(cur);
      cur = ch;
      w = cw;
      if (lines.length >= maxLines) break;
    } else {
      cur += ch;
      w += cw;
    }
  }
  if (cur && lines.length < maxLines) lines.push(cur);
  if (lines.length === maxLines && src.length > lines.join('').length) {
    lines[maxLines - 1] = lines[maxLines - 1].slice(0, -1) + '…';
  }
  return lines.length ? lines : ['—'];
}

function clip(text, n) {
  const s = String(text || '');
  return s.length > n ? s.slice(0, n - 1) + '…' : s;
}

/**
 * 绘制整页签署页
 * @param ctx   画布上下文
 * @param o     { title, contractNo, company, sealStyle, needSeal, signerName, signPack, dateText }
 * @param W     画布逻辑宽
 * @param H     画布逻辑高
 */
export function paintSheet(ctx, o, W, H) {
  const data = o || {};
  const IN = W - PAD * 2;

  // 纸面 + 顶部色条
  ctx.setFillStyle('#ffffff');
  ctx.fillRect(0, 0, W, H);
  ctx.setFillStyle('#1d6bf3');
  ctx.fillRect(0, 0, W, 6);

  // 标题
  spaced(ctx, '签署页', W / 2, 98, 36, '#1a1a1a', 14);
  ctx.setStrokeStyle('#e6ebf3');
  ctx.setLineWidth(2);
  ctx.beginPath();
  ctx.moveTo(PAD, 136);
  ctx.lineTo(W - PAD, 136);
  ctx.stroke();

  // 合同名称 / 编号
  ctx.setTextAlign('left');
  ctx.setTextBaseline('middle');
  let y = 186;
  wrap(data.title || '—', IN, 24, 2).forEach((ln) => {
    ctx.setFillStyle('#1a1a1a');
    setFont(ctx, 24);
    ctx.fillText(ln, PAD, y);
    y += 34;
  });
  ctx.setFillStyle('#8a94a3');
  setFont(ctx, 20);
  ctx.fillText('合同编号：' + (data.contractNo || '—'), PAD, y + 6);
  y += 42;

  // 说明
  ctx.setFillStyle('#5a6675');
  setFont(ctx, 19);
  wrap('以下为本合同签署要素，与合同正文具有同等法律效力。', IN, 19, 3).forEach((ln) => {
    ctx.fillText(ln, PAD, y + 14);
    y += 30;
  });

  // ---- 签约主体（盖章） ----
  y += 34;
  blockTitle(ctx, '签约主体（盖章）', PAD, y, 21);
  y += 22;
  const box1 = 224;
  box(ctx, PAD, y, IN, box1);
  ctx.setTextAlign('center');
  ctx.setFillStyle('#1a1a1a');
  setFont(ctx, 23);
  ctx.fillText(clip(data.company || '—', 16), W / 2, y + 44);
  if (data.needSeal === false) {
    ctx.setFillStyle('#a8b3c4');
    setFont(ctx, 19);
    ctx.fillText('个人签约，无需盖章', W / 2, y + 130);
  } else {
    // 签章与预览用同一套笔触直接绘制（不走图片，避免画布被跨域污染）
    // tilt：盖印时手不会端得笔直，轻微倾斜更像真章（-0.07 rad ≈ -4°）
    paintSeal(ctx, {
      style: data.sealStyle || 'round',
      size: 140,
      name: data.company,
      x: (W - 140) / 2,
      y: y + 62,
      tilt: -0.07
    });
  }
  y += box1;

  // ---- 签约负责人（签字） ----
  y += 44;
  ctx.setTextAlign('left');
  blockTitle(ctx, '签约负责人（签字）', PAD, y, 21);
  y += 22;
  const box2 = 210;
  box(ctx, PAD, y, IN, box2);
  const drawn = drawStrokes(ctx, data.signPack, {
    x: PAD + 30,
    y: y + 20,
    w: IN - 60,
    h: 108
  });
  if (!drawn) {
    ctx.setTextAlign('center');
    ctx.setFillStyle('#c3cad6');
    setFont(ctx, 18);
    ctx.fillText('（手写签名）', W / 2, y + 74);
  }
  ctx.setTextAlign('center');
  ctx.setFillStyle('#1a1a1a');
  setFont(ctx, 22);
  ctx.fillText('负责人：' + (data.signerName || '—'), W / 2, y + box2 - 34);
  y += box2;

  // ---- 签署日期 ----
  y += 50;
  ctx.setTextAlign('left');
  ctx.setFillStyle('#1a1a1a');
  setFont(ctx, 21);
  ctx.fillText('签署日期：' + (data.dateText || '—'), PAD, y);

  // ---- 页脚 ----
  ctx.setStrokeStyle('#e6ebf3');
  ctx.setLineWidth(2);
  ctx.beginPath();
  ctx.moveTo(PAD, H - 86);
  ctx.lineTo(W - PAD, H - 86);
  ctx.stroke();
  ctx.setTextAlign('center');
  ctx.setFillStyle('#9aa4b2');
  setFont(ctx, 17);
  ctx.fillText('本签署页由系统根据合同签署数据自动生成', W / 2, H - 56);
}

export default { paintSheet };
