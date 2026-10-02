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
 * 企业服务-合同
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_svc_contract")
@ApiModel(value = "SvcContract对象", description = "企业服务-合同")
public class SvcContract implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签约状态常量（2026-10-02 第八批语义重构）
     *
     * 合同在线下签署，线上只做「登记 → 选状态 → 归档」，不做双方在线签署、不做客户确认。
     * 状态由后台管理员直接选择，每次变更写一条日志进 status_log。
     */
    public static final Integer STATUS_DRAFT = 0;        // 草稿：刚登记，还没定稿
    public static final Integer STATUS_WAIT_SIGN = 1;    // 待签署：定稿了，等线下签（已寄出 / 平台已发起）
    public static final Integer STATUS_SIGNED = 2;       // 已签署：双方已签字盖章，原件在途或已回寄
    public static final Integer STATUS_EFFECTIVE = 3;    // 已生效：已签收归档，正式生效（触发自动建项目）
    public static final Integer STATUS_TERMINATED = -1;  // 已终止：作废或中止，不再推进

    /** 签约渠道 */
    public static final String CHANNEL_PAPER = "paper";       // 纸质合同（线下签署）
    public static final String CHANNEL_ELECTRONIC = "electronic"; // 电子合同（第三方平台）

    /** 签约主体 */
    public static final Integer PARTY_COMPANY = 1;      // 企业
    public static final Integer PARTY_PERSON = 2;       // 个人

    /** 状态中文名（后台与会员端共用，保证两端口径一致） */
    public static String statusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "待签署";
            case 2: return "已签署";
            case 3: return "已生效";
            case -1: return "已终止";
            default: return "";
        }
    }

    /** 渠道中文名 */
    public static String channelName(String channel) {
        return CHANNEL_ELECTRONIC.equals(channel) ? "电子合同" : "纸质合同";
    }

    /** 签章样式（电子签章） */
    public static final String SEAL_ROUND = "round";    // 圆形标准章
    public static final String SEAL_OVAL = "oval";      // 椭圆公章
    public static final String SEAL_SQUARE = "square";  // 方形合同章
    public static final String SEAL_MODERN = "modern";  // 现代电子章

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "会员UID（甲方：签约客户）")
    private Integer uid;

    @ApiModelProperty(value = "乙方（黔序）签署会员账号UID，为 0 表示未指定")
    private Integer ourUid;

    @ApiModelProperty(value = "关联项目ID（签约后自动生成）")
    private Integer projectId;

    // ==================== 签约渠道（2026-10-02 第八批） ====================

    @ApiModelProperty(value = "签约渠道：paper=纸质 electronic=电子")
    private String signChannel;

    @ApiModelProperty(value = "纸质签署方式：当面签/快递往返/扫描件回传")
    private String signType;

    @ApiModelProperty(value = "纸质打印份数")
    private String printCount;

    @ApiModelProperty(value = "快递公司")
    private String expressCo;

    @ApiModelProperty(value = "寄出快递单号")
    private String expressNo;

    @ApiModelProperty(value = "回寄快递单号")
    private String expressNoBack;

    @ApiModelProperty(value = "合同寄出日期 yyyy-MM-dd")
    private String sentDate;

    @ApiModelProperty(value = "回寄签收日期 yyyy-MM-dd")
    private String backDate;

    @ApiModelProperty(value = "电子签约平台名称")
    private String signPlatform;

    @ApiModelProperty(value = "平台合同编号/存证编号")
    private String platformNo;

    @ApiModelProperty(value = "平台查看链接")
    private String platformUrl;

    @ApiModelProperty(value = "电子合同发起日期 yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty(value = "存证报告/出证报告URL")
    private String voucherFile;

    @ApiModelProperty(value = "存证报告文件名")
    private String voucherName;

    @ApiModelProperty(value = "关联服务需求ID（eb_svc_demand.id）")
    private Integer demandId;

    @ApiModelProperty(value = "服务期限描述，如 2026-10-02 ~ 2027-10-01")
    private String serviceTerm;

    @ApiModelProperty(value = "签约状态变更日志 JSON：[{time,status,statusName,operator,remark}]")
    private String statusLog;

    @ApiModelProperty(value = "状态语义迁移标记（内部用）")
    private Integer statusMigrated;

    @ApiModelProperty(value = "合同名称")
    private String title;

    @ApiModelProperty(value = "合同金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "签约主体：1=企业 2=个人")
    private Integer partyType;

    @ApiModelProperty(value = "签约主体名称（企业名称/个人姓名）")
    private String partyName;

    @ApiModelProperty(value = "甲方统一社会信用代码")
    private String custCode;

    @ApiModelProperty(value = "甲方法定代表人")
    private String custLegal;

    @ApiModelProperty(value = "甲方联系电话")
    private String custPhone;

    @ApiModelProperty(value = "甲方联系地址")
    private String custAddr;

    @ApiModelProperty(value = "合同内容")
    private String content;

    @ApiModelProperty(value = "合同文件URL，多个逗号分隔（pdf/doc/docx/xls/图片）")
    private String annex;

    @ApiModelProperty(value = "合同文件名，与 annex 的URL按顺序一一对应，多个逗号分隔")
    private String annexName;

    // ==================== 用印位置 + 双方电子签章（2026-10-02 第六批） ====================

    @ApiModelProperty(value = "合同正文 PDF 的 URL（doc/docx/xls 转换产物，或原 PDF/图片）")
    private String srcPdf;

    @ApiModelProperty(value = "合同正文 PDF 的文件名")
    private String srcPdfName;

    @ApiModelProperty(value = "合同正文 PDF 页数")
    private Integer pageCount;

    @ApiModelProperty(value = "用印位置布局 JSON（相对页面宽高的百分比坐标）")
    private String signLayout;

    @ApiModelProperty(value = "乙方（黔序）签约方名称")
    private String ourCompany;

    @ApiModelProperty(value = "乙方统一社会信用代码")
    private String ourCode;

    @ApiModelProperty(value = "乙方法定代表人")
    private String ourLegal;

    @ApiModelProperty(value = "乙方联系电话")
    private String ourPhone;

    @ApiModelProperty(value = "乙方地址")
    private String ourAddr;

    @ApiModelProperty(value = "乙方公章图片URL")
    private String ourSeal;

    @ApiModelProperty(value = "乙方法定代表人签字图片URL")
    private String ourSign;

    @ApiModelProperty(value = "乙方（黔序）签署人姓名")
    private String ourSigner;

    @ApiModelProperty(value = "甲方（客户）圆形公章图片URL（签署时按签约方名称生成）")
    private String custSeal;

    @ApiModelProperty(value = "乙方（黔序）签署时间")
    private Date ourSignedAt;

    @ApiModelProperty(value = "乙方单方签署版 PDF 的 URL（原稿+乙方章签，供乙方签完立刻查看）")
    private String ourStampedFile;

    @ApiModelProperty(value = "乙方单方签署版 PDF 的文件名")
    private String ourStampedName;

    @ApiModelProperty(value = "甲方单方签署版 PDF 的 URL（原稿+甲方章签，供甲方签完立刻查看）")
    private String custStampedFile;

    @ApiModelProperty(value = "甲方单方签署版 PDF 的文件名")
    private String custStampedName;

    @ApiModelProperty(value = "双方签署完成的正式合同 PDF 的 URL（带双方圆形公章与双方签字）")
    private String signedFile;

    @ApiModelProperty(value = "正式合同 PDF 的文件名")
    private String signedFileName;

    @ApiModelProperty(value = "正式合同 PDF 的 sha256（存证）")
    private String fileHash;

    @ApiModelProperty(value = "签字/签章所在合同图片页码（1开始，0=最后一页；文件模式下不再使用，仅兼容旧数据）")
    private Integer signPage;

    @ApiModelProperty(value = "签字位置：left/center/right（独立签署区内的排版位置）")
    private String signPos;

    @ApiModelProperty(value = "签章位置：left/center/right（独立签署区内的排版位置）")
    private String sealPos;

    @ApiModelProperty(value = "签章样式：round=圆形标准章 oval=椭圆公章 square=方形合同章 modern=现代电子章")
    private String sealStyle;

    @ApiModelProperty(value = "电子签章图片URL（用签约主体名称自动生成）")
    private String sealImage;

    @ApiModelProperty(value = "独立签署页图片URL（签章+签字+签署日期，附着到合同）")
    private String signSheet;

    @ApiModelProperty(value = "签约状态：0=草稿 1=待签署 2=已签署 3=已生效 -1=已终止")
    private Integer status;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "甲方签署人姓名（手写人）")
    private String signName;

    @ApiModelProperty(value = "甲方签署人手机号")
    private String signPhone;

    @ApiModelProperty(value = "甲方手写签名图片URL")
    private String signHand;

    @ApiModelProperty(value = "甲方签署时间")
    private Date signTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "会员昵称（甲方）")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "会员手机号（甲方）")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "乙方会员昵称")
    @TableField(exist = false)
    private String ourNickname;

    @ApiModelProperty(value = "乙方会员手机号")
    @TableField(exist = false)
    private String ourPhoneNo;

    @ApiModelProperty(value = "当前登录者在合同中的角色：cust=甲方 our=乙方（会员端返回）")
    @TableField(exist = false)
    private String myRole;

    @ApiModelProperty(value = "甲方是否已签署（派生字段）")
    @TableField(exist = false)
    private Boolean custSigned;

    @ApiModelProperty(value = "乙方是否已签署（派生字段）")
    @TableField(exist = false)
    private Boolean ourSigned;

    @ApiModelProperty(value = "双方是否已签完、正式合同是否已生成（派生字段）")
    @TableField(exist = false)
    private Boolean finished;

    @ApiModelProperty(value = "进度记录（详情用）")
    @TableField(exist = false)
    private List<SvcProjectProgress> progressList;

    // ==================== 派生字段（不入库，接口返回给前端用） ====================

    @ApiModelProperty(value = "签约状态中文名（派生字段）")
    @TableField(exist = false)
    private String statusText;

    @ApiModelProperty(value = "签约渠道中文名：纸质合同/电子合同（派生字段）")
    @TableField(exist = false)
    private String channelText;

    @ApiModelProperty(value = "关联项目名称（派生字段，合同详情展示「关联项目进度」用）")
    @TableField(exist = false)
    private String projectName;

    @ApiModelProperty(value = "关联项目（派生字段，合同详情展示用）")
    @TableField(exist = false)
    private SvcProject project;

    @ApiModelProperty(value = "关联需求编号（派生字段）")
    @TableField(exist = false)
    private String demandNo;

    @ApiModelProperty(value = "状态变更日志（解析后的结构化数据，派生字段）")
    @TableField(exist = false)
    private List<StatusLogItem> statusLogList;

    /** 一条状态变更记录 */
    @Data
    public static class StatusLogItem implements Serializable {
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
