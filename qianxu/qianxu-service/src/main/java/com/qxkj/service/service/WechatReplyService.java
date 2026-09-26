package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.wechat.WechatReply;
import com.qxkj.common.request.WechatReplyRequest;
import com.qxkj.common.request.WechatReplySearchRequest;

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
public interface WechatReplyService extends IService<WechatReply> {

    /**
     * 列表
     * @param request 请求参数
     * @param pageParamRequest 分页类参数
     * @return List<WechatReply>
     */
    List<WechatReply> getList(WechatReplySearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增微信关键字回复表
     * @param wechatReply 新增参数
     */
    Boolean create(WechatReply wechatReply);

    /**
     * 根据关键字查询数据
     * @param keywords 新增参数
     * @return WechatReply
     */
    WechatReply getVoByKeywords(String keywords);

    /**
     * 查询微信关键字回复表信息
     * @param id Integer
     */
    WechatReply getInfo(Integer id);

    /**
     * 修改微信关键字回复表
     * @param wechatReplyRequest 修改参数
     */
    Boolean updateReply(WechatReplyRequest wechatReplyRequest);

    /**
     * 修改状态
     * @param id integer id
     * @param status boolean 状态
     */
    Boolean updateStatus(Integer id, Boolean status);
}
