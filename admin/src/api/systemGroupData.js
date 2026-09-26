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

export function groupDataDelete(pram) {
  const data = {
    id: pram.id,
  };
  return request({
    url: '/admin/system/group/data/delete',
    method: 'GET',
    params: data,
  });
}

export function groupDataInfo(pram) {
  const data = {
    id: pram.id,
  };
  return request({
    url: '/admin/system/group/data/info',
    method: 'GET',
    params: data,
  });
}

export function groupDataList(pram) {
  const data = {
    gid: pram.gid,
    keywords: pram.keywords,
    status: pram.status, // 1=开启 2=关闭
    page: pram.page,
    limit: pram.limit,
  };
  return request({
    url: '/admin/system/group/data/list',
    method: 'GET',
    params: data,
  });
}

export function groupDataSave(pram) {
  return request({
    url: '/admin/system/group/data/save',
    method: 'POST',
    data: pram,
  });
}

export function groupDataEdit(pram, id) {
  return request({
    url: '/admin/system/group/data/update',
    method: 'POST',
    data: pram,
    params: { id: id },
  });
}
