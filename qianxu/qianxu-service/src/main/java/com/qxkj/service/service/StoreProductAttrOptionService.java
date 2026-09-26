package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.product.StoreProductAttrOption;

import java.util.List;

public interface StoreProductAttrOptionService extends IService<StoreProductAttrOption> {

    /**
     * 商品变更导致的删除
     */
    Boolean deleteByProductUpdate(Integer proId);

    /**
     * 根据规格ID获取属性列表
     */
    List<StoreProductAttrOption> findListByAttrId(Integer attrId);

}
