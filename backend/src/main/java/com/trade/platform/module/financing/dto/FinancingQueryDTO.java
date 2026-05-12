package com.trade.platform.module.financing.dto;

import lombok.Data;

@Data
public class FinancingQueryDTO {
    private String financingNo;
    private Long enterpriseId;
    private String financingType;
    private String status;
    private Long current = 1L;
    private Long size = 20L;
}
