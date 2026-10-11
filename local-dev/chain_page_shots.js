// 会员端链动中心 页面层验证：注入登录态 → 逐页截图
const { chromium } = require('playwright-core');
const fs = require('fs');
const path = require('path');
const http = require('http');

const EXE = 'C:/Users/Administrator/AppData/Local/ms-playwright/chromium-1243/chrome-win64/chrome.exe';
const BASE = 'http://127.0.0.1:8090';
const OUT = 'D:/qianxu-java-3.0/docs/链动中心-页面验证';

function apiLogin() {
  return new Promise((resolve, reject) => {
    const body = JSON.stringify({ account: '16600040009', password: '123456' });
    const req = http.request(
      { host: '127.0.0.1', port: 8081, path: '/api/front/login', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) } },
      (res) => {
        let s = '';
        res.on('data', (c) => (s += c));
        res.on('end', () => {
          try { resolve(JSON.parse(s).data); } catch (e) { reject(new Error('login parse fail: ' + s)); }
        });
      }
    );
    req.on('error', reject);
    req.write(body); req.end();
  });
}

const PAGES = [
  { name: '01-链动中心-老板视角', url: '#/pages/promoter/chain_center/index' },
  { name: '02-收益明细', url: '#/pages/promoter/chain_commission/index' },
  { name: '03-我的团队', url: '#/pages/promoter/chain_team/index' },
  { name: '04-帮扶基金', url: '#/pages/promoter/chain_team/index?tab=help' },
  { name: '05-规则说明', url: '#/pages/promoter/chain_rules/index' },
];

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const login = await apiLogin();
  console.log('login ok uid=', login.uid, 'token=', String(login.token).slice(0, 12) + '...');

  const browser = await chromium.launch({ executablePath: EXE, headless: true });
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();

  const errors = [];
  page.on('console', (m) => { if (m.type() === 'error') errors.push('[console] ' + m.text()); });
  page.on('pageerror', (e) => errors.push('[pageerror] ' + e.message));

  // 先落地同源页面，再注入登录态
  await page.goto(BASE + '/index.html', { waitUntil: 'domcontentloaded', timeout: 60000 });
  await page.evaluate((l) => {
    localStorage.setItem('LOGIN_STATUS_TOKEN', l.token);
    localStorage.setItem('UID', String(l.uid));
    localStorage.setItem('USER_INFO', JSON.stringify({
      uid: l.uid, nickname: l.nickname, phone: l.phone, isLogin: true,
    }));
  }, login);
  console.log('token injected into localStorage (LOGIN_STATUS_TOKEN / UID / USER_INFO)');

  for (const p of PAGES) {
    await page.goto(BASE + '/' + p.url, { waitUntil: 'networkidle', timeout: 60000 }).catch(() => {});
    await page.waitForTimeout(2200);
    const file = path.join(OUT, p.name + '.png');
    await page.screenshot({ path: file, fullPage: true });
    const title = await page.title().catch(() => '');
    const bodyLen = await page.evaluate(() => (document.body.innerText || '').length).catch(() => 0);
    const snippet = await page.evaluate(() => (document.body.innerText || '').replace(/\s+/g, ' ').slice(0, 160)).catch(() => '');
    console.log('SHOT', p.name, '| textLen=' + bodyLen, '| ' + snippet);
  }

  console.log('--- console errors (' + errors.length + ') ---');
  errors.slice(0, 20).forEach((e) => console.log(e));
  await browser.close();
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
