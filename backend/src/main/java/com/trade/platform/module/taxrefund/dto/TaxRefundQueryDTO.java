package com.trade.platform.module.taxrefund.dto;

import lombok.Data;

@Data
public class TaxRefundQueryDTO {
    private String refundNo;
    private Long enterpriseId;
    private String status;
    private String riskFlag;
    private Long current = 1L;
    private Long size = 20L;
}
