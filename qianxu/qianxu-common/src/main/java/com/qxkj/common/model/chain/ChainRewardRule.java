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
@TableName("eb_chain_reward_rule")
@ApiModel(value = "ChainRewardRule", description = "链动奖励规则")
public class ChainRewardRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String type;
    private Integer genLevel;
    private BigDecimal amount;
    private BigDecimal ratio;
    private Integer status;
    private String triggerEvent;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
