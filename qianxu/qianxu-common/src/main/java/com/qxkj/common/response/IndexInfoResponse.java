package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="IndexInfoResponse对象", description="用户登录返回数据")
public class IndexInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "首页banner滚动图")
    private List<HashMap<String, Object>> banner;

    @ApiModelProperty(value = "导航模块")
    private List<HashMap<String, Object>> menus;

    @ApiModelProperty(value = "新闻简报消息滚动")
    private List<HashMap<String, Object>> roll;

    @ApiModelProperty(value = "企业logo")
    private String logoUrl;

    @ApiModelProperty(value = "是否关注公众号")
    private boolean subscribe;

    @ApiModelProperty(value = "首页超值爆款")
    private List<HashMap<String, Object>> explosiveMoney;

    @ApiModelProperty(value = "首页精品推荐图片")
    private List<HashMap<String, Object>> bastBanner;

    @ApiModelProperty(value = "云智服H5 url")
    private String yzfUrl;

    @ApiModelProperty(value = "商品分类页配置")
    private String categoryPageConfig;

    @ApiModelProperty(value = "是否隐藏一级分类")
    private String isShowCategory;

    @ApiModelProperty(value = "客服电话")
    private String consumerHotline;

    @ApiModelProperty(value = "客服电话服务开关")
    private String telephoneServiceSwitch;

    @ApiModelProperty(value = "首页商品列表模板配置")
    private String homePageSaleListStyle;

    @ApiModelProperty(value = "微信小程序客服额外开关")
    private String wxChatIndependent;
}
