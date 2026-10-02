package com.qxkj.common.model.svc;

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
import java.util.List;

/**
 * 企业服务-项目
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_project")
@ApiModel(value = "SvcProject对象", description = "企业服务-项目")
public class SvcProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态常量 */
    public static final Integer STATUS_DEMAND = 1;      // 需求确认
    public static final Integer STATUS_DEVELOPING = 2;  // 开发中
    public static final Integer STATUS_WAIT_ACCEPT = 3; // 待验收
    public static final Integer STATUS_COMPLETE = 4;    // 已完成
    public static final Integer STATUS_CLOSED = -1;     // 已终止

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "项目名称")
    private String name;

    @ApiModelProperty(value = "项目类型：小程序/APP开发/系统定制等")
    private String type;

    @ApiModelProperty(value = "状态：1=需求确认 2=开发中 3=待验收 4=已完成 -1=已终止")
    private Integer status;

    @ApiModelProperty(value = "进度百分比 0-100")
    private Integer percent;

    @ApiModelProperty(value = "当前阶段：如 UI 设计/联调中")
    private String stage;

    @ApiModelProperty(value = "项目经理")
    private String manager;

    @ApiModelProperty(value = "交付周期描述：如 45 天交付")
    private String cycle;

    @ApiModelProperty(value = "排序（降序）")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "会员昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "会员手机号")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "进度记录（详情用）")
    @TableField(exist = false)
    private List<SvcProjectProgress> progressList;

    @ApiModelProperty(value = "状态文案")
    @TableField(exist = false)
    private String statusText;
}
