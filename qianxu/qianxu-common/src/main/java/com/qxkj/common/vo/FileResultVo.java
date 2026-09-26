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
public class FileResultVo{

    // 文件名
    private String fileName;

    // 扩展名
    private String extName;

    // 文件大小，字节
    private Long fileSize;

    // 文件存储在服务器的相对地址
//    private String serverPath;

    //可供访问的url 域名根据配置存储，代替了上面serverPath 的功能
    private String url;

    //类型
    private String type;
}
