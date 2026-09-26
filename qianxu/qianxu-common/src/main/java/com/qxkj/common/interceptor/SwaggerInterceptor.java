package com.qxkj.common.interceptor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import java.util.Base64;

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
public class SwaggerInterceptor extends HandlerInterceptorAdapter {
    private String username;
    private String password;
    private Boolean check;
    public SwaggerInterceptor(String username, String password, Boolean check) {
        this.username = username;
        this.password = password;
        this.check = check;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        boolean isAuthSuccess = httpBasicAuth(authorization);
        if (!isAuthSuccess) {
            response.setCharacterEncoding("utf-8");
            response.setStatus(401);
//            response.setStatus(401,"Unauthorized");
            response.setHeader("WWW-authenticate", "Basic realm=\"Realm\"");
            try (PrintWriter writer = response.getWriter()) {
                writer.print("Forbidden, unauthorized user");
            }
        }
        return isAuthSuccess;
    }
    public boolean httpBasicAuth(String authorization) throws IOException {
        if(check){
            if (authorization != null && authorization.split(" ").length == 2) {
                String userAndPass = new String(Base64.getDecoder().decode(authorization.split(" ")[1]));                String username = userAndPass.split(":").length == 2 ? userAndPass.split(":")[0] : null;
                String password = userAndPass.split(":").length == 2 ? userAndPass.split(":")[1] : null;
                return this.username.equals(username) && this.password.equals(password);
            }
            return false;
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        if (!pathMatcher.match("/swagger-ui.html", uri) && !pathMatcher.match("/webjars/**", uri)) {
            response.setStatus(404);
            return;
        }
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/META-INF/resources" + uri);
        if (resources.length > 0) {
            FileCopyUtils.copy(resources[0].getInputStream(), response.getOutputStream());
        } else {
            response.setStatus(404);
        }
    }
}
