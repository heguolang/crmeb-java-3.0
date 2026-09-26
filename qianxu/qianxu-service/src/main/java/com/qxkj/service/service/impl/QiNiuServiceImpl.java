package com.qxkj.service.service.impl;

import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.vo.CloudVo;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qxkj.service.service.QiNiuService;
import com.qxkj.service.service.SystemAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
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
public class QiNiuServiceImpl implements QiNiuService {
    private static final Logger logger = LoggerFactory.getLogger(QiNiuServiceImpl.class);

    @Lazy
    @Autowired
    private SystemAttachmentService systemAttachmentService;

    /**
     * 同步到七牛云
     * @param cloudVo CloudVo
     * @author Mr.Zhang
     * @since 2020-05-06
     */
    @Async
    @Override
    public void upload(UploadManager uploadManager, CloudVo cloudVo, String upToken, String webPth, String localFile, Integer id) {
        try {
            logger.info("上传文件" + id + "开始：" + localFile);

            File file = new File(localFile);
            if(!file.exists()){
                logger.info("上传文件"+ id + localFile + "不存在：");
                return;
            }

            Response put = uploadManager.put(localFile, webPth, upToken);
            put.close();
            logger.info("上传文件" + id + " -- 结束：" + put.address);
            //更新数据库
            systemAttachmentService.updateCloudType(id, 2);
        } catch (QiniuException ex) {
            throw new QianxuException(ex.getMessage());
        }
    }

    @Override
    public void uploadFile(UploadManager uploadManager, String upToken, String webPth, String localFile, File file) {
        try {
            if(!file.exists()){
                logger.error("上传文件" + localFile + "不存在：");
                return;
            }
            Response put = uploadManager.put(file, webPth, upToken);
            put.close();
        } catch (QiniuException ex) {
            throw new QianxuException(ex.getMessage());
        }
    }
}

