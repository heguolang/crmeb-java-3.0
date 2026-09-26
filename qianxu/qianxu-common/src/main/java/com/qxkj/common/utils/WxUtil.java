package com.qxkj.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.exception.QianxuException;
import org.apache.commons.codec.binary.Base64;

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
public class WxUtil {

    private static final String WATERMARK = "watermark";
    private static final String APPID = "appid";
    /**
     * 解密数据
     * @return
     * @throws Exception
     */
    public static String decrypt(String appId, String encryptedData, String sessionKey, String iv){
        String result = "";
        try {
            AESUtil aes = new AESUtil();
            byte[] resultByte = aes.decrypt(Base64.decodeBase64(encryptedData), Base64.decodeBase64(sessionKey), Base64.decodeBase64(iv));
            if(null != resultByte && resultByte.length > 0){
                result = new String(WxPKCS7Encoder.decode(resultByte));
                JSONObject jsonObject = JSONObject.parseObject(result);
                String decryptAppid = jsonObject.getJSONObject(WATERMARK).getString(APPID);
                if(!appId.equals(decryptAppid)){
                    result = "";
                }
            }
        } catch (Exception e) {
            result = "";
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 检测结构请求返回的数据
     * @param result 接口返回的结果
     * @return JSONObject
     */
    public static JSONObject checkResult(JSONObject result){
        if(ObjectUtil.isNull(result)){
            throw new QianxuException("微信平台接口异常，没任何数据返回！");
        }
        if(result.containsKey("errcode") && result.getString("errcode").equals("0")){
            return result;
        }
        if(result.containsKey("errmsg")){
            throw new QianxuException("微信接口调用失败：" + result.getString("errcode") + result.getString("errmsg"));
        }
        return result;
    }

}
