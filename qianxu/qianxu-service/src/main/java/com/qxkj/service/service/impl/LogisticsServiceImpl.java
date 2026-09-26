package com.qxkj.service.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.utils.RestTemplateUtil;
import com.qxkj.common.vo.LogisticsResultVo;
import com.qxkj.service.service.LogisticService;
import com.qxkj.service.service.SystemConfigService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


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
@Data
@Service
public class LogisticsServiceImpl implements LogisticService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Autowired
    private RedisUtil redisUtil;

    private String redisKey = Constants.LOGISTICS_KEY;
    private Long redisCacheSeconds = 1800L;

    private String expressNo;


    /** 快递
     * @param expressNo String 物流单号
     * @param type String 快递公司字母简写：不知道可不填 95%能自动识别，填写查询速度会更快 https://market.aliyun.com/products/56928004/cmapi021863.html#sku=yuncode15863000015
     * @param com 快递公司编号
     * @author Mr.Zhang
     * @since 2020-06-10
     * @return Express
     */
    @Override
    public LogisticsResultVo info(String expressNo, String type, String com, String phone) {
        LogisticsResultVo resultVo;
        setExpressNo(expressNo);
        JSONObject result = getCache();
        if (ObjectUtil.isNotNull(result)) {
            return JSONObject.toJavaObject(result, LogisticsResultVo.class);
        }

        // 统一使用阿里云物流查询
        String appCode = systemConfigService.getValueByKey(Constants.CONFIG_KEY_LOGISTICS_APP_CODE);

        // 顺丰请输入单号 : 收件人或寄件人手机号后四位。例如：123456789:1234
        if (StrUtil.isNotBlank(com) && com.equals("shunfengkuaiyun")) {
            expressNo = expressNo.concat(":").concat(StrUtil.sub(phone, 7, -1));
        }
        String url = Constants.LOGISTICS_API_URL + "?no=" + expressNo;
        if (StringUtils.isNotBlank(type)) {
            url += "&type=" + type;
        }

        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", "APPCODE " + appCode);

        JSONObject data = restTemplateUtil.getData(url, header);
        checkResult(data);
        //把数据解析成对象返回到前端
        result = data.getJSONObject("result");
        saveCache(result);
        resultVo = JSONObject.toJavaObject(result, LogisticsResultVo.class);
        return resultVo;
    }

    /** 获取快递缓存
     * @author Mr.Zhang
     * @since 2020-07-06
     * @return JSONObject
     */
    private JSONObject getCache() {
        Object data = redisUtil.get(getRedisKey() + getExpressNo());
        if (null != data) {
            return JSONObject.parseObject(data.toString());
        }
        return null;
    }

    /** 获取快递缓存
     * @param data JSONObject 需要保存的数据
     * @author Mr.Zhang
     * @since 2020-07-06
     */
    private void saveCache(JSONObject data) {
        redisUtil.set(getRedisKey() + getExpressNo(), data.toJSONString(), getRedisCacheSeconds(), TimeUnit.SECONDS);
    }

    /** 获取快递缓存
     * @param data JSONObject 检测返回的结果
     * @author Mr.Zhang
     * @since 2020-07-06
     */
    private void checkResult(JSONObject data) {
        if (!data.getString("status").equals("0")) {
            throw new QianxuException(data.getString("msg"));
        }
    }
}
