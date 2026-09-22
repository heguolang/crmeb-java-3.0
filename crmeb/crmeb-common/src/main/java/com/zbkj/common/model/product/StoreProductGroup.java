package com.zbkj.common.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 商城商品分组
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_store_product_group")
@ApiModel(value = "StoreProductGroup", description = "商城商品分组")
public class StoreProductGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PERM_ALL = "all";
    public static final String PERM_PROMOTER = "promoter";
    public static final String PERM_AGENT = "agent";
    public static final String PERM_STOCK_AGENT = "stock_agent";

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "分组名称")
    private String name;

    @ApiModelProperty(value = "权限：all/promoter/agent/stock_agent")
    private String permissionType;

    @ApiModelProperty(value = "会员分组id，逗号分隔")
    private String userGroupIds;

    @ApiModelProperty(value = "会员等级id，逗号分隔")
    private String userLevelIds;

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

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "商品数量")
    private Integer productCount;

    @TableField(exist = false)
    @ApiModelProperty(value = "关联商品id列表")
    private List<Integer> productIds;
}
