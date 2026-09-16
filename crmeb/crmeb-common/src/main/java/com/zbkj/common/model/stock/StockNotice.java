package com.zbkj.common.model.stock;

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
 * 订货系统-消息通知
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_notice")
@ApiModel(value = "StockNotice对象", description = "订货系统-消息通知")
public class StockNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer TYPE_ORDER_AUDIT = 1;   // 订单审核
    public static final Integer TYPE_ORDER_SEND = 2;    // 发货通知
    public static final Integer TYPE_REWARD = 3;        // 奖金到账
    public static final Integer TYPE_WITHDRAW = 4;      // 提现审核

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "接收用户UID")
    private Integer uid;

    @ApiModelProperty(value = "类型：1=订单审核 2=发货 3=奖金到账 4=提现审核")
    private Integer type;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "是否已读：0=未读 1=已读")
    private Integer isRead;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
