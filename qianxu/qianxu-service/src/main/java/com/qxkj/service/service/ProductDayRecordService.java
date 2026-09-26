package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.ProductRankingRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.record.ProductDayRecord;

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
public interface ProductDayRecordService extends IService<ProductDayRecord> {

    /**
     * 获取商品排行榜
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<ProductDayRecord> getRanking(ProductRankingRequest request);
}
