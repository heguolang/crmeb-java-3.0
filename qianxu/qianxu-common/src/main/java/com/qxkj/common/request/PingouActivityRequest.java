package com.qxkj.common.request;

import com.qxkj.common.model.pingou.PingouActivity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼购活动 新增/修改 请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouActivityRequest对象", description = "拼购活动新增修改请求")
public class PingouActivityRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID（修改时必传）")
    private Integer id;

    @ApiModelProperty(value = "活动名称")
    @NotBlank(message = "活动名称不能为空")
    private String name;

    @ApiModelProperty(value = "活动图片")
    private String image;

    @ApiModelProperty(value = "拼购商品ID")
    @NotNull(message = "拼购商品不能为空")
    private Integer productId;

    @ApiModelProperty(value = "活动开始时间")
    @NotNull(message = "活动开始时间不能为空")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    @NotNull(message = "活动结束时间不能为空")
    private Date endTime;

    @ApiModelProperty(value = "成团人数")
    @NotNull(message = "成团人数不能为空")
    private Integer totalNum;

    @ApiModelProperty(value = "中奖人数")
    @NotNull(message = "中奖人数不能为空")
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

    public PingouActivity toEntity() {
        PingouActivity activity = new PingouActivity();
        BeanUtils.copyProperties(this, activity);
        return activity;
    }
}
