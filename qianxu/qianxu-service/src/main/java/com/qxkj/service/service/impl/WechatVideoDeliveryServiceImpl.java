package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.WeChatConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.RestTemplateUtil;
import com.qxkj.common.utils.WxUtil;
import com.qxkj.common.vo.DeliveryCompanyVo;
import com.qxkj.common.vo.DeliveryInfoVo;
import com.qxkj.common.vo.DeliverySendVo;
import com.qxkj.common.vo.ShopOrderCommonVo;
import com.qxkj.service.service.WechatNewService;
import com.qxkj.service.service.WechatVideoDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Service
public class WechatVideoDeliveryServiceImpl implements WechatVideoDeliveryService {

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Autowired
    private WechatNewService wechatNewService;

    /**
     * 获取快递公司列表
     * @return List<DeliveryCompanyVo>
     */
    @Override
    public List<DeliveryCompanyVo> shopDeliveryGetCompanyList() {
        // 获取accessToken
        String miniAccessToken = wechatNewService.getMiniAccessToken();
        // 请求微信接口
        String url = StrUtil.format(WeChatConstants.WECHAT_SHOP_DELIVERY_GET_COMPANY_LIST_URL, miniAccessToken);
        String stringData = restTemplateUtil.postStringData(url, "{}");
        JSONObject jsonObject = JSONObject.parseObject(stringData);
        WxUtil.checkResult(jsonObject);
        List<DeliveryCompanyVo> voList = JSONArray.parseArray(jsonObject.getJSONArray("company_list").toJSONString(), DeliveryCompanyVo.class);
        return voList;
    }

    /**
     * 订单发货
     * @return Boolean
     */
    @Override
    public Boolean shopDeliverySend(DeliverySendVo deliverySendVo) {
        Map<String, Object> sendMap = assembleSendMap(deliverySendVo);
        // 获取accessToken
        String miniAccessToken = wechatNewService.getMiniAccessToken();
        // 请求微信接口
        String url = StrUtil.format(WeChatConstants.WECHAT_SHOP_DELIVERY_SEND_URL, miniAccessToken);
        String stringData = restTemplateUtil.postStringData(url, JSONObject.toJSONString(sendMap));
        JSONObject jsonObject = JSONObject.parseObject(stringData);
        WxUtil.checkResult(jsonObject);
        return Boolean.TRUE;
    }

    private Map<String, Object> assembleSendMap(DeliverySendVo deliverySendVo) {
        Map<String, Object> map = CollUtil.newHashMap();
        map.put("out_order_id", deliverySendVo.getOutOrderId());
        map.put("openid", deliverySendVo.getOpenid());
        map.put("finish_all_delivery", deliverySendVo.getFinishSllDelivery());
        List<DeliveryInfoVo> deliveryList = deliverySendVo.getDeliveryList();
        List<Map<String, Object>> infoMapList = deliveryList.stream().map(e -> {
            Map<String, Object> infoMap = CollUtil.newHashMap();
            infoMap.put("delivery_id", e.getDeliveryId());
            infoMap.put("waybill_id", e.getWaybillId());
            return infoMap;
        }).collect(Collectors.toList());
        map.put("delivery_list", infoMapList);
        return map;
    }

    /**
     * 订单确认收货
     * 把订单状态从30（待收货）流转到100（完成）
     * @return Boolean
     */
    @Override
    public Boolean shopDeliveryRecieve(ShopOrderCommonVo shopOrderCommonVo) {
        if (ObjectUtil.isNull(shopOrderCommonVo.getOrderId()) && StrUtil.isBlank(shopOrderCommonVo.getOutOrderId())) {
            throw new QianxuException("订单ID不能为空");
        }
        // 获取accessToken
        String miniAccessToken = wechatNewService.getMiniAccessToken();
        // 请求微信接口
        String url = StrUtil.format(WeChatConstants.WECHAT_SHOP_DELIVERY_RECIEVE_URL, miniAccessToken);
        Map<String, Object> map = new HashMap<>();
        if (ObjectUtil.isNotNull(shopOrderCommonVo.getOrderId())) {
            map.put("order_id", shopOrderCommonVo.getOrderId());
        }
        if (StrUtil.isNotBlank(shopOrderCommonVo.getOutOrderId())) {
            map.put("out_order_id", shopOrderCommonVo.getOutOrderId());
        }
        map.put("openid", shopOrderCommonVo.getOpenid());
        String mapData = restTemplateUtil.postMapData(url, map);
        JSONObject jsonObject = JSONObject.parseObject(mapData);
        WxUtil.checkResult(jsonObject);
        return Boolean.TRUE;
    }
}
