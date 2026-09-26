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
public class ShopSpuAuditVo {

    /** 上一次提交时间, yyyy-MM-dd HH:mm:ss */
    @TableField(value = "submit_time")
    private String submitTime;

    /** 上一次审核时间, yyyy-MM-dd HH:mm:ss */
    @TableField(value = "audit_time")
    private String auditTime;

    /** 拒绝理由，只有edit_status为3时出现 */
    @TableField(value = "reject_reason")
    private String rejectReason;

    /** 审核单id */
    @TableField(value = "audit_id")
    private String auditId;

}
