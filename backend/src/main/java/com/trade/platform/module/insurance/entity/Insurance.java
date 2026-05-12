package com.trade.platform.module.insurance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance")
public class Insurance extends BaseEntity {

    private Long tenantId;
    private String policyNo;
    private Long enterpriseId;
    private String buyerName;
    private String buyerCountry;
    private BigDecimal creditLimit;
    private BigDecimal insuredAmount;
    private BigDecimal premium;
    private LocalDate coverageStart;
    private LocalDate coverageEnd;
    private LocalDate shipmentDate;
    private BigDecimal shipmentAmount;
    private BigDecimal overdueAmount;
    private BigDecimal claimAmount;
    private String status;
    private String remark;
}
