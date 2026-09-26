package com.qxkj.common.annotation;


import com.qxkj.common.enums.MethodType;

import java.lang.annotation.*;

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
@Target({ElementType.PARAMETER, ElementType.METHOD})//作用在参数和方法上
@Retention(RetentionPolicy.RUNTIME)//运行时注解
@Documented//表明这个注解应该被 javadoc工具记录
public @interface LogControllerAnnotation {

    // 日志是否存入数据库
    boolean intoDB() default false;

    // 操作类型, 默认是查询
    MethodType methodType() default MethodType.SELECT;

    // 接口描述
    String description() default "";

}
