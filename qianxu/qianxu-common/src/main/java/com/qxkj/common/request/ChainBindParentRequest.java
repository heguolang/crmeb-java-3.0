package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("链动绑定上级")
public class ChainBindParentRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "邀请人不能为空")
    @ApiModelProperty(value = "邀请人UID", required = true)
    private Integer inviteUid;
}
