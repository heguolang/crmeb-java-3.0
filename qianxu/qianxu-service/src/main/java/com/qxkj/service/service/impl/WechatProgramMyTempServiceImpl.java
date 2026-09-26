package com.qxkj.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.vo.ProgramTempKeywordsVo;
import com.qxkj.common.vo.ProgramTemplateMessageVo;
import com.qxkj.common.vo.SendProgramTemplateMessageItemVo;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.model.user.UserToken;
import com.qxkj.common.model.wechat.WechatProgramMyTemp;
import com.qxkj.common.request.WechatProgramMyTempSearchRequest;
import com.qxkj.service.dao.WechatProgramMyTempDao;
import com.qxkj.service.service.UserTokenService;
import com.qxkj.service.service.WechatProgramMyTempService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@Service
public class WechatProgramMyTempServiceImpl extends ServiceImpl<WechatProgramMyTempDao, WechatProgramMyTemp> implements WechatProgramMyTempService {

    @Resource
    private WechatProgramMyTempDao dao;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private RedisUtil redisUtil;

    /**
    * 列表
    * @param request 请求参数
    * @param pageParamRequest 分页类参数
    * @return List<WechatProgramMyTemp>
    */
    @Override
    public List<WechatProgramMyTemp> getList(WechatProgramMyTempSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        //带 WechatProgramMyTemp 类的多条件查询
        LambdaQueryWrapper<WechatProgramMyTemp> lambdaQueryWrapper = Wrappers.lambdaQuery();
        //模板标题模糊查询，其余精确查询
        if (StringUtils.isNotBlank(request.getTitle())) {
            lambdaQueryWrapper.like(WechatProgramMyTemp::getTitle, request.getTitle());
        }

        if (StringUtils.isNotBlank(request.getTempId())) {
            lambdaQueryWrapper.eq(WechatProgramMyTemp::getTempId, request.getTempId());
        }

        if (null != request.getStatus()) {
            lambdaQueryWrapper.eq(WechatProgramMyTemp::getStatus, request.getStatus());
        }

        if (StringUtils.isNotBlank(request.getType())) {
            lambdaQueryWrapper.eq(WechatProgramMyTemp::getType, request.getType());
        }
        if (null != request.getId()) {
            lambdaQueryWrapper.eq(WechatProgramMyTemp::getId, request.getId());
        }

        lambdaQueryWrapper.orderByDesc(WechatProgramMyTemp::getUpdateTime);
        return dao.selectList(lambdaQueryWrapper);
    }

    @Override
    public void push(int myTempId, HashMap<String, String> map, Integer userId) {
        // 查询对应id并且状态启用的模版
        WechatProgramMyTempSearchRequest pram = new WechatProgramMyTempSearchRequest();
//        pram.setTempId(tempKey+"");
        pram.setId(myTempId);
        pram.setStatus(true);
        List<WechatProgramMyTemp> existMyTemps = getList(pram, new PageParamRequest());
        if (null == existMyTemps) {
            return;
        }
        WechatProgramMyTemp wechatProgramMyTemp = null;
        if (existMyTemps.size() >= 1) wechatProgramMyTemp = existMyTemps.get(0);
        if (wechatProgramMyTemp == null || StringUtils.isBlank(wechatProgramMyTemp.getKid())) {
            return;
        }

        //拿到三方登录绑定的token
        UserToken UserToken = userTokenService.getTokenByUserId(userId, Constants.THIRD_LOGIN_TOKEN_TYPE_PROGRAM);

        if (null == UserToken || StringUtils.isBlank(UserToken.getToken())) {
            return;
        }

        ProgramTemplateMessageVo programTemplateMessageVo = new ProgramTemplateMessageVo();
        programTemplateMessageVo.setTemplate_id(wechatProgramMyTemp.getTempId());

        //组装关键字数据
        HashMap<String, SendProgramTemplateMessageItemVo> hashMap = new HashMap<>();
        List<ProgramTempKeywordsVo> programTempKeywordsVoList = JSONArray.parseArray(wechatProgramMyTemp.getExtra(), ProgramTempKeywordsVo.class);
        for (ProgramTempKeywordsVo programTempKeywordsVo : programTempKeywordsVoList) {
            if (StringUtils.isBlank(map.get(programTempKeywordsVo.getKey()))) {
                continue;
            }
            hashMap.put(programTempKeywordsVo.getSendKey(), new SendProgramTemplateMessageItemVo(map.get(programTempKeywordsVo.getKey())));
        }

        programTemplateMessageVo.setData(hashMap);
        programTemplateMessageVo.setTouser(UserToken.getToken());
        redisUtil.lPush(Constants.WE_CHAT_MESSAGE_KEY_PROGRAM, JSONObject.toJSONString(programTemplateMessageVo));
    }
}

