package com.qxkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.WeChatConstants;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.service.WechatNewService;
import com.qxkj.service.service.WechatPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class WechatPublicServiceImpl implements WechatPublicService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private WechatNewService wechatNewService;

    /**
     * 获取公众号自定义菜单
     * @return Object
     */
    @Override
    public Object getCustomizeMenus() {
        Object list = redisUtil.get(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        if (list == null || list.equals("")) {
            //如果没有， 去读取
            JSONObject tagsList = wechatNewService.getPublicCustomMenu();
            redisUtil.set(WeChatConstants.REDIS_PUBLIC_MENU_KEY, tagsList);
            list = tagsList;
        }
        return list;
    }

    /**
     * 保存自定义菜单
     * @param data 菜单json
     * @return Boolean
     */
    @Override
    public Boolean createMenus(String data) {
        Boolean create = wechatNewService.createPublicCustomMenu(data);
        if (!create) {
            return create;
        }
        // 清除历史缓存
        if (redisUtil.exists(WeChatConstants.REDIS_PUBLIC_MENU_KEY)) {
            redisUtil.delete(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        }
        return create;
    }

    /**
     * 删除自定义菜单
     * @return Boolean
     */
    @Override
    public Boolean deleteMenus() {
        Boolean delete = wechatNewService.deletePublicCustomMenu();
        if (!delete) {
            return delete;
        }
        // 清除历史缓存
        if (redisUtil.exists(WeChatConstants.REDIS_PUBLIC_MENU_KEY)) {
            redisUtil.delete(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        }
        return delete;
    }
}
