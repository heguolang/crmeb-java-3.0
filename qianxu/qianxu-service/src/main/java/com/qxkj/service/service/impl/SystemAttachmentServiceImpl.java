package com.qxkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.model.system.SystemAttachment;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemAttachmentMoveRequest;
import com.qxkj.common.request.SystemAttachmentRequest;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.service.dao.SystemAttachmentDao;
import com.qxkj.service.service.SystemAttachmentService;
import com.qxkj.service.service.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SystemAttachmentServiceImpl extends ServiceImpl<SystemAttachmentDao, SystemAttachment>
        implements SystemAttachmentService {

    @Resource
    private SystemAttachmentDao dao;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 同步到云服务， 更新图片上传类型
     * @param attId Integer 主键id
     * @param type int 图片上传类型 1本地 2七牛云 3OSS 4COS
     */
    @Override
    public void updateCloudType(Integer attId, int type) {
        SystemAttachment systemAttachment = new SystemAttachment();
        systemAttachment.setAttId(attId);
        systemAttachment.setImageType(type);
        systemAttachment.setUpdateTime(DateUtil.date());
        updateById(systemAttachment);
    }

    /**
     * 附件分页
     * @param pid Integer pid
     * @param pageParamRequest PageParamRequest 分页参数
     * @return List<SystemAttachment>
     */
    @Override
    public List<SystemAttachment> getList(Integer pid, String attType, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<SystemAttachment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SystemAttachment::getPid, pid);
        if(StringUtils.isNotEmpty(attType)){
            lambdaQueryWrapper.in(SystemAttachment::getAttType, QianxuUtil.stringToArrayStr(attType));
        }
        lambdaQueryWrapper.orderByDesc(SystemAttachment::getAttId);
        return dao.selectList(lambdaQueryWrapper);
    }

    /**
     * 给图片加前缀
     * @param path String 路径
     * @return String
     */
    @Override
    public String prefixImage(String path) {
        // 如果那些域名不需要加，则跳过
        return prefixRelativePath(path, UploadConstants.UPLOAD_FILE_KEYWORD + "/", getCdnUrl());
    }

    @Override
    public String prefixUploadf(String path) {
        // 如果那些域名不需要加，则跳过
        return prefixRelativePath(path, UploadConstants.UPLOAD_FILE_KEYWORD + "/" + UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/", getCdnUrl());
    }

    /**
     * 给文件加前缀
     * @param path String 路径
     * @return String
     */
    @Override
    public String prefixFile(String path) {
        if (path.contains(Constants.WECHAT_SOURCE_CODE_FILE_NAME)) {
            String cdnUrl = systemConfigService.getValueByKey("local" + "UploadUrl");
            return prefixRelativePath(path, UploadConstants.UPLOAD_FILE_KEYWORD + "/", cdnUrl);
        }
        if (path.contains("downloadf/excel")) {
            String cdnUrl = systemConfigService.getValueByKey("local" + "UploadUrl");
            return prefixRelativePath(path, UploadConstants.UPLOAD_FILE_KEYWORD + "/downloadf/", cdnUrl);
        }
        return prefixRelativePath(path, UploadConstants.UPLOAD_FILE_KEYWORD + "/file/", getCdnUrl());
    }

    private String prefixRelativePath(String data, String pathKeyword, String prefix) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(pathKeyword) || StringUtils.isBlank(prefix)) {
            return data;
        }

        String normalizedPrefix = trimEndSlash(prefix);
        String replacement = normalizedPrefix + "/" + pathKeyword;
        StringBuilder builder = new StringBuilder(data.length());
        int searchStart = 0;
        int index = data.indexOf(pathKeyword);
        while (index >= 0) {
            if (isCompleteUrlPath(data, index)) {
                builder.append(data, searchStart, index).append(pathKeyword);
            } else if (hasRelativeStartSlash(data, index)) {
                builder.append(data, searchStart, index - 1).append(replacement);
            } else {
                builder.append(data, searchStart, index).append(replacement);
            }
            searchStart = index + pathKeyword.length();
            index = data.indexOf(pathKeyword, searchStart);
        }
        builder.append(data.substring(searchStart));
        return builder.toString();
    }

    private boolean hasRelativeStartSlash(String data, int pathIndex) {
        return pathIndex > 0
                && data.charAt(pathIndex - 1) == '/'
                && (pathIndex == 1 || isUrlBoundaryChar(data.charAt(pathIndex - 2)));
    }

    private boolean isCompleteUrlPath(String data, int pathIndex) {
        if (pathIndex <= 0 || data.charAt(pathIndex - 1) != '/') {
            return false;
        }
        return hasSchemeBeforePath(data, pathIndex, "http://")
                || hasSchemeBeforePath(data, pathIndex, "https://")
                || hasProtocolRelativeBeforePath(data, pathIndex);
    }

    private boolean hasSchemeBeforePath(String data, int pathIndex, String scheme) {
        int schemeIndex = data.lastIndexOf(scheme, pathIndex);
        return schemeIndex >= 0 && !hasUrlBoundary(data, schemeIndex, pathIndex);
    }

    private boolean hasProtocolRelativeBeforePath(String data, int pathIndex) {
        int tokenStart = findUrlTokenStart(data, pathIndex);
        return pathIndex - tokenStart > 2 && data.startsWith("//", tokenStart);
    }

    private int findUrlTokenStart(String data, int pathIndex) {
        for (int i = pathIndex - 1; i >= 0; i--) {
            if (isUrlBoundaryChar(data.charAt(i))) {
                return i + 1;
            }
        }
        return 0;
    }

    private boolean hasUrlBoundary(String data, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (isUrlBoundaryChar(data.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isUrlBoundaryChar(char value) {
        return value == '"' || value == '\'' || value == '<' || value == '>' || Character.isWhitespace(value);
    }

    private String trimEndSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 清除 cdn url， 在保存数据的时候使用
     * @param path String 文件路径
     * @return String
     */
    @Override
    public String clearPrefix(String path) {
        if(StringUtils.isBlank(path)){
            return path;
        }
        Constants.CND_URL = getCdnUrl();
        if(path.contains(getCdnUrl() + "/")){
            if (path.contains("callback/alipay")) {
                return path;
            }
            return path.replace(getCdnUrl() + "/", "");
        }

        return path;
    }

    /**
     * 清除 cdn url， 在保存数据的时候使用
     *
     * @param path String 文件路径
     * @return String
     */
    @Override
    public String clearPrefix(String path, String cdnUrl) {
        if (StrUtil.isBlank(path)) {
            return path;
        }
        if (path.contains(cdnUrl + "/")) {
            return path.replace(cdnUrl + "/", "");
        }

        return path;
    }

    /**
     * 新增附件
     * @param systemAttachmentRequest 新增参数
     */
    @Override
    public Boolean add(SystemAttachmentRequest systemAttachmentRequest) {
        SystemAttachment systemAttachment = new SystemAttachment();
        BeanUtils.copyProperties(systemAttachmentRequest, systemAttachment);
        return save(systemAttachment);
    }

    /**
     * 编辑附件
     * @param systemAttachmentRequest 更新参数
     */
    @Override
    public Boolean edit(SystemAttachmentRequest systemAttachmentRequest) {
        SystemAttachment systemAttachment = new SystemAttachment();
        BeanUtils.copyProperties(systemAttachmentRequest, systemAttachment);
        systemAttachment.setUpdateTime(DateUtil.date());
        return updateById(systemAttachment);
    }

    /**
     * 更改图片目录
     * @param move 参数
     */
    @Override
    public Boolean updateAttrId(SystemAttachmentMoveRequest move) {
        LambdaUpdateWrapper<SystemAttachment> lup = new LambdaUpdateWrapper<>();
        lup.in(SystemAttachment::getAttId, QianxuUtil.stringToArray(move.getAttrId()));
        lup.set(SystemAttachment::getPid, move.getPid());
        return update(lup);
    }

    /**
     * 获取cdn url
     *
     * @return String
     */
    @Override
    public String getCdnUrl() {
        String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
        //获取配置信息
        int type = Integer.parseInt(uploadType);
        String uploadUrl = SysConfigConstants.CONFIG_LOCAL_UPLOAD_URL;
        switch (type) {
            case 2:
                uploadUrl = SysConfigConstants.CONFIG_QN_UPLOAD_URL;
                break;
            case 3:
                uploadUrl = SysConfigConstants.CONFIG_AL_UPLOAD_URL;
                break;
            case 4:
                uploadUrl = SysConfigConstants.CONFIG_TX_UPLOAD_URL;
                break;
            case 5:
                uploadUrl = SysConfigConstants.CONFIG_JD_UPLOAD_URL;
                break;
            default:
                break;
        }
        return systemConfigService.getValueByKey(uploadUrl);
    }

    /**
     * 删除附件
     * @param idList 附件ID列表
     */
    @Override
    public Boolean deleteByIds(List<Integer> idList) {
        LambdaUpdateWrapper<SystemAttachment> wrapper = Wrappers.lambdaUpdate();
        wrapper.in(SystemAttachment::getAttId, idList);
        return remove(wrapper);
    }
}
