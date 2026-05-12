package com.trade.platform.module.taxrefund.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trade.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_tax_refund")
public class TaxRefund extends BaseEntity {

    private Long tenantId;
    private String refundNo;
    private Long enterpriseId;
    private Long orderId;
    private Long customsDeclId;
    private Long contractId;
    private String invoiceNo;
    private BigDecimal invoiceAmount;
    private BigDecimal refundRate;
    private BigDecimal refundAmount;
    private BigDecimal actualRefund;
    private LocalDate applyDate;
    private LocalDate approveDate;
    private LocalDate refundDate;
    private String status;
    private String riskFlag;
    private String riskRemark;
    private String remark;
}
