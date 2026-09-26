package com.qxkj.admin.config;

import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.interceptor.SwaggerInterceptor;
import com.qxkj.admin.filter.ResponseFilter;
import com.qxkj.common.config.QianxuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.io.File;

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
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 这里使用一个Bean为的是可以在拦截器中自由注入，也可以在拦截器中使用SpringUtil.getBean 获取
    // 但是觉得这样更优雅

    @Autowired
    QianxuConfig qianxuConfig;

    @Autowired
    private com.qxkj.service.service.SystemConfigService systemConfigService;

    @Bean
    public ResponseFilter responseFilter(){ return new ResponseFilter(); }

    @Value("${swagger.basic.username}")
    private String username;
    @Value("${swagger.basic.password}")
    private String password;
    @Value("${swagger.basic.check}")
    private Boolean check;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加token拦截器
        //addPathPatterns添加需要拦截的命名空间；
        //excludePathPatterns添加排除拦截命名空间

        // 模块开关拦截：关闭的模块接口返回404
        registry.addInterceptor(new com.qxkj.service.interceptor.ModuleSwitchInterceptor(systemConfigService))
                .addPathPatterns("/api/admin/**");

//        //后台token拦截
//        registry.addInterceptor(adminTokenInterceptor()).
//                addPathPatterns("/api/admin/**").
//                excludePathPatterns("/api/admin/validate/**").
//                excludePathPatterns("/api/admin/login").
//                excludePathPatterns("/api/admin/logout").
//                excludePathPatterns("/api/admin/getLoginPic").
//                excludePathPatterns("/api/admin/wechat/config").
//                excludePathPatterns("/api/admin/authorize/login").
//                excludePathPatterns("/api/admin/payment/callback/**").
////                excludePathPatterns("/api/admin/system/role/menu").
//                excludePathPatterns("/api/admin/system/role/info").
//                excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
//
//        //后台权限规则
//        registry.addInterceptor(adminAuthInterceptor()).
//                addPathPatterns("/api/admin/**").
//                excludePathPatterns("/api/admin/validate/**").
//                excludePathPatterns("/api/admin/login").
//                excludePathPatterns("/api/admin/logout").
//                excludePathPatterns("/api/admin/getLoginPic").
//                excludePathPatterns("/api/admin/payment/callback/**").
//                excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        /** 本地文件上传路径 */
        registry.addResourceHandler(UploadConstants.UPLOAD_FILE_KEYWORD + "/**")
                .addResourceLocations("file:" + qianxuConfig.getImagePath() + "/" + UploadConstants.UPLOAD_FILE_KEYWORD + "/");

        registry.addResourceHandler(UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/**")
                .addResourceLocations("file:" +qianxuConfig.getImagePath() + "/" + UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/" );

        /** 主题导出下载路径 */
        registry.addResourceHandler("/theme/download/**")
                .addResourceLocations("file:" + qianxuConfig.getImagePath() + "/" + UploadConstants.UPLOAD_FILE_KEYWORD + "/theme/download/");

        /** 主题导入图片路径 */
        registry.addResourceHandler("/uploads/theme/**")
                .addResourceLocations("file:" + qianxuConfig.getImagePath() + "/uploads/theme/");

    }

    @Bean
    public FilterRegistrationBean filterRegister()
    {
        //注册过滤器
        FilterRegistrationBean registration = new FilterRegistrationBean(responseFilter());
        // 仅仅api前缀的请求才会拦截
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    /* 必须在此处配置拦截器,要不然拦不到swagger的静态资源 */
    @Bean
    @ConditionalOnProperty(name = "swagger.basic.enable", havingValue = "true")
    public MappedInterceptor getMappedInterceptor() {
        return new MappedInterceptor(new String[]{"/doc.html", "/webjars/**"}, new SwaggerInterceptor(username, password, check));
    }
}
