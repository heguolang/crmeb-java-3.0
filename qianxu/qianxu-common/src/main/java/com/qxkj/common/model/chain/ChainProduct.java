package com.qxkj.common.model.chain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_chain_product")
@ApiModel(value = "ChainProduct", description = "链动礼包产品")
public class ChainProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty("商品ID")
    private Integer productId;
    @ApiModelProperty("直推奖金额")
    private BigDecimal directAmount;
    @ApiModelProperty("见单奖金额")
    private BigDecimal seeAmount;
    @ApiModelProperty("平级奖占见单比例%")
    private BigDecimal peerRatio;
    @ApiModelProperty("1启用 0停用")
    private Integer status;
    private Integer sort;
    private String remark;
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty("商品名称")
    private String storeName;
    @TableField(exist = false)
    @ApiModelProperty("商品图")
    private String image;
    @TableField(exist = false)
    @ApiModelProperty("售价")
    private BigDecimal price;
}
