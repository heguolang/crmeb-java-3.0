package com.qxkj.common.exception;

import com.qxkj.common.result.CommonResultCode;
import com.qxkj.common.result.IResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
@Slf4j
@RestControllerAdvice
public class QianxuException extends RuntimeException {

    private static final long serialVersionUID = 6397082987802748517L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public QianxuException() {
        super();
    }

    public QianxuException(String message) {
        super(CommonResultCode.ERROR.getCode() + "-" + message);
        this.code = CommonResultCode.ERROR.getCode();
        this.message = message;
    }

    public QianxuException(IResultEnum iResultEnum) {
        super(iResultEnum.getCode() + "-" + iResultEnum.getMessage());
        this.code = iResultEnum.getCode();
        this.message = iResultEnum.getMessage();
    }

    public QianxuException(IResultEnum iResultEnum, String message) {
        super(iResultEnum.getCode() + "-" + message);
        this.code = iResultEnum.getCode();
        this.message = message;
    }

    public QianxuException(IResultEnum iResultEnum, Throwable throwable) {
        super(iResultEnum.getCode() + "-" + iResultEnum.getMessage() + ", " + throwable.getMessage(), throwable);
        this.code = iResultEnum.getCode();
        this.message = iResultEnum.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
