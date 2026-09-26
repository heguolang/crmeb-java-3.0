package com.qxkj.admin.pub;

import com.qxkj.admin.service.WeChatMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


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
@Slf4j
@RestController
@RequestMapping("api/public/wechat/message")
@Api(tags = "微信开放平台 -- 消息")
public class WeChatMessageController {

    @Autowired
    private WeChatMessageService weChatMessageService;

    /**
     * 微信关键字回复
     * @author Mr.Zhang
     * @since 2020-04-22
     */
    @ApiOperation(value = "接受微信推送过来的消息")
    @RequestMapping(value = "/webHook", method = RequestMethod.POST)
    public String webHook(HttpServletRequest request){
       return weChatMessageService.init(request);
    }

    /**
     * 微信推送地址验证
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = { "/webHook" }, method = RequestMethod.GET)
    private void webHook(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr") String echostr,
            HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.print(echostr);
        writer.flush();
        writer.close();
    }
}



