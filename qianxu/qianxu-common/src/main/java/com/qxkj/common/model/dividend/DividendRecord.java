package com.qxkj.common.model.dividend;

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
 * 全球分红结算记录（每期一条汇总）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_dividend_record")
@ApiModel(value = "DividendRecord对象", description = "全球分红结算记录")
public class DividendRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "期号：日D20260928/周W2026-W39/月M2026-09")
    private String periodNo;

    @ApiModelProperty(value = "周期类型：1=日，2=周，3=月")
    private Integer cycleType;

    @ApiModelProperty(value = "分红基数：1=订单总业绩，2=订单利润")
    private Integer baseType;

    @ApiModelProperty(value = "本期分红基数总额")
    private BigDecimal baseAmount;

    @ApiModelProperty(value = "本期参与统计订单数")
    private Integer orderCount;

    @ApiModelProperty(value = "本期分红总发放金额")
    private BigDecimal totalPaid;

    @ApiModelProperty(value = "本期参与分红会员人次")
    private Integer memberCount;

    @ApiModelProperty(value = "备注（每档均分明细说明）")
    private String mark;

    @ApiModelProperty(value = "结算时间")
    private Date createTime;
}
