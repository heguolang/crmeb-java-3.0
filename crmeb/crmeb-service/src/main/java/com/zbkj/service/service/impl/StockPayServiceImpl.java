package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.PayConstants;
import com.zbkj.common.constants.UserConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.model.user.UserToken;
import com.zbkj.common.request.StockPayRequest;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.WxPayUtil;
import com.zbkj.common.vo.AttachVo;
import com.zbkj.common.vo.CreateOrderRequestVo;
import com.zbkj.common.vo.CreateOrderResponseVo;
import com.zbkj.common.vo.WxPayJsResultVo;
import com.zbkj.service.dao.StockOrderDao;
import com.zbkj.service.service.StockOrderService;
import com.zbkj.service.service.StockPayService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserBillService;
import com.zbkj.service.service.UserService;
import com.zbkj.service.service.UserTokenService;
import com.zbkj.service.service.WechatNewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * 订货系统-订货单支付服务实现
 * 微信支付复用 wechatNewService.payUnifiedorder 统一下单（自动落 eb_wechat_pay_info），
 * 回调 attach.type=stock 由 CallbackServiceImpl 分发到 StockOrderService.payStockOrder。
 */
@Slf4j
@Service
public class StockPayServiceImpl implements StockPayService {

    @Autowired
    @Lazy
    private StockOrderService stockOrderService;

    @Resource
    private StockOrderDao stockOrderDao;

    @Resource
    private UserService userService;

    @Resource
    private UserBillService userBillService;

