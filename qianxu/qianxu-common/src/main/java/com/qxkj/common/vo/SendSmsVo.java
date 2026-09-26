package com.qxkj.common.vo;

import lombok.Data;

/**
 * 短信发送参数
 */
@Data
public class SendSmsVo {
    /** 待发送短信手机号 */
    private String mobile;

    /** 阿里云模板 CODE */
    private String template;

    /** 模板参数 JSON */
    private String param;

    private String content;
}
