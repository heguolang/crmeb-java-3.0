package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新会员拼购等级（人工指定）
 *
 * 语义：pingouLevelId = 0 表示恢复自动判定（按直推人数命中 eb_pingou_level 阶梯）；
 *       > 0 表示人工强制指定，该会员在任何拼购活动里都固定按该等级的档位拿分享奖励，不看直推人数。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UpdateUserPingouLevelRequest", description = "更新会员拼购等级")
public class UpdateUserPingouLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户uid", required = true)
    @NotNull(message = "用户id不能为空")
    private Integer uid;

    @ApiModelProperty(value = "拼购等级ID，0=恢复自动判定", required = true)
    @NotNull(message = "拼购等级不能为空")
    private Integer pingouLevelId;
}
