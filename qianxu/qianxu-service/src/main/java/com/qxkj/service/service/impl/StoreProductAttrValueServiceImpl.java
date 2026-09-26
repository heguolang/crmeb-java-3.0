package com.qxkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.product.StoreProductAttrValue;
import com.qxkj.service.dao.StoreProductAttrValueDao;
import com.qxkj.service.service.StoreProductAttrValueService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@Service
public class StoreProductAttrValueServiceImpl extends ServiceImpl<StoreProductAttrValueDao, StoreProductAttrValue>
        implements StoreProductAttrValueService {

    @Resource
    private StoreProductAttrValueDao dao;

    /**
     *
     * @param productId 商品id
     * @param attrId 属性id
     * @return 商品属性集合
     */
    @Override
    public List<StoreProductAttrValue> getListByProductIdAndAttrId(Integer productId, String attrId, Integer type) {
        LambdaQueryWrapper<StoreProductAttrValue> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(StoreProductAttrValue::getProductId, productId);
        lambdaQueryWrapper.eq(StoreProductAttrValue::getType, type);
        if(null != attrId){
            lambdaQueryWrapper.eq(StoreProductAttrValue::getId, attrId);
        }
        lambdaQueryWrapper.eq(StoreProductAttrValue::getIsDel, false);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据产品属性查询
     * @param storeProductAttrValue 商品属性参数
     * @return 查询到的属性结果
     */
    @Override
    public List<StoreProductAttrValue> getByEntity(StoreProductAttrValue storeProductAttrValue) {
        LambdaQueryWrapper<StoreProductAttrValue> lqw = new LambdaQueryWrapper<>();
        lqw.setEntity(storeProductAttrValue);
        return dao.selectList(lqw);
    }

    /**
     * 根据商品id删除AttrValue
     * @param productId 商品id
     * @param type 类型区分是是否添加营销
     * @reture 删除结果
     */
    @Override
    public boolean removeByProductId(Integer productId,int type) {
        LambdaQueryWrapper<StoreProductAttrValue> lambdaQW = Wrappers.lambdaQuery();
        lambdaQW.eq(StoreProductAttrValue::getProductId, productId).eq(StoreProductAttrValue::getType,type);
        return dao.delete(lambdaQW) > 0;
    }

    /**
     * 根据id、类型查询
     * @param id ID
     * @param type 类型
     * @return StoreProductAttrValue
     */
    @Override
    public StoreProductAttrValue getByIdAndProductIdAndType(Integer id, Integer productId, Integer type) {
        LambdaQueryWrapper<StoreProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductAttrValue::getId, id);
        lqw.eq(StoreProductAttrValue::getProductId, productId);
        lqw.eq(StoreProductAttrValue::getType, type);
        lqw.eq(StoreProductAttrValue::getIsDel, false);
        return dao.selectOne(lqw);
    }

    /**
     * 根据sku查询
     * @param productId 商品id
     * @param suk   sku
     * @param type  规格类型
     * @return StoreProductAttrValue
     */
    @Override
    public StoreProductAttrValue getByProductIdAndSkuAndType(Integer productId, String suk, Integer type) {
        LambdaQueryWrapper<StoreProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductAttrValue::getProductId, productId);
        lqw.eq(StoreProductAttrValue::getSuk, suk);
        lqw.eq(StoreProductAttrValue::getType, type);
        lqw.eq(StoreProductAttrValue::getIsDel, false);
        return dao.selectOne(lqw);
    }

    /**
     * 添加(退货)/扣减库存
     * @param id 秒杀商品id
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @param type 活动类型 0=商品，1=秒杀，2=砍价，3=拼团
     * @param version 版本号
     * @return Boolean
     */
    @Override
    public Boolean operationStock(Integer id, Integer num, String operationType, Integer type, Integer version) {
        UpdateWrapper<StoreProductAttrValue> updateWrapper = new UpdateWrapper<>();
        if (operationType.equals("quick_add")) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
        }
        if (operationType.equals("add")) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales - {}", num));
            if (type > 0) {
                updateWrapper.setSql(StrUtil.format("quota = quota + {}", num));
            }
        }
        if (operationType.equals("sub")) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales + {}", num));
            if (type > 0) {
                updateWrapper.setSql(StrUtil.format("quota = quota - {}", num));
                // 扣减时加乐观锁保证库存不为负
                updateWrapper.last(StrUtil.format("and (quota - {} >= 0)", num));
            } else {
                // 扣减时加乐观锁保证库存不为负
                updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
            }
        }
        updateWrapper.setSql("version = version + 1");
        updateWrapper.eq("id", id);
        updateWrapper.eq("type", type);
        updateWrapper.eq("version", version);
        boolean update = update(updateWrapper);
        if (!update) {
            throw new QianxuException("更新商品attrValue失败，attrValueId = " + id);
        }
        return update;
    }

    @Override
    public Boolean operationStock(Integer id, Integer num, String operationType, Integer version) {
        UpdateWrapper<StoreProductAttrValue> updateWrapper = new UpdateWrapper<>();
        if (operationType.equals("quick_add")) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
        }
        if (operationType.equals("add")) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales - {}", num));
        }
        if (operationType.equals("sub")) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales + {}", num));

        }
        updateWrapper.setSql("version = version + 1");
        updateWrapper.eq("id", id);
        updateWrapper.eq("version", version);
        boolean update = update(updateWrapper);
        if (!update) {
            throw new QianxuException("更新商品attrValue失败，attrValueId = " + id);
        }
        return update;
    }

    /**
     * 删除商品规格属性值
     * @param productId 商品id
     * @param type 商品类型
     * @return Boolean
     */
    @Override
    public Boolean deleteByProductIdAndType(Integer productId, Integer type) {
        LambdaUpdateWrapper<StoreProductAttrValue> luw = Wrappers.lambdaUpdate();
        luw.set(StoreProductAttrValue::getIsDel, true);
        luw.eq(StoreProductAttrValue::getProductId, productId);
        luw.eq(StoreProductAttrValue::getType, type);
        return update(luw);
    }

    /**
     * 获取商品规格列表
     * @param productId 商品id
     * @param type 商品类型
     * @return List
     */
    @Override
    public List<StoreProductAttrValue> getListByProductIdAndType(Integer productId, Integer type) {
        LambdaQueryWrapper<StoreProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductAttrValue::getProductId, productId);
        lqw.eq(StoreProductAttrValue::getType, type);
        lqw.eq(StoreProductAttrValue::getIsDel, false);
        lqw.orderByAsc(StoreProductAttrValue::getId);
        return dao.selectList(lqw);
    }

    /**
     * 根据商品id和attrIdList获取 商品属性列表集合
     * @param productId 商品id
     * @param attrIdList 属性idList
     * @return 商品属性集合
     */
    @Override
    public List<StoreProductAttrValue> getByProductIdAndAttrIdList(Integer productId, List<Integer> attrIdList) {
        LambdaQueryWrapper<StoreProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(StoreProductAttrValue::getProductId, productId);
        lqw.in(StoreProductAttrValue::getId, attrIdList);
        lqw.eq(StoreProductAttrValue::getIsDel, false);
        return dao.selectList(lqw);
    }
}

