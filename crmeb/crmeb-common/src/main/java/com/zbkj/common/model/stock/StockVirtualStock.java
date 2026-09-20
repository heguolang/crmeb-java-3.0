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
 * 订货系统-会员虚拟库存
 * 付款成功的虚拟库存单按商品维度入账（累加 remain_num）；
 * 提货/换货扣减 remain_num。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_virtual_stock")
@ApiModel(value = "StockVirtualStock对象", description = "订货系统-会员虚拟库存")
public class StockVirtualStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称（冗余）")
    private String productName;

    @ApiModelProperty(value = "商品图（冗余）")
    private String image;

    @ApiModelProperty(value = "规格标识（预留，空=商品级）")
    private String skuKey;

    @ApiModelProperty(value = "累计入账数量")
    private Integer num;

    @ApiModelProperty(value = "剩余可提货数量")
    private Integer remainNum;

    @ApiModelProperty(value = "最近一次入账来源订货单号")
    private String sourceOrderNo;

    @ApiModelProperty(value = "入账时订单上级代理快照（提货单沿用）")
    private Integer parentAgentId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "换货入库累计件数（已完成虚拟换入，派生字段不落库）")
    @TableField(exist = false)
    private Integer exchangeInNum;
}
