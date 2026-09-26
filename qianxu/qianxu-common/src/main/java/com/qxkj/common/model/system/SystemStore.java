package com.qxkj.common.model.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_system_store")
@ApiModel(value="SystemStore对象", description="门店自提")
public class SystemStore implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "门店名称")
    private String name;

    @ApiModelProperty(value = "简介")
    private String introduction;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "省市区")
    private String address;

    @ApiModelProperty(value = "详细地址")
    private String detailedAddress;

    @ApiModelProperty(value = "门店logo")
    private String image;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "核销有效日期")
    private String validTime;

    @ApiModelProperty(value = "每日营业开关时间")
    private String dayTime;

    @ApiModelProperty(value = "是否显示")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否支持到店自提：1=是 0=否")
    private Boolean selfPickup;

    @ApiModelProperty(value = "是否支持上门配送：1=是 0=否")
    private Boolean delivery;

    @ApiModelProperty(value = "配送服务半径(公里)")
    private BigDecimal deliveryRadius;

    @ApiModelProperty(value = "门店核销服务费")
    private BigDecimal verifyFee;

    @ApiModelProperty(value = "到店自提服务费")
    private BigDecimal pickupFee;

    @ApiModelProperty(value = "上门配送服务费")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "门店负责人用户UID")
    private Integer leaderUid;

    @ApiModelProperty(value = "门店负责人昵称(冗余)")
    private String leaderName;

}
