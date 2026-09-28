package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新会员分红等级（人工指定）
 *
 * 语义：dividendLevelId = 0 表示恢复自动判定（按分红等级自身升级条件，可同时达标多个）；
 *       > 0 表示人工强制指定，该会员只在指定等级参与分红，不再参与其它等级。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UpdateUserDividendLevelRequest", description = "更新会员分红等级")
public class UpdateUserDividendLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户uid", required = true)
    @NotNull(message = "用户id不能为空")
    private Integer uid;

    @ApiModelProperty(value = "分红等级ID，0=恢复自动判定", required = true)
    @NotNull(message = "分红等级不能为空")
    private Integer dividendLevelId;
}
