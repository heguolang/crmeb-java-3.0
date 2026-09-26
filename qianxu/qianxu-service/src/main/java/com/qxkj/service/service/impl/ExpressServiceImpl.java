package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.exception.QianxuException;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.model.express.Express;
import com.qxkj.common.request.ExpressSearchRequest;
import com.qxkj.common.request.ExpressUpdateRequest;
import com.qxkj.common.request.ExpressUpdateShowRequest;
import com.qxkj.service.dao.ExpressDao;
import com.qxkj.service.service.ExpressService;
import org.springframework.beans.BeanUtils;
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
public class ExpressServiceImpl extends ServiceImpl<ExpressDao, Express> implements ExpressService {

    @Resource
    private ExpressDao dao;

    /**
     * 分页显示快递公司表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @Override
    public List<Express> getList(ExpressSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<Express> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(request.getKeywords())) {
            lambdaQueryWrapper.like(Express::getCode, request.getKeywords()).or().like(Express::getName, request.getKeywords());
        }
        // 排序：sort字段倒序，正常id正序，方便展示常用物流公司
        lambdaQueryWrapper.orderByDesc(Express::getSort);
        lambdaQueryWrapper.orderByAsc(Express::getId);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 编辑
     */
    @Override
    public Boolean updateExpress(ExpressUpdateRequest expressRequest) {
        Express temp = getById(expressRequest.getId());
        if (ObjectUtil.isNull(temp)) throw new QianxuException("快递公司不存在!");

        if (StrUtil.isBlank(expressRequest.getAccount()) && temp.getPartnerId().equals(true)) {
            throw new QianxuException("请输入月结账号");
        }
        if (StrUtil.isBlank(expressRequest.getPassword()) && temp.getPartnerKey().equals(true)) {
            throw new QianxuException("请输入月结密码");
        }
        if (StrUtil.isBlank(expressRequest.getNetName()) && temp.getNet().equals(true)) {
            throw new QianxuException("请输入取件网点");
        }
        Express express = new Express();
        BeanUtils.copyProperties(expressRequest, express);

        return updateById(express);
    }

    /**
     * 修改显示状态
     */
    @Override
    public Boolean updateExpressShow(ExpressUpdateShowRequest expressRequest) {
        Express temp = getById(expressRequest.getId());
        if (ObjectUtil.isNull(temp)) throw new QianxuException("编辑的记录不存在!");
        if (temp.getIsShow().equals(expressRequest.getIsShow())) {
            return Boolean.TRUE;
        }
        Express express = new Express();
        BeanUtils.copyProperties(expressRequest, express);
        return updateById(express);
    }

    /**
     * 同步物流公司（本地库维护，不再远程同步）
     */
    @Override
    public Boolean syncExpress() {
        return Boolean.TRUE;
    }

    /**
     * 查询全部物流公司
     * @param type 类型：normal-普通，elec-电子面单
     */
    @Override
    public List<Express> findAll(String type) {
        LambdaQueryWrapper<Express> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Express::getIsShow, true);
        if (type.equals("elec")) {
            lqw.eq(Express::getStatus, true);
        }
        lqw.orderByDesc(Express::getSort);
        lqw.orderByAsc(Express::getId);
        return dao.selectList(lqw);
    }

    /**
     * 查询物流公司面单模板（电子面单已停用）
     * @param com 快递公司编号
     */
    @Override
    public JSONObject template(String com) {
        JSONObject result = new JSONObject();
        result.put("data", new com.alibaba.fastjson.JSONArray());
        result.put("status", 200);
        return result;
    }

    @Override
    public JSONObject templateFor(String com, String type, String is_shipment, String page, String limit) {
        JSONObject result = new JSONObject();
        result.put("data", new com.alibaba.fastjson.JSONArray());
        result.put("status", 200);
        return result;
    }

    /**
     * 查询快递公司
     * @param code 快递公司编号
     */
    @Override
    public Express getByCode(String code) {
        LambdaQueryWrapper<Express> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Express::getCode, code);
        return dao.selectOne(lqw);
    }

    /**
     * 通过物流公司名称获取
     * @param name 物流公司名称
     */
    @Override
    public Express getByName(String name) {
        LambdaQueryWrapper<Express> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Express::getName, name);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 获取快递公司详情
     * @param id 快递公司id
     */
    @Override
    public Express getInfo(Integer id) {
        Express express = getById(id);
        if (ObjectUtil.isNull(express)) {
            throw new QianxuException("快递公司不存在");
        }
        return express;
    }
}
