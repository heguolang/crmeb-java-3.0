package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.utils.QRCodeUtil;
import com.qxkj.common.utils.RestTemplateUtil;
import com.qxkj.common.vo.QrCodeVo;
import com.qxkj.service.service.QrCodeService;
import com.qxkj.service.service.WechatNewService;
import org.apache.commons.codec.binary.Base64;
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
public class QrCodeServiceImpl implements QrCodeService {
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private WechatNewService wechatNewService;

    /**
     * 二维码
     *
     * @return QrCodeVo
     */
    @Override
    public QrCodeVo getWecahtQrCode(JSONObject data) {
//        StringBuilder scene = new StringBuilder();
//        String page = "";
//        try {
//            if (ObjectUtil.isNotNull(data)) {
//                Map<Object, Object> dataMap = JSONObject.toJavaObject(data, Map.class);
//
//                for (Map.Entry<Object, Object> m : dataMap.entrySet()) {
//                    if (m.getKey().equals("path")) {
//                        //前端路由， 不需要拼参数
//                        page = m.getValue().toString();
//                        continue;
//                    }
//                    if (scene.length() > 0) {
//                        scene.append(",");
//                    }
//                    scene.append(m.getKey()).append(":").append(m.getValue());
//                }
//            }
//        } catch (Exception e) {
//            throw new QianxuException("url参数错误 " + e.getMessage());
//        }
        if (ObjectUtil.isNull(data) || data.isEmpty())
            throw new QianxuException(CommonResultCode.VALIDATE_FAILED, "生成微信参数不能为空");
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(wechatNewService.createQrCode(data));
        return vo;
    }

    /**
     * 远程图片转base64
     *
     * @param url 图片链接地址
     * @return QrCodeVo
     */
    @Override
    public QrCodeVo urlToBase64(String url) {
        HttpResponse httpResponse = HttpRequest.get(url).execute();
        byte[] bytes = httpResponse.bodyBytes();
        ;
        String base64Image = QianxuUtil.getBase64Image(Base64.encodeBase64String(bytes));
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(base64Image);
        return vo;
    }

    /**
     * 字符串转base64
     *
     * @param text 待转换字符串
     * @return QrCodeVo base64格式
     */
    @Override
    public QrCodeVo strToBase64(String text, Integer width, Integer height) {
        if ((width < 50 || height < 50) && (width > 500 || height > 500) && text.length() >= 999) {
            throw new QianxuException(CommonResultCode.VALIDATE_FAILED, "生成二维码参数不合法");
        }
        String base64Image;
        try {
            base64Image = QRCodeUtil.crateQRCode(text, width, height);
        } catch (Exception e) {
            throw new QianxuException("生成二维码异常");
        }
        QrCodeVo vo = new QrCodeVo();
        vo.setCode(base64Image);
        return vo;
    }

    /**
     * 二维码
     * @return Object
     */
    @Override
    public Map<String, Object> get(JSONObject data) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder scene = new StringBuilder();
        String page = "";
        try{
            if(null != data){
                Map<Object, Object> dataMap = JSONObject.toJavaObject(data, Map.class);

                for (Map.Entry<Object, Object> m : dataMap.entrySet()) {
                    if(m.getKey().equals("path")){
                        //前端路由， 不需要拼参数
                        page = m.getValue().toString();
                        continue;
                    }
                    if (scene.length() > 0) {
                        scene.append(",");
                    }
                    scene.append(m.getKey()).append(":").append(m.getValue());
                }
            }
        }catch (Exception e){
            throw new QianxuException("url参数错误 " + e.getMessage());
        }
        map.put("code", wechatNewService.createQrCode(page, scene.length() > 0 ? scene.toString() : ""));
        return map;
    }

    @Override
    public Map<String, Object> base64(String url) {
        byte[] bytes = restTemplateUtil.getBuffer(url);
        String base64Image = QianxuUtil.getBase64Image(Base64.encodeBase64String(bytes));
        Map<String, Object> map = new HashMap<>();
        map.put("code", base64Image);
        return map;
    }

    /**
     * 讲字符串转为QRcode
     * @param text 待转换字符串
     * @return QRcode base64格式
     */
    @Override
    public Map<String, Object> base64String(String text,int width, int height) {

        String base64Image = null;
        try {
            base64Image = QRCodeUtil.crateQRCode(text,width,height);
        }catch (Exception e){
            throw new QianxuException("生成二维码异常");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("code", base64Image);
        return map;
    }
}

