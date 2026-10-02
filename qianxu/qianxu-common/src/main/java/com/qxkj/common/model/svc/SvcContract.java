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

    /** 状态常量 */
    public static final Integer STATUS_WAIT_SIGN = 0;   // 待签约
    public static final Integer STATUS_SIGNED = 1;      // 已签约
    public static final Integer STATUS_REJECT = 2;      // 已驳回

    /** 签约主体 */
    public static final Integer PARTY_COMPANY = 1;      // 企业
    public static final Integer PARTY_PERSON = 2;       // 个人

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

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "关联项目ID（签约后自动生成）")
    private Integer projectId;

    @ApiModelProperty(value = "合同名称")
    private String title;

    @ApiModelProperty(value = "合同金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "签约主体：1=企业 2=个人")
    private Integer partyType;

    @ApiModelProperty(value = "签约主体名称（企业名称/个人姓名）")
    private String partyName;

    @ApiModelProperty(value = "合同内容")
    private String content;

    @ApiModelProperty(value = "合同文件URL，多个逗号分隔（pdf/doc/docx/xls/图片）")
    private String annex;

    @ApiModelProperty(value = "合同文件名，与 annex 的URL按顺序一一对应，多个逗号分隔")
    private String annexName;

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

    @ApiModelProperty(value = "状态：0=待签约 1=已签约 2=已驳回")
    private Integer status;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "签约负责人姓名（手写人）")
    private String signName;

    @ApiModelProperty(value = "签约负责人手机号")
    private String signPhone;

    @ApiModelProperty(value = "手写签名图片URL")
    private String signHand;

    @ApiModelProperty(value = "签约时间")
    private Date signTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "会员昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "会员手机号")
    @TableField(exist = false)
    private String phone;

    @ApiModelProperty(value = "进度记录（详情用）")
    @TableField(exist = false)
    private List<SvcProjectProgress> progressList;
}
