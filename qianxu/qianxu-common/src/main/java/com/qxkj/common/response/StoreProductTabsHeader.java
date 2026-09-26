package com.qxkj.common.response;

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
public class StoreProductTabsHeader {
    private Integer count;
    private String name;
    private Integer type; // 1=出售中商品 2=仓库中商品 4=已经售罄商品 5=警戒库存 6=商品回收站

    public StoreProductTabsHeader() {
    }

    public StoreProductTabsHeader(Integer count, String name, Integer type) {
        this.count = count;
        this.name = name;
        this.type = type;
    }
}
