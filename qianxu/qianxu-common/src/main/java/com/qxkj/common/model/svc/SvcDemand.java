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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 企业服务-服务需求单
 *
 * 客户在会员端 H5 提交需求 → 落这张表 → 后台受理 / 指派跟进人 / 报价 / 转合同。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_demand")
@ApiModel(value = "SvcDemand对象", description = "企业服务-服务需求单")
public class SvcDemand implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态常量 */
    public static final Integer STATUS_WAIT = 0;       // 待受理
    public static final Integer STATUS_FOLLOWING = 1;  // 跟进中
    public static final Integer STATUS_QUOTED = 2;     // 已报价（等客户同意）
    public static final Integer STATUS_DEAL = 3;       // 已成单（客户已同意报价 / 已转合同）
    public static final Integer STATUS_CLOSED = 4;     // 已关闭

    /** 状态中文名 */
    public static String statusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待受理";
            case 1: return "跟进中";
            case 2: return "已报价";
            case 3: return "已成单";
            case 4: return "已关闭";
            default: return "";
        }
    }

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "需求编号")
    private String demandNo;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "需求类型：小程序/APP定制/系统开发等")
    private String type;

    @ApiModelProperty(value = "需求标题")
    private String title;

    @ApiModelProperty(value = "需求描述")
    private String content;

    @ApiModelProperty(value = "预算区间，如 5~10 万")
    private String budget;

    @ApiModelProperty(value = "期望完成时间")
    private String hopeDate;

    @ApiModelProperty(value = "参考附件URL，多个逗号分隔")
    private String attach;

    @ApiModelProperty(value = "附件文件名，与 attach 按顺序一一对应")
    private String attachName;

    @ApiModelProperty(value = "联系人")
    private String contactName;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "公司/单位名称")
    private String company;

    @ApiModelProperty(value = "状态：0=待受理 1=跟进中 2=已报价 3=已成单 4=已关闭")
    private Integer status;

    @ApiModelProperty(value = "跟进人后台UID")
    private Integer followerUid;

    @ApiModelProperty(value = "跟进人姓名")
    private String follower;

    @ApiModelProperty(value = "报价金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "方案说明")
    private String quoteDesc;

    @ApiModelProperty(value = "报价单/方案附件URL")
    private String quoteFile;

    @ApiModelProperty(value = "报价附件文件名")
    private String quoteName;

    @ApiModelProperty(value = "报价发布时间")
    private Date quoteTime;

    @ApiModelProperty(value = "跟进记录 JSON 数组：[{time,content,operator}]")
    private String followLog;

    @ApiModelProperty(value = "转出的合同ID（eb_svc_contract.id）")
    private Integer contractId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    // ==================== 派生字段（不入库） ====================

    @ApiModelProperty(value = "状态中文名（派生字段）")
    @TableField(exist = false)
    private String statusText;

    @ApiModelProperty(value = "会员昵称（派生字段）")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "会员手机号（派生字段）")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "转出的合同编号（派生字段）")
    @TableField(exist = false)
    private String contractNo;

    @ApiModelProperty(value = "跟进记录（解析后的结构化数据，派生字段）")
    @TableField(exist = false)
    private List<FollowItem> followList;

    @ApiModelProperty(value = "附件 URL 列表（派生字段）")
    @TableField(exist = false)
    private List<String> attachList;

    @ApiModelProperty(value = "附件文件名列表（派生字段）")
    @TableField(exist = false)
    private List<String> attachNameList;

    /** 一条跟进记录 */
    @Data
    public static class FollowItem implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 记录时间 yyyy-MM-dd HH:mm:ss */
        private String time;
        /** 跟进内容 */
        private String content;
        /** 记录人 */
        private String operator;
    }
}
