package com.qxkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.vo.QrCodeVo;

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
public interface QrCodeService {

    /**
     * 获取二维码
     *
     * @return QrCodeVo
     */
    QrCodeVo getWecahtQrCode(JSONObject data);

    /**
     * 远程图片转base64
     *
     * @param url 图片链接地址
     * @return QrCodeVo
     */
    QrCodeVo urlToBase64(String url);

    /**
     * 将字符串 转base64
     *  @param text   字符串
     * @param width  宽
     * @param height 高
     * @return QrCodeVo
     */
    QrCodeVo strToBase64(String text, Integer width, Integer height);

    /**
     * 获取二维码
     * @return CommonResult
     */
    Map<String, Object> get(JSONObject data);

    /**
     * 远程图片转base64
     * @param url 图片链接地址
     */
    Map<String, Object> base64(String url);

    /**
     * 将字符串 转base64
     * @param text 字符串
     * @param width 宽
     * @param height 高
     */
    Map<String, Object> base64String(String text,int width, int height);

}
