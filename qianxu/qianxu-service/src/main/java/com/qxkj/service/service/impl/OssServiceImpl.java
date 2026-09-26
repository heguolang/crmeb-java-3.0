package com.qxkj.service.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.vo.CloudVo;
import com.qxkj.service.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;


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
@Service
public class OssServiceImpl implements OssService {

    private static final Logger logger = LoggerFactory.getLogger(OssServiceImpl.class);

    @Override
    public void upload(CloudVo cloudVo, String webPth, String localFile, File file) {
        logger.info("上传文件开始：" + localFile);
        OSS ossClient = new OSSClientBuilder().build(cloudVo.getRegion(), cloudVo.getAccessKey(), cloudVo.getSecretKey());
        try {
            //判断bucket是否存在
            if (!ossClient.doesBucketExist(cloudVo.getBucketName())){
                ossClient.createBucket(cloudVo.getBucketName());
            }

            if(!file.exists()){
                logger.info("上传文件" + localFile + "不存在：");
                return;
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(cloudVo.getBucketName(), webPth, file);
            // 上传文件。
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            logger.info("上传文件 -- 结束：" + putObjectResult.getETag());

        } catch (Exception e){
            throw new QianxuException(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
}

