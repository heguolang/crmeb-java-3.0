package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 商品批量移出分组
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StoreProductGroupBatchUnbindRequest", description = "商品批量移出分组")
public class StoreProductGroupBatchUnbindRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id列表", required = true)
    @NotEmpty(message = "请选择商品")
    private List<Integer> productIds;

    @ApiModelProperty(value = "分组id列表，为空表示移除所选商品的全部分组")
    private List<Integer> groupIds;
}
