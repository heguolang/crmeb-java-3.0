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
 * 订货系统-云仓库存变动日志
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_log")
@ApiModel(value = "StockLog对象", description = "订货系统-云仓库存变动日志")
public class StockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer TYPE_AUDIT_DEDUCT = 1;   // 审核通过扣库存
    public static final Integer TYPE_REJECT_BACK = 2;    // 驳回回补
    public static final Integer TYPE_EXCHANGE_DEDUCT = 3; // 换货新品扣
    public static final Integer TYPE_EXCHANGE_BACK = 4;  // 换货旧品回补
    public static final Integer TYPE_MANUAL = 5;         // 手动调整

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "类型：1=审核通过扣 2=驳回回补 3=换货新品扣 4=换货旧品回补 5=手动调整")
    private Integer type;

    @ApiModelProperty(value = "变动数量（±）")
    private Integer changeNum;

    @ApiModelProperty(value = "变动前库存")
    private Integer beforeStock;

    @ApiModelProperty(value = "变动后库存")
    private Integer afterStock;

    @ApiModelProperty(value = "关联单号")
    private String linkNo;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "商品名称")
    @TableField(exist = false)
    private String productName;

    @ApiModelProperty(value = "关联会员昵称（由关联单号反查；无关联订单为空）")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "关联会员手机号")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "关联会员UID")
    @TableField(exist = false)
    private Integer uid;
}
