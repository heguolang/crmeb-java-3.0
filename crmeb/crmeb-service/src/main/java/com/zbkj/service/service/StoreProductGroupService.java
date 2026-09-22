package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.product.StoreProductGroup;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductGroupRequest;
import com.zbkj.common.request.StoreProductGroupSearchRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 商品分组服务
 */
public interface StoreProductGroupService extends IService<StoreProductGroup> {

    CommonPage<StoreProductGroup> getAdminList(StoreProductGroupSearchRequest request, PageParamRequest pageParamRequest);

    StoreProductGroup getInfo(Integer id);

    Boolean create(StoreProductGroupRequest request);

    Boolean edit(Integer id, StoreProductGroupRequest request);

    Boolean deleteGroup(Integer id);

    Boolean updateStatus(Integer id, Boolean status);

    HashMap<String, Object> getConfig();

    Boolean updateConfig(HashMap<String, Object> config);

    /** 启用中的分组列表（装修选分组用） */
    List<StoreProductGroup> getEnabledSimpleList();

    /** 按分组取商品 id */
    List<Integer> getProductIdsByGroupIds(List<Integer> groupIds);

    /**
     * 当前用户不可见的商品 id（用于列表过滤）。
     * 当全局「其它人能否看到」=1 时返回空集合（列表可见，详情/购买另拦）。
     */
    Set<Integer> getHiddenProductIds(User user);

    /** 用户是否有权查看该商品（含全局 other_visible 语义） */
    boolean canViewProduct(User user, Integer productId);

    /** 用户是否有权购买该商品（不受 other_visible 影响，无权限一律不可买） */
    boolean canBuyProduct(User user, Integer productId);

    /** 无权限提示语 */
    String getDenyTip();

    /** 校验购买数量（起卖数 / 限购一件），不通过抛异常 */
    void assertPurchaseAllowed(User user, Integer productId, Integer buyNum);

    /** 设置单个商品所属分组（全量覆盖） */
    Boolean bindProductGroups(Integer productId, List<Integer> groupIds);

    /** 批量将商品加入指定分组（追加，不移除已有其它分组） */
    Boolean batchBindProducts(List<Integer> productIds, List<Integer> groupIds);

    /** 查询商品所属分组id */
    List<Integer> getGroupIdsByProductId(Integer productId);
}
