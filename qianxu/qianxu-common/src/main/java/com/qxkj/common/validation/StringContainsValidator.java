package com.qxkj.common.validation;

import cn.hutool.core.util.StrUtil;
import com.qxkj.common.annotation.StringContains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
public class StringContainsValidator implements ConstraintValidator<StringContains, String> {

    private Set<String> limitValues;

    @Override
    public void initialize (StringContains constraintAnnotation) {
        limitValues = Arrays.stream(constraintAnnotation.limitValues()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isBlank(value)) {
            return true;
        }
        return limitValues.contains(value.trim());
    }
}
