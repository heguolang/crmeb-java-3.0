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
 * 订货系统-订货商变更记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_change_log")
@ApiModel(value = "StockChangeLog对象", description = "订货系统-订货商变更记录")
public class StockChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 变更类型：1=新增 2=层级变更 3=上级变更 4=状态变更 5=删除 */
    public static final int TYPE_ADD = 1;
    public static final int TYPE_LEVEL = 2;
    public static final int TYPE_PARENT = 3;
    public static final int TYPE_STATUS = 4;
    public static final int TYPE_DELETE = 5;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订货商ID（eb_stock_agent.id）")
    private Integer agentId;

    @ApiModelProperty(value = "用户UID")
    private Integer uid;

    @ApiModelProperty(value = "变更类型：1=新增 2=层级变更 3=上级变更 4=状态变更 5=删除")
    private Integer type;

    @ApiModelProperty(value = "变更前")
    private String oldValue;

    @ApiModelProperty(value = "变更后")
    private String newValue;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "订货商昵称（联查，非表字段）")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "手机号（联查，非表字段）")
    @TableField(exist = false)
    private String phone;
}
