package com.trade.platform.module.insurance.dto;

import lombok.Data;

@Data
public class InsuranceQueryDTO {
    private String policyNo;
    private Long enterpriseId;
    private String buyerCountry;
    private String status;
    private Long current = 1L;
    private Long size = 20L;
}
