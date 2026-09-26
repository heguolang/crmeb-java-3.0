package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门店-附近门店 Response
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MerchantStoreNearVo对象", description = "附近门店信息")
public class MerchantStoreNearVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "门店ID")
    private Integer id;

    @ApiModelProperty(value = "门店名称")
    private String name;

    @ApiModelProperty(value = "门店地址")
    private String address;

    @ApiModelProperty(value = "详细地址")
    private String detailedAddress;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "营业时间")
    private String dayTime;

    @ApiModelProperty(value = "门店logo")
    private String image;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "是否支持到店自提")
    private Boolean selfPickup;

    @ApiModelProperty(value = "是否支持上门配送")
    private Boolean delivery;

    @ApiModelProperty(value = "配送服务半径(公里)")
    private BigDecimal deliveryRadius;

    @ApiModelProperty(value = "门店核销服务费")
    private BigDecimal verifyFee;

    @ApiModelProperty(value = "到店自提服务费")
    private BigDecimal pickupFee;

    @ApiModelProperty(value = "上门配送服务费")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "距离(公里)")
    private BigDecimal distanceKm;

    @ApiModelProperty(value = "当前用户可选自提")
    private Boolean canPickup;

    @ApiModelProperty(value = "当前用户可选配送")
    private Boolean canDelivery;
}
