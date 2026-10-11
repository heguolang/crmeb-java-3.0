package com.qxkj.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChainCommissionExcelVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer userId;
    private Integer sourceUid;
    private String orderId;
    private String rewardType;
    private String amount;
    private String status;
    private String mark;
    private String createTime;
}
