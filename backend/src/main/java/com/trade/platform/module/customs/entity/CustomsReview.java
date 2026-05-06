package com.trade.platform.module.customs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_customs_review")
public class CustomsReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long declarationId;

    /** 操作类型: SUBMIT/APPROVE/REJECT/RELEASE/REVOKE */
    private String action;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String operatorName;

    private String comment;

    private LocalDateTime createTime;
}
