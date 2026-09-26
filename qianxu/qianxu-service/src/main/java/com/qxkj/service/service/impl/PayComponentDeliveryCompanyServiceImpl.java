package com.qxkj.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.model.express.Express;
import com.qxkj.common.model.wechat.video.PayComponentDeliveryCompany;
import com.qxkj.common.vo.DeliveryCompanyVo;
import com.qxkj.service.dao.PayComponentDeliveryCompanyDao;
import com.qxkj.service.service.ExpressService;
import com.qxkj.service.service.PayComponentDeliveryCompanyService;
import com.qxkj.service.service.WechatVideoDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class PayComponentDeliveryCompanyServiceImpl extends ServiceImpl<PayComponentDeliveryCompanyDao, PayComponentDeliveryCompany> implements PayComponentDeliveryCompanyService {

    @Resource
    private PayComponentDeliveryCompanyDao dao;

    @Autowired
    private WechatVideoDeliveryService wechatVideoDeliveryService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ExpressService expressService;

    private final String DELIVERY_COMPANY_LIST = "delivery_company_list";

    /**
     * 更新物流公司数据
     */
    @Override
    public void updateData() {
        List<DeliveryCompanyVo> companyVoList = wechatVideoDeliveryService.shopDeliveryGetCompanyList();
        List<PayComponentDeliveryCompany> companyList = companyVoList.stream().map(e -> {
            PayComponentDeliveryCompany company = new PayComponentDeliveryCompany();
            company.setDeliveryId(e.getDeliveryId());
            company.setDeliveryName(e.getDeliveryName());
            return company;
        }).collect(Collectors.toList());
        boolean saveBatch = saveBatch(companyList, 100);
        if (!saveBatch) {
            throw new QianxuException("批量更新物流公司错误");
        }
        System.out.println("批量更新物流公司成功");
    }

    /**
     * 获取组件物流公司列表
     * @return List
     */
    @Override
    public List<PayComponentDeliveryCompany> getList() {
        if (!redisUtil.exists(DELIVERY_COMPANY_LIST)) {
            LambdaQueryWrapper<PayComponentDeliveryCompany> lqw = Wrappers.lambdaQuery();
            lqw.select(PayComponentDeliveryCompany::getDeliveryId, PayComponentDeliveryCompany::getDeliveryName);
            lqw.eq(PayComponentDeliveryCompany::getIsDel, false);
            List<PayComponentDeliveryCompany> deliveryCompanyList = dao.selectList(lqw);
            // 为兼容现有程序，取现用物流公司交集
            List<Express> expressList = expressService.findAll("normal");
            List<PayComponentDeliveryCompany> companyList = deliveryCompanyList.stream().filter(e -> {
                boolean exist = false;
                for (Express express : expressList) {
                    if (express.getName().equals(e.getDeliveryName())) {
                        exist = true;
                        break;
                    }
                }
                return exist;
            }).collect(Collectors.toList());

            redisUtil.set(DELIVERY_COMPANY_LIST, JSONArray.toJSONString(companyList), 48L, TimeUnit.HOURS);
            return companyList;
        }
        String companyJsonStr = (String) redisUtil.get(DELIVERY_COMPANY_LIST);
        List<PayComponentDeliveryCompany> companyList = JSONArray.parseArray(companyJsonStr, PayComponentDeliveryCompany.class);
        return companyList;
    }

    /**
     * 通过快递公司ID获取
     * @param deliveryId 快递公司ID
     * @return PayComponentDeliveryCompany
     */
    @Override
    public PayComponentDeliveryCompany getByDeliveryId(String deliveryId) {
        LambdaQueryWrapper<PayComponentDeliveryCompany> lqw = Wrappers.lambdaQuery();
        lqw.select(PayComponentDeliveryCompany::getDeliveryId, PayComponentDeliveryCompany::getDeliveryName);
        lqw.eq(PayComponentDeliveryCompany::getIsDel, false);
        lqw.eq(PayComponentDeliveryCompany::getDeliveryId, deliveryId);
        return dao.selectOne(lqw);
    }
}

