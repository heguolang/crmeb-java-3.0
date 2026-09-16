package com.zbkj.common.model.agent;

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

/**
 * 区域代理表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_agent")
@ApiModel(value = "Agent对象", description = "区域代理表")
public class Agent implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer LEVEL_PROVINCE = 1;
    public static final Integer LEVEL_CITY = 2;
    public static final Integer LEVEL_DISTRICT = 3;

    public static final Integer STATUS_WAIT_AUDIT = 0;
    public static final Integer STATUS_PASS = 1;
    public static final Integer STATUS_FAIL = 2;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "代理级别：1=省级 2=市级 3=区级")
    private Integer level;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区/县")
    private String district;

    @ApiModelProperty(value = "区域全称")
    private String regionName;

    @ApiModelProperty(value = "订单地址匹配关键词")
    private String matchKey;

    @ApiModelProperty(value = "奖励比例（%）")
    private BigDecimal ratio;

    @ApiModelProperty(value = "状态：0=待审核 1=已通过 2=已拒绝")
    private Integer status;

    @ApiModelProperty(value = "申请说明/后台备注")
    private String applyMark;

    @ApiModelProperty(value = "审核时间")
    private Date checkTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "用户账号")
    @TableField(exist = false)
    private String account;
}
