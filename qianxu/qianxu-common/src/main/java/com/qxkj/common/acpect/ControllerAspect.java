package com.qxkj.common.acpect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;


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
public class ControllerAspect {

    Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    @Pointcut("execution(* com.qxkj.system.controller.*.*(..))")
    private void pointCutMethodController() {

    }

    @Around("pointCutMethodController()")
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {

        long begin = System.nanoTime();

        Object obj = pjp.proceed();

        long end = System.nanoTime();

        logger.info("Controller method：{}，prams：{}，cost time：{} ns，cost：{} ms",

                pjp.getSignature().toString(), Arrays.toString(pjp.getArgs()), (end - begin), (end - begin) / 1000000);

        return obj;
    }

}
