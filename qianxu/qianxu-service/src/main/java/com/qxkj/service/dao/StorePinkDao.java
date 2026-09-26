package com.qxkj.service.dao;

import com.qxkj.common.model.combination.StorePink;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
public interface StorePinkDao extends BaseMapper<StorePink> {

    List<StorePink> selectSizePink(Integer size);

    List<StorePink> selectPinkList(HashMap<String, Object> map);

    Integer selectPinkListNum(HashMap<String, Object> map);
}
