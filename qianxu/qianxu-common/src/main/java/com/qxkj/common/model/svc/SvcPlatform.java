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
 * 企业服务-签约渠道平台
 *
 * 决定登记合同时「电子合同」下能选到哪些平台。停用不影响历史合同里的记录。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_platform")
@ApiModel(value = "SvcPlatform对象", description = "企业服务-签约渠道平台")
public class SvcPlatform implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "平台名称")
    private String name;

    @ApiModelProperty(value = "类型：电子签平台/线下渠道")
    private String kind;

    @ApiModelProperty(value = "资质与定位说明")
    private String description;

    @ApiModelProperty(value = "状态：1=启用 0=停用")
    private Integer status;

    @ApiModelProperty(value = "排序（降序）")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;
}
