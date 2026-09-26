package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 佣金账户操作（后台「修改佣金」）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "佣金账户操作", description = "后台调整会员佣金账户")
public class UserOperateBrokerageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "uid")
    @NotNull
    @Min(value = 1, message = "请输入正确的uid")
    private Integer uid;

    @ApiModelProperty(value = "佣金类型， 1 = 增加， 2 = 减少")
    @NotNull
    @Range(min = 1, max = 2, message = "请选择正确的类型， 【1 = 增加， 2 = 减少】")
    private Integer brokerageType;

    @ApiModelProperty(value = "佣金金额")
    @NotNull
    @DecimalMin(value = "0.00", message = "佣金金额不能小于0")
    @DecimalMax(value = "999999.99", message = "佣金金额不能大于999999.99")
    private BigDecimal brokerageValue;
}
