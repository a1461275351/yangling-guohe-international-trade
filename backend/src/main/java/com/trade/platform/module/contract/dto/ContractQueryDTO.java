package com.trade.platform.module.contract.dto;

import lombok.Data;

@Data
public class ContractQueryDTO {

    private String contractNo;

    private String partnerName;

    private String status;

    private String startDate;

    private String endDate;

    private Long current = 1L;

    private Long size = 20L;
}
