// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

const events = [];

const $scroll = function (dom, fn) {
  events.push({ dom, fn });
  fn._index = events.length - 1;
};

$scroll.remove = function (fn) {
  fn._index && events.splice(fn._index, 1);
};

//上拉加载；
const Scroll = {
  addHandler: function (element, type, handler) {
    if (element.addEventListener) element.addEventListener(type, handler, false);
    else if (element.attachEvent) element.attachEvent('on' + type, handler);
    else element['on' + type] = handler;
  },
  listenTouchDirection: function () {
    this.addHandler(window, 'scroll', function () {
      const wh = window.innerHeight,
        st = window.scrollY;
      events
        .filter((e) => e.dom.scrollHeight && e.dom.scrollHeight > 0)
        .forEach((e) => {
          var dh = e.dom.scrollHeight;
          var s = Math.ceil((st / (dh - wh)) * 100);
          if (s > 85) e.fn();
        });
    });
  },
};

Scroll.listenTouchDirection();

export default $scroll;
export { Scroll };
