// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import request from '@/utils/request';

/**
 * 绑定产品
 * @param pram
 */
export function bindProduct(pram) {
  const data = {
    id: pram.id,
    productId: pram.productId,
  };
  return request({
    url: '/admin/article/bind/product',
    method: 'POST',
    params: data,
  });
}

/**
 * 删除文章
 * @param id
 * @constructor
 */
export function DelArticle(pram) {
  const data = {
    id: pram.id,
  };
  return request({
    url: '/admin/article/delete',
    method: 'GET',
    params: data,
  });
}

/**
 * 文章详情
 * @param id
 * @constructor
 */
export function InfoArticle(id) {
  const data = {
    id: id,
  };
  return request({
    url: '/admin/article/info',
    method: 'get',
    params: data,
  });
}

/**
 * 文章列表
 * @param pram
 * @constructor
 */
export function ListArticle(pram) {
  const data = {
    keywords: pram.keywords,
    cid: pram.cid,
    page: pram.page,
    limit: pram.limit,
  };
  return request({
    url: '/admin/article/list',
    method: 'GET',
    params: data,
  });
}

/**
 * 新增文章
 * @param pram
 * @constructor
 */
export function AddArticle(pram) {
  const data = {
    author: pram.author,
    cid: pram.cid,
    content: pram.content,
    imageInput: pram.imageInput,
    isBanner: pram.isBanner,
    isHot: pram.isHot,
    shareSynopsis: pram.shareSynopsis,
    shareTitle: pram.shareTitle,
    sort: pram.sort,
    synopsis: pram.synopsis,
    title: pram.title,
    url: pram.url,
  };
  return request({
    url: '/admin/article/save',
    method: 'post',
    data: data,
  });
}

/**
 * 更新文章
 * @param pram
 * @constructor
 */
export function UpdateArticle(pram) {
  const data = {
    author: pram.author,
    cid: pram.cid,
    content: pram.content,
    imageInput: pram.imageInput,
    isBanner: pram.isBanner,
    isHot: pram.isHot,
    shareSynopsis: pram.shareSynopsis,
    shareTitle: pram.shareTitle,
    sort: pram.sort,
    synopsis: pram.synopsis,
    title: pram.title,
    url: pram.url,
  };
  return request({
    url: '/admin/article/update',
    method: 'post',
    params: { id: pram.id },
    data: data,
  });
}
