package com.qxkj.common.enums;

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
public enum MethodType {

    ADD("add", "增加"),
    DELETE("delete", "删除"),
    UPDATE("update", "修改"),
    EXPORT("export", "导出"),
    SELECT("query", "查询");

    private final String type;
    private final String name;

    MethodType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getName(String type){
        for (MethodType modelType : MethodType.values()) {
            if (modelType.getType().equals(type))
                return modelType.getName();
        }
        return null;
    }

    public static String getType(String name){
        for (MethodType modelType : MethodType.values()) {
            if (modelType.getName().equals(name))
                return modelType.getType();
        }
        return "-1";
    }
}
