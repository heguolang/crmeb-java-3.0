package com.qxkj.common.exception;

import com.qxkj.common.result.IResultEnum;

/**
 * @ClassName BusinessException
 * @Description 业务异常类
 * @Author HZW
 * @Date 2023/2/22 12:34
 * @Version 1.0
 */
public class BusinessException extends QianxuException {

    private static final long serialVersionUID = 1L;

    public BusinessException(IResultEnum iResultEnum) {
        super(iResultEnum);
    }

    public BusinessException(IResultEnum iResultEnum, Throwable throwable) {
        super(iResultEnum, throwable);
    }

}
