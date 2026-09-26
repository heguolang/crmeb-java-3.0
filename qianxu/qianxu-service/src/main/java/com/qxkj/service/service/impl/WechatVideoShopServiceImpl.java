package com.qxkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.WeChatConstants;
import com.qxkj.common.utils.RestTemplateUtil;
import com.qxkj.common.utils.WxUtil;
import com.qxkj.common.vo.BaseResultResponseVo;
import com.qxkj.common.vo.RegisterCheckResponseVo;
import com.qxkj.service.service.WechatNewService;
import com.qxkj.service.service.WechatVideoShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
public class WechatVideoShopServiceImpl implements WechatVideoShopService {

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Autowired
    private WechatNewService wechatNewService;

    /**
     * 接入申请
     *
     * @return 接入结果
     */
    @Override
    public BaseResultResponseVo shopRegisterApply() {
        // get accessToken
        String miniAccessToken = wechatNewService.getMiniAccessToken();
        // 请求微信接口
        String url = StrUtil.format(WeChatConstants.WECHAT_SHOP_REGISTER_APPLY, miniAccessToken);
        Map<String, Object> map = new HashMap<>();
        String mapData = restTemplateUtil.postMapData(url, map);
        JSONObject jsonObject = JSONObject.parseObject(mapData);
        WxUtil.checkResult(jsonObject);
        return JSONObject.parseObject(jsonObject.toJSONString(),BaseResultResponseVo.class);
    }

    /**
     * 获取接入状态
     *
     * @return 接入状态结果
     */
    @Override
    public RegisterCheckResponseVo shopRegisterCheck() {
        // get accessToken
        String miniAccessToken = wechatNewService.getMiniAccessToken();
        // 请求微信接口
        String url = StrUtil.format(WeChatConstants.WECHAT_SHOP_REGISTER_CHECK, miniAccessToken);
        Map<String, Object> map = new HashMap<>();
        String mapData = restTemplateUtil.postMapData(url, map);
        JSONObject jsonObject = JSONObject.parseObject(mapData);
        WxUtil.checkResult(jsonObject);
        return JSONObject.parseObject(jsonObject.toJSONString(),RegisterCheckResponseVo.class);
    }
}
