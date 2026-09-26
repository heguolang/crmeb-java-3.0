package com.qxkj.common.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
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

/**
 * 门店-核销记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_store_verify_record")
@ApiModel(value = "MerchantStoreVerifyRecord对象", description = "门店-核销记录")
public class MerchantStoreVerifyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 核销方式：1=核销码核销 */
    public static final int TYPE_CODE = 1;
    /** 核销来源：1=门店负责人端 2=平台后台 */
    public static final int SOURCE_LEADER = 1;
    public static final int SOURCE_ADMIN = 2;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "门店ID(eb_system_store.id)")
    private Integer storeId;

    @ApiModelProperty(value = "门店名称(冗余)")
    private String storeName;

    @ApiModelProperty(value = "订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "核销码")
    private String verifyCode;

    @ApiModelProperty(value = "核销商品概要")
    private String productInfo;

    @ApiModelProperty(value = "核销方式：1=核销码核销")
    private Integer verifyType;

    @ApiModelProperty(value = "本次核销服务费")
    private BigDecimal serviceFee;

    @ApiModelProperty(value = "订单支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "核销后订单状态")
    private Integer orderStatus;

    @ApiModelProperty(value = "核销操作人UID(用户端)")
    private Integer verifyUid;

    @ApiModelProperty(value = "核销操作人昵称")
    private String verifyName;

    @ApiModelProperty(value = "核销来源：1=门店负责人端 2=平台后台")
    private Integer verifySource;

    @ApiModelProperty(value = "核销时间")
    private Date createTime;
}
