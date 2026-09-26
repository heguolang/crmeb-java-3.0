package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.TemplateMessage;

import java.util.HashMap;
import java.util.List;

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
public interface TemplateMessageService extends IService<TemplateMessage> {

    /**
     * 公众号消费队列消费
     */
    void consumePublic();

    /**
     * 小程序消费队列消费
     */
    void consumeProgram();

    /**
     * 发送公众号模板消息
     * @param templateId 模板消息编号
     * @param temMap 内容Map
     * @param openId 微信用户openId
     */
    void pushTemplateMessage(Integer templateId, HashMap<String, String> temMap, String openId);

    /**
     * 发送小程序订阅消息
     * @param templateId 模板消息编号
     * @param temMap 内容Map
     * @param openId 微信用户openId
     */
    void pushMiniTemplateMessage(Integer templateId, HashMap<String, String> temMap, String openId);

    /**
     * 修改模板状态
     * @param id 模板id
     * @param status 状态
     */
    Boolean updateStatus(Integer id, Integer status);

    /**
     * 公众号模板消息同步
     * @return Boolean
     */
    Boolean whcbqhnSync();

    /**
     * 小程序订阅消息同步
     * @return Boolean
     */
    Boolean routineSync();

    /**
     * 获取详情
     * @param id id
     */
    TemplateMessage info(Integer id);

    /**
     * 获取模板列表
     * @param tidList id数组
     * @return List
     */
    List<TemplateMessage> getByIdList(List<Integer> tidList);
}
