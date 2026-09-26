package com.qxkj.front.filter;


import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


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
//@Component
public class ResponseFilter implements Filter {

    @Autowired
    QianxuConfig qianxuConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        ResponseWrapper wrapperResponse = new ResponseWrapper((HttpServletResponse) response);//转换成代理类
        // 这里只拦截返回，直接让请求过去，如果在请求前有处理，可以在这里处理
        filterChain.doFilter(request, wrapperResponse);
        byte[] content = wrapperResponse.getContent();//获取返回值
        //判断是否有值
        if (content.length > 0) {
            String str = new String(content, StandardCharsets.UTF_8);

            try {
                HttpServletRequest req = (HttpServletRequest) request;
                str = new ResponseRouter().filter(str, RequestUtil.getUri(req), qianxuConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //把返回值输出到客户端
            ServletOutputStream outputStream = response.getOutputStream();
            if (str.length() > 0) {
                outputStream.write(str.getBytes());
                outputStream.flush();
                outputStream.close();
                //最后添加这一句，输出到客户端
                response.flushBuffer();
            }
        }
    }
}
