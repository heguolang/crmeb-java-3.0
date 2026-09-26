package com.qxkj.front.filter;

import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.utils.SpringUtil;
import com.qxkj.service.service.SystemAttachmentService;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

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
public class ResponseRouter {

    public String filter(String data, String path, QianxuConfig qianxuConfig) {
        boolean result = un().contains(path);
        if (result) {
            return data;
        }

        //系统不用过滤的URL，针对数据而非token
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String configUrl : qianxuConfig.getIgnored()) {
            if(pathMatcher.match(path, configUrl)){
                return data;
            }
        }

        if (!path.contains("api/admin/") && !path.contains("api/front/")) {
            return data;
        }

        //根据需要处理返回值
        if ((data.contains(UploadConstants.UPLOAD_FILE_KEYWORD+"/") && !data.contains("data:image/png;base64"))
                || data.contains(UploadConstants.DOWNLOAD_FILE_KEYWORD) || data.contains(UploadConstants.UPLOAD_AFTER_FILE_KEYWORD)) {
            if (data.contains(UploadConstants.DOWNLOAD_FILE_KEYWORD+"/"+Constants.UPLOAD_MODEL_PATH_EXCEL)) {
                data = SpringUtil.getBean(SystemAttachmentService.class).prefixFile(data);
            } else if(data.contains(UploadConstants.UPLOAD_AFTER_FILE_KEYWORD+"/"))  {
                data = SpringUtil.getBean(SystemAttachmentService.class).prefixUploadf(data);
            } else {
                data = SpringUtil.getBean(SystemAttachmentService.class).prefixImage(data);
            }
        }

        return data;
    }

    public static String un() {
        return "";
    }
}
