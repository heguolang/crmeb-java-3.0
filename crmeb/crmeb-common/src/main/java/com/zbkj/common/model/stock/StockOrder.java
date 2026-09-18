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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订货系统-订货订单
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_order")
@ApiModel(value = "StockOrder对象", description = "订货系统-订货订单")
public class StockOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态常量 */
    public static final Integer STATUS_WAIT_PARENT_AUDIT = 0;   // 待上级审核（付款后进入）
    public static final Integer STATUS_WAIT_PAY = 1;            // 待付款（下单后默认进入）
    public static final Integer STATUS_WAIT_SEND = 2;           // 待发货
    public static final Integer STATUS_WAIT_RECEIVE = 3;        // 待收货
    public static final Integer STATUS_COMPLETE = 4;            // 已完成
    public static final Integer STATUS_REJECT = -1;             // 上级驳回
    public static final Integer STATUS_WAIT_MATCH = 10;         // 等待匹配上级（付款后上级无库存）
    public static final Integer STATUS_CANCEL = -2;             // 已取消（待付款超时/主动取消）

    public static final Integer PAY_TYPE_WECHAT = 1;            // 微信线上支付
    public static final Integer PAY_TYPE_RECORD = 2;            // 后台记账欠款
    public static final Integer PAY_TYPE_YUE = 3;               // 余额支付

    public static final Integer STOCK_TYPE_PHYSICAL = 1;        // 实体库存
    public static final Integer STOCK_TYPE_VIRTUAL = 2;         // 虚拟库存

    public static final Integer ORDER_TYPE_PURCHASE = 1;        // 采购
    public static final Integer ORDER_TYPE_PICKUP = 2;          // 虚拟提货
    public static final Integer ORDER_TYPE_EXCHANGE = 3;        // 换货

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订货单号")
    private String orderNo;

    @ApiModelProperty(value = "下单代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "下单代理ID")
    private Integer agentId;

    @ApiModelProperty(value = "直接上级代理ID（0=上级为总部）")
    private Integer parentAgentId;

    @ApiModelProperty(value = "下单时层级名称（冗余）")
    private String levelName;

    @ApiModelProperty(value = "商品总数量")
    private Integer totalNum;

    @ApiModelProperty(value = "订单总额（按下单者拿货价）")
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "付款方式：1=微信线上支付 2=后台记账欠款 3=余额支付")
    private Integer payType;

    @ApiModelProperty(value = "付款状态：0=未付款 1=已付款")
    private Integer payStatus;

    @ApiModelProperty(value = "状态：0=待上级审核 1=待付款 2=待发货 3=待收货 4=已完成 -1=上级驳回 10=等待匹配上级 -2=已取消")
    private Integer status;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "上级审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "付款时间")
    private Date payTime;

    @ApiModelProperty(value = "总部发货时间")
    private Date sendTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "快递公司")
    private String expressName;

    @ApiModelProperty(value = "快递单号")
    private String expressNum;

    @ApiModelProperty(value = "订单备注")
    private String mark;

    @ApiModelProperty(value = "下单时选用的用户地址ID")
    private Integer addressId;

    @ApiModelProperty(value = "收货人姓名（下单地址快照）")
    private String realName;

    @ApiModelProperty(value = "收货人电话（下单地址快照）")
    private String phone;

    @ApiModelProperty(value = "收货地址（下单地址快照）")
    private String userAddress;

    @ApiModelProperty(value = "库存类型：1=实体 2=虚拟")
    private Integer stockType;

    @ApiModelProperty(value = "订单类型：1=采购 2=虚拟提货 3=换货")
    private Integer orderType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "向上查找上级库存的时间（付款后上级无库存时生成）")
    private Date upSearchTime;

    @ApiModelProperty(value = "已向上查找次数")
    private Integer upSearchNum;

    @ApiModelProperty(value = "取消时间（待付款超时/主动取消）")
    private Date cancelTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "下单用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "订单明细")
    @TableField(exist = false)
    private List<StockOrderProduct> productList;
}
