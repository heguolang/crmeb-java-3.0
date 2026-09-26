package com.qxkj.common.response;



import com.qxkj.common.model.systemStatus.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

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
public class SystemStatusResponse {

    @ApiModelProperty(value = "CPU信息")
    private CpuInfo cpuInfo;

    @ApiModelProperty(value = "系统内存")
    private com.qxkj.common.model.systemStatus.systemMemoryInfo systemMemoryInfo;

    @ApiModelProperty(value = "JVM内存")
    private JvmMemoryInfo jvmMemoryInfo;

    @ApiModelProperty(value = "服务器信息")
    private ServerInfo serverInfo;

    @ApiModelProperty(value = "JVM信息")
    private JvmInfo jvmInfo;

    @ApiModelProperty(value = "磁盘信息")
    private List<DiskInfo> diskInfos;

}














