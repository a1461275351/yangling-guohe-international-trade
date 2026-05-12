package com.trade.platform.module.customs.dto;

import lombok.Data;

@Data
public class CustomsQueryDTO {

    private String declarationNo;

    private String ieType;

    private String startDate;

    private String endDate;

    private String transportMode;

    private String status;

    private Long contractId;

    private Long orderId;

    private Long enterpriseId;

    private Long current = 1L;

    private Long size = 20L;
}
