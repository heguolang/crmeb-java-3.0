package com.qxkj.common.model.svc;

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
 * 企业服务-项目进度记录（后台设置，会员端只读）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_project_progress")
@ApiModel(value = "SvcProjectProgress对象", description = "企业服务-项目进度记录")
public class SvcProjectProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "项目ID（eb_svc_project.id）")
    private Integer projectId;

    @ApiModelProperty(value = "会员UID（冗余）")
    private Integer uid;

    @ApiModelProperty(value = "阶段名称")
    private String stage;

    @ApiModelProperty(value = "进度百分比")
    private Integer percent;

    @ApiModelProperty(value = "说明")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
