package com.qxkj.common.model.chain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_chain_audit_log")
@ApiModel(value = "ChainAuditLog", description = "链动关系审计日志")
public class ChainAuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String operator;
    private Integer userId;
    private String beforeVal;
    private String afterVal;
    private String reason;
    private Date createTime;
}
