package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.system.SystemAttachment;
import com.qxkj.common.request.SystemAttachmentMoveRequest;
import com.qxkj.common.request.SystemAttachmentRequest;

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
public interface SystemAttachmentService extends IService<SystemAttachment> {

    /**
     * 同步到云服务， 更新图片上传类型
     * @param attId Integer 主键id
     * @param type int 图片上传类型 1本地 2七牛云 3OSS 4COS
     */
    void updateCloudType(Integer attId, int type);

    /**
     * 附件分页
     * @param pid Integer pid
     * @param attType 格式png,jpeg,jpg,audio/mpeg,text/plain,video/mp4,gif
     * @param pageParamRequest PageParamRequest 分页参数
     * @return List<SystemAttachment>
     */
    List<SystemAttachment> getList(Integer pid, String attType, PageParamRequest pageParamRequest);

    /**
     * 给图片加前缀
     * @param path String 路径
     * @return String
     */
    String prefixImage(String path);

    /**
     * 给前端上传的文件加前缀
     * @param path 路径
     * @return 替换后的
     */
    String prefixUploadf(String path);

    /**
     * 给文件加前缀
     * @param path String 路径
     * @return String
     */
    String prefixFile(String path);

    /**
     * 清除 cdn url， 在保存数据的时候使用
     * @param path String 文件路径
     * @return String
     */
    String clearPrefix(String path);

    /**
     * 清除 cdn url， 在保存数据的时候使用
     * @param path String 文件路径
     * @param cdnUrl String cdnUrl
     * @return String
     */
    String clearPrefix(String path, String cdnUrl);

    /**
     * 新增附件
     * @param systemAttachmentRequest 新增参数
     */
    Boolean add(SystemAttachmentRequest systemAttachmentRequest);

    /**
     * 编辑附件
     * @param systemAttachmentRequest 更新参数
     */
    Boolean edit(SystemAttachmentRequest systemAttachmentRequest);

    /**
     * 更改图片目录
     * @param move 参数
     */
    Boolean updateAttrId(SystemAttachmentMoveRequest move);

    /**
     * 获取cdn url
     * @return String
     */
    String getCdnUrl();

    /**
     * 删除附件
     * @param idList 附件ID列表
     */
    Boolean deleteByIds(List<Integer> idList);
}
