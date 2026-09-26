package com.qxkj.common.vo;

import lombok.Data;

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
@Data
public class CatItem {

    private Integer value;

    private String label;

    private String qualification;

    private Integer qualificationType;

    private String productQualification;

    private Integer productQualificationType;

    private List<CatItem> children;

    public CatItem() {
    }

    public CatItem(Integer value, String label, List<CatItem> childrens) {
        this.value = value;
        this.label = label;
        this.children = childrens;
    }
}
