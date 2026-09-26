package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class ShopOrderAddressInfoAddVo {

    /** 收件人姓名 */
    @TableField(value = "receiver_name")
    private String receiverName;

    /** 详细收货地址信息 */
    @TableField(value = "detailed_address")
    private String detailedAddress;

    /** 收件人手机号码 */
    @TableField(value = "tel_number")
    private String telNumber;

    /** 国家 */
    private String country;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 乡镇 */
    private String town;
}
