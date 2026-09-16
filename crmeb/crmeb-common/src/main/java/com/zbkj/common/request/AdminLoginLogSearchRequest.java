package com.zbkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 管理员登录日志搜索请求对象
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_admin_login_log")
@ApiModel(value="AdminLoginLogSearchRequest对象", description="管理员登录日志搜索请求对象")
public class AdminLoginLogSearchRequest extends PageParamRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "登录账号")
    private String adminAccount;

    @ApiModelProperty(value = "状态 1成功 0失败, 不传为全部")
    private Integer status;

    @ApiModelProperty(value = "登录IP")
    private String ip;

    @ApiModelProperty(value = "时间范围")
    private String dateLimit;
}
