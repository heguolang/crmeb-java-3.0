package com.qxkj.common.utils;


import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.exception.QianxuException;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class UploadWeChatMediaUtil {
    /**
     * 把文件上传到指定url上去
     * @param url 上传地址
     * @param file 待上传文件
     */
    public static JSONObject uploadFile(String url, InputStream file, String fileName) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);

            /**
             * 这一步最关键：
             *调用addBinaryBody("media",input2byte(file),ContentType.DEFAULT_BINARY, fileName),
             * fileName可以为任意值，但不能为null，如果为null则上传失败。
             * input2byte(file)：把inputstream转为byte[]，
             * 如果直接调用addPart用FileBody做参数，则MultifilePart不好转换；
             * 如果直接调用addPart用InpustreamBody做参数，则因为没有fileName会造成上传失败
             */
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("media", input2byte(file), ContentType.DEFAULT_BINARY,
                            fileName).build();

            httppost.setEntity(reqEntity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String responseEntityStr = EntityUtils.toString(response.getEntity());
                    return JSONObject.parseObject(responseEntityStr);
                }
                EntityUtils.consume(resEntity);
            }catch (Exception e){
                e.printStackTrace();
                throw new QianxuException(e.getMessage());
            }finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new QianxuException(e.getMessage());
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                throw new QianxuException(e.getMessage());
            }
        }

        return null;
    }


    private static byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }
}

