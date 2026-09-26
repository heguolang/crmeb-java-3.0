package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.category.Category;
import com.qxkj.common.request.CategoryRequest;
import com.qxkj.common.request.CategorySearchRequest;
import com.qxkj.common.vo.CategoryTreeVo;

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
public interface CategoryService extends IService<Category> {

    List<Category> getList(CategorySearchRequest request);

    int delete(Integer id);

    /**
     * 获取树形结构数据
     * @param type 分类
     * @param status 状态
     * @param name 名称
     * @return List
     */
    List<CategoryTreeVo> getListTree(Integer type, Integer status, String name);

    /**
     * 获取树形结构数据
     * @param type 分类
     * @param status 状态
     * @param categoryIdList 分类idList
     * @return List
     */
    List<CategoryTreeVo> getListTree(Integer type, Integer status, List<Integer> categoryIdList);

    List<Category> getByIds(List<Integer> ids);

    /**
     * 获取父级id集合
     * @param idList
     * @return
     */
    List<Category> getByPIds(List<Integer> idList);

    HashMap<Integer, String> getListInId(List<Integer> cateIdList);

    Boolean checkAuth(List<Integer> pathIdList, String uri);

    boolean update(CategoryRequest request, Integer id);

    List<Category> getChildVoListByPid(Integer pid);
    /**
     * 获取分类下子类时判断当前分类有效
     */
    List<Category> getChildVoStatusOnListByPid(Integer pid);

    boolean checkUrl(String uri);

    boolean updateStatus(Integer id);

    /**
     * 新增分类表
     */
    Boolean create(CategoryRequest categoryRequest);

    /**
     * 获取文章分类列表
     * @return List<Category>
     */
    List<Category> findArticleCategoryList();
}
