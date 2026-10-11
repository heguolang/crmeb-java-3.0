package com.qxkj.common.model.chain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_chain_relation")
@ApiModel(value = "ChainRelation", description = "链动上下级关系")
public class ChainRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    @ApiModelProperty("安置上级")
    private Integer parentId;
    @ApiModelProperty("邀请人")
    private Integer inviteUid;
    private Integer levelDepth;
    private Integer isHelp;
    private String pathSnapshot;
    private Date createTime;
    private Date updateTime;
}
