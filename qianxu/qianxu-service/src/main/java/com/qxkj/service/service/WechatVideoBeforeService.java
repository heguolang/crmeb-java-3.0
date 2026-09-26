package com.qxkj.service.service;

import com.qxkj.common.request.ShopUploadImgRequest;
import com.qxkj.common.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

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
public interface WechatVideoBeforeService {
    // 获取类目详情 在其他商品中实现了

    /**
     * 上传图片
     * @return 图片上传结果
     */
    WechatVideoUploadImageResponseVo shopImgUpload(ShopUploadImgRequest request);

    /**
     * 上传品牌信息
     * @param requestVo 待上传品牌参数
     * @return 审核单Id
     */
    ShopAuditBrandResponseVo shopAuditBrand(ShopAuditBrandRequestVo requestVo);

    /**
     * 上传类目资质
     * @param requestVo 类目资质参数
     * @return 类目资质结果
     */
    ShopAuditCategoryResponseVo shopAuditCategory(ShopAuditCategoryRequestVo requestVo);

    /**
     * 获取类目审核结果
     * @param request 待审核类目id
     * @return 审核结果
     */
    ShopAuditResultResponseVo shopAuditResult(HashMap<String,String> request);

    // 待完善
    void shopAuditGetMinCertificate(HashMap<String,String> request);


    WechatVideoUploadImageResponseVo shopImgUploadTest(MultipartFile file, Integer respType, Integer uploadType, String imgUrl);
}
