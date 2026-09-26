package com.qxkj.service.service;

import com.qxkj.common.vo.FileResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
public interface UploadService {

    /**
     * 图片上传
     * @param multipartFile 文件
     * @param model 模块 用户user,商品product,微信wechat,news文章
     * @param pid 分类ID 0编辑器,1商品图片,2拼团图片,3砍价图片,4秒杀图片,5文章图片,6组合数据图,7前台用户,8微信系列
     * @return FileResultVo
     */
    FileResultVo imageUpload(MultipartFile multipartFile, String model, Integer pid) throws IOException;

    /**
     * 文件长传
     * @param multipartFile 文件
     * @param model 模块 用户user,商品product,微信wechat,news文章
     * @param pid 分类ID 0编辑器,1商品图片,2拼团图片,3砍价图片,4秒杀图片,5文章图片,6组合数据图,7前台用户,8微信系列
     * @return FileResultVo
     */
    FileResultVo fileUpload(MultipartFile multipartFile, String model, Integer pid) throws IOException;
}
