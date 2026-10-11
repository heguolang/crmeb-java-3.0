package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("链动团队节点")
public class ChainTeamNodeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer uid;
    private String nickname;
    private String avatar;
    @ApiModelProperty("0无 1代理 2老板")
    private Integer identity;
    private String identityName;
    private Boolean isHelp;
    private List<ChainTeamNodeResponse> children = new ArrayList<>();
}
