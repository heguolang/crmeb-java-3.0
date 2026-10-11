package com.qxkj.common.model.chain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_chain_commission")
@ApiModel(value = "ChainCommission", description = "链动佣金流水")
public class ChainCommission implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer sourceUid;
    private String orderId;
    private String rewardType;
    private BigDecimal amount;
    private Integer status;
    private Integer brokerageRecordId;
    private String mark;
    private Date createTime;
    private Date updateTime;
}
