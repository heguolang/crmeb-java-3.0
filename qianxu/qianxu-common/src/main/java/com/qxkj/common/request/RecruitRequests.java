package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 招聘系统 请求参数集合
 */
public class RecruitRequests {

    private RecruitRequests() {
    }

    @Data
    @ApiModel(value = "JobSaveRequest对象", description = "保存招聘职位")
    public static class JobSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "职位名称")
        @NotBlank(message = "请填写职位名称")
        private String title;

        @ApiModelProperty(value = "职位类别")
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

        @ApiModelProperty(value = "发布日期 yyyy-MM-dd")
        private String onlineDate;
    }

    @Data
    @ApiModel(value = "ApplySubmitRequest对象", description = "会员端投递应聘")
    public static class ApplySubmitRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "应聘职位ID（为空表示「人才自荐」）")
        private Integer jobId;

        @ApiModelProperty(value = "姓名")
        @NotBlank(message = "请填写姓名")
        private String name;

        @ApiModelProperty(value = "联系电话")
        @NotBlank(message = "请填写联系电话")
        private String phone;

        @ApiModelProperty(value = "短信验证码（后台开启短信验证时必填）")
        private String code;

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
    }

    @Data
    @ApiModel(value = "ApplyHandleRequest对象", description = "后台处理应聘（改状态 + 备注 + 面试时间）")
    public static class ApplyHandleRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "应聘记录ID")
        private Integer id;

        @ApiModelProperty(value = "状态：0=待联系 1=已联系 2=面试中 3=已录用 4=不合适")
        private Integer status;

        @ApiModelProperty(value = "面试时间 yyyy-MM-dd HH:mm")
        private String interviewDate;

        @ApiModelProperty(value = "后台备注 / 面试评价")
        private String adminRemark;

        @ApiModelProperty(value = "处理人")
        private String handler;
    }
}
