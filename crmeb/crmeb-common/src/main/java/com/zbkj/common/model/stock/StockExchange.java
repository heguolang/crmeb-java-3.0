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
    public static final Integer STATUS_WAIT_RECEIVE = 5;        // 待下级收货（上级已发新品，等换货人确认）
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

    @ApiModelProperty(value = "原商品规格key")
    private String skuKey;

    @ApiModelProperty(value = "换货类型：1=虚拟换货 2=实体换货")
    private Integer exchangeType;

    @ApiModelProperty(value = "换入商品ID")
    private Integer targetProductId;

    @ApiModelProperty(value = "换入商品规格key")
    private String targetSkuKey;

    @ApiModelProperty(value = "换入商品名称")
    private String targetProductName;

    @ApiModelProperty(value = "换入商品拿货价")
    private java.math.BigDecimal targetPrice;

    @ApiModelProperty(value = "原商品拿货价")
    private java.math.BigDecimal originPrice;

    @ApiModelProperty(value = "需补差价 =（换入价 - 原价）× 数量")
    private java.math.BigDecimal diffPrice;

    @ApiModelProperty(value = "差价支付状态：0=未付 1=已付")
    private Integer diffPayStatus;

    @ApiModelProperty(value = "差价支付方式 yue/weixin")
    private String diffPayType;

    @ApiModelProperty(value = "差价支付时间")
    private Date diffPayTime;

    @ApiModelProperty(value = "新品收货人")
    private String realName;

    @ApiModelProperty(value = "新品收货电话")
    private String phone;

    @ApiModelProperty(value = "新品收货地址")
    private String userAddress;

    @ApiModelProperty(value = "收货地址ID")
    private Integer addressId;

    @ApiModelProperty(value = "商品名称（冗余）")
    private String productName;

    @ApiModelProperty(value = "换入库存类型：1=实体 2=虚拟（虚拟换货时可选择；NULL/1=实体）")
    private Integer targetStockType;

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

    @ApiModelProperty(value = "申请用户头像")
    @TableField(exist = false)
    private String avatar;

    @ApiModelProperty(value = "关联订货单号")
    @TableField(exist = false)
    private String orderNo;

    @ApiModelProperty(value = "商品缩略图")
    @TableField(exist = false)
    private String productImage;

    @ApiModelProperty(value = "换入商品缩略图")
    @TableField(exist = false)
    private String targetProductImage;

    @ApiModelProperty(value = "换入商品规格名（attrValue 可读文本）")
    @TableField(exist = false)
    private String targetSkuName;

    @ApiModelProperty(value = "库存类型：1=实体 2=虚拟（取自关联订货单）")
    @TableField(exist = false)
    private Integer stockType;

    @ApiModelProperty(value = "旧品寄回对象说明（如：总部 / 上级张三）")
    @TableField(exist = false)
    private String backTarget;

    @ApiModelProperty(value = "旧品寄回地址（上级为总部取后台配置，否则取上级会员默认收货地址）")
    @TableField(exist = false)
    private String backAddress;
}
