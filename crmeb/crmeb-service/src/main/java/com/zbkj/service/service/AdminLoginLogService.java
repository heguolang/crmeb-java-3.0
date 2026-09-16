package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.log.AdminLoginLog;
import com.zbkj.common.request.AdminLoginLogSearchRequest;

/**
 * 管理员登录日志 Service 接口
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
public interface AdminLoginLogService extends IService<AdminLoginLog> {

    /**
     * 添加登录日志
     * @param loginLog 日志信息
     */
    void addLog(AdminLoginLog loginLog);

    /**
     * 分页列表
     * @param request 搜索条件
     * @return PageInfo
     */
    PageInfo<AdminLoginLog> getPageList(AdminLoginLogSearchRequest request);

    /**
     * 删除日志
     * @param id 日志id
     * @return Boolean
     */
    Boolean deleteById(Integer id);

    /**
     * 清空全部登录日志
     * @return Boolean
     */
    Boolean clearAll();
}
