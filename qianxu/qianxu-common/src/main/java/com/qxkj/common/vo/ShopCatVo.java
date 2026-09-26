package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class ShopCatVo {

    /** 错误码 */
    @TableField(value = "errcode")
    private Integer errCode;

    /** 错误信息 */
    @TableField(value = "errmsg")
    private Integer errMsg;

    /** 类目列表 */
    @TableField(value = "third_cat_list")
    private List<ShopCatDetailVo> thirdCatList;
}
