package com.qxkj.common.model.pingou;

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
 * 拼购活动表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_pingou_activity")
@ApiModel(value = "PingouActivity对象", description = "拼购活动表")
public class PingouActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "活动名称")
    private String name;

    @ApiModelProperty(value = "活动图片")
    private String image;

    @ApiModelProperty(value = "拼购商品ID")
    private Integer productId;

    @ApiModelProperty(value = "拼购商品名称（冗余）")
    private String productName;

    @ApiModelProperty(value = "拼购商品售价（参与金额口径，冗余）")
    private BigDecimal productPrice;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    @ApiModelProperty(value = "成团人数")
    private Integer totalNum;

    @ApiModelProperty(value = "中奖人数")
    private Integer winNum;

    @ApiModelProperty(value = "未中奖补贴佣金（每人，元）")
    private BigDecimal refundSubsidy;

    @ApiModelProperty(value = "分享奖励低比例（‰）")
    private BigDecimal shareRateLow;

    @ApiModelProperty(value = "分享奖励高比例（‰）")
    private BigDecimal shareRateHigh;

    @ApiModelProperty(value = "高比例直推人数阈值")
    private Integer shareRateThreshold;

    @ApiModelProperty(value = "每人限参与次数（0=不限）")
    private Integer limitNum;

    @ApiModelProperty(value = "是否展示：1=是 0=否")
    private Boolean isShow;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "状态：1=启用 0=禁用")
    private Boolean status;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
