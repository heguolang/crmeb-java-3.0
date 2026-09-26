package com.qxkj.common.vo;

import lombok.Data;

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
public class CloudVo {

    //域名空间
    private String domain;

    //accessKey
    private String accessKey;

    //secretKey
    private String secretKey;

    //bucketName
    private String bucketName;

//    //webPath web访问的url地址，云服务会自动创建相对应的文件夹
//    private String webPath;
//
//    //服务器存储的文件绝对路径
//    private String filePath;
//
//    //图片id
//    private Integer id;

    //节点
    private String region;
}