    @Resource
    private UserTokenService userTokenService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private WechatNewService wechatNewService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public HashMap<String, Object> pay(Integer uid, StockPayRequest request, String ip) {
        StockOrder order = stockOrderService.getByOrderNo(request.getOrderNo());
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!uid.equals(order.getUid())) {
            throw new CrmebException("无权支付该订单");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            throw new CrmebException("订单已支付，请勿重复支付");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PAY)) {
            throw new CrmebException("订单当前状态不可支付");
        }
        User user = userService.getById(uid);
        if (user == null) {
            throw new CrmebException("用户不存在");
        }
        String payType = request.getPayType() == null ? "" : request.getPayType().toLowerCase();
        HashMap<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("totalPrice", order.getTotalPrice());
        // 余额支付
        if (PayConstants.PAY_TYPE_YUE.equals(payType)) {
            if (user.getNowMoney() == null || user.getNowMoney().compareTo(order.getTotalPrice()) < 0) {
                throw new CrmebException("用户余额不足");
            }
            transactionTemplate.executeWithoutResult(status -> {
                userService.updateNowMoney(user, order.getTotalPrice(), "sub");
                // 余额账单
                UserBill bill = new UserBill();
                bill.setPm(0);
                bill.setUid(uid);
                bill.setLinkId(order.getId().toString());
                bill.setTitle("购买商品");
                bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
                bill.setType(Constants.USER_BILL_TYPE_PAY_ORDER);
                bill.setNumber(order.getTotalPrice());
                bill.setBalance(user.getNowMoney().subtract(order.getTotalPrice()));
                bill.setMark("支付订货单 " + order.getOrderNo());
                userBillService.save(bill);
            });
            // 同步完成订单付款流转
            stockOrderService.payStockOrder(order.getOrderNo(), StockOrder.PAY_TYPE_YUE);
            result.put("payType", PayConstants.PAY_TYPE_YUE);
            result.put("status", true);
            return result;
        }
        // 微信支付（公众号/小程序 JSAPI）
        if (PayConstants.PAY_TYPE_WE_CHAT.equals(payType)) {
            String channel = request.getPayChannel() == null ? "routine" : request.getPayChannel();
            boolean isPublic = PayConstants.PAY_CHANNEL_WE_CHAT_PUBLIC.equals(channel);
            int tokenType = isPublic ? UserConstants.USER_TOKEN_TYPE_WECHAT : UserConstants.USER_TOKEN_TYPE_ROUTINE;
            UserToken userToken = userTokenService.getTokenByUserId(uid, tokenType);
            if (userToken == null || userToken.getToken() == null) {
                throw new CrmebException("当前渠道缺少支付所需的openId，请使用余额支付或在微信内重试");
            }
            String appId = systemConfigService.getValueByKeyException(isPublic
                    ? Constants.CONFIG_KEY_PAY_WE_CHAT_APP_ID : Constants.CONFIG_KEY_PAY_ROUTINE_APP_ID);
            String mchId = systemConfigService.getValueByKeyException(isPublic
                    ? Constants.CONFIG_KEY_PAY_WE_CHAT_MCH_ID : Constants.CONFIG_KEY_PAY_ROUTINE_MCH_ID);
            String signKey = systemConfigService.getValueByKeyException(isPublic
                    ? Constants.CONFIG_KEY_PAY_WE_CHAT_APP_KEY : Constants.CONFIG_KEY_PAY_ROUTINE_APP_KEY);
            String apiDomain = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_API_URL);
            String siteName = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME);

            AttachVo attachVo = new AttachVo(Constants.SERVICE_PAY_TYPE_STOCK, uid);
            CreateOrderRequestVo vo = new CreateOrderRequestVo();
            vo.setAppid(appId);
            vo.setMch_id(mchId);
            vo.setNonce_str(WxPayUtil.getNonceStr());
            vo.setSign_type(PayConstants.WX_PAY_SIGN_TYPE_MD5);
            vo.setBody(siteName);
            vo.setAttach(JSONObject.toJSONString(attachVo));
            vo.setOut_trade_no(order.getOrderNo());
            vo.setTotal_fee(order.getTotalPrice().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
            vo.setSpbill_create_ip(ip);
            vo.setNotify_url(apiDomain + PayConstants.WX_PAY_NOTIFY_API_URI);
            vo.setTrade_type(PayConstants.WX_PAY_TRADE_TYPE_JS);
            vo.setOpenid(userToken.getToken());
            String sign = WxPayUtil.getSign(vo, signKey);
            vo.setSign(sign);
            // 统一下单（内部自动保存 eb_wechat_pay_info 预支付记录）
            CreateOrderResponseVo responseVo = wechatNewService.payUnifiedorder(vo);

            WxPayJsResultVo jsConfig = new WxPayJsResultVo();
            jsConfig.setAppId(appId);
            jsConfig.setNonceStr(vo.getNonce_str());
            jsConfig.setPackages("prepay_id=".concat(responseVo.getPrepayId()));
            jsConfig.setSignType(PayConstants.WX_PAY_SIGN_TYPE_MD5);
            jsConfig.setTimeStamp(String.valueOf(WxPayUtil.getCurrentTimestamp()));
            HashMap<String, String> signMap = new HashMap<>();
            signMap.put("appId", jsConfig.getAppId());
            signMap.put("nonceStr", jsConfig.getNonceStr());
            signMap.put("package", jsConfig.getPackages());
            signMap.put("signType", jsConfig.getSignType());
            signMap.put("timeStamp", jsConfig.getTimeStamp());
            jsConfig.setPaySign(WxPayUtil.getSign(signMap, signKey));

            // 记录支付方式为微信
            stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                    .eq(StockOrder::getId, order.getId())
                    .set(StockOrder::getPayType, StockOrder.PAY_TYPE_WECHAT));
            result.put("payType", PayConstants.PAY_TYPE_WE_CHAT);
            result.put("status", true);
            result.put("jsConfig", jsConfig);
            return result;
        }
        throw new CrmebException("不支持的支付方式");
    }

    @Resource
    private com.zbkj.service.dao.StockExchangeDao stockExchangeDao;

    /** 换货差价微信支付（JSAPI）：返回前端调起支付所需 jsConfig */
    @Override
    public HashMap<String, Object> payExchangeDiffWeixin(Integer uid, Integer exchangeId, String channel, String ip) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_exchange_diff_wechat"))) {
            throw new CrmebException("后台未开启「换货差价支持微信支付」，请使用余额支付");
        }
        com.zbkj.common.model.stock.StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1 || !uid.equals(exchange.getUid())) {
            throw new CrmebException("换货单不存在");
        }
        if (exchange.getDiffPrice() == null || exchange.getDiffPrice().signum() <= 0) {
            throw new CrmebException("该换货单无需支付差价");
        }
        if (exchange.getDiffPayStatus() != null && exchange.getDiffPayStatus() == 1) {
            throw new CrmebException("差价已支付，请勿重复支付");
        }
        boolean isPublic = PayConstants.PAY_CHANNEL_WE_CHAT_PUBLIC.equals(channel == null ? "routine" : channel);
        int tokenType = isPublic ? UserConstants.USER_TOKEN_TYPE_WECHAT : UserConstants.USER_TOKEN_TYPE_ROUTINE;
        UserToken userToken = userTokenService.getTokenByUserId(uid, tokenType);
        if (userToken == null || userToken.getToken() == null) {
            throw new CrmebException("当前渠道缺少支付所需的openId，请使用余额支付或在微信内重试");
        }
        String appId = systemConfigService.getValueByKeyException(isPublic
                ? Constants.CONFIG_KEY_PAY_WE_CHAT_APP_ID : Constants.CONFIG_KEY_PAY_ROUTINE_APP_ID);
        String mchId = systemConfigService.getValueByKeyException(isPublic
                ? Constants.CONFIG_KEY_PAY_WE_CHAT_MCH_ID : Constants.CONFIG_KEY_PAY_ROUTINE_MCH_ID);
        String signKey = systemConfigService.getValueByKeyException(isPublic
                ? Constants.CONFIG_KEY_PAY_WE_CHAT_APP_KEY : Constants.CONFIG_KEY_PAY_ROUTINE_APP_KEY);
        String apiDomain = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_API_URL);
        String siteName = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_SITE_NAME);

        AttachVo attachVo = new AttachVo("exchange", uid);
        CreateOrderRequestVo vo = new CreateOrderRequestVo();
        vo.setAppid(appId);
        vo.setMch_id(mchId);
        vo.setNonce_str(WxPayUtil.getNonceStr());
        vo.setSign_type(PayConstants.WX_PAY_SIGN_TYPE_MD5);
        vo.setBody(siteName);
        vo.setAttach(JSONObject.toJSONString(attachVo));
        vo.setOut_trade_no(exchange.getExchangeNo());
        vo.setTotal_fee(exchange.getDiffPrice().multiply(new BigDecimal(100)).intValue());
        vo.setSpbill_create_ip(ip);
        vo.setNotify_url(apiDomain + PayConstants.WX_PAY_NOTIFY_API_URI);
        vo.setTrade_type(PayConstants.WX_PAY_TRADE_TYPE_JS);
        vo.setOpenid(userToken.getToken());
        vo.setSign(WxPayUtil.getSign(vo, signKey));
        CreateOrderResponseVo responseVo = wechatNewService.payUnifiedorder(vo);

        WxPayJsResultVo jsConfig = new WxPayJsResultVo();
        jsConfig.setAppId(appId);
        jsConfig.setNonceStr(vo.getNonce_str());
        jsConfig.setPackages("prepay_id=".concat(responseVo.getPrepayId()));
        jsConfig.setSignType(PayConstants.WX_PAY_SIGN_TYPE_MD5);
        jsConfig.setTimeStamp(String.valueOf(WxPayUtil.getCurrentTimestamp()));
        HashMap<String, String> signMap = new HashMap<>();
        signMap.put("appId", jsConfig.getAppId());
        signMap.put("nonceStr", jsConfig.getNonceStr());
        signMap.put("package", jsConfig.getPackages());
        signMap.put("signType", jsConfig.getSignType());
        signMap.put("timeStamp", jsConfig.getTimeStamp());
        jsConfig.setPaySign(WxPayUtil.getSign(signMap, signKey));

        HashMap<String, Object> result = new HashMap<>();
        result.put("payType", PayConstants.PAY_TYPE_WE_CHAT);
        result.put("status", true);
        result.put("jsConfig", jsConfig);
        return result;
    }
}
