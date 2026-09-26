package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.model.system.SystemCity;
import com.qxkj.common.vo.SystemCityTreeVo;
import com.qxkj.service.dao.SystemCityDao;
import com.qxkj.service.service.SystemCityAsyncService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SystemCityAsyncServiceImpl extends ServiceImpl<SystemCityDao, SystemCity> implements SystemCityAsyncService {

    @Resource
    private SystemCityDao dao;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 设置属性列表进入redis
     */
    @Override
    public void setListTree() {
        //循环数据，把数据对象变成带list结构的vo
        List<SystemCityTreeVo> treeList = new ArrayList<>();

        LambdaQueryWrapper<SystemCity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(SystemCity::getId, SystemCity::getCityId, SystemCity::getParentId, SystemCity::getName);
        lambdaQueryWrapper.eq(SystemCity::getIsShow, true);
        List<SystemCity> allTree = dao.selectList(lambdaQueryWrapper);
        if (CollUtil.isEmpty(allTree)) {
            return;
        }

        for (SystemCity systemCity : allTree) {
            SystemCityTreeVo systemCityTreeVo = new SystemCityTreeVo();
            BeanUtils.copyProperties(systemCity, systemCityTreeVo);
            treeList.add(systemCityTreeVo);
        }
        //返回
        Map<Integer, SystemCityTreeVo> map = new HashMap<>();
        //cityId 为 key 存储到map 中
        for (SystemCityTreeVo systemCityTreeVo1 : treeList) {
            map.put(systemCityTreeVo1.getCityId(), systemCityTreeVo1);
        }
        List<SystemCityTreeVo> list = new ArrayList<>();
        for (SystemCityTreeVo tree : treeList) {
            //子集ID返回对象，有则添加。
            SystemCityTreeVo tree1 = map.get(tree.getParentId());
            if (tree1 != null) {
                tree1.getChild().add(tree);
            } else {
                list.add(tree);
            }
        }
        redisUtil.set(Constants.CITY_LIST_TREE, list);
    }

    /**
     * 数据整体刷入redis
     */
    private void asyncList(Integer pid) {
        LambdaQueryWrapper<SystemCity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SystemCity::getParentId, pid);
        lambdaQueryWrapper.in(SystemCity::getIsShow, true);
        List<SystemCity> systemCityList = dao.selectList(lambdaQueryWrapper);
        if (systemCityList != null && systemCityList.size() > 0) {
            redisUtil.hset(Constants.CITY_LIST, pid.toString(), systemCityList);
        }
    }

    /**
     * 数据整体刷入redis
     */
    @Async
    public void async(Integer pid) {
        asyncList(pid);
        setListTree();
    }
}

