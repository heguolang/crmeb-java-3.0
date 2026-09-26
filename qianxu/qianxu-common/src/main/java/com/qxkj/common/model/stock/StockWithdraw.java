package com.qxkj.common.model.stock;

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

/**
 * 订货系统-奖金提现
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_withdraw")
@ApiModel(value = "StockWithdraw对象", description = "订货系统-奖金提现")
public class StockWithdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STATUS_WAIT_AUDIT = 0;  // 待审核
    public static final Integer STATUS_PAID = 1;        // 已打款
    public static final Integer STATUS_REJECT = -1;     // 驳回

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "提现单号")
    private String withdrawNo;

    @ApiModelProperty(value = "提现金额")
    private BigDecimal price;

    @ApiModelProperty(value = "状态：0=待审核 1=已打款 -1=驳回")
    private Integer status;

    @ApiModelProperty(value = "申请备注")
    private String mark;

    @ApiModelProperty(value = "审核备注")
    private String auditMark;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "用户手机号")
    @TableField(exist = false)
    private String phone;
}
