package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.article.Article;
import com.qxkj.common.model.category.Category;
import com.qxkj.common.request.ArticleRequest;
import com.qxkj.common.request.ArticleSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.ArticleResponse;
import com.qxkj.common.vo.ArticleVo;
import com.github.pagehelper.PageInfo;

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
public interface ArticleService extends IService<Article> {

    /**
     * 文章列表
     * @param cid 文章分类id
     * @param pageParamRequest 分页类参数
     * @return PageInfo<Article>
     */
    PageInfo<ArticleResponse> getList(String cid, PageParamRequest pageParamRequest);

    /**
     * 获取文章列表
     * @param request 请求参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<ArticleVo> getAdminList(ArticleSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 文章详情
     * @param id 文章id
     * @return ArticleVo
     */
    ArticleResponse getVoByFront(Integer id);

    /**
     * 获取移动端banner列表
     * @return List<Article>
     */
    List<Article> getBannerList();

    /**
     * 获取移动端热门列表
     * @return List<ArticleResponse>
     */
    List<ArticleResponse> getHotList();

    /**
     * 获取文章分类列表
     * @return List<Category>
     */
    List<Category> getCategoryList();

    /**
     * 文章新增
     * @param articleRequest 文章新增参数
     * @return Boolean
     */
    Boolean create(ArticleRequest articleRequest);

    /**
     * 文章删除
     * @param id 文章id
     * @return Boolean
     */
    Boolean deleteById(Integer id);

    /**
     * 文章修改
     * @param id 文章id
     * @param articleRequest 文章修改参数
     */
    Boolean updateArticle(Integer id, ArticleRequest articleRequest);

    /**
     * 获取文章详情
     * @param id 文章id
     * @return Article
     */
    Article getDetail(Integer id);
}
