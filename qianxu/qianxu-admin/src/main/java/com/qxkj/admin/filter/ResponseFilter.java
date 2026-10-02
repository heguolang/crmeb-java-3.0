package com.qxkj.admin.filter;


import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.FilterInvocation;

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
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        ResponseWrapper wrapperResponse = new ResponseWrapper((HttpServletResponse) response);//转换成代理类
        FilterInvocation fi = new FilterInvocation(servletRequest, wrapperResponse, filterChain);
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        //OPTIONS请求直接放行
        if(request.getMethod().equals(HttpMethod.OPTIONS.toString())){
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            return;
        }
        // 这里只拦截返回，直接让请求过去，如果在请求前有处理，可以在这里处理
        filterChain.doFilter(servletRequest, wrapperResponse);
        byte[] content = wrapperResponse.getContent();//获取返回值
        //判断是否有值
        if (content.length > 0) {
            // 二进制响应（图片/PDF/压缩包等）必须原样回写。
            // 把 byte[] 当 UTF-8 字符串往返，非法字节会被替换成 U+FFFD（EF BF BD），
            // PNG 的 0x89 签名一变浏览器直接拒绝解码 —— 表现为「底图/图片裂了」。
            if (isBinaryContent(wrapperResponse.getContentType())) {
                ServletOutputStream rawOut = response.getOutputStream();
                rawOut.write(content);
                rawOut.flush();
                return;
            }
            String str = new String(content, StandardCharsets.UTF_8);

            try {
                HttpServletRequest req = (HttpServletRequest) servletRequest;
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

    /**
     * 是否为二进制响应类型。
     * <p>只识别「明确的二进制 MIME」，其余（含 contentType 为空）一律按文本走原有的 URL 改写逻辑，
     * 保证既有行为零变化。</p>
     */
    private static boolean isBinaryContent(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        String ct = contentType.toLowerCase();
        return ct.startsWith("image/") || ct.startsWith("video/") || ct.startsWith("audio/")
                || ct.startsWith("font/")
                || ct.contains("octet-stream") || ct.contains("/pdf") || ct.contains("zip")
                || ct.contains("vnd.") || ct.contains("protobuf");
    }
}
