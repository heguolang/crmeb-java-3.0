package com.qxkj.common.model.agent;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 代理商变更记录（2026-09-18 需求：与订货商变更记录一致）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_agent_change_log")
@ApiModel(value = "AgentChangeLog对象", description = "代理商变更记录")
public class AgentChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer TYPE_ADD = 1;      // 新增代理
    public static final Integer TYPE_LEVEL = 2;    // 代理级别/区域变更
    public static final Integer TYPE_STATUS = 3;   // 状态变更（审核/启禁）
    public static final Integer TYPE_DELETE = 4;   // 删除代理
    public static final Integer TYPE_RATIO = 5;    // 分成比例变更

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "代理ID")
    private Integer agentId;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "类型：1新增 2级别/区域变更 3状态变更 4删除 5比例变更")
    private Integer type;

    @ApiModelProperty(value = "变更前")
    private String oldValue;

    @ApiModelProperty(value = "变更后")
    private String newValue;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    // ===== 非表字段（列表展示） =====
    @ApiModelProperty(value = "会员昵称")
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "手机号")
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "会员头像")
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String avatar;
}
