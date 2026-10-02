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
}
