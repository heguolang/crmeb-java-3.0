package com.qxkj.common.utils;
 
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
 
import java.io.IOException;
import java.math.BigDecimal;
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
public class CustomerBigDecimalSerialize extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(Objects.nonNull(value)) {
            gen.writeNumber(value.stripTrailingZeros().doubleValue());
            //去除0后缀,如果想统一进行保留精度，也可以采用类似处理
        }else {//如果为null的话，就写null
            gen.writeNull();
        }
    }
}