package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.express.ShippingTemplates;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.ShippingTemplatesRegionRequest;
import com.qxkj.common.request.ShippingTemplatesRequest;
import com.qxkj.common.request.ShippingTemplatesSearchRequest;
import com.qxkj.common.response.ShippingTemplatesFreeResponse;
import com.qxkj.common.response.ShippingTemplatesInfoResponse;
import com.qxkj.common.response.ShippingTemplatesRegionResponse;
import com.qxkj.service.dao.ShippingTemplatesDao;
import com.qxkj.service.service.ShippingTemplatesFreeService;
import com.qxkj.service.service.ShippingTemplatesRegionService;
import com.qxkj.service.service.ShippingTemplatesService;
import com.qxkj.service.service.StoreProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
public class ShippingTemplatesServiceImpl extends ServiceImpl<ShippingTemplatesDao, ShippingTemplates> implements ShippingTemplatesService {

    @Resource
    private ShippingTemplatesDao dao;

    @Autowired
    private ShippingTemplatesRegionService shippingTemplatesRegionService;

    @Autowired
    private ShippingTemplatesFreeService shippingTemplatesFreeService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private StoreProductService storeProductService;

    /**
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<ShippingTemplates>
     * @author Mr.Zhang
     * @since 2020-04-17
     */
    @Override
    public List<ShippingTemplates> getList(ShippingTemplatesSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ShippingTemplates> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isBlank(request.getKeywords())) {
            lambdaQueryWrapper.like(ShippingTemplates::getName, request.getKeywords());
        }
        lambdaQueryWrapper.orderByDesc(ShippingTemplates::getSort).orderByDesc(ShippingTemplates::getId);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 新增
     *
     * @param request 新增参数
     * @return Boolean
     */
    @Override
    public Boolean create(ShippingTemplatesRequest request) {
        // 判断模板名称是否重复
        if (isExistName(request.getName())) {
            throw new QianxuException("模板名称已存在,请更换模板名称!");
        }
        List<ShippingTemplatesRegionRequest> shippingTemplatesRegionRequestList = request.getShippingTemplatesRegionRequestList();
        if (request.getAppoint().equals(2) && CollUtil.isEmpty(shippingTemplatesRegionRequestList)) {
            throw new QianxuException("不包邮，最少需要一条公共区域运费数据");
        }

        ShippingTemplates shippingTemplates = new ShippingTemplates();
        shippingTemplates.setName(request.getName());
        shippingTemplates.setSort(request.getSort());
        shippingTemplates.setType(request.getType());
        shippingTemplates.setAppoint(request.getAppoint());

        return transactionTemplate.execute(e -> {
            boolean save = save(shippingTemplates);
            if (!save) {
                e.setRollbackOnly();
                return Boolean.FALSE;
            }
            if (!shippingTemplates.getAppoint().equals(0) && CollUtil.isNotEmpty(shippingTemplatesRegionRequestList)) {
                save = shippingTemplatesRegionService.saveAll(shippingTemplatesRegionRequestList, shippingTemplates.getType(), shippingTemplates.getId());
                if (!save) {
                    e.setRollbackOnly();
                    return Boolean.FALSE;
                }
            }
            if (shippingTemplates.getAppoint().equals(2) && CollUtil.isNotEmpty(request.getShippingTemplatesFreeRequestList())) {
                save = shippingTemplatesFreeService.saveAll(request.getShippingTemplatesFreeRequestList(), shippingTemplates.getType(), shippingTemplates.getId());
                if (!save) {
                    e.setRollbackOnly();
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        });
    }

    /**
     * 根据模板名称获取模板
     *
     * @param name 模板名称
     * @return ShippingTemplates
     */
    private ShippingTemplates getByName(String name) {
        LambdaQueryWrapper<ShippingTemplates> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShippingTemplates::getName, name);
        return dao.selectOne(lqw);
    }

    /**
     * 是否存在模板名称
     */
    private Boolean isExistName(String name) {
        ShippingTemplates templates = getByName(name);
        if (ObjectUtil.isNull(templates)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 新增
     *
     * @param id      Integer 模板id
     * @param request ShippingTemplatesRequest 新增参数
     */
    @Override
    public Boolean update(Integer id, ShippingTemplatesRequest request) {
        ShippingTemplates shippingTemplates = getByIdException(id);
        List<ShippingTemplatesRegionRequest> shippingTemplatesRegionRequestList = request.getShippingTemplatesRegionRequestList();
        if (request.getAppoint().equals(2) && CollUtil.isEmpty(shippingTemplatesRegionRequestList)) {
            throw new QianxuException("不包邮，最少需要一条公共区域运费数据");
        }
        if (!shippingTemplates.getName().equals(request.getName())) {
            if (isExistName(request.getName())) {
                throw new QianxuException("模板名称已存在,请更换模板名称!");
            }
        }

        shippingTemplates.setName(request.getName());
        shippingTemplates.setSort(request.getSort());
        shippingTemplates.setType(request.getType());

        return transactionTemplate.execute(e -> {
            if (!shippingTemplates.getAppoint().equals(0)) {
                shippingTemplatesRegionService.deleteByTempId(id);
            }
            if (shippingTemplates.getAppoint().equals(2)) {
                shippingTemplatesFreeService.deleteByTempId(id);
            }
            shippingTemplates.setAppoint(request.getAppoint());
            shippingTemplates.setUpdateTime(DateUtil.date());
            boolean update = updateById(shippingTemplates);
            if (!update) {
                e.setRollbackOnly();
                return Boolean.FALSE;
            }
            if (CollUtil.isNotEmpty(shippingTemplatesRegionRequestList)) {
                update = shippingTemplatesRegionService.saveAll(shippingTemplatesRegionRequestList, request.getType(), shippingTemplates.getId());
                if (!update) {
                    e.setRollbackOnly();
                    return Boolean.FALSE;
                }
            }
            if (shippingTemplates.getAppoint().equals(2) && CollUtil.isNotEmpty(request.getShippingTemplatesFreeRequestList())) {
                update = shippingTemplatesFreeService.saveAll(request.getShippingTemplatesFreeRequestList(), request.getType(), shippingTemplates.getId());
                if (!update) {
                    e.setRollbackOnly();
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        });
    }

    /**
     * 删除
     *
     * @param id Integer
     * @return boolean
     */
    @Override
    public Boolean remove(Integer id) {
        getByIdException(id);
        if (storeProductService.isUseShippingTemplateId(id)) {
            throw new QianxuException("有商品使用此运费模板，无法删除");
        }
        return transactionTemplate.execute(e -> {
            shippingTemplatesRegionService.deleteByTempId(id);
            shippingTemplatesFreeService.deleteByTempId(id);
            removeById(id);
            return Boolean.TRUE;
        });
    }

    /**
     * 获取模板信息
     *
     * @param id 模板id
     * @return ShippingTemplates
     */
    @Override
    public ShippingTemplatesInfoResponse getInfo(Integer id) {
        ShippingTemplates shippingTemplates = getByIdException(id);
        ShippingTemplatesInfoResponse response = new ShippingTemplatesInfoResponse();
        BeanUtils.copyProperties(shippingTemplates, response);
        if (shippingTemplates.getAppoint().equals(0)) {
            return response;
        }
        List<ShippingTemplatesRegionResponse> regionResponseList = shippingTemplatesRegionService.getListGroup(id);
        if (CollUtil.isNotEmpty(regionResponseList)) {
            regionResponseList.forEach(regionResponse -> {
                regionResponse.setTitle("[" + regionResponse.getTitle() + "]");
            });
            response.setRegionList(regionResponseList);
        }
        if (shippingTemplates.getAppoint().equals(2)) {
            List<ShippingTemplatesFreeResponse> freeResponseList = shippingTemplatesFreeService.getListGroup(id);
            freeResponseList.forEach(freeResponse -> {
                freeResponse.setTitle("[" + freeResponse.getTitle() + "]");
            });
            response.setFreeList(freeResponseList);
        }
        return response;
    }

    private ShippingTemplates getByIdException(Integer id) {
        ShippingTemplates shippingTemplates = getById(id);
        if (ObjectUtil.isNull(shippingTemplates)) {
            throw new QianxuException("运费模板不存在");
        }
        return shippingTemplates;
    }


}

