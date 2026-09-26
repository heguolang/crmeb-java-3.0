package com.qxkj.service.service.impl;

import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.vo.CloudVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;
import com.qxkj.service.service.CosService;
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
public class CosServiceImpl implements CosService {

    private static final Logger logger = LoggerFactory.getLogger(CosServiceImpl.class);

    @Override
    public void uploadFile(CloudVo cloudVo, String webPth, String localFile, Integer id, COSClient cosClient) {

        logger.info("上传文件" + id + "开始：" + localFile);
        try {
            File file = new File(localFile);
            if(!file.exists()){
                logger.info("上传文件"+ id + localFile + "不存在：");
                return;
            }

            if(!cosClient.doesBucketExist(cloudVo.getBucketName())){
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(cloudVo.getBucketName());
                // 设置 bucket 的权限为 Private(私有读写), 其他可选有公有读私有写, 公有读写
                createBucketRequest.setCannedAcl(CannedAccessControlList.Private);

                try{
                    cosClient.createBucket(createBucketRequest);
                } catch (CosClientException serverException) {
                    serverException.printStackTrace();
                }
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(cloudVo.getBucketName(), webPth, file);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            logger.info("上传文件" + id + " -- 结束：" + putObjectResult.getETag());
        } catch (Exception e) {
            throw new QianxuException(e.getMessage());
        }
    }

    @Override
    public void uploadFile(CloudVo cloudVo, String webPth, String localFile, File file, COSClient cosClient) {
        logger.info("上传文件开始：" + localFile);
        try {
            if(!file.exists()){
                logger.info("上传文件" + localFile + "不存在：");
                return;
            }

            if(!cosClient.doesBucketExist(cloudVo.getBucketName())){
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(cloudVo.getBucketName());
                // 设置 bucket 的权限为 Private(私有读写), 其他可选有公有读私有写, 公有读写
                createBucketRequest.setCannedAcl(CannedAccessControlList.Private);

                try{
                    cosClient.createBucket(createBucketRequest);
                } catch (CosClientException serverException) {
                    serverException.printStackTrace();
                }
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(cloudVo.getBucketName(), webPth, file);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            logger.info("上传文件 -- 结束：" + putObjectResult.getETag());
        } catch (Exception e) {
            throw new QianxuException(e.getMessage());
        }
    }
}

