package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("后台调整链动关系")
public class ChainRelationUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer userId;
    @NotNull
    @ApiModelProperty("新安置上级，0表示清空")
    private Integer parentId;
    private String reason;
}
