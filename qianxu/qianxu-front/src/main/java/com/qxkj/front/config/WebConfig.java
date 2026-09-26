package com.qxkj.front.config;

import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.interceptor.SwaggerInterceptor;
import com.qxkj.front.filter.ResponseFilter;
import com.qxkj.front.interceptor.FrontTokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

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

    @Bean
    public HandlerInterceptor frontTokenInterceptor(){
        return new FrontTokenInterceptor();
    }

    @Bean
    public ResponseFilter responseFilter(){ return new ResponseFilter(); }

    @org.springframework.beans.factory.annotation.Autowired
    private com.qxkj.service.service.SystemConfigService systemConfigService;

    @Value("${swagger.basic.username}")
    private String username;
    @Value("${swagger.basic.password}")
    private String password;
    @Value("${swagger.basic.check}")
    private Boolean check;

    /** 服务器图片根路径（qianxuimage 的上级目录，斜杠结尾） */
    @Value("${qianxu.imagePath}")
    private String imagePath;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加token拦截器
        //addPathPatterns添加需要拦截的命名空间；
        //excludePathPatterns添加排除拦截命名空间

        // 模块开关拦截：关闭的模块接口返回404
        registry.addInterceptor(new com.qxkj.service.interceptor.ModuleSwitchInterceptor(systemConfigService))
                .addPathPatterns("/api/front/**");

        //前端用户登录token
        registry.addInterceptor(frontTokenInterceptor()).
                addPathPatterns("/api/front/**").
                excludePathPatterns("/api/front/index/**").
                excludePathPatterns("/api/front/qrcode/**").
                excludePathPatterns("/api/front/login/mobile").
                excludePathPatterns("/api/front/login").
                excludePathPatterns("/api/front/register").
                excludePathPatterns("/api/front/sendCode").
                excludePathPatterns("/api/front/wechat/**").
                excludePathPatterns("/api/front/search/keyword").
                excludePathPatterns("/api/front/share").
                excludePathPatterns("/api/front/article/**").
                excludePathPatterns("/api/front/city/**").
                excludePathPatterns("/api/front/product/hot").
                excludePathPatterns("/api/front/product/good").
                excludePathPatterns("/api/front/products/**").
                excludePathPatterns("/api/front/reply/**").
                excludePathPatterns("/api/front/user/service/**").
                excludePathPatterns("/api/front/logistics").
                excludePathPatterns("/api/front/groom/list/**").
                excludePathPatterns("/api/front/config").
                excludePathPatterns("/api/front/category").
                excludePathPatterns("/api/front/categorybypid/*").
                excludePathPatterns("/api/front/ios/*").
                excludePathPatterns("/api/front/ios/register/binding/phone").
                excludePathPatterns("/api/front/index/product/*").
                excludePathPatterns("/api/front/index/color/config").
                excludePathPatterns("/api/front/image/domain").
                excludePathPatterns("/api/front/product/leaderboard").
                excludePathPatterns("/api/front/product/byids/**").
                excludePathPatterns("/api/front/token/is/exist").
                excludePathPatterns("/api/front/login/config").
                excludePathPatterns("/api/front/copyright/info").
                excludePathPatterns("/api/front/get/bottom/navigation").
                excludePathPatterns("/api/front/agreement/**").
                excludePathPatterns("/api/front/pagediy/**").
                excludePathPatterns("/api/front/theme/product").
                excludePathPatterns("/api/front/theme/coupon").
                excludePathPatterns("/api/front/theme/combination").
                excludePathPatterns("/api/front/theme/bargain").
                excludePathPatterns("/api/front/theme/article").
                excludePathPatterns("/api/front/theme_info").
                excludePathPatterns("/api/front/theme_info/**").
                excludePathPatterns("/api/front/menu/user").
                excludePathPatterns("/api/front/user/sign/config").
                excludePathPatterns("/api/front/product/group/**").

                excludePathPatterns("/api/front/seckill/**").

                excludePathPatterns("/api/front/bargain/index").
                excludePathPatterns("/api/front/bargain/header").
                excludePathPatterns("/api/front/bargain/list").
                excludePathPatterns("/api/front/bargain/detail/*").

                excludePathPatterns("/api/front/combination/index").
                excludePathPatterns("/api/front/combination/list").
                excludePathPatterns("/api/front/combination/header").
                excludePathPatterns("/api/front/combination/detail/*").

                excludePathPatterns("/api/front/splash/ad/info").
                excludePathPatterns("/api/front/hidden/switches").

                excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        /** 本地文件上传路径 qianxuimage */
        registry.addResourceHandler(UploadConstants.UPLOAD_FILE_KEYWORD + "/**")
                .addResourceLocations("file:" + imagePath + "/" + UploadConstants.UPLOAD_FILE_KEYWORD + "/");

        /** 前端上传后下载路径 uploadf */
        registry.addResourceHandler(UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/**")
                .addResourceLocations("file:" + imagePath + "/" + UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/");

        /** 文件导出下载路径 downloadf */
        registry.addResourceHandler(UploadConstants.DOWNLOAD_FILE_KEYWORD + "/**")
                .addResourceLocations("file:" + imagePath + "/" + UploadConstants.DOWNLOAD_FILE_KEYWORD + "/");
    }

    @Bean
    public FilterRegistrationBean filterRegister()
    {
        //注册过滤器
        FilterRegistrationBean registration = new FilterRegistrationBean(responseFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    /* 必须在此处配置拦截器,要不然拦不到swagger的静态资源 */
    @Bean
    @ConditionalOnProperty(name = "swagger.basic.enable", havingValue = "true")
    public MappedInterceptor getMappedInterceptor() {
        return new MappedInterceptor(new String[]{"/doc.html", "/webjars/**"}, new SwaggerInterceptor(username, password, check));
    }
}
