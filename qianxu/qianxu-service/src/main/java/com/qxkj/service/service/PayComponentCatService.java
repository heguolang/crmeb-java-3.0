package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.wechat.video.PayComponentCat;
import com.qxkj.common.vo.CatItem;

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
public interface PayComponentCatService extends IService<PayComponentCat> {

    /**
     * 自动更新自定义交易组件类目
     */
    void autoUpdate();

    /**
     * 获取类目
     * @return List<FirstCatVo>
     */
    List<CatItem> getList();

    /**
     * 根据第三级id获取类目
     * @param thirdCatId 第三级id
     * @return PayComponentCat
     */
    PayComponentCat getByThirdCatId(Integer thirdCatId);
}
