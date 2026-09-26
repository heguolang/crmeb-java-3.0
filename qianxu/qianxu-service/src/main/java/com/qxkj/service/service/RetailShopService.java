package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.RetailShopRequest;
import com.qxkj.common.request.RetailShopSearchRequest;
import com.qxkj.common.response.SpreadUserResponse;
import com.qxkj.common.model.user.User;

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
public interface RetailShopService extends IService<User> {

    /**
     * 分销员列表
     *
     * @return CommonPage
     */
    PageInfo<SpreadUserResponse> getSpreadPeopleList(RetailShopSearchRequest request);

    /**
     * 获取分销配置
     * @return 分销配置信息
     */
    RetailShopRequest getManageInfo();

    /**
     * 保存或者更新分销配置信息
     * @param retailShopRequest 待保存数据
     * @return 保存结果
     */
    boolean setManageInfo(RetailShopRequest retailShopRequest);

}
