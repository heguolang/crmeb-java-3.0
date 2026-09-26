package com.qxkj.service.service;


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
public interface JdCloudService {

    /**
     * 文件上传
     * @param fileName 文件名称
     * @param localFilePath 本地文件地址
     * @param bucket 存储桶名称
     */
    void uploadFile(String fileName, String localFilePath, String bucket);

    /**
     * 创建新的存储空间
     */
    void createBucket(String bucketName);

    /**
     * 获取文件URL
     * @param bucket 存储桶名称
     * @param fileName 文件名称
     */
    String getUrl(String bucket, String fileName);
}
