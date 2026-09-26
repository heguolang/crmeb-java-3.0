package com.qxkj.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Data
public class SystemAttachmentRequest {
    private static final long serialVersionUID=1L;

    private Integer attId;

    @ApiModelProperty(value = "附件名称")
    private String name;

    @ApiModelProperty(value = "附件路径")
    private String attDir;

    @ApiModelProperty(value = "压缩图片路径")
    private String sattDir;

    @ApiModelProperty(value = "服务器上存储的绝对地址")
    private String rootDir;

    @ApiModelProperty(value = "附件大小")
    private String attSize;

    @ApiModelProperty(value = "附件类型")
    private String attType;

    @ApiModelProperty(value = "模块，store")
    private String model;

    @ApiModelProperty(value = "图片上传类型 1本地 2七牛云 3OSS 4COS ")
    private Integer imageType;

    @ApiModelProperty(value = "图片上传模块类型 1 后台上传 2 用户生成")
    private Integer moduleType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
