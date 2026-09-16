package com.zbkj.common.model.stock;

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
import java.util.Date;

/**
 * 订货系统-换货单
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_exchange")
@ApiModel(value = "StockExchange对象", description = "订货系统-换货单")
public class StockExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STATUS_WAIT_PARENT_AUDIT = 0;   // 待上级审核
    public static final Integer STATUS_WAIT_HQ_AUDIT = 1;       // 待总部审核
    public static final Integer STATUS_WAIT_BACK = 2;           // 待旧品退回
    public static final Integer STATUS_WAIT_SEND = 3;           // 待发新品
    public static final Integer STATUS_COMPLETE = 4;            // 已完成
    public static final Integer STATUS_REJECT = -1;             // 驳回

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "换货单号")
    private String exchangeNo;

    @ApiModelProperty(value = "申请代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "申请代理ID")
    private Integer agentId;

    @ApiModelProperty(value = "直接上级代理ID")
    private Integer parentAgentId;

    @ApiModelProperty(value = "关联订货订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称（冗余）")
    private String productName;

    @ApiModelProperty(value = "换货数量")
    private Integer num;

    @ApiModelProperty(value = "换货原因")
    private String reason;

    @ApiModelProperty(value = "状态：0=待上级审核 1=待总部审核 2=待旧品退回 3=待发新品 4=已完成 -1=驳回")
    private Integer status;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "旧品退回快递公司")
    private String backExpressName;

    @ApiModelProperty(value = "旧品退回快递单号")
    private String backExpressNum;

    @ApiModelProperty(value = "新品发出快递公司")
    private String newExpressName;

    @ApiModelProperty(value = "新品发出快递单号")
    private String newExpressNum;

    @ApiModelProperty(value = "上级审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "总部审核时间")
    private Date hqAuditTime;

    @ApiModelProperty(value = "旧品核验入库时间")
    private Date backTime;

    @ApiModelProperty(value = "新品发出时间")
    private Date sendTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "申请用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "关联订货单号")
    @TableField(exist = false)
    private String orderNo;
}
