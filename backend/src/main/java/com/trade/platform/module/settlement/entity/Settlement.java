package com.trade.platform.module.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement")
public class Settlement extends BaseEntity {

    private Long tenantId;
    private String settlementNo;
    private String settlementType;
    private Long enterpriseId;
    private Long orderId;
    private Long customsDeclId;
    private String currency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal rmbAmount;
    private String bankName;
    private String bankAccount;
    private LocalDate paymentDate;
    private String status;
    private String remark;
}
