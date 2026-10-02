package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业服务 请求参数集合
 */
public class SvcRequests {

    private SvcRequests() {
    }

    @Data
    @ApiModel(value = "SvcContractSaveRequest对象", description = "保存合同")
    public static class ContractSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "会员UID")
        @NotNull(message = "请选择会员")
        private Integer uid;

        @ApiModelProperty(value = "合同名称")
        @NotBlank(message = "请填写合同名称")
        private String title;

        @ApiModelProperty(value = "合同金额")
        private BigDecimal amount;

        @ApiModelProperty(value = "签约主体：1=企业 2=个人")
        private Integer partyType;

        @ApiModelProperty(value = "签约主体名称（企业名称/个人姓名）")
        private String partyName;

        @ApiModelProperty(value = "合同文件URL，多个逗号分隔（pdf/doc/docx/xls/图片）")
        private String annex;

        @ApiModelProperty(value = "合同文件名，与 annex 的URL按顺序一一对应，多个逗号分隔")
        private String annexName;

        @ApiModelProperty(value = "签字/签章所在合同页码（文件模式下不再使用）")
        private Integer signPage;

        @ApiModelProperty(value = "签字位置：left/center/right（独立签署区内排版）")
        private String signPos;

        @ApiModelProperty(value = "签章位置：left/center/right（独立签署区内排版）")
        private String sealPos;

        @ApiModelProperty(value = "合同内容")
        private String content;

        @ApiModelProperty(value = "备注")
        private String remark;

        // ==================== 用印位置 + 乙方（黔序）签约方信息（2026-10-02 第六批） ====================

        @ApiModelProperty(value = "合同正文 PDF 的 URL（上传后由服务端转换生成）")
        private String srcPdf;

        @ApiModelProperty(value = "合同正文 PDF 的文件名")
        private String srcPdfName;

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

        @ApiModelProperty(value = "甲方（客户）公章图片URL（后台预设，可为空）")
        private String custSeal;

        // ==================== 双方各自登录 H5 签署（2026-10-02 第七批） ====================

        @ApiModelProperty(value = "乙方（黔序）签署会员账号UID")
        private Integer ourUid;

        @ApiModelProperty(value = "甲方统一社会信用代码")
        private String custCode;

        @ApiModelProperty(value = "甲方法定代表人")
        private String custLegal;

        @ApiModelProperty(value = "甲方联系电话")
        private String custPhone;

        @ApiModelProperty(value = "甲方联系地址")
        private String custAddr;

        // ==================== 签约渠道 + 状态直选（2026-10-02 第八批） ====================

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

        @ApiModelProperty(value = "关联服务需求ID")
        private Integer demandId;

        @ApiModelProperty(value = "服务期限描述")
        private String serviceTerm;

        @ApiModelProperty(value = "签约状态：0=草稿 1=待签署 2=已签署 3=已生效 -1=已终止（登记时可选）")
        private Integer status;

        @ApiModelProperty(value = "成品件（双方签署完成的扫描件/平台导出件）URL")
        private String signedFile;

        @ApiModelProperty(value = "成品件文件名")
        private String signedFileName;
    }

    @Data
    @ApiModel(value = "SvcContractOurDefaultRequest对象", description = "乙方（黔序）签约方默认值")
    public static class ContractOurDefaultRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "乙方（黔序）签署会员账号UID")
        private Integer ourUid;

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
    }

    @Data
    @ApiModel(value = "SvcContractLayoutRequest对象", description = "保存合同用印位置")
    public static class ContractLayoutRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "合同ID")
        @NotNull(message = "请选择合同")
        private Integer id;

        @ApiModelProperty(value = "用印位置布局 JSON")
        private String signLayout;
    }

    @Data
    @ApiModel(value = "SvcProjectSaveRequest对象", description = "保存项目")
    public static class ProjectSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "会员UID")
        @NotNull(message = "请选择会员")
        private Integer uid;

        @ApiModelProperty(value = "项目名称")
        @NotBlank(message = "请填写项目名称")
        private String name;

        @ApiModelProperty(value = "项目类型：小程序/APP开发/系统定制等")
        private String type;

        @ApiModelProperty(value = "状态：1=需求确认 2=开发中 3=待验收 4=已完成 -1=已终止")
        private Integer status;

        @ApiModelProperty(value = "当前阶段")
        private String stage;

        @ApiModelProperty(value = "项目经理")
        private String manager;

        @ApiModelProperty(value = "交付周期描述")
        private String cycle;

        @ApiModelProperty(value = "排序（降序）")
        private Integer sort;
    }

    @Data
    @ApiModel(value = "SvcContractSignRequest对象", description = "会员签约合同")
    public static class ContractSignRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "合同ID")
        @NotNull(message = "参数错误")
        private Integer id;

        @ApiModelProperty(value = "签约负责人姓名")
        @NotBlank(message = "请输入负责人姓名")
        private String signName;

        @ApiModelProperty(value = "签约负责人手机号")
        @NotBlank(message = "请输入负责人手机号")
        private String phone;

        @ApiModelProperty(value = "短信验证码（后台开启验证时必填）")
        private String code;

        @ApiModelProperty(value = "手写签名图片URL")
        @NotBlank(message = "请手写签名")
        private String signImage;

        @ApiModelProperty(value = "签章样式：round=圆形标准章 oval=椭圆公章 square=方形合同章 modern=现代电子章")
        private String sealStyle;

        @ApiModelProperty(value = "电子签章图片URL（企业签约时前端用主体名称自动生成后上传）")
        private String sealImage;

        @ApiModelProperty(value = "独立签署页图片URL（签章+签字+签署日期合成后上传，附着到合同）")
        private String signSheet;
    }

    @Data
    @ApiModel(value = "SvcProgressSetRequest对象", description = "设置项目进度")
    public static class ProgressSetRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "项目ID")
        @NotNull(message = "请选择项目")
        private Integer projectId;

        @ApiModelProperty(value = "阶段名称")
        @NotBlank(message = "请填写阶段名称")
        private String stage;

        @ApiModelProperty(value = "进度百分比 0-100")
        @NotNull(message = "请填写进度百分比")
        private Integer percent;

        @ApiModelProperty(value = "说明")
        private String remark;

        @ApiModelProperty(value = "是否同步更新项目状态为该进度：1=是")
        private Integer syncStatus;
    }

    @Data
    @ApiModel(value = "SvcCaseSaveRequest对象", description = "保存客户案例")
    public static class CaseSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "案例名称")
        @NotBlank(message = "请填写案例名称")
        private String title;

        @ApiModelProperty(value = "分类：小程序/APP开发/系统定制")
        private String category;

        @ApiModelProperty(value = "一句话描述")
        private String description;

        @ApiModelProperty(value = "案例配图URL")
        private String image;

        @ApiModelProperty(value = "排序（降序）")
        private Integer sort;

        @ApiModelProperty(value = "是否显示：1=是 0=否")
        private Integer isShow;
    }

    // ==================================================================
    //  服务需求单（2026-10-02 第八批）
    // ==================================================================

    @Data
    @ApiModel(value = "SvcDemandSubmitRequest对象", description = "会员端提交服务需求")
    public static class DemandSubmitRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "需求类型：小程序/APP定制/系统开发等")
        private String type;

        @ApiModelProperty(value = "需求标题")
        @NotBlank(message = "请填写需求标题")
        private String title;

        @ApiModelProperty(value = "需求描述")
        @NotBlank(message = "请填写需求描述")
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
        @NotBlank(message = "请填写联系人")
        private String contactName;

        @ApiModelProperty(value = "联系电话")
        @NotBlank(message = "请填写联系电话")
        private String contactPhone;

        @ApiModelProperty(value = "公司/单位名称")
        private String company;
    }

    @Data
    @ApiModel(value = "SvcDemandSaveRequest对象", description = "后台保存服务需求")
    public static class DemandSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "会员UID")
        private Integer uid;

        @ApiModelProperty(value = "需求类型")
        private String type;

        @ApiModelProperty(value = "需求标题")
        @NotBlank(message = "请填写需求标题")
        private String title;

        @ApiModelProperty(value = "需求描述")
        private String content;

        @ApiModelProperty(value = "预算区间")
        private String budget;

        @ApiModelProperty(value = "期望完成时间")
        private String hopeDate;

        @ApiModelProperty(value = "参考附件URL")
        private String attach;

        @ApiModelProperty(value = "附件文件名")
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
    }

    @Data
    @ApiModel(value = "SvcDemandQuoteRequest对象", description = "后台对服务需求报价 / 指派跟进人")
    public static class DemandQuoteRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "需求ID")
        @NotNull(message = "请选择需求")
        private Integer id;

        @ApiModelProperty(value = "报价金额")
        private BigDecimal amount;

        @ApiModelProperty(value = "方案说明")
        private String quoteDesc;

        @ApiModelProperty(value = "报价单/方案附件URL")
        private String quoteFile;

        @ApiModelProperty(value = "报价附件文件名")
        private String quoteName;

        @ApiModelProperty(value = "跟进人后台UID")
        private Integer followerUid;

        @ApiModelProperty(value = "跟进人姓名")
        private String follower;

        @ApiModelProperty(value = "是否同时把状态置为「已报价」：1=是（默认），0=只指派不报价")
        private Integer publish;
    }

    @Data
    @ApiModel(value = "SvcDemandFollowRequest对象", description = "后台追加一条跟进记录")
    public static class DemandFollowRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "需求ID")
        @NotNull(message = "请选择需求")
        private Integer id;

        @ApiModelProperty(value = "跟进内容")
        @NotBlank(message = "请填写跟进内容")
        private String content;

        @ApiModelProperty(value = "记录人")
        private String operator;
    }

    @Data
    @ApiModel(value = "SvcDemandCloseRequest对象", description = "关闭服务需求")
    public static class DemandCloseRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "需求ID")
        @NotNull(message = "请选择需求")
        private Integer id;

        @ApiModelProperty(value = "关闭原因")
        private String reason;
    }

    // ==================================================================
    //  合同签约状态变更（2026-10-02 第八批）
    // ==================================================================

    @Data
    @ApiModel(value = "SvcContractStatusRequest对象", description = "变更合同签约状态")
    public static class ContractStatusRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "合同ID")
        @NotNull(message = "请选择合同")
        private Integer id;

        @ApiModelProperty(value = "变更为：0=草稿 1=待签署 2=已签署 3=已生效 -1=已终止")
        @NotNull(message = "请选择要变更的状态")
        private Integer status;

        @ApiModelProperty(value = "经办人")
        private String operator;

        @ApiModelProperty(value = "备注（会记入合同日志，并显示在客户 H5 的合同详情里）")
        private String remark;
    }

    // ==================================================================
    //  签约渠道平台（2026-10-02 第八批）
    // ==================================================================

    @Data
    @ApiModel(value = "SvcPlatformSaveRequest对象", description = "保存签约渠道平台")
    public static class PlatformSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=新增）")
        private Integer id;

        @ApiModelProperty(value = "平台名称")
        @NotBlank(message = "请填写平台名称")
        private String name;

        @ApiModelProperty(value = "类型：电子签平台/线下渠道")
        private String kind;

        @ApiModelProperty(value = "资质与定位说明")
        private String description;

        @ApiModelProperty(value = "状态：1=启用 0=停用")
        private Integer status;

        @ApiModelProperty(value = "排序（降序）")
        private Integer sort;
    }

    // ==================================================================
    //  发票申请（2026-10-02 第九批）
    // ==================================================================

    @Data
    @ApiModel(value = "SvcInvoiceSubmitRequest对象", description = "会员端提交开票申请")
    public static class InvoiceSubmitRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "关联合同ID（可为空=不关联合同）")
        private Integer contractId;

        @ApiModelProperty(value = "发票类型：1=增值税普通发票 2=增值税专用发票 3=电子普通发票")
        @NotNull(message = "请选择发票类型")
        private Integer type;

        @ApiModelProperty(value = "抬头类型：1=企业 2=个人")
        private Integer titleType;

        @ApiModelProperty(value = "发票抬头（公司名称/个人姓名）")
        @NotBlank(message = "请填写发票抬头")
        private String title;

        @ApiModelProperty(value = "纳税人识别号（企业抬头必填）")
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
        @NotNull(message = "请填写开票金额")
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
    }

    @Data
    @ApiModel(value = "SvcInvoiceSaveRequest对象", description = "后台保存 / 开票 / 变更发票状态")
    public static class InvoiceSaveRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "主键（为空=后台代客补录）")
        private Integer id;

        @ApiModelProperty(value = "会员UID")
        private Integer uid;

        @ApiModelProperty(value = "关联合同ID")
        private Integer contractId;

        @ApiModelProperty(value = "发票类型：1=增值税普通发票 2=增值税专用发票 3=电子普通发票")
        private Integer type;

        @ApiModelProperty(value = "抬头类型：1=企业 2=个人")
        private Integer titleType;

        @ApiModelProperty(value = "发票抬头")
        private String title;

        @ApiModelProperty(value = "纳税人识别号")
        private String taxNo;

        @ApiModelProperty(value = "开户银行")
        private String bank;

        @ApiModelProperty(value = "银行账号")
        private String bankAccount;

        @ApiModelProperty(value = "注册地址")
        private String addr;

        @ApiModelProperty(value = "注册电话")
        private String phone;

        @ApiModelProperty(value = "开票金额")
        private BigDecimal amount;

        @ApiModelProperty(value = "开票内容")
        private String content;

        @ApiModelProperty(value = "收件人")
        private String receiver;

        @ApiModelProperty(value = "收件电话")
        private String receiverPhone;

        @ApiModelProperty(value = "收件地址")
        private String receiverAddr;

        @ApiModelProperty(value = "接收邮箱")
        private String email;

        @ApiModelProperty(value = "客户备注")
        private String remark;

        @ApiModelProperty(value = "状态：0=待开票 1=开票中 2=已开票 3=已驳回")
        private Integer status;

        @ApiModelProperty(value = "成品发票文件URL")
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

        @ApiModelProperty(value = "变更备注（记入日志，客户在 H5 可见）")
        private String logRemark;
    }
}
