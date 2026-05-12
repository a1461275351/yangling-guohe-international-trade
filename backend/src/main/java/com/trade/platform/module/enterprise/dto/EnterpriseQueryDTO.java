package com.trade.platform.module.enterprise.dto;

import lombok.Data;

@Data
public class EnterpriseQueryDTO {
    private String enterpriseName;
    private String creditCode;
    private String agreementStatus;
    private String riskLevel;
    private Integer status;
    private Long current = 1L;
    private Long size = 20L;
}
