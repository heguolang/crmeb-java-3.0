package com.qxkj.common.utils;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

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
public class AESUtil {

    public static boolean initialized = false;

    /**
     * AES解密
     *
     * @param content
     *            密文
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     */
    public byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws InvalidAlgorithmParameterException {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// 初始化
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initialize() {
        if (initialized)
            return;
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }

    // 生成iv
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }
}
