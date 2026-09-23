package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品分组保存/编辑请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StoreProductGroupRequest", description = "商品分组请求")
public class StoreProductGroupRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分组名称", required = true)
    @NotBlank(message = "分组名称不能为空")
    private String name;

    @ApiModelProperty(value = "权限：all全部会员/promoter仅分销商/agent仅区域代理/stock_agent仅订货商/team仅社群团队", required = true)
    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    @ApiModelProperty(value = "会员分组id列表")
    private List<Integer> userGroupIds;

    @ApiModelProperty(value = "会员等级id列表")
    private List<Integer> userLevelIds;

    @ApiModelProperty(value = "分销商等级id列表")
    private List<Integer> distributorLevelIds;

    @ApiModelProperty(value = "区域代理等级列表（1=省代 2=市代 3=区代）")
    private List<Integer> agentLevelIds;

    @ApiModelProperty(value = "订货商等级id列表（eb_stock_level）")
    private List<Integer> stockLevelIds;

    @ApiModelProperty(value = "社群团队等级id列表（eb_system_team_level）")
    private List<Integer> teamLevelIds;

    @ApiModelProperty(value = "是否仅限所选等级")
    private Boolean levelOnly;

    @ApiModelProperty(value = "起卖数")
    private Integer minBuy;

    @ApiModelProperty(value = "是否限购一件")
    private Boolean limitOne;

    @ApiModelProperty(value = "布局 double/single")
    private String layout;

    @ApiModelProperty(value = "内容样式")
    private Integer style;

    @ApiModelProperty(value = "图片角标")
    private String badge;

    @ApiModelProperty(value = "标题多行")
    private Boolean titleMulti;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "状态")
    private Boolean status;

    @ApiModelProperty(value = "关联商品id列表")
    private List<Integer> productIds;
}
