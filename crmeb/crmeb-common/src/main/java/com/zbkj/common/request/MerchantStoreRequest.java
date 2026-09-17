package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门店创建/编辑 Request
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MerchantStoreRequest对象", description = "门店创建/编辑")
public class MerchantStoreRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "门店ID（编辑时必传）")
    private Integer id;

    @ApiModelProperty(value = "门店名称", required = true)
    @NotBlank(message = "门店名称不能为空")
    private String name;

    @ApiModelProperty(value = "门店地址", required = true)
    @NotBlank(message = "门店地址不能为空")
    private String address;

    @ApiModelProperty(value = "详细地址")
    private String detailedAddress;

    @ApiModelProperty(value = "联系电话", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @ApiModelProperty(value = "营业时间（如 08:30-21:30）")
    private String dayTime;

    @ApiModelProperty(value = "纬度", required = true)
    @NotBlank(message = "纬度不能为空")
    private String latitude;

    @ApiModelProperty(value = "经度", required = true)
    @NotBlank(message = "经度不能为空")
    private String longitude;

    @ApiModelProperty(value = "门店logo")
    private String image;

    @ApiModelProperty(value = "门店状态：true=启用 false=禁用")
    @NotNull(message = "门店状态不能为空")
    private Boolean isShow;

    @ApiModelProperty(value = "是否支持到店自提")
    @NotNull(message = "请选择是否支持到店自提")
    private Boolean selfPickup;

    @ApiModelProperty(value = "是否支持上门配送")
    @NotNull(message = "请选择是否支持上门配送")
    private Boolean delivery;

    @ApiModelProperty(value = "配送服务半径(公里)", required = true)
    @NotNull(message = "配送半径不能为空")
    @DecimalMin(value = "0.1", message = "配送半径必须大于0")
    private BigDecimal deliveryRadius;

    @ApiModelProperty(value = "门店核销服务费")
    private BigDecimal verifyFee;

    @ApiModelProperty(value = "到店自提服务费")
    private BigDecimal pickupFee;

    @ApiModelProperty(value = "上门配送服务费")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "门店负责人用户UID（0或null=未绑定）")
    private Integer leaderUid;

    @ApiModelProperty(value = "门店简介")
    private String introduction;
}
