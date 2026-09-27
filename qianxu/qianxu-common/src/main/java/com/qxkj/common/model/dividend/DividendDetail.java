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
 * 全球分红发放明细
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_dividend_detail")
@ApiModel(value = "DividendDetail对象", description = "全球分红发放明细")
public class DividendDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "结算记录ID")
    private Integer recordId;

    @ApiModelProperty(value = "期号")
    private String periodNo;

    @ApiModelProperty(value = "会员uid")
    private Integer uid;

    @ApiModelProperty(value = "分红级别ID（分销商等级ID）")
    private Integer levelId;

    @ApiModelProperty(value = "分红级别名称")
    private String levelName;

    @ApiModelProperty(value = "分红比例（%）")
    private BigDecimal rate;

    @ApiModelProperty(value = "分红金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "发放时间")
    private Date createTime;
}
