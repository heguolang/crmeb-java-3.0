package com.qxkj.common.model.product;

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

/**
 * 商品分组关联
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_store_product_group_rel")
@ApiModel(value = "StoreProductGroupRel", description = "商品分组关联")
public class StoreProductGroupRel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "分组id")
    private Integer groupId;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
