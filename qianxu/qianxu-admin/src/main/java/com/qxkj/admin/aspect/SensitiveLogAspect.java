package com.qxkj.admin.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.admin.filter.TokenComponent;
import com.qxkj.admin.service.ActionService;
import com.qxkj.common.model.log.SensitiveMethodLog;
import com.qxkj.common.utils.SpringUtil;
import com.qxkj.common.vo.LoginUserVo;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

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
@Aspect
@Component
public class SensitiveLogAspect {

    @Resource
    private ActionService actionService;

    @Pointcut("@annotation(com.qxkj.common.annotation.LogControllerAnnotation)")
    public void controllerAspect() {
    }

    /**
     * 处理完请求后执行
     * @param joinPoint 切点
     * @param logServiceAnnotation 注解
     */
    @AfterReturning(pointcut = "controllerAspect() && @annotation(logServiceAnnotation)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, LogControllerAnnotation logServiceAnnotation, Object jsonResult) {
        addLog(joinPoint, logServiceAnnotation, jsonResult, null);
    }

    /**
     * 拦截异常操作
     * @param joinPoint 切点
     * @param logServiceAnnotation 注解
     * @param e 异常
     */
    @AfterThrowing(pointcut = "controllerAspect() && @annotation(logServiceAnnotation)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, LogControllerAnnotation logServiceAnnotation, Exception e) {
        addLog(joinPoint, logServiceAnnotation, null, e);
    }

    private void addLog(JoinPoint joinPoint, LogControllerAnnotation logServiceAnnotation, Object jsonResult, Exception e) {
        if (!logServiceAnnotation.intoDB()) {
            return;
        }

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        // 获取当前用户
        LoginUserVo loginUser = SpringUtil.getBean(TokenComponent.class).getLoginUser(request);
        // 系统运维账号的所有操作不写入操作日志
        if (loginUser != null && "qxtec".equals(loginUser.getUser().getAccount())) {
            return;
        }
        Signature signature = joinPoint.getSignature();

        SensitiveMethodLog methodLog = new SensitiveMethodLog();
        methodLog.setAdminId(loginUser.getUser().getId());
        methodLog.setAdminAccount(loginUser.getUser().getAccount());
        methodLog.setDescription(logServiceAnnotation.description());
        methodLog.setMethodType(logServiceAnnotation.methodType().getName());
        methodLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName() + "()");
        methodLog.setRequestMethod(request.getMethod());
        methodLog.setUrl(request.getRequestURI());
        methodLog.setIp(ServletUtil.getClientIP(request));
        String requestValue = getRequestValue(joinPoint, request.getMethod(), request);
        methodLog.setRequestParam(requestValue);
        if (jsonResult != null) {
            methodLog.setResult(StrUtil.sub(JSON.toJSONString(jsonResult), 0, 2000));
        }
        methodLog.setStatus(0);
        if (e != null) {
            methodLog.setStatus(1);
            methodLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
        }
        actionService.addSensitiveLog(methodLog);
    }

    private String getRequestValue(JoinPoint joinPoint, String requestMethod, HttpServletRequest request) {
        if (requestMethod.equals("PUT") || requestMethod.equals("POST")) {
            return StrUtil.sub(argsArrayToString(joinPoint.getArgs()), 0, 2000);
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            return StrUtil.sub(paramsMap.toString(), 0, 2000);
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (!isFilterObject(o)) {
                    Object jsonObj = JSON.toJSON(o);
                    params.append(jsonObj.toString()).append(" ");
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o)
    {
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }
}
