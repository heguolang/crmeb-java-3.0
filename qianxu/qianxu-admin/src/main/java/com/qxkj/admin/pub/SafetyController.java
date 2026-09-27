package com.qxkj.admin.pub;

import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.service.service.SafetyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
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
@Slf4j
@RestController
@RequestMapping("api/public/safety")
@Api(tags = "安全验证控制器")
public class SafetyController {

    @Autowired
    private SafetyService safetyService;

    @Resource
    private RedisUtil redisUtil;

    @ApiOperation(value = "获取行为验证码")
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public CommonResult<ResponseModel> getSafetyCode(@RequestBody com.anji.captcha.model.vo.CaptchaVO data, HttpServletRequest request) {
        return CommonResult.success(safetyService.getSafetyCode(data, request));
    }

    @ApiOperation(value = "验证行为验证码")
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public CommonResult<ResponseModel> checkSafetyCode(@RequestBody com.anji.captcha.model.vo.CaptchaVO data, HttpServletRequest request) {
        return CommonResult.success(safetyService.checkSafetyCode(data, request));
    }

    @ApiOperation(value = "行为验证码二次校验")
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public CommonResult<ResponseModel> verifySafetyCode(@RequestBody com.anji.captcha.model.vo.CaptchaVO data, HttpServletRequest request) {
        return CommonResult.success(safetyService.verifySafetyCode(data));
    }

    @ApiOperation(value = "获取后台登录图形验证码")
    @RequestMapping(value = "/captcha/image", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> getImageCaptcha() {
        // 4 位字符验证码，剔除易混淆字符 0/O/1/I；2 倍尺寸 + 粗体大字号，前端缩小显示保证清晰
        ShearCaptcha captcha = new ShearCaptcha(260, 96, 4, 2);
        captcha.setFont(new Font("Arial", Font.BOLD, 62));
        captcha.setBackground(new Color(238, 241, 247));
        String code = captcha.getCode();
        String key = IdUtil.fastSimpleUUID();
        // 2 分钟有效，小写入库便于忽略大小写比对，一次性使用
        redisUtil.set(StrUtil.format(Constants.ADMIN_IMAGE_CAPTCHA_KEY, key), code.toLowerCase(), 120L, TimeUnit.SECONDS);
        Map<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("image", "data:image/png;base64," + captcha.getImageBase64());
        return CommonResult.success(map);
    }
}
