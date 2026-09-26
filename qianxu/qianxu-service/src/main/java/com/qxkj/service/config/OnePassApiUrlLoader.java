package com.qxkj.service.config;

import cn.hutool.core.util.StrUtil;
import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.constants.OnePassConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 将开放平台 API 地址从配置注入常量，避免源码硬编码第三方域名。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OnePassApiUrlLoader {

    @Autowired
    private QianxuConfig qianxuConfig;

    @PostConstruct
    public void load() {
        String url = qianxuConfig.getOnePassApiUrl();
        if (StrUtil.isBlank(url)) {
            return;
        }
        OnePassConstants.ONE_PASS_API_URL = url.endsWith("/") ? url : url + "/";
    }
}
