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
 * 订货系统-订货代理（树形层级）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_agent")
@ApiModel(value = "StockAgent对象", description = "订货系统-订货代理")
public class StockAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "层级ID")
    private Integer levelId;

    @ApiModelProperty(value = "上级代理ID（0=上级为总部）")
    private Integer parentId;

    @ApiModelProperty(value = "状态：0=禁用 1=启用")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "层级名称")
    @TableField(exist = false)
    private String levelName;

    @ApiModelProperty(value = "层级排序")
    @TableField(exist = false)
    private Integer levelSort;

    @ApiModelProperty(value = "用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "用户手机号")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "上级昵称")
    @TableField(exist = false)
    private String parentName;
}
