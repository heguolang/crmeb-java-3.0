package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 商品批量操作（下架 / 删除 / 修改分类）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StoreProductBatchRequest", description = "商品批量操作")
public class StoreProductBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id列表", required = true)
    @NotEmpty(message = "请选择商品")
    private List<Integer> ids;

    @ApiModelProperty(value = "删除类型：recycle——移入回收站 delete——彻底删除，默认recycle")
    private String type;

    @ApiModelProperty(value = "商品分类id，多个用逗号分隔")
    private String cateId;
}
