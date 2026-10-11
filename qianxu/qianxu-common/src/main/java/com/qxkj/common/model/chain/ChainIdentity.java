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
@TableName("eb_chain_identity")
@ApiModel(value = "ChainIdentity", description = "链动身份记录")
public class ChainIdentity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty("用户UID")
    private Integer userId;
    @ApiModelProperty("1代理 2老板")
    private Integer identityType;
    private Date upgradeTime;
    @ApiModelProperty("1有效 0失效")
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
