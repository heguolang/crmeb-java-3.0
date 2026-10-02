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
 * 企业服务-发票申请
 *
 * 客户在会员端 H5 提交开票信息 → 后台查看信息、开票、上传成品发票文件并变更状态。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_invoice")
@ApiModel(value = "SvcInvoice对象", description = "企业服务-发票申请")
public class SvcInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态常量 */
    public static final Integer STATUS_WAIT = 0;      // 待开票
    public static final Integer STATUS_ISSUING = 1;   // 开票中
    public static final Integer STATUS_ISSUED = 2;    // 已开票
    public static final Integer STATUS_REJECT = 3;    // 已驳回

    /** 发票类型 */
    public static final Integer TYPE_NORMAL = 1;      // 增值税普通发票
    public static final Integer TYPE_SPECIAL = 2;     // 增值税专用发票
    public static final Integer TYPE_ELECTRONIC = 3;  // 电子普通发票

    /** 抬头类型 */
    public static final Integer TITLE_COMPANY = 1;    // 企业
    public static final Integer TITLE_PERSON = 2;     // 个人

    /** 状态中文名 */
    public static String statusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待开票";
            case 1: return "开票中";
            case 2: return "已开票";
            case 3: return "已驳回";
            default: return "";
        }
    }

    /** 发票类型中文名 */
    public static String typeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "增值税普通发票";
            case 2: return "增值税专用发票";
            case 3: return "电子普通发票";
            default: return "";
        }
    }

    /** 抬头类型中文名 */
    public static String titleTypeName(Integer titleType) {
        return TITLE_PERSON.equals(titleType) ? "个人" : "企业";
    }

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "申请单号")
    private String invoiceNo;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "关联合同ID")
    private Integer contractId;

    @ApiModelProperty(value = "关联合同编号（冗余，便于列表展示）")
    private String contractNo;

    @ApiModelProperty(value = "发票类型：1=增值税普通发票 2=增值税专用发票 3=电子普通发票")
    private Integer type;

    @ApiModelProperty(value = "抬头类型：1=企业 2=个人")
    private Integer titleType;

    @ApiModelProperty(value = "发票抬头（公司名称/个人姓名）")
    private String title;

    @ApiModelProperty(value = "纳税人识别号")
    private String taxNo;

    @ApiModelProperty(value = "开户银行（专票必填）")
    private String bank;

    @ApiModelProperty(value = "银行账号（专票必填）")
    private String bankAccount;

    @ApiModelProperty(value = "注册地址（专票必填）")
    private String addr;

    @ApiModelProperty(value = "注册电话（专票必填）")
    private String phone;

    @ApiModelProperty(value = "开票金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "开票内容：如 技术服务费")
    private String content;

    @ApiModelProperty(value = "收件人")
    private String receiver;

    @ApiModelProperty(value = "收件电话")
    private String receiverPhone;

    @ApiModelProperty(value = "收件地址（纸质发票邮寄用）")
    private String receiverAddr;

    @ApiModelProperty(value = "接收邮箱（电子发票用）")
    private String email;

    @ApiModelProperty(value = "客户备注")
    private String remark;

    @ApiModelProperty(value = "状态：0=待开票 1=开票中 2=已开票 3=已驳回")
    private Integer status;

    @ApiModelProperty(value = "成品发票文件URL（PDF/图片）")
    private String fileUrl;

    @ApiModelProperty(value = "成品发票文件名")
    private String fileName;

    @ApiModelProperty(value = "邮寄快递公司")
    private String expressCo;

    @ApiModelProperty(value = "邮寄快递单号")
    private String expressNo;

    @ApiModelProperty(value = "寄出日期 yyyy-MM-dd")
    private String sentDate;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "经办人")
    private String operator;

    @ApiModelProperty(value = "开票时间")
    private Date issueTime;

    @ApiModelProperty(value = "状态变更日志 JSON：[{time,status,statusName,operator,remark}]")
    private String log;

    @ApiModelProperty(value = "提交时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    // ==================== 派生字段（不入库） ====================

    @ApiModelProperty(value = "状态中文名（派生字段）")
    @TableField(exist = false)
    private String statusText;

    @ApiModelProperty(value = "发票类型中文名（派生字段）")
    @TableField(exist = false)
    private String typeText;

    @ApiModelProperty(value = "抬头类型中文名（派生字段）")
    @TableField(exist = false)
    private String titleTypeText;

    @ApiModelProperty(value = "会员昵称（派生字段）")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "会员手机号（派生字段）")
    @TableField(exist = false)
    private String phoneNo;

    @ApiModelProperty(value = "状态变更日志（解析后的结构化数据，派生字段）")
    @TableField(exist = false)
    private List<LogItem> logList;

    /** 一条状态变更记录 */
    @Data
    public static class LogItem implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 变更时间 yyyy-MM-dd HH:mm:ss */
        private String time;
        /** 变更后的状态值 */
        private Integer status;
        /** 变更后的状态名 */
        private String statusName;
        /** 经办人 */
        private String operator;
        /** 备注 */
        private String remark;
    }
}
