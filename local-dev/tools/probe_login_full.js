// 完整登录流实测:立即登录 → register → 绑定按钮出现 → 一键授权手机号
const automator = require('miniprogram-automator');

(async () => {
  const mp = await automator.connect({ wsEndpoint: 'ws://localhost:9422' });
  mp.on('console', (msg) => {
    const args = (msg.args || []).map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ');
    console.log(`[CONSOLE.${msg.type}]`, args.slice(0, 250));
  });

  const page = await mp.reLaunch('/pages/users/wechat_login/index');
  await new Promise(r => setTimeout(r, 4000));

  const d1 = await page.data();
  console.log('[pre] routinePhoneVerification =', JSON.stringify(d1.routinePhoneVerification), ' wxLogin =', d1.wxLogin);

  // 点立即登录
  let btn = await page.$('.btn1');
  await btn.tap();
  console.log('[tap1] 立即登录,等 5s...');
  await new Promise(r => setTimeout(r, 5000));

  const d2 = await page.data();
  console.log('[post1] wxLogin =', d2.wxLogin, ' authKey =', JSON.stringify(d2.authKey), ' rpV =', JSON.stringify(d2.routinePhoneVerification));

  // wxLogin=false 后按钮区应出现「一键绑定手机号」
  const btns = await page.$$('.btn1, .btn2');
  console.log('[btns] 数量 =', btns.length);
  for (const b of btns) {
    try {
      const txt = await b.text();
      const sz = await b.size();
      console.log('  按钮:', JSON.stringify(txt), 'size=', JSON.stringify(sz));
    } catch (e) { console.log('  按钮 err', e.message); }
  }

  await mp.disconnect();
  console.log('[done]');
})().catch(e => { console.error('FATAL', e.message || e); process.exit(1); });
