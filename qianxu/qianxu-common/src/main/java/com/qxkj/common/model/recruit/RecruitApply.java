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
 * 招聘系统-应聘登记
 *
 * 会员端投递（可游客）或后台代客登记，后台按状态跟进：
 * 0 待联系 → 1 已联系 → 2 面试中 → 3 已录用 / 4 不合适
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_recruit_apply")
@ApiModel(value = "RecruitApply对象", description = "招聘系统-应聘登记")
public class RecruitApply implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 待联系 */
    public static final int STATUS_WAIT = 0;
    /** 已联系 */
    public static final int STATUS_CONTACTED = 1;
    /** 面试中 */
    public static final int STATUS_INTERVIEW = 2;
    /** 已录用 */
    public static final int STATUS_HIRED = 3;
    /** 不合适 */
    public static final int STATUS_REJECT = 4;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "报名编号")
    private String applyNo;

    @ApiModelProperty(value = "应聘职位ID")
    private Integer jobId;

    @ApiModelProperty(value = "应聘职位名称（冗余）")
    private String jobTitle;

    @ApiModelProperty(value = "会员UID（0=游客投递）")
    private Integer uid;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "年龄")
    private String age;

    @ApiModelProperty(value = "工作年限")
    private String experience;

    @ApiModelProperty(value = "最高学历")
    private String education;

    @ApiModelProperty(value = "毕业院校")
    private String school;

    @ApiModelProperty(value = "意向城市")
    private String expectCity;

    @ApiModelProperty(value = "期望薪资")
    private String expectSalary;

    @ApiModelProperty(value = "简历附件URL")
    private String resume;

    @ApiModelProperty(value = "简历文件名")
    private String resumeName;

    @ApiModelProperty(value = "个人留言 / 自我评价")
    private String remark;

    @ApiModelProperty(value = "来源：h5=会员端投递 admin=后台登记")
    private String source;

    @ApiModelProperty(value = "处理状态：0=待联系 1=已联系 2=面试中 3=已录用 4=不合适")
    private Integer status;

    @ApiModelProperty(value = "面试时间 yyyy-MM-dd HH:mm")
    private String interviewDate;

    @ApiModelProperty(value = "后台备注 / 面试评价")
    private String adminRemark;

    @ApiModelProperty(value = "处理人")
    private String handler;

    @ApiModelProperty(value = "跟进日志 JSON 数组")
    private String log;

    @ApiModelProperty(value = "投递时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;
}
