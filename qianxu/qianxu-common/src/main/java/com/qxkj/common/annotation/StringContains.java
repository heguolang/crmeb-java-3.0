package com.qxkj.common.annotation;

import com.qxkj.common.validation.StringContainsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
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
@Documented
@Constraint(validatedBy = StringContainsValidator.class)
@Target({ ElementType.PARAMETER,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StringContains {

    String message() default "字符串不符合规则";

    String[] limitValues() default {};

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
