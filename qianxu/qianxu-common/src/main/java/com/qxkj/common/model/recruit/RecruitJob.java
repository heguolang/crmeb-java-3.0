package com.qxkj.common.model.recruit;

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
 * 招聘系统-招聘职位
 *
 * 后台发布 → 会员端展示 → 用户投递（eb_recruit_apply）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_recruit_job")
@ApiModel(value = "RecruitJob对象", description = "招聘系统-招聘职位")
public class RecruitJob implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态：招聘中 */
    public static final int STATUS_ON = 1;
    /** 状态：已关闭 */
    public static final int STATUS_OFF = 0;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "职位名称")
    private String title;

    @ApiModelProperty(value = "职位类别（技术/产品/运营/销售/职能）")
    private String category;

    @ApiModelProperty(value = "所属部门")
    private String dept;

    @ApiModelProperty(value = "工作城市")
    private String city;

    @ApiModelProperty(value = "详细工作地点")
    private String address;

    @ApiModelProperty(value = "工作性质：全职/兼职/实习/远程")
    private String jobType;

    @ApiModelProperty(value = "薪资范围文本，如 8K-12K")
    private String salary;

    @ApiModelProperty(value = "月薪下限（元）")
    private Integer salaryMin;

    @ApiModelProperty(value = "月薪上限（元）")
    private Integer salaryMax;

    @ApiModelProperty(value = "招聘人数")
    private Integer recruitNum;

    @ApiModelProperty(value = "经验要求")
    private String experience;

    @ApiModelProperty(value = "学历要求")
    private String education;

    @ApiModelProperty(value = "福利标签，逗号分隔")
    private String tags;

    @ApiModelProperty(value = "职位描述")
    private String description;

    @ApiModelProperty(value = "任职要求")
    private String requirement;

    @ApiModelProperty(value = "招聘联系人")
    private String hrName;

    @ApiModelProperty(value = "招聘联系电话")
    private String hrPhone;

    @ApiModelProperty(value = "状态：1=招聘中 0=已关闭")
    private Integer status;

    @ApiModelProperty(value = "热门推荐：1=是 0=否")
    private Integer isHot;

    @ApiModelProperty(value = "排序（降序）")
    private Integer sort;

    @ApiModelProperty(value = "浏览量")
    private Integer viewCount;

    @ApiModelProperty(value = "投递数（冗余）")
    private Integer applyCount;

    @ApiModelProperty(value = "发布日期 yyyy-MM-dd")
    private String onlineDate;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;
}
