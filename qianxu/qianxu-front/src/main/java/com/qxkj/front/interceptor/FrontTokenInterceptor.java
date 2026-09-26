package com.qxkj.front.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.token.FrontTokenComponent;
import com.qxkj.common.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class FrontTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private FrontTokenComponent frontTokenComponent;

    //程序处理之前需要处理的业务
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("UTF-8");
        String token = frontTokenComponent.getToken(request);
        if(token == null || token.isEmpty()){
            //判断路由，部分路由不管用户是否登录都可以访问
            boolean result = frontTokenComponent.checkRouter(RequestUtil.getUri(request));
            if(result){
                return true;
            }
            response.getWriter().write(JSONObject.toJSONString(CommonResult.failed(CommonResultCode.UNAUTHORIZED)));
            return false;
        }

        Boolean result = frontTokenComponent.check(token, request);
        if(!result){
            response.getWriter().write(JSONObject.toJSONString(CommonResult.failed(CommonResultCode.PERMISSION_EXPIRATION)));
            return false;
        }
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    }

}
