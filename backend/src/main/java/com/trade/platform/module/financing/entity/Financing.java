package com.trade.platform.module.financing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_financing")
public class Financing extends BaseEntity {

    private Long tenantId;
    private String financingNo;
    private Long enterpriseId;
    private String financingType;
    private String bankName;
    private BigDecimal applyAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private LocalDate applyDate;
    private LocalDate approveDate;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private BigDecimal repaidAmount;
    private String status;
    private String remark;
}
